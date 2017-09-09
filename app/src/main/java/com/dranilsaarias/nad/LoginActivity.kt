package com.dranilsaarias.nad

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.PasswordTransformationMethod
import android.view.View

import kotlinx.android.synthetic.main.content_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        password.transformationMethod = PasswordTransformationMethod()
    }

    fun vetTos(view: View) {

    }

    fun registrar(view: View){
        startActivity(Intent(this, RegisterActivity::class.java))
    }
}
