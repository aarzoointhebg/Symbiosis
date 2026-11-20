package com.example.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Interests
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Reply
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.model.AvailabilityWindow
import com.example.model.CommunityComment
import com.example.model.CommunityPost
import com.example.model.CommunityProfile
import com.example.model.FollowedTopic
import com.example.model.IntentPostRecommendation
import com.example.model.Mission
import com.example.model.PostCategory
import com.example.model.UiState
import com.example.ui.CommunityFeedMode
import com.example.ui.SuggestedForYouItem
import com.example.ui.components.AiBadge
import com.example.ui.components.InfoBannerCard
import com.example.ui.components.MascotBadgeAvatar
import kotlinx.coroutines.launch
import com.example.ui.theme.BotanicalGreen
import com.example.ui.theme.ComplementaryAmber
import com.example.ui.theme.CoverageGreen
import com.example.ui.theme.DarkBarkText
import com.example.ui.theme.DarkForestSurfaceVariant
import com.example.ui.theme.DarkMintPrimary
import com.example.ui.theme.DarkSageText
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.LocalSymbiosisIsDark
import com.example.ui.theme.SageSurface
import com.example.ui.theme.SoftCream
import com.example.ui.theme.SoftEarthBrown
import com.example.ui.theme.WarmGoldenYellow
import java.io.File
import java.io.FileOutputStream

private data class CategoryStyle(
    val icon: ImageVector,
    val accent: Color,
    val container: Color
)

