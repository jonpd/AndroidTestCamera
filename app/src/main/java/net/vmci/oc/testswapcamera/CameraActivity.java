package net.vmci.oc.testswapcamera;

import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private ImageButton buttonSwapCamera;
    private ImageButton buttonStopStartCamera;
    private FrameLayout videoLocal;
    private boolean isRearCamera;
    private boolean cameraIsLive;
    private CameraPreviewBasic cameraPreviewLocal;
    private TextView textViewSurfaceSizeInput;
    private TextView textViewOptimalResolutionInput;
    private TextView textViewFrameRateRangeInput;
    private TextView textViewOrientationInput;
    private List<TextView> statsTextViewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        buttonSwapCamera = (ImageButton) findViewById(R.id.button_swap_camera);
        buttonSwapCamera.setOnClickListener(new SwapCameraListener());

        buttonStopStartCamera = (ImageButton) findViewById(R.id.button_stop_start_camera);
        buttonStopStartCamera.setOnClickListener(new StopStartCameraListener());

        this.videoLocal = (FrameLayout) findViewById(R.id.video_local);

        // stats
        textViewSurfaceSizeInput = (TextView) findViewById(R.id.stats_surface_size_input);
        textViewOptimalResolutionInput = (TextView) findViewById(R.id.stats_optimal_resolution_input);
        textViewFrameRateRangeInput = (TextView) findViewById(R.id.stats_framerate_range_input);
        textViewOrientationInput = (TextView) findViewById(R.id.stats_orientation_input);
        statsTextViewList = new ArrayList<TextView>();
        statsTextViewList.add(textViewSurfaceSizeInput);
        statsTextViewList.add(textViewOptimalResolutionInput);
        statsTextViewList.add(textViewFrameRateRangeInput);
        statsTextViewList.add(textViewOrientationInput);
    }

    @Override
    protected void onPause() {

        super.onPause();
        stopLocalVideo();
    }

    @Override
    protected void onResume() {

        super.onResume();
        startLocalVideo();
    }

    private void stopLocalVideo() {

        // reset stats view
        for (TextView textView: statsTextViewList) {

            textView.setText(getResources().getText(R.string.stats_default));
        }

        cameraPreviewLocal.stopPreview();
        this.videoLocal.removeAllViews();
    }

    private void startLocalVideo() {

        Handler handler = new Handler();
        handler.post(new Runnable() {

            @Override
            public void run() {

                int availableCameras = Camera.getNumberOfCameras();

                Camera camera;
                int cameraId = 0;

                if (isRearCamera && availableCameras > 1) {

                    camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                    cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                }
                else {

                    isRearCamera = false;
                    camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                    cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                }

                if (camera != null) {

                    cameraPreviewLocal = new CameraPreviewBasic(getApplicationContext(), camera, cameraId, statsTextViewList);
                    videoLocal.addView(cameraPreviewLocal);

                    cameraIsLive = true;

                } else {

                    Toast.makeText(getApplicationContext(), "Failed to open camera...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class SwapCameraListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if (isRearCamera) {

                // swap to front
                isRearCamera = false;
                buttonSwapCamera.setImageResource(R.drawable.ic_camera_rear_white_24dp);
            }
            else {

                // swap to rear
                isRearCamera = true;
                buttonSwapCamera.setImageResource(R.drawable.ic_camera_front_white_24dp);
            }

            stopLocalVideo();
            startLocalVideo();
        }
    }

    private class StopStartCameraListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if (cameraIsLive) {

                // stop
                cameraIsLive = false;
                buttonStopStartCamera.setImageResource(R.drawable.ic_videocam_white_24dp);
                stopLocalVideo();
            }
            else {

                // start
                cameraIsLive = true;
                buttonStopStartCamera.setImageResource(R.drawable.ic_videocam_off_white_24dp);
                startLocalVideo();
            }
        }
    }
}
