package com.example.data

import com.example.model.AmbitionHorizon
import com.example.model.AmbitionPriority
import com.example.model.AvailabilityWindow
import com.example.model.CollaborationInvitation
import com.example.model.CollaborationStyle
import com.example.model.CommunityProfile
import com.example.model.InvitationStatus
import com.example.model.Mission
import com.example.model.MissionMilestone
import com.example.model.MissionStatus
import com.example.model.ProfileVisibility
import com.example.model.UserAmbition

object DemoSeedData {

    val aarav = CommunityProfile(
        id = "demo_aarav",
        userId = "demo_aarav",
        displayName = "Aarav",
        headline = "Systems Engineer & Embedded Firmware Builder",
        bio = "I build reliable software pipelines, work with low-power microcontrollers, and break complex technical systems into clean milestones.",
        skills = listOf("Programming", "Basic electronics", "Logical problem-solving"),
        skillProficiencies = mapOf(
            "Programming" to "EXPERT",
            "Basic electronics" to "ADVANCED",
            "Logical problem-solving" to "EXPERT"
        ),
        knowledgeAreas = listOf("Sensor interfaces", "Python & Kotlin"),
        practicalAbilities = listOf("Circuit prototyping", "Debugging"),
        interests = listOf("Environmental technology", "Open-source tools"),
        resources = listOf("Microcontroller kit", "Hardware test bench"),
        supportWanted = listOf("Visual design", "Field research"),
        ambitions = listOf("Build an open-source neighborhood air-quality monitor"),
        currentProjects = listOf("Prototype air-quality logger"),
        collaborationStyle = CollaborationStyle.HYBRID.name,
        availability = AvailabilityWindow.WEEKENDS.name,
        communicationPrefs = listOf("Text Chat", "Shared Docs"),
        accessibilityPrefs = listOf("Clear written milestones", "High-contrast code views"),
        constraintsNote = "Available weekend mornings and weekday evenings.",
        visibility = ProfileVisibility.COMMUNITY.name,
        onboardingCompleted = true
    )

    val mira = CommunityProfile(
        id = "demo_mira",
        userId = "demo_mira",
        displayName = "Mira",
        headline = "Visual Designer & Inclusive Storyteller",
        bio = "I turn complex technical and environmental insights into warm, clear diagrams, tactile guides, and accessible community stories.",
        skills = listOf("Visual design", "Presentation design", "Creative communication"),
        skillProficiencies = mapOf(
            "Visual design" to "EXPERT",
            "Presentation design" to "ADVANCED",
            "Creative communication" to "EXPERT"
        ),
        knowledgeAreas = listOf("Information hierarchy", "Inclusive storytelling"),
        practicalAbilities = listOf("Diagram layout", "Public facilitation"),
        interests = listOf("Inclusive storytelling", "Community sustainability"),
        resources = listOf("Illustration tablet", "Open visual templates"),
        supportWanted = listOf("Programming", "Data collection"),
        ambitions = listOf("Design accessible visual guides for local community initiatives"),
        currentProjects = listOf("Neighborhood air quality graphics"),
        collaborationStyle = CollaborationStyle.SMALL_GROUP.name,
        availability = AvailabilityWindow.FLEXIBLE.name,
        communicationPrefs = listOf("Visual Boards", "Voice Notes"),
        accessibilityPrefs = listOf("Alt-text on all visuals", "Captioned video calls"),
        constraintsNote = "Prefers collaborative co-design reviews twice a week.",
        visibility = ProfileVisibility.COMMUNITY.name,
        onboardingCompleted = true
    )

