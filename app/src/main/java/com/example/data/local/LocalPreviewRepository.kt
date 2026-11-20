package com.example.data.local

import com.example.R
import com.example.data.contract.AuthRepository
import com.example.data.contract.MediaStorageRepository
import com.example.data.contract.SocialFeedRepository
import com.example.model.CommentReply
import com.example.model.MediaType
import com.example.model.PostComment
import com.example.model.SocialPost
import com.example.model.StoryItem
import com.example.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class LocalPreviewMediaStorageRepository : MediaStorageRepository

class LocalPreviewSocialRepository : SocialFeedRepository {

    private val initialUsers = listOf(
        UserProfile(
            id = "u_ananya",
            fullName = "Ananya Rahman",
            username = "ananya.visuals",
            email = "ananya@friendzone.app",
            bio = "Golden hour chaser ✨ | Visual storyteller between Dhaka & Lisbon 📍",
            coverResId = R.drawable.img_cover_aurora,
            avatarGradientIndex = 0,
            followersCount = 2480,
            followingCount = 312,
            isFollowedByCurrentUser = true,
            isVerified = true
        ),
        UserProfile(
            id = "u_rafi",
            fullName = "Rafiul Karim",
            username = "rafi_streetframes",
            email = "rafi@friendzone.app",
            bio = "Street photography & Dhaka rickshaw art 🎨 | Coffee & film rolls ☕",
            coverResId = R.drawable.img_post_dhaka_street,
            avatarGradientIndex = 2,
            followersCount = 1840,
            followingCount = 415,
            isFollowedByCurrentUser = false,
            isVerified = true
        ),
        UserProfile(
            id = "u_mehak",
            fullName = "Mehak Chowdhury",
            username = "mehak.escapes",
            email = "mehak@friendzone.app",
            bio = "Salt air, coastal sunsets & slow mornings 🌊✨ | Travel reels",
            coverResId = R.drawable.img_post_coastal_escape,
            avatarGradientIndex = 3,
            followersCount = 5120,
            followingCount = 289,
            isFollowedByCurrentUser = true,
            isVerified = true
        ),
        UserProfile(
            id = "u_shayan",
            fullName = "Shayan Bose",
            username = "shayan_creates",
            email = "shayan@friendzone.app",
            bio = "Mobile filmmaker & music producer 🎬🎧 | Sharing daily sparks",
            coverResId = R.drawable.img_post_sunset_rooftop,
            avatarGradientIndex = 4,
            followersCount = 960,
            followingCount = 198,
            isFollowedByCurrentUser = false,
            isVerified = false
        ),
        UserProfile(
            id = "u_nadia",
            fullName = "Nadia Sultana",
            username = "nadia.atelier",
            email = "nadia@friendzone.app",
            bio = "UI designer • Color palettes • Weekend café explorer 💜",
            coverResId = R.drawable.img_cover_aurora,
            avatarGradientIndex = 1,
            followersCount = 1325,
            followingCount = 520,
            isFollowedByCurrentUser = false,
            isVerified = false
        )
    )

