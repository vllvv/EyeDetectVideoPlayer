package usw.ict.eye;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

/**
 * The controller associated with the only view of our application. The
 * application logic is implemented here. It handles the button for
 * starting/stopping the camera, the acquired video stream, the relative
 * controls and the face detection/tracking.
 */
public class StepByStepController {
//   private MainApp mainApp;

   @FXML private Button btnStep1;
   @FXML private Button btnStep2;
   @FXML private Button btnStep3;
   @FXML private Button btnStep4;
   @FXML private Button btnStep5;

   // the FXML area for showing the current frame
   @FXML private ImageView imageView;
   // checkboxes for enabling/disabling a classifier

   // the OpenCV object that performs the video capture
   private VideoCapture capture;
   private Mat frame;

   private EyeDetect eyeDetect = new EyeDetect();
   
   public void setMainApp(MainApp mainApp) {
//      this.mainApp = mainApp;
   }
   
   /**
    * Init the controller, at start time
    */
   public void init() {
      this.capture = new VideoCapture();
      btnStep1.setDisable(false);
      btnStep2.setDisable(true);
      btnStep3.setDisable(true);
      btnStep4.setDisable(true);
      btnStep5.setDisable(true);
   }

   private void updateImageView(ImageView view, Image image) {
      Utils.onFXThread(view.imageProperty(), image);
   }

   @FXML
   protected void handleTakePhoto() {
      frame = new Mat();

      this.capture.open(0);
      if (this.capture.isOpened()) {
         try {
            // read the current frame
            this.capture.read(frame);

            // if the frame is not empty, process it
            if (!frame.empty()) {
               btnStep2.setDisable(false);
               btnStep3.setDisable(false);
               btnStep4.setDisable(false);
               btnStep5.setDisable(false);
            }

         } catch (Exception e) {
            // log the (full) error
            System.err.println("Exception during taking photo: " + e);
         }
         Image imageToShow = Utils.mat2Image(frame);
         updateImageView(imageView, imageToShow);
      }
      this.capture.release();
   }

   @FXML
   protected void handleConvertToGray() {
      if (!frame.empty()) {
         Mat result = this.eyeDetect.detectAndDisplay(frame, EyeDetect.GRAY_SCALE);

         Image imageToShow = Utils.mat2Image(result);
         updateImageView(imageView, imageToShow);
      }
   }

   @FXML
   protected void handleDetectFaces() {
      if (!frame.empty()) {
         Mat result = this.eyeDetect.detectAndDisplay(frame, EyeDetect.DETECT_FACES);

         Image imageToShow = Utils.mat2Image(result);
         updateImageView(imageView, imageToShow);
      }
   }

   @FXML
   protected void handleDetectEyes() {
      if (!frame.empty()) {
         Mat result = this.eyeDetect.detectAndDisplay(frame, EyeDetect.DETECT_EYES);

         Image imageToShow = Utils.mat2Image(result);
         updateImageView(imageView, imageToShow);
      }
   }

   @FXML
   protected void handleFinalResult() {
      if (!frame.empty()) {
         Mat result = this.eyeDetect.detectAndDisplay(frame, EyeDetect.FINAL_RESULT);

         Image imageToShow = Utils.mat2Image(result);
         updateImageView(imageView, imageToShow);
      }
   }

}