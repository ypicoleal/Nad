package com.dranilsaarias.nad


import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


/**
 * A simple [Fragment] subclass.
 */
class AgendarFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_agendar, container, false)

        val imageSlider = view.findViewById<ViewPager>(R.id.img_slider)
        val adapter = ImageFragmenPagerAdapter(childFragmentManager)
        imageSlider.adapter = adapter

        val indicator = view.findViewById<TabLayout>(R.id.img_slider_indicator)
        indicator.setupWithViewPager(imageSlider)

        return view
    }

}// Required empty public constructor
