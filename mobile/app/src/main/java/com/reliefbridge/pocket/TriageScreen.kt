package com.reliefbridge.pocket

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TriageScreen(vm: AppViewModel, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        item {
            IntakeCard(value = vm.inputText, onValueChange = vm::updateInput)
        }

        item {
            QueueHeader(count = vm.actionQueue.size)
        }

        items(vm.actionQueue, key = { it.message.id }) { item ->
            ActionItemCard(
                item = item,
                status = vm.statusOf(item.message.id),
                onClaim = { vm.claim(item.message.id) },
                onResolve = { vm.resolve(item.message.id) },
                onReset = { vm.resetStatus(item.message.id) },
            )
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun IntakeCard(value: String, onValueChange: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50)),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Field message intake",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF666666),
                )
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = onValueChange,
                minLines = 3,
                placeholder = { Text("Paste a messy field message to triage…") },
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Results update in real-time · all processing is on-device",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF999999),
            )
        }
    }
}

@Composable
private fun QueueHeader(count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Action queue",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Box(
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF245B45))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                text = "$count items",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun ActionItemCard(
    item: ActionItem,
    status: ClaimStatus,
    onClaim: () -> Unit,
    onResolve: () -> Unit,
    onReset: () -> Unit,
) {
    val labelColor = labelColor(item.classification.label)
    val borderColor = when (status) {
        ClaimStatus.CLAIMED -> Color(0xFFFF9800)
        ClaimStatus.RESOLVED -> Color(0xFF4CAF50)
        ClaimStatus.OPEN -> Color.Transparent
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(if (status == ClaimStatus.OPEN) 2.dp else 0.dp),
        modifier = if (status != ClaimStatus.OPEN) {
            Modifier.border(1.dp, borderColor, RoundedCornerShape(12.dp))
        } else {
            Modifier
        },
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LabelChip(label = item.classification.label, color = labelColor)
                    CategoryChip(category = item.classification.category)
                }
                UrgencyDots(urgency = item.classification.urgency)
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = item.message.text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
            )

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item.volunteer?.let { MatchChip(icon = "👤", text = it.name) }
                item.resource?.let { MatchChip(icon = "📦", text = it.name) }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = item.recommendedAction,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF555555),
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (status) {
                    ClaimStatus.OPEN -> {
                        Button(
                            onClick = onClaim,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF245B45)),
                            modifier = Modifier.height(36.dp),
                        ) {
                            Text("Claim", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    ClaimStatus.CLAIMED -> {
                        Button(
                            onClick = onResolve,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            modifier = Modifier.height(36.dp),
                        ) {
                            Text("Mark resolved", style = MaterialTheme.typography.labelMedium)
                        }
                        TextButton(onClick = onReset, modifier = Modifier.height(36.dp)) {
                            Text("Unclaim", color = Color(0xFF999999), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    ClaimStatus.RESOLVED -> {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text("✓ Resolved", color = Color(0xFF2E7D32), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                        TextButton(onClick = onReset, modifier = Modifier.height(36.dp)) {
                            Text("Reopen", color = Color(0xFF999999), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LabelChip(label: String, color: Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = label.uppercase(),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun CategoryChip(category: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = category,
            color = Color(0xFF555555),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun MatchChip(icon: String, text: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF0F7F3))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = "$icon $text",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF245B45),
        )
    }
}

@Composable
private fun UrgencyDots(urgency: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index ->
            Box(
                Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (index < urgency) urgencyColor(urgency) else Color(0xFFE0E0E0)),
            )
        }
    }
}

private fun labelColor(label: String): Color = when (label) {
    "need" -> Color(0xFFD32F2F)
    "offer" -> Color(0xFF245B45)
    "update" -> Color(0xFF1565C0)
    else -> Color(0xFF9E9E9E)
}

private fun urgencyColor(urgency: Int): Color = when {
    urgency >= 5 -> Color(0xFFB71C1C)
    urgency >= 4 -> Color(0xFFD96C2C)
    urgency >= 3 -> Color(0xFFFFA000)
    else -> Color(0xFF245B45)
}
