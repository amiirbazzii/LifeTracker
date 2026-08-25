package com.example

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import com.example.ui.LifeTrackerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.Executor

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LifeTrackerViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()
  private val directExecutor = Executor { it.run() }

  private lateinit var db: AppDatabase
  private lateinit var repository: LifeTrackerRepository
  private lateinit var viewModel: LifeTrackerViewModel
  private lateinit var context: Application

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    context = ApplicationProvider.getApplicationContext<Application>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
        .allowMainThreadQueries()
        .setQueryExecutor(directExecutor)
        .setTransactionExecutor(directExecutor)
        .build()
    repository = LifeTrackerRepository(db.timelineDao())
    viewModel = LifeTrackerViewModel(repository, context, testDispatcher)

    // Clear shared preferences
    val prefs = context.getSharedPreferences("life_tracker_prefs", Context.MODE_PRIVATE)
    prefs.edit().clear().commit()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    db.close()
  }

  @Test
  fun testNormalTaskCompletionAwardsTenPoints() = runTest(testDispatcher) {
    val task = DailyTask(
      taskId = "normal_1",
      weekIndex = 0,
      dayOfWeek = 1,
      taskTitle = "Normal Task Test",
      isCompleted = 0,
      createdTimestamp = System.currentTimeMillis(),
      routineId = null
    )
    db.timelineDao().insertTask(task)

    // Verify initial points are 0
    assertEquals(0, viewModel.userPoints.value)

    viewModel.toggleTaskCompletion(task)

    // Assert that completing normal task adds 10 points
    assertEquals(10, viewModel.userPoints.value)
    
    // Check SharedPreferences persistence
    val prefs = context.getSharedPreferences("life_tracker_prefs", Context.MODE_PRIVATE)
    assertEquals(10, prefs.getInt("user_points", 0))
  }

  @Test
  fun testLinkedTaskCompletionAwardsFifteenPoints() = runTest(testDispatcher) {
    val task = DailyTask(
      taskId = "linked_1",
      weekIndex = 0,
      dayOfWeek = 1,
      taskTitle = "Linked Task Test",
      isCompleted = 0,
      createdTimestamp = System.currentTimeMillis(),
      routineId = "routine_123"
    )
    db.timelineDao().insertTask(task)

    assertEquals(0, viewModel.userPoints.value)

    viewModel.toggleTaskCompletion(task)

    // Assert that completing a linked task adds 15 points
    assertEquals(15, viewModel.userPoints.value)

    val prefs = context.getSharedPreferences("life_tracker_prefs", Context.MODE_PRIVATE)
    assertEquals(15, prefs.getInt("user_points", 0))
  }

  @Test
  fun testMilestoneAchievedAwardsOneHundredPointsAndIncrementsRoutineCount() = runTest(testDispatcher) {
    val routine = Routine(
      id = "routine_123",
      categoryId = "cat_1",
      title = "Publish Article",
      targetCount = 2,
      completedCount = 0,
      createdTimestamp = System.currentTimeMillis()
    )
    db.timelineDao().insertRoutine(routine)

    // Set initial points
    viewModel.addPoints(50)
    assertEquals(50, viewModel.userPoints.value)

    viewModel.incrementRoutineCompletion("routine_123")

    // Awards exactly +100 extra points (50 + 100 = 150)
    assertEquals(150, viewModel.userPoints.value)

    // Correctly increments the completedCount of the linked Routine by 1
    val routinesFromDb = db.timelineDao().getAllRoutines().first()
    val updatedRoutine = routinesFromDb.find { it.id == "routine_123" }
    assertNotNull(updatedRoutine)
    assertEquals(1, updatedRoutine?.completedCount)
  }

  @Test
  fun testAntiFarmingGuardDeductions() = runTest(testDispatcher) {
    val normalTask = DailyTask(
      taskId = "normal_2",
      weekIndex = 0,
      dayOfWeek = 1,
      taskTitle = "Normal Task Test",
      isCompleted = 1, // Already completed
      createdTimestamp = System.currentTimeMillis(),
      routineId = null
    )
    val linkedTask = DailyTask(
      taskId = "linked_2",
      weekIndex = 0,
      dayOfWeek = 1,
      taskTitle = "Linked Task Test",
      isCompleted = 1, // Already completed
      createdTimestamp = System.currentTimeMillis(),
      routineId = "routine_123"
    )

    db.timelineDao().insertTask(normalTask)
    db.timelineDao().insertTask(linkedTask)

    // Set high points
    viewModel.addPoints(100)
    assertEquals(100, viewModel.userPoints.value)

    // 1. Uncheck Normal Task should deduct 10 points (100 -> 90)
    viewModel.toggleTaskCompletion(normalTask)
    assertEquals(90, viewModel.userPoints.value)

    // 2. Uncheck Linked Task should deduct 15 points (90 -> 75)
    viewModel.toggleTaskCompletion(linkedTask)
    assertEquals(75, viewModel.userPoints.value)
  }

  @Test
  fun testRewardClaimLogic() = runTest(testDispatcher) {
    val reward = Reward(
      id = "reward_1",
      name = "Play Game",
      pointCost = 80,
      claimedCount = 0,
      createdTimestamp = System.currentTimeMillis()
    )
    db.timelineDao().insertReward(reward)

    // Test Case 1: Insufficient points
    viewModel.addPoints(50) // Balance = 50, cost = 80
    viewModel.onClaimReward(reward)

    // Claim should fail/blocked, total points unchanged
    assertEquals(50, viewModel.userPoints.value)
    var rewardsFromDb = db.timelineDao().getAllRewards().first()
    var updatedReward = rewardsFromDb.find { it.id == "reward_1" }
    assertEquals(0, updatedReward?.claimedCount)

    // Test Case 2: Sufficient points
    viewModel.addPoints(50) // Balance = 100, cost = 80
    assertEquals(100, viewModel.userPoints.value)

    viewModel.onClaimReward(reward)

    // Claim should succeed, points deducted (100 - 80 = 20)
    assertEquals(20, viewModel.userPoints.value)
    rewardsFromDb = db.timelineDao().getAllRewards().first()
    updatedReward = rewardsFromDb.find { it.id == "reward_1" }
    assertEquals(1, updatedReward?.claimedCount)
  }

  @Test
  fun testCreateHabitAndDailyAutoInjection() = runTest(testDispatcher) {
    // Initialize timeline first
    viewModel.initializeTimeline(25, "Build Life Mastery")

    // Create habit
    viewModel.onCreateHabit("Morning Meditation")

    val habitsFromDb = db.timelineDao().getAllDailyHabits().first()
    val habit = habitsFromDb.find { it.title == "Morning Meditation" }
    assertNotNull(habit)

    // Verify task is injected for today
    val tasksFromDb = db.timelineDao().getAllTasks().first()
    val injectedTask = tasksFromDb.find { it.habitId == habit?.id }
    assertNotNull(injectedTask)
    assertEquals("Morning Meditation", injectedTask?.taskTitle)
    assertEquals(0, injectedTask?.isCompleted)

    // Toggling the injected task completes it and awards 10 points
    viewModel.toggleTaskCompletion(injectedTask!!)
    assertEquals(10, viewModel.userPoints.value)

    val updatedTask = db.timelineDao().getAllTasks().first().find { it.taskId == injectedTask.taskId }
    assertEquals(1, updatedTask?.isCompleted)
  }

  @Test
  fun testUpdateHabitUpdatesUncompletedDailyTasks() = runTest(testDispatcher) {
    viewModel.initializeTimeline(25, "Mastery")
    viewModel.onCreateHabit("Read Books")

    val habit = db.timelineDao().getAllDailyHabits().first().find { it.title == "Read Books" }!!
    viewModel.onUpdateHabit(habit.id, "Read 30 Pages")

    val updatedHabit = db.timelineDao().getAllDailyHabits().first().find { it.id == habit.id }
    assertEquals("Read 30 Pages", updatedHabit?.title)

    val tasks = db.timelineDao().getAllTasks().first()
    val task = tasks.find { it.habitId == habit.id }
    assertEquals("Read 30 Pages", task?.taskTitle)
  }

  @Test
  fun testDeleteHabitCleansUpUncompletedDailyTasks() = runTest(testDispatcher) {
    viewModel.initializeTimeline(25, "Mastery")
    viewModel.onCreateHabit("Morning Stretch")

    val habit = db.timelineDao().getAllDailyHabits().first().find { it.title == "Morning Stretch" }!!
    viewModel.onDeleteHabit(habit.id)

    val habits = db.timelineDao().getAllDailyHabits().first()
    assertEquals(null, habits.find { it.id == habit.id })

    val tasks = db.timelineDao().getAllTasks().first()
    assertEquals(null, tasks.find { it.habitId == habit.id })
  }

  @Test
  fun testTaskTimerUpdateAndSorting() = runTest(testDispatcher) {
    val task1 = DailyTask(
      taskId = "task_no_time",
      weekIndex = 0,
      dayOfWeek = 1,
      taskTitle = "No Time Task",
      isCompleted = 0,
      createdTimestamp = 1000L
    )
    val task2 = DailyTask(
      taskId = "task_late_time",
      weekIndex = 0,
      dayOfWeek = 1,
      taskTitle = "Late Task",
      isCompleted = 0,
      createdTimestamp = 2000L,
      startTime = "14:00",
      endTime = "15:00"
    )
    val task3 = DailyTask(
      taskId = "task_early_time",
      weekIndex = 0,
      dayOfWeek = 1,
      taskTitle = "Early Task",
      isCompleted = 0,
      createdTimestamp = 3000L,
      startTime = "09:00",
      endTime = "10:00"
    )

    db.timelineDao().insertTask(task1)
    db.timelineDao().insertTask(task2)
    db.timelineDao().insertTask(task3)

    // Test sortedWithTimeOrder
    val unsortedList = listOf(task1, task2, task3)
    val sortedList = unsortedList.sortedWithTimeOrder()

    assertEquals("task_early_time", sortedList[0].taskId)
    assertEquals("task_late_time", sortedList[1].taskId)
    assertEquals("task_no_time", sortedList[2].taskId)

    // Test updateTaskTimer
    viewModel.updateTaskTimer(task1, "08:00", "08:30")
    val updatedTask1 = db.timelineDao().getAllTasks().first().find { it.taskId == "task_no_time" }
    assertEquals("08:00", updatedTask1?.startTime)
    assertEquals("08:30", updatedTask1?.endTime)
  }
}
