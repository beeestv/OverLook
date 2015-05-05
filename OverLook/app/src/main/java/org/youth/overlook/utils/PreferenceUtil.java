package org.youth.overlook.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtil {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public final String TAG = "org.youth.overlook";

    /**
     * Construct SharePreference object by content and name
     * @param context
     */
    public PreferenceUtil(Context context) {
        sharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * add a key-value pair to SharePreference object
     * @param key
     * @param value
     */
    public void putValues(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * get the value by key
     * @param key
     * @return
     */
    public String getValue(String key) {
        return sharedPreferences.getString(key, null);
    }
}
