import java.io.*;
import java.sql.*;
import java.math.*;
import oracle.jdbc.*;
import oracle.jdbc.pool.*;
import oracle.sql.*;


public class ISDAConnect {

   Connection conn;

   public ISDAConnect() {

       // Connect to ORACLE
       
       try {

         OracleDataSource ods = new OracleDataSource();
         ods.setURL("jdbc:oracle:thin:@//intdb01.esac.esa.int:1521/isdapro");
         ods.setUser("archive");
         ods.setPassword("isda_archive");
         conn = ods.getConnection();
       
       } catch (SQLException e) {

          System.err.println(e);
          return;

       }

   }

  public void DisConnect() {

       // Connect to ORACLE
       
       try {

	  conn.close();
       
       } catch (SQLException e) {

          System.err.println(e);
          return;

       }

   }

}
   






        
