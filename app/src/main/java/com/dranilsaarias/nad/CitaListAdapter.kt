package com.dranilsaarias.nad

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class CitaListAdapter : RecyclerView.Adapter<CitaListAdapter.CitasViewHolder>() {

    private var citas: JSONArray? = null
    lateinit var calendarClickListener: onCitaClickListener


    fun setCitas(calendarios: JSONArray) {
        this.citas = calendarios
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: CitasViewHolder?, position: Int) {
        val cita = citas!!.getJSONObject(position)
        if (cita.getInt("procedimiento__modalidad") == AgendarActivity.Type.IN_PERSON) {
            holder!!.callBtn.visibility = View.GONE
        } else {
            holder!!.callBtn.visibility = View.VISIBLE
        }

        holder.dateState.text = "Estado cita: " + cita.getString("estado_nombre")
        if (cita.getInt("estado") == 1) {
            holder.dateState.setTextColor(ContextCompat.getColor(holder.dateState.context, R.color.citaVigente))
            holder.stateIndicator.setImageResource(R.drawable.cita_vigente)
        } else if (cita.getInt("estado") == 2) {
            holder.dateState.setTextColor(ContextCompat.getColor(holder.dateState.context, R.color.citaCancelada))
            holder.stateIndicator.setImageResource(R.drawable.cita_cancelada)
        } else {
            holder.dateState.setTextColor(ContextCompat.getColor(holder.dateState.context, R.color.citaExpirada))
            holder.stateIndicator.setImageResource(R.drawable.cita_expirada)
        }

        val d = cita.getString("fecha")
        if (d.equals(null) || d.equals("null")) {
            holder.date.text = "Sin fecha"
        } else {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val monthFormatter = SimpleDateFormat("dd MMMM y", Locale.getDefault())
            val date = parser.parse(d)
            holder.date.text = monthFormatter.format(date).capitalize()
        }

        if (position > 0 && citas!!.getJSONObject(position - 1).getString("fecha").equals(d)) {
            holder.date.visibility = View.GONE
        } else {
            holder.date.visibility = View.VISIBLE
        }
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
        var callBtn: ImageView = itemView!!.findViewById(R.id.call_btn)
        var dateState: TextView = itemView!!.findViewById(R.id.date_state)
        var stateIndicator: ImageView = itemView!!.findViewById(R.id.state_indicator)
    }

    interface onCitaClickListener {
        fun onClick(cita: JSONObject)
    }
}