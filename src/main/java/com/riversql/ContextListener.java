
package com.riversql;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;


public class ContextListener implements ServletContextListener {

    private static final String PU_NAME = "riversql";

    private static EntityManagerFactory emf;

    public void contextDestroyed(ServletContextEvent sce) {
        EntityManagerFactory emf = (EntityManagerFactory) sce.getServletContext().getAttribute("emf");
        if (emf != null)
            emf.close();
    }

    protected EntityManagerFactory createEntityManagerFactoryFromProperties(Path propFile) throws IOException {
        if (!Files.isRegularFile(propFile)) {
            throw new IllegalStateException("Not a regular file : " + propFile);
        }
        try (final InputStream in = Files.newInputStream(propFile)) {
            final Properties props = new Properties();
            props.load(in);

            return Persistence.createEntityManagerFactory(PU_NAME, props);
        }

    }

    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();

        Path props = Paths.get(sc.getRealPath("WEB-INF/database.properties"));

        try {
            EntityManagerFactory emf = createEntityManagerFactoryFromProperties(props);
            setEntityManagerFactory(emf);
            sc.setAttribute("emf", emf);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        sc.setAttribute("riversql_version", sc.getInitParameter("riversql_version"));
    }

    public static void setEntityManagerFactory(EntityManagerFactory emf) {
        ContextListener.emf = emf;
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }
}