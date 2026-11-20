package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.i18n.LocalAppStrings
import com.example.model.MediaType
import com.example.ui.components.GradientButton
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.FriendZoneGradients
import com.example.ui.theme.FzPink
import com.example.ui.theme.FzPurple

private data class SampleMediaOption(
    val id: String,
    val resId: Int,
    val type: MediaType,
    val label: String
)

private val sampleMediaOptions = listOf(
    SampleMediaOption("sample_rooftop", R.drawable.img_post_sunset_rooftop, MediaType.PHOTO, "Sunset Café"),
    SampleMediaOption("sample_dhaka", R.drawable.img_post_dhaka_street, MediaType.PHOTO, "Dhaka Art"),
    SampleMediaOption("sample_coast", R.drawable.img_post_coastal_escape, MediaType.VIDEO, "Beach Reel"),
    SampleMediaOption("sample_aurora", R.drawable.img_cover_aurora, MediaType.PHOTO, "Silk Glow")
)

private val trendingHashtags = listOf(
    "#FriendZone",
    "#DhakaVibes",
    "#GoldenHour",
    "#Photography",
    "#Travel",
    "#Bangladesh"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreatePostScreen(
    onPublish: (
        mediaType: MediaType,
        mediaResId: Int?,
        mediaUri: String?,
        caption: String,
        rawHashtags: String
    ) -> Boolean
) {
    val strings = LocalAppStrings.current

    var mediaType by remember { mutableStateOf(MediaType.PHOTO) }
    var selectedResId by remember { mutableStateOf<Int?>(R.drawable.img_post_sunset_rooftop) }
    var selectedUri by remember { mutableStateOf<String?>(null) }
    var caption by remember { mutableStateOf("") }
    var hashtags by remember { mutableStateOf("#FriendZone ") }
    var mediaError by remember { mutableStateOf<String?>(null) }
    var captionError by remember { mutableStateOf<String?>(null) }

    // Zero-permission Android Photo Picker (Play Policy compliant)
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri.toString()
            selectedResId = null
            mediaType = MediaType.PHOTO
            mediaError = null
        }
    }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri.toString()
            selectedResId = null
            mediaType = MediaType.VIDEO
            mediaError = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .statusBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .testTag("create_post_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = strings.createPostTitle,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Upload Photo & Upload Video Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = if (mediaType == MediaType.PHOTO && selectedUri != null) 2.dp else 1.dp,
                        color = if (mediaType == MediaType.PHOTO) FzPurple else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("upload_photo_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddPhotoAlternate,
                        contentDescription = null,
                        tint = FzPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.uploadPhoto,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedButton(
                    onClick = {
                        videoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = if (mediaType == MediaType.VIDEO && selectedUri != null) 2.dp else 1.dp,
                        color = if (mediaType == MediaType.VIDEO) FzPink else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("upload_video_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Videocam,
                        contentDescription = null,
                        tint = FzPink,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.uploadVideo,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Live Media Preview Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.35f),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        !selectedUri.isNullOrBlank() -> {
                            AsyncImage(
                                model = selectedUri,
                                contentDescription = "Selected media preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        selectedResId != null -> {
                            Image(
                                painter = painterResource(id = selectedResId!!),
                                contentDescription = "Sample visual preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = strings.previewPlaceholder,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Media Type Badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (mediaType == MediaType.VIDEO) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = if (mediaType == MediaType.VIDEO) strings.videoTag else strings.photoTag,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    if (mediaType == MediaType.VIDEO && !selectedUri.isNullOrBlank()) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = FzPurple.copy(alpha = 0.85f)
                        ) {
                            Text(
                                text = strings.videoSelectedPreview,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            if (mediaError != null) {
                Text(
                    text = mediaError!!,
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Quick Sample Gallery Picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.sampleGalleryTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(sampleMediaOptions, key = { it.id }) { option ->
                        val isSelected = selectedUri == null && selectedResId == option.resId
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier.border(
                                            width = 2.5.dp,
                                            brush = FriendZoneGradients.Primary,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable {
                                    selectedResId = option.resId
                                    selectedUri = null
                                    mediaType = option.type
                                    mediaError = null
                                }
                                .testTag("sample_media_${option.id}")
                        ) {
                            Image(
                                painter = painterResource(id = option.resId),
                                contentDescription = option.label,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (option.type == MediaType.VIDEO) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(4.dp)
                                        .size(18.dp)
                                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = FzPink,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp)
                                        .background(Color.White, RoundedCornerShape(10.dp))
                                )
                            }
                        }
                    }
                }
            }

            // Caption Input
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it; captionError = null },
                label = { Text(strings.captionLabel) },
                placeholder = { Text(strings.captionPlaceholder) },
                isError = captionError != null,
                supportingText = captionError?.let { { Text(it) } },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_caption_input")
            )

            // Hashtags Input
            OutlinedTextField(
                value = hashtags,
                onValueChange = { hashtags = it },
                label = { Text(strings.hashtagsLabel) },
                placeholder = { Text(strings.hashtagsPlaceholder) },
                leadingIcon = { Icon(Icons.Outlined.Tag, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_hashtags_input")
            )

            // Trending Hashtag Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = strings.trendingTags,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    trendingHashtags.forEach { tag ->
                        AssistChip(
                            onClick = {
                                if (!hashtags.contains(tag, ignoreCase = true)) {
                                    hashtags = (hashtags.trim() + " $tag ").trimStart()
                                }
                            },
                            label = { Text(tag, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            GradientButton(
                text = strings.publishButton,
                onClick = {
                    val hasMedia = selectedResId != null || !selectedUri.isNullOrBlank()
                    val hasCaption = caption.trim().isNotEmpty()
                    mediaError = if (!hasMedia) strings.errSelectMedia else null
                    captionError = if (!hasCaption) strings.errEmptyCaption else null

                    if (hasMedia && hasCaption) {
                        val published = onPublish(
                            mediaType,
                            selectedResId,
                            selectedUri,
                            caption,
                            hashtags
                        )
                        if (published) {
                            caption = ""
                            hashtags = "#FriendZone "
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("publish_post_button")
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
