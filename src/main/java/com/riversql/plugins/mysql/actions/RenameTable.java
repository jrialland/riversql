package com.riversql.plugins.mysql.actions;

import com.riversql.sql.SQLConnection;
import org.json.JSONObject;

import java.sql.PreparedStatement;


public class RenameTable {

    private final SQLConnection conn;
    private final String tableName;
    private final String newName;

    public RenameTable(SQLConnection conn, String tableName, String newName) {
        this.conn = conn;
        this.tableName = tableName;
        this.newName = newName;
    }

    public JSONObject execute() throws Exception {
        String sql = "RENAME TABLE " + tableName + " TO " + newName;
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
