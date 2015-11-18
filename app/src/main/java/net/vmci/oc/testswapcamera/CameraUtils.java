package net.vmci.oc.testswapcamera;

import android.hardware.Camera;
import android.util.Log;

import java.util.Collections;
import java.util.List;

public class CameraUtils {

    // supported sizes are in landscape format, ie width > height
    protected static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {

        double targetRatio = (double) w / h;
        double supportedRatio;
        double ratioDifference;
        double bestRatioDifference = 2;
        int indexOfSizeWithClosestRatio = -1;

        // ensure list is sorted by ascending width
        Collections.sort(sizes, new CameraSizeComparatorByWidth());

        // find largest size with closest ratio - assumes the list is already ordered smallest to largest
        for (Camera.Size size : sizes) {

            supportedRatio = (double) size.width / size.height;
            ratioDifference = Math.abs(targetRatio - supportedRatio);

            // catch the last match as well
            if (ratioDifference <= bestRatioDifference) {

                bestRatioDifference = ratioDifference;
                indexOfSizeWithClosestRatio = sizes.indexOf(size);
            }
        }

        if (indexOfSizeWithClosestRatio != -1) {

            Camera.Size size = sizes.get(indexOfSizeWithClosestRatio);
            Log.i("TestCamera", "Camera size with closest ratio, width: " + size.width + ", height: " + size.height);
            return size;
        }

        return null;
    }
}
