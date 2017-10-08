package com.dranilsaarias.nad

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class CalendarioListAdapter : RecyclerView.Adapter<CalendarioListAdapter.CalendarioViewHolder>() {

    private var calendarios: JSONArray? = null
    lateinit var calendarClickListener: onCalendarClickListener


    fun setCalendarios(calendarios: JSONArray) {
        this.calendarios = calendarios
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: CalendarioViewHolder?, position: Int) {
        val calendario = calendarios!!.getJSONObject(position)

        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val start = parser.parse(calendario.getString("start"))
        val end = parser.parse(calendario.getString("end"))
        val hourFomatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        val name = hourFomatter.format(start) + " - " + hourFomatter.format(end)

        holder!!.title.text = name
        holder.itemView!!.setOnClickListener {
            calendarClickListener.onClick(calendario)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CalendarioViewHolder {
        val inflater = LayoutInflater.from(parent!!.context)
        val view = inflater.inflate(R.layout.horario, parent, false)
        return CalendarioViewHolder(view)
    }

    override fun getItemCount(): Int {
        if (calendarios == null) {
            return 0
        }
        return calendarios!!.length()
    }

    class CalendarioViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        var title: TextView = itemView!!.findViewById(R.id.title)
    }

    interface onCalendarClickListener {
        fun onClick(calendario: JSONObject)
    }
}