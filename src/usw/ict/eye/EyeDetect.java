package usw.ict.eye;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

public class EyeDetect {
    public static final int GRAY_SCALE = 1;
    public static final int DETECT_FACES = 2;
    public static final int DETECT_EYES = 3;
    public static final int FINAL_RESULT = 4;

    private CascadeClassifier faceCascade;
    private CascadeClassifier eyeCascade;
    private int absoluteFaceSize;


    /**
     * Init the controller, at start time
     */
    public EyeDetect() {
        this.faceCascade = new CascadeClassifier();
        this.faceCascade.load("resources/haarcascades/haarcascade_frontalface_alt.xml");

        this.eyeCascade = new CascadeClassifier();
        this.eyeCascade.load("resources/haarcascades/haarcascade_eye.xml");

        this.absoluteFaceSize = 0;
    }

    /**
     * Method for face detection and tracking
     *
     * @param orig it looks for faces in this frame
     */
    public Mat detectAndDisplay(Mat orig, int step) {
        Mat grayFrame; // = new Mat();
        Mat result = orig.clone();

        // compute minimum face size (20% of the result height, in our case)
        if (this.absoluteFaceSize == 0) {
            int height = result.rows();
            if (Math.round(height * 0.2f) > 0) {
                this.absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        // Step 1 : convert to gray scaled image
        grayFrame = convertToGrayScaledImage(orig);
        if (step != FINAL_RESULT) {
            Imgproc.cvtColor(grayFrame, result, Imgproc.COLOR_GRAY2BGR);
        }

        if (step == GRAY_SCALE) return result;

        // Step 2 : Detect Faces
        Rect[] facesArray = procDetectFaces(result, grayFrame);
        if (step == DETECT_FACES) return result;

        // Step 3 : Detect Eyes
        for (Rect face : facesArray) {
            procDetectEyes(result, grayFrame, face);
        }
        if (step == DETECT_EYES) return result;

        return result;
    }

    private Mat convertToGrayScaledImage(Mat orig) {
        Mat grayFrame = new Mat();

        // convert the frame in gray scale
        Imgproc.cvtColor(orig, grayFrame, Imgproc.COLOR_BGR2GRAY);
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);

        return grayFrame;
    }

    private Rect[] procDetectFaces(Mat orig, Mat gray) {
        MatOfRect faces = new MatOfRect();

        // detect faces
        this.faceCascade.detectMultiScale(gray, faces, 1.1, 2, Objdetect.CASCADE_SCALE_IMAGE,
                new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

        Rect[] facesArray = faces.toArray();

        for (Rect face : facesArray) {
            Imgproc.rectangle(orig, face.tl(), face.br(), new Scalar(0, 255, 0), 3);
        }

        // each rectangle in faces is a face: draw them!
        return facesArray;
    }

    private void procDetectEyes(Mat orig, Mat gray, Rect face) {
        MatOfRect eyes = new MatOfRect();

        Mat eyeFrame = gray.submat(face);

        this.eyeCascade.detectMultiScale(eyeFrame, eyes, 1.1, 2, Objdetect.CASCADE_SCALE_IMAGE,
                new Size(30, 30), new Size());

        Rect[] eyesArray = eyes.toArray();

        for (Rect eye : eyesArray) {
            Imgproc.rectangle(orig,
                    new Point(face.tl().x + eye.tl().x,face.tl().y + eye.tl().y),
                    new Point(face.tl().x + eye.br().x,face.tl().y + eye.br().y),
                    new Scalar(0, 0, 255), 3);
        }
    }
}
