package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MinimalOnPrimaryContainerLight
import com.example.ui.theme.MinimalPrimaryContainerLight

@Composable
fun QuickJotBar(
    onNewTextNote: () -> Unit,
    onNewChecklist: () -> Unit,
    onNewSketch: () -> Unit,
    onNewAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color.Black.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(28.dp),
        color = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else Color(0xFFEFF1F9),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE0E2EC)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Quick Jot input trigger
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onNewTextNote() }
                    .padding(horizontal = 12.dp, vertical = 9.dp)
                    .testTag("quick_jot_input_trigger"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📝", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Quick jot...",
                    color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF44474E),
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Action icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onNewChecklist,
                    modifier = Modifier.size(38.dp).clip(CircleShape).testTag("quick_checklist_button")
                ) {
                    Text("✅", fontSize = 15.sp)
                }

                IconButton(
                    onClick = onNewSketch,
                    modifier = Modifier.size(38.dp).clip(CircleShape).testTag("quick_sketch_button")
                ) {
                    Text("🎨", fontSize = 15.sp)
                }

                IconButton(
                    onClick = onNewAudio,
                    modifier = Modifier.size(38.dp).clip(CircleShape).testTag("quick_voice_button")
                ) {
                    Text("🎙️", fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Primary Add FAB Pill
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isDark) MinimalPrimaryContainerLight else MinimalPrimaryContainerLight)
                        .clickable { onNewTextNote() }
                        .testTag("quick_primary_fab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Light,
                        color = MinimalOnPrimaryContainerLight
                    )
                }
            }
        }
    }
}
