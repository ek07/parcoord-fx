package tugraz.ivis.parcoord.chart;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.ArrayList;

import org.controlsfx.control.RangeSlider;

// TODO: implement basic graph here
// TODO: this is basically only a bit of "playing around" for now
public class ParallelCoordinatesChart extends HighDimensionalChart {
    private ArrayList<Object>[] data;// => list of columns
    private ArrayList<String> axisLabels;
    private ArrayList<ParallelCoordinatesAxis> axes = new ArrayList<ParallelCoordinatesAxis>();
    private double top;
    private double left;
    private boolean showLabels = true;
    private boolean useAxisFilters = true;

    /**
     * Property holding the height of the chartContent which is updated with each layoutChartChildren call.
     * Represents inner values (without padding, titleLabel, etc.)
     */
    private DoubleProperty innerHeightProperty = new SimpleDoubleProperty();
    /**
     * Property holding the width of the chartContent which is updated with each layoutChartChildren call.
     * Represents inner values (without padding, etc.)
     */
    private DoubleProperty innerWidthProperty = new SimpleDoubleProperty();

    /**
     * Default constructor needed for FXML
     */
    public ParallelCoordinatesChart() {
    }

    public void updateData(ArrayList<Object>[] data, ArrayList<String> axisLabels) {
        setData(data, axisLabels);
        bindData();
    }

    public void setData(ArrayList<Object>[] data, ArrayList<String> axisLabels) {
        this.data = data;
        this.axisLabels = axisLabels;
        System.out.println("imported records: " + data[0].size() + " with columns:" + data.length);
    }

    /**
     * Binds the set data to the graph
     */
    public void bindData() {
        //drawBorder();

        bindAxes();
        bindRecords();
        reorder();
    }

    public void forceRedraw() {
        getChartChildren().clear();
        bindData();
    }

    // TODO: not needed for now
    private void drawBorder() {
        Path path = new Path();

        MoveTo moveTo = new MoveTo();
        moveTo.setX(0);
        moveTo.setY(0);
        path.getElements().add(moveTo);

        LineTo lineTo = new LineTo();
        lineTo.setX(innerWidthProperty.doubleValue());
        lineTo.setY(0);
        path.getElements().add(lineTo);

        lineTo = new LineTo();
        lineTo.setX(innerWidthProperty.doubleValue());
        lineTo.setY(innerHeightProperty.doubleValue());
        path.getElements().add(lineTo);

        lineTo = new LineTo();
        lineTo.setX(0);
        lineTo.setY(innerHeightProperty.doubleValue());
        path.getElements().add(lineTo);

        lineTo = new LineTo();
        lineTo.setX(0);
        lineTo.setY(0);
        path.getElements().add(lineTo);

        getChartChildren().add(path);
    }
    
    /**
     * Reorders elements in the z dimensions to push certain elements to the front.
     */
    private void reorder() {
    	for(ParallelCoordinatesAxis axis : axes) {
    		if(axis.getFilterSlider() != null)
    			axis.getFilterSlider().toFront();
    	}
    }

