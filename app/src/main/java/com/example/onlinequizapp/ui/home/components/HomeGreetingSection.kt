package com.example.onlinequizapp.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.R
import com.example.onlinequizapp.ui.home.TextMuted
import com.example.onlinequizapp.ui.home.TextWhite

@Composable
internal fun HomeGreetingSection(userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hello, $userName 👋",
                style = TextStyle(
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Keep going! You're doing great.",
                style = TextStyle(
                    color = TextMuted,
                    fontSize = 13.sp
                )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Image(
            painter = painterResource(R.drawable.img_student_hero),
            contentDescription = "Student Hero Character",
            modifier = Modifier
                .width(122.dp)
                .height(78.dp)
                .clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop
        )
    }
}
