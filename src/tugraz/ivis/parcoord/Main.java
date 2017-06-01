package tugraz.ivis.parcoord;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tugraz.ivis.parcoord.util.data.DataSet;
import tugraz.ivis.parcoord.util.importer.ExampleDataImporter;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // TODO: initialize properly
        Parent root = FXMLLoader.load(getClass().getResource("parcoord.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 500, 500));
        primaryStage.show();
    }

    public static void main(String[] args) {
        // just print for testing for now
        ExampleDataImporter importer = new ExampleDataImporter();
        DataSet data = importer.getDataSet();
        System.out.println(data.toString());

        launch(args);
    }
}
