package com.dranilsaarias.nad


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
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
            date = arguments.getSerializable(ARG_DATE) as Date
            adapter.shouldShowDate = false
            adapter.isPacient = false
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_citas, container, false)

        setupCitas(view.findViewById(R.id.citas_rv), view.findViewById(R.id.swipe))

        view.findViewById<SwipeRefreshLayout>(R.id.swipe).setOnRefreshListener {
            setupCitas(view.findViewById(R.id.citas_rv), view.findViewById(R.id.swipe))
        }

        adapter.calendarClickListener = this

        return view
    }

    override fun onClick(cita: JSONObject) {
        val intent = Intent(context, DateDetailsActivity::class.java)
        intent.putExtra("cita", cita.toString())
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            setupCitas(view!!.findViewById(R.id.citas_rv), view!!.findViewById(R.id.swipe))
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setupCitas(citasRV: RecyclerView, swipe: SwipeRefreshLayout) {
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
                        //empty_list.visibility = View.VISIBLE
                        swipe.visibility = View.GONE
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
    }
}// Required empty public constructor
