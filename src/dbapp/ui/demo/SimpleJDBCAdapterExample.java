package dbapp.ui.demo;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import dbapp.basic.JDBCAdapter;

public class SimpleJDBCAdapterExample {

   public SimpleJDBCAdapterExample(String URL, String driver, String user, String passwd,
         String query) {
      JFrame frame = new JFrame("Simple JDBCAdapter Demo");
      frame.addWindowListener(new WindowAdapter() {

         @Override
         public void windowClosing(WindowEvent e) {
            System.exit(0);
         }
      });
      
      JDBCAdapter dt = new JDBCAdapter(URL, driver, user, passwd);

      // Create the table
      JTable tableView = new JTable(dt.fetchResultTable(query, false));

      JScrollPane scrollpane = new JScrollPane(tableView);
      scrollpane.setPreferredSize(new Dimension(700, 300));

      frame.getContentPane().add(scrollpane);
      frame.pack();
      frame.setVisible(true);
   }

   public static void main(String[] args) {
      if (args.length != 5) {
         System.err.println("Needs database parameters eg. ...");
         System.err.println("java JDBCAdapterSimpleExample \"jdbc:sqlserver://dbsme.pknu.ac.kr:1433;DatabaseName=DB???\" "
                     + "com.microsoft.sqlserver.jdbc.SQLServerDriver USER??? password "
                     + "\"SELECT * FROM STUDENT\"");
         return;
      }

      // Trying to set Nimbus look and feel
      try {
         for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
               UIManager.setLookAndFeel(info.getClassName());
               break;
            }
         }
      } catch (Exception ex) {
         ex.printStackTrace();
      }

      new SimpleJDBCAdapterExample(args[0], args[1], args[2], args[3], args[4]);
   }
}
