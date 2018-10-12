package com.riversql.plugins.mysql.actions;

import com.riversql.sql.SQLConnection;
import org.json.JSONObject;

import java.sql.PreparedStatement;


public class EmptyTable {

    private final SQLConnection conn;
    private final String tableName;

    public EmptyTable(SQLConnection conn, String tableName) {
        this.conn = conn;
        this.tableName = tableName;

    }

    public JSONObject execute() throws Exception {
        String sql = "TRUNCATE TABLE " + tableName;
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.execute();

        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                }
            }
        }
        return new JSONObject();
    }


}
