package tugraz.ivis.parcoord.chart;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.CacheHint;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.controlsfx.control.RangeSlider;
import tugraz.ivis.parcoord.chart.Record.Status;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO: implement basic graph here
// TODO: this is basically only a bit of "playing around" for now
public class ParallelCoordinatesChart extends HighDimensionalChart {
    private static final double BUTTON_MARGIN = 5.0;
    private List<String> axisLabels;
    private Map<Integer, ParallelCoordinatesAxis> axes = new HashMap<>();
    private boolean useAxisFilters = true;
    private double filteredOutOpacity = 0.0;

    private double pathStrokeWidth = 1.0;

    private double legend_height_relative = 0.05;

    private boolean useHighlighting = true;
    private double highlightOpacity = 1.0;
    private Color highlightColor = Color.RED;
    private double highlightStrokeWidth = 3.0;

    private Rectangle brushingRectangle;
    private double brushingRectangleX = 0.0;
    private double brushingRectangleY = 0.0;

    private ExecutorService highlightExecutor = Executors.newFixedThreadPool(1);
    private ExecutorService filterExecutor = Executors.newFixedThreadPool(4);
    private ExecutorService brushingExecutor = Executors.newFixedThreadPool(1);

    private long lastFilterHandle = 0;
    private final static long FILTER_FREQUENCY = 100; // handle filter changes every x milliseconds

    /**
     * Default constructor needed for FXML
     */
    public ParallelCoordinatesChart() {
    }

    /**
     * Sets the new axesLabels
     *
     * @param axisLabels the labels of the axes
     */
    public void setAxisLabels(List<String> axisLabels) {
        this.axisLabels = axisLabels;
    }

    /**
     * Immediately clears the whole chart and chartChildren
     */
    @Override
    public void clear() {
        super.clear();
        if (axisLabels != null)
            axisLabels.clear();
        if (axes != null)
            axes.clear();
    }

    /**
     * Reorders elements in the z dimensions to push certain elements to the front.
     */
    protected void reorder() {
        for (ParallelCoordinatesAxis axis : axes.values()) {
            if (axis.getFilterSlider() != null)
                axis.getFilterSlider().toFront();
        }
    }

    /**
     * Initially adds a given axis to the List of axis which exist in this chart.
     * This leads to an setting of the ids and the initial position (this setting of id is the important thing!)
     */
    @Override
    protected void createAxes() {
        int numAxes = getAttributeCount();
        for (int iAxis = 0; iAxis < numAxes; iAxis++) {
            axes.put(iAxis, new ParallelCoordinatesAxis(iAxis));
        }
    }