    /**
     * Creates and binds axes, axes labels and filters.
     */
    private void bindAxes() {
        int numAxes = data.length - 1; // TODO: remove -1 later, for now because of Categories in datamodel

        // configurable
        double spaceBetweenTicks = 50;
        double labelMinWidth = 500;
        double labelYOffset = 0;

        for (int iAxis = 0; iAxis < numAxes; iAxis++) {

            String label = null;
            if (axisLabels.size() - 1 >= iAxis) {
                label = axisLabels.get(iAxis);
            }

            DoubleBinding trueAxisSeparation = getAxisSeparationBinding().multiply(iAxis + 1);

            // TODO get real bounds
            double upperBound = 10.0;
            double lowerBound = 0.0;
            double delta = Math.abs(upperBound - lowerBound);
            
            // axis
            NumberAxis numberAxis = new NumberAxis(null, lowerBound, upperBound, 1.0);
            numberAxis.setSide(Side.LEFT);
            numberAxis.setMinorTickVisible(false);
            numberAxis.setAnimated(false);
            numberAxis.translateXProperty().bind(trueAxisSeparation);
            numberAxis.tickUnitProperty().bind(innerHeightProperty.divide(innerHeightProperty).divide(innerHeightProperty).multiply(spaceBetweenTicks).multiply(delta));

        	getChartChildren().add(numberAxis);
        	
            // label
        	HBox box = null;
        	if(showLabels) {
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
        	if(useAxisFilters) {
	            vSlider = new RangeSlider(lowerBound, upperBound, lowerBound, upperBound);
	            vSlider.setOrientation(Orientation.VERTICAL);
	            vSlider.setShowTickLabels(false);
	            vSlider.setShowTickMarks(false);
	            vSlider.translateXProperty().bind(trueAxisSeparation);
	            vSlider.getProperties().put("axis", iAxis);
	            
	            addFilterListeners(vSlider);
	            
	        	getChartChildren().add(vSlider);
	        	
	        	// have to style after adding it (CSS wouldn't be accessible otherwise)
	            vSlider.applyCss();
	            vSlider.lookup(".range-slider .track").setStyle("-fx-opacity: 0;");
	            vSlider.lookup(".range-slider .range-bar").setStyle("-fx-opacity: 0.15;");
        	}

            ParallelCoordinatesAxis pcAxis = new ParallelCoordinatesAxis(numberAxis, iAxis, label, box, vSlider);
        	axes.add(pcAxis);

        }
        resizeAxes();
    }
    
    /**
     * Adds listeners to the given slider to be notified when high and low values change.
     * @param slider the slider to add listeners to
     */
	private void addFilterListeners(RangeSlider slider) {
		slider.highValueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
				int axisId = (int)slider.getProperties().get("axis");
				handleFilterChange(axisId, oldVal, newVal, true);
			}
		});
		
		slider.lowValueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
				int axisId = (int)slider.getProperties().get("axis");
				handleFilterChange(axisId, oldVal, newVal, false);
			}
		});
	}
	
	/**
	 * Handle changes to filter values. All filters have to be checked again for newly added lines.
	 * 
	 * @param axisId Index of the affected axis
	 * @param oldValue Old value of the filter
	 * @param newValue New value of the filter
	 * @param isHighValue Indicates whether the changed value was a high value or low value
	 */
	private void handleFilterChange(int axisId, Number oldValue, Number newValue, boolean isHighValue) {
		double oldV = oldValue.doubleValue();
		double newV = newValue.doubleValue();
		
		ParallelCoordinatesAxis axis = getAxisById(axisId);
		
		if(isHighValue) {
			if (newV > oldV) {
				// new lines could get active - we have to check all filters for the new lines
				
				// TODO iterate through all lines, if a new line would be added, check the line for all other filters as well
			}
			else {
				// this can only diminish the number of visible lines
				
				// TODO iterate through all lines and simply set them invisible if required
			}
		}
		else {
			if(newV < oldV) {
				// new lines could get active - we have to check all filters for the new lines
				
				// TODO iterate through all lines, if a new line would be added, check the line for all other filters as well
			}
			else {
				// this can only diminish the number of visible lines
				
				// TODO iterate through all lines and simply set them invisible if required

			}
		}
	}
	
	/**
	 * Returns the axis specified by the given axis id (null if it cannot be found).
	 * 
	 * @param axisId the index of the axis
	 * @return the axis or null
	 */
	private ParallelCoordinatesAxis getAxisById(int axisId) {
		for(ParallelCoordinatesAxis axis : axes) {
			if(axis.getAxisIndex() == axisId)
				return axis;
		}
		return null;
	}
    
    /**
     * Manually resizes axes and filters to fit current dimensions. This is necessary as height and
     * width of axes and sliders cannot be bound.
     */
    private void resizeAxes() {
    	for(ParallelCoordinatesAxis axis : axes) {
    		axis.getAxis().resize(1.0, innerHeightProperty.doubleValue());
    		
    		if(axis.getFilterSlider() != null)
    			axis.getFilterSlider().resize(1.0, innerHeightProperty.doubleValue());
    	}
    }


    /**
     * Binds the dataset to the chart.
     * <p>
     * TODO: why are there vertical lines at the beginning of the graph??
     */
    private void bindRecords() {
        DoubleBinding axisSeparation = getAxisSeparationBinding();
        DoubleProperty heightProp = innerHeightProperty();

        int numRecords = data[0].size();
        int numColumns = data.length - 1; // TODO: remove -1 later, for now because of Categories in datamodel
        //System.out.println("cols:" + numColumns + "records" + numRecords);
        for (int record = 0; record < numRecords; record++) {
            Path path = new Path();
            MoveTo moveTo = new MoveTo();
            moveTo.xProperty().bind(axisSeparation);
            moveTo.yProperty().bind(heightProp.divide(2.0).subtract(top));

            path.getElements().add(moveTo);
            for (int column = 0; column < numColumns; column++) {
                Object dataPoint = data[column].get(record);

                if (dataPoint instanceof String) {
                    break;
                }

                Double value = (Double) dataPoint;
                //System.out.println("data at " + record + ", col:" + column + ";" + "dataPoint" + value);
                if (value != null) {
                    LineTo lineTo = new LineTo();
                    lineTo.xProperty().bind(axisSeparation.add(axisSeparation.multiply(column)));
                    lineTo.yProperty().bind(heightProp.subtract(heightProp.multiply(value)));
                    path.getElements().add(lineTo);
                }
            }
            path.setStroke(new Color(0, 0, 0, 0.2));
            getChartChildren().add(path);
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
        return innerWidthProperty().divide(((data.length + 1) - 1)); // TODO: remove -1 later, for now because of Categories in datamodel
    }

    /**
     * Overwritten function of the Chart superclass.
     * Automatically called by JavaFX after initialization and resizing of the chart.
     * Used to update the top and left coordinates as well as the innerWidthProperty and innerHeightProperty with the
     * updated values (after resizing the window).
     */
    @Override
    protected void layoutChartChildren(double top, double left, double width, double height) {
        this.top = top;
        this.left = left;
        innerWidthProperty.set(width);
        innerHeightProperty.set(height);

        if (data == null || axisLabels == null || data.length != axisLabels.size()) {
            System.out.println("Error with data, number of axisLabels != number of axis");
            return;
        }
        resizeAxes();
    }

}