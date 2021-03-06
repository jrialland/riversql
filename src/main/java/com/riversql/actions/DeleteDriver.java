package com.riversql.actions;

import com.riversql.JSONAction;
import com.riversql.dao.DriversDAO;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class DeleteDriver implements JSONAction {
    String id;

    public void setId(String id) {
        this.id = id;
    }

    public JSONObject execute(HttpServletRequest request,
                              HttpServletResponse response, EntityManager em, EntityTransaction et)
            throws Exception {
        DriversDAO.deleteDriver(em, id);
        return null;
    }
}
