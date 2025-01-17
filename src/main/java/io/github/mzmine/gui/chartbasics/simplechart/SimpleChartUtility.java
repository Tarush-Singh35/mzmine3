/*
 * Copyright (c) 2004-2022 The MZmine Development Team
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.mzmine.gui.chartbasics.simplechart;

import com.google.common.primitives.Ints;
import io.github.mzmine.gui.chartbasics.chartthemes.EStandardChartTheme;
import io.github.mzmine.gui.chartbasics.simplechart.datasets.ColoredXYDataset;
import io.github.mzmine.main.MZmineConfiguration;
import io.github.mzmine.main.MZmineCore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.data.xy.XYDataset;

/**
 * Contains utility methods for {@link SimpleXYChart}.
 */
public class SimpleChartUtility {

  private static final Logger logger = Logger.getLogger(SimpleChartUtility.class.getName());

  private SimpleChartUtility() {
  }

  /**
   * Checks if given data point is local maximum.
   *
   * @param item the index of the item to check.
   * @return true/false if the item is a local maximum.
   */
  public static boolean isLocalMaximum(XYDataset dataset, final int series, final int item) {
    final boolean isLocalMaximum;
    if (item <= 0 || item >= dataset.getItemCount(series) - 1) {
      isLocalMaximum = false;
    } else {
      final double intensity = dataset.getYValue(series, item);
      isLocalMaximum =
          dataset.getYValue(series, item - 1) <= intensity && intensity >= dataset.getYValue(series,
              item + 1);
    }
    return isLocalMaximum;
  }

  /**
   * Gets indexes of local maxima within given range.
   *
   * @param xMin minimum of range on x-axis.
   * @param xMax maximum of range on x-axis.
   * @param yMin minimum of range on y-axis.
   * @param yMax maximum of range on y-axis.
   * @return the local maxima in the given range.
   */
  public static int[] findLocalMaxima(XYDataset dataset, int series, final double xMin,
      final double xMax, final double yMin, final double yMax) {

    if (!(dataset instanceof ColoredXYDataset) || dataset.getItemCount(series) == 0) {
      return new int[0];
    }

    int startIndex = 0;
    for (int i = 0; i < dataset.getItemCount(series); i++) {
      if (dataset.getXValue(series, i) > xMin) {
        startIndex = i;
        break;
      }
    }

    if (startIndex < 0) {
      startIndex = -startIndex - 1;
    }

    final int length = dataset.getItemCount(series);
    // todo: is size = lendth correct?
    final Collection<Integer> indices = new ArrayList<>(length);
    for (int index = startIndex; index < length && dataset.getXValue(series, index) <= xMax;
        index++) {

      // Check Y range..
      final double intensity = dataset.getYValue(series, index);
      if (yMin <= intensity && intensity <= yMax && ((ColoredXYDataset) dataset).isLocalMaximum(
          index)) {
        indices.add(index);
      }
    }

    return Ints.toArray(indices);
  }

  /**
   * Applies the chart theme from the {@link MZmineConfiguration} to a renderer. This method can be
   * safely used in renderer constructors to be up-to-date, all exceptions are caught.
   */
  public static void tryApplyDefaultChartThemeToRenderer(AbstractRenderer r) {
    if (r == null) {
      return;
    }

    try {
      final MZmineConfiguration configuration = MZmineCore.getConfiguration();
      if (configuration == null) {
        logger.fine(() -> "Cannot apply item label color, configuration == null.");
        return;
      }

      final EStandardChartTheme chartTheme = configuration.getDefaultChartTheme();
      if (chartTheme == null) {
        logger.fine(() -> "Cannot apply item label color, chart theme == null.");
        return;
      }

      chartTheme.applyToAbstractRenderer(r);
    } catch (Exception e) {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }
}
