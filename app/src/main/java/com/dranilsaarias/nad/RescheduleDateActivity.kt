package com.dranilsaarias.nad

import android.app.Activity
import android.content.ContentValues
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import kotlinx.android.synthetic.main.activity_reschedule_date.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class RescheduleDateActivity : AppCompatActivity(), CalendarioListAdapter.onCalendarClickListener {

    private var validSnackbar: Snackbar? = null
    private var selectedCalendar: JSONObject? = null
    private var cita: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reschedule_date)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            if (hours_container.visibility == View.VISIBLE) {
                calendar_container.visibility = View.VISIBLE
                hours_container.visibility = View.GONE
            } else if (date_programming_container.visibility == View.VISIBLE) {
                hours_container.visibility = View.VISIBLE
                date_programming_container.visibility = View.GONE
            } else {
                finish()
            }
        }

        setup()
    }

    override fun onBackPressed() {
        if (hours_container.visibility == View.VISIBLE) {
            calendar_container.visibility = View.VISIBLE
            hours_container.visibility = View.GONE
        } else if (date_programming_container.visibility == View.VISIBLE) {
            hours_container.visibility = View.VISIBLE
            date_programming_container.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }

    private fun setup() {
        if (intent.hasExtra("cita")) {
            cita = JSONObject(intent.getStringExtra("cita"))

            val d = cita!!.getString("fecha")
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

            next_btn.setOnClickListener {
                agendarCita()
            }
        }
    }

    private fun setupCalendarios(date: Date) {
        calendar_container.visibility = View.GONE
        hours_container.visibility = View.VISIBLE

        val monthFormatter = SimpleDateFormat("MMMM dd / y", Locale.getDefault())
        date_indicator.text = monthFormatter.format(date).capitalize()

        val c = Calendar.getInstance()
        c.time = date

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        calendar_rv.layoutManager = layoutManager
        calendar_rv.setHasFixedSize(true)

        val adapter = CalendarioListAdapter()
        adapter.calendarClickListener = this
        calendar_rv.adapter = adapter

        val serviceUrl = getString(R.string.calendarios, c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
        val url = getString(R.string.host, serviceUrl)

        val request = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener<JSONObject> { response ->
                    if (response.getJSONArray("object_list").length() == 0) {
                        empty_list.visibility = View.VISIBLE
                        swipe.visibility = View.GONE
                    }
                    adapter.setCalendarios(response.getJSONArray("object_list"))
                    swipe.isRefreshing = false
                },
                Response.ErrorListener { error ->
                    Log.i("error", error.toString())
                })
        VolleySingleton.getInstance().addToRequestQueue(request, this)
        swipe.isRefreshing = true
    }

    private fun agendar(date: Date) {
        val today = Calendar.getInstance()
        if (date > today.time) {
            setupCalendarios(date)
        } else {
            if (validSnackbar == null) {
                validSnackbar = Snackbar.make(calendar, "La cita solo se puede reprogramar para dias posteriores a la fecha de hoy", Snackbar.LENGTH_LONG)
            }
            validSnackbar!!.show()
        }
    }

    private fun agendarCita() {
        val serviceUrl = getString(R.string.reprogramar_cita)
        val url = getString(R.string.host, serviceUrl)

        val request = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    Log.i("response", response)
                    loading.visibility = View.GONE
                    finalizarAgendar()
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                        Snackbar.make(loading, "No se pueden asignar citas para d\u00edas anteriores a la fecha actual", Snackbar.LENGTH_LONG).show()
                    } else {
                        Log.e("error", String(error.networkResponse.data))
                        Snackbar.make(loading, "Al parecer hubo un error en la peticion intentelo nuevamente mas tarde", Snackbar.LENGTH_LONG).show()
                    }
                }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params.put("motivo", motivo.text.toString())
                params.put("cita", cita!!.getInt("id").toString())
                params.put("calendario", selectedCalendar!!.getInt("id").toString())
                return params
            }
        }
        VolleySingleton.getInstance().addToRequestQueue(request, this)
        loading.visibility = View.VISIBLE
    }

    private fun finalizarAgendar() {
        val dialog = AlertDialog
                .Builder(this)
                .setTitle("Agendar cita")
                .setMessage("Su cita se agendó satisfactoriamente.")
                .setPositiveButton("Aceptar", null)
                .setOnDismissListener {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                .create()
        dialog.show()
        dialog.findViewById<TextView>(android.R.id.message)!!.gravity = Gravity.CENTER
    }

    override fun onClick(calendario: JSONObject) {
        selectedCalendar = calendario

        hours_container.visibility = View.GONE
        date_programming_container.visibility = View.VISIBLE

        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val start = parser.parse(selectedCalendar!!.getString("start"))
        val end = parser.parse(selectedCalendar!!.getString("end"))

        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val hourFomatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        val name = hourFomatter.format(start) + " - " + hourFomatter.format(end)
        var medico = "Sin asignar"
        if (!selectedCalendar!!.getString("nombre_medico").equals("")) {
            medico = selectedCalendar!!.getString("nombre_medico")
        }
        date_details.text = getString(R.string.date_description, dateFormatter.format(start), name, medico)
    }
}
