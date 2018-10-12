package com.riversql.actions;

import com.riversql.JSONAction;
import com.riversql.dao.DriversDAO;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CreateDriver implements JSONAction {


    String driverclass, drivername, exampleurl;

    public void setDriverclass(String driverclass) {
        this.driverclass = driverclass;
    }

    public void setDrivername(String drivername) {
        this.drivername = drivername;
    }

    public void setExampleurl(String exampleurl) {
        this.exampleurl = exampleurl;
    }

    public JSONObject execute(HttpServletRequest request,
                              HttpServletResponse response, EntityManager em, EntityTransaction et)
            throws Exception {
        DriversDAO.addDriver(em, drivername, driverclass, exampleurl);
        JSONObject obj = new JSONObject();
        obj.put("success", true);
        return obj;
    }
}
