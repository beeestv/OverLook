package org.youth.overlook.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

public class WelcomeActivity extends Activity {

    private Context context;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Log.d("jdbc", "initSMS");
        context = this;

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.image_fade_out);

        anim.setFillAfter(false);
        anim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PreferenceUtil preferenceUtil = new PreferenceUtil(context);
                        String phoneNumber = preferenceUtil.getValue("phoneNumber");
                        String password = preferenceUtil.getValue("password");
                        boolean didRemembered = Boolean.parseBoolean(preferenceUtil.getValue("didRemembered"));
                        Log.d("jdbc","welcome  " + preferenceUtil.getValue("didRemembered"));
                        if (phoneNumber != null && password != null && didRemembered == true) {
                            Intent intent = new Intent(context, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            Intent intent = new Intent(context, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                },2000);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

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
