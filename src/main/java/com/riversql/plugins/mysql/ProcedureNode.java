package com.riversql.plugins.mysql;

import com.riversql.plugin.BasePluginType;
import com.riversql.sql.SQLConnection;

public class ProcedureNode extends BasePluginType {

    public ProcedureNode(ProcedureTypeNode procTypeNode, String name,
                         SQLConnection conn) {
        super(name, procTypeNode, conn);
    }


    @Override
    public void load() {
    }

    public String getCls() {
        return "obj";
    }

    public String getType() {

        return "mysql_proc";
    }

    public boolean isLeaf() {

        return true;
    }

}
