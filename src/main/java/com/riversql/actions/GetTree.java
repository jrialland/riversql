package com.riversql.actions;

import com.riversql.IDManager;
import com.riversql.JSONAction;
import com.riversql.dbtree.DatabaseNode;
import com.riversql.dbtree.IStructureNode;
import com.riversql.dbtree.SQLSession;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class GetTree implements JSONAction {
    String node, refresh;
    String dbid;
    private boolean refreshing;

    public void setDbid(String dbid) {
        this.dbid = dbid;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public JSONObject execute(HttpServletRequest request,
                              HttpServletResponse response,
                              EntityManager em, EntityTransaction et) throws Exception {


        if ("root".equals(node)) {

            SQLSession sqlsession = null;
            if (dbid != null && !dbid.equals("")) {

                sqlsession = (SQLSession) IDManager.get().get((dbid));
                if (sqlsession == null) {
                    return null;
                    //sqlsession = createSQLSession(request, em,dbid);
                    //sessions.getSqlsessions().add(new SQLSession(alias.getAliasname(),conn));
                }
            } else {
                //JSONObject obj=new JSONObject();

                JSONArray arr = new JSONArray();
                //arr.put(obj);
                JSONObject ret = new JSONObject();
                ret.put("nodes", arr);
                return ret;
            }

            DatabaseNode dn = sqlsession.getDatabaseNode();
            if (refreshing)
                dn.refresh();
            return dn.toJSON();

        } else {
            String id = node;
            Object obj = IDManager.get().get(id);
            if (obj != null) {
                if (obj instanceof IStructureNode) {
                    if (refreshing)
                        ((IStructureNode) obj).refresh();
                    JSONObject js = ((IStructureNode) obj).getChildrenToJSon();
                    return js;
                }
            }
        }
        return null;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
        this.refreshing = refresh != null && "true".equals(refresh);
    }

}
