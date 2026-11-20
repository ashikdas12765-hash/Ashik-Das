package com.example

import android.content.Context
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppLanguage
import com.example.i18n.LocalAppStrings
import com.example.i18n.LocaleManager
import com.example.i18n.stringsFor
import com.example.ui.components.FriendZoneLogoBadge
import com.example.ui.components.LanguageToggleButton
import com.example.ui.screens.AuthFlowScreen
import com.example.ui.screens.ChatThreadScreen
import com.example.ui.screens.CommentsScreen
import com.example.ui.screens.CreatePostScreen
import com.example.ui.screens.EditProfileScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeFeedScreen
import com.example.ui.screens.MessengerInboxScreen
import com.example.ui.screens.PostDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReelsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StoryViewerOverlay
import com.example.ui.theme.FriendZoneGradients
import com.example.ui.theme.FriendZoneTheme
import com.example.ui.theme.FzPinkHot
import com.example.ui.theme.FzPurple
import com.example.viewmodel.FriendZoneUiState
import com.example.viewmodel.FriendZoneViewModel
import com.example.viewmodel.MainTab
import com.example.viewmodel.OverlayScreen

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.wrapBaseContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: FriendZoneViewModel = viewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            val isDark = state.preferences.isDarkMode
            val language = state.preferences.language
            val isBengali = language == AppLanguage.BENGALI
            val baseContext = LocalContext.current
            val localizedContext = remember(language, baseContext) {
                LocaleManager.syncLanguage(baseContext, language)
                LocaleManager.createLocalizedContext(baseContext, language)
            }

            LaunchedEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    }
                )
            }

            FriendZoneTheme(darkTheme = isDark, isBengali = isBengali) {
                CompositionLocalProvider(
                    LocalContext provides localizedContext,
                    LocalConfiguration provides localizedContext.resources.configuration,
                    LocalAppLanguage provides language,
                    LocalAppStrings provides stringsFor(language)
                ) {
                    FriendZoneAppRoot(
                        state = state,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

private data class NavTabSpec(
    val tab: MainTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
private fun FriendZoneAppRoot(
    state: FriendZoneUiState,
    viewModel: FriendZoneViewModel
) {
    val strings = LocalAppStrings.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        val msg = state.snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeSnackbar()
        }
    }

    val currentUser = state.currentUserSynced
    if (currentUser == null) {
        AuthFlowScreen(
            authMode = state.authMode,
            language = state.preferences.language,
            onToggleLanguage = viewModel::toggleLanguage,
            isValidEmail = viewModel::isValidEmail,
            isValidUsername = viewModel::isValidUsername,
            onSwitchMode = viewModel::switchAuthMode,
            onLogin = viewModel::login,
            onRegister = viewModel::register,
            onDemoSignIn = viewModel::loginDemoPreview
        )
        return
    }

    // Return to Home tab when pressing back from secondary bottom tabs
    if (state.activeOverlay == OverlayScreen.None && state.activeTab != MainTab.HOME) {
        BackHandler { viewModel.selectTab(MainTab.HOME) }
    }

    val navItems = remember(strings) {
        listOf(
            NavTabSpec(MainTab.HOME, strings.navHome, Icons.Filled.Home, Icons.Outlined.Home),
            NavTabSpec(MainTab.SEARCH, strings.navSearch, Icons.Filled.Search, Icons.Outlined.Search),
            NavTabSpec(MainTab.CREATE, strings.navCreate, Icons.Filled.AddBox, Icons.Outlined.AddBox),
            NavTabSpec(MainTab.REELS, strings.navReels, Icons.Filled.SmartDisplay, Icons.Outlined.SmartDisplay),
            NavTabSpec(MainTab.PROFILE, strings.navProfile, Icons.Filled.Person, Icons.Outlined.PersonOutline)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavRail = maxWidth >= 680.dp

            if (useNavRail) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            header = {
                                FriendZoneLogoBadge(
                                    size = 44.dp,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
                                )
                            },
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            navItems.forEach { item ->
                                val selected = state.activeTab == item.tab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(item.tab) },
                                    icon = {
                                        NavItemIcon(item = item, selected = selected)
                                    },
                                    label = {
                                        Text(
                                            text = item.label,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = FzPurple,
                                        selectedTextColor = FzPurple,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${item.tab.name.lowercase()}")
                                )
                            }
                            NavigationRailItem(
                                selected = state.activeOverlay is OverlayScreen.MessengerInbox || state.activeOverlay is OverlayScreen.ChatThread,
                                onClick = viewModel::openMessenger,
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (state.totalUnreadMessages > 0) {
                                                Badge(
                                                    containerColor = FzPinkHot,
                                                    contentColor = Color.White
                                                ) {
                                                    Text(
                                                        text = if (state.totalUnreadMessages > 9) "9+" else "${state.totalUnreadMessages}",
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.Send,
                                            contentDescription = strings.messengerTitle
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = strings.messengerTitle,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = FzPurple,
                                    selectedTextColor = FzPurple,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("rail_messenger_button")
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            LanguageToggleButton(
                                currentLanguage = state.preferences.language,
                                onToggle = viewModel::toggleLanguage,
                                showTranslateIcon = false,
                                tag = "rail_language_toggle_button",
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            MainTabContent(
                                state = state,
                                currentUser = currentUser,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            } else {
                val isReelsTab = state.activeTab == MainTab.REELS
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        NavigationBar(
                            containerColor = if (isReelsTab) Color(0xFF0E0B16) else MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            navItems.forEach { item ->
                                val selected = state.activeTab == item.tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(item.tab) },
                                    icon = {
                                        NavItemIcon(
                                            item = item,
                                            selected = selected,
                                            forceLightTint = isReelsTab
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = if (isReelsTab) Color.White else FzPurple,
                                        selectedTextColor = if (isReelsTab) Color.White else FzPurple,
                                        unselectedIconColor = if (isReelsTab) Color.White.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = if (isReelsTab) Color.White.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = if (isReelsTab) FzPurple.copy(alpha = 0.38f) else MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${item.tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        MainTabContent(
                            state = state,
                            currentUser = currentUser,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        // Full-Screen Overlay Stack (Comments, Other User Profile, Settings, Edit Profile, Story Viewer, Post Detail)
        when (val overlay = state.activeOverlay) {
            OverlayScreen.None -> Unit

            is OverlayScreen.Comments -> {
                val post = state.posts.firstOrNull { it.id == overlay.postId }
                if (post != null) {
                    val author = state.userById(post.authorId) ?: currentUser
                    CommentsScreen(
                        post = post,
                        postAuthor = author,
                        comments = state.commentsByPost[post.id].orEmpty(),
                        onBack = viewModel::popOverlay,
                        onAddComment = { text -> viewModel.addComment(post.id, text) },
                        onReplyToComment = { parentId, text ->
                            viewModel.replyToComment(post.id, parentId, text)
                        },
                        onToggleCommentLike = { commentId ->
                            viewModel.toggleCommentLike(post.id, commentId)
                        },
                        onAuthorClick = { userId -> viewModel.openUserProfile(userId) }
                    )
                }
            }

            is OverlayScreen.UserProfileDetail -> {
                val user = state.userById(overlay.userId)
                if (user != null) {
                    val userPosts = state.posts.filter { it.authorId == user.id }
                    ProfileScreen(
                        profile = user,
                        posts = userPosts,
                        isOwnProfile = user.id == currentUser.id,
                        savedPosts = if (user.id == currentUser.id) state.posts.filter { it.isSaved } else emptyList(),
                        showBackButton = true,
                        onBack = viewModel::popOverlay,
                        onEditProfileClick = { viewModel.openOverlay(OverlayScreen.EditProfile) },
                        onSettingsClick = { viewModel.openOverlay(OverlayScreen.Settings) },
                        onFollowToggle = { viewModel.toggleFollow(user.id) },
                        onPostClick = { post -> viewModel.openOverlay(OverlayScreen.PostDetail(post.id)) },
                        onMessageClick = if (user.id != currentUser.id) {
                            { viewModel.openChatWithUser(user.id) }
                        } else null,
                        onCreatePostClick = if (user.id == currentUser.id) {
                            { viewModel.selectTab(MainTab.CREATE) }
                        } else null,
                        onToggleLanguage = if (user.id == currentUser.id) viewModel::toggleLanguage else null
                    )
                }
            }

            OverlayScreen.Settings -> {
                SettingsScreen(
                    preferences = state.preferences,
                    onBack = viewModel::popOverlay,
                    onEditAccount = { viewModel.openOverlay(OverlayScreen.EditProfile) },
                    onSelectLanguage = viewModel::setLanguage,
                    onToggleLanguage = viewModel::toggleLanguage,
                    onToggleDarkMode = viewModel::setDarkMode,
                    onUpdatePrivacy = viewModel::updatePrivacy,
                    onLogout = viewModel::logout
                )
            }

            OverlayScreen.EditProfile -> {
                EditProfileScreen(
                    user = currentUser,
                    isValidUsername = viewModel::isValidUsername,
                    onBack = viewModel::popOverlay,
                    onSave = { name, handle, bio, avatarUri ->
                        viewModel.updateProfile(
                            fullName = name,
                            username = handle,
                            bio = bio,
                            avatarUri = avatarUri,
                            successMessage = strings.profileUpdated
                        )
                    }
                )
            }

            is OverlayScreen.StoryViewer -> {
                val story = state.stories.firstOrNull { it.id == overlay.storyId }
                if (story != null) {
                    val author = state.userById(story.userId) ?: currentUser
                    StoryViewerOverlay(
                        story = story,
                        author = author,
                        onClose = viewModel::popOverlay,
                        onAuthorClick = {
                            viewModel.popOverlay()
                            viewModel.openUserProfile(author.id)
                        }
                    )
                }
            }

            is OverlayScreen.PostDetail -> {
                val post = state.posts.firstOrNull { it.id == overlay.postId }
                if (post != null) {
                    val author = state.userById(post.authorId) ?: currentUser
                    PostDetailScreen(
                        post = post,
                        author = author,
                        comments = state.commentsByPost[post.id].orEmpty(),
                        currentUserId = currentUser.id,
                        onBack = viewModel::popOverlay,
                        onLikeClick = { viewModel.toggleLike(post.id) },
                        onSaveClick = { viewModel.toggleSave(post.id) },
                        onCommentClick = { viewModel.openOverlay(OverlayScreen.Comments(post.id)) },
                        onFollowClick = { viewModel.toggleFollow(author.id) },
                        onAuthorClick = { viewModel.openUserProfile(author.id) }
                    )
                }
            }

            OverlayScreen.MessengerInbox -> {
                MessengerInboxScreen(
                    currentUser = currentUser,
                    users = state.users,
                    conversations = state.conversations,
                    messagesByConversation = state.messagesByConversation,
                    onBack = viewModel::popOverlay,
                    onOpenConversation = viewModel::openConversation,
                    onStartChatWithUser = viewModel::startOrSelectConversationForUser,
                    onMarkConversationRead = viewModel::markConversationRead,
                    onOpenProfile = viewModel::openUserProfile,
                    onSendMessage = viewModel::sendMessage,
                    onToggleReaction = viewModel::toggleMessageReaction,
                    onDeleteMessage = { convId, msgId ->
                        viewModel.deleteMessage(convId, msgId, strings.messageDeleted)
                    },
                    onTogglePin = viewModel::togglePinConversation,
                    onToggleMute = viewModel::toggleMuteConversation
                )
            }

            is OverlayScreen.ChatThread -> {
                val conversation = state.conversations.firstOrNull { it.id == overlay.conversationId }
                val participant = conversation?.let { state.userById(it.participantId) }
                if (conversation != null && participant != null) {
                    ChatThreadScreen(
                        conversation = conversation,
                        participant = participant,
                        currentUser = currentUser,
                        messages = state.messagesByConversation[conversation.id].orEmpty(),
                        onBack = viewModel::popOverlay,
                        onOpenProfile = viewModel::openUserProfile,
                        onSendMessage = { text, uri, replyId ->
                            viewModel.sendMessage(conversation.id, text, uri, replyId)
                        },
                        onToggleReaction = { msgId, emoji ->
                            viewModel.toggleMessageReaction(conversation.id, msgId, emoji)
                        },
                        onDeleteMessage = { msgId ->
                            viewModel.deleteMessage(conversation.id, msgId, strings.messageDeleted)
                        },
                        onTogglePin = { viewModel.togglePinConversation(conversation.id) },
                        onToggleMute = { viewModel.toggleMuteConversation(conversation.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NavItemIcon(
    item: NavTabSpec,
    selected: Boolean,
    forceLightTint: Boolean = false
) {
    if (item.tab == MainTab.CREATE && selected) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(FriendZoneGradients.Primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.selectedIcon,
                contentDescription = item.label,
                tint = Color.White,
                modifier = Modifier.size(19.dp)
            )
        }
    } else {
        Icon(
            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            tint = when {
                selected && forceLightTint -> Color.White
                selected -> FzPurple
                forceLightTint -> Color.White.copy(alpha = 0.7f)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun MainTabContent(
    state: FriendZoneUiState,
    currentUser: com.example.model.UserProfile,
    viewModel: FriendZoneViewModel
) {
    val strings = LocalAppStrings.current

    AnimatedContent(
        targetState = state.activeTab,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "main_tab_transition"
    ) { tab ->
        when (tab) {
            MainTab.HOME -> HomeFeedScreen(
                currentUser = currentUser,
                currentLanguage = state.preferences.language,
                users = state.users,
                posts = state.posts,
                stories = state.stories,
                commentsByPost = state.commentsByPost,
                unreadMessagesCount = state.totalUnreadMessages,
                onToggleLanguage = viewModel::toggleLanguage,
                onCreatePostClick = { viewModel.selectTab(MainTab.CREATE) },
                onMessengerClick = viewModel::openMessenger,
                onSettingsClick = { viewModel.openOverlay(OverlayScreen.Settings) },
                onStoryClick = { story -> viewModel.openOverlay(OverlayScreen.StoryViewer(story.id)) },
                onLikeClick = viewModel::toggleLike,
                onSaveClick = viewModel::toggleSave,
                onCommentClick = { postId -> viewModel.openOverlay(OverlayScreen.Comments(postId)) },
                onFollowClick = viewModel::toggleFollow,
                onAuthorClick = viewModel::openUserProfile
            )

            MainTab.SEARCH -> ExploreScreen(
                currentUserId = currentUser.id,
                users = state.users,
                posts = state.posts,
                onUserClick = viewModel::openUserProfile,
                onFollowClick = viewModel::toggleFollow,
                onPostClick = { post -> viewModel.openOverlay(OverlayScreen.PostDetail(post.id)) }
            )

            MainTab.CREATE -> CreatePostScreen(
                onPublish = { mediaType, resId, uri, caption, hashtags ->
                    viewModel.publishPost(
                        mediaType = mediaType,
                        mediaResId = resId,
                        mediaUri = uri,
                        caption = caption,
                        rawHashtags = hashtags,
                        successMessage = strings.postPublished
                    )
                }
            )

            MainTab.REELS -> ReelsScreen(
                currentUserId = currentUser.id,
                users = state.users,
                posts = state.posts,
                commentsByPost = state.commentsByPost,
                onLikeClick = viewModel::toggleLike,
                onSaveClick = viewModel::toggleSave,
                onCommentClick = { postId -> viewModel.openOverlay(OverlayScreen.Comments(postId)) },
                onFollowClick = viewModel::toggleFollow,
                onAuthorClick = viewModel::openUserProfile
            )

            MainTab.PROFILE -> ProfileScreen(
                profile = currentUser,
                posts = state.posts.filter { it.authorId == currentUser.id },
                isOwnProfile = true,
                savedPosts = state.posts.filter { it.isSaved },
                onEditProfileClick = { viewModel.openOverlay(OverlayScreen.EditProfile) },
                onSettingsClick = { viewModel.openOverlay(OverlayScreen.Settings) },
                onFollowToggle = {},
                onPostClick = { post -> viewModel.openOverlay(OverlayScreen.PostDetail(post.id)) },
                onCreatePostClick = { viewModel.selectTab(MainTab.CREATE) },
                onToggleLanguage = viewModel::toggleLanguage
            )
        }
    }
}
