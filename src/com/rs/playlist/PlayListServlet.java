package com.rs.playlist;

import com.google.gson.Gson;
import com.rs.FtpFileUploader;
import com.rs.upload.UploadServlet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by miguelkrantz on 2021-06-04.
 */
public class PlayListServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(PlayListServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        String lang = request.getParameter("play_list_language");

        if(lang != null) {
            String jsonPayload = fetchFromServer(lang.toUpperCase());

            if(jsonPayload==null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to get directories from one.com " + lang);
                return;
            }
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(jsonPayload);
            out.flush();
        }
    }

    private String fetchFromServer(String lang) {

        String server = "ftp.pods.one";
        int port = 21;
        String user = "pods.one";
        String pass = "Kodar%123";

        FTPClient ftpClient = new FTPClient();
        try {

            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            String status = ftpClient.getStatus();
            LOG.info("fetchFromServer status=" + status);
        //ftpClient.enterLocalPassiveMode();
            //ftpClient.setFileType(FTP.);

            String workingDirectory = FtpFileUploader.PLAY_LISTS_DIRECTORY + "/" + lang;
            LOG.info("workingDirectory=" + workingDirectory);
            ftpClient.changeWorkingDirectory(workingDirectory);

            FTPFile[] files = ftpClient.listFiles();
            System.out.println(files.length);
            ArrayList<String> directoryNames = new ArrayList<>();
            for (FTPFile file : files) {
                if(file.getName().length() > 5){
                    directoryNames.add(file.getName());
                                   } 
            }

            return new Gson().toJson(directoryNames);

        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
