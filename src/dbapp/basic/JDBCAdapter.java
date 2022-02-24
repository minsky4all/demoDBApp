package dbapp.basic;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class JDBCAdapter {
   private boolean valid = false;
   
   private Connection connection;
   private Statement statement;
   
   /**
    * For SQLite DB
    * Since SQLite always create a new database if there's no such file,
    * we need to check the existence of target database before connect.
    * 
    * @param url
    * @param driverName
    */
   public JDBCAdapter(String url, String driverName) {
      if(url.startsWith("jdbc:sqlite:")) {
         String filePath = url.substring("jdbc:sqlite:".length());
         System.out.println("SQLite DB path = " + filePath);
         File dbFile = new File(filePath);
         if(!dbFile.exists() || !dbFile.isFile()) {
            System.err.println("Cannot find the database file: " + filePath);
            valid = false;
         } else {  // Now, try to connect to database.
            try {
               Class.forName(driverName);
               System.out.println("Opening db connection");

               connection = DriverManager.getConnection(url);
               statement = connection.createStatement();
            } catch (ClassNotFoundException ex) {
               System.err.println("Cannot find the database driver classes.");
               System.err.println(ex);
            } catch (SQLException ex) {
               System.err.println("Cannot connect to this database.");
               System.err.println(ex);
            } finally {
               if(connection != null && statement != null)
                  valid = true;
            }
         }
      }
   }
   
   /**
    * For MS SQLServer
    * 
    * @param url
    * @param driverName
    * @param user
    * @param passwd
    */
   public JDBCAdapter(String url, String driverName, String user, String passwd) {
      try {
         Class.forName(driverName);
         System.out.println("Opening db connection");

         connection = DriverManager.getConnection(url, user, passwd);
         statement = connection.createStatement();
      } catch (ClassNotFoundException ex) {
         System.err.println("Cannot find the database driver classes.");
         System.err.println(ex);
      } catch (SQLException ex) {
         System.err.println("Cannot connect to this database.");
         System.err.println(ex);
      } finally {
         if(connection != null && statement != null)
            valid = true;
      }
   }
   
   public boolean isValid() {
      return valid;
   }
   
   /**
    * Get current Connection object for subsequent Statement/PreparedStatement/CallableStatement object creation.
    * Beware to use it serially since there's no synchronized mechanism for this connection's sharing.
    * 
    * @return java.sql.Connection object
    */
   public Connection getConnection() {
      return this.connection;
   }
   
   public ResultSet executeQuery(String query) {
      if (connection == null || statement == null) {
         System.err.println("There is no database to execute the query.");
         
         return null;
      }
      
      ResultSet rs = null;
      try {
         rs = statement.executeQuery(query);
      } catch (SQLException e) {
         e.printStackTrace();
         rs = null;
      }
      
      return rs;
   }
   
   /**
    * Obtain a table by executing query string.
    * Returned JTable will be read-only.
    * 
    * @param query
    * @return
    */
   public JDBCTable fetchResultTable(String query) {
      return new JDBCTable(executeQuery(query), true);
   }
   
   /**
    * Obtain a table by executing query string.
    * Returned JTable will be set read-only or not by 'readOnly' parameter value.
    * 
    * @param query Query string to execute and fill the table data.
    * @param readOnly indicate whether returned JDBCTable is read-only or not.
    * 
    * @return
    */
   public JDBCTable fetchResultTable(String query, boolean readOnly) {
      return new JDBCTable(executeQuery(query), readOnly);
   }
   
   /**
    * Fill a table by populating ResultSet's row data.
    * Returned JTable will be set read-only or not by 'readOnly' parameter value.
    * 
    * @param query Query string to execute and fill the table data.
    * @param readOnly indicate whether returned JDBCTable is read-only or not.
    * 
    * @return
    */
   public JDBCTable fetchResultTable(ResultSet rs, boolean readOnly) {
      return new JDBCTable(rs, readOnly);
   }
   
   /**
    * Fetch a whole table by indicating tableName.
    * 
    * @param tableName
    * @param readOnly
    * @return
    */
   public JDBCTable fetchWholeTable(String tableName, boolean readOnly) {
      return new JDBCTable(tableName, readOnly);
   }
   
   public void close() throws SQLException {
      System.out.println("Closing db connection");

      statement.close();
      connection.close();
   }

   @Override
   protected void finalize() throws Throwable {
      close();
      super.finalize();
   }
   
   public class JDBCTable extends DefaultTableModel {
      String tableName = null;
      String queryString = null;
      ResultSet resultSet;
      ResultSetMetaData metaData;
      boolean readOnly = false;
      
      private JDBCTable() {}
      
      private JDBCTable(String tName, boolean read) {
         this(tName, "SELECT * FROM " + tName + ";", read);
      }
      
      private JDBCTable(String tName, String sql, boolean read) {
         this.tableName = tName;
         this.queryString = sql;
         this.readOnly = read;
         
         this.resultSet = executeQuery(queryString);  // this can return null.
         if(resultSet != null)
            fillTableModel(resultSet);
      }
            
      private JDBCTable(ResultSet rs) {
         this(rs, false);   // make it read-only.
      }
      
      private JDBCTable(ResultSet rs, boolean read) {
         this.resultSet = rs;
         this.readOnly = read;
         
         if(this.resultSet != null)
            fillTableModel(resultSet);
      }

      /**
       * It does not check whether ResultSet object is null or not.
       * So, argument rs should not be null.
       * 
       * @param rs
       */
      private void fillTableModel(ResultSet rs) {
         try {
            // Copy ResultSetMetaData before, since some JDBC drivers issue errors when MetaData is accessed later.
            this.metaData = ResultSetMetaDataCache.getCopy(resultSet.getMetaData()); 
     
            int numberOfColumns = metaData.getColumnCount();
            String[] columnNames = new String[numberOfColumns];
            
            // Get the column names and cache them, then we can safely close the connection.
            for (int column = 0; column < numberOfColumns; column++)
               columnNames[column] = metaData.getColumnLabel(column + 1);
            
            // Set the column names.
            setColumnIdentifiers(columnNames);
            
            // Get all rows from database's ResultSet.
            while (resultSet.next()) {
               Vector<Object> newRow = new Vector<Object>();
               for (int i = 1; i <= getColumnCount(); i++)
                  newRow.add(resultSet.getObject(i));
               
               // add a new row to model.
               addRow(newRow);
            }
            
            // Need to copy the metaData (already done) before close.
            // Bugs in some jdbc drivers like jdbc:odbc driver.
            resultSet.close(); 
         } catch (SQLException e) {
            e.printStackTrace();            
         }

         // Tell the listeners a new table has arrived.
         fireTableChanged(null);
      }
      
      public void setTableName(String n) {
         setTableName(n, false);
      }
      
      public void setTableName(String n, boolean refreshModel) {
         this.tableName = n;
         if(refreshModel) {
            this.queryString = "SELECT * FROM " + tableName + ";";
            
            this.resultSet = executeQuery(queryString);  // this can return null.
            if(resultSet != null)
               fillTableModel(resultSet);
         }            
      }
      
      public String getTableName() {
         return this.tableName;
      }
      
      public void setQuery(String sql) {
         setQuery(sql, false);
      }
      
      public void setQuery(String sql, boolean refreshModel) {
         this.queryString = sql;
         if(refreshModel) {
            this.resultSet = executeQuery(queryString);  // this can return null.
            if(resultSet != null)
               fillTableModel(resultSet);
         }
      }
      
      @Override
      public Class<?> getColumnClass(int column) {
         int type;
         
         try {
            type = metaData.getColumnType(column + 1);
         } catch (SQLException e) {
            return super.getColumnClass(column);
         }

         switch (type) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
               return String.class;

            case Types.BIT:
               return Boolean.class;

            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
               return Integer.class;

            case Types.BIGINT:
               return Long.class;

            case Types.FLOAT:
            case Types.DOUBLE:
               return Double.class;

            case Types.DATE:
               return java.sql.Date.class;

            default:
               return Object.class;
         }
      }

      @Override
      public boolean isCellEditable(int row, int column) {
         if(readOnly)  // not editable!
            return false;
         
         try {
            return metaData.isWritable(column + 1);
         } catch (SQLException e) {
            return false;
         }
      }
      
      public String dbRepresentation(int column, Object value) {
         int type;

         if (value == null || "".equals(value)) {
            return "NULL";   // should change to 'IS NULL' phrase later.
         }

         try {
            type = metaData.getColumnType(column + 1);
         } catch (SQLException e) {
            return value.toString();
         }

         switch (type) {
            case Types.INTEGER:
            case Types.DOUBLE:
            case Types.FLOAT:
               return value.toString();
            case Types.BIT:
               return ((Boolean) value).booleanValue() ? "1" : "0";
            case Types.DATE:
               return value.toString(); // This will need some conversion.
            default:
               return "'" + value.toString().trim() + "'";  // SQL uses single(') quotation for string literals.
         }
      }

      @Override
      public void setValueAt(Object value, int row, int column) {
         int numUpdate = -1;
         try {
            // Most JDBC drivers do not support getTableName() method, so we need to memorize table name before!
            if(this.tableName == null) {
               String tN = metaData.getTableName(column + 1);
               if(tN != null && !tN.trim().equals(""))
                  this.tableName = tN;
            }
            
            // check it again!
            if(tableName != null) {
               String columnName = getColumnName(column);
               String query = "UPDATE " + tableName + " SET " + columnName + " = " + dbRepresentation(column, value) + " WHERE ";
               String subsequentQuery = "";
               
               // We don't have a model of the schema so we don't know the primary keys or which columns to lock on.
               // To demonstrate that editing is possible, we'll just lock on everything.
               for (int col = 0; col < getColumnCount(); col++) {
                  String colName = getColumnName(col);
                  if (colName.equals("")) {
                     continue;
                  }
                  
                  if (col != 0) {
                     subsequentQuery += " AND ";
                  }
                  subsequentQuery += colName + " = " + dbRepresentation(col, getValueAt(row, col));
               }
               
               subsequentQuery = subsequentQuery.replaceAll(" = NULL", " IS NULL");
               query += subsequentQuery;
               System.out.println("Query: " + query);
               
               numUpdate = statement.executeUpdate(query);
               System.out.println("Number of affected rows should be 1: " + numUpdate);
               if(numUpdate == 1) {   // check if '1' to make it sure.
                  Vector rowVector = (Vector) dataVector.elementAt(row);
                  rowVector.setElementAt(value, column);
                  
                  fireTableCellUpdated(row, column);   // notify change! 
               }
            } else {
               System.out.println("Table name returned null.");
               
               // revoke updated view without applying change.               
               fireTableCellUpdated(row, column);
            }            
         } catch (SQLException e) {
            System.err.println("Update failed");
            e.printStackTrace();
            
            // revoke updated view to old value without applying change.
            fireTableCellUpdated(row, column);
         }
      }
      
      @Override
      public void removeRow(int row) {
         int numDelete = -1;
         if (this.tableName != null) {
            String query = "DELETE FROM " + this.tableName + " WHERE ";
            String subsequentQuery = "";

            // We don't have a model of the schema so we don't know the primary keys or which columns to lock on.
            // To demonstrate that editing is possible, we'll just lock on everything.
            for (int col = 0; col < getColumnCount(); col++) {
               String colName = getColumnName(col);
               if (colName.equals(""))
                  continue;

               if (col != 0)
                  subsequentQuery += " AND ";

               subsequentQuery += colName + " = "
                     + dbRepresentation(col, getValueAt(row, col));
            }

            subsequentQuery = subsequentQuery.replaceAll(" = NULL", " IS NULL");
            query += subsequentQuery;
            System.out.println("Query: " + query);

            try {
               numDelete = statement.executeUpdate(query);
               System.out.println("Number of affected rows should be 1: " + numDelete);
               if (numDelete == 1) { // check if '1' to make it sure.
                  dataVector.removeElementAt(row);
                  fireTableRowsDeleted(row, row);
               }
            } catch (SQLException e) {
               System.err.println("Delete failed");
               e.printStackTrace();
            }
         }
      }
      
      public void addDBTableRow(Vector rowData) {
         int numInsert = -1;
         if(this.tableName != null) {
            String query = "INSERT INTO " + this.tableName + " VALUES (";
            String subsequentQuery = "";
          
            for (int col = 0; col < getColumnCount(); col++) {               
               subsequentQuery += dbRepresentation(col, rowData.get(col));
                     
               if (col < getColumnCount() - 1)
                  subsequentQuery += ", ";
               else
                  subsequentQuery += ");";               
            }
            
            query += subsequentQuery;
            System.out.println("Query: " + query);
            
            try {
               numInsert = statement.executeUpdate(query);
               System.out.println("Number of affected rows should be 1: " + numInsert);
               if(numInsert == 1)  // check if '1' to make it sure.
                  super.addRow(rowData);
            } catch(SQLException e) {
               System.err.println("Insert failed");
               e.printStackTrace();
            }
         }
      }
   }
}
