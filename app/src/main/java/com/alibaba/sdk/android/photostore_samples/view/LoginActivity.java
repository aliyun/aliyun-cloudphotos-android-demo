/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alibaba.sdk.android.photostore.PhotoStoreClient;
import com.alibaba.sdk.android.photostore.model.User;
import com.alibaba.sdk.android.photostore_samples.BusProvider;
import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.constants.Constants;
import com.alibaba.sdk.android.photostore_samples.controller.AccountController;
import com.alibaba.sdk.android.photostore_samples.event.OnLoginEvent;
import com.alibaba.sdk.android.photostore_samples.util.PreferenceManager;
import com.squareup.otto.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class LoginActivity extends AppCompatActivity {

    String TAG = LoginActivity.class.getSimpleName();

    private Handler handler = new Handler(Looper.getMainLooper());

    private boolean bAK = false;

    @BindView(R.id.ll_intro)
    LinearLayout llIntro;

    @BindView(R.id.ll_login_ak)
    LinearLayout llLoginAK;

    @BindView(R.id.tv_store_name)
    EditText mStoreName;

    @BindView(R.id.tv_library_id)
    EditText mLibraryId;

    @BindView(R.id.tv_app_key)
    EditText mAppKey;

    @BindView(R.id.tv_app_secret)
    EditText mAppSecret;

    @BindView(R.id.btn_login_ak)
    Button mLoginAKBtn;

    Unbinder unbinder;

    int stack = 0;
    private int mCurrentEnvIndex = 0;
    private final String ENV[] = new String[] {"pre_release", "release"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        unbinder = ButterKnife.bind(this);

        SharedPreferences sharedPreferences = PreferenceManager.getSharedPref(this);
        String currentEnv = sharedPreferences.getString(Constants.PREF_ENV, "");
        if(ENV[0].equals(currentEnv)) {
            mCurrentEnvIndex = 0;
        } else {
            mCurrentEnvIndex = 1;
        }

        mLoginAKBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tryLogin();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onBackPressed() {
        if (stack > 0) {
            stack--;
            toggle(false, false);
        } else {
            super.onBackPressed();
        }
    }

    @OnClick(R.id.rl_ak)
    public void loginAK() {
        if (stack <= 0) {
            stack++;
            toggle(true, true);
        }
    }

    void toggle(final boolean showLogin, final boolean bAK) {
        llIntro.animate()
                .setInterpolator(new DecelerateInterpolator())
                .alpha(showLogin ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                llIntro.setVisibility(showLogin ? View.GONE : View.VISIBLE);
            }
        });
        if (showLogin) {
            this.bAK = bAK;
            if (bAK) {
                llLoginAK.animate()
                        .setInterpolator(new DecelerateInterpolator())
                        .alpha(1)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                llLoginAK.setVisibility(View.VISIBLE);
                            }
                        });
            }
            else {
                llLoginAK.animate()
                        .setInterpolator(new DecelerateInterpolator())
                        .alpha(0)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                llLoginAK.setVisibility(View.GONE);
                            }
                        });
            }
        }
        else {
            llLoginAK.animate()
                    .setInterpolator(new DecelerateInterpolator())
                    .alpha(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            llLoginAK.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private void tryLogin() {
        if (bAK) {
            String storeName = mStoreName.getText().toString();
            String libraryId = mLibraryId.getText().toString();
            String appKey = mAppKey.getText().toString();
            String appSecret = mAppSecret.getText().toString();
            if (!TextUtils.isEmpty(storeName)
                    && !TextUtils.isEmpty(libraryId)
                    && !TextUtils.isEmpty(appKey)
                    && !TextUtils.isEmpty(appSecret)) {
                PreferenceManager.putBoolean(this, Constants.PREF_PRES_LOGIN, false);
                PreferenceManager.putString(this, Constants.PREF_STORE_NAME, storeName);
                PhotoStoreClient.getInstance().setStoreName(storeName);
                PhotoStoreClient.getInstance().setLibraryId(libraryId);
                PhotoStoreClient.getInstance().setSecurityInfo(appKey, appSecret);
                AccountController.getInstance().getQuota();
                User user = new User();
                user.kp = libraryId + appKey;
                user.accessKeyId = appKey;
                user.accessKeySecret = appSecret;
                user.stsToken = libraryId;
                BusProvider.getInstance().post(new OnLoginEvent(1, "", user));
            }
        }
    }

    @Subscribe
    public void onLogin(OnLoginEvent event) {
        Log.d(TAG, "onLogin");

        if (event.code == 0 && event.user != null) {
            PreferenceManager.putString(this, Constants.PREF_UID, event.user.kp);
            PreferenceManager.putString(this, Constants.PREF_APP_KEY, event.user.accessKeyId);
            PreferenceManager.putString(this, Constants.PREF_APP_SECRET, event.user.accessKeySecret);
            PreferenceManager.putString(this, Constants.PREF_STS_TOKEN, event.user.stsToken);

            setResult(RESULT_OK);
            finish();
        }
        else if (event.code == 1 && event.user != null) {
            PreferenceManager.putString(this, Constants.PREF_UID, event.user.kp);
            PreferenceManager.putString(this, Constants.PREF_APP_KEY, event.user.accessKeyId);
            PreferenceManager.putString(this, Constants.PREF_APP_SECRET, event.user.accessKeySecret);
            PreferenceManager.putString(this, Constants.PREF_LIBRARY_ID, event.user.stsToken);

            setResult(RESULT_OK);
            finish();
        } else {

            if (!TextUtils.isEmpty(event.msg)) {
                Toast.makeText(this, event.msg, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

}

