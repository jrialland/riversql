package com.riversql.sql;

import com.riversql.id.IHasIdentifier;
import com.riversql.id.IIdentifier;
import com.riversql.id.IntegerIdentifier;
import com.riversql.id.IntegerIdentifierFactory;
import com.riversql.util.StringManager;
import com.riversql.util.StringManagerFactory;

import java.io.Serializable;

/**
 * Defines the different types of database objects.
 *
 * @author <A HREF="mailto:colbell@users.sourceforge.net">Colin Bell</A>
 */
public class DatabaseObjectType implements IHasIdentifier, Serializable {

    static final long serialVersionUID = 2325635336825122256L;
    /**
     * Internationalized strings for this class.
     */
    private static final StringManager s_stringMgr =
            StringManagerFactory.getStringManager(DatabaseObjectType.class);

    /**
     * Other - general purpose.
     */
    public final static DatabaseObjectType OTHER = createNewDatabaseObjectType(s_stringMgr.getString("DatabaseObjectType.other"));

    /**
     * Column.
     */
    public final static DatabaseObjectType COLUMN = createNewDatabaseObjectType(s_stringMgr.getString("DatabaseObjectType.column"));

    /**
     * Standard datatype.
     */
    public final static DatabaseObjectType DATATYPE = createNewDatabaseObjectType(s_stringMgr.getString("DatabaseObjectType.datatype"));

    /**
     * Unique Key for a table.
     */
    public final static DatabaseObjectType PRIMARY_KEY = createNewDatabaseObjectType(s_stringMgr.getString("DatabaseObjectType.primarykey"));
    /**
     * Foreign Key relationship.
     */
    public final static DatabaseObjectType FOREIGN_KEY = createNewDatabaseObjectType(s_stringMgr.getString("DatabaseObjectType.foreignkey"));

    /**
     * Stored procedure.
     */
    public final static DatabaseObjectType PROCEDURE = createNewDatabaseObjectType(s_stringMgr.getString("DatabaseObjectType.storproc"));

    /**
     * TABLE.
     */
    public final static DatabaseObjectType TABLE = createNewDatabaseObjectType(s_stringMgr.getString("DatabaseObjectType.table"));

    public static final DatabaseObjectType VIEW = createNewDatabaseObjectType(s_stringMgr.getString("DatabaseObjectType.view"));

    /**
     * User defined type.
     */
    public final static DatabaseObjectType UDT = createNewDatabaseObjectType(s_stringMgr.getString("DatabaseObjectType.udt"));

    /**
     * Uniquely identifies this Object.
     */
    private final IIdentifier _id;

    /**
     * Describes this object type.
     */
    private final String _name;

    /**
     * Default ctor.
     */
    private DatabaseObjectType(String name) {
        super();
        _id = new IntegerIdentifierFactory().createIdentifier();
        _name = name != null ? name : _id.toString();
    }

    public static DatabaseObjectType createNewDatabaseObjectType(String name) {
        return new DatabaseObjectType(name);
    }

    /**
     * Return the object that uniquely identifies this object.
     *
     * @return Unique ID.
     */
    public IIdentifier getIdentifier() {
        return _id;
    }

    /**
     * Retrieve the descriptive name of this object.
     *
     * @return The descriptive name of this object.
     */
    public String getName() {
        return _name;
    }

    public String toString() {
        return getName();
    }
}
