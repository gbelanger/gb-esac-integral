package gb.esac.integral;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.util.BufferedDataInputStream;
import org.apache.log4j.Logger;
import java.text.DecimalFormat;

public final class PointingListWriter {

    private static Logger logger  = Logger.getLogger(PointingListWriter.class);
    private static String sep = File.separator;
    private static DecimalFormat number = new DecimalFormat("0.0");

    public static void constructPointingList(String scwidxFilename) throws Exception {
	File scwidxFile = new File(scwidxFilename);
	constructPointingList(scwidxFile);
    }

    public static void constructPointingList(File scwidxFile) throws Exception {
	//  Get data from GNRL-SCWG-GRP-IDX.fits.gz
	logger.info("General index file: "+scwidxFile.getCanonicalPath());
	long lastModif = scwidxFile.lastModified();
	long currentTime = System.currentTimeMillis();
	double diff = (currentTime - lastModif)/(3600.0*1000.0);
	String unit = "hours";
	if ( diff < 1.0 ) {
	    diff *= 60.0;
	    unit = "minutes";
	}
	if ( diff < 1.0 ) {
	    diff *= 60.0;
	    unit = "seconds";
	}
	logger.info("Last modified "+number.format(diff)+" "+unit+" ago");
	logger.info("Reading "+scwidxFile.getName());
	Fits f = openFits(scwidxFile);
	BinaryTableHDU hdu = (BinaryTableHDU) f.getHDU(1);
	String[] scwid = (String[]) hdu.getColumn("SWID");
	String[] expid = (String[]) hdu.getColumn("EXPID");
	String[] scwtype = (String[]) hdu.getColumn("SW_TYPE");
	float[] ra_scx = (float[]) hdu.getColumn("RA_SCX");
	float[] dec_scx = (float[]) hdu.getColumn("DEC_SCX");
	double[] telapse = (double[]) hdu.getColumn("TELAPSE");
	String[] ertFirst = (String[]) hdu.getColumn("ERTFIRST");
	String[] ertLast = (String[]) hdu.getColumn("ERTLAST");
	double[] tstart = (double[]) hdu.getColumn("TSTART");
	double[] tstop = (double[]) hdu.getColumn("TSTOP");
	byte[] spimode = (byte[]) hdu.getColumn("SPIMODE");
	byte[] ibismode = (byte[]) hdu.getColumn("IBISMODE");
	byte[] jmx1mode = (byte[]) hdu.getColumn("SPIMODE");
	byte[] jmx2mode = (byte[]) hdu.getColumn("SPIMODE");
	byte[] omcmode = (byte[]) hdu.getColumn("SPIMODE");
	float[] ra_scz = (float[]) hdu.getColumn("RA_SCZ");
	float[] dec_scz = (float[]) hdu.getColumn("DEC_SCZ");
	float[] pos_angle = (float[]) hdu.getColumn("POSANGLE");
	//  Write to file
	logger.info("Writing pointing list");
	String parent = scwidxFile.getParent();
	if ( parent == null ) parent = ".";
	PrintWriter pw1 = new PrintWriter(new BufferedWriter(new FileWriter(parent+sep+"point.lis")));
	PrintWriter pw2 = new PrintWriter(new BufferedWriter(new FileWriter(parent+sep+"bad_ibis.lis")));
	PrintWriter pw3 = new PrintWriter(new BufferedWriter(new FileWriter(parent+sep+"bad_spi.lis")));
	PrintWriter pw4 = new PrintWriter(new BufferedWriter(new FileWriter(parent+sep+"bad_jmx.lis")));
	PrintWriter pw5 = new PrintWriter(new BufferedWriter(new FileWriter(parent+sep+"bad_omc.lis")));
	PrintWriter pw6 = new PrintWriter(new BufferedWriter(new FileWriter(parent+sep+"slews.lis")));
	PrintWriter pw7 = new PrintWriter(new BufferedWriter(new FileWriter(parent+sep+"expid.lis")));
	for ( int i=0; i < scwid.length; i++ ) {
	    String type = scwtype[i];
	    int ibis = ibismode[i];
	    int spi = spimode[i];
	    int jmx1 = jmx1mode[i];
	    int jmx2 = jmx2mode[i];
	    int omc = omcmode[i];
	    if ( type.equals("POINTING") ) {
		// IBIS
		if ( ibis == 41 || ibis == 42 || ibis == 43 ) {
		    //if ( telapse[i] >= 1800 && telapse[i] <= 4000 ) {
		    pw1.println(scwid[i]+"\t"+ra_scx[i]+"\t"+dec_scx[i]+"\t"
			       +telapse[i]+"\t"+tstart[i]+"\t"+tstop[i]+"\t"+ertFirst[i]+"\t"+ertLast[i]+"\t"
			       +ra_scz[i]+"\t"+dec_scz[i]+"\t"+pos_angle[i]);
		    //}
		}
		else {
		    pw2.println(scwid[i]+"\t"+ra_scx[i]+"\t"+dec_scx[i]+"\t"
				+telapse[i]+"\t"+tstart[i]+"\t"+tstop[i]+"\t"
				+"\t"+ibis);
		}
		// SPI
		if ( spi != 41 ) {
		    pw3.println(scwid[i]+"\t"+ra_scx[i]+"\t"+dec_scx[i]+"\t"
				+telapse[i]+"\t"+tstart[i]+"\t"+tstop[i]+"\t"
				+spi);
		}
		// JEM-X
		boolean jmx1_ok = ( jmx1 == 41 || jmx1 ==42 || jmx1 == 43 || jmx1 == 44 || jmx1 == 45 );
		boolean jmx2_ok = ( jmx2 == 41 || jmx2 ==42 || jmx2 == 43 || jmx2 == 44 || jmx2 == 45 );
		if ( !jmx1_ok && !jmx2_ok ) {
		    pw4.println(scwid[i]+"\t"+ra_scx[i]+"\t"+dec_scx[i]+"\t"
				+telapse[i]+"\t"+tstart[i]+"\t"+tstop[i]+"\t"
				+jmx1+"\t"+jmx2);		    
		}
		// OMC
		boolean omc_ok = ( omc == 41 || omc == 42 || omc == 43 );
		if ( !omc_ok ) {
		    pw5.println(scwid[i]+"\t"+ra_scx[i]+"\t"+dec_scx[i]+"\t"
				+telapse[i]+"\t"+tstart[i]+"\t"+tstop[i]+"\t"
				+omc);		    
		}
		// EXPID
		if ( !expid[i].equals(null) && !expid[i].equals("") ) {
		    pw7.println(expid[i]+"\t"+ertFirst[i]);
		}
	    }
	    else {
		if ( type.equals("SLEW") ) {
		    pw6.println(scwid[i]+"\t"+ra_scx[i]+"\t"+dec_scx[i]+"\t"
				+telapse[i]+"\t"+tstart[i]+"\t"+tstop[i]);
		}
	    }
	}
	pw1.close();
	pw2.close();
	pw3.close();
	pw4.close();
	pw5.close();
	pw6.close();
	pw7.close();
    }

    public static Fits openFits(File file) throws Exception {
	boolean isGzipped = isGzipped(file);
	BufferedDataInputStream dis = new BufferedDataInputStream(new FileInputStream(file));
	Fits fitsFile = new Fits(dis, isGzipped);
	return fitsFile;
    }
    
    public static boolean isGzipped(File file) throws IOException {
	InputStream in = new FileInputStream(file);
	int magic1 = in.read();
	int magic2 = in.read();
	in.close();
	return (magic1 == 0037 && magic2 == 0213);
    }
}
