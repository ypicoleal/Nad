package com.dranilsaarias.nad

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class NotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
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
            val bigText = NotificationCompat.BigTextStyle()
            Log.d(TAG, "Message Notification Body: " + remoteMessage.notification.body!!)
            val builder = NotificationCompat.Builder(this, "NAD")

            builder.setSmallIcon(R.drawable.ic_notificacion)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher))
                    .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .setContentTitle(remoteMessage.notification.title)
                    .setContentText(remoteMessage.notification.body)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setStyle(bigText)
                    .setAutoCancel(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.priority = NotificationManager.IMPORTANCE_HIGH
            } else {
                builder.priority = Notification.PRIORITY_MAX
            }

            val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.notify(0, builder.build())
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    companion object {
        private val TAG = "NotifactionService"
    }
}
