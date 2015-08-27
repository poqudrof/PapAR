/* 
 *  Copyright Inria and Bordeaux University.
 *  Author Jeremy Laviole. jeremy.laviole@inria.fr
 *  PapAR project is the open-source version of the
 *  PapARt project. License is LGPLv3, distributed with the sources.
 *  This project can also distributed with standard commercial
 *  licence for closed-sources projects.
 */
package fr.inria.papart.procam;

import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.calibration.CameraConfiguration;
import fr.inria.papart.calibration.HomographyCalibration;
import fr.inria.papart.calibration.PlanarTouchCalibration;
import fr.inria.papart.procam.display.BaseDisplay;
import fr.inria.papart.procam.display.ARDisplay;
import org.bytedeco.javacpp.freenect;
import fr.inria.papart.calibration.PlaneAndProjectionCalibration;
import fr.inria.papart.calibration.PlaneCalibration;
import fr.inria.papart.calibration.ScreenConfiguration;
import fr.inria.papart.procam.camera.CameraFactory;
import fr.inria.papart.procam.camera.CameraOpenKinect;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Set;
import org.reflections.Reflections;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Plane;

/**
 *
 * @author jiii
 */
public class Papart {

    public final static String folder = fr.inria.papart.procam.Utils.getPapartFolder();
    public final static String calibrationFolder = folder + "/data/calibration/";
    public final static String markerFolder = folder + "/data/markers/";

    public static String cameraCalibName = "camera.xml";
    public static String projectorCalibName = "projector.yaml";

    public static String cameraCalib = calibrationFolder + cameraCalibName;
    public static String projectorCalib = calibrationFolder + projectorCalibName;

    public static String camCalibARtoolkit = calibrationFolder + "camera-projector.cal";
    public static String kinectIRCalib = calibrationFolder + "calibration-kinect-IR.yaml";
    public static String kinectRGBCalib = calibrationFolder + "calibration-kinect-RGB.yaml";
    public static String kinectStereoCalib = calibrationFolder + "calibration-kinect-Stereo.xml";

    public static String kinectTrackingCalib = "kinectTracking.xml";
    public static String cameraProjExtrinsics = "camProjExtrinsics.xml";

    public static String screenConfig = calibrationFolder + "screenConfiguration.xml";
    public static String cameraConfig = calibrationFolder + "cameraConfiguration.xml";
    public static String tablePosition = calibrationFolder + "tablePosition.xml";
    public static String planeCalib = calibrationFolder + "PlaneCalibration.xml";
    public static String homographyCalib = calibrationFolder + "HomographyCalibration.xml";
    public static String planeAndProjectionCalib = calibrationFolder + "PlaneProjectionCalibration.xml";
    public static String touchCalib = calibrationFolder + "Touch2DCalibration.xml";
    public static String touchCalib3D = calibrationFolder + "Touch3DCalibration.xml";
    public static String defaultFont = folder + "/data/Font/" + "GentiumBookBasic-48.vlw";
    public int defaultFontSize = 12;

    protected static Papart singleton = null;

    protected float zNear = 10;
    protected float zFar = 6000;

    private final PApplet applet;
    private final Class appletClass;

    private boolean displayInitialized;
    private boolean cameraInitialized;
    private boolean touchInitialized;

    private BaseDisplay display;
    private ARDisplay arDisplay;

    private Camera cameraTracking;

    private PVector frameSize = new PVector();
    private CameraOpenKinect cameraOpenKinect;
    private boolean isWithoutCamera = false;

    public CameraConfiguration cameraConfiguration;
    public ScreenConfiguration screenConfiguration;
    // TODO: find what to do with these...

    /**
     * Create the main PapARt object, look at the examples for how to use it.
     *
     * @param applet
     */
    public Papart(Object applet) {
        this.displayInitialized = false;
        this.cameraInitialized = false;
        this.touchInitialized = false;
        this.applet = (PApplet) applet;

        cameraConfiguration = getDefaultCameraConfiguration(this.applet);
        screenConfiguration = getDefaultScreenConfiguration(this.applet);

        this.appletClass = applet.getClass();
        PFont font = this.applet.loadFont(defaultFont);
        // TODO: singleton -> Better implementation. 
        if (Papart.singleton == null) {
            Papart.singleton = this;
        }
    }

