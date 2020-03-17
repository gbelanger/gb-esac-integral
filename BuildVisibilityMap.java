package gb.esac.integral;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import esa.isoc.fd.FDFactory;
import esa.isoc.fd.FlightDynamics;
import esa.isoc.tvp.TVP;
import esa.isoc.util.sky.BinInterval;
import esa.isoc.util.sky.Position;
import gb.esac.tools.MyHeader;
import jsky.coords.WCSTransform;
import jsky.coords.wcscon;
import nom.tam.fits.Fits;
import nom.tam.fits.Header;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedDataOutputStream;
import org.apache.log4j.Logger;

public class BuildVisibilityMap {

    private static Logger logger  = Logger.getLogger(BuildVisibilityMap.class);
    
    public static void main(String[] args) throws Exception {
	if ( args.length != 4 ) {
	    logger.error("Usage: java gb.esac.integral.BuildVisibilityMap templateMap /full/path/to/DBOB_file.zip startDate endDate (dd-mm-yyyy)");
	    System.exit(-1);
	}
	String templateFilename = args[0];
	String dbobFile = args[1];
	String start = args[2];
	String end = args[3];
	String dither = "R";
	double minVisibility = 12600;
	initializeInProcessFD(dbobFile);

	// Go through each pixel of the template and get the visibility for the corresponding sky coord
	File dataFile = new File(templateFilename);
	Fits template = new Fits(dataFile.getAbsoluteFile());
	ImageHDU hdu = (ImageHDU) template.getHDU(0);
	Header head = hdu.getHeader();
	double[][] data = (double[][]) hdu.getKernel();
	double[][] visibilityData = (double[][]) hdu.getKernel();
	WCSTransform wcs = new WCSTransform(new MyHeader(head));
	int[] dims = ArrayFuncs.getDimensions(data);
	for (int i = 0; i < dims[0]; i++) {
	    logger.info("Building Row " + (i + 1) + " (of " + dims[0] + ")");
	    for (int j = 0; j < dims[1]; j++) {
		Point2D.Double skyCoords;
		try {
		    skyCoords = wcs.pix2wcs(j, i);
		    Point2D.Double raDec = wcscon.gal2fk5(skyCoords);
		    double visibility = getVisibilityFromFD(raDec.getX(), raDec.getY(), dither, start, end, minVisibility);
		    //double visibility = getVisibilityFromWeb(raDec.getX(), raDec.getY(), dither, start, end, minVisibility);		    
		    visibilityData[i][j] = visibility;
		} catch (Exception e) {
		    visibilityData[i][j] = 0;
		}
	    }
	    writeToFitsFile(head, data, "visibilityMap_"+start+"_to_"+end+".fits");
	}
    }

    private static boolean initializeInProcessFD(String skyMapsSource) {
	if (skyMapsSource == null) {
	    return false;
	}
	List<String> skyMapDataFiles = Arrays.asList(skyMapsSource.split(":"));
	FDFactory.initializeInProcessSkyMapsOnly(skyMapDataFiles);
	return true;
    }

    private static void writeToFitsFile(Header header, double[][] data, String outputFilename) throws Exception {
	ImageHDU visibilityHDU = new ImageHDU(header, new ImageData(data));
	Fits visibilityMap = new Fits();
	visibilityMap.addHDU(visibilityHDU);
	FileOutputStream fos = new FileOutputStream(outputFilename);
	BufferedDataOutputStream dos = new BufferedDataOutputStream(fos);
	visibilityMap.write(dos);
	dos.close();	
    }

    private static double getVisibilityFromFD(double ra, double dec, String dither, String start, String end, double minVisibility) throws Exception {
       long totalTime = 0;
       FlightDynamics flightDynamics = FDFactory.getFlightDynamics();
       SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
       TVP tvp = new TVP(flightDynamics, 2.17d, 5, 5, formatter.parse(start), formatter.parse(end), 
			 new Position(ra, dec), TVP.HEX, formatter.parse(start), formatter.parse(end), 3600);
       List<BinInterval> v = tvp.getVisibility();
       if (!tvp.hasErrors() && v != null) {
           for (BinInterval vis : v) {
               totalTime += vis.getDuration();
           }
       } else {
           System.out.println(tvp.getMessage());
       }
       return totalTime;
   }

    private static double getVisibilityFromWeb(double ra, double dec, String dither, String start, String end, double minVisibility) throws Exception {
	String tvpURL = "http://integral.esac.esa.int/isocweb/tvp.html";
	String completeURL = tvpURL.concat("?action=predict");
	completeURL = completeURL.concat("&ra="+ra);
	completeURL = completeURL.concat("&dec="+dec);
	completeURL = completeURL.concat("&dither="+dither);
	completeURL = completeURL.concat("&startDate="+start);
	completeURL = completeURL.concat("&endDate="+end);
	completeURL = completeURL.concat("&duration="+(new Double(minVisibility)).toString());
	BufferedReader r = new BufferedReader(new InputStreamReader(new URL(completeURL).openStream()));

	//  Count output lines
	int nLines = 0;
	while (r.readLine() != null) {
	    nLines++;
	}

	//  Get only the line with the total visibility which is the 14th from the bottom
	int lineNoOfTotalVisibility = nLines-14;
	r = new BufferedReader(new InputStreamReader(new URL(completeURL).openStream()));
	int i=0;
	while ( i < lineNoOfTotalVisibility ) {
	    r.readLine();
	    i++;
	}
	String s = r.readLine();
	r.close();
	
	//  Tokenize to drop the leading white spaces
	StringTokenizer st = new StringTokenizer(s);
	String totalVisibility = st.nextToken();
	return (new Double(totalVisibility)).doubleValue();
    }
}
