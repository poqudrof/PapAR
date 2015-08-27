/* 
 *  Copyright Inria and Bordeaux University.
 *  Author Jeremy Laviole. jeremy.laviole@inria.fr
 *  PapAR project is the open-source version of the
 *  PapARt project. License is LGPLv3, distributed with the sources.
 *  This project can also distributed with standard commercial
 *  licence for closed-sources projects.
 */
package fr.inria.papart.procam.camera;

import fr.inria.papart.procam.Utils;
import org.bytedeco.javacpp.freenect;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author jiii
 */
public class CameraOpenKinectDepth extends Camera {

    private CameraOpenKinect parent;
    private int depthFormat = freenect.FREENECT_DEPTH_MM;
    // other possibility freenect.FREENECT_DEPTH_10_BIT -> Obselete;

    private IplImage depthImage;
    private PImage camImageDepth = null;

    protected CameraOpenKinectDepth(CameraOpenKinect parent) {
        this.parent = parent;
        this.setPixelFormat(PixelFormat.DEPTH_KINECT_MM);
    }

    // Nothing, this is virtual...
    @Override
    public void start() {
        parent.grabber.setDepthFormat(depthFormat);
    }

    @Override
    public void grab() {
        try {
            IplImage img = parent.grabber.grabDepth();

            this.currentImage = img;
        } catch (Exception e) {
            System.err.println("Camera: Kinect Grab depth Error ! " + e);
            e.printStackTrace();
        }

    }

    @Override
    public PImage getPImage() {
        if (camImageDepth == null) {
            camImageDepth = parent.parent.createImage(width, height, PApplet.ALPHA);
        }

        if (depthImage != null) {
            Utils.IplImageToPImageKinect(depthImage, false, camImageDepth);
        }
        return camImageDepth;
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int getDepthFormat() {
        return depthFormat;
    }

    public void setDepthFormat(int depthFormat) {
        this.depthFormat = depthFormat;
    }

}
