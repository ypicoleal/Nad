package com.dranilsaarias.nad

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_agendar.*
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
    }

    private inner class Type internal constructor(override val label: String) : Listable {

    }
}
