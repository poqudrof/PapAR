/* 
 *  Copyright Inria and Bordeaux University.
 *  Author Jeremy Laviole. jeremy.laviole@inria.fr
 *  PapAR project is the open-source version of the
 *  PapARt project. License is LGPLv3, distributed with the sources.
 *  This project can also distributed with standard commercial
 *  licence for closed-sources projects.
 */
package fr.inria.papart.procam;

import fr.inria.papart.calibration.HomographyCalibration;
import fr.inria.papart.calibration.HomographyCreator;
import fr.inria.papart.procam.camera.Camera;
import static fr.inria.papart.procam.Utils.toVec;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;
import toxi.geom.*;

/**
 * This class implements a virtual screen. The position of the screen has to be
 * passed. It no longers handles a camera.
 *
 * @author jeremylaviole
 */
public class Screen {

    private PApplet parent;
    // The current graphics
    private PGraphicsOpenGL thisGraphics;
    // Position holding...
    private PMatrix3D initPosM = null;

    // Either one, or the other is unique to this object. 
    // The other one is unique to the camera/markerboard couple. 
    private float[] posFloat;
    private PMatrix3D transformation = new PMatrix3D(); // init to avoid nullPointerExceptions 
    private PMatrix3D secondTransformation = null;

    private boolean isFloatArrayUpdating;

    ////////////
    private PVector size;
    private float scale;
    protected Plane plane = new Plane();

    private static final int nbPaperPosRender = 4;
    private final PVector[] paperPosCorners3D = new PVector[nbPaperPosRender];

    // TODO: update this again
    private HomographyCreator homography;
    protected HomographyCalibration worldToScreen;
    public float halfEyeDist = 10; // 2cm
    private boolean isDrawing = true;
    private boolean isOpenGL = false;

    public Screen(PApplet parent, PVector size, float scale) {
        this(parent, size, scale, false, 1);
    }

    public Screen(PApplet parent, PVector size, float scale, boolean useAA, int AAValue) {
        this.size = size.get();
        this.scale = scale;
        this.parent = parent;
        initHomography();
    }

    // Get the texture to display...
    public PGraphicsOpenGL getTexture() {
        return getGraphics();
    }

    public PGraphicsOpenGL getGraphics() {
        if (thisGraphics == null) {
            thisGraphics = (PGraphicsOpenGL) parent.createGraphics((int) (size.x * scale), (int) (size.y * scale), PApplet.OPENGL);
        }

        return thisGraphics;
    }

    // The board must be registered with the camera. 
    /**
     * **
     * @deprecated
     *
     */
//    public void setAutoUpdatePos(Camera camera, MarkerBoard board) {
//        if (!camera.tracks(board)) {
//            camera.trackMarkerBoard(board);
//        }
//
//        isFloatArrayUpdating = board.useFloatArray();
//        if (this.isFloatArrayUpdating) {
//            posFloat = board.getTransfo(camera);
//            transformation = new PMatrix3D();
//        } else {
////            System.out.println("Getting the original transfo");
//
//            transformation = board.getTransfoMat(camera);
//            posFloat = new float[12];
//        }
//    }

    public boolean isOpenGL() {
        return isOpenGL;
    }

    public boolean isDrawing() {
        return isDrawing;
    }

    public void setDrawing(boolean isDrawing) {
        this.isDrawing = isDrawing;
    }

    public PVector getSize() {
        return size;
    }

    public int getDrawSizeX() {
        return (int) (size.x * scale);
    }

    public int getDrawSizeY() {
        return (int) (size.y * scale);
    }

    /**
     * Set the main position (override tracking system). Use only after the call
     * of paperScreen.useManualLocation(false);
     *
     * @param position
     */
    public void setMainLocation(PMatrix3D position) {
        transformation.set(position);
    }

    /**
     * Set a second transformation applied after tracking transform.
     *
     * @param tr
     */
    public void setTransformation(PMatrix3D tr) {
        if (secondTransformation == null) {
            this.secondTransformation = new PMatrix3D(tr);
        } else {
            this.secondTransformation.set(tr);
        }
    }

    /**
     * Set an additional translation (replace the second transformation)
     *
     * @param tr
     */
    public void setTranslation(PVector tr) {
        setTranslation(tr.x, tr.y, tr.z);
    }

    /**
     * Set an additional translation (replace the second transformation)
     *
     * @param x
     * @param y
     * @param z
     */
    public void setTranslation(float x, float y, float z) {
        if (secondTransformation == null) {
            secondTransformation = new PMatrix3D();
        }
        secondTransformation.reset();
        secondTransformation.translate(x, y, z);
    }

    /**
     * @deprecated @return
     */
    public PMatrix3D getPosition() {
        return getLocation();
    }

    /**
     * Get a copy of the overall transform (after tracking and second
     * transform).
     *
     * @return
     */
    public PMatrix3D getLocation() {
        if (secondTransformation == null) {
            return transformation.get();
        } else {
            PMatrix3D combinedTransfos = transformation.get();
            combinedTransfos.apply(secondTransformation);
            return combinedTransfos;
        }
    }

