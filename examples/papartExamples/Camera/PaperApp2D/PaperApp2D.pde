// PapARt library
import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.opencv_core;
import org.reflections.*;
import toxi.geom.*;

import processing.video.*;

boolean useProjector = false;
Papart papart;

public void setup() {

    // use yaml calibration
    // Papart.cameraCalibName = "camera.yaml";
    // Papart.cameraCalib = Papart.calibrationFolder + Papart.cameraCalibName;
    
    papart = Papart.seeThrough(this);

    papart.loadSketches();
    papart.startTracking();
}

void settings(){
    size(300, 300, P3D);
}

void draw() {
    
}