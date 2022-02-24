package dbapp.ms;

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
   
   // CREATE PROCEDURE dbo.getAveragePoint
   // AS
   // BEGIN
   //    SET NOCOUNT ON;
   //
   //    SELECT ROUND(SUM(GradePoint * CreditPoint)/SUM(CreditPoint), 2) AS AveragePoint
   //    FROM STUDENT_TO_CLASS
   //    WHERE GradePoint IS NOT NULL AND StudentNumber=1 AND YearTerm='2017-1'
   // END
   // GO
   //
   // EXEC getAveragePoint
   private static void demoCallableStatement(Connection conn) throws Exception {      
      String command = "{ call getAveragePoint }";
      
      CallableStatement cstmt = conn.prepareCall(command);      

      cstmt.execute();
      ResultSet rs = cstmt.getResultSet();   // get a single double value.
      if(rs.next()) {
         System.out.println("AveragePoint(CallableStatement, No Argument)");
         System.out.println("------------");
         System.out.println(rs.getDouble("AveragePoint"));
         System.out.println("------------");
      }
      
      rs.close();
      cstmt.close();
   }

   // CREATE PROCEDURE dbo.getAveragePointOUT
   //    @averagePoint decimal(4, 2) OUT
   // AS
   // BEGIN
   //    SET NOCOUNT ON;
   //
   //    SELECT @averagePoint = ROUND(SUM(GradePoint * CreditPoint)/SUM(CreditPoint), 2)
   //    FROM STUDENT_TO_CLASS
   //    WHERE GradePoint IS NOT NULL AND StudentNumber=1 AND YearTerm='2017-1'
   // END
   // GO
   //
   // DECLARE @result decimal(4,2)
   // EXEC getAveragePointOUT @averagePoint = @result OUTPUT
   // SELECT @result AS AveragePoint;   
   private static void demoCallableStatementOUT(Connection conn) throws Exception {      
      String command = "{ call getAveragePointOUT(?) }";
      
      CallableStatement cstmt = conn.prepareCall(command);      
      cstmt.registerOutParameter("averagePoint", Types.DECIMAL);
      cstmt.execute();
      double result = cstmt.getDouble("averagePoint");   // get a single double value.
      
      System.out.println("AveragePoint(CallableStatement, OUT Argument)");
      System.out.println("------------");
      System.out.println(result);
      System.out.println("------------");

      cstmt.close();
   }
   
   // CREATE PROCEDURE dbo.getAveragePointIN
   //    @studentNumber int,
   //    @yearTerm char(6)
   // AS
   // BEGIN
   //    SET NOCOUNT ON;
   //
   //    SELECT ROUND(SUM(GradePoint * CreditPoint)/SUM(CreditPoint), 2)
   //    FROM STUDENT_TO_CLASS
   //    WHERE GradePoint IS NOT NULL AND StudentNumber=@studentNumber AND YearTerm=@yearTerm
   // END
   // GO
   //
   // EXEC getAveragePointIN @studentNumber = 1, @yearTerm = '2017-1' 
   private static void demoCallableStatementIN(Connection conn) throws Exception {      
      String command = "{ call getAveragePointIN(?, ?) }";
      
      CallableStatement cstmt = conn.prepareCall(command);      
      cstmt.setInt("studentNumber", 1);
      cstmt.setString("yearTerm", "2017-1");      
      cstmt.execute();
      
      ResultSet rs = cstmt.getResultSet();   // get a single double value.
      if(rs.next()) {
         System.out.println("AveragePoint(CallableStatement, IN Argument)");
         System.out.println("------------");
         System.out.println(rs.getDouble(1));
         System.out.println("------------");
      }
      
      rs.close();
      cstmt.close();
   }
   
   // CREATE PROCEDURE dbo.getAveragePointINOUT
   //    @studentNumber int,
   //    @yearTerm char(6),
   //    @averagePoint decimal(4, 2) OUT
   // AS
   // BEGIN
   //    SET NOCOUNT ON;
   //
   //    SELECT @averagePoint = ROUND(SUM(GradePoint * CreditPoint)/SUM(CreditPoint), 2)
   //    FROM STUDENT_TO_CLASS
   //    WHERE GradePoint IS NOT NULL AND StudentNumber=@studentNumber AND YearTerm=@yearTerm
   // END
   // GO
   //
   // DECLARE @gpa decimal(4,2)
   // EXEC getAveragePointINOUT 1, '2017-1', @averagePoint = @gpa OUTPUT
   // SELECT @gpa AS AveragePoint;  
   private static void demoCallableStatementINOUT(Connection conn) throws Exception {      
      String command = "{ call getAveragePointINOUT(?, ?, ?) }";
      
      CallableStatement cstmt = conn.prepareCall(command);      
      cstmt.setInt("studentNumber", 1);
      cstmt.setString("yearTerm", "2017-1");  
      cstmt.registerOutParameter("averagePoint", Types.DECIMAL);
      cstmt.execute();
      
      double result = cstmt.getDouble("averagePoint");   // get a single double value.
      System.out.println("AveragePoint(CallableStatement, IN & OUT Argument)");
      System.out.println("------------");
      System.out.println(result);
      System.out.println("------------");

      cstmt.close();
   }

   // CREATE PROCEDURE dbo.getAveragePointResultTable
   //    @yearTerm char(6)
   // AS
   // BEGIN
   //    SET NOCOUNT ON;
   //             
   //    SELECT StudentNumber, StudentName, SUM(GradePoint * CreditPoint)/SUM(CreditPoint) AS AveragePoint
   //    FROM STUDENT_TO_CLASS
   //    WHERE GradePoint IS NOT NULL AND YearTerm = @yearTerm
   //    GROUP BY StudentNumber, StudentName
   //    ORDER BY AveragePoint DESC;
   // END
   // GO 
   //
   // EXEC getAveragePointResultTable '2017-1'
   private static void demoCallableStatementResultTable(Connection conn) throws Exception {      
      String command = "{ call getAveragePointResultTable(?) }";
      ResultSet rs = null;
      
      CallableStatement cstmt = conn.prepareCall(command, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);      
      cstmt.setString("yearTerm", "2017-1");
      boolean results = cstmt.execute();
      int rowsAffected = 0;
      
      // Protects against lack of 'SET NOCOUNT ON' in stored procedure.
      // No needed if 'SET NOCOUNT ON' is declared in the stored procedure.
      while(results || rowsAffected != -1) {
         if(results) {
            rs = cstmt.getResultSet();
            break;
         } else {
            rowsAffected = cstmt.getUpdateCount();
         }
         
         results = cstmt.getMoreResults();
      }
      
      // main loop for record processing.
      System.out.println("StudentNumber\tStudentName\t\tAveragePoint");
      System.out.println("-----------------------------------------------------");
      while (rs.next()) {
        int sNo = rs.getInt("StudentNumber");         // StudentNumber
        String sName = rs.getString("StudentName");   // StudentName 
        double avgPt = rs.getDouble("AveragePoint");  // AveragePoint
        System.out.print(sNo + "\t\t");
        System.out.print(sName + "\t");
        System.out.println(avgPt);
      }
      System.out.println("-----------------------------------------------------");

      rs.close();
      cstmt.close();
   }
   
   public static void main(String[] args) throws Exception {
      String url = "jdbc:sqlserver://dbsme.pknu.ac.kr:1433;DatabaseName=" + args[0];   // dbname 
      Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
      
      Connection conn = DriverManager.getConnection(url, args[1], args[2]);   // username & password
      
      demoStaticStatement(conn);              // 1. static sql statement.
      
      demoPreparedStatement(conn);            // 2. prepared statement.
      
      demoPreparedStatementWithInput(conn);   // 3. prepared statement with input parameters.
      
      demoCallableStatement(conn);            // 4. callable statement.
      
      demoCallableStatementOUT(conn);         // 5. callable statement with output parameter.
      
      demoCallableStatementIN(conn);          // 6. callable statement with input parameters.
      
      demoCallableStatementINOUT(conn);       // 7. callable statement with input & output parameters.
      
      demoCallableStatementResultTable(conn); // 8. callable statement with input parameters and return ResultSet table.
      
      conn.close();
   }
}