    public float getScale() {
        return this.scale;
    }

    /**
     * update the internals of the screen to match the tracking.
     */
    public void updatePos(Camera camera, MarkerBoard board) {
        transformation.set(board.getTransfoMat(camera));
    }

    public void computeScreenPosTransform() {

        ///////////////////// PLANE COMPUTATION  //////////////////
        PMatrix3D mat = this.getLocation();

        paperPosCorners3D[0] = new PVector(mat.m03, mat.m13, mat.m23);
        mat.translate(size.x, 0, 0);
        paperPosCorners3D[1] = new PVector(mat.m03, mat.m13, mat.m23);
        mat.translate(0, size.y, 0);
        paperPosCorners3D[2] = new PVector(mat.m03, mat.m13, mat.m23);
        mat.translate(-size.x, 0, 0);
        paperPosCorners3D[3] = new PVector(mat.m03, mat.m13, mat.m23);

        plane = new Plane(new Triangle3D(toVec(paperPosCorners3D[0]), toVec(paperPosCorners3D[1]), toVec(paperPosCorners3D[2])));

        homography.addPoint(paperPosCorners3D[0], new PVector(0, 0));
        homography.addPoint(paperPosCorners3D[1], new PVector(1, 0));
        homography.addPoint(paperPosCorners3D[2], new PVector(1, 1));
        homography.addPoint(paperPosCorners3D[3], new PVector(0, 1));
        worldToScreen = homography.getHomography();
    }

    public PVector[] getCornerPos() {
        computeScreenPosTransform();
        return paperPosCorners3D;
    }

    ////////////////// 3D SPACE TO PAPER HOMOGRAPHY ///////////////
    // Version 2.0 :  (0,0) is the top-left corner.
    private void initHomography() {
        homography = new HomographyCreator(3, 2, 4);

    }

    public void initDraw(PVector userPos) {
        initDraw(userPos, 40, 5000);
    }

    public void initDraw(PVector userPos, float nearPlane, float farPlane) {
        initDraw(userPos, nearPlane, farPlane, false, false, true);
    }
    // TODO: optionnal args.

    public void initDraw(PVector userPos, float nearPlane, float farPlane, boolean isAnaglyph, boolean isLeft, boolean isOnly) {

        PGraphicsOpenGL graphics = getGraphics();

        if (initPosM == null) {
            this.isOpenGL = true;
            // Transformation  Camera -> Marker

            initPosM = this.getLocation();

            initPosM.translate(this.getDrawSizeX() / 2, this.getDrawSizeY() / 2);
            // All is relative to the paper's center. not the corner. 
            initPosM.scale(-1, 1, -1);

        }

        // get the current transformation... 
        PMatrix3D newPos = this.getLocation();

        newPos.translate(this.getDrawSizeX() / 2, this.getDrawSizeY() / 2);
        newPos.scale(-1, 1, -1);

        newPos.invert();
        newPos.apply(initPosM);

        PVector user = new PVector();

        if (isAnaglyph && isLeft) {
            userPos.add(-halfEyeDist * 2, 0, 0);
        }
        newPos.mult(userPos, user);
        PVector paperCameraPos = user;

        // Camera must look perpendicular to the screen. 
        graphics.camera(paperCameraPos.x, paperCameraPos.y, paperCameraPos.z,
                paperCameraPos.x, paperCameraPos.y, 0,
                0, 1, 0);

        // http://www.gamedev.net/topic/597564-view-and-projection-matrices-for-vr-window-using-head-tracking/
        float nearFactor = nearPlane / paperCameraPos.z;

        float left = nearFactor * (-size.x / 2f - paperCameraPos.x);
        float right = nearFactor * (size.x / 2f - paperCameraPos.x);
        float top = nearFactor * (size.y / 2f - paperCameraPos.y);
        float bottom = nearFactor * (-size.y / 2f - paperCameraPos.y);

        graphics.frustum(left, right, bottom, top, nearPlane, farPlane);
        graphics.projection.m11 = -graphics.projection.m11;

        // No detection?
        if (transformation.m03 == 0 && transformation.m13 == 0 && transformation.m23 == 0) {
            resetPos();
        }
    }

    public void endDrawPerspective() {
        PGraphicsOpenGL graphics = getGraphics();
        graphics.perspective();
        graphics.camera();
        graphics.projection.m11 = -graphics.projection.m11;
    }

    public void resetPos() {
        initPosM = null;
    }

    public float getHalfEyeDist() {
        return halfEyeDist;
    }

    public void setHalfEyeDist(float halfEyeDist) {
        this.halfEyeDist = halfEyeDist;
    }

    public Plane getPlane() {
        return plane;
    }

    public HomographyCalibration getWorldToScreen() {
        return worldToScreen;
    }

}
