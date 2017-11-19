package com.dranilsaarias.nad


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.google.firebase.iid.FirebaseInstanceId
import org.json.JSONObject
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class CitasFragment : Fragment(), CitaListAdapter.onCitaClickListener {

    val adapter = CitaListAdapter()

    private var date: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            adapter.isPacient = arguments.getBoolean(ARG_PACIENT, true)

            if (arguments.getSerializable(ARG_DATE) != null) {
                date = arguments.getSerializable(ARG_DATE) as Date
                adapter.shouldShowDate = false
                adapter.isPacient = false
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_citas, container, false)

        setupCitas(view.findViewById(R.id.citas_rv), view.findViewById(R.id.swipe), view.findViewById(R.id.empty_list))

        view.findViewById<SwipeRefreshLayout>(R.id.swipe).setOnRefreshListener {
            setupCitas(view.findViewById(R.id.citas_rv), view.findViewById(R.id.swipe), view.findViewById(R.id.empty_list))
        }

        adapter.calendarClickListener = this

        return view
    }

    override fun onClick(cita: JSONObject) {
        val intent = Intent(context, DateDetailsActivity::class.java)
        intent.putExtra("cita", cita.toString())
        intent.putExtra("isPacient", adapter.isPacient)
        startActivityForResult(intent, 1)
    }

    override fun onCallClick(cita: JSONObject) {
        if (cita.getInt("minutos") > 0) {
            val devices = cita.getJSONArray("devices")
            val intent = Intent(context, CallActivity::class.java)
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
                    .Builder(context)
                    .setTitle(R.string.app_name)
                    .setMessage("Tiempo de cita terminado para esta consulta.")
                    .setPositiveButton("Aceptar", null)
                    .create()
                    .show()
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
                params.put("Content-Type", "application/json; charset=UTF-8")
                params.put("Authorization", "key=AAAAQnVV4F0:APA91bFWJ-vz8oUSc3l_qDkiKdng1vodSlDkWVEX6paYl_dBRRslvcuOjjRzWwvpnd_fB-ayki5aCNesTxVxgksYb_bz_gifJ0TLNNHHmit_yDCXrmpjPQ6ZJ3595V7KNurzFX9B5CNb")
                return params
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val body = JSONObject()
                if (type.equals("ios")) {
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
        VolleySingleton.getInstance().addToRequestQueue(request, context)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            setupCitas(view!!.findViewById(R.id.citas_rv), view!!.findViewById(R.id.swipe), view!!.findViewById(R.id.empty_list))
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setupCitas(citasRV: RecyclerView, swipe: SwipeRefreshLayout, empty_list: TextView) {
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        citasRV.layoutManager = layoutManager
        citasRV.setHasFixedSize(true)

        citasRV.adapter = adapter

        var serviceUrl = getString(R.string.citas_list)
        if (date != null) {
            val c = Calendar.getInstance()
            c.time = date
            serviceUrl = getString(R.string.citas_list_date, c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
        }
        val url = getString(R.string.host, serviceUrl)

        Log.i("url", url)

        val request = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener<JSONObject> { response ->
                    Log.i("response", response.toString())
                    if (response.getJSONArray("object_list").length() == 0) {
                        empty_list.visibility = View.VISIBLE
                        swipe.visibility = View.GONE
                        if (!adapter.isPacient) {
                            empty_list.text = "No hay pacientes agendados para esta fecha"
                        }
                    }
                    adapter.setCitas(response.getJSONArray("object_list"))
                    swipe.isRefreshing = false
                },
                Response.ErrorListener { error ->
                    Log.i("error", error.toString())
                })
        VolleySingleton.getInstance().addToRequestQueue(request, context)
        swipe.isRefreshing = true
    }

    companion object {
        val ARG_DATE = "date"
        val ARG_PACIENT = "pacient"
    }
}// Required empty public constructor
