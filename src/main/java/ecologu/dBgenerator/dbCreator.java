package ecologu.dBgenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;


/**
 *
 * @author Alex & Hugo
 */

public class dbCreator 
{
    private String framework = "derbyclient";
    private String driver = "org.apache.derby.jdbc.ClientDriver";
    private String protocol = "jdbc:derby://localhost:1527/";
    private String DBname = "EcologU_DB";
    
    public void setUpDb (){
        this.loadDriver();
        Connection con = null;
        Statement s = null;

        try
        {
            Properties props = new Properties();
            props.put("user", "root");
            props.put("password", "root");
            System.out.println("Création de la base '" + DBname + "'");
            con = DriverManager.getConnection(protocol + DBname + ";create=true;", props);
            con.setAutoCommit(false);
            s = con.createStatement();
            s.execute("SET SCHEMA APP");
        }
        catch(SQLException sqle)
        {
            printSQLException(sqle);
        }
        finally
        {
            try{
                ScriptRunner runner = new ScriptRunner(con, false, false);
                runner.runScript(new BufferedReader(new FileReader("./setDB.sql")));
            }catch(SQLException sqle)
            {
                if(!(sqle.getErrorCode() == -1 && "X0Y32".equals(sqle.getSQLState())))
                {
                    printSQLException(sqle);
                }
            }
            catch(IOException e)    {e.printStackTrace();}
            
            try{
                ScriptRunner runner = new ScriptRunner(con, false, false);
                runner.runScript(new BufferedReader(new FileReader("./setSecurityTable.sql")));
            }catch(SQLException sqle)
            {
                if(!(sqle.getErrorCode() == -1 && "X0Y32".equals(sqle.getSQLState())))
                {
                    printSQLException(sqle);
                }
            }
            catch(IOException e)    {e.printStackTrace();}
            
            csvLoader loader = new csvLoader(con);
            // Ajouter ICI le code pour peupler la base de consommation d'électricité 
            try{
                loader.loadCSV("./dataelec.csv", "ELECTRICITE", true);
            }catch (Exception e){
                e.printStackTrace();
            }
            // Remplissage de la table configurations
            try{
                loader.loadCSV("./dataConfig.csv", "CONFIGURATIONS", true);
            }catch (Exception e){
                e.printStackTrace();
            }
            // Remplissage de la table chauffage
            try{
                ScriptRunner runner = new ScriptRunner(con, false, false);
                runner.runScript(new BufferedReader(new FileReader("./fillChauffage.sql")));
            }
            catch(SQLException sqle)   {printSQLException(sqle);}
            catch(IOException e)    {e.printStackTrace();}
            // Remplissage de la table notifications
            try{
                loader.loadCSV("./dataNotif.csv", "NOTIFICATIONS", true);
            }catch (Exception e){
                e.printStackTrace();
            }
            
            
            try
            {
                ResultSet rs = s.executeQuery("SELECT TABLENAME FROM SYS.SYSTABLES WHERE TABLENAME NOT LIKE 'SYS%'");
                while(rs.next())
                {
                    System.out.println("Table "+rs.getString("TABLENAME")
                            + " créée avec succès");
                }
                con.commit();
                if(rs != null)
                {
                    rs.close();
                    rs = null;
                }
            }
            catch(SQLException sqle) {printSQLException(sqle);}
            // Décommenter pour afficher le contenu des tables ELECTRICITE et CONFIGURATIONS
            try
            {
                System.out.println("Le système est pré-configuré avec les"
                        + " configurations suivantes:");
                ResultSet rs = s.executeQuery("SELECT * FROM APP.CONFIGURATIONS");
                while(rs.next())
                {
                    System.out.println("[ "+rs.getString("mode")+", "
                            + rs.getString("attribut") + ", "
                            + rs.getString("valeur") + " ]");
                }
                con.commit();
                /*rs = s.executeQuery("SELECT * FROM APP.ELECTRICITE");
                while(rs.next())
                {
                    System.out.println("[ "+rs.getString("heure")+", "
                            + rs.getString("consommation") + " ]");
                }
                con.commit();*/
                if(rs != null)
                {
                    rs.close();
                    rs = null;
                }
            }
            catch(SQLException sqle) {printSQLException(sqle);}
            
            try
            {
                if(s != null)
                {
                    s.close();
                    s = null;
                }
                if(con != null)
                {
                    con.close();
                    con = null;
                }
            }
            catch(SQLException sqle)
            {
                printSQLException(sqle);
            }   
        }
    }
    
    private void loadDriver()
    {
        try
        {
            Class.forName(driver).newInstance();
            System.out.println(driver + " chargé");
        }
        catch(ClassNotFoundException cnfe)
        {
            System.err.println("\nImpossible de charger le JDBC driver " + driver);
            System.err.println("Vérifiez votre CLASSPATH.");
            cnfe.printStackTrace(System.err);
        }
        catch(InstantiationException ie)
        {
            System.err.println("\nImpossible d'instantier le JDBC driver " + driver);
            ie.printStackTrace(System.err);
        }
        catch(IllegalAccessException iae)
        {
            System.err.println("\nAccès non autorisé au JDBC driver " + driver);
            iae.printStackTrace(System.err);
        }
    }
    
    private void reportFailure(String message)
    {
        System.err.println("\nEchec de la vérification des données:");
        System.err.println('\t' + message);
    }
    
    public static void printSQLException(SQLException sqle)
    {
        while(sqle != null)
        {
            System.err.println("\n----- SQLException -----");
            System.err.println("  SQL State:  " + sqle.getSQLState());
            System.err.println("  Error Code: " + sqle.getErrorCode());
            System.err.println("  Message:    " + sqle.getMessage());
            // for stack traces, refer to derby.log or uncomment this:
            //sqle.printStackTrace(System.err);
            sqle = sqle.getNextException();
        }
    }
}
