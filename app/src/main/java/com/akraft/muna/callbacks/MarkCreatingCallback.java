package com.akraft.muna.callbacks;

import android.text.Editable;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;

public interface MarkCreatingCallback {
    void locationGot(LatLng latLng);
    void detailsGot(String name, String codeword, String note, File imageFile);
    void next();

    void started(String codeword);

    void cancel();
}
