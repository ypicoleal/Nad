package com.dranilsaarias.nad

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import java.text.SimpleDateFormat
import java.util.*


class DoctorCalendarActivity : AppCompatActivity() {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * [FragmentPagerAdapter] derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    /**
     * The [ViewPager] that will host the section contents.
     */
    private var mViewPager: ViewPager? = null
    private var date: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_calendar)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        date = intent.getSerializableExtra("date") as Date


        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById<View>(R.id.container) as ViewPager
        mViewPager!!.adapter = mSectionsPagerAdapter

        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout

        tabLayout.setupWithViewPager(mViewPager)
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
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
}
