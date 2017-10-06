package com.dranilsaarias.nad

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment(), OnMapReadyCallback, BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var mMap: GoogleMap

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_maps, container, false)

        val mapFragment = childFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val bottomNavigation = view.findViewById<BottomNavigationView>(R.id.navigation)
        bottomNavigation.setOnNavigationItemSelectedListener(this)

        return view
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_web -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.web_url)))
                startActivity(browserIntent)
            }

            R.id.navigation_call -> {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.setData(Uri.parse("tel:" + getString(R.string.phone)))
                startActivity(intent)
            }

            R.id.navigation_message -> {
                val stringArray = arrayOfNulls<String>(1)
                stringArray[0] = getString(R.string.doctor_email)

                val intent = Intent(Intent.ACTION_SENDTO)
                intent.setData(Uri.parse("mailto:"))
                intent.putExtra(Intent.EXTRA_EMAIL, stringArray)
                startActivity(Intent.createChooser(intent, "Envio de mensaje"))
            }
        }
        return false
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(4.6949137, -74.0363918)
        val adapter = CustomInfoWindowAdapter(this.activity)
        mMap.setInfoWindowAdapter(adapter)
        mMap.addMarker(MarkerOptions()
                .position(sydney)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin)))
                .showInfoWindow()
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 17f))

    }
}
