package com.lockchat.app.ui.screens.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lockchat.app.domain.model.ChatFilter
import com.lockchat.app.domain.model.ConversationUi
import com.lockchat.app.domain.model.TransportUiState
import com.lockchat.app.ui.theme.LockChatTheme
import com.lockchat.app.ui.theme.TerminalFontFamily
import com.lockchat.app.ui.components.SignalWavesIcon
import com.lockchat.app.ui.screens.onboarding.LockChatPermissionsGate

@Composable
fun ChatsScreen(
    viewModel: ChatsViewModel = hiltViewModel(),
    onChatClick: (String) -> Unit,
    onNavigateToPing: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAddContact: () -> Unit,
    onNavigateToSolicitudes: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LockChatPermissionsGate {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick       = onNavigateToAddContact,
                containerColor = LockChatTheme.colors.primary,
                contentColor  = LockChatTheme.colors.onPrimary,
                shape         = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar contacto")
            }
        },
        bottomBar = {
            BottomNavBar(
                currentRoute   = "chats",
                unreadCount    = state.totalUnread,
                onChatsClick   = {},
                onPingClick    = onNavigateToPing,
                onProfileClick = onNavigateToProfile
            )
        },
        containerColor = LockChatTheme.colors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(LockChatTheme.colors.background)
        ) {
            ChatsTopBar(
                appVersion       = state.appVersion,
                newMessagesCount = state.totalUnread,
                solicitudesCount = state.solicitudesCount,
                transportState   = state.transportState,
                onSolicitudes    = onNavigateToSolicitudes
            )

            OutlinedTextField(
                value         = "",
                onValueChange = {},
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = {
                    Text("buscar contacto...", color = LockChatTheme.colors.outline,
                        fontFamily = TerminalFontFamily, fontSize = 13.sp)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null,
                        tint = LockChatTheme.colors.outline, modifier = Modifier.size(18.dp))
                },
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = LockChatTheme.colors.primary,
                    unfocusedBorderColor    = LockChatTheme.colors.outline.copy(alpha = 0.4f),
                    focusedTextColor        = LockChatTheme.colors.onBackground,
                    unfocusedTextColor      = LockChatTheme.colors.onBackground,
                    focusedContainerColor   = LockChatTheme.colors.surface,
                    unfocusedContainerColor = LockChatTheme.colors.surface
                )
            )

            // Filtros
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val unreadCount = state.conversations.count { it.unreadCount > 0 }
                FilterChip(
                    selected = state.filter == ChatFilter.TODOS,
                    onClick  = { viewModel.onFilterChange(ChatFilter.TODOS) },
                    label    = { Text("TODOS", fontFamily = TerminalFontFamily, fontSize = 12.sp) },
                    shape    = RoundedCornerShape(4.dp),
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LockChatTheme.colors.primary.copy(alpha = 0.2f),
                        selectedLabelColor     = LockChatTheme.colors.primary,
                        labelColor             = LockChatTheme.colors.onSurfaceVariant
                    )
                )
                FilterChip(
                    selected = state.filter == ChatFilter.NO_LEIDOS,
                    onClick  = { viewModel.onFilterChange(ChatFilter.NO_LEIDOS) },
                    label = {
                        Text(
                            "NO LEÍDOS${if (unreadCount > 0) " ($unreadCount)" else ""}",
                            fontFamily = TerminalFontFamily, fontSize = 12.sp
                        )
                    },
                    shape = RoundedCornerShape(4.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LockChatTheme.colors.primary.copy(alpha = 0.2f),
                        selectedLabelColor     = LockChatTheme.colors.primary,
                        labelColor             = LockChatTheme.colors.onSurfaceVariant
                    )
                )
            }

            if (state.filteredConversations.isEmpty() && !state.isLoading) {
                // Estado vacío
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SignalWavesIcon(
                            color = LockChatTheme.colors.outline,
                            size = 72.dp
                        )
                        Text(
                            text = "Sin contactos aún",
                            style = MaterialTheme.typography.titleMedium,
                            color = LockChatTheme.colors.onSurfaceVariant
                        )
                        Text(
                            text = "Toca (+) para agregar contactos\nescaneando su código QR",
                            style = MaterialTheme.typography.bodySmall,
                            color = LockChatTheme.colors.outline,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(state.filteredConversations, key = { it.contactId }) { conv ->
                        ConversationItem(conversation = conv, onClick = { onChatClick(conv.contactId) })
                        HorizontalDivider(
                            color     = LockChatTheme.colors.outline.copy(alpha = 0.15f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
fun ChatsTopBar(
    appVersion: String,
    newMessagesCount: Int,
    solicitudesCount: Int,
    transportState: TransportUiState,
    onSolicitudes: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LockChatTheme.colors.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SignalWavesIcon(
                color = if (transportState == TransportUiState.SIN_TRANSPORTE)
                    LockChatTheme.colors.outline else LockChatTheme.colors.primary,
                size = 18.dp
            )
            Text(
                text = appVersion,
                style = MaterialTheme.typography.titleMedium,
                color = LockChatTheme.colors.onBackground
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (solicitudesCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(containerColor = LockChatTheme.colors.primary) {
                            Text(
                                solicitudesCount.toString(),
                                fontFamily = TerminalFontFamily,
                                fontSize = 9.sp,
                                color = LockChatTheme.colors.onPrimary
                            )
                        }
                    }
                ) {
                    IconButton(onClick = onSolicitudes) {
                        Icon(
                            Icons.Outlined.Inbox,
                            contentDescription = "Solicitudes",
                            tint = LockChatTheme.colors.primary
                        )
                    }
                }
            } else {
                IconButton(onClick = onSolicitudes) {
                    Icon(
                        Icons.Outlined.Inbox,
                        contentDescription = "Solicitudes",
                        tint = LockChatTheme.colors.outline
                    )
                }
            }
            if (newMessagesCount > 0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LockChatTheme.colors.surfaceVariant
                ) {
                    Text(
                        text = "$newMessagesCount NUEVOS",
                        style = MaterialTheme.typography.labelSmall,
                        color = LockChatTheme.colors.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationItem(conversation: ConversationUi, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(LockChatTheme.colors.surfaceVariant)
                .then(
                    if (conversation.isOnline)
                        Modifier.border(2.dp, LockChatTheme.colors.primary, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(conversation.avatarInitials, color = LockChatTheme.colors.primary,
                fontFamily = TerminalFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = conversation.handle,
                        style = MaterialTheme.typography.titleSmall,
                        color = LockChatTheme.colors.onBackground,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (conversation.isBleAvailable) {
                        Text(
                            text = "[BLE]",
                            color = LockChatTheme.colors.primary,
                            fontFamily = TerminalFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (conversation.isLoraAvailable) {
                        Text(
                            text = "[LoRa]",
                            color = LockChatTheme.colors.primary,
                            fontFamily = TerminalFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(conversation.lastMessageTime, style = MaterialTheme.typography.labelSmall,
                    color = LockChatTheme.colors.outline)
            }
            Text(conversation.nodeIdFormatted, style = MaterialTheme.typography.labelSmall,
                color = LockChatTheme.colors.outline)
            Spacer(Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(conversation.lastMessagePreview, style = MaterialTheme.typography.bodySmall,
                    color = LockChatTheme.colors.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                if (conversation.unreadCount > 0) {
                    Box(modifier = Modifier.size(20.dp).clip(CircleShape)
                        .background(LockChatTheme.colors.primary),
                        contentAlignment = Alignment.Center) {
                        Text(conversation.unreadCount.toString(),
                            color = LockChatTheme.colors.onPrimary,
                            fontFamily = TerminalFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(
    currentRoute: String,
    unreadCount: Int,
    onChatsClick: () -> Unit,
    onPingClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = LockChatTheme.colors.surface,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "chats",
            onClick  = onChatsClick,
            icon = {
                BadgedBox(badge = {
                    if (unreadCount > 0) Badge(containerColor = LockChatTheme.colors.primary) {
                        Text(unreadCount.toString(), fontFamily = TerminalFontFamily, fontSize = 9.sp,
                            color = LockChatTheme.colors.onPrimary)
                    }
                }) {
                    Icon(Icons.Default.Chat, contentDescription = "Chats",
                        tint = if (currentRoute == "chats") LockChatTheme.colors.primary
                        else LockChatTheme.colors.outline)
                }
            },
            label = {
                Text("Chats", fontFamily = TerminalFontFamily,
                    color = if (currentRoute == "chats") LockChatTheme.colors.primary
                    else LockChatTheme.colors.outline, fontSize = 10.sp)
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = LockChatTheme.colors.primary.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "ping",
            onClick  = onPingClick,
            icon = {
                Icon(Icons.Outlined.NetworkCheck, contentDescription = "Ping",
                    tint = if (currentRoute == "ping") LockChatTheme.colors.primary
                    else LockChatTheme.colors.outline)
            },
            label = {
                Text("Ping", fontFamily = TerminalFontFamily,
                    color = if (currentRoute == "ping") LockChatTheme.colors.primary
                    else LockChatTheme.colors.outline, fontSize = 10.sp)
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = LockChatTheme.colors.primary.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick  = onProfileClick,
            icon = {
                Icon(Icons.Default.Person, contentDescription = "Perfil",
                    tint = if (currentRoute == "profile") LockChatTheme.colors.primary
                    else LockChatTheme.colors.outline)
            },
            label = {
                Text("Perfil", fontFamily = TerminalFontFamily,
                    color = if (currentRoute == "profile") LockChatTheme.colors.primary
                    else LockChatTheme.colors.outline, fontSize = 10.sp)
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = LockChatTheme.colors.primary.copy(alpha = 0.15f)
            )
        )
    }
}
