package usw.ict.eye;

import java.awt.AWTException;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
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

import sun.audio.*;  

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
public class EyeDetectVideoPlayerController {
   private static final int GRAP_FRAME_DURATION = 330;
   
   @FXML
   private Button cameraButton;
   // the FXML area for showing the current frame
   @FXML
   private ImageView cameraView;
   // checkboxes for enabling/disabling a classifier
   @FXML
   private MediaView mediaView;
   @FXML
   private Button playButton;
   @FXML
   private Slider mediaSlider;
   
   private MediaPlayer mediaPlayer;
   private Media media;

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

   private int jacheol;

   private String koo;

   /**
    * Init the controller, at start time
    */
   protected void init(String fileURL) {
      this.capture = new VideoCapture();
      
      this.faceCascade = new CascadeClassifier();
      this.faceCascade.load("resources/haarcascades/haarcascade_frontalface_alt.xml");
      
      this.eyeCascade = new CascadeClassifier();
      this.eyeCascade.load("resources/haarcascades/haarcascade_eye.xml");
      
      this.absoluteFaceSize = 0;

      // set a fixed width for the frame
      cameraView.setFitWidth(200);
      // preserve image ratio
      cameraView.setPreserveRatio(true);

      this.media = new Media(fileURL);
      this.mediaPlayer = new MediaPlayer(media);
      this.mediaView.setMediaPlayer(this.mediaPlayer);
      
      // Providing functionality to time slider 
      mediaPlayer.currentTimeProperty().addListener(new InvalidationListener() { 
            public void invalidated(Observable ov) 
            { 
                updateSlide(); 
            } 
        }); 

        // Inorder to jump to the certain part of video 
        mediaSlider.valueProperty().addListener(new InvalidationListener() { 
            public void invalidated(Observable ov) 
            { 
                if (mediaSlider.isPressed()) { // It would set the time 
                    // as specified by user by pressing 
                   mediaPlayer.seek(mediaPlayer.getMedia().getDuration().multiply(mediaSlider.getValue() / 100)); 
                } 
            } 
        });
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

            this.mediaPlayer.play();
            
            Platform.runLater(() -> {
               // update the button content
               this.cameraButton.setText("Stop Camera");
               this.playButton.setText("||");
               });
         } else {
            // log the error
            System.err.println("Failed to open the camera connection...");
         }
      } else {
         // the camera is not active at this point
         this.cameraActive = false;
         this.mediaPlayer.pause();
         
         Platform.runLater(() -> {
            // update again the button content
            this.cameraButton.setText("Start Camera");
            this.playButton.setText(">");
            });

         // stop the timer
         this.stopAcquisition();
      }
   }

   @FXML
   public void videoplay() {      
      Status status = mediaPlayer.getStatus(); // To get the status of Player 
        if (status == Status.PLAYING) { 

            // If the status is Video playing 
            if (mediaPlayer.getCurrentTime().greaterThanOrEqualTo(mediaPlayer.getTotalDuration())) { 

                // If the player is at the end of video 
               mediaPlayer.seek(mediaPlayer.getStartTime()); // Restart the video 
               mediaPlayer.play(); 
            } 
            else { 
                // Pausing the player 
               mediaPlayer.pause(); 
                playButton.setText(">"); 
            } 
        } // If the video is stopped, halted or paused 
        if (status == Status.HALTED || status == Status.STOPPED || status == Status.PAUSED) { 
           mediaPlayer.play(); // Start the video 
            playButton.setText("||"); 
        } 
        
   }

   @FXML
   public void handleFileOpen(ActionEvent event) {
      if (this.cameraActive) handleStartCamera();
      else mediaPlayer.pause();
      
      FileChooser fileChooser = new FileChooser();
      File file = fileChooser.showOpenDialog((Stage) this.cameraButton.getScene().getWindow());
      
      if (file != null) {
         try {
            this.media = new Media(file.toURI().toURL().toExternalForm());
            this.mediaPlayer = new MediaPlayer(media);
            this.mediaView.setMediaPlayer(this.mediaPlayer);
         } catch (MalformedURLException e1) {
            e1.printStackTrace();
         }
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
      int num1 = (int) (Math.random() * 101);
      int num2 = (int) (Math.random() * 101);
      String total;

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
               this.mediaPlayer.pause();
               try {
               AudioInputStream ais = AudioSystem.getAudioInputStream(new File("C:\\119.wav"));
               Clip clip = AudioSystem.getClip();
               clip.open(ais);

               clip.start();
               clip.loop(-1); //반복재생

               

                  while (true) {
                     String name = JOptionPane.showInputDialog(num1 +"+"+num2);
                     if (name == null) {
                        JOptionPane.showMessageDialog(null, " 알람을 끌 수 없습니다.");
                        continue;
                     }
                     if (name.equals(num1+num2+"")) {
                        JOptionPane.showMessageDialog(null, "알람이 꺼집니다");
                        clip.stop();
                        break;
                     }

                     else {
                        JOptionPane.showMessageDialog(null, " 알람을 끌 수 없습니다.");
                     }

                  }
               } catch (Exception e) {
                  e.printStackTrace();

               }
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
   
   protected void updateSlide() 
    { 
        Platform.runLater(new Runnable() { 
            public void run() 
            { 
                // Updating to the new time value 
                // This will move the slider while running your video 
               mediaSlider.setValue(mediaPlayer.getCurrentTime().toMillis() / mediaPlayer.getTotalDuration().toMillis() * 100); 
            } 
        }); 
    } 
   
   public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        
    }
}