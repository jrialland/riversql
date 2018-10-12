package com.riversql.actions;

import com.riversql.IDManager;
import com.riversql.JSONAction;
import com.riversql.WebSQLSession;
import com.riversql.dao.DriversDAO;
import com.riversql.dao.SourcesDAO;
import com.riversql.dbtree.SQLSession;
import com.riversql.entities.Driver;
import com.riversql.entities.Source;
import com.riversql.plugin.PluginManager;
import com.riversql.sql.ISQLDriver;
import com.riversql.sql.SQLConnection;
import com.riversql.sql.SQLDriver;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.DriverManager;

public class Connect implements JSONAction {
    String user, password;
    int driverid, sourceid;
    String autocommit;

    public void setAutocommit(String autocommit) {
        this.autocommit = autocommit;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDriverid(int driverid) {
        this.driverid = driverid;
    }

    public void setSourceid(int sourceid) {
        this.sourceid = sourceid;
    }

    public JSONObject execute(HttpServletRequest request,
                              HttpServletResponse response, EntityManager em, EntityTransaction et)
            throws Exception {
        Driver driver = DriversDAO.getDriver(em, driverid);
        Source source = SourcesDAO.getSource(em, sourceid);
        ISQLDriver idriver = new SQLDriver();
        idriver.setDriverClassName(driver.getDriverClassName());

        JSONObject obj = new JSONObject();

        java.sql.Driver sqlDriver = (java.sql.Driver) Class.forName(driver.getDriverClassName()).newInstance();
        DriverManager.registerDriver(sqlDriver);


        Connection _conn = DriverManager.getConnection(source.getJdbcUrl(), user, password);
        if (autocommit != null) {
            _conn.setAutoCommit(true);
        } else {
            _conn.setAutoCommit(false);
        }

        SQLConnection conn = new SQLConnection(_conn, null, idriver);

        WebSQLSession sessions = (WebSQLSession) request.getSession(true).getAttribute("sessions");
        sessions.getSqlsessions().add(new SQLSession(sourceid, source.getSourceName() + " (" + IDManager.get().nextSessionID() + ")", conn));
        obj.put("success", true);
        JSONArray arr = new JSONArray();
        PluginManager.getInstance().dynamicPluginScripts(arr, conn);
        obj.put("pluginScripts", arr);

        return obj;
    }

}
