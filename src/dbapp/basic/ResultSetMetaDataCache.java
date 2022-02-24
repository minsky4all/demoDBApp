package dbapp.basic;

import java.io.Serializable;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class ResultSetMetaDataCache implements ResultSetMetaData, Serializable {
   private int _columnCount;
   private String[] _columnTypeName;
   private String[] _columnClassName;
   private int[] _scale;
   private String[] _columnLabel;
   private boolean[] _autoIncrement;
   private int[] _columnDisplaySize;
   private String[] _catalogName;
   private String[] _columnName;
   private boolean[] _writable;
   private boolean[] _searchable;
   private int[] _columnType;
   private boolean[] _currency;
   private String[] _tableName;
   private int[] _nullable;
   private boolean[] _signed;
   private boolean[] _readOnly;
   private boolean[] _definitelyWritable;
   private int[] _precision;
   private String[] _schemaName;
   private boolean[] _caseSensitive;

   // used for storing error information from when getColumnClassName() fails
   private String _sqlReason;
   private String _sqlState;
   private int _sqlVendorCode;

   public static ResultSetMetaData getCopy(ResultSetMetaData original) throws SQLException {
      if (original instanceof ResultSetMetaDataCache)  // already copied ResultSetMetaData!
         return original;
      else
         return new ResultSetMetaDataCache(original);
   }

   private ResultSetMetaDataCache(ResultSetMetaData other) throws SQLException {
      _columnCount = other.getColumnCount();

      _columnTypeName = new String[_columnCount];
      _columnClassName = new String[_columnCount];
      _scale = new int[_columnCount];
      _columnLabel = new String[_columnCount];
      _autoIncrement = new boolean[_columnCount];
      _columnDisplaySize = new int[_columnCount];
      _catalogName = new String[_columnCount];
      _columnName = new String[_columnCount];
      _writable = new boolean[_columnCount];
      _searchable = new boolean[_columnCount];
      _columnType = new int[_columnCount];
      _currency = new boolean[_columnCount];
      _tableName = new String[_columnCount];
      _nullable = new int[_columnCount];
      _signed = new boolean[_columnCount];
      _readOnly = new boolean[_columnCount];
      _definitelyWritable = new boolean[_columnCount];
      _precision = new int[_columnCount];
      _schemaName = new String[_columnCount];
      _caseSensitive = new boolean[_columnCount];

      for(int c = 0; c < _columnCount; c++) {
         _columnTypeName[c] = other.getColumnTypeName(c + 1);

         if(_columnClassName != null) { // this only works on JDBC compliant drivers
            try {
               _columnClassName[c] = other.getColumnClassName(c + 1);
            } catch (SQLException x) {
               _columnClassName = null; // don't try again!
               _sqlReason = x.getMessage();
               _sqlState = x.getSQLState();
               _sqlVendorCode = x.getErrorCode();
            } catch (Throwable e) {
               _columnClassName = null; // don't try again!
            }
         }

         _scale[c] = other.getScale(c + 1);
         _columnLabel[c] = other.getColumnLabel(c + 1);
         _autoIncrement[c] = other.isAutoIncrement(c + 1);
         _columnDisplaySize[c] = other.getColumnDisplaySize(c + 1);

         try {
            _catalogName[c] = other.getCatalogName(c + 1);
         } catch (Exception e) {
         }
         
         if (_catalogName[c] == null)
            _catalogName[c] = "";

         _columnName[c] = other.getColumnName(c + 1);
         _writable[c] = other.isWritable(c + 1);
         _searchable[c] = other.isSearchable(c + 1);
         _columnType[c] = other.getColumnType(c + 1);
         _currency[c] = other.isCurrency(c + 1);

         try {
            _tableName[c] = other.getTableName(c + 1);
         } catch (Exception e) {
         }
         
         if (_tableName[c] == null)
            _tableName[c] = "";

         _nullable[c] = other.isNullable(c + 1);
         _signed[c] = other.isSigned(c + 1);
         _readOnly[c] = other.isReadOnly(c + 1);
         _definitelyWritable[c] = other.isDefinitelyWritable(c + 1);
         _precision[c] = other.getPrecision(c + 1);

         try {
            _schemaName[c] = other.getSchemaName(c + 1);
         } catch (Exception e) {
         }
         
         if (_schemaName[c] == null)
            _schemaName[c] = "";

         _caseSensitive[c] = other.isCaseSensitive(c + 1);
      }
   }

   public int getColumnCount() throws SQLException {
      return _columnCount;
   }

   public String getColumnTypeName(int column) throws SQLException {
      return _columnTypeName[column - 1];
   }

   public String getColumnClassName(int column) throws SQLException {
      if (_columnClassName == null) {
         // java.sql.ResultSetMetaData.getColumnClassName(int col) requires a JDBC 2 compliant database driver.
         throw new SQLException(_sqlReason, _sqlState, _sqlVendorCode);
      }
      return _columnClassName[column - 1];
   }

   public int getScale(int column) throws SQLException {
      return _scale[column - 1];
   }

   public java.lang.String getColumnLabel(int column) throws SQLException {
      return _columnLabel[column - 1];
   }

   public boolean isAutoIncrement(int column) throws SQLException {
      return _autoIncrement[column - 1];
   }

   public int getColumnDisplaySize(int column) throws SQLException {
      return _columnDisplaySize[column - 1];
   }

   public String getCatalogName(int column) throws SQLException {
      return _catalogName[column - 1];
   }

   public String getColumnName(int column) throws SQLException {
      return _columnName[column - 1];
   }

   public boolean isWritable(int column) throws SQLException {
      return _writable[column - 1];
   }

   public boolean isSearchable(int column) throws SQLException {
      return _searchable[column - 1];
   }

   public int getColumnType(int column) throws SQLException {
      return _columnType[column - 1];
   }

   public boolean isCurrency(int column) throws SQLException {
      return _currency[column - 1];
   }

   public String getTableName(int column) throws SQLException {
      return _tableName[column - 1];
   }

   public int isNullable(int column) throws SQLException {
      return _nullable[column - 1];
   }

   public boolean isSigned(int column) throws SQLException {
      return _signed[column - 1];
   }

   public boolean isReadOnly(int column) throws SQLException {
      return _readOnly[column - 1];
   }

   public boolean isDefinitelyWritable(int column) throws SQLException {
      return _definitelyWritable[column - 1];
   }

   public int getPrecision(int column) throws SQLException {
      return _precision[column - 1];
   }

   public String getSchemaName(int column) throws SQLException {
      return _schemaName[column - 1];
   }

   public boolean isCaseSensitive(int column) throws SQLException {
      return _caseSensitive[column - 1];
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      if (isWrapperFor(iface)) {
         return (T) this;
      }
      throw new SQLException("No wrapper for " + iface);
   }

   @Override
   public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return iface != null && iface.isAssignableFrom(getClass());
   }
}
