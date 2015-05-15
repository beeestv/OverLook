package org.youth.overlook.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import org.youth.overlook.R;
import org.youth.overlook.fragment.MapFragment;
import org.youth.overlook.fragment.MenuFragment;
import org.youth.overlook.utils.PreferenceUtil;

import cn.smssdk.SMSSDK;


public class MainActivity extends SlidingFragmentActivity implements MenuFragment.RefreshActionListener, MapFragment.ActionBarTitleChangeListener {

    private final int REQUEST_INVITATION = 1;

    public SlidingMenu menu;
    private MainActivity myActivity;
    public MapFragment myMapFragment;
    public MenuFragment myMenuFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        myMenuFragment = new MenuFragment();
        myMenuFragment.refreshActionListener = this;
        myMapFragment = new MapFragment();
        myMapFragment.actionBarTitleChangeListener = this;


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
                inviteMembers();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void inviteMembers() {
        Intent intent = new Intent(myActivity, InvitationActivity.class);
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
        myMapFragment.refreshMap();
    }

}
