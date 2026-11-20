package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.i18n.LocalAppStrings
import com.example.model.ChatConversation
import com.example.model.ChatMessage
import com.example.model.UserProfile
import com.example.ui.components.UserAvatar
import com.example.ui.theme.FriendZoneGradients
import com.example.ui.theme.FzPurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.VerifiedBlue

private enum class InboxFilter {
    ALL,
    UNREAD,
    ACTIVE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessengerInboxScreen(
    currentUser: UserProfile,
    users: List<UserProfile>,
    conversations: List<ChatConversation>,
    messagesByConversation: Map<String, List<ChatMessage>>,
    onBack: () -> Unit,
    onOpenConversation: (String) -> Unit,
    onStartChatWithUser: (String) -> String,
    onMarkConversationRead: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onSendMessage: (conversationId: String, text: String, mediaUri: String?, replyToMessageId: String?) -> Unit,
    onToggleReaction: (conversationId: String, messageId: String, emoji: String) -> Unit,
    onDeleteMessage: (conversationId: String, messageId: String) -> Unit,
    onTogglePin: (String) -> Unit,
    onToggleMute: (String) -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val userMap = remember(users) { users.associateBy { it.id } }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(InboxFilter.ALL) }
    var showNewChatSheet by remember { mutableStateOf(false) }
    var selectedSplitConversationId by remember { mutableStateOf(conversations.firstOrNull()?.id) }

    val totalUnread = remember(conversations) { conversations.sumOf { it.unreadCount } }

    val activeFriends = remember(conversations, userMap) {
        conversations
            .filter { it.isOnline }
            .mapNotNull { conv -> userMap[conv.participantId]?.let { user -> conv to user } }
    }

