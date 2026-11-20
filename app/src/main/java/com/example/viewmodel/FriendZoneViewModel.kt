package com.example.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.contract.AuthRepository
import com.example.data.contract.MediaStorageRepository
import com.example.data.contract.MessengerRepository
import com.example.data.contract.SocialFeedRepository
import com.example.data.local.LocalPreviewAuthRepository
import com.example.data.local.LocalPreviewMediaStorageRepository
import com.example.data.local.LocalPreviewMessengerRepository
import com.example.data.local.LocalPreviewSocialRepository
import com.example.data.local.UserPreferencesStore
import com.example.i18n.AppLanguage
import com.example.i18n.LocaleManager
import com.example.model.AppPreferences
import com.example.model.ChatConversation
import com.example.model.ChatMessage
import com.example.model.MediaType
import com.example.model.PostComment
import com.example.model.PrivacySettings
import com.example.model.SocialPost
import com.example.model.StoryItem
import com.example.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MainTab {
    HOME,
    SEARCH,
    CREATE,
    REELS,
    PROFILE
}

enum class AuthMode {
    LOGIN,
    REGISTER
}

sealed interface OverlayScreen {
    data object None : OverlayScreen
    data class Comments(val postId: String) : OverlayScreen
    data class UserProfileDetail(val userId: String) : OverlayScreen
    data object Settings : OverlayScreen
    data object EditProfile : OverlayScreen
    data class StoryViewer(val storyId: String) : OverlayScreen
    data class PostDetail(val postId: String) : OverlayScreen
    data object MessengerInbox : OverlayScreen
    data class ChatThread(val conversationId: String) : OverlayScreen
}

data class FriendZoneUiState(
    val currentUser: UserProfile? = null,
    val authMode: AuthMode = AuthMode.LOGIN,
    val activeTab: MainTab = MainTab.HOME,
    val overlayStack: List<OverlayScreen> = emptyList(),
    val users: List<UserProfile> = emptyList(),
    val posts: List<SocialPost> = emptyList(),
    val commentsByPost: Map<String, List<PostComment>> = emptyMap(),
    val stories: List<StoryItem> = emptyList(),
    val conversations: List<ChatConversation> = emptyList(),
    val messagesByConversation: Map<String, List<ChatMessage>> = emptyMap(),
    val preferences: AppPreferences = AppPreferences(),
    val snackbarMessage: String? = null
) {
    val activeOverlay: OverlayScreen
        get() = overlayStack.lastOrNull() ?: OverlayScreen.None

    val currentUserSynced: UserProfile?
        get() = currentUser?.let { me -> users.firstOrNull { it.id == me.id } ?: me }

    val totalUnreadMessages: Int
        get() = conversations.sumOf { it.unreadCount }

    fun userById(id: String): UserProfile? = users.firstOrNull { it.id == id }
}

class FriendZoneViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesStore = UserPreferencesStore(application)
    private val socialRepository: SocialFeedRepository = LocalPreviewSocialRepository()
    private val authRepository: AuthRepository = LocalPreviewAuthRepository(socialRepository)
    private val mediaStorageRepository: MediaStorageRepository = LocalPreviewMediaStorageRepository()
    private val messengerRepository: MessengerRepository = LocalPreviewMessengerRepository()

    private val _navState = MutableStateFlow(NavState())

    private data class NavState(
        val authMode: AuthMode = AuthMode.LOGIN,
        val activeTab: MainTab = MainTab.HOME,
        val overlayStack: List<OverlayScreen> = emptyList(),
        val snackbarMessage: String? = null
    )

    private data class SocialBundle(
        val users: List<UserProfile>,
        val posts: List<SocialPost>,
        val comments: Map<String, List<PostComment>>,
        val stories: List<StoryItem>
    )

    private data class MessengerBundle(
        val conversations: List<ChatConversation>,
        val messagesByConversation: Map<String, List<ChatMessage>>
    )

    private val socialBundleFlow = combine(
        socialRepository.users,
        socialRepository.posts,
        socialRepository.commentsByPost,
        socialRepository.stories
    ) { users, posts, comments, stories ->
        SocialBundle(users, posts, comments, stories)
    }

    private val messengerBundleFlow = combine(
        messengerRepository.conversations,
        messengerRepository.messagesByConversation
    ) { conversations, messages ->
        MessengerBundle(conversations, messages)
    }

    private val initialLanguage: AppLanguage = LocaleManager.getSavedLanguage(application)

    val uiState: StateFlow<FriendZoneUiState> = combine(
        authRepository.currentUser,
        _navState,
        socialBundleFlow,
        messengerBundleFlow,
        preferencesStore.preferencesFlow
    ) { user, nav, social, messenger, prefs ->
        LocaleManager.syncLanguage(getApplication(), prefs.language)
        FriendZoneUiState(
            currentUser = user,
            authMode = nav.authMode,
            activeTab = nav.activeTab,
            overlayStack = nav.overlayStack,
            users = social.users,
            posts = social.posts,
            commentsByPost = social.comments,
            stories = social.stories,
            conversations = messenger.conversations,
            messagesByConversation = messenger.messagesByConversation,
            preferences = prefs,
            snackbarMessage = nav.snackbarMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FriendZoneUiState(
            preferences = AppPreferences(language = initialLanguage)
        )
    )

    // --- Validation Helpers ---
    fun isValidEmail(email: String): Boolean =
        email.trim().isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    fun isValidUsername(username: String): Boolean {
        val clean = username.trim().removePrefix("@")
        return clean.matches(Regex("^[a-z0-9._]{3,20}$"))
    }

    // --- Auth Actions ---
    fun switchAuthMode(mode: AuthMode) {
        _navState.update { it.copy(authMode = mode) }
    }

    fun login(email: String, password: String): Boolean {
        if (!isValidEmail(email) || password.length < 6) return false
        val result = authRepository.signInLocalPreview(email, password)
        if (result.isSuccess) {
            _navState.update {
                it.copy(activeTab = MainTab.HOME, overlayStack = emptyList())
            }
            return true
        }
        return false
    }

    fun register(fullName: String, username: String, email: String, password: String): Boolean {
        if (fullName.trim().length < 2 || !isValidUsername(username) || !isValidEmail(email) || password.length < 6) {
            return false
        }
        val result = authRepository.registerLocalPreview(fullName, username, email, password)
        if (result.isSuccess) {
            _navState.update {
                it.copy(activeTab = MainTab.HOME, overlayStack = emptyList())
            }
            return true
        }
        return false
    }

    fun loginDemoPreview() {
        authRepository.signInDemoPreview()
        _navState.update {
            it.copy(activeTab = MainTab.HOME, overlayStack = emptyList())
        }
    }

    fun logout() {
        authRepository.signOut()
        _navState.update {
            it.copy(
                authMode = AuthMode.LOGIN,
                activeTab = MainTab.HOME,
                overlayStack = emptyList()
            )
        }
    }

    // --- Navigation Actions ---
    fun selectTab(tab: MainTab) {
        _navState.update { it.copy(activeTab = tab, overlayStack = emptyList()) }
    }

    fun openOverlay(overlay: OverlayScreen) {
        if (overlay is OverlayScreen.StoryViewer) {
            socialRepository.markStorySeen(overlay.storyId)
        }
        _navState.update { state ->
            state.copy(overlayStack = state.overlayStack + overlay)
        }
    }

    fun openUserProfile(userId: String) {
        val me = uiState.value.currentUserSynced
        if (me != null && userId == me.id) {
            selectTab(MainTab.PROFILE)
        } else {
            openOverlay(OverlayScreen.UserProfileDetail(userId))
        }
    }

    fun popOverlay() {
        _navState.update { state ->
            if (state.overlayStack.isEmpty()) state
            else state.copy(overlayStack = state.overlayStack.dropLast(1))
        }
    }

    fun showSnackbar(message: String) {
        _navState.update { it.copy(snackbarMessage = message) }
    }

    fun consumeSnackbar() {
        _navState.update { it.copy(snackbarMessage = null) }
    }

    // --- Social Interactions ---
    fun toggleLike(postId: String) {
        socialRepository.toggleLike(postId)
    }

    fun toggleSave(postId: String) {
        socialRepository.toggleSave(postId)
    }

    fun toggleFollow(targetUserId: String) {
        val currentId = uiState.value.currentUserSynced?.id ?: return
        socialRepository.toggleFollow(targetUserId, currentId)
    }

    fun addComment(postId: String, text: String) {
        val me = uiState.value.currentUserSynced ?: return
        socialRepository.addComment(postId, me, text)
    }

    fun replyToComment(postId: String, parentCommentId: String, text: String) {
        val me = uiState.value.currentUserSynced ?: return
        socialRepository.replyToComment(postId, parentCommentId, me, text)
    }

    fun toggleCommentLike(postId: String, commentId: String) {
        socialRepository.toggleCommentLike(postId, commentId)
    }

    fun publishPost(
        mediaType: MediaType,
        mediaResId: Int?,
        mediaUri: String?,
        caption: String,
        rawHashtags: String,
        successMessage: String
    ): Boolean {
        val me = uiState.value.currentUserSynced ?: return false
        if (mediaResId == null && mediaUri.isNullOrBlank()) return false
        if (caption.trim().isEmpty()) return false

        val preparedUri = mediaUri?.let { mediaStorageRepository.prepareMediaReference(it) }
        val tags = rawHashtags
            .split(Regex("[\\s,]+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { if (it.startsWith("#")) it else "#$it" }
            .distinct()

        socialRepository.createPost(
            author = me,
            mediaType = mediaType,
            mediaResId = mediaResId,
            mediaUri = preparedUri,
            caption = caption,
            hashtags = tags
        )
        _navState.update {
            it.copy(
                activeTab = MainTab.HOME,
                overlayStack = emptyList(),
                snackbarMessage = successMessage
            )
        }
        return true
    }

    fun updateProfile(
        fullName: String,
        username: String,
        bio: String,
        avatarUri: String?,
        successMessage: String
    ) {
        authRepository.updateCurrentUserProfile(fullName, username, bio, avatarUri)
        _navState.update {
            it.copy(
                overlayStack = it.overlayStack.dropLast(1),
                snackbarMessage = successMessage
            )
        }
    }

    // --- Messenger Actions ---
    fun openMessenger() {
        openOverlay(OverlayScreen.MessengerInbox)
    }

    fun openConversation(conversationId: String) {
        messengerRepository.markConversationRead(conversationId)
        openOverlay(OverlayScreen.ChatThread(conversationId))
    }

    fun openChatWithUser(participantId: String) {
        val currentId = uiState.value.currentUserSynced?.id
        if (participantId == currentId) return
        val conversationId = messengerRepository.getOrCreateConversation(participantId)
        messengerRepository.markConversationRead(conversationId)
        openOverlay(OverlayScreen.ChatThread(conversationId))
    }

    fun startOrSelectConversationForUser(participantId: String): String {
        val conversationId = messengerRepository.getOrCreateConversation(participantId)
        messengerRepository.markConversationRead(conversationId)
        return conversationId
    }

    fun markConversationRead(conversationId: String) {
        messengerRepository.markConversationRead(conversationId)
    }

    fun sendMessage(
        conversationId: String,
        text: String,
        mediaUri: String? = null,
        replyToMessageId: String? = null
    ) {
        val me = uiState.value.currentUserSynced ?: return
        val preparedUri = mediaUri?.let { mediaStorageRepository.prepareMediaReference(it) }
        messengerRepository.sendMessage(
            conversationId = conversationId,
            senderId = me.id,
            text = text,
            mediaUri = preparedUri,
            replyToMessageId = replyToMessageId
        )
    }

    fun toggleMessageReaction(conversationId: String, messageId: String, emoji: String) {
        messengerRepository.toggleMessageReaction(conversationId, messageId, emoji)
    }

    fun deleteMessage(conversationId: String, messageId: String, deletedNotice: String) {
        messengerRepository.deleteMessage(conversationId, messageId)
        showSnackbar(deletedNotice)
    }

    fun togglePinConversation(conversationId: String) {
        messengerRepository.togglePinConversation(conversationId)
    }

    fun toggleMuteConversation(conversationId: String) {
        messengerRepository.toggleMuteConversation(conversationId)
    }

    // --- Settings & Preferences ---
    fun setLanguage(language: AppLanguage) {
        LocaleManager.applyLocale(getApplication(), language)
        viewModelScope.launch {
            preferencesStore.setLanguage(language)
        }
    }

    fun toggleLanguage() {
        val next = LocaleManager.toggleLanguage(getApplication())
        viewModelScope.launch {
            preferencesStore.setLanguage(next)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesStore.setDarkMode(enabled)
        }
    }

    fun updatePrivacy(transform: (PrivacySettings) -> PrivacySettings) {
        viewModelScope.launch {
            preferencesStore.updatePrivacy(transform)
        }
    }
}

