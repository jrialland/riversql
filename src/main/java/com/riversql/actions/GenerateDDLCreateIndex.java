package com.riversql.actions;

//import java.util.Arrays;

import com.riversql.JSONAction;
import com.riversql.WebSQLSession;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class GenerateDDLCreateIndex implements JSONAction {
    String tableid;

    public void setTableid(String tableid) {
        this.tableid = tableid;
    }

    public JSONObject execute(HttpServletRequest request,
                              HttpServletResponse response, WebSQLSession sessions,
                              EntityManager em, EntityTransaction et) throws Exception {

        JSONObject obj = new JSONObject();

        return obj;
    }

    public JSONObject execute(HttpServletRequest request,
                              HttpServletResponse response, EntityManager em, EntityTransaction et)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
}
