/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.solenopsis.sforce.util;

import com.redhat.sforce.soap.metadata.*;
import com.redhat.solenopsis.app.Main;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author alex
 */
public class PackageUtils {
    
    
    public static com.redhat.sforce.soap.metadata.Package createFullPackage(MetadataPortType port, double apiVersion) {
        
        com.redhat.sforce.soap.metadata.Package fullPackage = new com.redhat.sforce.soap.metadata.Package();
        
        final DescribeMetadataResult describeMetadata = port.describeMetadata(apiVersion);
        final List<DescribeMetadataObject> metadataObjects = describeMetadata.getMetadataObjects();
        List<PackageTypeMembers> typeMembers = fullPackage.getTypes();
        for (DescribeMetadataObject metadataObject : metadataObjects) {
            PackageTypeMembers newMemberset = new PackageTypeMembers();
            String objectName = metadataObject.getXmlName();
            newMemberset.setName(objectName);
            //TODO: Figure out way to get members that can't do star notation.
            List<String> membersList = getMembersForType(objectName);
            newMemberset.getMembers().addAll(membersList);
            typeMembers.add(newMemberset);
        }
        
        fullPackage.getTypes().addAll(typeMembers);
        
        return fullPackage;
    }
    
    public static List<String> getMembersForType(String typeName) {
        List<String> membersForType = new ArrayList<String>();
        
        //TODO: Figure out how to get all members for a type
        membersForType.add("*");
        
        return membersForType;
    }
    
    public static String writeBytesToDisk(byte[] bytes, String outputFile) throws IOException {
        String createdFileName = "";
        InputStream is = new ByteArrayInputStream(bytes);
        File resultsFile = new File(outputFile);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(resultsFile);
            ReadableByteChannel src = Channels.newChannel(is);
            FileChannel dest = os.getChannel();
            copy(src, dest);
            createdFileName = resultsFile.getAbsolutePath();
            System.out.println("Results written to " +
                resultsFile.getAbsolutePath() + "\n");
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            os.close();
        }
        
        return createdFileName;
        
    }
    
    public static void copy(ReadableByteChannel src, WritableByteChannel dest){
        try {
            // use an in-memory byte buffer
            ByteBuffer buffer = ByteBuffer.allocate(8092);
            while (src.read(buffer) != -1) {
                buffer.flip();
                while(buffer.hasRemaining()) {
                    dest.write(buffer);
                }
                buffer.clear();
            }
        } catch (IOException ioe) {
        }
    }
    
    public static void unZipZipFileToLocation(File zipFile, File targetDir)
            throws IOException, Exception {
        if (!targetDir.isDirectory()) {
            throw new Exception("Target is not a directory.");
        }
        FileInputStream flInStr = new FileInputStream(zipFile);
        try {
            ZipInputStream zis = new ZipInputStream(flInStr);
            try {
                ZipEntry entry = null;
                while ((entry = zis.getNextEntry()) != null) {
                    String name = entry.getName();
                    File newFile = new File(targetDir, name);
                    if (entry.isDirectory() && !newFile.exists()) {
                        newFile.mkdirs();
                    } else if (!entry.isDirectory()) {
                        if (newFile.exists()) {
                            newFile.delete();
                        }
                        File parentDir = newFile.getParentFile();
                        if (!parentDir.exists()) {
                            parentDir.mkdirs();
                        }
                        FileOutputStream stmOut = new FileOutputStream(newFile);
                        try {
                            simpleInputStreamToOutputStream(zis, stmOut);
                        } finally {
                            stmOut.close();
                        }
                    }
                }//end while.
            } finally {
                zis.close();
            }
        } finally {
            flInStr.close();
        }
    }

    private static void simpleInputStreamToOutputStream(InputStream stmIn, OutputStream stmOut)
            throws IOException {

        int iBufferSize = 4096;
        byte[] buffer = new byte[iBufferSize];

        boolean bKeepStreaming = true;
        while (bKeepStreaming) {
            int iBytes = stmIn.read(buffer);
            if (iBytes == -1) {
                bKeepStreaming = false;
            } else {
                stmOut.write(buffer, 0, iBytes);
            }//end else some bytes returned.
        }//end while
    }
    
}
