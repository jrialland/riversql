package com.riversql.sql;

import com.riversql.databases.DialectFactory;
import com.riversql.sql.dbobj.BestRowIdentifier;
import com.riversql.utils.ResultSetReader;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
//import com.riversql.util.log.ILogger;
//import com.riversql.util.log.LoggerController;

/**
 * This class represents the metadata for a database. It is essentially
 * a wrapper around <TT>java.sql.DatabaseMetaData</TT>.
 *
 * <P>Some data can be cached on the first retrieval in order to speed up
 * subsequent retrievals. To clear this cache call <TT>clearCache()</TT>.
 *
 * <P>From the JavaDoc for <TT>java.sql.DatabaseMetaData</TT>. &quot;Some
 * methods take arguments that are String patterns. These arguments all
 * have names such as fooPattern. Within a pattern String, "%" means match any
 * substring of 0 or more characters, and "_" means match any one character. Only
 * metadata entries matching the search pattern are returned. If a search pattern
 * argument is set to null, that argument's criterion will be dropped from the
 * search.&quot;
 *
 * <P>Additionally, it should be noted that some JDBC drivers (like Oracle) do
 * not handle multi-threaded access to methods that return ResultSets very well.
 * It is therefore highly recommended that methods in this class that return
 * a ResultSet, should not be called outside of this class where this class'
 * monitor has no jurisdiction.  Furthermore, methods that are meant to be
 * called externally that create a ResultSet should package the data in some
 * container object structure for use by the caller, and should always be
 * synchronized on this class' monitor.
 *
 * @author <A HREF="mailto:colbell@users.sourceforge.net">Colin Bell</A>
 */
public class SQLDatabaseMetaData {
    /**
     * Connection to database this class is supplying information for.
     */
    private SQLConnection _conn;

    /** Logger for this class. */
//	private final static ILogger s_log =
//		LoggerController.createLogger(SQLDatabaseMetaData.class);
    /**
     * Cache of commonly accessed metadata properties keyed by the method
     * name that attempts to retrieve them.
     * Note, this cache should only be used for metadata that are not
     * likely to be changed during an open Session.
     * Meta data that is likely to be changed should be kept in SchemaInfo.
     */
    private Map<String, Object> _cache =
            Collections.synchronizedMap(new HashMap<String, Object>());
    /**
     * If previous attempts to getSuperTables fail, then this will be set to
     * false, and prevent further attempts.
     */
    private boolean supportsSuperTables = true;

    /**
     * ctor specifying the connection that we are retrieving metadata for.
     *
     * @param    conn    Connection to database.
     * @throws IllegalArgumentException Thrown if null SQLConnection passed.
     */
    SQLDatabaseMetaData(SQLConnection conn) {
        super();
        if (conn == null) {
            throw new IllegalArgumentException("SQLDatabaseMetaData == null");
        }
        _conn = conn;
    }

    /**
     * Make a String array of the passed string. Commas separate the elements
     * in the input string. The array is sorted.
     *
     * @param    data    Data to be split into the array.
     * @return data as an array.
     */
    private static String[] makeArray(String data) {
        if (data == null) {
            data = "";
        }

        final List<String> list = new ArrayList<String>();
        final StringTokenizer st = new StringTokenizer(data, ",");
        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        Collections.sort(list);

        return list.toArray(new String[list.size()]);
    }