    private val initialPosts = listOf(
        SocialPost(
            id = "p_1",
            authorId = "u_ananya",
            mediaType = MediaType.PHOTO,
            mediaResId = R.drawable.img_post_sunset_rooftop,
            caption = "Golden hour iced lattes above the skyline — nothing beats twilight conversations with friends.",
            hashtags = listOf("#FriendZone", "#GoldenHour", "#CityVibes", "#Rooftop"),
            location = "Skyline Terrace, Dhaka",
            createdAtLabel = "18m ago",
            likesCount = 248,
            isLiked = false,
            isSaved = false,
            isReel = false
        ),
        SocialPost(
            id = "p_2",
            authorId = "u_rafi",
            mediaType = MediaType.VIDEO,
            mediaResId = R.drawable.img_post_dhaka_street,
            videoDurationLabel = "0:28",
            caption = "Colors that never fade — capturing the timeless floral craft of Old Dhaka streets in warm afternoon light.",
            hashtags = listOf("#DhakaVibes", "#RickshawArt", "#Bangladesh", "#StreetReel"),
            location = "Old Dhaka, Bangladesh",
            createdAtLabel = "1h ago",
            likesCount = 512,
            isLiked = true,
            isSaved = true,
            isReel = true,
            audioTrack = "Monsoon Strings • Acoustic Loop"
        ),
        SocialPost(
            id = "p_3",
            authorId = "u_mehak",
            mediaType = MediaType.VIDEO,
            mediaResId = R.drawable.img_post_coastal_escape,
            videoDurationLabel = "0:19",
            caption = "Lavender clouds drifting over turquoise tide. Save this spot for your next weekend getaway!",
            hashtags = listOf("#CoastalEscape", "#CoxsBazar", "#TravelReels", "#Sunset"),
            location = "Inani Beach Cove",
            createdAtLabel = "3h ago",
            likesCount = 894,
            isLiked = false,
            isSaved = false,
            isReel = true,
            audioTrack = "Ocean Breeze • Lo-Fi Chill"
        ),
        SocialPost(
            id = "p_4",
            authorId = "u_nadia",
            mediaType = MediaType.PHOTO,
            mediaResId = R.drawable.img_cover_aurora,
            caption = "Playing with silk light gradients for our upcoming FriendZone creative showcase. Purple + pink forever.",
            hashtags = listOf("#DesignInspo", "#FriendZone", "#GradientArt"),
            location = "Studio 4B",
            createdAtLabel = "6h ago",
            likesCount = 319,
            isLiked = false,
            isSaved = true,
            isReel = false
        ),
        SocialPost(
            id = "p_5",
            authorId = "u_shayan",
            mediaType = MediaType.VIDEO,
            mediaResId = R.drawable.img_post_sunset_rooftop,
            videoDurationLabel = "0:42",
            caption = "Behind the scenes shooting dusk transitions with just a phone and natural twilight glow.",
            hashtags = listOf("#MobileFilmmaking", "#Reels", "#Twilight"),
            location = "Banani Rooftop",
            createdAtLabel = "1d ago",
            likesCount = 410,
            isLiked = false,
            isSaved = false,
            isReel = true,
            audioTrack = "Violet Dusk • Shayan Original"
        ),
        SocialPost(
            id = "p_me_1",
            authorId = "u_current",
            mediaType = MediaType.PHOTO,
            mediaResId = R.drawable.img_post_sunset_rooftop,
            caption = "Weekend rooftop catch-up as the sky turned electric violet over the city.",
            hashtags = listOf("#FriendZone", "#GoldenHour", "#CityLights"),
            location = "Dhanmondi Terrace",
            createdAtLabel = "2h ago",
            likesCount = 134,
            isLiked = true,
            isSaved = false,
            isReel = false
        ),
        SocialPost(
            id = "p_me_2",
            authorId = "u_current",
            mediaType = MediaType.VIDEO,
            mediaResId = R.drawable.img_post_coastal_escape,
            videoDurationLabel = "0:24",
            caption = "Sea breeze, pastel horizon, and zero rush — my favorite coastal reel from this month.",
            hashtags = listOf("#Travel", "#CoastalEscape", "#FriendZone"),
            location = "Cox's Bazar",
            createdAtLabel = "1d ago",
            likesCount = 276,
            isLiked = false,
            isSaved = true,
            isReel = true,
            audioTrack = "Warm Tide • Original Mix"
        ),
        SocialPost(
            id = "p_me_3",
            authorId = "u_current",
            mediaType = MediaType.PHOTO,
            mediaResId = R.drawable.img_post_dhaka_street,
            caption = "Afternoon walk capturing hand-painted stories on every corner.",
            hashtags = listOf("#DhakaVibes", "#StreetFrames", "#Art"),
            location = "Old Dhaka",
            createdAtLabel = "3d ago",
            likesCount = 189,
            isLiked = false,
            isSaved = false,
            isReel = false
        ),
        SocialPost(
            id = "p_me_4",
            authorId = "u_current",
            mediaType = MediaType.PHOTO,
            mediaResId = R.drawable.img_cover_aurora,
            caption = "Late-night color study and ambient playlist while designing new stories.",
            hashtags = listOf("#CreativeFlow", "#Aurora", "#FriendZone"),
            location = "Home Studio",
            createdAtLabel = "5d ago",
            likesCount = 97,
            isLiked = false,
            isSaved = false,
            isReel = false
        )
    )

