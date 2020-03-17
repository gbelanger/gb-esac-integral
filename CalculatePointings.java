package gb.esac.integral;

import java.awt.geom.Point2D; 
import gb.esac.tools.SunPosition;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import com.mhuss.AstroLib.*;
import com.mhuss.Util.*;


public class CalculatePointings {


    static Logger logger = Logger.getLogger(CalculatePointings.class);


    public static void main (String[] args) throws NoInitException {


	PropertyConfigurator.configure("/Users/gbelanger/javaProgs/gb/esac/logger.config");

	//  Define the precise time
	int year = 2010;
	int month = 12;
	int day = 1;
	int hours = 0;
	int min = 0;
	int sec = 0;
	double fracOfDay = hours/24.0 + min/1440.0 + sec/86400.0;


	//  Get the RA and Dec of the Sun
	AstroDate astroDate = new AstroDate(year, month, day, fracOfDay);
	double jd = astroDate.jd();
	Planets planets = new Planets();
	ObsInfo obsInfo = new ObsInfo();
	PlanetData planetData = new PlanetData(planets.SUN, jd, obsInfo);
	double ra = planetData.getRightAscension();
	double dec = planetData.getDeclination();
	    
	logger.info("RA = "+ra+"\t Dec = "+dec);

	Point2D.Double sunRaDec = SunPosition.getSunRaDec(year, month, day, hours, min, sec);
	double ra2 =  sunRaDec.getX();
	double dec2 = sunRaDec.getY();
	
	logger.info("RA = "+ra2+"\t Dec = "+dec2);

    }


}