package com.riversql.actions;

import com.riversql.actions.export.impl.CSVTableExporter;
import org.json.JSONArray;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CsvExport extends ExportPage {

    public void execute(HttpServletRequest request, HttpServletResponse response, EntityManager em, EntityTransaction et) throws Exception {
        loadUploadParameter(request);

        String meta_ = parameterMap.get("meta");
        String data_ = parameterMap.get("data");

        JSONArray meta = new JSONArray(meta_);
        JSONArray data = new JSONArray(data_);


        CSVTableExporter tableExporter = new CSVTableExporter(meta.length(), meta);

        for (int i = 0; i < data.length(); i++) {
            JSONArray row = data.getJSONArray(i);
            tableExporter.newLine();
            for (int j = 0; j < row.length(); j++) {
                tableExporter.newCell(row.get(j));
            }
        }
        tableExporter.finish();

        response.setHeader("Pragma", "public");
        response.setHeader("Expires", "0"); // set expiration time
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setContentType("application/x-msdownload");
        response.setHeader("Content-Disposition", "attachment;filename=export.csv");
        response.setContentLength(tableExporter.getContentSize());
        ServletOutputStream os = response.getOutputStream();
        tableExporter.copyTo(os);
        os.flush();
    }

}
