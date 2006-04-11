/*
 * Copyright 2006 Okinawa Institute of Science and Technology
 * 
 * This file is part of MZmine.
 * 
 * MZmine is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

/**
 * 
 */
package net.sf.mzmine.io.mzxml;

import net.sf.mzmine.io.Scan;
import net.sf.mzmine.io.ScanHeader;

/**
 * 
 */
class MZXMLScan extends MZXMLScanHeader implements Scan, ScanHeader {

    private double MZValues[], intensityValues[];

    /**
     * 
     */
    public MZXMLScan(MZXMLFile rawDataFile, int scanNumber) {
        super(rawDataFile, scanNumber);
    }

    /**
     * @return Returns the intensityValues.
     */
    public double[] getIntensityValues() {
        return intensityValues;
    }

    /**
     * @return Returns the mZValues.
     */
    public double[] getMZValues() {
        return MZValues;
    }

    /**
     * @see net.sf.mzmine.io.Scan#getNumberOfDataPoints()
     */
    public int getNumberOfDataPoints() {
        return MZValues.length;
    }

}
