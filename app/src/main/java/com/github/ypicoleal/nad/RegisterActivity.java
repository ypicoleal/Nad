package com.github.ypicoleal.nad;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setSpinners();
    }


    private void setSpinners() {
        ClickToSelectEditText<Documento> document = findViewById(R.id.document);
        ClickToSelectEditText<Documento> civil = findViewById(R.id.civil);
        ClickToSelectEditText<Documento> entidad = findViewById(R.id.entidad);

        ArrayList<Documento> documentos = new ArrayList<>();
        documentos.add(new Documento("Cedula de ciudadania"));
        documentos.add(new Documento("Cedula de extrangeria"));
        documentos.add(new Documento("Pasaporte"));
        documentos.add(new Documento("Tarjeta de identidad"));
        document.setItems(documentos);
        document.setOnItemSelectedListener(new ClickToSelectEditText.OnItemSelectedListener<Documento>() {
            @Override
            public void onItemSelectedListener(Documento item, int selectedIndex) {
                Log.i("documento", item.getLabel());
            }
        });

        ArrayList<Documento> estados = new ArrayList<>();
        estados.add(new Documento("Casado/a"));
        estados.add(new Documento("Soltero/a"));
        civil.setItems(estados);
        civil.setOnItemSelectedListener(new ClickToSelectEditText.OnItemSelectedListener<Documento>() {
            @Override
            public void onItemSelectedListener(Documento item, int selectedIndex) {
                Log.i("estado", item.getLabel());
            }
        });

        ArrayList<Documento> entidades = new ArrayList<>();
        entidades.add(new Documento("Colsanitas"));
        entidades.add(new Documento("Medisanitas"));
        entidades.add(new Documento("Particular"));
        entidad.setItems(entidades);
        entidad.setOnItemSelectedListener(new ClickToSelectEditText.OnItemSelectedListener<Documento>() {
            @Override
            public void onItemSelectedListener(Documento item, int selectedIndex) {
                Log.i("entidades", item.getLabel());
            }
        });

    }

    public void verTos(View view) {
        startActivity(new Intent(this, TosActivity.class));
    }


    private class Documento implements Listable {
        String label;

        Documento(String label) {
            this.label = label;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

}
