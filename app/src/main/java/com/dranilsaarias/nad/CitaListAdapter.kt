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
            holder.date.text = holder.date.context.getString(R.string.no_date)
        } else {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val monthFormatter = SimpleDateFormat("dd MMMM y", Locale.getDefault())
            val date = parser.parse(d)
            holder.date.text = monthFormatter.format(date).capitalize()
        }

        if (!shouldShowDate || (position > 0 && citas!!.getJSONObject(position - 1).getString("fecha").equals(d))) {
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
        val hour: String
        if (s.equals(JSONObject.NULL) || s.equals(null) || s.equals("null")) {
            hour = "Sin hora"
        } else {
            hour = s + " - " + e
        }

        var medico = holder.dateDetails.context.getString(R.string.doctor_name)
        if (!cita.getString("medico").equals(JSONObject.NULL) && !cita.getString("medico").equals("null") && !cita.getString("medico").equals(null)) {
            medico = cita!!.getString("medico")
        }

        val paciente = cita.getString("paciente__nombre")

        if (isPacient) {
            holder.dateDetails.text = holder.dateDetails.context.getString(R.string.cita_descripcion, motivo, modalidad, hour, medico)
        } else {
            holder.dateDetails.text = holder.dateDetails.context.getString(R.string.cita_descripcion_medico, motivo, modalidad, hour, paciente)
        }

        if (cita.getInt("procedimiento__modalidad") != AgendarActivity.Type.IN_PERSON && isPacient && !cita.getBoolean("pago") && cita.getInt("estado") == 1) {
            holder.noPago.setText(R.string.cita_no_paga)
        } else {
            holder.noPago.setText(R.string.cita_paga)
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

    interface onCitaClickListener {
        fun onClick(cita: JSONObject)
        fun onCallClick(cita: JSONObject)
    }
}