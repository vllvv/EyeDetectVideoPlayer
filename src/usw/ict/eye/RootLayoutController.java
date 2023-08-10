package usw.ict.eye;

import java.io.File;
import java.net.MalformedURLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class RootLayoutController {
   private MainApp mainApp;

   @FXML
   public void handleFileOpen(ActionEvent event) {
      
      FileChooser fileChooser = new FileChooser();
      File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());
      
      if (file != null) {
         try {
            mainApp.showEyeDetectVideoPlayerLayout(file.toURI().toURL().toExternalForm());
         } catch (MalformedURLException e1) {
            e1.printStackTrace();
         }
      }
   }

   @FXML
   public void handleEyeDetect(ActionEvent event) {   
      mainApp.showEyeDetectLayout();
   }
   
   @FXML
   public void handleStepByStep() {
      mainApp.showStepByStep();
   }

   /**
     * 참조를 다시 유지하기 위해 메인 애플리케이션이 호출한다.
     *
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
}