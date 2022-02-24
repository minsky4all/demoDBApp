package dbapp.ui.demo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.BevelBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public class JTableRendererExample {

   public JTableRendererExample() {
      JFrame frame = new JFrame("JTable Header & Cell Rendering Demo");
      frame.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            System.exit(0);
         }
      });

      // Take the dummy data for JTable.
      final String[] names = { "First Name", "Last Name", "Favorite Color", "Favorite Number", "Vegetarian" };
      final Object[][] data = {
            { "Mark", "Andrews", "Red", new Integer(2), Boolean.TRUE },
            { "Tom", "Ball", "Blue", new Integer(99), Boolean.FALSE },
            { "Alan", "Chung", "Green", new Integer(838), Boolean.FALSE },
            { "Jeff", "Dinkins", "Turquois", new Integer(8), Boolean.TRUE },
            { "Amy", "Fowler", "Yellow", new Integer(3), Boolean.FALSE },
            { "Brian", "Gerhold", "Green", new Integer(0), Boolean.FALSE },
            { "James", "Gosling", "Pink", new Integer(21), Boolean.FALSE },
            { "David", "Karlton", "Red", new Integer(1), Boolean.FALSE },
            { "Dave", "Kloba", "Yellow", new Integer(14), Boolean.FALSE },
            { "Peter", "Korn", "Purple", new Integer(12), Boolean.FALSE },
            { "Phil", "Milne", "Purple", new Integer(3), Boolean.FALSE },
            { "Dave", "Moore", "Green", new Integer(88), Boolean.FALSE },
            { "Hans", "Muller", "Maroon", new Integer(5), Boolean.FALSE },
            { "Rick", "Levenson", "Blue", new Integer(2), Boolean.FALSE },
            { "Tim", "Prinzing", "Blue", new Integer(22), Boolean.FALSE },
            { "Chester", "Rose", "Black", new Integer(0), Boolean.FALSE },
            { "Ray", "Ryan", "Gray", new Integer(77), Boolean.FALSE },
            { "Georges", "Saab", "Red", new Integer(4), Boolean.FALSE },
            { "Willie", "Walker", "Phthalo Blue", new Integer(4), Boolean.FALSE },
            { "Kathy", "Walrath", "Blue", new Integer(8), Boolean.FALSE },
            { "Arnaud", "Weber", "Green", new Integer(44), Boolean.FALSE } };

      // Create a model of the data.
      @SuppressWarnings("serial")
      TableModel dataModel = new AbstractTableModel() {
         // These methods always need to be implemented.

         public int getColumnCount() {
            return names.length;
         }

         public int getRowCount() {
            return data.length;
         }

         public Object getValueAt(int row, int col) {
            return data[row][col];
         }

         // The default implementations of AbstractTableModel would work, but we can refine them.
         @Override
         public String getColumnName(int column) {
            return names[column];
         }

         @Override
         public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
         }

         @Override
         public boolean isCellEditable(int row, int col) {
            return true;
         }

         @Override
         public void setValueAt(Object aValue, int row, int column) {
            System.out.println("Setting value to: " + aValue);
            data[row][column] = aValue;
         }
      };

      // Create the table
      JTable tableView = new JTable(dataModel);
      
      // Turn off auto-resizing so that we can set column sizes programmatically.
      // In this mode, all columns will get their preferred widths, as set blow.
      tableView.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

      // Create a combo box to show that you can use one in a table.
      JComboBox comboBox = new JComboBox();
      comboBox.addItem("Red");
      comboBox.addItem("Orange");
      comboBox.addItem("Yellow");
      comboBox.addItem("Green");
      comboBox.addItem("Blue");
      comboBox.addItem("Indigo");
      comboBox.addItem("Violet");

      TableColumn colorColumn = tableView.getColumn("Favorite Color");
      
      // Use the combo box as the editor in the "Favorite Color" column.
      colorColumn.setCellEditor(new DefaultCellEditor(comboBox));

      // Set a pink background and tooltip for the Color column renderer.
      DefaultTableCellRenderer colorColumnRenderer = new DefaultTableCellRenderer();
      colorColumnRenderer.setBackground(Color.pink);
      colorColumnRenderer.setToolTipText("Click to open combo box");
      colorColumn.setCellRenderer(colorColumnRenderer);

      // Set a tooltip for the header of colors column.
      JTableHeaderRenderer headerRenderer = new JTableHeaderRenderer();
      headerRenderer.setToolTipText("Choose your favorite color");
      colorColumn.setHeaderRenderer(headerRenderer);
      
      // Set the width of the "Vegetarian" column.
      TableColumn vegetarianColumn = tableView.getColumn("Vegetarian");
      vegetarianColumn.setPreferredWidth(100);

      // Show the values in the "Favorite Number" column in different colors.
      TableColumn numbersColumn = tableView.getColumn("Favorite Number");
      @SuppressWarnings("serial")
      DefaultTableCellRenderer numberColumnRenderer = new DefaultTableCellRenderer() {
         @Override
         public void setValue(Object value) {
            int cellValue = (value instanceof Number) ? ((Number) value).intValue() : 0;
            setForeground((cellValue > 30) ? Color.black : Color.red);
            setText((value == null) ? "" : value.toString());
         }
      };
      numberColumnRenderer.setHorizontalAlignment(JLabel.RIGHT);
      numbersColumn.setCellRenderer(numberColumnRenderer);
      numbersColumn.setPreferredWidth(110);

      // Finish setting up the table.
      JScrollPane scrollpane = new JScrollPane(tableView);
      scrollpane.setBorder(new BevelBorder(BevelBorder.LOWERED));
      scrollpane.setPreferredSize(new Dimension(430, 200));
      frame.getContentPane().add(scrollpane);
      
      frame.pack();
      frame.setVisible(true);
   }

   public static void main(String[] args) {
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

      new JTableRendererExample();
   }
}
