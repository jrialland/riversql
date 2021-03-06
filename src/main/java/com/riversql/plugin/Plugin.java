package com.riversql.plugin;

import com.riversql.dbtree.CatalogNode;
import com.riversql.dbtree.IStructureNode;
import com.riversql.dbtree.SchemaNode;
import com.riversql.sql.SQLConnection;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


public interface Plugin {

    List<IStructureNode> getSchemaAddedChildren(SchemaNode schemaNode, SQLConnection conn);

    JSONArray[] getContextMenu(SQLConnection conn, String nodeType);

    JSONObject executeAction(HttpServletRequest request,
                             HttpServletResponse response,
                             EntityManager em, EntityTransaction et) throws Exception;

    JSONArray[] getAddedTabs(SQLConnection conn, String nodeType);

    List<IStructureNode> getCatalogAddedChildren(CatalogNode schemaNode,
                                                 SQLConnection conn);

    JSONArray[] getDynamicPluginScripts(SQLConnection conn);
}
