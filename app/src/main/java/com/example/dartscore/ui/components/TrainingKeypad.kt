package com.example.dartscore.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dartscore.ui.theme.*

@Composable
fun TrainingKeypad(
    inputText: String,
    placeholder: String = "UPIŠI REZULTAT",
    inputColor: Color = GreenAccent,
    enabled: Boolean = true,
    showBust: Boolean = true,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onBust: (() -> Unit)? = null,
    onUndo: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkCard, RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFF333333), RoundedCornerShape(10.dp))
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (inputText.isEmpty()) placeholder else inputText,
                color = if (inputText.isEmpty()) TextHint else inputColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        val keys = buildList {
            if (showBust) {
                add(listOf("1", "2", "3", "BUST"))
            } else {
                add(listOf("1", "2", "3", "⌫"))
            }
            add(listOf("4", "5", "6", "⌫"))
            add(listOf("7", "8", "9", "0"))
        }

        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { key ->
                    TrainingKeypadButton(
                        label = key,
                        isAccent = key == "BUST",
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when (key) {
                                "BUST" -> onBust?.invoke()
                                "⌫" -> onBackspace()
                                else -> onDigit(key)
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(
                onClick = onUndo,
                enabled = enabled,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF444444))
            ) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("RESET", fontSize = 10.sp)
            }
            Button(
                onClick = onConfirm,
                enabled = enabled,
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
            ) {
                Text("POTVRDI", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text("→", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TrainingKeypadButton(
    label: String,
    isAccent: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .background(
                if (isAccent) Color(0xFF3A1010) else DarkCard,
                RoundedCornerShape(8.dp)
            )
            .border(1.dp, BorderNeutral, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (label == "⌫") {
            Icon(
                Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Obriši",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = label,
                color = if (isAccent) RedAccent else TextPrimary,
                fontSize = if (isAccent) 11.sp else 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SinglesPointsPicker(
    enabled: Boolean,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Bodovi ovog runda (0–9)",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            (0..4).forEach { points ->
                PointsButton(points, enabled, Modifier.weight(1f), onSelect)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            (5..9).forEach { points ->
                PointsButton(points, enabled, Modifier.weight(1f), onSelect)
            }
        }
    }
}

@Composable
private fun PointsButton(
    points: Int,
    enabled: Boolean,
    modifier: Modifier,
    onSelect: (Int) -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .background(
                if (points >= 7) Color(0xFF1A3320) else DarkCard,
                RoundedCornerShape(8.dp)
            )
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onSelect(points) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = points.toString(),
            color = if (points >= 7) GreenAccent else TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
