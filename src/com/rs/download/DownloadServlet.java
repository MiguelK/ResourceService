package com.rs.download;

import com.rs.Resource;
import org.apache.commons.io.IOUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;

public class DownloadServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                throw new UnsupportedOperationException();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Resource resource = Resource.create(request.getParameter("fileName"));

        if(resource.isNonExisting() || resource.isInvalid()){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,"No resource with fileName="
                    + request.getParameter("fileName") + " exists");
            return;
        }

        String player = request.getParameter("player");
        if(player != null){

            RequestDispatcher requestDispatcher = request.getRequestDispatcher("/old-html5Player.jsp");
            request.setAttribute("fileName", resource.getFile().getName());
            requestDispatcher.forward(request, response);
            return;
        }


        FileInputStream fileInputStream = new FileInputStream(resource.getFile());
        try {
            response.setContentType(resource.getContentType());
            //response.setCharacterEncoding("UTF-8");//FIXME
            IOUtils.copy(fileInputStream, response.getOutputStream());
        } finally {
            IOUtils.closeQuietly(fileInputStream);
            response.getOutputStream().flush(); //FIXME
        }
    }

    void writeHTMLPlayer(HttpServletResponse response) {


    }
}
