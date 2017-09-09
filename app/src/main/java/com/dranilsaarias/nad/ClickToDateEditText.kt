package com.dranilsaarias.nad

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Canvas
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ClickToDateEditText : AppCompatEditText {
    internal var date: Calendar? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        isFocusable = false
        isClickable = true
        isLongClickable = false
        configureOnClickListener()
    }

    private fun configureOnClickListener() {
        if (date == null) {
            date = Calendar.getInstance()
        }

        setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                    context,
                    DatePickerDialog.OnDateSetListener { datePicker, year, monthOfYear, dayOfMonth ->
                        date!!.set(year, monthOfYear, dayOfMonth)
                        val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                        setText(format.format(date!!.time))
                    },
                    date!!.get(Calendar.YEAR),
                    date!!.get(Calendar.MONTH),
                    date!!.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }
    }
}