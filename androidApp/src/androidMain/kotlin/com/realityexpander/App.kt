package com.realityexpander

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import appContext
import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.crashlytics.CrashlyticsLogWriter
import debugLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.ZoneId

class App: Application() {

    @OptIn(ExperimentalKermitApi::class) // for crashlytics logger
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate() {
        super.onCreate()

        // Setup debugLog for emailing to developer
        Logger.addLogWriter(
            object: LogWriter() {
                override fun log(
                    severity: Severity,
                    message: String,
                    tag: String,
                    throwable: Throwable?
                ) {
                    CoroutineScope(Dispatchers.Main).launch {
                        //    _debugLogFlow.emit("debug string") // try instead of debugLog.add()
                        debugLog.add(
                            "${Clock.system(ZoneId.systemDefault()).instant()}: " +
                                    "$severity $tag: " + message
                        )
                        if (debugLog.size > 1200) { // max line count, must keep under 1mb for Binder limitations
                            debugLog.removeAt(0)
                        }
                    }
                }
            }
        )
        // Add Crashlytics to logger
        Logger.addLogWriter(CrashlyticsLogWriter())

        // Create the notification channel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location",
                "Marker Status Notifications",
                NotificationManager.IMPORTANCE_LOW // IMPORTANCE_HIGH will make the notification sound
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        appContext = applicationContext

    }

}
