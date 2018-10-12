package com.riversql.sql;

import java.io.Serializable;

public interface ITableInfo extends IDatabaseObjectInfo, Serializable {
    String getType();

    String getRemarks();

    ITableInfo[] getChildTables();

    ForeignKeyInfo[] getImportedKeys();

    void setImportedKeys(ForeignKeyInfo[] foreignKeys);

    ForeignKeyInfo[] getExportedKeys();

    void setExportedKeys(ForeignKeyInfo[] foreignKeys);
}
