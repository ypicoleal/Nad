package com.dranilsaarias.nad

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.content_forgot_password.*
import org.json.JSONArray
import java.util.*

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        send_btn.setOnClickListener {
            send()
        }
    }


    private fun send() {
        clearErrors()
        if (!validateForm()) {
            return
        }

        val serviceUrl = getString(R.string.forgot_password_form)
        val url = getString(R.string.host, serviceUrl)

        val request = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    Log.e("tales", response)
                    loading.visibility = View.GONE
                    showSuccess()
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
                params.put("username", document.text.toString())
                params.put("newPassword1", new_passsoword1.text.toString())
                params.put("newPassword2", new_passsoword2.text.toString())
                params.put("email", email.text.toString())
                return params
            }
        }
        VolleySingleton.getInstance().addToRequestQueue(request, this)
        loading.visibility = View.VISIBLE
    }

    private fun showErrors(serverResponse: String) {
        Log.e("errors", serverResponse)
        val errors = JSONArray(serverResponse)

        for (i in 0 until errors.length()) {
            val error = errors.getJSONArray(i)
            val errorStr = error.getJSONArray(1).getString(0)
            val errorField = error.getString(0)
            var container: TextInputLayout? = null

            when {
                errorField == "username" -> container = document_container
                errorField == "email" -> container = email_container
                errorField == "newPassword1" -> container = new_passsoword1_container
                errorField == "newPassword2" -> container = new_passsoword2_container
            }
            container!!.isErrorEnabled = true
            container.error = errorStr
        }
    }

    private fun validateForm(): Boolean {
        var clean = true
        if (new_passsoword1.text.toString() == "") {
            new_passsoword1_container.isErrorEnabled = true
            new_passsoword1_container.error = getString(R.string.required_field)
            clean = false
        }
        if (new_passsoword2.text.toString() == "") {
            new_passsoword2_container.isErrorEnabled = true
            new_passsoword2_container.error = getString(R.string.required_field)
            clean = false
        }

        if (email.text.toString() == "") {
            email_container.isErrorEnabled = true
            email_container.error = getString(R.string.required_field)
            clean = false
        }
        if (document.text.toString() == "") {
            document_container.isErrorEnabled = true
            document_container.error = getString(R.string.required_field)
            clean = false
        }
        return clean
    }

    private fun clearErrors() {
        email_container.isErrorEnabled = false
        email_container.error = null
        new_passsoword1_container.isErrorEnabled = false
        new_passsoword1_container.error = null
        new_passsoword2_container.isErrorEnabled = false
        new_passsoword2_container.error = null
        document_container.error = null
        document_container.isErrorEnabled = false
    }

    @SuppressLint("InflateParams")
    private fun showSuccess() {
        val inflater = this.layoutInflater
        val v = inflater.inflate(R.layout.check_email, null)

        AlertDialog
                .Builder(this)
                .setCancelable(false)
                .setView(v)
                .setPositiveButton("Aceptar", { dialog, _ ->
                    dialog.dismiss()
                    finish()
                })
                .create()
                .show()

        v.findViewById<TextView>(R.id.text).setText(R.string.passwor_recover_text)
    }

}
