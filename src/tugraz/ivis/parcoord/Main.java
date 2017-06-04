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
        primaryStage.setTitle("Hello World");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        Controller controller = loader.getController();
        controller.setStage(primaryStage);
        primaryStage.show();
        //DataModel dm = new DataModel("C:\\Users\\mchegini\\Documents\\NetBeansProjects\\PaCoPlot\\parcoord-fx\\src\\data\\auto3.csv", ";", true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
