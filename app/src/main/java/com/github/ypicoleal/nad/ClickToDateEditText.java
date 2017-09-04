package com.github.ypicoleal.nad;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ClickToDateEditText extends AppCompatEditText {
    Calendar date;

    public ClickToDateEditText(Context context) {
        super(context);
    }

    public ClickToDateEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickToDateEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setFocusable(false);
        setClickable(true);
        setLongClickable(false);
        configureOnClickListener();
    }

    private void configureOnClickListener() {
        if (date == null) {
            date = Calendar.getInstance();
        }

        setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                                date.set(year, monthOfYear, dayOfMonth);
                                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                                setText(format.format(date.getTime()));
                            }
                        },
                        date.get(Calendar.YEAR),
                        date.get(Calendar.MONTH),
                        date.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
    }
}