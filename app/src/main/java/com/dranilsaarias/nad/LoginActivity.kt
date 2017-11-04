package com.dranilsaarias.nad

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.text.Html
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.bluejamesbond.text.DocumentView
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.content_login.*
import org.json.JSONObject
import java.util.*


class LoginActivity : AppCompatActivity() {
    private var tos: String = ""
    private var privacy: String = ""
    private var conection: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        password.transformationMethod = PasswordTransformationMethod()

        checkSession()

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

        setTos()
    }

    @SuppressLint("InflateParams")
    private fun vetTos() {

        val inflater = this.layoutInflater
        val v = inflater.inflate(R.layout.tos, null)

        val alert = AlertDialog
                .Builder(this)
                .setView(v)
                .create()

        alert.show()
        val btn = v.findViewById<CardView>(R.id.accept_tos)

        val tosText = v.findViewById<DocumentView>(R.id.tos_text)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tosText.text = Html.fromHtml(tos, Html.FROM_HTML_MODE_LEGACY)
        } else {
            tosText.text = Html.fromHtml(tos)
        }

        btn.setOnClickListener {
            alert.dismiss()
            accept_tos_cb.isChecked = true
        }
    }

    @SuppressLint("InflateParams")
    private fun resenVerification() {
        val inflater = this.layoutInflater
        val v = inflater.inflate(R.layout.forgot_password, null)

        AlertDialog
                .Builder(this)
                .setView(v)
                .setPositiveButton("Reenviar confirmacion", { dialog, _ ->
                    val serviceUrl = getString(R.string.nueva_confirmacion)
                    val url = getString(R.string.host, serviceUrl)
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(browserIntent)
                    dialog.dismiss()
                })
                .create()
                .show()
    }

    private fun forgotPassword() {
        val serviceUrl = getString(R.string.password_reset)
        val url = getString(R.string.host, serviceUrl)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    private fun registrar() {
        val intent = Intent(this, RegisterActivity::class.java)
        intent.putExtra("tos", tos)
        startActivity(intent)
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
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    form.visibility = View.VISIBLE
                    if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                        Log.e("error", String(error.networkResponse.data))
                        Snackbar.make(loading, "Usuario y/o contraseña incorrecta", Snackbar.LENGTH_LONG).show()
                    } else if (error.networkResponse != null && error.networkResponse.statusCode == 403) {
                        Log.e("error", String(error.networkResponse.data))
                        resenVerification()
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
        form.visibility = View.GONE
    }

    private fun checkSession() {
        val serviceUrl = getString(R.string.publicidad)
        val url = getString(R.string.host, serviceUrl)

        val request = StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    Log.e("tales", response)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    if (error.networkResponse != null && error.networkResponse.statusCode == 403) {
                        Log.e("error", String(error.networkResponse.data))
                        form.visibility = View.VISIBLE
                    } else {
                        Snackbar
                                .make(loading, "Al parecer hubo un error en la peticion intentelo nuevamente mas tarde", Snackbar.LENGTH_LONG)
                                .setAction("Reintentar", {
                                    checkSession()
                                })
                                .show()
                    }
                })
        VolleySingleton.getInstance().addToRequestQueue(request, this)
        loading.visibility = View.VISIBLE
        form.visibility = View.GONE
    }

    private fun setTos() {
        val serviceUrl = getString(R.string.tos_url)
        val url = getString(R.string.host, serviceUrl)

        val request = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener<JSONObject> { response ->
                    Log.i("user", response.toString())
                    tos = response.getString("terminos")
                    conection = response.getString("condiciones")
                    privacy = response.getString("politica")

                },
                Response.ErrorListener { error ->
                    Log.e("error", error.message)
                })
        VolleySingleton.getInstance().addToRequestQueue(request, this)
    }
}
