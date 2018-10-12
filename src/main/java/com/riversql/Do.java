package com.riversql;

import com.riversql.actions.*;
import org.apache.commons.beanutils.BeanUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@SuppressWarnings("serial")
public class Do extends DoServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(DoServlet.class);

    Map<String, Class<? extends JSONAction>> jsonActionMap;

    Map<String, Class<? extends IPageAction>> pageActionMap;


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        HashMap<String, Class<? extends JSONAction>> tmp = new HashMap<String, Class<? extends JSONAction>>();
        tmp.put("generateDDLAlterTable", GenerateDDLAlterTable.class);
        tmp.put("generateDDLCreateTable", GenerateDDLCreateTable.class);
        tmp.put("generateDDLCreateIndex", GenerateDDLCreateIndex.class);
        tmp.put("getTableColumns", GetTableColumns.class);
        tmp.put("getTableIndexesAlterTable", GetTableIndexesForAlterTable.class);
        tmp.put("getTableColumnsAlterTable", GetTableColumnsForAlterTable.class);
        tmp.put("getPKAlterTable", GetPKForAlterTable.class);
        tmp.put("getTypesAlterTable", GetTypesAlterTable.class);
        tmp.put("getMenu", GetMenu.class);
        tmp.put("pluginAction", PluginAction.class);
        tmp.put("getPK", GetPK.class);
        tmp.put("getFK", GetFK.class);
        tmp.put("getMeta", GetMeta.class);
        tmp.put("getExportedKeys", GetExportedKeys.class);
        tmp.put("getAdditionalData", GetAdditionalData.class);
        tmp.put("redoQuery", RedoQuery.class);
        tmp.put("changeCatalog", ChangeCatalog.class);
        tmp.put("getColumnsForViewer", GetColumnsForViewer.class);
        tmp.put("getSources", GetSources.class);
        tmp.put("getIndexes", GetIndexes.class);
        tmp.put("getDatabaseMetadata", GetDatabaseMetadata.class);
        tmp.put("getConnectionStatus", GetConnectionStatus.class);
        tmp.put("getGrants", GetGrants.class);
        tmp.put("closeResultSet", CloseResultSet.class);
        tmp.put("commitConnection", CommitConnection.class);
        tmp.put("closeConnection", CloseConnection.class);
        tmp.put("rollbackConnection", RollbackConnection.class);
        tmp.put("getDrivers", GetDrivers.class);
        tmp.put("getDetails", GetDetails.class);

        tmp.put("createDriver", CreateDriver.class);
        tmp.put("deleteDriver", DeleteDriver.class);
        tmp.put("updateDriver", UpdateDriver.class);
        tmp.put("preview", Preview.class);
        tmp.put("ping", Ping.class);

        tmp.put("updateSource", UpdateSource.class);

        tmp.put("createSource", CreateSource.class);


        tmp.put("deleteSource", DeleteSource.class);


        tmp.put("getTree", GetTree.class);
        tmp.put("getDatabases", GetDatabases.class);

        tmp.put("testSourceConnection", TestSourceConnection.class);
        tmp.put("connect", Connect.class);

        tmp.put("execute", ExecuteSQL.class);
        tmp.put("export", Export.class);
        tmp.put("import", Import.class);
        jsonActionMap = Collections.unmodifiableMap(tmp);

        HashMap<String, Class<? extends IPageAction>> tmp2 = new HashMap<String, Class<? extends IPageAction>>();


        //tmp2.put("selectTable",SelectTable.class);

        tmp2.put("about", AboutPage.class);
        tmp2.put("config", ConfigPage.class);

        tmp2.put("sourcesPage", SourcesPage.class);
        tmp2.put("exportTablePage", ExportTablePage.class);
        tmp2.put("doExport", DoExport.class);

        tmp2.put("excelExport", ExcelExport.class);
        tmp2.put("pdfExport", PdfExport.class);
        tmp2.put("csvExport", CsvExport.class);

        pageActionMap = Collections.unmodifiableMap(tmp2);

    }

    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp, EntityManager em, EntityTransaction et) throws Exception {
        String action = req.getParameter("action");
        Class<? extends JSONAction> iactionclass = jsonActionMap.get(action);
        if (iactionclass != null) {
            JSONObject obj = new JSONObject();
            PrintWriter writer = resp.getWriter();
            try {
                JSONAction iaction = iactionclass.newInstance();
                BeanUtils.populate(iaction, req.getParameterMap());

                et = em.getTransaction();
                et.begin();
                JSONObject objsr = iaction.execute(req, resp, em, et);
                if (et.isActive())
                    et.commit();
                resp.setHeader("Content-Type", "text/html;charset=ISO-8859-1");
                obj.put("success", true);
                if (objsr != null)
                    obj.put("result", objsr);
            } catch (Exception e) {
                LOGGER.error("While handling action '" + action + "'", e);
                if (et != null && et.isActive())
                    et.rollback();
                try {

                    obj.put("success", false);
                    obj.put("error", e.toString());
                } catch (JSONException e1) {
                    LOGGER.error("JSON Error", e1);
                }

            } finally {
                IDManager.set(null);
                if (em != null)
                    em.close();
            }
            writer.write(obj.toString());
        } else {
            Class<? extends IPageAction> iPageActionclass = pageActionMap.get(action);
            if (iPageActionclass != null) {
                try {
                    IPageAction iPageAction = iPageActionclass.newInstance();
                    BeanUtils.populate(iPageAction, req.getParameterMap());
                    et = em.getTransaction();
                    et.begin();
                    iPageAction.execute(req, resp, em, et);
                    et.commit();
                } catch (Exception e) {
                    LOGGER.error("While handling page action", e);
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
                        LOGGER.error("", e1);
                    }
                } finally {
                    IDManager.set(null);
                    if (em != null)
                        em.close();
                }
            } else {
                if (em != null)
                    em.close();

                throw new IllegalArgumentException("No such action : " + action);

            }


        }
    }
}
