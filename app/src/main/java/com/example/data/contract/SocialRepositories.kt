package com.example.data.contract

import com.example.model.ChatConversation
import com.example.model.ChatMessage
import com.example.model.MediaType
import com.example.model.PostComment
import com.example.model.SocialPost
import com.example.model.StoryItem
import com.example.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

/**
 * Clean Phase 1 repository contracts designed so Phase 2 can swap in
 * Firebase Authentication, Cloud Firestore, and Firebase Storage
 * implementations without changing ViewModels or Compose UI screens.
 */
interface AuthRepository {
    val currentUser: StateFlow<UserProfile?>

    fun signInLocalPreview(email: String, password: String): Result<UserProfile>

    fun registerLocalPreview(
        fullName: String,
        username: String,
        email: String,
        password: String
    ): Result<UserProfile>

    fun signInDemoPreview(): UserProfile

    fun updateCurrentUserProfile(
        fullName: String,
        username: String,
        bio: String,
        avatarUri: String?
    )

    fun signOut()
}

interface SocialFeedRepository {
    val users: StateFlow<List<UserProfile>>
    val posts: StateFlow<List<SocialPost>>
    val commentsByPost: StateFlow<Map<String, List<PostComment>>>
    val stories: StateFlow<List<StoryItem>>

    fun toggleLike(postId: String)
    fun toggleSave(postId: String)
    fun toggleFollow(targetUserId: String, currentUserId: String)
    fun addComment(postId: String, author: UserProfile, text: String)
    fun replyToComment(postId: String, parentCommentId: String, author: UserProfile, text: String)
    fun toggleCommentLike(postId: String, commentId: String)
    fun createPost(
        author: UserProfile,
        mediaType: MediaType,
        mediaResId: Int?,
        mediaUri: String?,
        caption: String,
        hashtags: List<String>
    ): SocialPost

    fun markStorySeen(storyId: String)
    fun syncCurrentUser(user: UserProfile)
}

interface MessengerRepository {
    val conversations: StateFlow<List<ChatConversation>>
    val messagesByConversation: StateFlow<Map<String, List<ChatMessage>>>

    fun getOrCreateConversation(participantId: String): String
    fun markConversationRead(conversationId: String)
    fun sendMessage(
        conversationId: String,
        senderId: String,
        text: String,
        mediaUri: String? = null,
        replyToMessageId: String? = null
    ): ChatMessage?

    fun toggleMessageReaction(conversationId: String, messageId: String, emoji: String)
    fun deleteMessage(conversationId: String, messageId: String)
    fun togglePinConversation(conversationId: String)
    fun toggleMuteConversation(conversationId: String)
}

interface MediaStorageRepository {
    /**
     * Phase 1 keeps picked device URIs locally for immediate preview.
     * Phase 2 implementation uploads the URI to Firebase Storage and returns the download URL.
     */
    fun prepareMediaReference(localUri: String): String = localUri
}

