package tugraz.ivis.parcoord.chart;

import java.util.ArrayList;
import java.util.List;

import org.controlsfx.control.RangeSlider;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

// TODO: implement basic graph here
// TODO: this is basically only a bit of "playing around" for now
public class ParallelCoordinatesChart extends HighDimensionalChart {
    private List<String> axisLabels;
    private ArrayList<ParallelCoordinatesAxis> axes = new ArrayList<ParallelCoordinatesAxis>();
    private boolean useAxisFilters = true;
    private double filteredOutOpacity = 0.0;
    
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
        if(axisLabels != null)
        	axisLabels.clear();
        if(axes != null)
        	axes.clear();
    }
    
    /**
     * Reorders elements in the z dimensions to push certain elements to the front.
     */
    protected void reorder() {
    	for(ParallelCoordinatesAxis axis : axes) {
    		if(axis.getFilterSlider() != null)
    			axis.getFilterSlider().toFront();
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

        for (int iAxis = 0; iAxis < numAxes; iAxis++) {

            String label = null;
            if (axisLabels.size() - 1 >= iAxis) {
                if (iAxis < axisLabels.size()) {
                    label = axisLabels.get(iAxis);
                } else {
                    label = "?";
                }
            }

            DoubleBinding trueAxisSeparation = getAxisSeparationBinding().multiply(iAxis + 1);

            // TODO  use real bounds
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
        	// TODO disable moving high and low value at once by moving filter bar (as it's buggy)
        	RangeSlider vSlider = null;
        	if(useAxisFilters) {
        		
        		// using bounds from 1.0 to 0.0 should work as we draw in this space anyway
	            vSlider = new RangeSlider(0.0, 1.0, 0.0, 1.0);
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
	 * @param axisId Index of the affected axis0
	 * @param oldValue Old value of the filter (not used)
	 * @param newValue New value of the filter
	 * @param isHighValue Indicates whether the changed value was a high value or low value
	 */
	private void handleFilterChange(int axisId, Number oldValue, Number newValue, boolean isHighValue) {
		
		// TODO replace this with an async solution (as this isn't working as intended)
	    long systemTime = System.currentTimeMillis();
	    if(systemTime - lastFilterHandle < FILTER_FREQUENCY) {
	    	return;
	    }
	    lastFilterHandle = systemTime;

		ParallelCoordinatesAxis axis = getAxisById(axisId);
		double newV = newValue.doubleValue();
		double oldV = 0;

		// sliders don't quite manage to reach extreme values
		if(newV > 0.99)
			newV = 1.0;
		if(newV < 0.01)
			newV = 0.0;
		
		if(isHighValue) {
			oldV = axis.getFilterHigh();
			axis.setFilterHigh(newV);
			if (newV > oldV) {
				// new lines could get active - we have to check all filters for the new lines
				// iterate through all lines, if a new line would be added, check the line for all other filters as well
				filterInLines(axisId, newV, true);
			}
			else {
				// this can only diminish the number of visible lines
				filterOutLines(axisId, newV, true);
			}
		}
		else {
			oldV = axis.getFilterLow();
			axis.setFilterLow(newV);
			if(newV < oldV) {
				// new lines could get active - we have to check all filters for the new lines
				// iterate through all lines, if a new line would be added, check the line for all other filters as well
				filterInLines(axisId, newV, false);
			}
			else {
				// this can only diminish the number of visible lines
				// iterate through all lines and simply set them invisible if required
				filterOutLines(axisId, newV, false);
			}
		}
		
	    System.out.println("Old: " + Double.toString(oldV) + "; New: " + Double.toString(newV));
	}
	
	/**
	 * Sets records to opaque if they have to be removed according to the filter criteria.
	 * This method can only hide lines, not make them visible.
	 * 
	 * @param axisId		The index of the axis the filter is on
	 * @param filterValue	The updated filter value
	 * @param isHighValue	Whether the filter value is a high or low value
	 */
	private void filterOutLines(int axisId, double filterValue, boolean isHighValue) {
		for(Series s : series) {
			for(Record r : s.getRecords()) {
				// TODO investigate why this is necessary
				if(r.getValues().get(axisId) == null)
					continue;
				
				// we could skip lines which are already hidden here, would probably just diminish performance though
				
				double recordValue = (double)r.getValues().get(axisId);
				if(!isHighValue && recordValue < filterValue || isHighValue && recordValue > filterValue) {
					r.setAxisFilterStatus(Record.Status.OPAQUE);
					r.getPath().setOpacity(filteredOutOpacity);
				}
			}
		}
	}
	
	/**
	 * Updates filter statuses and sets records visible if allowed by other criteria as well.
	 * This method can only make lines visible, not hide them.
	 * 
	 * @param axisId		The index of the axis the filter is on
	 * @param filterValue	The updated filter value
	 * @param isHighValue	Whether the filter value is a high or low value
	 */
	private void filterInLines(int axisId, double filterValue, boolean isHighValue) {
		for(Series s : series) {
			for(Record r : s.getRecords()) {
				// we can skip lines which are already visible (according to filter criteria)
				if(r.getAxisFilterStatus() == Record.Status.VISIBLE)
					continue;
				
				// TODO investigate why this is necessary
				if(r.getValues().get(axisId) == null)
					continue;
				
				double recordValue = (double)r.getValues().get(axisId);
				if((isHighValue && (recordValue <= filterValue)) || (!isHighValue && (recordValue >= filterValue))) {
					boolean visible = true;
					
					//check all axes
					for(ParallelCoordinatesAxis pcAxis : axes) {
						int id = pcAxis.getAxisIndex();
						
						// TODO investigate why this is necessary
						if(r.getValues().get(id) == null)
							continue;
						
						double recordValueAxis = (double)r.getValues().get(id);
						//check for current axis
						if(recordValueAxis > pcAxis.getFilterHigh() || recordValueAxis < pcAxis.getFilterLow()) {
							visible = false;
							break;
						}
					}
					
					if(visible) {
						// we can now set it to visible according to filter criteria
						r.setAxisFilterStatus(Record.Status.VISIBLE);
						// still have to check for other stuff like brushing
						if(r.isVisible()) {
							r.getPath().setOpacity(s.getOpacity());
							r.getPath().setStroke(s.getColor());
						}
					}
				}
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
	
    protected void resizeAxes() {
    	for(ParallelCoordinatesAxis axis : axes) {
    		axis.getAxis().resize(1.0, innerHeightProperty.doubleValue());
    		
    		if(axis.getFilterSlider() != null)
    			axis.getFilterSlider().resize(1.0, innerHeightProperty.doubleValue());
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

        return valueCount - 1;
    }

    @Override
    protected void layoutChartChildren(double top, double left, double width, double height) {
        super.layoutChartChildren(top, left, width, height);
        resizeAxes();
    }

    @Override
    protected void bindSeries(Series s) {
        DoubleBinding axisSeparation = getAxisSeparationBinding();
        DoubleProperty heightProp = innerHeightProperty();

        //int numRecords = s.getRecords().size();
        int numColumns = getAttributeCount();
        //System.out.println("cols:" + numColumns + "records" + numRecords);
        for (Record record : s.getRecords()) {
            Path path = new Path();
            MoveTo moveTo = new MoveTo();
            moveTo.xProperty().bind(axisSeparation);
            moveTo.yProperty().bind(heightProp.divide(2.0).subtract(top));

            path.getElements().add(moveTo);
            for (int column = 0; column < numColumns; column++) {
                Object dataPoint = record.getAttByIndex(column);

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
            path.setStroke(s.getColor());
            path.setOpacity(s.getOpacity());
            record.setPath(path);
            getChartChildren().add(path);
        }
    }
}