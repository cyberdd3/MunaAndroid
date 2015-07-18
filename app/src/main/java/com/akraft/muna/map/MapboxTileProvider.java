package com.akraft.muna.map;

import android.widget.Toast;

import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;

public class MapboxTileProvider extends UrlTileProvider {

    //TODO different tile resolution for different screens
    public static  int MAP_TILE_DIMENSION = 256;
    private static String MAP_ID = "cyberdd3.bacd1a2d";
    private static String MAP_ACCESS_TOKEN = "pk.eyJ1IjoiY3liZXJkZDMiLCJhIjoiOWExMmEyNzdiNzFmNGVlYTkzMDFmZDc2MDFiNzRmNDgifQ.lMY_7jPhXLmJ5cXIRDK3gw";
    public static String MAP_BASE_URL = "http://api.tiles.mapbox.com/v4/" + MAP_ID + "/%d/%d/%d@2x.png" + MAP_TILE_DIMENSION + "?access_token=" + MAP_ACCESS_TOKEN;

    public MapboxTileProvider(int width, int height) {
        super(width, height);
    }

    @Override
    public URL getTileUrl(int x, int y, int z) {
        try {
            return new URL(String.format(MAP_BASE_URL, z, x, y));
        }  catch (MalformedURLException e) {
            throw new RuntimeException("Failed constructing map tile URL", e);
        }
    }
}
