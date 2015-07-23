package org.youth.overlook.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;
import org.youth.overlook.bean.User;

import java.util.Date;

public class PreferenceUtil {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public final String TAG = "org.youth.overlook";

    /**
     * Construct SharePreference object by content and name
     *
     * @param context
     */
    public PreferenceUtil(Context context) {
        sharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * add a key-string pair to SharePreference object
     *
     * @param key
     * @param value
     */
    public void putValues(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * get the value by key
     *
     * @param key
     * @return
     */
    public String getValue(String key) {
        return sharedPreferences.getString(key, null);
    }

    /**
     * clear sharePreference
     */
    public void clear() {
        editor.clear();
        editor.commit();
    }

    /**
     * 对象转成Json后存入preference
     *
     * @param user
     */
    public void putUser(User user) {
        if (user != null) {
            JSONObject userJson = new JSONObject();
            try {
                userJson.put("phonenumber", user.getPhonenumber());
                userJson.put("password", user.getPassword());
                userJson.put("nickname", user.getNickname());
                userJson.put("registertime", user.getRegisterTime());
                userJson.put("lastlogintime", user.getLastLoginTime());
                userJson.put("contactuploadtime", user.getContactUploadTime());
                userJson.put("contactamount", user.getContactAmount());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                putValues("user", userJson.toString());
            }
        }
    }

    /**
     * 获得Json后转成user对象再返回
     *
     * @return User
     */
    public User getUser() {
        User user = null;
        String userString = getValue("user");
        if (userString != null && !userString.isEmpty()) {
            try {
                user = new User();
                JSONObject userJson = new JSONObject(userString);
                user.setPhonenumber(userJson.getString("phonenumber"));
                user.setPassword(userJson.getString("password"));
                user.setNickname(userJson.getString("nickname"));
                user.setRegisterTime((Date) (userJson.get("registertime")));
                user.setLastLoginTime((Date) (userJson.get("lastlogintime")));
                user.setContactUploadTime((Date) (userJson.get("contactuploadtime")));
                user.setContactAmount(userJson.getInt("contactamount"));
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                return user;
            }
        }
        return user;
    }
}
