package com.dranilsaarias.nad


import android.app.Activity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.fragment_comments.*
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class CommentsFragment : Fragment() {

    private var mContent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mContent = arguments.getString(ARG_CONTENT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_comments, container, false)

        val mail = view.findViewById<TextInputEditText>(R.id.mail)
        mail.setText(mContent)

        view.findViewById<CardView>(R.id.send_btn).setOnClickListener {
            send()
        }

        return view
    }

    private fun send() {

        val serviceUrl = getString(R.string.comentarios_form)
        val url = getString(R.string.host, serviceUrl)

        val request = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    Log.e("tales", response)
                    loading.visibility = View.GONE
                    comentario.setText("")
                    Snackbar.make(loading, "Mensaje enviado con exito", Snackbar.LENGTH_LONG).show()
                },
                Response.ErrorListener { error ->
                    loading.visibility = View.GONE
                    if (error.networkResponse != null) {
                        Log.e("error", String(error.networkResponse.data))
                        //
                    }
                    Snackbar.make(loading, "Al parecer hubo un error en la peticion intentelo nuevamente mas tarde", Snackbar.LENGTH_LONG).show()
                }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params.put("email", mail.text.toString())
                params.put("comentario", comentario.text.toString())

                return params
            }
        }
        VolleySingleton.getInstance().addToRequestQueue(request, context)
        loading.visibility = View.VISIBLE
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    companion object {
        private val ARG_CONTENT = "content"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @param content Parameter 1.
         * *
         * *
         * @return A new instance of fragment CommentsFragment.
         */
        fun newInstance(content: String): CommentsFragment {
            val fragment = CommentsFragment()
            val args = Bundle()
            args.putString(ARG_CONTENT, content)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