    private val initialComments = mapOf(
        "p_1" to listOf(
            PostComment(
                id = "c_1",
                postId = "p_1",
                authorId = "u_mehak",
                authorName = "Mehak Chowdhury",
                authorUsername = "mehak.escapes",
                authorAvatarGradientIndex = 3,
                text = "The violet-pink glow in the sky is unreal! Which café is this?",
                timestampLabel = "12m",
                likesCount = 9,
                replies = listOf(
                    CommentReply(
                        id = "r_1",
                        authorId = "u_ananya",
                        authorName = "Ananya Rahman",
                        authorUsername = "ananya.visuals",
                        authorAvatarGradientIndex = 0,
                        text = "Skyline Terrace on Road 11 — let's go this Friday!",
                        timestampLabel = "8m"
                    )
                )
            ),
            PostComment(
                id = "c_2",
                postId = "p_1",
                authorId = "u_rafi",
                authorName = "Rafiul Karim",
                authorUsername = "rafi_streetframes",
                authorAvatarGradientIndex = 2,
                text = "অসাধারণ আলো আর কম্পোজিশন! 🔥",
                timestampLabel = "5m",
                likesCount = 4
            )
        ),
        "p_2" to listOf(
            PostComment(
                id = "c_3",
                postId = "p_2",
                authorId = "u_nadia",
                authorName = "Nadia Sultana",
                authorUsername = "nadia.atelier",
                authorAvatarGradientIndex = 1,
                text = "Every detail of that rickshaw art pops so vividly!",
                timestampLabel = "42m",
                likesCount = 15
            )
        ),
        "p_3" to listOf(
            PostComment(
                id = "c_4",
                postId = "p_3",
                authorId = "u_shayan",
                authorName = "Shayan Bose",
                authorUsername = "shayan_creates",
                authorAvatarGradientIndex = 4,
                text = "Adding this beach cove straight to my travel bucket list 🌊",
                timestampLabel = "2h",
                likesCount = 11
            )
        )
    )

    private val initialStories = listOf(
        StoryItem(
            id = "s_1",
            userId = "u_ananya",
            caption = "Evening espresso & golden clouds ✨",
            mediaResId = R.drawable.img_post_sunset_rooftop,
            timestampLabel = "25m ago"
        ),
        StoryItem(
            id = "s_2",
            userId = "u_rafi",
            caption = "Walking through colors of Dhaka 🎨",
            mediaResId = R.drawable.img_post_dhaka_street,
            timestampLabel = "1h ago"
        ),
        StoryItem(
            id = "s_3",
            userId = "u_mehak",
            caption = "Sunset waves calling 🌊💜",
            mediaResId = R.drawable.img_post_coastal_escape,
            timestampLabel = "2h ago"
        ),
        StoryItem(
            id = "s_4",
            userId = "u_nadia",
            caption = "New FriendZone palette drops tonight!",
            mediaResId = R.drawable.img_cover_aurora,
            timestampLabel = "4h ago"
        )
    )

    private val _users = MutableStateFlow(initialUsers)
    override val users: StateFlow<List<UserProfile>> = _users.asStateFlow()

    private val _posts = MutableStateFlow(initialPosts)
    override val posts: StateFlow<List<SocialPost>> = _posts.asStateFlow()

