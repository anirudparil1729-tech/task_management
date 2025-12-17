package com.ctonew.taskmanagement.ui.features

import android.app.Notification
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
import com.ctonew.taskmanagement.R
import com.ctonew.taskmanagement.core.database.repository.ProductivityRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FocusTimerService : Service() {

    @Inject
    lateinit var productivityRepository: ProductivityRepository

    private var timerJob: Job? = null
    private var startTime: Long = 0L
    private var targetDuration: Long = 0L
    private var sessionLocalId: String = ""
    private var sessionCompleted = false

    private val timerCallback = mutableListOf<(Long) -> Unit>()

    inner class FocusTimerBinder : Binder() {
        fun getService(): FocusTimerService = this@FocusTimerService
    }

    private val binder = FocusTimerBinder()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val durationMinutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 25)
                startFocusSession(durationMinutes)
            }
            ACTION_STOP -> {
                stopFocusSession()
            }
            ACTION_COMPLETE -> {
                completeFocusSession()
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tracks focus session progress"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun startFocusSession(durationMinutes: Int) {
        if (sessionCompleted) return
        
        startTime = System.currentTimeMillis()
        targetDuration = durationMinutes * 60 * 1000L
        sessionCompleted = false

        // Create focus session in repository
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sessionLocalId = productivityRepository.createFocusSession(startTime, durationMinutes)
                startTimerCountdown()
            } catch (e: Exception) {
                // Handle error
            }
        }

        startForeground(NOTIFICATION_ID, createTimerNotification())
    }

    private fun startTimerCountdown() {
        timerJob?.cancel()
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (System.currentTimeMillis() - startTime < targetDuration) {
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = targetDuration - elapsed
                
                timerCallback.forEach { callback ->
                    try {
                        callback(remaining)
                    } catch (e: Exception) {
                        // Handle callback error
                    }
                }
                
                updateNotification(remaining)
                delay(1000) // Update every second
            }
            
            // Timer completed
            completeFocusSession()
        }
    }

    fun stopFocusSession() {
        timerJob?.cancel()
        timerJob = null
        
        // Update repository that session was stopped
        if (sessionLocalId.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    productivityRepository.completeFocusSession(sessionLocalId, System.currentTimeMillis())
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun completeFocusSession() {
        timerJob?.cancel()
        timerJob = null
        sessionCompleted = true
        
        // Mark session as completed in repository
        if (sessionLocalId.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    productivityRepository.completeFocusSession(sessionLocalId, System.currentTimeMillis())
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }

        // Send completion notification
        sendCompletionNotification()
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun addTimerCallback(callback: (Long) -> Unit) {
        timerCallback.add(callback)
    }

    fun removeTimerCallback(callback: (Long) -> Unit) {
        timerCallback.remove(callback)
    }

    fun getElapsedTime(): Long {
        return if (startTime > 0) {
            System.currentTimeMillis() - startTime
        } else 0L
    }

    fun getRemainingTime(): Long {
        return if (startTime > 0 && targetDuration > 0) {
            val elapsed = System.currentTimeMillis() - startTime
            (targetDuration - elapsed).coerceAtLeast(0L)
        } else 0L
    }

    fun isRunning(): Boolean = timerJob?.isActive == true

    private fun createTimerNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, com.ctonew.taskmanagement.MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, FocusTimerService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focus Session")
            .setContentText("Timer running...")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                R.drawable.ic_stop,
                "Stop",
                stopPendingIntent
            )
            .build()
    }

    private fun updateNotification(remainingTime: Long) {
        val minutes = remainingTime / 60000
        val seconds = (remainingTime % 60000) / 1000
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focus Session")
            .setContentText(String.format("%02d:%02d remaining", minutes, seconds))
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun sendCompletionNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)

        // Send completion notification
        val completionNotification = NotificationCompat.Builder(this, COMPLETION_CHANNEL_ID)
            .setContentTitle("Focus Session Complete!")
            .setContentText("Nice work. Time for a break!")
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(COMPLETION_NOTIFICATION_ID, completionNotification)
    }

    companion object {
        const val ACTION_START = "START_TIMER"
        const val ACTION_STOP = "STOP_TIMER"
        const val ACTION_COMPLETE = "COMPLETE_TIMER"
        const val EXTRA_DURATION_MINUTES = "duration_minutes"
        
        private const val CHANNEL_ID = "focus_timer"
        private const val COMPLETION_CHANNEL_ID = "focus_completion"
        private const val NOTIFICATION_ID = 1001
        private const val COMPLETION_NOTIFICATION_ID = 1002
        
        fun startService(context: Context, durationMinutes: Int) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DURATION_MINUTES, durationMinutes)
            }
            context.startService(intent)
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}