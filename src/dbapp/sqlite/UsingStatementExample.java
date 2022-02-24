package dbapp.sqlite;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

public class UsingStatementExample {
   private static void demoStaticStatement(Connection conn) throws Exception {
      Statement stmt = conn.createStatement();
      String sql = "SELECT StudentNumber, YearTerm, SUM(GradePoint * CreditPoint)/SUM(CreditPoint) AS AveragePoint"
            + " FROM STUDENT_TO_CLASS"
            + " WHERE GradePoint IS NOT NULL"
            + " GROUP BY StudentNumber, YearTerm"
            + " ORDER BY StudentNumber, YearTerm";
      
      ResultSet rs = stmt.executeQuery(sql);
      
      System.out.println("StudentNumber\tYearTerm\tAveragePoint");
      System.out.println("-------------------------------------------");
      while (rs.next()) {
        int sNo = rs.getInt(1);         // StudentNumber
        String yrTr = rs.getString(2);  // YearTerm 
        double avgPt = rs.getDouble(3); // AveragePoint
        System.out.print(sNo + "\t\t");
        System.out.print(yrTr + "\t\t");
        System.out.println(avgPt);
      }
      System.out.println("-------------------------------------------");
      
      rs.close();
      stmt.close();
   }

   private static void demoPreparedStatement(Connection conn) throws Exception {      
      String sql = "SELECT ROUND(SUM(GradePoint * CreditPoint)/SUM(CreditPoint), 2) AS AveragePoint"
            + " FROM STUDENT_TO_CLASS"
            + " WHERE GradePoint IS NOT NULL AND StudentNumber=1 AND YearTerm='2017-1'";
      
      PreparedStatement pstmt = conn.prepareStatement(sql);
      
      // get a single double value.
      ResultSet rs = pstmt.executeQuery();
      if(rs.next()) {
         System.out.println("AveragePoint(PreparedStatement, No Argument)");
         System.out.println("------------");
         System.out.println(rs.getDouble("AveragePoint"));
         System.out.println("------------");
      }
      
      rs.close();
      pstmt.close();
   }

   private static void demoPreparedStatementWithInput(Connection conn) throws Exception {      
      String sql = "SELECT ROUND(SUM(GradePoint * CreditPoint)/SUM(CreditPoint), 2) AS AveragePoint"
            + " FROM STUDENT_TO_CLASS"
            + " WHERE GradePoint IS NOT NULL AND StudentNumber=? AND YearTerm=?";
      
      PreparedStatement pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, 1);             // for StudentNumber = 1.
      pstmt.setString(2, "2017-1");   // for YearTerm = '2017-1'.
      
      // get a single double value.
      ResultSet rs = pstmt.executeQuery();
      if(rs.next()) {
         System.out.println("AveragePoint(PreparedStatement, With Arguement)");
         System.out.println("------------");
         System.out.println(rs.getDouble("AveragePoint"));
         System.out.println("------------");
      }
      
      rs.close();
      pstmt.close();
   }
   
   public static void main(String[] args) throws Exception {
      String url = "jdbc:sqlite:" + args[0];   // dbfilename 
      Class.forName("org.sqlite.JDBC");
      
      Connection conn = DriverManager.getConnection(url);   // neither username nor password is needed.
      
      demoStaticStatement(conn);              // 1. static sql statement.
      
      demoPreparedStatement(conn);            // 2. prepared statement.
      
      demoPreparedStatementWithInput(conn);   // 3. prepared statement with input parameters.
      
      // sadly, sqlite does not support stored procedure!
      conn.close();
   }
}
