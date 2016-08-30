package com.example.lenovo.gupshup.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.lenovo.gupshup.R;
import com.example.lenovo.gupshup.firebase.FcmId;
import com.example.lenovo.gupshup.fragments.ContactList;
import com.example.lenovo.gupshup.fragments.RecentChats;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static MainActivity mainActivity;
    private ViewPager pager;
    private TabLayout layout;
    private OnBackPressedListener backListenerForFrag1;
    private OnBackPressedListener backListenerForFrag2;

    public void setBackListenerForFrag2(OnBackPressedListener backListenerForFrag2) {
        this.backListenerForFrag2 = backListenerForFrag2;
    }

    public void setBackListenerForFrag1(OnBackPressedListener backListenerForFrag1) {
        this.backListenerForFrag1 = backListenerForFrag1;
    }

    //TODO: avoid notifydatasetchanged. Use alternatives instead.
    //TODO: implement multi-delete in messages and also create its DB
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isUserExisting();
        //noinspection ConstantConditions
        getSupportActionBar().setElevation(0);

        layout = (TabLayout) findViewById(R.id.head);
        pager = (ViewPager) findViewById(R.id.my_pager);
        pager.setOffscreenPageLimit(1);

        pager.setAdapter(new MyPager(getSupportFragmentManager()));

        layout.setBackgroundColor((ContextCompat.getColor(this, R.color.colorPrimary)));
        layout.setTabTextColors(Color.WHITE, Color.WHITE);
        layout.setupWithViewPager(pager);
    }

    public static MainActivity getInstance() {
        if (mainActivity == null) mainActivity = new MainActivity();
        return mainActivity;
    }

    class MyPager extends FragmentPagerAdapter {

        public MyPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Recents";
                case 1:
                    return "Contacts";
            }
            return super.getPageTitle(position);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:

                    return RecentChats.getInstance();
                case 1:
                    return ContactList.getInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }
    }

    public interface OnBackPressedListener {
        boolean doBack();
    }

    @Override
    public void onBackPressed() {
        if (backListenerForFrag2==null || backListenerForFrag1==null) super.onBackPressed();
        else if (!backListenerForFrag1.doBack() && !backListenerForFrag2.doBack()) super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        backListenerForFrag1 = null;
    }

    private void isUserExisting() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                SharedPreferences preferences = getSharedPreferences(
                        FcmId.FCM_TOKEN_SAVE,
                        MODE_PRIVATE
                );

                String name = preferences.getString(LoginActivity.USER_NAME, "");
                String phone = preferences.getString(LoginActivity.USER_PHONE, "");
                String email = preferences.getString(LoginActivity.USER_EMAIL, "");
                String token = preferences.getString(LoginActivity.USER_TOKEN, "");

                Log.d(TAG, "Name" + name.isEmpty());
                Log.d(TAG, "phone" + phone.isEmpty());
                Log.d(TAG, "email" + email.isEmpty());
                Log.d(TAG, "token" + token.isEmpty());

                if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || token.isEmpty()) {
                    Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Log.d(TAG, "run: int2");
                    startActivity(intent);
                }
            }
        }).start();

    }
}
