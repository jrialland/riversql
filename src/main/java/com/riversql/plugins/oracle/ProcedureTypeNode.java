package com.riversql.plugins.oracle;

import com.riversql.dbtree.IStructureNode;
import com.riversql.dbtree.SchemaNode;
import com.riversql.plugin.BasePluginType;
import com.riversql.sql.SQLConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ProcedureTypeNode extends BasePluginType implements IStructureNode {


    public ProcedureTypeNode(SchemaNode schemaNode, SQLConnection conn) {
        super("Procedure", schemaNode, conn);
    }

    @Override
    public void load() {
        if (loaded)
            return;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            final String sql = "SELECT  object_name,status, created,last_ddl_time,timestamp " +
                    " FROM sys.all_objects where owner=? " +
                    " and object_type='PROCEDURE' order by 1 asc";
            ps = conn.prepareStatement(sql);
            String owner = getOwner();
            ps.setString(1, owner);
            rs = ps.executeQuery();
            while (rs.next()) {
                String name2 = rs.getString(1);
                String status = rs.getString(2);
                ProcedureNode pkNode = new ProcedureNode(this, name2, conn, status);
                list.add(pkNode);
            }

        } catch (Exception e) {
            list.clear();
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
        loaded = true;
    }

    private String getOwner() {
        return parentNode.getName();
    }

    public String getCls() {
        return "objs";
    }

    public String getType() {
        return "ora_procs";
    }

    public boolean isLeaf() {
        return false;
    }

}