    private val _commentsByPost = MutableStateFlow(initialComments)
    override val commentsByPost: StateFlow<Map<String, List<PostComment>>> = _commentsByPost.asStateFlow()

    private val _stories = MutableStateFlow(initialStories)
    override val stories: StateFlow<List<StoryItem>> = _stories.asStateFlow()

    override fun syncCurrentUser(user: UserProfile) {
        _users.update { current ->
            val exists = current.any { it.id == user.id }
            if (exists) {
                current.map { if (it.id == user.id) user else it }
            } else {
                listOf(user) + current
            }
        }
    }

    override fun toggleLike(postId: String) {
        _posts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    val nowLiked = !post.isLiked
                    post.copy(
                        isLiked = nowLiked,
                        likesCount = (post.likesCount + if (nowLiked) 1 else -1).coerceAtLeast(0)
                    )
                } else {
                    post
                }
            }
        }
    }

    override fun toggleSave(postId: String) {
        _posts.update { list ->
            list.map { post ->
                if (post.id == postId) post.copy(isSaved = !post.isSaved) else post
            }
        }
    }

    override fun toggleFollow(targetUserId: String, currentUserId: String) {
        var didFollow = false
        _users.update { list ->
            val target = list.firstOrNull { it.id == targetUserId } ?: return@update list
            if (target.id == currentUserId) return@update list
            didFollow = !target.isFollowedByCurrentUser
            list.map { user ->
                when (user.id) {
                    targetUserId -> user.copy(
                        isFollowedByCurrentUser = didFollow,
                        followersCount = (user.followersCount + if (didFollow) 1 else -1).coerceAtLeast(0)
                    )
                    currentUserId -> user.copy(
                        followingCount = (user.followingCount + if (didFollow) 1 else -1).coerceAtLeast(0)
                    )
                    else -> user
                }
            }
        }
    }

    override fun addComment(postId: String, author: UserProfile, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val newComment = PostComment(
            id = "c_${UUID.randomUUID()}",
            postId = postId,
            authorId = author.id,
            authorName = author.fullName,
            authorUsername = author.username,
            authorAvatarGradientIndex = author.avatarGradientIndex,
            text = trimmed,
            timestampLabel = "Just now",
            likesCount = 0,
            isLiked = false
        )
        _commentsByPost.update { map ->
            val existing = map[postId].orEmpty()
            map + (postId to (existing + newComment))
        }
    }

    override fun replyToComment(
        postId: String,
        parentCommentId: String,
        author: UserProfile,
        text: String
    ) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val reply = CommentReply(
            id = "r_${UUID.randomUUID()}",
            authorId = author.id,
            authorName = author.fullName,
            authorUsername = author.username,
            authorAvatarGradientIndex = author.avatarGradientIndex,
            text = trimmed,
            timestampLabel = "Just now"
        )
        _commentsByPost.update { map ->
            val existing = map[postId].orEmpty()
            val updated = existing.map { comment ->
                if (comment.id == parentCommentId) {
                    comment.copy(replies = comment.replies + reply)
                } else {
                    comment
                }
            }
            map + (postId to updated)
        }
    }

    override fun toggleCommentLike(postId: String, commentId: String) {
        _commentsByPost.update { map ->
            val existing = map[postId].orEmpty()
            val updated = existing.map { comment ->
                if (comment.id == commentId) {
                    val nowLiked = !comment.isLiked
                    comment.copy(
                        isLiked = nowLiked,
                        likesCount = (comment.likesCount + if (nowLiked) 1 else -1).coerceAtLeast(0)
                    )
                } else {
                    comment
                }
            }
            map + (postId to updated)
        }
    }

    override fun createPost(
        author: UserProfile,
        mediaType: MediaType,
        mediaResId: Int?,
        mediaUri: String?,
        caption: String,
        hashtags: List<String>
    ): SocialPost {
        val newPost = SocialPost(
            id = "p_${UUID.randomUUID()}",
            authorId = author.id,
            mediaType = mediaType,
            mediaResId = mediaResId,
            mediaUri = mediaUri,
            videoDurationLabel = if (mediaType == MediaType.VIDEO) "0:15" else null,
            caption = caption.trim(),
            hashtags = hashtags,
            location = "Local Preview",
            createdAtLabel = "Just now",
            likesCount = 1,
            isLiked = true,
            isSaved = false,
            isReel = mediaType == MediaType.VIDEO,
            audioTrack = if (mediaType == MediaType.VIDEO) "Original Audio • ${author.username}" else null
        )
        _posts.update { listOf(newPost) + it }
        return newPost
    }

    override fun markStorySeen(storyId: String) {
        _stories.update { list ->
            list.map { if (it.id == storyId) it.copy(isSeen = true) else it }
        }
    }
}

