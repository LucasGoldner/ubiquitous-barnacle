package folder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;

public class Main {
    // JDBC driver name and database URL 

    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:~/test";

    //  Database credentials 
    static final String USER = "sa";
    static final String PASS = "";

    public static void createTables(Statement stmt) throws SQLException {
        String sql1 = "CREATE TABLE CLASS_NAME ("
                + "name TEXT )";
        stmt.executeUpdate(sql1);
        String sql2 = "CREATE TABLE METHOD_DECLARATION ("
                + "class_name TEXT, "
                + "return_type TEXT, "
                + "name TEXT, "
                + "parameters_size INT, "
                + "parameters TEXT )";
        stmt.executeUpdate(sql2);
        String sql3 = "CREATE TABLE FIELD_DECLARATION ("
                + "class_name TEXT, "
                + "return_type TEXT, "
                + "name TEXT )";

        stmt.executeUpdate(sql3);
    }
    
    public static List<String> getJavaClasses() throws IOException {
        BufferedReader buffRead = new BufferedReader(new FileReader("C:\\Users\\lucas\\OneDrive\\Documentos\\NetBeansProjects\\h2\\src\\main\\java\\folder\\classes.txt"));
        String linha = "";
        
        List<String> list = new ArrayList<>();
                
        while (true) {
            if(linha != null)
                list.add(linha);
            
            else
               break;
            
            linha = buffRead.readLine();
        }
        
        buffRead.close();
        
        return list;
    }
    
    public static void getJavaObject(String name, Statement stmt) throws ClassNotFoundException, SQLException {
        Class clazz = Class.forName(name);
        
        for(Method method: clazz.getMethods()) {
            List<String> parameters = new ArrayList<>();
            for(Class param : method.getParameterTypes())
                parameters.add(param.getCanonicalName());
           
            
            stmt.executeUpdate("INSERT INTO METHOD_DECLARATION VALUES"
                    + "('" + name + "',"
                    + "'" + method.getReturnType().getCanonicalName() + "',"
                    + "'" + method.getName() + "',"
                    + "'" + parameters.size() + "',"
                    + "'" + (new JSONArray(parameters)).toString().replace("\"", "''") + "')");
        }
    }
    
    public static void dropTables(Statement stmt) throws SQLException {
        String sql1 = "DROP TABLE CLASS_NAME";
        stmt.executeUpdate(sql1);
        String sql2 = "DROP TABLE METHOD_DECLARATION";
        stmt.executeUpdate(sql2);
        String sql3 = "DROP TABLE FIELD_DECLARATION";
        stmt.executeUpdate(sql3);
    }
    
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            stmt = conn.createStatement();
            //dropTables(stmt);
            createTables(stmt);

            for(String clazz : getJavaClasses())
                stmt.executeUpdate("INSERT INTO CLASS_NAME VALUES ('" + clazz + "')");
            
            List<String> list = new ArrayList<>();
            ResultSet rs = stmt.executeQuery("SELECT * FROM CLASS_NAME WHERE name LIKE '%java.lang%'");
            while(rs.next()) {
                list.add(rs.getString("name"));
            }
            
            for(String str : list)
                getJavaObject(str, stmt);

            
            rs = stmt.executeQuery("SELECT * FROM METHOD_DECLARATION WHERE class_name = 'java.lang.String' AND parameters_size = 2");
            while(rs.next()) {
                System.out.println(rs.getString("class_name") +
                        " " + rs.getString("return_type") +
                        " " + rs.getString("name") + 
                        " " + new JSONArray(rs.getString("parameters")));
            }
            
            dropTables(stmt);
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC 
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName 
            e.printStackTrace();
        } finally {
            //finally block used to close resources 
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
            } // nothing we can do 
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            } //end finally try 
        } //end try 
        System.out.println("Goodbye!");
    }
}
