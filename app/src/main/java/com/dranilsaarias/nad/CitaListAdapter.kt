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
    lateinit var calendarClickListener: OnCitaClickListener
    var shouldShowDate: Boolean = true
    var isPacient: Boolean = true


    fun setCitas(calendarios: JSONArray) {
        this.citas = calendarios
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: CitasViewHolder?, position: Int) {
        val cita =
                if (isPacient) {
                    citas!!.getJSONObject(position)
                } else {
                    citas!!.getJSONObject(citas!!.length() - position - 1)
                }

        if (cita.getInt("procedimiento__modalidad") == AgendarActivity.Type.IN_PERSON || isPacient || !cita.getBoolean("pago") || cita.getInt("estado") != 1) {
            holder!!.callBtn.visibility = View.GONE
        } else {
            holder!!.callBtn.visibility = View.VISIBLE
        }

        holder.dateState.text = holder.dateState.context.getString(R.string.date_state, cita.getString("estado_nombre"))
        when {
            cita.getInt("estado") == 1 -> {
                holder.dateState.setTextColor(ContextCompat.getColor(holder.dateState.context, R.color.citaVigente))
                holder.stateIndicator.setImageResource(R.drawable.cita_vigente)
            }
            cita.getInt("estado") == 2 -> {
                holder.dateState.setTextColor(ContextCompat.getColor(holder.dateState.context, R.color.citaCancelada))
                holder.stateIndicator.setImageResource(R.drawable.cita_cancelada)
            }
            else -> {
                holder.dateState.setTextColor(ContextCompat.getColor(holder.dateState.context, R.color.citaExpirada))
                holder.stateIndicator.setImageResource(R.drawable.cita_expirada)
            }
        }

        val d = cita.getString("fecha")
        if (d == JSONObject.NULL || d == null || d == "null") {
            holder.date.text = holder.date.context.getString(R.string.no_date)
        } else {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val monthFormatter = SimpleDateFormat("dd MMMM y", Locale.getDefault())
            val date = parser.parse(d)
            holder.date.text = monthFormatter.format(date).capitalize()
        }

        if (!shouldShowDate || (position > 0 && citas!!.getJSONObject(position - 1).getString("fecha") == d)) {
            holder.date.visibility = View.GONE
        } else {
            holder.date.visibility = View.VISIBLE
        }

        val motivo = cita.getString("procedimiento__nombre")
        var modalidad = holder.dateDetails.context.getText(R.string.atenci_n_online)
        if (cita.getInt("procedimiento__modalidad") == AgendarActivity.Type.IN_PERSON) {
            modalidad = holder.dateDetails.context.getText(R.string.atenci_n_consultorio)
        }

        val s = cita.getString("inicio")
        val e = cita.getString("fin")
        val hour = if (s == JSONObject.NULL || s == null || s == "null") {
            "Sin hora"
        } else {
            "$s - $e"
        }

        var medico = holder.dateDetails.context.getString(R.string.doctor_name)
        if (cita.getString("medico") != JSONObject.NULL && cita.getString("medico") != "null" && cita.getString("medico") != null) {
            medico = cita!!.getString("medico")
        }

        val paciente = cita.getString("paciente__nombre")
        val entidad = cita.getJSONObject("entidad_medica").getString("nombre")

        if (isPacient) {
            holder.dateDetails.text = holder.dateDetails.context.getString(R.string.cita_descripcion, motivo, modalidad, hour, medico)
        } else {
            holder.dateDetails.text = holder.dateDetails.context.getString(R.string.cita_descripcion_medico, motivo, modalidad, hour, paciente, entidad)
        }

        if (cita.getInt("procedimiento__modalidad") != AgendarActivity.Type.IN_PERSON && isPacient) {
            holder.noPago.visibility = View.VISIBLE
            if (!cita.getBoolean("pago") && cita.getInt("estado") == 1){
                holder.noPago.setText(R.string.cita_no_paga)
            } else {
                holder.noPago.setText(R.string.cita_paga)
            }
        } else{
            holder.noPago.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            calendarClickListener.onClick(cita)
        }

        holder.callBtn.setOnClickListener {
            calendarClickListener.onCallClick(cita)
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
        var dateDetails: TextView = itemView!!.findViewById(R.id.date_details)
        var noPago: TextView = itemView!!.findViewById(R.id.no_pago)
    }

    interface OnCitaClickListener {
        fun onClick(cita: JSONObject)
        fun onCallClick(cita: JSONObject)
    }
}