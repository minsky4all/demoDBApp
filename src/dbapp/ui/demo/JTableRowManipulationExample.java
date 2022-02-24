package dbapp.ui.demo;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class JTableRowManipulationExample extends JFrame {
   private JTable table;
   private JButton btnAdd, btnDel;
   private DefaultTableModel tableModel;
   private JTextField txtField1, txtField2;

   private JTableRowManipulationExample(String title) {
      super(title);

      setLayout(new BorderLayout());

      table = new JTable();
      JScrollPane pane = new JScrollPane();
      pane.setViewportView(table);

      JPanel eastPanel = new JPanel();
      eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));
      btnAdd = new JButton("Add");
      eastPanel.add(btnAdd);
      btnDel = new JButton("Del");
      eastPanel.add(btnDel);
      eastPanel.add(Box.createVerticalGlue()); // eat-up all vertical margin.
      add(eastPanel, BorderLayout.EAST);

      JPanel northPanel = new JPanel();
      txtField1 = new JTextField();
      txtField2 = new JTextField();
      JLabel lblField1 = new JLabel("Column1   ");
      JLabel lblField2 = new JLabel("Column2   ");
      northPanel.add(lblField1);
      northPanel.add(txtField1);
      northPanel.add(lblField2);
      northPanel.add(txtField2);
      txtField1.setPreferredSize(lblField1.getPreferredSize());
      txtField2.setPreferredSize(lblField2.getPreferredSize());
      add(northPanel, BorderLayout.NORTH);

      add(pane, BorderLayout.CENTER);

      tableModel = new DefaultTableModel(new Object[] { "Column1", "Column2" }, 0);
      table.setModel(tableModel);
      
      btnAdd.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            tableModel.addRow(new Object[] { txtField1.getText(), txtField2.getText() });
            txtField1.setText(""); // clear text1
            txtField2.setText(""); // clear text2

            System.out.println("Current Number of Rows = " + tableModel.getRowCount());
         }
      });
      
      btnDel.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if(table.getSelectedRow() >= 0) {
               int[] index = table.getSelectedRows();
               for(int i = index.length - 1; i >= 0; i--) // should remove rows reversely!
                  tableModel.removeRow(i);
            } else {
               System.out.println("No rows are selected to delete.");
            }
         }         
      });
   }

   public static void main(String[] args) {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            JTableRowManipulationExample frm = new JTableRowManipulationExample("JTable's Row Insert & Delete Demo");
            frm.setLocationByPlatform(true);
            frm.pack();
            frm.setDefaultCloseOperation(EXIT_ON_CLOSE);
            frm.setVisible(true);
         }
      });
   }
}