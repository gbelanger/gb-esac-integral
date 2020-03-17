package gb.esac.integral;

import gb.esac.io.MyFile;
import gb.esac.tools.FitsUtils;
import java.io.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Date;
import nom.tam.fits.*;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import org.apache.log4j.Logger;


public class UpdatePointList {

    public static String sep = File.separator;
    public static String homeDir = System.getProperty("user.home");
    public static String osName = System.getProperty("os.name");
    private static Logger logger  = Logger.getLogger(UpdatePointList.class);

    public static void main(String[] args) throws Exception {
	logger.info("Running java UpdatePointList");
	updatePointList();
    }

    public static void updatePointList() throws Exception {
	String integDir = "Documents"+sep+"integ"+sep+"idx";
	String macDirName = homeDir+sep+integDir;
	logger.info("Working directory is "+macDirName);
	String idxName = "GNRL-SCWG-GRP-IDX.fits.gz";
	MyFile scwidxFile = new MyFile(macDirName+sep+idxName);
	MyFile pointlisFile = new MyFile(macDirName+sep+"point.lis");	
	long pointlisLastModif = pointlisFile.lastModified();
	long scwidxLastModif = scwidxFile.lastModified();
	long currentTime = System.currentTimeMillis();
	long threeDaysInMillis = 3*86400*1000;
	long threeDaysAgo = currentTime - threeDaysInMillis;
	if ( ! pointlisFile.exists() || pointlisLastModif < threeDaysAgo ) {
	    if ( ! pointlisFile.exists() ) {
		logger.warn("File "+pointlisFile.getName()+" does not exist.");
	    }
	    else {
		logger.warn("File "+pointlisFile.getName()+" is more than 3 days old.");
	    }
	    if ( ! scwidxFile.exists() || scwidxLastModif < threeDaysAgo ) {
		if ( ! scwidxFile.exists() ) {
		    logger.warn("File "+scwidxFile.getName()+" does not exist.");
		}
		else {
		    logger.warn("File "+scwidxFile.getName()+" is more than 3 days old.");
		}
		boolean fetched = getscwidx();
		if ( fetched ) {
		    logger.info("Download successful");
		    logger.info("Constructing new "+pointlisFile.getName()+" ...");
		    scwidxFile = new MyFile(idxName);
		    scwidxFile.setLastModified(System.currentTimeMillis());
		    PointingListWriter.constructPointingList(scwidxFile);
		}
		else {
		    logger.error("Could not fetch index file");
		    logger.error("Cannot update "+pointlisFile.getName());
		}
	    }
	    else {
		logger.info("File "+scwidxFile.getName()+" is up to date (less than 3 days old)"); 
		logger.info("Updating "+pointlisFile.getName()+" ...");
		PointingListWriter.constructPointingList(scwidxFile);
	    }
	}
	else logger.info("File "+pointlisFile.getName()+" is up to date (less than 3 days old)"); 
    }

    public static boolean getscwidx() throws Exception {
	logger.info("Fetching from ISDC latest index file GNRL-SCWG-GRP-IDX.fits ...");
	PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("getscwidx")));
	String[] command = new String[] {"lftp", "-c", "\"open ftp://isdcarc.unige.ch/arc/rev_3/idx/scw/ && get GNRL-SCWG-GRP-IDX.fits\""};
	for ( int i=0; i < command.length; i++ ) {
	    pw.print(command[i]+" ");
	}
	pw.println();
	command = new String[] {"gzip", "GNRL-SCWG-GRP-IDX.fits"};
	for ( int i=0; i < command.length; i++ ) {
	    pw.print(command[i]+" ");
	}
	pw.close();
	MyFile scwidxFile = new MyFile("getscwidx");
	scwidxFile.chmod(755);
	systemCall(new String[] {"./"+"getscwidx"});
	scwidxFile.deleteOnExit();
	return ( (new File("GNRL-SCWG-GRP-IDX.fits.gz")).exists() );
    }

    static boolean systemCall(String[] args) {
	Runtime rt = Runtime.getRuntime();
	try {
	    Process p = rt.exec(args);
	    int rc = -1;
	    while ( rc == -1 ) {
		try { rc = p.waitFor(); }
		catch (InterruptedException e) { }
	    }
	    return rc == 0;
	}
	catch (IOException e) { return false; }
    } 

}
