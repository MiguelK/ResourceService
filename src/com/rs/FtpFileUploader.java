package com.rs;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Created by miguelkrantz on 2018-04-20.
 */
public class FtpFileUploader {

    public static FtpFileUploader INSTANCE = new FtpFileUploader();

    private static final Logger LOG = Logger.getLogger(FtpFileUploader.class.getName());

    private final ExecutorService executorService;

    private FtpFileUploader() {
        executorService = Executors.newSingleThreadExecutor();
    }

    public void uploadToOneCom(String changeWorkingDirectory, File sourceFile, String originalName) {
        try {
            FileTask fileTask = new FileTask(changeWorkingDirectory, sourceFile, originalName);
            executorService.submit(fileTask);
        } catch (Exception ex) {
            LOG.info("Failed submit new task " + ex.getMessage());
        }
    }

    private class FileTask implements  Runnable {

        private final File sourceFile;
        private final String originalName;
        private final String changeWorkingDirectory;

        FileTask(String changeWorkingDirectory, File sourceFile, String originalName) {
            this.changeWorkingDirectory = changeWorkingDirectory;
            this.sourceFile = sourceFile;
            this.originalName = originalName;
        }

        String getOriginalName() {
            return originalName;
        }


        File getSourceFile() {
            return sourceFile;
        }

        @Override
        public void run() {

            String server = "ftp.pods.one";
            int port = 21;
            String user = "pods.one";
            String pass = "Kodar%123";

            FTPClient ftpClient = new FTPClient();
            InputStream inputStream = null;
            try {

                ftpClient.connect(server, port);
                ftpClient.login(user, pass);
                ftpClient.enterLocalPassiveMode();

                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

               // String pathname = "/sound-files/" + getLang() + "/";
                ftpClient.changeWorkingDirectory(changeWorkingDirectory);

                String name = getSourceFile().getName();
                String id = name.substring(0, name.lastIndexOf("."));
                String remoteFileName = id + getOriginalName();
                inputStream =  new FileInputStream(getSourceFile());

                System.out.println("Start uploading first file " + remoteFileName);
                boolean done = ftpClient.storeFile(remoteFileName, inputStream);

                if (done) {
                    LOG.info("Sound file uploaded successfully. " + remoteFileName);
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                try {
                    if(inputStream!=null){
                        inputStream.close();
                    }
                    if (ftpClient.isConnected()) {
                        ftpClient.logout();
                        ftpClient.disconnect();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
