/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.view;

import android.Manifest;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.photostore.PhotoStoreClient;
import com.alibaba.sdk.android.photostore.api.BaseResponse;
import com.alibaba.sdk.android.photostore.api.GetPhotoStoreResponse;
import com.alibaba.sdk.android.photostore.runner.Callback;
import com.alibaba.sdk.android.photostore_samples.BuildConfig;
import com.alibaba.sdk.android.photostore_samples.BusProvider;
import com.alibaba.sdk.android.photostore_samples.MyApplication;
import com.alibaba.sdk.android.photostore_samples.constants.Constants;
import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.constants.ContentType;
import com.alibaba.sdk.android.photostore_samples.constants.FragmentType;
import com.alibaba.sdk.android.photostore_samples.controller.AccountController;
import com.alibaba.sdk.android.photostore_samples.controller.AlbumsController;
import com.alibaba.sdk.android.photostore_samples.controller.DownloadController;
import com.alibaba.sdk.android.photostore_samples.controller.FacesController;
import com.alibaba.sdk.android.photostore_samples.controller.PhotosController;
import com.alibaba.sdk.android.photostore_samples.controller.UploadController;
import com.alibaba.sdk.android.photostore_samples.event.OnFinishActionModeEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnGetQuotaEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnGoToFragmentEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnLogoutEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnStartActionModeEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnUploadStateChangedEvent;
import com.alibaba.sdk.android.photostore_samples.model.MyAlbum;
import com.alibaba.sdk.android.photostore_samples.model.MyPhoto;
import com.alibaba.sdk.android.photostore_samples.model.MyFace;
import com.alibaba.sdk.android.photostore_samples.util.PreferenceManager;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.alibaba.sdk.android.photostore_samples.constants.Constants.INTENT_LOGIN;
import static com.alibaba.sdk.android.photostore_samples.constants.Constants.INTENT_PICK_IMAGE;
import static com.alibaba.sdk.android.photostore_samples.constants.Constants.INTENT_TAKE_IMAGE;
import static com.alibaba.sdk.android.photostore_samples.constants.Constants.PREF_FIRST_TIME;
import static com.alibaba.sdk.android.photostore_samples.constants.Constants.VOICE_SEARCH_CODE;

public class MainActivity extends AppCompatActivity {

