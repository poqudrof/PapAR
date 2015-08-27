// PapARt library

import fr.inria.papart.procam.*;
import fr.inria.papart.procam.display.*;

import org.bytedeco.javacpp.*;
import org.reflections.*;
import toxi.geom.*;


Papart papart;

boolean useProjector = true;
float planetScale = 2f / 20000f;


PVector boardSize = new PVector(297, 210);   //  21 * 29.7 cm
float boardResolution = 1;  // 3 pixels / mm
float renderQuality = 1.5f;


void settings(){
    size(300, 300, P3D);
}

void setup() {

    papart = Papart.seeThrough(this);
    papart.loadSketches();
    papart.startTracking();
}


void draw() {
}

