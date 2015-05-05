package org.youth.overlook.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.youth.overlook.R;
import org.youth.overlook.utils.SQLUtil;
import org.youth.overlook.utils.PreferenceUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;


public class LoginActivity extends Activity {

    private final int CANLOGIN = 0;
    private final int LOGINFAILED = 1;

    private PreferenceUtil preferenceUtil;
    private SQLUtil SQLUtil;
    private EditText et_phonenumber;
    private EditText et_password;
    private ProgressDialog progDialog;
    private boolean canLogin;
    private String phoneNumber;
    private String password;
    private Context myContext;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CANLOGIN:
                    if (canLogin == true) {
                        dismissProgressDialog();
                        preferenceUtil.putValues("didRemembered", String.valueOf(true));
                        preferenceUtil.putValues("phoneNumber", phoneNumber);
                        preferenceUtil.putValues("password", password);
                        Log.d("jdbc", preferenceUtil.getValue("phoneNumber"));
                        Log.d("jdbc", preferenceUtil.getValue("password"));
                        Intent intent = new Intent(myContext, MainActivity.class);
                        myContext.startActivity(intent);
                        finish();
                    }
                    break;
                case LOGINFAILED:
                    dismissProgressDialog();
                    Toast.makeText(myContext, "账号或密码错误，请重新输入", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findView();
        init();
    }

    private void findView() {
        myContext = this;
        et_phonenumber = (EditText) findViewById(R.id.et_phonenumber_login);
        et_password = (EditText) findViewById(R.id.et_password_login);
    }

    private void init() {
        SQLUtil = new SQLUtil();
        preferenceUtil = new PreferenceUtil(myContext);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.login_anim);
        anim.setFillAfter(true);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                String info = getIntent().getStringExtra("info");
                if (info != null) {
                    Log.d("jdbc", info);
                    if (info.equals("registerSucceed_intent")) {
                        Toast.makeText(myContext, "注册成功", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        LinearLayout ll_login = (LinearLayout) findViewById(R.id.ll_login);
        ll_login.startAnimation(anim);

        SMSSDK.initSDK(this, "69a088afb802", "decbbe716fb138e5f18da2f5d309e576");//初始化短信调用SDK
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void clickLoginButton(View view) {
        showProgressDialog();

        phoneNumber = et_phonenumber.getText().toString().trim();
        password = et_password.getText().toString().trim();
        String sql = "select password from userinfo where phonenumber=?";
        List<Object> params = new ArrayList<Object>();
        params.add(phoneNumber);

        query(sql, params);
    }

    public void clickRegisterButton(View view) {
        //打开注册页面
        RegisterPage registerPage = new RegisterPage();
        registerPage.setRegisterCallback(new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                // 解析注册结果
                if (result == SMSSDK.RESULT_COMPLETE) {
                    HashMap<String, Object> phoneMap = (HashMap<String, Object>) data;
                    String phone = (String) phoneMap.get("phone");
                    boolean newAccount = Boolean.valueOf(phoneMap.get("newAccount").toString());
                    Intent intent = new Intent(myContext, RegisterPasswordActivity.class);
                    intent.putExtra("phoneNumber", phone);
                    intent.putExtra("newAccount", newAccount);
                    myContext.startActivity(intent);
                }
            }
        });

        registerPage.show(myContext);
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setMessage("正在登陆...");
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dismissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    public void query(final String sql, final List<Object> params) {
        new Thread() {
            public void run() {
                Looper.prepare();
                try {
                    List<Map<String, Object>> list = SQLUtil.queryResults(sql, params);
                    Log.d("jdbc", "query succeed.");
                    Log.d("jdbc", list.toString());
                    Log.d("jdbc", String.valueOf(list.size()));

                    Message msg = new Message();

                    if (list.size() != 0) {
                        Map<String, Object> map = list.get(0);
                        if (password.equals(String.valueOf(map.get("password")))) {
                            String sql = "update userinfo set latestlogin=? where phonenumber=?";
                            List<Object> params = new ArrayList<Object>();
                            params.add(new Date());
                            params.add(phoneNumber);
                            update(sql, params);

                            canLogin = true;
                            msg.what = CANLOGIN;
                            handler.sendMessage(msg);
                        } else {
                            msg.what = LOGINFAILED;
                            handler.sendMessage(msg);
                            Log.d("jdbc", "密码错误");
                        }
                    } else {
                        msg.what = LOGINFAILED;
                        handler.sendMessage(msg);
                        Log.d("jdbc", "账号不存在");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    dismissProgressDialog();
                    Toast.makeText(myContext, "验证出错", Toast.LENGTH_LONG).show();
                }
            }
        }.start();
    }

    public void update(final String sql, final List<Object> params) {
        new Thread() {
            public void run() {
                try {
                    SQLUtil.updateByPrepareStatment(sql, params);
                    Log.d("jdbc", "update succeed.");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    protected void onResume() {
        super.onResume();
        canLogin = false;
    }

    protected void onPause() {
        super.onPause();
    }
}
