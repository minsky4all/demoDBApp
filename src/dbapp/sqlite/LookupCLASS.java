package dbapp.sqlite;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class LookupCLASS {
   public static void main(String[] args) { 
      JFrame frame = new JFrame("Lookup CLASS Table"); 
      frame.setPreferredSize(new Dimension(500, 200)); 
      frame.setLocation(500, 400); 
      
      Container contentPane = frame.getContentPane(); 
      
      String colNames[] = { "ClassNumber", "ClassName", "HostDepartment", "CreditPoint" }; 
      DefaultTableModel model = new DefaultTableModel(colNames, 0); 
      JTable table = new JTable(model); 
      contentPane.add(new JScrollPane(table), BorderLayout.CENTER); 
      
      JButton button = new JButton("Fetch Class");
      JPanel panel = new JPanel();
      panel.add(button); 
      contentPane.add(panel, BorderLayout.SOUTH); 

      button.addActionListener(new FetchListener(table, args[0]));   // dbname, dbfilename
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
      frame.pack(); 
      frame.setVisible(true); 
  } 
}
