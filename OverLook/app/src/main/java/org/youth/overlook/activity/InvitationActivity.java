package org.youth.overlook.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.youth.overlook.R;
import org.youth.overlook.fragment.InvitationFragment;

public class InvitationActivity extends Activity implements InvitationFragment.OnFragmentInteractionListener {

    private InvitationFragment invitationFragment;

    public String invitation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle("返回");
        invitation= getIntent().getStringExtra("invitation");
        if(invitation!=null){
            Log.d("jdbc", getIntent().getStringExtra("invitation"));
            EditText actionname = (EditText)findViewById(R.id.action_name);
            actionname.setHint("无需填写活动名称");
            actionname.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_invitation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_complete:
                invitationFragment = (InvitationFragment) getFragmentManager().findFragmentById(R.id.contact_fragment);
                String actionname = invitationFragment.actionName.getText().toString();
                if(invitation==null && (actionname == null || actionname.length()==0)){
                    Toast.makeText(this, "活动名称不可为空", Toast.LENGTH_SHORT).show();
                }else {
                    invitationFragment.buildAction();
                    item.setEnabled(false);//点击后按钮不可用，等待数据库响应
                    invitationFragment.showDialog();
                }
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFragmentInteraction(String id) {

    }
}
