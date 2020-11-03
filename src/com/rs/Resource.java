package com.rs;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class Resource {

    private static final Resource INVALID_RESOURCE = new Resource(null,false,false, null);
    public static final ContentHomeLocator CONTENT_HOME_LOCATOR;
    private static final IdGenerator ID_GENERATOR;

    static {
        CONTENT_HOME_LOCATOR = new ContentHomeLocator();
        ID_GENERATOR = new IdGenerator(CONTENT_HOME_LOCATOR.getContentHome());
    }

    private final boolean exists;

    private final boolean valid;

    private final File file;

    private final String contentType;

    private Resource(File file, boolean exists, boolean valid, String contentType) {
        this.file = file;
        this.exists = exists;
        this.valid = valid;
        this.contentType = contentType;
    }


    public String getContentType() {
        return contentType;
    }

    public static Resource createWritable(String fileName) {

        ContentType contentType = ContentType.parse(fileName);

        if(contentType ==null){
            return INVALID_RESOURCE;
        }

        String newName = ID_GENERATOR.getId() + "." + contentType.getExtension();

        return create(newName);
    }

    public static Resource create(String fileName) {
        ContentType contentType = ContentType.parse(fileName);

        if(contentType ==null){
            return INVALID_RESOURCE;
        }

        File file = new File(CONTENT_HOME_LOCATOR.getContentHome(), fileName.trim());

        boolean existingFile = file.isFile() && file.exists() && file.canRead();

        boolean valid = file.isFile();

        return new Resource(file, existingFile,valid, contentType.getContentType());
    }

    public File getFile() {
        return file;
    }

    public boolean isNonExisting(){
        return !exists;
    }

    public boolean isInvalid(){
        return !valid;
    }

    private enum ContentType {

        m4a("audio/mp4","m4a"), //audio/mp4
        m4v("video/m4v","m4v"),
        mp4("video/mp4","mp4"),
        mov("video/quicktime","mov"),

        jpg("image/jpeg","jpg"),
        png("image/png","png"),
        json("application/json","json");

        private final String contentType;
        private final String extension;

        ContentType(String contentType, String extension) {
            this.contentType = contentType;
            this.extension = extension;
        }

        public String getExtension() {
            return extension;
        }

        public String getContentType() {
            return contentType;
        }

        public static ContentType parse(String fileName){

            String fileNameTrimmed = StringUtils.trimToNull(fileName);

            if(fileNameTrimmed == null){
                return null;
            }

            int i = fileNameTrimmed.lastIndexOf(".");
            if(i== -1 || i + 1 > fileNameTrimmed.length() ) {
                return null;
            }

            String extension = fileNameTrimmed.substring(i + 1, fileNameTrimmed.length());


            for (ContentType contentType : values()) {
                if(contentType.getExtension().equalsIgnoreCase(extension)){
                    return contentType;
                }
            }

            return null;
        }
    }
}
