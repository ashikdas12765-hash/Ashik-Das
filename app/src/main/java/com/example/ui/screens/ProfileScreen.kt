package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.i18n.LocalAppLanguage
import com.example.i18n.LocalAppStrings
import com.example.model.MediaType
import com.example.model.SocialPost
import com.example.model.UserProfile
import com.example.ui.components.FollowPillButton
import com.example.ui.components.GradientButton
import com.example.ui.components.LanguageToggleButton
import com.example.ui.components.UserAvatar
import com.example.ui.components.formatCompactCount
import com.example.ui.theme.FriendZoneGradients
import com.example.ui.theme.FzPink
import com.example.ui.theme.FzPurple
import com.example.ui.theme.VerifiedBlue

private enum class ProfileMomentsTab {
    MOMENTS,
    REELS,
    SAVED
}

@Composable
fun ProfileScreen(
    profile: UserProfile,
    posts: List<SocialPost>,
    isOwnProfile: Boolean,
    savedPosts: List<SocialPost> = emptyList(),
    showBackButton: Boolean = false,
    onBack: () -> Unit = {},
    onEditProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFollowToggle: () -> Unit,
    onPostClick: (SocialPost) -> Unit,
    onMessageClick: (() -> Unit)? = null,
    onCreatePostClick: (() -> Unit)? = null,
    onToggleLanguage: (() -> Unit)? = null
) {
    if (showBackButton) {
        BackHandler(onBack = onBack)
    }

    val strings = LocalAppStrings.current
    val currentLanguage = LocalAppLanguage.current
    var activeTab by remember(profile.id, isOwnProfile) { mutableStateOf(ProfileMomentsTab.MOMENTS) }

    val reelPosts = remember(posts) {
        posts.filter { it.isReel || it.mediaType == MediaType.VIDEO }
    }
    val displayedPosts = remember(activeTab, posts, reelPosts, savedPosts) {
        when (activeTab) {
            ProfileMomentsTab.MOMENTS -> posts
            ProfileMomentsTab.REELS -> reelPosts
            ProfileMomentsTab.SAVED -> savedPosts
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        val gridColumns = if (maxWidth >= 600.dp) 4 else 3

        LazyVerticalGrid(
            columns = GridCells.Fixed(gridColumns),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .navigationBarsPadding()
                .testTag("profile_screen_${profile.id}"),
            contentPadding = PaddingValues(bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. Hero Cover Banner + Overlapping User Avatar + Top Controls
            item(key = "profile_hero", span = { GridItemSpan(maxLineSpan) }) {
                ProfileHeroHeader(
                    profile = profile,
                    isOwnProfile = isOwnProfile,
                    showBackButton = showBackButton,
                    currentLanguage = currentLanguage,
                    onBack = onBack,
                    onEditProfileClick = onEditProfileClick,
                    onSettingsClick = onSettingsClick,
                    onFollowToggle = onFollowToggle,
                    onMessageClick = onMessageClick,
                    onToggleLanguage = onToggleLanguage
                )
            }

            // 2. Display Name, @Username, Bio Card, Stats Bar, and Quick Actions
            item(key = "profile_identity_and_bio", span = { GridItemSpan(maxLineSpan) }) {
                ProfileIdentityAndBioSection(
                    profile = profile,
                    postsCount = posts.size,
                    isOwnProfile = isOwnProfile,
                    onEditProfileClick = onEditProfileClick,
                    onCreatePostClick = onCreatePostClick
                )
            }

            // 3. Moments / Reels / Saved Section Tabs
            item(key = "profile_moments_tabs", span = { GridItemSpan(maxLineSpan) }) {
                ProfileMomentsTabBar(
                    activeTab = activeTab,
                    isOwnProfile = isOwnProfile,
                    momentsCount = posts.size,
                    reelsCount = reelPosts.size,
                    savedCount = savedPosts.size,
                    onSelectTab = { activeTab = it }
                )
            }

            // 4. Shared Moments Grid or Contextual Empty State
            if (displayedPosts.isEmpty()) {
                item(key = "profile_empty_state", span = { GridItemSpan(maxLineSpan) }) {
                    ProfileEmptyMomentsState(
                        activeTab = activeTab,
                        isOwnProfile = isOwnProfile,
                        onCreatePostClick = onCreatePostClick
                    )
                }
            } else {
                items(
                    items = displayedPosts,
                    key = { "${activeTab.name}_${it.id}" }
                ) { post ->
                    ProfileGridTile(
                        post = post,
                        onClick = { onPostClick(post) },
                        showEngagementOverlay = true,
                        modifier = Modifier.padding(horizontal = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeroHeader(
    profile: UserProfile,
    isOwnProfile: Boolean,
    showBackButton: Boolean,
    currentLanguage: com.example.i18n.AppLanguage,
    onBack: () -> Unit,
    onEditProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFollowToggle: () -> Unit,
    onMessageClick: (() -> Unit)?,
    onToggleLanguage: (() -> Unit)?
) {
    val strings = LocalAppStrings.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(226.dp)
    ) {
        // Cover Art
        Image(
            painter = painterResource(id = profile.coverResId),
            contentDescription = "Profile cover image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(184.dp)
        )

        // Atmospheric Scrim
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(184.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.48f),
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.55f)
                        )
                    )
                )
        )

        // Top Overlay Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton) {
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.48f),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(42.dp)
                        .testTag("profile_back_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(42.dp))
            }

            if (isOwnProfile) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (onToggleLanguage != null) {
                        LanguageToggleButton(
                            currentLanguage = currentLanguage,
                            onToggle = onToggleLanguage,
                            darkSurface = true,
                            tag = "profile_language_toggle_button"
                        )
                    }
                    Surface(
                        onClick = onSettingsClick,
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.48f),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(42.dp)
                            .testTag("profile_settings_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = strings.settingsTitle,
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Elevated Overlapping User Avatar
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp)
                .offset(y = 12.dp)
                .size(98.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
                .padding(4.dp)
                .testTag("profile_avatar")
        ) {
            UserAvatar(
                fullName = profile.fullName,
                avatarUri = profile.avatarUri,
                gradientIndex = profile.avatarGradientIndex,
                size = 90.dp,
                showStoryRing = true,
                onClick = if (isOwnProfile) onEditProfileClick else null
            )
        }

        // Right Action Button aligned to Avatar baseline
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp)
                .offset(y = 8.dp)
        ) {
            if (isOwnProfile) {
                OutlinedButton(
                    onClick = onEditProfileClick,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.2.dp, FzPurple),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("edit_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = FzPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.editProfile,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = FzPurple
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onMessageClick != null) {
                        OutlinedButton(
                            onClick = onMessageClick,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.2.dp, FzPurple),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("profile_message_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = strings.messageButton,
                                tint = FzPurple,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = strings.messageButton,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = FzPurple
                            )
                        }
                    }
                    FollowPillButton(
                        isFollowing = profile.isFollowedByCurrentUser,
                        onClick = onFollowToggle,
                        modifier = Modifier.testTag("profile_follow_button")
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileIdentityAndBioSection(
    profile: UserProfile,
    postsCount: Int,
    isOwnProfile: Boolean,
    onEditProfileClick: () -> Unit,
    onCreatePostClick: (() -> Unit)?
) {
    val strings = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 10.dp)
    ) {
        // Display Name & Verified Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.testTag("profile_display_name")
        ) {
            Text(
                text = profile.fullName,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (profile.isVerified) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Verified",
                    tint = VerifiedBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // @Username Handle
        Text(
            text = "@${profile.username}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag("profile_username")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Dedicated Bio Section Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isOwnProfile && profile.bio.isBlank()) {
                        Modifier.clickable(onClick = onEditProfileClick)
                    } else {
                        Modifier
                    }
                )
                .testTag("profile_bio_section")
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = strings.bioSectionTitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = profile.bio.ifBlank { strings.addBioPrompt },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (profile.bio.isNotBlank()) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Profile Stats Strip
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            tonalElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_stats_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileStatColumn(
                    value = formatCompactCount(postsCount),
                    label = strings.postsStat
                )
                VerticalDivider(
                    modifier = Modifier.height(28.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                ProfileStatColumn(
                    value = formatCompactCount(profile.followersCount),
                    label = strings.followersStat
                )
                VerticalDivider(
                    modifier = Modifier.height(28.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                ProfileStatColumn(
                    value = formatCompactCount(profile.followingCount),
                    label = strings.followingStat
                )
            }
        }

        if (isOwnProfile && onCreatePostClick != null) {
            Spacer(modifier = Modifier.height(12.dp))
            GradientButton(
                text = "+ ${strings.shareMomentButton}",
                onClick = onCreatePostClick,
                height = 48.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_share_moment_button")
            )
        }
    }
}

@Composable
private fun ProfileMomentsTabBar(
    activeTab: ProfileMomentsTab,
    isOwnProfile: Boolean,
    momentsCount: Int,
    reelsCount: Int,
    savedCount: Int,
    onSelectTab: (ProfileMomentsTab) -> Unit
) {
    val strings = LocalAppStrings.current
    val tabs = remember(isOwnProfile, strings, momentsCount, reelsCount, savedCount) {
        buildList {
            add(Triple(ProfileMomentsTab.MOMENTS, "${strings.tabMoments} ($momentsCount)", Icons.Outlined.GridOn))
            add(Triple(ProfileMomentsTab.REELS, "${strings.navReels} ($reelsCount)", Icons.Outlined.SmartDisplay))
            if (isOwnProfile) {
                add(Triple(ProfileMomentsTab.SAVED, "${strings.tabSaved} ($savedCount)", Icons.Outlined.BookmarkBorder))
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEach { (tab, label, icon) ->
                val selected = tab == activeTab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectTab(tab) }
                        .padding(top = 10.dp)
                        .testTag("profile_tab_${tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (selected) FzPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selected) FzPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            .background(
                                if (selected) FriendZoneGradients.Primary
                                else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun ProfileEmptyMomentsState(
    activeTab: ProfileMomentsTab,
    isOwnProfile: Boolean,
    onCreatePostClick: (() -> Unit)?
) {
    val strings = LocalAppStrings.current
    val (icon, title, subtitle) = when (activeTab) {
        ProfileMomentsTab.MOMENTS -> Triple(
            Icons.Outlined.PhotoLibrary,
            strings.noPostsYet,
            strings.noPostsHint
        )
        ProfileMomentsTab.REELS -> Triple(
            Icons.Outlined.SmartDisplay,
            strings.noReelsYet,
            strings.noPostsHint
        )
        ProfileMomentsTab.SAVED -> Triple(
            Icons.Outlined.BookmarkBorder,
            strings.noSavedYet,
            strings.noSavedHint
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp)
            .testTag("profile_empty_state"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FzPurple,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (isOwnProfile && activeTab != ProfileMomentsTab.SAVED && onCreatePostClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onCreatePostClick,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.2.dp, FzPurple),
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = FzPurple,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = strings.shareMomentButton,
                    color = FzPurple,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProfileStatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileGridTile(
    post: SocialPost,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showEngagementOverlay: Boolean = false
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .testTag("grid_post_${post.id}")
    ) {
        when {
            !post.mediaUri.isNullOrBlank() -> {
                AsyncImage(
                    model = post.mediaUri,
                    contentDescription = post.caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            post.mediaResId != null -> {
                Image(
                    painter = painterResource(id = post.mediaResId),
                    contentDescription = post.caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(FriendZoneGradients.Primary)
                )
            }
        }

        // Top-right media badge (video duration / play pill or saved bookmark)
        if (post.mediaType == MediaType.VIDEO) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.58f))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Video",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
                if (!post.videoDurationLabel.isNullOrBlank()) {
                    Text(
                        text = post.videoDurationLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        } else if (showEngagementOverlay && post.isSaved) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.52f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Bookmark,
                    contentDescription = "Saved",
                    tint = FzPink,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        // Bottom engagement scrim with like count on profile moments grid
        if (showEngagementOverlay) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                        )
                    )
                    .padding(horizontal = 7.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = if (post.isLiked) FzPink else Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = formatCompactCount(post.likesCount),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
