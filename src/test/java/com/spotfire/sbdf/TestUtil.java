/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.sbdf;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Test utilities for SBDF.
 */
public class TestUtil {
    /**
     * Returns a File for the bootstrap file with the given file name. Will first attempt to find the resource on the
     * classpath then and, if that fails, by looking in the {@code /WEB-INF} folder.
     *
     * @param fileName the file name
     * @return a File
     */
    public static File getFile(String fileName) {
        URL url = null;
        try {
            final ClassLoader cl = TestUtil.class.getClassLoader();
            url = cl.getResource(fileName);
            if (url == null) {
                throw new IllegalStateException("Invalid test setup, cannot find the file '" + fileName + "'");
            }
            final URI uri = url.toURI();
            if ("jar".equals(uri.getScheme())) {
                final File file = File.createTempFile(fileName, ".xml");
                try (InputStream is = TestUtil.getFileAsStream(fileName);
                     FileOutputStream os = new FileOutputStream(file)) {
                    pump(is, os);
                }
                return file;
            }

            return new File(uri);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid URL: " + url + " (file name: " + fileName + ")", e);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + url + " (file name: " + fileName + ")", e);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Error copying resource to file: " + e.getMessage() + " (file name: " + fileName + ")", e);
        }
    }

    /**
     * Returns an InputStream for the bootstrap file with the given file name. Will first attempt to find the resource on the
     * classpath then and, if that fails, by looking in the {@code /WEB-INF} folder.
     *
     * @param fileName the file name
     * @return an InputStream
     */
    public static InputStream getFileAsStream(String fileName) {
        final ClassLoader cl = TestUtil.class.getClassLoader();
        final InputStream is = cl.getResourceAsStream(fileName);
        if (is == null) {
            throw new IllegalStateException("Invalid test setup, cannot find the file '" + fileName + "'");
        }

        return is;
    }

    /**
     * Pumps data from the input stream to the output stream until end of file is reached on the input stream.
     * @param is the input stream to pump data from
     * @param os the output stream to pump data to
     * @throws IOException if an error occurs.
     */
    public static void pump(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[32*1024];
        int len = 0;
        while ((len = is.read(buf)) != -1) {
            os.write(buf, 0, len);
        }
    }
}
