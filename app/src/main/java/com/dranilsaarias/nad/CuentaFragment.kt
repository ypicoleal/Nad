package com.dranilsaarias.nad


import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
    lateinit var email: TextInputEditText

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


        nombre.setText(accountData!!.getString("nombre"))
        apellidos.setText(accountData!!.getString("apellidos"))
        nacimiento.setText(accountData!!.getString("fecha_nacimiento"))
        civil.setText(accountData!!.getString("estado_civil"))
        profesion.setText(accountData!!.getString("profesion"))
        direccion.setText(accountData!!.getString("direccion"))
        celular.setText(accountData!!.getString("telefono"))
        email.setText(accountData!!.getString("email"))

        //TODO hacer que esta verguis guarde
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
