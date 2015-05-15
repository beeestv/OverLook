package org.youth.overlook.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.youth.overlook.R;
import org.youth.overlook.activity.LoginActivity;
import org.youth.overlook.activity.MainActivity;
import org.youth.overlook.activity.RegisterPasswordActivity;
import org.youth.overlook.utils.PreferenceUtil;
import org.youth.overlook.utils.SQLUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class MenuFragment extends Fragment implements View.OnClickListener {

    private ListView actionListView;

    private List<String> actionIdList = new ArrayList<String>();
    private List<String> actionNameList = new ArrayList<String>();
    private String phoneNumber;
    public LoadTask listLoadTask;

    private PreferenceUtil preferenceUtil;

    private String actionid;
    private String actionname;
    private MainActivity myMainActivity;
    private MapFragment myMapFragment;

    public RefreshActionListener refreshActionListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myMainActivity = ((MainActivity) getActivity());
        preferenceUtil = new PreferenceUtil(myMainActivity);

        myMapFragment = myMainActivity.myMapFragment;

        View view = inflater.inflate(R.layout.fragment_menu, null);
        ((Button) view.findViewById(R.id.btn_about)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.btn_changepassword)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.btn_logout)).setOnClickListener(this);
        actionListView = (ListView) view.findViewById(R.id.action_list);

        phoneNumber = new PreferenceUtil(getActivity()).getValue("phoneNumber");
        Log.d("jdbc", phoneNumber);
        listLoadTask = new LoadTask();
        listLoadTask.execute();

        return view;
    }

    /**
     * 初始化actionList
     */
    public void initActionList() {
        Log.d("ASYCN_TASK", actionNameList.toString());
        ListAdapter mAdapter = new ArrayAdapter<String>(getActivity(), R.layout.listview_item1, actionNameList);
        actionListView.setAdapter(mAdapter);
        actionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                myMapFragment.didQuery.put(myMapFragment.actionid, false);//切换action时把didQuery设为false
                actionname = actionNameList.get(position);
                actionid = actionIdList.get(position);
                preferenceUtil.putValues("actionid", actionid);//把id写入preference
                preferenceUtil.putValues("actionname", actionname);//把name写入preference
                myMainActivity.menu.showContent();
                refreshActionListener.onChangeTitle(actionname);//回调MainActivity的方法修改title
                refreshActionListener.refreshAction();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_about: {
                //TODO:弹出AlertDialog展示软件相关信息
                break;
            }
            case R.id.btn_changepassword: {
                //打开SMSSDK修改密码
                changePassword();
                break;
            }
            case R.id.btn_logout: {
                //将didRemembered置为false，并跳转到登录界面，把主界面finish()
                (new PreferenceUtil(myMainActivity)).putValues("didRemembered", String.valueOf(false));
                startActivity(new Intent(myMainActivity, LoginActivity.class));
                preferenceUtil.clear();
                getActivity().finish();
                break;
            }
        }
    }

    public class LoadTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            SQLUtil sqlUtil = new SQLUtil();

            actionNameList.clear();//查询前清空list
            actionIdList.clear();

            String sql = "select actionid,actionname from actioninfo where actionid in (select actionid from useraction where phonenumber=?)";
            List<Object> param = new ArrayList<Object>();
            param.add(phoneNumber);
            try {
                List<Map<String, Object>> list = sqlUtil.queryResults(sql, param);
                Log.d("ASYNC_TASK", list.toString());
                if (list.size() != 0) {
                    for (int i = 0; i < list.size(); i++) {
                        Map<String, Object> map = list.get(i);
                        String id = String.valueOf(map.get("actionid"));
                        String name = String.valueOf(map.get("actionname"));
                        actionIdList.add(id);
                        actionNameList.add(name);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            initActionList();
        }
    }

    /**
     * 打开SMSSDK修改密码
     */
    public void changePassword(){
        RegisterPage registerPage = new RegisterPage();
        registerPage.setRegisterCallback(new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                // 解析注册结果
                if (result == SMSSDK.RESULT_COMPLETE) {
                    //以当前登录用户的phoneNumber和非新账户标记进入密码注册界面
                    HashMap<String, Object> phoneMap = (HashMap<String, Object>) data;
                    String phone = (String) phoneMap.get("phone");
                    boolean newAccount = false;
                    Intent intent = new Intent(myMainActivity, RegisterPasswordActivity.class);
                    intent.putExtra("phoneNumber", phone);
                    intent.putExtra("newAccount", newAccount);
                    myMainActivity.startActivity(intent);
                }
            }
        });
        //带着当前登录用户的phoneNumber和非新账户标记进入短信验证界面
        Map<String, String> map = new HashMap<String, String>();
        String phoneNumber = (new PreferenceUtil(myMainActivity)).getValue("phoneNumber");
        map.put("phoneNumber", phoneNumber);
        map.put("newAccount", String.valueOf(false));
        registerPage.show(getActivity(), map);
    }

    /**
     * 导航栏标题修改监听器
     */
    public interface RefreshActionListener {
        void onChangeTitle(String title);
        void refreshAction();
    }

}
