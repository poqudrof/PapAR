/* 
 *  Copyright Inria and Bordeaux University.
 *  Author Jeremy Laviole. jeremy.laviole@inria.fr
 *  PapAR project is the open-source version of the
 *  PapARt project. License is LGPLv3, distributed with the sources.
 *  This project can also distributed with standard commercial
 *  licence for closed-sources projects.
 */
package fr.inria.papart.procam.camera;

import java.util.concurrent.TimeUnit;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PImage;

/**
 *
 * @author jiii
 */
public class CameraProcessing extends Camera {

    protected CaptureIpl captureIpl;

    protected CameraProcessing(String description) {
        this.cameraDescription = description;
        this.setPixelFormat(PixelFormat.ARGB);
    }

    @Override
    public void start() {

        if (cameraDescription == null) {
            System.out.println("Starting capture !");
            this.captureIpl = new CaptureIpl(parent, width, height);
        } else {

            System.out.println("Starting capture on device " + cameraDescription);
            this.captureIpl = new CaptureIpl(parent, width, height, cameraDescription);
        }

        this.captureIpl.start();
        this.isConnected = true;
    }

    @Override
    public void grab() {
        if (this.isClosing()) {
            return;
        }

        if (this.captureIpl.available()) {
            captureIpl.read();
            IplImage img = captureIpl.getIplImage();
            if (img != null) {
                updateCurrentImage(img);
            }
        } else {  // sleep for a short time..
            waitForNextFrame();
        }
    }

    @Override
    public PImage getPImage() {
        return this.captureIpl;
    }

    private void waitForNextFrame() {
        try {
            // TimeUnit.MILLISECONDS.sleep((long) (1f / frameRate));
            TimeUnit.MILLISECONDS.sleep((long) (10));
        } catch (Exception e) {
        }
    }

    @Override
    public void close() {
        this.setClosing();
        if (captureIpl != null) {
            captureIpl.stop();
        }
    }
}
