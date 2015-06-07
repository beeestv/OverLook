package org.youth.overlook.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import org.youth.overlook.R;
import org.youth.overlook.utils.PreferenceUtil;
import org.youth.overlook.utils.SQLUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WelcomeActivity extends Activity {

    private Context context;
    private PreferenceUtil preferenceUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        context = this;

        int pictures[] = {R.drawable.p1,R.drawable.p2,R.drawable.p3,R.drawable.p4,R.drawable.p5};
        int resId = pictures[(int)(Math.random()*5)];
        ImageView welcomeImage = (ImageView)findViewById(R.id.iv_welcome);
        welcomeImage.setImageResource(resId);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.image_fade_out);

        anim.setFillAfter(false);
        anim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                preferenceUtil = new PreferenceUtil(context);
                String phoneNumber = preferenceUtil.getValue("phonenumber");
                String password = preferenceUtil.getValue("password");
                boolean didRemembered = Boolean.parseBoolean(preferenceUtil.getValue("didRemembered"));
                Log.d("jdbc", "welcome  " + preferenceUtil.getValue("didRemembered"));
                if (phoneNumber != null && password != null && didRemembered == true) {
                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        ImageView iv_welcome = (ImageView) findViewById(R.id.iv_welcome);
        iv_welcome.startAnimation(anim);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
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
}
