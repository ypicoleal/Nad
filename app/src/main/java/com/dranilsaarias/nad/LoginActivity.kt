package com.dranilsaarias.nad

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.text.method.PasswordTransformationMethod

import kotlinx.android.synthetic.main.content_login.*


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        password.transformationMethod = PasswordTransformationMethod()

        tos_btn.setOnClickListener {
            vetTos()
        }

        register_btn.setOnClickListener {
            registrar()
        }

        login_btn.setOnClickListener {
            login()
        }
    }

    fun vetTos() {

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
            accept_tos_cb.isChecked = true
        }
    }

    fun registrar() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    fun login() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}