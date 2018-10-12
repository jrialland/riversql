package com.riversql.plugins.mysql;

import com.riversql.dbtree.CatalogNode;
import com.riversql.dbtree.IStructureNode;
import com.riversql.plugin.BasePluginType;
import com.riversql.sql.SQLConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TriggersNode extends BasePluginType implements IStructureNode {

    public TriggersNode(CatalogNode caNode, SQLConnection conn) {
        super("Trigger", caNode, conn);
    }

    @Override
    public void load() {
        if (loaded)
            return;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            final String sql = "select trigger_name from information_schema.triggers where trigger_schema=? order by 1 asc";
            ps = conn.prepareStatement(sql);
            String owner = getOwner();
            ps.setString(1, owner);
            rs = ps.executeQuery();
            while (rs.next()) {
                String oname = rs.getString(1);
                TriggerNode triggerNode = new TriggerNode(this, oname, conn);
                list.add(triggerNode);
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

        return "mysql_triggers";
    }

    public boolean isLeaf() {

        return false;
    }

}
