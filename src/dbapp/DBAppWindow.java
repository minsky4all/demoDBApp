package dbapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableModel;

import dbapp.basic.JDBCAdapter;
import dbapp.basic.JDBCAdapter.JDBCTable;
import dbapp.ui.ClosableTabbedPane;

public class DBAppWindow extends JFrame {
   // Application's State
   private static final int NOT_CONNECTED = 0;
   private static final int CONNECTED = 1;
   
   // Target DB Types
   private static final int DB_NOT_KNOWN = 0;
   private static final int DB_MSSQL = 1;
   private static final int DB_SQLITE = 2;
   
   // The current Look & Feel
   private static final String windows = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
   private static final String nimbus = "javax.swing.plaf.nimbus.NimbusLookAndFeel";
   
   private int appState = NOT_CONNECTED;
   private int dbType = DB_NOT_KNOWN;
         
   // Shared singleton database connection object.
   private JDBCAdapter dbAdapter = null;
   private String userName, passWord, serverURL, dbDriver;
   
   private String iconPath = "/dbapp/ui/resources/images/";

   // The preferred size of the Frame
   private static final int PREFERRED_WIDTH = 720;
   private static final int PREFERRED_HEIGHT = 640;

   // SchemaPanel for DB Schema Image
   private JComponent schemaPanel = null;

   // Status Bar
   private StatusMessagePanel messagePanel = null;
   
   // Tool Bar
   private ButtonToolBar toolbar = null;

   // Menu & Tool Bar Images
   private ImageIcon connIcon, disconnIcon, schemaIcon, authorsIcon, schemaPaneIcon, exitIcon, aboutIcon; 
   private ImageIcon onLineIcon, offLineIcon,  tableLookupIcon, viewIcon, executeIcon, sqlIcon, aboutDBIcon;
   private ImageIcon tabCloseOnIcon, tabCloseOffIcon;      // for ClosableTabbedPane
   private ImageIcon recordAdd, recordDelete;              // for Lookup Table Button
   
   // Menus & Actions
   private String[] allMenus = { "Database", "Table Management", "Project Operations", "Help" };
   private AbstractAction[] allActions = new AbstractAction[15];
   
   private ClosableTabbedPane contentTabbedPane = null;

