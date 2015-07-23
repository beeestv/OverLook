package org.youth.overlook.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * description:上传手机所有联系人到远程数据库
 */
public class ContactUpdateUtil {
    private String USERNAME = "root";
    private String PASSWORD = "rbi0Npvwlyxc";
    private String URL = "jdbc:mysql://121.43.234.220:3306/overlookcontacts?useUnicode=true&characterEncoding=utf8";

    SQLUtil sqlUtil;

    /**
     * 创建和overlookcontacts的连接
     */
    public ContactUpdateUtil() {
        sqlUtil = new SQLUtil(URL, USERNAME, PASSWORD);
    }

    /**
     * 为用户在overlookcontacts数据库中创建联系人表 contact_"phonenumber"
     *
     * @param phonenumber 用户手机号
     */
    public void createTable(String phonenumber) throws SQLException {
        String sql = "CREATE TABLE contact_" + phonenumber + "(contactid int NOT NULL AUTO_INCREMENT,name varchar(20) NOT NULL, phonenumber char(11), PRIMARY KEY (contactid))";
        sqlUtil.updateByPrepareStatment(sql, null);
    }

    /**
     * 上传联系人到数据库，后关闭连接
     *
     * @param activity
     * @return 成功上传的联系人数量
     */
    public int updateAllContacts(Context activity) {
        //FIXME：SIM卡上联系人未考虑
        ContentResolver resolver;
        Cursor cursor;
        Uri uri;
        int sum = 0;

        uri = Uri.parse("content://com.android.contacts/contacts");
        resolver = activity.getContentResolver();
        cursor = resolver.query(uri, new String[]{"_id"}, null, null, null);
        while (cursor.moveToNext()) {
            int contractID = cursor.getInt(0);
            StringBuilder sb = new StringBuilder("contractID=");
            sb.append(contractID);
            uri = Uri.parse("content://com.android.contacts/contacts/" + contractID + "/data");
            Cursor cursor1 = resolver.query(uri, new String[]{"mimetype", "data1"}, null, null, null);
            String name = "";
            String phonenumber = "";
            while (cursor1.moveToNext()) {
                String data1 = cursor1.getString(cursor1.getColumnIndex("data1"));
                String mimeType = cursor1.getString(cursor1.getColumnIndex("mimetype"));
                if ("vnd.android.cursor.item/name".equals(mimeType)) { //是姓名
                    name = data1;
                    sb.append(",name=" + data1);
                } else if ("vnd.android.cursor.item/phone_v2".equals(mimeType)) { //手机
                    sb.append(",phone=" + data1);
                    data1 = data1.replace(" ", "");//删除XXX XXXX XXXX格式中间的空格
                    data1 = data1.replace("+86", "");//删除某些号码的+86前缀
                    phonenumber = data1;
                    String sql = "select phonenumber from userinfo where phonenumber=?";
                    List<Object> params = new ArrayList<Object>();
                    params.add(data1);
                }
            }
            Log.d("ol_contactupload", sb.toString());
            //若获取到姓名，则写入数据库
            if (!name.isEmpty()) {
                List<Object> data = new ArrayList<>();
                data.add(name);
                data.add(phonenumber);
                String sql = "";
                try {
                    sqlUtil.updateByPrepareStatment(sql, data);
                    sum++;
                } catch (SQLException e) {
                    return sum;
                }
            }
            cursor1.close();
        }
        cursor.close();
        sqlUtil.releaseConnection();
        return sum;
    }
}
