package com.ctonew.taskmanagement.ui.features

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.test.*

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class FocusTimerServiceTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var context: Context

    private lateinit var service: FocusTimerService
    private var serviceBound = false
    private var binder: FocusTimerService.FocusTimerBinder? = null
    private val connectionLatch = CountDownLatch(1)
    private val disconnectionLatch = CountDownLatch(1)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binder = service as? FocusTimerService.FocusTimerBinder
            serviceBound = binder != null
            connectionLatch.countDown()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
            binder = null
            disconnectionLatch.countDown()
        }
    }

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        if (serviceBound) {
            context.unbindService(serviceConnection)
        }
        // Clean up any running service
        context.stopService(Intent(context, FocusTimerService::class.java))
    }

    @Test
    fun `service should start and bind successfully`() = runBlocking {
        // Given
        val intent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_START
            putExtra(FocusTimerService.EXTRA_DURATION_MINUTES, 25)
        }

        // When
        val started = context.startService(intent)
        val bound = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        // Then
        assertNotNull(started, "Service should start successfully")
        assertTrue(bound, "Service should bind successfully")
        
        val connected = connectionLatch.await(5, TimeUnit.SECONDS)
        assertTrue(connected, "Service should connect within timeout")
        assertTrue(serviceBound, "Service should be bound")
        assertNotNull(binder, "Binder should not be null")
    }

    @Test
    fun `service should handle start action`() = runBlocking {
        // Given
        val intent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_START
            putExtra(FocusTimerService.EXTRA_DURATION_MINUTES, 15)
        }

        // When
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        connectionLatch.await(5, TimeUnit.SECONDS)

        // Then
        assertTrue(serviceBound)
        val serviceFromBinder = binder?.getService()
        assertNotNull(serviceFromBinder)
        
        // Verify service is in running state
        assertTrue(serviceFromBinder.isRunning())
    }

    @Test
    fun `service should handle stop action`() = runBlocking {
        // Given - Start service first
        val startIntent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_START
            putExtra(FocusTimerService.EXTRA_DURATION_MINUTES, 25)
        }
        context.startService(startIntent)
        context.bindService(startIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        connectionLatch.await(5, TimeUnit.SECONDS)

        assertTrue(serviceBound)
        assertTrue(binder?.getService()?.isRunning() ?: false)

        // When - Stop service
        val stopIntent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_STOP
        }
        context.startService(stopIntent)

        // Then
        delay(1000) // Give time for service to stop
        val serviceFromBinder = binder?.getService()
        assertNotNull(serviceFromBinder)
        assertFalse(serviceFromBinder.isRunning())
    }

    @Test
    fun `service should complete timer automatically`() = runBlocking {
        // Given - Short timer for testing
        val intent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_START
            putExtra(FocusTimerService.EXTRA_DURATION_MINUTES, 1) // 1 minute timer
        }

        // Track callback invocations
        var callbackInvoked = false
        val callback: (Long) -> Unit = { remainingTime ->
            callbackInvoked = true
        }

        // When
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        connectionLatch.await(5, TimeUnit.SECONDS)

        val serviceFromBinder = binder?.getService()
        assertNotNull(serviceFromBinder)

        // Add callback
        serviceFromBinder.addTimerCallback(callback)

        // Wait for timer to complete (1 minute + buffer)
        delay(70 * 1000) // 70 seconds to be safe

        // Then
        assertTrue(callbackInvoked, "Timer callback should be invoked")
        
        // Service should stop automatically after completion
        delay(2000) // Give time for cleanup
        assertFalse(serviceFromBinder.isRunning(), "Service should stop after timer completion")
    }

    @Test
    fun `service should update remaining time correctly`() = runBlocking {
        // Given - Short timer
        val intent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_START
            putExtra(FocusTimerService.EXTRA_DURATION_MINUTES, 2)
        }

        // When
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        connectionLatch.await(5, TimeUnit.SECONDS)

        val serviceFromBinder = binder?.getService()
        assertNotNull(serviceFromBinder)

        // Let it run for a bit
        delay(30 * 1000) // 30 seconds

        // Then
        val remainingTime = serviceFromBinder.getRemainingTime()
        assertTrue(remainingTime < (2 * 60 * 1000), "Remaining time should be less than total duration")
        assertTrue(remainingTime > 0, "Remaining time should still be positive")
    }

    @Test
    fun `service should handle configuration changes`() = runBlocking {
        // Given
        val intent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_START
            putExtra(FocusTimerService.EXTRA_DURATION_MINUTES, 3)
        }

        // Start service
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        connectionLatch.await(5, TimeUnit.SECONDS)

        val serviceFromBinder = binder?.getService()
        assertNotNull(serviceFromBinder)

        val initialRemainingTime = serviceFromBinder.getRemainingTime()

        // Simulate configuration change by rebinding
        context.unbindService(serviceConnection)
        serviceBound = false
        binder = null

        // Rebind
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        connectionLatch.await(5, TimeUnit.SECONDS)

        // Then
        val serviceAfterRebind = binder?.getService()
        assertNotNull(serviceAfterRebind)
        assertTrue(serviceAfterRebind.isRunning(), "Service should still be running after configuration change")
        
        val remainingAfterRebind = serviceAfterRebind.getRemainingTime()
        assertTrue(remainingAfterRebind <= initialRemainingTime, "Time should have elapsed")
        assertTrue(remainingAfterRebind >= 0, "Remaining time should not be negative")
    }

    @Test
    fun `static methods should work correctly`() = runBlocking {
        // Test that static factory methods work without throwing exceptions
        val context = InstrumentationRegistry.getInstrumentation().context
        
        assertDoesNotThrow {
            FocusTimerService.startService(context, 25)
        }
        
        // Give it a moment to start
        delay(1000)
        
        assertDoesNotThrow {
            FocusTimerService.stopService(context)
        }
    }

    @Test
    fun `service should clean up resources on stop`() = runBlocking {
        // Given
        val intent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_START
            putExtra(FocusTimerService.EXTRA_DURATION_MINUTES, 1)
        }

        var callbackInvoked = false
        val callback: (Long) -> Unit = { 
            callbackInvoked = true 
        }

        // When
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        connectionLatch.await(5, TimeUnit.SECONDS)

        val serviceFromBinder = binder?.getService()
        assertNotNull(serviceFromBinder)

        serviceFromBinder.addTimerCallback(callback)
        serviceFromBinder.getService() // Keep reference

        // Stop service
        val stopIntent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_STOP
        }
        context.startService(stopIntent)

        // Unbind
        context.unbindService(serviceConnection)

        // Then - Service should eventually stop
        delay(2000)
        assertFalse(serviceFromBinder.isRunning())
    }
}