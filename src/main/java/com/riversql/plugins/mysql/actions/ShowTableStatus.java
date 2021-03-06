package com.riversql.plugins.mysql.actions;

import com.riversql.dbtree.CatalogNode;
import com.riversql.sql.SQLConnection;

public class ShowTableStatus extends Show {

    private CatalogNode cn;

    public ShowTableStatus(SQLConnection conn, CatalogNode cn) {
        super(conn);
        this.cn = cn;
    }

    @Override
    public String getShowString() {
        return "SHOW TABLE STATUS FROM " + cn.getName();
    }

}
