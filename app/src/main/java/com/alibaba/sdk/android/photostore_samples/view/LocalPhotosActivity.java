/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.view;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.adapter.LocalPhotosAdapter;
import com.alibaba.sdk.android.photostore_samples.controller.UploadController;
import com.alibaba.sdk.android.photostore_samples.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class LocalPhotosActivity extends AppCompatActivity {

    private static final String TAG = LocalPhotosActivity.class.getSimpleName();
    private ProgressBar mProgressBar;

    private LocalPhotosAdapter adapter;
    private LoadFileListTask mLoadFileListTask;
    private List<String> mCurrentPathList = new ArrayList<>();

    Unbinder unbinder;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    GridLayoutManager gridLayoutManager;

    int cols = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_photos);

        unbinder = ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");

        adapter = new LocalPhotosAdapter(this, cols);
        adapter.addHeader(new View(this));

        gridLayoutManager = new GridLayoutManager(this, cols);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getSpanSize(position);
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.pb_local_file_list_waiting);

        try {
            StorageManager sm = (StorageManager) getSystemService(Context.STORAGE_SERVICE); // 获取sdcard的路径：外置和内置
            String[] paths = (String[]) sm.getClass().getMethod("getVolumePaths", null).invoke(sm, null);
            for (String p : paths) {
                File dcim = new File(p, "DCIM");
                if (dcim.exists()) {
                    File folder = new File(dcim, "Camera");
                    if (folder.exists())
                        mCurrentPathList.add(folder.getAbsolutePath());
                    else
                        mCurrentPathList.add(dcim.getAbsolutePath());
                }
            }
        }
        catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        asyncUploadFileList();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mLoadFileListTask != null) {
            mLoadFileListTask.cancel(true);
            mLoadFileListTask = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_local_photo_upload:
                List<File> selected = adapter.getSelected();
                if (selected != null) {
                    UploadController.getInstance().upload(selected, false);
                }
                setResult(RESULT_OK, null);
                finish();
                return true;
            case R.id.menu_local_photo_select_all:
                adapter.selectAll(true);
                return true;
            case R.id.menu_local_photo_select_none:
                adapter.selectAll(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_local_photos, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void asyncUploadFileList() {
        if (mLoadFileListTask == null) {
            mProgressBar.setVisibility(View.VISIBLE);
            mLoadFileListTask = new LoadFileListTask();
            mLoadFileListTask.execute(mCurrentPathList);
        }
    }

    private class LoadFileListTask extends AsyncTask<List<String>, Void, List<File>> {

        @Override
        protected List<File> doInBackground(List<String>... params) {
            List<String> paths = params[0];
            List<File> list = new ArrayList<>();
            for (String p : paths) {
                File testDir = new File(p);
                scanPhotos(testDir, list);
                Collections.sort(list, new FileComparator());
            }

            return list;
        }

        @Override
        protected void onPostExecute(List<File> files) {
            mProgressBar.setVisibility(View.GONE);
            mLoadFileListTask = null;
            adapter.setData(files);
            adapter.notifyDataSetChanged();
        }

        private void scanPhotos(File folder, List<File> list) {
            if (folder != null) {
                File[] fileList = folder.listFiles();
                for (File f : fileList) {
                    if (f.isFile()) {
                        if (FileUtil.getFileExt(f).toLowerCase().equals("jpg") || FileUtil.getFileExt(f).toLowerCase().equals("png")
                                || FileUtil.getFileExt(f).toLowerCase().equals("mp4") || FileUtil.getFileExt(f).toLowerCase().equals("jpeg")) {
                            list.add(f);
                        }
                    }
                    else if (f.isDirectory()) {
                        scanPhotos(f, list);
                    }
                }
            }
        }
    }

    private class FileComparator implements Comparator<File> {

        @Override
        public int compare(File file, File file2) {
            if (file.isFile() == file2.isFile()) {
                return file.lastModified() < file2.lastModified() ? 1 : -1;
            }
            return file.isDirectory() ? -1 : 1;
        }
    }

}