   public DBAppWindow(GraphicsConfiguration gc) {
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      this.setLayout(new BorderLayout());
      this.setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));

      setPreferredLookAndFeel();
      preloadToolbarImages();
      initializeUI();
   }

   /**
    * Start Main Frame Window for DB Application.
    */
   public static void main(String[] args) {
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            JFrame f = new DBAppWindow(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
            f.setTitle("DB Application Main Window");
            f.pack();

            Rectangle screenRect = f.getGraphicsConfiguration().getBounds();
            Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(f.getGraphicsConfiguration());

            // Make sure we don't place the demo off the screen.
            int centerWidth = (screenRect.width < f.getSize().width) ? screenRect.x : screenRect.x + screenRect.width / 2 - f.getSize().width / 2;
            int centerHeight = (screenRect.height < f.getSize().height) ? screenRect.y : screenRect.y + screenRect.height / 2 - f.getSize().height / 2;

            centerHeight = (centerHeight < screenInsets.top) ? screenInsets.top : centerHeight;

            f.setLocation(centerWidth, centerHeight);
            f.setVisible(true);
         }
      });
   }

   public void initializeUI() {
      // set MenuBar and menus.
      JMenuBar menuBar = createMenus();
      this.setJMenuBar(menuBar);

      JPanel top = new JPanel();
      top.setLayout(new BorderLayout());
      add(top, BorderLayout.NORTH);

      ToolBarPanel toolbarPanel = new ToolBarPanel();
      toolbarPanel.setLayout(new BorderLayout());
      toolbar = new ButtonToolBar();
      toolbar.addToolBarButton(allActions[0], false);   // ConnectAction
      toolbar.addToolBarButton(allActions[1], false);   // DisconnectAction
      toolbar.addSeparator();
      toolbar.addToolBarButton(allActions[14], false);  // AboutDBParametersAction
      toolbarPanel.add(toolbar, BorderLayout.CENTER);
      top.add(toolbarPanel, BorderLayout.SOUTH);
      toolbarPanel.addContainerListener(toolbarPanel);

      contentTabbedPane = new ClosableTabbedPane(tabCloseOnIcon, tabCloseOffIcon);
      add(contentTabbedPane, BorderLayout.CENTER);

      messagePanel = new StatusMessagePanel();
      add(messagePanel, BorderLayout.SOUTH);

      schemaPanel = getSchemaPane();
      schemaPanel.setBorder(new EtchedBorder());
      contentTabbedPane.addTab("DB Schema", schemaPaneIcon, schemaPanel, null, null, false);
   }

   /**
    * Create menus
    */
   public JMenuBar createMenus() {
      JMenuBar menuBar = new JMenuBar();
  
      // -----------------------------
      // create the 'Database' menu.
      JMenu aMenu = (JMenu) menuBar.add(new JMenu(allMenus[0]));
      allActions[0] = new ConnectAction("Login to database");        // 0: ConnectAction
      createMenuItem(aMenu, allActions[0], true);
      allActions[1] = new DisconnectAction("Logout from database");  // 1: DisconnectAction 
      createMenuItem(aMenu, allActions[1], false);
      aMenu.addSeparator();
      allActions[2] = new ExitAction("Exit");                        // 2: ExitAction
      createMenuItem(aMenu, allActions[2], true);

      // ------------------------------
      // create the 'Table Management' menu.
      // Lookup Single Table.
      aMenu = (JMenu) menuBar.add(new JMenu(allMenus[1]));
      allActions[3] = new TableLookupAction("STUDENT");              // 3: LookupAction - STUDENT 
      createMenuItem(aMenu, allActions[3], false);
      allActions[4] = new TableLookupAction("CLASS");                // 4: LookupAction - CLASS
      createMenuItem(aMenu, allActions[4], false);
      allActions[5] = new TableLookupAction("CLASS_OFFERING");       // 5: LookupAction - CLASS_OFFERING
      createMenuItem(aMenu, allActions[5], false);     
      allActions[6] = new TableLookupAction("GRADE");                // 6: LookupAction - GRADE
      createMenuItem(aMenu, allActions[6], false);   
      aMenu.addSeparator();
      
      // Lookup View.
      allActions[7] = new ViewLookupAction("STUDENT_TO_CLASS");     // 7: ViewAction - STUDENT_TO_CLASS
      createMenuItem(aMenu, allActions[7], false);
      allActions[8] = new ViewLookupAction("CLASS_TO_STUDENT");     // 8: ViewAction - CLASS_TO_STUDENT
      createMenuItem(aMenu, allActions[8], false);
      allActions[9] = new ViewLookupAction("ALL_CLASS_STUDENT");    // 9: ViewAction - ALL_CLASS_STUDENT
      createMenuItem(aMenu, allActions[9], false);
      
      // -------------------------------
      // create the 'Project Operations' menu.
      aMenu = (JMenu) menuBar.add(new JMenu(allMenus[2]));
      allActions[10] = new StoredProcedureCallAction("getAveragePointResultTable");   // 10: Stored Procedure CallAction
      createMenuItem(aMenu, allActions[10], false);
      aMenu.addSeparator();
      allActions[11] = new GeneralSelectSQLAction("SELECT SQL");       // 11: General SELECT SQLAction
      createMenuItem(aMenu, allActions[11], false);
      allActions[12] = new CustomProjectAction("STUDENT's CLASS Subscription");    // 12: Custom Project Action (Student's Subscription)
      createMenuItem(aMenu, allActions[12], false);
      
      // -------------------------------
      // create the 'Help' menu.
      aMenu = (JMenu) menuBar.add(new JMenu(allMenus[3]));
      allActions[13] = new AboutDeveloperAction("Developers...");                     // 13: AboutDeveloperAction
      createMenuItem(aMenu, allActions[13], true);
      allActions[14] = new AboutDBParametersAction("DB Parameters...");               // 14: AboutDBParametersAction
      createMenuItem(aMenu, allActions[14], true);
      
      return menuBar;
   }

   public JMenuItem createMenuItem(JMenu menu, Action action, boolean isEnabled) {
      JMenuItem mi = (JMenuItem) menu.add(new JMenuItem(action));
      if (action == null || !isEnabled)
         mi.setEnabled(false);

      return mi;
   }

   // *******************************************************
   // ****************** Utility Methods ********************
   // *******************************************************

   /**
    * Returns the frame instance
    */
   public JFrame getApplicationWindow() {
      return this;
   }

   public Container getContentTabbedPane() {
      return contentTabbedPane;
   }

   /**
    * Stores the current L&F, and calls updateLookAndFeel, below
    */
   private void setPreferredLookAndFeel() {
      try {
         UIManager.setLookAndFeel(windows);
      } catch (Exception e) {
         System.out.println("Failed loading L&F: " + windows);
         e.printStackTrace();
         
         try {
            UIManager.setLookAndFeel(nimbus);  // Nimbus Look&Feel for Other OS.
         } catch(Exception ee) {
            System.out.println("Failed loading L&F: " + nimbus); 
            ee.printStackTrace();
         }
      } finally {
         SwingUtilities.updateComponentTreeUI(this);
      }
   }
   
   /**
    * Creates an icon from an image contained in the "dbapp/ui/resources/images" directory.
    */
   private ImageIcon createImageIcon(String filename) {
      String path = iconPath + filename;
      
      return new ImageIcon(getClass().getResource(path));
   }
   
   private void preloadToolbarImages() {
      connIcon = createImageIcon("connect.png");                     // 16x16\devices\connect.png
      disconnIcon = createImageIcon("disconnect.png");               // 16x16\devices\disconnect.png
      
      schemaIcon = createImageIcon("db_schema.jpg");                 // Your db-schema image.
      authorsIcon = createImageIcon("author_image.jpg");             // Author's group image.
      
      exitIcon = createImageIcon("application-exit.png");            // 16x16\actions\application-exit.png
      aboutIcon = createImageIcon("help-3.png");                     // 16x16\actions\help-3.png
      
      schemaPaneIcon = createImageIcon("table-relationship.png");    // 16x16\actions\table-relationship.png
      
      offLineIcon = createImageIcon("status-offline.png");           // 16x16\status\status-offline.png
      onLineIcon = createImageIcon("status-online.png");             // 16x16\status\status-online.png
      
      tabCloseOffIcon = createImageIcon("window-close.png");         // 16x16\actions\window-close.png
      tabCloseOnIcon = createImageIcon("window-close-2.png");        // 16x16\actions\window-close-2.png
      
      tableLookupIcon = createImageIcon("database-table.png");       // 16x16\actions\database-table.png
      viewIcon = createImageIcon("view-calendar-workweek.png");      // 16x16\actions\view-calendar-workweek.png
      executeIcon = createImageIcon("computer-go.png");              // 16x16\actions\computer-go.png
      sqlIcon = createImageIcon("text-x-sql.png");                   // 16x16\mimetypes\tango-style\text-x-sql.png
      aboutDBIcon = createImageIcon("db_status-2.png");              // 16x16\actions\db_status-2.png
      
      recordAdd = createImageIcon("table-add.png");                  // 16x16\actions\table-add.png
      recordDelete = createImageIcon("table-row-delete-2.png");      // 16x16\actions\table-row-delete-2.png      
   }
   
   private JComponent getSchemaPane() {
      JLabel imgLabel = new JLabel(schemaIcon);
      imgLabel.setOpaque(true);
      imgLabel.setBackground(Color.WHITE);
      
      JScrollPane scroller = new JScrollPane();
      scroller.getViewport().add(imgLabel);
      
      return scroller;
   }
   
   private void setConnectionParameter(String user, String pw, String url, String drv) {
      userName = user;
      passWord = pw;
      serverURL = url;
      dbDriver = drv;
   }
   
   public void setApplicationStatus(int status, int type) {
      this.appState = status;
      this.dbType = type;
      manageApplicationUIStatus(status);
   }
   
   public int getApplicationStatus() {
      return this.appState;
   }
   
   private void manageApplicationUIStatus(int status) {      
      switch(status) {
         case NOT_CONNECTED:
            allActions[0].setEnabled(true);    // ConnectAction
            allActions[1].setEnabled(false);   // DisconnectAction
            allActions[2].setEnabled(true);    // ExitAction, always enabled
            allActions[3].setEnabled(false);   // TableLookupAction - STUDENT
            allActions[4].setEnabled(false);   // TableLookupAction - CLASS
            allActions[5].setEnabled(false);   // TableLookupAction - CLASS_OFFERING
            allActions[6].setEnabled(false);   // TableLookupAction - GRADE
            allActions[7].setEnabled(false);   // ViewLookupAction - STUDENT_TO_CLASS
            allActions[8].setEnabled(false);   // ViewLookupAction - CLASS_TO_STUDENT
            allActions[9].setEnabled(false);   // ViewLookupAction - ALL_CLASS_STUDENT 
            allActions[10].setEnabled(false);  // StoredProcedureCallAction - getAveragePointResultTable
            allActions[11].setEnabled(false);  // General SELECT SQLAction
            allActions[12].setEnabled(false);  // CustomeProjectAction - Student's Class Subscription
            allActions[13].setEnabled(true);  // AboutDeveloperAction, always enabled
            allActions[14].setEnabled(true);  // AboutDBParametersAction, always enabled
            break;
            
         case CONNECTED:
            allActions[0].setEnabled(false);  // ConnectAction
            allActions[1].setEnabled(true);   // DisconnectAction
            allActions[2].setEnabled(true);   // ExitAction, always enabled
            allActions[3].setEnabled(true);   // TableLookupAction - STUDENT
            allActions[4].setEnabled(true);   // TableLookupAction - CLASS
            allActions[5].setEnabled(true);   // TableLookupAction - CLASS_OFFERING
            allActions[6].setEnabled(true);   // TableLookupAction - GRADE
            allActions[7].setEnabled(true);   // ViewLookupAction - STUDENT_TO_CLASS
            allActions[8].setEnabled(true);   // ViewLookupAction - CLASS_TO_STUDENT
            allActions[9].setEnabled(true);   // ViewLookupAction - ALL_CLASS_STUDENT
            if(dbType == DB_MSSQL)            // StoredProcedureCallAction - getAveragePointResultTable, enabled when connected to MSSQL
               allActions[10].setEnabled(true);
            else
               allActions[10].setEnabled(false);
            allActions[11].setEnabled(true);    // General SELECT SQLAction
            allActions[12].setEnabled(true);    // CustomeProjectAction - Student's Class Subscription
            allActions[13].setEnabled(true);    // AboutDeveloperAction, always enabled
            allActions[14].setEnabled(true);    // AboutDBParametersAction, always enabled
            break;
      }
   }
   
   private void closeAdditionalTabs() {
      while(contentTabbedPane.getTabCount() > 1) {  // keep schema tab open.
         System.out.println("Removing tab at :" + (contentTabbedPane.getTabCount() - 1));
         contentTabbedPane.removeTabAt(contentTabbedPane.getTabCount() - 1);
      }
   }
   
   // *******************************************************
   // ************** ToggleButtonToolbar *****************
   // *******************************************************
   static Insets zeroInsets = new Insets(1, 1, 1, 1);
   
   protected class ButtonToolBar extends JToolBar {
      public ButtonToolBar() {
         super();
      }

      AbstractButton addToolBarButton(Action a, boolean isToggle) {
         AbstractButton ab = isToggle ?  new JToggleButton((Icon) a.getValue(Action.SMALL_ICON)) :
               new JButton((Icon) a.getValue(Action.SMALL_ICON));
         ab.setAction(a);
         ab.setMargin(zeroInsets);
         ab.setEnabled(a.isEnabled());
         ab.setToolTipText((String) a.getValue(Action.SHORT_DESCRIPTION));
         ab.setText(null);          // remove text from the toolbar
         add(ab);
         
         return ab;
      }
   }

   // *******************************************************
   // ********* ToolBar Panel / Docking Listener ***********
   // *******************************************************
   class ToolBarPanel extends JPanel implements ContainerListener {

      public boolean contains(int x, int y) {
         Component c = getParent();
         if (c != null) {
            Rectangle r = c.getBounds();
            return (x >= 0) && (x < r.width) && (y >= 0) && (y < r.height);
         } else {
            return super.contains(x, y);
         }
      }

      public void componentAdded(ContainerEvent e) {
         Container c = e.getContainer().getParent();
         if (c != null) {
            c.getParent().validate();
            c.getParent().repaint();
         }
      }

      public void componentRemoved(ContainerEvent e) {
         Container c = e.getContainer().getParent();
         if (c != null) {
            c.getParent().validate();
            c.getParent().repaint();
         }
      }
   }

   // *******************************************************
   // ********* StatusMessage Panel               ***********
   // *******************************************************
   class StatusMessagePanel extends JPanel {
      private JTextField messageField;
      private boolean isConnected = false;
      private JLabel statusLabel;
      
      public StatusMessagePanel() {
         this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
         
         messageField = new JTextField();
         messageField.setEnabled(false);
         this.add(messageField);
         
         statusLabel = new JLabel("offline", offLineIcon, SwingConstants.LEFT);
         this.add(statusLabel);
      }
      
      public String getMessage() {
         return messageField.getText();
      }
      
      public void setMessage(String m) {
         messageField.setText(m);
      }
      
      public boolean isConnected() {
         return isConnected;
      }
      
      public void setConnected(boolean status) {
         if(status) {
            statusLabel.setIcon(onLineIcon);
            statusLabel.setText("online");
         } else {
            statusLabel.setIcon(offLineIcon);
            statusLabel.setText("offline");
         }
      }
   }
   
   // *******************************************************
   // ******************** Actions ***********************
   // *******************************************************
   class ConnectAction extends AbstractAction {      
      JPanel dbLoginUI;
      
      String[] dbServerURL = { "jdbc:sqlserver://server.somewhere.com:1433;DatabaseName=?????", "jdbc:sqlite:D:/Temp/student.sqlite" };
      String[] dbDriver = { "com.microsoft.sqlserver.jdbc.SQLServerDriver", "org.sqlite.JDBC" };
      String[] connectOptionNames = { "Connect", "Cancel" };
      String connectTitle = "Login to database";
      
      JTextField userNameField = new JTextField();
      JTextField passwordField = new JPasswordField();
      JTextField serverField = new JTextField("jdbc:sqlserver://server.somewhere.com:1433;DatabaseName=?????");
      JTextField driverField = new JTextField("com.microsoft.sqlserver.jdbc.SQLServerDriver");
      
      ButtonGroup group = new ButtonGroup();
      JRadioButton b1 = new JRadioButton("SQLServer");
      JRadioButton b2 = new JRadioButton("SQLite DB");
      
      protected ConnectAction(String name) {
         super(name, connIcon);
         
         putValue(Action.DEFAULT, "LOGIN"); 
         putValue(Action.SHORT_DESCRIPTION, "Login to database");
         
         this.setEnabled(true);
      }

      public void actionPerformed(ActionEvent e) {
         if(dbLoginUI == null) {          
            group = new ButtonGroup();
            group.add(b1);
            group.add(b2);
            b1.addActionListener(new ActionListener() {

               @Override
               public void actionPerformed(ActionEvent e) {                  
                  userNameField.setEnabled(true);
                  passwordField.setEnabled(true);
                  serverField.setText(dbServerURL[0]);
                  driverField.setText(dbDriver[0]);
                  
                  userNameField.requestFocusInWindow(); 
               }               
            });
            b2.addActionListener(new ActionListener() {

               @Override
               public void actionPerformed(ActionEvent e) {                  
                  userNameField.setEnabled(false);
                  passwordField.setEnabled(false);
                  serverField.setText(dbServerURL[1]);
                  driverField.setText(dbDriver[1]);
                  
                  serverField.requestFocusInWindow(); 
               }               
            });            

            JPanel namePanel = new JPanel(false);
            namePanel.setLayout(new GridLayout(0, 1));
            
            namePanel.add(new JLabel("Database Type: ", JLabel.RIGHT));
            namePanel.add(new JLabel("User ID: ", JLabel.RIGHT));
            namePanel.add(new JLabel("Password: ", JLabel.RIGHT));
            namePanel.add(new JLabel("Database URL: ", JLabel.RIGHT));
            namePanel.add(new JLabel("DB Driver: ", JLabel.RIGHT));

            JPanel fieldPanel = new JPanel(false);
            fieldPanel.setLayout(new GridLayout(0, 1));
            
            JPanel radioPanel = new JPanel(false);
            radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.X_AXIS));   
            radioPanel.add(b1);
            radioPanel.add(b2);
            radioPanel.add(Box.createHorizontalStrut(200));
            fieldPanel.add(radioPanel);
            fieldPanel.add(userNameField);
            fieldPanel.add(passwordField);
            fieldPanel.add(serverField);
            fieldPanel.add(driverField);
            
            dbLoginUI = new JPanel(false);
            dbLoginUI.setLayout(new BoxLayout(dbLoginUI, BoxLayout.X_AXIS));
            dbLoginUI.add(namePanel);
            dbLoginUI.add(fieldPanel);            

            // initially, start with DB_MSSQL type.
            b1.setSelected(true);
         }
         
         while(JOptionPane.showOptionDialog(DBAppWindow.this, dbLoginUI, connectTitle, JOptionPane.OK_CANCEL_OPTION,
               JOptionPane.QUESTION_MESSAGE, null, connectOptionNames, connectOptionNames[0]) == 0) {
            // check input validity.
            String userID = userNameField.getText().trim();
            String pw = passwordField.getText().trim();
            String svrURL = serverField.getText().trim();
            String drvString = driverField.getText().trim();
            
            if(b1.isSelected()) { // type: DB_MSSQL             
               if(userID.length() == 0 || pw.length() == 0 || svrURL.length() == 0 || drvString.length() == 0) {
                  messagePanel.setMessage("Some input is empty. Please, check again!");
                  
                  // reshow the option dialog.
                  continue;
               } else {  // new connection parameters!               
                  messagePanel.setMessage("Opening new database connection...");
                  
                  dbAdapter = new JDBCAdapter(svrURL, drvString, userID, pw);               
                  if(dbAdapter.isValid()) {   // update database connection parameters.                  
                     setConnectionParameter(userID, pw, svrURL, drvString);
                     setApplicationStatus(DBAppWindow.CONNECTED, DBAppWindow.DB_MSSQL);
                     messagePanel.setMessage("User " + userName + " is logged in to MS SQLServer.");
                     messagePanel.setConnected(true);      
                     
                     // successfully connected, escape the while-loop.
                     break;
                  }
               }
            } else {  // type: DB_SQLITE
               if(svrURL.length() == 0 || drvString.length() == 0) {
                  messagePanel.setMessage("Some input is empty. Please, check again!");
                  
                  // reshow the option dialog.
                  continue;
               } else {  // new connection parameters!               
                  messagePanel.setMessage("Opening new database connection...");
                  
                  dbAdapter = new JDBCAdapter(svrURL, drvString);   // SQLite don't need authentication & authorization.               
                  if(dbAdapter.isValid()) {   // update database connection parameters.                  
                     setConnectionParameter(userID, pw, svrURL, drvString);
                     setApplicationStatus(DBAppWindow.CONNECTED, DBAppWindow.DB_SQLITE);
                     messagePanel.setMessage("Connected to SQLite DB.");
                     messagePanel.setConnected(true);      
                     
                     // successfully connected, escape the while-loop.
                     break;
                  }
               }
            }
            
            messagePanel.setMessage("Unable to connect to database. Please, check parameters!");
            dbAdapter = null;
         }         
      }
   }
   
   class DisconnectAction extends AbstractAction {      
      String title = "DB Logout";
      String message = "Logout from current database?";
      
      protected DisconnectAction(String name) {
         super(name, disconnIcon);
         
         putValue(Action.DEFAULT, "LOGOUT"); 
         putValue(Action.SHORT_DESCRIPTION, "Logout from current database");
         
         this.setEnabled(false);
      }

      public void actionPerformed(ActionEvent e) {
         if (JOptionPane.showConfirmDialog(DBAppWindow.this, message, title, JOptionPane.YES_NO_OPTION) == 0) {
            try {
               // Should close all the open Tabs beforehand!
               closeAdditionalTabs();
               dbAdapter.close();
            } catch (SQLException evt) {
               evt.printStackTrace();
            } finally {
               dbAdapter = null;
               setConnectionParameter(null, null, null, null);
               setApplicationStatus(DBAppWindow.NOT_CONNECTED, DBAppWindow.DB_NOT_KNOWN);
               messagePanel.setMessage("User logged out.");
               messagePanel.setConnected(false);
            }
         } else {
            messagePanel.setMessage("Cancelled, hold connection.");
         }
      }
   }
   
   class ExitAction extends AbstractAction {
      protected ExitAction(String name) {
         super(name, exitIcon);
         
         putValue(Action.DEFAULT, "EXIT"); 
         putValue(Action.SHORT_DESCRIPTION, "Exit application");  // for ToolTip
         
         this.setEnabled(true);
      }

      public void actionPerformed(ActionEvent e) {
         SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               // disconnect first.
               if(getApplicationStatus() == CONNECTED)
                  allActions[1].actionPerformed(null);    // DisconnectAction  
               
               System.exit(0);  // now, finish the application.
            }
         });
      }
   }
   
   class TableLookupAction extends AbstractAction {
      protected TableLookupAction(String table) {
         super("Table: " + table, tableLookupIcon);
         
         putValue(Action.DEFAULT, "LOOKUP"); 
         putValue(Action.SHORT_DESCRIPTION, "Table(" + table + ") Lookup");  // for ToolTip
         putValue("TableName", table);
         
         this.setEnabled(false);
      }
      
      @Override
      public void actionPerformed(ActionEvent e) {
         System.out.println("Lookup Table: " + getValue("TableName"));
         JPanel lookupUI = new JPanel();
         lookupUI.setLayout(new BorderLayout());
         
         final JDBCTable tm = dbAdapter.fetchWholeTable((String) getValue("TableName"), false);
         final JTable table = new JTable(tm);
         table.setAutoCreateRowSorter(true);  // make this table be sortable!            
         table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
         table.setFillsViewportHeight(true);  // fill the extra area by stretching JTable
         
         JScrollPane jsp = new JScrollPane(table);
         jsp.setBorder(new BevelBorder(BevelBorder.LOWERED));
         lookupUI.add(jsp, BorderLayout.CENTER);

         // create a button panel at the bottom
         JPanel bottomButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         
         JButton insertButton = new JButton("Add A New Row", recordAdd);
         insertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               int nCol = tm.getColumnCount();
               
               JPanel addRowPanel = new JPanel(new GridLayout(nCol, 2));
               JTextField[] values = new JTextField[nCol];
               for(int i = 0; i < nCol; i++) {
                  addRowPanel.add(new JLabel(tm.getColumnName(i)));
                  values[i] = new JTextField(10);
                  addRowPanel.add(values[i]);
               }               
               
               if (JOptionPane.showConfirmDialog(DBAppWindow.this, addRowPanel, "Insert A Record", JOptionPane.OK_CANCEL_OPTION) == 0) {
                  Vector<Object> aRow = new Vector<Object>();
                  for(int i = 0; i < nCol; i++)
                     aRow.addElement(values[i].getText());
                  
                  tm.addDBTableRow(aRow);
                  messagePanel.setMessage("User inserted a new record to table.");
               } else {
                  messagePanel.setMessage("Cancelled, table not changed.");
               }
            }            
         });
         bottomButtons.add(insertButton);
         
         JButton deleteButton = new JButton("Delete Selected Rows", recordDelete);
         deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               System.out.println("You shold delete selected rows from the db table & refresh the JTable view.");
               int[] toDelete = table.getSelectedRows();  // will this always be ascending order??
               System.out.println("You seleted: " + Arrays.toString(toDelete));
               for(int x = toDelete.length - 1; x >= 0; --x)
                  tm.removeRow(toDelete[x]);
            }            
         });
         bottomButtons.add(deleteButton);       
         
         lookupUI.add(bottomButtons, BorderLayout.SOUTH);
         
         // lookup the TabbedPane to find a tab with the same title string.
         int index = contentTabbedPane.indexOfTab((String) getValue(Action.NAME));
         if(index == -1) {  // when there's no such tab, add a new tab.
            contentTabbedPane.addTab((String) getValue(Action.NAME), tableLookupIcon, lookupUI, null, null);
            index = contentTabbedPane.getTabCount();
            contentTabbedPane.setSelectedIndex(index - 1);
         } else {  // if the tab already exists, bring it to front by selecting the tab index. 
            contentTabbedPane.setSelectedIndex(index);
         }
      }
   }

   class ViewLookupAction extends AbstractAction {
      protected ViewLookupAction(String view) {
         super("View: " + view, viewIcon);
         
         putValue(Action.DEFAULT, "LOOKUP"); 
         putValue(Action.SHORT_DESCRIPTION, "View(" + view + ") Lookup");  // for ToolTip
         putValue("ViewName", view);
         
         this.setEnabled(false);
      }
      
      @Override
      public void actionPerformed(ActionEvent e) {
         System.out.println("Lookup View: " + getValue("ViewName"));
         JPanel lookupUI = new JPanel();
         lookupUI.setLayout(new BorderLayout());
         
         JDBCTable tm = dbAdapter.fetchWholeTable((String) getValue("ViewName"), true);  // make the view read-only!
         JTable table = new JTable(tm);
         table.setAutoCreateRowSorter(true);  // make this view be sortable!            
         table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
         table.setFillsViewportHeight(true);  // fill the extra area by stretching JTable
         
         JScrollPane jsp = new JScrollPane(table);
         jsp.setBorder(new BevelBorder(BevelBorder.LOWERED));
         lookupUI.add(jsp, BorderLayout.CENTER);

         // lookup the TabbedPane to find a tab with the same title string.
         int index = contentTabbedPane.indexOfTab((String) getValue(Action.NAME));
         if(index == -1) {  // when there's no such tab, add a new tab.
            contentTabbedPane.addTab((String) getValue(Action.NAME), viewIcon, lookupUI, null, null);
            index = contentTabbedPane.getTabCount();
            contentTabbedPane.setSelectedIndex(index - 1);
         } else {  // if the tab already exists, bring it to front by selecting the tab index. 
            contentTabbedPane.setSelectedIndex(index);
         }
      }
   }

   class StoredProcedureCallAction extends AbstractAction {
      protected StoredProcedureCallAction(String sp) {
         super("STUDENT's GPA", executeIcon);
         
         putValue(Action.DEFAULT, "EXECUTE"); 
         putValue(Action.SHORT_DESCRIPTION, "Run Stored Procedure(" + sp + ")");  // for ToolTip
         putValue("Stored Procedure", sp);
         
         this.setEnabled(false);
      }
      
      @Override
      public void actionPerformed(ActionEvent e) {
         System.out.println("Run Stored Procedure: " + getValue("Stored Procedure"));
         
         ResultSet rs = dbAdapter.executeQuery("SELECT DISTINCT YearTerm FROM CLASS_OFFERING");
         Vector<String> yrTr = new Vector<String>();
         try {
            while(rs.next()) {
               yrTr.add(rs.getString("YearTerm"));
            }
         } catch(Exception ex) {
            ex.printStackTrace();
            messagePanel.setMessage("Unable to initialize UI. ComboBox becomes empty!");
         } finally {
            try {
               if(rs != null) {
                  rs.close();
                  rs = null;
               }
            } catch(Exception ignore) {}
         }
         
         JPanel spUI = new JPanel();
         spUI.setLayout(new BorderLayout());
                  
         JPanel topPanel = new JPanel();
         topPanel.add(new JLabel("Select Year & Term :"));
         
         final JComboBox cBox = new JComboBox(yrTr);
         cBox.setSelectedIndex(0);    // set the first item selected.

         topPanel.add(cBox);
         spUI.add(topPanel, BorderLayout.NORTH);

         final JTable table = new JTable(getTableModel((String) cBox.getSelectedItem()));
         table.setAutoCreateRowSorter(true);  // make this view be sortable!            
         table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
         table.setFillsViewportHeight(true);  // fill the extra area by stretching JTable
         
         JScrollPane jsp = new JScrollPane(table);
         jsp.setBorder(new BevelBorder(BevelBorder.LOWERED));
         spUI.add(jsp, BorderLayout.CENTER);

         // add ItemListener to JComboBox to refresh JTable.
         cBox.addItemListener(new ItemListener() {            
            @Override
            public void itemStateChanged(ItemEvent e) {
               if(e.getStateChange() == ItemEvent.SELECTED) {
                  table.setModel(getTableModel((String) cBox.getSelectedItem()));
               }
            }            
         });
         
         // lookup the TabbedPane to find a tab with the same title string.
         int index = contentTabbedPane.indexOfTab((String) getValue(Action.NAME));
         if(index == -1) {  // when there's no such tab, add a new tab.
            contentTabbedPane.addTab((String) getValue(Action.NAME), executeIcon, spUI, null, null);
            index = contentTabbedPane.getTabCount();
            contentTabbedPane.setSelectedIndex(index - 1);
         } else {  // if the tab already exists, bring it to front by selecting the tab index. 
            contentTabbedPane.setSelectedIndex(index);
         }   
      }
      
      /**
       * Get the TableModel from the database 'Stored Procedure' to refresh JTable.
       * 
       * @param value input parameter (yearTerm) for getAveragePointResultTable stored procedure.
       * @return
       */
      private TableModel getTableModel(String value) {
         Connection conn = dbAdapter.getConnection();
         String sqlCommand = "{ call getAveragePointResultTable(?) }";
         
         TableModel tm = null;
         try {
            CallableStatement cstmt = conn.prepareCall(sqlCommand, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);      
            cstmt.setString("yearTerm", value);
            cstmt.execute();         
                        
            tm = dbAdapter.fetchResultTable(cstmt.getResultSet(), true);
         } catch (SQLException error) {
            error.printStackTrace();
         }
         
         return tm;
      }
   }
   
   class GeneralSelectSQLAction extends AbstractAction {
      protected GeneralSelectSQLAction(String sql) {
         super("CLASS's GPA", sqlIcon);
         
         putValue(Action.DEFAULT, "SELECT"); 
         putValue(Action.SHORT_DESCRIPTION, "Run: [" + sql + "]");  // for ToolTip
         putValue("SELECT SQL", sql);
         
         this.setEnabled(false);
      }
      
      @Override
      public void actionPerformed(ActionEvent e) {
         System.out.println("Run: " + getValue("SELECT SQL"));

         JPanel sqlUI = new JPanel();
         sqlUI.setLayout(new BorderLayout());                 

         TableModel tm = dbAdapter.fetchResultTable("SELECT ClassNumber, ClassName, OfferingNumber, YearTerm, ClassSection,"
               + " ROUND(SUM(GradePoint * CreditPoint)/SUM(CreditPoint), 2) AS GlassGPA"
               + " FROM CLASS_TO_STUDENT"
               + " WHERE GradePoint IS NOT NULL"
               + " GROUP BY ClassNumber, ClassName, OfferingNumber, YearTerm, ClassSection"
               + " ORDER BY ClassNumber ASC, OfferingNumber DESC;", true);
         JTable table = new JTable(tm);
         table.setAutoCreateRowSorter(true);  // make this view be sortable!            
         table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
         table.setFillsViewportHeight(true);  // fill the extra area by stretching JTable
         
         JScrollPane jsp = new JScrollPane(table);
         jsp.setBorder(new BevelBorder(BevelBorder.LOWERED));
         sqlUI.add(jsp, BorderLayout.CENTER);
         
         // lookup the TabbedPane to find a tab with the same title string.
         int index = contentTabbedPane.indexOfTab((String) getValue(Action.NAME));
         if(index == -1) {  // when there's no such tab, add a new tab.
            contentTabbedPane.addTab((String) getValue(Action.NAME), sqlIcon, sqlUI, null, null);
            index = contentTabbedPane.getTabCount();
            contentTabbedPane.setSelectedIndex(index - 1);
         } else {  // if the tab already exists, bring it to front by selecting the tab index. 
            contentTabbedPane.setSelectedIndex(index);
         }   
      }
   }
   
	class CustomProjectAction extends AbstractAction {
		protected CustomProjectAction(String type) {
			super("Manage Subscription", executeIcon);

			putValue(Action.DEFAULT, "EXECUTE");
			putValue(Action.SHORT_DESCRIPTION, "Manage STUDENT's Class Subscription"); // for ToolTip
			putValue("CUSTOM ACTION", type);

			this.setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("Run CUSTOM ACTION: " + getValue("CUSTOM ACTION"));

			// Prepare Student ComboBox
			ResultSet rs = dbAdapter.executeQuery("SELECT StudentNumber, StudentName FROM STUDENT ORDER BY StudentNumber ASC");
			Vector<String> studentList = new Vector<String>();
			try {
				while (rs.next()) { // add ComboBox item as 'StudentNumber: StudentName' format.
					studentList.add(rs.getInt("StudentNumber") + ": " + rs.getString("StudentName"));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				messagePanel.setMessage("Unable to initialize UI. Student's ComboBox becomes empty!");
			} finally {
				try {
					if (rs != null) {
						rs.close();
						rs = null;
					}
				} catch (Exception ignore) {
				}
			}
			
			// Prepare YearTerm ComboBox
			rs = dbAdapter.executeQuery("SELECT DISTINCT YearTerm FROM CLASS_OFFERING ORDER BY YearTerm ASC" );
			Vector<String> yrTr = new Vector<String>();
			try {
				while (rs.next()) {
					yrTr.add(rs.getString("YearTerm"));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				messagePanel.setMessage("Unable to initialize UI. YearTerm's ComboBox becomes empty!");
			} finally {
				try {
					if (rs != null) {
						rs.close();
						rs = null;
					}
				} catch (Exception ignore) {
				}
			}
			
			JPanel customUI = new JPanel();
			customUI.setLayout(new BorderLayout());

			JPanel topPanel = new JPanel();
			topPanel.setLayout(new GridLayout(1, 2));
			
			JPanel dummyPanel = new JPanel();
			dummyPanel.add(new JLabel("Choose a STUDENT: "));
			
			final JComboBox<String> studentCBox = new JComboBox<String>(studentList);
			studentCBox.setSelectedIndex(0); // set the first item selected.
			dummyPanel.add(studentCBox);
			
			topPanel.add(dummyPanel);  // add first component to the GridLayout.
			
			dummyPanel = new JPanel();
			dummyPanel.add(new JLabel("Choose a YearTerm: "));
			
			final JComboBox<String> yrTrCBox = new JComboBox<String>(yrTr);
			yrTrCBox.setSelectedIndex(yrTrCBox.getItemCount() - 1); // choose the latest YearTerm.
			dummyPanel.add(yrTrCBox);
			
			topPanel.add(dummyPanel);  // add second component to the GridLayout.
			
			customUI.add(topPanel, BorderLayout.NORTH);

			final JTable table = new JTable(getTableModel((String) studentCBox.getSelectedItem(), (String) yrTrCBox.getSelectedItem()));
			table.setAutoCreateRowSorter(true); // make this view be sortable!
			table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			table.setFillsViewportHeight(true); // fill the extra area by stretching JTable

			JScrollPane jsp = new JScrollPane(table);
			jsp.setBorder(new BevelBorder(BevelBorder.LOWERED));
			customUI.add(jsp, BorderLayout.CENTER);

			// add ItemListener to JComboBox to refresh JTable.
			studentCBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						table.setModel(getTableModel((String) studentCBox.getSelectedItem(), (String) yrTrCBox.getSelectedItem()));
					}
				}
			});
			
			yrTrCBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						table.setModel(getTableModel((String) studentCBox.getSelectedItem(), (String) yrTrCBox.getSelectedItem()));
					}
				}
			});

	        // create a button panel at the bottom
	         JPanel bottomButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	         
	         JButton addButton = new JButton("Add A Subscription", recordAdd);
	         addButton.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	               JOptionPane.showMessageDialog(DBAppWindow.this, "Not yet implemented!", "Add Subscription", JOptionPane.WARNING_MESSAGE);
	            }            
	         });
	         bottomButtons.add(addButton);
	         
	         JButton delButton = new JButton("Delete A Subscription", recordDelete);
	         delButton.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
		           JOptionPane.showMessageDialog(DBAppWindow.this, "Not yet implemented!", "Delete Subscription", JOptionPane.WARNING_MESSAGE);
	            }            
	         });
	         bottomButtons.add(delButton);       
	         
	         customUI.add(bottomButtons, BorderLayout.SOUTH);
	         
			// lookup the TabbedPane to find a tab with the same title string.
			int index = contentTabbedPane.indexOfTab((String) getValue(Action.NAME));
			if (index == -1) { // when there's no such tab, add a new tab.
				contentTabbedPane.addTab((String) getValue(Action.NAME), executeIcon, customUI, null, null);
				index = contentTabbedPane.getTabCount();
				contentTabbedPane.setSelectedIndex(index - 1);
			} else { // if the tab already exists, bring it to front by selecting the tab index.
				contentTabbedPane.setSelectedIndex(index);
			}
		}

		/**
		 * Get the TableModel from the database to refresh JTable.
		 * 
		 * @param studentComboValue input parameter (yearTerm) for getAveragePointResultTable stored procedure.
		 * @return
		 */
		private TableModel getTableModel(String studentComboValue, String yrTrComboValue) {
			Connection conn = dbAdapter.getConnection();
			String sID = studentComboValue.substring(0, studentComboValue.indexOf(":"));
			String sqlCommand = "SELECT ClassNumber, ClassSection, ClassName, HostDepartment, ClassTime, ProfessorName, ClassRoom "
					+ "FROM STUDENT_TO_CLASS "
					+ "WHERE StudentNumber = " + sID + " "
					+ "AND YearTerm = '" + yrTrComboValue + "' "
					+ "AND ClassOfferingNumber IS NOT NULL "
					+ "ORDER BY ClassNumber ASC, ClassSection ASC;";
			System.out.println("Custom Action (SQL): " + sqlCommand);
			
			return dbAdapter.fetchResultTable(sqlCommand, true);
		}
	}
   
   class AboutDeveloperAction extends AbstractAction {
      JPanel aboutUI;
      
      protected AboutDeveloperAction(String name) {
         super(name, aboutIcon);
                  
         putValue(Action.DEFAULT, "Developers");
         putValue(Action.SHORT_DESCRIPTION, "About developers...");  // for ToolTip
         this.setEnabled(true);
      }

      @Override
      public void actionPerformed(ActionEvent e) {
         if(aboutUI == null) {
            aboutUI = new JPanel(false);
            aboutUI.setLayout(new BoxLayout(aboutUI, BoxLayout.X_AXIS));

            JLabel authorImage = new JLabel(authorsIcon);
            aboutUI.add(authorImage);
            aboutUI.add(Box.createHorizontalStrut(7));   // insert an empty space.
            JPanel infoPanel = new JPanel(false);
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.add(new JLabel("DB Application for Demo"));
            infoPanel.add(Box.createVerticalStrut(7));  // insert an empty space.
            infoPanel.add(new JLabel("Developer: Author Who"));
            infoPanel.add(new JLabel("E-mail: noanswer@nowhere.com"));
            infoPanel.add(Box.createVerticalStrut(7));  // insert an empty space.
            infoPanel.add(new JLabel("Warning: don't distribute outside PKNU"));
            infoPanel.add(Box.createVerticalGlue());     // absorb vertical extra space.
            aboutUI.add(infoPanel);
         }
         
         JOptionPane.showMessageDialog(DBAppWindow.this, aboutUI, "Application Developers", JOptionPane.INFORMATION_MESSAGE);
      }
   }
   
   class AboutDBParametersAction extends AbstractAction {
      
      protected AboutDBParametersAction(String name) {
         super(name, aboutDBIcon);
                  
         putValue(Action.DEFAULT, "DB Parameters");
         putValue(Action.SHORT_DESCRIPTION, "About DB Parameters...");  // for ToolTip
         this.setEnabled(true);
      }

      @Override
      public void actionPerformed(ActionEvent e) {
         JPanel aboutUI = new JPanel(false);
         aboutUI.setLayout(new BoxLayout(aboutUI, BoxLayout.X_AXIS));

         if(appState == DBAppWindow.NOT_CONNECTED) {
            JPanel infoPanel = new JPanel(false);
            infoPanel.add(new JLabel("Application is not connected to any database."));

            aboutUI.add(infoPanel);
         } else if(dbType == DBAppWindow.DB_MSSQL) {
            JPanel namePanel = new JPanel(false);
            namePanel.setLayout(new GridLayout(0, 1));
            namePanel.add(new JLabel("Database Type: ", JLabel.RIGHT));
            namePanel.add(new JLabel("User ID: ", JLabel.RIGHT));
            namePanel.add(new JLabel("Database URL: ", JLabel.RIGHT));
            namePanel.add(new JLabel("DB Driver: ", JLabel.RIGHT));
            
            JPanel fieldPanel = new JPanel(false);
            fieldPanel.setLayout(new GridLayout(0, 1));
            
            JTextField tf = new JTextField("MS SQLServer");
            tf.setEnabled(false);
            fieldPanel.add(tf);
            
            tf = new JTextField(userName);
            tf.setEnabled(false);
            fieldPanel.add(tf);
            
            tf = new JTextField(serverURL);
            tf.setEnabled(false);
            fieldPanel.add(tf);
            
            tf = new JTextField(dbDriver);
            tf.setEnabled(false);
            fieldPanel.add(tf);
            
            aboutUI.add(namePanel);
            aboutUI.add(fieldPanel);
         } else {
            JPanel namePanel = new JPanel(false);
            namePanel.setLayout(new GridLayout(0, 1));
            namePanel.add(new JLabel("Database Type: ", JLabel.RIGHT));
            namePanel.add(new JLabel("Database URL: ", JLabel.RIGHT));
            namePanel.add(new JLabel("DB Driver: ", JLabel.RIGHT));
            
            JPanel fieldPanel = new JPanel(false);
            fieldPanel.setLayout(new GridLayout(0, 1));
            
            JTextField tf = new JTextField("SQLite DB");
            tf.setEnabled(false);
            fieldPanel.add(tf);
            
            tf = new JTextField(serverURL);
            tf.setEnabled(false);
            fieldPanel.add(tf);
            
            tf = new JTextField(dbDriver);
            tf.setEnabled(false);
            fieldPanel.add(tf);
            
            aboutUI.add(namePanel);
            aboutUI.add(fieldPanel);
         }
         
         JOptionPane.showMessageDialog(DBAppWindow.this, aboutUI, "DB Connection Parameters", JOptionPane.INFORMATION_MESSAGE);
      }
   }
}
