package com.tomiyama.noir.camerasample;


import android.content.Intent;
import android.graphics.SurfaceTexture;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.TextureView;
import android.view.View;

public class Camera2ExActivity extends AppCompatActivity {

    private Camera2 mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2_ex);

        TextureView textureView = (TextureView) findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mCamera.startCamera();

            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                // カメラの停止
                mCamera.stopCamera();

                // TODO 値渡しができない

//                Intent intent = new Intent();
//                intent.putExtra("PATH",mCamera.getPath());
//                setResult(RESULT_OK, intent);
//
//                finish();

                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });

        mCamera = new Camera2(textureView,Camera2ExActivity.this);
//        Log.d("test", String.valueOf(this));

        findViewById(R.id.shutterButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.onButtonClick();
            }
        });

        findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });





    }



}
