package com.dranilsaarias.nad;

import android.app.Activity;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Activity context;

    public CustomInfoWindowAdapter(Activity context) {
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return context.getLayoutInflater().inflate(R.layout.custominfowindow, null);
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
