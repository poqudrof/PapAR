/* 
 *  Copyright Inria and Bordeaux University.
 *  Author Jeremy Laviole. jeremy.laviole@inria.fr
 *  PapAR project is the open-source version of the
 *  PapARt project. License is LGPLv3, distributed with the sources.
 *  This project can also distributed with standard commercial
 *  licence for closed-sources projects.
 */
package fr.inria.papart.apps;

import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.opencv_core;
import org.reflections.*;
import fr.inria.papart.apps.MyApp2D;
import processing.core.PApplet;
import toxi.geom.*;

import processing.video.*;

public class PaperApp2D extends PApplet {

    boolean useProjector = false;
    Papart papart;

    public static void main(String args[]) {
        PApplet.main(new String[]{"--present", "fr.inria.papart.apps.PaperApp2D"});
    }

    public void setup() {

            papart = Papart.seeThrough(this);
//
//        papart.loadSketches();

        MyApp2D app = new MyApp2D();

        papart.startTracking();
    }

    public void settings() {
        fullScreen(P3D);
    }

    public void draw() {
//        println("frameRate " + frameRate);
    }
}
