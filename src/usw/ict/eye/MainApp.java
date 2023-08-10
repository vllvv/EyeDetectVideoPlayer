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
     * ���� ���̾ƿ��� �ʱ�ȭ�Ѵ�.
     */
    public void initRootLayout() {
        try {
            // fxml ���Ͽ��� ���� ���̾ƿ��� �����´�.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();
            
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);

            // ���� ���̾ƿ��� �����ϴ� scene�� �����ش�.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ���� ���̾ƿ� �ȿ� ����ó ���(person overview)�� �����ش�.
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
     * ���� ���������� ��ȯ�Ѵ�.
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