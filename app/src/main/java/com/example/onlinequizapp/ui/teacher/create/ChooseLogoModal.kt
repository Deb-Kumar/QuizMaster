package com.example.onlinequizapp.ui.teacher.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.onlinequizapp.ui.components.SubjectLogoRegistry
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme

@Composable
fun ChooseLogoModal(
    selectedLogoId: String,
    onLogoSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isTeacherInDarkTheme()
    val modalBg = if (isDark) Color(0xFF091030) else Color.White
    val modalBorder = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.5f) else Color(0xFFCBD5E1)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val searchBg = if (isDark) Color(0xFF050B22).copy(alpha = 0.85f) else Color(0xFFF8FAFC)
    val searchBorder = if (isDark) Color(0xFF1B2E6C) else Color(0xFFCBD5E1)

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val allCategories = remember {
        listOf("All") + SubjectLogoRegistry.logos.map { it.category }.distinct()
    }

    val filteredLogos = remember(searchQuery, selectedCategoryFilter) {
        SubjectLogoRegistry.logos.filter { item ->
            val matchesCategory = selectedCategoryFilter == "All" || item.category.equals(selectedCategoryFilter, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true) ||
                    item.id.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 620.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = modalBg),
            border = BorderStroke(1.2.dp, modalBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Choose Subject Logo",
                                color = textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Select an icon for your quiz subject",
                                color = textSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text("Search logos (e.g. Python, Azure, React)...", color = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8), fontSize = 12.5.sp)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = searchBorder,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary,
                        focusedContainerColor = searchBg,
                        unfocusedContainerColor = searchBg
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allCategories) { cat ->
                        val isSelected = cat == selectedCategoryFilter
                        val chipBg = if (isSelected) Brush.horizontalGradient(
                            listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
                        ) else Brush.linearGradient(
                            listOf(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9), if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                        )
                        val chipBorder = if (isSelected) Color.Transparent else (if (isDark) Color(0xFF1E293B) else Color(0xFFCBD5E1))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(chipBg)
                                .border(1.dp, chipBorder, RoundedCornerShape(16.dp))
                                .clickable { selectedCategoryFilter = cat }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else textSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                // Grid of logos
                if (filteredLogos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No logos found matching your search",
                            color = textSecondary,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredLogos) { logo ->
                            val isSelected = logo.id.equals(selectedLogoId, ignoreCase = true)
                            val itemBg = if (isSelected) (if (isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF))
                            else (if (isDark) Color(0xFF0D153A) else Color(0xFFF8FAFC))
                            val itemBorder = if (isSelected) Color(0xFF818CF8)
                            else (if (isDark) Color(0xFF1E2A58) else Color(0xFFE2E8F0))
                            val iconBoxBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.7f) else Color(0xFFE2E8F0)
                            val logoTextColor = if (isSelected) (if (isDark) Color(0xFFE0E7FF) else Color(0xFF4338CA))
                            else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155))

                            Box(
                                modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(itemBg)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = itemBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onLogoSelected(logo.id) }
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(iconBoxBg)
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = logo.drawableRes),
                                            contentDescription = logo.name,
                                            modifier = Modifier.size(30.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }

                                    Text(
                                        text = logo.name,
                                        color = logoTextColor,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF6366F1)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Footer / Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedLogoId.isNotBlank()) {
                        TextButton(
                            onClick = { onLogoSelected("") },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF87171))
                        ) {
                            Text("Clear Logo", fontSize = 12.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Done", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
