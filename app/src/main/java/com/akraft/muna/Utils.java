package com.akraft.muna;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.akraft.muna.models.Exclude;
import com.akraft.muna.models.Mark;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.gsonfire.GsonFireBuilder;
import io.gsonfire.PostProcessor;

public class Utils {

    public static final DisplayImageOptions NO_CACHE_OPTION = new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisk(false).build();

    public static String AUTH_PREF = "auth";
    public static String SHOWCASE_PREF = "showcases";

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);


    public static boolean isValidEmail(String text) {
        Matcher matcher = emailPattern.matcher(text);
        boolean validLength = text.length() < 254;
        return matcher.matches() && validLength;
    }

    //in meters
    public static double calculateDistance(LatLng p1, LatLng p2) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = p1.latitude;
        double lat2 = p2.latitude;
        double lon1 = p1.longitude;
        double lon2 = p2.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));

        return Radius * c * 1000;
    }

    public static LatLng moveLatLng(LatLng position, double ns, double we) {
        return new LatLng(position.latitude + ns / 111111, position.longitude + we / (111111 * Math.cos(position.latitude)));
    }

    public static Gson createGson() {
        return new GsonFireBuilder()
                .registerPostProcessor(Mark.class, new PostProcessor<Mark>() {
                    @Override
                    public void postDeserialize(Mark result, JsonElement src, Gson gson) {
                        Mark savedData = Mark.findById(Mark.class, result.getId());
                        if (savedData != null) {
                            result.setHidden(savedData.isHidden());
                            result.setBookmarked(savedData.isBookmarked());
                        }
                        result.setThumbnail(normalizePhotoPath(result.getThumbnail()));
                        result.setPhoto(normalizePhotoPath(result.getPhoto()));
                        //result.save();
                    }

                    @Override
                    public void postSerialize(JsonElement result, Mark src, Gson gson) {

                    }
                })
                .createGsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSSSS'Z'")
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getAnnotation(Exclude.class) != null;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                }).create();
    }

    private static String normalizePhotoPath(String path) {
        if (path != null) {
            if (!path.startsWith(Config.SERVER_URL_PORT)) {
                return Config.SERVER_URL_PORT + path;
            }
        }
        return path;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isGPSEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    }


    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES);

        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }


    public static void compressImage(File file) throws IOException {
        //Convert bitmap to byte array
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        bitmapToFile(file, bitmap);
    }

    public static void bitmapToFile(File file, Bitmap bitmap) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos);
        byte[] bitmapdata = bos.toByteArray();

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();
    }

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static String getPath(Uri uri, Context context) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }
}