    val kabir = CommunityProfile(
        id = "demo_kabir",
        userId = "demo_kabir",
        displayName = "Kabir",
        headline = "Environmental Researcher & Field Data Analyst",
        bio = "I investigate urban ecology questions systematically, design reliable field sampling protocols, and write clear open documentation.",
        skills = listOf("Research", "Data collection", "Documentation"),
        skillProficiencies = mapOf(
            "Research" to "EXPERT",
            "Data collection" to "ADVANCED",
            "Documentation" to "ADVANCED"
        ),
        knowledgeAreas = listOf("Environmental science", "Experimental methodology"),
        practicalAbilities = listOf("Field sampling", "Structured analysis"),
        interests = listOf("Urban ecology", "Clean air & soil health"),
        resources = listOf("Water & soil test strips", "Field notebook templates"),
        supportWanted = listOf("Programming", "Visual design"),
        ambitions = listOf("Publish an open neighborhood microclimate dataset"),
        currentProjects = listOf("District microclimate log"),
        collaborationStyle = CollaborationStyle.ASYNC.name,
        availability = AvailabilityWindow.WEEKDAYS.name,
        communicationPrefs = listOf("Structured Notes", "Text Chat"),
        accessibilityPrefs = listOf("Screen-reader friendly tables", "Audio summaries"),
        constraintsNote = "Can collect field samples on weekday afternoons.",
        visibility = ProfileVisibility.COMMUNITY.name,
        onboardingCompleted = true
    )

    val samir = CommunityProfile(
        id = "demo_samir",
        userId = "demo_samir",
        displayName = "Samir",
        headline = "Tactile & Audio Accessibility Architect",
        bio = "I design multi-sensory experiences so blind and low-vision people can explore public spaces and interactive tools independently.",
        skills = listOf("Audio engineering", "Tactile prototyping", "Accessibility testing"),
        skillProficiencies = mapOf(
            "Audio engineering" to "EXPERT",
            "Tactile prototyping" to "ADVANCED",
            "Accessibility testing" to "EXPERT"
        ),
        knowledgeAreas = listOf("Screen reader UX", "Universal design"),
        practicalAbilities = listOf("Braille labeling", "Soundscape cues"),
        interests = listOf("Assistive tech", "Inclusive public spaces"),
        resources = listOf("3D tactile print templates", "Portable audio recorder"),
        supportWanted = listOf("Basic electronics", "Visual design"),
        ambitions = listOf("Make neighborhood public spaces multisensory and accessible"),
        currentProjects = listOf("Audio-tactile botanical trail"),
        collaborationStyle = CollaborationStyle.HYBRID.name,
        availability = AvailabilityWindow.FLEXIBLE.name,
        communicationPrefs = listOf("Audio Notes", "Screen-Reader Text"),
        accessibilityPrefs = listOf("Semantic headings", "Keyboard & TalkBack navigation"),
        constraintsNote = "Happy to co-test any prototype for accessibility.",
        visibility = ProfileVisibility.COMMUNITY.name,
        onboardingCompleted = true
    )

    val elena = CommunityProfile(
        id = "demo_elena",
        userId = "demo_elena",
        displayName = "Elena",
        headline = "Community Botanist & Urban Greening Organizer",
        bio = "I coordinate local planting drives, match native saplings to neighborhood soil, and welcome newcomers to hands-on stewardship.",
        skills = listOf("Community gardening", "Workshop coordination", "Tree planting"),
        skillProficiencies = mapOf(
            "Community gardening" to "EXPERT",
            "Workshop coordination" to "ADVANCED",
            "Tree planting" to "EXPERT"
        ),
        knowledgeAreas = listOf("Native flora", "Soil restoration"),
        practicalAbilities = listOf("Sapling care", "Volunteer scheduling"),
        interests = listOf("Neighborhood greenery", "Urban shade"),
        resources = listOf("Compost starter bins", "Native seed library"),
        supportWanted = listOf("Creative communication", "Data collection"),
        ambitions = listOf("Restore green canopy spaces across local city blocks"),
        currentProjects = listOf("Block 4 tree-planting circle"),
        collaborationStyle = CollaborationStyle.SMALL_GROUP.name,
        availability = AvailabilityWindow.WEEKENDS.name,
        communicationPrefs = listOf("Group Check-ins", "Text Chat"),
        accessibilityPrefs = listOf("Step-free garden pathways", "Seated planting stations"),
        constraintsNote = "Weekend morning fieldwork with step-free accessible beds.",
        visibility = ProfileVisibility.COMMUNITY.name,
        onboardingCompleted = true
    )

