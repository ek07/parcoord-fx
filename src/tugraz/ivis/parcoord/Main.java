package tugraz.ivis.parcoord;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("parcoord.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Parallel Coordinates Plot");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        //scene.addEventFilter(MouseEvent.ANY, e -> System.out.println( e));


        Controller controller = loader.getController();
        controller.setStage(primaryStage);
        primaryStage.show();
        //controller.initTestGraphData();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
