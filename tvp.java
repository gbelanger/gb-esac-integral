package gb.esac.integral;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;

public class tvp {

    public static void main(String[] args) throws Exception {

	if ( args.length != 6 ) {
	    System.out.println("Usage: java gb.esac.integral.tvp ra dec dither (R/H/S) startDate endDate minVisibility_ks");
	    System.out.println("Example: java gb.esac.integral.tvp 299.6 35.2 R 15-07-2017 15-08-2017 50");
	    System.exit(-1);
	}

	String ra = args[0];
	String dec = args[1];
	String dither = args[2];
	String start = args[3];
	String end = args[4];
	String minContinuousVisibility = args[5];

	String tvpURL = "http://integral.esac.esa.int/isocweb/tvp.html";
	String completeURL = tvpURL.concat("?action=predict");
	completeURL = completeURL.concat("&ra="+ra);
	completeURL = completeURL.concat("&dec="+dec);
	completeURL = completeURL.concat("&dither="+dither);
	completeURL = completeURL.concat("&startDate="+start);
	completeURL = completeURL.concat("&endDate="+end);
	completeURL = completeURL.concat("&duration="+minContinuousVisibility);
	BufferedReader r = new BufferedReader(new InputStreamReader(new URL(completeURL).openStream()));
	//System.out.println(completeURL);
	
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
	
	//  Tokenize to drop the leading white spaces
	StringTokenizer st = new StringTokenizer(s);
	String totalVisibility = st.nextToken();
	System.out.println(totalVisibility);
    }
}

