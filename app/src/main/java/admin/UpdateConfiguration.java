/**************************************************
 * Android Web Server
 * Based on JavaLittleWebServer (2008)
 * <p/>
 * Copyright (c) Piotr Polak 2008-2017
 **************************************************/

package admin;

import java.io.File;

import ro.polak.http.ServerConfig;
import ro.polak.http.servlet.FileUpload;
import ro.polak.http.servlet.HttpRequest;
import ro.polak.http.servlet.HttpResponse;
import ro.polak.http.servlet.Servlet;
import ro.polak.utilities.Utilities;

public class UpdateConfiguration extends Servlet {

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        ServerConfig serverConfig = (ServerConfig) getServletContext().getAttribute(ServerConfig.class.getName());
        AccessControl ac = new AccessControl(serverConfig, request.getSession());
        if (!ac.isLogged()) {
            response.sendRedirect("/admin/Login.dhtml?relocate=" + request.getRequestURI());
            return;
        }

        String message = handleFileUpload(request);

        HTMLDocument doc = renderDocument(message);
        response.getPrintWriter().print(doc.toString());
    }

    private String handleFileUpload(HttpRequest request) {
        FileUpload fileUpload = request.getFileUpload();
        String message;

        if (fileUpload.get("file") == null) {
            message = "Error: no file uploaded (" + fileUpload.size() + ")";
        } else {
            String basePath = ((ServerConfig) getServletContext().getAttribute(ServerConfig.class.getName())).getBasePath();

            if (Utilities.getExtension(fileUpload.get("file").getFileName()).equals("conf")) {

                File file = fileUpload.get("file").getFile();
                File dest = new File(basePath + "httpd_test.conf");
                if (file.renameTo(dest)) {
                    (new File(basePath + "bakup_httpd.conf")).delete();
                    (new File(basePath + "httpd.conf")).renameTo(new File(basePath + "bakup_httpd.conf"));
                    if (dest.renameTo((new File(basePath + "httpd.conf")))) {
                        message = "New configuration will be applied after server restart.";
                    } else {
                        message = "Unable to apply new configuration file.";
                    }

                } else {
                    message = "Unable to move file.";
                }
            } else {
                message = "Uploaded file <b>" + fileUpload.get("file").getFileName() + "</b> does not appear to be a valid configuration file. <a href=\"/admin/Management.dhtml?task=updateConfiguration\">Back</a>";
            }
        }
        return message;
    }

    private HTMLDocument renderDocument(String message) {
        HTMLDocument doc = new HTMLDocument("Update configuration");
        doc.setOwnerClass(getClass().getSimpleName());

        doc.writeln("<div class=\"page-header\"><h1>Update configuration</h1></div>");
        if (message != null) {
            doc.writeln("<p>" + message + "</p>");
        }

        return doc;
    }
}