    /**
     * Creates and binds axes, axes labels and filters.
     */
    protected void bindAxes() {
        int numAxes = getAttributeCount();

        // configurable
        double spaceBetweenTicks = 50;
        double labelMinWidth = 500;
        double labelYOffset = 0;

        List<MinMaxPair> minMax = getMinMaxValues();

        Pane buttonPane = new Pane();
        getChartChildren().add(buttonPane);
        Image btnInvertImg = new Image("resources/invert_1x.png", 17, 17, true, true);
        Image btnRightImg = new Image("resources/right_1x.png", 17, 17, true, true);
        Image btnLeftImg = new Image("resources/left_1x.png", 17, 17, true, true);

        for (ParallelCoordinatesAxis pcAxis : axes.values()) {
            int axisIndex = pcAxis.getAxisIndex(); // the current position
            int axisId = pcAxis.getId();
            String label = null;
            if (axisLabels.size() - 1 >= axisId) {
                if (axisId < axisLabels.size()) {
                    label = axisLabels.get(axisId);
                } else {
                    label = "?";
                }
            }

            DoubleBinding trueAxisSeparation = getAxisSeparationBinding().multiply(axisIndex + 1);

            double upperBound = minMax.get(axisId).getMaximum();
            double lowerBound = minMax.get(axisId).getMinimum();
            double delta = Math.abs(upperBound - lowerBound);

            // Buttons
            Button btnInvert = new Button();
            btnInvert.setGraphic(new ImageView(btnInvertImg));
            DoubleBinding invertBtnPosition = trueAxisSeparation.subtract(btnInvert.widthProperty().divide(2));
            btnInvert.translateXProperty().bind(invertBtnPosition);

            buttonPane.getChildren().add(btnInvert);
            Button btnRight = new Button();
            btnRight.setGraphic(new ImageView(btnRightImg));
            btnRight.translateXProperty().bind(invertBtnPosition.add(btnInvert.widthProperty()));
            buttonPane.getChildren().add(btnRight);

            Button btnLeft = new Button();
            btnLeft.setGraphic(new ImageView(btnLeftImg));
            btnLeft.translateXProperty().bind(invertBtnPosition.subtract(btnLeft.widthProperty()));
            buttonPane.getChildren().add(btnLeft);

            // axis
            NumberAxis numberAxis = new NumberAxis(null, lowerBound, upperBound, 1.0);
            numberAxis.setSide(Side.LEFT);
            numberAxis.setMinorTickVisible(false);
            numberAxis.setAnimated(false);
            numberAxis.translateXProperty().bind(trueAxisSeparation);
            DoubleBinding heightButton = btnInvert.heightProperty().add(BUTTON_MARGIN);
            numberAxis.translateYProperty().bind(heightButton);
            DoubleBinding innerHeightWithoutButton = innerHeightProperty().subtract(heightButton);
            numberAxis.tickUnitProperty().bind(
                    innerHeightWithoutButton.divide(innerHeightWithoutButton).divide(innerHeightWithoutButton)
                            .multiply(spaceBetweenTicks).multiply(delta));

            getChartChildren().add(numberAxis);

            // label
            HBox box = null;
            if (showLabels) {
                Label labelNode = new Label(label);
                labelNode.setMinWidth(labelMinWidth);
                labelNode.setAlignment(Pos.CENTER);
                box = new HBox(labelNode);
                box.translateXProperty().bind(trueAxisSeparation.subtract(labelMinWidth / 2));
                box.translateYProperty().bind(innerHeightProperty.subtract(labelYOffset));

                getChartChildren().add(box);
            }

            // filters
            RangeSlider vSlider = null;
            if (useAxisFilters) {

                // using bounds from 1.0 to 0.0 should work as we draw in this space anyway
                vSlider = new RangeSlider(0.0, 1.0, 0.0, 1.0);
                vSlider.setOrientation(Orientation.VERTICAL);
                vSlider.setShowTickLabels(false);
                vSlider.setShowTickMarks(false);
                vSlider.translateXProperty().bind(trueAxisSeparation);
                vSlider.translateYProperty().bind(btnInvert.heightProperty().add(BUTTON_MARGIN));
                vSlider.getProperties().put("axis", pcAxis.getId());

                addFilterListeners(vSlider);

                getChartChildren().add(vSlider);

                // have to style after adding it (CSS wouldn't be accessible otherwise)
                vSlider.applyCss();
                vSlider.lookup(".range-slider .track").setStyle("-fx-opacity: 0;");
                // TODO fix range-bar gap
                vSlider.lookup(".range-slider .range-bar").setStyle("-fx-opacity: 0.15;");
                vSlider.lookup(".range-slider .range-bar").setDisable(true);
                vSlider.lookup(".range-slider .low-thumb").setStyle("-fx-shape: \"M150 0 L75 200 L225 200 Z\"; -fx-scale-y: 0.5; -fx-translate-y: 5; -fx-scale-x:1.3;");
                vSlider.lookup(".range-slider .high-thumb").setStyle("-fx-shape: \"M75 0 L225 0 L150 200 Z\"; -fx-scale-y: 0.5; -fx-translate-y: -5; -fx-scale-x:1.3;");

                vSlider.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        mouseEvent.consume();
                    }
                });
            }

            btnInvert.setOnAction(event -> {
                pcAxis.invert();
                redrawAllSeries();
                reorder();
            });

            btnRight.setOnAction(event -> {
                // redraws everything by hard right now
                // TODO moveAxes: in the future, this should be replaced by something better performing
                int newIndex = pcAxis.getAxisIndex() == (numAxes - 1) ? 0 : pcAxis.getAxisIndex() + 1;
                moveAxis(pcAxis.getAxisIndex(), newIndex);
                getChartChildren().remove(buttonPane);
            });

            btnLeft.setOnAction(event -> {
                // redraws everything by hard right now
                // TODO moveAxes: in the future, this should be replaced by something better performing
                int newIndex = pcAxis.getAxisIndex() == 0 ? numAxes - 1 : pcAxis.getAxisIndex() - 1;
                moveAxis(pcAxis.getAxisIndex(), newIndex);
                getChartChildren().remove(buttonPane);
            });

            pcAxis.initialize(numberAxis, label, box, vSlider, btnInvert, btnLeft, btnRight);
            pcAxis.registerDragAndDropListener(this);
        }

        resizeAxes();
    }

    /**
     * Removes all javaFX components of all ParallelCoordinatesAxis objects from the graph:
     * TODO moveAxes: in the future, this should be replaced by something better performing
     * --> may not even be needed anymore
     */
    private void removeAxesFromChartChildren() {
        for (ParallelCoordinatesAxis pcAxes : axes.values()) {
            getChartChildren().remove(pcAxes.getAxis());
            getChartChildren().remove(pcAxes.getLabelBox());
            //getChartChildren().remove(pcAxes.getBtnInvert());
            // getChartChildren().remove(pcAxes.getBtnRight());
            //getChartChildren().remove(pcAxes.getBtnLeft());
            getChartChildren().remove(pcAxes.getFilterSlider());
        }
    }


    /**
     * Moves the given axes to the correct position and repositions the other ones
     * TODO moveAxes: in the future, this should be replaced by something better performing (or be doing more)
     */
    public void moveAxis(int oldIndex, int newIndex) {
        int deltaPosition = newIndex - oldIndex;
        ParallelCoordinatesAxis currAxis = getAxisByIndex(oldIndex);

        if (deltaPosition == 0) {
            System.out.println("MoveAxis: Same position for axis");
            return; // same position, nothing to do here
        }

        if (newIndex < -1 || newIndex > (axes.size() - 1)) {
            System.out.println("MoveAxis: invalid axes index");
            return;
        }

        for (ParallelCoordinatesAxis axis : axes.values()) {
            int axisIndex = axis.getAxisIndex();
            if (currAxis.getAxisIndex() != axisIndex) {
                if (deltaPosition > 0) {
                    // move pcAxis left, move all others right
                    if (oldIndex < axisIndex && axisIndex <= newIndex) {
                        axis.moveToPosition(axisIndex - 1, axes);
                    }
                } else {
                    // move pcAxis right, move all others right
                    if (oldIndex > axisIndex && axisIndex >= newIndex) {
                        axis.moveToPosition(axisIndex + 1, axes);
                    }
                }
            }
        }
        currAxis.moveToPosition(newIndex, axes);

        // refreshUI
        removeAxesFromChartChildren();
        bindAxes();
        redrawAllSeries();
        reorder();
    }

    /**
     * Swaps the position of the given axes given axes to the correct position and repositions the other ones
     * TODO moveAxes: in the future, this should be replaced by something better performing (or be doing more)
     */
    public void swapAxes(int oldIndex, int newIndex) {
        int deltaPosition = newIndex - oldIndex;
        ParallelCoordinatesAxis currAxis = getAxisByIndex(oldIndex);
        ParallelCoordinatesAxis swapAxis = getAxisByIndex(newIndex);
        currAxis.moveToPosition(newIndex, axes);
        swapAxis.moveToPosition(oldIndex, axes);

        // refreshUI
        removeAxesFromChartChildren();
        bindAxes();
        redrawAllSeries();
        reorder();
    }

    /**
     * Adds listeners to the given slider to be notified when high and low values change.
     *
     * @param slider the slider to add listeners to
     */

    private void addFilterListeners(RangeSlider slider) {
        ChangeListener<Number> highListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
                filterExecutor.submit(() -> {
                    int axisId = (int) slider.getProperties().get("axis");
                    handleFilterChange(axisId, oldVal, newVal, true);
                });
            }
        };

        slider.highValueProperty().addListener(highListener);
        slider.getProperties().put("highListener", highListener);

        ChangeListener<Number> lowListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
                filterExecutor.submit(() -> {
                    int axisId = (int) slider.getProperties().get("axis");
                    handleFilterChange(axisId, oldVal, newVal, false);
                });
            }
        };

        slider.lowValueProperty().addListener(lowListener);
        slider.getProperties().put("lowListener", lowListener);
    }

    /**
     * Handle changes to filter values. All filters have to be checked again for newly added lines.
     *
     * @param axisId      Index of the affected axis0
     * @param oldValue    Old value of the filter (not used)
     * @param newValue    New value of the filter
     * @param isHighValue Indicates whether the changed value was a high value or low value
     */
    private void handleFilterChange(int axisId, Number oldValue, Number newValue, boolean isHighValue) {

        // TODO replace this with a proper async solution (as this isn't working as intended)
        long systemTime = System.currentTimeMillis();
        if (systemTime - lastFilterHandle < FILTER_FREQUENCY) {
            return;
        }
        lastFilterHandle = systemTime;

        ParallelCoordinatesAxis axis = getAxisById(axisId);
        double newV = newValue.doubleValue();
        double oldV = 0;

        // sliders don't quite manage to reach extreme values
        if (newV > 0.99)
            newV = 1.0;
        if (newV < 0.01)
            newV = 0.0;

        //everything is switched around when inverted
        if (axis.isInverted()) {
            newV = 1.0 - newV;
            isHighValue = !isHighValue;
        }

        if (isHighValue) {
            oldV = axis.getFilterHigh();
            axis.setFilterHigh(newV);
            if (newV > oldV) {
                // new lines could get active - we have to check all filters for the new lines
                // iterate through all lines, if a new line would be added, check the line for all other filters as well
                filterInLines(axisId, newV, true);
            } else {
                // this can only diminish the number of visible lines
                filterOutLines(axisId, newV, true);
            }
        } else {
            oldV = axis.getFilterLow();
            axis.setFilterLow(newV);
            if (newV < oldV) {
                // new lines could get active - we have to check all filters for the new lines
                // iterate through all lines, if a new line would be added, check the line for all other filters as well
                filterInLines(axisId, newV, false);
            } else {
                // this can only diminish the number of visible lines
                // iterate through all lines and simply set them invisible if required
                filterOutLines(axisId, newV, false);
            }
        }

        //System.out.println("Old: " + Double.toString(oldV) + "; New: " + Double.toString(newV));
    }

    /**
     * Sets records to opaque if they have to be removed according to the filter criteria.
     * This method can only hide lines, not make them visible.
     *
     * @param axisId      The index of the axis the filter is on
     * @param filterValue The updated filter value
     * @param isHighValue Whether the filter value is a high or low value
     */
    private void filterOutLines(int axisId, double filterValue, boolean isHighValue) {
        for (Series s : series) {
            for (Record r : s.getRecords()) {
                // TODO investigate why this is necessary
                if (r.getValues().get(axisId) == null)
                    continue;

                // we cannot skip lines which are already hidden here (causes a bug with brushing)
//				if(!r.isVisible())
//					continue;

                double recordValue = (double) r.getValues().get(axisId);
                if (!isHighValue && recordValue < filterValue || isHighValue && recordValue > filterValue) {
                    r.setAxisFilterStatus(Record.Status.OPAQUE);
                    r.drawByStatus(this);
                }
            }
        }
    }

    /**
     * Updates filter statuses and sets records visible if allowed by other criteria as well.
     * This method can only make lines visible, not hide them.
     *
     * @param axisId      The index of the axis the filter is on
     * @param filterValue The updated filter value
     * @param isHighValue Whether the filter value is a high or low value
     */
    private void filterInLines(int axisId, double filterValue, boolean isHighValue) {
        for (Series s : series) {
            for (Record r : s.getRecords()) {
                // we can skip lines which are already visible (according to filter criteria)
                if (r.getAxisFilterStatus() == Record.Status.VISIBLE)
                    continue;

                // TODO investigate why this is necessary
                if (r.getValues().get(axisId) == null)
                    continue;

                double recordValue = (double) r.getValues().get(axisId);
                if ((isHighValue && (recordValue <= filterValue)) || (!isHighValue && (recordValue >= filterValue))) {
                    boolean visible = true;

                    //check all axes
                    for (Map.Entry<Integer, ParallelCoordinatesAxis> mapEntry : axes.entrySet()) {
                        //int id = mapEntry.getKey();
                        ParallelCoordinatesAxis pcAxis = mapEntry.getValue();
                        int id = pcAxis.getId();

                        // TODO investigate why this is necessary
                        if (r.getValues().get(id) == null)
                            continue;

                        double recordValueAxis = (double) r.getValues().get(id);
                        double low = pcAxis.getFilterLow();
                        double high = pcAxis.getFilterHigh();
                        //check for current axis
                        if (recordValueAxis > high || recordValueAxis < low) {
                            visible = false;
                            break;
                        }
                    }

                    if (visible) {
                        // we can now set it to visible according to filter criteria
                        r.setAxisFilterStatus(Record.Status.VISIBLE);
                        r.drawByStatus(this);
                    }
                }
            }
        }
    }

    /**
     * Returns the axis specified by the given axis id (null if it cannot be found).
     *
     * @param axisId the id of the axis
     * @return the axis with the given id or null if no axis found
     */
    private ParallelCoordinatesAxis getAxisById(int axisId) {
        return axes.get(axisId);
    }

    /**
     * Returns the axis specified by the given axis id (null if it cannot be found).
     *
     * @param axisIndex the index of the axis which should be solved
     * @return the axis with the given index or null if no axis found
     */
    private ParallelCoordinatesAxis getAxisByIndex(int axisIndex) {
        for (ParallelCoordinatesAxis axis : axes.values()) {
            if (axis.getAxisIndex() == axisIndex) {
                return axis;
            }
        }
        return null;
    }

    /**
     * Manually resizes axes and filters to fit current dimensions. This is necessary as height and
     * width of axes and sliders cannot be bound.
     */
    protected void resizeAxes() {
        for (ParallelCoordinatesAxis axis : axes.values()) {
            double buttonHeight = axis.getBtnInvert().heightProperty().doubleValue() + BUTTON_MARGIN;
            axis.getAxis().resize(1.0, innerHeightProperty.doubleValue() - buttonHeight);

            if (axis.getFilterSlider() != null)
                axis.getFilterSlider().resize(1.0, innerHeightProperty.doubleValue() - buttonHeight);
        }
    }

    /**
     * Returns a property holding the height of the chartContent which is updated with each layoutChartChildren call.
     * Represents inner values (without padding, titleLabel, etc.)
     *
     * @returns property representing the height of the chartContent
     */
    public DoubleProperty innerHeightProperty() {
        return innerHeightProperty;
    }

    /**
     * Returns a property holding the width of the chartContent which is updated with each layoutChartChildren call.
     * Represents inner values (without padding, titleLabel, etc.)
     *
     * @returns property representing the width of the chartContent
     */
    public DoubleProperty innerWidthProperty() {
        return innerWidthProperty;
    }

    /**
     * Returns a DoubleBinding representing the horizontal space between the axes
     * Uses a binding on innerWidthProperty and the data length
     *
     * @returns DoubleBinding which equals the horizontal space between axes
     */
    private DoubleBinding getAxisSeparationBinding() {
        return innerWidthProperty().divide(getAttributeCount() + 1);
    }

    /**
     * TODO: is this the best way to get attribute/axis count
     */
    private int getAttributeCount() {
        int valueCount = 0;
        if (series.size() > 0) {
            if (series.get(0).getRecords().size() > 0) {
                valueCount = series.get(0).getRecord(0).getValues().size();
            }
        }

        //TODO remove -1 after categorial data has been handled
        return valueCount - 1;
    }

    @Override
    protected void layoutChartChildren(double top, double left, double width, double height) {
        super.layoutChartChildren(top, left, width, height);
        resizeAxes();
    }

    /**
     * Binds a given series to the chart content, and adds it to its chartChildren
     *
     * @param s the series which the chart adds and binds to its content
     */
    @Override
    protected void bindSeries(Series s) {
        // easier and more performant to simply sort the axes right away instead of crawling the map
        List<ParallelCoordinatesAxis> axesSorted = getAxesInOrder();

        DoubleProperty yStartAxes = axes.get(0).getAxis().translateYProperty(); // starting point of axes
        DoubleBinding axisSeparation = getAxisSeparationBinding();
        DoubleBinding heightProp = innerHeightProperty().subtract(yStartAxes);

        Double value;
        Object dataPoint;
        //int numRecords = s.getRecords().size();
        int numColumns = getAttributeCount();
        //System.out.println("cols:" + numColumns + "records" + numRecords);
        for (Record record : s.getRecords()) {
            Path path = new Path();
            MoveTo moveTo = new MoveTo();
            dataPoint = record.getAttByIndex(axesSorted.get(0).getId());
            value = (Double) dataPoint;
            // for first data point, use moveto not lineto
            // this has to be refactored when moving axes
            moveTo.xProperty().bind(axisSeparation);
            moveTo.yProperty().bind(getValueOnAxis(yStartAxes, heightProp, value, axesSorted.get(0)));
            path.getElements().add(moveTo);

            for (int curr = 1; curr < numColumns; curr++) {
                ParallelCoordinatesAxis currAxis = axesSorted.get(curr);
                dataPoint = record.getAttByIndex(currAxis.getId());

                if (dataPoint instanceof String) {
                    break;
                }

                value = (Double) dataPoint;
                if (value != null) {
                    LineTo lineTo = new LineTo();
                    lineTo.xProperty().bind(axisSeparation.add(axisSeparation.multiply(curr)));
                    lineTo.yProperty().bind(getValueOnAxis(yStartAxes, heightProp, value, currAxis));
                    path.getElements().add(lineTo);
                }
            }

            //handled by record.drawByStatus()
//            path.setStroke(s.getColor());
//            path.setOpacity(s.getOpacity());
//            path.setStrokeWidth(pathStrokeWidth);

            if (useHighlighting)
                setupHighlightingEvents(path);

            record.setPath(path);
            record.drawByStatus(this);

            path.getProperties().put("record", record);
            path.setCache(true);
            path.setCacheHint(CacheHint.SPEED);

            getChartChildren().add(path);
        }
    }

    /**
     * Helper method for getting axes in correct display order
     */
    private List<ParallelCoordinatesAxis> getAxesInOrder() {
        List<ParallelCoordinatesAxis> sortedAxes = new ArrayList<>(axes.values());
        Collections.sort(sortedAxes, Comparator.comparingInt(ParallelCoordinatesAxis::getAxisIndex));
        return sortedAxes;
    }

    /**
     * Converts the given data value to the correct coordinate which matches the given axis.
     * If the axis is inverted, this method also correctly considers this
     *
     * @param yStartAxes the y-Coordinate where the axis begins
     * @param heightAxis the available space for the given axis
     * @param value      the value of the data to be displayed
     * @param axis       the axis to look at
     */
    private DoubleBinding getValueOnAxis(DoubleProperty yStartAxes, DoubleBinding heightAxis, double value, ParallelCoordinatesAxis axis) {
        DoubleBinding binding;

        if (!axis.isInverted()) {
            binding = heightAxis.subtract(heightAxis.multiply(value)).add(yStartAxes);
        } else {
            binding = heightAxis.multiply(value).add(yStartAxes);
        }

        return binding;
    }

    /**
     * Sets up event handling for the given path.
     *
     * @param path The path for which events should be handled
     */
    private void setupHighlightingEvents(Path path) {

        // permanent highlighting for clicks
        path.setOnMouseClicked((MouseEvent event) -> {
            Path src = (Path) event.getSource();
            Record record = (Record) src.getProperties().get("record");

            // we don't need to handle events for invisible records
            if (record.isVisible()) {

                // record is already highlighted
                if (record.getHighlightingStatus() == Status.VISIBLE) {
                    record.setHighlightingStatus(Status.NONE);
                } else {
                    record.setHighlightingStatus(Status.VISIBLE);
                    record.getPath().toFront();
                }

                record.drawByStatus(this);
            }
        });


        // temporal highlighting for hover
        path.setOnMouseEntered((MouseEvent event) -> {
            highlightExecutor.submit(() -> {

                Path src = (Path) event.getSource();
                Record record = (Record) src.getProperties().get("record");

                record.drawByStatus(this, true);
            });
        });

        path.setOnMouseExited((MouseEvent event) -> {
            highlightExecutor.submit(() -> {
                Path src = (Path) event.getSource();
                Record record = (Record) src.getProperties().get("record");

                record.drawByStatus(this);
            });
        });
    }

    /**
     * Enables brushing for this chart by setting up corresponding Mouse events.
     */
    public void enableBrushing() {
        initializeBrushingRectangle();

        setOnMousePressed((MouseEvent event) -> {
            //reset the rectangle
            brushingRectangle.setWidth(0.0);
            brushingRectangle.setHeight(0.0);
            brushingRectangle.setVisible(true);

            brushingRectangleX = event.getX();
            brushingRectangleY = event.getY();

            brushingRectangle.setX(brushingRectangleX);
            brushingRectangle.setY(brushingRectangleY);

        });

        setOnMouseDragged((MouseEvent event) -> {
            brushingRectangle.setWidth(event.getX() - brushingRectangleX);
            brushingRectangle.setHeight(event.getY() - brushingRectangleY);

            if (brushingRectangle.getWidth() < 0) {
                brushingRectangle.setWidth(-brushingRectangle.getWidth());
                brushingRectangle.setX(brushingRectangleX - brushingRectangle.getWidth());
            }

            if (brushingRectangle.getHeight() < 0) {
                brushingRectangle.setHeight(-brushingRectangle.getHeight());
                brushingRectangle.setY(brushingRectangleY - brushingRectangle.getHeight());
            }

            //doesn't work as it doesn't catch every intersection
//			if(event.getPickResult().getIntersectedNode() instanceof Path) {
//				Path path = (Path)event.getPickResult().getIntersectedNode();
//				path.setStroke(Color.RED);
//			}
        });

        setOnMouseReleased((MouseEvent event) -> {
            brushingRectangle.setVisible(false);

            //dismiss small rectangles
            if (brushingRectangle.getWidth() < 7.5 && brushingRectangle.getHeight() < 7.5)
                return;

            //handle brushing
            brushingExecutor.submit(() -> handleBrushing());
        });
    }

    /**
     * Creates and styles the rectangle which is used for brushing.
     */
    private void initializeBrushingRectangle() {
        brushingRectangle = new Rectangle(0, 0, 0, 0);
        brushingRectangle.setVisible(false);
        brushingRectangle.setFill(Color.BLUE);
        brushingRectangle.setOpacity(0.1);
        getChildren().add(brushingRectangle);
    }

    /**
     * Handles brushing given that the brushingRectangle is present and set correctly.
     */
    private void handleBrushing() {
        for (Series s : series) {
            for (Record r : s.getRecords()) {
                //skip lines which are not visible
                if (!r.isVisible())
                    continue;

                Shape intersection = Shape.intersect(r.getPath(), brushingRectangle);
                if (intersection.getBoundsInParent().intersects(getBoundsInLocal())) {
                    //collision detected
                    r.setBrushingStatus(Status.VISIBLE);
                } else {
                    r.setBrushingStatus(Status.OPAQUE);
                    r.drawByStatus(this);
                }
            }
        }
    }

    /**
     * Resets all changes made by brushing.
     */
    public void resetBrushing() {
        if (series == null)
            return;

        for (Series s : series) {
            for (Record r : s.getRecords()) {
                r.setBrushingStatus(Status.NONE);
                r.drawByStatus(this);
//    			if(r.isVisible()) {
//    				r.getPath().setStroke(s.getColor());
//    				r.getPath().setOpacity(s.getOpacity());
//    				r.getPath().setStrokeWidth(pathStrokeWidth);
//    			}
            }
        }
    }


    /**
     * @return the useAxisFilters
     */
    public boolean isUseAxisFilters() {
        return useAxisFilters;
    }

    /**
     * @param useAxisFilters the useAxisFilters to set
     */
    public void setUseAxisFilters(boolean useAxisFilters) {
        this.useAxisFilters = useAxisFilters;
    }

    /**
     * @return the filteredOutOpacity
     */
    public double getFilteredOutOpacity() {
        return filteredOutOpacity;
    }

    /**
     * @param filteredOutOpacity the filteredOutOpacity to set
     */
    public void setFilteredOutOpacity(double filteredOutOpacity) {
        this.filteredOutOpacity = filteredOutOpacity;
    }

    /**
     * @return the pathStrokeWidth
     */
    public double getPathStrokeWidth() {
        return pathStrokeWidth;
    }

    /**
     * @param pathStrokeWidth the pathStrokeWidth to set
     */
    public void setPathStrokeWidth(double pathStrokeWidth) {
        this.pathStrokeWidth = pathStrokeWidth;
    }

    /**
     * @return the useHighlighting
     */
    public boolean isUseHighlighting() {
        return useHighlighting;
    }

    /**
     * @param useHighlighting the useHighlighting to set
     */
    public void setUseHighlighting(boolean useHighlighting) {
        this.useHighlighting = useHighlighting;
    }

    /**
     * @return the highlightOpacity
     */
    public double getHighlightOpacity() {
        return highlightOpacity;
    }

    /**
     * @param highlightOpacity the highlightOpacity to set
     */
    public void setHighlightOpacity(double highlightOpacity) {
        this.highlightOpacity = highlightOpacity;
    }

    /**
     * @return the highlightColor
     */
    public Color getHighlightColor() {
        return highlightColor;
    }

    /**
     * @param highlightColor the highlightColor to set
     */
    public void setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
    }

    /**
     * @return the highlightStrokeWidth
     */
    public double getHighlightStrokeWidth() {
        return highlightStrokeWidth;
    }

    /**
     * @param highlightStrokeWidth the highlightStrokeWidth to set
     */
    public void setHighlightStrokeWidth(double highlightStrokeWidth) {
        this.highlightStrokeWidth = highlightStrokeWidth;
    }


    public void drawLegend() {

        DoubleBinding legendSeparation = innerWidthProperty().divide(series.size() + 1);
        DoubleBinding heightProp = innerHeightProperty().multiply(1);
        DoubleBinding widthProp = innerWidthProperty().multiply(1);

        DoubleBinding heightPropLegendBorder = innerHeightProperty().multiply(1 - legend_height_relative);
        DoubleBinding heightPropLegend = innerHeightProperty().multiply(1 - legend_height_relative / 2);


        Path path = new Path();

        MoveTo moveTo = new MoveTo();
        moveTo.setX(0);
        moveTo.yProperty().bind(heightPropLegendBorder);
        path.getElements().add(moveTo);

        LineTo lineTo = new LineTo();
        lineTo.xProperty().bind(widthProp);
        lineTo.yProperty().bind(heightPropLegendBorder);
        path.getElements().add(lineTo);

        lineTo = new LineTo();
        lineTo.xProperty().bind(widthProp);
        lineTo.yProperty().bind(heightProp);
        path.getElements().add(lineTo);

        lineTo = new LineTo();
        lineTo.setX(0);
        lineTo.yProperty().bind(heightProp);
        path.getElements().add(lineTo);

        lineTo = new LineTo();
        lineTo.setX(0);
        lineTo.yProperty().bind(heightPropLegendBorder);
        path.getElements().add(lineTo);

        getChartChildren().add(path);

        HBox box = null;
        Label labelNode = null;

        for (int curr = 0; curr < series.size(); curr++) {
            path = new Path();

            labelNode = new Label(series.get(curr).getName());
            labelNode.setMinWidth(100);
            labelNode.setAlignment(Pos.CENTER_LEFT);

            box = new HBox(labelNode);
            box.setAlignment(Pos.CENTER_LEFT);
            box.translateXProperty().bind(legendSeparation.multiply(curr + 1));
            box.translateYProperty().bind(heightPropLegend);

            getChartChildren().add(box);

            moveTo = new MoveTo();
            moveTo.xProperty().bind(legendSeparation.multiply(curr + 1).subtract(5));
            moveTo.yProperty().bind(heightPropLegend);
            path.getElements().add(moveTo);

            lineTo = new LineTo();
            lineTo.xProperty().bind(legendSeparation.multiply(curr + 1).subtract(20));
            lineTo.yProperty().bind(heightPropLegend);
            path.getElements().add(lineTo);

            path.setStrokeWidth(2);
            path.setStroke(series.get(curr).getColor());

            getChartChildren().add(path);
        }

    }
}