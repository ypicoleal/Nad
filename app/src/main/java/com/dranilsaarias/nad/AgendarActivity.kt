package com.dranilsaarias.nad

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.activity_agendar.*
import kotlinx.android.synthetic.main.content_agendar.*
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AgendarActivity : AppCompatActivity(), CalendarioListAdapter.onCalendarClickListener {

    private lateinit var virtualTypes: ArrayList<Type>
    private lateinit var inPersonTypes: ArrayList<Type>

    private var selectedCalendar: JSONObject? = null
    private var selectedType: Type? = null

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

        setupTypes()
        setupCheckBoxes()
        setupCalendarios()

        swipe.setOnRefreshListener {
            selectedCalendar = null
            setupCalendarios()
        }

        next_btn.setOnClickListener {
            if (selectedType != null) {
                if (atencion_consultorio.isChecked) {
                    agendarCita()
                } else {
                    pagarCita()
                }
            }
        }
    }

    private fun pagarCita() {
        price.text = getString(R.string.precion, NumberFormat.getNumberInstance(Locale.getDefault()).format(selectedType!!.price))
        next_btn_tv.text = getString(R.string.pago_texto)

        price.visibility = View.VISIBLE
        price_label.visibility = View.VISIBLE
        date_type.visibility = View.GONE
        motivo_container.visibility = View.GONE
    }

    private fun agendarCita() {
        val entidad = intent.getIntExtra("entidad", -1).toString()

        val serviceUrl = getString(R.string.agenda_form)
        val url = getString(R.string.host, serviceUrl)

        val request = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->

                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                        Log.e("error", String(error.networkResponse.data))
                        Snackbar.make(loading, "Usuario y/o contraseña incorrecta", Snackbar.LENGTH_LONG).show()
                    } else {
                        Snackbar.make(loading, "Al parecer hubo un error en la peticion intentelo nuevamente mas tarde", Snackbar.LENGTH_LONG).show()
                    }
                }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params.put("procedimiento", selectedType!!.id.toString())
                params.put("entidad", entidad)
                params.put("calendario", selectedCalendar!!.getInt("id").toString())
                return params
            }
        }
        VolleySingleton.getInstance().addToRequestQueue(request, this)
        loading.visibility = View.VISIBLE
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

                })
        VolleySingleton.getInstance().addToRequestQueue(request, this)
        swipe.isRefreshing = true
    }

    private fun setupTypes() {
        virtualTypes = ArrayList()
        inPersonTypes = ArrayList()

        val type = findViewById<ClickToSelectEditText<Type>>(R.id.motivo)
        type.setOnItemSelectedListener(object : ClickToSelectEditText.OnItemSelectedListener<Type> {
            override fun onItemSelectedListener(item: Type, selectedIndex: Int) {
                selectedType = item
            }
        })

        val serviceUrl = getString(R.string.procedimientos)
        val url = getString(R.string.host, serviceUrl)

        val request = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener<JSONObject> { response ->
                    Log.e("tales", response.toString())
                    val list = response.getJSONArray("object_list")
                    for (i in 0 until list.length()) {
                        val item = list.getJSONObject(i)
                        val t = Type(item.getString("nombre"))
                        //todo decomentar esta linea
                        //t.id = item.getInt("id")
                        t.price = item.getInt("precio")
                        if (item.getInt("modalidad") == Type.IN_PERSON) {
                            inPersonTypes.add(t)
                        } else {
                            virtualTypes.add(t)
                        }
                    }
                    filterType()
                },
                Response.ErrorListener { _ ->
                    swipe.isRefreshing = false
                })
        VolleySingleton.getInstance().addToRequestQueue(request, this)
        swipe.isRefreshing = true

    }

    private fun setupCheckBoxes() {
        if (intent.getIntExtra("entidad", -1) > 1) {
            atencion_online.isChecked = false
            atencion_consultorio.isChecked = true
            atencion_consultorio.isEnabled = false
            atencion_online.isEnabled = false
            next_btn_tv.text = getString(R.string.agendar)
        }
        atencion_consultorio.setOnCheckedChangeListener { button, checked ->
            if (checked) {
                selectedType = null
                atencion_online.isChecked = false
                next_btn_tv.text = getString(R.string.agendar)
                filterType()
            }
        }

        atencion_online.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                selectedType = null
                atencion_consultorio.isChecked = false
                next_btn_tv.text = getString(R.string.next)
                filterType()
            }
        }
    }

    private fun filterType() {
        val type = findViewById<ClickToSelectEditText<Type>>(R.id.motivo)
        if (atencion_consultorio.isChecked) {
            type.setItems(inPersonTypes)
        } else {
            type.setItems(virtualTypes)
        }
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

    private class Type(override val label: String) : Listable {

        var id: Int = -1
        var price: Int = 0

        companion object {
            val IN_PERSON: Int = 1
            val VIRTUAL: Int = 2
        }
    }
}
