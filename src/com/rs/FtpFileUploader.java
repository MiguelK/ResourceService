package com.rs;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by miguelkrantz on 2018-04-20.
 */
public class FtpFileUploader {

    public static FtpFileUploader INSTANCE = new FtpFileUploader();

    private static class FtpException extends RuntimeException {
        FtpException(String message) {
            super(message);
        }
    }
    private final ExecutorService executorService;


    private final BlockingQueue<FileTask> workQueue;

    private FtpFileUploader() {
        workQueue = new ArrayBlockingQueue<FileTask>(1000);
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Worker(workQueue));

    }

    public void uploadToOneCom(File sourceFile, String originalName) {
        try {
            workQueue.put(new FileTask(sourceFile, originalName));
            System.out.println("Added new task to queue " + workQueue.size());
        } catch (Exception ex) {
            ex.printStackTrace();
            //   Thread.currentThread().interrupt();
        }
    }

    private static class FileTask {
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
    }

    private static class Worker implements Runnable {
        private final BlockingQueue<FileTask> workQueue;

        Worker(BlockingQueue<FileTask> workQueue) {
            this.workQueue = workQueue;
        }

        @Override
        public void run() {

           /* FileTask task;
            while((task = workQueue.take()) != null){
                Thread.sleep(10);

                    uploadFile(task);

               // System.out.println("Consumed "+msg.getMsg());
            }*/

            while (true) { //!Thread.currentThread().isInterrupted()) {
                try{
                    System.out.println("Waiting...");
                    FileTask file = workQueue.take();

                    System.out.println(workQueue.size() + " Processing " + file.originalName);

                    uploadFile(file);

                    Thread.sleep(1000);

                }catch(Exception e) {
                    e.printStackTrace();
                }
            }

        }

        private static void showServerReply(FTPClient ftpClient) {
            String[] replies = ftpClient.getReplyStrings();
            if (replies != null && replies.length > 0) {
                for (String aReply : replies) {
                    System.out.println("SERVER: " + aReply);
                }
            }
        }


        void uploadFile(FileTask sourceFile) {
            String server = "ftp.pods.one";
            int port = 21;
            String user = "pods.one";
            String pass = "Kodar%123";

            //FileName=region-SEpid-1196194685eid--33a6eb58-211d4f63.m4a


            FTPClient ftpClient = new FTPClient();
            InputStream inputStream = null;
            try {

                ftpClient.connect(server, port);
                ftpClient.login(user, pass);
                ftpClient.enterLocalPassiveMode();

                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                //sound-files/swe/
                String pathname = "/sound-files/" + sourceFile.getLang() + "/";
                ftpClient.changeWorkingDirectory(pathname); //FIXME

                String name = sourceFile.getSourceFile().getName();
                String id = name.substring(0, name.lastIndexOf("."));
                String remoteFileName = id + sourceFile.getOriginalName(); //sourceFile.getSourceFile().getName();
                inputStream =  new FileInputStream(sourceFile.getSourceFile());

                System.out.println("Start uploading first file " + remoteFileName);
                boolean done = ftpClient.storeFile(remoteFileName, inputStream);


                inputStream.close();
                if (done) {
                    System.out.println("The first file is uploaded successfully.");
                }

                boolean completed = ftpClient.completePendingCommand();
                if (completed) {
                    System.out.println("The second file is uploaded successfully.");
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
