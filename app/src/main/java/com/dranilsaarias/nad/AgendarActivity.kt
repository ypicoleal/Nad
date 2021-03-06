package com.dranilsaarias.nad

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.activity_agendar.*
import kotlinx.android.synthetic.main.content_agendar.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AgendarActivity : AppCompatActivity(), CalendarioListAdapter.OnCalendarClickListener {

    private lateinit var virtualTypes: ArrayList<Type>
    private lateinit var inPersonTypes: ArrayList<Type>

    private var selectedCalendar: JSONObject? = null
    private var selectedType: Type? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agendar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            when {
                price.visibility == View.VISIBLE -> {
                    next_btn_tv.text = getString(R.string.next)

                    price.visibility = View.GONE
                    price_label.visibility = View.GONE
                    date_type.visibility = View.VISIBLE
                    motivo_container.visibility = View.VISIBLE
                    tos_payu_btn.visibility = View.GONE
                }
                hours_container.visibility == View.GONE -> {
                    closeProgramingAnimation()
                    selectedCalendar = null
                }
                else -> finish()
            }
        }
        setupCheckBoxes()
        setupCalendarios()

        swipe.setOnRefreshListener {
            selectedCalendar = null
            setupCalendarios()
        }

        next_btn.setOnClickListener {
            if (selectedType != null) {
                when {
                    atencion_consultorio.isChecked -> agendarCita()
                    price.visibility != View.VISIBLE -> pagarCita()
                    else -> agendarCita()
                }
            }
        }
        tos_payu_btn.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://legal.payulatam.com/ES/terminos_y_condiciones_compradores.html"))
            startActivity(browserIntent)
        }
    }

    private fun pagarCita() {
        price.text = getString(R.string.precion, NumberFormat.getNumberInstance(Locale.getDefault()).format(selectedType!!.price))
        next_btn_tv.text = getString(R.string.pago_texto)

        price.visibility = View.VISIBLE
        price_label.visibility = View.VISIBLE
        date_type.visibility = View.GONE
        motivo_container.visibility = View.GONE
        tos_payu_btn.visibility = View.VISIBLE
    }

    private fun agendarCita() {
        val entidad = intent.getIntExtra("entidad", -1).toString()

        val serviceUrl = getString(R.string.agenda_form)
        val url = getString(R.string.host, serviceUrl)

        val request = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    Log.i("response", response)
                    val cita = JSONObject(response)
                    loading.visibility = View.GONE
                    if (cita.getBoolean("virtual")) {
                        goToPayU(cita)
                    } else {
                        finalizarAgendar()
                    }
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                        val err = JSONObject(String(error.networkResponse.data))
                        for (key in err.keys())
                            Snackbar.make(loading, err.getJSONArray(key).getString(0), Snackbar.LENGTH_LONG).show()
                    } else {
                        Log.e("error", String(error.networkResponse.data))
                        Snackbar.make(loading, "Al parecer hubo un error en la peticion intentelo nuevamente mas tarde", Snackbar.LENGTH_LONG).show()
                    }
                }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["procedimiento"] = selectedType!!.id.toString()
                params["entidadMedica"] = entidad
                params["calendario"] = selectedCalendar!!.getInt("id").toString()
                return params
            }
        }
        VolleySingleton.getInstance().addToRequestQueue(request, this)
        loading.visibility = View.VISIBLE
    }

    private fun goToPayU(cita: JSONObject) {
        val tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val countryCode = tm.networkCountryIso
        Log.i("locale", countryCode)
        val currency: String
        currency = if (countryCode == "co") {
            getString(R.string.param_cop)
        } else {
            getString(R.string.param_usd)
        }
        val serviceUrl = getString(R.string.pay_url, cita.getInt("id"), currency)
        val url = getString(R.string.host, serviceUrl)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
        finish()
    }

    private fun finalizarAgendar() {
        val dialog = AlertDialog
                .Builder(this)
                .setTitle("Agendar cita")
                .setMessage("Su cita se agendó satisfactoriamente.")
                .setPositiveButton("Aceptar", null)
                .setOnDismissListener {
                    finish()
                }
                .create()
        dialog.show()
        dialog.findViewById<TextView>(android.R.id.message)!!.gravity = Gravity.CENTER
    }

    override fun onBackPressed() {
        when {
            price.visibility == View.VISIBLE -> {
                next_btn_tv.text = getString(R.string.next)

                price.visibility = View.GONE
                price_label.visibility = View.GONE
                date_type.visibility = View.VISIBLE
                motivo_container.visibility = View.VISIBLE
                tos_payu_btn.visibility = View.GONE
            }
            hours_container.visibility == View.GONE -> {
                closeProgramingAnimation()
                selectedCalendar = null
            }
            else -> super.onBackPressed()
        }
    }

    private fun setupCalendarios() {
        val date = intent.getSerializableExtra("date") as Date
        val restriccion = intent.getStringExtra("restriccion")
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
                    if (response.getJSONArray("object_list").length() == 0) {
                        empty_list.visibility = View.VISIBLE
                        swipe.visibility = View.GONE
                    }

                    val res = if (restriccion.isNullOrBlank()) {
                        null
                    } else {
                        JSONArray(restriccion)
                    }
                    adapter.setCalendarios(response.getJSONArray("object_list"), res)
                    swipe.isRefreshing = false
                },
                Response.ErrorListener { error ->
                    Log.i("error", error.toString())
                })
        VolleySingleton.getInstance().addToRequestQueue(request, this)
        swipe.isRefreshing = true
    }

    private fun setupTypes(list: JSONArray) {
        virtualTypes = ArrayList()
        inPersonTypes = ArrayList()

        val type = findViewById<ClickToSelectEditText<Type>>(R.id.motivo)
        type.setOnItemSelectedListener(object : ClickToSelectEditText.OnItemSelectedListener<Type> {
            override fun onItemSelectedListener(item: Type, selectedIndex: Int) {
                selectedType = item
            }
        })

        for (i in 0 until list.length()) {
            val item = list.getJSONObject(i)
            val t = Type(item.getString("nombre"))
            t.id = item.getInt("id")
            t.price = item.getInt("precio")
            if (item.getInt("modalidad") == Type.IN_PERSON) {
                inPersonTypes.add(t)
            } else {
                virtualTypes.add(t)
            }
        }
        filterType()
    }

    private fun setupCheckBoxes() {
        if (intent.getStringExtra("entidadName") != PARTICULAR) {
            atencion_online.isChecked = false
            atencion_consultorio.isChecked = true
            atencion_consultorio.isEnabled = false
            atencion_online.isEnabled = false
            next_btn_tv.text = getString(R.string.agendar)
        }
        atencion_consultorio.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                selectedType = null
                atencion_online.isChecked = false
                next_btn_tv.text = getString(R.string.agendar)
                filterType()
            } else if (!atencion_online.isChecked) {
                atencion_consultorio.isChecked = true
            }
        }

        atencion_online.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                selectedType = null
                atencion_consultorio.isChecked = false
                next_btn_tv.text = getString(R.string.next)
                filterType()
            } else if (!atencion_consultorio.isChecked) {
                atencion_online.isChecked = true
            }
        }
    }

    private fun filterType() {
        val type = findViewById<ClickToSelectEditText<Type>>(R.id.motivo)
        type.setText("")
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
        var medico = getString(R.string.doctor_name)
        if (selectedCalendar!!.getString("nombre_medico") != "") {
            medico = selectedCalendar!!.getString("nombre_medico")
        }
        date_details.text = getString(R.string.date_description, dateFormatter.format(start), name, medico)
    }

    private fun closeProgramingAnimation() {
        hours_container.visibility = View.VISIBLE
        date_programming_container.visibility = View.GONE
    }

    override fun onClick(calendario: JSONObject) {
        Log.e("calendar", calendario.toString())
        selectedCalendar = calendario
        setupTypes(calendario.getJSONArray("procedimientos"))
        openProgramingAnimation()
    }

    companion object {
        const val PARTICULAR = "Particular"
    }

    class Type(override val label: String) : Listable {

        var id: Int = -1
        var price: Int = 0

        companion object {
            const val IN_PERSON = 1
        }
    }
}
