package com.dranilsaarias.nad


import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class DoctorCalendarFragment : Fragment() {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    private var mViewPager: ViewPager? = null
    private var date: Date? = null


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater!!.inflate(R.layout.fragment_doctor_calendar, container, false)

        mSectionsPagerAdapter = SectionsPagerAdapter(childFragmentManager)

        // Set up the ViewPager with the sections adapter.
        mViewPager = v.findViewById(R.id.container)
        mViewPager!!.adapter = mSectionsPagerAdapter

        date = Date()

        val tabLayout = v.findViewById<TabLayout>(R.id.tabs)

        tabLayout.setupWithViewPager(mViewPager)

        return v
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            val fragment = CitasFragment()
            val args = Bundle()
            if (position == 0) {
                args.putSerializable(CitasFragment.ARG_DATE, date)
            } else {
                val c = Calendar.getInstance()
                c.time = date
                c.add(Calendar.DATE, 1)
                args.putSerializable(CitasFragment.ARG_DATE, c.time)
            }
            fragment.arguments = args
            return fragment
        }

        override fun getPageTitle(position: Int): CharSequence {
            val monthFormatter = SimpleDateFormat("dd MMMM", Locale.getDefault())
            if (position == 0) {
                return monthFormatter.format(date)
            } else {
                val c = Calendar.getInstance()
                c.time = date
                c.add(Calendar.DATE, 1)
                return monthFormatter.format(c.time)
            }

        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 2
        }
    }

}// Required empty public constructor
