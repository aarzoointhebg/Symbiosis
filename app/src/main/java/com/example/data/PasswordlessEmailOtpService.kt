package com.example.data

import com.example.BuildConfig
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap

/**
 * Production Contract for Transactional Email Delivery (Part 11.4 & 11.6).
 * Implement with a backend HTTPS service (e.g., SendGrid/Postmark/Firebase Functions)
 * so verification codes are generated, hashed, stored, and verified server-side.
 */
interface EmailDeliveryGateway {
    val isLiveEmailConfigured: Boolean
    val configurationNotice: String
    suspend fun dispatchVerificationEmail(
        recipientEmail: String,
        subject: String,
        body: String
    ): Result<Unit>
}

class UnconfiguredEmailDeliveryGateway : EmailDeliveryGateway {
    override val isLiveEmailConfigured: Boolean = false
    override val configurationNotice: String =
        "Transactional email delivery is not configured (requires backend SMTP/API endpoint and server-side OTP verification). Running in Development Demonstration Mode."

    override suspend fun dispatchVerificationEmail(
        recipientEmail: String,
        subject: String,
        body: String
    ): Result<Unit> {
        return Result.failure(IllegalStateException(configurationNotice))
    }
}

sealed interface OtpDispatchOutcome {
    data class Dispatched(
        val accountExists: Boolean,
        val isDemoMode: Boolean,
        val statusMessage: String,
        val expiresInSeconds: Int,
        val cooldownSeconds: Int,
        val debugDemoHint: String?
    ) : OtpDispatchOutcome

    data class Rejected(val reason: String) : OtpDispatchOutcome
}

sealed interface OtpVerifyOutcome {
    data class Verified(val normalizedEmail: String, val isNewAccount: Boolean) : OtpVerifyOutcome
    data class Failed(
        val reason: String,
        val remainingAttempts: Int,
        val lockedOut: Boolean
    ) : OtpVerifyOutcome
}

/**
 * Passwordless Email OTP Authentication Manager (Part 11).
 * Enforces:
 * - 6-digit cryptographically random code (SecureRandom)
 * - Salted SHA-256 hash storage (no plaintext OTP storage, no Logcat exposure)
 * - 5-minute TTL, single-use invalidation, previous-code invalidation
 * - 30-second resend cooldown, max 5 failed attempts, rate limiting
 * - Honest Development Demonstration Mode when no email provider is configured
 */