    val nadia = CommunityProfile(
        id = "demo_nadia",
        userId = "demo_nadia",
        displayName = "Nadia",
        headline = "Sustainability Facilitator & Circular Living Lead",
        bio = "I host neighborhood workshops that help households cut single-use waste and turn kitchen organics into community compost.",
        skills = listOf("Workshop coordination", "Creative communication", "Research"),
        skillProficiencies = mapOf(
            "Workshop coordination" to "EXPERT",
            "Creative communication" to "ADVANCED",
            "Research" to "INTERMEDIATE"
        ),
        knowledgeAreas = listOf("Waste reduction", "Circular neighborhoods"),
        practicalAbilities = listOf("Educational zine making", "Resident outreach"),
        interests = listOf("Circular living", "Civic innovation"),
        resources = listOf("Printed multilingual guides"),
        supportWanted = listOf("Visual design", "Community gardening"),
        ambitions = listOf("Educate residents about waste reduction and composting"),
        currentProjects = listOf("Zero-waste street toolkit"),
        collaborationStyle = CollaborationStyle.HYBRID.name,
        availability = AvailabilityWindow.EVENINGS.name,
        communicationPrefs = listOf("Voice Calls", "Shared Slides"),
        accessibilityPrefs = listOf("Live captions", "Easy-read summaries"),
        constraintsNote = "Available weekday evenings and Sunday afternoons.",
        visibility = ProfileVisibility.COMMUNITY.name,
        onboardingCompleted = true
    )

    val rohan = CommunityProfile(
        id = "demo_rohan",
        userId = "demo_rohan",
        displayName = "Rohan",
        headline = "Hardware Maker & Sensor Technician",
        bio = "I build low-cost environmental sensor rigs, solder weatherproof enclosures, and help teams debug hardware step by step.",
        skills = listOf("Basic electronics", "Programming", "Data collection"),
        skillProficiencies = mapOf(
            "Basic electronics" to "EXPERT",
            "Programming" to "INTERMEDIATE",
            "Data collection" to "ADVANCED"
        ),
        knowledgeAreas = listOf("Arduino & ESP32", "Low-power sensors"),
        practicalAbilities = listOf("Soldering", "Sensor calibration"),
        interests = listOf("Open hardware", "Environmental technology"),
        resources = listOf("DHT22 & PM2.5 sensors", "Portable soldering iron"),
        supportWanted = listOf("Documentation", "Visual design"),
        ambitions = listOf("Build reliable open-source environmental sensors with local makers"),
        currentProjects = listOf("Solar weather box"),
        collaborationStyle = CollaborationStyle.REALTIME.name,
        availability = AvailabilityWindow.WEEKENDS.name,
        communicationPrefs = listOf("Video Call", "Diagrams"),
        accessibilityPrefs = listOf("Step-by-step checklists"),
        constraintsNote = "Happy to collaborate with hardware beginners and designers.",
        visibility = ProfileVisibility.COMMUNITY.name,
        onboardingCompleted = true
    )

    val featuredCollaborators: List<CommunityProfile> = listOf(aarav, mira, kabir)
    val communityCollaborators: List<CommunityProfile> = listOf(aarav, mira, kabir, samir, elena, nadia, rohan)

    val featuredCommunityMission = Mission(
        id = "mission_community_env",
        ownerId = "demo_aarav",
        ownerName = "Aarav",
        title = "Neighborhood Environmental Sensor Network",
        description = "Co-build an open community project that measures local air quality, temperature, and soil moisture, analyzes trends, and shares accessible insights with residents.",
        objective = "Combine real-time sensor telemetry, field research, and accessible visual storytelling for local neighborhoods.",
        intendedOutcome = "A working open-source sensor network with live accessible visualizations and open documentation.",
        problemAddressed = "Individual contributors rarely hold all technical, analytical, and design capabilities needed for cross-disciplinary civic work.",
        interpretedIntent = "Collaboratively engineer, research, and communicate an open neighborhood environmental monitoring initiative.",
        intentDomain = "Environmental Technology & Civic Stewardship",
        requiredCapabilities = listOf("Programming", "Research", "Visual design", "Data collection"),
        optionalCapabilities = listOf("Basic electronics", "Accessibility testing"),
        activities = listOf("Sensor programming", "Field sampling", "Visual storytelling", "Resident walkthrough"),
        availabilityReq = "Flexible / Weekends",
        accessibilityReq = "Multisensory dashboard with alt-text and audio summaries",
        targetParticipants = 3L,
        memberUids = listOf("demo_aarav", "demo_mira", "demo_kabir"),
        status = MissionStatus.ACTIVE.name,
        visibility = "COMMUNITY",
        progressPercent = 45L
    )

