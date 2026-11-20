package com.example.model

import androidx.annotation.DrawableRes
import com.example.i18n.AppLanguage

enum class MediaType {
    PHOTO,
    VIDEO
}

data class UserProfile(
    val id: String,
    val fullName: String,
    val username: String,
    val email: String,
    val bio: String,
    @DrawableRes val coverResId: Int,
    val avatarUri: String? = null,
    val avatarGradientIndex: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isFollowedByCurrentUser: Boolean = false,
    val isVerified: Boolean = false,
    val isCurrentUser: Boolean = false
)

data class CommentReply(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatarGradientIndex: Int,
    val text: String,
    val timestampLabel: String
)

data class PostComment(
    val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatarGradientIndex: Int,
    val text: String,
    val timestampLabel: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val replies: List<CommentReply> = emptyList()
)

data class SocialPost(
    val id: String,
    val authorId: String,
    val mediaType: MediaType,
    @DrawableRes val mediaResId: Int? = null,
    val mediaUri: String? = null,
    val videoDurationLabel: String? = null,
    val caption: String,
    val hashtags: List<String> = emptyList(),
    val location: String? = null,
    val createdAtLabel: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val isReel: Boolean = false,
    val audioTrack: String? = null
)

data class StoryItem(
    val id: String,
    val userId: String,
    val caption: String,
    @DrawableRes val mediaResId: Int,
    val timestampLabel: String,
    val isSeen: Boolean = false
)

data class PrivacySettings(
    val isPrivateAccount: Boolean = false,
    val showActivityStatus: Boolean = true,
    val allowTagging: Boolean = true
)

data class AppPreferences(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val isDarkMode: Boolean = false,
    val privacy: PrivacySettings = PrivacySettings()
)

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val text: String,
    val mediaUri: String? = null,
    @DrawableRes val mediaResId: Int? = null,
    val timestampLabel: String,
    val createdAtMillis: Long,
    val isRead: Boolean = true,
    val reaction: String? = null,
    val replyToMessageId: String? = null
)

data class ChatConversation(
    val id: String,
    val participantId: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val lastActiveLabel: String,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val updatedAtMillis: Long
)