class PasswordlessEmailOtpService(
    private val emailGateway: EmailDeliveryGateway = UnconfiguredEmailDeliveryGateway(),
    private val timeProviderMs: () -> Long = { System.currentTimeMillis() }
) {
    private data class HashedOtpRecord(
        val saltHex: String,
        val sha256HashHex: String,
        val expiresAtMs: Long,
        val issuedAtMs: Long,
        val remainingAttempts: Int,
        val used: Boolean
    )

    private val secureRandom = SecureRandom()
    private val registeredEmails = ConcurrentHashMap.newKeySet<String>().apply {
        add("aarav@symbiosis.edu")
        add("mira@symbiosis.edu")
        add("kabir@symbiosis.edu")
    }
    private val activeOtps = ConcurrentHashMap<String, HashedOtpRecord>()
    private val lockoutUntilMs = ConcurrentHashMap<String, Long>()
    private val requestTimestamps = ConcurrentHashMap<String, MutableList<Long>>()

    // Kept strictly in-memory for BuildConfig.DEBUG interactive demonstration mode only;
    // never persisted, never logged, and always null in release builds.
    private val debugDemoCodes = ConcurrentHashMap<String, String>()

    companion object {
        const val CODE_TTL_SECONDS = 300 // 5 minutes
        const val RESEND_COOLDOWN_SECONDS = 30
        const val MAX_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MS = 120_000L
        const val RATE_LIMIT_MAX_REQUESTS = 5
        const val RATE_LIMIT_WINDOW_MS = 600_000L
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    fun isLiveEmailConfigured(): Boolean = emailGateway.isLiveEmailConfigured

    fun gatewayNotice(): String = emailGateway.configurationNotice

    suspend fun requestOtp(rawEmail: String): OtpDispatchOutcome {
        val email = rawEmail.trim().lowercase()
        if (!isValidEmail(email)) {
            return OtpDispatchOutcome.Rejected("Please enter a valid email address.")
        }

        val now = timeProviderMs()
        val lockedUntil = lockoutUntilMs[email] ?: 0L
        if (now < lockedUntil) {
            val waitSec = ((lockedUntil - now) / 1000L).coerceAtLeast(1L)
            return OtpDispatchOutcome.Rejected("Too many attempts. Please wait ${waitSec}s before requesting a new code.")
        }

        val existing = activeOtps[email]
        if (existing != null && !existing.used) {
            val elapsedSec = ((now - existing.issuedAtMs) / 1000L).toInt()
            if (elapsedSec < RESEND_COOLDOWN_SECONDS) {
                return OtpDispatchOutcome.Rejected(
                    "Please wait ${RESEND_COOLDOWN_SECONDS - elapsedSec}s before resending a verification code."
                )
            }
        }

        val history = requestTimestamps.getOrPut(email) { mutableListOf() }
        history.removeAll { now - it > RATE_LIMIT_WINDOW_MS }
        if (history.size >= RATE_LIMIT_MAX_REQUESTS) {
            return OtpDispatchOutcome.Rejected("Rate limit reached for this email. Please try again in a few minutes.")
        }
        history.add(now)

        // Generate 6-digit numeric code and salted SHA-256 digest
        val rawCode = (100000 + secureRandom.nextInt(900000)).toString()
        val salt = ByteArray(16).also { secureRandom.nextBytes(it) }.toHex()
        val hash = sha256Hex("$salt:$rawCode")

        // Overwrite & invalidate any previous code immediately
        activeOtps[email] = HashedOtpRecord(
            saltHex = salt,
            sha256HashHex = hash,
            expiresAtMs = now + (CODE_TTL_SECONDS * 1000L),
            issuedAtMs = now,
            remainingAttempts = MAX_ATTEMPTS,
            used = false
        )

        val accountExists = registeredEmails.contains(email)
        val emailBody = """
            Welcome to SYMBIOSIS.

            Your verification code is:
            $rawCode

            This code expires in five minutes.
            If you did not request this code, you can safely ignore this email.

            Different Abilities. Shared Possibilities.
        """.trimIndent()

        return if (emailGateway.isLiveEmailConfigured) {
            val sendRes = emailGateway.dispatchVerificationEmail(
                recipientEmail = email,
                subject = "Your SYMBIOSIS verification code",
                body = emailBody
            )
            if (sendRes.isSuccess) {
                OtpDispatchOutcome.Dispatched(
                    accountExists = accountExists,
                    isDemoMode = false,
                    statusMessage = "If this email is eligible, a 6-digit verification code has been dispatched. It expires in 5 minutes.",
                    expiresInSeconds = CODE_TTL_SECONDS,
                    cooldownSeconds = RESEND_COOLDOWN_SECONDS,
                    debugDemoHint = null
                )
            } else {
                OtpDispatchOutcome.Rejected(
                    sendRes.exceptionOrNull()?.message ?: "Email delivery service failed."
                )
            }
        } else {
            // Honest development demonstration mode: do NOT pretend a real email was sent
            if (BuildConfig.DEBUG) {
                debugDemoCodes[email] = rawCode
            }
            OtpDispatchOutcome.Dispatched(
                accountExists = accountExists,
                isDemoMode = true,
                statusMessage = "Development Demonstration Mode: No live email provider is configured, so no real email was sent. Verification uses salted SHA-256 locally for testing.",
                expiresInSeconds = CODE_TTL_SECONDS,
                cooldownSeconds = RESEND_COOLDOWN_SECONDS,
                debugDemoHint = if (BuildConfig.DEBUG) rawCode else null
            )
        }
    }

    fun verifyOtp(rawEmail: String, rawCode: String): OtpVerifyOutcome {
        val email = rawEmail.trim().lowercase()
        val code = rawCode.trim()
        val now = timeProviderMs()

        val lockedUntil = lockoutUntilMs[email] ?: 0L
        if (now < lockedUntil) {
            return OtpVerifyOutcome.Failed(
                reason = "Verification temporarily locked due to repeated invalid attempts.",
                remainingAttempts = 0,
                lockedOut = true
            )
        }

        if (code.length != 6 || !code.all { it.isDigit() }) {
            return OtpVerifyOutcome.Failed(
                reason = "Enter the 6-digit numeric verification code.",
                remainingAttempts = activeOtps[email]?.remainingAttempts ?: MAX_ATTEMPTS,
                lockedOut = false
            )
        }

        val record = activeOtps[email]
            ?: return OtpVerifyOutcome.Failed("No active verification session. Please request a new code.", 0, false)

        if (record.used) {
            return OtpVerifyOutcome.Failed("This verification code has already been used. Request a new code.", 0, false)
        }

        if (now > record.expiresAtMs) {
            activeOtps.remove(email)
            debugDemoCodes.remove(email)
            return OtpVerifyOutcome.Failed("This verification code has expired (5-minute limit). Request a fresh code.", 0, false)
        }

        val candidateHash = sha256Hex("${record.saltHex}:$code")
        if (constantTimeEquals(candidateHash, record.sha256HashHex)) {
            // Single-use invalidation
            activeOtps[email] = record.copy(used = true)
            debugDemoCodes.remove(email)
            val isNew = registeredEmails.add(email)
            return OtpVerifyOutcome.Verified(normalizedEmail = email, isNewAccount = isNew)
        } else {
            val nextAttempts = (record.remainingAttempts - 1).coerceAtLeast(0)
            if (nextAttempts == 0) {
                activeOtps.remove(email)
                debugDemoCodes.remove(email)
                lockoutUntilMs[email] = now + LOCKOUT_DURATION_MS
                return OtpVerifyOutcome.Failed(
                    reason = "Maximum verification attempts exceeded. Locked for 2 minutes.",
                    remainingAttempts = 0,
                    lockedOut = true
                )
            } else {
                activeOtps[email] = record.copy(remainingAttempts = nextAttempts)
                return OtpVerifyOutcome.Failed(
                    reason = "Invalid verification code. $nextAttempts attempt(s) remaining.",
                    remainingAttempts = nextAttempts,
                    lockedOut = false
                )
            }
        }
    }

    private fun sha256Hex(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.toHex()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var diff = 0
        for (i in a.indices) {
            diff = diff or (a[i].code xor b[i].code)
        }
        return diff == 0
    }
}
