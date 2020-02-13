package com.example.mycurrency;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefsMgr {
    private static SharedPreferences sShredPreferences;
    public static void setString(Context context,String locale,String code){
        sShredPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor=sShredPreferences.edit();
        editor.putString(locale,code);
        editor.commit();
    }
    public static String getString(Context context, String locale){
        sShredPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        return sShredPreferences.getString(locale,null);
    }
}
