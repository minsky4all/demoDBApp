package dbapp.ms;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.sql.*;

class FetchListener implements ActionListener {
   JTable table;
   String dbName;
   String userName;
   String password;

   FetchListener(JTable table, String db, String user, String pw) {
      this.table = table;
      this.dbName = db;
      this.userName = user;
      this.password = pw;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      DefaultTableModel model = (DefaultTableModel) table.getModel();
      model.setRowCount(0); // remove all rows from the table data.

      Connection conn = null;
      Statement stmt = null;

      try {
         Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
         conn = DriverManager.getConnection("jdbc:sqlserver://dbsme.pknu.ac.kr:1433;DatabaseName=" + dbName + ";user=" + userName + ";password=" + password);
         stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT StudentNumber, StudentName, MajorDepartment FROM STUDENT;");
         while (rs.next()) {
            String arr[] = new String[3];
            arr[0] = "" + rs.getInt("StudentNumber");
            arr[1] = toUnicode(rs.getString("StudentName"));
            arr[2] = toUnicode(rs.getString("MajorDepartment"));
            model.addRow(arr);
         }
      } catch (Exception ex) {
         ex.printStackTrace();
      } finally {
         try {
            stmt.close();
            conn.close();
         } catch (Exception ignored) {
         }
      }
   }

   private String toUnicode(String str) throws java.io.UnsupportedEncodingException {
      return new String(str.getBytes("EUC-kr"));
   }
}
