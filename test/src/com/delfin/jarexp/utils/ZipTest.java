package com.delfin.jarexp.utils;

import java.util.List;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.junit.Assert;
import org.junit.Test;

import com.delfin.jarexp.utils.Zip.BypassAction;
import com.delfin.jarexp.utils.Zip.BypassRecursivelyAction;

public class ZipTest {

    @Test
    public void bypass() {

        File zip = new File("test/resources/search", "test_3.zip");

        final List<String> actual = new ArrayList<String>();

        Zip.bypass(zip, new BypassAction() {

            @Override
            public void process(ZipFile zipFile, ZipEntry zipEntry) throws IOException {

                if (Zip.isArchive(zipEntry.getName())) {
                    InputStream stream = zipFile.getInputStream(zipEntry);
                    ZipInputStream zipInputStream = new ZipInputStream(stream);
                    Zip.bypass(zipInputStream, zipEntry.getName() + "!", new BypassRecursivelyAction() {
                        @Override
                        public void process(ZipEntry zipEntry, String fullPath) throws IOException {
                            actual.add(fullPath + zipEntry.getName());
                        }

                        @Override
                        public void error(ZipEntry zipEntry, Exception error) {
                            Assert.fail();
                        }
                    });
                } else {
                    actual.add(zipEntry.getName());
                }

            }

        });
        
        System.out.println("$$ " + actual);

        List<String> expected = new ArrayList<String>();
        expected.add("test_2.txt");
        expected.add("test_1.txt");
        expected.add("test_0.txt");

        assertEquals(expected, actual);

    }

}
