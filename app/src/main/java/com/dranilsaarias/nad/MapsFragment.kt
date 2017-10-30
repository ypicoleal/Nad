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
    private var mContent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mContent = arguments.getString(ARG_CONTENT)
        }
    }

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


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(4.6949137, -74.0363918)
        val adapter = CustomInfoWindowAdapter(this.activity, mContent!!)
        mMap.setInfoWindowAdapter(adapter)
        mMap.addMarker(MarkerOptions()
                .position(sydney)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin)))
                .showInfoWindow()
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 17f))

    }

    companion object {
        private val ARG_CONTENT = "content"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @param content Parameter 1.
         * *
         * *
         * @return A new instance of fragment MapsFragment
         */
        fun newInstance(content: String): MapsFragment {
            val fragment = MapsFragment()
            val args = Bundle()
            args.putString(ARG_CONTENT, content)
            fragment.arguments = args
            return fragment
        }
    }
}
