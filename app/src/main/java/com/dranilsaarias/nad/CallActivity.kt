package com.dranilsaarias.nad

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager.LayoutParams
import kotlinx.android.synthetic.main.activity_call.*


class CallActivity : AppCompatActivity() {
    lateinit var r: Ringtone

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val window = this.window
        window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON)
        window.setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN)

        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        r = RingtoneManager.getRingtone(applicationContext, notification)
        r.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        r.stop()
    }

}
