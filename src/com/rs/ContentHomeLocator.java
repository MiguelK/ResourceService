package com.rs;

import java.io.File;

public class ContentHomeLocator {

    private static final File LOCAL_DEV = new File(File.separator + "Users" + File.separator + "miguelkrantz" +
            File.separator + "Documents" + File.separator + "temp" + File.separator);

    private static final String OPENSHIFT_DATA_DIR = "OPENSHIFT_DATA_DIR";

    private static final String RESOURCE_HOME = "RESOURCE_HOME";

    private  File contentRootDir;


    public ContentHomeLocator() {
        locateOrCreate();
    }

   public File getContentHome() {
        return contentRootDir;
    }

    private void locateOrCreate(){
        if(LOCAL_DEV.exists() && LOCAL_DEV.isDirectory()){
            contentRootDir = new File(LOCAL_DEV, RESOURCE_HOME);
        } else {
            String openShiftDataDir = System.getenv(OPENSHIFT_DATA_DIR);
            contentRootDir = new File(openShiftDataDir, RESOURCE_HOME);
        }

        if (!contentRootDir.exists()) {
            contentRootDir.mkdir();
        }

        if (!contentRootDir.exists()) {
            throw new IllegalArgumentException("FileStreamingServlet init param 'basePath' value '"
                    + contentRootDir.getAbsolutePath() + "' does actually not exist in file system.");
        } else if (!contentRootDir.isDirectory()) {
            throw new IllegalArgumentException("FileStreamingServlet init param 'basePath' value '"
                    + contentRootDir.getAbsolutePath() + "' is actually not a directory in file system.");
        } else if (!contentRootDir.canRead()) {
            throw new IllegalArgumentException("FileStreamingServlet init param 'basePath' value '"
                    + contentRootDir.getAbsolutePath() + "' is actually not readable in file system.");
        }
    }
}
