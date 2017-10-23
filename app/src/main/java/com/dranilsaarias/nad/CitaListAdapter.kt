package com.dranilsaarias.nad

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject

class CitaListAdapter : RecyclerView.Adapter<CitaListAdapter.CitasViewHolder>() {

    private var citas: JSONArray? = null
    lateinit var calendarClickListener: onCitaClickListener


    fun setCitas(calendarios: JSONArray) {
        this.citas = calendarios
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: CitasViewHolder?, position: Int) {

    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CitasViewHolder {
        val inflater = LayoutInflater.from(parent!!.context)
        val view = inflater.inflate(R.layout.cita, parent, false)
        return CitasViewHolder(view)
    }

    override fun getItemCount(): Int {
        if (citas == null) {
            return 0
        }
        return citas!!.length()
    }

    class CitasViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        var date: TextView = itemView!!.findViewById(R.id.date)
    }

    interface onCitaClickListener {
        fun onClick(cita: JSONObject)
    }
}