package com.riversql.plugins.oracle.actions;

import com.riversql.IDManager;
import com.riversql.plugins.oracle.PackageBodyTypeNode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class PackageBodys {

    private String id;

    public PackageBodys(String id) {
        this.id = id;
    }

    public JSONObject execute() throws Exception {
        JSONObject results = new JSONObject();
        JSONArray meta = new JSONArray();
        JSONArray data = new JSONArray();
        Object obj = IDManager.get().get(id);
        PackageBodyTypeNode packageBodyTypeNode = (PackageBodyTypeNode) obj;
        ResultSet rs = null;
        PreparedStatement ps = null;


        String[] strs = {"Name", "Status", "Created", "Last DDL Time", "Timestamp"};

        for (int i = 0; i < strs.length; i++) {
            meta.put(strs[i]);
        }

        try {
            final String sql = "SELECT  object_name,status, created,last_ddl_time,timestamp " +
                    " FROM sys.all_objects where owner=? " +
                    " and object_type='PACKAGE BODY'";
            ps = packageBodyTypeNode.getConn().prepareStatement(sql);
            String owner = packageBodyTypeNode.getParent().getName();

            ps.setString(1, owner);
            rs = ps.executeQuery();
            while (rs.next()) {
                JSONArray record = new JSONArray();
                record.put(rs.getString(1));
                record.put(rs.getString(2));
                record.put(rs.getString(3));
                record.put(rs.getString(4));
                record.put(rs.getString(5));

                data.put(record);
            }

        } catch (Exception e) {

        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (Exception e) {
            }
            try {
                if (ps != null)
                    ps.close();
            } catch (Exception e) {
            }
        }
        results.put("meta", meta);
        results.put("data", data);
        return results;
    }

}
