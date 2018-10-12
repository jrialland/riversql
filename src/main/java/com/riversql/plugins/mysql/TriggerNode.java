package com.riversql.plugins.mysql;

import com.riversql.plugin.BasePluginType;
import com.riversql.sql.SQLConnection;

public class TriggerNode extends BasePluginType {

    public TriggerNode(TriggersNode triggersNode, String name,
                       SQLConnection conn) {
        super(name, triggersNode, conn);
    }


    @Override
    public void load() {
    }

    public String getCls() {
        return "obj";
    }

    public String getType() {

        return "mysql_trigger";
    }

    public boolean isLeaf() {

        return true;
    }

}
