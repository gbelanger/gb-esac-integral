import java.io.*;
import java.util.Arrays;
import java.sql.*;
import java.math.*;
import java.awt.List;
import oracle.jdbc.*;
import oracle.jdbc.pool.*;
import oracle.sql.*;

// The ISDAExample class will print the observations in the database 

public class GetSingleScwList {

    public static void main (String[] args)  throws IOException {

	if ( args.length != 1 ) {
	    System.out.println("Usage: java GetScwList propID");
	    System.exit(-1);
	}
	String propID = args[0];

	
	// Connect to ORACLE
	ISDAConnect isdaConnection = new ISDAConnect();


	// Get observation data 
	ObsData allObsData= new ObsData(isdaConnection.conn);


	//  Get the scw data for the specified propID
	List allScwsList = new List();
	ScwData currentScwData = null;
	String previousPropID = allObsData.obsIDs[0].substring(0,7);
	String thisPropID = allObsData.obsIDs[0].substring(0,7);
	int nScwsInObs = 0;
	int tot = 0;
	int i = 0; 

	while ( i < allObsData.nObs ) {

	    if ( thisPropID.equals(propID) ) {
	    
		//  Loop over all obsIDs with the same propID
		while ( i < allObsData.nObs && thisPropID.equals(propID) ) {		    
		
		    //  Get the scw IDs
		    currentScwData= new ScwData(isdaConnection.conn, allObsData.obsIDs[i]);
		    String[] scws = currentScwData.getScwIDs();
		    nScwsInObs = currentScwData.nScw;
		
		    //  Append to allScwsList
		    if ( nScwsInObs > 0 ) {
		    
			for ( int j=0; j < scws.length; j++ ) {
			    allScwsList.add(scws[j]);
			}
		    }
		
		    //  Go to the next obsID
		    previousPropID = thisPropID;
		    i++;
		    if ( i < allObsData.nObs ) {
			thisPropID = allObsData.obsIDs[i].substring(0,7);
		    }
		}

		//  Print scw list to file
		String[] allScws = allScwsList.getItems();
		Arrays.sort(allScws);
		if ( allScws.length > 0 ) {
		    String filename = previousPropID+"_scw.lis";
		    ScwData.writeAsScwListFile(filename, allScws);
		    allScwsList.removeAll();
		    tot += allScws.length;
		    System.out.println(previousPropID+"\t"+allScws.length+"\t"+tot);
		}

		//break;

	    }
	    else {
		//  Go to the next obsID
		previousPropID = thisPropID;
		i++;
		if ( i < allObsData.nObs ) {
		    thisPropID = allObsData.obsIDs[i].substring(0,7);
		}
	    }

	}


	// Close ORACLE Connection
	isdaConnection.DisConnect(); 
    }
}
