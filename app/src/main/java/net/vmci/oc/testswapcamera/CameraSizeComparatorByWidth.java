package net.vmci.oc.testswapcamera;

import android.hardware.Camera;

import java.util.Comparator;

public class CameraSizeComparatorByWidth implements Comparator<Camera.Size> {

    public CameraSizeComparatorByWidth() {

    }

    @Override
    public int compare(Camera.Size lhs, Camera.Size rhs) {

        if (lhs.width < rhs.width) {

            return -1;
        }
        if (lhs.width > rhs.width) {

            return 1;
        }

        return 0;
    }
}
