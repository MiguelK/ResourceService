package com.rs.upload;

import com.rs.FtpFileUploader;
import com.rs.Resource;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.rs.FtpFileUploader.PLAY_LIST_FILE_PREFIX;

public class UploadServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(UploadServlet.class.getName());


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);

        try {
            List<FileItem> fileItems = upload.parseRequest(request);

            for (FileItem fileItem : fileItems) {

                LOG.info("Received file " + fileItem.getName());

                if(fileItem.getName().startsWith("region-")){
                    Resource resource = Resource.createWritableRenameFile(fileItem.getName());
                    File file = resource.getFile();
                    fileItem.write(file);

                    response.setHeader("newFileName", file.getName());

                    String workingDirectory = "/sound-files/" + getLang(fileItem.getName()) + "/";
                    FtpFileUploader.INSTANCE.uploadToOneCom(null, workingDirectory, file, fileItem.getName());
                }

                if(fileItem.getName().startsWith(PLAY_LIST_FILE_PREFIX)){
                    Resource resource = Resource.createWritable(fileItem.getName());
                    File file = resource.getFile();
                    fileItem.write(file);

                    String languagePath =  request.getHeader("language") + "/";

                    String uniquePath = request.getHeader("uniquePath");
                    System.out.println("uniquePath=" + uniquePath);
                    String level1Directory = null;
                    if(uniquePath == null) {
                        level1Directory = "/" + FtpFileUploader.PLAY_LISTS_DIRECTORY + "/" + languagePath;
                        uniquePath = languagePath + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + "-" + Resource.createUniqueID();
                        response.setHeader("uniquePath", uniquePath);
                    }

                    String workingDirectory = "/" + FtpFileUploader.PLAY_LISTS_DIRECTORY + "/" +uniquePath + "/";
                    FtpFileUploader.INSTANCE.uploadToOneCom(level1Directory, workingDirectory, file, fileItem.getName());
                }
            }
        }catch (Exception e){
            LOG.log(Level.SEVERE, "Unable to upload file " + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private String getLang(String originalName) {
        if(originalName == null) {
            return "trash";
        }

        int startOffset = originalName.indexOf("-") + 1;
        int endOffset = originalName.indexOf("pid-");

        if(endOffset <0 || endOffset>= originalName.length()){
            return "trash";
        }

        return originalName.substring(startOffset, endOffset);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String sid = request.getParameter("sid");
        String action = request.getParameter("action");

        if("delete".equalsIgnoreCase(action) && sid != null) {

           File fileName = new File(Resource.CONTENT_HOME_LOCATOR.getContentHome(), sid);

            if(fileName.exists()){
                FileUtils.forceDelete(fileName);
            }
        }
    }
}
