package gb.esac.integral;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Date;

import cern.colt.list.DoubleArrayList;
import hep.aida.IAxis;
import hep.aida.IHistogram1D;
import hep.aida.ref.histogram.Histogram1D;
import org.apache.log4j.Logger;

import gb.esac.tools.BasicStats;
import gb.esac.tools.MinMax;


/**
 * The class <code>ISGRITimeseriesWriter</code> is used to write data as ASCI files in QDP format.
 *
 * @author <a href="mailto: guilaume.belanger@esa.int">Guillaume Belanger</a>
 * @version 1.0 (June 2016, ESAC)
 */
public class ISGRITimeseriesWriter {

    // Class variables
    private PrintWriter printWriter;
    private static Logger logger  = Logger.getLogger(ISGRITimeseriesWriter.class);
    private static DecimalFormat stats = new DecimalFormat("0.00E00");
    private static DecimalFormat number = new DecimalFormat("0.000");
    private static DecimalFormat twoDigits = new DecimalFormat("0.00");
    private static DecimalFormat threeDigits = new DecimalFormat("0.000");
    private static DecimalFormat timeFormat = new DecimalFormat("0.000E0");

    //  Constructor
    public ISGRITimeseriesWriter(String filename) throws IOException {
	int bufferSize = 256000;
  	printWriter = new PrintWriter(new BufferedWriter(new FileWriter(filename), bufferSize));
    }


