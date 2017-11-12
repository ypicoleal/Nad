package com.dranilsaarias.nad


import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import org.json.JSONArray
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 * Use the [CuentaFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CuentaFragment : Fragment() {

    lateinit var nombre: TextInputEditText
    lateinit var apellidos: TextInputEditText
    lateinit var nacimiento: EditText
    lateinit var civil: EditText
    lateinit var profesion: TextInputEditText
    lateinit var direccion: TextInputEditText
    lateinit var celular: TextInputEditText
    lateinit var nombre_acudiente: TextInputEditText
    lateinit var cedula_acudiente: TextInputEditText
    lateinit var email: TextInputEditText
    lateinit var saveBtn: CardView
    lateinit var loading: FrameLayout
    lateinit var setPassword: TextView

    lateinit var fecha_nacimiento_container: TextInputLayout
    lateinit var civil_container: TextInputLayout
    lateinit var nombre_acudiente_container: TextInputLayout
    lateinit var cedula_acudiente_container: TextInputLayout
    lateinit var nombre_container: TextInputLayout
    lateinit var apellidos_container: TextInputLayout
    lateinit var email_container: TextInputLayout
    lateinit var profesion_container: TextInputLayout
    lateinit var celular_container: TextInputLayout
    lateinit var direccion_container: TextInputLayout

    private var password: TextInputEditText? = null
    private var newPassword1: TextInputEditText? = null
    private var newPassword2: TextInputEditText? = null
    private var password_loading: ProgressBar? = null
    private var new_passsoword1_container: TextInputLayout? = null
    private var new_passsoword2_container: TextInputLayout? = null
    private var password_container: TextInputLayout? = null

    private var accountData: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            accountData = JSONObject(arguments.getString(ARG_DATA))
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater!!.inflate(R.layout.fragment_cuenta, container, false)
        setup(v!!)
        return v
    }

    private fun setup(v: View) {
        nombre = v.findViewById(R.id.nombre)
        apellidos = v.findViewById(R.id.apellidos)
        nacimiento = v.findViewById(R.id.fecha_nacimiento)
        civil = v.findViewById(R.id.civil)
        profesion = v.findViewById(R.id.profesion)
        direccion = v.findViewById(R.id.direccion)
        celular = v.findViewById(R.id.celular)
        email = v.findViewById(R.id.email)
        saveBtn = v.findViewById(R.id.save_btn)
        nombre_acudiente = v.findViewById(R.id.nombre_acudiente)
        cedula_acudiente = v.findViewById(R.id.cedula_acudiente)
        setPassword = v.findViewById(R.id.set_password)

        fecha_nacimiento_container = v.findViewById(R.id.fecha_nacimiento_container)
        civil_container = v.findViewById(R.id.civil_container)
        nombre_acudiente_container = v.findViewById(R.id.nombre_acudiente_container)
        cedula_acudiente_container = v.findViewById(R.id.cedula_acudiente_container)
        nombre_container = v.findViewById(R.id.nombre_container)
        apellidos_container = v.findViewById(R.id.apellidos_container)
        email_container = v.findViewById(R.id.email_container)
        profesion_container = v.findViewById(R.id.profesion_container)
        celular_container = v.findViewById(R.id.celular_container)
        direccion_container = v.findViewById(R.id.direccion_container)
        loading = v.findViewById(R.id.loading)

        nombre.setText(accountData!!.getString("nombre"))
        apellidos.setText(accountData!!.getString("apellidos"))
        nacimiento.setText(accountData!!.getString("fecha_nacimiento"))
        civil.setText(accountData!!.getString("estado_civil"))
        profesion.setText(accountData!!.getString("profesion"))
        direccion.setText(accountData!!.getString("direccion"))
        celular.setText(accountData!!.getString("telefono"))
        email.setText(accountData!!.getString("email"))
        if (accountData!!.get("acudiente") != JSONObject.NULL) {
            nombre_acudiente.setText(accountData!!.getString("acudiente"))
            nombre_acudiente_container.visibility = View.VISIBLE
        }
        if (accountData!!.get("cedula_acudiente") != JSONObject.NULL) {
            cedula_acudiente.setText(accountData!!.getString("cedula_acudiente"))
            cedula_acudiente_container.visibility = View.VISIBLE
        }
        saveBtn.setOnClickListener {
            register()
        }

        setPassword.setOnClickListener {
            openSetPassword()
        }
    }

    @SuppressLint("InflateParams")
    private fun openSetPassword() {
        val inflater = this.layoutInflater
        val v = inflater.inflate(R.layout.set_password, null)
        val dialog = AlertDialog.Builder(context)
                .setView(v)
                .setCancelable(false)
                .setPositiveButton("Cambiar contraseña", { _, _ ->
                })
                .setNegativeButton("Cerrar", { _, _ ->
                })
                .create()
        password = v.findViewById(R.id.password)
        newPassword1 = v.findViewById(R.id.new_passsoword1)
        newPassword2 = v.findViewById(R.id.new_passsoword2)
        password_loading = v.findViewById(R.id.password_loading)
        password_container = v.findViewById(R.id.password_container)
        new_passsoword1_container = v.findViewById(R.id.new_passsoword1_container)
        new_passsoword2_container = v.findViewById(R.id.new_passsoword2_container)
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            changePassword(dialog)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).visibility = View.INVISIBLE
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).visibility = View.INVISIBLE
        }
    }

    private fun changePassword(dialog: AlertDialog) {
        val serviceUrl = getString(R.string.change_password_form)
        val url = getString(R.string.host, serviceUrl)

        val request = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    Log.e("tales", response)
                    dialog.dismiss()
                    showPasswordSuccess()
                },
                Response.ErrorListener { error ->
                    password_loading!!.visibility = View.GONE
                    password_container!!.visibility = View.VISIBLE
                    new_passsoword1_container!!.visibility = View.VISIBLE
                    new_passsoword2_container!!.visibility = View.VISIBLE
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).visibility = View.VISIBLE
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).visibility = View.VISIBLE
                    if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                        showPasswordErrors(String(error.networkResponse.data))
                    } else {
                        Snackbar.make(view!!, "Al parecer hubo un error en la peticion intentelo nuevamente mas tarde", Snackbar.LENGTH_LONG).show()
                    }
                }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params.put("old_password", password!!.text.toString())
                params.put("new_password1", newPassword1!!.text.toString())
                params.put("new_password2", newPassword2!!.text.toString())
                return params
            }
        }
        VolleySingleton.getInstance().addToRequestQueue(request, context)
        password_loading!!.visibility = View.VISIBLE
        password_container!!.visibility = View.GONE
        password_container!!.error = null
        password_container!!.isErrorEnabled = false
        new_passsoword1_container!!.visibility = View.GONE
        new_passsoword1_container!!.error = null
        new_passsoword1_container!!.isErrorEnabled = false
        new_passsoword2_container!!.visibility = View.GONE
        new_passsoword2_container!!.error = null
        new_passsoword2_container!!.isErrorEnabled = false

    }

    private fun showPasswordSuccess() {
        AlertDialog.Builder(context)
                .setMessage(getString(R.string.password_set_success))
                .setPositiveButton("Cerrar", { _, _ ->
                })
                .create()
                .show()
    }

    private fun showPasswordErrors(serverResponse: String) {
        Log.e("errors", serverResponse)
        val errors = JSONArray(serverResponse)

        for (i in 0 until errors.length()) {
            val error = errors.getJSONArray(i)
            val errorStr = error.getJSONArray(1).getString(0)
            val errorField = error.getString(0)
            var container: TextInputLayout? = null

            when {
                errorField == "old_password" -> container = password_container
                errorField == "new_password1" -> container = new_passsoword1_container
                errorField == "new_password2" -> container = new_passsoword2_container
            }
            container!!.isErrorEnabled = true
            container.error = errorStr
        }
    }

    private fun register() {
        clearErrors()
        if (!validateForm()) {
            return
        }

        val serviceUrl = getString(R.string.cuenta_form, accountData!!.getInt("id"))
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
                        Snackbar.make(view!!, "Al parecer hubo un error en la peticion intentelo nuevamente mas tarde", Snackbar.LENGTH_LONG).show()
                    }
                }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params.put("first_name", nombre.text.toString())
                params.put("last_name", apellidos.text.toString())
                params.put("email", email.text.toString())
                params.put("fecha_nacimiento", nacimiento.text.toString())
                params.put("estado_civil", civil.text.toString())
                params.put("profesion", profesion.text.toString())
                params.put("telefono", celular.text.toString())
                params.put("nombre_a", nombre_acudiente.text.toString())
                params.put("cedula_a", cedula_acudiente.text.toString())
                params.put("direccion", direccion.text.toString())
                return params
            }
        }
        VolleySingleton.getInstance().addToRequestQueue(request, context)
        loading.visibility = View.VISIBLE
    }

    private fun validateForm(): Boolean {
        var clean = true
        if (nombre_acudiente.text.toString().equals("") && nombre_acudiente_container.visibility == View.VISIBLE) {
            nombre_acudiente_container.isErrorEnabled = true
            nombre_acudiente_container.error = getString(R.string.required_field)
            clean = false
        }
        if (cedula_acudiente.text.toString().equals("") && cedula_acudiente_container.visibility == View.VISIBLE) {
            cedula_acudiente_container.isErrorEnabled = true
            cedula_acudiente_container.error = getString(R.string.required_field)
            clean = false
        }


        if (nombre.text.toString().equals("")) {
            nombre_container.isErrorEnabled = true
            nombre_container.error = getString(R.string.required_field)
            clean = false
        }
        if (apellidos.text.toString().equals("")) {
            apellidos_container.isErrorEnabled = true
            apellidos_container.error = getString(R.string.required_field)
            clean = false
        }
        if (email.text.toString().equals("")) {
            email_container.isErrorEnabled = true
            email_container.error = getString(R.string.required_field)
            clean = false
        }
        if (nacimiento.text.toString().equals("")) {
            fecha_nacimiento_container.isErrorEnabled = true
            fecha_nacimiento_container.error = getString(R.string.required_field)
            clean = false
        }
        if (civil.text.toString().equals("")) {
            civil_container.isErrorEnabled = true
            civil_container.error = getString(R.string.required_field)
            clean = false
        }
        if (profesion.text.toString().equals("")) {
            profesion_container.isErrorEnabled = true
            profesion_container.error = getString(R.string.required_field)
            clean = false
        }
        if (celular.text.toString().equals("")) {
            celular_container.isErrorEnabled = true
            celular_container.error = getString(R.string.required_field)
            clean = false
        }
        return clean
    }

    private fun clearErrors() {
        nombre_container.isErrorEnabled = false
        nombre_container.error = null
        celular_container.isErrorEnabled = false
        celular_container.error = null
        apellidos_container.isErrorEnabled = false
        apellidos_container.error = null
        profesion_container.isErrorEnabled = false
        profesion_container.error = null
        fecha_nacimiento_container.error = null
        fecha_nacimiento_container.isErrorEnabled = false
        civil_container.error = null
        civil_container.isErrorEnabled = false
        email_container.error = null
        email_container.isErrorEnabled = false
        direccion_container.error = null
        direccion_container.isErrorEnabled = false
    }

    private fun showErrors(serverResponse: String) {
        Log.e("errors", serverResponse)
        val errors = JSONObject(serverResponse)

        if (errors.has("fecha_nacimiento")) {
            val error = errors.getJSONArray("fecha_nacimiento").getString(0)
            fecha_nacimiento_container.isErrorEnabled = true
            fecha_nacimiento_container.error = error
        }

        if (errors.has("estado_civil")) {
            val error = errors.getJSONArray("estado_civil").getString(0)
            civil_container.isErrorEnabled = true
            civil_container.error = error
        }
    }

    private fun showSuccess() {
        Snackbar.make(view!!, "Datos actualizados con exito", Snackbar.LENGTH_LONG).show()
    }

    companion object {

        private val ARG_DATA = "param1"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param data Parameter 1.
         * @return A new instance of fragment CuentaFragment.
         */
        fun newInstance(data: String): CuentaFragment {
            val fragment = CuentaFragment()
            val args = Bundle()
            args.putString(ARG_DATA, data)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
