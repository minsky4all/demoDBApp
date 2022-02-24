package dbapp.sqlite;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class FetchListener implements ActionListener {
   JTable table;
   String dbFileName;

   FetchListener(JTable table, String fName) {
      this.table = table;
      this.dbFileName = fName;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      DefaultTableModel model = (DefaultTableModel) table.getModel();
      model.setRowCount(0); // remove all rows from the table data.

      Connection conn = null;
      Statement stmt = null;

      try {
         Class.forName("org.sqlite.JDBC");
         conn = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);
         stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT ClassNumber, ClassName, HostDepartment, CreditPoint FROM CLASS;");
         while (rs.next()) {
            String arr[] = new String[4];
            arr[0] = "" + rs.getInt("ClassNumber");
            arr[1] = toUnicode(rs.getString("ClassName"));
            arr[2] = toUnicode(rs.getString("HostDepartment"));
            arr[3] = "" + rs.getInt("CreditPoint");
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