    val intentDiscoveryMissions: List<Mission> = listOf(
        featuredCommunityMission,
        Mission(
            id = "mission_greener_a",
            ownerId = "demo_elena",
            ownerName = "Elena",
            title = "Make our neighbourhood greener",
            description = "Bring neighbors together to add shade trees, native shrubs, and welcoming pocket parks along concrete streets.",
            objective = "Increase neighborhood greenery and foster community participation.",
            intendedOutcome = "30 newly planted native saplings and shared street planter beds.",
            problemAddressed = "Heat islands and lack of accessible green corners in our block.",
            interpretedIntent = "Improve the local environment by increasing greenery, planting trees, and encouraging community participation.",
            intentDomain = "Local Environmental Stewardship",
            requiredCapabilities = listOf("Community gardening", "Workshop coordination", "Visual design"),
            optionalCapabilities = listOf("Data collection"),
            activities = listOf("Site survey", "Sapling planting", "Watering rota"),
            targetParticipants = 4L,
            memberUids = listOf("demo_elena"),
            status = MissionStatus.OPEN.name,
            visibility = "COMMUNITY",
            progressPercent = 25L
        ),
        Mission(
            id = "mission_tree_drive_b",
            ownerId = "demo_elena",
            ownerName = "Elena",
            title = "Organise a community tree-planting drive",
            description = "Coordinate local residents and volunteers for a weekend planting event along neighborhood avenues and public parks.",
            objective = "Plant native shade trees together to cool sidewalks and restore urban canopy.",
            intendedOutcome = "A thriving green corridor cared for by neighborhood teams.",
            problemAddressed = "Sparse shade and declining tree cover in local public spaces.",
            interpretedIntent = "Improve the local environment by planting trees and mobilizing community volunteers.",
            intentDomain = "Local Environmental Stewardship",
            requiredCapabilities = listOf("Tree planting", "Workshop coordination"),
            optionalCapabilities = listOf("Creative communication"),
            activities = listOf("Sapling sourcing", "Accessible planting beds", "Care schedule"),
            targetParticipants = 4L,
            memberUids = listOf("demo_elena"),
            status = MissionStatus.OPEN.name,
            visibility = "COMMUNITY",
            progressPercent = 20L
        ),
        Mission(
            id = "mission_restore_green_c",
            ownerId = "demo_kabir",
            ownerName = "Kabir",
            title = "Restore local green spaces",
            description = "Revive neglected neighborhood patches with native flora, soil care, and quiet shaded seating accessible to everyone.",
            objective = "Transform unused corners into healthy, inclusive botanical pockets.",
            intendedOutcome = "Restored soil biodiversity and lush shared green spaces.",
            problemAddressed = "Degraded soil and neglected urban lots.",
            interpretedIntent = "Restore local green spaces and increase urban foliage through collaborative stewardship.",
            intentDomain = "Local Environmental Stewardship",
            requiredCapabilities = listOf("Community gardening", "Research", "Documentation"),
            optionalCapabilities = listOf("Accessibility testing"),
            activities = listOf("Soil testing", "Native planting", "Biodiversity log"),
            targetParticipants = 3L,
            memberUids = listOf("demo_kabir"),
            status = MissionStatus.OPEN.name,
            visibility = "COMMUNITY",
            progressPercent = 30L
        ),
        Mission(
            id = "mission_waste_edu_d",
            ownerId = "demo_nadia",
            ownerName = "Nadia",
            title = "Educate residents about waste reduction",
            description = "Run friendly household workshops and create clear visual guides on sorting, reusing, and cutting single-use plastic.",
            objective = "Help households adopt practical daily waste-reduction habits.",
            intendedOutcome = "Lower neighborhood landfill waste and stronger reuse networks.",
            problemAddressed = "Confusion around sorting and high single-use packaging waste.",
            interpretedIntent = "Empower residents with practical knowledge and campaigns to reduce local waste.",
            intentDomain = "Local Environmental Stewardship",
            requiredCapabilities = listOf("Creative communication", "Workshop coordination", "Visual design"),
            optionalCapabilities = listOf("Research"),
            activities = listOf("Zine design", "Neighborhood Q&A", "Waste audit"),
            targetParticipants = 3L,
            memberUids = listOf("demo_nadia"),
            status = MissionStatus.OPEN.name,
            visibility = "COMMUNITY",
            progressPercent = 40L
        ),
        Mission(
            id = "mission_compost_e",
            ownerId = "demo_nadia",
            ownerName = "Nadia",
            title = "Launch a neighbourhood composting collective",
            description = "Turn kitchen organics into rich soil amendments that nourish nearby community gardens and street trees.",
            objective = "Create a closed-loop organic composting system for the block.",
            intendedOutcome = "Rich compost supporting local tree planting and gardens.",
            problemAddressed = "Organic waste going to landfill while urban soil lacks nutrients.",
            interpretedIntent = "Convert local organic waste into healthy soil to nourish neighborhood greenery.",
            intentDomain = "Local Environmental Stewardship",
            requiredCapabilities = listOf("Workshop coordination", "Documentation"),
            optionalCapabilities = listOf("Community gardening"),
            activities = listOf("Bin setup", "Moisture tracking", "Soil sharing"),
            targetParticipants = 3L,
            memberUids = listOf("demo_nadia"),
            status = MissionStatus.OPEN.name,
            visibility = "COMMUNITY",
            progressPercent = 35L
        ),
        Mission(
            id = "mission_tactile_guide_f",
            ownerId = "demo_samir",
            ownerName = "Samir",
            title = "Create an accessible audio & tactile plant guide",
            description = "Build braille plaques and gentle soundscape guides so blind and low-vision visitors can explore neighborhood botanical parks independently.",
            objective = "Make neighborhood botanical spaces welcoming and multisensory.",
            intendedOutcome = "Tactile leaf maps and QR audio stories at every garden bed.",
            problemAddressed = "Nature trails often rely solely on printed visual signage.",
            interpretedIntent = "Design inclusive multisensory audio and tactile guides for shared botanical spaces.",
            intentDomain = "Inclusive Design & Accessibility",
            requiredCapabilities = listOf("Audio engineering", "Tactile prototyping", "Accessibility testing"),
            optionalCapabilities = listOf("Programming", "Visual design"),
            activities = listOf("Audio recording", "Tactile plaque craft", "User co-testing"),
            targetParticipants = 3L,
            memberUids = listOf("demo_samir"),
            status = MissionStatus.OPEN.name,
            visibility = "COMMUNITY",
            progressPercent = 50L
        )
    )

