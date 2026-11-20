package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.sp
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.model.PostComment
import com.example.model.SocialPost
import com.example.model.StoryItem
import com.example.model.UserProfile
import com.example.ui.components.FriendZoneLogoBadge
import com.example.ui.components.FriendZoneWordmark
import com.example.ui.components.LanguageToggleButton
import com.example.ui.components.PostCard
import com.example.ui.components.UserAvatar
import com.example.ui.components.sharePostViaSheet
import com.example.ui.theme.FriendZoneGradients
import com.example.ui.theme.FzPinkHot
import com.example.ui.theme.FzPurple

@Composable
fun HomeFeedScreen(
    currentUser: UserProfile,
    currentLanguage: AppLanguage,
    users: List<UserProfile>,
    posts: List<SocialPost>,
    stories: List<StoryItem>,
    commentsByPost: Map<String, List<PostComment>>,
    unreadMessagesCount: Int = 0,
    onToggleLanguage: () -> Unit,
    onCreatePostClick: () -> Unit,
    onMessengerClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStoryClick: (StoryItem) -> Unit,
    onLikeClick: (String) -> Unit,
    onSaveClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onFollowClick: (String) -> Unit,
    onAuthorClick: (String) -> Unit
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val userMap = remember(users) { users.associateBy { it.id } }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .testTag("home_feed_list"),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Branded Top Header
            item(key = "header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FriendZoneLogoBadge(
                        size = 38.dp,
                        modifier = Modifier.testTag("home_brand_logo")
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        FriendZoneWordmark(fontSize = 24.sp)
                        Text(
                            text = strings.tagline,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    LanguageToggleButton(
                        currentLanguage = currentLanguage,
                        onToggle = onToggleLanguage,
                        tag = "home_language_toggle_button"
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onMessengerClick,
                        modifier = Modifier.testTag("header_messenger_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadMessagesCount > 0) {
                                    Badge(
                                        containerColor = FzPinkHot,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = if (unreadMessagesCount > 9) "9+" else "$unreadMessagesCount",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FriendZoneGradients.Primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Send,
                                    contentDescription = strings.messengerTitle,
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.testTag("header_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = strings.settingsTitle,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Stories Strip
            item(key = "stories") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.testTag("stories_row")
                ) {
                    // Current User's "Your Story" Quick Card
                    item(key = "your_story") {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(72.dp)
                                .clickable(onClick = onCreatePostClick)
                                .testTag("your_story_item")
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                UserAvatar(
                                    fullName = currentUser.fullName,
                                    avatarUri = currentUser.avatarUri,
                                    gradientIndex = currentUser.avatarGradientIndex,
                                    size = 64.dp,
                                    showStoryRing = false
                                )
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(FriendZoneGradients.Primary)
                                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = strings.yourStory,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = strings.yourStory,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    items(stories, key = { it.id }) { story ->
                        val storyUser = userMap[story.userId] ?: return@items
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(72.dp)
                                .clickable { onStoryClick(story) }
                                .testTag("story_item_${story.id}")
                        ) {
                            UserAvatar(
                                fullName = storyUser.fullName,
                                avatarUri = storyUser.avatarUri,
                                gradientIndex = storyUser.avatarGradientIndex,
                                size = 66.dp,
                                showStoryRing = true,
                                isStorySeen = story.isSeen
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = storyUser.fullName.substringBefore(" "),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (story.isSeen) FontWeight.Normal else FontWeight.SemiBold
                                ),
                                color = if (story.isSeen) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Feed Posts
            items(posts, key = { it.id }) { post ->
                val author = userMap[post.authorId] ?: currentUser
                val postComments = commentsByPost[post.id].orEmpty()
                val totalComments = postComments.size + postComments.sumOf { it.replies.size }

                PostCard(
                    post = post,
                    author = author,
                    commentsCount = totalComments,
                    currentUserId = currentUser.id,
                    onLikeClick = { onLikeClick(post.id) },
                    onCommentClick = { onCommentClick(post.id) },
                    onShareClick = { sharePostViaSheet(context, author.fullName, post) },
                    onSaveClick = { onSaveClick(post.id) },
                    onFollowClick = { onFollowClick(author.id) },
                    onAuthorClick = { onAuthorClick(author.id) },
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
            }
        }
    }
}

@Composable
fun StoryViewerOverlay(
    story: StoryItem,
    author: UserProfile,
    onClose: () -> Unit,
    onAuthorClick: () -> Unit
) {
    BackHandler(onBack = onClose)

    val progress = remember(story.id) { Animatable(0f) }
    LaunchedEffect(story.id) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 5500, easing = LinearEasing)
        )
        onClose()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(onClick = onClose)
    ) {
        Image(
            painter = painterResource(id = story.mediaResId),
            contentDescription = story.caption,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Top & bottom cinematic scrims
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    fullName = author.fullName,
                    avatarUri = author.avatarUri,
                    gradientIndex = author.avatarGradientIndex,
                    size = 40.dp,
                    onClick = onAuthorClick
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = author.fullName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "@${author.username} • ${story.timestampLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_story_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = LocalAppStrings.current.close,
                        tint = Color.White
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(20.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = FzPurple.copy(alpha = 0.35f)
        ) {
            Text(
                text = story.caption,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
