package com.riversql.actions;

import com.riversql.JSONAction;
import com.riversql.dao.DriversDAO;
import com.riversql.dao.SourcesDAO;
import com.riversql.entities.Driver;
import com.riversql.entities.Source;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.DriverManager;

public class TestSourceConnection implements JSONAction {

    String password;
    int id;

    public void setPassword(String password) {
        this.password = password;
    }

    public void setId(int id) {
        this.id = id;
    }

    public JSONObject execute(HttpServletRequest request,
                              HttpServletResponse response, EntityManager em, EntityTransaction et)
            throws Exception {
        Source s = SourcesDAO.getSource(em, id);

        int driverid = s.getDriverid();
        Driver driver = DriversDAO.getDriver(em, driverid);
        String jdbcUrl = s.getJdbcUrl();
        String username = s.getUserName();
        //Driver drv=null;
        Connection conn = null;
        try {
            Class.forName(driver.getDriverClassName());
            conn = DriverManager.getConnection(jdbcUrl, username, password);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                }
            }
        }

        return new JSONObject();
    }

}
