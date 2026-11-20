package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.i18n.LocalAppStrings
import com.example.model.ChatConversation
import com.example.model.ChatMessage
import com.example.model.UserProfile
import com.example.ui.components.UserAvatar
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.FriendZoneGradients
import com.example.ui.theme.FzPinkHot
import com.example.ui.theme.FzPurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.VerifiedBlue

private val QuickReactionEmojis = listOf("❤️", "😂", "🔥", "👍", "😮")

@Composable
fun ChatThreadScreen(
    conversation: ChatConversation,
    participant: UserProfile,
    currentUser: UserProfile,
    messages: List<ChatMessage>,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onSendMessage: (text: String, mediaUri: String?, replyToMessageId: String?) -> Unit,
    onToggleReaction: (messageId: String, emoji: String) -> Unit,
    onDeleteMessage: (messageId: String) -> Unit,
    onTogglePin: () -> Unit,
    onToggleMute: () -> Unit
) {
    BackHandler(onBack = onBack)
    ChatThreadPane(
        conversation = conversation,
        participant = participant,
        currentUser = currentUser,
        messages = messages,
        showBackButton = true,
        onBack = onBack,
        onOpenProfile = onOpenProfile,
        onSendMessage = onSendMessage,
        onToggleReaction = onToggleReaction,
        onDeleteMessage = onDeleteMessage,
        onTogglePin = onTogglePin,
        onToggleMute = onToggleMute
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatThreadPane(
    conversation: ChatConversation,
    participant: UserProfile,
    currentUser: UserProfile,
    messages: List<ChatMessage>,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onSendMessage: (text: String, mediaUri: String?, replyToMessageId: String?) -> Unit,
    onToggleReaction: (messageId: String, emoji: String) -> Unit,
    onDeleteMessage: (messageId: String) -> Unit,
    onTogglePin: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val listState = rememberLazyListState()

    var draftText by remember(conversation.id) { mutableStateOf("") }
    var attachedPhotoUri by remember(conversation.id) { mutableStateOf<String?>(null) }
    var replyingTo by remember(conversation.id) { mutableStateOf<ChatMessage?>(null) }
    var activeActionMessageId by remember(conversation.id) { mutableStateOf<String?>(null) }
    var inspectedPhoto by remember { mutableStateOf<Pair<String?, Int?>?>(null) }

    val messageMap = remember(messages) { messages.associateBy { it.id } }

    // Zero-permission Android Photo Picker (Play Policy compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            attachedPhotoUri = uri.toString()
        }
    }

    LaunchedEffect(messages.size, conversation.id) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    Scaffold(
        modifier = modifier.testTag("chat_thread_screen_${conversation.id}"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    navigationIcon = {
                        if (showBackButton) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("chat_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenProfile(participant.id) }
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                .testTag("chat_header_profile")
                        ) {
                            Box {
                                UserAvatar(
                                    fullName = participant.fullName,
                                    avatarUri = participant.avatarUri,
                                    gradientIndex = participant.avatarGradientIndex,
                                    size = 40.dp
                                )
                                if (conversation.isOnline) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .align(Alignment.BottomEnd)
                                            .clip(CircleShape)
                                            .background(SuccessGreen)
                                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = participant.fullName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (participant.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Verified",
                                            tint = VerifiedBlue,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (conversation.isOnline) strings.activeNowStatus else conversation.lastActiveLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (conversation.isOnline) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onTogglePin,
                            modifier = Modifier.testTag("chat_pin_button")
                        ) {
                            Icon(
                                imageVector = if (conversation.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = if (conversation.isPinned) strings.unpinChat else strings.pinChat,
                                tint = if (conversation.isPinned) FzPurple else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = onToggleMute,
                            modifier = Modifier.testTag("chat_mute_button")
                        ) {
                            Icon(
                                imageVector = if (conversation.isMuted) Icons.Filled.NotificationsOff else Icons.Outlined.NotificationsActive,
                                contentDescription = if (conversation.isMuted) strings.unmuteChat else strings.muteChat,
                                tint = if (conversation.isMuted) FzPinkHot else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
        bottomBar = {
            ChatComposerBar(
                draftText = draftText,
                onDraftChange = { draftText = it },
                attachedPhotoUri = attachedPhotoUri,
                onClearPhoto = { attachedPhotoUri = null },
                onPickPhoto = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                replyingTo = replyingTo,
                replyAuthorName = replyingTo?.let { msg ->
                    if (msg.senderId == currentUser.id) strings.youPrefix else participant.fullName
                },
                onCancelReply = { replyingTo = null },
                onSend = {
                    onSendMessage(draftText, attachedPhotoUri, replyingTo?.id)
                    draftText = ""
                    attachedPhotoUri = null
                    replyingTo = null
                },
                onSendQuickHeart = {
                    onSendMessage("❤️", null, replyingTo?.id)
                    replyingTo = null
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = { activeActionMessageId = null }
                )
                .testTag("chat_messages_list"),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Conversation Intro Header
            item(key = "intro_${participant.id}") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserAvatar(
                        fullName = participant.fullName,
                        avatarUri = participant.avatarUri,
                        gradientIndex = participant.avatarGradientIndex,
                        size = 68.dp,
                        showStoryRing = true,
                        onClick = { onOpenProfile(participant.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = participant.fullName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "@${participant.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = if (messages.isEmpty()) strings.sayHelloHint else strings.todayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                val isMine = msg.senderId == currentUser.id
                val quoted = msg.replyToMessageId?.let { messageMap[it] }
                val isSelected = activeActionMessageId == msg.id

                ChatBubbleRow(
                    message = msg,
                    isMine = isMine,
                    participant = participant,
                    quotedMessage = quoted,
                    quotedAuthorLabel = quoted?.let {
                        if (it.senderId == currentUser.id) strings.youPrefix else participant.fullName.substringBefore(" ")
                    },
                    showActions = isSelected,
                    onToggleActions = {
                        activeActionMessageId = if (isSelected) null else msg.id
                    },
                    onReact = { emoji ->
                        onToggleReaction(msg.id, emoji)
                        activeActionMessageId = null
                    },
                    onReply = {
                        replyingTo = msg
                        activeActionMessageId = null
                    },
                    onDelete = {
                        onDeleteMessage(msg.id)
                        activeActionMessageId = null
                    },
                    onPhotoClick = {
                        inspectedPhoto = msg.mediaUri to msg.mediaResId
                    }
                )
            }
        }
    }

    inspectedPhoto?.let { (uri, resId) ->
        Dialog(
            onDismissRequest = { inspectedPhoto = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.94f))
                    .clickable { inspectedPhoto = null },
                contentAlignment = Alignment.Center
            ) {
                when {
                    !uri.isNullOrBlank() -> AsyncImage(
                        model = uri,
                        contentDescription = strings.photoAttachmentLabel,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                    resId != null -> Image(
                        painter = painterResource(id = resId),
                        contentDescription = strings.photoAttachmentLabel,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
                IconButton(
                    onClick = { inspectedPhoto = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = strings.close,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatBubbleRow(
    message: ChatMessage,
    isMine: Boolean,
    participant: UserProfile,
    quotedMessage: ChatMessage?,
    quotedAuthorLabel: String?,
    showActions: Boolean,
    onToggleActions: () -> Unit,
    onReact: (String) -> Unit,
    onReply: () -> Unit,
    onDelete: () -> Unit,
    onPhotoClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    val bubbleShape = if (isMine) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        // Floating Quick Reaction & Action Bar
        AnimatedVisibility(visible = showActions) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    QuickReactionEmojis.forEach { emoji ->
                        val active = message.reaction == emoji
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (active) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .clickable { onReact(emoji) }
                                .testTag("react_${message.id}_$emoji"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 18.sp)
                        }
                    }
                    IconButton(
                        onClick = onReply,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("reply_msg_${message.id}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Reply,
                            contentDescription = strings.replyAction,
                            tint = FzPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (isMine) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("delete_msg_${message.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = strings.deleteMessage,
                                tint = ErrorRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isMine) {
                UserAvatar(
                    fullName = participant.fullName,
                    avatarUri = participant.avatarUri,
                    gradientIndex = participant.avatarGradientIndex,
                    size = 28.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
                modifier = Modifier.widthIn(max = 290.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(bubbleShape)
                        .then(
                            if (isMine) {
                                Modifier.background(FriendZoneGradients.Primary)
                            } else {
                                Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, bubbleShape)
                            }
                        )
                        .combinedClickable(
                            onClick = onToggleActions,
                            onLongClick = onToggleActions
                        )
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                        .testTag("chat_bubble_${message.id}")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Optional Quoted Reply Preview
                        if (quotedMessage != null && quotedAuthorLabel != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isMine) Color.White.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                                    Text(
                                        text = quotedAuthorLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isMine) Color.White else FzPurple,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = quotedMessage.text.ifBlank { strings.photoAttachmentLabel },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isMine) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Optional Photo Attachment
                        if (!message.mediaUri.isNullOrBlank() || message.mediaResId != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(onClick = onPhotoClick)
                            ) {
                                when {
                                    !message.mediaUri.isNullOrBlank() -> AsyncImage(
                                        model = message.mediaUri,
                                        contentDescription = strings.photoAttachmentLabel,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(170.dp)
                                    )
                                    message.mediaResId != null -> Image(
                                        painter = painterResource(id = message.mediaResId),
                                        contentDescription = strings.photoAttachmentLabel,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(170.dp)
                                    )
                                }
                            }
                        }

                        // Text Content
                        if (message.text.isNotBlank()) {
                            val isSingleEmoji = message.text == "❤️"
                            Text(
                                text = message.text,
                                style = if (isSingleEmoji) {
                                    MaterialTheme.typography.headlineMedium
                                } else {
                                    MaterialTheme.typography.bodyMedium
                                },
                                color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Reaction Pill + Timestamp & Read Checkmarks
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 3.dp, start = 4.dp, end = 4.dp)
                ) {
                    if (!message.reaction.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            tonalElevation = 2.dp
                        ) {
                            Text(
                                text = message.reaction,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = message.timestampLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isMine) {
                        Icon(
                            imageVector = Icons.Filled.DoneAll,
                            contentDescription = "Delivered",
                            tint = if (message.isRead) FzPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatComposerBar(
    draftText: String,
    onDraftChange: (String) -> Unit,
    attachedPhotoUri: String?,
    onClearPhoto: () -> Unit,
    onPickPhoto: () -> Unit,
    replyingTo: ChatMessage?,
    replyAuthorName: String?,
    onCancelReply: () -> Unit,
    onSend: () -> Unit,
    onSendQuickHeart: () -> Unit
) {
    val strings = LocalAppStrings.current
    val hasPayload = draftText.isNotBlank() || !attachedPhotoUri.isNullOrBlank()

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Reply Banner
            AnimatedVisibility(visible = replyingTo != null && replyAuthorName != null) {
                if (replyingTo != null && replyAuthorName != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${strings.replyingTo} $replyAuthorName",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = replyingTo.text.ifBlank { strings.photoAttachmentLabel },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = onCancelReply, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = strings.cancel,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Attached Photo Thumbnail Preview
            AnimatedVisibility(visible = !attachedPhotoUri.isNullOrBlank()) {
                if (!attachedPhotoUri.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(60.dp)) {
                            AsyncImage(
                                model = attachedPhotoUri,
                                contentDescription = strings.photoAttachmentLabel,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .clickable(onClick = onClearPhoto),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = strings.cancel,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onPickPhoto,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("chat_attach_photo_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = strings.attachPhotoAction,
                        tint = FzPurple
                    )
                }

                OutlinedTextField(
                    value = draftText,
                    onValueChange = onDraftChange,
                    placeholder = { Text(strings.typeMessagePlaceholder) },
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedBorderColor = FzPurple
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input")
                )

                if (hasPayload) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(FriendZoneGradients.Primary)
                            .clickable(onClick = onSend)
                            .testTag("chat_send_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = strings.sendMessageAction,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onSendQuickHeart,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("chat_quick_heart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = strings.quickHeartAction,
                            tint = FzPinkHot,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}
