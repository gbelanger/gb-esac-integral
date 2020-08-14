package gb.esac.integral;


import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import gb.esac.io.MyFile;
import gb.esac.tools.MyHeader;

import jsky.coords.WCSTransform;
import nom.tam.fits.Fits;
import nom.tam.fits.Header;
import nom.tam.fits.ImageHDU;
import nom.tam.util.BufferedDataInputStream;
import org.apache.log4j.Logger;
import nom.tam.util.ArrayFuncs;
import java.io.FileNotFoundException;
import java.io.InputStream;


public final class IntegralUtils {

    private static Logger logger  = Logger.getLogger(IntegralUtils.class);
    private static String sep = File.separator;
    
    public static double getIsgriPixelValue(Point2D.Double radec, Fits fitsImage, int extnum) throws Exception {
	//  Find pixel that corresponds to ra dec
	ImageHDU imaHDU = (ImageHDU) fitsImage.getHDU(extnum);
	Point2D.Double ccdXY  = getXY(radec, imaHDU, false);
	int x = (new Double(Math.rint(ccdXY.getX()))).intValue();
	int y = (new Double(Math.rint(ccdXY.getY()))).intValue();
	//  Get pixel value
	float[][] data = (float[][]) imaHDU.getKernel(); 
	double pixValue = 0;
	if ( x<=20+5 || x>=380-5 || y<=18+5 || y>=382-5 ) {
	    logger.warn("  Outside FOV");
	    pixValue = Double.NaN;
	}
	else pixValue = data[y-1][x-1]; 
	return pixValue;
    }

    public static Point2D.Double getXY(Point2D.Double radec, ImageHDU imageHDU, boolean physical) {
	Point2D.Double ccdXY = null;
	Point2D.Double physXY = null;
	// Get image header
	Header imaHead  = imageHDU.getHeader();
	//  Get keywords necessary to make imaCoords to physCoords
	double ltm1 = imaHead.getDoubleValue("LTM1_1");
	double ltv1 = imaHead.getDoubleValue("LTV1");
	if ( ltm1 == 0 ) ltm1 = 1;
	//  Transform
	MyHeader myHeader = new MyHeader(imaHead);
	WCSTransform wcsTransform = new WCSTransform(myHeader);
	ccdXY = wcsTransform.wcs2pix(radec.getX(), radec.getY());
 	physXY = new Point2D.Double((ccdXY.getX()-ltv1)/ltm1, (ccdXY.getY()-ltv1)/ltm1);
	if ( physical ) return physXY;
	else return ccdXY; 
    }

    public static float[] getIsgriEnergyBounds(Fits fitsImage, int bandNo) throws Exception {
	float[] emin_emax = new float[2];
	int bandIdx = bandNo-1;
	int extnum=2 + 4*bandIdx;
	ImageHDU imaHDU = (ImageHDU) fitsImage.getHDU(extnum);
	Header imaHead  = imaHDU.getHeader();
	emin_emax[0]   = imaHead.getFloatValue("E_MIN");
	emin_emax[1]   = imaHead.getFloatValue("E_MAX");
	(fitsImage.getStream()).close();
	return emin_emax;
    }

    public static float[][] getNormalisedEffectiveArea(String instrumentName) throws Exception {
	String filename;
	if ( instrumentName.equals("ISGRI") ) {
	    filename = "resources/eff_area_isgri.fits.gz";
	}
	else if ( instrumentName.equals("SPI") ) {
	    filename = "resources/eff_area_spi.fits.gz";
	}
	else if ( instrumentName.equals("JEMX1") ) {
	    filename = "resources/eff_area_jmx1.fits.gz";
	}
	else if ( instrumentName.equals("JEMX2") ) {
	    filename = "resources/eff_area_jmx2.fits.gz";
	}
	else {
	    throw new IntegralException("Instrument: "+instrumentName+" Not recognised");
	}
	BufferedDataInputStream effAreaFileAsStream;
	try {
	    effAreaFileAsStream = new BufferedDataInputStream(new FileInputStream(filename));
	}
	catch ( FileNotFoundException e ) {
	    effAreaFileAsStream = new BufferedDataInputStream(getFileFromJarAsStream(filename));
	}

	Fits effAreaFits = new Fits(effAreaFileAsStream, true);
	ImageHDU effAreaHDU = (ImageHDU) effAreaFits.getHDU(0);
	float[][] effArea = (float[][]) effAreaHDU.getKernel();
	int[] dims = ArrayFuncs.getDimensions(effArea);
	int n = dims[0];
	int m = dims[1];
	float max = -Float.MAX_VALUE;
	float min = Float.MAX_VALUE;
	//  Determine min and max values
	for ( int row=0; row < n; row++ ) {
	    for ( int col=0; col < m; col++ ) {
		max = Math.max(max, effArea[row][col]);
		min = Math.min(min, effArea[row][col]);
	    }
	}
	max = max - min;
	//  Normalize to min=0 and max=1
	float[][] normEffArea = new float[n][m];
	for ( int row=0; row < n; row++ ) {
	    for ( int col=0; col < m; col++ ) {
		normEffArea[row][col] = (effArea[row][col] - min)/max;
	    }
	}
	return normEffArea;
    }

    public static InputStream getFileFromJarAsStream(String name) {
	return ClassLoader.getSystemResourceAsStream(name);
    }


    public static ArrayList<MyFile> generateImageFileArrayListFromScwIDs(String[] scwIDs, String[] dataDirNames) throws IOException {
	logger.info("Selecting images ...");
	ArrayList<MyFile> imageFilesArrayList = new ArrayList<MyFile>();
	int k=0;
	String scwID = scwIDs[k];
	String rev = scwID.substring(0,4);
	for ( int i=0; i < dataDirNames.length; i++ ) {
	    String dirname = dataDirNames[i];
	    while ( k < scwIDs.length && dirname.contains(sep+rev+sep) ) {
		String isgri_sky_ima = "isgri_sky_ima.fits.gz";
		String fullDirname = dirname+sep+scwID+sep+"scw"+sep+scwID+".001";
		String imageFilename = fullDirname+sep+isgri_sky_ima;
		MyFile imageFile = new MyFile(imageFilename);
		if ( imageFile.exists() ) {
		    imageFilesArrayList.add(imageFile);
		}
		else {
		    isgri_sky_ima = "isgri_sky_ima.fits";
		    imageFilename = fullDirname+sep+isgri_sky_ima;
		    imageFile = new MyFile(imageFilename);
		    if ( imageFile.exists() ) {
			imageFilesArrayList.add(imageFile);
		    }
		}
		k++;
		if ( k < scwIDs.length ) {
		    scwID = scwIDs[k];
		    rev = scwID.substring(0,4);
		}
	    }
	}
	imageFilesArrayList.trimToSize();
	logger.info("  "+imageFilesArrayList.size()+" selected.");
	return imageFilesArrayList;
    }

}
