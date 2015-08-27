/* 
 *  Copyright Inria and Bordeaux University.
 *  Author Jeremy Laviole. jeremy.laviole@inria.fr
 *  PapAR project is the open-source version of the
 *  PapARt project. License is LGPLv3, distributed with the sources.
 *  This project can also distributed with standard commercial
 *  licence for closed-sources projects.
 */
package fr.inria.papart.apps;

import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.PaperScreen;

public class MyApp2D extends PaperScreen {

    public MyApp2D(){
        super();
    }
    
    protected void setup() {
        setDrawingSize(297, 210);
//        loadMarkerBoard(Papart.markerFolder + "big.cfg", 297, 210);
        loadMarkerBoard(Papart.markerFolder + "dlink.png", 140,140);
    }

    public void draw() {
//        this.getLocation().print();
        beginDraw2D();
        background(100, 0, 0);
        fill(200, 100, 20);
        rect(10, 10, 100, 30);
        endDraw();
    }
}
