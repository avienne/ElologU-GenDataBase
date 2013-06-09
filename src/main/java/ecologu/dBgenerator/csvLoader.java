package ecologu.dBgenerator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.apache.commons.lang3.StringUtils;
import au.com.bytecode.opencsv.CSVReader;
import java.io.IOException;
import java.sql.SQLException;

/**
 * 
 * @author viralpatel.net
 */
public class csvLoader
{
    private static final String SQL_INSERT = "INSERT INTO ${table}(${keys}) VALUES(${values})";
    private static final String TABLE_REGEX = "\\$\\{table\\}";
    private static final String KEYS_REGEX = "\\$\\{keys\\}";
    private static final String VALUES_REGEX = "\\$\\{values\\}";
 
    private Connection connection;
    private char seprator;
 
    /**
     * Public constructor to build CSVLoader object with
     * Connection details. The connection is closed on success
     * or failure.
     * @param connection
     */
    public csvLoader(Connection connection)
    {
        this.connection = connection;
        //Set default separator
        this.seprator = ';';
    }
     
    /**
     * Parse CSV file using OpenCSV library and load in 
     * given database table. 
     * @param csvFile Input CSV file
     * @param tableName Database table name to import data
     * @param truncateBeforeLoad Truncate the table before inserting 
     *          new records.
     * @throws Exception
     */
    public void loadCSV(String csvFile, String tableName,
            boolean truncateBeforeLoad) throws Exception
    {
        CSVReader csvReader = null;
        if(this.connection == null)
        {
            throw new Exception("Not a valid connection.");
        }
        try
        {
            csvReader = new CSVReader(new FileReader(csvFile), this.seprator);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new Exception("Error occured while executing file. " + e.getMessage());
        }
 
        String[] headerRow = csvReader.readNext();
 
        if(headerRow == null)
        {
            throw new FileNotFoundException(
                    "No columns defined in given CSV file." +
                    "Please check the CSV file format.");
        }
 
        String questionmarks = StringUtils.repeat("?,", headerRow.length);
        questionmarks = (String) questionmarks.subSequence(0, questionmarks
                .length() - 1);
 
        String query = SQL_INSERT.replaceFirst(TABLE_REGEX, tableName);
        query = query.replaceFirst(KEYS_REGEX, StringUtils.join(headerRow, ","));
        query = query.replaceFirst(VALUES_REGEX, questionmarks);
 
        //System.out.println("Query: " + query);
 
        String[] nextLine;
        Connection con = null;
        PreparedStatement ps = null;
        try
        {
            con = this.connection;
            con.setAutoCommit(false);
            //ps = con.prepareStatement("INSERT INTO ELECTRICITE VALUES(?,?)");
            ps = con.prepareStatement(query); //c'est mieux comme ça ;)
            if(truncateBeforeLoad)
            {
                //delete data from table before loading csv
                con.createStatement().execute("DELETE FROM " + tableName);
            }
 
            //final int batchSize = 1000;
            //int count = 0;
            //Date date = null;
            while((nextLine = csvReader.readNext()) != null)
            {
                
                //ps.setString(1,nextLine[0]);
                //ps.setString(2,nextLine[1]);
                for(int i=0; i<headerRow.length; i++)
                {
                    ps.setString(i+1, nextLine[i]);
                }
                ps.execute();
                con.commit();
            }
        }
        catch(SQLException e)
        {
            con.rollback();
            e.printStackTrace();
            throw new Exception("Error occured while loading data from file to database."+ e.getMessage());
        }
         catch (IOException e)
         {
            con.rollback();
            e.printStackTrace();
            throw new Exception("Error occured while loading data from file to database."+ e.getMessage());
        }
        finally
        {
            if(ps != null)
            {
                ps.close();
                ps = null;
            }
            // j'ai commenté ça car c'est pas le csvLoader qui ouvre la connection.
            // je vois pas pourquoi ce serait lui qui la fermerait...
            /*if(con != null)
            {
              con.close();
              con = null;
            }*/
 
            csvReader.close();
        }
    }
 
    public char getSeprator()
    {
        return seprator;
    }
 
    public void setSeprator(char seprator)
    {
        this.seprator = seprator;
    }
}
