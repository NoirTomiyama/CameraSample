package com.tomiyama.noir.camerasample;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

//カメラとフォト
public class Camera2View extends TextureView {

    //システム
    private Activity activity;    //アクティビティ
    private Handler uiHandler;   //UIハンドラ
    private Handler workHandler; //ワークハンドラ
    private boolean active;      //アクティブ
    private CameraManager manager;     //カメラマネージャ

    //カメラ
    private String cameraId;       //カメラID
    private CameraCharacteristics cameraInfo;     //カメラ情報
    private Size previewSize;    //プレビューサイズ
    private Size pictureSize;    //写真サイズ
    private CameraDevice cameraDevice;   //カメラデバイス
    private CaptureRequest.Builder previewBuilder; //プレビュービルダー
    private CameraCaptureSession previewSession; //プレビューセッション

    //アプリケーション名
    private final String APP_NAME = "CameraSample";


    //コンストラクタ
    public Camera2View(Context context) {
        super(context);
        //システム
        activity = (Activity)context;
        active = false;

        //ハンドラの生成(3)
        uiHandler = new Handler();
        HandlerThread thread = new HandlerThread("work");
        thread.start();
        workHandler = new Handler(thread.getLooper());

        //カメラマネージャーの取得(4)
        manager = (CameraManager)activity.getSystemService(Context.CAMERA_SERVICE);

        //テクスチャービューのリスナーの指定(5)
        setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            //テクスチャ有効化時に呼ばれる
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                startCamera();
            }

