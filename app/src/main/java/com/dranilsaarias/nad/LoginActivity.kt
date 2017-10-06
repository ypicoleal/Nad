package com.dranilsaarias.nad

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.content_login.*
import java.util.*


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        password.transformationMethod = PasswordTransformationMethod()

        tos_btn.setOnClickListener {
            vetTos()
        }

        register_btn.setOnClickListener {
            registrar()
        }

        login_btn.setOnClickListener {
            login()
        }

        forgot_password.setOnClickListener {
            forgotPassword()
        }
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
            accept_tos_cb.isChecked = true
        }
    }

    private fun forgotPassword() {
        val inflater = this.layoutInflater
        val v = inflater.inflate(R.layout.forgot_password, null)

        AlertDialog
                .Builder(this)
                .setView(v)
                .setPositiveButton("Recuperar contraseña", { dialog, _ ->
                    dialog.dismiss()
                })
                .create()
                .show()
    }

    private fun registrar() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    private fun login() {
        if (!accept_tos_cb.isChecked) {
            Snackbar.make(loading, "Debe aceptar los Términos y condiciones para poder iniciar", Snackbar.LENGTH_LONG)
                    .setAction("Aceptar", {
                        accept_tos_cb.isChecked = true
                    })
                    .setActionTextColor(Color.WHITE)
                    .show()
            return
        }
        val serviceUrl = getString(R.string.login)
        val url = getString(R.string.host, serviceUrl)

        val request = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    Log.e("tales", response)
                    loading.visibility = View.GONE
                    finish()
                    startActivity(Intent(this, MainActivity::class.java))
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
                params.put("username", username.text.toString())
                params.put("password", password.text.toString())

                return params
            }
        }
        VolleySingleton.getInstance().addToRequestQueue(request, this)
        loading.visibility = View.VISIBLE
    }
}
