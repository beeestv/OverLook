package org.youth.overlook.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import org.youth.overlook.R;
import org.youth.overlook.fragment.MapFragment;
import org.youth.overlook.fragment.MenuFragment;
import org.youth.overlook.utils.PreferenceUtil;
import org.youth.overlook.utils.SQLUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.smssdk.SMSSDK;


public class MainActivity extends SlidingFragmentActivity implements MenuFragment.RefreshActionListener, MapFragment.ActionBarTitleChangeListener {

    private final int REQUEST_INVITATION = 1;

    public SlidingMenu menu;
    private MainActivity myActivity;
    public MapFragment myMapFragment;
    public MenuFragment myMenuFragment;

    private SQLUtil sqlUtil = new SQLUtil();
    private PreferenceUtil preferenceUtil;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        myMenuFragment = new MenuFragment();
        myMenuFragment.refreshActionListener = this;
        myMapFragment = new MapFragment();
        myMapFragment.actionBarTitleChangeListener = this;

        preferenceUtil = new PreferenceUtil(this);

        SMSSDK.initSDK(this, "69a088afb802", "decbbe716fb138e5f18da2f5d309e576");//初始化短信调用SDK

        myActivity = this;

        // set the Above View
        setContentView(R.layout.activity_frame);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_frame, myMapFragment)
                .commit();

        // set the Behind View
        setBehindContentView(R.layout.slidingmenu_frame);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.slidingmenu_frame, myMenuFragment)
                .commit();
        initSlidingMenu();
    }

    private void initSlidingMenu() {
        menu = getSlidingMenu();
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        menu.setTouchModeBehind(SlidingMenu.TOUCHMODE_MARGIN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        setSlidingActionBarEnabled(true);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.35f);

        menu.setOnCloseListener(new SlidingMenu.OnCloseListener() {
            @Override
            public void onClose() {
                getActionBar().setDisplayShowHomeEnabled(true);
            }
        });
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
        switch (item.getItemId()) {
            case android.R.id.home: {
                menu.toggle();
                getActionBar().setDisplayShowHomeEnabled(false);
                return true;
            }
            case R.id.action_new:
                if(preferenceUtil.getValue("actionid")==null){
                    new AlertDialog.Builder(myActivity)
                            .setMessage("先进入活动才可邀请成员")
                            .setNegativeButton("确定",null)
                            .show();
                }else{
                    inviteMembers(false);
                }
                return true;
            case R.id.action_quit:
                if(preferenceUtil.getValue("actionid")==null){
                    new AlertDialog.Builder(myActivity)
                            .setMessage("先进入活动才可退出活动")
                            .setNegativeButton("确定",null)
                            .show();
                }else{
                    quitAction();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 退出活动
     */
    public void quitAction(){
        new AlertDialog.Builder(this)
                .setMessage("该操作不可撤销，请确认是否退出活动？")
                .setNegativeButton("否", null)
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AsyncTask<String, Integer, String>() {

                            @Override
                            protected String doInBackground(String... params) {
                                String sql = "select builder from actioninfo where actionid=?";
                                List<Object> param = new ArrayList<Object>();
                                param.add(preferenceUtil.getValue("actionid"));
                                List<Map<String, Object>> list = null;
                                try {
                                    list = sqlUtil.queryResults(sql, param);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                                String builder = String.valueOf(list.get(0).get("builder"));
                                return builder;
                            }

                            @Override
                            protected void onPostExecute(String builder) {
                                //如果登陆用户是欲退出活动的创建者，用对话框提醒用户
                                if (builder.equals(preferenceUtil.getValue("phonenumber"))) {
                                    new AlertDialog.Builder(myActivity)
                                            .setMessage("您是当前活动的创建者，您退出活动将会直接解散活动，是否继续退出？")
                                            .setNegativeButton("否", null)
                                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    myMapFragment.showProgressDialog("清除数据中...");
                                                    new AsyncTask<String, Integer, String>() {

                                                        @Override
                                                        protected String doInBackground(String... params) {
                                                            //删除useraction表中该用户在该活动的记
                                                            String sql = "delete from useraction where actionid=?";
                                                            List<Object> param = new ArrayList<Object>();
                                                            param.add(preferenceUtil.getValue("actionid"));
                                                            try {
                                                                sqlUtil.updateByPrepareStatment(sql, param);
                                                            } catch (SQLException e) {
                                                                e.printStackTrace();
                                                            }
                                                            param.clear();
                                                            //删除actioninfo中该活动的记录
                                                            sql = "delete from actioninfo where actionid=?";
                                                            param = new ArrayList<Object>();
                                                            param.add(preferenceUtil.getValue("actionid"));
                                                            try {
                                                                sqlUtil.updateByPrepareStatment(sql, param);
                                                            } catch (SQLException e) {
                                                                e.printStackTrace();
                                                            }
                                                            return null;
                                                        }

                                                        @Override
                                                        protected void onPostExecute(String s) {
                                                            Log.d("jdbc", "删除活动成功");
                                                            //删除后清空actionid、actionname，清楚云图数据，刷新actionlist和map
                                                            preferenceUtil.putValues("actionid", null);
                                                            preferenceUtil.putValues("actionname", null);
                                                            Map<String, String> params = new HashMap<String, String>();
                                                            myMapFragment.dismissProgressDialog();
                                                            myMapFragment.aMap.clear();
                                                            refreshAction();
                                                            refreshActionList();
                                                        }
                                                    }.execute();
                                                }
                                            })
                                            .show();
                                } else {
                                    myMapFragment.showProgressDialog("清除数据中...");
                                    new AsyncTask<String, Integer, String>() {

                                        @Override
                                        protected String doInBackground(String... params) {
                                            //删除useraction表中该用户在该活动的记
                                            String sql = "delete from useraction where phonenumber=? and actionid=?";
                                            List<Object> param = new ArrayList<Object>();
                                            param.add(preferenceUtil.getValue("phonenumber"));
                                            param.add(preferenceUtil.getValue("actionid"));
                                            try {
                                                sqlUtil.updateByPrepareStatment(sql, param);
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }
                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(String s) {
                                            Log.d("jdbc", "删除活动成功");
                                            //删除后清空actionid、actionname，清楚云图数据，刷新actionlist和map
                                            preferenceUtil.putValues("actionid", null);
                                            preferenceUtil.putValues("actionname", null);
                                            Map<String, String> params = new HashMap<String, String>();
                                            params.put("key", myMapFragment.yuntuKey);
                                            params.put("tableid", myMapFragment.tableId);
                                            params.put("ids", myMapFragment.locationid);
                                            myMapFragment.deleteLocation(params);
                                            myMapFragment.dismissProgressDialog();
                                            myMapFragment.aMap.clear();
                                            refreshAction();
                                            refreshActionList();
                                        }
                                    }.execute();
                                }
                            }
                        }.execute();
                    }
                })
                .show();
    }

    /**
     * 邀请成员
     */
    public void inviteMembers(boolean isNewAction) {
        Intent intent = new Intent(myActivity, InvitationActivity.class);
        if(!isNewAction){
            intent.putExtra("invitation","1");
        }
        myActivity.startActivityForResult(intent, REQUEST_INVITATION);
    }

    /**
     * InvitationActivty调用finish()方法后调用
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_INVITATION) {
            if (resultCode == REQUEST_INVITATION) {
                refreshActionList();
            }
        }
    }

    /**
     * 通过MenuFragment的异步任务刷新ActionList
     */
    public void refreshActionList(){
        Log.d("jdbc", "refreshActionList");
        MenuFragment.LoadTask loadTask = myMenuFragment.new LoadTask();
        loadTask.execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private long lastPressTime;

    /**
     * 返回键调用的方法
     */
    @Override
    public void onBackPressed() {
        if (menu.isMenuShowing()) {
            menu.showContent();
        } else {
            /*两次按键间隔小于3500millis则退出*/
            if (System.currentTimeMillis() - lastPressTime > 2000) {
                Toast.makeText(myActivity, "再次点击返回键退出", Toast.LENGTH_SHORT).show();
                lastPressTime = System.currentTimeMillis();
            } else {
                System.exit(0);
            }
        }
    }

    @Override
    public void onChangeTitle(String title) {
        getActionBar().setTitle(title);
        myMapFragment.actionname = title;
    }

    @Override
    public void refreshAction() {
        Log.d("jdbc", "refreshAction");
        myMapFragment.refreshMap();
    }

}