    private static CameraConfiguration getDefaultCameraConfiguration(PApplet applet) {
        CameraConfiguration config = new CameraConfiguration();
        config.loadFrom(applet, cameraConfig);
        return config;
    }

    private static ScreenConfiguration getDefaultScreenConfiguration(PApplet applet) {
        ScreenConfiguration config = new ScreenConfiguration();
        config.loadFrom(applet, screenConfig);
        return config;
    }

    /**
     * Start a see through AR application, the size is automatic.
     *
     * @param applet
     * @return
     */
    public static Papart seeThrough(PApplet applet) {

        CameraConfiguration cameraConfiguration = getDefaultCameraConfiguration(applet);

        Camera cameraTracking = CameraFactory.createCamera(
                cameraConfiguration.getCameraType(),
                cameraConfiguration.getCameraName());
        cameraTracking.setParent(applet);
        cameraTracking.setCalibration(cameraCalib);

        Papart papart = new Papart(applet);

        papart.frameSize.set(cameraTracking.width(), cameraTracking.height());
        papart.shouldSetWindowSize = true;
        papart.registerPost();

        papart.initCamera();

        return papart;
    }

    private boolean shouldSetWindowLocation = false;
    private boolean shouldSetWindowSize = false;

    private void registerPost() {
        applet.registerMethod("post", this);
    }

    /**
     * Places the window at the correct location if required, according to the
     * configuration.
     *
     */
    public static void checkWindowLocation() {
        Papart papart = getPapart();
        if (papart != null && papart.shouldSetWindowLocation) {
            papart.defaultFrameLocation();
            papart.shouldSetWindowLocation = false;
        }
        if (papart != null && papart.shouldSetWindowSize) {

            papart.setFrameSize();
            papart.shouldSetWindowSize = true;
        }
    }

    /**
     * Does not draw anything, it used only to check the window location.
     */
    public void post() {
        checkWindowLocation();
        applet.unregisterMethod("post", this);
    }

    /**
     * Set the frame to default location.
     */
    public void defaultFrameLocation() {
        System.out.println("Setting the frame location...");

        this.applet.frame.setLocation(screenConfiguration.getProjectionScreenOffsetX(),
                screenConfiguration.getProjectionScreenOffsetY());
    }

    /**
     * Set the frame to default location.
     */
    public void setFrameSize() {
        System.out.println("Trying to set the size of the frame...");
//        this.applet.frame.setSize((int) frameSize.x, (int) frameSize.y);
        this.applet.getSurface().setSize((int) frameSize.x, (int) frameSize.y);
    }

    protected static void removeFrameBorder(PApplet applet) {
        if (!applet.g.isGL()) {
            applet.frame.removeNotify();
            applet.frame.setUndecorated(true);
            applet.frame.addNotify();
        }
    }

    /**
     * Get the Papart singleton.
     *
     * @return
     */
    public static Papart getPapart() {
        return Papart.singleton;
    }

    public void saveCalibration(String fileName, PMatrix3D mat) {
        HomographyCalibration.saveMatTo(applet, mat, Papart.calibrationFolder + fileName);
    }

    /**
     * Get a calibration from sketchbook/libraries/PapARt/data/calibration
     * folder.
     *
     * @param fileName
     * @return null if the file does not exists.
     */
    public PMatrix3D loadCalibration(String fileName) {

        File f = new File(Papart.calibrationFolder + fileName);
        if (f.exists()) {
            return HomographyCalibration.getMatFrom(applet, Papart.calibrationFolder + fileName);
        } else {
            return null;
        }
    }

    /**
     * Save the position of a paperScreen as the default table location.
     *
     * @param paperScreen
     */
    public void setTableLocation(PaperScreen paperScreen) {
        HomographyCalibration.saveMatTo(applet, paperScreen.getLocation(), tablePosition);
    }

