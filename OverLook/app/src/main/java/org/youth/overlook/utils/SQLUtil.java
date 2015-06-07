package org.youth.overlook.utils;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/4/10.
 */
public class SQLUtil {

    private final String DRIVER = "com.mysql.jdbc.Driver";
    private  String USERNAME = "root";
    private  String PASSWORD = "rbi0Npvwlyxc";
    private  String URL = "jdbc:mysql://121.43.234.220:3306/overlook";

    private Connection connection;
    private PreparedStatement pstmt;
    private ResultSet resultSet;

    /**
     * 使用默认URL、数据库账号密码构造连接工具
     */
    public SQLUtil() {
        try {
            Class.forName(DRIVER);
            Log.d("jdbc", "Register driver succeed.");
        } catch (Exception e) {
            Log.d("jdbc", e.getMessage());
        }
    }

    /**
     * 使用自定义URL、数据库账号密码构造连接工具
     * @param url
     * @param username
     * @param password
     */
    public SQLUtil(String url, String username, String password) {
        try {
            Class.forName(DRIVER);
            Log.d("jdbc", "Register driver succeed.");
            this.URL = url;
            this.USERNAME = username;
            this.PASSWORD = password;
        } catch (Exception e) {
            Log.d("jdbc", e.getMessage());
        }
    }

    /**
     * 释放连接
     * @throws SQLException
     */
    public void releaseConnection() throws SQLException {
        if (resultSet != null) {
            resultSet.close();
        }
        if (pstmt != null) {
            pstmt.close();
        }
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    /**
     * update、insert、delete语句
     * @param sql
     * @param params
     * @return
     * @throws SQLException
     */
    public boolean updateByPrepareStatment(String sql, List<Object> params) throws SQLException {
        if(connection == null){
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            Log.d("jdbc","Connect succeed.");
        }
        boolean flag = false;
        int result = -1;
        fillPlaceholder(sql, params);
        result = pstmt.executeUpdate();
        flag = result > 0 ? true : false;
        return flag;
    }

    /**
     * @param sql
     * @param params
     * @return
     * @throws SQLException
     */
    public List<Map<String, Object>> queryResults(String sql, List<Object> params) throws SQLException {
        if(connection == null){
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            Log.d("jdbc","Connect succeed.");
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        this.fillPlaceholder(sql, params);
        resultSet = pstmt.executeQuery();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int col_len = metaData.getColumnCount();
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<String, Object>();
            for (int i = 0; i < col_len; i++) {
                String col_name = metaData.getColumnName(i + 1);
                Object col_value = resultSet.getObject(col_name);
                if (col_value == null) {
                    col_value = "";
                }
                map.put(col_name, col_value);
            }
            list.add(map);
        }
        return list;
    }

    /**
     * 占位符填充
     * @param sql
     * @param params
     */
    public void fillPlaceholder(String sql, List<Object> params) throws SQLException {
        int index = 1;
        pstmt = connection.prepareStatement(sql);
        if (params != null && !params.isEmpty()) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(index++, params.get(i));
            }
        }
    }
}
