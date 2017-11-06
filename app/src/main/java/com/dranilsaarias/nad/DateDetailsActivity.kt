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
            if (cita.getInt("procedimiento__modalidad") == AgendarActivity.Type.IN_PERSON || isPacient || !cita.getBoolean("pago")) {
                call_btn.visibility = View.GONE
                cancel_btn.visibility = View.VISIBLE
            } else {
                call_btn.visibility = View.VISIBLE
                cancel_btn.visibility = View.GONE
            }

            if (cita.getInt("reprogramar") >= 3) {
                reschedule_date_btn.visibility = View.GONE
            }

            date_state.text = getString(R.string.date_state, cita.getString("estado_nombre"))
            if (cita.getInt("estado") == 1) {
                date_state.setTextColor(ContextCompat.getColor(this, R.color.citaVigente))
                state_indicator.setImageResource(R.drawable.cita_vigente)

                buttons_container.visibility = View.VISIBLE
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
            } else if (isPacient && !cita.getBoolean("pago")) {
                AlertDialog.Builder(this)
                        .setMessage("Para acceder a la cita Online, por favor cancelar el valor de la consulta presionando el botón pagar")
                        .setNegativeButton("Pagar", { _, _ ->
                            goToPayU(cita)
                        })
                        .create()
                        .show()
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

            var medico = "Sin asignar"
            if (!cita.getString("medico").equals(JSONObject.NULL) && !cita.getString("medico").equals("null") && !cita.getString("medico").equals(null)) {
                medico = cita.getString("medico")
            }

            date_details.text = getString(R.string.cita_descripcion, motivo, modalidad, hour, medico)

            border.visibility = View.GONE

            cancel_btn.setOnClickListener {
                cancelar(cita.getInt("id"))
            }

            reschedule_date_btn.setOnClickListener {
                reprogramar()
            }
        }
    }

    private fun goToPayU(cita: JSONObject) {
        val tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val countryCode = tm.networkCountryIso
        Log.i("locale", countryCode)
        val currency: String
        if (countryCode.equals("co")) {
            currency = getString(R.string.param_cop)
        } else {
            currency = getString(R.string.param_usd)
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
                .setPositiveButton("No", { _, _ ->

                })
                .setNegativeButton("Si", { _, _ ->
                    confirmCancelar(id)
                })
                .create()
        alert.show()

        val textView = alert.findViewById<TextView>(android.R.id.message)
        val face = Typeface.createFromAsset(assets, "font/futurabt_book.otf")

        textView!!.setTypeface(face)
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
                .setPositiveButton("Cancelar", { _, _ ->
                    cancelar(id, reasonSelected)
                })
                .create()

        alert.show()

        val textView = alert.findViewById<TextView>(R.id.alertTitle)
        val face = Typeface.createFromAsset(assets, "font/futurabt_book.otf")

        textView!!.setTypeface(face)
        textView.gravity = Gravity.CENTER

        val mejoria = v.findViewById<CheckBox>(R.id.mejoria)
        val sin_tiempo = v.findViewById<CheckBox>(R.id.sin_tiempo)
        val otro_motivo = v.findViewById<CheckBox>(R.id.otro_motivo)

        mejoria.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                reasonSelected = 1
                sin_tiempo.isChecked = false
                otro_motivo.isChecked = false
            } else if (!sin_tiempo.isChecked && !otro_motivo.isChecked) {
                mejoria.isChecked = true
            }
        }

        sin_tiempo.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                reasonSelected = 2
                mejoria.isChecked = false
                otro_motivo.isChecked = false
            } else if (!otro_motivo.isChecked && !mejoria.isChecked) {
                sin_tiempo.isChecked = true
            }
        }

        otro_motivo.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                reasonSelected = 3
                sin_tiempo.isChecked = false
                mejoria.isChecked = false
            } else if (!sin_tiempo.isChecked && !mejoria.isChecked) {
                otro_motivo.isChecked = true
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
                val params = HashMap<String, String>()
                params.put("username", reason.toString())

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
        dialog.findViewById<TextView>(android.R.id.message)!!.setTypeface(face)
    }
}
