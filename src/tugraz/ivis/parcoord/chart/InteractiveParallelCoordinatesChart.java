/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tugraz.ivis.parcoord.chart;

import tugraz.ivis.parcoord.util.importer.DataModel;

/**
 * This class support additional functionalities than ParallelCoordinatesChart
 * e.g. new interactions, listeners etc.
 *
 * @author mchegini
 */
public class InteractiveParallelCoordinatesChart extends ParallelCoordinatesChart {

    public InteractiveParallelCoordinatesChart(DataModel dataModel) {
        super(dataModel);
    }

    public InteractiveParallelCoordinatesChart() {

    }
}
