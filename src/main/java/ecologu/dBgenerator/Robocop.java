package ecologu.dBgenerator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Hugo
 */

public abstract class Robocop
{
    /** 
      * Turn on built-in user authentication and user authorization. 
      * 
      * @param con a connection to the database.
      */
    public static void turnOnBuiltInUsers(Connection con) throws SQLException
    { 
        System.out.println("Protection de la DB."); 
        try(Statement s = con.createStatement())
        {
            s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
                "'derby.connection.requireAuthentication', 'true')");
            ResultSet rs = s.executeQuery( 
                "VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY(" + 
                "'derby.connection.requireAuthentication')"); 
            rs.next(); 
            System.out.println("requireAuthentication="+rs.getString(1)); 
            // Setting authentication scheme to Derby 
            s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
                "'derby.authentication.provider', 'BUILTIN')"); 

            // Creating some sample users 
            s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
                "'derby.user.root', 'root')"); 

            // Setting default connection mode to no access 
            // (user authorization) 
            s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
                "'derby.database.defaultConnectionMode', 'noAccess')"); 
            // Confirming default connection mode 
            rs = s.executeQuery (
                "VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY(" + 
                "'derby.database.defaultConnectionMode')"); 
            rs.next(); 
            System.out.println("defaultConnectionMode="+rs.getString(1)); 

            // Defining read-write users 
            s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
                "'derby.database.fullAccessUsers', 'root')");

            // Confirming full-access users 
            rs = s.executeQuery(
                "VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY(" + 
                "'derby.database.fullAccessUsers')"); 
            rs.next(); 
            System.out.println("fullAccessUsers="+rs.getString(1)); 

            //we would set the following property to TRUE only 
            //when we were ready to deploy. 
            s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
                "'derby.database.propertiesOnly', 'false')");
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
      * Turn off built-in user authentication and user authorization. 
      * 
      * @param con a connection to the database.
      */
    public static void turnOffBuiltInUsers(Connection con) throws SQLException { 
        try(Statement s = con.createStatement())
        {
            System.out.println("Suppression de la protection de la DB."); 

            s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
                "'derby.connection.requireAuthentication', 'false')"); 
            s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
                "'derby.authentication.provider', null)"); 
            s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
                "'derby.user.root', null)"); 
            s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
                "'derby.database.defaultConnectionMode', 'fullAccess')"); 
            s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
                "'derby.database.fullAccessUsers', null)");  
            s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + 
                "'derby.database.propertiesOnly', 'false')"); 

            // Confirming requireAuthentication 
            ResultSet rs = s.executeQuery(
                "VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY(" + 
                "'derby.connection.requireAuthentication')"); 
            rs.next(); 
            System.out.println("requireAuthentication="+rs.getString(1)); 

            // Confirming default connection mode 
            rs = s.executeQuery(
                "VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY(" + 
                "'derby.database.defaultConnectionMode')"); 
            rs.next(); 
            System.out.println("defaultConnectionMode="+rs.getString(1)); 
            System.out.println("DB non protégée.");
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
