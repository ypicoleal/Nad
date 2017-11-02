package com.dranilsaarias.nad

import android.content.ContentValues
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import kotlinx.android.synthetic.main.activity_reschedule_date.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class RescheduleDateActivity : AppCompatActivity() {

    private var validSnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reschedule_date)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        setup()
    }

    private fun setup() {
        if (intent.hasExtra("cita")) {
            val cita = JSONObject(intent.getStringExtra("cita"))

            val d = cita.getString("fecha")
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = parser.parse(d)

            calendar.setCurrentDate(date)
            val monthFormatter = SimpleDateFormat("MMMM / y", Locale.getDefault())

            calendar_indicator.text = monthFormatter.format(date).capitalize()


            val names = Array(7, { _ -> "" })
            names[0] = "Lun" // monday
            names[1] = "Mar" // tuesday
            names[2] = "Mie" // wednesday
            names[3] = "Jue" // thursday
            names[4] = "Vie" // friday
            names[5] = "Sab" // saturday
            names[6] = "Dom" // sunday
            calendar.setDayColumnNames(names)

            calendar.setListener(object : CompactCalendarView.CompactCalendarViewListener {
                override fun onDayClick(dateClicked: Date) {
                    agendar(dateClicked)
                }

                override fun onMonthScroll(firstDayOfNewMonth: Date) {
                    calendar_indicator.text = monthFormatter.format(firstDayOfNewMonth).capitalize()
                    Log.d(ContentValues.TAG, "Month was scrolled to: " + firstDayOfNewMonth)
                }
            })

            prev.setOnClickListener {
                calendar.showPreviousMonth()
            }

            next.setOnClickListener {
                calendar.showNextMonth()
            }
        }
    }

    private fun agendar(date: Date) {
        val today = Calendar.getInstance()
        if (date > today.time) {
            Log.i("agendar", "calendarios")
        } else {
            if (validSnackbar == null) {
                validSnackbar = Snackbar.make(calendar, "La cita solo se puede reservar para dias posteriores a la fecha de hoy", Snackbar.LENGTH_LONG)
            }
            validSnackbar!!.show()
        }
    }
}
