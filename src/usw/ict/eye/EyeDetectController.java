package usw.ict.eye;

import java.awt.AWTException;
import java.awt.Toolkit;
import java.io.File;
import java.net.MalformedURLException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * The controller associated with the only view of our application. The
 * application logic is implemented here. It handles the button for
 * starting/stopping the camera, the acquired video stream, the relative
 * controls and the face detection/tracking.
 * 
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @version 1.1 (2015-11-10)
 * @since 1.0 (2014-01-10)
 * 
 */
public class EyeDetectController {
   private static final int GRAP_FRAME_DURATION = 33;
   
   @FXML
   private Button cameraButton;
   // the FXML area for showing the current frame
   @FXML
   private ImageView cameraView;

   // a timer for acquiring the video stream
   private ScheduledExecutorService timer;
   // the OpenCV object that performs the video capture
   private VideoCapture capture;
   // a flag to change the button behavior
   private boolean cameraActive;

   // face cascade classifier
   private CascadeClassifier faceCascade;
   private CascadeClassifier eyeCascade;
   private int absoluteFaceSize;
   
   // calculate no face duration
   private LocalTime startNoFace = null;

   private MainApp mainApp;

   /**
    * Init the controller, at start time
    */
   protected void init() {
      this.capture = new VideoCapture();
      
      this.faceCascade = new CascadeClassifier();
      this.faceCascade.load("resources/haarcascades/haarcascade_frontalface_alt.xml");
      
      this.eyeCascade = new CascadeClassifier();
      this.eyeCascade.load("resources/haarcascades/haarcascade_eye.xml");
      
      this.absoluteFaceSize = 0;
   }

   /**
    * The action triggered by pushing the button on the GUI
    */
   @FXML
   protected void handleStartCamera() {
      if (!this.cameraActive) {
         // start the video capture
         this.capture.open(0);

         // is the video stream available?
         if (this.capture.isOpened()) {
            this.cameraActive = true;

            // grab a frame every GRAP_FRAME_DURATION ms (3 frames/sec)
            Runnable frameGrabber = new Runnable() {

               @Override
               public void run() {
                  // effectively grab and process a single frame
                  Mat frame = grabFrame();
                  // convert and show the frame
                  Image imageToShow = Utils.mat2Image(frame);
                  updateImageView(cameraView, imageToShow);
               }
            };

            this.timer = Executors.newSingleThreadScheduledExecutor();
            this.timer.scheduleAtFixedRate(frameGrabber, 0, GRAP_FRAME_DURATION, TimeUnit.MILLISECONDS);
            
            Platform.runLater(() -> {
               // update the button content
               this.cameraButton.setText("Stop Camera");
               });
         } else {
            // log the error
            System.err.println("Failed to open the camera connection...");
         }
      } else {
         // the camera is not active at this point
         this.cameraActive = false;
         
         Platform.runLater(() -> {
            // update again the button content
            this.cameraButton.setText("Start Camera");
            });

         // stop the timer
         this.stopAcquisition();
      }
   }

   /**
    * Get a frame from the opened video stream (if any)
    * 
    * @return the {@link Image} to show
    */
   private Mat grabFrame() {
      Mat frame = new Mat();

      // check if the capture is open
      if (this.capture.isOpened()) {
         try {
            // read the current frame
            this.capture.read(frame);

            // if the frame is not empty, process it
            if (!frame.empty()) {
               // face detection
               this.detectAndDisplay(frame);
            }

         } catch (Exception e) {
            // log the (full) error
            System.err.println("Exception during the image elaboration: " + e);
         }
      }

      return frame;
   }

   /**
    * Method for face detection and tracking
    * 
    * @param frame it looks for faces in this frame
    * @throws AWTException
    */
   private void detectAndDisplay(Mat frame) throws AWTException {
      MatOfRect faces = new MatOfRect();
      Mat grayFrame = new Mat();
      MatOfRect eyes = new MatOfRect();
      Mat eyeFrame;
      boolean isGoodToPlay = false;

      // convert the frame in gray scale
      Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
      // equalize the frame histogram to improve the result
      Imgproc.equalizeHist(grayFrame, grayFrame);

      // compute minimum face size (20% of the frame height, in our case)
      if (this.absoluteFaceSize == 0) {
         int height = grayFrame.rows();
         if (Math.round(height * 0.2f) > 0) {
            this.absoluteFaceSize = Math.round(height * 0.2f);
         }
      }

      // detect faces
      this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
            new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

      // each rectangle in faces is a face: draw them!
      Rect[] facesArray = faces.toArray();
      for (int i = 0; i < facesArray.length; i++) {
         Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);

         eyeFrame = grayFrame.submat(facesArray[i]);
         this.eyeCascade.detectMultiScale(eyeFrame, eyes, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
               new Size(30, 30), new Size());

         Rect[] eyesArray = eyes.toArray();
         if (eyesArray.length >= 2) {
            isGoodToPlay = true;
         }
         for (int j = 0; j < eyesArray.length; j++) {
            Imgproc.rectangle(frame,
                  new Point(facesArray[i].tl().x + eyesArray[j].tl().x,
                        facesArray[i].tl().y + eyesArray[j].tl().y),
                  new Point(facesArray[i].tl().x + eyesArray[j].br().x,
                        facesArray[i].tl().y + eyesArray[j].br().y),
                  new Scalar(0, 0, 255), 3);

         }
      }
      if (isGoodToPlay == true) {
//         this.mediaPlayer.play();  
         this.startNoFace = null;
      } else {
         LocalTime current = LocalTime.now();
         if (this.startNoFace == null) this.startNoFace = current;
         else {
            Duration duration = Duration.between(this.startNoFace, current);
            if (duration.getSeconds() > 1) {
               this.handleStartCamera();
               Toolkit.getDefaultToolkit().beep();
            }
         }
      }
   }

   /**
    * Stop the acquisition from the camera and release all the resources
    */
   private void stopAcquisition() {
      if (this.timer != null && !this.timer.isShutdown()) {
         try {
            // stop the timer
            this.timer.shutdown();
            this.timer.awaitTermination(GRAP_FRAME_DURATION, TimeUnit.MILLISECONDS);
         } catch (InterruptedException e) {
            // log any exception
            System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
         }
      }

      if (this.capture.isOpened()) {
         // release the camera
         this.capture.release();
      }
   }

   /**
    * Update the {@link ImageView} in the JavaFX main thread11111
    * 
    * @param view  the {@link ImageView} to update
    * @param image the {@link Image} to show
    */
   private void updateImageView(ImageView view, Image image) {
      Utils.onFXThread(view.imageProperty(), image);
   }

   /**
    * On application close, stop the acquisition from the camera
    */
   protected void setClosed() {
      this.stopAcquisition();
   }
   
   public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        
    }
}