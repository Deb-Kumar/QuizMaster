package com.example.onlinequizapp.ui.teacher.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.onlinequizapp.data.session.CategoryManager
import com.example.onlinequizapp.ui.components.SubjectLogoRegistry
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme

private val BrandPurple = Color(0xFF8B5CF6)
private val BrandIndigo = Color(0xFF6366F1)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiQuizMetadataDialog(
    initialTitle: String,
    initialLogo: String,
    initialCategories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, logoId: String, categories: List<String>) -> Unit
) {
    val isDark = isTeacherInDarkTheme()

    var title by remember { mutableStateOf(initialTitle) }
    var selectedLogo by remember { mutableStateOf(initialLogo) }
    var selectedCategories by remember {
        mutableStateOf(
            initialCategories
                .flatMap { it.split(",") }
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .take(5)
        )
    }

    var showLogoPicker by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var categorySearchQuery by remember { mutableStateOf("") }
    var showAddCustomCategoryDialog by remember { mutableStateOf(false) }
    var customCategoryInput by remember { mutableStateOf("") }
    var customCategoryError by remember { mutableStateOf<String?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    var availableCategories by remember { mutableStateOf(CategoryManager.getAllCategories()) }

    val filteredCategories = remember(availableCategories, categorySearchQuery) {
        if (categorySearchQuery.isBlank()) availableCategories
        else availableCategories.filter { it.contains(categorySearchQuery, ignoreCase = true) }
    }

    val bg = if (isDark) Color(0xFF0F1738) else Color.White
    val borderCol = if (isDark) Color(0xFF1E2D64) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val inputBg = if (isDark) Color(0xFF0A102E) else Color(0xFFF8FAFC)
    val inputBorder = if (isDark) Color(0xFF1E2D64) else Color(0xFFCBD5E1)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(1.dp, borderCol),
            color = bg
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header Row (Matches CreateBatchDialog exactly)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(BrandPurple, BrandIndigo))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Finalize AI Quiz Details",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = "Choose title, subject icon & categories",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = borderCol)
                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Form Body
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Quiz Title Input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF6366F1)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Quiz Title *",
                                color = textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { input ->
                                    if (input.length <= 100) {
                                        title = input
                                        validationError = null
                                    }
                                },
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                                placeholder = {
                                    Text("Enter quiz title (e.g. Java Multithreading Quiz)...", color = textSecondary, fontSize = 13.sp)
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6366F1),
                                    unfocusedBorderColor = inputBorder,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary,
                                    focusedContainerColor = inputBg,
                                    unfocusedContainerColor = inputBg
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "${title.length}/100",
                                color = textSecondary,
                                fontSize = 10.5.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 12.dp, bottom = 8.dp)
                            )
                        }
                    }

                    // 2. Quiz Subject Logo Card
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GridView,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Quiz Subject Logo",
                                    color = textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (selectedLogo.isNotBlank()) {
                                TextButton(
                                    onClick = { selectedLogo = "" },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Clear", color = Color(0xFFEF4444), fontSize = 11.5.sp)
                                }
                            }
                        }

                        val currentLogo = SubjectLogoRegistry.getLogoById(selectedLogo)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(inputBg)
                                .border(
                                    1.dp,
                                    if (currentLogo != null) Color(0xFF8B5CF6) else inputBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { showLogoPicker = true }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            if (currentLogo != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isDark) Color(0xFF1E2850) else Color(0xFFE2E8F0))
                                                .padding(5.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource(id = currentLogo.drawableRes),
                                                contentDescription = currentLogo.name,
                                                modifier = Modifier.size(26.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = currentLogo.name,
                                                color = textPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = currentLogo.category,
                                                color = Color(0xFF818CF8),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Change",
                                            color = Color(0xFF818CF8),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = Color(0xFF818CF8),
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = textSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Text(
                                            text = "Choose a subject / tech logo...",
                                            color = textSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Category Tags & Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF0284C7)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GridView,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Category *",
                                    color = textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = "${selectedCategories.size}/5 selected",
                                color = if (selectedCategories.isNotEmpty()) Color(0xFF38BDF8) else textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Category Dropdown trigger box
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(inputBg)
                                    .border(1.dp, inputBorder, RoundedCornerShape(12.dp))
                                    .clickable { categoryDropdownExpanded = true }
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (selectedCategories.isEmpty()) "Select categories (up to 5)..." else "Add / Edit categories",
                                    color = textSecondary,
                                    fontSize = 12.5.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = {
                                    categoryDropdownExpanded = false
                                    categorySearchQuery = ""
                                },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .background(if (isDark) Color(0xFF0A102E) else Color.White)
                                    .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                            ) {
                                // Search Header in Dropdown
                                Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    OutlinedTextField(
                                        value = categorySearchQuery,
                                        onValueChange = { categorySearchQuery = it },
                                        placeholder = { Text("Search or type custom category...", fontSize = 11.5.sp) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF6366F1),
                                            unfocusedBorderColor = inputBorder,
                                            focusedTextColor = textPrimary,
                                            unfocusedTextColor = textPrimary
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }

                                filteredCategories.take(15).forEach { cat ->
                                    val isSelected = selectedCategories.any { it.equals(cat, ignoreCase = true) }
                                    val canSelect = isSelected || selectedCategories.size < 5

                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = cat,
                                                    color = if (isSelected) Color(0xFF818CF8) else if (canSelect) textPrimary else textSecondary,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color(0xFF818CF8),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            if (isSelected) {
                                                selectedCategories = selectedCategories.filter { !it.equals(cat, ignoreCase = true) }
                                            } else if (selectedCategories.size < 5) {
                                                selectedCategories = (selectedCategories + cat).distinct().take(5)
                                                validationError = null
                                            }
                                        }
                                    )
                                }

                                // Create custom category action
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AddCircleOutline,
                                                contentDescription = null,
                                                tint = Color(0xFF38BDF8),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "+ Create Custom Category",
                                                color = Color(0xFF38BDF8),
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    },
                                    onClick = {
                                        categoryDropdownExpanded = false
                                        customCategoryInput = categorySearchQuery.trim()
                                        categorySearchQuery = ""
                                        customCategoryError = null
                                        showAddCustomCategoryDialog = true
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF4F46E5))
                                                .padding(vertical = 5.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Done (${selectedCategories.size}/5)",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    },
                                    onClick = {
                                        categoryDropdownExpanded = false
                                        categorySearchQuery = ""
                                    }
                                )
                            }
                        }

                        // Selected Category Tags FlowRow
                        if (selectedCategories.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                selectedCategories.forEach { cat ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isDark) Color(0xFF1E2850) else Color(0xFFEEF2FF))
                                            .border(1.dp, if (isDark) Color(0xFF4F46E5) else Color(0xFFC7D2FE), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 9.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = cat,
                                                color = if (isDark) Color.White else Color(0xFF3730A3),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove $cat",
                                                tint = if (isDark) Color(0xFFA5B4FC) else Color(0xFF6366F1),
                                                modifier = Modifier
                                                    .size(13.dp)
                                                    .clickable {
                                                        selectedCategories = selectedCategories.filter { it != cat }
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Validation Error Text
                    if (validationError != null) {
                        Text(
                            text = validationError!!,
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Action Buttons (46.dp height, 12.dp corner matching CreateBatchDialog)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isDark) Color.Transparent else Color.White,
                            contentColor = textSecondary
                        ),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1))
                    ) {
                        Text("Cancel", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                    }

                    val canProceed = title.trim().isNotBlank() && selectedCategories.isNotEmpty()
                    Button(
                        onClick = {
                            if (title.trim().isBlank()) {
                                validationError = "Please enter a quiz title."
                                return@Button
                            }
                            if (selectedCategories.isEmpty()) {
                                validationError = "Please select at least 1 category."
                                return@Button
                            }
                            onConfirm(title.trim(), selectedLogo, selectedCategories)
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1)
                        ),
                        enabled = canProceed
                    ) {
                        Text(
                            text = "Next: Choose Audience",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    // Logo Picker Dialog
    if (showLogoPicker) {
        ChooseLogoModal(
            selectedLogoId = selectedLogo,
            onLogoSelected = { logoId ->
                selectedLogo = logoId
                showLogoPicker = false
            },
            onDismiss = { showLogoPicker = false }
        )
    }

    // Custom Category Dialog
    if (showAddCustomCategoryDialog) {
        Dialog(onDismissRequest = { showAddCustomCategoryDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, borderCol),
                color = bg,
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Create Custom Category",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    OutlinedTextField(
                        value = customCategoryInput,
                        onValueChange = {
                            customCategoryInput = it
                            customCategoryError = null
                        },
                        placeholder = { Text("Enter category name...", fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            unfocusedBorderColor = inputBorder,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    if (customCategoryError != null) {
                        Text(customCategoryError!!, color = Color(0xFFEF4444), fontSize = 11.5.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddCustomCategoryDialog = false },
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel", fontSize = 12.5.sp)
                        }
                        Button(
                            onClick = {
                                val trimmed = customCategoryInput.trim()
                                if (trimmed.isBlank()) {
                                    customCategoryError = "Category cannot be empty."
                                } else {
                                    CategoryManager.addCustomCategory(trimmed)
                                    availableCategories = CategoryManager.getAllCategories()
                                    if (selectedCategories.size < 5 && !selectedCategories.any { it.equals(trimmed, ignoreCase = true) }) {
                                        selectedCategories = selectedCategories + trimmed
                                        validationError = null
                                    }
                                    showAddCustomCategoryDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                        ) {
                            Text("Add", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
