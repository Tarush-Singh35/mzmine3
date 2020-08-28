/*
 * Copyright 2006-2020 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.mzmine.modules.dataprocessing.masscalibration.charts;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.function.Function2D;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Arrays;

public class ChartUtils {
  public static double calculateRSquared(XYDataItem[] items, Function2D trend) {
    double yMean = Arrays.stream(items).mapToDouble(item -> item.getYValue()).average().orElse(0);
    double ssTot = Arrays.stream(items).mapToDouble(item -> Math.pow(item.getYValue() - yMean, 2)).sum();
    double ssRes = Arrays.stream(items).
            mapToDouble(item -> Math.pow(item.getYValue() - trend.getValue(item.getXValue()), 2)).sum();
    double rSquared = 1 - ssRes / ssTot;
    return rSquared;
  }

  public static void cleanPlot(XYPlot plot) {
    for (int i = 0; i < plot.getDatasetCount(); i++) {
      plot.setDataset(i, null);
    }
    plot.clearRangeMarkers();
    plot.clearAnnotations();
  }

  public static void cleanPlotLabels(XYPlot plot) {
    plot.clearRangeMarkers();
    plot.clearAnnotations();
  }

  public static ValueMarker createValueMarker(String label, double value) {
    ValueMarker valueMarker = new ValueMarker(value);
    valueMarker.setLabel(String.format("%s: %.4f", label, value));
    valueMarker.setPaint(Color.blue);
    valueMarker.setLabelTextAnchor(TextAnchor.BASELINE_LEFT);
    valueMarker.setLabelPaint(Color.blue);
    valueMarker.setLabelFont(new Font(null, 0, 11));
    return valueMarker;
  }

  public static XYToolTipGenerator createTooltipGenerator() {
    return new XYToolTipGenerator() {
      @Override
      public String generateToolTip(XYDataset dataset, int series, int item) {
        double yValue = dataset.getYValue(series, item);
        double xValue = dataset.getXValue(series, item);
        return String.format("x: %s, y: %s", xValue, yValue);
      }
    };
  }

  public static XYLineAndShapeRenderer createErrorsRenderer() {
    XYLineAndShapeRenderer errorsRenderer = new XYLineAndShapeRenderer(false, true);
    Shape circle = new Ellipse2D.Double(-2, -2, 4, 4);
    Color paintColor = new Color(230, 160, 30, 100);
    errorsRenderer.setSeriesShape(0, circle);
    errorsRenderer.setSeriesPaint(0, paintColor);
    errorsRenderer.setSeriesFillPaint(0, paintColor);
    errorsRenderer.setSeriesOutlinePaint(0, paintColor);
    errorsRenderer.setUseFillPaint(true);
    errorsRenderer.setUseOutlinePaint(true);

    errorsRenderer.setDefaultToolTipGenerator(createTooltipGenerator());

    return errorsRenderer;
  }

  public static XYLineAndShapeRenderer createTrendRenderer() {
    XYLineAndShapeRenderer trendRenderer = new XYLineAndShapeRenderer();
    Shape circle = new Ellipse2D.Double(-2, -2, 4, 4);
    Color paintColor = new Color(0, 0, 0, 150);
    Stroke stroke = new BasicStroke(2);
    trendRenderer.setSeriesShape(0, circle);
    trendRenderer.setSeriesPaint(0, paintColor);
    trendRenderer.setSeriesStroke(0, stroke);
    trendRenderer.setDefaultStroke(stroke);
    trendRenderer.setAutoPopulateSeriesStroke(false);
    trendRenderer.setAutoPopulateSeriesPaint(false);
    return trendRenderer;
  }
}