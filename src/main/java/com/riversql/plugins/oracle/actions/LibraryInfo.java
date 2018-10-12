package com.riversql.plugins.oracle.actions;

import com.riversql.IDManager;
import com.riversql.plugin.BasePluginType;
import com.riversql.plugins.oracle.LibraryNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class LibraryInfo {
    String id;

    public LibraryInfo(String id) {
        this.id = id;
    }

    public JSONObject execute() throws Exception {
        JSONObject results = new JSONObject();
        JSONArray meta = new JSONArray();
        JSONArray data = new JSONArray();
        String[] strs = {"Property", "Value"};

        for (int i = 0; i < strs.length; i++) {
            meta.put(strs[i]);
        }
        Object obj = IDManager.get().get(id);
        LibraryNode library = (LibraryNode) obj;
        final String sql =
                "SELECT  created,last_ddl_time, timestamp, a.status, dynamic, file_spec FROM sys.all_objects a, sys.all_libraries b " +
                        " where a.owner=? and object_type='LIBRARY' and object_name=? " +
                        " and a.owner=b.owner and a.object_name=b.library_name ";

        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = library.getConn().prepareStatement(sql);
            BasePluginType tot = (BasePluginType) library.getParent();
            String owner = tot.getParent().getName();
            ps.setString(1, owner);
            ps.setString(2, library.getName());
            rs = ps.executeQuery();

            if (rs.next()) {
                JSONArray record = new JSONArray();
                record.put("Created");
                record.put(rs.getString(1));
                data.put(record);

                record = new JSONArray();
                record.put("Last DDL Time");
                record.put(rs.getString(2));
                data.put(record);

                record = new JSONArray();
                record.put("TimeStamp");
                record.put(rs.getString(3));
                data.put(record);

                record = new JSONArray();
                record.put("Status");
                record.put(rs.getString(4));
                data.put(record);

                record = new JSONArray();
                record.put("Dynamic");
                record.put(rs.getString(5));
                data.put(record);

                record = new JSONArray();
                record.put("File Spec");
                record.put(rs.getString(6));
                data.put(record);


            }
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("error", e);
            ;
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
