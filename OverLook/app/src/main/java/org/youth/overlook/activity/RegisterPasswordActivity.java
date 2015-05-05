package org.youth.overlook.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.youth.overlook.R;
import org.youth.overlook.utils.SQLUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class RegisterPasswordActivity extends Activity {

    private SQLUtil SQLUtil;
    private Context myContext;
    private EditText et_password;
    private EditText et_password_verification;
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            Intent intent = new Intent(myContext, LoginActivity.class);
            intent.putExtra("info","registerSucceed_intent");
            myContext.startActivity(intent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_password);
        findView();
        init();
    }

    private void findView() {
        myContext = this;
        et_password = (EditText) findViewById(R.id.et_password_register);
        et_password_verification = (EditText) findViewById(R.id.et_password_verification);
    }

    private void init() {
        SQLUtil = new SQLUtil();
        ActionBar actionBar = this.getActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register_password, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void clickCompleteButton(View view) {

        String password = et_password.getText().toString().trim();
        String passwordVerification = et_password_verification.getText().toString().trim();

        if (!password.equals("") && password.equals(passwordVerification)) {
            if(getIntent().getBooleanExtra("newAccount",true) ) {
                String sql = "insert into userinfo(phonenumber,password,registerdate) values(?,?,?)";
                List<Object> params = new ArrayList<Object>();
                params.add(getIntent().getStringExtra("phoneNumber"));
                params.add(password);
                params.add(new Date());

                insert(sql, params);
            }else {
                String sql = "update userinfo set password=? where phonenumber=?";
                List<Object> params = new ArrayList<Object>();
                params.add(password);
                params.add(getIntent().getStringExtra("phoneNumber"));
                insert(sql, params);
            }
        } else {
            Toast.makeText(this, "密码不一致，请重新输入", Toast.LENGTH_SHORT).show();
            et_password.setText(null);
            et_password_verification.setText(null);
        }
    }

    public void insert(final String sql, final List<Object> params){
        new Thread(){
            public void run(){
                try {
                    SQLUtil.updateByPrepareStatment(sql, params);
                    handler.sendMessage(new Message());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
