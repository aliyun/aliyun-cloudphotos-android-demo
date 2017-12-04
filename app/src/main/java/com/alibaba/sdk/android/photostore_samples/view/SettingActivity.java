/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;

import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.model.DatabaseCallback;
import com.alibaba.sdk.android.photostore_samples.model.MySetting;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SettingActivity extends AppCompatActivity {

    @BindView(R.id.tv_setting_share_expire_time)
    EditText tvShareExpireTime;

    Handler handler = new Handler(Looper.getMainLooper());
    Unbinder unbinder;
    public static void launch(Context context) {
        Intent i = new Intent(context, SettingActivity.class);
        Bundle bundle = new Bundle();
        i.putExtras(bundle);
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        unbinder = ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        MySetting.getValue("ShareExpireDays", new DatabaseCallback<MySetting>() {
            @Override
            public void onCompleted(int code, List<MySetting> data) {
                String days = "0";
                if (data != null && data.size() > 0) {
                    days = data.get(0).value;

                }
                final String showDays = days;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvShareExpireTime.setText(showDays);
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        final String days = tvShareExpireTime.getText().toString();
        new Thread(new Runnable() {
            @Override
            public void run() {
                MySetting.update("ShareExpireDays", days, null);
            }
        }).start();
        super.onDestroy();
        unbinder.unbind();
    }
}
