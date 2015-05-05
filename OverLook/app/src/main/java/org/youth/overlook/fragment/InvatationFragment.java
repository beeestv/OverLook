package org.youth.overlook.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
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
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.youth.overlook.R;
import org.youth.overlook.utils.SQLUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class InvatationFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private ProgressDialog progDialog;

    private ListView mListView;
    private ListAdapter mAdapter;
    private List<String> contactsList;

    private SQLUtil sqlUtil = new SQLUtil();
    /**
     * Handler，联系人查询完毕后更新UI线程
     */
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_multiple_choice, contactsList);
            mListView.setAdapter(mAdapter);
            mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            dismissDialog();
        }
    };

    public InvatationFragment() {
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

        // Set the adapter
        mListView = (ListView) view.findViewById(android.R.id.list);

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
     * 在子线程中查询所有联系人信息
     */
    private void queryAllContacts() {
        new Thread() {
            public void run() {
                Looper.prepare();

//                contactsList = new ArrayList<Map<String, String>>();
                contactsList = new ArrayList<String>();

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
                    while (cursor1.moveToNext()) {
                        String data1 = cursor1.getString(cursor1.getColumnIndex("data1"));
                        String mimeType = cursor1.getString(cursor1.getColumnIndex("mimetype"));
                        if ("vnd.android.cursor.item/name".equals(mimeType)) { //是姓名
                            sb.append(",name=" + data1);
                            if (flag)
                                contactsList.add(data1);
                        } else if ("vnd.android.cursor.item/phone_v2".equals(mimeType)) { //手机
                            sb.append(",phone=" + data1);
                            data1 = data1.replace(" ", "");
                            String sql = "select phonenumber from userinfo where phonenumber=?";
                            List<Object> params = new ArrayList<Object>();
                            params.add(data1);
                            try {
                                if (sqlUtil.queryResults(sql, params) != null && !sqlUtil.queryResults(sql, params).isEmpty()) {
                                    flag = true;
                                }else{
                                    break;
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    cursor1.close();
                    Log.d("jdbc", sb.toString());
                }
                cursor.close();
                mHandler.sendMessage(new Message());

            }
        }.start();

    }
//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            // Notify the active callbacks interface (the activity, if the
//        if (null != mListener) {

//            // fragment is attached to one) that an item has been selected.
//            mListener.onFragmentInteraction();
//        }
//    }

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
}
