package com.dranilsaarias.nad

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.widget.TextView

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter(private val context: Activity, private val direccion: String) : GoogleMap.InfoWindowAdapter {

    @SuppressLint("InflateParams")
    override fun getInfoWindow(marker: Marker): View {
        val view = context.layoutInflater.inflate(R.layout.custominfowindow, null)
        val direccion = view.findViewById<TextView>(R.id.direccion)
        direccion.text = this.direccion
        return view
    }

    override fun getInfoContents(marker: Marker): View? {
        return null
    }
}