    //  Methods
    public static void writeAsPLT(DoubleArrayList t, DoubleArrayList rate, DoubleArrayList rateErr,
				    int nWithinFOV, double fracFC,
				    double sumOfOntimes, double effExposure,
				    String raStr, String decStr, String eminStr, String emaxStr, 
				    String outfilename) throws IOException {				    

	defineTimeAxisVariables(t);
	calculateStats(rate, rateErr);
	String titleText = "INTEGRAL Time Series ("+eminStr+"-"+emaxStr+" keV) at RA="+raStr+", Dec="+decStr;

	//  Print the results
	ISGRITimeseriesWriter.logger.info("Summary of results:");
	ISGRITimeseriesWriter.logger.info("  Selected observation = "+nWithinFOV);
	ISGRITimeseriesWriter.logger.info("  Fraction within FCFOV = "+twoDigits.format(fracFC));
	ISGRITimeseriesWriter.logger.info("  Total Exposure = "+ timeFormat.format(sumOfOntimes) + " s");
	ISGRITimeseriesWriter.logger.info("  Effective exposure = "+ timeFormat.format(effExposure) + " s");
	ISGRITimeseriesWriter.logger.info("  Max count rate = "+ threeDigits.format(max));
	ISGRITimeseriesWriter.logger.info("  Min count rate = "+ threeDigits.format(min));
	ISGRITimeseriesWriter.logger.info("  Mean error = "+threeDigits.format(meanErr));
	ISGRITimeseriesWriter.logger.info("  Weighted mean count rate = "+ threeDigits.format(weightedMean) +" +/- "+ threeDigits.format(errOnWMean) );
	ISGRITimeseriesWriter.logger.info("  Total significance = "+ threeDigits.format(totalSignif));

	PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outfilename)));
	String[] header = new String[]{
	    "# Data file for Ptplot 5.3",
	    "# Id: "+ outfilename+", "+(new Date())+" ptII Exp",
	    "#",
	    "# Author: Guillaume Belanger - SAp",
	    "# Summary", 
	    "# Log  : Total ONTIME = "+sumOfOntimes +" s",
	    "# Log  : Effective exposure = "+effExposure +" s",
	    "# Log  : Weighted mean count rate = "+ weightedMean +" +/- "+ errOnWMean,
	    "# Log  : Detection significance = "+ totalSignif,
	    "# Log  : This file's name = "+ outfilename,
	    "#",
	    "TitleText: "+titleText,
	    "Marks: dots",
	    "Lines: off",
	    "XLabel: Time (MJD)",
	    "YLabel: Count rate (cts/s)",
	    "DataSet:"
	};
	for ( int i=0; i < header.length; i++ ) pw.println(header[i]);

	double[] ts = t.elements();
	double[] rates = rate.elements();
	double[] rateErrs = rateErr.elements();
	for ( int j=0; j < rates.length; j++ ) {
	    if ( !Double.isNaN(rates[j]) && rates[j] != 0.0 )
		pw.println( ts[j] +", "+ rates[j] +", "+ (rates[j] - rateErrs[j]) +", "+ ( rates[j] + rateErrs[j]) );
	}
	pw.flush();
	pw.close();
    }


    public static void writeAsXML(DoubleArrayList t, DoubleArrayList rate, DoubleArrayList rateErr,
				    int nWithinFOV, double fracFC,
				    double sumOfOntimes, double effExposure,
				    String raStr, String decStr, String eminStr, String emaxStr, 
				    String outfilename) throws IOException {				    	
	defineTimeAxisVariables(t);
	calculateStats(rate, rateErr);
	String titleText = "INTEGRAL Time Series ("+eminStr+"-"+emaxStr+" keV) at RA="+raStr+", Dec="+decStr;
	//  Print the results
	ISGRITimeseriesWriter.logger.info("Summary of results:");
	ISGRITimeseriesWriter.logger.info("  Selected observation = "+nWithinFOV);
	ISGRITimeseriesWriter.logger.info("  Fraction within FCFOV = "+twoDigits.format(fracFC));
	ISGRITimeseriesWriter.logger.info("  Total Exposure = "+ timeFormat.format(sumOfOntimes) + " s");
	ISGRITimeseriesWriter.logger.info("  Effective exposure = "+ timeFormat.format(effExposure) + " s");
	ISGRITimeseriesWriter.logger.info("  Max count rate = "+ threeDigits.format(max));
	ISGRITimeseriesWriter.logger.info("  Min count rate = "+ threeDigits.format(min));
	ISGRITimeseriesWriter.logger.info("  Mean error = "+threeDigits.format(meanErr));
	ISGRITimeseriesWriter.logger.info("  Weighted mean count rate = "+ threeDigits.format(weightedMean) +" +/- "+ threeDigits.format(errOnWMean) );
	ISGRITimeseriesWriter.logger.info("  Total significance = "+ threeDigits.format(totalSignif));
	PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outfilename)));
	String[] header = new String[] {
	    "<?xml version ='1.0' standalone='no'?>",
	    "<!DOCTYPE model PUBLIC '-//UC Berkeley//DTD PlotML 1//EN'",
	    "'http://ptolemy.eecs.berkeley.edu/xml/dtd/PlotML_1.dtd'>",
	    "<plot>",
	    "<!-- Ptolemy plot, version 5.3, PlotML format. -->",
	    "<!-- Author: Guillaume Belanger - SAp -->",
	    "<!-- Summary -->",
	    "<!-- Log  : Sum of ONTIMEs = "+sumOfOntimes +" s -->",
	    "<!-- Log  : Effective exposure = "+effExposure +" s -->",
	    "<!-- Log  : Weighted mean count rate = "+ weightedMean +" +/- "+ errOnWMean +" -->",
	    "<!-- Log  : Detection significance = "+ totalSignif +" -->",
	    "<!-- Log  : This file's name = "+ outfilename +" -->",
	    "<!-- -->",
	    "<title>"+titleText+"</title>",
	    "<xLabel>Time (MJD)</xLabel>",
	    "<yLabel>Count rate (cts/s)</yLabel>",
	    "<dataset marks='dots' connected='no' stems='no'>"
	};
	for ( int i=0; i < header.length; i++ ) pw.println(header[i]);
	double[] ts = t.elements();
	double[] rates = rate.elements();
	double[] rateErrs = rateErr.elements();
	for ( int j=0; j < rates.length; j++ ) {
	    if ( !Double.isNaN(rates[j]) && rates[j] != 0.0 ) {
		pw.println( "<p x='"+ ts[j] +"' y='"+ rates[j] +"' "+ "lowErrorBar='"+ (rates[j] - rateErrs[j])+ "' " + "highErrorBar='"+ (rates[j] + rateErrs[j])+ "'/>");
	    }
	}
	pw.println("</dataset>");
	pw.println("</plot>");
	pw.flush();
	pw.close();
    }

    public static void writeAsQDPWithOffAxisAngle(DoubleArrayList t, DoubleArrayList dt, DoubleArrayList rate, DoubleArrayList rateErr,
				    DoubleArrayList offAxisAngle, int nWithinFOV, double fracFC,
				    double sumOfOntimes, double effExposure,
				    String raStr, String decStr, String eminStr, String emaxStr, 
				    String outfilename) throws IOException {
	defineTimeAxisVariables(t);
	calculateStats(rate, rateErr);
	double fracPC = 1d - fracFC;
	//  Print the results
	ISGRITimeseriesWriter.logger.info("Summary of results:");
	ISGRITimeseriesWriter.logger.info("  Selected observation = "+nWithinFOV);
	ISGRITimeseriesWriter.logger.info("  Fraction within FCFOV = "+twoDigits.format(fracFC));
	ISGRITimeseriesWriter.logger.info("  Total Exposure = "+ timeFormat.format(sumOfOntimes) + " s");
	ISGRITimeseriesWriter.logger.info("  Effective exposure = "+ timeFormat.format(effExposure) + " s");
	ISGRITimeseriesWriter.logger.info("  Max count rate = "+ threeDigits.format(max));
	ISGRITimeseriesWriter.logger.info("  Min count rate = "+ threeDigits.format(min));
	ISGRITimeseriesWriter.logger.info("  Mean error = "+threeDigits.format(meanErr));
	ISGRITimeseriesWriter.logger.info("  Weighted mean count rate = "+ threeDigits.format(weightedMean) +" +/- "+ threeDigits.format(errOnWMean) );
	ISGRITimeseriesWriter.logger.info("  Total significance = "+ threeDigits.format(totalSignif));
	//  Print the plot
	String effExpStr = null;
	String ontimeStr = null;
	DecimalFormat noDecimals = new DecimalFormat("0");
	if ( sumOfOntimes < 1e6 ) {
	    ontimeStr = noDecimals.format(Math.round(sumOfOntimes/1e3))+" ks";
	    effExpStr = noDecimals.format(Math.round(effExposure/1e3))+" ks";
	}
	else {
	    ontimeStr = twoDigits.format(sumOfOntimes/1e6)+" Ms";
	    effExpStr = twoDigits.format(effExposure/1e6)+" Ms";
	}
	String[] header = new String[] {
	    "! QDP data filename: "+outfilename,
	    "! Produced by: ISGRITimeseriesWriter.java",
	    "! Author: G. Belanger - ESA/ESAC",
	    "!",
	    "DEV /XS",
	    "READ SERR 1 2",
	    "PLOT VERT",
	    "LAB F",
	    "TIME OFF",
	    "LW 3", 
	    "CS 1.0",
	    "LAB OT INTEGRAL Time Series ("+eminStr+"-"+emaxStr+" keV)",
	    "LAB T (RA="+raStr+", Dec="+decStr+")",
	    "MA 1 ON",
	    "CO 2 ON 3",
	    "LAB X Time (MJD - "+mjdMin+")",
	    "LAB Y2 ISGRI Count Rate (s\\u-1\\d)",
	    "LAB Y3 Off-Axis (deg)",
	    "R Y2 "+(int)Math.floor(yMin)+" "+(int)Math.ceil(yMax),
	    "R Y3 -3 18",
	    "R X "+twoDigits.format(xMin)+" "+twoDigits.format(xMax),
	    "VIEW 0.1 0.2 0.9 0.8",
	    "WIN 3",
	    "LOC 0 0.1 1 0.4",
	    "LAB 100 POS "+twoDigits.format(xMin + 0.01*xRange)+" 4.15 LINE 0 0.98 \"",
	    "LAB 100 LS 4 JUST CEN",
	    "LAB 101 POS "+twoDigits.format(xMin + 0.02*xRange)+" 9.5 \""+(int)Math.round(fracPC*100)+"%\" CS 0.55 JUST CEN",
	    "LAB 102 POS "+twoDigits.format(xMin + 0.02*xRange)+" 7.1 \""+(int)Math.round(fracFC*100)+"%\" CS 0.55 JUST CEN",
	    "WIN 2",
	    "LOC 0 0.22 1 0.92",
	    "LAB 200 VPOS 0.88 0.75 \"Selected Observations = "+nWithinFOV+"\" CS 0.55 JUST RIGHT",
	    "LAB 201 VPOS 0.88 0.73 \"Total Exposure (on-time) = "+ontimeStr+"\" CS 0.55 JUST RIGHT",
	    "LAB 202 VPOS 0.88 0.71 \"Effective Exposure = "+effExpStr+"\" CS 0.55 JUST RIGHT",
	    "LAB 203 VPOS 0.12 0.75 \"Weighted Mean Rate = "+threeDigits.format(weightedMean)+" +/- "+threeDigits.format(errOnWMean)+" s\\u-1\\d\" CS 0.55 JUST LEFT",
	    "LAB 204 VPOS 0.12 0.73 \"Detection Significance = "+twoDigits.format(totalSignif)+"\" CS 0.55 JUST LEFT",
	    "!"
	};
	PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outfilename)));
	for ( int i=0; i < header.length; i++ ) pw.println(header[i]);
	double[] ts = t.elements();
	double[] dts = dt.elements();
	double[] rates = rate.elements();
	double[] rateErrs = rateErr.elements();
	double[] angles = offAxisAngle.elements();
	for ( int j=0; j < rates.length; j++ ) {
	    if ( !Double.isNaN(rates[j])  &&  rates[j] != 0.0 ) {
		pw.println( ts[j] +"\t"+ (dts[j]/2d) +"\t"+ rates[j] +"\t"+ rateErrs[j] +"\t"+ angles[j]);
	    }
	}
	String psFilename = outfilename.substring(0, outfilename.length()-4)+".ps";
	pw.println("HARD "+psFilename+"/ps");
	pw.flush();
	pw.close();
    }
    
    private static void calculateStats(DoubleArrayList ratesList, DoubleArrayList errorsList) {
	calculateStats(ratesList.elements(), errorsList.elements());
    }

    private static double max;
    private static double min;
    private static double yMin;
    private static double yMax;
    private static double meanErr;
    private static double weightedMean;
    private static double errOnWMean;
    private static double totalSignif;
    private static void calculateStats(double[] rates, double[] errors) {
	//  Determine Y min, max and range
	min = MinMax.getMin(rates);
	max = MinMax.getMax(rates);
	double yRange = max - min;
	meanErr = BasicStats.getMean(errors);
	yMin = min - 5*meanErr;
	yMax = max + 0.25*yRange;
	// Get weighted mean, error on mean, and signif
	double[] weightedMeanAndErr = BasicStats.getWMeanAndError(rates, errors);
	weightedMean = weightedMeanAndErr[0];
	errOnWMean = weightedMeanAndErr[1];
	totalSignif = weightedMean/errOnWMean;
    }

    private static int mjdMin;
    private static double xMin;
    private static double xMax;
    private static double xRange;
    private static void defineTimeAxisVariables(DoubleArrayList t) {
	//  Determine MJD zero and subtract it from the MJD
	double[] meanTimes = t.elements();
	mjdMin = (int) Math.floor(meanTimes[0]);
	for ( int i=0; i < meanTimes.length; i++ ) {
	    meanTimes[i] -= mjdMin;
	}
	xRange = meanTimes[meanTimes.length-1] - meanTimes[0];
	double min = meanTimes[0];
	double max = meanTimes[ meanTimes.length-1];
	xMin = min - 0.04*xRange;
	xMax = max + 0.03*xRange;
    }

    public static void plotCorrelation(DoubleArrayList xList, DoubleArrayList yList, DoubleArrayList yErrList, String filename) throws IOException {
	plotCorrelation(xList.elements(), yList.elements(), yErrList.elements(), filename);
    }

    public static void plotCorrelation(double[] x, double[] y, double[] yErr, String filename) throws IOException {
	int bufferSize = 256000;
	PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(filename), bufferSize));
	calculateStats(y, yErr);
	double[] corrCoeff = BasicStats.getCorrelationCoefficient(x, y);
	String plotLabel = "\\gr = "+twoDigits.format(corrCoeff[0])+" +/- "+twoDigits.format(corrCoeff[1]);
	String xLabel = "Angular Distance from Pointing Axis (deg)";
	String yLabel = "ISGRI Count Rate (s\\u-1\\d)";
	String[] header = new String[] {
	    "DEV /XS",
	    "READ SERR 2",
	    "LAB T", "LAB F",
	    "TIME OFF",
	    "LINE OFF",
	    "MA 1 ON", "MA SIZE 3",
	    "LW 3", "CS 1.3",
	    "LAB X "+xLabel,
	    "LAB Y "+yLabel,
	    "LAB 1 \""+plotLabel+"\" CS 1.3",
	    "LAB 1 VPOS 0.27 0.8 JUST LEFT",
	    "VIEW 0.2 0.1 0.8 0.9",
	    "R Y "+twoDigits.format(yMin)+" "+twoDigits.format(yMax),
	    "R X -1.9 15.9",
	    "!"
	};
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]);
	int nbins = (new Double(Math.min(x.length, y.length))).intValue();
	for ( int i=0; i < nbins; i++ ) {
	    double halfErr = yErr[i]/2;
	    printWriter.println(x[i] +"\t"+ y[i] +"\t"+ halfErr);
	}
	printWriter.close();
    }
    
    private void printToFile(String[] header, double[] binCentres, double[] binHeights) {
	for ( int i=0; i < header.length; i++ ) {
	    printWriter.println(header[i]);
	}
	for ( int i=0; i < binCentres.length; i++ ) {
	    printWriter.println((binCentres[i]) +"\t"+ (binHeights[i]) +"\t");
	}
	printWriter.close();
    }

    private void printToFile(String[] header, double[] binCentres, double[] binHeights, DecimalFormat numberFormat) {
	for ( int i=0; i < header.length; i++ ) {
	    printWriter.println(header[i]);
	}
 	for ( int i=0; i < binCentres.length; i++ ) {
	    printWriter.println(numberFormat.format(binCentres[i]) +"\t"+ numberFormat.format(binHeights[i]) +"\t");
	}
	printWriter.close();
    }

    private void printToFile(String[] header, double[] binCentres, double[] binHeights, double[] function) {
	for ( int i=0; i < header.length; i++ ) {
	    printWriter.println(header[i]);
	}
      	for ( int i=0; i < binCentres.length; i++ ) {
	    printWriter.println((binCentres[i]) +"\t"+ (binHeights[i]) +"\t"+ function[i]);
	}
	printWriter.close();
    }


    // Methods writeData

    public void writeData(String[] header, double[] x, double[] y) throws IOException {
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]);
	int nbins = (new Double(Math.min(x.length, y.length))).intValue();
	for ( int i=0; i < nbins; i++ ) {
	    printWriter.println((x[i]) +"\t"+ (y[i]) +"\t");
	}
	printWriter.close();
    }

    public void writeData(String[] header, double[] x, double[] y, int startIndex) throws IOException {
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]);
	int nbins = (new Double(Math.min(x.length, y.length))).intValue();
	for ( int i=startIndex; i < nbins; i++ ) {
	    printWriter.println((x[i]) +"\t"+ (y[i]) +"\t");
	}
	printWriter.close();
    }

    public void writeData(String[] header, int[] x, double[] y) throws IOException {
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]);
	int nbins = (new Double(Math.min(x.length, y.length))).intValue();
	for ( int i=0; i < nbins; i++ ) {
	    printWriter.println((x[i]) +"\t"+ (y[i]) +"\t");
	}
	printWriter.close();
    }

    public void writeData(String[] header, int[] x, int[] y) throws IOException {
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]);
	int nbins = (new Double(Math.min(x.length, y.length))).intValue();
	for ( int i=0; i < nbins; i++ ) {
	    printWriter.println(x[i] +"\t"+ y[i] +"\t");
	}
	printWriter.close();
    }

    public void writeData(String[] header, int[] x, double[] y, double[] y2) throws IOException {
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]);
	int lengths[] = new int[] {x.length, y.length, y2.length};
	int nbins = (new Double(MinMax.getMin(lengths))).intValue();
	for ( int i=0; i < nbins; i++ ) {
	    printWriter.println((x[i]) +"\t"+ (y[i]) +"\t"+ (y2[i]) +"\t");
	}
	printWriter.close();
    }

    public void writeData(String[] header, int[] x, double[] y, double[] y2, double[] y3) throws IOException {
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]);
	int lengths[] = new int[] {x.length, y.length, y2.length, y3.length};
	int nbins = (new Double(MinMax.getMin(lengths))).intValue();
	for ( int i=0; i < nbins; i++ ) {
	    printWriter.println((x[i]) +"\t"+ (y[i]) +"\t"+ (y2[i]) +"\t"+ (y3[i]) +"\t");
	}
	printWriter.close();
    }

    public void writeData(String[] header, int[] x, double[] y, double[] y2, double[] y3, double[] y4) throws IOException {
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]);
	int lengths[] = new int[] {x.length, y.length, y2.length, y3.length, y4.length};
	int nbins = (new Double(MinMax.getMin(lengths))).intValue();
	for ( int i=0; i < nbins; i++ ) {
	    printWriter.println((x[i]) +"\t"+ (y[i]) +"\t"+ (y2[i]) +"\t"+ (y3[i]) +"\t" +(y4[i]) +"\t");
	}
	printWriter.close();
    }

    public void writeData(String[] header, int[] x, double[] y, double[] y2, double[] y3, double[] y4, double[] y5) throws IOException {
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]);
	int lengths[] = new int[] {x.length, y.length, y2.length, y3.length, y4.length, y5.length};
	int nbins = (new Double(MinMax.getMin(lengths))).intValue();
	for ( int i=0; i < nbins; i++ ) {
	    printWriter.println((x[i]) +"\t"+ (y[i]) +"\t"+ (y2[i]) +"\t"+ (y3[i]) +"\t" +(y4[i]) +"\t" +(y5[i]) +"\t");
	}
	printWriter.close();
    }

    public void writeData(String[] header, int[] col1, int[] col2, double[] col3) throws IOException {
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]);
	int lengths[] = new int[] {col1.length, col2.length, col3.length};
	int nbins = (new Double(MinMax.getMin(lengths))).intValue();
	for ( int i=0; i < nbins; i++ ) {
	    printWriter.println((col1[i]) +"\t"+ (col2[i]) +"\t"+ (col3[i]) +"\t");
	}
	printWriter.close();
    }

    public void writeData(String[] header, String[] col1, int[] col2, double[] y) throws IOException {
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]); 
	for ( int i=0; i < col1.length; i++ ) {
	    printWriter.println(col1[i] +"\t"+ (col2[i]) +"\t"+ (y[i]) +"\t");
	}
	printWriter.close();
    }

    public void writeData(String[] header, double[] c1, double[] c2, double[] c3) throws IOException {
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]);
	int lengths[] = new int[] {c1.length, c2.length, c3.length};
	double var = BasicStats.getVariance(lengths);
	if ( var != 0 ) {
	    logger.warn("input column data of different lengths. Using min.");
	}
	int nbins = (new Double(MinMax.getMin(lengths))).intValue();
	for ( int i=0; i < nbins; i++ ) {
	    printWriter.println((c1[i]) +"\t"+ (c2[i]) +"\t"+ (c3[i]) +"\t");
	}
	printWriter.close();
    }

    public void writeData(String[] header, double[] c1, double[] c2, double[] c3, double[] c4) throws IOException {
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]);
	int lengths[] = new int[] {c1.length, c2.length, c3.length, c4.length};
	double var = BasicStats.getVariance(lengths);
	if ( var != 0 ) {
	    logger.warn("input column data of different lengths. Using min.");
	}
	int nbins = (new Double(MinMax.getMin(lengths))).intValue();
	for ( int i=0; i < nbins; i++ ) {
	    printWriter.println((c1[i]) +"\t"+ (c2[i]) +"\t"+ 
				(c3[i]) +"\t"+ (c4[i]) +"\t");
	}
	printWriter.close();
    }
    
    public void writeData(String[] header, double[] c1, double[] c2, double[] c3, double[] c4, double[] c5) throws IOException {
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]);
	int lengths[] = new int[] {c1.length, c2.length, c3.length, c4.length, c5.length};
	double var = BasicStats.getVariance(lengths);
	if ( var != 0 ) {
	    logger.warn("input column data of different lengths. Using min.");
	}
	int nbins = (new Double(MinMax.getMin(lengths))).intValue();
	for ( int i=0; i < nbins; i++ ) {
	    printWriter.println((c1[i]) +"\t"+ (c2[i]) +"\t"+ 
				(c3[i]) +"\t"+ (c4[i]) +"\t"+ 
				(c5[i]) +"\t");
	}
	printWriter.close();
    }

    public void writeData(String[] header, int[] c1, double[] c2, double[] c3, double[] c4, int[] c5) 	throws IOException {
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]);
	int lengths[] = new int[] {c1.length, c2.length, c3.length, c4.length, c5.length};
	double var = BasicStats.getVariance(lengths);
	if ( var != 0 ) {
	    logger.warn("input column data of different lengths. Using min.");
	}
	int nbins = (new Double(MinMax.getMin(lengths))).intValue();
	for ( int i=0; i < nbins; i++ ) {
	    printWriter.println((c1[i]) +"\t"+ (c2[i]) +"\t"+ 
				(c3[i]) +"\t"+ (c4[i]) +"\t"+ 
				(c5[i]) +"\t");
	}
	printWriter.close();
    }

    public void writeData(String[] header, double[] c1, double[] c2, double[] c3, double[] c4, double[] c5, double[] c6) throws IOException {
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]);
	int lengths[] = new int[] {c1.length, c2.length, c3.length, c4.length, c5.length, c6.length};
	double var = BasicStats.getVariance(lengths);
	if ( var != 0 ) {
	    logger.warn("input column data of different lengths. Using min.");
	}
	int nbins = (new Double(MinMax.getMin(lengths))).intValue();
	for ( int i=0; i < nbins; i++ ) {
	    printWriter.println((c1[i]) +"\t"+ (c2[i]) +"\t"+ 
				(c3[i]) +"\t"+ (c4[i]) +"\t"+
				(c5[i]) +"\t"+ (c6[i]) +"\t");
	}
	printWriter.close();
    }

    public void writeData(String[] header, double[] c1, double[] c2, double[] c3, double[] c4, double[] c5, double[] c6, double[] c7) throws IOException {
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]);
	int lengths[] = new int[] {c1.length, c2.length, c3.length, c4.length, c5.length, c6.length, c7.length};
	double var = BasicStats.getVariance(lengths);
	if ( var != 0 ) {
	    logger.warn("input column data of different lengths. Using min.");
	}
	int nbins = (new Double(MinMax.getMin(lengths))).intValue();
	for ( int i=0; i < nbins; i++ ) {
	    printWriter.println((c1[i]) +"\t"+ (c2[i]) +"\t"+ 
				(c3[i]) +"\t"+ (c4[i]) +"\t"+
				(c5[i]) +"\t"+ (c6[i]) +"\t"+
				(c7[i]) +"\t");
	}
	printWriter.close();
    }

    public void writeData(String[] header, double[] c1, double[] c2, double[] c3, double[] c4, double[] c5, double[] c6, double[] c7, double[] c8) throws IOException {
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]);
	int lengths[] = new int[] {c1.length, c2.length, c3.length, c4.length, c5.length, c6.length, c7.length, c8.length};
	double var = BasicStats.getVariance(lengths);
	if ( var != 0 ) {
	    logger.warn("input column data of different lengths. Using min.");
	}
	int nbins = (new Double(MinMax.getMin(lengths))).intValue();
	for ( int i=0; i < nbins; i++ ) {
	    printWriter.println((c1[i]) +"\t"+ (c2[i]) +"\t"+ 
				(c3[i]) +"\t"+ (c4[i]) +"\t"+
				(c5[i]) +"\t"+ (c6[i]) +"\t"+
				(c7[i]) +"\t"+ (c8[i]) +"\t");
	}
	printWriter.close();
    }

    public void writeData(String[] header, double[] c1, double[] c2, double[] c3, double[] c4, double[] c5, double[] c6, double[] c7, double[] c8, double[] c9) throws IOException {
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]);
	int lengths[] = new int[] {c1.length, c2.length, c3.length, c4.length, c5.length, c6.length, c7.length, c8.length, c9.length};
	double var = BasicStats.getVariance(lengths);
	if ( var != 0 ) {
	    logger.warn("input column data of different lengths. Using min.");
	}
	int nbins = (new Double(MinMax.getMin(lengths))).intValue();
	for ( int i=0; i < nbins; i++ ) {
	    printWriter.println((c1[i]) +"\t"+ (c2[i]) +"\t"+ 
				(c3[i]) +"\t"+ (c4[i]) +"\t"+
				(c5[i]) +"\t"+ (c6[i]) +"\t"+
				(c7[i]) +"\t"+ (c8[i]) +"\t"+
				(c9[i]) +"\t");
	}
	printWriter.close();
    }
    public void writeData(String[] header, double[] c1, double[] c2, double[] c3, double[] c4, double[] c5, double[] c6, double[] c7, double[] c8, double[] c9, double[] c10) throws IOException {
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]);
	int lengths[] = new int[] {c1.length, c2.length, c3.length, c4.length, c5.length, c6.length, c7.length, c8.length, c9.length, c10.length};
	double var = BasicStats.getVariance(lengths);
	if ( var != 0 ) {
	    logger.warn("input column data of different lengths. Using min.");
	}
	int nbins = (new Double(MinMax.getMin(lengths))).intValue();
	for ( int i=0; i < nbins; i++ ) {
	    printWriter.println((c1[i]) +"\t"+ (c2[i]) +"\t"+ 
				(c3[i]) +"\t"+ (c4[i]) +"\t"+
				(c5[i]) +"\t"+ (c6[i]) +"\t"+
				(c7[i]) +"\t"+ (c8[i]) +"\t"+
				(c9[i]) +"\t"+ (c10[i]) +"\t");
	}
	printWriter.close();
    }

    public void writeData(String[] header, double[] c1, double[] c2, double[] c3, double[] c4, double[] c5, double[] c6, double[] c7, double[] c8, double[] c9, double[] c10, double[] c11) throws IOException {
	for ( int i=0; i < header.length; i++ )  printWriter.println(header[i]);
	int lengths[] = new int[] {c1.length, c2.length, c3.length, c4.length, c5.length, c6.length, c7.length, c8.length, c9.length, c10.length, c11.length};
	double var = BasicStats.getVariance(lengths);
	if ( var != 0 ) {
	    logger.warn("input column data of different lengths. Using min.");
	}
	int nbins = (new Double(MinMax.getMin(lengths))).intValue();
	for ( int i=0; i < nbins; i++ ) {
	    printWriter.println((c1[i]) +"\t"+ (c2[i]) +"\t"+ 
				(c3[i]) +"\t"+ (c4[i]) +"\t"+
				(c5[i]) +"\t"+ (c6[i]) +"\t"+
				(c7[i]) +"\t"+ (c8[i]) +"\t"+
				(c9[i]) +"\t"+ (c10[i]) +"\t"+
				(c11[i]) +"\t");
	}
	printWriter.close();
    }

}
