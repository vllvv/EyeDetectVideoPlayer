package usw.ict.eye;

import java.io.IOException;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author bonas
 *
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Eye Detection Video Player");

        initRootLayout();
        showStepByStep();
    }

    /**
     * 상위 레이아웃을 초기화한다.
     */
    public void initRootLayout() {
        try {
            // fxml 파일에서 상위 레이아웃을 가져온다.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();
            
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);

            // 상위 레이아웃을 포함하는 scene을 보여준다.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 상위 레이아웃 안에 연락처 요약(person overview)을 보여준다.
     */
    public void showEyeDetectVideoPlayerLayout(String fileURL) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/EyeDetectVideoPlayerLayout.fxml"));
            BorderPane faceDetection = (BorderPane) loader.load();

            rootLayout.setCenter(faceDetection);
            
            // init the controller
            EyeDetectVideoPlayerController controller = loader.getController();
            controller.setMainApp(this);
            controller.init(fileURL);
            
            primaryStage.setOnCloseRequest((we -> controller.setClosed()));
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void showEyeDetectLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/EyeDetectLayout.fxml"));
            BorderPane faceDetection = (BorderPane) loader.load();

            rootLayout.setCenter(faceDetection);
            
            // init the controller
            EyeDetectController controller = loader.getController();
            controller.setMainApp(this);
            controller.init();
            
            primaryStage.setOnCloseRequest((we -> controller.setClosed()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void showStepByStep() {
      try {
         FXMLLoader loader = new FXMLLoader();
         loader.setLocation(MainApp.class.getResource("view/StepByStepLayout.fxml"));
         BorderPane eyeDetect = loader.load();

         rootLayout.setCenter(eyeDetect);

         StepByStepController controller = loader.getController();
         controller.setMainApp(this);
         controller.init();

      } catch (IOException e) {
         e.printStackTrace();
      }
   }
    
    
    /**
     * 메인 스테이지를 반환한다.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
       // load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
       launch(args);
    }
}