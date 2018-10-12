package com.riversql.dbtree;

import com.riversql.IDManager;
import com.riversql.sql.SQLConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public abstract class DBNode implements IStructureNode {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBNode.class);

    protected List<IStructureNode> children = new ArrayList<IStructureNode>();
    protected String id;
    protected SQLConnection conn;
    private boolean loaded;

    public DBNode(SQLConnection conn) {
        this.id = IDManager.get().nextID();
        IDManager.get().put(id, this);
        this.conn = conn;
    }

    public void refresh() {
        loaded = false;
        children.clear();
    }

    public String getId() {
        return id;
    }

    final public SQLConnection getConn() {
        return conn;
    }

    final public JSONObject getChildrenToJSon() {
        try {
            load();
        } catch (SQLException e1) {
            LOGGER.error("getChildrenToJSon", e1);
        }

        JSONObject js = new JSONObject();
        try {
            JSONArray arr = new JSONArray();
            for (int i = 0; i < children.size(); i++) {
                JSONObject obj = new JSONObject();
                IStructureNode is = children.get(i);
                obj.put("text", is.getName());
                obj.put("id", is.getId());
                obj.put("leaf", is.isLeaf());
                obj.put("type", is.getType());
                obj.put("cls", is.getCls());
                obj.put("qname", is.getQualifiedName());
                arr.put(obj);
            }
            js.put("nodes", arr);
        } catch (JSONException e) {
            LOGGER.error("getChildrenToJSon", e);
        }
        return js;
    }

    public String getQualifiedName() {
        return null;
    }

    abstract protected void nodeLoad() throws SQLException;

    final public void load() throws SQLException {
        if (loaded) return;
        nodeLoad();
        loaded = true;
    }

    final public List<IStructureNode> getChildren() {
        try {
            load();
        } catch (SQLException e1) {
            LOGGER.error("getChildren", e1);
        }
        return children;
    }

    public IStructureNode getChildrenByName(String nodeName) throws SQLException {
        load();
        IStructureNode childNode = null;
        for (int i = 0; i < children.size(); i++) {
            childNode = children.get(i);
            if (childNode.getName().equalsIgnoreCase(nodeName)) {
                return childNode;
            }
        }
        return childNode;
    }

}
