package com.dranilsaarias.nad

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.util.Log
import android.view.View
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.content_register.*
import org.json.JSONObject
import java.util.*


class RegisterActivity : AppCompatActivity() {
    var tipoDocumento = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setSpinners()
        see_tos_btn.setOnClickListener { vetTos() }
        register_btn.setOnClickListener { register() }
    }


    private fun setSpinners() {
        val document = findViewById<ClickToSelectEditText<Documento>>(R.id.document)
        val civil = findViewById<ClickToSelectEditText<Documento>>(R.id.civil)
        val entidad = findViewById<ClickToSelectEditText<Documento>>(R.id.entidad)

        val documentos = ArrayList<Documento>()
        documentos.add(Documento("Cédula de ciudadanía"))
        documentos.add(Documento("Tarjeta de indetidad"))
        documentos.add(Documento("Cédula de extranjería"))
        documentos.add(Documento("Registro Civil"))
        document.setItems(documentos)
        document.setOnItemSelectedListener(object : ClickToSelectEditText.OnItemSelectedListener<Documento> {
            override fun onItemSelectedListener(item: Documento, selectedIndex: Int) {
                Log.i("documento", "" + selectedIndex)
                tipoDocumento = selectedIndex + 1
                if (selectedIndex == 1 || selectedIndex == 3) {
                    nombre_acudiente_container.visibility = View.VISIBLE
                    cedula_acudiente_container.visibility = View.VISIBLE
                } else {
                    nombre_acudiente_container.visibility = View.GONE
                    cedula_acudiente_container.visibility = View.GONE
                }
            }
        })

        val estados = ArrayList<Documento>()
        estados.add(Documento("Casado/a"))
        estados.add(Documento("Soltero/a"))
        civil.setItems(estados)
        civil.setOnItemSelectedListener(object : ClickToSelectEditText.OnItemSelectedListener<Documento> {
            override fun onItemSelectedListener(item: Documento, selectedIndex: Int) {
                Log.i("estado", item.label)
            }
        })

        val entidades = ArrayList<Documento>()
        entidades.add(Documento("Colsanitas"))
        entidades.add(Documento("Medisanitas"))
        entidades.add(Documento("Particular"))
        entidad.setItems(entidades)
        entidad.setOnItemSelectedListener(object : ClickToSelectEditText.OnItemSelectedListener<Documento> {
            override fun onItemSelectedListener(item: Documento, selectedIndex: Int) {
                Log.i("entidades", item.label)
            }
        })

    }

    private fun vetTos() {

        val inflater = this.layoutInflater
        val v = inflater.inflate(R.layout.tos, null)

        val alert = AlertDialog
                .Builder(this)
                .setView(v)
                .create()

        alert.show()
        val btn = v.findViewById<CardView>(R.id.accept_tos)
        btn.setOnClickListener {
            alert.dismiss()
            accept_tos_cbr.isChecked = true
        }
    }

    private fun register() {

        if (!accept_tos_cbr.isChecked) {
            Snackbar.make(loading, "Debe aceptar los Términos y condiciones para poder registrarse", Snackbar.LENGTH_LONG)
                    .setAction("Aceptar", {
                        accept_tos_cbr.isChecked = true
                    })
                    .setActionTextColor(Color.WHITE)
                    .show()
            return
        }

        val serviceUrl = getString(R.string.register_form)
        val url = getString(R.string.host, serviceUrl)

        val request = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    Log.e("tales", response)
                    loading.visibility = View.GONE
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                        showErrors(String(error.networkResponse.data))
                    } else {
                        Snackbar.make(loading, "Al parecer hubo un error en la peticion intentelo nuevamente mas tarde", Snackbar.LENGTH_LONG).show()
                    }
                }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params.put("username", numero_documento.text.toString())
                params.put("password1", password.text.toString())
                params.put("password2", password_confirm.text.toString())
                params.put("first_name", nombre.text.toString())
                params.put("last_name", apellidos.text.toString())
                params.put("email", email.text.toString())
                params.put("tipo", "" + tipoDocumento)
                params.put("identificacion", numero_documento.text.toString())
                params.put("fecha_nacimiento", fecha_nacimiento.text.toString())
                params.put("estado_civil", civil.text.toString())
                params.put("profesion", profesion.text.toString())
                params.put("telefono", celular.text.toString())
                return params
            }
        }
        VolleySingleton.getInstance().addToRequestQueue(request, this)
        clearErrors()
        loading.visibility = View.VISIBLE
    }

    private fun showErrors(serverResponse: String) {
        Log.e("errors", serverResponse)
        val errors = JSONObject(serverResponse)
        if (errors.has("username")) {
            val error = errors.getJSONArray("username").getString(0)
            numero_documento_container.isErrorEnabled = true
            numero_documento_container.error = error
        }

        if (errors.has("password1")) {
            val error = errors.getJSONArray("password1").getString(0)
            password_container.isErrorEnabled = true
            password_container.error = error
        }

        if (errors.has("password2")) {
            val error = errors.getJSONArray("password2").getString(0)
            password_confirm_container.isErrorEnabled = true
            password_confirm_container.error = error
        }

        if (errors.has("tipo")) {
            val error = errors.getJSONArray("tipo").getString(0)
            document_container.isErrorEnabled = true
            document_container.error = error
        }

        if (errors.has("fecha_nacimiento")) {
            val error = errors.getJSONArray("fecha_nacimiento").getString(0)
            fecha_nacimiento_container.isErrorEnabled = true
            fecha_nacimiento_container.error = error
        }

        if (errors.has("identificacion")) {
            val error = errors.getJSONArray("identificacion").getString(0)
            numero_documento_container.isErrorEnabled = true
            numero_documento_container.error = error
        }

        if (errors.has("estado_civil")) {
            val error = errors.getJSONArray("estado_civil").getString(0)
            civil_container.isErrorEnabled = true
            civil_container.error = error
        }
    }

    private fun clearErrors() {
        numero_documento_container.error = null
        numero_documento_container.isErrorEnabled = false
        password_container.error = null
        password_container.isErrorEnabled = false
        password_confirm_container.error = null
        password_confirm_container.isErrorEnabled = false
        document_container.error = null
        document_container.isErrorEnabled = false
        fecha_nacimiento_container.error = null
        fecha_nacimiento_container.isErrorEnabled = false
        numero_documento_container.error = null
        numero_documento_container.isErrorEnabled = false
        civil_container.error = null
        civil_container.isErrorEnabled = false
    }

    private inner class Documento internal constructor(override val label: String) : Listable
}
