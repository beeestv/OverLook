/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2014年 mob.com. All rights reserved.
 */
package cn.smssdk.gui;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mob.tools.FakeActivity;

import cn.smssdk.EventHandler;
import cn.smssdk.OnSendMessageHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.UserInterruptException;

import static com.mob.tools.utils.R.getBitmapRes;
import static com.mob.tools.utils.R.getIdRes;
import static com.mob.tools.utils.R.getLayoutRes;
import static com.mob.tools.utils.R.getStringRes;
import static com.mob.tools.utils.R.getStyleRes;

/**
 * 短信注册页面
 */
public class RegisterPage extends FakeActivity implements OnClickListener,
        TextWatcher {

    private int index = 0;

    // 默认使用中国区号
    private static final String DEFAULT_COUNTRY_ID = "42";

    private EventHandler callback;

    // 国家
    private TextView tvCountry;
    // 手机号码
    private EditText etPhoneNum;
    // 国家编号
    private TextView tvCountryNum;
    // clear 号码
    private ImageView ivClear;
    // 下一步按钮
    private Button btnNext;

    private String currentId;
    private String currentCode;
    private EventHandler handler;
    // 国家号码规则
    private HashMap<String, String> countryRules;
    private Dialog pd;
    private OnSendMessageHandler osmHandler;

    private String phone;
    private String code;

    private final String DRIVER = "com.mysql.jdbc.Driver";
    private String USERNAME = "root";
    private String PASSWORD = "rbi0Npvwlyxc";
    private String URL = "jdbc:mysql://121.43.234.220:3306/overlook";
    private Connection connection;
    private PreparedStatement pstmt;
    private ResultSet resultSet;
    public boolean newAccount;
    private ProgressDialog progDialog;
    private Map<String, String> map;

    public void setRegisterCallback(EventHandler callback) {
        this.callback = callback;
    }

    public void setOnSendMessageHandler(OnSendMessageHandler h) {
        osmHandler = h;
    }

    public void show(Context context) {
        super.show(context, null);
        this.newAccount = true;
    }

    public void show(Context context, Map<String, String> map) {
        super.show(context, null);
        this.map = map;
        this.newAccount = Boolean.getBoolean(map.get("newAccount"));
    }

    public void onCreate() {
        int resId = getLayoutRes(activity, "smssdk_regist_page");
        if (resId > 0) {
            activity.setContentView(resId);
            currentId = DEFAULT_COUNTRY_ID;

            resId = getIdRes(activity, "ll_back");
            View llBack = activity.findViewById(resId);
            resId = getIdRes(activity, "tv_title");
            TextView tv = (TextView) activity.findViewById(resId);
            resId = getStringRes(activity, "smssdk_regist");
            if (resId > 0) {
                tv.setText(resId);
            }
            resId = getIdRes(activity, "rl_country");
            View viewCountry = activity.findViewById(resId);
            resId = getIdRes(activity, "btn_next");
            btnNext = (Button) activity.findViewById(resId);

            resId = getIdRes(activity, "tv_country");
            tvCountry = (TextView) activity.findViewById(resId);

            String[] country = getCurrentCountry();
            // String[] country = SMSSDK.getCountry(currentId);
            if (country != null) {
                currentCode = country[1];
                code = currentCode;
                tvCountry.setText(country[0]);
            }
            resId = getIdRes(activity, "tv_country_num");
            tvCountryNum = (TextView) activity.findViewById(resId);

            tvCountryNum.setText("+" + currentCode);

            resId = getIdRes(activity, "et_write_phone");
            etPhoneNum = (EditText) activity.findViewById(resId);
            if (map != null) {
                String phonenumber = map.get("phoneNumber");
                if (phonenumber != null) {
                    etPhoneNum.setText(phonenumber);
                }
            }
            etPhoneNum.addTextChangedListener(this);
            etPhoneNum.requestFocus();
            if (etPhoneNum.getText().length() > 0) {
                btnNext.setEnabled(true);
                resId = getIdRes(activity, "iv_clear");
                ivClear = (ImageView) activity.findViewById(resId);
                ivClear.setVisibility(View.VISIBLE);
                resId = getBitmapRes(activity, "smssdk_btn_enable");
                if (resId > 0) {
                    btnNext.setBackgroundResource(resId);
                }
            }

            resId = getIdRes(activity, "iv_clear");
            ivClear = (ImageView) activity.findViewById(resId);

            llBack.setOnClickListener(this);
            btnNext.setOnClickListener(this);
            ivClear.setOnClickListener(this);
            viewCountry.setOnClickListener(this);

            handler = new EventHandler() {
                @SuppressWarnings("unchecked")
                public void afterEvent(final int event, final int result,
                                       final Object data) {
                    runOnUIThread(new Runnable() {
                        public void run() {
                            if (pd != null && pd.isShowing()) {
                                pd.dismiss();
                            }
                            if (result == SMSSDK.RESULT_COMPLETE) {
                                if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                                    // 请求支持国家列表
                                    onCountryListGot((ArrayList<HashMap<String, Object>>) data);
                                    View view = new View(activity);
                                    view.setId(getIdRes(activity, "btn_next"));
                                    Log.d("jdbc", "请求国家代码结束");
                                    onClick(view);
                                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                                    // 请求验证码后，跳转到验证码填写页面
                                    afterVerificationCodeRequested();
                                }
                            } else {
                                if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE
                                        && data != null
                                        && (data instanceof UserInterruptException)) {
                                    // 由于此处是开发者自己决定要中断发送的，因此什么都不用做
                                    return;
                                }

                                // 根据服务器返回的网络错误，给toast提示
                                try {
                                    ((Throwable) data).printStackTrace();
                                    Throwable throwable = (Throwable) data;

                                    JSONObject object = new JSONObject(
                                            throwable.getMessage());
                                    String des = object.optString("detail");
                                    if (!TextUtils.isEmpty(des)) {
                                        Toast.makeText(activity, des,
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                // 如果木有找到资源，默认提示
                                int resId = getStringRes(activity,
                                        "smssdk_network_error");
                                if (resId > 0) {
                                    Toast.makeText(activity, resId,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            };
        }

        //获取国家代码信息
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
        pd = new ProgressDialog(activity);
        ((ProgressDialog)pd).setMessage("正在获取国家和地区码...");
        if (pd != null) {
            pd.show();
        }

        SMSSDK.getSupportedCountries();
    }

    private String[] getCurrentCountry() {
        String mcc = getMCC();
        String[] country = null;
        if (!TextUtils.isEmpty(mcc)) {
            country = SMSSDK.getCountryByMCC(mcc);
        }

        if (country == null) {
            Log.w("SMSSDK", "no country found by MCC: " + mcc);
            country = SMSSDK.getCountry(DEFAULT_COUNTRY_ID);
        }
        return country;
    }

    private String getMCC() {
        TelephonyManager tm = (TelephonyManager) activity
                .getSystemService(Context.TELEPHONY_SERVICE);
        // 返回当前手机注册的网络运营商所在国家的MCC+MNC. 如果没注册到网络就为空.
        String networkOperator = tm.getNetworkOperator();

        // 返回SIM卡运营商所在国家的MCC+MNC. 5位或6位. 如果没有SIM卡返回空
        String simOperator = tm.getSimOperator();

        String mcc = null;
        if (!TextUtils.isEmpty(networkOperator)
                && networkOperator.length() >= 5) {
            mcc = networkOperator.substring(0, 3);
        }

        if (TextUtils.isEmpty(mcc)) {
            if (!TextUtils.isEmpty(simOperator) && simOperator.length() >= 5) {
                mcc = simOperator.substring(0, 3);
            }
        }

        return mcc;
    }

    public void onResume() {
        SMSSDK.registerEventHandler(handler);
    }

    public void onPause() {
        SMSSDK.unregisterEventHandler(handler);
    }

    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            btnNext.setEnabled(true);
            ivClear.setVisibility(View.VISIBLE);
            int resId = getBitmapRes(activity, "smssdk_btn_enable");
            if (resId > 0) {
                btnNext.setBackgroundResource(resId);
            }
        } else {
            btnNext.setEnabled(false);
            ivClear.setVisibility(View.GONE);
            int resId = getBitmapRes(activity, "smssdk_btn_disenable");
            if (resId > 0) {
                btnNext.setBackgroundResource(resId);
            }
        }
    }

    public void afterTextChanged(Editable s) {

    }

    public void onClick(View v) {
        int id = v.getId();
        int id_ll_back = getIdRes(activity, "ll_back");
        int id_rl_country = getIdRes(activity, "rl_country");
        int id_btn_next = getIdRes(activity, "btn_next");
        int id_iv_clear = getIdRes(activity, "iv_clear");

        if (id == id_ll_back) {
            finish();
        } else if (id == id_rl_country) {
            // 国家列表
            CountryPage countryPage = new CountryPage();
            countryPage.setCountryId(currentId);
            countryPage.setCountryRuls(countryRules);
            countryPage.showForResult(activity, null, this);
        } else if (id == id_btn_next) {
            showProgressDialog();
            new Thread() {
                public void run() {
                    Log.d("jdbc", "点击后"+index++);
                    Looper.prepare();
                    phone = etPhoneNum.getText().toString().trim()
                            .replaceAll("\\s*", "");
                    code = tvCountryNum.getText().toString().trim();
                    if (checkPhoneNum(phone, code)) {
                        Log.d("jdbc", "进入检测后"+index++);
                        Log.d("jdbc", "号码检测成功"+phone);

                        Log.d("jdbc", "手机格式正确");
                        if (code.startsWith("+")) {
                            code = code.substring(1);
                        }
                        try {
                            Class.forName(DRIVER);
                            Log.d("jdbc", "Register driver succeed.");
                            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                            Log.d("jdbc", "Connect succeed.");
                            Map<String, Object> map = new HashMap<String, Object>();
                            String sql = "select phonenumber from userinfo where phonenumber=?";
                            List<Object> params = new ArrayList<Object>();
                            params.add(etPhoneNum.getText().toString());
                            int index = 1;
                            pstmt = connection.prepareStatement(sql);
                            if (params != null && !params.isEmpty()) {
                                for (int i = 0; i < params.size(); i++) {
                                    pstmt.setObject(index++, params.get(i));
                                }
                            }
                            resultSet = pstmt.executeQuery();
                            ResultSetMetaData metaData = resultSet.getMetaData();
                            Log.d("jdbc", "查询后"+index++);
                            if (metaData != null) {
                                Log.d("jdbc", "查询结果不为空");
                                if (resultSet.next()) {
                                    String col_name = metaData.getColumnName(1);
                                    Object col_value = resultSet.getObject(col_name);
                                    Log.d("jdbc", "账号存在");
                                    newAccount = false;
                                    dismissProgressDialog();
                                    showDialog_changePassword(phone, code);
                                }else {
                                    dismissProgressDialog();
                                    Log.d("jdbc", "结果不为空，但里面没数据");
                                    Log.d("jdbc", "账号不存在");
                                    dismissProgressDialog();
                                    showDialog(phone, code);
                                }
                            } else {
                                Log.d("jdbc", "手机格式错误");
                                dismissProgressDialog();
                                showDialog(phone, code);
                            }
                        } catch (Exception e) {
                        }
                    } else {
                        dismissProgressDialog();
                    }
                    Looper.loop();
                }
            }.start();
        } else if (id == id_iv_clear) {
            // 清除电话号码输入框
            etPhoneNum.getText().clear();
        }
    }

    @SuppressWarnings("unchecked")
    public void onResult(HashMap<String, Object> data) {
        if (data != null) {
            int page = (Integer) data.get("page");
            if (page == 1) {
                // 国家列表返回
                currentId = (String) data.get("id");
                countryRules = (HashMap<String, String>) data.get("rules");
                String[] country = SMSSDK.getCountry(currentId);
                if (country != null) {
                    currentCode = country[1];
                    tvCountryNum.setText("+" + currentCode);
                    tvCountry.setText(country[0]);
                }
            } else if (page == 2) {
                // 验证码校验返回
                Object res = data.get("res");
                HashMap<String, Object> phoneMap = (HashMap<String, Object>) data
                        .get("phone");
                phoneMap.put("newAccount", newAccount);
                if (res != null && phoneMap != null) {
                    int resId = getStringRes(activity,
                            "smssdk_your_ccount_is_verified");
                    if (resId > 0) {
                        Toast.makeText(activity, resId, Toast.LENGTH_SHORT)
                                .show();
                    }

                    if (callback != null) {
                        callback.afterEvent(
                                SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE,
                                SMSSDK.RESULT_COMPLETE, phoneMap);
                    }
                    finish();
                }
            }
        }
    }

    private void onCountryListGot(ArrayList<HashMap<String, Object>> countries) {
        // 解析国家列表
        for (HashMap<String, Object> country : countries) {
            String code = (String) country.get("zone");
            String rule = (String) country.get("rule");
            if (TextUtils.isEmpty(code) || TextUtils.isEmpty(rule)) {
                continue;
            }

            if (countryRules == null) {
                countryRules = new HashMap<String, String>();
            }
            countryRules.put(code, rule);
        }
    }

    /**
     * 分割电话号码
     */
    private String splitPhoneNum(String phone) {
        StringBuilder builder = new StringBuilder(phone);
        builder.reverse();
        for (int i = 4, len = builder.length(); i < len; i += 5) {
            builder.insert(i, ' ');
        }
        builder.reverse();
        return builder.toString();
    }

    /**
     * 检查电话号码
     */
    private boolean checkPhoneNum(String phone, String code) {
        if (code.startsWith("+")) {
            code = code.substring(1);
        }

        if (TextUtils.isEmpty(phone)) {
            int resId = getStringRes(activity, "smssdk_write_mobile_phone");
            if (resId > 0) {
                Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        Log.d("jdbc", countryRules.toString());
        String rule = countryRules.get(code);
        Pattern p = Pattern.compile(rule);
        Matcher m = p.matcher(phone);
        int resId = 0;
        if (!m.matches()) {
            resId = getStringRes(activity, "smssdk_write_right_mobile_phone");
            if (resId > 0) {
                Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    /**
     * 是否请求发送验证码，对话框
     */
    public void showDialog(final String phone, final String code) {
        int resId = getStyleRes(activity, "CommonDialog");
        if (resId > 0) {
            final String phoneNum = "+" + code + " " + splitPhoneNum(phone);
            final Dialog dialog = new Dialog(getContext(), resId);
            resId = getLayoutRes(activity, "smssdk_send_msg_dialog");
            if (resId > 0) {
                dialog.setContentView(resId);
                resId = getIdRes(activity, "tv_phone");
                ((TextView) dialog.findViewById(resId)).setText(phoneNum);
                resId = getIdRes(activity, "tv_dialog_hint");
                TextView tv = (TextView) dialog.findViewById(resId);
                resId = getStringRes(activity, "smssdk_make_sure_mobile_detail");
                if (resId > 0) {
                    String text = getContext().getString(resId);
                    tv.setText(Html.fromHtml(text));
                }
                resId = getIdRes(activity, "btn_dialog_ok");
                if (resId > 0) {
                    ((Button) dialog.findViewById(resId))
                            .setOnClickListener(new OnClickListener() {
                                public void onClick(View v) {
                                    // 跳转到验证码页面
                                    dialog.dismiss();

                                    if (pd != null && pd.isShowing()) {
                                        pd.dismiss();
                                    }
                                    pd = CommonDialog.ProgressDialog(activity);
                                    if (pd != null) {
                                        pd.show();
                                    }
                                    Log.d("jdbc code", code);
                                    Log.e("verification phone ==>>", phone);
                                    SMSSDK.getVerificationCode(code,
                                            phone.trim(), osmHandler);
                                }
                            });
                }
                resId = getIdRes(activity, "btn_dialog_cancel");
                if (resId > 0) {
                    ((Button) dialog.findViewById(resId))
                            .setOnClickListener(new OnClickListener() {
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });
                }
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
            }
        }
    }

    /**
     * 是否修改密码，对话框
     */
    public void showDialog_changePassword(final String phone, final String code) {
        int resId = getStyleRes(activity, "CommonDialog");
        if (resId > 0) {
            final String phoneNum = "+" + code + " " + splitPhoneNum(phone);
            final Dialog dialog = new Dialog(getContext(), resId);
            resId = getLayoutRes(activity, "smssdk_send_msg_dialog");
            if (resId > 0) {
                dialog.setContentView(resId);
                resId = getIdRes(activity, "tv_phone");
                ((TextView) dialog.findViewById(resId)).setText(phoneNum);
                resId = getIdRes(activity, "tv_dialog_title");
                ((TextView) dialog.findViewById(resId)).setText("账号已存在");
                resId = getIdRes(activity, "tv_dialog_hint");
                TextView tv = (TextView) dialog.findViewById(resId);
                resId = getStringRes(activity, "smssdk_make_sure_mobile_detail");
                if (resId > 0) {
                    String text = "若重置密码，将发送<font color=#499bf7>验证码</font>到号码:";
                    tv.setText(Html.fromHtml(text));
                }
                resId = getIdRes(activity, "btn_dialog_ok");
                if (resId > 0) {
                    ((Button) dialog.findViewById(resId))
                            .setOnClickListener(new OnClickListener() {
                                public void onClick(View v) {
                                    // 跳转到验证码页面
                                    dialog.dismiss();

                                    if (pd != null && pd.isShowing()) {
                                        pd.dismiss();
                                    }
                                    pd = CommonDialog.ProgressDialog(activity);
                                    if (pd != null) {
                                        pd.show();
                                    }
                                    Log.d("jdbc code", code);
                                    Log.e("verification phone ==>>", phone);
                                    SMSSDK.getVerificationCode(code,
                                            phone.trim(), osmHandler);
                                }
                            });
                }
                resId = getIdRes(activity, "btn_dialog_cancel");
                if (resId > 0) {
                    ((Button) dialog.findViewById(resId))
                            .setOnClickListener(new OnClickListener() {
                                public void onClick(View v) {
                                    dialog.dismiss();
                                    finish();
                                }
                            });
                }
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
            }
        }
    }

    /**
     * 请求验证码后，跳转到验证码填写页面
     */
    private void afterVerificationCodeRequested() {
        String phone = etPhoneNum.getText().toString().trim()
                .replaceAll("\\s*", "");
        String code = tvCountryNum.getText().toString().trim();
        if (code.startsWith("+")) {
            code = code.substring(1);
        }
        String formatedPhone = "+" + code + " " + splitPhoneNum(phone);
        // 验证码页面
        IdentifyNumPage page = new IdentifyNumPage();
        page.setPhone(phone, code, formatedPhone);
        page.showForResult(activity, null, this);
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(activity);
        progDialog.setMessage("正在发起网络请求...");
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

}