    String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.floating_search_view)
    FloatingSearchView searchView;

    @BindView(R.id.rl_assistant)
    RelativeLayout rlAssistant;

    @BindView(R.id.rl_assistant_icon)
    ImageView rlAssistantIcon;

    @BindView(R.id.rl_photos)
    RelativeLayout rlPhotos;

    @BindView(R.id.rl_photos_icon)
    ImageView rlPhotosIcon;

    @BindView(R.id.rl_momentphotos)
    RelativeLayout rlMomentPhotos;

    @BindView(R.id.rl_momentphotos_icon)
    ImageView rlMomentPhotosIcon;

    @BindView(R.id.rl_albums)
    RelativeLayout rlAlbums;

    @BindView(R.id.rl_albums_icon)
    ImageView rlAlbumsIcon;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.ll_tab)
    LinearLayout llTab;

    @BindView(R.id.rl_upload)
    RelativeLayout rlUpload;

    @BindView(R.id.rl_backup)
    RelativeLayout rlBackup;

    @BindView(R.id.tv_count)
    TextView tvCount;

    @BindView(R.id.tv_state)
    TextView tvState;

    @BindView(R.id.tv_about)
    TextView tvAbout;

    @BindView(R.id.tv_quota)
    TextView tvQuota;

    @BindView(R.id.pb_upload)
    ProgressBar pbUpload;

    @BindView(R.id.tv_stop)
    TextView tvStop;

    Unbinder unbinder;

    private boolean isActionMode;
    public static final String REGION_CN_HANGZHOU = "cn-hangzhou";
    public static final String STS_API_VERSION = "2015-04-01";
    public static Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private Uri photoUri;
    /**获取到的图片路径*/
    private String picPath;

    ActionMode actionMode;

    AssistantFragment assistantFragment = null;
    PhotosFragment photosFragment = null;
    MomentsFragment momentsFragment = null;
    AlbumsFragment albumsFragment = null;
    AlbumPhotosFragment albumPhotosFragment = null;
    FacesFragment facesFragment = null;
    FacePhotosFragment facePhotosFragment = null;

    Handler handler = new Handler(Looper.getMainLooper());

    public HashMap<String, Integer> positionMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        assistantFragment = AssistantFragment.newInstance();
        photosFragment = PhotosFragment.newInstance();
        momentsFragment = MomentsFragment.newInstance();
        albumsFragment = AlbumsFragment.newInstance();
        albumPhotosFragment = AlbumPhotosFragment.newInstance();
        facesFragment = FacesFragment.newInstance();
        facePhotosFragment = FacePhotosFragment.newInstance();

        String versionName = BuildConfig.VERSION_NAME;
        tvAbout.append("(" + versionName + ")");

        rlPhotosIcon.setImageResource(R.drawable.ic_photo_activated);

        getSupportFragmentManager().beginTransaction().add(R.id.container, photosFragment).commit();

        searchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {

            }

            @Override
            public void onSearchAction(String currentQuery) {
                Log.d(TAG, "search: " + currentQuery);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit, R.anim.fragment_stack_in, R.anim.fragment_stask_out);

                changeMenu(FragmentType.MOMENTS);

                Bundle bundle = new Bundle();
                MomentsFragment momentsFragment = MomentsFragment.newInstance();
                bundle.putInt("what", ContentType.SEARCH_PHOTO.ordinal());
                bundle.putString("query", currentQuery);
                momentsFragment.setArguments(bundle);
                transaction.add(R.id.container, momentsFragment).addToBackStack(null).commit();

                momentsFragment.what = ContentType.SEARCH_PHOTO.ordinal();
                momentsFragment.query(currentQuery);
            }
        });
        searchView.attachNavigationDrawerToMenuButton(drawerLayout);
        searchView.setOnLeftMenuClickListener(new FloatingSearchView.OnLeftMenuClickListener() {
            @Override
            public void onMenuOpened() {
                drawerLayout.openDrawer(Gravity.START);
            }

            @Override
            public void onMenuClosed() {
                drawerLayout.closeDrawer(Gravity.START);
            }
        });

        setReadonly();

        changeMenu(FragmentType.PHOTOS);

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                // set to null
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.container);
                if (f instanceof PhotosFragment) {
                    PhotosFragment ph = (PhotosFragment) f;
                    if (ph.what == ContentType.COLLAGES.ordinal()) {
                        changeMenu(FragmentType.DEFAULT);
                    } else {
                        changeMenu(FragmentType.PHOTOS);
                    }
                } else {
                    changeMenu(FragmentType.DEFAULT);
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                        Constants.INTENT_PERMISSIONS_REQUEST_STORAGE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                UploadController.getInstance().scan();
            }
        }, 500);

        fillUploadCard();

        Log.d(TAG, "onResume, login");
        login();

        fetchData();
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
        searchView.clearQuery();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.rl_photos)
    void onClickPhotos() {
        rlPhotosIcon.setImageResource(R.drawable.ic_photo_activated);
        rlMomentPhotosIcon.setImageResource(R.drawable.ic_moment_normal);
        rlAlbumsIcon.setImageResource(R.drawable.ic_album_normal);
        rlAssistantIcon.setImageResource(R.drawable.ic_assistant_normal);
        clearFragmentStack();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit);
        photosFragment.what = ContentType.PHOTO.ordinal();
        transaction.replace(R.id.container, photosFragment).commit();
        changeMenu(FragmentType.PHOTOS);
    }

    @OnClick(R.id.rl_momentphotos)
    void onClickMomentPhotos() {
        rlPhotosIcon.setImageResource(R.drawable.ic_photo_normal);
        rlMomentPhotosIcon.setImageResource(R.drawable.ic_moment_activated);
        rlAlbumsIcon.setImageResource(R.drawable.ic_album_normal);
        rlAssistantIcon.setImageResource(R.drawable.ic_assistant_normal);
        clearFragmentStack();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit);
        momentsFragment.what = ContentType.MOMENT_PHOTO.ordinal();
        transaction.replace(R.id.container, momentsFragment).commit();
        changeMenu(FragmentType.MOMENTS);
    }

    @OnClick(R.id.rl_albums)
    void onClickAlbums() {
        rlPhotosIcon.setImageResource(R.drawable.ic_photo_normal);
        rlMomentPhotosIcon.setImageResource(R.drawable.ic_moment_normal);
        rlAlbumsIcon.setImageResource(R.drawable.ic_album_activated);
        rlAssistantIcon.setImageResource(R.drawable.ic_assistant_normal);
        clearFragmentStack();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit);
        transaction.replace(R.id.container, albumsFragment).commit();
        changeMenu(FragmentType.ALBUMS);
    }

    @OnClick(R.id.rl_assistant)
    void onClickAssistant() {
        rlPhotosIcon.setImageResource(R.drawable.ic_photo_normal);
        rlMomentPhotosIcon.setImageResource(R.drawable.ic_moment_normal);
        rlAlbumsIcon.setImageResource(R.drawable.ic_album_normal);
        rlAssistantIcon.setImageResource(R.drawable.ic_assistant_activated);
        clearFragmentStack();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit);
        transaction.replace(R.id.container, assistantFragment).commit();
        changeMenu(FragmentType.ASSISTANT);
    }

    @OnClick(R.id.rl_upload)
    public void onClickUpload() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        Constants.INTENT_PERMISSIONS_REQUEST_STORAGE);
            }
        } else {
            selectImages();
//            takePhoto();
        }
    }

    @OnClick(R.id.rl_backup)
    public void onClickUploadCard() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                        Constants.INTENT_PERMISSIONS_REQUEST_STORAGE);
            }
        } else {
            UploadController.getInstance().startBackup();
        }
    }

    @OnClick(R.id.rl_inactive)
    public void onClickInactivePhotos() {
        InactivePhotosActivity.launch(this);
    }

    boolean loggingOut = false;
    @OnClick(R.id.rl_account_logout)
    public void onClickLogout() {
        if (loggingOut) {
            return;
        }
        loggingOut = true;

        BusProvider.getInstance().post(new OnLogoutEvent(false));

        cleanAndCallLogin();
    }

    void cleanAndCallLogin() {
        PhotoStoreClient.getInstance().logout();
        clearFragmentStack();
        photosFragment = PhotosFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, photosFragment).commit();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                drawerLayout.closeDrawer(Gravity.START);
            }
        }, 500);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loggingOut = false;
                Log.d(TAG, "cleanAndCallLogin, login");
                login();
            }
        }, 1000);
    }

    @OnClick(R.id.tv_stop)
    public void onClickStopBackup() {
        UploadController.getInstance().stopBackup();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.INTENT_PERMISSIONS_REQUEST_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    UploadController.getInstance().scan();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        List<File> images = new ArrayList<>();

        if (requestCode == INTENT_PICK_IMAGE) {
            try {
                if (resultCode == RESULT_OK && null != data) {
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    if (data.getData() != null) {
                        Uri uri = data.getData();

                        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        images.add(new File(cursor.getString(columnIndex)));
                        cursor.close();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            if (data.getClipData() != null) {
                                ClipData mClipData = data.getClipData();
                                for (int i = 0; i < mClipData.getItemCount(); i++) {
                                    ClipData.Item item = mClipData.getItemAt(i);
                                    Uri uri = item.getUri();
                                    Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                                    cursor.moveToFirst();

                                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                    cursor.getString(columnIndex);
                                    cursor.close();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, e.getMessage());
            }

            UploadController.getInstance().upload(images, false);
        } else if (requestCode == INTENT_TAKE_IMAGE) {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(photoUri, filePathColumn, null, null, null);
            cursor.moveToFirst();

            if (filePathColumn.length > 1) {
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                images.add(new File(cursor.getString(columnIndex)));
                cursor.close();
                Log.i(TAG, "imagePath = " + picPath);

                UploadController.getInstance().upload(images, false);
            }

        } else if (requestCode == INTENT_LOGIN) {
            if (resultCode == RESULT_OK) {
                photosFragment = new PhotosFragment();
                momentsFragment = new MomentsFragment();
                assistantFragment = new AssistantFragment();
                albumsFragment = new AlbumsFragment();
                albumPhotosFragment = new AlbumPhotosFragment();
                facesFragment = new FacesFragment();
                facePhotosFragment = new FacePhotosFragment();
                onClickPhotos();
                fetchData();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showHint();
                    }
                }, 500);
            } else {
                finish();
            }
        }
        else if (requestCode == VOICE_SEARCH_CODE && resultCode == RESULT_OK) {
            ArrayList < String > matches = data
                    .getStringArrayListExtra("android.speech.extra.RESULTS");
            searchView.setSearchText(matches.get(0));
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Subscribe
    public void onLogout(OnLogoutEvent event) {
        if (event.isInvalid) {
            Log.d(TAG, "InvalidSecurityToken.Expired");
            if (MyApplication.isPresLogin) {
            }
            else {
                cleanAndCallLogin();
            }
        }
    }

    @Subscribe
    public void onStartActionMode(OnStartActionModeEvent event) {
        startActionMode();
    }

    @Subscribe
    public void onFinishActoinMode(OnFinishActionModeEvent event) {
        actionMode.finish();
    }

    @Subscribe
    public void onUploadStateChange(OnUploadStateChangedEvent event) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                fillUploadCard();
            }
        });
    }

    @Subscribe
    public void onGoToFragment(OnGoToFragmentEvent event) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit, R.anim.fragment_stack_in, R.anim.fragment_stask_out);

        changeMenu(event.whatFragment);

        boolean reused = false;
        Bundle bundle = new Bundle();
        switch (event.whatFragment) {
            case PHOTOS:
                PhotosFragment photosFragment = PhotosFragment.newInstance();
                bundle.putInt("what", event.whatContent.ordinal());
                bundle.putLong("id", event.id);
                if (event.cols > 0) {
                    bundle.putInt("cols", event.cols);
                }
                photosFragment.setArguments(bundle);
                transaction.add(R.id.container, photosFragment).addToBackStack(null).commit();
                break;
            case MOMENTS:
                bundle.putInt("what", event.whatContent.ordinal());
                bundle.putLong("id", event.id);
                if (event.cols > 0) {
                    bundle.putInt("cols", event.cols);
                }
                momentsFragment.setArguments(bundle);
                transaction.add(R.id.container, momentsFragment).addToBackStack(null).commit();
                break;
            case ALBUMS:
                transaction.add(R.id.container, albumsFragment).addToBackStack(null).commit();
                break;
            case ALBUM_PHOTOS:
                bundle.putInt("what", event.whatContent.ordinal());
                bundle.putLong("id", event.id);
                if (event.cols > 0) {
                    bundle.putInt("cols", event.cols);
                }
                albumPhotosFragment.setArguments(bundle);
                transaction.add(R.id.container, albumPhotosFragment).addToBackStack(null).commit();
                break;
            case FACES:
                transaction.add(R.id.container, facesFragment).addToBackStack(null).commit();
                break;
            case FACE_PHOTOS:
                bundle.putInt("what", event.whatContent.ordinal());
                bundle.putLong("id", event.id);
                if (event.cols > 0) {
                    bundle.putInt("cols", event.cols);
                }
                facePhotosFragment.setArguments(bundle);
                transaction.add(R.id.container, facePhotosFragment).addToBackStack(null).commit();
                break;
            case TAGS:
                transaction.add(R.id.container, TagsFragment.newInstance()).addToBackStack(null).commit();
                break;
            case CATEGORY:
                CategoryFragment categoryFragment = CategoryFragment.newInstance();
                bundle.putInt("what", event.whatContent.ordinal());
                bundle.putLong("id", event.id);
                categoryFragment.setArguments(bundle);
                transaction.add(R.id.container, categoryFragment).addToBackStack(null).commit();
                break;
            default:
                Log.d(TAG, "wtf?");
        }
    }

    void takePhoto() {
        String SDState = Environment.getExternalStorageState();
        if (SDState.equals(Environment.MEDIA_MOUNTED))
        {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//"android.media.action.IMAGE_CAPTURE"
            /***
             * 需要说明一下，以下操作使用照相机拍照，拍照后的图片会存放在相册中的
             * 这里使用的这种方式有一个好处就是获取的图片是拍照后的原图
             * 如果不实用ContentValues存放照片路径的话，拍照后获取的图片为缩略图不清晰
             */
            ContentValues values = new ContentValues();
            photoUri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri);
            /**-----------------*/
            startActivityForResult(intent, INTENT_TAKE_IMAGE);
        } else {
            Toast.makeText(this,"内存卡不存在", Toast.LENGTH_LONG).show();
        }
    }

    void startVoiceRecognition() {
        try{
            //通过Intent传递语音识别的模式，开启语音
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            //语言模式和自由模式的语音识别
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            //提示语音开始
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "开始语音");
            //开始语音识别
            startActivityForResult(intent, VOICE_SEARCH_CODE);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "找不到语音设备", 1).show();
        }
    }

    void selectImages() {
        Intent i = new Intent(this, LocalPhotosActivity.class);
        startActivity(i);
    }

    void startActionMode() {
        searchView.animate().translationYBy(-200).alpha(0).start();
        llTab.animate().translationYBy(200).alpha(0).start();

        startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                isActionMode = true;
                actionMode = mode;
                actionMode.setTitle(R.string.action_select);
                getMenuInflater().inflate(R.menu.menu_main_action, menu);
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.container);
                if (f instanceof PhotosFragment) {
                    menu.removeItem(R.id.action_rename);
                    menu.removeItem(R.id.action_setcover);
                    menu.removeItem(R.id.action_move);
                    menu.removeItem(R.id.action_merge);
                    menu.removeItem(R.id.action_setme);
                }
                else if (f instanceof MomentsFragment) {
                    menu.removeItem(R.id.action_rename);
                    menu.removeItem(R.id.action_setcover);
                    menu.removeItem(R.id.action_move);
                    menu.removeItem(R.id.action_merge);
                    menu.removeItem(R.id.action_setme);
                }
                else if (f instanceof AlbumsFragment) {
                    menu.removeItem(R.id.action_download);
                    menu.removeItem(R.id.action_setcover);
                    menu.removeItem(R.id.action_move);
                    menu.removeItem(R.id.action_merge);
                    menu.removeItem(R.id.action_setme);
                }
                else if (f instanceof AlbumPhotosFragment) {
                    menu.removeItem(R.id.action_rename);
                    menu.removeItem(R.id.action_merge);
                    menu.removeItem(R.id.action_setme);
                }
                else if (f instanceof FacesFragment) {
                    menu.removeItem(R.id.action_move);
                    menu.removeItem(R.id.action_setcover);
                    menu.removeItem(R.id.action_download);
                }
                else if (f instanceof FacePhotosFragment) {
                    menu.removeItem(R.id.action_merge);
                    menu.removeItem(R.id.action_rename);
                    menu.removeItem(R.id.action_setme);
                }
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.container);
                if (f instanceof PhotosFragment) {
                    switch (item.getItemId()) {
                        case R.id.action_download:
                            String downloadPath = "Download";
                            DownloadController.getInstance().download(getApplicationContext(), photosFragment.getSelected(), downloadPath);
                            actionMode.finish();
                            break;
                        case R.id.action_delete:
                            PhotosController.getInstance().inactiveCloudPhotos(photosFragment.getSelected());
                            actionMode.finish();
                            break;
                    }
                }
                else if (f instanceof AlbumsFragment) {
                    switch (item.getItemId()) {
                        case R.id.action_delete:
                            AlbumsController.getInstance().deleteAlbums(albumsFragment.getSelected());
                            actionMode.finish();
                            break;
                        case R.id.action_rename:
                            MyAlbum album = albumsFragment.getSelected().get(0);
                            final EditText editText = new EditText(f.getContext());
                            AlertDialog.Builder inputDialog = new AlertDialog.Builder(f.getContext());
                            inputDialog.setTitle(getString(R.string.action_rename)).setView(editText);
                            inputDialog.setPositiveButton(getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            AlbumsController.getInstance().renameAlbum(album.id, editText.getText().toString());
                                        }
                                    }).setNegativeButton(getString(R.string.cancel),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    }).show();
                            actionMode.finish();
                            break;
                    }
                }
                else if (f instanceof AlbumPhotosFragment) {
                    switch (item.getItemId()) {
                        case R.id.action_delete:
                            AlbumsController.getInstance().removePhotos(albumPhotosFragment.getAlbumId(), albumPhotosFragment.getSelected());
                            actionMode.finish();
                            break;
                        case R.id.action_setcover:
                            Long pid = albumPhotosFragment.getSelected().get(0);
                            // TODO:mengzheng photo.isVideo
                            AlbumsController.getInstance().setCover(albumPhotosFragment.getAlbumId(), pid, false);
                            actionMode.finish();
                            break;
                        case R.id.action_download:
                            String downloadPath = "Download";
                            DownloadController.getInstance().download(getApplicationContext(), albumPhotosFragment.getSelected(), downloadPath);
                            actionMode.finish();
                            break;
                        case R.id.action_move:
                            albumPhotosFragment.movePhotosToAlbum();
                            actionMode.finish();
                            break;
                    }
                }
                else if (f instanceof FacesFragment) {
                    switch (item.getItemId()) {
                        case R.id.action_delete:
                            FacesController.getInstance().deleteFaces(facesFragment.getSelected());
                            actionMode.finish();
                            break;
                        case R.id.action_setme:
                            MyFace myF = facesFragment.getSelected().get(0);
                            FacesController.getInstance().setMe(myF.id);
                            actionMode.finish();
                            break;
                        case R.id.action_merge:
                            facesFragment.mergeFaces();
                            actionMode.finish();
                            break;
                        case R.id.action_rename:
                            MyFace face = facesFragment.getSelected().get(0);
                            final EditText editText = new EditText(f.getContext());
                            AlertDialog.Builder inputDialog = new AlertDialog.Builder(f.getContext());
                            inputDialog.setTitle(getString(R.string.action_rename)).setView(editText);
                            inputDialog.setPositiveButton(getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            FacesController.getInstance().rename(face.id, editText.getText().toString());
                                        }
                                    }).setNegativeButton(getString(R.string.cancel),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    }).show();
                            actionMode.finish();
                            break;
                    }

                }
                else if (f instanceof FacePhotosFragment) {
                    switch (item.getItemId()) {
                        case R.id.action_delete:
                            FacesController.getInstance().removePhotos(facePhotosFragment.getFaceId(), facePhotosFragment.getSelected());
                            actionMode.finish();
                            break;
                        case R.id.action_move:
                            facePhotosFragment.movePhotosToFace();
                            actionMode.finish();
                            break;
                        case R.id.action_setcover:
                            MyPhoto photo = facePhotosFragment.getSelected().get(0);
                            FacesController.getInstance().setCover(facePhotosFragment.getFaceId(), photo.id);
                            actionMode.finish();
                            break;
                    }
                }
                else {
                    switch (item.getItemId()) {
                        case R.id.action_delete:
                            PhotosController.getInstance().inactiveCloudPhotos(momentsFragment.getSelected());
                            actionMode.finish();
                            break;
                    }
                }

                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                if (isActionMode) {
                    isActionMode = false;
                    searchView.animate().translationYBy(200).alpha(1).start();
                    llTab.animate().translationYBy(-200).alpha(1).start();
                    BusProvider.getInstance().post(new OnFinishActionModeEvent());
                }
            }
        });
    }

    void fetchData() {
    }

    public void clearFragmentStack() {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    void setReadonly() {
        SharedPreferences sharedPreferences = PreferenceManager.getSharedPref(this);
        String uid = sharedPreferences.getString(Constants.PREF_UID, "");
        String accessToken = sharedPreferences.getString(Constants.PREF_ACCESS_TOKEN, "");
    }

    void login() {
        Log.d(TAG, "login...");
        SharedPreferences sharedPreferences = PreferenceManager.getSharedPref(this);
        String uid = sharedPreferences.getString(Constants.PREF_UID, "");
        String appKey = sharedPreferences.getString(Constants.PREF_APP_KEY, "");
        String appSecret = sharedPreferences.getString(Constants.PREF_APP_SECRET, "");
        String stsToken = sharedPreferences.getString(Constants.PREF_STS_TOKEN, "");
        String libraryId = sharedPreferences.getString(Constants.PREF_LIBRARY_ID, "");
        String storeName = sharedPreferences.getString(Constants.PREF_STORE_NAME, "");
        MyApplication.isPresLogin = sharedPreferences.getBoolean(Constants.PREF_PRES_LOGIN, false);
        MyApplication.event = sharedPreferences.getString(Constants.PREF_LOGIN_EVENT, "");

        String currentEnv = sharedPreferences.getString(Constants.PREF_ENV, "");
        PhotoStoreClient.getInstance().setEnv(currentEnv);

        if (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(libraryId)) {
            PhotoStoreClient.getInstance().setLibraryId(libraryId);
            if (!TextUtils.isEmpty(storeName))
                PhotoStoreClient.getInstance().setStoreName(storeName);
            PhotoStoreClient.getInstance().setSecurityInfo(appKey, appSecret);
            AccountController.getInstance().getQuota();
            PhotoStoreClient.getInstance().getPhotoStore(new Callback<GetPhotoStoreResponse>() {
                @Override
                public void onSuccess(GetPhotoStoreResponse response) {
                    MyApplication.autoCleanDays = response.photoStore.autoCleanDays;
                    MyApplication.autoCleanEnabled = response.photoStore.autoCleanEnabled;
                }

                @Override
                public void onFailure(int code, BaseResponse response) {

                }
            });

        } else if (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(stsToken)) {
            PhotoStoreClient.getInstance().setToken(stsToken);
            PhotoStoreClient.getInstance().setSecurityInfo(appKey, appSecret);
            AccountController.getInstance().getQuota();
            PhotoStoreClient.getInstance().getPhotoStore(new Callback<GetPhotoStoreResponse>() {
                @Override
                public void onSuccess(GetPhotoStoreResponse response) {
                    MyApplication.autoCleanDays = response.photoStore.autoCleanDays;
                    MyApplication.autoCleanEnabled = response.photoStore.autoCleanEnabled;
                }

                @Override
                public void onFailure(int code, BaseResponse response) {

                }
            });

        } else {
            Intent i = new Intent(this, LoginActivity.class);
            startActivityForResult(i, INTENT_LOGIN);
        }
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.container);
    }

    void fillUploadCard() {
        int total = UploadController.getInstance().totalTaskCount.get();
        int finished = UploadController.getInstance().finishedTaskCount.get();

        Log.d(TAG, String.valueOf(finished) + " / " + String.valueOf(total));

        if (UploadController.getInstance().uploading) {
            tvState.setText(R.string.backup_in_progress);
            tvCount.setText(String.valueOf(finished) + " / " + String.valueOf(total));
            pbUpload.setVisibility(View.VISIBLE);
            pbUpload.setProgress((int) ((double) finished * 100 / total));
            tvStop.setVisibility(View.VISIBLE);
        } else {
            tvState.setText(R.string.backup_main);
            if (UploadController.getInstance().toUpload > 0) {
                tvCount.setText(String.format(getString(R.string.backup_to_upload), UploadController.getInstance().toUpload));
            } else {
                tvCount.setText(R.string.backup_no_more);
            }
            pbUpload.setVisibility(View.INVISIBLE);
            pbUpload.setProgress(0);
            tvStop.setVisibility(View.GONE);
        }
    }

    void changeMenu(FragmentType fragmentType) {
        switch (fragmentType) {
            case PHOTOS:
                searchView.inflateOverflowMenu(R.menu.menu_main);
                searchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
                    @Override
                    public void onActionMenuItemSelected(MenuItem item) {
                        int id = item.getItemId();
                        switch (id) {
                            case R.id.action_select:
                                photosFragment.startActionMode();
                                break;
                            case R.id.action_voice:
                                startVoiceRecognition();
                                break;
                        }
                    }
                });
                break;
            case MOMENTS:
                searchView.inflateOverflowMenu(R.menu.menu_main);
                searchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
                    @Override
                    public void onActionMenuItemSelected(MenuItem item) {
                        int id = item.getItemId();
                        switch (id) {
                            case R.id.action_select:
                                momentsFragment.startActionMode();
                                break;
                        }
                    }
                });
                break;
            case ALBUM_PHOTOS:
                searchView.inflateOverflowMenu(R.menu.menu_main);
                searchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
                    @Override
                    public void onActionMenuItemSelected(MenuItem item) {
                        int id = item.getItemId();
                        switch (id) {
                            case R.id.action_select:
                                albumPhotosFragment.startActionMode();
                                break;
                        }
                    }
                });
                break;
            default:
                searchView.inflateOverflowMenu(R.menu.menu_main_empty);
                break;
        }

    }

    void showHint() {
        SharedPreferences sharedPreferences = PreferenceManager.getSharedPref(this);
        boolean firstTime = sharedPreferences.getBoolean(PREF_FIRST_TIME, true);

        if (firstTime) {
            PreferenceManager.putBoolean(this, PREF_FIRST_TIME, false);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.hint)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    }).create();

            dialog.show();
        }

    }

    @Subscribe
    public void onGetQuota(OnGetQuotaEvent event) {
         if (event.quota != null) {

             double used = event.quota.usedQutoa/(1024.0*1024.0*1024.0);
             double total = event.quota.totalQuota/(1024.0*1024.0*1024.0);
             String parten = "#.##";
             DecimalFormat decimal = new DecimalFormat(parten);
             String usedStr = decimal.format(used);
             String totalStr = decimal.format(total);

             String quota = String.valueOf(usedStr) + "G/"
                     + String.valueOf(totalStr) + "G";
             tvQuota.setText(quota);
         }
    }
}
