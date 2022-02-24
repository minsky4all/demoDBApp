package dbapp.ui.demo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.BevelBorder;

import dbapp.basic.JDBCAdapter;

public final class DBConsoleExample implements LayoutManager {

   static String[] ConnectOptionNames = { "Connect" };
   static String ConnectTitle = "Connection Information";
   Dimension origin = new Dimension(0, 0);
   JButton fetchButton;
   JButton showConnectionInfoButton;
   JPanel connectionPanel;
   JFrame frame; // The query/results window.
   JLabel userNameLabel;
   JTextField userNameField;
   JLabel passwordLabel;
   JPasswordField passwordField;
   JTextArea queryTextArea;
   JComponent queryAggregate;
   JLabel serverLabel;
   JTextField serverField;
   JLabel driverLabel;
   JTextField driverField;
   JPanel mainPanel;
   
   JTable table;
   JDBCAdapter dataBase;
   JScrollPane tableAggregate;

   /**
    * Brigs up a JDialog using JOptionPane containing the connectionPanel.
    * If the user clicks on the 'Connect' button the connection is reset.
    */
   void activateConnectionDialog() {
      if (JOptionPane.showOptionDialog(tableAggregate, connectionPanel, ConnectTitle, JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE, null, ConnectOptionNames, ConnectOptionNames[0]) == 0) {
         connect();
         frame.setVisible(true);
      } else if (!frame.isVisible()) {
         System.exit(0);
      }
   }

   /**
    * Creates the connectionPanel, which will contain all the fields for the connection information.
    */
   public void createConnectionDialog() {
      // Create the labels and text fields.
      userNameLabel = new JLabel("User name: ", JLabel.RIGHT);
      userNameField = new JTextField("USER135");

      passwordLabel = new JLabel("Password: ", JLabel.RIGHT);
      passwordField = new JPasswordField("");

      serverLabel = new JLabel("Database URL: ", JLabel.RIGHT);
      serverField = new JTextField("jdbc:sqlserver://dbsme.pknu.ac.kr:1433;DatabaseName=DB135");

      driverLabel = new JLabel("Driver: ", JLabel.RIGHT);
      driverField = new JTextField("com.microsoft.sqlserver.jdbc.SQLServerDriver");

      connectionPanel = new JPanel(false);
      connectionPanel.setLayout(new BoxLayout(connectionPanel, BoxLayout.X_AXIS));

      JPanel namePanel = new JPanel(false);
      namePanel.setLayout(new GridLayout(0, 1));
      namePanel.add(userNameLabel);
      namePanel.add(passwordLabel);
      namePanel.add(serverLabel);
      namePanel.add(driverLabel);

      JPanel fieldPanel = new JPanel(false);
      fieldPanel.setLayout(new GridLayout(0, 1));
      fieldPanel.add(userNameField);
      fieldPanel.add(passwordField);
      fieldPanel.add(serverField);
      fieldPanel.add(driverField);

      connectionPanel.add(namePanel);
      connectionPanel.add(fieldPanel);
   }

   public DBConsoleExample() {
      mainPanel = new JPanel();

      // Create the panel for the connection information
      createConnectionDialog();

      // Create the buttons.
      showConnectionInfoButton = new JButton("Configuration");
      showConnectionInfoButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            activateConnectionDialog();
         }
      });

      fetchButton = new JButton("Fetch");
      fetchButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            fetch();
         }
      });

      // Create the query text area and label.
      queryTextArea = new JTextArea("SELECT * FROM STUDENT", 25, 25);
      queryAggregate = new JScrollPane(queryTextArea);
      queryAggregate.setBorder(new BevelBorder(BevelBorder.LOWERED));

      // Create the table.
      tableAggregate = createTable();
      tableAggregate.setBorder(new BevelBorder(BevelBorder.LOWERED));

      // Add all the components to the main panel.
      mainPanel.add(fetchButton);
      mainPanel.add(showConnectionInfoButton);
      mainPanel.add(queryAggregate);
      mainPanel.add(tableAggregate);
      mainPanel.setLayout(this);

      // Create a Frame and put the main panel in it.
      frame = new JFrame("JDBCAdapter ConsoleUI Example");
      frame.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            System.exit(0);
         }
      });
      
      frame.setBackground(Color.lightGray);
      frame.getContentPane().add(mainPanel);
      frame.pack();
      frame.setVisible(false);
      frame.setBounds(200, 200, 640, 480);

      activateConnectionDialog();
   }

   public void connect() {
      dataBase = new JDBCAdapter(serverField.getText(), driverField.getText(), userNameField.getText(), passwordField.getText());
   }

   public void fetch() {
      table.setModel(dataBase.fetchResultTable(queryTextArea.getText(), false));
   }

   public JScrollPane createTable() {
      // Create the table
      table = new JTable();
      table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
      table.setFillsViewportHeight(true);
      JScrollPane scrollpane = new JScrollPane(table);

      return scrollpane;
   }

   public static void main(String s[]) {
      // Trying to set Nimbus look and feel
      try {
         for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
               UIManager.setLookAndFeel(info.getClassName());
               break;
            }
         }
      } catch (Exception ex) {
         ex.printStackTrace();;
      }

      new DBConsoleExample();
   }

   public Dimension preferredLayoutSize(Container c) {
      return origin;
   }

   public Dimension minimumLayoutSize(Container c) {
      return origin;
   }

   public void addLayoutComponent(String s, Component c) { }

   public void removeLayoutComponent(Component c) { }

   public void layoutContainer(Container c) {
      Rectangle b = c.getBounds();
      int topHeight = 90;
      int inset = 4;
      showConnectionInfoButton.setBounds(b.width - 2 * inset - 120, inset, 120, 25);
      fetchButton.setBounds(b.width - 2 * inset - 120, 60, 120, 25);
      queryAggregate.setBounds(inset, inset, b.width - 2 * inset - 150, 80);
      tableAggregate.setBounds(new Rectangle(inset, inset + topHeight, b.width - 2 * inset, b.height - 2 * inset - topHeight));
   }
}
