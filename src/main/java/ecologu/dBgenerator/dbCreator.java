package ecologu.dBgenerator;

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
 * @author Alex
 */

public class dbCreator 
{
    private String framework = "derbyclient";
    private String driver = "org.apache.derby.jdbc.ClientDriver";
    private String protocol = "jdbc:derby://localhost:1527/";
    private String DBname = "EcologU_DB2";
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
            for(int i=0; i<tables.size(); i++)
            {
                try
                {
                    s.execute("CREATE TABLE APP." + (String)this.tables.get(i)
                        + "(heure char(40) not null primary key,"
                            + "consommation char(10))");
                    con.commit();
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
                s.execute("CREATE TABLE APP.configurations "
                            + "(mode varchar(8) not null,"
                            + "attribut varchar(15) not null,"
                            + "valeur varchar(60),"
                            + "primary key(mode, attribut))");
                con.commit();
            }catch(SQLException sqle)
            {
                if(!(sqle.getErrorCode() == -1 && "X0Y32".equals(sqle.getSQLState())))
                {
                    printSQLException(sqle);
                }
            }
            
            try{
                s.execute("CREATE TABLE APP.notifications "
                            + "(id int not null primary key "
                            + "GENERATED ALWAYS AS IDENTITY"
                            + "(START WITH 1, INCREMENT BY 1),"
                            + "gravite varchar(10) not null,"
                            + "heure char(22) not null,"
                            + "action varchar(20) not null,"
                            + "equipement varchar(100) not null)");
                con.commit();
            }catch(SQLException sqle)
            {
                if(!(sqle.getErrorCode() == -1 && "X0Y32".equals(sqle.getSQLState())))
                {
                    printSQLException(sqle);
                }
            }
            
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
            
            // Décommenter pour afficher le contenu des tables ELECTRICITE et CONFIGURATIONS
            /*
            try
            {
                ResultSet rs = s.executeQuery("SELECT * FROM APP.ELECTRICITE");
                while(rs.next())
                {
                    System.out.println("[ "+rs.getString("heure")+", "
                            + rs.getString("consommation") + " ]");
                }
                rs = s.executeQuery("SELECT * FROM APP.CONFIGURATIONS");
                while(rs.next())
                {
                    System.out.println("[ "+rs.getString("mode")+", "
                            + rs.getString("attribut") + ", "
                            + rs.getString("valeur") + " ]");
                }
                if(rs != null)
                {
                    rs.close();
                    rs = null;
                }
            }
            catch(SQLException sqle) {printSQLException(sqle);}
            */
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
