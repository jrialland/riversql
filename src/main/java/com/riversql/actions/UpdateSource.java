package com.riversql.actions;

import com.riversql.JSONAction;
import com.riversql.dao.SourcesDAO;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class UpdateSource implements JSONAction {

    private int sourceid;
    private String user;
    private int driverid;

    private String url;
    private String sourceName;

    public void setDriverid(int driverid) {
        this.driverid = driverid;
    }

    public void setSourceid(int sourceid) {
        this.sourceid = sourceid;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public JSONObject execute(HttpServletRequest request,
                              HttpServletResponse response, EntityManager em, EntityTransaction et) throws Exception {

        JSONObject obj = new JSONObject();

        SourcesDAO.updateSource(em, sourceid, url, driverid, user, sourceName);

        return obj;
    }

}
