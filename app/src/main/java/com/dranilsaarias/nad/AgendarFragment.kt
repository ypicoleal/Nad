package com.dranilsaarias.nad


import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import org.json.JSONObject
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

        setupAdsPager(imageSlider)

        val indicator = view.findViewById<TabLayout>(R.id.img_slider_indicator)
        indicator.setupWithViewPager(imageSlider)

        //todo validar no escojer un dia menor al dia de mañana

        setupCalendarView(
                view.findViewById(R.id.compactcalendar_view),
                view.findViewById(R.id.calendar_indicator),
                view.findViewById(R.id.calendar_prev),
                view.findViewById(R.id.calendar_next))

        return view
    }

    private fun setupAdsPager(adsPager: ViewPager) {

        val serviceUrl = getString(R.string.publicidad)
        val url = getString(R.string.host, serviceUrl)

        val request = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener<JSONObject> { response ->
                    Log.e("tales", response.toString())
                    val adapter = ImageFragmenPagerAdapter(childFragmentManager)
                    adapter.data = response.getJSONArray("object_list")
                    adsPager.adapter = adapter
                },
                Response.ErrorListener { _ ->

                })
        VolleySingleton.getInstance().addToRequestQueue(request, context)
    }

    private fun setupCalendarView(calendarView: CompactCalendarView, calendarIndicator: TextView, prev: View, next: View) {

        val date = calendarView.firstDayOfCurrentMonth
        val monthFormatter = SimpleDateFormat("MMMM / y", Locale.getDefault())

        calendarIndicator.text = monthFormatter.format(date).capitalize()

        val names = Array(7, { _ -> "" })
        names[0] = "Lun" // monday
        names[1] = "Mar" // tuesday
        names[2] = "Mie" // wednesday
        names[3] = "Jue" // thursday
        names[4] = "Vie" // friday
        names[5] = "Sab" // saturday
        names[6] = "Dom" // sunday
        calendarView.setDayColumnNames(names)

        calendarView.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                agendar(dateClicked)
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                calendarIndicator.text = monthFormatter.format(firstDayOfNewMonth).capitalize()
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

    private fun agendar(date: Date) {
        AlertDialog.Builder(activity)
                .setTitle(getString(R.string.choose_entidad))
                .setItems(R.array.entidad_array, { _, index ->
                    val intent = Intent(context, AgendarActivity::class.java)
                    intent.putExtra("date", date)
                    intent.putExtra("entidad", index + 1)
                    startActivity(intent)
                })
                .setNegativeButton("Cancelar", { _, _ ->

                })
                .create()
                .show()
    }

}// Required empty public constructor