    val defaultMilestones: List<MissionMilestone> = listOf(
        MissionMilestone(
            id = "ms_1",
            missionId = featuredCommunityMission.id,
            ownerId = "demo_aarav",
            memberUids = listOf("demo_aarav", "demo_mira", "demo_kabir"),
            title = "Define research questions & environmental metrics",
            assignedRole = "Kabir (Research & Data Analysis)",
            completed = true
        ),
        MissionMilestone(
            id = "ms_2",
            missionId = featuredCommunityMission.id,
            ownerId = "demo_aarav",
            memberUids = listOf("demo_aarav", "demo_mira", "demo_kabir"),
            title = "Program sensor data pipeline & calibration",
            assignedRole = "Aarav (Systems & Firmware Builder)",
            completed = true
        ),
        MissionMilestone(
            id = "ms_3",
            missionId = featuredCommunityMission.id,
            ownerId = "demo_aarav",
            memberUids = listOf("demo_aarav", "demo_mira", "demo_kabir"),
            title = "Design accessible visual & audio community dashboard",
            assignedRole = "Mira (Visual Storytelling & Design)",
            completed = false
        ),
        MissionMilestone(
            id = "ms_tactile_1",
            missionId = "mission_tactile_guide_f",
            ownerId = "demo_samir",
            memberUids = listOf("demo_samir"),
            title = "Record 30-second native plant audio stories",
            assignedRole = "Samir (Audio & Soundscape)",
            completed = true
        ),
        MissionMilestone(
            id = "ms_tactile_2",
            missionId = "mission_tactile_guide_f",
            ownerId = "demo_samir",
            memberUids = listOf("demo_samir"),
            title = "Wire QR/NFC trigger script & accessible captions",
            assignedRole = "Interactive Trigger Builder",
            completed = false
        ),
        MissionMilestone(
            id = "ms_greener_1",
            missionId = "mission_greener_a",
            ownerId = "demo_elena",
            memberUids = listOf("demo_elena"),
            title = "Survey sunny sidewalk corners & accessible planter beds",
            assignedRole = "Elena (Community Botanist)",
            completed = true
        ),
        MissionMilestone(
            id = "ms_greener_2",
            missionId = "mission_greener_a",
            ownerId = "demo_elena",
            memberUids = listOf("demo_elena"),
            title = "Source 30 native shade saplings",
            assignedRole = "Volunteer Team",
            completed = false
        ),
        MissionMilestone(
            id = "ms_greener_3",
            missionId = "mission_greener_a",
            ownerId = "demo_elena",
            memberUids = listOf("demo_elena"),
            title = "Publish neighborhood watering & care schedule",
            assignedRole = "Visual & Outreach Lead",
            completed = false
        )
    )

