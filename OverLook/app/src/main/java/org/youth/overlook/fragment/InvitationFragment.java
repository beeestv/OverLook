package org.youth.overlook.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.youth.overlook.R;
import org.youth.overlook.activity.InvitationActivity;
import org.youth.overlook.utils.PreferenceUtil;
import org.youth.overlook.utils.SQLUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class InvitationFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private ProgressDialog progDialog;

    private String actionid;
    private boolean didSuccess;

    public EditText actionName;
    private ListView mListView;
    private ListAdapter mAdapter;
    private List<Map<String, String>> contactsList;
    private List<String> contactsNameList;
    private SQLUtil sqlUtil = new SQLUtil();
    /**
     * Handler，联系人查询完毕后更新UI线程
     */
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_multiple_choice, contactsNameList);
            mListView.setAdapter(mAdapter);
            mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                int i;

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    // TODO Auto-generated method stub
                    String city = contactsList.get(position).get("name");
                    boolean isSelected = Boolean.valueOf(contactsList.get(position)
                            .get("isSelected"));
                    isSelected = !isSelected;
                    contactsList.get(position).put("isSelected", String.valueOf(isSelected));
                }

            });
            dismissDialog();
        }
    };

    public InvitationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        queryAllContacts();
        showDialog();
        Log.d("jdbc", "显示dialog");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member, container, false);

        mListView = (ListView) view.findViewById(android.R.id.list);
        actionName = (EditText) view.findViewById(R.id.action_name);

        // Set OnItemClickListener so we can be notified on item clicks
//        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * 查询所有联系人
     */
    private void queryAllContacts() {
        new Thread() {
            public void run() {
                Looper.prepare();

                contactsList = new ArrayList<Map<String, String>>();
                contactsNameList = new ArrayList<String>();

                Uri uri = Uri.parse("content://com.android.contacts/contacts");
                ContentResolver resolver = getActivity().getContentResolver();
                Cursor cursor = resolver.query(uri, new String[]{"_id"}, null, null, null);
                while (cursor.moveToNext()) {
                    int contractID = cursor.getInt(0);
                    StringBuilder sb = new StringBuilder("contractID=");
                    sb.append(contractID);
                    uri = Uri.parse("content://com.android.contacts/contacts/" + contractID + "/data");
                    Cursor cursor1 = resolver.query(uri, new String[]{"mimetype", "data1"}, null, null, null);
                    boolean flag = false;
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
                            try {
                                List<Map<String, Object>> resultSet;
                                resultSet = sqlUtil.queryResults(sql, params);
                                if (resultSet != null && !resultSet.isEmpty()) {
                                    Log.d("jdbc", resultSet.toString());
                                    flag = true;
                                } else {
                                    break;
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (flag && !name.isEmpty()) {
                        /*把查询到的联系人添加到contactsList，把isSelected设为false*/
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("name", name);
                        data.put("isSelected", String.valueOf(false));
                        data.put("phonenumber", phonenumber);
                        contactsList.add(data);
                        contactsNameList.add(name);
                    }
                    cursor1.close();
                    Log.d("jdbc", sb.toString());
                }
                cursor.close();
                mHandler.sendMessage(new Message());

            }
        }.start();

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

    /**
     * 显示loading提示框
     */
    public void showDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(getActivity());
        progDialog.setMessage("加载联系人信息...");
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setCanceledOnTouchOutside(false);
        progDialog.show();
    }

    /**
     * 取消显示loading提示框
     */
    public void dismissDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    /**
     * 根据选择的联系人建立活动(build action)
     */
    public void buildAction() {
        new Thread() {
            public void run() {
                Looper.prepare();

                PreferenceUtil preferenceUtil = new PreferenceUtil(getActivity());
                List<Object> params = new ArrayList<Object>();
                String builder = null;
                boolean flag = true;
                if(((InvitationActivity)getActivity()).invitation!=null){
                    actionid = preferenceUtil.getValue("actionid");
                    builder = "";
                }else {
                    actionid = String.valueOf(System.currentTimeMillis());
                    builder = preferenceUtil.getValue("phonenumber");
                    String sql = "insert into actioninfo (actionid,builder,actionname) values (?,?,?)";
                    params.add(actionid);
                    params.add(builder);
                    params.add(actionName.getText().toString());
                    try {
                        flag = sqlUtil.updateByPrepareStatment(sql, params);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                writeData(flag,builder,actionid);
            }
        }.start();
    }

    private void writeData(boolean bool,String builder,String actionid){
            if (bool) {
                String sql =null;
                List<Object> params = new ArrayList<Object>();
                boolean flag = false;//创建者是否写入
                Map<String, String> contact;
                for (int i = 0; i<contactsList.size();i++) {
                    contact = contactsList.get(i);
                    String phonenumber = contact.get("phonenumber");
                    if (phonenumber.equals(builder) || Boolean.parseBoolean(contact.get("isSelected"))) {
                        params = new ArrayList<Object>();
                        params.add(phonenumber);
                        params.add(actionid);
                        if (phonenumber.equals(builder)) {
                            flag = true;
                            Log.d("jdbc","被选择或是本机     "+flag);
                            sql = "insert into useraction (phonenumber,actionid,didjoin) values (?,?,'1')";
                        } else {
                            sql = "insert into useraction (phonenumber,actionid) values (?,?)";
                        }
                        try {
                            sqlUtil.updateByPrepareStatment(sql, params);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }finally {
                            dismissDialog();
                            continue;
                        }
                    }
                }
                dismissDialog();
                Log.d("jdbc", "活动创建成功");
                Intent data = new Intent();
                data.putExtra("actionid", String.valueOf(actionid));
                getActivity().setResult(1, data);
                getActivity().finish();
            } else {
                dismissDialog();
                Log.d("jdbc", "actionifo写入失败");
            }
    }

}
