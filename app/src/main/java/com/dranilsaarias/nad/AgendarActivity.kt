package com.dranilsaarias.nad

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import kotlinx.android.synthetic.main.activity_agendar.*
import kotlinx.android.synthetic.main.content_agendar.*
import java.util.*


class AgendarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agendar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val type = findViewById<ClickToSelectEditText<Type>>(R.id.motivo)
        val types = ArrayList<Type>()
        types.add(Type("Consulta"))
        types.add(Type("Depilación láser"))
        types.add(Type("Otro procedimiento"))
        types.add(Type("Láser"))
        types.add(Type("Aplicación botox"))
        types.add(Type("Otro motivo"))
        type.setItems(types)
        type.setOnItemSelectedListener(object : ClickToSelectEditText.OnItemSelectedListener<Type> {
            override fun onItemSelectedListener(item: Type, selectedIndex: Int) {
                Log.i("type", item.label)
            }
        })

        first.setOnClickListener {
            openProgramingAnimation()
        }
    }

    private fun openProgramingAnimation() {
        //hours_container.visibility = View.GONE
        //date_programming_container.visibility = View.VISIBLE

        val a = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {

            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        a.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                hours_container.visibility = View.VISIBLE
                date_programming_container.visibility = View.VISIBLE
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                hours_container.visibility = View.GONE
                date_programming_container.visibility = View.VISIBLE
            }
        })

        a.duration = 200
        hours_container.startAnimation(a)
    }

    override fun onBackPressed() {
        if (hours_container.visibility == View.GONE) {
            closeProgramingAnimation()
        } else {
            super.onBackPressed()
        }
    }

    private fun closeProgramingAnimation() {
        hours_container.visibility = View.VISIBLE
        date_programming_container.visibility = View.GONE

    }

    private inner class Type internal constructor(override val label: String) : Listable {

    }
}
