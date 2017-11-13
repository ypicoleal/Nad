package com.dranilsaarias.nad

import android.content.Intent
import android.util.Log

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage!!.from)

        // Check if message contains a data payload.
        if (remoteMessage.data.size > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)

            val cita = remoteMessage.data

            val intent = Intent(this, CallActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            if (cita.containsKey("decline")) {
                if (CallActivity.activity != null) {
                    CallActivity.activity!!.finish()
                }
                return
            }

            intent.putExtra("room", cita.get("paciente"))
            intent.putExtra("cita", cita.get("id"))
            intent.putExtra("doctorToken", cita.get("doctorToken"))
            startActivity(intent)
            /*if ( Check if data needs to be processed by long running job  true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                //scheduleJob();
            } else {
                // Handle message within 10 seconds
                //handleNow();
            }*/

        }

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.notification.body!!)
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    companion object {
        private val TAG = "NotifactionService"
    }
}
