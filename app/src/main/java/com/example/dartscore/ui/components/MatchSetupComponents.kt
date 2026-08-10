package com.example.dartscore.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dartscore.ui.theme.*

@Composable
fun MatchSetupSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = GreenAccent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
fun MatchChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                if (selected) Color(0xFF1A3320) else DarkCardLight,
                RoundedCornerShape(10.dp)
            )
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) GreenAccent else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun <T> MatchRuleChipRow(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            MatchChoiceChip(
                text = label(option),
                selected = selected == option,
                onClick = { onSelect(option) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun MatchNumberStepper(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    step: Int = 1
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            MatchStepperButton(text = "−", enabled = value - step >= range.first) {
                onValueChange((value - step).coerceIn(range))
            }
            Text(
                text = value.toString(),
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            MatchStepperButton(text = "+", enabled = value + step <= range.last) {
                onValueChange((value + step).coerceIn(range))
            }
        }
    }
}

@Composable
fun MatchStepperButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                if (enabled) DarkCardLight else DarkCard,
                RoundedCornerShape(8.dp)
            )
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = if (enabled) GreenAccent else TextHint, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MatchSetupTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BorderSubtle,
            unfocusedBorderColor = BorderSubtle,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedLabelColor = GreenAccent,
            unfocusedLabelColor = TextSecondary,
            cursorColor = GreenAccent
        )
    )
}

@Composable
fun MatchSettingsForm(
    format: com.example.dartscore.model.MatchFormat,
    onFormatChange: (com.example.dartscore.model.MatchFormat) -> Unit,
    unit: com.example.dartscore.model.MatchUnit,
    onUnitChange: (com.example.dartscore.model.MatchUnit) -> Unit,
    count: Int,
    onCountChange: (Int) -> Unit,
    startScore: Int,
    onStartScoreChange: (Int) -> Unit,
    inRule: com.example.dartscore.model.InRule,
    onInRuleChange: (com.example.dartscore.model.InRule) -> Unit,
    outRule: com.example.dartscore.model.OutRule,
    onOutRuleChange: (com.example.dartscore.model.OutRule) -> Unit
) {
    Text("Format", color = TextSecondary, fontSize = 12.sp)
    Spacer(modifier = Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MatchChoiceChip(
            text = "Prvi do",
            selected = format == com.example.dartscore.model.MatchFormat.FIRST_TO,
            onClick = { onFormatChange(com.example.dartscore.model.MatchFormat.FIRST_TO) },
            modifier = Modifier.weight(1f)
        )
        MatchChoiceChip(
            text = "Najbolji od",
            selected = format == com.example.dartscore.model.MatchFormat.BEST_OF,
            onClick = { onFormatChange(com.example.dartscore.model.MatchFormat.BEST_OF) },
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))
    Text("Legovi ili setovi", color = TextSecondary, fontSize = 12.sp)
    Spacer(modifier = Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MatchChoiceChip(
            text = "Legovi",
            selected = unit == com.example.dartscore.model.MatchUnit.LEGS,
            onClick = { onUnitChange(com.example.dartscore.model.MatchUnit.LEGS) },
            modifier = Modifier.weight(1f)
        )
        MatchChoiceChip(
            text = "Setovi",
            selected = unit == com.example.dartscore.model.MatchUnit.SETS,
            onClick = { onUnitChange(com.example.dartscore.model.MatchUnit.SETS) },
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))
    MatchNumberStepper(
        label = if (unit == com.example.dartscore.model.MatchUnit.LEGS) "Broj legova" else "Broj setova",
        value = count,
        onValueChange = onCountChange,
        range = 1..21
    )

    Spacer(modifier = Modifier.height(12.dp))
    MatchNumberStepper(
        label = "Startni rezultat",
        value = startScore,
        onValueChange = onStartScoreChange,
        range = 101..1001,
        step = 100
    )

    Spacer(modifier = Modifier.height(12.dp))
    Text("Ulazak (In)", color = TextSecondary, fontSize = 12.sp)
    Spacer(modifier = Modifier.height(6.dp))
    MatchRuleChipRow(
        options = com.example.dartscore.model.InRule.entries.toList(),
        selected = inRule,
        label = { it.label },
        onSelect = onInRuleChange
    )

    Spacer(modifier = Modifier.height(12.dp))
    Text("Izlazak (Out)", color = TextSecondary, fontSize = 12.sp)
    Spacer(modifier = Modifier.height(6.dp))
    MatchRuleChipRow(
        options = com.example.dartscore.model.OutRule.entries.toList(),
        selected = outRule,
        label = { it.label },
        onSelect = onOutRuleChange
    )
}
