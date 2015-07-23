package org.youth.overlook.utils;

import android.content.Context;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 获取自己的locationId 和 集合点destinationId
 */
public class InitUtil {

    public InitUtil(Context context, String actionid){
        SQLUtil sqlUtil = new SQLUtil();
        PreferenceUtil preferenceUtil = new PreferenceUtil(context);
        List<Object> params = new ArrayList<Object>();

        String phoneNumber = preferenceUtil.getValue("phonenumber");

        String sql = "select destinationid from actioninfo where actionid=?";
        params.add(actionid);
        try {
            List<Map<String, Object>> result = sqlUtil.queryResults(sql,params);
            String  destinationid = String.valueOf(result.get(0).get("destinationid"));
            if (destinationid!=null) {
                Log.d("jdbc", "destinationid   " + destinationid);
                preferenceUtil.putValues("destinationid", destinationid);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        params.clear();

        sql = "select locationid from useraction where actionid=? and phonenumber=?";
        params.add(actionid);
        params.add(phoneNumber);
        try {
            List<Map<String, Object>> result = sqlUtil.queryResults(sql,params);
            String  locationid = String.valueOf(result.get(0).get("locationid"));
            if (locationid!=null) {
                Log.d("jdbc", "locationid    " + locationid);
                preferenceUtil.putValues("locationid", locationid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
