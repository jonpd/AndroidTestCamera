package net.vmci.oc.testswapcamera;

import android.hardware.Camera;
import android.util.Size;

import java.util.Arrays;

public class CameraStats {

    private int id;
    private CameraResolution availableSurfaceSize;
    private CameraResolution optimalResolution;
    private int[] supportedFramerateRangePerSecond;
    private int orientation;

    public CameraStats() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CameraResolution getAvailableSurfaceSize() {
        return availableSurfaceSize;
    }

    public void setAvailableSurfaceSize(CameraResolution availableSurfaceSize) {
        this.availableSurfaceSize = availableSurfaceSize;
    }

    public CameraResolution getOptimalResolution() {
        return optimalResolution;
    }

    public void setOptimalResolution(CameraResolution optimalResolution) {
        this.optimalResolution = optimalResolution;
    }

    public int[] getSupportedFramerateRangePerSecond() {
        return supportedFramerateRangePerSecond;
    }

    public void setSupportedFramerateRangePerSecond(int[] supportedFramerateRangePerSecond) {
        this.supportedFramerateRangePerSecond = supportedFramerateRangePerSecond;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    @Override
    public String toString() {

        String faces = "front";

        if (id == 0) {

            faces = "back";
        }

        return "CameraStats {" +
                "facing = " + faces +
                ", surface size = " + availableSurfaceSize.getWidth() + " x " + availableSurfaceSize.getHeight() +
                ", optimal resolution = " + optimalResolution.getWidth() + " x " + optimalResolution.getHeight() +
                ", framerate range = " + supportedFramerateRangePerSecond[0]/1000 + " to " + supportedFramerateRangePerSecond[1]/1000 +
                ", orientation = " + orientation +
                '}';
    }
}
