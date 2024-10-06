package com.fixcare.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

public class Utils {
    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }
    public static void basicDialog(Context context, String title, String button){
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context);
        materialAlertDialogBuilder.setTitle(title);
        materialAlertDialogBuilder.setPositiveButton(button, (dialogInterface, i) -> { });
        materialAlertDialogBuilder.show();
    }
    public static void simpleDialog(Context context, String title, String message, String button){
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context);
        materialAlertDialogBuilder.setTitle(title);
        materialAlertDialogBuilder.setMessage(message);
        materialAlertDialogBuilder.setPositiveButton(button, (dialogInterface, i) -> { });
        materialAlertDialogBuilder.show();
    }

    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId, int width, int height) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, width, height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static class Cache {
        public static void removeKey(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().remove(key).apply();
        }

        public static String getString(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            return sharedPreferences.getString(key, "");
        }

        public static void setString(Context context, String key, String value){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().putString(key, value).apply();
        }

        public static int getInt(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            return sharedPreferences.getInt(key, 0);
        }

        public static void setInt(Context context, String key, int value){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().putInt(key, value).apply();
        }

        public static double getDouble(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            return Double.longBitsToDouble(sharedPreferences.getLong(key, 0));
        }

        public static void setDouble(Context context, String key, double value){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().putLong(key, Double.doubleToRawLongBits(value)).apply();
        }

        public static long getLong(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            return sharedPreferences.getLong(key, 0);
        }

        public static void setLong(Context context, String key, long value){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().putLong(key, value).apply();
        }

        public static boolean getBoolean(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean(key, false);
        }

        public static void setBoolean(Context context, String key, boolean value){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().putBoolean(key, value).apply();
        }

        public static Set<String> getStringSet(Context context, String key){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            return sharedPreferences.getStringSet(key, new HashSet<>());
        }

        public static void setStringSet(Context context, String key, Set<String> set){
            SharedPreferences sharedPreferences = context.getSharedPreferences("fixcare_cache", Context.MODE_PRIVATE);
            sharedPreferences.edit().putStringSet(key, set).apply();
        }
    }

    public static String addressBuilder(String locality, String subAdminArea) {
        StringBuilder addressBuilder = new StringBuilder();
        if (locality.isEmpty()) {
            addressBuilder.append("Unspecifiable, ");
        }
        else {
            addressBuilder.append(locality).append(", ");
        }

        if (subAdminArea.isEmpty()) {
            addressBuilder.append("Unspecifiable");
        }
        else {
            addressBuilder.append(subAdminArea);
        }

        return addressBuilder.toString();
    }

    public static class DoubleFormatter {

        public static String currencyFormat(double dbl){
            if (dbl == 0) {
                return "0.00";
            }
            else {
                DecimalFormat formatter = new DecimalFormat("#,###.00");
                return formatter.format(dbl);
            }
        }

    }
}
