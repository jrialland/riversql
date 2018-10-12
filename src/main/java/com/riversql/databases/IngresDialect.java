package com.riversql.databases;

public class IngresDialect
        implements IDialect {

    public boolean supportsDatabase(String databaseProductName,
                                    String databaseProductVersion) {
        if (databaseProductName == null) {
            return false;
        }
        if (databaseProductName.trim().toLowerCase().startsWith("ingres")) {
            // We don't yet have the need to discriminate by version.
            return true;
        }
        return false;
    }


}
