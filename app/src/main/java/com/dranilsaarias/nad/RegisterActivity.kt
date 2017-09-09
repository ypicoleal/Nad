package com.dranilsaarias.nad

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.util.Log
import kotlinx.android.synthetic.main.content_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setSpinners()
        see_tos_btn.setOnClickListener { vetTos() }
    }


    private fun setSpinners() {
        val document = findViewById<ClickToSelectEditText<Documento>>(R.id.document)
        val civil = findViewById<ClickToSelectEditText<Documento>>(R.id.civil)
        val entidad = findViewById<ClickToSelectEditText<Documento>>(R.id.entidad)

        val documentos = ArrayList<Documento>()
        documentos.add(Documento("Cedula de ciudadania"))
        documentos.add(Documento("Cedula de extrangeria"))
        documentos.add(Documento("Pasaporte"))
        documentos.add(Documento("Tarjeta de identidad"))
        document.setItems(documentos)
        document.setOnItemSelectedListener(object : ClickToSelectEditText.OnItemSelectedListener<Documento> {
            override fun onItemSelectedListener(item: Documento, selectedIndex: Int) {
                Log.i("documento", item.label)
            }
        })

        val estados = ArrayList<Documento>()
        estados.add(Documento("Casado/a"))
        estados.add(Documento("Soltero/a"))
        civil.setItems(estados)
        civil.setOnItemSelectedListener(object : ClickToSelectEditText.OnItemSelectedListener<Documento> {
            override fun onItemSelectedListener(item: Documento, selectedIndex: Int) {
                Log.i("estado", item.label)
            }
        })

        val entidades = ArrayList<Documento>()
        entidades.add(Documento("Colsanitas"))
        entidades.add(Documento("Medisanitas"))
        entidades.add(Documento("Particular"))
        entidad.setItems(entidades)
        entidad.setOnItemSelectedListener(object : ClickToSelectEditText.OnItemSelectedListener<Documento> {
            override fun onItemSelectedListener(item: Documento, selectedIndex: Int) {
                Log.i("entidades", item.label)
            }
        })

    }

    private fun vetTos() {

        val inflater = this.layoutInflater
        val v = inflater.inflate(R.layout.tos, null)

        val alert = AlertDialog
                .Builder(this)
                .setView(v)
                .create()

        alert.show()
        val btn = v.findViewById<CardView>(R.id.accept_tos)
        btn.setOnClickListener {
            alert.dismiss()
            accept_tos_cbr.isChecked = true
        }
    }

    private inner class Documento internal constructor(override val label: String) : Listable {

    }
}
