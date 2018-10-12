package com.riversql.plugins.oracle;


import com.riversql.plugin.BasePluginType;
import com.riversql.sql.SQLConnection;

public class PackageNode extends BasePluginType {


    //private String status;

    public PackageNode(PackageTypeNode packageTypeNode, String name,
                       SQLConnection conn, String status) {
        super(name, packageTypeNode, conn);

        //this.status=status;

    }


    @Override
    public void load() {
    }

    public String getCls() {

        return "package";
    }

    public String getType() {

        return "ora_pkg";
    }

    public boolean isLeaf() {

        return true;
    }

}
