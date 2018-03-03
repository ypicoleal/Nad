package com.dranilsaarias.nad


import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
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

    private var timer: Timer? = null
    private var validSnackbar: Snackbar? = null
    private var isPaciente: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            isPaciente = arguments.getBoolean(ARG_PACIENTE, true)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_agendar, container, false)

        val imageSlider = view.findViewById<ViewPager>(R.id.img_slider)

        setupAdsPager(imageSlider)

        val indicator = view.findViewById<TabLayout>(R.id.img_slider_indicator)
        indicator.setupWithViewPager(imageSlider)

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

                    val handler = Handler()
                    val update = Runnable {
                        if (adsPager.currentItem == (adapter.data!!.length()) - 1) {
                            adsPager.setCurrentItem(0, true)
                        } else {
                            adsPager.setCurrentItem((adsPager.currentItem + 1), true)
                        }
                    }

                    timer = Timer() // This will create a new Thread
                    timer!!.schedule(object : TimerTask() { // task to be scheduled

                        override fun run() {
                            handler.post(update)
                        }
                    }, DELAY_MS, PERIOD_MS)
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
                Log.d(TAG, "Month was scrolled to: $firstDayOfNewMonth")
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
        if (isPaciente) {
            val today = Calendar.getInstance()
            if (date > today.time) {
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
            } else {
                if (validSnackbar == null) {
                    validSnackbar = Snackbar.make(view!!, "La cita médica solo se puede agendar para días posteriores la fecha de hoy.", Snackbar.LENGTH_LONG)
                }
                validSnackbar!!.show()
            }
        } else {
            val intent = Intent(context, DoctorCalendarActivity::class.java)
            intent.putExtra("date", date)
            startActivity(intent)
        }
    }

    companion object {
        private const val ARG_PACIENTE = "paciente"
        private const val DELAY_MS: Long = 500//delay in milliseconds before task is to be executed
        private const val PERIOD_MS: Long = 8000 // time in milliseconds between successive task executions.


        fun newInstance(isPaciente: Boolean): AgendarFragment {
            val fragment = AgendarFragment()
            val args = Bundle()
            args.putBoolean(ARG_PACIENTE, isPaciente)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
