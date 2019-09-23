package com.vectormax.mobile.launcher;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

public class Utils {


//    public static final int INSTALL_REPLACE_EXISTING = 0x00000002;
//    public static final int INSTALL_SUCCEEDED = 1;
//
//    private static Method installPackageMethod;
//    private static Method deletePackageMethod;
//
//    static {
//        try {
//            installPackageMethod = PackageManager.class.getMethod("installPackage", Uri.class, IPackageInstallObserver.class, Integer.TYPE, String.class);
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void installPackage(PackageManager pm, Uri mPackageUri, IPackageInstallObserver observer, int installFlags, String installerPackageName) {
//        try {
//            installPackageMethod.invoke(pm, mPackageUri, observer, installFlags, installerPackageName);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    public static void InstallAPK(String filename){
        File file = new File(filename);
        if(file.exists()){
            try {
                String command;
                //filename = StringUtils.insertEscape(filename);
                command = "adb install -r " + filename;
                Process proc = Runtime.getRuntime().exec(new String[] { "su", "-c", command });
                proc.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void installApk(File file) {
        //File file = new File(filename);
        if(file.exists()){
            try {
                //final String command = "pm install -r " + file.getAbsolutePath();
                final String command = "adb install -r " + file.getAbsolutePath();
                Process proc = Runtime.getRuntime().exec(new String[] { "su", "-c", command });
                proc.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getSkyworthDeviceId() {
        String r = null;
        try{
            BufferedReader localBufferedReader = new BufferedReader(new FileReader("/sys/class/mipt_hwconfig/deviceid"));
            char[] buffer = new char[1024];
            int i = localBufferedReader.read(buffer);
            localBufferedReader.close();
            r = String.valueOf(buffer, 0, i);
        } catch (IOException localIOException){
            localIOException.printStackTrace();
        }
        return r;
    }


    public static void downloadFile(String url, File outputFile) {
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            int contentLength = conn.getContentLength();

            DataInputStream stream = new DataInputStream(u.openStream());

            byte[] buffer = new byte[contentLength];
            stream.readFully(buffer);
            stream.close();

            DataOutputStream fos = new DataOutputStream(new FileOutputStream(outputFile));
            fos.write(buffer);
            fos.flush();
            fos.close();
        } catch(FileNotFoundException e) {
            return; // swallow a 404
        } catch (Exception e) {
            return; // swallow a 404
        }
    }
}
