package net.vmci.oc.testswapcamera;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraPreviewBasic extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private Context context;
    private int cameraId;
    private int frameCount;
    private CameraStats cameraStats;
    private Handler handler;
    private List<TextView> textViewList;

    public CameraPreviewBasic(Context context, Camera cameraInstance, int cameraId, List<TextView> textViewList) {

        super(context);
        this.context = context;
        camera = cameraInstance;
        this.cameraId = cameraId;
        cameraStats = new CameraStats();
        cameraStats.setId(cameraId);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        this.textViewList = textViewList;

        createHandler();
    }

    private void createHandler() {

        // Defines a Handler object that's attached to the UI thread
        handler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message message) {

                // can update ui
                CameraStats messageCameraStats = (CameraStats) message.obj;

                if (messageCameraStats != null) {

                    for (TextView textView : textViewList) {

                        if (textView.getId() == R.id.stats_surface_size_input) {
                            textView.setText(messageCameraStats.getAvailableSurfaceSize().getWidth() + " x " + cameraStats.getAvailableSurfaceSize().getHeight());
                        }
                        if (textView.getId() == R.id.stats_optimal_resolution_input) {
                            textView.setText(messageCameraStats.getOptimalResolution().getWidth() + " x " + cameraStats.getOptimalResolution().getHeight());
                        }
                        if (textView.getId() == R.id.stats_framerate_range_input) {
                            textView.setText(messageCameraStats.getSupportedFramerateRangePerSecond()[0] / 1000 + " to " + cameraStats.getSupportedFramerateRangePerSecond()[1] / 1000);
                        }
                        if (textView.getId() == R.id.stats_orientation_input) {
                            textView.setText(String.valueOf(messageCameraStats.getOrientation()));
                        }
                    }
                }
            }
        };
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {

            camera.setPreviewDisplay(surfaceHolder);
            camera.setPreviewCallback(this);

        } catch (IOException e) {

            // log
        }
    }

    // If you want to set a specific size for your camera preview, set here
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        Log.i("TestCamera", "surfaceChanged width: " + width + ", height: " + height);

        cameraStats.setAvailableSurfaceSize(new CameraResolution(width, height));

        // Make sure to stop the preview, if applicable, before setting size.
        setOrientation();
        setOptimalCameraPreviewSize(width, height);
        // start preview once we have set orientation and size
        camera.startPreview();

        int what = 0;
        Message message = handler.obtainMessage(what, cameraStats);
        message.sendToTarget();
    }

    private void setOrientation() {

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();

        int degreesDisplayRotated = 0;
        if (rotation == Surface.ROTATION_0) {

            degreesDisplayRotated = 0;
        }

        if (rotation == Surface.ROTATION_90) {

            degreesDisplayRotated = 90;
        }
        if (rotation == Surface.ROTATION_180) {

            degreesDisplayRotated = 180;
        }
        if (rotation == Surface.ROTATION_270) {

            degreesDisplayRotated = 270;
        }

        Log.i("TestCamera", "Display rotation: " + degreesDisplayRotated);

        // cameraInfo.orientation:
        // the angle that the camera image needs to be rotated clockwise so it shows correctly on the display in its natural orientation
        // starting position is 0 (and in landscape), and natural orientation is portrait, so this value is usually always 270
        Log.i("TestCamera", "CameraInfo.orientation (adjustment to get back to natural position): " + cameraInfo.orientation);
        cameraStats.setOrientation(cameraInfo.orientation);

        int degreesToRotate = 0;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {

            // front facing
            degreesToRotate = (cameraInfo.orientation + degreesDisplayRotated) % 360;
            // compensate the mirror
            degreesToRotate = (360 - degreesToRotate) % 360;
        }
        else {

            // back facing
            degreesToRotate = (cameraInfo.orientation - degreesDisplayRotated + 360) % 360;
        }

        Log.i("TestCamera", "Camera setDisplayOrientation: " + degreesToRotate);
        camera.setDisplayOrientation(degreesToRotate);
    }

    private void setOptimalCameraPreviewSize(int width, int height) {

        Camera.Parameters params = camera.getParameters();

        List<int[]> fpsRangeList = params.getSupportedPreviewFpsRange();

        if (fpsRangeList.size() > 1) {

            // find lowest and highest
            int min = 300000;
            int max = 0;

            for (int[] intArray : fpsRangeList) {

                if (intArray[0] < min) {

                    min = intArray[0];
                }
                if (intArray[1] > max) {

                    max = intArray[1];
                }
            }

            int[] minMaxArray = {min, max};

            cameraStats.setSupportedFramerateRangePerSecond(minMaxArray);
        }
        else {

            cameraStats.setSupportedFramerateRangePerSecond(params.getSupportedPreviewFpsRange().get(0));
        }

        // these are all in landscape format, ie width > height
        List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
        Log.i("TestCamera", "Current preview size width: " + params.getPreviewSize().width + ", height: " + params.getPreviewSize().height);
        // When setting preview size, you must use values from getSupportedPreviewSizes()
        // Do not set arbitrary values in the setPreviewSize() method.
        logSupportedPreviewSizes(previewSizes);

        if (width < height) {

            // portrait

        }
        else {

            // landscape
            Camera.Size previewSize = CameraUtils.getOptimalPreviewSize(previewSizes, width, height);

            if (previewSize != null) {

                Log.i("TestCamera", "Setting optimal size (landscape) width: " + previewSize.width + ", height: " + previewSize.height);
                params.setPreviewSize(previewSize.width, previewSize.height);
                cameraStats.setOptimalResolution(new CameraResolution(previewSize.width, previewSize.height));
            }
        }

        camera.setParameters(params);
    }

    private void logSupportedPreviewSizes(List<Camera.Size> previewSizes) {

        StringBuilder sb = new StringBuilder();

        for (Camera.Size size: previewSizes) {

            sb.append(" (");
            sb.append(size.width + " x " + size.height);
            sb.append(")");
        }

        Log.i("TestCamera", "Supported preview sizes:" + sb.toString());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        stopPreview();
    }

    public void stopPreview() {

        surfaceHolder.removeCallback(this);
        camera.setPreviewCallback(null);

        camera.stopPreview();
        camera.release();
        camera = null;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        frameCount++;

        if (frameCount > 25 && frameCount < 37) {

            String filename = "frame_pic_" + frameCount + ".jpg";
            //saveImageFromPreviewFrame(data, filename);
        }
    }

    private void saveImageFromPreviewFrame(byte[] data, String filename) {

        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        YuvImage image = new YuvImage(data, parameters.getPreviewFormat(),
                size.width, size.height, null);

        File file = new File(Environment.getExternalStorageDirectory()
                .getPath() + "/" + filename);
        FileOutputStream fileOutputStream = null;

        int compressionQuality = 90;

        try {

            fileOutputStream = new FileOutputStream(file);
            Rect rectangle = new Rect(0, 0, image.getWidth(), image.getHeight());
            image.compressToJpeg(rectangle, compressionQuality, fileOutputStream);
            Log.i("TestCamera", "Preview frame data saved as jpeg");

        } catch (Exception e) {


        }
    }
}
