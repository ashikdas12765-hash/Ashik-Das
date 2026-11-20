package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.i18n.LocalAppStrings
import com.example.model.MediaType
import com.example.model.PostComment
import com.example.model.SocialPost
import com.example.model.UserProfile
import com.example.ui.components.FollowPillButton
import com.example.ui.components.UserAvatar
import com.example.ui.components.sharePostViaSheet
import com.example.ui.theme.FriendZoneGradients
import com.example.ui.theme.FzPink
import com.example.ui.theme.FzPinkHot
import com.example.ui.theme.FzPurple

@Composable
fun ReelsScreen(
    currentUserId: String,
    users: List<UserProfile>,
    posts: List<SocialPost>,
    commentsByPost: Map<String, List<PostComment>>,
    onLikeClick: (String) -> Unit,
    onSaveClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onFollowClick: (String) -> Unit,
    onAuthorClick: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val userMap = remember(users) { users.associateBy { it.id } }

    val reelPosts = remember(posts) {
        val videos = posts.filter { it.isReel || it.mediaType == MediaType.VIDEO }
        if (videos.isNotEmpty()) videos else posts
    }

    val pagerState = rememberPagerState(pageCount = { reelPosts.size.coerceAtLeast(1) })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("reels_screen")
    ) {
        if (reelPosts.isNotEmpty()) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val reel = reelPosts[page]
                val author = userMap[reel.authorId]
                val commentsCount = commentsByPost[reel.id].orEmpty().let { list ->
                    list.size + list.sumOf { it.replies.size }
                }
                var isPlaying by remember(reel.id) { mutableStateOf(true) }

                val infiniteTransition = rememberInfiniteTransition(label = "reel_progress")
                val progress by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 8000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "reel_bar"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { isPlaying = !isPlaying }
                ) {
                    // Full-bleed Visual
                    when {
                        !reel.mediaUri.isNullOrBlank() -> {
                            AsyncImage(
                                model = reel.mediaUri,
                                contentDescription = reel.caption,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        reel.mediaResId != null -> {
                            Image(
                                painter = painterResource(id = reel.mediaResId),
                                contentDescription = reel.caption,
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

                    // Bottom Gradient Scrim
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                    )

                    // Play / Pause Indicator
                    AnimatedVisibility(
                        visible = !isPlaying,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.55f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = strings.tapToPlayPause,
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }

                    // Right Action Rail
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 14.dp, bottom = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ReelActionIcon(
                            icon = if (reel.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            label = "${reel.likesCount}",
                            tint = if (reel.isLiked) FzPinkHot else Color.White,
                            onClick = { onLikeClick(reel.id) },
                            tag = "reel_like_${reel.id}"
                        )
                        ReelActionIcon(
                            icon = Icons.Outlined.ChatBubbleOutline,
                            label = "$commentsCount",
                            tint = Color.White,
                            onClick = { onCommentClick(reel.id) },
                            tag = "reel_comment_${reel.id}"
                        )
                        ReelActionIcon(
                            icon = Icons.AutoMirrored.Outlined.Send,
                            label = strings.sharePost,
                            tint = Color.White,
                            onClick = {
                                sharePostViaSheet(context, author?.fullName ?: "FriendZone", reel)
                            },
                            tag = "reel_share_${reel.id}"
                        )
                        ReelActionIcon(
                            icon = if (reel.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            label = "",
                            tint = if (reel.isSaved) FzPink else Color.White,
                            onClick = { onSaveClick(reel.id) },
                            tag = "reel_save_${reel.id}"
                        )
                        IconButton(onClick = { isPlaying = !isPlaying }) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = strings.tapToPlayPause,
                                tint = Color.White
                            )
                        }
                    }

                    // Bottom-Left Creator & Caption Info
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(0.78f)
                            .padding(start = 16.dp, bottom = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (author != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                UserAvatar(
                                    fullName = author.fullName,
                                    avatarUri = author.avatarUri,
                                    gradientIndex = author.avatarGradientIndex,
                                    size = 40.dp,
                                    showStoryRing = true,
                                    onClick = { onAuthorClick(author.id) }
                                )
                                Text(
                                    text = "@${author.username}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.clickable { onAuthorClick(author.id) }
                                )
                                if (author.id != currentUserId) {
                                    FollowPillButton(
                                        isFollowing = author.isFollowedByCurrentUser,
                                        onClick = { onFollowClick(author.id) }
                                    )
                                }
                            }
                        }

                        Text(
                            text = reel.caption,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (reel.hashtags.isNotEmpty()) {
                            Text(
                                text = reel.hashtags.joinToString(" "),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = FzPink,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.16f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = reel.audioTrack ?: strings.originalAudio,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Bottom Playback Progress Indicator
                    LinearProgressIndicator(
                        progress = { if (isPlaying) progress else 0.35f },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(3.dp),
                        color = FzPink,
                        trackColor = Color.White.copy(alpha = 0.22f)
                    )
                }
            }
        }

        // Top Reels Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.reelsTitle,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(FzPurple.copy(alpha = 0.75f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}/${reelPosts.size.coerceAtLeast(1)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ReelActionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    tag: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.testTag(tag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label.ifEmpty { "Action" },
                tint = tint,
                modifier = Modifier.size(29.dp)
            )
        }
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
        }
    }
}
