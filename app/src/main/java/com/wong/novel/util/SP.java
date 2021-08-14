package com.wong.novel.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.wong.novel.constant.App;

import java.util.Map;
import java.util.Set;

public class SP implements SharedPreferences {

    private static final String TAG = "SP";

    private static final SharedPreferences prefs = App.instance.getApplicationContext().getSharedPreferences(TAG,Context.MODE_PRIVATE);

    private static final Editor mEditor = prefs.edit();

    private static SP instance;

    public static  SP getInstance(){
        if (instance == null){
            synchronized (SP.class){
                if (instance == null){
                    instance = new SP();
                }
            }
        }
        return instance;
    }

    private SP() {
    }


    @Override
    public Map<String, ?> getAll() {
        return null;
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        return prefs.getString(key, defValue);
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return null;
    }

    @Override
    public int getInt(String key, int defValue) {
        return prefs.getInt(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return 0;
    }

    @Override
    public float getFloat(String key, float defValue) {
        return 0;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return prefs.getBoolean(key, defValue);
    }

    @Override
    public boolean contains(String key) {
        return false;
    }

    @Override
    public Editor edit() {
        return null;
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

    }


    public void set(String key,String value){
        mEditor.putString(key, value);
        mEditor.apply();
    }

    public void set(String key,boolean value){
        mEditor.putBoolean(key, value);
        mEditor.apply();
    }
}
