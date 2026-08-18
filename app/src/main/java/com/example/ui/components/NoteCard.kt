package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioPlayerHelper
import com.example.data.NoteEntity
import com.example.model.NoteColorThemes
import com.example.model.NoteFontStyle
import com.example.model.NoteType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onPinClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onArchiveSwipe: () -> Unit,
    onDeleteSwipe: () -> Unit,
    onShareClick: (() -> Unit)? = null,
    onExportTxtClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val colorTheme = NoteColorThemes.getById(note.colorIndex)
    val cardBg = if (isDark) colorTheme.darkBg else colorTheme.lightBg
    val borderColor = if (isDark) colorTheme.cardBorderDark else colorTheme.cardBorderLight
    val primaryText = if (isDark) colorTheme.textColorDark else colorTheme.textColorLight
    val secondaryText = primaryText.copy(alpha = 0.72f)

    var showCardMenu by remember { mutableStateOf(false) }

    val font = try {
        NoteFontStyle.valueOf(note.fontStyle).fontFamily
    } catch (e: Exception) {
        NoteFontStyle.SANS.fontFamily
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) {
                onDeleteSwipe()
                true
            } else if (value == SwipeToDismissBoxValue.EndToStart) {
                onArchiveSwipe()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.clip(RoundedCornerShape(24.dp)),
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.errorContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.secondaryContainer
                    else -> Color.Transparent
                },
                label = "swipeBg"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(color, RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp),
                contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Trash", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Archive", color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Outlined.Archive, contentDescription = "Archive", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(cardBg)
                .border(
                    width = if (note.isPinned) 1.5.dp else 0.5.dp,
                    color = if (note.isPinned) colorTheme.accentColor else borderColor,
                    shape = RoundedCornerShape(24.dp)
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        showCardMenu = true
                        onLongClick()
                    }
                )
                .testTag("note_card_${note.id}")
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header row: Folder Tag, Pin, Favorite, More
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Folder pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(primaryText.copy(alpha = 0.10f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = note.folder.ifBlank { "General" },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryText
                            )
                        }

                        // Actions
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (note.reminderEpochMillis != null) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Reminder",
                                    tint = primaryText.copy(alpha = 0.8f),
                                    modifier = Modifier.size(15.dp).padding(end = 2.dp)
                                )
                            }

                            if (note.isLocked) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = primaryText.copy(alpha = 0.8f),
                                    modifier = Modifier.size(15.dp).padding(end = 2.dp)
                                )
                            }

                            IconButton(
                                onClick = onFavoriteClick,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (note.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (note.isFavorite) Color(0xFFE11D48) else primaryText.copy(alpha = 0.4f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            IconButton(
                                onClick = onPinClick,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = "Pin",
                                    tint = if (note.isPinned) primaryText else primaryText.copy(alpha = 0.35f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            Box {
                                IconButton(
                                    onClick = { showCardMenu = true },
                                    modifier = Modifier.size(24.dp).testTag("note_card_more_${note.id}")
                                ) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "Options",
                                        tint = primaryText.copy(alpha = 0.5f),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showCardMenu,
                                    onDismissRequest = { showCardMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(if (note.isPinned) "Unpin" else "Pin") },
                                        leadingIcon = {
                                            Icon(
                                                if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            onPinClick()
                                            showCardMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (note.isFavorite) "Unfavorite" else "Favorite") },
                                        leadingIcon = {
                                            Icon(
                                                if (note.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            onFavoriteClick()
                                            showCardMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Archive") },
                                        leadingIcon = { Icon(Icons.Outlined.Archive, contentDescription = null) },
                                        onClick = {
                                            onArchiveSwipe()
                                            showCardMenu = false
                                        }
                                    )
                                    if (onShareClick != null) {
                                        DropdownMenuItem(
                                            text = { Text("Share Note") },
                                            leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) },
                                            onClick = {
                                                onShareClick()
                                                showCardMenu = false
                                            }
                                        )
                                    }
                                    if (onExportTxtClick != null) {
                                        DropdownMenuItem(
                                            text = { Text("Export as .txt") },
                                            leadingIcon = { Icon(Icons.Outlined.Description, contentDescription = null) },
                                            onClick = {
                                                onExportTxtClick()
                                                showCardMenu = false
                                            }
                                        )
                                    }
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Move to Trash", color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Outlined.Delete,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        onClick = {
                                            onDeleteSwipe()
                                            showCardMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Title
                    if (note.title.isNotBlank()) {
                        Text(
                            text = if (note.isLocked) "🔒 Locked Note" else note.title,
                            fontFamily = font,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = primaryText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Content preview
                    if (note.isLocked) {
                        Text(
                            text = "Tap to unlock private note",
                            fontFamily = font,
                            fontSize = 12.5.sp,
                            color = secondaryText,
                            maxLines = 2
                        )
                    } else {
                        when (note.noteType) {
                            NoteType.CHECKLIST.name -> {
                                val items = remember(note.checklistJson) { note.parseChecklist() }
                                if (items.isNotEmpty()) {
                                    val doneCount = items.count { it.isDone }
                                    val progress = doneCount.toFloat() / items.size.toFloat()

                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape),
                                            color = primaryText,
                                            trackColor = primaryText.copy(alpha = 0.2f)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "$doneCount/${items.size}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryText
                                        )
                                    }

                                    items.take(3).forEach { item ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 1.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (item.isDone) Icons.Default.CheckCircle else Icons.Default.CheckBox,
                                                contentDescription = null,
                                                tint = if (item.isDone) primaryText else primaryText.copy(alpha = 0.4f),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = item.text.ifBlank { "Untitled task" },
                                                fontFamily = font,
                                                fontSize = 12.5.sp,
                                                textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                                color = if (item.isDone) secondaryText.copy(alpha = 0.5f) else primaryText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            NoteType.SKETCH.name -> {
                                val strokes = remember(note.sketchDataJson) { note.parseSketchStrokes() }
                                if (strokes.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(65.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isDark) Color(0xFF141822) else Color(0xFFFCFCFD))
                                            .border(0.5.dp, borderColor, RoundedCornerShape(12.dp))
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxWidth().height(65.dp)) {
                                            strokes.forEach { stroke ->
                                                if (stroke.points.size > 1 && !stroke.isEraser) {
                                                    val path = Path().apply {
                                                        moveTo(stroke.points[0].x * 0.35f, stroke.points[0].y * 0.35f)
                                                        for (i in 1 until stroke.points.size) {
                                                            lineTo(stroke.points[i].x * 0.35f, stroke.points[i].y * 0.35f)
                                                        }
                                                    }
                                                    drawPath(
                                                        path = path,
                                                        color = Color(stroke.colorHex),
                                                        style = Stroke(
                                                            width = (stroke.strokeWidth * 0.35f).coerceAtLeast(1.2f),
                                                            cap = StrokeCap.Round,
                                                            join = StrokeJoin.Round
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                if (note.content.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = note.content,
                                        fontFamily = font,
                                        fontSize = 12.sp,
                                        color = secondaryText,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            NoteType.AUDIO.name -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.size(width = 3.dp, height = 8.dp).clip(CircleShape).background(primaryText))
                                        Box(modifier = Modifier.size(width = 3.dp, height = 14.dp).clip(CircleShape).background(primaryText))
                                        Box(modifier = Modifier.size(width = 3.dp, height = 10.dp).clip(CircleShape).background(primaryText))
                                        Box(modifier = Modifier.size(width = 3.dp, height = 18.dp).clip(CircleShape).background(primaryText))
                                        Box(modifier = Modifier.size(width = 3.dp, height = 11.dp).clip(CircleShape).background(primaryText))
                                        Box(modifier = Modifier.size(width = 3.dp, height = 6.dp).clip(CircleShape).background(primaryText))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${note.audioDurationSeconds}s",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryText
                                    )
                                }
                                if (note.content.isNotBlank()) {
                                    Text(
                                        text = note.content,
                                        fontFamily = font,
                                        fontSize = 12.sp,
                                        color = secondaryText,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            else -> {
                                if (note.content.isNotBlank()) {
                                    Text(
                                        text = note.content,
                                        fontFamily = font,
                                        fontSize = 13.sp,
                                        color = secondaryText,
                                        lineHeight = 17.5.sp,
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Tags
                    val tags = remember(note.tags) { note.getTagList() }
                    if (tags.isNotEmpty() && !note.isLocked) {
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            tags.take(2).forEach { tag ->
                                Text(
                                    text = "#$tag",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = primaryText.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Bottom row: Uppercase small tracking date & Type Icon
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val relativeDateStr = formatRelativeDate(note.updatedAt)
                    Text(
                        text = relativeDateStr,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = primaryText.copy(alpha = 0.5f)
                    )

                    val typeEmoji = when (note.noteType) {
                        NoteType.CHECKLIST.name -> "🛒"
                        NoteType.SKETCH.name -> "🎨"
                        NoteType.AUDIO.name -> "🎙️"
                        else -> if (note.folder.equals("work", ignoreCase = true)) "⚡" else "📝"
                    }
                    Text(
                        text = typeEmoji,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

private fun formatRelativeDate(timestamp: Long): String {
    val now = Calendar.getInstance()
    val noteCal = Calendar.getInstance().apply { timeInMillis = timestamp }

    val diffMillis = now.timeInMillis - timestamp
    val diffHours = diffMillis / (1000 * 60 * 60)

    return when {
        diffHours < 1 -> "JUST NOW"
        diffHours < 24 && now.get(Calendar.DAY_OF_YEAR) == noteCal.get(Calendar.DAY_OF_YEAR) -> {
            "${diffHours}H AGO"
        }
        now.get(Calendar.DAY_OF_YEAR) - noteCal.get(Calendar.DAY_OF_YEAR) == 1 -> "YESTERDAY"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp)).uppercase(Locale.getDefault())
    }
}
