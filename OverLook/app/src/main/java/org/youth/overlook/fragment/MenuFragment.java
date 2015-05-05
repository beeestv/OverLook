package org.youth.overlook.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.youth.overlook.R;
import org.youth.overlook.activity.LoginActivity;
import org.youth.overlook.activity.RegisterPasswordActivity;
import org.youth.overlook.utils.PreferenceUtil;

import java.util.HashMap;
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

    private Activity myActivity;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myActivity = getActivity();
        View view = inflater.inflate(R.layout.fragment_menu, null);
        ((Button)view.findViewById(R.id.btn_newactivity)).setOnClickListener(this);
        ((Button)view.findViewById(R.id.btn_about)).setOnClickListener(this);
        ((Button)view.findViewById(R.id.btn_changepassword)).setOnClickListener(this);
        ((Button)view.findViewById(R.id.btn_logout)).setOnClickListener(this);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_newactivity:{
                startActivity(new Intent(myActivity, LoginActivity.class));
                Log.d("jdbc", "click btn_newactivity");
                break;
            }
            case R.id.btn_about:{
                Log.d("jdbc", "click btn_about");
                break;
            }
            case R.id.btn_changepassword:{
                //打开注册页面
                RegisterPage registerPage = new RegisterPage();
                registerPage.setRegisterCallback(new EventHandler() {
                    public void afterEvent(int event, int result, Object data) {
                        // 解析注册结果
                        if (result == SMSSDK.RESULT_COMPLETE) {
                            HashMap<String, Object> phoneMap = (HashMap<String, Object>) data;
                            String phone = (String) phoneMap.get("phone");
                            boolean newAccount = false;
                            Intent intent = new Intent(myActivity, RegisterPasswordActivity.class);
                            intent.putExtra("phoneNumber", phone);
                            intent.putExtra("newAccount", newAccount);
                            myActivity.startActivity(intent);
                        }
                    }
                });
                Map<String, String> map = new HashMap<String, String>();
                String phoneNumber = (new PreferenceUtil(myActivity)).getValue("phoneNumber");
                Log.d("jdbc", "changePassword"+phoneNumber);
                map.put("phoneNumber", phoneNumber);
                map.put("newAccount", String.valueOf(false));
                registerPage.show(getActivity(), map);
                Log.d("jdbc", "click btn_changepassword");
                break;
            }
            case R.id.btn_logout:{
                (new PreferenceUtil(myActivity)).putValues("didRemembered", String.valueOf(false));
                Log.d("jdbc", "click btn_logout  "+(new PreferenceUtil(myActivity)).getValue("phoneNumber"));
                startActivity(new Intent(myActivity, LoginActivity.class));
                getActivity().finish();
                Log.d("jdbc", "click btn_logout");
                break;
            }
        }
    }
}
