package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.theme.FriendZoneGradients
import com.example.ui.theme.FzPurple
import com.example.ui.theme.NotoSansBengaliFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily

/**
 * Tactile single-tap runtime language toggle button that switches dynamically
 * between English ("EN") and Bengali ("বাং") with a spring-animated sliding pill.
 *
 * Meets Material 3 accessibility guidelines with a 48.dp minimum interactive
 * touch target, descriptive TalkBack semantics, and ripple feedback.
 */
@Composable
fun LanguageToggleButton(
    currentLanguage: AppLanguage,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    showTranslateIcon: Boolean = true,
    darkSurface: Boolean = false,
    tag: String = "language_toggle_button"
) {
    val strings = LocalAppStrings.current
    val isBengali = currentLanguage == AppLanguage.BENGALI

    val segmentWidth = 34.dp
    val thumbOffset by animateDpAsState(
        targetValue = if (isBengali) segmentWidth else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "language_thumb_offset"
    )

    val enTextColor by animateColorAsState(
        targetValue = when {
            !isBengali -> Color.White
            darkSurface -> Color.White.copy(alpha = 0.72f)
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "en_text_color"
    )
    val bnTextColor by animateColorAsState(
        targetValue = when {
            isBengali -> Color.White
            darkSurface -> Color.White.copy(alpha = 0.72f)
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "bn_text_color"
    )

    val containerColor = if (darkSurface) {
        Color.White.copy(alpha = 0.14f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
    }
    val borderColor = if (darkSurface) {
        Color.White.copy(alpha = 0.28f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
    }
    val iconTint = if (darkSurface) Color.White else FzPurple

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .semantics {
                contentDescription = strings.switchLanguageAction
                stateDescription = currentLanguage.displayName
            }
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                role = Role.Button,
                onClickLabel = strings.switchLanguageAction,
                onClick = onToggle
            )
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = containerColor,
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier.height(36.dp)
        ) {
            Row(
                modifier = Modifier.padding(start = if (showTranslateIcon) 8.dp else 3.dp, end = 3.dp, top = 3.dp, bottom = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (showTranslateIcon) {
                    Icon(
                        imageVector = Icons.Rounded.Translate,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Sliding EN / বাং dual-segment capsule
                Box(
                    modifier = Modifier
                        .width(segmentWidth * 2)
                        .fillMaxHeight()
                ) {
                    // Sliding gradient pill thumb
                    Box(
                        modifier = Modifier
                            .offset(x = thumbOffset)
                            .width(segmentWidth)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(15.dp))
                            .background(FriendZoneGradients.Primary)
                    )

                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .width(segmentWidth)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = AppLanguage.ENGLISH.shortLabel,
                                color = enTextColor,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = if (!isBengali) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(segmentWidth)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = AppLanguage.BENGALI.shortLabel,
                                color = bnTextColor,
                                fontFamily = NotoSansBengaliFontFamily,
                                fontWeight = if (isBengali) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full-width language selector cards for English and Bengali, suited for Settings
 * or onboarding surfaces where full language names and locale tags are helpful.
 */
@Composable
fun LanguageSegmentedSelector(
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AppLanguage.entries.forEach { lang ->
            val selected = currentLanguage == lang
            val cardBrush = if (selected) {
                FriendZoneGradients.Primary
            } else {
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
            val fontFamily = if (lang == AppLanguage.BENGALI) {
                NotoSansBengaliFontFamily
            } else {
                PlusJakartaSansFontFamily
            }

            Surface(
                onClick = { onSelectLanguage(lang) },
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("settings_lang_${lang.code}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(cardBrush, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = lang.displayName,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = lang.code.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) {
                                Color.White.copy(alpha = 0.82f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    if (selected) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        )
                    }
                }
            }
        }
    }
}
