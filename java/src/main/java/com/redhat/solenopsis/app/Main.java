package com.redhat.solenopsis.app;

import com.redhat.sforce.soap.metadata.*;
import com.redhat.solenopsis.credentials.Credentials;
import com.redhat.solenopsis.credentials.impl.PropertiesCredentials;
import com.redhat.solenopsis.properties.impl.FileMonitorPropertiesMgr;
import com.redhat.solenopsis.util.PackageXml;
import com.redhat.solenopsis.ws.LoginSvc;
import com.redhat.solenopsis.ws.MetadataSvc;
import com.redhat.solenopsis.ws.impl.DefaultEnterpriseSvc;
import com.redhat.solenopsis.ws.impl.DefaultMetadataSvc;
import com.redhat.solenopsis.ws.impl.DefaultPartnerSvc;
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

/**
 *
 * The purpose of this class is
 *
 * @author sfloess
 *
 */
public class Main {
    public static void emitMetadata(final String msg, final LoginSvc loginSvc, final double apiVersion) throws Exception {
        MetadataSvc metadataSvc = new DefaultMetadataSvc(loginSvc);
        
        final DescribeMetadataResult describeMetadata = metadataSvc.getPort().describeMetadata(apiVersion);
        
        final List<DescribeMetadataObject> metadataObjects = describeMetadata.getMetadataObjects();                
        
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
            
            System.out.println(PackageXml.computePackage(describeMetadata));
            
        }          

    }
    
    public static void fetchAllMetadata(LoginSvc loginSvc, double apiVersion) throws Exception {
        MetadataSvc metadataSvc = new DefaultMetadataSvc(loginSvc);
        
        MetadataPortType port = metadataSvc.getPort();
        
        //Setup request parameter.
        RetrieveRequest req = new RetrieveRequest();
        req.setApiVersion(apiVersion);
        com.redhat.sforce.soap.metadata.Package myPackage = new com.redhat.sforce.soap.metadata.Package();
        myPackage = createFullPackage(port, apiVersion);
        req.setUnpackaged(myPackage);
        
        //Send request to web service.
        AsyncResult asyncRetrieveResponse = port.retrieve(req);
        AsyncResult retrieveResponse = waitForResponse(asyncRetrieveResponse, port);
        
        //Ask web service for status of completed response.
        RetrieveResult retrieveStatusResponse = port.checkRetrieveStatus(retrieveResponse.getId());
        byte[] zipFileBytes = retrieveStatusResponse.getZipFile();
        
        String destFilename = "myZip.zip";
        writeBytesToDisk(zipFileBytes, destFilename);
        
    }
    
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
    
    public static AsyncResult waitForResponse(AsyncResult asyncRetrieveResponse, MetadataPortType port) throws Exception {
        
        final long ONE_SECOND = 1000;
        final int MAX_NUM_POLL_REQUESTS = 50;
        
        // Wait until the AsyncResult has returned with a value.
        int poll = 0;
        long waitTimeMilliSecs = ONE_SECOND;
        
        //Get response
        while (!asyncRetrieveResponse.isDone()) {
            try {
                // Exponential backoff
                Thread.sleep(waitTimeMilliSecs);
                waitTimeMilliSecs *= 2;
                if (poll++ > MAX_NUM_POLL_REQUESTS) {
                    throw new Exception("Request timed out.  If this is a large set "
                            + "of metadata components, check that the time allowed "
                            + "by MAX_NUM_POLL_REQUESTS is sufficient.");
                }

                // Log the Id of the last response
                List<String> responseIds = new ArrayList<String>();
                responseIds.add(asyncRetrieveResponse.getId());
                
                // We've waited, let's check the web service again for completion.
                asyncRetrieveResponse = (AsyncResult) port.checkStatus(responseIds).get(0);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return asyncRetrieveResponse;
        
    }
    
    public static void writeBytesToDisk(byte[] bytes, String outputFilename) throws IOException {
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        File resultsFile = new File(outputFilename);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(resultsFile);
            ReadableByteChannel src = Channels.newChannel(bais);
            FileChannel dest = os.getChannel();
            copy(src, dest);
            System.out.println("Results written to " +
                resultsFile.getAbsolutePath() + "\n");
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            os.close();
        }
        
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
    
        
    public static void main(final String[] args) throws Exception {
        //final String env = "prod.properties";
        //final String env = "test-dev.properties";
        final String env = "de-org.properties";
        
        Credentials credentials = new PropertiesCredentials(new FileMonitorPropertiesMgr(System.getProperty("user.home") + "/.solenopsis/credentials/" + env));
        
        double apiVersion = Double.parseDouble(credentials.getApiVersion());
        
        //emitMetadata("Enterprise WSDL", new DefaultEnterpriseSvc(credentials), apiVersion);
        //emitMetadata("Partner WSDL", new DefaultPartnerSvc(credentials), apiVersion);

        fetchAllMetadata(new DefaultEnterpriseSvc(credentials), apiVersion);
        
    }
}
