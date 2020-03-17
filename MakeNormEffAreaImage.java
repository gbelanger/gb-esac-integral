package gb.esac.integral;

import gb.esac.tools.MinMax;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import nom.tam.fits.Fits;
import nom.tam.fits.Header;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedDataInputStream;
import nom.tam.util.BufferedDataOutputStream;
import org.apache.log4j.Logger;

public class MakeNormEffAreaImage {

    static Logger logger  = Logger.getLogger(MakeNormEffAreaImage.class);

    public static void main(String[] args) throws Exception   {

	String eff_area_filename = "eff_area_ibis.fits.gz";
	if ( args.length == 1 ) {
	    eff_area_filename = args[0];
	}
	String instrument = "ibis";
	if ( eff_area_filename.contains("ibis") ) instrument = "ibis";
	else if ( eff_area_filename.contains("spi") ) instrument = "spi";
	else {
	    logger.error("Unknown instrument: did not find 'ibis' or 'spi' in filename");
	    System.exit(-1);
	}

	//  Read effective area file
	logger.info("Reading effective area ("+eff_area_filename+")");
	BufferedDataInputStream effAreaFileAsStream = new BufferedDataInputStream(new FileInputStream(eff_area_filename));	    
	Fits effAreaFits = new Fits(effAreaFileAsStream, isGzipped(eff_area_filename));
	int exten = 0;
	if ( instrument == "spi" ) exten = 2;
	ImageHDU effAreaHDU = (ImageHDU) effAreaFits.getHDU(exten);
	float[][] effArea = (float[][]) effAreaHDU.getKernel();

	//  Determine min and max values
	float min = MinMax.getMin(effArea);
	float max = MinMax.getMax(effArea);
	max = max - min;

	//  Normalize to min=0 and max=1
	logger.info("Normalising map as (value - min)/max");
	int[] dims = ArrayFuncs.getDimensions(effArea);
	int nCols = dims[0];
	int nRows = dims[1];
	float[][] normEffArea = new float[nCols][nRows];
	for ( int row=0; row < nRows; row++ ) {
	    for ( int col=0; col < nCols; col++ ) {
		normEffArea[row][col] = (effArea[row][col] - min)/max;
	    }
	}

	//  Construct the header with the essential WCS keywords
	logger.info("Constructing FITS header");
	Header head = new Header(effAreaHDU.getData());
 	head.addValue("EXTNAME", "NORM-EFF-AREA", "Extension name");
 	head.addValue("CRVAL1", 266.4168, "LONG at the reference value");
 	head.addValue("CRVAL2", -29.0078, "LAT at the reference value");
	head.addValue("RADECSYS", "FK5", "Stellar reference frame");
	head.addValue("EQUINOX", 2000.0, "Coordinate system equinox");
	//head.addValue("RADECSYS", "GAL", "Coordinate system");
	//head.addValue("EQUINOX", 2000.0, "Epoch of the equinox");
	head.addValue("CTYPE1","RA---TAN", "Coordinates -- projection");
	head.addValue("CTYPE2","DEC--TAN", "Coordinates -- projection");

	double crpix;
	double cdelt1, cdelt2;
	String outputName = "norm_eff_area_ibis.fits";
	double rotationAngle = -11.3*Math.PI/180;
	// For IBIS
	if ( instrument == "ibis" ) {
	    head.addValue("INSTRUME", "IBIS", "Telescope or mission name");
	    crpix = 200.5;
	    cdelt1 = 0.0822862539155913;
	    cdelt2 = 0.0822862539155913;
	}
	// For SPI
	else {
	    head.addValue("INSTRUME", "SPI", "Telescope or mission name");
	    crpix = 126.0;
	    cdelt1 = 0.2;
	    cdelt2 = 0.2;
	    outputName = "norm_eff_area_spi.fits";
	}
	double xscale = cdelt1;
	double yscale = cdelt2;
	double cd1_1 = xscale*Math.cos(rotationAngle);
	double cd1_2 = -yscale*Math.sin(rotationAngle);
	double cd2_1 = xscale*Math.sin(rotationAngle);
	double cd2_2 = yscale*Math.cos(rotationAngle);
	head.addValue("CRPIX1", crpix, "X reference pixel");
	head.addValue("CRPIX2", crpix, "Y reference pixel");
	head.addValue("CDELT1", cdelt1, "Degrees per pixel along x axis");
	head.addValue("CDELT2", cdelt2, "Degrees per pixel along y axis");
	head.addValue("CD1_1", cd1_1, "Element (1,1) of coordinate transf. matrix");
 	head.addValue("CD1_2", cd1_2, "Element (1,2) of coordinate transf. matrix");
 	head.addValue("CD2_1", cd2_1, "Element (2,1) of coordinate transf. matrix");
  	head.addValue("CD2_2", cd2_2, "Element (2,2) of coordinate transf. matrix");


	// Construct normEffArea.fits and write to file
	Fits normEffAreaFits = new Fits();
	ImageData normEffAreaData = new ImageData(normEffArea);
	ImageHDU normEffAreaHDU = new ImageHDU(head, normEffAreaData);
	normEffAreaFits.addHDU(normEffAreaHDU);
	FileOutputStream fos = new FileOutputStream(outputName);
	BufferedDataOutputStream dos = new BufferedDataOutputStream(fos);
	normEffAreaFits.write(dos);
	dos.flush();
	dos.close();
	logger.info("Output written to "+outputName);

    }

    public static boolean isGzipped(String fileName) throws IOException {
        return isGzipped(new File(fileName));
    }
    
    public static boolean isGzipped(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        int magic1 = in.read();
        int magic2 = in.read();
        in.close();
        return (magic1 == 0037 && magic2 == 0213);
    }

}