package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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

        // Dynamic columns based on current widget width:
        // Size 2 width (~110-170dp): exactly 5 squares per row
        // Size 3 width (~171-250dp): exactly 7 squares per row
        // Size 4 width (~251-330dp): exactly 9 squares per row
        // Larger sizes: 11-13 squares per row
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minWidthDp = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 0
        
        val columns = when {
            minWidthDp <= 170 -> 5
            minWidthDp <= 250 -> 7
            minWidthDp <= 330 -> 9
            minWidthDp <= 410 -> 11
            else -> 13
        }

        // Each square matches the app proportion with clearly visible typography
        val cellWidth = 48f
        val cellHeight = 42f
        val spacing = 6f
        val weeksPerMonth = 4

        val levelColors = intArrayOf(
            Color.parseColor("#18181B"), // Level 0 (Dark)
            Color.parseColor("#00441B"), // Level 1
            Color.parseColor("#006D2C"), // Level 2
            Color.parseColor("#238B45"), // Level 3
            Color.parseColor("#39FF14")  // Level 4
        )

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            textSize = 15f
        }

        val totalCanvasWidth = ((columns * cellWidth) + ((columns - 1) * spacing)).toInt()
        val rowHeight = (cellHeight + 4f).toInt()

        // Organize weeks and expanded current week into rows
        var weekIdx = 0
        while (weekIdx < totalWeeks) {
            val isCurrentInThisRow = (weekIdx == currentWeekIndex)

            val rowBitmap = Bitmap.createBitmap(totalCanvasWidth, rowHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(rowBitmap)
            canvas.drawColor(Color.TRANSPARENT)

            if (isCurrentInThisRow) {
                // The current week is expanded into 7 individual day blocks across the row
                val dayColumns = 7
                val daySubCellWidth = (totalCanvasWidth - (spacing * (dayColumns - 1))) / dayColumns.toFloat()

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

                    val left = (d - 1) * (daySubCellWidth + spacing)
                    val right = left + daySubCellWidth
                    val top = 2f
                    val bottom = top + cellHeight
                    val rect = RectF(left, top, right, bottom)

                    // Draw day cell background
                    paint.color = levelColors[dayLevel]
                    canvas.drawRect(rect, paint)

                    // Border (Neon green for Today)
                    val isToday = com.example.ui.Utils.isCellToday(inceptionTimestamp, currentWeekIndex, d)
                    if (isToday) {
                        borderPaint.color = Color.parseColor("#39FF14")
                        borderPaint.strokeWidth = 3f
                    } else {
                        borderPaint.color = Color.parseColor("#3F3F46")
                        borderPaint.strokeWidth = 1.5f
                    }
                    canvas.drawRect(rect, borderPaint)

                    // Day Number (Clearly legible size matching app)
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = inceptionTimestamp
                        add(Calendar.WEEK_OF_YEAR, currentWeekIndex)
                        add(Calendar.DAY_OF_YEAR, d - 1)
                    }
                    val dayNum = cal.get(Calendar.DAY_OF_MONTH).toString()
                    textPaint.color = if (isToday) Color.parseColor("#39FF14") else if (dayLevel > 0) Color.WHITE else Color.parseColor("#A1A1AA")
                    textPaint.textSize = 14f
                    canvas.drawText(dayNum, rect.centerX(), rect.centerY() + 5f, textPaint)
                }

                matrixRowBitmaps.add(rowBitmap)
                weekIdx++
            } else {
                // Standard row containing regular week squares
                val countInRow = minOf(columns, totalWeeks - weekIdx)
                // If the next items contain the current week, slice before it
                val actualCount = if (currentWeekIndex in weekIdx until (weekIdx + countInRow)) {
                    currentWeekIndex - weekIdx
                } else {
                    countInRow
                }

                for (col in 0 until actualCount) {
                    val w = weekIdx + col
                    val left = col * (cellWidth + spacing)
                    val top = 2f
                    val rect = RectF(left, top, left + cellWidth, top + cellHeight)

                    val level = weekColors[w] ?: 0
                    val isGoalDeadline = subGoals.isNotEmpty() && subGoals.any { sg ->
                        val targetWeekIdx = ((sg.startMonth + sg.durationMonths) * weeksPerMonth) - 1
                        w == targetWeekIdx
                    }

                    val cellColor = when {
                        isGoalDeadline -> Color.parseColor("#27272A")
                        level in 0..4 -> levelColors[level]
                        else -> levelColors[0]
                    }

                    // Background
                    paint.color = cellColor
                    canvas.drawRect(rect, paint)

                    // Border
                    if (isGoalDeadline) {
                        borderPaint.color = Color.parseColor("#FFFFFF")
                        borderPaint.strokeWidth = 2.5f
                    } else {
                        borderPaint.color = Color.parseColor("#27272A")
                        borderPaint.strokeWidth = 1.2f
                    }
                    canvas.drawRect(rect, borderPaint)

                    // Week Number (Large, high-contrast, perfectly legible)
                    textPaint.color = if (isGoalDeadline || level > 0) Color.WHITE else Color.parseColor("#71717A")
                    textPaint.textSize = 15f
                    val weekNumStr = (w + 1).toString()
                    canvas.drawText(weekNumStr, rect.centerX(), rect.centerY() + 5.5f, textPaint)
                }

                matrixRowBitmaps.add(rowBitmap)
                weekIdx += actualCount
            }
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