    val cleanQuery = searchQuery.trim().lowercase()
    val filteredConversations = remember(conversations, messagesByConversation, userMap, cleanQuery, selectedFilter) {
        conversations.filter { conv ->
            val participant = userMap[conv.participantId] ?: return@filter false
            val lastMessage = messagesByConversation[conv.id]?.lastOrNull()
            val matchesFilter = when (selectedFilter) {
                InboxFilter.ALL -> true
                InboxFilter.UNREAD -> conv.unreadCount > 0
                InboxFilter.ACTIVE -> conv.isOnline
            }
            val matchesQuery = cleanQuery.isEmpty() ||
                participant.fullName.lowercase().contains(cleanQuery) ||
                participant.username.lowercase().contains(cleanQuery) ||
                (lastMessage?.text?.lowercase()?.contains(cleanQuery) == true)
            matchesFilter && matchesQuery
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("messenger_inbox_screen")
    ) {
        val isSplitLayout = maxWidth >= 680.dp

        LaunchedEffect(isSplitLayout, selectedSplitConversationId) {
            if (isSplitLayout && selectedSplitConversationId != null) {
                onMarkConversationRead(selectedSplitConversationId!!)
            }
        }

        val handleSelectConversation: (String) -> Unit = { convId ->
            if (isSplitLayout) {
                selectedSplitConversationId = convId
                onMarkConversationRead(convId)
            } else {
                onOpenConversation(convId)
            }
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // Left / Primary Inbox Pane
            Scaffold(
                modifier = if (isSplitLayout) {
                    Modifier
                        .width(350.dp)
                        .fillMaxHeight()
                } else {
                    Modifier.fillMaxSize()
                },
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        navigationIcon = {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("messenger_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = strings.messengerTitle,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                if (totalUnread > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.Transparent
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(FriendZoneGradients.Primary, RoundedCornerShape(12.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "$totalUnread",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { showNewChatSheet = true },
                                modifier = Modifier.testTag("new_chat_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddComment,
                                    contentDescription = strings.newMessageTitle,
                                    tint = FzPurple
                                )
                            }
                        }
                    )
                }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .navigationBarsPadding()
                        .testTag("messenger_conversations_list"),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search Bar
                    item(key = "search_bar") {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(strings.searchChatsPlaceholder) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = strings.navSearch,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Filled.Close, contentDescription = strings.cancel)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedBorderColor = FzPurple
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .testTag("messenger_search_input")
                        )
                    }

                    // Filter Chips
                    item(key = "filters") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val chips = listOf(
                                InboxFilter.ALL to strings.filterAllChats,
                                InboxFilter.UNREAD to if (totalUnread > 0) "${strings.filterUnreadChats} ($totalUnread)" else strings.filterUnreadChats,
                                InboxFilter.ACTIVE to strings.filterActiveChats
                            )
                            chips.forEach { (filter, label) ->
                                FilterChip(
                                    selected = selectedFilter == filter,
                                    onClick = { selectedFilter = filter },
                                    label = { Text(label, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.testTag("messenger_filter_${filter.name.lowercase()}")
                                )
                            }
                        }
                    }

                    // Active Now Horizontal Strip
                    if (activeFriends.isNotEmpty() && cleanQuery.isEmpty() && selectedFilter != InboxFilter.UNREAD) {
                        item(key = "active_strip") {
                            Column(modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)) {
                                Text(
                                    text = strings.activeNowTitle,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    items(activeFriends, key = { it.first.id }) { (conv, user) ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .width(66.dp)
                                                .clickable { handleSelectConversation(conv.id) }
                                                .testTag("active_friend_${user.id}")
                                        ) {
                                            Box {
                                                UserAvatar(
                                                    fullName = user.fullName,
                                                    avatarUri = user.avatarUri,
                                                    gradientIndex = user.avatarGradientIndex,
                                                    size = 56.dp,
                                                    showStoryRing = true
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .align(Alignment.BottomEnd)
                                                        .clip(CircleShape)
                                                        .background(SuccessGreen)
                                                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(5.dp))
                                            Text(
                                                text = user.fullName.substringBefore(" "),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Conversation Rows or Empty State
                    if (filteredConversations.isEmpty()) {
                        item(key = "empty_inbox") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp, vertical = 44.dp),
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
                                        imageVector = Icons.Outlined.ChatBubbleOutline,
                                        contentDescription = null,
                                        tint = FzPurple,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (selectedFilter == InboxFilter.UNREAD) strings.noUnreadChats else strings.noChatsYet,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                if (selectedFilter != InboxFilter.UNREAD) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = strings.noChatsHint,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredConversations, key = { it.id }) { conv ->
                            val participant = userMap[conv.participantId] ?: return@items
                            val lastMessage = messagesByConversation[conv.id]?.lastOrNull()
                            val isSelected = isSplitLayout && selectedSplitConversationId == conv.id

                            ConversationRowCard(
                                conversation = conv,
                                participant = participant,
                                lastMessage = lastMessage,
                                currentUserId = currentUser.id,
                                isSelected = isSelected,
                                onClick = { handleSelectConversation(conv.id) },
                                onTogglePin = { onTogglePin(conv.id) },
                                onToggleMute = { onToggleMute(conv.id) },
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                        }
                    }
                }
            }

            // Right Detail Pane on Tablet / Foldable
            if (isSplitLayout) {
                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                val activeConv = conversations.firstOrNull { it.id == selectedSplitConversationId }
                val activeUser = activeConv?.let { userMap[it.participantId] }

                if (activeConv != null && activeUser != null) {
                    ChatThreadPane(
                        conversation = activeConv,
                        participant = activeUser,
                        currentUser = currentUser,
                        messages = messagesByConversation[activeConv.id].orEmpty(),
                        showBackButton = false,
                        onBack = {},
                        onOpenProfile = onOpenProfile,
                        onSendMessage = { text, uri, replyId ->
                            onSendMessage(activeConv.id, text, uri, replyId)
                        },
                        onToggleReaction = { msgId, emoji ->
                            onToggleReaction(activeConv.id, msgId, emoji)
                        },
                        onDeleteMessage = { msgId ->
                            onDeleteMessage(activeConv.id, msgId)
                        },
                        onTogglePin = { onTogglePin(activeConv.id) },
                        onToggleMute = { onToggleMute(activeConv.id) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = null,
                                tint = FzPurple.copy(alpha = 0.6f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = strings.selectConversationHint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (showNewChatSheet) {
            NewMessageBottomSheet(
                users = users.filter { it.id != currentUser.id },
                onDismiss = { showNewChatSheet = false },
                onSelectUser = { userId ->
                    showNewChatSheet = false
                    val convId = onStartChatWithUser(userId)
                    handleSelectConversation(convId)
                }
            )
        }
    }
}

@Composable
private fun ConversationRowCard(
    conversation: ChatConversation,
    participant: UserProfile,
    lastMessage: ChatMessage?,
    currentUserId: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val hasUnread = conversation.unreadCount > 0
    var showMenu by remember { mutableStateOf(false) }

    val previewText = when {
        lastMessage == null -> strings.sayHelloHint
        lastMessage.text.isNotBlank() && lastMessage.senderId == currentUserId -> "${strings.youPrefix}: ${lastMessage.text}"
        lastMessage.text.isNotBlank() -> lastMessage.text
        lastMessage.senderId == currentUserId -> "${strings.youPrefix}: ${strings.photoAttachmentLabel}"
        else -> strings.photoAttachmentLabel
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("conversation_row_${conversation.id}"),
        shape = RoundedCornerShape(20.dp),
        color = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.48f)
            hasUnread -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            else -> MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = if (isSelected) 1.4.dp else 1.dp,
            color = if (isSelected) FzPurple else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                UserAvatar(
                    fullName = participant.fullName,
                    avatarUri = participant.avatarUri,
                    gradientIndex = participant.avatarGradientIndex,
                    size = 52.dp,
                    showStoryRing = hasUnread
                )
                if (conversation.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(13.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(SuccessGreen)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = participant.fullName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (hasUnread) FontWeight.ExtraBold else FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (participant.isVerified) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Verified",
                            tint = VerifiedBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (conversation.isPinned) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = strings.pinChat,
                            tint = FzPurple,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    if (conversation.isMuted) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsOff,
                            contentDescription = strings.muteChat,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (lastMessage != null && (!lastMessage.mediaUri.isNullOrBlank() || lastMessage.mediaResId != null)) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            tint = FzPurple,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = previewText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (hasUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = lastMessage?.timestampLabel ?: "",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (hasUnread) FzPurple else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (hasUnread) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(FriendZoneGradients.Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${conversation.unreadCount}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(22.dp))
                }
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.testTag("conversation_menu_${conversation.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Conversation options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (conversation.isPinned) strings.unpinChat else strings.pinChat) },
                        onClick = {
                            showMenu = false
                            onTogglePin()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (conversation.isMuted) strings.unmuteChat else strings.muteChat) },
                        onClick = {
                            showMenu = false
                            onToggleMute()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewMessageBottomSheet(
    users: List<UserProfile>,
    onDismiss: () -> Unit,
    onSelectUser: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }

    val filtered = remember(users, query) {
        val q = query.trim().lowercase()
        users.filter { user ->
            q.isEmpty() ||
                user.fullName.lowercase().contains(q) ||
                user.username.lowercase().contains(q)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 6.dp)
        ) {
            Text(
                text = strings.newMessageTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = strings.startChatHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(strings.searchChatsPlaceholder) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_chat_search_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filtered, key = { it.id }) { user ->
                    Surface(
                        onClick = { onSelectUser(user.id) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_chat_user_${user.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(
                                fullName = user.fullName,
                                avatarUri = user.avatarUri,
                                gradientIndex = user.avatarGradientIndex,
                                size = 44.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.fullName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "@${user.username}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = strings.messageButton,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = FzPurple
                            )
                        }
                    }
                }
            }
        }
    }
}
