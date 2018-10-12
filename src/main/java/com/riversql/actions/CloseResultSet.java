package com.riversql.actions;

import com.riversql.IDManager;
import com.riversql.JSONAction;
import com.riversql.utils.SQLExecutor;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CloseResultSet implements JSONAction {
    String rset;

    public void setRset(String rset) {
        this.rset = rset;
    }


    public JSONObject execute(HttpServletRequest request,
                              HttpServletResponse response, EntityManager em, EntityTransaction et)
            throws Exception {
        SQLExecutor executor = (SQLExecutor) IDManager.get().get(rset);
        if (executor != null) {
            executor.close();
        }
        return null;
    }

}
