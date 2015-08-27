/* 
 *  Copyright Inria and Bordeaux University.
 *  Author Jeremy Laviole. jeremy.laviole@inria.fr
 *  PapAR project is the open-source version of the
 *  PapARt project. License is LGPLv3, distributed with the sources.
 *  This project can also distributed with standard commercial
 *  licence for closed-sources projects.
 */
package fr.inria.papart.procam.camera;

import java.awt.Image;
import java.nio.ByteBuffer;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author jiii
 */
public abstract class CamImage extends PImage {

    protected Object bufferSink = null;
    protected ByteBuffer natBuffer = null;

    protected CamImage(PApplet parent, int width, int height, int format) {
        super(width, height, format);
        camInit(parent);
    }

    public CamImage(PApplet parent, Image img) {
        super(img);
        camInit(parent);
    }

    protected abstract void camInit(PApplet parent);

    public abstract void update(IplImage iplImage);

    public synchronized void disposeBuffer(Object buf) {

    }

}