            //テクスチャサイズ変更時に呼ばれる
            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
                                                    int width, int height) {
            }

            //テクスチャ更新時に呼ばれる
            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }

            //テクスチャ破棄時に呼ばれる
            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                //カメラの停止
                stopCamera();
                return true;
            }
        });
    }

    //カメラの開始
    private void startCamera() {
        try {
            //カメラの取得(6)
            cameraId = getCameraId();
            cameraInfo = manager.getCameraCharacteristics(cameraId);

            //プレビューサイズと写真サイズの取得(7)
            previewSize = getPreviewSize(cameraInfo);
            pictureSize = getPictureSize(cameraInfo);

            //カメラのオープン(8)
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                //接続時に呼ばれる
                @Override
                public void onOpened(CameraDevice camera) {
                    cameraDevice = camera;
                    //プレビュー開始
                    startPreview();
                }

                //切断時に呼ばれる
                @Override
                public void onDisconnected(CameraDevice camera) {
                    camera.close();
                    cameraDevice = null;
                }

                //エラー時に呼ばれる
                @Override
                public void onError(CameraDevice camera, int error) {
                    camera.close();
                    cameraDevice = null;
                    toast("カメラのオープンに失敗しました");
                }
            }, null);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            toast(e.toString());
        }
    }

    //カメラの停止
    private void stopCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    //カメラIDの取得(6)
    private String getCameraId() {
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics cameraInfo =
                        manager.getCameraCharacteristics(cameraId);
                //背面カメラ
                if (cameraInfo.get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_BACK) {
                    return cameraId;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //プレビューサイズの取得(7)
    private Size getPreviewSize(CameraCharacteristics characteristics) {
        StreamConfigurationMap map = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
        for (int i = 0; i < sizes.length; i++) {
            //サイズ2000x2000以下
            if (sizes[i].getWidth() < 2000 && sizes[i].getHeight() < 2000) {
                return sizes[i];
            }
        }
        return sizes[0];
    }

    //写真サイズの取得(7)
    private Size getPictureSize(CameraCharacteristics characteristics) {
        Size[] sizes = characteristics.
                get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).
                getOutputSizes(ImageFormat.JPEG);
        for (int i = 0; i < sizes.length; i++) {
            //サイズ2000x2000以下
            if (sizes[i].getWidth() < 2000 && sizes[i].getHeight() < 2000) {
                return sizes[i];
            }
        }
        return null;
    }

    //プレビューの開始
    private void startPreview() {
        if (cameraDevice == null) return;
        active = true;

        //出力先となるテクスチャの準備(9)
        SurfaceTexture texture = getSurfaceTexture();
        if (texture == null) return;
        texture.setDefaultBufferSize(
                previewSize.getWidth(),
                previewSize.getHeight());
        Surface surface = new Surface(texture);

        //プレビューセッションの生成(10)
        try {
            previewBuilder = cameraDevice.
                    createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {
                        //成功時に呼ばれる
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            previewSession = session;
                            updatePreview();
                        }

                        //失敗時に呼ばれる
                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            toast("プレビューセッションの生成に失敗しました");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //プレビューの更新
    protected void updatePreview() {
        if (cameraDevice == null) return;

        //オートフォーカスの指定(11)
        previewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

        //カメラ映像をテクスチャに表示(12)
        try {
            previewSession.setRepeatingRequest(
                    previewBuilder.build(), null, workHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //タッチ時に呼ばれる
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!active) return true;
            active = false;

            //撮影
            takePicture();
        }
        return true;
    }

    //撮影
    private void takePicture() {
        if (cameraDevice == null) return;

        try {
            //出力先となるイメージリーダーの準備(13)
            ImageReader reader = ImageReader.newInstance(
                    pictureSize.getWidth(), pictureSize.getHeight(),
                    ImageFormat.JPEG, 2);
            reader.setOnImageAvailableListener(
                    new ImageReader.OnImageAvailableListener() {
                        //イメージ利用可能時に呼ばれる
                        @Override
                        public void onImageAvailable(ImageReader reader) {
                            Image image = null;
                            try {
                                //画像のバイト配列の取得
                                image = reader.acquireLatestImage();
                                byte[] data = image2data(image);
                                savePhoto(data);
                            } catch (Exception e) {
                                if (image != null) image.close();
                            }
                        }
                    }, workHandler);

            //キャプチャセッションの開始(14)
            final CaptureRequest.Builder captureBuilder =
                    cameraDevice.createCaptureRequest(
                            CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE,
                    CameraMetadata.CONTROL_MODE_AUTO);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,
                    getPhotoOrientation());
            List<Surface> outputSurfaces = new LinkedList<>();
            outputSurfaces.add(reader.getSurface());
            cameraDevice.createCaptureSession(outputSurfaces,
                    new CameraCaptureSession.StateCallback() {
                        //成功時に呼ばれる
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            try {
                                session.capture(captureBuilder.build(),
                                        new CameraCaptureSession.CaptureCallback() {
                                            //完了時に呼ばれる
                                            @Override
                                            public void onCaptureCompleted(CameraCaptureSession session,
                                                                           CaptureRequest request, TotalCaptureResult result) {
                                                super.onCaptureCompleted(session, request, result);
                                                toast("撮影完了");

                                                //プレビュー再開
                                                startPreview();
                                            }
                                        }, workHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        //失敗時に呼ばれる
                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            toast("キャプチャーセッションの生成に失敗しました");

                            //プレビュー再開
                            startPreview();
                        }
                    }, workHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //Image→バイト配列(13)
    private byte[] image2data(Image image) {
        Image.Plane plane = image.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer();
        byte[] data = new byte[buffer.capacity()];
        buffer.get(data);
        return data;
    }


    //写真の向きの計算(15)
    private int getPhotoOrientation() {
        int displayRotation = 0;
        int rotation = activity.getWindowManager().
                getDefaultDisplay().getRotation();
        if (rotation == Surface.ROTATION_0)   displayRotation = 0;
        if (rotation == Surface.ROTATION_90)  displayRotation = 90;
        if (rotation == Surface.ROTATION_180) displayRotation = 180;
        if (rotation == Surface.ROTATION_270) displayRotation = 270;
        int sensorOrientation = cameraInfo.get(
                CameraCharacteristics.SENSOR_ORIENTATION);
        return (sensorOrientation-displayRotation+360)%360;
    }

    //写真の保存
    private void savePhoto(byte[] data) {
        try {
            //保存先のパスの生成(16)
            SimpleDateFormat format = new SimpleDateFormat(
                    "'IMG'_yyyyMMdd_HHmmss'.jpg'", Locale.getDefault());

            // 保存ディレクトリの作成
            File file = new File(Environment.getExternalStorageDirectory(),APP_NAME);

            if(!file.exists()){
                file.mkdir();
            }

            String fileName = format.format(
                    new Date(System.currentTimeMillis()));
            String path = Environment.getExternalStorageDirectory()+
                    "/"+APP_NAME+
                    "/"+fileName;

            //バイト配列の保存
            saveData(data, path);

            //フォトへの登録(17)
            MediaScannerConnection.scanFile(getContext(),
                    new String[]{path}, null, null);
        } catch (Exception e) {
            toast(e.toString());
        }
    }

    //バイト配列の保存
    private void saveData(byte[] w, String path)
            throws Exception {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            out.write(w);
            out.close();
        } catch (Exception e) {
            if (out != null) out.close();
            throw e;
        }
    }

    //トーストの表示
    private void toast(final String text) {
        uiHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(getContext(), text,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

}
