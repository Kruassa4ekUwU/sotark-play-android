package com.sotark.play.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sotark.play.data.model.App
import com.sotark.play.ui.theme.GreenPrimary

// ─── App Card (grid / list) ──────────────────────────────────────────────────
@Composable
fun AppCard(app: App, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(url = app.iconUrl, size = 56)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(app.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(app.developer, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("${app.rating}", style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.width(8.dp))
                    Text(app.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.width(8.dp))
            Text("${app.sizeMb} MB",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─── App Icon ────────────────────────────────────────────────────────────────
@Composable
fun AppIcon(url: String?, size: Int = 48) {
    val shape = RoundedCornerShape((size * 0.22f).dp)
    if (url != null) {
        AsyncImage(
            model  = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    } else {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(shape)
                .background(GreenPrimary.copy(alpha = .2f)),
            contentAlignment = Alignment.Center
        ) {
            Text("?", fontSize = (size * 0.4f).sp, color = GreenPrimary)
        }
    }
}

// ─── Rating bar ──────────────────────────────────────────────────────────────
@Composable
fun RatingStars(rating: Float, modifier: Modifier = Modifier) {
    Row(modifier) {
        repeat(5) { i ->
            Icon(
                imageVector = if (i < rating.toInt()) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ─── Category chip ───────────────────────────────────────────────────────────
@Composable
fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick  = onClick,
        label    = { Text(label, style = MaterialTheme.typography.labelMedium) },
        colors   = FilterChipDefaults.filterChipColors(
            selectedContainerColor = GreenPrimary,
            selectedLabelColor     = Color.White
        )
    )
}

// ─── Section header ──────────────────────────────────────────────────────────
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text  = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

// ─── Install button ──────────────────────────────────────────────────────────
@Composable
fun InstallButton(
    label: String = "Установить",
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        shape    = RoundedCornerShape(8.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
        modifier = Modifier.height(40.dp)
    ) { Text(label, fontWeight = FontWeight.SemiBold) }
}
