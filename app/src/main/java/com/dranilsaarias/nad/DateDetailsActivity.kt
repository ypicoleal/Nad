package com.dranilsaarias.nad

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View

import kotlinx.android.synthetic.main.activity_date_details.*
import kotlinx.android.synthetic.main.cita.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class DateDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_details)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        setup()
    }

    private fun setup() {
        if (intent.hasExtra("cita")) {
            val cita = JSONObject(intent.getStringExtra("cita"))
            if (cita.getInt("procedimiento__modalidad") == AgendarActivity.Type.IN_PERSON) {
                call_btn.visibility = View.GONE
            } else {
                call_btn.visibility = View.VISIBLE
            }

            date_state.text = getString(R.string.date_state, cita.getString("estado_nombre"))
            if (cita.getInt("estado") == 1) {
                date_state.setTextColor(ContextCompat.getColor(this, R.color.citaVigente))
                state_indicator.setImageResource(R.drawable.cita_vigente)
            } else if (cita.getInt("estado") == 2) {
                date_state.setTextColor(ContextCompat.getColor(this, R.color.citaCancelada))
                state_indicator.setImageResource(R.drawable.cita_cancelada)
            } else {
                date_state.setTextColor(ContextCompat.getColor(this, R.color.citaExpirada))
                state_indicator.setImageResource(R.drawable.cita_expirada)
            }

            val d = cita.getString("fecha")
            if (d.equals(null) || d.equals("null")) {
                date.text = getString(R.string.no_date)
            } else {
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val monthFormatter = SimpleDateFormat("dd MMMM y", Locale.getDefault())
                val dat = parser.parse(d)
                date.text = monthFormatter.format(dat).capitalize()
            }

            val motivo = cita.getString("procedimiento__nombre")
            var modalidad = getText(R.string.atenci_n_online)
            if (cita.getInt("procedimiento__modalidad") == AgendarActivity.Type.IN_PERSON) {
                modalidad = getText(R.string.atenci_n_consultorio)
            }

            val parser = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val hourFomatter = SimpleDateFormat("h:mm a", Locale.getDefault())
            val s = cita.getString("inicio")
            val e = cita.getString("fin")
            val hour: String
            if (s.equals(JSONObject.NULL) || s.equals(null) || s.equals("null")) {
                hour = "Sin hora"
            } else {
                val start = parser.parse(s)
                val end = parser.parse(e)
                hour = hourFomatter.format(start) + " - " + hourFomatter.format(end)
            }

            date_details.text = getString(R.string.cita_descripcion, motivo, modalidad, hour)

            border.visibility = View.GONE
        }
    }
}
