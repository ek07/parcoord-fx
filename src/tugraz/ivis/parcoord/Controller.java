package tugraz.ivis.parcoord;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tugraz.ivis.parcoord.chart.ParallelCoordinatesChart;
import tugraz.ivis.parcoord.chart.Record;
import tugraz.ivis.parcoord.chart.Series;
import tugraz.ivis.parcoord.util.importer.DataModel;

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
        // changed to current directory
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV", "*.csv"),
                new FileChooser.ExtensionFilter("All", "*.*"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            /*
                wanted to do some kind of feedback while loading, didn't succeed

                //parcoordChart.clear();
                parcoordChart.setTitle("loading ...");
            */
            importDataFromFile(file.getAbsolutePath());
            parcoordChart.setTitle(file.getName());
        }
    }

    @FXML
    public void onShowInfo(ActionEvent actionEvent) {
        showInfoDialog("Project made over the course of the lecture " +
                "'Information Visualisation'\nat Graz University of Technology by:" +
                "\n  Thomas Absenger\n  Mohammad Chegini\n  Thorsten Ruprechter\n  Helmut Zöhrer");
    }

    @FXML
    public void onResetBrushing(ActionEvent actionEvent) {
        if (parcoordChart != null)
            parcoordChart.resetBrushing();
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
            setDataModelToGraph(dm);
        }

        parcoordChart.enableBrushing();
    }

    private void showErrorDialog(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.show();
    }

    private void showInfoDialog(String headerText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(headerText);
        alert.show();
    }

    public void setDataModelToGraph(DataModel dm) {
        List<Record> series = dm.getItemsAsRecords();
        List<Record> series1 = series.subList(0, series.size() / 2);
        List<Record> series2 = series.subList(series.size() / 2, series.size());

        Series s1 = new Series(series1, Color.RED, 0.2);
        Series s2 = new Series(series2, Color.BLUE, 0.2);
        parcoordChart.clear();
        parcoordChart.setMinMaxValuesFromArray(dm.getMinMaxValues());
        parcoordChart.setAxisLabels(dm.getDataHeader());
        parcoordChart.addSeries(s1);
        parcoordChart.addSeries(s2);
        parcoordChart.setHighlightColor(Color.BLACK);
        parcoordChart.setHighlightStrokeWidth(3);
    }

    //TODO: this is just a "hack" for testing
    public void initTestGraphData() {
        // TODO: hardcoded path because its simply quicker for now
        DataModel dm = new DataModel("/home/thorsten/Uni/master/Sem3/InfoVis/Ass3/parcoord-fx/src/data/auto3.csv", ";", true);
        //new DataModel("C:\\Users\\mchegini\\Documents\\NetBeansProjects\\PaCoPlot\\parcoord-fx\\src\\data\\auto3.csv", ";", true);
        //DataModel dm = new DataModel("C:\\Users\\Thomas\\Documents\\Uni\\10. Semester\\ivis\\project\\parcoord-fx\\src\\data\\auto3.csv", ";", true);
        setDataModelToGraph(dm);
        parcoordChart.enableBrushing();
    }
}
