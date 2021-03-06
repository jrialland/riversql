package com.riversql.plugins.mysql;

import com.riversql.plugin.BasePluginType;
import com.riversql.sql.SQLConnection;

public class UserNode extends BasePluginType {

    public UserNode(UsersNode usersNode, String name,
                    SQLConnection conn) {
        super(name, usersNode, conn);
    }


    @Override
    public void load() {
    }

    public String getCls() {
        return "obj";
    }

    public String getType() {

        return "mysql_user";
    }

    public boolean isLeaf() {

        return true;
    }

}
