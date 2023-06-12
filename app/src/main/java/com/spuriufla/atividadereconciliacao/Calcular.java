package com.spuriufla.atividadereconciliacao;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Toast;
import java.text.DecimalFormat;

public class Calcular extends AppCompatActivity {
    private EditText Ed_consumo, Ed_valorCombustivel, Ed_VelRecomendada, Ed_VelMedia;
    private EditText Ed_distanciaTotal, Ed_DistPercorrida, Ed_DistRestante;
    private EditText Ed_LocalAtual, Ed_LocalDestino, Ed_tempoDesejado;
    private double consumo, velRecomendada, velMedia;
    private double distanciaRestante, distanciaTotal;
    private static double latitudeNova, longitudeNova;
    private static double latitudeDestino, longitudeDestino;
    private boolean setInicio = true, gpsAtivo = true;
    private float valorAutonomia, valorCombustivel;
    private LocationManager locationManager;
    private Chronometer cronometro;
    private long tempoRestante;
    private int tempoTotal;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculo);

        Button myButton = findViewById(R.id.BTFinalizar);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Calcular.this, MainActivity.class);
                startActivity(i);
            }
        });

        tempoTotal = getIntent().getIntExtra("tempoTotal",0);
        valorAutonomia = getIntent().getFloatExtra("valorAutonomia",0);
        valorCombustivel = getIntent().getFloatExtra("valorCombustivel",0);

        latitudeDestino = -21.225691;
        longitudeDestino = -44.978236;

        // Localização
        Ed_LocalAtual = findViewById(R.id.localAtual);
        Ed_LocalDestino = findViewById(R.id.localDestino);

        // Destino
        Ed_distanciaTotal = findViewById(R.id.DistTotal);
        Ed_DistPercorrida = findViewById(R.id.DistPercorrida);
        Ed_DistRestante = findViewById(R.id.DistRestante);

        // Tempo
        Ed_tempoDesejado = findViewById(R.id.tempoDesejado);
        Ed_tempoDesejado.setText(tempoTotal+ " min");
        cronometro = findViewById(R.id.cronometro);

        // Otimização
        Ed_consumo = findViewById(R.id.consumo);
        Ed_valorCombustivel = findViewById(R.id.custo);
        Ed_VelRecomendada = findViewById(R.id.VelRecomendada);
        Ed_VelMedia = findViewById(R.id.velMedia);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        gpsAtivo = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (gpsAtivo) {
            capturarUltimaLocalizacaoValida();

        } else {
            latitudeNova = 0.00;
            longitudeNova = 0.00;
            Toast.makeText(this, "GPS não Disponível", Toast.LENGTH_LONG).show();
        }

        Ed_LocalDestino.setText("Lat.: "+ formatarGeopoint(latitudeDestino) + " Long.: " + formatarGeopoint(longitudeDestino));

        distanciaTotal = calculoDistancia(latitudeNova, longitudeNova, latitudeDestino, longitudeDestino);
        tempoRestante = tempoTotal*60;
        myThread.start();
    }

    private Thread myThread = new Thread(new Runnable() {
        @Override
        public void run() {

            Looper.prepare();
            while(tempoRestante > 0) {

                capturarUltimaLocalizacaoValida();

                if (latitudeNova != 0) {
                    distanciaRestante = calculoDistancia(latitudeNova, longitudeNova, latitudeDestino, longitudeDestino);
                    consumo = (distanciaRestante / 1000) / valorAutonomia;
                    velRecomendada = (distanciaRestante / tempoRestante) * 3.6;

                    tempoRestante = tempoTotal * 60 - (SystemClock.elapsedRealtime() - cronometro.getBase()) / 1000;

                    if (setInicio) { //Executa só uma vez os códigos aqui dentro
                        distanciaTotal = calculoDistancia(latitudeNova, longitudeNova, latitudeDestino, longitudeDestino);
                        cronometro.setBase(SystemClock.elapsedRealtime());
                        velMedia = (distanciaTotal / (tempoTotal * 60)) * 3.6;

                        setInicio = false;
                    }
                    cronometro.start();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Ed_LocalAtual.setText("Lat.: " + formatarGeopoint(latitudeNova) + " Long.: " + formatarGeopoint(longitudeNova));

                        Ed_distanciaTotal.setText(String.format("%.2f", distanciaTotal) + " m");
                        Ed_DistRestante.setText(String.format("%.2f", distanciaRestante) + " m");
                        Ed_DistPercorrida.setText(String.format("%.2f", distanciaTotal - distanciaRestante) + " m");

                        Ed_consumo.setText(String.format("%.2f", consumo) + " Litros");
                        Ed_valorCombustivel.setText("R$ " + String.format("%.2f", consumo * valorCombustivel));

                        Ed_VelMedia.setText(String.format("%.2f", velMedia) + " Km/h");
                        Ed_VelRecomendada.setText(String.format("%.2f", velRecomendada) + " Km/h");
                    }
                });
            }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            cronometro.stop();

            if(tempoRestante <= 0){

                if( distanciaRestante < 30){
                    Toast.makeText(Calcular.this, "Parabéns! Você chegou ao Destino dentro do prazo!", Toast.LENGTH_LONG).show();

                }else{
                    Toast.makeText(Calcular.this, "Você não chegou ao destino no tempo determinado.", Toast.LENGTH_LONG).show();
                }
            }
        }
    });

    private void capturarUltimaLocalizacaoValida() {
        gpsAtivo = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (gpsAtivo) {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {

                AtualizaLocalizacao atualizaLoc = new AtualizaLocalizacao();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, atualizaLoc);
                latitudeNova = atualizaLoc.getLatitude();
                longitudeNova = atualizaLoc.getLongitude();

            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private String formatarGeopoint(double valor){
        DecimalFormat decimalFormat = new DecimalFormat("#.####");
        return decimalFormat.format(valor);
    }

    private double calculoDistancia(double lat1, double long1, double lat2, double long2){
        float[] dist = new float[1];
        Location.distanceBetween(lat1, long1,  lat2, long2, dist);
        return dist[0];
    }
}
