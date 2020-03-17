package gb.esac.integral;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;

import gb.esac.io.AsciiDataFileReader;
import nom.tam.fits.Fits;
import nom.tam.fits.Header;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;
import nom.tam.util.BufferedDataInputStream;
import nom.tam.util.BufferedDataOutputStream;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class GenerateExposures {

    static Logger logger  = Logger.getLogger(GenerateExposures.class);
    static DecimalFormat number = new DecimalFormat("0.0000");

    public static void main(String[] args) throws Exception   {

	PropertyConfigurator.configure("/Users/gbelanger/javaProgs/gb/esac/logger.config");

	String inputFilename = "pointings.dat";
	if ( args.length == 1 )
	    inputFilename = args[0];


	//  Read map of ISGRI effective area and normalize it so that max = 1
	logger.info("Reading ISGRI effective area");
	BufferedDataInputStream effAreaFileAsStream = 
	    new BufferedDataInputStream(new FileInputStream("/Users/gbelanger/javaProgs/gb/esac/integral/eff_area.fits.gz"));
	Fits effAreaFits = new Fits(effAreaFileAsStream, true);
	ImageHDU effAreaHDU = (ImageHDU) effAreaFits.getHDU(0);
	float[][] effArea = (float[][]) effAreaHDU.getKernel();
	float max = 0;
	float min = Float.MAX_VALUE;

	//  Determine min and max values
	for ( int row=0; row < 400; row++ ) {
	    for ( int col=0; col < 400; col++ ) {
		max = Math.max(max, effArea[row][col]);
		min = Math.min(min, effArea[row][col]);
	    }
	}
	max = max - min;

	//  Normalize to min=0 and max=1
	float[][] normEffArea = new float[400][400];
	for ( int row=0; row < 400; row++ ) {
	    for ( int col=0; col < 400; col++ ) {
		normEffArea[row][col] = (effArea[row][col] - min)/max;
	    }
	}

	//  Construct the header with the essential WCS keywords
	logger.info("Constructing FITS header template");
	Header head = new Header(effAreaHDU.getData());
 	head.addValue("EXTNAME", "NORM-EFF-AREA", "Extension name");
 	head.addValue("CRVAL1", 266.4168, "LONG at the reference value");
 	head.addValue("CRVAL2", -29.0078, "LAT at the reference value");
	head.addValue("RADECSYS", "J2000", "Coordinate system");
	//head.addValue("RADECSYS", "GAL", "Coordinate system");
	//head.addValue("EQUINOX", 2000.0, "Epoch of the equinox");
	head.addValue("CTYPE1","RA---TAN", "Coordinates -- projection");
	head.addValue("CTYPE2","DEC--TAN", "Coordinates -- projection");
	head.addValue("CRPIX1", 200.5, "X reference pixel");
	head.addValue("CRPIX2", 200.5, "Y reference pixel");
	double scale = 0.0822862539155913;
	double xscale = scale;
	double yscale = scale;
	double rotationAngle = 11.3*Math.PI/180;
	rotationAngle = 0;
	double cd1_1 = xscale*Math.cos(rotationAngle);
	double cd1_2 = -yscale*Math.sin(rotationAngle);
	double cd2_1 = xscale*Math.sin(rotationAngle);
	double cd2_2 = yscale*Math.cos(rotationAngle);
	head.addValue("CD1_1", cd1_1, "Element (1,1) of coordinate transf. matrix");
 	head.addValue("CD1_2", cd1_2, "Element (1,2) of coordinate transf. matrix");
 	head.addValue("CD2_1", cd2_1, "Element (2,1) of coordinate transf. matrix");
  	head.addValue("CD2_2", cd2_2, "Element (2,2) of coordinate transf. matrix");


	//  Read in the list of pointings
	logger.info("Reading list of pointings");
	AsciiDataFileReader listOfPointings = new AsciiDataFileReader(inputFilename);
	double[] ra = listOfPointings.getDblCol(0);
	double[] dec = listOfPointings.getDblCol(1);
	float[] dwell = listOfPointings.getFltCol(2);

// 	//  Keep these for the future refined exposure map tool
// 	double[] ra_z = listOfPointings.getDblCol(3);
// 	double[] dec_z = listOfPointings.getDblCol(4);
// 	double[] rollAngle = listOfPointings.getDblCol(5);
// 	float[] proposedExp = listOfPointings.getFltCol(6);


	//  Construct a fits image for each pointing
	//  by defining CRVAL1 and CRVAL2 as the pointing direction
	//  and normalizing the effective area to the exposure time
	logger.info("Generating exposure map for each pointing");
	int nPointings = ra.length;
	Fits fitsIma = new Fits();
	float[][] data = new float[400][400];
	ImageData imaData = null;
	ImageHDU imaHDU = null;

    	for ( int i=0; i < nPointings; i++ ) {

	    //  Scale effective area to pointing duration
	    float dwellTime = dwell[i];
	    if ( dwellTime != 0 ) {

		logger.info("Pointing "+(i+1)+":  RA = "+number.format(ra[i])+"\t Dec = "+number.format(dec[i])+"\t exposure = "+dwellTime);

		for ( int row=0; row < 400; row++ )
		    for ( int col=0; col < 400; col++ )
			data[row][col] = normEffArea[row][col]*dwellTime;
		imaData = new ImageData(data);


		//  Modify reference RA and Dec
		head.addValue("CRVAL1", ra[i], "LONG at the reference value");
		head.addValue("CRVAL2", dec[i], "LAT at the reference value");


		//  Make FITS image
		imaHDU = new ImageHDU(head, imaData);
		fitsIma = new Fits();
		fitsIma.addHDU(imaHDU);


		//  Write individual exposures
		File outputDir = new File("exposures");
		if ( outputDir.exists() ) {
		    outputDir.delete();
		    outputDir.mkdir();
		}
		else {
		    outputDir.mkdir();
		}
		String dirName = outputDir.getPath();
		String outputName = dirName+"/exp_"+i+".fits";
		FileOutputStream fos = new FileOutputStream(outputName);
		BufferedDataOutputStream dos = new BufferedDataOutputStream(fos);
		fitsIma.write(dos);
		dos.flush();
		dos.close();
	    }

	}

	logger.info("Program complete");

    }

}
