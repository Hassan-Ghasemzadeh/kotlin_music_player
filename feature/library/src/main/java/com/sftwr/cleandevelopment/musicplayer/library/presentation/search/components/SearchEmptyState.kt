package com.sftwr.cleandevelopment.musicplayer.library.presentation.search.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sftwr.cleandevelopment.musicplayer.home.R

@Composable
fun SearchEmptyState(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Neon Glowing Icon Placeholder
            Box(contentAlignment = Alignment.Center) {
                Image(
                    // فراخوانی تصویر آپلود شده از Drawable
                    painter = painterResource(id = R.drawable.ic_search_empty_state),
                    contentDescription = null, // خود تصویر دارای متن است، نال کافیست
                    modifier = Modifier
                        .size(
                            width = 280.dp, height = 300.dp
                        ) // تنظیم اندازه متناسب
                        .alpha(0.8f) // کمی شفافیت برای همخوانی بیشتر با پس‌زمینه
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "برای شروع، کلمه‌ای را جستجو کنید.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "لیست آهنگ‌ها، خواننده‌ها و آلبوم‌ها اینجا نشان داده خواهد شد.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}