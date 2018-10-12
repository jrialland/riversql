package com.riversql;

import org.apache.commons.fileupload.FileItem;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface IFileUploadAction {
    public void execute(Map<String, FileItem> fileMap, Map<String, String> parameterMap, HttpServletResponse response, EntityManager em, EntityTransaction et) throws Exception;
}