    /**
     * Save the position of a matrix the default table location.
     *
     * @param mat
     */
    public void setTableLocation(PMatrix3D mat) {
        HomographyCalibration.saveMatTo(applet, mat, tablePosition);
    }

    /**
     * The location of the table, warning it must be set once by
     * setTablePosition.
     *
     * @return
     */
    public PMatrix3D getTableLocation() {
        return HomographyCalibration.getMatFrom(applet, tablePosition);
    }

    /**
     * Work in progress function
     *
     * @return
     */
    public PlaneCalibration getTablePlane() {
        return PlaneCalibration.CreatePlaneCalibrationFrom(HomographyCalibration.getMatFrom(applet, tablePosition),
                new PVector(100, 100));
    }

    /**
     * Move a PaperScreen to the table location. After this, the paperScreen
     * location is not updated anymore. To activate the tracking again use :
     * paperScreen.useManualLocation(false); You can move the paperScreen
     * according to its current location with the paperScreen.setLocation()
     * methods.
     *
     * @param paperScreen
     */
    public void moveToTablePosition(PaperScreen paperScreen) {
        paperScreen.useManualLocation(true);
        paperScreen.screen.setMainLocation(HomographyCalibration.getMatFrom(applet, tablePosition));
    }

    @Deprecated
    public void initNoCamera(int quality) {
        this.isWithoutCamera = true;
        initNoCameraDisplay(quality);
    }

    public void initDebug() {
        this.isWithoutCamera = true;
        initDebugDisplay();
    }


    private void tryLoadExtrinsics() {
        PMatrix3D extrinsics = loadCalibration(cameraProjExtrinsics);
        if (extrinsics == null) {
            System.out.println("loading default extrinsics. Could not find " + cameraProjExtrinsics + " .");
        } else {
            arDisplay.setExtrinsics(extrinsics);
        }
    }

    public void initKinectCamera(float quality) {
        assert (!cameraInitialized);

        cameraTracking = CameraFactory.createCamera(Camera.Type.OPEN_KINECT, 0);
        cameraTracking.setParent(applet);
        cameraTracking.setCalibration(kinectRGBCalib);
        ((CameraOpenKinect) cameraTracking).getDepthCamera().setCalibration(kinectIRCalib);
        cameraTracking.start();
        cameraOpenKinect = (CameraOpenKinect) cameraTracking;
        loadTracking(kinectRGBCalib);
        cameraTracking.setThread();

        initARDisplay(quality);

        checkInitialization();
    }

    /**
     * Initialize the default camera for object tracking.
     *
     * @see initCamera(String, int, float)
     */
    public void initCamera() {
        initCamera(cameraConfiguration.getCameraName(),
                cameraConfiguration.getCameraType(), 1);
    }

    /**
     * Initialize a camera for object tracking.
     *
     * @see initCamera(String, int, float)
     */
    public void initCamera(String cameraNo, Camera.Type cameraType) {
        initCamera(cameraNo, cameraType, 1);
    }

    public void initCamera(String cameraNo, Camera.Type cameraType, float quality) {
        assert (!cameraInitialized);

        cameraTracking = CameraFactory.createCamera(cameraType, cameraNo);
        cameraTracking.setParent(applet);
        cameraTracking.setCalibration(cameraCalib);
        cameraTracking.start();
        loadTracking(cameraCalib);
        cameraTracking.setThread();

        initARDisplay(quality);
        checkInitialization();
    }

    private void initARDisplay(float quality) {
        assert (this.cameraTracking != null && this.applet != null);

        arDisplay = new ARDisplay(this.applet, cameraTracking);
        arDisplay.setZNearFar(zNear, zFar);
        arDisplay.setQuality(quality);
        arDisplay.init();
        this.display = arDisplay;
        frameSize.set(arDisplay.getWidth(), arDisplay.getHeight());
        displayInitialized = true;
    }

    @Deprecated
    private void initNoCameraDisplay(float quality) {
        initDebugDisplay();
    }

    private void initDebugDisplay() {
        display = new BaseDisplay();

        display.setFrameSize(applet.width, applet.height);
        display.setDrawingSize(applet.width, applet.height);
        display.init();
        displayInitialized = true;
    }

