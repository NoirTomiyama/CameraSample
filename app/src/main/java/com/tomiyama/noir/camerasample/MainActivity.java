package com.tomiyama.noir.camerasample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_PERMISSONS = 0;
    private final static String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    private Button button;
    private ImageView imageView;

    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        button.setEnabled(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Camera2ExActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

//        //フルスクリーンの指定
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //ユーザーの利用許可のチェック
        checkPermissions();

        imageView = findViewById(R.id.imageView);

//        Log.d("check(Main)","onCreate");

    }

    @Override
    protected void onStart() {
        super.onStart();

//        Log.d("check(Main)","onStart");
//
//        CameraSampleApplication cameraSampleApplication =
//                (CameraSampleApplication) getApplication();
//
//        byte[] bytes = cameraSampleApplication.getTempRawPicture();
////        Log.d("check(onStart)", String.valueOf(bytes));
//
//        String path = cameraSampleApplication.getPath();
//        Log.d("check(onStart)",path);

//        if(!path.isEmpty()){
//
//        }


//        if(cameraSampleApplication.getTempRawPicture() != null){
//            Bitmap bmp = null;
//            bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
//            if(bmp != null){
//                imageView.setImageBitmap(bmp);
//            }
//        }

    }

    //ユーザーの利用許可のチェック(2)
    private void checkPermissions() {
        //許可
        if (isGranted()) {
            initContentView();
        }
        //未許可
        else {
            //許可ダイアログの表示
            ActivityCompat.requestPermissions(this, PERMISSIONS,
                    REQUEST_PERMISSONS);
        }
    }

    //ユーザーの利用許可が済かどうかの取得(2)
    private boolean isGranted() {
        for (int i = 0; i < PERMISSIONS.length; i++) {
            if (PermissionChecker.checkSelfPermission(
                    MainActivity.this, PERMISSIONS[i]) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    //許可ダイアログ選択時に呼ばれる(2)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] results) {
        if (requestCode == REQUEST_PERMISSONS) {
            //許可
            if (isGranted()) {
                initContentView();
            }
        } else {
            super.onRequestPermissionsResult(
                    requestCode, permissions, results);
        }
    }

    //コンテンツビューの初期化
    private void initContentView() {
//        setContentView(new Camera2View(this));
//
//        setContentView(R.layout.activity_camera2_ex);

        button.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

//        Log.d("確認","確認");
//        Log.d("ちぇっく1", String.valueOf(requestCode));
//        Log.d("ちぇっく 2", String.valueOf(resultCode));

        switch (requestCode) {
            //Camera2ExActivityから戻ってきた場合
            case (REQUEST_CODE):
                if (resultCode == RESULT_OK) {
                    //OKボタンを押して戻ってきたときの処理


//                    String path = data.getStringExtra("PATH");
//
//                    Log.d("ある？", "画像パス = " + path);


                    CameraSampleApplication cameraSampleApplication =
                            (CameraSampleApplication) getApplication();

                    byte[] bytes = cameraSampleApplication.getTempRawPicture();
                    Log.d("check(onActivityResult)", String.valueOf(cameraSampleApplication.getTempRawPicture()));

                    if(cameraSampleApplication.getTempRawPicture() != null){
                        Bitmap bmp = null;
                        bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        if(bmp != null){
                            imageView.setImageBitmap(bmp);
                        }
                    }


                } else if (resultCode == RESULT_CANCELED) {
                    //キャンセルボタンを押して戻ってきたときの処理
                } else {
                    //その他
                }
                break;
            default:
                break;
        }
    }
}
