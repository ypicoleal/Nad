package com.dranilsaarias.nad

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_date_details.*
import kotlinx.android.synthetic.main.cita.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class DateDetailsActivity : AppCompatActivity() {

    private var isPacient: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_details)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        setup()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            finish()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setup() {

        isPacient = intent.getBooleanExtra("isPacient", true)

        if (intent.hasExtra("cita")) {
            Log.i("cita", intent.getStringExtra("cita"))
            val cita = JSONObject(intent.getStringExtra("cita"))

            cancel_btn.setOnClickListener {
                cancelar(cita.getInt("id"))
            }

            reschedule_date_btn.setOnClickListener {
                reprogramar()
            }

            if (cita.getInt("procedimiento__modalidad") == AgendarActivity.Type.IN_PERSON || isPacient || !cita.getBoolean("pago") || cita.getInt("estado") != 1) {
                call_btn.visibility = View.GONE
            } else {
                call_btn.visibility = View.VISIBLE
            }

            if (cita.getInt("procedimiento__modalidad") == AgendarActivity.Type.IN_PERSON && cita.getInt("estado") == 1) {
                cancel_btn.visibility = View.VISIBLE
            } else {
                cancel_btn.visibility = View.GONE
            }

            if (cita.getInt("reprogramar") >= 3 && cita.getInt("estado") != 1) {
                reschedule_date_btn.visibility = View.GONE
            } else {
                reschedule_date_btn.visibility = View.VISIBLE
            }

            date_state.text = getString(R.string.date_state, cita.getString("estado_nombre"))
            when {
                cita.getInt("estado") == 1 -> {
                    date_state.setTextColor(ContextCompat.getColor(this, R.color.citaVigente))
                    state_indicator.setImageResource(R.drawable.cita_vigente)

                    buttons_container.visibility = View.VISIBLE
                }
                cita.getInt("estado") == 2 -> {
                    date_state.setTextColor(ContextCompat.getColor(this, R.color.citaCancelada))
                    state_indicator.setImageResource(R.drawable.cita_cancelada)
                }
                else -> {
                    date_state.setTextColor(ContextCompat.getColor(this, R.color.citaExpirada))
                    state_indicator.setImageResource(R.drawable.cita_expirada)
                }
            }

            val d = cita.getString("fecha")
            if (d == null || d == "null") {
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
            } else if (isPacient && !cita.getBoolean("pago") && cita.getInt("estado") == 1) {
                AlertDialog.Builder(this)
                        .setMessage("Para acceder a la cita Online, por favor cancelar el valor de la consulta.")
                        .setNegativeButton("Aceptar", { _, _ -> })
                        .create()
                        .show()
                cancel_btn.setOnClickListener {
                    goToPayU(cita)
                }
                cancel_btn.visibility = View.VISIBLE
                cancel_text.text = getString(R.string.pagar)
            }

            val s = cita.getString("inicio")
            val e = cita.getString("fin")
            val hour = if (s == JSONObject.NULL || s == null || s == "null") {
                "Sin hora"
            } else {
                "$s - $e"
            }

            var medico = getString(R.string.doctor_name)
            if (cita.getString("medico") != JSONObject.NULL && cita.getString("medico") != "null" && cita.getString("medico") != null) {
                medico = cita.getString("medico")
            }

            val paciente = cita.getString("paciente__nombre")
            val entidad = if (cita.has("entidad_medica") && cita.get("entidad_medica") != JSONObject.NULL) {
                cita.getJSONObject("entidad_medica").getString("nombre")
            } else {
                cita.getString("entidad_nombre")
            }

            if (isPacient) {
                date_details.text = getString(R.string.cita_descripcion, motivo, modalidad, hour, medico)
            } else {
                date_details.text = getString(R.string.cita_descripcion_medico, motivo, modalidad, hour, paciente, entidad)
            }

            border.visibility = View.GONE

            call_btn.setOnClickListener {
                val devices = cita.getJSONArray("devices")
                val intent = Intent(this, CallActivity::class.java)
                if (cita.getInt("minutos") > 0) {
                    intent.putExtra("room", cita.getInt("paciente").toString())
                    intent.putExtra("cita", cita.getInt("id").toString())
                    intent.putExtra("isDoctor", true)
                    intent.putExtra("devices", devices.toString())
                    startActivity(intent)
                    (0 until devices.length())
                            .map { devices.getJSONObject(it) }
                            .forEach { sendCallNotification(cita, it.getString("registration_id"), it.getString("type")) }
                } else {
                    AlertDialog
                            .Builder(this)
                            .setTitle(R.string.app_name)
                            .setMessage("Tiempo de cita terminado para esta consulta.")
                            .setPositiveButton("Aceptar", null)
                            .create()
                            .show()
                }
            }

        }
    }

    private fun sendCallNotification(cita: JSONObject, token: String, type: String) {

        val url = "https://fcm.googleapis.com/fcm/send"

        val request = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    Log.e("tales", response)
                },
                Response.ErrorListener { error ->
                    if (error.networkResponse != null) {
                        Log.e("error", String(error.networkResponse.data))
                    }
                }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                params["Authorization"] = "key=AAAAQnVV4F0:APA91bFWJ-vz8oUSc3l_qDkiKdng1vodSlDkWVEX6paYl_dBRRslvcuOjjRzWwvpnd_fB-ayki5aCNesTxVxgksYb_bz_gifJ0TLNNHHmit_yDCXrmpjPQ6ZJ3595V7KNurzFX9B5CNb"
                return params
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val body = JSONObject()
                if (type == "ios") {
                    val n = JSONObject("{\n" +
                            "      \"body\" : \"Llamada entrante\",\n" +
                            "      \"title\" : \"CitaOnline\"\n" +
                            "      \n" +
                            "    }")
                    body.put("notification", n)
                }
                cita.put("isCalling", true)
                cita.put("doctorToken", FirebaseInstanceId.getInstance().token)
                body.put("to", token)
                body.put("data", cita)
                return body.toString().toByteArray()
            }
        }
        request.setShouldCache(false)
        VolleySingleton.getInstance().addToRequestQueue(request, this)
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

    private fun reprogramar() {
        val intent = Intent(this, RescheduleDateActivity::class.java)
        intent.putExtra("cita", this.intent.getStringExtra("cita"))
        startActivityForResult(intent, 1)
    }

    private fun cancelar(id: Int) {
        val alert = AlertDialog
                .Builder(this)
                .setMessage("¿Esta seguro de que desea cancelar la cita?")
                .setNegativeButton("Cancelar", { _, _ ->

                })
                .setPositiveButton("Aceptar", { _, _ ->
                    confirmCancelar(id)
                })
                .create()
        alert.show()

        val textView = alert.findViewById<TextView>(android.R.id.message)
        val face = Typeface.createFromAsset(assets, "font/futurabt_book.otf")

        textView!!.typeface = face
        textView.gravity = Gravity.CENTER
    }

    @SuppressLint("InflateParams")
    private fun confirmCancelar(id: Int) {
        var reasonSelected = 1

        val inflater = this.layoutInflater
        val v = inflater.inflate(R.layout.cancel_reasons, null)

        val alert = AlertDialog
                .Builder(this)
                .setView(v)
                .setTitle("¿Cuál es el motivo de cancelación?")
                .setPositiveButton("Aceptar", { _, _ ->
                    cancelar(id, reasonSelected)
                })
                .setNegativeButton("Cancelar", { _, _ -> })
                .create()

        alert.show()

        val textView = alert.findViewById<TextView>(R.id.alertTitle)
        val face = Typeface.createFromAsset(assets, "font/futurabt_book.otf")

        textView!!.typeface = face
        textView.gravity = Gravity.CENTER

        val mejoria = v.findViewById<CheckBox>(R.id.mejoria)
        val sinTiempo = v.findViewById<CheckBox>(R.id.sin_tiempo)
        val otroMotivo = v.findViewById<CheckBox>(R.id.otro_motivo)

        mejoria.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                reasonSelected = 1
                sinTiempo.isChecked = false
                otroMotivo.isChecked = false
            } else if (!sinTiempo.isChecked && !otroMotivo.isChecked) {
                mejoria.isChecked = true
            }
        }

        sinTiempo.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                reasonSelected = 2
                mejoria.isChecked = false
                otroMotivo.isChecked = false
            } else if (!otroMotivo.isChecked && !mejoria.isChecked) {
                sinTiempo.isChecked = true
            }
        }

        otroMotivo.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                reasonSelected = 3
                sinTiempo.isChecked = false
                mejoria.isChecked = false
            } else if (!sinTiempo.isChecked && !mejoria.isChecked) {
                otroMotivo.isChecked = true
            }
        }
    }

    private fun cancelar(id: Int, reason: Int) {
        val serviceUrl = getString(R.string.cancelar_cita_form, id)
        val url = getString(R.string.host, serviceUrl)

        val request = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    Log.e("tales", response)
                    finalizarCancelar()
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    if (error.networkResponse != null) {
                        Log.e("error", String(error.networkResponse.data))
                    }
                    Snackbar.make(loading, "Al parecer hubo un error en la peticion intentelo nuevamente mas tarde", Snackbar.LENGTH_LONG).show()
                }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = hashMapOf<String, String>()
                params["username"] = reason.toString()

                return params
            }
        }
        VolleySingleton.getInstance().addToRequestQueue(request, this)
        loading.visibility = View.VISIBLE
    }

    private fun finalizarCancelar() {
        val dialog = AlertDialog
                .Builder(this)
                .setTitle("Cancelar cita")
                .setMessage("Su cita se canceló satisfactoriamente.")
                .setPositiveButton("Aceptar", null)
                .setOnDismissListener {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                .create()
        dialog.show()
        dialog.findViewById<TextView>(android.R.id.message)!!.gravity = Gravity.CENTER
        val face = Typeface.createFromAsset(assets, "font/futurabt_book.otf")
        dialog.findViewById<TextView>(android.R.id.message)!!.typeface = face
    }
}
