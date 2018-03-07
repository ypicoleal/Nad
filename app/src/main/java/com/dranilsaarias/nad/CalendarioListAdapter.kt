package com.dranilsaarias.nad

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class CalendarioListAdapter : RecyclerView.Adapter<CalendarioListAdapter.CalendarioViewHolder>() {

    private var calendarios: List<JSONObject>? = null
    lateinit var calendarClickListener: OnCalendarClickListener


    fun setCalendarios(calendarios: JSONArray, restricciones: JSONArray? = null) {
        val newArray = mutableListOf<JSONObject>()
        for (j in 0 until calendarios.length()) {
            val calendario = calendarios.getJSONObject(j)
            newArray.add(calendario)
        }
        if (restricciones != null && restricciones != JSONObject.NULL) {
            val tempArray = mutableListOf<JSONObject>()
            tempArray.addAll(newArray)
            for (i in 0 until restricciones.length()) {
                val  restriccion = restricciones.getJSONObject(i)
                Log.e("restriccion", restriccion.toString())
                for (calendario in tempArray) {
                    val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val start = parser.parse(calendario.getString("start"))
                    val calendar = Calendar.getInstance()
                    calendar.time = start
                    val hour = parser.parse(calendario.getString("start").split(" ")[0] + " " + restriccion.getString("hora"))
                    Log.e("dias", "${restriccion.getInt("dia")} ${calendar.get(Calendar.DAY_OF_WEEK) - 2}")
                    if (restriccion.getInt("dia") == calendar.get(Calendar.DAY_OF_WEEK) - 2) {
                        Log.e("dias", "$hour")
                        if (start >= hour) {
                            newArray.remove(calendario)
                        }
                    }
                }
            }

        }
        this.calendarios = newArray
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: CalendarioViewHolder?, position: Int) {
        calendarios?.get(position)?.let { calendario ->
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val start = parser.parse(calendario.getString("start"))
            val end = parser.parse(calendario.getString("end"))
            val hourFomatter = SimpleDateFormat("h:mm a", Locale.getDefault())
            val name = hourFomatter.format(start) + " - " + hourFomatter.format(end)

            holder?.title?.text = name
            holder?.itemView!!.setOnClickListener {
                calendarClickListener.onClick(calendario)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CalendarioViewHolder {
        val inflater = LayoutInflater.from(parent!!.context)
        val view = inflater.inflate(R.layout.horario, parent, false)
        return CalendarioViewHolder(view)
    }

    override fun getItemCount(): Int {
        return calendarios?.size ?: 0
    }

    class CalendarioViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        var title: TextView = itemView!!.findViewById(R.id.title)
    }

    interface OnCalendarClickListener {
        fun onClick(calendario: JSONObject)
    }
}