    fun defaultUserProfile(userId: String, displayName: String): CommunityProfile =
        CommunityProfile(
            id = userId,
            userId = userId,
            displayName = displayName.ifBlank { "Explorer" },
            headline = "Collaborator discovering shared possibilities",
            bio = "I enjoy building alongside others and contributing my strengths to meaningful community missions.",
            skills = listOf("Programming", "Logical problem-solving"),
            skillProficiencies = mapOf(
                "Programming" to "INTERMEDIATE",
                "Logical problem-solving" to "ADVANCED"
            ),
            knowledgeAreas = listOf("Digital literacy"),
            practicalAbilities = listOf("Structured note-taking"),
            interests = listOf("Environmental technology", "Assistive tech"),
            resources = listOf("Laptop"),
            supportWanted = listOf("Visual design", "Basic electronics"),
            ambitions = listOf("Co-create an open neighborhood sustainability project"),
            currentProjects = emptyList(),
            collaborationStyle = CollaborationStyle.HYBRID.name,
            availability = AvailabilityWindow.FLEXIBLE.name,
            communicationPrefs = listOf("Text Chat", "Shared Docs"),
            accessibilityPrefs = listOf("Clear headings", "Flexible pacing"),
            constraintsNote = "Available evenings and weekends.",
            visibility = ProfileVisibility.COMMUNITY.name,
            onboardingCompleted = false
        )

    fun defaultUserAmbitions(userId: String): List<UserAmbition> = listOf(
        UserAmbition(
            id = "amb_default_1",
            userId = userId,
            title = "Co-create an environmental monitoring prototype",
            description = "Partner with researchers and designers to turn open ideas into an accessible community tool.",
            horizon = AmbitionHorizon.SHORT_TERM.name,
            priority = AmbitionPriority.HIGH.name,
            tags = listOf("Programming", "Environment", "Accessibility"),
            sharedToCommunity = true
        ),
        UserAmbition(
            id = "amb_default_2",
            userId = userId,
            title = "Learn electronics & tactile prototyping",
            description = "Grow hands-on hardware confidence by building alongside a complementary collaborator.",
            horizon = AmbitionHorizon.LONG_TERM.name,
            priority = AmbitionPriority.MEDIUM.name,
            tags = listOf("Electronics", "Accessibility"),
            sharedToCommunity = true
        )
    )

