package com.rs;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by miguelkrantz on 2018-04-20.
 */
public class FtpFileUploader {

    public static FtpFileUploader INSTANCE = new FtpFileUploader();

    private final ExecutorService executorService;

    private FtpFileUploader() {
        executorService = Executors.newSingleThreadExecutor();

    }

    public void uploadToOneCom(File sourceFile, String originalName) {
        try {
            FileTask fileTask = new FileTask(sourceFile, originalName);

            executorService.submit(fileTask);
            //System.out.println("Added new task to queue " + workQueue.size());
        } catch (Exception ex) {
            ex.printStackTrace();
            //   Thread.currentThread().interrupt();
        }
    }

    private class FileTask implements  Runnable {

        private final File sourceFile;
        private final String originalName;

        FileTask(File sourceFile, String originalName) {
            this.sourceFile = sourceFile;
            this.originalName = originalName;
        }

        public String getOriginalName() {
            return originalName;
        }

        String getLang() {

            //FileName=region-SEpid-1196194685eid--33a6eb58-211d4f63.m4a
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

                //sound-files/swe/
                String pathname = "/sound-files/" + getLang() + "/";
                ftpClient.changeWorkingDirectory(pathname); //FIXME

                String name = getSourceFile().getName();
                String id = name.substring(0, name.lastIndexOf("."));
                String remoteFileName = id + getOriginalName(); //sourceFile.getSourceFile().getName();
                inputStream =  new FileInputStream(getSourceFile());

                System.out.println("Start uploading first file " + remoteFileName);
                boolean done = ftpClient.storeFile(remoteFileName, inputStream);


                inputStream.close();
                if (done) {
                    System.out.println("The first file is uploaded successfully.");
                }

               /* boolean completed = ftpClient.completePendingCommand();
                if (completed) {
                    System.out.println("The second file is uploaded successfully.");
                }*/

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

                //    ftpClient.completePendingCommand();
                    //    ftpClient.disconnect();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
