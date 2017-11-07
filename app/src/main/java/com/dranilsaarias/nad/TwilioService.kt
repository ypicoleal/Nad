package com.dranilsaarias.nad

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.twilio.video.*

class TwilioService : Service(), Room.Listener {
    private var room: Room? = null
    private var localParticipant: LocalParticipant? = null
    private var accessToken: String? = null

    override fun onCreate() {
        super.onCreate()
        retrieveAccessTokenfromServer()
        Log.i("room", "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onParticipantDisconnected(p0: Room?, p1: Participant?) {
        Log.i("room", "fallo conexion")
    }

    override fun onRecordingStarted(p0: Room?) {}

    override fun onConnectFailure(p0: Room?, p1: TwilioException?) {
        Log.i("room", "fallo conexion")
    }

    override fun onParticipantConnected(p0: Room?, p1: Participant?) {
        Log.i("room", "entro participante")
    }

    override fun onConnected(p0: Room?) {
        Log.i("room", "conectado")
    }

    override fun onDisconnected(room: Room?, exception: TwilioException?) {
        Log.i("room", "desconectado")
    }

    override fun onRecordingStopped(p0: Room?) {}

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun connectToRoom(roomName: String) {
        Log.i("room", "conectando al room " + roomName)
        val connectOptionsBuilder = ConnectOptions.Builder(accessToken)
                .roomName(roomName)
                .audioTracks(ArrayList())
                .videoTracks(ArrayList())
        room = Video.connect(this, connectOptionsBuilder.build(), this)
    }

    private fun retrieveAccessTokenfromServer() {
        val serviceUrl = getString(R.string.is_login)
        val url = getString(R.string.host, serviceUrl)

        val request = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    Log.i("room", response.toString())
                    if (response.getInt("tipo") == 1) {
                        accessToken = response.getString("token")
                        connectToRoom(response.getInt("id").toString())
                    } else {
                        Log.i("room", "no serivicio para el medico")
                        stopSelf()
                    }
                },
                Response.ErrorListener { error -> Log.e("error", error.message) }
        )
        request.setShouldCache(false)
        VolleySingleton.getInstance().addToRequestQueue(request, this)
    }

    companion object {
        fun startService(context: Context) {
            val intent = Intent(context, TwilioService::class.java)
            context.startService(intent)
        }
    }
}