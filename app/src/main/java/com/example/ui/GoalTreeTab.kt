package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Category
import com.example.data.Routine
import com.example.ui.theme.*

data class NodeBounds(
    val topCenter: Offset,
    val bottomCenter: Offset
)

@Composable
fun GoalTreeTab(
    grandGoal: String,
    categories: List<Category>,
    routines: List<Routine>,
    onAddCategoryClick: () -> Unit,
    onAddRoutineClick: (String) -> Unit,
    onUpdateCategory: (String, String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onUpdateRoutine: (String, String, Int) -> Unit,
    onDeleteRoutine: (String) -> Unit
) {
    var viewMode by remember { mutableStateOf(0) } // 0 = List View, 1 = Tree Canvas View
    
    val isDark = isSystemInDarkTheme()
    val primaryLabelColor = if (isDark) MonochromeWhite else MonochromeBlack

    // State for managing Edit/Delete dialogs
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var editingRoutine by remember { mutableStateOf<Routine?>(null) }

    val routinesByCategory = remember(routines) {
        routines.groupBy { it.categoryId }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (categories.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DesignTokens.PaddingExtraLarge),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NO LIFE SECTORS DETECTED",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Zinc500,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
                Text(
                    text = "Partition your 25-year goal into actionable categories (sectors) to begin tracking your routines.",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = Zinc500,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(DesignTokens.PaddingLarge))
                Button(
                    onClick = onAddCategoryClick,
                    shape = RoundedCornerShape(DesignTokens.PaddingZero),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("add_category_button_empty")
                ) {
                    Text("INITIALIZE SECTOR", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Brutalist Switcher at the top
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignTokens.PaddingLarge, vertical = DesignTokens.PaddingSmall)
                        .border(DesignTokens.StrokeMedium, primaryLabelColor, RoundedCornerShape(DesignTokens.PaddingZero))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (viewMode == 0) primaryLabelColor else Color.Transparent)
                            .clickable { viewMode = 0 }
                            .padding(vertical = 8.dp)
                            .testTag("toggle_list_view"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "STANDARD LIST",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (viewMode == 0) (if (isDark) MonochromeBlack else MonochromeWhite) else primaryLabelColor
                            )
                        )
                    }

                    // Divider Line
                    Box(
                        modifier = Modifier
                            .width(DesignTokens.StrokeMedium)
                            .height(34.dp)
                            .background(primaryLabelColor)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (viewMode == 1) primaryLabelColor else Color.Transparent)
                            .clickable { viewMode = 1 }
                            .padding(vertical = 8.dp)
                            .testTag("toggle_canvas_view"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "INTERACTIVE TREE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (viewMode == 1) (if (isDark) MonochromeBlack else MonochromeWhite) else primaryLabelColor
                            )
                        )
                    }
                }

                if (viewMode == 0) {
                    // View 1: List View
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignTokens.PaddingLarge, vertical = DesignTokens.PaddingSmall),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACTIVE CATEGORIES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Zinc500
                            )
                            
                            TextButton(
                                onClick = onAddCategoryClick,
                                modifier = Modifier.testTag("add_category_header_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Category",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(DesignTokens.PaddingTiny))
                                Text("ADD CATEGORY", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = DesignTokens.PaddingLarge),
                            verticalArrangement = Arrangement.spacedBy(DesignTokens.PaddingMedium),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(categories, key = { it.id }) { category ->
                                val categoryRoutines = remember(routinesByCategory, category.id) {
                                    routinesByCategory[category.id] ?: emptyList()
                                }
                                CategoryCard(
                                    category = category,
                                    routines = categoryRoutines,
                                    onAddRoutineClick = { onAddRoutineClick(category.id) },
                                    onCategoryClick = { editingCategory = it },
                                    onRoutineClick = { editingRoutine = it }
                                )
                            }
                        }
                    }
                } else {
                    // View 2: Tree Canvas View with Zoom & Pan and Vector-Pure Scaling
                    var scale by remember { mutableStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }

                    val colWidth = 200.dp
                    val spacing = 32.dp

                    val contentWidth = if (categories.isNotEmpty()) {
                        (colWidth * categories.size) + (spacing * (categories.size - 1))
                    } else {
                        240.dp
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RectangleShape)
                            .background(MaterialTheme.colorScheme.background)
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(0.5f, 2.0f)
                                    offset += pan
                                }
                            }
                    ) {
                        var parentCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
                        val nodePositions = remember { mutableStateMapOf<String, NodeBounds>() }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    translationX = offset.x
                                    translationY = offset.y
                                    scaleX = scale
                                    scaleY = scale
                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
                                }
                                .onGloballyPositioned { parentCoordinates = it }
                        ) {
                            // Dynamic background Canvas drawing perfect center-to-center connecting lines
                            Canvas(
                                modifier = Modifier.matchParentSize()
                            ) {
                                val lineColor = primaryLabelColor
                                val strokeWidthPx = 2.dp.toPx()

                                fun drawConnection(parentKey: String, childKey: String) {
                                    val parentBounds = nodePositions[parentKey]
                                    val childBounds = nodePositions[childKey]
                                    if (parentBounds != null && childBounds != null) {
                                        drawLine(
                                            color = lineColor,
                                            start = parentBounds.bottomCenter,
                                            end = childBounds.topCenter,
                                            strokeWidth = strokeWidthPx
                                        )
                                    }
                                }

                                if (categories.isEmpty()) {
                                    drawConnection("root", "add_category_initial")
                                } else {
                                    categories.forEach { category ->
                                        drawConnection("root", "category_${category.id}")

                                        val categoryRoutines = routinesByCategory[category.id] ?: emptyList()
                                        if (categoryRoutines.isEmpty()) {
                                            drawConnection("category_${category.id}", "add_routine_initial_${category.id}")
                                        } else {
                                            drawConnection("category_${category.id}", "routine_${categoryRoutines.first().id}")
                                            for (rIndex in 0 until categoryRoutines.size - 1) {
                                                val curr = categoryRoutines[rIndex]
                                                val next = categoryRoutines[rIndex + 1]
                                                drawConnection("routine_${curr.id}", "routine_${next.id}")
                                            }
                                            drawConnection("routine_${categoryRoutines.last().id}", "add_routine_trailing_${category.id}")
                                        }
                                    }
                                }
                            }

                            // Interactive content layout placed over the background canvas
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(
                                        top = 180.dp,
                                        bottom = 300.dp,
                                        start = 180.dp,
                                        end = 180.dp
                                    )
                                    .requiredWidth(contentWidth),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Symmetrical Grand Goal row: Goal Card + spacer + "+" Category Button (if categories exist)
                                val rootBorderStroke = DesignTokens.StrokeThick
                                val rootPadding = DesignTokens.PaddingMedium
                                val rootWidth = 240.dp
                                val rootHeaderFontSize = 10.sp
                                val rootTextFontSize = 14.sp
                                val rootTextSpacer = 4.dp

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = if (categories.isNotEmpty()) Modifier.padding(start = 44.dp) else Modifier // Center-aligned when no plus button
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .border(rootBorderStroke, primaryLabelColor, RoundedCornerShape(DesignTokens.PaddingZero))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(rootPadding)
                                            .width(rootWidth)
                                            .onGloballyPositioned { coordinates ->
                                                parentCoordinates?.let { parent ->
                                                    if (coordinates.isAttached && parent.isAttached) {
                                                        val localPos = parent.localPositionOf(coordinates, Offset.Zero)
                                                        val size = coordinates.size
                                                        val topCenter = Offset(localPos.x + size.width / 2f, localPos.y)
                                                        val bottomCenter = Offset(localPos.x + size.width / 2f, localPos.y + size.height)
                                                        val bounds = NodeBounds(topCenter, bottomCenter)
                                                        val existing = nodePositions["root"]
                                                        if (existing == null ||
                                                            Math.abs(existing.topCenter.x - bounds.topCenter.x) > 0.5f ||
                                                            Math.abs(existing.topCenter.y - bounds.topCenter.y) > 0.5f ||
                                                            Math.abs(existing.bottomCenter.x - bounds.bottomCenter.x) > 0.5f ||
                                                            Math.abs(existing.bottomCenter.y - bounds.bottomCenter.y) > 0.5f
                                                        ) {
                                                            nodePositions["root"] = bounds
                                                        }
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "GRAND GOAL",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Zinc500,
                                                    fontSize = rootHeaderFontSize
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(rootTextSpacer))
                                            Text(
                                                text = grandGoal.uppercase(),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    color = primaryLabelColor,
                                                    textAlign = TextAlign.Center,
                                                    fontSize = rootTextFontSize
                                                )
                                            )
                                        }
                                    }

                                    if (categories.isNotEmpty()) {
                                        val plusButtonSpacer = 12.dp
                                        val plusButtonSize = 32.dp
                                        val plusButtonStroke = DesignTokens.StrokeMedium
                                        val plusButtonFontSize = 18.sp

                                        Spacer(modifier = Modifier.width(plusButtonSpacer))

                                        // High-contrast "+" button symmetrically to the right of the Grand Goal Card
                                        Box(
                                            modifier = Modifier
                                                .size(plusButtonSize)
                                                .zIndex(3f)
                                                .minimumInteractiveComponentSize()
                                                .border(plusButtonStroke, primaryLabelColor, RoundedCornerShape(DesignTokens.PaddingZero))
                                                .background(primaryLabelColor)
                                                .clickable { onAddCategoryClick() }
                                                .testTag("canvas_add_category_button"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "+",
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isDark) MonochromeBlack else MonochromeWhite,
                                                    fontSize = plusButtonFontSize
                                                )
                                            )
                                        }
                                    }
                                }

                                if (categories.isEmpty()) {
                                    val emptyAddCategorySpacer = 32.dp
                                    val emptyAddCategoryStroke = DesignTokens.StrokeMedium
                                    val emptyAddCategoryPaddingH = 24.dp
                                    val emptyAddCategoryPaddingV = 12.dp
                                    val emptyAddCategoryFontSize = 14.sp

                                    Spacer(modifier = Modifier.height(emptyAddCategorySpacer))

                                    // Prominent, high-contrast text button labeled "[ADD CATEGORY]"
                                    Box(
                                        modifier = Modifier
                                            .zIndex(3f)
                                            .border(emptyAddCategoryStroke, primaryLabelColor, RoundedCornerShape(DesignTokens.PaddingZero))
                                            .background(primaryLabelColor)
                                            .clickable { onAddCategoryClick() }
                                            .padding(horizontal = emptyAddCategoryPaddingH, vertical = emptyAddCategoryPaddingV)
                                            .onGloballyPositioned { coordinates ->
                                                parentCoordinates?.let { parent ->
                                                    if (coordinates.isAttached && parent.isAttached) {
                                                        val localPos = parent.localPositionOf(coordinates, Offset.Zero)
                                                        val size = coordinates.size
                                                        val topCenter = Offset(localPos.x + size.width / 2f, localPos.y)
                                                        val bottomCenter = Offset(localPos.x + size.width / 2f, localPos.y + size.height)
                                                        val bounds = NodeBounds(topCenter, bottomCenter)
                                                        val existing = nodePositions["add_category_initial"]
                                                        if (existing == null ||
                                                            Math.abs(existing.topCenter.x - bounds.topCenter.x) > 0.5f ||
                                                            Math.abs(existing.topCenter.y - bounds.topCenter.y) > 0.5f ||
                                                            Math.abs(existing.bottomCenter.x - bounds.bottomCenter.x) > 0.5f ||
                                                            Math.abs(existing.bottomCenter.y - bounds.bottomCenter.y) > 0.5f
                                                        ) {
                                                            nodePositions["add_category_initial"] = bounds
                                                        }
                                                    }
                                                }
                                            }
                                            .testTag("canvas_add_category_initial"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "[ADD CATEGORY]",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Black,
                                                color = if (isDark) MonochromeBlack else MonochromeWhite,
                                                fontSize = emptyAddCategoryFontSize
                                            )
                                        )
                                    }
                                }

                                val categoryRowSpacer = 64.dp
                                Spacer(modifier = Modifier.height(categoryRowSpacer))

                                if (categories.isNotEmpty()) {
                                    // Row of category branches
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(spacing),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        categories.forEach { category ->
                                            val categoryRoutines = remember(routinesByCategory, category.id) {
                                                routinesByCategory[category.id] ?: emptyList()
                                            }

                                            Column(
                                                modifier = Modifier.width(colWidth),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                val categoryBorderStroke = DesignTokens.StrokeMedium
                                                val categoryPadding = DesignTokens.PaddingMedium
                                                val categoryHeight = 60.dp
                                                val categorySpacerHeight = 2.dp
                                                val categoryTitleFontSize = 9.sp
                                                val categoryNameFontSize = 14.sp

                                                // Category Card Node
                                                Box(
                                                    modifier = Modifier
                                                        .width(colWidth)
                                                        .heightIn(min = categoryHeight)
                                                        .border(categoryBorderStroke, primaryLabelColor, RoundedCornerShape(DesignTokens.PaddingZero))
                                                        .background(MaterialTheme.colorScheme.surface)
                                                        .clickable { editingCategory = category }
                                                        .padding(categoryPadding)
                                                        .onGloballyPositioned { coordinates ->
                                                            parentCoordinates?.let { parent ->
                                                                if (coordinates.isAttached && parent.isAttached) {
                                                                    val localPos = parent.localPositionOf(coordinates, Offset.Zero)
                                                                    val size = coordinates.size
                                                                    val topCenter = Offset(localPos.x + size.width / 2f, localPos.y)
                                                                    val bottomCenter = Offset(localPos.x + size.width / 2f, localPos.y + size.height)
                                                                    val bounds = NodeBounds(topCenter, bottomCenter)
                                                                    val key = "category_${category.id}"
                                                                    val existing = nodePositions[key]
                                                                    if (existing == null ||
                                                                        Math.abs(existing.topCenter.x - bounds.topCenter.x) > 0.5f ||
                                                                        Math.abs(existing.topCenter.y - bounds.topCenter.y) > 0.5f ||
                                                                        Math.abs(existing.bottomCenter.x - bounds.bottomCenter.x) > 0.5f ||
                                                                        Math.abs(existing.bottomCenter.y - bounds.bottomCenter.y) > 0.5f
                                                                    ) {
                                                                        nodePositions[key] = bounds
                                                                    }
                                                                }
                                                            }
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                            text = "SECTOR",
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                fontFamily = FontFamily.Monospace,
                                                                fontSize = categoryTitleFontSize,
                                                                color = Zinc500
                                                            )
                                                        )
                                                        Spacer(modifier = Modifier.height(categorySpacerHeight))
                                                        Text(
                                                            text = category.name.uppercase(),
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                fontFamily = FontFamily.Monospace,
                                                                fontWeight = FontWeight.Bold,
                                                                color = primaryLabelColor,
                                                                textAlign = TextAlign.Center,
                                                                fontSize = categoryNameFontSize
                                                            ),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }

                                                val categoryToRoutineSpacer = 32.dp
                                                Spacer(modifier = Modifier.height(categoryToRoutineSpacer))

                                                // Leaf Nodes or Dynamic Add buttons
                                                if (categoryRoutines.isEmpty()) {
                                                    val addRoutineStroke = DesignTokens.StrokeMedium
                                                    val addRoutinePaddingH = 16.dp
                                                    val addRoutinePaddingV = 8.dp
                                                    val addRoutineFontSize = 11.sp

                                                    // Category Level Contextual Actions: if NO routines exist
                                                    Box(
                                                        modifier = Modifier
                                                            .zIndex(3f)
                                                            .minimumInteractiveComponentSize()
                                                            .border(addRoutineStroke, primaryLabelColor, RoundedCornerShape(DesignTokens.PaddingZero))
                                                            .background(primaryLabelColor)
                                                            .clickable { onAddRoutineClick(category.id) }
                                                            .padding(horizontal = addRoutinePaddingH, vertical = addRoutinePaddingV)
                                                            .onGloballyPositioned { coordinates ->
                                                                parentCoordinates?.let { parent ->
                                                                    if (coordinates.isAttached && parent.isAttached) {
                                                                        val localPos = parent.localPositionOf(coordinates, Offset.Zero)
                                                                        val size = coordinates.size
                                                                        val topCenter = Offset(localPos.x + size.width / 2f, localPos.y)
                                                                        val bottomCenter = Offset(localPos.x + size.width / 2f, localPos.y + size.height)
                                                                        val bounds = NodeBounds(topCenter, bottomCenter)
                                                                        val key = "add_routine_initial_${category.id}"
                                                                        val existing = nodePositions[key]
                                                                        if (existing == null ||
                                                                            Math.abs(existing.topCenter.x - bounds.topCenter.x) > 0.5f ||
                                                                            Math.abs(existing.topCenter.y - bounds.topCenter.y) > 0.5f ||
                                                                            Math.abs(existing.bottomCenter.x - bounds.bottomCenter.x) > 0.5f ||
                                                                            Math.abs(existing.bottomCenter.y - bounds.bottomCenter.y) > 0.5f
                                                                        ) {
                                                                            nodePositions[key] = bounds
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            .testTag("canvas_add_routine_initial_${category.id}"),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "[ADD ROUTINE]",
                                                            style = MaterialTheme.typography.labelMedium.copy(
                                                                fontFamily = FontFamily.Monospace,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isDark) MonochromeBlack else MonochromeWhite,
                                                                fontSize = addRoutineFontSize
                                                            )
                                                        )
                                                    }
                                                } else {
                                                    // If one or more routines exist
                                                    val spacingBetweenRoutines = 24.dp
                                                    Column(
                                                        modifier = Modifier.width(colWidth),
                                                        verticalArrangement = Arrangement.spacedBy(spacingBetweenRoutines),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        categoryRoutines.forEachIndexed { rIndex, routine ->
                                                            val progress = if (routine.targetCount > 0) {
                                                                (routine.completedCount.toFloat() / routine.targetCount.toFloat()).coerceIn(0f, 1f)
                                                            } else {
                                                                0f
                                                            }

                                                            val routineStroke = DesignTokens.StrokeThin
                                                            val routinePadding = DesignTokens.PaddingSmall
                                                            val routineMinHeight = 50.dp
                                                            val routineTitleFontSize = 11.sp
                                                            val routineTextFontSize = 9.sp
                                                            val routineTextSpacer = 4.dp

                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .heightIn(min = routineMinHeight)
                                                                    .border(routineStroke, primaryLabelColor.copy(alpha = 0.5f), RoundedCornerShape(DesignTokens.PaddingZero))
                                                                    .background(MaterialTheme.colorScheme.surface)
                                                                    .clickable { editingRoutine = routine }
                                                                    .padding(routinePadding)
                                                                    .onGloballyPositioned { coordinates ->
                                                                        parentCoordinates?.let { parent ->
                                                                            if (coordinates.isAttached && parent.isAttached) {
                                                                                val localPos = parent.localPositionOf(coordinates, Offset.Zero)
                                                                                val size = coordinates.size
                                                                                val topCenter = Offset(localPos.x + size.width / 2f, localPos.y)
                                                                                val bottomCenter = Offset(localPos.x + size.width / 2f, localPos.y + size.height)
                                                                                val bounds = NodeBounds(topCenter, bottomCenter)
                                                                                val key = "routine_${routine.id}"
                                                                                val existing = nodePositions[key]
                                                                                if (existing == null ||
                                                                                    Math.abs(existing.topCenter.x - bounds.topCenter.x) > 0.5f ||
                                                                                    Math.abs(existing.topCenter.y - bounds.topCenter.y) > 0.5f ||
                                                                                    Math.abs(existing.bottomCenter.x - bounds.bottomCenter.x) > 0.5f ||
                                                                                    Math.abs(existing.bottomCenter.y - bounds.bottomCenter.y) > 0.5f
                                                                                ) {
                                                                                    nodePositions[key] = bounds
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                             ) {
                                                                Column {
                                                                    Text(
                                                                        text = routine.title.uppercase(),
                                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                                            fontFamily = FontFamily.Monospace,
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = primaryLabelColor,
                                                                            fontSize = routineTitleFontSize
                                                                        ),
                                                                        maxLines = 2,
                                                                        overflow = TextOverflow.Ellipsis
                                                                    )
                                                                    Spacer(modifier = Modifier.height(routineTextSpacer))
                                                                    Row(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        Text(
                                                                            text = "${routine.completedCount}/${routine.targetCount} MO",
                                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                                fontFamily = FontFamily.Monospace,
                                                                                fontSize = routineTextFontSize,
                                                                                fontWeight = FontWeight.Bold
                                                                            ),
                                                                            color = if (progress >= 1f) GridLevel4 else primaryLabelColor
                                                                        )
                                                                    }
                                                                    Spacer(modifier = Modifier.height(routineTextSpacer))
                                                                    // Mini progress indicator
                                                                    val progressHeight = 4.dp
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .height(progressHeight)
                                                                            .background(primaryLabelColor.copy(alpha = 0.1f))
                                                                    ) {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .fillMaxHeight()
                                                                                .fillMaxWidth(progress)
                                                                                .background(if (progress >= 1f) GridLevel4 else primaryLabelColor)
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        // Routine Level Continuous Actions: Trailing add button
                                                        val trailingAddSize = 28.dp
                                                        val trailingAddStroke = DesignTokens.StrokeThin
                                                        val trailingAddFontSize = 14.sp

                                                        Box(
                                                            modifier = Modifier
                                                                .size(trailingAddSize)
                                                                .zIndex(3f)
                                                                .minimumInteractiveComponentSize()
                                                                .border(trailingAddStroke, primaryLabelColor, RoundedCornerShape(DesignTokens.PaddingZero))
                                                                .background(primaryLabelColor)
                                                                .clickable { onAddRoutineClick(category.id) }
                                                                .onGloballyPositioned { coordinates ->
                                                                    parentCoordinates?.let { parent ->
                                                                        if (coordinates.isAttached && parent.isAttached) {
                                                                            val localPos = parent.localPositionOf(coordinates, Offset.Zero)
                                                                            val size = coordinates.size
                                                                            val topCenter = Offset(localPos.x + size.width / 2f, localPos.y)
                                                                            val bottomCenter = Offset(localPos.x + size.width / 2f, localPos.y + size.height)
                                                                            val bounds = NodeBounds(topCenter, bottomCenter)
                                                                            val key = "add_routine_trailing_${category.id}"
                                                                            val existing = nodePositions[key]
                                                                            if (existing == null ||
                                                                                Math.abs(existing.topCenter.x - bounds.topCenter.x) > 0.5f ||
                                                                                Math.abs(existing.topCenter.y - bounds.topCenter.y) > 0.5f ||
                                                                                Math.abs(existing.bottomCenter.x - bounds.bottomCenter.x) > 0.5f ||
                                                                                Math.abs(existing.bottomCenter.y - bounds.bottomCenter.y) > 0.5f
                                                                            ) {
                                                                                nodePositions[key] = bounds
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                .testTag("canvas_add_routine_trailing_${category.id}"),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = "+",
                                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                                    fontFamily = FontFamily.Monospace,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (isDark) MonochromeBlack else MonochromeWhite,
                                                                    fontSize = trailingAddFontSize
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Brutalist Styled Popups for Item Management (Edit & Delete Operations) ---

    // Edit/Delete Category Dialog
    editingCategory?.let { category ->
        var catName by remember(category) { mutableStateOf(category.name) }
        var showConfirmDelete by remember { mutableStateOf(false) }

        if (!showConfirmDelete) {
            AlertDialog(
                onDismissRequest = { editingCategory = null },
                shape = RoundedCornerShape(DesignTokens.PaddingZero),
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier
                    .padding(24.dp)
                    .border(DesignTokens.StrokeThick, primaryLabelColor, RoundedCornerShape(DesignTokens.PaddingZero)),
                title = {
                    Text(
                        text = "CONFIGURE SECTOR",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = primaryLabelColor
                        )
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Modify the Sector identification moniker or decommission it entirely from the framework profile.",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = Zinc500
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))
                        
                        OutlinedTextField(
                            value = catName,
                            onValueChange = { catName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_category_name_input"),
                            shape = RoundedCornerShape(DesignTokens.PaddingZero),
                            placeholder = { Text("e.g., SOFT SKILLS", fontFamily = FontFamily.Monospace) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GridLevel4,
                                unfocusedBorderColor = primaryLabelColor.copy(alpha = 0.3f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(DesignTokens.PaddingLarge))

                        // Warning Decommission button
                        Button(
                            onClick = { showConfirmDelete = true },
                            shape = RoundedCornerShape(DesignTokens.PaddingZero),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Red
                            ),
                            border = androidx.compose.foundation.BorderStroke(DesignTokens.StrokeMedium, Color.Red),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("delete_category_trigger")
                        ) {
                            Text(
                                "DELETE SECTOR & NESTED ROUTINES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (catName.isNotBlank()) {
                                onUpdateCategory(category.id, catName.trim())
                                editingCategory = null
                            }
                        },
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryLabelColor,
                            contentColor = if (isDark) MonochromeBlack else MonochromeWhite
                        ),
                        modifier = Modifier.testTag("save_category_changes")
                    ) {
                        Text("SAVE CHANGES", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { editingCategory = null }
                    ) {
                        Text("ABORT", fontFamily = FontFamily.Monospace, color = Zinc500)
                    }
                }
            )
        } else {
            // Symmetrical confirmation of destruction
            AlertDialog(
                onDismissRequest = { showConfirmDelete = false },
                shape = RoundedCornerShape(DesignTokens.PaddingZero),
                modifier = Modifier.border(DesignTokens.StrokeThick, Color.Red, RoundedCornerShape(DesignTokens.PaddingZero)),
                title = {
                    Text(
                        text = "DECOMMISSION WARNING",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    )
                },
                text = {
                    Text(
                        text = "Are you absolutely sure you want to delete Sector '${category.name.uppercase()}'? This action is irreversible and will permanently delete all associated monthly routines.",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = primaryLabelColor
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteCategory(category.id)
                            showConfirmDelete = false
                            editingCategory = null
                        },
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = MonochromeWhite
                        ),
                        modifier = Modifier.testTag("confirm_delete_category")
                    ) {
                        Text("CONFIRM DELETION", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showConfirmDelete = false }
                    ) {
                        Text("ABORT", fontFamily = FontFamily.Monospace, color = Zinc500)
                    }
                }
            )
        }
    }

    // Edit/Delete Routine Dialog
    editingRoutine?.let { routine ->
        var routineTitle by remember(routine) { mutableStateOf(routine.title) }
        var targetText by remember(routine) { mutableStateOf(routine.targetCount.toString()) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var showConfirmDelete by remember { mutableStateOf(false) }

        if (!showConfirmDelete) {
            AlertDialog(
                onDismissRequest = { editingRoutine = null },
                shape = RoundedCornerShape(DesignTokens.PaddingZero),
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier
                    .padding(24.dp)
                    .border(DesignTokens.StrokeThick, primaryLabelColor, RoundedCornerShape(DesignTokens.PaddingZero)),
                title = {
                    Text(
                        text = "CONFIGURE ROUTINE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = primaryLabelColor
                        )
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Modify core routine description details and target monthly threshold directly.",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = Zinc500
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))

                        OutlinedTextField(
                            value = routineTitle,
                            onValueChange = { routineTitle = it },
                            label = { Text("Routine Title", fontFamily = FontFamily.Monospace) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_routine_title_input"),
                            shape = RoundedCornerShape(DesignTokens.PaddingZero),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GridLevel4,
                                unfocusedBorderColor = primaryLabelColor.copy(alpha = 0.3f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))

                        OutlinedTextField(
                            value = targetText,
                            onValueChange = { targetText = it },
                            label = { Text("Target Monthly Count", fontFamily = FontFamily.Monospace) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_routine_target_input"),
                            shape = RoundedCornerShape(DesignTokens.PaddingZero),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GridLevel4,
                                unfocusedBorderColor = primaryLabelColor.copy(alpha = 0.3f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        errorMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
                            Text(text = msg, color = Color.Red, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
                        }

                        Spacer(modifier = Modifier.height(DesignTokens.PaddingLarge))

                        // Delete button
                        Button(
                            onClick = { showConfirmDelete = true },
                            shape = RoundedCornerShape(DesignTokens.PaddingZero),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Red
                            ),
                            border = androidx.compose.foundation.BorderStroke(DesignTokens.StrokeMedium, Color.Red),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("delete_routine_trigger")
                        ) {
                            Text(
                                "DELETE ROUTINE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val targetVal = targetText.toIntOrNull()
                            if (routineTitle.isBlank()) {
                                errorMessage = "Title cannot be blank"
                            } else if (targetVal == null || targetVal <= 0) {
                                errorMessage = "Enter valid positive count"
                            } else {
                                onUpdateRoutine(routine.id, routineTitle.trim(), targetVal)
                                editingRoutine = null
                            }
                        },
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryLabelColor,
                            contentColor = if (isDark) MonochromeBlack else MonochromeWhite
                        ),
                        modifier = Modifier.testTag("save_routine_changes")
                    ) {
                        Text("SAVE CHANGES", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { editingRoutine = null }
                    ) {
                        Text("ABORT", fontFamily = FontFamily.Monospace, color = Zinc500)
                    }
                }
            )
        } else {
            // Confirm delete routine
            AlertDialog(
                onDismissRequest = { showConfirmDelete = false },
                shape = RoundedCornerShape(DesignTokens.PaddingZero),
                modifier = Modifier.border(DesignTokens.StrokeThick, Color.Red, RoundedCornerShape(DesignTokens.PaddingZero)),
                title = {
                    Text(
                        text = "DELETION WARNING",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    )
                },
                text = {
                    Text(
                        text = "Are you absolutely sure you want to delete Routine '${routine.title.uppercase()}'? This action is irreversible.",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = primaryLabelColor
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteRoutine(routine.id)
                            showConfirmDelete = false
                            editingRoutine = null
                        },
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = MonochromeWhite
                        ),
                        modifier = Modifier.testTag("confirm_delete_routine")
                    ) {
                        Text("CONFIRM DELETION", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showConfirmDelete = false }
                    ) {
                        Text("ABORT", fontFamily = FontFamily.Monospace, color = Zinc500)
                    }
                }
            )
        }
    }
}

@Composable
fun CategoryCard(
    category: Category,
    routines: List<Routine>,
    onAddRoutineClick: () -> Unit,
    onCategoryClick: (Category) -> Unit,
    onRoutineClick: (Routine) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                DesignTokens.StrokeMedium,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                RoundedCornerShape(DesignTokens.PaddingZero)
            )
            .background(MaterialTheme.colorScheme.surface)
            .padding(DesignTokens.PaddingMedium)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Symmetrical Category header with clickable manage "⚙️" icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onCategoryClick(category) }
                        .testTag("edit_category_trigger_${category.id}")
                ) {
                    Text(
                        text = category.name.uppercase(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "⚙️",
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Box(
                    modifier = Modifier
                        .border(
                            DesignTokens.StrokeMedium,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            RoundedCornerShape(DesignTokens.PaddingZero)
                        )
                        .clickable { onAddRoutineClick() }
                        .padding(horizontal = DesignTokens.PaddingSmall, vertical = DesignTokens.PaddingTiny)
                        .testTag("add_routine_trigger_${category.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ ADD ROUTINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))

            if (routines.isEmpty()) {
                Text(
                    text = "NO ROUTINES REGISTERED IN THIS SECTOR.",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = Zinc500,
                    modifier = Modifier.padding(vertical = DesignTokens.PaddingSmall)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.PaddingSmall)) {
                    routines.forEach { routine ->
                        RoutineItem(
                            routine = routine,
                            onRoutineClick = onRoutineClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoutineItem(
    routine: Routine,
    onRoutineClick: (Routine) -> Unit
) {
    val progress = if (routine.targetCount > 0) {
        (routine.completedCount.toFloat() / routine.targetCount.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                DesignTokens.StrokeThin,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                RoundedCornerShape(DesignTokens.PaddingZero)
            )
            .clickable { onRoutineClick(routine) }
            .testTag("routine_item_trigger_${routine.id}")
            .padding(DesignTokens.PaddingSmall)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = routine.title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${routine.completedCount} / ${routine.targetCount} MO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (progress >= 1f) GridLevel4 else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(DesignTokens.PaddingTiny))

            // Brutalist custom linear progress indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(if (progress >= 1f) GridLevel4 else MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
