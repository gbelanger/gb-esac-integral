import java.io.*;
import java.sql.*;
import java.math.*;
import oracle.jdbc.*;
import oracle.jdbc.pool.*;
import oracle.sql.*;


   

public class ScwData {

    public static int nScw;
    public String obsID;
    public String [] scwIDs;

    public static String sep = File.separator;

    public ScwData(Connection conn, String id){ 

	try {

	    // Initialise obsID
	    obsID=id;

	    // Get the number of SCWs 

	    Statement stmt_count = conn.createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

	    //  This was Rees' original selection criterion
	    
	    // 	    String selectionQuery = "FROM obs,expo,scw "+
	    // 		"where obs.obs_id='"+obsID+
	    // 		"' AND obs.obs_id=expo.obs_id "+
	    // 		"AND expo.exp_id=scw.exp_id";
	
	    //  This is the new one

	    String selectionQuery = "FROM obs, expo, scw "+ 

		//  Only PUBLIC data
 		//"where (ps_spi + ps_ibis + ps_jemx1 + ps_jemx2 + ps_omc) = 0 "+
   		//"AND substr(obs.OBS_ID,1,2) NOT LIKE '88' "+

		//  Exclude calibrations and dummy pointings 
		//"where substr(obs.OBS_ID,1,2) NOT LIKE '88' "+
		//"AND obs.obs_id='"+obsID+"' "+

		"where obs.obs_id='"+obsID+"' "+
		"AND substr(scw.scw_ID,12,1) not in ('1') "+
		"AND obs.obs_id=expo.obs_id "+
		"AND expo.exp_id=scw.exp_id "+
		"AND scw.GOOD_ISGRI > 1000 "+
		"AND scw.IJD_START > 1093 "+
		"AND scw.IBISMODE = 41 "+
 		"AND scw.pod_ver=expo.pod_ver "+
 		"AND obs.amalg != 'A' "+
		"ORDER by scw_id";

	    ResultSet rset_count = 
		stmt_count.executeQuery("SELECT COUNT(UNIQUE scw.scw_id) "+selectionQuery);

	    rset_count.next();
	    nScw=rset_count.getInt(1);
	    rset_count.close();
	    stmt_count.close();

	    // Query the  observation table
	    if (nScw > 0) {

		Statement stmt1 
		    = conn.createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

		ResultSet scwList = stmt1.executeQuery ("SELECT UNIQUE scw.scw_id "+selectionQuery);


		// Store the SCW ids

		scwList.beforeFirst(); 
		scwIDs = new String [nScw];
		int icount=0;
		while (scwList.next ()) {
		    scwIDs[icount]=scwList.getString(1);
		    icount++;
		}

		//close the result set, statement

		stmt1.close();
		scwList.close();
	    }

	} catch (SQLException e) {
	    System.err.println(e);
	    return;
	}
    }

    public void printSCW(){

	if(nScw > 0) {

	    System.out.println ("OBS " + obsID + " " + nScw);

	    for (int icount=0; icount<nScw; icount++) {
		System.out.println ("SCW "+ icount + ": " + scwIDs[icount]);
	    }
	}
    }

    public String[] getScwIDs() {

	return scwIDs;
    }

   public void writeAsScwListFile(String filename) throws IOException {

	if ( nScw > 0 ) {

	    File file = new File(filename);
	    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
	    String rev = null;
	    String line = null;
	    String sufix = null;
	    int revNum = 0;
	    for ( int i=0; i < scwIDs.length; i++ ) {
		rev = scwIDs[i].substring(0,4);
		revNum = (new Integer(rev)).intValue();
		if ( revNum < 200 ) sufix = "swg_prp.fits[1]";
		else sufix = "swg.fits[1]";
		line = "scw" +sep+ rev +sep+ scwIDs[i]+ ".001" +sep+ sufix;
		pw.println(line);
	    }
	    pw.flush(); 
	    pw.close();
	}

   }

   public static void writeAsScwListFile(String filename, String[] scwIDNums) throws IOException {

       File file = new File(filename);
       PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
       String rev = null;
       String line = null;
       String sufix = null;
       int revNum = 0;
       for ( int i=0; i < scwIDNums.length; i++ ) {
	   rev = scwIDNums[i].substring(0,4);
	   revNum = (new Integer(rev)).intValue();
	   if ( revNum < 200 ) sufix = "swg_prp.fits[1]";
	   else sufix = "swg.fits[1]";
	   line = "scw" +sep+ rev +sep+ scwIDNums[i]+ ".001" +sep+ sufix;
	   pw.println(line);
       }
       pw.flush(); 
       pw.close();
       
   }
    

}
