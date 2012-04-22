package com.redhat.solenopsis.app;

import com.redhat.solenopsis.sforce.util.AsyncUtils;
import com.redhat.solenopsis.sforce.util.PackageUtils;
import com.redhat.solenopsis.sforce.localfs.MetadataDirectory;
import com.redhat.solenopsis.sforce.Type;
import com.redhat.sforce.soap.metadata.*;
import com.redhat.solenopsis.ws.LoginSvc;
import com.redhat.solenopsis.ws.MetadataSvc;
import com.redhat.solenopsis.ws.impl.DefaultMetadataSvc;
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
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author alex
 */
public class SfCodeSource {
    
    //For pulling metadata
    private File workingDir;
    
    //For Salesforce org as source
    private LoginSvc login;
    private double apiVersion;
    
    //For filesystem directory as source
    private MetadataDirectory metadataDir;
    
    //Actual Salesforce code, in comparable form
    private List<Type> types;
    private List<DescribeMetadataObject> metadataObjects;
    
    
    //==========================================
    //     CONSTRUCTORS
    //==========================================
    
    // Salesforce org as source
    public SfCodeSource(String workingDir, LoginSvc loginSvc) {
        this(workingDir, loginSvc, Double.valueOf(loginSvc.getCredentials().getApiVersion()));
    }
    public SfCodeSource(String workingDir, LoginSvc loginSvc, double apiVersion) {
        this.workingDir = new File(workingDir);
        this.login = loginSvc;
        this.apiVersion = apiVersion;
    }
    
    // Filesystem directory as source
    public SfCodeSource(MetadataDirectory metadataDir) {
        this.metadataDir = metadataDir;
    }
    
    //==========================================
    //     GETTERS SETTERS
    //==========================================
    public File getWorkingDir() {
        return this.workingDir;
    }
    public List<Type> getTypes() {
        if (this.types == null) {
            this.types = findTypes(this.workingDir);
        }
        return this.types;
    }
    
    public void buildTypesPropFile() throws Exception {
        
        //Get the org's metadata from its web service.
        MetadataSvc metadataSvc = new DefaultMetadataSvc(this.login);
        final DescribeMetadataResult describeMetadata = metadataSvc.getPort().describeMetadata(this.apiVersion);
        final List<DescribeMetadataObject> metaDescribe = describeMetadata.getMetadataObjects();                
        
        //Save the metadata description to state. We can serialize to disk later.
        this.metadataObjects = metaDescribe;
        
        //Build the prop file.
        for (final DescribeMetadataObject dmo : metadataObjects) {
        
            System.out.println("==============================================");
            
            System.out.println("Dir:       " + dmo.getDirectoryName());
            System.out.println("Suffix:    " + dmo.getSuffix());
            System.out.println("XML:       " + dmo.getXmlName());
            System.out.println("In folder: " + dmo.isInFolder());
            System.out.println("Meta file: " + dmo.isMetaFile());
            System.out.println("Children:");
            for (final String child : dmo.getChildXmlNames()) {
                System.out.println("          " + child);
            }
            
            final ListMetadataQuery query = new ListMetadataQuery();
            query.setType(dmo.getXmlName());
            if (null != dmo.getDirectoryName() || ! "".equals(dmo.getDirectoryName())) {
                query.setFolder(dmo.getDirectoryName());
            }

            final List<ListMetadataQuery> metaDataQuertyList = new ArrayList<ListMetadataQuery>();  
            
            metaDataQuertyList.add(query);
        
            System.out.println();

            final List<FileProperties> filePropertiesList = metadataSvc.getPort().listMetadata(metaDataQuertyList, 24);
            for (final FileProperties fileProperties : filePropertiesList) {
                System.out.println ("Full name:      " + fileProperties.getFullName());
                System.out.println ("     file name: " + fileProperties.getFileName());
                System.out.println ("     type:      " + fileProperties.getType());
            }
        
            System.out.println("\n\n");
        }
    }
    
    public static List<Type> findTypes(File workingDir) {
        List<Type> foundTypes = new ArrayList<Type>();
        
        //Loop over all files in the working directory to find all types.
        File[] fileToSearch = new File[500];
        fileToSearch[0] = workingDir;
        List<File> metadataFiles = findAllFilesInDir(fileToSearch, new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                //This may not be necessary...
                return (!name.contains("-meta"));
            }
        });
        
        for (File metadataFile : metadataFiles) {
            
        }
        
        return foundTypes;
    }
    
    public static List<File> findAllFilesInDir(File[] files, FilenameFilter filter) {
        List<File> currentDirFiles = new ArrayList<File>();
        for (File file : files) {
            if (file.isDirectory()) {
                currentDirFiles.addAll(findAllFilesInDir(file.listFiles(filter), filter));
            } else {
                System.out.println("File: " + file.getName());
                currentDirFiles.add(file);
            }
        }
        return currentDirFiles;
    }
    
    
    //==========================================
    //     OPERATIONS
    //==========================================
    public void fetchAllMetadata() throws Exception {
        this.fetchAllMetadata(this.workingDir.getPath());
    }
    
    //The optWorkingDir parameter is optional, and overrides the specified 
    //  workingDir when creating the SfCodeSource object.
    public void fetchAllMetadata(String optWorkingDir) throws Exception {
        
        //Decide on a destination directory for fetched metadata.
        String destDirPath = null;
        if (optWorkingDir != null) {
            destDirPath = optWorkingDir;
        }
        else if (this.workingDir != null) {
            destDirPath = this.workingDir.getPath();
        }
        else {
            //No directory specified. Find an intelligent default.
            destDirPath = ClassLoader.getSystemClassLoader().getResource(".").getPath();
            //destDirPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        }
        
        //Setup the web service
        File destDir = new File(destDirPath);
        MetadataSvc metadataSvc = new DefaultMetadataSvc(this.login);
        MetadataPortType port = metadataSvc.getPort();
        
        //Setup request parameter.
        RetrieveRequest req = new RetrieveRequest();
        req.setApiVersion(apiVersion);
        com.redhat.sforce.soap.metadata.Package myPackage 
                = PackageUtils.createFullPackage(port, apiVersion);
        req.setUnpackaged(myPackage);
        
        //Send request to web service.
        AsyncResult asyncRetrieveResponse = port.retrieve(req);
        AsyncResult retrieveResponse = AsyncUtils.waitForResponse(asyncRetrieveResponse, port);
        
        //Get response once web service says it's done.
        RetrieveResult retrieveStatusResponse = port.checkRetrieveStatus(retrieveResponse.getId());
        
        //The response should have a zip file.
        //Write the zip to disk.
        byte[] zipFileBytes = retrieveStatusResponse.getZipFile();
        String destFilename = "metadata.zip";
        String destFilePath = destDir.getPath().concat("/" + destFilename);
        String createdFileName = PackageUtils.writeBytesToDisk(zipFileBytes, destFilePath);
        
        //Also, unzip the file.
        File createdFile = new File(createdFileName);
        File parentDir = createdFile.getParentFile();
        PackageUtils.unZipZipFileToLocation(createdFile, parentDir);
    }
    
    
    public void compareLocalSourceTo(SfCodeSource otherCodeSource) {
        
//        File thisSourceWorkingDir = this.getWorkingDir();
//        List<SfMetadataType> thisTypeList = this.getTypeList();
//        File otherSourceWorkingDir = otherCodeSource.getWorkingDir();
//        
//        //First, compare files for missing files.
//        
//        for () {
//            
//        }
//        
//        //Then, compare file contents for missing members.
        
    }
    
    
    public void pushAllMetadata() throws Exception {
        
    }
    
}
