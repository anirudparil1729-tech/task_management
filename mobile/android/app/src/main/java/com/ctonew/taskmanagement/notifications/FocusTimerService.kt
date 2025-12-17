package com.ctonew.taskmanagement.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ctonew.taskmanagement.MainActivity
import com.ctonew.taskmanagement.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FocusTimerService : Service() {
  @Inject
  lateinit var notificationHelper: NotificationHelper

  private val binder = FocusTimerBinder()
  private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

  private val _timerState = MutableStateFlow(TimerState.IDLE)
  val timerState: StateFlow<TimerState> = _timerState

  private val _remainingSeconds = MutableStateFlow(0)
  val remainingSeconds: StateFlow<Int> = _remainingSeconds

  private var timerJob: Job? = null
  private var totalDurationMinutes = 0

  override fun onBind(intent: Intent): IBinder = binder

  fun startFocusTimer(durationMinutes: Int) {
    totalDurationMinutes = durationMinutes
    _remainingSeconds.value = durationMinutes * 60
    _timerState.value = TimerState.RUNNING

    startForeground(NOTIFICATION_ID, createForegroundNotification())

    timerJob?.cancel()
    timerJob = serviceScope.launch {
      while (_remainingSeconds.value > 0) {
        delay(1000)
        _remainingSeconds.value--
        updateForegroundNotification()
      }
      onTimerComplete()
    }
  }

  fun pauseFocusTimer() {
    _timerState.value = TimerState.PAUSED
    timerJob?.cancel()
    updateForegroundNotification()
  }

  fun resumeFocusTimer() {
    _timerState.value = TimerState.RUNNING
    timerJob = serviceScope.launch {
      while (_remainingSeconds.value > 0) {
        delay(1000)
        _remainingSeconds.value--
        updateForegroundNotification()
      }
      onTimerComplete()
    }
  }

  fun stopFocusTimer() {
    _timerState.value = TimerState.IDLE
    timerJob?.cancel()
    stopForeground(STOP_FOREGROUND_REMOVE)
    stopSelf()
  }

  private fun onTimerComplete() {
    _timerState.value = TimerState.COMPLETED
    notificationHelper.showFocusCompletionNotification(totalDurationMinutes)
    stopForeground(STOP_FOREGROUND_REMOVE)
    stopSelf()
  }

  private fun createForegroundNotification(): android.app.Notification {
    createNotificationChannel()

    val intent = Intent(this, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
      this,
      0,
      intent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    val minutes = _remainingSeconds.value / 60
    val seconds = _remainingSeconds.value % 60

    return NotificationCompat.Builder(this, CHANNEL_FOCUS_TIMER)
      .setSmallIcon(R.drawable.ic_notification)
      .setContentTitle("Focus Timer Running")
      .setContentText(String.format("%02d:%02d remaining", minutes, seconds))
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .setOngoing(true)
      .setContentIntent(pendingIntent)
      .build()
  }

  private fun updateForegroundNotification() {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(NOTIFICATION_ID, createForegroundNotification())
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_FOCUS_TIMER,
        "Focus Timer",
        NotificationManager.IMPORTANCE_LOW,
      ).apply {
        description = "Ongoing focus timer notification"
        setShowBadge(false)
      }
      val notificationManager = getSystemService(NotificationManager::class.java)
      notificationManager.createNotificationChannel(channel)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    serviceScope.cancel()
    timerJob?.cancel()
  }

  inner class FocusTimerBinder : Binder() {
    fun getService(): FocusTimerService = this@FocusTimerService
  }

  enum class TimerState {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED,
  }

  companion object {
    private const val NOTIFICATION_ID = 5000
    private const val CHANNEL_FOCUS_TIMER = "focus_timer"
  }
}