    fun defaultSampleInvitations(userId: String, userName: String): List<CollaborationInvitation> = listOf(
        CollaborationInvitation(
            id = "inv_sample_1",
            missionId = "mission_tactile_guide_f",
            missionTitle = "Create an accessible audio & tactile plant guide",
            senderId = "demo_samir",
            senderName = "Samir",
            recipientId = userId,
            recipientName = userName,
            participantUids = listOf("demo_samir", userId),
            proposedRole = "Software & Interactive Trigger Builder",
            rationale = "Your programming strength complements Samir's audio & tactile prototyping so visitors can trigger plant audio stories effortlessly.",
            status = InvitationStatus.PENDING.name
        ),
        CollaborationInvitation(
            id = "inv_sample_accepted_mira",
            missionId = featuredCommunityMission.id,
            missionTitle = featuredCommunityMission.title,
            senderId = userId,
            senderName = userName,
            recipientId = "demo_mira",
            recipientName = "Mira",
            participantUids = listOf(userId, "demo_mira"),
            proposedRole = "Visual Storytelling & Accessible Graphics",
            rationale = "Pairing programming data pipelines with accessible visual design for residents.",
            status = InvitationStatus.ACCEPTED.name
        )
    )

    val sampleCommunityPosts: List<com.example.model.CommunityPost> = listOf(
        com.example.model.CommunityPost(
            id = "post_seed_collab",
            authorId = "demo_rohan",
            authorName = "Rohan",
            authorHeadline = "Hardware Maker & Sensor Technician",
            category = com.example.model.PostCategory.LOOKING_FOR_COLLABORATORS.name,
            title = "Solar rainwater soil-moisture node needs a dashboard & data story partner",
            body = "I finished soldering two weatherproof ESP32 soil-moisture boxes for community planters. Looking for someone who enjoys turning raw sensor logs into clear visual charts or accessible audio updates for garden volunteers.",
            capabilityTags = listOf("Basic electronics", "Data collection", "Open hardware"),
            requestedCapabilities = listOf("Programming", "Visual design", "Documentation"),
            missionId = featuredCommunityMission.id,
            missionTitle = featuredCommunityMission.title,
            communityLocationTag = "East Block Botanical Corner",
            shareLocationTag = true,
            visibility = "COMMUNITY",
            appreciatorUids = listOf("demo_aarav", "demo_elena")
        ),
        com.example.model.CommunityPost(
            id = "post_seed_idea",
            authorId = "demo_samir",
            authorName = "Samir",
            authorHeadline = "Tactile & Audio Accessibility Architect",
            category = com.example.model.PostCategory.IDEAS.name,
            title = "Idea: Tactile QR-triggered bird & leaf soundscapes at neighborhood benches",
            body = "What if public benches carried raised tactile leaf silhouettes paired with tiny NFC/QR plaques? Blind and sighted neighbors alike could listen to a 30-second story about the tree overhead and local ecology.",
            capabilityTags = listOf("Audio engineering", "Tactile prototyping", "Accessibility testing"),
            requestedCapabilities = listOf("Community gardening", "Programming"),
            missionId = "mission_tactile_guide_f",
            missionTitle = "Create an accessible audio & tactile plant guide",
            communityLocationTag = "Botanical Walk",
            shareLocationTag = true,
            visibility = "COMMUNITY",
            appreciatorUids = listOf("demo_mira", "demo_kabir", "demo_elena")
        ),
        com.example.model.CommunityPost(
            id = "post_seed_question",
            authorId = "demo_kabir",
            authorName = "Kabir",
            authorHeadline = "Environmental Researcher & Field Data Analyst",
            category = com.example.model.PostCategory.QUESTIONS.name,
            title = "Best low-cost way to verify air-quality sensor drift outdoors over 30 days?",
            body = "Before publishing our neighborhood microclimate log, I want to make sure humidity spikes aren't skewing particulate readings. Has anyone calibrated optical dust sensors alongside reference stations?",
            capabilityTags = listOf("Research", "Data collection", "Environmental technology"),
            requestedCapabilities = listOf("Basic electronics", "Programming"),
            missionId = featuredCommunityMission.id,
            missionTitle = featuredCommunityMission.title,
            visibility = "COMMUNITY",
            appreciatorUids = listOf("demo_aarav")
        ),
        com.example.model.CommunityPost(
            id = "post_seed_update",
            authorId = "demo_mira",
            authorName = "Mira",
            authorHeadline = "Visual Designer & Inclusive Storyteller",
            category = com.example.model.PostCategory.PROJECT_UPDATES.name,
            title = "Update: First high-contrast air quality poster draft tested with screen readers",
            body = "We paired tabular summaries with clean color-independent icons and semantic alt descriptions so every resident can follow weekly PM2.5 trends. Next step: connecting live CSV exports.",
            capabilityTags = listOf("Visual design", "Inclusive storytelling", "Accessibility testing"),
            requestedCapabilities = listOf("Programming"),
            missionId = featuredCommunityMission.id,
            missionTitle = featuredCommunityMission.title,
            visibility = "COMMUNITY",
            appreciatorUids = listOf("demo_aarav", "demo_samir")
        ),
        com.example.model.CommunityPost(
            id = "post_seed_opportunity",
            authorId = "demo_elena",
            authorName = "Elena",
            authorHeadline = "Community Botanist & Urban Greening Organizer",
            category = com.example.model.PostCategory.OPPORTUNITIES.name,
            title = "Saturday step-free native sapling planting & soil prep circle",
            body = "We have 18 native shade saplings and raised wheelchair-accessible planter beds ready. All experience levels welcome — especially folks who enjoy photography, signage, or watering logistics.",
            capabilityTags = listOf("Community gardening", "Tree planting", "Workshop coordination"),
            requestedCapabilities = listOf("Creative communication", "Visual design"),
            missionId = "mission_greener_a",
            missionTitle = "Make our neighbourhood greener",
            communityLocationTag = "Cedar Courtyard",
            shareLocationTag = true,
            visibility = "COMMUNITY",
            appreciatorUids = listOf("demo_nadia", "demo_kabir")
        ),
        com.example.model.CommunityPost(
            id = "post_seed_resource",
            authorId = "demo_aarav",
            authorName = "Aarav",
            authorHeadline = "Systems Engineer & Embedded Firmware Builder",
            category = com.example.model.PostCategory.RESOURCES.name,
            title = "Open checklist: Designing accessible CSV & JSON telemetry for civic tools",
            body = "Put together a concise starter template for naming units clearly, adding plain-language summaries, and exposing keyboard-navigable summaries alongside charts.",
            capabilityTags = listOf("Programming", "Documentation", "Open-source tools"),
            linkUrl = "https://www.w3.org/WAI/tutorials/",
            visibility = "COMMUNITY",
            appreciatorUids = listOf("demo_mira", "demo_rohan")
        ),
        com.example.model.CommunityPost(
            id = "post_seed_achievement",
            authorId = "demo_nadia",
            authorName = "Nadia",
            authorHeadline = "Sustainability Facilitator & Circular Living Lead",
            category = com.example.model.PostCategory.ACHIEVEMENTS.name,
            title = "Milestone celebrated: 42 households joined the block composting collective!",
            body = "Thanks to Elena's soil advice, Mira's illustrated sorting zine, and Kabir's moisture logs, our first batch of cured compost just nourished 12 newly planted street trees.",
            capabilityTags = listOf("Workshop coordination", "Creative communication", "Community gardening"),
            missionId = "mission_compost_e",
            missionTitle = "Launch a neighbourhood composting collective",
            communityLocationTag = "Maple Lane Commons",
            shareLocationTag = true,
            visibility = "COMMUNITY",
            appreciatorUids = listOf("demo_elena", "demo_mira", "demo_kabir", "demo_aarav")
        )
    )

    val sampleCommunityComments: List<com.example.model.CommunityComment> = listOf(
        com.example.model.CommunityComment(
            id = "cmt_seed_1",
            postId = "post_seed_collab",
            authorId = "demo_mira",
            authorName = "Mira",
            body = "Rohan, I can craft clean accessible soil-moisture graphics if someone wants to wire up the JSON feed!",
            replyToAuthor = "Rohan"
        ),
        com.example.model.CommunityComment(
            id = "cmt_seed_2",
            postId = "post_seed_idea",
            authorId = "demo_elena",
            authorName = "Elena",
            body = "Love this! We can pair each tactile leaf plaque with real pressed specimens from Cedar Courtyard.",
            replyToAuthor = "Samir"
        ),
        com.example.model.CommunityComment(
            id = "cmt_seed_3",
            postId = "post_seed_question",
            authorId = "demo_aarav",
            authorName = "Aarav",
            body = "Adding a simple rolling median filter plus a humidity compensation curve cuts morning fog spikes dramatically. Happy to share the script!",
            replyToAuthor = "Kabir"
        )
    )
}

