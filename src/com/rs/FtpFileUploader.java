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

    public static final String PLAY_LIST_FILE_PREFIX = "play-list";
    public static FtpFileUploader INSTANCE = new FtpFileUploader();

    public static final String PLAY_LISTS_DIRECTORY = "play-lists";
    private static final Logger LOG = Logger.getLogger(FtpFileUploader.class.getName());

    private final ExecutorService executorService;

    private FtpFileUploader() {
        executorService = Executors.newSingleThreadExecutor();
    }

    public void uploadToOneCom(String level1Directory, String workingDirectory, File sourceFile, String originalName) {
        try {
            FileTask fileTask = new FileTask(level1Directory, workingDirectory, sourceFile, originalName);
            executorService.submit(fileTask);
        } catch (Exception ex) {
            LOG.info("Failed submit new task " + ex.getMessage());
        }
    }

    private class FileTask implements  Runnable {

        private final File sourceFile;
        private final String originalName;
        private final String workingDirectory;
        private final String level1Directory;

        FileTask(String level1Directory, String workingDirectory, File sourceFile, String originalName) {
            this.level1Directory =level1Directory;
            this.workingDirectory = workingDirectory;
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



                String name = getSourceFile().getName();
                String id = name.substring(0, name.lastIndexOf("."));
                String remoteFileName = id + getOriginalName();

                if(name.endsWith(".html") || name.endsWith(".txt")) {
                    ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
                } else {
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                }


                if(name.contains(PLAY_LIST_FILE_PREFIX)) {
                    remoteFileName = name;
                }

                if(level1Directory != null) {
                    ftpClient.makeDirectory(level1Directory);
                }

                ftpClient.makeDirectory(workingDirectory);
                ftpClient.changeWorkingDirectory(workingDirectory);

                inputStream =  new FileInputStream(getSourceFile());

                System.out.println("Start uploading first file " + remoteFileName + ", to directory=" +
                        workingDirectory);
                boolean done = ftpClient.storeFile(remoteFileName, inputStream);

                if (done) {
                    LOG.info("File uploaded successfully. " + remoteFileName + " to one.com");
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