    private void checkInitialization() {
        assert (cameraTracking != null);
        this.applet.registerMethod("dispose", this);
        this.applet.registerMethod("stop", this);
    }

    private void loadTracking(String calibrationPath) {
        // TODO: check if file exists !
        Camera.convertARParams(this.applet, calibrationPath, camCalibARtoolkit);
        cameraTracking.initMarkerDetection(camCalibARtoolkit);

        // The camera view is handled in another thread;
        cameraInitialized = true;
    }

    private final int depthFormat = freenect.FREENECT_DEPTH_MM;

    private void loadDefaultCameraKinect() {
        cameraOpenKinect = (CameraOpenKinect) CameraFactory.createCamera(Camera.Type.OPEN_KINECT, 0);
        cameraOpenKinect.setParent(applet);
        cameraOpenKinect.setCalibration(kinectRGBCalib);
        cameraOpenKinect.getDepthCamera().setCalibration(kinectIRCalib);
        cameraOpenKinect.getDepthCamera().setDepthFormat(depthFormat);
        cameraOpenKinect.start();
        cameraOpenKinect.setThread();
    }

    public PlanarTouchCalibration getDefaultTouchCalibration() {
        PlanarTouchCalibration calib = new PlanarTouchCalibration();
        calib.loadFrom(applet, Papart.touchCalib);
        return calib;
    }

    public PlanarTouchCalibration getDefaultTouchCalibration3D() {
        PlanarTouchCalibration calib = new PlanarTouchCalibration();
        calib.loadFrom(applet, Papart.touchCalib3D);
        return calib;
    }

    public void loadSketches() {

        // Sketches are not within a package.
        Reflections reflections = new Reflections("");

        Set<Class<? extends PaperScreen>> paperScreenClasses = reflections.getSubTypesOf(PaperScreen.class);

        for (Class<? extends PaperScreen> klass : paperScreenClasses) {
            try {
                Class[] ctorArgs2 = new Class[1];
                ctorArgs2[0] = this.appletClass;
                Constructor<? extends PaperScreen> constructor = klass.getDeclaredConstructor(ctorArgs2);
                System.out.println("Starting a PaperScreen. " + klass.getName());
                constructor.newInstance(this.appletClass.cast(this.applet));
            } catch (Exception ex) {
                System.out.println("Error loading PapartApp : " + klass.getName());
            }
        }

    }

    public void startTracking() {
        if (this.cameraTracking == null) {
            System.err.println("Start Tracking requires a Camera...");
            return;
        }
        this.cameraTracking.trackSheets(true);
    }

    public void stop() {
        this.dispose();
    }

    public void dispose() {
        if (touchInitialized && cameraOpenKinect != null) {
            cameraOpenKinect.close();
        }
        if (cameraInitialized && cameraTracking != null) {
            try {
                cameraTracking.close();
            } catch (Exception e) {
                System.err.println("Error closing the tracking camera" + e);
            }
        }
//        System.out.println("Cameras closed.");
    }

    public BaseDisplay getDisplay() {
//        assert (displayInitialized);
        return this.display;
    }

    public void setDisplay(BaseDisplay display) {
        // todo check this . 
        displayInitialized = true;
        this.display = display;
    }

    public void setTrackingCamera(Camera camera) {
        this.cameraTracking = camera;
        if (camera == null) {
            setNoTrackingCamera();
        }
    }

    public void setNoTrackingCamera() {
        this.isWithoutCamera = true;
    }

    public ARDisplay getARDisplay() {
//        assert (displayInitialized);
        return this.arDisplay;
    }

    public Camera getCameraTracking() {
//        assert (cameraInitialized);
        return this.cameraTracking;
    }

    public PVector getFrameSize() {
        assert (this.frameSize != null);
        return this.frameSize.get();
    }

    public boolean isWithoutCamera() {
        return this.isWithoutCamera;
    }

    public CameraOpenKinect getKinectCamera() {
        return this.cameraOpenKinect;
    }

    public PApplet getApplet() {
        return applet;
    }

}
