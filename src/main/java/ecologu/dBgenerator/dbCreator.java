package ecologu.dBgenerator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;


/**
 *
 * @author Alex
 */

public class dbCreator 
{
    private String framework = "derbyclient"; //"embedded";
    private String driver = "org.apache.derby.jdbc.ClientDriver"; //"org.apache.derby.jdbc.EmbeddedDriver";
    private String protocol = "jdbc:derby://localhost:1527/"; //"jdbc:derby:";
    private String DBname = "EcologU_DB";
    private ArrayList<String> tables = new ArrayList<String>()
    {
        {
            add("chauffage");
            add("eclairage");
            add("eau");
            add("ventillation");
            add("electricite");
        }
    };
    //private csvLoader csv; 
    
    public void setUpDb (){
        this.loadDriver();
        Connection con = null;
        Statement s = null;
        
        try
        {
            //uncomment to add a user and an authenticated connection
            Properties props = new Properties();
            props.put("user", "root");
            props.put("password", "root");
            System.out.println("Création de la base '" + DBname + "'");
            con = DriverManager.getConnection(protocol + DBname + ";create=true;", props);
            s = con.createStatement();
            s.execute("SET SCHEMA APP");
        }
        catch(SQLException sqle)
        {
            printSQLException(sqle);
        }
        finally
        {
            for(int i=0; i<tables.size(); i++)
            {
                try
                {
                    s.execute("CREATE TABLE APP." + (String)this.tables.get(i)
                        + "(heure char(40) not null primary key,"
                            + "consommation char(10))");
                }
                catch(SQLException sqle)
                {
                    if(!(sqle.getErrorCode() == -1 && "X0Y32".equals(sqle.getSQLState())))
                    {
                        printSQLException(sqle);
                    }
                }
                System.out.println("Création de la table '" +  this.tables.get(i) + "'");
            }

            try{
                s.execute( "CREATE TABLE APP.configurations "
                            + "(mode char(10) not null,"
                            + "attribut char(20) not null,"
                            + "valeur varchar(60),"
                            + "primary key(mode,attribut))");
                con.commit();
            }catch(SQLException sqle)
            {
                    printSQLException(sqle);
            }



            // Ajouter ICI le code pour peupler la base de consommation d'électricité 
            try{
                csvLoader loader = new csvLoader(con);
                loader.setSeprator(';');
                loader.loadCSV("./dataelec.csv","ELECTRICITE",true);
            }catch (Exception e){
                e.printStackTrace();
            }
            try
            {
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

