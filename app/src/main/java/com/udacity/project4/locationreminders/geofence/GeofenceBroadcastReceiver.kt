package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.Constants
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.asDomainModell
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * Triggered by the Geofence. Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB.
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that, you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private val remindersLocalRepository: ReminderDataSource by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Constants.ACTION_GEOFENCE_EVENT) return

        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError() || geofencingEvent.triggeringGeofences == null) return

        val geofences = geofencingEvent.triggeringGeofences
        if (geofencingEvent == null || geofencingEvent.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER || geofences!!.isEmpty()) return

        val reminderId = geofences?.get(0)?.requestId
        if (reminderId != null) {
            sendNotification(reminderId, context)
        }
    }

    private fun sendNotification(reminderId: String, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            when (val result = remindersLocalRepository.getReminder(reminderId)) {
                is Result.Success -> sendNotification(context, result.data.asDomainModell())
                is Result.Error -> Timber.i("notification Error")
            }
        }
    }
}
