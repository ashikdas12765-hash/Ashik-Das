package com.example.data.local

import com.example.R
import com.example.data.contract.MessengerRepository
import com.example.model.ChatConversation
import com.example.model.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class LocalPreviewMessengerRepository : MessengerRepository {

    private val now = System.currentTimeMillis()

    private val initialConversations = listOf(
        ChatConversation(
            id = "conv_ananya",
            participantId = "u_ananya",
            unreadCount = 2,
            isOnline = true,
            lastActiveLabel = "Active now",
            isPinned = true,
            isMuted = false,
            updatedAtMillis = now - 2 * 60_000L
        ),
        ChatConversation(
            id = "conv_rafi",
            participantId = "u_rafi",
            unreadCount = 1,
            isOnline = true,
            lastActiveLabel = "Active now",
            isPinned = false,
            isMuted = false,
            updatedAtMillis = now - 18 * 60_000L
        ),
        ChatConversation(
            id = "conv_mehak",
            participantId = "u_mehak",
            unreadCount = 0,
            isOnline = false,
            lastActiveLabel = "Active 14m ago",
            isPinned = false,
            isMuted = false,
            updatedAtMillis = now - 95 * 60_000L
        ),
        ChatConversation(
            id = "conv_nadia",
            participantId = "u_nadia",
            unreadCount = 0,
            isOnline = true,
            lastActiveLabel = "Active now",
            isPinned = false,
            isMuted = false,
            updatedAtMillis = now - 4 * 60 * 60_000L
        )
    )

    private val initialMessages: Map<String, List<ChatMessage>> = mapOf(
        "conv_ananya" to listOf(
            ChatMessage(
                id = "m_a1",
                conversationId = "conv_ananya",
                senderId = "u_ananya",
                text = "Hey! Loved your rooftop sunset shot today ✨",
                timestampLabel = "6:12 PM",
                createdAtMillis = now - 25 * 60_000L,
                isRead = true
            ),
            ChatMessage(
                id = "m_a2",
                conversationId = "conv_ananya",
                senderId = "u_current",
                text = "Thanks Ananya! The violet light lasted maybe five minutes before dusk.",
                timestampLabel = "6:15 PM",
                createdAtMillis = now - 20 * 60_000L,
                isRead = true,
                reaction = "❤️"
            ),
            ChatMessage(
                id = "m_a3",
                conversationId = "conv_ananya",
                senderId = "u_ananya",
                text = "Caught this angle from Skyline Terrace right around the same time!",
                mediaResId = R.drawable.img_post_sunset_rooftop,
                timestampLabel = "6:28 PM",
                createdAtMillis = now - 5 * 60_000L,
                isRead = false
            ),
            ChatMessage(
                id = "m_a4",
                conversationId = "conv_ananya",
                senderId = "u_ananya",
                text = "Are you free for iced lattes & a photo walk this Friday evening?",
                timestampLabel = "6:30 PM",
                createdAtMillis = now - 2 * 60_000L,
                isRead = false
            )
        ),
        "conv_rafi" to listOf(
            ChatMessage(
                id = "m_r1",
                conversationId = "conv_rafi",
                senderId = "u_current",
                text = "Rafi, your Old Dhaka rickshaw art reel is pure magic!",
                timestampLabel = "5:40 PM",
                createdAtMillis = now - 40 * 60_000L,
                isRead = true,
                reaction = "🔥"
            ),
            ChatMessage(
                id = "m_r2",
                conversationId = "conv_rafi",
                senderId = "u_rafi",
                text = "ধন্যবাদ! পরের সপ্তাহে একসাথে স্ট্রিট ফটোগ্রাফি ওয়াক করতে পারো — দারুণ লাইট থাকে বিকেলে 🎨",
                timestampLabel = "6:14 PM",
                createdAtMillis = now - 18 * 60_000L,
                isRead = false
            )
        ),
        "conv_mehak" to listOf(
            ChatMessage(
                id = "m_m1",
                conversationId = "conv_mehak",
                senderId = "u_mehak",
                text = "Sending you the exact beach cove pin from my coastal reel 🌊",
                mediaResId = R.drawable.img_post_coastal_escape,
                timestampLabel = "4:45 PM",
                createdAtMillis = now - 110 * 60_000L,
                isRead = true
            ),
            ChatMessage(
                id = "m_m2",
                conversationId = "conv_mehak",
                senderId = "u_current",
                text = "Bookmarked it! Definitely planning a weekend trip soon.",
                timestampLabel = "4:58 PM",
                createdAtMillis = now - 95 * 60_000L,
                isRead = true,
                replyToMessageId = "m_m1"
            )
        ),
        "conv_nadia" to listOf(
            ChatMessage(
                id = "m_n1",
                conversationId = "conv_nadia",
                senderId = "u_nadia",
                text = "Just finished the gradient showcase banner — purple + pink looks so crisp on dark mode 💜",
                timestampLabel = "2:10 PM",
                createdAtMillis = now - 4 * 60 * 60_000L,
                isRead = true
            )
        )
    )

    private val _conversations = MutableStateFlow(sortConversations(initialConversations))
    override val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    private val _messagesByConversation = MutableStateFlow(initialMessages)
    override val messagesByConversation: StateFlow<Map<String, List<ChatMessage>>> =
        _messagesByConversation.asStateFlow()

    override fun getOrCreateConversation(participantId: String): String {
        val existing = _conversations.value.firstOrNull { it.participantId == participantId }
        if (existing != null) return existing.id

        val newId = "conv_${participantId}_${UUID.randomUUID().toString().take(6)}"
        val created = ChatConversation(
            id = newId,
            participantId = participantId,
            unreadCount = 0,
            isOnline = true,
            lastActiveLabel = "Active now",
            isPinned = false,
            isMuted = false,
            updatedAtMillis = System.currentTimeMillis()
        )
        _conversations.update { sortConversations(it + created) }
        _messagesByConversation.update { it + (newId to emptyList()) }
        return newId
    }

    override fun markConversationRead(conversationId: String) {
        _conversations.update { list ->
            list.map { conv ->
                if (conv.id == conversationId && conv.unreadCount > 0) {
                    conv.copy(unreadCount = 0)
                } else {
                    conv
                }
            }
        }
        _messagesByConversation.update { map ->
            val messages = map[conversationId] ?: return@update map
            map + (conversationId to messages.map { if (!it.isRead) it.copy(isRead = true) else it })
        }
    }

    override fun sendMessage(
        conversationId: String,
        senderId: String,
        text: String,
        mediaUri: String?,
        replyToMessageId: String?
    ): ChatMessage? {
        val trimmed = text.trim()
        if (trimmed.isEmpty() && mediaUri.isNullOrBlank()) return null

        val timestamp = System.currentTimeMillis()
        val message = ChatMessage(
            id = "m_${UUID.randomUUID()}",
            conversationId = conversationId,
            senderId = senderId,
            text = trimmed,
            mediaUri = mediaUri,
            timestampLabel = "Just now",
            createdAtMillis = timestamp,
            isRead = true,
            replyToMessageId = replyToMessageId
        )

        _messagesByConversation.update { map ->
            val current = map[conversationId].orEmpty()
            map + (conversationId to (current + message))
        }

        _conversations.update { list ->
            val updated = list.map { conv ->
                if (conv.id == conversationId) {
                    conv.copy(unreadCount = 0, updatedAtMillis = timestamp)
                } else {
                    conv
                }
            }
            sortConversations(updated)
        }
        return message
    }

    override fun toggleMessageReaction(conversationId: String, messageId: String, emoji: String) {
        _messagesByConversation.update { map ->
            val messages = map[conversationId] ?: return@update map
            val updated = messages.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(reaction = if (msg.reaction == emoji) null else emoji)
                } else {
                    msg
                }
            }
            map + (conversationId to updated)
        }
    }

    override fun deleteMessage(conversationId: String, messageId: String) {
        _messagesByConversation.update { map ->
            val messages = map[conversationId] ?: return@update map
            map + (conversationId to messages.filterNot { it.id == messageId })
        }
    }

    override fun togglePinConversation(conversationId: String) {
        _conversations.update { list ->
            val updated = list.map { conv ->
                if (conv.id == conversationId) conv.copy(isPinned = !conv.isPinned) else conv
            }
            sortConversations(updated)
        }
    }

    override fun toggleMuteConversation(conversationId: String) {
        _conversations.update { list ->
            list.map { conv ->
                if (conv.id == conversationId) conv.copy(isMuted = !conv.isMuted) else conv
            }
        }
    }

    private fun sortConversations(list: List<ChatConversation>): List<ChatConversation> {
        return list.sortedWith(
            compareByDescending<ChatConversation> { it.isPinned }
                .thenByDescending { it.updatedAtMillis }
        )
    }
}
