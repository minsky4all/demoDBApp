package dbapp.ms;

import java.awt.*; 
import javax.swing.*; 
import javax.swing.table.*; 

class LookupSTUDENT { 
    public static void main(String[] args) { 
        JFrame frame = new JFrame("Lookup STUDENT Table"); 
        frame.setPreferredSize(new Dimension(500, 200)); 
        frame.setLocation(500, 400); 
        
        Container contentPane = frame.getContentPane(); 
        
        String colNames[] = { "StudentNumber", "StudentName", "MajorDepartment" }; 
        DefaultTableModel model = new DefaultTableModel(colNames, 0); 
        JTable table = new JTable(model); 
        contentPane.add(new JScrollPane(table), BorderLayout.CENTER); 
        
        JButton button = new JButton("Fetch Student");
        JPanel panel = new JPanel();
        panel.add(button); 
        contentPane.add(panel, BorderLayout.SOUTH); 
 
        button.addActionListener(new FetchListener(table, args[0], args[1], args[2]));   // dbname, username, password
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        frame.pack(); 
        frame.setVisible(true); 
    } 
} 
