package com.dranilsaarias.nad

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_agendar.*
import kotlinx.android.synthetic.main.content_agendar.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class AgendarActivity : AppCompatActivity(), CalendarioListAdapter.onCalendarClickListener {

    var selectedCalendar: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agendar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            if (hours_container.visibility == View.GONE) {
                closeProgramingAnimation()
                selectedCalendar = null
            } else {
                finish()
            }
        }

        val type = findViewById<ClickToSelectEditText<Type>>(R.id.motivo)
        val types = ArrayList<Type>()
        types.add(Type("Cita primera vez"))
        types.add(Type("Cita control"))
        types.add(Type("Cita control"))
        types.add(Type("Cita primera vez"))
        types.add(Type("Depilacion laser"))
        types.add(Type("Otro motivo"))
        type.setItems(types)
        type.setOnItemSelectedListener(object : ClickToSelectEditText.OnItemSelectedListener<Type> {
            override fun onItemSelectedListener(item: Type, selectedIndex: Int) {
                Log.i("type", item.label)
            }
        })

        setupCalendarios()

        swipe.setOnRefreshListener {
            selectedCalendar = null
            setupCalendarios()
        }
    }

    override fun onBackPressed() {
        if (hours_container.visibility == View.GONE) {
            closeProgramingAnimation()
            selectedCalendar = null
        } else {
            super.onBackPressed()
        }
    }

    private fun setupCalendarios() {
        val date = intent.getSerializableExtra("date") as Date
        val monthFormatter = SimpleDateFormat("MMMM dd / y", Locale.getDefault())
        calendar_indicator.text = monthFormatter.format(date).capitalize()

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
                    Log.e("tales", response.toString())
                    adapter.setCalendarios(response.getJSONArray("object_list"))
                    swipe.isRefreshing = false
                },
                Response.ErrorListener { _ ->
                    swipe.isRefreshing = false
                })
        VolleySingleton.getInstance().addToRequestQueue(request, this)
        swipe.isRefreshing = true
    }

    private fun openProgramingAnimation() {
        hours_container.visibility = View.GONE
        date_programming_container.visibility = View.VISIBLE

        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val start = parser.parse(selectedCalendar!!.getString("start"))
        val end = parser.parse(selectedCalendar!!.getString("end"))

        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val hourFomatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        val name = hourFomatter.format(start) + " - " + hourFomatter.format(end)
        date_details.text = getString(R.string.date_description, dateFormatter.format(start), name)
    }

    private fun closeProgramingAnimation() {
        hours_container.visibility = View.VISIBLE
        date_programming_container.visibility = View.GONE

    }

    override fun onClick(calendario: JSONObject) {
        Log.e("calendar", calendario.toString())
        selectedCalendar = calendario
        openProgramingAnimation()
    }

    private inner class Type internal constructor(override val label: String) : Listable
}
