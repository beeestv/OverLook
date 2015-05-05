package org.youth.overlook.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import org.youth.overlook.R;
import org.youth.overlook.fragment.MapFragment;
import org.youth.overlook.fragment.MenuFragment;

import cn.smssdk.SMSSDK;


public class MainActivity extends SlidingFragmentActivity {

    private SlidingMenu menu;
    private MainActivity myActivity;
    private MapFragment myContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        SMSSDK.initSDK(this, "69a088afb802", "decbbe716fb138e5f18da2f5d309e576");//初始化短信调用SDK
        
        myActivity = this;

        myContext = new MapFragment();

        // set the Above View
        setContentView(R.layout.activity_frame);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_frame, myContext)
                .commit();

        // set the Behind View
        setBehindContentView(R.layout.slidingmenu_frame);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.slidingmenu_frame, new MenuFragment())
                .commit();
        initSlidingMenu();
    }

    private void initSlidingMenu(){
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
                getActionBar().setDisplayShowTitleEnabled(true);
                Log.d("jdbc","slidingmenu close");
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
                getActionBar().setDisplayShowTitleEnabled(false);
                Log.d("jdcb", "头像和标题隐藏了");
                return true;
            }
            case R.id.action_new:
                inviteMembers();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean inviteMembers(){
        Intent intent = new Intent(myActivity, InvitationActivity.class);
        myActivity.startActivity(intent);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void onStart(){
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

    @Override
    public void onBackPressed() {
        if (menu.isMenuShowing()) {
            menu.showContent();
        } else {
            super.onBackPressed();
        }
    }

}
