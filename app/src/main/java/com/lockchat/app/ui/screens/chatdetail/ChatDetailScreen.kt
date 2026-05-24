package com.lockchat.app.ui.screens.chatdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lockchat.app.domain.model.Direction
import com.lockchat.app.domain.model.MessageStatus
import com.lockchat.app.domain.model.MessageUi
import com.lockchat.app.ui.theme.LockChatTheme
import com.lockchat.app.ui.theme.TerminalFontFamily
import kotlinx.coroutines.launch

@Composable
fun ChatDetailScreen(
    contactId: String,
    viewModel: ChatDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(state.messages.size - 1) }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onScreenEntered()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LockChatTheme.colors.background)
            .navigationBarsPadding()
            .imePadding()
    ) {
        // TopBar
        ChatDetailTopBar(
            handle         = state.contactHandle,
            nodeIdFormatted = state.nodeIdFormatted,
            isOnline       = state.isOnline,
            onBackClick    = onNavigateBack
        )

        // Banner canal cifrado
        ChannelInfoBanner(channelLabel = state.channelInfo)

        // Separador HOY
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("HOY", style = MaterialTheme.typography.labelSmall,
                color = LockChatTheme.colors.outline)
        }

        // Lista de mensajes
        LazyColumn(
            state          = listState,
            modifier       = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(state.messages, key = { it.id }) { msg ->
                MessageBubble(message = msg)
            }
        }

        // Input
        MessageInput(
            text         = state.inputText,
            onTextChange = viewModel::onInputChange,
            onSendClick  = viewModel::onSend,
            isSending    = state.isSending
        )
    }
}

@Composable
private fun ChatDetailTopBar(
    handle: String,
    nodeIdFormatted: String,
    isOnline: Boolean,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LockChatTheme.colors.surface)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás",
                tint = LockChatTheme.colors.primary)
        }

        // Avatar
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape)
                .background(LockChatTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(handle.take(2).uppercase(), color = LockChatTheme.colors.primary,
                fontFamily = TerminalFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(handle, style = MaterialTheme.typography.titleSmall,
                    color = LockChatTheme.colors.onBackground)
                if (isOnline) {
                    Text("● ONLINE", style = MaterialTheme.typography.labelSmall,
                        color = LockChatTheme.colors.primary, fontFamily = TerminalFontFamily)
                }
            }
            Text(nodeIdFormatted, style = MaterialTheme.typography.labelSmall,
                color = LockChatTheme.colors.outline)
        }
    }
}

@Composable
private fun ChannelInfoBanner(channelLabel: String) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(LockChatTheme.colors.surface.copy(alpha = 0.6f))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("[E2E] $channelLabel", style = MaterialTheme.typography.labelSmall,
            color = LockChatTheme.colors.outline, textAlign = TextAlign.Center)
    }
}

@Composable
private fun MessageBubble(message: MessageUi) {
    val isOutgoing = message.direction == Direction.OUTGOING
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        // Sender handle
        Text(
            text   = if (isOutgoing) "Tú" else (message.senderHandle ?: ""),
            style  = MaterialTheme.typography.labelSmall,
            color  = if (isOutgoing) LockChatTheme.colors.outline else LockChatTheme.colors.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (isOutgoing) LockChatTheme.colors.background
                    else LockChatTheme.colors.surfaceVariant,
                    shape = RoundedCornerShape(
                        topStart = 12.dp, topEnd = 12.dp,
                        bottomStart = if (isOutgoing) 12.dp else 2.dp,
                        bottomEnd   = if (isOutgoing) 2.dp else 12.dp
                    )
                )
                .then(
                    if (isOutgoing)
                        Modifier.background(
                            color = LockChatTheme.colors.background,
                            shape = RoundedCornerShape(12.dp, 12.dp, 2.dp, 12.dp)
                        ).padding(1.dp).background(
                            color = LockChatTheme.colors.primary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(11.dp, 11.dp, 1.dp, 11.dp)
                        )
                    else Modifier
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                Text(message.content, style = MaterialTheme.typography.bodyMedium,
                    color = LockChatTheme.colors.onBackground)
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(message.timestamp, style = MaterialTheme.typography.labelSmall,
                        color = LockChatTheme.colors.outline, fontSize = 10.sp)
                    if (isOutgoing) {
                        Text(
                            text  = when (message.status) {
                                MessageStatus.SENDING   -> "○"
                                MessageStatus.SENT      -> "✓"
                                MessageStatus.DELIVERED -> "✓✓"
                                MessageStatus.FAILED    -> "✗"
                            },
                            color = when (message.status) {
                                MessageStatus.FAILED -> LockChatTheme.colors.error
                                else                 -> LockChatTheme.colors.primary
                            },
                            fontSize = 11.sp, fontFamily = TerminalFontFamily
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isSending: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LockChatTheme.colors.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value         = text,
            onValueChange = onTextChange,
            modifier      = Modifier.weight(1f),
            placeholder   = {
                Text("> escribe un mensaje...", color = LockChatTheme.colors.outline,
                    fontFamily = TerminalFontFamily, fontSize = 13.sp)
            },
            singleLine    = true,
            shape         = RoundedCornerShape(4.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor    = LockChatTheme.colors.primary,
                unfocusedBorderColor  = LockChatTheme.colors.outline.copy(alpha = 0.4f),
                focusedTextColor      = LockChatTheme.colors.onBackground,
                unfocusedTextColor    = LockChatTheme.colors.onBackground,
                cursorColor           = LockChatTheme.colors.primary,
                focusedContainerColor    = LockChatTheme.colors.surfaceVariant,
                unfocusedContainerColor  = LockChatTheme.colors.surfaceVariant
            ),
            textStyle = MaterialTheme.typography.bodyMedium
        )

        IconButton(
            onClick  = onSendClick,
            enabled  = text.isNotBlank() && !isSending,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (text.isNotBlank()) LockChatTheme.colors.primary
                    else LockChatTheme.colors.outline.copy(alpha = 0.3f)
                )
        ) {
            if (isSending) {
                CircularProgressIndicator(Modifier.size(20.dp),
                    color = LockChatTheme.colors.onPrimary, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Send, contentDescription = "Enviar",
                    tint = LockChatTheme.colors.onPrimary, modifier = Modifier.size(20.dp))
            }
        }
    }
}
