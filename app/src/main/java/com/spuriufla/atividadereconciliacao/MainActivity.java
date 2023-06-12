package com.spuriufla.atividadereconciliacao;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private Button myButton;
    private EditText tempDesejado, autonomia, combustivel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myButton = findViewById(R.id.BTCalcular);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Calcular.class);
                i.putExtra("tempoTotal", Integer.parseInt(tempDesejado.getText().toString()));
                i.putExtra("valorAutonomia", Float.parseFloat(autonomia.getText().toString()));
                i.putExtra("valorCombustivel", Float.parseFloat(combustivel.getText().toString()));
                startActivity(i);
            }
        });

        tempDesejado = findViewById(R.id.tempDesejado);
        autonomia = findViewById(R.id.autonomia);
        combustivel = findViewById(R.id.combustivel);
    }
}