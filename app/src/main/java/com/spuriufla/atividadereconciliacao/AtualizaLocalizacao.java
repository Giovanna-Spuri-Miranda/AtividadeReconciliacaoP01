package com.spuriufla.atividadereconciliacao;

import android.location.Location;
import android.location.LocationListener;

public class AtualizaLocalizacao implements LocationListener {
    private static double lat, longi;

    @Override
    public void onLocationChanged(Location location) {
        this.lat  = location.getLatitude();
        this.longi = location.getLongitude();
    }

    public double getLatitude(){
        return lat;
    }

    public double getLongitude(){
        return longi;
    }
}