    private void close(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
//        s_log.info("closing resultset failed", e);
        }
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getUserName()
     */
    public synchronized String getUserName() throws SQLException {
        final String key = "getUserName";
        String value = (String) _cache.get(key);
        if (value == null) {
            value = privateGetJDBCMetaData().getUserName();
            _cache.put(key, value);
        }
        return value;
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getDatabaseProductName()
     */
    public synchronized String getDatabaseProductName()
            throws SQLException {
        final String key = "getDatabaseProductName";
        String value = (String) _cache.get(key);
        if (value == null) {
            value = privateGetJDBCMetaData().getDatabaseProductName();
            _cache.put(key, value);
        }
        return value;
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getDatabaseProductVersion()
     */
    public synchronized String getDatabaseProductVersion()
            throws SQLException {
        final String key = "getDatabaseProductVersion";
        String value = (String) _cache.get(key);
        if (value == null) {
            value = privateGetJDBCMetaData().getDatabaseProductVersion();
            _cache.put(key, value);
        }
        return value;
    }

    public synchronized int getDatabaseMajorVersion()
            throws SQLException {
        final String key = "getDatabaseMajorVersion";
        Integer value = (Integer) _cache.get(key);
        if (value == null) {
            value = privateGetJDBCMetaData().getDatabaseMajorVersion();
            _cache.put(key, value);
        }
        return value;
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getDriverName()
     */
    public synchronized String getDriverName() throws SQLException {
        final String key = "getDriverName";
        String value = (String) _cache.get(key);
        if (value == null) {
            value = privateGetJDBCMetaData().getDriverName();
            _cache.put(key, value);
        }
        return value;
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getJDBCVersion()
     */
    public int getJDBCVersion() throws SQLException {
        final String key = "getJDBCVersion";
        Integer value = (Integer) _cache.get(key);
        if (value == null) {
            DatabaseMetaData md = privateGetJDBCMetaData();
            int major = md.getJDBCMajorVersion();
            int minor = md.getJDBCMinorVersion();
            int vers = (major * 100) + minor;
            value = Integer.valueOf(vers);
            _cache.put(key, value);
        }
        return value.intValue();
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getIdentifierQuoteString()
     */
    public synchronized String getIdentifierQuoteString() throws SQLException {
        final String key = "getIdentifierQuoteString";
        String value = (String) _cache.get(key);
        if (value == null) {
            value = privateGetJDBCMetaData().getIdentifierQuoteString();
            if (value == null) {
                value = "";
            }
            _cache.put(key, value);
        }
        return value;
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getCascadeClause()
     */
    public synchronized String getCascadeClause() throws SQLException {
        final String key = "getCascadeClause";
        String value = (String) _cache.get(key);
        if (value == null) {
            if (DialectFactory.isDB2(_conn) || DialectFactory.isOracle(_conn)) {
                value = "CASCADE";
            } else {
                value = "";
            }
            _cache.put(key, value);
        }
        return value;
    }

    /**
     * @see com.riversql.sql.ISQLDatabaseMetaData#getSchemas()
     */
    public synchronized String[] getSchemas() throws SQLException {

        boolean hasGuest = false;
        boolean hasSysFun = false;

        final boolean isMSSQLorSYBASE = DialectFactory.isSyBase(this._conn)
                || DialectFactory.isMSSQL(this._conn);

        final boolean isDB2 = DialectFactory.isDB2(this._conn);

        final ArrayList<String> list = new ArrayList<String>();
        ResultSet rs = privateGetJDBCMetaData().getSchemas();
        try {
            if (rs != null) {
                // DialectType dialectType = DialectFactory.getDialectType(this);
                final ResultSetReader rdr = new ResultSetReader(rs);//, dialectType);
                Object[] row = null;
                while ((row = rdr.readRow()) != null) {
                    if (isMSSQLorSYBASE && row[0].equals("guest")) {
                        hasGuest = true;
                    }
                    if (isDB2 && row[0].equals("SYSFUN")) {
                        hasSysFun = true;
                    }
                    list.add((String) row[0]);
                }
            }
        } finally {
            close(rs);
        }

        // Some drivers for both MS SQL and Sybase don't return guest as
        // a schema name.
        if (isMSSQLorSYBASE && !hasGuest) {
            list.add("guest");
        }

        // Some drivers for DB2 don't return SYSFUN as a schema name. A
        // number of system stored procs are kept in this schema.
        if (isDB2 && !hasSysFun) {
            list.add("SYSFUN");
        }

        return list.toArray(new String[list.size()]);
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#supportsSchemas()
     */
    public boolean supportsSchemas() throws SQLException {
        return supportsSchemasInDataManipulation()
                || supportsSchemasInTableDefinitions();
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#supportsSchemasInDataManipulation()
     */
    public synchronized boolean supportsSchemasInDataManipulation()
            throws SQLException {
        final String key = "supportsSchemasInDataManipulation";
        Boolean value = (Boolean) _cache.get(key);
        if (value != null) {
            return value.booleanValue();
        }

        try {
            value = Boolean.valueOf(privateGetJDBCMetaData().supportsSchemasInDataManipulation());
        } catch (SQLException ex) {
            boolean isSQLServer =
                    DialectFactory.isSyBase(this._conn) || DialectFactory.isMSSQL(this._conn);

            if (isSQLServer) {
                value = Boolean.TRUE;
                _cache.put(key, value);
            }
            throw ex;
        }

        _cache.put(key, value);

        return value.booleanValue();
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#supportsSchemasInTableDefinitions()
     */
    public synchronized boolean supportsSchemasInTableDefinitions()
            throws SQLException {
        final String key = "supportsSchemasInTableDefinitions";
        Boolean value = (Boolean) _cache.get(key);
        if (value != null) {
            return value.booleanValue();
        }

        try {
            value = Boolean.valueOf(privateGetJDBCMetaData().supportsSchemasInTableDefinitions());
        } catch (SQLException ex) {
            boolean isSQLServer =
                    DialectFactory.isSyBase(this._conn) || DialectFactory.isMSSQL(this._conn);
            if (isSQLServer) {
                value = Boolean.TRUE;
                _cache.put(key, value);
            }
            throw ex;
        }

        _cache.put(key, value);

        return value.booleanValue();
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#supportsStoredProcedures()
     */
    public synchronized boolean supportsStoredProcedures() throws SQLException {
        final String key = "supportsStoredProcedures";
        Boolean value = (Boolean) _cache.get(key);
        if (value != null) {
            return value.booleanValue();
        }

        // PostgreSQL (at least 7.3.2) returns false for
        // supportsStoredProcedures() even though it does support them.
        if (DialectFactory.isPostgreSQL(this._conn)) {
            value = Boolean.TRUE;
        } else {
            value = Boolean.valueOf(privateGetJDBCMetaData().supportsStoredProcedures());
        }
        _cache.put(key, value);

        return value.booleanValue();
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#supportsSavepoints()
     */
    public synchronized boolean supportsSavepoints() throws SQLException {

        final String key = "supportsSavepoints";
        Boolean value = (Boolean) _cache.get(key);
        if (value != null) {
            return value.booleanValue();
        }
        value = Boolean.valueOf(privateGetJDBCMetaData().supportsSavepoints());

        _cache.put(key, value);

        return value.booleanValue();
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#supportsResultSetType(int)
     */
    public synchronized boolean supportsResultSetType(int type)
            throws SQLException {
        final String key = "supportsResultSetType";
        Boolean value = (Boolean) _cache.get(key);
        if (value != null) {
            return value.booleanValue();
        }
        value = Boolean.valueOf(privateGetJDBCMetaData().supportsResultSetType(type));

        _cache.put(key, value);

        return value.booleanValue();
    }

    /* Not cached.
     * (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getCatalogs()
     */
    public synchronized String[] getCatalogs() throws SQLException {
        final ArrayList<String> list = new ArrayList<String>();
        ResultSet rs = privateGetJDBCMetaData().getCatalogs();
        try {
            if (rs != null) {
                //DialectType dialectType = DialectFactory.getDialectType(this);
                final ResultSetReader rdr = new ResultSetReader(rs);
                Object[] row = null;
                while ((row = rdr.readRow()) != null) {
                    if (row != null && row[0] != null) {
                        list.add(row[0].toString());
                    }
                }
            }
        } finally {
            close(rs);
        }

        return list.toArray(new String[list.size()]);
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getURL()
     */
    public synchronized String getURL() throws SQLException {
        final String key = "getURL";
        String value = (String) _cache.get(key);
        if (value != null) {
            return value;
        }

        value = privateGetJDBCMetaData().getURL();
        _cache.put(key, value);

        return value;
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getCatalogTerm()
     */
    public synchronized String getCatalogTerm() throws SQLException {
        final String key = "getCatalogTerm";
        String value = (String) _cache.get(key);
        if (value != null) {
            return value;
        }

        value = privateGetJDBCMetaData().getCatalogTerm();
        _cache.put(key, value);

        return value;
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getSchemaTerm()
     */
    public synchronized String getSchemaTerm() throws SQLException {
        final String key = "getSchemaTerm";
        String value = (String) _cache.get(key);
        if (value != null) {
            return value;
        }

        value = privateGetJDBCMetaData().getSchemaTerm();
        _cache.put(key, value);

        return value;
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getProcedureTerm()
     */
    public synchronized String getProcedureTerm() throws SQLException {
        final String key = "getProcedureTerm";
        String value = (String) _cache.get(key);
        if (value != null) {
            return value;
        }

        value = privateGetJDBCMetaData().getProcedureTerm();
        _cache.put(key, value);

        return value;
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getCatalogSeparator()
     */
    public synchronized String getCatalogSeparator() throws SQLException {
        final String key = "getCatalogSeparator";
        String value = (String) _cache.get(key);
        if (value != null) {
            return value;
        }

        value = privateGetJDBCMetaData().getCatalogSeparator();
        _cache.put(key, value);

        return value;
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#supportsCatalogs()
     */
    public boolean supportsCatalogs() throws SQLException {
        return supportsCatalogsInTableDefinitions()
                || supportsCatalogsInDataManipulation()
                || supportsCatalogsInProcedureCalls();
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#supportsCatalogsInTableDefinitions()
     */
    public synchronized boolean supportsCatalogsInTableDefinitions() throws SQLException {
        final String key = "supportsCatalogsInTableDefinitions";
        Boolean value = (Boolean) _cache.get(key);
        if (value != null) {
            return value.booleanValue();
        }

        try {
            value = Boolean.valueOf(privateGetJDBCMetaData().supportsCatalogsInTableDefinitions());
        } catch (SQLException ex) {
            boolean isSQLServer =
                    DialectFactory.isSyBase(this._conn) || DialectFactory.isMSSQL(this._conn);

            if (isSQLServer) {
                value = Boolean.TRUE;
                _cache.put(key, value);
            }
            throw ex;
        }

        _cache.put(key, value);

        return value.booleanValue();
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#supportsCatalogsInDataManipulation()
     */
    public synchronized boolean supportsCatalogsInDataManipulation() throws SQLException {
        final String key = "supportsCatalogsInDataManipulation";
        Boolean value = (Boolean) _cache.get(key);
        if (value != null) {
            return value.booleanValue();
        }

        try {
            value = Boolean.valueOf(privateGetJDBCMetaData().supportsCatalogsInDataManipulation());
        } catch (SQLException ex) {
            boolean isSQLServer =
                    DialectFactory.isSyBase(this._conn) || DialectFactory.isMSSQL(this._conn);

            if (isSQLServer) {
                value = Boolean.TRUE;
                _cache.put(key, value);
            }
            throw ex;
        }
        _cache.put(key, value);

        return value.booleanValue();
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#supportsCatalogsInProcedureCalls()
     */
    public synchronized boolean supportsCatalogsInProcedureCalls() throws SQLException {
        final String key = "supportsCatalogsInProcedureCalls";
        Boolean value = (Boolean) _cache.get(key);
        if (value != null) {
            return value.booleanValue();
        }

        try {
            value = Boolean.valueOf(privateGetJDBCMetaData().supportsCatalogsInProcedureCalls());
        } catch (SQLException ex) {
            boolean isSQLServer =
                    DialectFactory.isSyBase(this._conn) || DialectFactory.isMSSQL(this._conn);

            if (isSQLServer) {
                value = Boolean.TRUE;
                _cache.put(key, value);
            }
            throw ex;
        }
        _cache.put(key, value);

        return value.booleanValue();
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getJDBCMetaData()
     */
    public synchronized DatabaseMetaData getJDBCMetaData() throws SQLException {
        return privateGetJDBCMetaData();
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getTypeInfo()
     *
     * @deprecated  Replaced by getDataTypes
     */
    public ResultSet getTypeInfo() throws SQLException {
        return privateGetJDBCMetaData().getTypeInfo();
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getDataTypes()
     */
    public synchronized DataTypeInfo[] getDataTypes()
            throws SQLException {
        final DatabaseMetaData md = privateGetJDBCMetaData();
        final ArrayList<DataTypeInfo> list = new ArrayList<DataTypeInfo>();
        final ResultSet rs = md.getTypeInfo();
        try {
            ResultSetColumnReader rdr = new ResultSetColumnReader(rs);
            while (rdr.next()) {
                final String typeName = rdr.getString(1);
                final int dataType = rdr.getLong(2).intValue();
                final int precis = rdr.getLong(3).intValue();
                final String literalPrefix = rdr.getString(4);
                final String literalSuffix = rdr.getString(5);
                final String createParams = rdr.getString(6);
                final int nullable = rdr.getLong(7).intValue();
                final boolean caseSens = rdr.getBoolean(8).booleanValue();
                final int searchable = rdr.getLong(9).intValue();
                final boolean unsigned = rdr.getBoolean(10).booleanValue();
                final boolean canBeMoney = rdr.getBoolean(11).booleanValue();
                final boolean canBeAutoInc = rdr.getBoolean(12).booleanValue();
                final String localTypeName = rdr.getString(13);
                final int min = rdr.getLong(14).intValue();
                final int max = rdr.getLong(15).intValue();
                final int radix = rdr.getLong(18).intValue();
                list.add(new DataTypeInfo(typeName, dataType, precis,
                        literalPrefix, literalSuffix,
                        createParams, nullable, caseSens,
                        searchable, unsigned, canBeMoney,
                        canBeAutoInc, localTypeName,
                        min, max, radix,
                        this));
            }
        } finally {
            close(rs);
        }
        return list.toArray(new DataTypeInfo[list.size()]);
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getMetaDataSet()
     */
//    public synchronized IDataSet getMetaDataSet() throws SQLException {
//        return new MetaDataDataSet(privateGetJDBCMetaData());
//    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getTableTypes()
     */
    public synchronized String[] getTableTypes() throws SQLException {
        final String key = "getTableTypes";
        String[] value = (String[]) _cache.get(key);
        if (value != null) {
            return value;
        }

        final DatabaseMetaData md = privateGetJDBCMetaData();

        // Use a set rather than a list as some combinations of MS SQL and the
        // JDBC/ODBC return multiple copies of each table type.
        final Set<String> tableTypes = new TreeSet<String>();
        final ResultSet rs = md.getTableTypes();
        if (rs != null) {
            try {
                while (rs.next()) {
                    tableTypes.add(rs.getString(1).trim());
                }
            } finally {
                close(rs);
            }
        }

        final String dbProductName = getDatabaseProductName();
        final int nbrTableTypes = tableTypes.size();

        // InstantDB (at least version 3.13) only returns "TABLES"
        // for getTableTypes(). If you try to use this in a call to
        // DatabaseMetaData.getTables() no tables will be found. For the
        // moment hard code the types for InstantDB.
        if (nbrTableTypes == 1 && dbProductName.equals("InstantDB")) {
            tableTypes.clear();
            tableTypes.add("TABLE");
            tableTypes.add("SYSTEM TABLE");
        }

        // At least one version of PostgreSQL through the JDBC/ODBC
        // bridge returns an empty result set for the list of table
        // types. Another version of PostgreSQL returns 6 entries
        // of "SYSTEM TABLE" (which we have already filtered back to one).
        else if (dbProductName.equals("PostgreSQL")) {
            if (nbrTableTypes == 0 || nbrTableTypes == 1) {
//                if (s_log.isDebugEnabled()) {
//                    s_log.debug("Detected PostgreSQL and "+nbrTableTypes+
//                                " table types - overriding to 4 table types");
//                }
                tableTypes.clear();
                tableTypes.add("TABLE");
                tableTypes.add("SYSTEM TABLE");
                tableTypes.add("VIEW");
                tableTypes.add("SYSTEM VIEW");
            }
            // Treating indexes as tables interferes with the operation of the
            // PostgreSQL plugin
            if (tableTypes.contains("INDEX")) {
                tableTypes.remove("INDEX");
            }
            // Treating sequences as tables interferes with the operation of the
            // PostgreSQL plugin
            if (tableTypes.contains("SEQUENCE")) {
                tableTypes.remove("SEQUENCE");
            }
            // There are many of these "tables", that PostgreSQL throws
            // SQLExceptions for whenever a table-like operation is attempted.
            if (tableTypes.contains("SYSTEM INDEX")) {
                tableTypes.remove("SYSTEM INDEX");
            }
        }

        // Informix: when no database is given in the connect url, then no table types are returned.  The
        // catalog can be changed which will select a database, but by that time it is too late.
        else if (DialectFactory.isInformix(this._conn)) {
            if (nbrTableTypes == 0) {
//				if (s_log.isDebugEnabled()) {
//					s_log.debug("Detected Informix with no table types returned.  Defaulting to "
//						+ "TABLE | SYSTEM TABLE | VIEW");
//				}
                tableTypes.add("TABLE");
                tableTypes.add("SYSTEM TABLE");
                tableTypes.add("VIEW");
            }
        }

        value = tableTypes.toArray(new String[tableTypes.size()]);
        _cache.put(key, value);
        return value;
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getTypesDataSet()
     */
//    public synchronized IDataSet getTypesDataSet() throws DataSetException {
//        ResultSet rs = null;
//        try {
//            rs = privateGetJDBCMetaData().getTypeInfo();
//            return (new DatabaseTypesDataSet(rs));
//        } catch (SQLException e) {
//            throw new DataSetException(e);
//        } finally {
//            SQLUtilities.closeResultSet(rs);
//        }
//    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getTables(java.lang.String, java.lang.String, java.lang.String, java.lang.String[], com.riversql.sql.ProgressCallBack)
     */
    public synchronized ITableInfo[] getTables(String catalog,
                                               String schemaPattern,
                                               String tableNamePattern,
                                               String[] types,
                                               ProgressCallBack progressCallBack)
            throws SQLException {


        final DatabaseMetaData md = privateGetJDBCMetaData();
        final String dbDriverName = getDriverName();
        Set<ITableInfo> list = new TreeSet<ITableInfo>();

        /* work-around for this driver, which must have "dbo" for schema.  The
         * JConnect family of drivers appears to not be affected and can accept a
         * null schema, which is necessary to find tables in other schemas, within
         * the same catalog.  Similarly, jTDS 1.2.2 doesn't require this, yet it
         * doesn't return non-dbo schema tables, unfortunately.
         */
        if (dbDriverName.equals(IDriverNames.FREE_TDS) && schemaPattern == null) {
            schemaPattern = "dbo";
        }
        if (dbDriverName.equals(IDriverNames.AS400) && schemaPattern == null) {
            schemaPattern = "*ALLUSR";
        }

        //Add begin
        if (catalog == null && DriverMatch.isComHttxDriver(_conn)) {
            String[] catalogs = getCatalogs();
            if (catalogs != null) {
                for (int i = 0; i < catalogs.length; i++) {
                    ITableInfo[] tables = getTables(catalogs[i],
                            schemaPattern,
                            tableNamePattern,
                            types,
                            progressCallBack);
                    for (int j = 0; j < tables.length; j++) {
                        list.add(tables[j]);
                    }
                }
                return list.toArray(new ITableInfo[list.size()]);
            }
        }
        //Add end


        Map<String, ITableInfo> nameMap = null;
        ResultSet superTabResult = null;
        ResultSet tabResult = null;
        try {
            if (supportsSuperTables) {
                try {
                    superTabResult = md.getSuperTables(catalog,
                            schemaPattern,
                            tableNamePattern);
                    // create a mapping of names if we have supertable info, since
                    // we need to find the ITableInfo again for re-ordering.
                    if (superTabResult != null && superTabResult.next()) {
                        nameMap = new HashMap<String, ITableInfo>();
                    }
                } catch (Throwable th) {
//                s_log.debug("DBMS/Driver doesn't support getSupertables(): "+
//                            th.getMessage());
                    supportsSuperTables = false;
                }
            }
            // store all plain table info we have.
            tabResult = md.getTables(catalog, schemaPattern, tableNamePattern, types);
            int count = 0;
            while (tabResult != null && tabResult.next()) {
                ITableInfo tabInfo = new TableInfo(tabResult.getString(1),
                        tabResult.getString(2), tabResult.getString(3),
                        tabResult.getString(4), tabResult.getString(5),
                        this);
                if (nameMap != null) {
                    nameMap.put(tabInfo.getSimpleName(), tabInfo);
                }
                list.add(tabInfo);

                if (null != progressCallBack) {
                    if (0 == count++ % 100) {
                        progressCallBack.currentlyLoading(tabInfo.getSimpleName());
                    }
                }
            }

            // re-order nodes if the tables are stored hierachically
            if (nameMap != null) {
                do {
                    String tabName = superTabResult.getString(3);
                    TableInfo tabInfo = (TableInfo) nameMap.get(tabName);
                    if (tabInfo == null)
                        continue;
                    String superTabName = superTabResult.getString(4);
                    if (superTabName == null)
                        continue;
                    TableInfo superInfo = (TableInfo) nameMap.get(superTabName);
                    if (superInfo == null)
                        continue;
                    superInfo.addChild(tabInfo);
                    list.remove(tabInfo); // remove from toplevel.

                    if (null != progressCallBack) {
                        if (0 == count++ % 20) {
                            progressCallBack.currentlyLoading(tabInfo.getSimpleName());
                        }
                    }
                }
                while (superTabResult.next());
            }
        } finally {
            close(tabResult);
            close(superTabResult);
        }

        return list.toArray(new ITableInfo[list.size()]);
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getProcedures(java.lang.String, java.lang.String, java.lang.String, com.riversql.sql.ProgressCallBack)
     */
//   public synchronized IProcedureInfo[] getProcedures(String catalog,
//                                                      String schemaPattern,
//                                                      String procedureNamePattern,
//                                                      ProgressCallBack progressCallBack)
//     throws SQLException
//  {
//     DatabaseMetaData md = privateGetJDBCMetaData();
//     ArrayList<ProcedureInfo> list = new ArrayList<ProcedureInfo>();
//     ResultSet rs = md.getProcedures(catalog, schemaPattern, procedureNamePattern);
//     if (rs != null) {
//	     int count = 0;
//	     try
//	     {
//	        final int[] cols = new int[]{1, 2, 3, 7, 8};
//	        DialectType dialectType = DialectFactory.getDialectType(this);
//	        final ResultSetReader rdr = new ResultSetReader(rs, cols, dialectType);
//	        Object[] row = null;
//	        while ((row = rdr.readRow()) != null)
//	        {
//	           final int type = ((Number) row[4]).intValue();
//	           ProcedureInfo pi = new ProcedureInfo(getAsString(row[0]), getAsString(row[1]),
//	              getAsString(row[2]), getAsString(row[3]), type, this);
//	
//	           list.add(pi);
//	
//	           if (null != progressCallBack)
//	           {
//	              if(0 == count++ % 200 )
//	              {
//	                 progressCallBack.currentlyLoading(pi.getSimpleName());
//	              }
//	           }
//	        }
//	     }
//	     finally
//	     {
//	   	  close(rs);
//	     }
//     }
//     return list.toArray(new IProcedureInfo[list.size()]);
//  }

    /**
     * If we are connected to Informix, then we may need to cleanup
     *
     * @param catalogName
     */
    private void checkForInformix(String catalogName) {
        if (!DialectFactory.isInformix(this._conn)) {
            return;
        }

        Statement stmt = null;
        String sql = "DATABASE " + catalogName;

        try {
            stmt = _conn.createStatement();
            stmt.execute("Drop procedure mode_decode");
        } catch (SQLException e) {
            // The mode_decode routine may or may not be there. We don't care if it is not there, but log an
            // info if we failed to drop it for some other reason.
//			s_log.info("setInformixCatalog: unable to drop procedure mode_decode: " + e.getMessage(), e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {

                }
            }
        }
    }

    private String getAsString(Object val) {
        if (null == val) {
            return null;
        } else {
            if (val instanceof String) {
                return (String) val;
            } else {
                return "" + val;
            }
        }

    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getUDTs(java.lang.String, java.lang.String, java.lang.String, int[])
     */
//    public synchronized IUDTInfo[] getUDTs(String catalog, String schemaPattern,
//								           String typeNamePattern, int[] types)
//		throws SQLException
//	{
//		DatabaseMetaData md = privateGetJDBCMetaData();
//		ArrayList<UDTInfo> list = new ArrayList<UDTInfo>();
//		checkForInformix(catalog);
//		ResultSet rs = md.getUDTs(catalog, schemaPattern, typeNamePattern, types);
//		try
//		{
//			final int[] cols = new int[] {1, 2, 3, 4, 5, 6};
//			DialectType dialectType = DialectFactory.getDialectType(this);
//			final ResultSetReader rdr = new ResultSetReader(rs, cols, dialectType);
//			Object[] row = null;
//			while ((row = rdr.readRow()) != null)
//			{
//				list.add(new UDTInfo(getAsString(row[0]), getAsString(row[1]), getAsString(row[2]),
//									getAsString(row[3]), getAsString(row[4]), getAsString(row[5]),
//									this));
//			}
//		}
//		finally
//		{
//			close(rs);
//      }
//
//		return list.toArray(new IUDTInfo[list.size()]);
//	}

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getNumericFunctions()
     */
    public synchronized String[] getNumericFunctions() throws SQLException {
        final String key = "getNumericFunctions";
        String[] value = (String[]) _cache.get(key);
        if (value != null) {
            return value;
        }

        value = makeArray(privateGetJDBCMetaData().getNumericFunctions());
        _cache.put(key, value);
        return value;
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getStringFunctions()
     */
    public synchronized String[] getStringFunctions() throws SQLException {
        final String key = "getStringFunctions";
        String[] value = (String[]) _cache.get(key);
        if (value != null) {
            return value;
        }

        value = makeArray(privateGetJDBCMetaData().getStringFunctions());
        _cache.put(key, value);
        return value;
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getSystemFunctions()
     */
    public synchronized String[] getSystemFunctions() throws SQLException {
        final String key = "getSystemFunctions";
        String[] value = (String[]) _cache.get(key);
        if (value != null) {
            return value;
        }

        value = makeArray(privateGetJDBCMetaData().getSystemFunctions());
        _cache.put(key, value);
        return value;
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getTimeDateFunctions()
     */
    public synchronized String[] getTimeDateFunctions() throws SQLException {
        final String key = "getTimeDateFunctions";
        String[] value = (String[]) _cache.get(key);
        if (value != null) {
            return value;
        }

        value = makeArray(privateGetJDBCMetaData().getTimeDateFunctions());
        _cache.put(key, value);
        return value;
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getSQLKeywords()
     */
    public synchronized String[] getSQLKeywords() throws SQLException {
        final String key = "getSQLKeywords";
        String[] value = (String[]) _cache.get(key);
        if (value != null) {
            return value;
        }

        value = makeArray(privateGetJDBCMetaData().getSQLKeywords());
        _cache.put(key, value);
        return value;
    }

    /**
     * @see com.riversql.sql.ISQLDatabaseMetaData#getBestRowIdentifier(com.riversql.sql.ITableInfo)
     */
    public synchronized BestRowIdentifier[] getBestRowIdentifier(ITableInfo ti) throws SQLException {
        final List<BestRowIdentifier> results = new ArrayList<BestRowIdentifier>();

        ResultSet rs = null;
        try {
            boolean columnsCanBeNullable = true;
            rs = privateGetJDBCMetaData().getBestRowIdentifier(
                    ti.getCatalogName(), ti.getSchemaName(), ti.getSimpleName(), DatabaseMetaData.bestRowTransaction,
                    columnsCanBeNullable);

            final String catalog = ti.getCatalogName();
            final String schema = ti.getSchemaName();
            final String table = ti.getSimpleName();

            final ResultSetColumnReader rdr = new ResultSetColumnReader(rs);
            while (rdr.next()) {
                final BestRowIdentifier rid = new BestRowIdentifier(
                        catalog, schema, table, rdr.getLong(1).intValue(), rdr.getString(2),
                        rdr.getLong(3).shortValue(), rdr.getString(4), rdr.getLong(5).intValue(),
                        rdr.getLong(7).shortValue(), rdr.getLong(8).shortValue(), this);
                results.add(rid);
            }
        } finally {
            close(rs);
        }

        final BestRowIdentifier[] ar = new BestRowIdentifier[results.size()];
        return results.toArray(ar);
    }

    /**
     * @see com.riversql.sql.ISQLDatabaseMetaData#getColumnPrivileges(com.riversql.sql.ITableInfo)
     * @deprecated use getColumnPrivilegesDataSet instead.
     */
    public ResultSet getColumnPrivileges(ITableInfo ti) throws SQLException {
        // MM-MYSQL driver doesnt support null for column name.
        final String columns = DialectFactory.isMySQL(this._conn) ? "%" : null;
        return privateGetJDBCMetaData().getColumnPrivileges(ti.getCatalogName(),
                ti.getSchemaName(),
                ti.getSimpleName(),
                columns);
    }

    /**
     * @deprecated. Replaced by getExportedKeysInfo
     * @see com.riversql.sql.ISQLDatabaseMetaData#getExportedKeys(com.riversql.sql.ITableInfo)
     */
    public ResultSet getExportedKeys(ITableInfo ti) throws SQLException {
        return privateGetJDBCMetaData().getExportedKeys(ti.getCatalogName(),
                ti.getSchemaName(),
                ti.getSimpleName());
    }

    /**
     * @deprecated. Replaced by getImportedKeysInfo
     * @see com.riversql.sql.ISQLDatabaseMetaData#getImportedKeys(com.riversql.sql.ITableInfo)
     */
    public ResultSet getImportedKeys(ITableInfo ti)
            throws SQLException {
        return privateGetJDBCMetaData().getImportedKeys(
                ti.getCatalogName(), ti.getSchemaName(),
                ti.getSimpleName());
    }

    /**
     * @see com.riversql.sql.ISQLDatabaseMetaData#getColumnPrivilegesDataSet(com.riversql.sql.ITableInfo,
     *      int[], boolean)
     */
//   public synchronized IDataSet getColumnPrivilegesDataSet(ITableInfo ti,
//         int[] columnIndices, boolean computeWidths) throws DataSetException {
//      ResultSet rs = null;
//      try {
//         DatabaseMetaData md = privateGetJDBCMetaData();
//         final String columns = DialectFactory.isMySQL(this) ? "%" : null;
//
//         rs = md.getColumnPrivileges(ti.getCatalogName(),
//                                     ti.getSchemaName(),
//                                     ti.getSimpleName(),
//                                     columns);
//         ResultSetDataSet rsds = new ResultSetDataSet();
//         rsds.setResultSet(rs,
//                           columnIndices,
//                           computeWidths,
//                           DialectFactory.getDialectType(this));
//         return rsds;
//      } catch (SQLException e) {
//         throw new DataSetException(e);
//      } finally {
//         close(rs);
//      }
//   }

    /**
     * @see com.riversql.sql.ISQLDatabaseMetaData#getImportedKeysInfo(java.lang.String, java.lang.String, java.lang.String)
     */
    public synchronized ForeignKeyInfo[] getImportedKeysInfo(String catalog,
                                                             String schema,
                                                             String tableName)
            throws SQLException {
        ResultSet rs =
                privateGetJDBCMetaData().getImportedKeys(catalog, schema, tableName);
        return getForeignKeyInfo(rs);
    }

    /**
     * @see com.riversql.sql.ISQLDatabaseMetaData#getExportedKeysDataSet(com.riversql.sql.ITableInfo)
     */
//   public synchronized IDataSet getExportedKeysDataSet(ITableInfo ti)
//         throws DataSetException 
//   {
//      ResultSet rs = null;
//      try {
//         rs = privateGetJDBCMetaData().getExportedKeys(ti.getCatalogName(),
//                                                       ti.getSchemaName(),
//                                                       ti.getSimpleName());
//         ResultSetDataSet rsds = new ResultSetDataSet();
//         rsds.setResultSet(rs, null, true, DialectFactory.getDialectType(this));
//         return rsds;
//      } catch (SQLException e) {
//         throw new DataSetException(e);
//      } finally {
//         close(rs);
//      }
//   }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getImportedKeysInfo(com.riversql.sql.ITableInfo)
     */
    public synchronized ForeignKeyInfo[] getImportedKeysInfo(ITableInfo ti)
            throws SQLException {
        return getForeignKeyInfo(privateGetJDBCMetaData().getImportedKeys(ti.getCatalogName(),
                ti.getSchemaName(), ti.getSimpleName()));
    }

    /**
     * @see com.riversql.sql.ISQLDatabaseMetaData#getExportedKeysInfo(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    public synchronized ForeignKeyInfo[] getExportedKeysInfo(String catalog,
                                                             String schema, String tableName) throws SQLException {
        ResultSet rs = privateGetJDBCMetaData().getExportedKeys(catalog,
                schema,
                tableName);
        return getForeignKeyInfo(rs);
    }

    /**
     * @see com.riversql.sql.ISQLDatabaseMetaData#getExportedKeysInfo(com.riversql.sql.ITableInfo)
     */
    public synchronized ForeignKeyInfo[] getExportedKeysInfo(ITableInfo ti)
            throws SQLException {
        return getForeignKeyInfo(privateGetJDBCMetaData().getExportedKeys(ti.getCatalogName(),
                ti.getSchemaName(), ti.getSimpleName()));
    }

    /**
     * @see com.riversql.sql.ISQLDatabaseMetaData#getImportedKeysDataSet(com.riversql.sql.ITableInfo)
     */
//   public synchronized IDataSet getImportedKeysDataSet(ITableInfo ti)
//         throws DataSetException {
//      ResultSet rs = null;
//      try {
//         rs = privateGetJDBCMetaData().getImportedKeys(ti.getCatalogName(),
//                                                       ti.getSchemaName(),
//                                                       ti.getSimpleName());
//         ResultSetDataSet rsds = new ResultSetDataSet();
//         rsds.setResultSet(rs, null, true, DialectFactory.getDialectType(this));
//         return rsds;
//      } catch (SQLException e) {
//         throw new DataSetException(e);
//      } finally {
//         close(rs);
//      }
//   }    
//    

    private ForeignKeyInfo[] getForeignKeyInfo(ResultSet rs)
            throws SQLException {
        final Map<String, ForeignKeyInfo> keys =
                new HashMap<String, ForeignKeyInfo>();
        final Map<String, ArrayList<ForeignKeyColumnInfo>> columns =
                new HashMap<String, ArrayList<ForeignKeyColumnInfo>>();

        try {
            final ResultSetColumnReader rdr = new ResultSetColumnReader(rs);
            while (rdr.next()) {
                final ForeignKeyInfo fki = new ForeignKeyInfo(rdr.getString(1),
                        rdr.getString(2), rdr.getString(3), rdr.getString(4),
                        rdr.getString(5), rdr.getString(6), rdr.getString(7),
                        rdr.getString(8),
                        rdr.getLong(10).intValue(), rdr.getLong(11).intValue(),
                        rdr.getString(12), rdr.getString(13),
                        rdr.getLong(14).intValue(), null, this);
                final String key = createForeignKeyInfoKey(fki);
                if (!keys.containsKey(key)) {
                    keys.put(key, fki);
                    columns.put(key, new ArrayList<ForeignKeyColumnInfo>());
                }

                ForeignKeyColumnInfo fkiCol = new ForeignKeyColumnInfo(rdr.getString(8),
                        rdr.getString(8),
                        rdr.getLong(9).intValue());
                columns.get(key).add(fkiCol);
            }
        } finally {
            close(rs);
        }

        final ForeignKeyInfo[] results = new ForeignKeyInfo[keys.size()];
        Iterator<ForeignKeyInfo> it = keys.values().iterator();
        int idx = 0;
        while (it.hasNext()) {
            final ForeignKeyInfo fki = it.next();
            final String key = createForeignKeyInfoKey(fki);
            final List<ForeignKeyColumnInfo> colsList = columns.get(key);
            final ForeignKeyColumnInfo[] fkiCol =
                    colsList.toArray(new ForeignKeyColumnInfo[colsList.size()]);
            fki.setForeignKeyColumnInfo(fkiCol);
            results[idx++] = fki;
        }

        return results;
    }

    /**
     * Returns a list of IndexInfos describing indexes for the specified table.
     *
     * @param ti the table to find all index information for.
     * @return a list of IndexInfos
     * @throws SQLException
     */
//    public List<IndexInfo> getIndexInfo(ITableInfo ti) throws SQLException {
//        List<IndexInfo> result = new ArrayList<IndexInfo>();
//        ResultSet rs = null;
//        try {
//            rs = privateGetJDBCMetaData().getIndexInfo(
//                    ti.getCatalogName(), ti.getSchemaName(),
//                    ti.getSimpleName(), false, true);
//            while (rs.next()) {
//                String catalog = rs.getString(1);
//                String schema = rs.getString(2);
//                String table = rs.getString(3);
//                boolean nonunique = rs.getBoolean(4);
//                String indexQualifier = rs.getString(5);
//                String indexName = rs.getString(6);
//                IndexInfo.IndexType indexType =
//                    JDBCTypeMapper.getIndexType(rs.getShort(7));
//                short ordinalPosition = rs.getShort(8);
//                String column = rs.getString(9);
//                IndexInfo.SortOrder sortOrder =
//                    JDBCTypeMapper.getIndexSortOrder(rs.getString(10));
//                int cardinality = rs.getInt(11);
//                int pages = rs.getInt(12);
//                String filterCondition = rs.getString(13);
//
//                IndexInfo indexInfo = new IndexInfo(catalog,
//                                                schema,
//                                                indexName,
//                                                table,
//                                                column,
//                                                nonunique,
//                                                indexQualifier,
//                                                indexType,
//                                                ordinalPosition,
//                                                sortOrder,
//                                                cardinality,
//                                                pages,
//                                                filterCondition,
//                                                this);
//                result.add(indexInfo);
//            }
//        } catch (SQLException e) {
//        } finally {
//            close(rs);
//        }
//        return result;
//    }
//
    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getPrimaryKeys(com.riversql.sql.ITableInfo)
     *
     * @deprecated use getPrimaryKey instead
     */
    public ResultSet getPrimaryKeys(ITableInfo ti)
            throws SQLException {
        return privateGetJDBCMetaData().getPrimaryKeys(
                ti.getCatalogName(), ti.getSchemaName(),
                ti.getSimpleName());
    }

    /**
     * @see com.riversql.sql.ISQLDatabaseMetaData#getPrimaryKey(com.riversql.sql.ITableInfo,
     * int[], boolean)
     */
//   public synchronized IDataSet getPrimaryKey(ITableInfo ti,
//         int[] columnIndices, boolean computeWidths) throws DataSetException {
//      ResultSet rs = null;
//      try {
//         rs = privateGetJDBCMetaData().getPrimaryKeys(ti.getCatalogName(),
//                                                      ti.getSchemaName(),
//                                                      ti.getSimpleName());
//         ResultSetDataSet rsds = new ResultSetDataSet();
//         rsds.setResultSet(rs,
//                           columnIndices,
//                           computeWidths,
//                           DialectFactory.getDialectType(this));
//         return rsds;
//      } catch (SQLException e) {
//         throw new DataSetException(e);
//      } finally {
//         close(rs);
//      }
//   }

    /*
     * (non-Javadoc)
     *
     * @see com.riversql.sql.ISQLDatabaseMetaData#getPrimaryKey(com.riversql.sql.ITableInfo)
     */
    public synchronized PrimaryKeyInfo[] getPrimaryKey(ITableInfo ti)
            throws SQLException {
        return getPrimaryKey(ti.getCatalogName(),
                ti.getSchemaName(),
                ti.getSimpleName());
    }

    /**
     * @see com.riversql.sql.ISQLDatabaseMetaData#getIndexInfo(com.riversql.sql.ITableInfo,
     *      int[], boolean)
     */
//   public synchronized ResultSetDataSet getIndexInfo(ITableInfo ti,
//         int[] columnIndices, boolean computeWidths) throws DataSetException {
//      ResultSet rs = null;
//      try {
//         rs = privateGetJDBCMetaData().getIndexInfo(ti.getCatalogName(),
//                                                    ti.getSchemaName(),
//                                                    ti.getSimpleName(),
//                                                    false,
//                                                    true);
//         ResultSetDataSet rsds = new ResultSetDataSet();
//         rsds.setResultSet(rs,
//                           columnIndices,
//                           computeWidths,
//                           DialectFactory.getDialectType(this));
//         return rsds;
//      } catch (SQLException e) {
//         throw new DataSetException(e);
//      } finally {
//         close(rs);
//      }
//   }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getPrimaryKey(java.lang.String, java.lang.String, java.lang.String)
     */
    public synchronized PrimaryKeyInfo[] getPrimaryKey(String catalog,
                                                       String schema,
                                                       String table)
            throws SQLException {
        final List<PrimaryKeyInfo> results = new ArrayList<PrimaryKeyInfo>();
        ResultSet rs = null;
        try {
            rs = privateGetJDBCMetaData().getPrimaryKeys(catalog, schema, table);
            while (rs.next()) {
                PrimaryKeyInfo pkInfo =
                        new PrimaryKeyInfo(rs.getString(1),  // catalog
                                rs.getString(2),  // schema
                                rs.getString(3),  // tableName
                                rs.getString(4),  // columnName
                                rs.getShort(5),   // keySequence
                                rs.getString(6),  // pkName
                                this);
                results.add(pkInfo);
            }
        } finally {
            close(rs);
        }

        final PrimaryKeyInfo[] ar = new PrimaryKeyInfo[results.size()];
        return results.toArray(ar);
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getProcedureColumns(com.riversql.sql.IProcedureInfo)
     *
     * @deprecated use getProcedureColumnsDataSet instead
     */
    public ResultSet getProcedureColumns(IProcedureInfo ti)
            throws SQLException {
        return privateGetJDBCMetaData().getProcedureColumns(ti.getCatalogName(),
                ti.getSchemaName(),
                ti.getSimpleName(),
                "%");
    }

    /**
     * @see com.riversql.sql.ISQLDatabaseMetaData#getProcedureColumnsDataSet(com.riversql.sql.IProcedureInfo)
     */
//   public synchronized IDataSet getProcedureColumnsDataSet(IProcedureInfo ti)
//         throws DataSetException {
//      ResultSet rs = null;
//      try {
//         DatabaseMetaData md = privateGetJDBCMetaData();
//         rs = md.getProcedureColumns(ti.getCatalogName(),
//                                     ti.getSchemaName(),
//                                     ti.getSimpleName(),
//                                     "%");
//         ResultSetDataSet rsds = new ResultSetDataSet();
//         rsds.setResultSet(rs, DialectFactory.getDialectType(this));
//         return rsds;
//      } catch (SQLException e) {
//         throw new DataSetException(e);
//      } finally {
//         close(rs);
//      }
//   }
//
    /*
     * (non-Javadoc)
     *
     * @see com.riversql.sql.ISQLDatabaseMetaData#getTablePrivileges(com.riversql.sql.ITableInfo)
     *
     * @deprecated use getTablePrivilegesDataSet instead
     */
    public ResultSet getTablePrivileges(ITableInfo ti)
            throws SQLException {
        return privateGetJDBCMetaData().getTablePrivileges(ti.getCatalogName(),
                ti.getSchemaName(),
                ti.getSimpleName());
    }

    /**
     * @see com.riversql.sql.ISQLDatabaseMetaData#getTablePrivilegesDataSet(com.riversql.sql.ITableInfo,
     * int[], boolean)
     */
//   public synchronized IDataSet getTablePrivilegesDataSet(ITableInfo ti,
//         int[] columnIndices, boolean computeWidths) throws DataSetException {
//      ResultSet rs = null;
//      try {
//         DatabaseMetaData md = privateGetJDBCMetaData();
//         rs = md.getTablePrivileges(ti.getCatalogName(),
//                                    ti.getSchemaName(),
//                                    ti.getSimpleName());
//         ResultSetDataSet rsds = new ResultSetDataSet();
//         rsds.setResultSet(rs,
//                           columnIndices,
//                           computeWidths,
//                           DialectFactory.getDialectType(this));
//         return rsds;
//      } catch (SQLException e) {
//         throw new DataSetException(e);
//      } finally {
//         close(rs);
//      }
//   }


    /*
     * (non-Javadoc)
     *
     * @see com.riversql.sql.ISQLDatabaseMetaData#getVersionColumns(com.riversql.sql.ITableInfo)
     *
     * @deprecated use getVersionColumnsDataSet instead
     */
    public ResultSet getVersionColumns(ITableInfo ti)
            throws SQLException {
        return privateGetJDBCMetaData().getVersionColumns(ti.getCatalogName(),
                ti.getSchemaName(),
                ti.getSimpleName());
    }

    /**
     * @see com.riversql.sql.ISQLDatabaseMetaData#getVersionColumnsDataSet(com.riversql.sql.ITableInfo)
     */
//    public synchronized IDataSet getVersionColumnsDataSet(ITableInfo ti)
//        throws DataSetException
//    {
//        ResultSet rs = null;
//        try {
//            DatabaseMetaData md = privateGetJDBCMetaData();
//            rs = md.getVersionColumns(ti.getCatalogName(),
//                                      ti.getSchemaName(),
//                                      ti.getSimpleName());
//            ResultSetDataSet rsds = new ResultSetDataSet();
//            rsds.setResultSet(rs, DialectFactory.getDialectType(this));
//            return rsds;
//        } catch (SQLException e) {
//            throw new DataSetException(e);
//        } finally {
//            close(rs);
//        }
//    }
    private ResultSet getColumns(ITableInfo ti)
            throws SQLException {
        return privateGetJDBCMetaData().getColumns(ti.getCatalogName(),
                ti.getSchemaName(),
                ti.getSimpleName(), "%");
    }

    /**
     * @see com.riversql.sql.ISQLDatabaseMetaData#getColumns(com.riversql.sql.ITableInfo, int[], boolean)
     */
//   public synchronized IDataSet getColumns(ITableInfo ti, int[] columnIndices,
//         boolean computeWidths) throws DataSetException {
//      IDataSet result = null;
//      ResultSet rs = null;
//      try {
//         rs = getColumns(ti);
//         ResultSetDataSet rsds = new ResultSetDataSet();
//         rsds.setResultSet(rs,
//                           columnIndices,
//                           computeWidths,
//                           DialectFactory.getDialectType(this));
//         result = rsds;
//      } catch (SQLException e) {
//         throw new DataSetException(e);
//      } finally {
//         close(rs);
//      }
//      return result;
//   }

    /*
     * (non-Javadoc)
     *
     * @see com.riversql.sql.ISQLDatabaseMetaData#getColumnInfo(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public synchronized TableColumnInfo[] getColumnInfo(String catalog,
                                                        String schema,
                                                        String table)
            throws SQLException {
        ResultSet rs = null;
        try {
            final Map<Integer, TableColumnInfo> columns =
                    new TreeMap<Integer, TableColumnInfo>();
            DatabaseMetaData md = privateGetJDBCMetaData();
            rs = md.getColumns(catalog, schema, table, "%");
            final ResultSetColumnReader rdr = new ResultSetColumnReader(rs);

            int index = 0;
            while (rdr.next()) {
                final TableColumnInfo tci =
                        new TableColumnInfo(rdr.getString(1),           // TABLE_CAT
                                rdr.getString(2),           // TABLE_SCHEM
                                rdr.getString(3),           // TABLE_NAME
                                rdr.getString(4),           // COLUMN_NAME
                                rdr.getLong(5).intValue(),  // DATA_TYPE
                                rdr.getString(6),           // TYPE_NAME
                                rdr.getLong(7).intValue(),  // COLUMN_SIZE
                                rdr.getLong(9).intValue(),  // DECIMAL_DIGITS
                                rdr.getLong(10).intValue(), // NUM_PREC_RADIX
                                rdr.getLong(11).intValue(), // NULLABLE
                                rdr.getString(12),          // REMARKS
                                rdr.getString(13),          // COLUMN_DEF
                                rdr.getLong(16).intValue(), // CHAR_OCTET_LENGTH
                                rdr.getLong(17).intValue(), // ORDINAL_POSITION
                                rdr.getString(18),          // IS_NULLABLE
                                this);
                ////////////////////////////////////////////////////////////////////////////////////////////
                // The index is needed in case this method is called with schema = null, catalog = null
                // and two tables with the same name in different schemas/catalogs.
                // Without the index the same ordinal position could only occur once.
                ++index;
                //
                ////////////////////////////////////////////////////////////////////////////////////////////
                columns.put(Integer.valueOf(10000 * tci.getOrdinalPosition() + index), tci);
            }

            return columns.values().toArray(new TableColumnInfo[columns.size()]);

        } finally {
            close(rs);
        }
    }

    /* (non-Javadoc)
     * @see com.riversql.sql.ISQLDatabaseMetaData#getColumnInfo(com.riversql.sql.ITableInfo)
     */
    public synchronized TableColumnInfo[] getColumnInfo(ITableInfo ti)
            throws SQLException {
        return getColumnInfo(ti.getCatalogName(), ti.getSchemaName(), ti.getSimpleName());
    }

    /**
     * @see com.riversql.sql.ISQLDatabaseMetaData#correctlySupportsSetMaxRows()
     */
    public boolean correctlySupportsSetMaxRows() throws SQLException {
        return !IDriverNames.OPTA2000.equals(getDriverName());
    }

    /**
     * @see com.riversql.sql.ISQLDatabaseMetaData#supportsMultipleResultSets()
     */
    public synchronized boolean supportsMultipleResultSets()
            throws SQLException {
        final String key = "supportsMultipleResultSets";
        Boolean value = (Boolean) _cache.get(key);
        if (value != null) {
            return value.booleanValue();
        }

        value = Boolean.valueOf(privateGetJDBCMetaData().supportsMultipleResultSets());
        _cache.put(key, value);

        return value.booleanValue();
    }

    /**
     * @see com.riversql.sql.ISQLDatabaseMetaData#storesUpperCaseIdentifiers()
     */
    public synchronized boolean storesUpperCaseIdentifiers()
            throws SQLException {
        final String key = "storesUpperCaseIdentifiers";
        Boolean value = (Boolean) _cache.get(key);
        if (value != null) {
            return value.booleanValue();
        }

        value = Boolean.valueOf(privateGetJDBCMetaData().storesUpperCaseIdentifiers());
        _cache.put(key, value);

        return value.booleanValue();
    }

    /**
     * @see com.riversql.sql.ISQLDatabaseMetaData#clearCache()
     */
    public void clearCache() {
        _cache.clear();
    }

    /**
     * Return the <TT>DatabaseMetaData</TT> object for this connection.
     *
     * @return The <TT>DatabaseMetaData</TT> object for this connection.
     * @throws SQLException    Thrown if an SQL error occurs.
     */
    private DatabaseMetaData privateGetJDBCMetaData() throws SQLException {
        checkThread();
        return _conn.getConnection().getMetaData();
    }

    /**
     * @param fki
     * @return
     */
    private String createForeignKeyInfoKey(ForeignKeyInfo fki) {
        final StringBuffer buf = new StringBuffer();
        buf.append(fki.getForeignKeyCatalogName())
                .append(fki.getForeignKeySchemaName())
                .append(fki.getForeignKeyTableName())
                .append(fki.getForeignKeyName())
                .append(fki.getPrimaryKeyCatalogName())
                .append(fki.getPrimaryKeySchemaName())
                .append(fki.getPrimaryKeyTableName())
                .append(fki.getPrimaryKeyName());
        return buf.toString();
    }

    /**
     * Check the thread of the caller to see if it is the event dispatch thread
     * and if we are debugging print a debug log message with the call trace.
     */
    private void checkThread() {
        /* This is extremely useful when trying to track down Swing UI freezing.
         * However, it currently fills the log which obscures other debug
         * messages even though UI performance is acceptable, so it is commented
         * out until it is needed later.
        if (s_log.isDebugEnabled() && SwingUtilities.isEventDispatchThread()) {
            try {
                throw new Exception("GUI Thread is doing database work");
            } catch (Exception e) {
                s_log.debug(e.getMessage(), e);
            }
        }
        */
    }

    public interface IDBMSProductNames {
        String DB2 = "DB2";
        String MYSQL = "mysql";
        String MICROSOFT_SQL = "Microsoft SQL Server";
        String POSTGRESQL = "PostgreSQL";
        String SYBASE = "Sybase SQL Server";
        String SYBASE_OLD = "SQL Server";
        String ORACLE = "Oracle";
    }

    /**
     * Full or partial names of various JDBC driivers that can be matched
     * to <tt>getDriverName()</tt>.
     */
    private interface IDriverNames {
        String AS400 = "AS/400 Toolbox for Java JDBC Driver";
        /* work-around for bug which means we must use "dbo" for schema */
        String FREE_TDS = "InternetCDS Type 4 JDBC driver for MS SQLServer";
        String OPTA2000 = "i-net OPTA 2000";
    }

    public static class DriverMatch {
        private static final String COM_HTTX_DRIVER_PREFIX = "com.hxtt.sql.";

        public static boolean isComHttxDriver(SQLConnection con) {
            if (null == con) {
                return false;
            }
            return con.getSQLDriver().getDriverClassName().startsWith(COM_HTTX_DRIVER_PREFIX);
        }
    }

//	/** Internationalized strings for this class. */
//	private static final StringManager s_stringMgr =
//		StringManagerFactory.getStringManager(SQLDatabaseMetaData.class);
//
//	/** Logger for this class. */
////	private final static ILogger s_log =
////		LoggerController.createLogger(SQLDatabaseMetaData.class);
//
//	/**
//	 * Full or partial names of various JDBC driivers that can be matched
//	 * to <tt>getDriverName()</tt>.
//	 */
//	private interface IDriverNames
//	{
//        String AS400 = "AS/400 Toolbox for Java JDBC Driver";
//        String FRONTBASE = "FBJDriver";
//        String FREE_TDS = "InternetCDS Type 4 JDBC driver for MS SQLServer";
//        String IBMDB2 = "IBM DB2";
//        String INGRES = "CA-Ingres JDBC Driver";
//        String JCONNECT = "jConnect (TM) for JDBC (TM)";
//		  String JTDS = "jTDS Type 4 JDBC Driver for MS SQL Server and Sybase";
//        String OPTA2000 = "i-net OPTA 2000";
//        String ORACLE = "Oracle";
//
//	}
//
//   public static class DriverMatch
//   {
//      private static final String COM_HTTX_DRIVER_PREFIX = "com.hxtt.sql.";
//
//      public static boolean isComHttxDriver(SQLConnection con)
//      {
//         if(null == con)
//         {
//            return false;
//         }
//         return con.getSQLDriver().getDriverClassName().startsWith(COM_HTTX_DRIVER_PREFIX);
//      }
//   }
//
//
//   /**
//	 * Full or partial names of various DBMS poducts that can be matched
//	 * to <tt>getDatabaseProductName()</tt>.
//	 */
//	public interface IDBMSProductNames
//	{
//		String DB2 = "DB2";
//		String MYSQL = "mysql";
//		String MICROSOFT_SQL = "Microsoft SQL Server";
//		String POSTGRESQL = "PostgreSQL";
//		String SYBASE = "Sybase SQL Server";
//		String SYBASE_OLD = "SQL Server";
//		String ORACLE = "Oracle";
//	}
//
//	/** Connection to database this class is supplying information for. */
//	private SQLConnection _conn;
//
//	/**
//	 * Cache of commonly accessed metadata properties keyed by the method
//	 * name that attempts to retrieve them.
//    * Note, this cache should only be used for metadata that are not
//    * likely to be changed during an open Session.
//    * Meta data that is likely to be changed should be kept in SchemaInfo.
//	 */
//	private Map _cache = Collections.synchronizedMap(new HashMap());
//
//    /**
//     * If previous attempts to getSuperTables fail, then this will be set to 
//     * false, and prevent further attempts.
//     */
//    private boolean supportsSuperTables = true;
//    
//	/**
//	 * ctor specifying the connection that we are retrieving metadata for.
//	 *
//	 * @param	conn	Connection to database.
//	 *
//	 * @throws	IllegalArgumentException
//	 * 			Thrown if null SQLConnection passed.
//	 */
//	SQLDatabaseMetaData(SQLConnection conn)
//	{
//		super();
//		if (conn == null)
//		{
//			throw new IllegalArgumentException("SQLDatabaseMetaData == null");
//		}
//		_conn = conn;
//	}
//
//    /**
//     * Return the name of the current user. Cached on first call.
//     *
//     * @return  the current user name.
//     */
//    public synchronized String getUserName() throws SQLException
//	{
//		final String key = "getUserName";
//		String value = (String)_cache.get(key);
//		if (value == null)
//		{
//			value = privateGetJDBCMetaData().getUserName();
//			_cache.put(key, value);
//		}
//		return value;
//	}
//
//    /**
//     * Return the database product name for this connection. Cached on first
//     * call.
//     *
//     * @return  the database product name for this connection.
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//    public synchronized String getDatabaseProductName()
//		throws SQLException
//	{
//		final String key = "getDatabaseProductName";
//		String value = (String)_cache.get(key);
//		if (value == null)
//		{
//			value = privateGetJDBCMetaData().getDatabaseProductName();
//            _cache.put(key, value);
//		}
//		return value;
//	}
//
//    /**
//     * Return the database product version for this connection. Cached on first
//     * call.
//     *
//     * @return  database product version
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//	public synchronized String getDatabaseProductVersion()
//		throws SQLException
//	{
//		final String key = "getDatabaseProductVersion";
//		String value = (String)_cache.get(key);
//		if (value == null)
//		{
//			value = privateGetJDBCMetaData().getDatabaseProductVersion();
//			_cache.put(key, value);
//		}
//		return value;
//	}
//
//    /**
//     * Return the JDBC driver name for this connection. Cached on first call.
//     *
//     * @return  the JDBC driver name for this connection.
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//	public synchronized String getDriverName() throws SQLException
//	{
//		final String key = "getDriverName";
//		String value = (String)_cache.get(key);
//		if (value == null)
//		{
//			value = privateGetJDBCMetaData().getDriverName();
//			_cache.put(key, value);
//		}
//		return value;
//	}
//
//    /**
//     * Return the JDBC version of this driver. Cached on first call.
//     *
//     * @return  the JDBC version of the driver.
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//    public int getJDBCVersion() throws SQLException
//	{
//		final String key = "getJDBCVersion";
//		Integer value = (Integer)_cache.get(key);
//		if (value == null)
//		{
//			DatabaseMetaData md = privateGetJDBCMetaData();
//            int major = md.getJDBCMajorVersion();
//            int minor = md.getJDBCMinorVersion();
//            int vers = (major * 100) + minor;
//            value = new Integer(vers);
//            _cache.put(key, value);
//		}
//		return value.intValue();
//	}
//
//    /**
//     * Return the string used to quote characters in this DBMS. Cached on first
//     * call.
//     *
//     * @return  quote string.
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//	public synchronized String getIdentifierQuoteString() throws SQLException
//	{
//		final String key = "getIdentifierQuoteString";
//		String value = (String)_cache.get(key);
//		if (value == null)
//		{
//			value = privateGetJDBCMetaData().getIdentifierQuoteString();
//			if (value == null) {
//			    value = "";
//                        }
//			_cache.put(key, value);
//		}
//		return value;
//	}
//
//    /**
//     * Returns the "cascade" constraints clause which is supported by some 
//     * databases when performing a delete to removed child records in dependent
//     * tables which would otherwise be orphaned and make the delete fail.
//     *  
//     * @return the "cascade" clause.
//     * 
//     * @throws SQLException
//     */
//    public synchronized String getCascadeClause() throws SQLException {
//        final String key = "getCascadeClause";
//        String value = (String)_cache.get(key);
//        if (value == null)
//        {
//            final String driverName = getDriverName();
//            if (driverName.startsWith(IDriverNames.IBMDB2)
//                || driverName.startsWith(IDriverNames.ORACLE))
//            {
//                value = "CASCADE";
//            }
//            else
//            {
//                value = "";
//            }
//            _cache.put(key, value);
//        }
//        return value;
//    }
//    
//    /**
//     * Return a string array containing the names of all the schemas in the
//     * database. Cached on first call.
//     *
//     * @return  String[] of the names of the schemas in the database.
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//	public synchronized String[] getSchemas() throws SQLException
//	{
////		final String key = "getSchemas";
////		String[] value = (String[])_cache.get(key);
////		if (value != null)
////		{
////			return value;
////		}
//
//		boolean hasGuest = false;
//		boolean hasSysFun = false;
//
//		final String dbProductName = getDatabaseProductName();
//		final boolean isMSSQLorSYBASE = dbProductName.equals(IDBMSProductNames.MICROSOFT_SQL)
//								|| dbProductName.equals(IDBMSProductNames.SYBASE)
//								|| dbProductName.equals(IDBMSProductNames.SYBASE_OLD);
//		final boolean isDB2 = dbProductName.startsWith(IDBMSProductNames.DB2);
//
//		final ArrayList list = new ArrayList();
//		ResultSet rs = privateGetJDBCMetaData().getSchemas();
//		try
//		{
//            if (rs != null) {
//    			final ResultSetReader rdr = new ResultSetReader(rs);
//    			Object[] row = null;
//    			while ((row = rdr.readRow()) != null)
//    			{
//    				if (isMSSQLorSYBASE && row[0].equals("guest"))
//    				{
//    					hasGuest = true;
//    				}
//    				if (isDB2 && row[0].equals("SYSFUN"))
//    				{
//    					hasSysFun = true;
//    				}
//    				list.add(row[0]);
//    			}
//            }
//		}
//		finally
//		{
//         close(rs);
//      }
//
//		// Some drivers for both MS SQL and Sybase don't return guest as
//		// a schema name.
//		if (isMSSQLorSYBASE && !hasGuest)
//		{
//			list.add("guest");
//		}
//
//		// Some drivers for DB2 don't return SYSFUN as a schema name. A
//		// number of system stored procs are kept in this schema.
//		if (isDB2 && !hasSysFun)
//		{
//			list.add("SYSFUN");
//		}
//
////		value = (String[])list.toArray(new String[list.size()]);
////		_cache.put(key, value);
//
//		return (String[])list.toArray(new String[list.size()]);
//	}
//
//    /**
//     * Retrieves whether this database supports schemas at all.
//     *
//     * @return  <TT>true</TT> if database supports schemas.
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//    public boolean supportsSchemas() throws SQLException
//	{
//		return supportsSchemasInDataManipulation()
//				|| supportsSchemasInTableDefinitions();
//	}
//
//    /**
//     * Retrieves whether a schema name can be used in a data manipulation
//     * statement. Cached on first call.
//     *
//     * @return  <TT>true</TT> if a schema name can be used in a data
//     *          manipulation statement.
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//    public synchronized boolean supportsSchemasInDataManipulation()
//		throws SQLException
//	{
//		final String key = "supportsSchemasInDataManipulation";
//		Boolean value = (Boolean)_cache.get(key);
//		if (value != null)
//		{
//			return value.booleanValue();
//		}
//
//		try
//		{
//			value = new Boolean(privateGetJDBCMetaData().supportsSchemasInDataManipulation());
//		}
//		catch (SQLException ex)
//		{
//			if (getDriverName().equals(IDriverNames.FREE_TDS))
//			{
//				value = Boolean.TRUE;
//			}
//			throw ex;
//		}
//
//		_cache.put(key, value);
//
//		return value.booleanValue();
//	}
//
//    /**
//     * Retrieves whether a schema name can be used in a table definition
//     * statement. Cached on first call.
//     *
//     * @return  <TT>true</TT> if a schema name can be used in a table
//     *          definition statement.
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//	public synchronized boolean supportsSchemasInTableDefinitions()
//		throws SQLException
//	{
//		final String key = "supportsSchemasInTableDefinitions";
//		Boolean value = (Boolean)_cache.get(key);
//		if (value != null)
//		{
//			return value.booleanValue();
//		}
//
//		try
//		{
//			value = new Boolean(privateGetJDBCMetaData().supportsSchemasInTableDefinitions());
//		}
//		catch (SQLException ex)
//		{
//			if (getDriverName().equals(IDriverNames.FREE_TDS))
//			{
//				value = Boolean.TRUE;
//			}
//			throw ex;
//		}
//
//		_cache.put(key, value);
//
//		return value.booleanValue();
//	}
//
//    /**
//     * Retrieves whether this DBMS supports stored procedures. Cached on first
//     * call.
//     *
//     * @return  <TT>true</TT> if DBMS supports stored procedures.
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//    public synchronized boolean supportsStoredProcedures() throws SQLException
//	{
//		final String key = "supportsStoredProcedures";
//		Boolean value = (Boolean)_cache.get(key);
//		if (value != null)
//		{
//			return value.booleanValue();
//		}
//
//		// PostgreSQL (at least 7.3.2) returns false for
//		// supportsStoredProcedures() even though it does support them.
//		if (getDatabaseProductName().equals(IDBMSProductNames.POSTGRESQL))
//		{
//			value = Boolean.TRUE;
//		}
//		else
//		{
//			value = new Boolean(privateGetJDBCMetaData().supportsStoredProcedures());
//		}
//		_cache.put(key, value);
//
//		return value.booleanValue();
//	}
//
//    /**
//     * Retrieves whether this DBMS supports save points. Cached on first
//     * call.
//     *
//     * @return  <TT>true</TT> if DBMS supports save points.
//     * 
//     * @throws SQLException if an SQL error occurs.
//     */
//    public synchronized boolean supportsSavepoints() throws SQLException {
//        
//        final String key = "supportsSavepoints";
//        Boolean value = (Boolean)_cache.get(key);
//        if (value != null)
//        {
//            return value.booleanValue();
//        }
//        value = new Boolean(privateGetJDBCMetaData().supportsSavepoints());
//
//        _cache.put(key, value);
//
//        return value.booleanValue();        
//    }
//    
//    /**
//     * Retrieves whether this DBMS supports result sets of the specified type. 
//     * Cached on first call.
//     *
//     * @param type the type of the ResultSet.  There are constants defined in 
//     *             the ResultSet class that define the different types.
//     *             
//     * @return  <TT>true</TT> if DBMS supports this type of ResultSet.
//     * 
//     * @throws SQLException if an SQL error occurs.
//     */
//    public synchronized boolean supportsResultSetType(int type) 
//        throws SQLException
//    {
//        final String key = "supportsResultSetType";
//        Boolean value = (Boolean)_cache.get(key);
//        if (value != null)
//        {
//            return value.booleanValue();
//        }
//        value = new Boolean(privateGetJDBCMetaData().supportsResultSetType(type));
//
//        _cache.put(key, value);
//
//        return value.booleanValue();                
//    }
//        
//    /**
//     * Return a string array containing the names of all the catalogs in the
//     * database. Cached on first call.
//     *
//     * @return  String[] of the names of the catalogs in the database.
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//    public synchronized String[] getCatalogs() throws SQLException
//	{
////		final String key = "getCatalogs";
////		String[] value = (String[])_cache.get(key);
////		if (value != null)
////		{
////			return value;
////		}
//
//		final ArrayList list = new ArrayList();
//		ResultSet rs = privateGetJDBCMetaData().getCatalogs();
//		try
//		{
//            if (rs != null) {
//    			final ResultSetReader rdr = new ResultSetReader(rs);
//    			Object[] row = null;
//    			while ((row = rdr.readRow()) != null)
//    			{
//    				list.add(row[0]);
//    			}
//            }
//		}
//		finally
//		{
//         close(rs);
//		}
//
////		value = (String[])list.toArray(new String[list.size()]);
////		_cache.put(key, value);
//
//		return (String[])list.toArray(new String[list.size()]);
//	}
//
//    /**
//     * Retrieves the URL for this DBMS.
//     * 
//     * @return  the URL for this DBMS or null if it cannot be generated
//     * 
//     * @throws SQLException if a database access error occurs
//     */
//    public synchronized String getURL() throws SQLException {
//        final String key = "getURL";
//        String value = (String)_cache.get(key);
//        if (value != null) {
//            return value;
//        }
//        
//        value = privateGetJDBCMetaData().getURL();
//        _cache.put(key, value);
//        
//        return value;
//    }    
//    
//    /**
//     * Retrieves the database vendor's preferred term for "catalog".
//     * 
//     * @return the vendor term for "catalog"
//     * 
//     * @throws SQLException if a database access error occurs
//     */
//    public synchronized String getCatalogTerm() throws SQLException {
//        final String key = "getCatalogTerm";
//        String value = (String)_cache.get(key);
//        if (value != null) {
//            return value;
//        }
//        
//        value = privateGetJDBCMetaData().getCatalogTerm();
//        _cache.put(key, value);
//        
//        return value;
//    }
//    
//    /**
//     * Retrieves the database vendor's preferred term for "schema".
//     * 
//     * @return  the vendor term for "schema"
//     * 
//     * @throws SQLException if a database access error occurs
//     */
//    public synchronized String getSchemaTerm() throws SQLException {
//        final String key = "getSchemaTerm";
//        String value = (String)_cache.get(key);
//        if (value != null) {
//            return value;
//        }
//        
//        value = privateGetJDBCMetaData().getSchemaTerm();
//        _cache.put(key, value);
//        
//        return value;        
//    }
//
//    /**
//     * Retrieves the database vendor's preferred term for "procedure".
//     * 
//     * @return the vendor term for "procedure"
//     * 
//     * @throws SQLException if a database access error occurs
//     */
//    public synchronized String getProcedureTerm() throws SQLException {
//        final String key = "getProcedureTerm";
//        String value = (String)_cache.get(key);
//        if (value != null) {
//            return value;
//        }
//        
//        value = privateGetJDBCMetaData().getProcedureTerm();
//        _cache.put(key, value);
//        
//        return value;        
//    }
//    
//    
//    /**
//     * Retrieves the String that this database uses as the separator between a
//     * catalog and table name. Cached on first call.
//     *
//     * @return  The separator character.
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//    public synchronized String getCatalogSeparator() throws SQLException
//	{
//		final String key = "getCatalogSeparator";
//		String value = (String)_cache.get(key);
//		if (value != null)
//		{
//			return value;
//		}
//
//		value = privateGetJDBCMetaData().getCatalogSeparator();
//		_cache.put(key, value);
//
//		return value;
//	}
//
//    /**
//     * Retrieves whether this database supports catalogs at all.
//     *
//     * @return  <TT>true</TT> fi database supports catalogs.
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//    public boolean supportsCatalogs() throws SQLException
//	{
//		return supportsCatalogsInTableDefinitions()
//			|| supportsCatalogsInDataManipulation()
//			|| supportsCatalogsInProcedureCalls();
//	}
//
//    /**
//     * Retrieves whether a catalog name can be used in a table definition
//     * statement. Cached on first call.
//     *
//     * @return  <TT>true</TT> if a catalog name can be used in a table
//     *          definition statement.
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//    public synchronized boolean supportsCatalogsInTableDefinitions() throws SQLException
//	{
//		final String key = "supportsCatalogsInTableDefinitions";
//		Boolean value = (Boolean)_cache.get(key);
//		if (value != null)
//		{
//			return value.booleanValue();
//		}
//
//		try
//		{
//			value = new Boolean(privateGetJDBCMetaData().supportsCatalogsInTableDefinitions());
//		}
//		catch (SQLException ex)
//		{
//			if (getDriverName().equals(IDriverNames.FREE_TDS))
//			{
//				value = Boolean.TRUE;
//			}
//			throw ex;
//		}
//
//		_cache.put(key, value);
//
//		return value.booleanValue();
//	}
//
//    /**
//     * Retrieves whether a catalog name can be used in a data manipulation
//     * statement. Cached on first call.
//     *
//     * @return  <TT>true</TT> if a catalog name can be used in a data
//     *          manipulation statement.
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//    public synchronized boolean supportsCatalogsInDataManipulation() throws SQLException
//	{
//		final String key = "supportsCatalogsInDataManipulation";
//		Boolean value = (Boolean)_cache.get(key);
//		if (value != null)
//		{
//			return value.booleanValue();
//		}
//
//		try
//		{
//			value = new Boolean(privateGetJDBCMetaData().supportsCatalogsInDataManipulation());
//		}
//		catch (SQLException ex)
//		{
//			if (getDriverName().equals(IDriverNames.FREE_TDS))
//			{
//				value = Boolean.TRUE;
//			}
//			throw ex;
//		}
//		_cache.put(key, value);
//
//		return value.booleanValue();
//	}
//
//    /**
//     * Retrieves whether a catalog name can be used in a procedure call. Cached
//     * on first call.
//     *
//     * @return  <TT>true</TT> if a catalog name can be used in a procedure
//     *          call.
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//	public synchronized boolean supportsCatalogsInProcedureCalls() throws SQLException
//	{
//		final String key = "supportsCatalogsInProcedureCalls";
//		Boolean value = (Boolean)_cache.get(key);
//		if (value != null)
//		{
//			return value.booleanValue();
//		}
//
//		try
//		{
//			value = new Boolean(privateGetJDBCMetaData().supportsCatalogsInProcedureCalls());
//		}
//		catch (SQLException ex)
//		{
//			if (getDriverName().equals(IDriverNames.FREE_TDS))
//			{
//				value = Boolean.TRUE;
//			}
//			throw ex;
//		}
//		_cache.put(key, value);
//
//		return value.booleanValue();
//	}
//
//    /**
//     * Return the <TT>DatabaseMetaData</TT> object for this connection.
//     *
//     * @return  The <TT>DatabaseMetaData</TT> object for this connection.
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//	public synchronized DatabaseMetaData getJDBCMetaData() throws SQLException
//	{
//		return privateGetJDBCMetaData();
//	}
//
//	/**
//     * 
//     * @return
//     * @throws SQLException
//	 */
////    public synchronized IDataSet getMetaDataSet() throws SQLException {
////        return new MetaDataDataSet(privateGetJDBCMetaData());
////    }
//    
//    /**
//     * @deprecated  Replaced by getDataTypes
//     */
//    public ResultSet getTypeInfo() throws SQLException
//	{
//		return privateGetJDBCMetaData().getTypeInfo();
//	}
//
//    /**
//     * 
//     * @return
//     * @throws DataSetException
//     */
////    public synchronized IDataSet getTypesDataSet() throws DataSetException {
////        ResultSet rs = null;
////        try {
////            rs = privateGetJDBCMetaData().getTypeInfo();
////            return (new DatabaseTypesDataSet(rs));
////        } catch (SQLException e) {
////            throw new DataSetException(e);
////        } finally {
////            if (rs != null) try { rs.close(); } catch (SQLException e) {}
////        }
////    }
//    
//    /**
//     * Retrieve information about the data types in the database.
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//    public synchronized DataTypeInfo[] getDataTypes()
//		throws SQLException
//	{
//		final DatabaseMetaData md = privateGetJDBCMetaData();
//		final ArrayList list = new ArrayList();
//		final ResultSet rs = md.getTypeInfo();
//		try
//		{
//			ResultSetColumnReader rdr = new ResultSetColumnReader(rs);
//			while (rdr.next())
//			{
//				final String typeName = rdr.getString(1);
//				final int dataType = rdr.getLong(2).intValue();
//				final int precis = rdr.getLong(3).intValue();
//				final String literalPrefix = rdr.getString(4);
//				final String literalSuffix = rdr.getString(5);
//				final String createParams = rdr.getString(6);
//				final int nullable = rdr.getLong(7).intValue();
//				final boolean caseSens = rdr.getBoolean(8).booleanValue();
//				final int searchable = rdr.getLong(9).intValue();
//				final boolean unsigned = rdr.getBoolean(10).booleanValue();
//				final boolean canBeMoney = rdr.getBoolean(11).booleanValue();
//				final boolean canBeAutoInc = rdr.getBoolean(12).booleanValue();
//				final String localTypeName = rdr.getString(13);
//				final int min = rdr.getLong(14).intValue();
//				final int max = rdr.getLong(15).intValue();
//				final int radix = rdr.getLong(18).intValue();
//				list.add(new DataTypeInfo(typeName, dataType, precis,
//										literalPrefix, literalSuffix,
//										createParams, nullable, caseSens,
//										searchable, unsigned, canBeMoney,
//										canBeAutoInc, localTypeName,
//										min, max, radix,
//										this));
//			}
//		}
//		finally
//		{
//         close(rs);
//      }
//		return (DataTypeInfo[])list.toArray(new DataTypeInfo[list.size()]);
//	}
//
//   private void close(ResultSet rs)
//   {
//      try
//      {
//         if (rs != null)
//         {
//            rs.close();
//         }
//      }
//      catch (Exception e)
//      {
////         s_log.info("closing resultset failed", e);
//      }
//   }
//
//   /**
//    * NOTE: This method should only be used by SchemaInfo since this class should not and does not cache.
//    *
//    * Retrieve information about the procedures in the system.
//    */
//   public synchronized IProcedureInfo[] getProcedures(String catalog,
//                                                      String schemaPattern,
//                                                      String procedureNamePattern,
//                                                      ProgressCallBack progressCallBack)
//     throws SQLException
//  {
//     DatabaseMetaData md = privateGetJDBCMetaData();
//     ArrayList list = new ArrayList();
//     ResultSet rs = md.getProcedures(catalog, schemaPattern, procedureNamePattern);
//     int count = 0;
//     try
//     {
//        final int[] cols = new int[]{1, 2, 3, 7, 8};
//        final ResultSetReader rdr = new ResultSetReader(rs, cols);
//        Object[] row = null;
//        while ((row = rdr.readRow()) != null)
//        {
//           final int type = ((Number) row[4]).intValue();
//           ProcedureInfo pi = new ProcedureInfo(getAsString(row[0]), getAsString(row[1]),
//              getAsString(row[2]), getAsString(row[3]), type, this);
//
//           list.add(pi);
//
//           if (null != progressCallBack)
//           {
//              if(0 == count++ % 200 )
//              {
//                 progressCallBack.currentlyLoading(pi.getSimpleName());
//              }
//           }
//        }
//     }
//     finally
//     {
//        close(rs);
//
//     }
//     return (IProcedureInfo[]) list.toArray(new IProcedureInfo[list.size()]);
//  }
//
//    /**
//     * Return a string array containing the different types of tables in this
//     * database. E.G. <TT>"TABLE", "VIEW", "SYSTEM TABLE"</TT>. Cached on first
//     * call.
//     *
//     * @return  table type names.
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//    public synchronized String[] getTableTypes() throws SQLException
//	{
//		final String key = "getTableTypes";
//		String[] value = (String[])_cache.get(key);
//		if (value != null)
//		{
//			return value;
//		}
//
//		final DatabaseMetaData md = privateGetJDBCMetaData();
//        
//		// Use a set rather than a list as some combinations of MS SQL and the
//		// JDBC/ODBC return multiple copies of each table type.
//		final Set tableTypes = new TreeSet();
//		final ResultSet rs = md.getTableTypes();
//		if (rs != null)
//		{
//			try
//			{
//				while (rs.next())
//				{
//					tableTypes.add(rs.getString(1).trim());
//				}
//			}
//			finally
//			{
//            close(rs);
//         }
//		}
//
//		final String dbProductName = getDatabaseProductName();
//		final int nbrTableTypes = tableTypes.size();
//
//		// InstantDB (at least version 3.13) only returns "TABLES"
//		// for getTableTypes(). If you try to use this in a call to
//		// DatabaseMetaData.getTables() no tables will be found. For the
//		// moment hard code the types for InstantDB.
//		if (nbrTableTypes == 1 && dbProductName.equals("InstantDB"))
//		{
//			tableTypes.clear();
//			tableTypes.add("TABLE");
//			tableTypes.add("SYSTEM TABLE");
//		}
//
//		// At least one version of PostgreSQL through the JDBC/ODBC
//		// bridge returns an empty result set for the list of table
//		// types. Another version of PostgreSQL returns 6 entries
//		// of "SYSTEM TABLE" (which we have already filtered back to one).
//		else if (dbProductName.equals("PostgreSQL"))
//		{
//			if (nbrTableTypes == 0 || nbrTableTypes == 1)
//			{
////                if (s_log.isDebugEnabled()) {
////                    s_log.debug("Detected PostgreSQL and "+nbrTableTypes+
////                                " table types - overriding to 4 table types");
////                }
//				tableTypes.clear();
//				tableTypes.add("TABLE");
//				tableTypes.add("SYSTEM TABLE");
//				tableTypes.add("VIEW");
//                tableTypes.add("SYSTEM VIEW");
//			}
//            // Treating indexes as tables interferes with the operation of the
//            // PostgreSQL plugin
//            if (tableTypes.contains("INDEX")) {
//                tableTypes.remove("INDEX");
//            }
//            // Treating sequences as tables interferes with the operation of the
//            // PostgreSQL plugin            
//            if (tableTypes.contains("SEQUENCE")) {
//                tableTypes.remove("SEQUENCE");
//            }
//		}
//
//		value = (String[])tableTypes.toArray(new String[tableTypes.size()]);
//		_cache.put(key, value);
//		return value;
//	}
//
//
//   /**
//    * NOTE: This method should only be used by SchemaInfo since this class sholud not and does not cache.
//    *
//    * Retrieve information about the tables in the system.
//    */
//   public synchronized ITableInfo[] getTables(String catalog,
//                                              String schemaPattern,
//                                              String tableNamePattern,
//                                              String[] types,
//                                              ProgressCallBack progressCallBack)
//      throws SQLException
//   {
//
//
//      final DatabaseMetaData md = privateGetJDBCMetaData();
//      final String dbDriverName = getDriverName();
//      Set list = new TreeSet();
//
//      if (dbDriverName.equals(IDriverNames.FREE_TDS) && schemaPattern == null)
//      {
//         schemaPattern = "dbo";
//      }
//      if (dbDriverName.equals(IDriverNames.AS400) && schemaPattern == null)
//      {
//          schemaPattern = "*ALLUSR";
//      }
//
//      //Add begin
//      if (catalog == null && DriverMatch.isComHttxDriver(_conn))
//      {
//          String[] catalogs=getCatalogs();
//          if (catalogs != null) {
//            for (int i = 0; i < catalogs.length; i++) {
//              ITableInfo[] tables = getTables(catalogs[i],
//                                              schemaPattern,
//                                              tableNamePattern,
//                                              types,
//                                              progressCallBack);
//              for(int j=0;j<tables.length;j++){
//                list.add(tables[j]);
//              }
//            }
//            return (ITableInfo[]) list.toArray(new ITableInfo[list.size()]);
//          }
//      }
//      //Add end
//
//
//      Map nameMap = null;
//      ResultSet superTabResult = null;
//      ResultSet tabResult = null;
//      try
//      {
//         if (supportsSuperTables) {
//             try
//             {
//                superTabResult = md.getSuperTables(catalog, 
//                                                   schemaPattern,
//                								   tableNamePattern);
//                // create a mapping of names if we have supertable info, since
//                // we need to find the ITableInfo again for re-ordering.
//                if (superTabResult != null && superTabResult.next())
//                {
//                   nameMap = new HashMap();
//                }
//             }
//             catch (Throwable th)
//             {
////                s_log.debug("DBMS/Driver doesn't support getSupertables(): "+
////                            th.getMessage());
//                supportsSuperTables = false;
//             }
//         }
//         // store all plain table info we have.
//         tabResult = md.getTables(catalog, schemaPattern, tableNamePattern, types);
//         int count = 0;
//         while (tabResult != null && tabResult.next())
//         {
//            ITableInfo tabInfo = new TableInfo(tabResult.getString(1),
//               tabResult.getString(2), tabResult.getString(3),
//               tabResult.getString(4), tabResult.getString(5),
//               this);
//            if (nameMap != null)
//            {
//               nameMap.put(tabInfo.getSimpleName(), tabInfo);
//            }
//            list.add(tabInfo);
//
//            if (null != progressCallBack)
//            {
//               if (0 == count++ % 100)
//               {
//                  progressCallBack.currentlyLoading(tabInfo.getSimpleName());
//               }
//            }
//         }
//
//         // re-order nodes if the tables are stored hierachically
//         if (nameMap != null)
//         {
//            do
//            {
//               String tabName = superTabResult.getString(3);
//               TableInfo tabInfo = (TableInfo) nameMap.get(tabName);
//               if (tabInfo == null)
//                  continue;
//               String superTabName = superTabResult.getString(4);
//               if (superTabName == null)
//                  continue;
//               TableInfo superInfo = (TableInfo) nameMap.get(superTabName);
//               if (superInfo == null)
//                  continue;
//               superInfo.addChild(tabInfo);
//               list.remove(tabInfo); // remove from toplevel.
//
//               if (null != progressCallBack)
//               {
//                  if (0 == count++ % 20)
//                  {
//                     progressCallBack.currentlyLoading(tabInfo.getSimpleName());
//                  }
//               }
//            }
//            while (superTabResult.next());
//         }
//      }
//      finally
//      {
//         close(tabResult);
//         close(superTabResult);
//      }
//
//      return (ITableInfo[]) list.toArray(new ITableInfo[list.size()]);
//   }
//   
//   /**
//     * Retrieve information about the UDTs in the system.
//     *
//     * @param   catalog     The name of the catalog to retrieve UDTs
//     *                      for. An empty string will return those without a
//     *                      catalog. <TT>null</TT> means that the catalog
//     *                      will not be used to narrow the search.
//     * @param   schemaPattern   The name of the schema to retrieve UDTs
//     *                      for. An empty string will return those without a
//     *                      schema. <TT>null</TT> means that the schema
//     *                      will not be used to narrow the search.
//     * @param   typeNamepattern     A type name pattern; must match the
//     *                              type name as it is stored in the
//     *                              database.
//     * @param   types       List of user-defined types (JAVA_OBJECT, STRUCT, or
//     *                      DISTINCT) to include; null returns all types
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//    public synchronized IUDTInfo[] getUDTs(String catalog, String schemaPattern,
//								           String typeNamePattern, int[] types)
//		throws SQLException
//	{
//		DatabaseMetaData md = privateGetJDBCMetaData();
//		ArrayList list = new ArrayList();
//		ResultSet rs = md.getUDTs(catalog, schemaPattern, typeNamePattern, types);
//		try
//		{
//			final int[] cols = new int[] {1, 2, 3, 4, 5, 6};
//			final ResultSetReader rdr = new ResultSetReader(rs, cols);
//			Object[] row = null;
//			while ((row = rdr.readRow()) != null)
//			{
//				list.add(new UDTInfo(getAsString(row[0]), getAsString(row[1]), getAsString(row[2]),
//									getAsString(row[3]), getAsString(row[4]), getAsString(row[5]),
//									this));
//			}
//		}
//		finally
//		{
//         close(rs);
//      }
//
//		return (IUDTInfo[])list.toArray(new IUDTInfo[list.size()]);
//	}
//
//   private String getAsString(Object val)
//   {
//      if(null == val)
//      {
//         return null;
//      }
//      else
//      {
//         if (val instanceof String) {
//             return (String)val;
//         } else {
//             return "" + val;
//         }
//      }
//
//   }
//
//   /**
//     * Retrieve the names of the Numeric Functions that this DBMS supports.
//     * Cached on first call.
//     *
//     * @return  String[] of function names.
//     */
//   public synchronized String[] getNumericFunctions() throws SQLException
//	{
//		final String key = "getNumericFunctions";
//		String[] value = (String[])_cache.get(key);
//		if (value != null)
//		{
//			return value;
//		}
//
//		value = makeArray(privateGetJDBCMetaData().getNumericFunctions());
//		_cache.put(key, value);
//		return value;
//	}
//
//    /**
//     * Retrieve the names of the String Functions that this DBMS supports.
//     * Cached on first call.
//     *
//     * @return  String[] of function names.
//     */
//	public synchronized String[] getStringFunctions() throws SQLException
//	{
//		final String key = "getStringFunctions";
//		String[] value = (String[])_cache.get(key);
//		if (value != null)
//		{
//			return value;
//		}
//
//		value = makeArray(privateGetJDBCMetaData().getStringFunctions());
//		_cache.put(key, value);
//		return value;
//	}
//
//    /**
//     * Retrieve the names of the System Functions that this DBMS supports.
//     * Cached on first call.
//     *
//     * @return  String[] of function names.
//     */
//	public synchronized String[] getSystemFunctions() throws SQLException
//	{
//		final String key = "getSystemFunctions";
//		String[] value = (String[])_cache.get(key);
//		if (value != null)
//		{
//			return value;
//		}
//
//		value = makeArray(privateGetJDBCMetaData().getSystemFunctions());
//		_cache.put(key, value);
//		return value;
//	}
//
//    /**
//     * Retrieve the names of the Date/Time Functions that this DBMS supports.
//     * Cached on first call.
//     *
//     * @return  String[] of function names.
//     */
//	public synchronized String[] getTimeDateFunctions() throws SQLException
//	{
//		final String key = "getTimeDateFunctions";
//		String[] value = (String[])_cache.get(key);
//		if (value != null)
//		{
//			return value;
//		}
//
//		value = makeArray(privateGetJDBCMetaData().getTimeDateFunctions());
//		_cache.put(key, value);
//		return value;
//	}
//
//    /**
//     * Retrieve the names of the non-standard keywords that this DBMS supports.
//     * Cached on first call.
//     *
//     * @return  String[] of keywords.
//     */
//	public synchronized String[] getSQLKeywords() throws SQLException
//	{
//		final String key = "getSQLKeywords";
//		String[] value = (String[])_cache.get(key);
//		if (value != null)
//		{
//			return value;
//		}
//
//		value = makeArray(privateGetJDBCMetaData().getSQLKeywords());
//		_cache.put(key, value);
//		return value;
//	}
//
//	public synchronized BestRowIdentifier[] getBestRowIdentifier(ITableInfo ti)
//		throws SQLException
//	{
//		final List results = new ArrayList();
//
//        ResultSet rs = null;
//		try
//		{
//            
//            rs = privateGetJDBCMetaData().getBestRowIdentifier(
//                    ti.getCatalogName(), ti.getSchemaName(),
//                    ti.getSimpleName(),
//                    DatabaseMetaData.bestRowTransaction, true);
//            
//			final String catalog = ti.getCatalogName();
//			final String schema = ti.getSchemaName();
//			final String table = ti.getSimpleName();
//
//			final ResultSetColumnReader rdr = new ResultSetColumnReader(rs);
//			while (rdr.next())
//			{
//				final BestRowIdentifier rid = new BestRowIdentifier(catalog,
//							schema, table, rdr.getLong(1).intValue(),
//							rdr.getString(2), rdr.getLong(3).shortValue(),
//							rdr.getString(4), rdr.getLong(5).intValue(),
//							rdr.getLong(7).shortValue(),
//							rdr.getLong(8).shortValue(), this);
//				results.add(rid);
//			}
//		}
//		finally
//		{
//            if (rs != null) try { rs.close(); } catch (SQLException e) {}
//		}
//
//		final BestRowIdentifier[] ar = new BestRowIdentifier[results.size()];
//		return (BestRowIdentifier[])results.toArray(ar);
//	}
//
//	/**
//     * @deprecated use getColumnPrivilegesDataSet instead. 
//	 */
//    public ResultSet getColumnPrivileges(ITableInfo ti)
//		throws SQLException
//	{
//		// MM-MYSQL driver doesnt support null for column name.
//		final String dbProdName = getDatabaseProductName();
//		final String columns = dbProdName.equalsIgnoreCase(IDBMSProductNames.MYSQL) ? "%" : null;
//		return privateGetJDBCMetaData().getColumnPrivileges(ti.getCatalogName(),
//													ti.getSchemaName(),
//													ti.getSimpleName(),
//													columns);
//	}
//
//    /**
//     * 
//     * @param ti
//     * @param columnIndices
//     * @param computeWidths
//     * @return
//     * @throws DataSetException
//     */
////    public synchronized IDataSet getColumnPrivilegesDataSet(ITableInfo ti,
////                                                            int[] columnIndices,
////                                                            boolean computeWidths) 
////        throws DataSetException 
////    {
////        ResultSet rs = null;
////        try {
////            DatabaseMetaData md = privateGetJDBCMetaData();
////            final String dbProdName = getDatabaseProductName();
////            final String columns = 
////                dbProdName.equalsIgnoreCase(IDBMSProductNames.MYSQL) ? "%" : null;
////            
////            rs = md.getColumnPrivileges(ti.getCatalogName(),
////                    ti.getSchemaName(),
////                    ti.getSimpleName(),
////                    columns);
////            ResultSetDataSet rsds = new ResultSetDataSet();
////                rsds.setResultSet(rs, columnIndices, computeWidths);
////            return rsds;
////        } catch (SQLException e) {
////            throw new DataSetException(e);
////        } finally {
////            if (rs != null) try { rs.close(); } catch (SQLException e) {}
////        }
////    }
//    
//    /**
//     *  @deprecated. Replaced by getExportedKeysInfo 
//     */
//    public ResultSet getExportedKeys(ITableInfo ti)
//		throws SQLException
//	{
//		return privateGetJDBCMetaData().getExportedKeys(
//			ti.getCatalogName(), ti.getSchemaName(),
//			ti.getSimpleName());
//	}
//
//    /**
//     * 
//     * @param ti
//     * @return
//     * @throws DataSetException
//     */
////    public synchronized IDataSet getExportedKeysDataSet(ITableInfo ti) 
////        throws DataSetException  
////    {
////        ResultSet rs = null;
////        try {
////            rs = privateGetJDBCMetaData().getExportedKeys(ti.getCatalogName(), 
////                                                          ti.getSchemaName(),
////                                                          ti.getSimpleName());
////            ResultSetDataSet rsds = new ResultSetDataSet();
////            rsds.setResultSet(rs, null, true);
////            return rsds;            
////        } catch (SQLException e) { 
////            throw new DataSetException(e);
////        } finally {
////            if (rs != null) try { rs.close(); } catch (SQLException e) {}
////        }
////    }
//    
//    /**
//     * @deprecated. Replaced by getImportedKeysInfo
//     */
//	public ResultSet getImportedKeys(ITableInfo ti)
//		throws SQLException
//	{
//		return privateGetJDBCMetaData().getImportedKeys(
//			ti.getCatalogName(), ti.getSchemaName(),
//			ti.getSimpleName());
//	}
//
//	public synchronized ForeignKeyInfo[] getImportedKeysInfo(String catalog, 
//                                                             String schema, 
//                                                             String tableName) 
//        throws SQLException 
//    {
//        ResultSet rs = 
//            privateGetJDBCMetaData().getImportedKeys(catalog, schema, tableName);
//        return getForeignKeyInfo(rs);
//    }
//    
//    public synchronized ForeignKeyInfo[] getImportedKeysInfo(ITableInfo ti)
//		throws SQLException
//	{
//		return getForeignKeyInfo(privateGetJDBCMetaData().getImportedKeys(ti.getCatalogName(),
//								ti.getSchemaName(), ti.getSimpleName()));
//	}
//
////    public synchronized IDataSet getImportedKeysDataSet(ITableInfo ti) 
////        throws DataSetException  
////    {
////        ResultSet rs = null;
////        try {
////            rs = privateGetJDBCMetaData().getImportedKeys(ti.getCatalogName(), 
////                                                          ti.getSchemaName(),
////                                                          ti.getSimpleName());
////            ResultSetDataSet rsds = new ResultSetDataSet();
////            rsds.setResultSet(rs, null, true);
////            return rsds;            
////        } catch (SQLException e) { 
////            throw new DataSetException(e);
////        } finally {
////            if (rs != null) try { rs.close(); } catch (SQLException e) {}
////        }
////    }    
//    
//    public synchronized ForeignKeyInfo[] getExportedKeysInfo(String catalog, String schema, String tableName)
//        throws SQLException
//    {
//        ResultSet rs = 
//            privateGetJDBCMetaData().getExportedKeys(catalog, schema, tableName);
//        return getForeignKeyInfo(rs);
//    } 
//    
//	public synchronized ForeignKeyInfo[] getExportedKeysInfo(ITableInfo ti)
//		throws SQLException
//	{
//		return getForeignKeyInfo(privateGetJDBCMetaData().getExportedKeys(ti.getCatalogName(),
//								ti.getSchemaName(), ti.getSimpleName()));
//	}
//        
//	private ForeignKeyInfo[] getForeignKeyInfo(ResultSet rs)
//		throws SQLException
//	{
//		final Map keys = new HashMap();
//		final Map columns = new HashMap();
//
//		try
//		{
//			final ResultSetColumnReader rdr = new ResultSetColumnReader(rs);
//			while (rdr.next())
//			{
//				final ForeignKeyInfo fki = new ForeignKeyInfo(rdr.getString(1),
//							rdr.getString(2), rdr.getString(3), rdr.getString(4),
//                            rdr.getString(5),rdr.getString(6), rdr.getString(7),
//                            rdr.getString(8),
//							rdr.getLong(10).intValue(), rdr.getLong(11).intValue(),
//							rdr.getString(12), rdr.getString(13),
//							rdr.getLong(14).intValue(), null, this);
//				final String key = createForeignKeyInfoKey(fki);
//				if (!keys.containsKey(key))
//				{
//					keys.put(key, fki);
//					columns.put(key, new ArrayList());
//				}
//
//				ForeignKeyColumnInfo fkiCol = new ForeignKeyColumnInfo(rdr.getString(8),
//														rdr.getString(8),
//														rdr.getLong(9).intValue());
//				((List)columns.get(key)).add(fkiCol);
//			}
//		}
//		finally
//		{
//         close(rs);
//      }
//
//		final ForeignKeyInfo[] results = new ForeignKeyInfo[keys.size()];
//		Iterator it = keys.values().iterator();
//		int idx = 0;
//		while (it.hasNext())
//		{
//			final ForeignKeyInfo fki = (ForeignKeyInfo)it.next();
//			final String key = createForeignKeyInfoKey(fki);
//			final List colsList = (List)columns.get(key);
//			final ForeignKeyColumnInfo[] fkiCol = (ForeignKeyColumnInfo[])colsList.toArray(new ForeignKeyColumnInfo[colsList.size()]);
//			fki.setForeignKeyColumnInfo(fkiCol);
//			results[idx++] = fki;
//		}
//
//		return results;
//	}
//
//    /**
//     * @deprecated use getIndexInfo instead.
//     */
//    public ResultSet getIndexInfo(ITableInfo ti)
//		throws SQLException
//	{
//		return privateGetJDBCMetaData().getIndexInfo(
//			ti.getCatalogName(), ti.getSchemaName(),
//			ti.getSimpleName(), false, true);
//	}
//
//    /**
//     * 
//     * @param ti
//     * @param columnIndices
//     * @param computeWidths
//     * @return
//     * @throws DataSetException
//     */
////    public synchronized ResultSetDataSet getIndexInfo(ITableInfo ti, 
////                                                      int[] columnIndices,
////                                                      boolean computeWidths) 
////        throws DataSetException 
////    {
////        ResultSet rs = null;
////        try {
////            rs = privateGetJDBCMetaData().getIndexInfo(
////                    ti.getCatalogName(), ti.getSchemaName(),
////                    ti.getSimpleName(), false, true);
////            ResultSetDataSet rsds = new ResultSetDataSet();
////            rsds.setResultSet(rs, columnIndices, computeWidths);
////            return rsds;
////        } catch (SQLException e) {
////            throw new DataSetException(e);
////        } finally {
////            if (rs != null) try { rs.close(); } catch (SQLException e) {}
////        }
////    }
//    
//    /**
//     * @deprecated use getPrimaryKey instead
//     */
//	public ResultSet getPrimaryKeys(ITableInfo ti)
//		throws SQLException
//	{
//		return privateGetJDBCMetaData().getPrimaryKeys(
//			ti.getCatalogName(), ti.getSchemaName(),
//			ti.getSimpleName());
//	}
//    
//	/**
//     * 
//     * @param ti
//     * @param columnIndices
//     * @param computeWidths
//     * @return
//     * @throws DataSetException
//	 */
////    public synchronized IDataSet getPrimaryKey(ITableInfo ti, 
////                                               int[] columnIndices,
////                                               boolean computeWidths)
////        throws DataSetException
////    {
////        ResultSet rs = null;
////        try {
////            rs = privateGetJDBCMetaData().getPrimaryKeys(
////                    ti.getCatalogName(), ti.getSchemaName(),
////                    ti.getSimpleName());
////            ResultSetDataSet rsds = new ResultSetDataSet();
////                rsds.setResultSet(rs, columnIndices , computeWidths);
////            return rsds;  
////        } catch (SQLException e) { 
////            throw new DataSetException(e);
////        }finally {
////            if (rs != null) try { rs.close(); } catch (SQLException e) {}
////        }
////    }
//
//    /**
//     * 
//     * @param ti
//     * @return
//     * @throws SQLException
//     */
//    public synchronized PrimaryKeyInfo[] getPrimaryKey(ITableInfo ti) 
//        throws SQLException
//    {
//        return getPrimaryKey(ti.getCatalogName(), 
//                             ti.getSchemaName(), 
//                             ti.getSimpleName());
//    }
//    
//    /**
//     * 
//     * @param ti
//     * @return
//     * @throws SQLException
//     */
//    public synchronized PrimaryKeyInfo[] getPrimaryKey(String catalog, 
//                                                       String schema, 
//                                                       String table) 
//        throws SQLException
//    {
//        final List<PrimaryKeyInfo> results = new ArrayList<PrimaryKeyInfo>();
//        ResultSet rs = null;
//        try {
//            rs = privateGetJDBCMetaData().getPrimaryKeys(catalog, schema,table);
//            while (rs.next()) {
//                PrimaryKeyInfo pkInfo = 
//                    new PrimaryKeyInfo(rs.getString(1),  // catalog
//                                       rs.getString(2),  // schema
//                                       rs.getString(3),  // tableName
//                                       rs.getString(4),  // columnName
//                                       rs.getShort(5),   // keySequence
//                                       rs.getString(6),  // pkName
//                                       this);
//                results.add(pkInfo);
//            }
//        }finally {
//            if (rs != null) try { rs.close(); } catch (SQLException e) {}
//        }
//        
//        final PrimaryKeyInfo[] ar = new PrimaryKeyInfo[results.size()];
//        return results.toArray(ar);
//    }
//    
//    
//    /**
//     * @deprecated use getProcedureColumnsDataSet instead
//     */
//    public ResultSet getProcedureColumns(IProcedureInfo ti)
//		throws SQLException
//	{
//		return privateGetJDBCMetaData().getProcedureColumns(ti.getCatalogName(),
//													        ti.getSchemaName(),
//													        ti.getSimpleName(),
//													        "%");
//	}
//
//    /**
//     * 
//     * @param ti
//     * @return
//     * @throws DataSetException
//     */
////    public synchronized IDataSet getProcedureColumnsDataSet(IProcedureInfo ti)
////        throws DataSetException
////    {
////        ResultSet rs = null;
////        try {
////            DatabaseMetaData md = privateGetJDBCMetaData();
////            rs = md.getProcedureColumns(ti.getCatalogName(),
////                                        ti.getSchemaName(),
////                                        ti.getSimpleName(),
////                                        "%");
////            ResultSetDataSet rsds = new ResultSetDataSet();
////            rsds.setResultSet(rs);
////            return rsds;
////        } catch (SQLException e) {
////            throw new DataSetException(e);
////        } finally {
////            if (rs != null) try { rs.close(); } catch (SQLException e) {}
////        }
////    }
//    
//    /**
//     * @deprecated use getTablePrivilegesDataSet instead
//     */ 
//	public ResultSet getTablePrivileges(ITableInfo ti)
//		throws SQLException
//	{
//		return privateGetJDBCMetaData().getTablePrivileges(ti.getCatalogName(),
//													ti.getSchemaName(),
//													ti.getSimpleName());
//	}
//
//	/**
//     * 
//     * @param ti
//     * @param columnIndices
//     * @param computeWidths
//     * @return
//     * @throws DataSetException
//	 */
////    public synchronized IDataSet getTablePrivilegesDataSet(ITableInfo ti,
////                                                           int[] columnIndices,
////                                                           boolean computeWidths) 
////        throws DataSetException 
////    {
////        ResultSet rs = null;
////        try {
////            DatabaseMetaData md = privateGetJDBCMetaData();
////            rs = md.getTablePrivileges(ti.getCatalogName(),
////                                       ti.getSchemaName(),
////                                       ti.getSimpleName());
////            ResultSetDataSet rsds = new ResultSetDataSet();
////            rsds.setResultSet(rs, columnIndices, computeWidths);
////            return rsds;
////        } catch (SQLException e) {
////            throw new DataSetException(e);
////        } finally {
////            if (rs != null) try { rs.close(); } catch (SQLException e) {}
////        }
////    }
//    
//    
//	/**
//     *  @deprecated use getVersionColumnsDataSet instead
//	 */
//	public ResultSet getVersionColumns(ITableInfo ti)
//		throws SQLException
//	{
//		return privateGetJDBCMetaData().getVersionColumns(ti.getCatalogName(),
//												          ti.getSchemaName(),
//												          ti.getSimpleName());
//	}
//
//	/**
//     * 
//     * @param ti
//     * @return
//     * @throws DataSetException
//	 */
////    public synchronized IDataSet getVersionColumnsDataSet(ITableInfo ti)
////        throws DataSetException
////    {
////        ResultSet rs = null;
////        try {
////            DatabaseMetaData md = privateGetJDBCMetaData();
////            rs = md.getVersionColumns(ti.getCatalogName(),
////                                      ti.getSchemaName(),
////                                      ti.getSimpleName());
////            ResultSetDataSet rsds = new ResultSetDataSet();
////            rsds.setResultSet(rs);
////            return rsds;
////        } catch (SQLException e) {
////            throw new DataSetException(e);
////        } finally {
////            if (rs != null) try { rs.close(); } catch (SQLException e) {}
////        }
////    }
////    
//    
//	private ResultSet getColumns(ITableInfo ti)
//		throws SQLException
//	{
//		return privateGetJDBCMetaData().getColumns(ti.getCatalogName(),
//											ti.getSchemaName(),
//											ti.getSimpleName(), "%");
//	}
//    
//	/**
//     * 
//     * @param ti
//     * @param columnIndices
//     * @param computeWidths
//     * @return
//     * @throws DataSetException
//	 */
////    public synchronized IDataSet getColumns(ITableInfo ti, 
////                                            int[] columnIndices,
////                                            boolean computeWidths)
////        throws DataSetException
////    {
////        IDataSet result = null;
////        ResultSet rs = null;
////        try {
////            rs = getColumns(ti);
////            ResultSetDataSet rsds = new ResultSetDataSet();
////            rsds.setResultSet(rs, columnIndices, computeWidths);
////            result = rsds;
////        } catch (SQLException e) { 
////            throw new DataSetException(e);
////        } finally {
////            if (rs != null) try { rs.close(); } catch (SQLException e) {}
////        }
////        return result;
////    }
//    
//    /**
//     * 
//     * @param catalog
//     * @param schema
//     * @param table
//     * @return
//     * @throws SQLException
//     */
//    public synchronized TableColumnInfo[] getColumnInfo(String catalog, 
//                                                        String schema, 
//                                                        String table) 
//        throws SQLException 
//    {
//       ResultSet rs = null;
//       try
//       {
//          final Map columns = new TreeMap();
//          DatabaseMetaData md = privateGetJDBCMetaData();
//          rs = md.getColumns(catalog, schema, table, "%");
//          final ResultSetColumnReader rdr = new ResultSetColumnReader(rs);
//
//          int index = 0;
//          while (rdr.next())
//          {
//             final TableColumnInfo tci = 
//                 new TableColumnInfo(rdr.getString(1),           // TABLE_CAT
//                                     rdr.getString(2),           // TABLE_SCHEM
//                                     rdr.getString(3),           // TABLE_NAME
//                                     rdr.getString(4),           // COLUMN_NAME
//                                     rdr.getLong(5).intValue(),  // DATA_TYPE
//                                     rdr.getString(6),           // TYPE_NAME
//                                     rdr.getLong(7).intValue(),  // COLUMN_SIZE
//                                     rdr.getLong(9).intValue(),  // DECIMAL_DIGITS
//                                     rdr.getLong(10).intValue(), // NUM_PREC_RADIX
//                                     rdr.getLong(11).intValue(), // NULLABLE
//                                     rdr.getString(12),          // REMARKS
//                                     rdr.getString(13),          // COLUMN_DEF
//                                     rdr.getLong(16).intValue(), // CHAR_OCTET_LENGTH
//                                     rdr.getLong(17).intValue(), // ORDINAL_POSITION
//                                     rdr.getString(18),          // IS_NULLABLE
//                                     this);
//             ////////////////////////////////////////////////////////////////////////////////////////////
//             // The index is needed in case this method is called with schema = null, catalog = null
//             // and two tables with the same name in different schemas/catalogs.
//             // Without the index the same ordinal position could only occur once.
//             ++index;
//             //
//             ////////////////////////////////////////////////////////////////////////////////////////////
//             columns.put(new Integer(10000 * tci.getOrdinalPosition()  + index), tci);
//          }
//
//          return (TableColumnInfo[]) columns.values().toArray(new TableColumnInfo[columns.size()]);
//
//       }
//       finally
//       {
//          if (rs != null) try
//          {
//             rs.close();
//          }
//          catch (SQLException e){}
//       }
//    }
//    
//    /**
//     * 
//     * @param ti
//     * @return
//     * @throws SQLException
//     */
//    public synchronized TableColumnInfo[] getColumnInfo(ITableInfo ti)
//		throws SQLException
//	{
//	    return getColumnInfo(ti.getCatalogName(), ti.getSchemaName(), ti.getSimpleName());
//    }
//
//    /**
//     * Retrieve whether this driver correctly handles Statement.setMaxRows(int).
//     * Some drivers such as version 5.02 of the Opta2000 driver use setMaxRows
//     * for UPDATEs, DELETEs etc. instead of just SELECTs. If this method returns
//     * <TT>false</TT> then setMaxRows should only be applied to statements
//     * that are running SELECTs.
//     *
//     * @return  <TT>true</TT> if this driver correctly implements setMaxRows().
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//    public boolean correctlySupportsSetMaxRows() throws SQLException
//	{
//		return !IDriverNames.OPTA2000.equals(getDriverName());
//	}
//
//    /**
//     * Retrieve whether this driver supports multiple result sets. Cached on
//     * first call.
//     *
//     * @return  <tt>true</tt> if driver supports multiple result sets
//     *          else <tt>false</tt>.
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//	public synchronized boolean supportsMultipleResultSets()
//			throws SQLException
//	{
//		final String key = "supportsMultipleResultSets";
//		Boolean value = (Boolean)_cache.get(key);
//		if (value != null)
//		{
//			return value.booleanValue();
//		}
//
//		value = new Boolean(privateGetJDBCMetaData().supportsMultipleResultSets());
//		_cache.put(key, value);
//
//		return value.booleanValue();
//	}
//
//    /**
//     * Retrieves whether this database treats mixed case unquoted SQL
//     * identifiers as case insensitive and stores them in upper case.
//     * Cached on first call.
//     *
//     * @return  <tt>true</tt> if driver stores upper case identifiers
//     *          else <tt>false</tt>.
//     *
//     * @throws  SQLException    Thrown if an SQL error occurs.
//     */
//	public synchronized boolean storesUpperCaseIdentifiers()
//		throws SQLException
//	{
//		final String key = "storesUpperCaseIdentifiers";
//		Boolean value = (Boolean)_cache.get(key);
//		if (value != null)
//		{
//			return value.booleanValue();
//		}
//
//		value = new Boolean(privateGetJDBCMetaData().storesUpperCaseIdentifiers());
//		_cache.put(key, value);
//
//		return value.booleanValue();
//	}
//
//
//    /**
//     * Clear cache of commonly accessed metadata properties.
//     */
//	public void clearCache()
//	{
//		_cache.clear();
//	}
//
//	/**
//	 * Make a String array of the passed string. Commas separate the elements
//	 * in the input string. The array is sorted.
//	 *
//	 * @param	data	Data to be split into the array.
//	 *
//	 * @return	data as an array.
//	 */
//	private static String[] makeArray(String data)
//	{
//		if (data == null)
//		{
//			data = "";
//		}
//
//		final List list = new ArrayList();
//		final StringTokenizer st = new StringTokenizer(data, ",");
//		while (st.hasMoreTokens())
//		{
//			list.add(st.nextToken());
//		}
//		Collections.sort(list);
//
//		return (String[])list.toArray(new String[list.size()]);
//	}
//
//	/**
//	 * Return the <TT>DatabaseMetaData</TT> object for this connection.
//	 *
//	 * @return	The <TT>DatabaseMetaData</TT> object for this connection.
//	 *
//	 * @throws	SQLException	Thrown if an SQL error occurs.
//	 */
//	private DatabaseMetaData privateGetJDBCMetaData() throws SQLException
//	{
//        checkThread();
//		return _conn.getConnection().getMetaData();
//	}
//
//    /**
//     * 
//     * @param fki
//     * @return
//     */
//	private String createForeignKeyInfoKey(ForeignKeyInfo fki)
//	{
//		final StringBuffer buf = new StringBuffer();
//		buf.append(fki.getForeignKeyCatalogName())
//			.append(fki.getForeignKeySchemaName())
//			.append(fki.getForeignKeyTableName())
//			.append(fki.getForeignKeyName())
//			.append(fki.getPrimaryKeyCatalogName())
//			.append(fki.getPrimaryKeySchemaName())
//			.append(fki.getPrimaryKeyTableName())
//			.append(fki.getPrimaryKeyName());
//		return buf.toString();
//	}
//    
//    /**
//     * Check the thread of the caller to see if it is the event dispatch thread
//     * and if we are debugging print a debug log message with the call trace.
//     */
//    private void checkThread() {
//        /* This is extremely useful when trying to track down Swing UI freezing.
//         * However, it currently fills the log which obscures other debug 
//         * messages even though UI performance is acceptable, so it is commented 
//         * out until it is needed later. 
//        if (s_log.isDebugEnabled() && SwingUtilities.isEventDispatchThread()) {
//            try {
//                throw new Exception("GUI Thread is doing database work");
//            } catch (Exception e) {
//                s_log.debug(e.getMessage(), e);
//            }
//        }
//        */
//    }
}

