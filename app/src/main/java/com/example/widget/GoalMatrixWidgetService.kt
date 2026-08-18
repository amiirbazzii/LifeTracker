package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.AppDatabase
import com.example.data.DailyTask
import com.example.data.SubGoal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class GoalMatrixWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        return GoalMatrixRemoteViewsFactory(applicationContext, appWidgetId)
    }
}

class GoalMatrixRemoteViewsFactory(
    private val context: Context,
    private val appWidgetId: Int
) : RemoteViewsService.RemoteViewsFactory {

    private var totalWeeks: Int = 0
    private var inceptionTimestamp: Long = 0L
    private var currentWeekIndex: Int = 0
    private var weekColors: Map<Int, Int> = emptyMap()
    private var currentWeekTasks: List<DailyTask> = emptyList()
    private var subGoals: List<SubGoal> = emptyList()

    // Calculated layout rows
    private val matrixRowBitmaps = mutableListOf<Bitmap>()

    override fun onCreate() {
        loadData()
    }

    override fun onDataSetChanged() {
        loadData()
    }

    private fun loadData() {
        try {
            runBlocking {
                val db = AppDatabase.getDatabase(context)
                val dao = db.timelineDao()
                val meta = dao.getTimelineMeta().firstOrNull()
                val allTasks = dao.getAllTasks().first()
                subGoals = dao.getAllSubGoals().first()

                if (meta != null) {
                    totalWeeks = meta.totalWeeks
                    inceptionTimestamp = meta.inceptionTimestamp

                    val elapsedMillis = System.currentTimeMillis() - inceptionTimestamp
                    val weekInMillis = 7 * 24 * 60 * 60 * 1000L
                    val computedWeek = (elapsedMillis / weekInMillis).toInt()
                    currentWeekIndex = computedWeek.coerceIn(0, totalWeeks - 1)

                    val tasksGroupedByWeek = allTasks.groupBy { it.weekIndex }
                    weekColors = (0 until totalWeeks).associateWith { wIndex ->
                        val weekTasks = tasksGroupedByWeek[wIndex] ?: emptyList()
                        val totalCreated = weekTasks.size
                        if (totalCreated == 0) {
                            0
                        } else {
                            val completed = weekTasks.count { it.isCompleted == 1 }
                            val sr = (completed.toFloat() / totalCreated.toFloat()) * 100f
                            when {
                                sr == 0f -> 0
                                sr < 33f -> 1
                                sr < 66f -> 2
                                sr < 100f -> 3
                                else -> 4
                            }
                        }
                    }
                    currentWeekTasks = tasksGroupedByWeek[currentWeekIndex] ?: emptyList()

                    generateRowBitmaps()
                } else {
                    totalWeeks = 0
                    matrixRowBitmaps.clear()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            totalWeeks = 0
            matrixRowBitmaps.clear()
        }
    }

    private fun generateRowBitmaps() {
        matrixRowBitmaps.clear()
        if (totalWeeks <= 0) return

        // 13 columns matching the app's timeline grid layout
        val columns = 13
        val cellSize = 38f
        val spacing = 4f
        val weeksPerMonth = 4

        val totalCanvasWidth = ((columns * cellSize) + ((columns - 1) * spacing)).toInt()
        val rowHeight = (cellSize + spacing).toInt()

        // Exact color tokens from Color.kt
        val levelColors = intArrayOf(
            Color.parseColor("#0A0A0A"), // GridLevel0_Dark
            Color.parseColor("#00441B"), // GridLevel1
            Color.parseColor("#006D2C"), // GridLevel2
            Color.parseColor("#238B45"), // GridLevel3
            Color.parseColor("#39FF14")  // GridLevel4 (Neon Matrix Green)
        )

        val monoBold = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        val monoNormal = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = monoNormal
            textSize = 13f
        }

        // Data structure for placed grid items
        data class GridPlacedItem(
            val colStart: Int,
            val span: Int,
            val weekIdx: Int
        )

        // 1. Pack items into 13-column rows matching LazyVerticalGrid logic
        val rowsList = mutableListOf<MutableList<GridPlacedItem>>()
        var currentRow = mutableListOf<GridPlacedItem>()
        var currentCol = 0

        for (w in 0 until totalWeeks) {
            val span = if (w == currentWeekIndex) 7 else 1
            if (currentCol + span > columns) {
                rowsList.add(currentRow)
                currentRow = mutableListOf()
                currentCol = 0
            }
            currentRow.add(GridPlacedItem(currentCol, span, w))
            currentCol += span
            if (currentCol >= columns) {
                rowsList.add(currentRow)
                currentRow = mutableListOf()
                currentCol = 0
            }
        }
        if (currentRow.isNotEmpty()) {
            rowsList.add(currentRow)
        }

        // 2. Render each row bitmap with equal horizontal and vertical spacing
        for (rowItems in rowsList) {
            val rowBitmap = Bitmap.createBitmap(totalCanvasWidth, rowHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(rowBitmap)
            canvas.drawColor(Color.TRANSPARENT)

            for (item in rowItems) {
                val startX = item.colStart * (cellSize + spacing)

                if (item.span == 7) {
                    // Active current week container spanning 7 columns with an outer border around all day squares
                    val totalSpanWidth = (7 * cellSize) + (6 * spacing)
                    val outerWeekRect = RectF(startX, 0f, startX + totalSpanWidth, cellSize)
                    val dayWidth = totalSpanWidth / 7f

                    // 1. Draw each individual day inside the active week container
                    for (d in 1..7) {
                        val dayTasks = currentWeekTasks.filter { it.dayOfWeek == d }
                        val dayTotal = dayTasks.size
                        val dayLevel = if (dayTotal == 0) {
                            0
                        } else {
                            val completed = dayTasks.count { it.isCompleted == 1 }
                            val sr = (completed.toFloat() / dayTotal.toFloat()) * 100f
                            when {
                                sr == 0f -> 0
                                sr < 33f -> 1
                                sr < 66f -> 2
                                sr < 100f -> 3
                                else -> 4
                            }
                        }

                        val dayLeft = startX + (d - 1) * dayWidth
                        val dayRight = if (d == 7) outerWeekRect.right else (dayLeft + dayWidth)
                        val dayTop = 0f
                        val dayBottom = cellSize
                        val dayRect = RectF(dayLeft, dayTop, dayRight, dayBottom)

                        // Fill day background
                        paint.color = levelColors[dayLevel]
                        canvas.drawRect(dayRect, paint)

                        // Inner day border
                        val isToday = com.example.ui.Utils.isCellToday(inceptionTimestamp, currentWeekIndex, d)
                        if (isToday) {
                            borderPaint.color = Color.parseColor("#39FF14")
                            borderPaint.strokeWidth = 2.5f
                            canvas.drawRect(dayRect, borderPaint)
                        } else {
                            borderPaint.color = Color.parseColor("#27272A")
                            borderPaint.strokeWidth = 1f
                            canvas.drawRect(dayRect, borderPaint)
                        }

                        // Day of Month text with exact Monospace font
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = inceptionTimestamp
                            add(Calendar.WEEK_OF_YEAR, currentWeekIndex)
                            add(Calendar.DAY_OF_YEAR, d - 1)
                        }
                        val dayNum = cal.get(Calendar.DAY_OF_MONTH).toString()
                        textPaint.typeface = monoBold
                        textPaint.textSize = 13.5f
                        textPaint.color = if (isToday) {
                            if (dayLevel == 4) Color.BLACK else Color.parseColor("#39FF14")
                        } else if (dayLevel > 0) {
                            Color.WHITE
                        } else {
                            Color.parseColor("#A1A1AA")
                        }
                        canvas.drawText(dayNum, dayRect.centerX(), dayRect.centerY() + 5f, textPaint)
                    }

                    // 2. Draw outer framing border around all 7 day squares (matching in-app active week container)
                    val outerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        color = Color.parseColor("#52525B")
                        strokeWidth = 2f
                    }
                    canvas.drawRect(outerWeekRect, outerBorderPaint)
                } else {
                    // Regular single week square
                    val left = startX
                    val top = 0f
                    val rect = RectF(left, top, left + cellSize, top + cellSize)

                    val level = weekColors[item.weekIdx] ?: 0
                    val isGoalDeadline = subGoals.isNotEmpty() && subGoals.any { sg ->
                        val targetWeekIdx = ((sg.startMonth + sg.durationMonths) * weeksPerMonth) - 1
                        item.weekIdx == targetWeekIdx
                    }

                    val cellColor = when {
                        isGoalDeadline -> Color.parseColor("#222222")
                        level in 0..4 -> levelColors[level]
                        else -> levelColors[0]
                    }

                    // Fill background
                    paint.color = cellColor
                    canvas.drawRect(rect, paint)

                    // Draw Border
                    if (isGoalDeadline) {
                        borderPaint.color = Color.WHITE
                        borderPaint.strokeWidth = 1.8f
                    } else {
                        borderPaint.color = Color.argb(38, 255, 255, 255) // Primary alpha 0.15f
                        borderPaint.strokeWidth = 1f
                    }
                    canvas.drawRect(rect, borderPaint)

                    // Draw Week Number with exact Monospace font
                    textPaint.typeface = if (isGoalDeadline || level > 0) monoBold else monoNormal
                    textPaint.textSize = 13f
                    textPaint.color = if (isGoalDeadline || level > 0) Color.WHITE else Color.argb(130, 255, 255, 255)
                    val weekNumStr = (item.weekIdx + 1).toString()
                    canvas.drawText(weekNumStr, rect.centerX(), rect.centerY() + 4.8f, textPaint)
                }
            }

            matrixRowBitmaps.add(rowBitmap)
        }
    }

    override fun onDestroy() {
        matrixRowBitmaps.clear()
    }

    override fun getCount(): Int = matrixRowBitmaps.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position !in matrixRowBitmaps.indices) return null
        val bitmap = matrixRowBitmaps[position]

        val views = RemoteViews(context.packageName, R.layout.widget_goal_matrix_row)
        views.setImageViewBitmap(R.id.matrix_row_image, bitmap)

        // Launch app on tap
        val fillInIntent = Intent()
        views.setOnClickFillInIntent(R.id.matrix_row_root, fillInIntent)
        views.setOnClickFillInIntent(R.id.matrix_row_image, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = false
}
