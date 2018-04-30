package com.rs;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.*;

/**
 * Created by miguelkrantz on 2018-04-30.
 */
public class FtpFileUploaderTest {



    @Test
    public void testUploadToOneCom() throws Exception {
        System.out.println("sdshdgshd");


        File source = new File("/Users/miguelkrantz/Documents/temp/Music1.m4a");
        FtpFileUploader.INSTANCE.uploadToOneCom(source);

        Thread.sleep(5000);
    }


}
