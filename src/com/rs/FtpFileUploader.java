package com.rs;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    FtpFileUploader() {
        workQueue = new LinkedBlockingQueue<FileTask>(1000);
        executorService = Executors.newFixedThreadPool(1);
        executorService.submit(new Worker(workQueue));
    }

    public void uploadToOneCom(File sourceFile) {
        try {
            workQueue.put(new FileTask(sourceFile));
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            //   Thread.currentThread().interrupt();
        }
    }

    private static class FileTask {
        private final File sourceFile;

        FileTask(File sourceFile) {
            this.sourceFile = sourceFile;
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
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    FileTask file = workQueue.take();

                    uploadFile(file);
                   // login();

                    // Process item
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
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

            FTPClient ftpClient = new FTPClient();
            InputStream inputStream;
            try {

                ftpClient.connect(server, port);
                ftpClient.login(user, pass);
                ftpClient.enterLocalPassiveMode();

                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                // APPROACH #1: uploads first file using an InputStream
              //File firstLocalFile = new File("D:/Test/Projects.zip");

                String firstRemoteFile = sourceFile.getSourceFile().getName(); //"Projects.zip";
                inputStream =  new FileInputStream(sourceFile.getSourceFile());// new FileInputStream(sourceFile);

                System.out.println("Start uploading first file " + firstRemoteFile);
                boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
                inputStream.close();
                if (done) {
                    System.out.println("The first file is uploaded successfully.");
                }

                // APPROACH #2: uploads second file using an OutputStream
              /*  File secondLocalFile = new File("E:/Test/Report.doc");
                String secondRemoteFile = "test/Report.doc";
                inputStream = new FileInputStream(secondLocalFile);

                System.out.println("Start uploading second file");
                OutputStream outputStream = ftpClient.storeFileStream(secondRemoteFile);
                byte[] bytesIn = new byte[4096];
                int read = 0;

                while ((read = inputStream.read(bytesIn)) != -1) {
                    outputStream.write(bytesIn, 0, read);
                }
                inputStream.close();
                outputStream.close();*/

                boolean completed = ftpClient.completePendingCommand();
                if (completed) {
                    System.out.println("The second file is uploaded successfully.");
                }

            } catch (IOException ex) {
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
        }

        private void login() throws FtpException {

            String server = "www.yourserver.net";
            int port = 21;
            String user = "username";
            String pass = "password";
            FTPClient ftpClient = new FTPClient();
            try {
                ftpClient.connect(server, port);
                showServerReply(ftpClient);
                int replyCode = ftpClient.getReplyCode();
                if (!FTPReply.isPositiveCompletion(replyCode)) {
                    System.out.println("Operation failed. Server reply code: " + replyCode);
                    return;
                }
                boolean success = ftpClient.login(user, pass);
                showServerReply(ftpClient);
                if (!success) {
                   throw  new FtpException("Could not login to the server");
                } else {
                    System.out.println("LOGGED IN SERVER");
                }
            } catch (IOException ex) {
                throw  new FtpException("Oops! Something wrong happened " + ex.getMessage());
            }

         /*   FTPClient ftp = new FTPClient();
            ftp.enterLocalPassiveMode();
            FTPClientConfig config = new FTPClientConfig();
            ftp.setControlKeepAliveTimeout(300); // set timeout to 5 minutes

            // config.setXXX(YYY); // change required options
            // for example config.setServerTimeZoneId("Pacific/Pitcairn")
            ftp.configure(config );
            boolean error = false;
            try {
                int reply;
                String server = "ftp.example.com";
                ftp.connect(server);
                System.out.println("Connected to " + server + ".");
                System.out.print(ftp.getReplyString());

                // After connection attempt, you should check the reply code to verify
                // success.
                reply = ftp.getReplyCode();

                if(!FTPReply.isPositiveCompletion(reply)) {
                    ftp.disconnect();
                    System.err.println("FTP server refused connection.");
                    System.exit(1);
                }
//... // transfer files
                ftp.logout();
            } catch(IOException e) {
                error = true;
                e.printStackTrace();
            } finally {
                if(ftp.isConnected()) {
                    try {
                        ftp.disconnect();
                    } catch(IOException ioe) {
                        // do nothing
                    }
                }
                System.exit(error ? 1 : 0);
            }*/
        }
    }
}
