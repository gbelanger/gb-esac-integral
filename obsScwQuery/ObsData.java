import java.io.*;
import java.sql.*;
import java.math.*;
import oracle.jdbc.*;
import oracle.jdbc.pool.*;
import oracle.sql.*;


public class ObsData {

    public static int nObs;           // Number of observation in database
    public String[] obsIDs;      // Array containing Observation ID   

    public ObsData(Connection conn){ 

	try {

	    // Get the number of observations 

	    Statement stmt_count = conn.createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);      
	    ResultSet rset_count = stmt_count.executeQuery("SELECT COUNT(UNIQUE obs_id) FROM obs");
	    rset_count.next();
	    nObs=rset_count.getInt(1);
	    rset_count.close();
	    stmt_count.close();

	    // Query the  observation table

	    Statement stmt1 = conn.createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);      
	    ResultSet obsList = stmt1.executeQuery ("SELECT UNIQUE obs_id FROM obs ORDER BY obs_id");
 
	    // Store the observation ids

	    obsList.beforeFirst(); 
	    obsIDs = new String [nObs];
	    int icount=0;
	    while (obsList.next ()) {
		obsIDs[icount]=obsList.getString(1);
		icount++;
	    }

	    //close the result set, statement

	    stmt1.close();
	    obsList.close();

	} catch (SQLException e) {
	    System.err.println(e);
	    return;
	}
    }


    public void printObs(){

	// Print the obsnos

	for (int icount=0; icount<nObs; icount++) {
	    System.out.println ("OBS "+ icount + ":" + obsIDs[icount]);
	}



    }

}    







        
