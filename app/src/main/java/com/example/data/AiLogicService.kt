package com.example.data

import android.util.Log
import com.example.model.StructuredIntent
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject

data class ExtractedCapabilityTags(
    val skills: List<String>,
    val knowledgeAreas: List<String>,
    val practicalAbilities: List<String>,
    val interests: List<String>,
    val supportWanted: List<String>
)

data class MissionRequirementSuggestion(
    val interpretedIntent: String,
    val intentDomain: String,
    val essentialCapabilities: List<String>,
    val optionalCapabilities: List<String>,
    val suggestedActivities: List<String>
)

class AiLogicService {
    companion object {
        private const val TAG = "AiLogicService"
        private const val MODEL_NAME = "gemini-3.8-flash"
        private const val TIMEOUT_MS = 45_000L
    }

    private val capabilityExtractorModel by lazy {
        val schema = Schema.obj(
            properties = mapOf(
                "skills" to Schema.array(Schema.string(), "Concrete technical or creative skills mentioned"),
                "knowledgeAreas" to Schema.array(Schema.string(), "Academic, domain, or lived knowledge areas"),
                "practicalAbilities" to Schema.array(Schema.string(), "Hands-on or coordination abilities"),
                "interests" to Schema.array(Schema.string(), "Topics the person is curious about"),
                "supportWanted" to Schema.array(Schema.string(), "Areas where the person would welcome collaboration")
            )
        )
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val intentInterpreterModel by lazy {
        val schema = Schema.obj(
            properties = mapOf(
                "mainObjective" to Schema.string("Clear statement of underlying purpose regardless of wording"),
                "broaderPurpose" to Schema.string("Broader societal or community purpose"),
                "problemDomain" to Schema.string("Domain label such as Local Environmental Stewardship"),
                "intendedOutcome" to Schema.string("Concrete outcome desired"),
                "activities" to Schema.array(Schema.string(), "Key activities involved"),
                "requiredCapabilities" to Schema.array(Schema.string(), "Essential capabilities needed"),
                "optionalCapabilities" to Schema.array(Schema.string(), "Helpful optional capabilities"),
                "semanticConcepts" to Schema.array(Schema.string(), "3-5 semantic concept tags")
            )
        )
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            }
        )
    }

    private val explanationModel by lazy {
        Firebase.ai.generativeModel(modelName = MODEL_NAME)
    }

    suspend fun extractCapabilityTags(naturalDescription: String): Result<ExtractedCapabilityTags> {
        return try {
            val prompt = """
                You are assisting a user on SYMBIOSIS, a dignified human capability-matching platform.
                Never infer medical diagnoses or assume limitations. Extract only positive, self-described
                strengths, knowledge, interests, and areas where they explicitly mention wanting support or learning.
                Keep each tag concise (1-4 words).
                User description:
                "$naturalDescription"
            """.trimIndent()

            val response = withTimeout(TIMEOUT_MS) {
                capabilityExtractorModel.generateContent(prompt)
            }
            val raw = response.text ?: return Result.failure(IllegalStateException("Gemini returned an empty response."))
            val json = JSONObject(raw)
            Result.success(
                ExtractedCapabilityTags(
                    skills = json.optStringList("skills"),
                    knowledgeAreas = json.optStringList("knowledgeAreas"),
                    practicalAbilities = json.optStringList("practicalAbilities"),
                    interests = json.optStringList("interests"),
                    supportWanted = json.optStringList("supportWanted")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "extractCapabilityTags timed out", e)
            Result.failure(IllegalStateException("AI tag extraction timed out after 45 seconds."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "extractCapabilityTags failed", e)
            Result.failure(e)
        }
    }

    suspend fun interpretMissionIntent(
        title: String,
        description: String,
        objective: String = ""
    ): Result<StructuredIntent> {
        return try {
            val prompt = """
                Interpret the underlying human intention behind this mission for semantic discovery.
                Focus on what the person means and hopes to achieve, not just exact keywords.
                Title: "$title"
                Description: "$description"
                Objective: "$objective"
            """.trimIndent()

            val response = withTimeout(TIMEOUT_MS) {
                intentInterpreterModel.generateContent(prompt)
            }
            val raw = response.text ?: return Result.failure(IllegalStateException("Gemini returned an empty response."))
            val json = JSONObject(raw)
            Result.success(
                StructuredIntent(
                    originalTitle = title,
                    mainObjective = json.optString("mainObjective", title),
                    broaderPurpose = json.optString("broaderPurpose", "Community Collaboration"),
                    problemDomain = json.optString("problemDomain", "Collaborative Initiative"),
                    intendedOutcome = json.optString("intendedOutcome", description),
                    activities = json.optStringList("activities"),
                    requiredCapabilities = json.optStringList("requiredCapabilities"),
                    semanticConcepts = json.optStringList("semanticConcepts"),
                    sourceLabel = "Gemini AI Logic (gemini-3.8-flash)"
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "interpretMissionIntent timed out", e)
            Result.failure(IllegalStateException("AI intent interpretation timed out after 45 seconds."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "interpretMissionIntent failed", e)
            Result.failure(e)
        }
    }

    suspend fun suggestMissionRequirements(
        title: String,
        description: String
    ): Result<MissionRequirementSuggestion> {
        return interpretMissionIntent(title, description).map { intent ->
            MissionRequirementSuggestion(
                interpretedIntent = intent.mainObjective,
                intentDomain = intent.problemDomain,
                essentialCapabilities = intent.requiredCapabilities.take(5),
                optionalCapabilities = intent.semanticConcepts.take(3),
                suggestedActivities = intent.activities.take(5)
            )
        }
    }

    suspend fun generateMatchNarrative(contextPrompt: String): Result<String> {
        return try {
            val response = withTimeout(TIMEOUT_MS) {
                explanationModel.generateContent(
                    "Write a warm, concise, dignified 2-sentence explanation of why these collaborators complement each other based strictly on the provided facts. Never invent facts or rank human worth.\n\n$contextPrompt"
                )
            }
            val text = response.text?.trim()
            if (!text.isNullOrBlank()) Result.success(text)
            else Result.failure(IllegalStateException("Gemini returned an empty narrative."))
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "generateMatchNarrative timed out", e)
            Result.failure(IllegalStateException("AI narrative timed out."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "generateMatchNarrative failed", e)
            Result.failure(e)
        }
    }

    private fun JSONObject.optStringList(key: String): List<String> {
        val arr: JSONArray = optJSONArray(key) ?: return emptyList()
        return (0 until arr.length())
            .mapNotNull { idx -> arr.optString(idx).trim().takeIf { it.isNotEmpty() } }
            .distinct()
    }
}
