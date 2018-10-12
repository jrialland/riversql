package com.riversql;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

@SuppressWarnings("serial")
public class Page extends DoServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(Page.class);

    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp, EntityManager em, EntityTransaction et) throws Exception {
        try {
            String className = req.getParameter("class");
            Class iPageActionClass = Class.forName("com.riversql.actions." + className);
            IPageAction iPageAction = (IPageAction) iPageActionClass.newInstance();

            BeanUtils.populate(iPageAction, req.getParameterMap());
            et = em.getTransaction();
            et.begin();
            iPageAction.execute(req, resp, em, et);
            et.commit();
        } catch (Exception e) {

            //TODO return page with error
            if (et != null && et.isActive())
                et.rollback();
            try {
                req.setAttribute("pageid", req.getParameter("pageid"));
                req.setAttribute("emsg", e.getMessage());
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                pw.close();
                sw.close();

                req.setAttribute("error", sw.toString());
                req.getRequestDispatcher("error.jsp").forward(req, resp);

            } catch (Exception e1) {

                LOGGER.error("could not display error page", e1);
            }
        } finally {
            IDManager.set(null);
            if (em != null)
                em.close();
        }
    }
}