@Composable
private fun categoryStyle(category: PostCategory): CategoryStyle {
    val isDark = LocalSymbiosisIsDark.current
    return when (category) {
        PostCategory.IDEAS -> CategoryStyle(
            icon = Icons.Rounded.Lightbulb,
            accent = if (isDark) WarmGoldenYellow else ComplementaryAmber,
            container = (if (isDark) WarmGoldenYellow else ComplementaryAmber).copy(alpha = 0.14f)
        )
        PostCategory.QUESTIONS -> CategoryStyle(
            icon = Icons.Rounded.HelpOutline,
            accent = if (isDark) Color(0xFF91D4F2) else Color(0xFF205E78),
            container = (if (isDark) Color(0xFF91D4F2) else Color(0xFF205E78)).copy(alpha = 0.14f)
        )
        PostCategory.LOOKING_FOR_COLLABORATORS -> CategoryStyle(
            icon = Icons.Rounded.GroupAdd,
            accent = if (isDark) DarkMintPrimary else DeepForestGreen,
            container = (if (isDark) DarkMintPrimary else DeepForestGreen).copy(alpha = 0.15f)
        )
        PostCategory.PROJECT_UPDATES -> CategoryStyle(
            icon = Icons.Rounded.Update,
            accent = if (isDark) Color(0xFF8FE3B0) else BotanicalGreen,
            container = (if (isDark) Color(0xFF8FE3B0) else BotanicalGreen).copy(alpha = 0.14f)
        )
        PostCategory.OPPORTUNITIES -> CategoryStyle(
            icon = Icons.Rounded.EventAvailable,
            accent = if (isDark) Color(0xFFFFC178) else SoftEarthBrown,
            container = (if (isDark) Color(0xFFFFC178) else SoftEarthBrown).copy(alpha = 0.15f)
        )
        PostCategory.RESOURCES -> CategoryStyle(
            icon = Icons.Rounded.MenuBook,
            accent = if (isDark) Color(0xFFB3D9FF) else Color(0xFF2E5C8A),
            container = (if (isDark) Color(0xFFB3D9FF) else Color(0xFF2E5C8A)).copy(alpha = 0.14f)
        )
        PostCategory.ACHIEVEMENTS -> CategoryStyle(
            icon = Icons.Rounded.EmojiEvents,
            accent = if (isDark) WarmGoldenYellow else CoverageGreen,
            container = (if (isDark) WarmGoldenYellow else CoverageGreen).copy(alpha = 0.16f)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommunityScreen(
    currentUserId: String,
    posts: List<CommunityPost>,
    intentRecommendations: List<IntentPostRecommendation>,
    commentsByPostId: Map<String, List<CommunityComment>>,
    bookmarkedPostIds: Set<String>,
    followedTopics: List<FollowedTopic>,
    selectedCategory: PostCategory?,
    feedMode: CommunityFeedMode,
    intentQuery: String,
    availableMissions: List<Mission>,
    discoverableProfiles: List<CommunityProfile>,
    suggestedForYouState: UiState<List<SuggestedForYouItem>> = UiState.Loading,
    onEnsureSuggestedForYou: () -> Unit = {},
    onRefreshSuggestedForYou: () -> Unit = {},
    onExpressMissionCollaborationInterest: (Mission) -> Unit = {},
    onOpenMissionTab: () -> Unit = {},
    memberSearchQuery: String = "",
    matchedMembers: List<CommunityProfile> = emptyList(),
    onMemberSearchQueryChange: (String) -> Unit = {},
    onClearMemberSearch: () -> Unit = {},
    onOpenMemberProfile: (CommunityProfile) -> Unit = {},
    onMessageMember: (CommunityProfile) -> Unit = {},
    onSelectCategory: (PostCategory?) -> Unit,
    onSelectFeedMode: (CommunityFeedMode) -> Unit,
    onIntentQueryChange: (String) -> Unit,
    onCreatePost: (
        category: PostCategory,
        title: String,
        body: String,
        capabilityTags: List<String>,
        requestedCapabilities: List<String>,
        mission: Mission?,
        communityLocationTag: String,
        shareLocationTag: Boolean,
        linkUrl: String,
        imageUri: String,
        visibility: String
    ) -> Unit,
    onToggleAppreciation: (CommunityPost) -> Unit,
    onAddComment: (CommunityPost, String, String) -> Unit,
    onToggleBookmark: (CommunityPost) -> Unit,
    onToggleFollowTopic: (String) -> Unit,
    onExpressCollaborationInterest: (CommunityPost) -> Unit,
    onSharePostToPeer: (CommunityPost, CommunityProfile, String) -> Unit,
    onSharePostToMissionGroup: (CommunityPost, Mission, String) -> Unit,
    onConvertToMission: (CommunityPost, Boolean) -> Unit,
    onReportPost: (CommunityPost, String) -> Unit,
    onBlockAuthor: (CommunityPost) -> Unit,
    focusedPostId: String? = null,
    onClearFocusedPost: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showComposer by remember { mutableStateOf(false) }
    var reportTarget by remember { mutableStateOf<CommunityPost?>(null) }
    var missionConvertTarget by remember { mutableStateOf<CommunityPost?>(null) }
    var shareTarget by remember { mutableStateOf<CommunityPost?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        onEnsureSuggestedForYou()
    }

    val quickMemberTags = remember(discoverableProfiles) {
        val dynamicTags = discoverableProfiles
            .flatMap { it.allCapabilities() + it.interests }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .take(10)
        dynamicTags.ifEmpty {
            listOf(
                "Programming",
                "Visual design",
                "Assistive tech",
                "Environmental technology",
                "Audio engineering",
                "Urban ecology",
                "Documentation"
            )
        }
    }

    val followedTopicSet = remember(followedTopics) {
        followedTopics.map { it.topic.lowercase() }.toSet()
    }

    val recommendationByPostId = remember(intentRecommendations) {
        intentRecommendations.associateBy { it.post.id }
    }

    val filteredPosts = remember(
        posts,
        selectedCategory,
        feedMode,
        bookmarkedPostIds,
        followedTopicSet,
        intentRecommendations
    ) {
        val base = when (feedMode) {
            CommunityFeedMode.ALL -> posts
            CommunityFeedMode.INTENT_DISCOVERY ->
                if (intentRecommendations.isNotEmpty()) intentRecommendations.map { it.post } else posts
            CommunityFeedMode.FOLLOWED_TOPICS ->
                posts.filter { post ->
                    followedTopicSet.isEmpty() ||
                        post.capabilityTags.any { it.lowercase() in followedTopicSet } ||
                        post.requestedCapabilities.any { it.lowercase() in followedTopicSet }
                }
            CommunityFeedMode.BOOKMARKS ->
                posts.filter { it.id in bookmarkedPostIds }
        }
        if (selectedCategory == null) base else base.filter { it.categoryEnum() == selectedCategory }
    }

    LaunchedEffect(focusedPostId) {
        if (!focusedPostId.isNullOrBlank()) {
            onSelectCategory(null)
            onSelectFeedMode(CommunityFeedMode.ALL)
        }
    }

    LaunchedEffect(focusedPostId, filteredPosts, memberSearchQuery) {
        if (!focusedPostId.isNullOrBlank()) {
            val postIndex = filteredPosts.indexOfFirst { it.id == focusedPostId }
            if (postIndex >= 0) {
                val headerCount = 6 +
                    (if (memberSearchQuery.isNotBlank()) 1 else 0) +
                    (if (followedTopics.isNotEmpty()) 1 else 0)
                listState.animateScrollToItem(headerCount + postIndex)
            }
        }
    }

    val isDark = LocalSymbiosisIsDark.current

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Editorial Banner & Quick Compose Trigger
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Forum,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(9.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SYMBIOSIS Community",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Ideas, questions, updates & mission opportunities — no popularity contests.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Button(
                            onClick = { showComposer = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_community_composer_button"),
                            contentPadding = PaddingValues(vertical = 13.dp)
                        ) {
                            Icon(Icons.Rounded.Lightbulb, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Share an Idea, Question, or Collaboration Request")
                        }
                    }
                }
            }

            // Gemini AI 'Suggested for You' Section (skills + mission history analysis)
            item(key = "suggested_for_you_section") {
                SuggestedForYouSection(
                    state = suggestedForYouState,
                    bookmarkedPostIds = bookmarkedPostIds,
                    onRefresh = onRefreshSuggestedForYou,
                    onOpenPost = { post ->
                        onSelectCategory(null)
                        onSelectFeedMode(CommunityFeedMode.ALL)
                        val targetIdx = posts.indexOfFirst { it.id == post.id }
                        if (targetIdx >= 0) {
                            val headerCount = 6 +
                                (if (memberSearchQuery.isNotBlank()) 1 else 0) +
                                (if (followedTopics.isNotEmpty()) 1 else 0)
                            coroutineScope.launch {
                                listState.animateScrollToItem(headerCount + targetIdx)
                            }
                        }
                    },
                    onToggleBookmarkPost = onToggleBookmark,
                    onExpressCollaborationInterest = onExpressMissionCollaborationInterest,
                    onOpenMissionWorkspace = onOpenMissionTab
                )
            }

            // Member Discovery Search Bar (capability tags & interests)
            item(key = "member_search_bar") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("community_member_search_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Find Community Members",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedTextField(
                            value = memberSearchQuery,
                            onValueChange = onMemberSearchQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("community_member_search_input"),
                            label = { Text("Search by capability tag or interest") },
                            placeholder = { Text("e.g., Python, Visual design, Assistive tech") },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                            singleLine = true,
                            trailingIcon = {
                                if (memberSearchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = onClearMemberSearch,
                                        modifier = Modifier.testTag("community_member_search_clear")
                                    ) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Clear member search")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(quickMemberTags, key = { it.lowercase() }) { tag ->
                                val selected = memberSearchQuery.equals(tag, ignoreCase = true)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        if (selected) onClearMemberSearch() else onMemberSearchQueryChange(tag)
                                    },
                                    label = { Text(tag) },
                                    modifier = Modifier.testTag("community_member_quick_tag_${tag.lowercase().replace(Regex("[^a-z0-9]+"), "_")}")
                                )
                            }
                        }
                    }
                }
            }

            // Matching Members Results Panel
            if (memberSearchQuery.isNotBlank()) {
                item(key = "member_search_results") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("community_member_results_section"),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Members matching \"$memberSearchQuery\" (${matchedMembers.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onClearMemberSearch) {
                                Text("Clear")
                            }
                        }

                        if (matchedMembers.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "No members matched \"$memberSearchQuery\" yet.",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Try a broader capability tag or interest chip above, or switch to Intent Picks below.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            matchedMembers.forEach { member ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("community_member_card_${member.userId}"),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            MascotBadgeAvatar(isDuck = true)
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = member.displayName,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (member.headline.isNotBlank()) {
                                                    Text(
                                                        text = member.headline,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Schedule,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(12.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = AvailabilityWindow.safeParse(member.availability).label,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }

                                        val caps = member.allCapabilities().take(6)
                                        if (caps.isNotEmpty()) {
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                caps.forEach { cap ->
                                                    val matchesQuery = cap.contains(memberSearchQuery.trim(), ignoreCase = true)
                                                    Surface(
                                                        shape = RoundedCornerShape(50),
                                                        color = if (matchesQuery) {
                                                            MaterialTheme.colorScheme.primaryContainer
                                                        } else {
                                                            MaterialTheme.colorScheme.surfaceVariant
                                                        }
                                                    ) {
                                                        Text(
                                                            text = cap,
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontWeight = if (matchesQuery) FontWeight.Bold else FontWeight.Medium
                                                            ),
                                                            color = if (matchesQuery) {
                                                                MaterialTheme.colorScheme.onPrimaryContainer
                                                            } else {
                                                                MaterialTheme.colorScheme.onSurfaceVariant
                                                            },
                                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (member.interests.isNotEmpty()) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Interests,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "Interests: ${member.interests.take(3).joinToString(" • ")}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.secondary,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { onOpenMemberProfile(member) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                                    .testTag("community_view_member_profile_${member.userId}")
                                            ) {
                                                Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text("View Profile")
                                            }
                                            Button(
                                                onClick = { onMessageMember(member) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                                    .testTag("community_message_member_${member.userId}")
                                            ) {
                                                Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text("Message")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Feed Mode Selector (All / Intent Picks / Following / Saved)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CommunityFeedMode.entries.forEach { mode ->
                        val selected = feedMode == mode
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectFeedMode(mode) },
                            label = { Text(mode.label) },
                            leadingIcon = if (mode == CommunityFeedMode.INTENT_DISCOVERY) {
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.Psychology,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            modifier = Modifier.testTag("community_mode_${mode.name.lowercase()}")
                        )
                    }
                }
            }

            // Intent-Aware Community Discovery Bar (Section 19.3)
            item {
                AnimatedVisibility(visible = feedMode == CommunityFeedMode.INTENT_DISCOVERY) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Intent-Aware Community Discovery",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Matches posts by underlying mission purpose, complementary skills you bring, and problems you're exploring.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = intentQuery,
                                onValueChange = onIntentQueryChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("community_intent_query_input"),
                                label = { Text("Focus purpose (optional, e.g., green spaces or tactile audio)") },
                                singleLine = true,
                                trailingIcon = {
                                    if (intentQuery.isNotBlank()) {
                                        IconButton(onClick = { onIntentQueryChange("") }) {
                                            Icon(Icons.Rounded.Close, contentDescription = "Clear intent filter")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Horizontal Scrollable Botanical Green Category Filter Chip Row (Section 19.2)
            item {
                val selectedChipBg = if (isDark) DarkMintPrimary else BotanicalGreen
                val selectedChipFg = if (isDark) DarkBarkText else SoftCream
                val unselectedChipBg = if (isDark) DarkForestSurfaceVariant else SageSurface.copy(alpha = 0.78f)
                val unselectedChipFg = if (isDark) DarkSageText else DeepForestGreen
                val unselectedIconTint = if (isDark) DarkMintPrimary else BotanicalGreen
                val chipBorderColorSelected = if (isDark) DarkMintPrimary else DeepForestGreen
                val chipBorderColorUnselected = if (isDark) BotanicalGreen.copy(alpha = 0.55f) else BotanicalGreen.copy(alpha = 0.34f)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Explore by Category",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isDark) DarkMintPrimary else DeepForestGreen
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("community_category_chip_row"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item(key = "cat_all") {
                            val allSelected = selectedCategory == null
                            FilterChip(
                                selected = allSelected,
                                onClick = { onSelectCategory(null) },
                                label = {
                                    Text(
                                        text = "All",
                                        fontWeight = if (allSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = if (allSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                shape = RoundedCornerShape(50),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = allSelected,
                                    borderColor = chipBorderColorUnselected,
                                    selectedBorderColor = chipBorderColorSelected,
                                    borderWidth = 1.dp,
                                    selectedBorderWidth = 1.2.dp
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = unselectedChipBg,
                                    labelColor = unselectedChipFg,
                                    iconColor = unselectedIconTint,
                                    selectedContainerColor = selectedChipBg,
                                    selectedLabelColor = selectedChipFg,
                                    selectedLeadingIconColor = selectedChipFg
                                ),
                                modifier = Modifier.testTag("post_category_filter_all")
                            )
                        }
                        items(PostCategory.entries, key = { it.name }) { category ->
                            val st = categoryStyle(category)
                            val isSelected = selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectCategory(if (isSelected) null else category) },
                                label = {
                                    Text(
                                        text = category.shortLabel,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = st.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(50),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = chipBorderColorUnselected,
                                    selectedBorderColor = chipBorderColorSelected,
                                    borderWidth = 1.dp,
                                    selectedBorderWidth = 1.2.dp
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = unselectedChipBg,
                                    labelColor = unselectedChipFg,
                                    iconColor = unselectedIconTint,
                                    selectedContainerColor = selectedChipBg,
                                    selectedLabelColor = selectedChipFg,
                                    selectedLeadingIconColor = selectedChipFg
                                ),
                                modifier = Modifier.testTag("post_category_filter_${category.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // Followed Topics Ribbon
            if (followedTopics.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Topics You Follow (tap a tag in any post to follow/unfollow)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            followedTopics.forEach { topic ->
                                FilterChip(
                                    selected = true,
                                    onClick = { onToggleFollowTopic(topic.topic) },
                                    label = { Text("# ${topic.topic}") },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Unfollow ${topic.topic}",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Empty Feed State
            if (filteredPosts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Nothing in this view yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Switch filters or share a new idea, question, or opportunity with the community.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(onClick = {
                                onSelectCategory(null)
                                onSelectFeedMode(CommunityFeedMode.ALL)
                            }) {
                                Text("Show All Posts")
                            }
                        }
                    }
                }
            }

            // Community Posts Feed
            items(filteredPosts, key = { it.id }) { post ->
                val recommendation = recommendationByPostId[post.id]
                val comments = commentsByPostId[post.id].orEmpty()
                val isBookmarked = post.id in bookmarkedPostIds
                val isAppreciated = currentUserId in post.appreciatorUids
                CommunityPostCard(
                    post = post,
                    isOwnPost = post.authorId == currentUserId,
                    initialExpandComments = post.id == focusedPostId,
                    recommendation = recommendation,
                    comments = comments,
                    isBookmarked = isBookmarked,
                    isAppreciated = isAppreciated,
                    followedTopicSet = followedTopicSet,
                    onToggleAppreciation = { onToggleAppreciation(post) },
                    onAddComment = { text, replyTo -> onAddComment(post, text, replyTo) },
                    onToggleBookmark = { onToggleBookmark(post) },
                    onToggleFollowTopic = onToggleFollowTopic,
                    onExpressCollaborationInterest = { onExpressCollaborationInterest(post) },
                    onOpenShareSheet = {
                        shareTarget = post
                        onClearFocusedPost()
                    },
                    onTurnIntoMission = { missionConvertTarget = post },
                    onReport = { reportTarget = post },
                    onBlockAuthor = { onBlockAuthor(post) }
                )
            }

            // Moderation & Dignity Footer (Section 19.6)
            item {
                InfoBannerCard(
                    icon = Icons.Rounded.Shield,
                    title = "Healthy Collaboration Space",
                    body = "SYMBIOSIS prioritizes mutual respect and purpose over vanity metrics. There are no follower counts or popularity rankings. Use the post menu to report harmful content or set personal safety boundaries anytime."
                )
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showComposer = true },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "New Post"
                )
            },
            text = {
                Text(
                    text = "New Post",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            containerColor = if (isDark) DarkMintPrimary else BotanicalGreen,
            contentColor = if (isDark) DarkBarkText else SoftCream,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
                .testTag("community_new_post_fab")
        )
    }

    if (showComposer) {
        CommunityPostComposerModal(
            availableMissions = availableMissions,
            onDismiss = { showComposer = false },
            onPublish = { cat, title, body, tags, reqs, mission, loc, shareLoc, link, imageUri, vis ->
                onCreatePost(cat, title, body, tags, reqs, mission, loc, shareLoc, link, imageUri, vis)
                showComposer = false
            }
        )
    }

    reportTarget?.let { target ->
        var selectedReason by remember { mutableStateOf("Off-topic or unhelpful self-promotion") }
        val reasons = listOf(
            "Off-topic or unhelpful self-promotion",
            "Disrespectful or non-inclusive language",
            "Unsafe in-person request or personal data exposure",
            "Spam or misleading link"
        )
        AlertDialog(
            onDismissRequest = { reportTarget = null },
            title = { Text("Report Community Post") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Flagging \"${target.title}\" removes it from your feed and records a moderation review.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    reasons.forEach { reason ->
                        FilterChip(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                            label = { Text(reason) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onReportPost(target, selectedReason)
                    reportTarget = null
                }) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { reportTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    missionConvertTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { missionConvertTarget = null },
            title = { Text("Turn Post into Collaborative Mission") },
            text = {
                Text(
                    text = "Create a structured SYMBIOSIS Mission from \"${target.title}\" with extracted required capabilities and starter milestones.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            onConvertToMission(target, true)
                            missionConvertTarget = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Create with Gemini AI")
                    }
                    OutlinedButton(
                        onClick = {
                            onConvertToMission(target, false)
                            missionConvertTarget = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create Locally (Instant)")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { missionConvertTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
    shareTarget?.let { target ->
        SharePostWithinSymbiosisModal(
            post = target,
            currentUserId = currentUserId,
            profiles = discoverableProfiles,
            missions = availableMissions,
            onDismiss = { shareTarget = null },
            onShareToPeer = { peer, note ->
                onSharePostToPeer(target, peer, note)
                shareTarget = null
            },
            onShareToMissionGroup = { mission, note ->
                onSharePostToMissionGroup(target, mission, note)
                shareTarget = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CommunityPostCard(
    post: CommunityPost,
    isOwnPost: Boolean,
    initialExpandComments: Boolean = false,
    recommendation: IntentPostRecommendation?,
    comments: List<CommunityComment>,
    isBookmarked: Boolean,
    isAppreciated: Boolean,
    followedTopicSet: Set<String>,
    onToggleAppreciation: () -> Unit,
    onAddComment: (String, String) -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleFollowTopic: (String) -> Unit,
    onExpressCollaborationInterest: () -> Unit,
    onOpenShareSheet: () -> Unit,
    onTurnIntoMission: () -> Unit,
    onReport: () -> Unit,
    onBlockAuthor: () -> Unit
) {
    val category = post.categoryEnum()
    val st = categoryStyle(category)
    val uriHandler = LocalUriHandler.current

    var showComments by remember(post.id, initialExpandComments) { mutableStateOf(initialExpandComments) }
    var commentDraft by remember { mutableStateOf("") }
    var replyTargetAuthor by remember { mutableStateOf("") }
    var showOverflowMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("community_post_card_${post.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (recommendation != null) 1.4.dp else 1.dp,
            color = if (recommendation != null) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.65f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Author & Category Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = st.container,
                        border = BorderStroke(1.dp, st.accent.copy(alpha = 0.45f)),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = post.authorName.firstOrNull()?.uppercase() ?: "S",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = st.accent
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = post.authorName.ifBlank { "Community Member" },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (post.authorHeadline.isNotBlank()) {
                            Text(
                                text = post.authorHeadline,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = st.container
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = st.icon,
                                contentDescription = null,
                                tint = st.accent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = category.shortLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = st.accent
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = { showOverflowMenu = true },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "Post options"
                            )
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Turn into Mission") },
                                leadingIcon = { Icon(Icons.Rounded.RocketLaunch, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    onTurnIntoMission()
                                }
                            )
                            if (!isOwnPost) {
                                DropdownMenuItem(
                                    text = { Text("Report post") },
                                    leadingIcon = { Icon(Icons.Rounded.Flag, contentDescription = null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        onReport()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Block ${post.authorName}") },
                                    leadingIcon = { Icon(Icons.Rounded.Shield, contentDescription = null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        onBlockAuthor()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Intent Relevance Explanation Banner (19.3)
            if (recommendation != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.72f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Intent Match (${recommendation.relevancePercent}%)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = recommendation.whyRelevant,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Title & Body
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Attached Image (via Photo Picker)
            if (post.imageUri.isNotBlank()) {
                AsyncImage(
                    model = post.imageUri,
                    contentDescription = "Community post attachment for ${post.title}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Optional Mission & Location & Link Metadata
            if (post.missionTitle.isNotBlank() || (post.shareLocationTag && post.communityLocationTag.isNotBlank()) || post.linkUrl.isNotBlank()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (post.missionTitle.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.RocketLaunch,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = post.missionTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    if (post.shareLocationTag && post.communityLocationTag.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = "Community location",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = post.communityLocationTag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (post.linkUrl.isNotBlank()) {
                        Surface(
                            onClick = {
                                runCatching { uriHandler.openUri(post.linkUrl) }
                            },
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Link,
                                    contentDescription = "Open shared resource link",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Open Resource Link",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Capability Tags (tap to Follow/Unfollow topic)
            if (post.capabilityTags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    post.capabilityTags.forEach { tag ->
                        val isFollowed = tag.lowercase() in followedTopicSet
                        Surface(
                            onClick = { onToggleFollowTopic(tag) },
                            shape = RoundedCornerShape(50),
                            color = if (isFollowed) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isFollowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Text(
                                text = if (isFollowed) "✓ $tag" else "+ $tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isFollowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Requested Capabilities Callout
            if (post.requestedCapabilities.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ) {
                    Text(
                        text = "Looking for skills: ${post.requestedCapabilities.joinToString(" • ")}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Action Row: Appreciate, Comments, Bookmark, Share, Collaborate / Turn into Mission
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = onToggleAppreciation,
                        modifier = Modifier.testTag("post_appreciate_${post.id}"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isAppreciated) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = if (isAppreciated) "Remove appreciation" else "Appreciate post",
                            tint = if (isAppreciated) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (post.appreciatorUids.isEmpty()) "Appreciate" else "${post.appreciatorUids.size}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    TextButton(
                        onClick = { showComments = !showComments },
                        modifier = Modifier.testTag("post_comments_toggle_${post.id}"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChatBubbleOutline,
                            contentDescription = "Comments",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (comments.isEmpty()) "Discuss" else "${comments.size}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = onOpenShareSheet,
                        modifier = Modifier.testTag("post_share_${post.id}"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share post within SYMBIOSIS",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Share",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("post_bookmark_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Remove bookmark" else "Save post",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Express Collaboration Interest / Turn into Mission buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isOwnPost) {
                    OutlinedButton(
                        onClick = onExpressCollaborationInterest,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("post_collaborate_${post.id}"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Rounded.Handshake, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Collaborate", style = MaterialTheme.typography.labelMedium, maxLines = 1)
                    }
                }
                OutlinedButton(
                    onClick = onTurnIntoMission,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("post_to_mission_${post.id}"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Rounded.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Make Mission", style = MaterialTheme.typography.labelMedium, maxLines = 1)
                }
            }

            // Expandable Comments & Replies Thread
            AnimatedVisibility(visible = showComments) {
                Column(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (comments.isEmpty()) {
                        Text(
                            text = "No replies yet. Start a thoughtful conversation below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        comments.forEach { comment ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (comment.replyToAuthor.isNotBlank()) {
                                                "${comment.authorName} -> ${comment.replyToAuthor}"
                                            } else {
                                                comment.authorName
                                            },
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        TextButton(
                                            onClick = { replyTargetAuthor = comment.authorName },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Icon(Icons.Rounded.Reply, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(Modifier.width(3.dp))
                                            Text("Reply", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                    Text(
                                        text = comment.body,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    if (replyTargetAuthor.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Replying to $replyTargetAuthor",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(
                                onClick = { replyTargetAuthor = "" },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("Clear", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = commentDraft,
                            onValueChange = { commentDraft = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("post_comment_input_${post.id}"),
                            placeholder = { Text("Add a constructive thought…") },
                            maxLines = 3
                        )
                        IconButton(
                            onClick = {
                                if (commentDraft.isNotBlank()) {
                                    onAddComment(commentDraft, replyTargetAuthor)
                                    commentDraft = ""
                                    replyTargetAuthor = ""
                                }
                            },
                            enabled = commentDraft.isNotBlank(),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("post_comment_send_${post.id}")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Send,
                                contentDescription = "Send reply",
                                tint = if (commentDraft.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CommunityPostComposerModal(
    availableMissions: List<Mission>,
    onDismiss: () -> Unit,
    onPublish: (
        category: PostCategory,
        title: String,
        body: String,
        capabilityTags: List<String>,
        requestedCapabilities: List<String>,
        mission: Mission?,
        communityLocationTag: String,
        shareLocationTag: Boolean,
        linkUrl: String,
        imageUri: String,
        visibility: String
    ) -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalSymbiosisIsDark.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var category by remember { mutableStateOf(PostCategory.IDEAS) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var capabilityTagsText by remember { mutableStateOf("") }
    var requestedCapsText by remember { mutableStateOf("") }
    var selectedMission by remember { mutableStateOf<Mission?>(null) }
    var communityLocationTag by remember { mutableStateOf("") }
    var shareLocationTag by remember { mutableStateOf(false) }
    var linkUrl by remember { mutableStateOf("") }
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageStatusMessage by remember { mutableStateOf<String?>(null) }
    var visibility by remember { mutableStateOf("COMMUNITY") }

    val quickCapabilitySuggestions = remember {
        listOf(
            "Programming",
            "Visual design",
            "Audio engineering",
            "Tactile prototyping",
            "Research",
            "Community gardening",
            "Accessibility testing",
            "Storytelling"
        )
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pickedImageUri = uri
            imageStatusMessage = null
        }
    }

    val cameraPreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            pickedImageUri = saveCapturedBitmapToCache(context, bitmap)
            imageStatusMessage = if (pickedImageUri != null) null else "Could not cache captured image."
        }
    }

    val activeChipBg = if (isDark) DarkMintPrimary else BotanicalGreen
    val activeChipFg = if (isDark) DarkBarkText else SoftCream

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("community_new_post_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "New Post",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isDark) DarkMintPrimary else DeepForestGreen
                    )
                    Text(
                        text = category.promptHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close new post modal")
                }
            }

            // Category Selection
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(PostCategory.entries, key = { it.name }) { cat ->
                    val st = categoryStyle(cat)
                    val isSelected = category == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { category = cat },
                        label = { Text(cat.shortLabel) },
                        leadingIcon = {
                            Icon(
                                imageVector = st.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = activeChipBg,
                            selectedLabelColor = activeChipFg,
                            selectedLeadingIconColor = activeChipFg
                        )
                    )
                }
            }

            // Text Content
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Headline or Question *") },
                placeholder = { Text("Give your post a clear, meaningful title") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("composer_post_title_input")
            )

            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Text Content *") },
                placeholder = { Text("Share your idea, question, project progress, or collaboration goal…") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("composer_post_body_input")
            )

            // Image Capture & Photo Attachment
            Text(
                text = "Image Capture & Attachment",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        try {
                            cameraPreviewLauncher.launch(null)
                        } catch (_: ActivityNotFoundException) {
                            imageStatusMessage = "No camera app is available on this device. Try choosing a photo instead."
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("composer_capture_photo_button")
                ) {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Capture Photo", maxLines = 1)
                }
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("composer_pick_photo_button")
                ) {
                    Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (pickedImageUri == null) "Gallery" else "Change", maxLines = 1)
                }
            }

            imageStatusMessage?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            pickedImageUri?.let { uri ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected or captured photo preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        IconButton(
                            onClick = { pickedImageUri = null },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = "Remove attached photo")
                        }
                    }
                }
            }

            // Mission Associations
            if (availableMissions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Mission Association",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.testTag("composer_mission_association_row")
                    ) {
                        item(key = "mission_none") {
                            FilterChip(
                                selected = selectedMission == null,
                                onClick = { selectedMission = null },
                                label = { Text("No Mission") },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = activeChipBg,
                                    selectedLabelColor = activeChipFg
                                )
                            )
                        }
                        items(availableMissions, key = { it.id }) { mission ->
                            val isSelected = selectedMission?.id == mission.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedMission = if (isSelected) null else mission },
                                label = {
                                    Text(
                                        text = mission.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.RocketLaunch,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = activeChipBg,
                                    selectedLabelColor = activeChipFg,
                                    selectedLeadingIconColor = activeChipFg
                                )
                            )
                        }
                    }
                }
            }

            // Capability Tags (quick chips + editable field)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Capability Tags",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                val currentTagList = remember(capabilityTagsText) {
                    capabilityTagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickCapabilitySuggestions.forEach { tag ->
                        val isAdded = currentTagList.any { it.equals(tag, ignoreCase = true) }
                        FilterChip(
                            selected = isAdded,
                            onClick = {
                                val updated = if (isAdded) {
                                    currentTagList.filterNot { it.equals(tag, ignoreCase = true) }
                                } else {
                                    currentTagList + tag
                                }
                                capabilityTagsText = updated.joinToString(", ")
                            },
                            label = { Text(tag) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = activeChipBg,
                                selectedLabelColor = activeChipFg
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = capabilityTagsText,
                    onValueChange = { capabilityTagsText = it },
                    label = { Text("Capability tags (comma-separated)") },
                    placeholder = { Text("e.g., Programming, Visual design") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("composer_capability_tags_input")
                )

                OutlinedTextField(
                    value = requestedCapsText,
                    onValueChange = { requestedCapsText = it },
                    label = { Text("Looking for skills (optional)") },
                    placeholder = { Text("e.g., Audio engineering, Research") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("composer_requested_skills_input")
                )
            }

            OutlinedTextField(
                value = linkUrl,
                onValueChange = { linkUrl = it },
                label = { Text("Resource link URL (optional)") },
                placeholder = { Text("https://…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Privacy-controlled optional location tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Share Community / Location Tag", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "Opt-in only — stays hidden unless toggled on.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = shareLocationTag,
                    onCheckedChange = { shareLocationTag = it }
                )
            }

            AnimatedVisibility(visible = shareLocationTag) {
                OutlinedTextField(
                    value = communityLocationTag,
                    onValueChange = { communityLocationTag = it },
                    label = { Text("Neighborhood or hub label") },
                    placeholder = { Text("e.g., Maple Lane Commons") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = visibility == "COMMUNITY",
                    onClick = { visibility = "COMMUNITY" },
                    label = { Text("Public Community") }
                )
                FilterChip(
                    selected = visibility == "CONNECTIONS",
                    onClick = { visibility = "CONNECTIONS" },
                    label = { Text("Connections Only") }
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val tags = capabilityTagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val reqs = requestedCapsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        onPublish(
                            category,
                            title,
                            body,
                            tags,
                            reqs,
                            selectedMission,
                            communityLocationTag,
                            shareLocationTag,
                            linkUrl,
                            pickedImageUri?.toString().orEmpty(),
                            visibility
                        )
                    },
                    enabled = title.trim().length >= 3 && body.trim().length >= 5,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = activeChipBg,
                        contentColor = activeChipFg
                    ),
                    modifier = Modifier
                        .weight(1.6f)
                        .testTag("composer_publish_post_button")
                ) {
                    Text("Publish Post", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun saveCapturedBitmapToCache(context: Context, bitmap: Bitmap): Uri? =
    runCatching {
        val dir = File(context.cacheDir, "community_captures").apply { mkdirs() }
        val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        Uri.fromFile(file)
    }.getOrNull()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SharePostWithinSymbiosisModal(
    post: CommunityPost,
    currentUserId: String,
    profiles: List<CommunityProfile>,
    missions: List<Mission>,
    onDismiss: () -> Unit,
    onShareToPeer: (CommunityProfile, String) -> Unit,
    onShareToMissionGroup: (Mission, String) -> Unit
) {
    val isDark = LocalSymbiosisIsDark.current
    val clipboard = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var note by remember { mutableStateOf("") }
    var copiedNotice by remember { mutableStateOf(false) }

    val postLink = remember(post.id, post.linkUrl) {
        post.linkUrl.trim().ifBlank { "symbiosis://community/post/${post.id}" }
    }
    val recipients = remember(profiles, currentUserId) {
        profiles.filter { it.userId != currentUserId }
    }
    val activeChipBg = if (isDark) DarkMintPrimary else BotanicalGreen
    val activeChipFg = if (isDark) DarkBarkText else SoftCream

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("community_share_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Share within SYMBIOSIS",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isDark) DarkMintPrimary else DeepForestGreen
                    )
                    Text(
                        text = "Send this thread or link to collaborators or mission groups.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close share sheet")
                }
            }

            // Post & Link Preview
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "By ${post.authorName} • ${post.categoryEnum().shortLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = postLink,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                val textToCopy = "${post.title}\n$postLink"
                                clipboard.setText(AnnotatedString(textToCopy))
                                copiedNotice = true
                            },
                            modifier = Modifier.testTag("share_copy_link_button"),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (copiedNotice) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(if (copiedNotice) "Copied" else "Copy Link", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Add an optional message") },
                placeholder = { Text("e.g., Thought this aligns with our next milestone!") },
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("share_personal_note_input")
            )

            // Share to Collaborators / Users
            Text(
                text = "Share with a Collaborator",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (recipients.isEmpty()) {
                Text(
                    text = "No peers loaded yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recipients.take(5).forEach { peer ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = peer.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (peer.headline.isNotBlank()) {
                                        Text(
                                            text = peer.headline,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Button(
                                    onClick = { onShareToPeer(peer, note) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = activeChipBg,
                                        contentColor = activeChipFg
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("share_to_peer_${peer.userId}")
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Send", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            // Share to Mission Groups
            if (missions.isNotEmpty()) {
                Text(
                    text = "Share to a Mission Group",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    missions.take(5).forEach { mission ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Groups,
                                    contentDescription = null,
                                    tint = if (isDark) DarkMintPrimary else BotanicalGreen
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mission.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${mission.memberUids.size.coerceAtLeast(1)} collaborator(s)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                OutlinedButton(
                                    onClick = { onShareToMissionGroup(mission, note) },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("share_to_group_${mission.id}")
                                ) {
                                    Text("Share Group", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuggestedForYouSection(
    state: UiState<List<SuggestedForYouItem>>,
    bookmarkedPostIds: Set<String>,
    onRefresh: () -> Unit,
    onOpenPost: (CommunityPost) -> Unit,
    onToggleBookmarkPost: (CommunityPost) -> Unit,
    onExpressCollaborationInterest: (Mission) -> Unit,
    onOpenMissionWorkspace: () -> Unit
) {
    val isDark = LocalSymbiosisIsDark.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("community_suggested_for_you_section"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Suggested for You",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            AiBadge("Gemini AI")
                        }
                        Text(
                            text = "Analyzed from your skills and mission history",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    enabled = state !is UiState.Loading,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("community_suggested_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh Suggested for You recommendations",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            when (state) {
                is UiState.Loading -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("community_suggested_loading")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                            Text(
                                text = "Gemini is matching your skills & mission history with community threads and open collaborations...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("community_suggested_error")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Gemini recommendations couldn't load",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            OutlinedButton(
                                onClick = onRefresh,
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("community_suggested_retry_button")
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Retry")
                            }
                        }
                    }
                }

                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Add more skills or explore posts to unlock tailored Gemini picks.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(
                                    onClick = onRefresh,
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text("Analyze")
                                }
                            }
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = state.data,
                                key = { "${it.recommendation.targetType.name}_${it.recommendation.targetId}" }
                            ) { item ->
                                val rec = item.recommendation
                                val isPost = item is SuggestedForYouItem.PostSuggestion
                                val cardAccent = if (isPost) {
                                    if (isDark) DarkMintPrimary else BotanicalGreen
                                } else {
                                    WarmGoldenYellow
                                }

                                Surface(
                                    modifier = Modifier
                                        .width(292.dp)
                                        .testTag("suggested_item_${rec.targetId}"),
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = cardAccent.copy(alpha = 0.18f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isPost) Icons.Rounded.Forum else Icons.Rounded.RocketLaunch,
                                                        contentDescription = null,
                                                        tint = if (!isPost && !isDark) DeepForestGreen else cardAccent,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Text(
                                                        text = if (isPost) "Community Post" else "Collaboration",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = if (!isPost && !isDark) DeepForestGreen else cardAccent
                                                    )
                                                }
                                            }
                                            Text(
                                                text = rec.relevanceSummary,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.secondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        when (item) {
                                            is SuggestedForYouItem.PostSuggestion -> {
                                                Text(
                                                    text = item.post.title,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "By ${item.post.authorName} • ${item.post.categoryEnum().shortLabel}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            is SuggestedForYouItem.CollaborationSuggestion -> {
                                                Text(
                                                    text = item.mission.title,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "Led by ${item.mission.ownerName} • ${item.mission.memberUids.size}/${item.mission.targetParticipants} collaborators",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = rec.reason,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 3,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (rec.linkedMissionTitle.isNotBlank()) {
                                                    Text(
                                                        text = "Connects with: ${rec.linkedMissionTitle}",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                                        color = MaterialTheme.colorScheme.primary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        if (rec.matchedSkills.isNotEmpty()) {
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                rec.matchedSkills.take(3).forEach { skill ->
                                                    Surface(
                                                        shape = RoundedCornerShape(50),
                                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                                    ) {
                                                        Text(
                                                            text = skill,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        when (item) {
                                            is SuggestedForYouItem.PostSuggestion -> {
                                                val isSaved = item.post.id in bookmarkedPostIds
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Button(
                                                        onClick = { onOpenPost(item.post) },
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(48.dp)
                                                            .testTag("suggested_open_post_${item.post.id}")
                                                    ) {
                                                        Text("View Post")
                                                    }
                                                    IconButton(
                                                        onClick = { onToggleBookmarkPost(item.post) },
                                                        modifier = Modifier.size(48.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                                            contentDescription = if (isSaved) "Remove bookmark" else "Save post",
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            }
                                            is SuggestedForYouItem.CollaborationSuggestion -> {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Button(
                                                        onClick = { onExpressCollaborationInterest(item.mission) },
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(48.dp)
                                                            .testTag("suggested_join_mission_${item.mission.id}")
                                                    ) {
                                                        Text("Express Interest")
                                                    }
                                                    OutlinedButton(
                                                        onClick = onOpenMissionWorkspace,
                                                        modifier = Modifier.height(48.dp)
                                                    ) {
                                                        Text("Missions")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


