package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.i18n.LocalAppStrings
import com.example.model.SocialPost
import com.example.model.UserProfile
import com.example.ui.components.FollowPillButton
import com.example.ui.components.UserAvatar
import com.example.ui.components.formatCompactCount
import com.example.ui.theme.FzPurple
import com.example.ui.theme.VerifiedBlue

private enum class ExploreTab {
    GRID,
    USERS,
    POSTS
}

@Composable
fun ExploreScreen(
    currentUserId: String,
    users: List<UserProfile>,
    posts: List<SocialPost>,
    onUserClick: (String) -> Unit,
    onFollowClick: (String) -> Unit,
    onPostClick: (SocialPost) -> Unit
) {
    val strings = LocalAppStrings.current
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(ExploreTab.GRID) }

    val cleanQuery = query.trim().lowercase()
    val filteredUsers = remember(users, cleanQuery, currentUserId) {
        users.filter { it.id != currentUserId }.filter { user ->
            cleanQuery.isEmpty() ||
                user.fullName.lowercase().contains(cleanQuery) ||
                user.username.lowercase().contains(cleanQuery) ||
                user.bio.lowercase().contains(cleanQuery)
        }
    }
    val userMap = remember(users) { users.associateBy { it.id } }

    val filteredPosts = remember(posts, cleanQuery, userMap) {
        posts.filter { post ->
            if (cleanQuery.isEmpty()) true
            else {
                val author = userMap[post.authorId]
                post.caption.lowercase().contains(cleanQuery) ||
                    post.hashtags.any { it.lowercase().contains(cleanQuery) } ||
                    (author?.username?.lowercase()?.contains(cleanQuery) == true) ||
                    (author?.fullName?.lowercase()?.contains(cleanQuery) == true)
            }
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
                .testTag("explore_screen")
        ) {
            // Search Input
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(strings.searchPlaceholder) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = strings.navSearch,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search")
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
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("explore_search_input")
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf(
                    ExploreTab.GRID to strings.tabExplore,
                    ExploreTab.USERS to strings.tabUsers,
                    ExploreTab.POSTS to strings.tabPosts
                )
                tabs.forEach { (tab, label) ->
                    FilterChip(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(label, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("explore_tab_${tab.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            when (selectedTab) {
                ExploreTab.GRID -> {
                    if (filteredPosts.isEmpty()) {
                        EmptySearchState()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Suggested Creators Strip when query is empty
                            if (cleanQuery.isEmpty() && filteredUsers.isNotEmpty()) {
                                item(key = "suggested_header") {
                                    Text(
                                        text = strings.suggestedPeople,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                                items(filteredUsers.take(2), key = { "sug_${it.id}" }) { user ->
                                    UserSearchRow(
                                        user = user,
                                        onClick = { onUserClick(user.id) },
                                        onFollowClick = { onFollowClick(user.id) },
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                                item(key = "grid_spacer") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            items(filteredPosts.chunked(3)) { rowPosts ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    rowPosts.forEach { post ->
                                        ProfileGridTile(
                                            post = post,
                                            onClick = { onPostClick(post) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    repeat(3 - rowPosts.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                ExploreTab.USERS -> {
                    if (filteredUsers.isEmpty()) {
                        EmptySearchState()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredUsers, key = { it.id }) { user ->
                                UserSearchRow(
                                    user = user,
                                    onClick = { onUserClick(user.id) },
                                    onFollowClick = { onFollowClick(user.id) }
                                )
                            }
                        }
                    }
                }

                ExploreTab.POSTS -> {
                    if (filteredPosts.isEmpty()) {
                        EmptySearchState()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredPosts, key = { it.id }) { post ->
                                val author = userMap[post.authorId]
                                Surface(
                                    onClick = { onPostClick(post) },
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ProfileGridTile(
                                            post = post,
                                            onClick = { onPostClick(post) },
                                            modifier = Modifier.size(68.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = author?.fullName ?: "@friendzone",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = post.caption,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (post.hashtags.isNotEmpty()) {
                                                Text(
                                                    text = post.hashtags.joinToString(" "),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = FzPurple,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserSearchRow(
    user: UserProfile,
    onClick: () -> Unit,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("user_search_row_${user.id}"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                fullName = user.fullName,
                avatarUri = user.avatarUri,
                gradientIndex = user.avatarGradientIndex,
                size = 46.dp,
                showStoryRing = true
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (user.isVerified) {
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
                    text = "@${user.username} • ${formatCompactCount(user.followersCount)} ${strings.followersStat}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            FollowPillButton(
                isFollowing = user.isFollowedByCurrentUser,
                onClick = onFollowClick
            )
        }
    }
}

@Composable
private fun EmptySearchState() {
    val strings = LocalAppStrings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(42.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = strings.noResultsFound,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = strings.tryAnotherSearch,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