class LocalPreviewAuthRepository(
    private val socialRepository: SocialFeedRepository
) : AuthRepository {

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    override val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val registeredLocalUsers = mutableMapOf<String, Pair<String, UserProfile>>()

    private fun buildDefaultDemoUser(email: String = "demo@friendzone.app"): UserProfile {
        val localHandle = email.substringBefore("@")
            .lowercase()
            .replace(Regex("[^a-z0-9._]"), "")
            .ifBlank { "friendzone_explorer" }
        return UserProfile(
            id = "u_current",
            fullName = "Aarav Das",
            username = localHandle,
            email = email,
            bio = "Connecting, sharing & discovering everyday moments on FriendZone ✨💜",
            coverResId = R.drawable.img_cover_aurora,
            avatarGradientIndex = 5,
            followersCount = 128,
            followingCount = 2,
            isCurrentUser = true
        )
    }

    override fun signInLocalPreview(email: String, password: String): Result<UserProfile> {
        val normalizedEmail = email.trim().lowercase()
        val stored = registeredLocalUsers[normalizedEmail]
        val user = if (stored != null) {
            if (stored.first != password) {
                return Result.failure(IllegalArgumentException("Password mismatch"))
            }
            stored.second
        } else {
            buildDefaultDemoUser(normalizedEmail)
        }
        socialRepository.syncCurrentUser(user)
        _currentUser.value = user
        return Result.success(user)
    }

    override fun registerLocalPreview(
        fullName: String,
        username: String,
        email: String,
        password: String
    ): Result<UserProfile> {
        val normalizedEmail = email.trim().lowercase()
        val cleanHandle = username.trim().lowercase().removePrefix("@")
        val newUser = UserProfile(
            id = "u_current",
            fullName = fullName.trim(),
            username = cleanHandle,
            email = normalizedEmail,
            bio = "New on FriendZone • Connect • Share • Discover ✨",
            coverResId = R.drawable.img_cover_aurora,
            avatarGradientIndex = 0,
            followersCount = 1,
            followingCount = 2,
            isCurrentUser = true
        )
        registeredLocalUsers[normalizedEmail] = password to newUser
        socialRepository.syncCurrentUser(newUser)
        _currentUser.value = newUser
        return Result.success(newUser)
    }

    override fun signInDemoPreview(): UserProfile {
        val demo = buildDefaultDemoUser()
        socialRepository.syncCurrentUser(demo)
        _currentUser.value = demo
        return demo
    }

    override fun updateCurrentUserProfile(
        fullName: String,
        username: String,
        bio: String,
        avatarUri: String?
    ) {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            fullName = fullName.trim().ifBlank { current.fullName },
            username = username.trim().lowercase().removePrefix("@").ifBlank { current.username },
            bio = bio.trim(),
            avatarUri = avatarUri ?: current.avatarUri
        )
        _currentUser.value = updated
        socialRepository.syncCurrentUser(updated)
    }

    override fun signOut() {
        _currentUser.value = null
    }
}
