package org.jay.googlemap.saveimage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import org.jay.googlemap.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class SaveImageActivity extends AppCompatActivity {
    private ImageView imageView;
    private ListView mListView;

    private List<ResolveInfo> activityList;

    // 所需的全部权限
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private ShareHelper mShareHelper;
    private File mImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);
        mShareHelper = new ShareHelper(this);

        mListView = (ListView) findViewById(R.id.list);

        imageView = (ImageView) findViewById(R.id.image);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImageView(getViewBitmap(imageView));
//                sendsms();

            }
        });
        requestPermissions();
        initData();

    }

    private void sendsms() {
        Log.d("jay", "sendsms: []=" + Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "");
        Intent intentsms = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + ""));
        intentsms.putExtra("sms_body", "Test text...");
        startActivity(intentsms);
    }

    public void shareText(String subject, String body) {
//        Intent txtIntent = new Intent(android.content.Intent.ACTION_SEND);
////        txtIntent .setType("text/plain");
//        txtIntent.setType("image/*");
//        txtIntent .putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
//        txtIntent .putExtra(android.content.Intent.EXTRA_TEXT, body);
//        startActivity(Intent.createChooser(txtIntent ,"Share"));
        Uri uri = Uri.fromFile(mImageFile);
        mShareHelper.getSomeIntent(ShareHelper.PACKAGENAME_MMS, uri, subject, body);
    }

    private void initData() {
        PackageManager pm = this.getPackageManager();
        Intent clipboardIntent = new Intent(Intent.ACTION_ALL_APPS);
        clipboardIntent.setType("text/plain");
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        activityList = pm.queryIntentActivities(sharingIntent, 0);

        AppInfoService appInfoService = new AppInfoService(this);
        List<AppInfo> list = appInfoService.getAppInfos();
//        Log.d("jay", "initData: []="+list.toString());

        ShareIntentListAdapter adapter = new ShareIntentListAdapter(this, activityList.toArray());
        mListView.setAdapter(adapter);
        adapter.setOnItemClickListener(new ShareIntentListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v) {
                saveImageView(getViewBitmap(v));
            }
        });

    }

    private void requestPermissions() {
        PermissionHelper
                .with(SaveImageActivity.this)
                .permissions(PERMISSIONS)
                .CallBack(new PermissionHelper.OnPermissionRequestListener() {
                    @Override
                    public void onGranted() {
                    }

                    @Override
                    public void onDenied() {
                    }
                })
                .request();
    }

    //2
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private class SaveObservable implements Observable.OnSubscribe<String> {

        private Bitmap drawingCache = null;

        public SaveObservable(Bitmap drawingCache) {
            this.drawingCache = drawingCache;
        }

        @Override
        public void call(Subscriber<? super String> subscriber) {
            if (drawingCache == null) {
                subscriber.onError(new NullPointerException("imageview的bitmap获取为null,请确认imageview显示图片了"));
            } else {
                try {
                    mImageFile = new File(Environment.getExternalStorageDirectory(), SystemClock.currentThreadTimeMillis() + "saveImage.png");
                    FileOutputStream outStream;
                    outStream = new FileOutputStream(mImageFile);
                    drawingCache.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    subscriber.onNext(Environment.getExternalStorageDirectory().getPath());
                    subscriber.onCompleted();
                    outStream.flush();
                    outStream.close();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        }
    }

    private class SaveSubscriber extends Subscriber<String> {

        @Override
        public void onCompleted() {
            shareText("aaaaa", "bbbbbbb");
            Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(Throwable e) {
            Log.i(getClass().getSimpleName(), e.toString());
            Toast.makeText(getApplicationContext(), "保存失败——> " + e.toString(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNext(String s) {

            Toast.makeText(getApplicationContext(), "保存路径为：-->  " + s, Toast.LENGTH_SHORT).show();
        }
    }


    private void saveImageView(Bitmap drawingCache) {
        Observable.create(new SaveObservable(drawingCache))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SaveSubscriber());
    }

    /**
     * 某些机型直接获取会为null,在这里处理一下防止国内某些机型返回null
     */
    private Bitmap getViewBitmap(View view) {
        if (view == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
}

