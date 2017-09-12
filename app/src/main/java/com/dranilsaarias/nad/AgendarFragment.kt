package com.dranilsaarias.nad


import android.content.ContentValues.TAG
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import java.text.SimpleDateFormat
import java.util.*


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

        setupCalendarView(
                view.findViewById(R.id.compactcalendar_view),
                view.findViewById(R.id.calendar_indicator),
                view.findViewById(R.id.calendar_prev),
                view.findViewById(R.id.calendar_next))

        return view
    }

    private fun setupCalendarView(calendarView: CompactCalendarView, calendarIndicator: TextView, prev: View, next: View) {

        val date = calendarView.firstDayOfCurrentMonth
        val monthFormatter = SimpleDateFormat("MMMM / y", Locale.getDefault())

        calendarIndicator.setText(monthFormatter.format(date).capitalize())

        val names = Array<String>(7, { _ -> "" })
        names[0] = "Lun" // monday
        names[1] = "Mar" // tuesday
        names[2] = "Mie" // wednesday
        names[3] = "Jue" // thursday
        names[4] = "Vie" // friday
        names[5] = "Sab" // saturday
        names[6] = "Dom" // sunday
        calendarView.setDayColumnNames(names)

        val ev1 = Event(ContextCompat.getColor(context, R.color.colorAccent), 1505339010000L, "Some extra data that I want to store.")
        calendarView.addEvent(ev1, false)

        // Added event 2 GMT: Sun, 07 Jun 2015 19:10:51 GMT
        val ev2 = Event(ContextCompat.getColor(context, R.color.colorAccent), 1505339010000L)
        calendarView.addEvent(ev2, false)

        calendarView.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                val events = calendarView.getEvents(dateClicked)
                Log.d(TAG, "Day was clicked: $dateClicked with events $events")
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                calendarIndicator.setText(monthFormatter.format(firstDayOfNewMonth).capitalize())
                Log.d(TAG, "Month was scrolled to: " + firstDayOfNewMonth)
            }
        })

        prev.setOnClickListener {
            calendarView.showPreviousMonth()
        }

        next.setOnClickListener {
            calendarView.showNextMonth()
        }
    }

}// Required empty public constructor
