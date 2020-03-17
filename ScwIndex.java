package gb.esac.integral;


import java.awt.geom.Point2D;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import gb.esac.io.AsciiDataFileReader;
import gb.esac.tools.Converter;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;


public class ScwIndex {

    // This class is an object that represents the GNRL-SCWG-GRP-IDX.fits file

    // THIS WAS NEVER FINISHED

    private InputStream str;
    private File file;
    private Fits f;
    private boolean fileIsFits;
    private boolean scwIDsAreSet = false;
    private boolean raDecsAreSet = false;
    private boolean exposuresAreSet = false;
    private boolean datesAreSet = false;

    public String[] scwIDs = null;
    public Point2D.Double[] raDecs = null;
    public double[] exposures = null;
    public String[] dates = null;;

    //  Constructors
    public ScwIndex() {

    }

    public ScwIndex(String filename) throws Exception  {
	file = new File(filename);
	if ( ! file.exists() ) {
	    throw new IntegralException("File does not exist");
	}
	else {
	    fileInit(file);
	    setData();
	}	    
    }

    //  Stream initialisers
    protected void fileInit(File file) throws IntegralException {
        try {
            FileInputStream filestr = new FileInputStream(file);
            streamInit(filestr);
        } catch (IOException e) {
              throw new IntegralException("Unable to create Input Stream from File: "+file);
        }
    }

    protected void streamInit(InputStream str) throws IntegralException {
	str = new BufferedInputStream(str);
	try {
	    f = new Fits(str);
	    fileIsFits = true;
	    
	} catch (FitsException e) {
	    fileIsFits = false;
	}		
	try {
	    str = new GZIPInputStream(str);
	    try {
		f = new Fits(str, true);
		fileIsFits = true;
	    } catch (FitsException e1) {
		fileIsFits = false;
	    }
	    
	} catch (IOException e2) {
	    throw new IntegralException("Cannot initialize input stream"+e2);
	}
    }

    // Set methods
    protected void setData() throws Exception {
	if ( fileIsFits ) {
	    BinaryTableHDU hdu = (BinaryTableHDU) f.getHDU(1);
	    scwIDs = (String[]) hdu.getColumn("SWID");
	    float[] ra_scx = (float[]) hdu.getColumn("RA_SCX");
	    float[] dec_scx = (float[]) hdu.getColumn("DEC_SCX");
	    dates = (String[]) hdu.getColumn("DATE");
	    exposures = (double[]) hdu.getColumn("TELAPSE");
	    double[] ra = Converter.float2double(ra_scx);
	    double[] dec = Converter.float2double(dec_scx);
	    for ( int i=0; i < ra_scx.length; i++ ) {
		raDecs[i] = new Point2D.Double(ra[i], dec[i]);
	    }
	}
	else {
	    AsciiDataFileReader dataFile = new AsciiDataFileReader(file.getCanonicalPath());
	}
	scwIDsAreSet = true;
	raDecsAreSet = true;
	exposuresAreSet = true;
	datesAreSet = true;
    }

    public void setScwIDs(String[] scwIDs) {
	scwIDsAreSet = true;
    }

    public void setRaDecs(Point2D.Double[] raDecs) {
	raDecsAreSet = true;
    }
    
    public void setExposures(double[] exposures) {
	exposuresAreSet = true;
    }

    public void setDates(String[] dates) {
	datesAreSet = true;
    }


    //  Get Methods
    public String[] getScwIDs() throws IntegralException {
	if ( scwIDsAreSet )
	    return scwIDs;
	else 
	    throw new IntegralException("ScwIDs are not set");
    }

    public Point2D.Double[] getRaDecs() throws IntegralException {
	if ( raDecsAreSet )
	    return raDecs;
	else
	    throw new IntegralException("RaDecs are not set");
    }

    public double[] getExposures() throws IntegralException {
	if ( exposuresAreSet )
	    return exposures;
	else
	    throw new IntegralException("Exposures are not set");
    }

    public String[] getDates() throws IntegralException {
	if ( datesAreSet )
	    return dates;
	else 
	    throw new IntegralException("Dates are not set");
    }

}