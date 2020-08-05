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


package io.github.mzmine.modules.dataprocessing.id_cliquems.cliquemsimplementation;


/**
 * Data taken from PeakListRow necessary for CliqueMS grouping and annotation
 *
 * Corresponding to the peakList class used in the R code
 * https://github.com/osenan/cliqueMS/blob/master/R/allClasses.R
 *
 */

public class PeakData {



  private final double mz ;
  private final double mzmin ;
  private final double mzmax ;
  private final double rt;
  private final double rtmin ;
  private final double rtmax ;
  private final double intensity;
  private final int nodeID;
  // To get the peakListRow corresponding to this node.
  private final int peakListRowID;

  private int cliqueID;
  private String isotopeAnnotation;
  private String adductAnnotation;

  public double getMz() {
    return mz;
  }

  public double getMzmin() {
    return mzmin;
  }

  public double getMzmax() {
    return mzmax;
  }

  public double getRt() {
    return rt;
  }

  public double getRtmin() {
    return rtmin;
  }

  public double getRtmax() {
    return rtmax;
  }

  public double getIntensity() {
    return intensity;
  }

  public int getNodeID(){
    return nodeID;
  }

  public int getPeakListRowID() {
    return peakListRowID;
  }


  public int getCliqueID() {
    return cliqueID;
  }

  public void setCliqueID(int cliqueID) {
    this.cliqueID = cliqueID;
  }

  public String getIsotopeAnnotation() {
    return isotopeAnnotation;
  }

  public void setIsotopeAnnotation(String isotopeAnnotation) {
    this.isotopeAnnotation = isotopeAnnotation;
  }

  public String getAdductAnnotation() {
    return adductAnnotation;
  }

  public void setAdductAnnotation(String adductAnnotation) {
    this.adductAnnotation = adductAnnotation;
  }

  PeakData(double mz, double mzmin, double mzmax, double rt, double rtmin, double rtmax, double intensity, int nodeID, int peakListRowID){
    this.mz = mz ;
    this.mzmin =  mzmin ;
    this.mzmax = mzmax ;
    this.rt = rt ;
    this.rtmin = rtmin ;
    this.rtmax = rtmax ;
    this.intensity = intensity;
    this.nodeID = nodeID;
    this.peakListRowID = peakListRowID;
  }

  PeakData(PeakData p){
    this.mz = p.mz ;
    this.mzmin =  p.mzmin ;
    this.mzmax = p.mzmax ;
    this.rt = p.rt ;
    this.rtmin = p.rtmin ;
    this.rtmax = p.rtmax ;
    this.intensity = p.intensity;
    this.nodeID = p.nodeID;
    this.peakListRowID = p.peakListRowID;
    this.cliqueID = p.cliqueID;
    this.isotopeAnnotation = p.isotopeAnnotation;
    this.adductAnnotation = p.adductAnnotation;
  }

}
