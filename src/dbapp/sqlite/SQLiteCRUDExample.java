package dbapp.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

public class SQLiteCRUDExample {
   public static void main(String[] args) throws Exception {
      // args[0] = dbfilename
      String connectionUrl = "jdbc:sqlite:" + args[0];

      Class.forName("org.sqlite.JDBC");
      Connection conn = DriverManager.getConnection(connectionUrl);

      Statement stmt = conn.createStatement();
      String sql = "CREATE TABLE MOVIES (" +
        " Title char(50) NOT NULL," +
        " Hero char(30) NULL," +
        " Invest int NULL," +
        " Revenue numeric(10, 2) NULL," +
        " CONSTRAINT MOVIES_PK PRIMARY KEY (Title))";

      stmt.executeUpdate(sql);
      
      String[] titles = { "Jason Bourne", "The Book Thief", "Far and Away", "John Wick", "Contact (1997)" };
      String[] heroes = { "Matt Damon", "Sophie Nelisse", "Tom Cruise", "Keanu Reeves", "Jodie Foster" };
      int[] invest = { 40204, 60124, 0, 90240, 0 };      
      double[] revenue = { 0.0, 120324.7, 0.0, 0.0, 184214.5 };
      
      PreparedStatement pstmt = conn.prepareStatement("INSERT INTO MOVIES VALUES (?, ?, ?, ?)");
      for(int i = 0; i < 5; i++) {
         pstmt.setString(1, titles[i]);
         pstmt.setString(2, heroes[i]);
         // Invest
         if(invest[i] != 0)
            pstmt.setInt(3, invest[i]);
         else
            pstmt.setNull(3, Types.INTEGER);
         // Revenue
         if(revenue[i] != 0.0)
            pstmt.setDouble(4, revenue[i]);
         else
            pstmt.setNull(4, Types.DOUBLE);
         
         pstmt.execute();  
      }
      
      sql = "SELECT * FROM MOVIES"; 
      ResultSet rs = stmt.executeQuery(sql);    

      System.out.println("Title\t\tHero\t\tInvest\tRevenue");
      System.out.println("------------------------------------------------");
      while(rs.next()) {
         System.out.print(rs.getString("Title").trim() + "\t");
         System.out.print(rs.getString("Hero").trim() + "\t"); 
         System.out.print(rs.getInt("Invest") + "\t");
         System.out.println(rs.getDouble("Revenue"));          
      }
      System.out.println("------------------------------------------------");
      
      sql = "DELETE FROM MOVIES";
      int count = stmt.executeUpdate(sql);
      
      stmt.executeUpdate("DROP TABLE MOVIES");
      System.out.println(count + " records removed,\n MOVIES Table dropped.");
      
      rs.close();  
      stmt.close();
      conn.close();
   }
}
