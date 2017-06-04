package tugraz.ivis.parcoord;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tugraz.ivis.parcoord.chart.ParallelCoordinatesChart;
import tugraz.ivis.parcoord.util.importer.DataModel;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

// FXML interaction goes here
public class Controller implements Initializable {
    // === FXML params
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private ParallelCoordinatesChart parcoordChart;

    // === other helper params
    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    // this is called by Main
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void onFileOpen(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open CSV File");
        // for now, select code repo - better for testing
        //fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV", "*.csv"),
                new FileChooser.ExtensionFilter("All", "*.*"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            importDataFromFile(file.getAbsolutePath());
        }
    }

    private void importDataFromFile(String absolutePath) {
        // just print for testing for now
        DataModel dm = null;
        try {
            dm = new DataModel(absolutePath, ";", true);
        } catch (Exception e) {
            showErrorDialog("Error while importing",
                    "An error occured while parsing the input file.");

            e.printStackTrace();
        }

        if (dm != null) {
            dm.printDataSet();
        }
    }

    private void showErrorDialog(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.show();
    }
}
