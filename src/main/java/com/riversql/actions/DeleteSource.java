package com.riversql.actions;

import com.riversql.JSONAction;
import com.riversql.dao.SourcesDAO;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DeleteSource implements JSONAction {

    int id;

    public void setId(int id) {
        this.id = id;
    }

    public JSONObject execute(HttpServletRequest request,
                              HttpServletResponse response, EntityManager em, EntityTransaction et)
            throws Exception {
        SourcesDAO.deleteSource(em, id);
        return null;
    }

}
