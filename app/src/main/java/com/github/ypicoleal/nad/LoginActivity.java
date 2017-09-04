package com.github.ypicoleal.nad;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void verTos(View view) {
        startActivity(new Intent(this, TosActivity.class));
    }

    public void registrar(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }
}
