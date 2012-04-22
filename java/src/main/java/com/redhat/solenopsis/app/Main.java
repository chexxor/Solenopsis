package com.redhat.solenopsis.app;

import com.redhat.sforce.soap.metadata.DescribeMetadataObject;
import com.redhat.sforce.soap.metadata.DescribeMetadataResult;
import com.redhat.sforce.soap.metadata.FileProperties;
import com.redhat.sforce.soap.metadata.ListMetadataQuery;
import com.redhat.solenopsis.credentials.Credentials;
import com.redhat.solenopsis.credentials.impl.PropertiesCredentials;
import com.redhat.solenopsis.properties.impl.FileMonitorPropertiesMgr;
import com.redhat.solenopsis.sforce.Member;
import com.redhat.solenopsis.sforce.Metadata;
import com.redhat.solenopsis.sforce.Type;
import com.redhat.solenopsis.sforce.localfs.MetadataDirectory;
import com.redhat.solenopsis.sforce.org.MetadataApi;
import com.redhat.solenopsis.ws.LoginSvc;
import com.redhat.solenopsis.ws.MetadataSvc;
import com.redhat.solenopsis.ws.impl.DefaultEnterpriseSvc;
import com.redhat.solenopsis.ws.impl.DefaultMetadataSvc;
import java.util.ArrayList;
import java.util.List;

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
            
            //System.out.println(PackageXml.computePackage(describeMetadata));
            
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
        
        String workingDir = "/home/alex/dev/git/AlexSandboxTest/unpackaged/";

        System.out.println("============ FS metadata ============");
        Metadata fsMetadata = new MetadataDirectory(workingDir);
        List<Type> fsTypes = fsMetadata.getTypes();
        for (Type fsType : fsTypes) {
            System.out.println("Found type: " + fsType.getName());
            for (Member fsMember : fsType.getMembers()) {
                System.out.println(" - " + fsMember.getName());
            }
        }
        
        System.out.println("============ Org metadata ============");
        Metadata orgMetadata = new MetadataApi(workingDir, new DefaultEnterpriseSvc(credentials));
        List<Type> orgTypes = orgMetadata.getTypes();
        for (Type orgType : orgTypes) {
            System.out.println("Found type: " + orgType.getName());
            for (Member orgMember : orgType.getMembers()) {
                System.out.println(" - " + orgMember.getName());
            }
        }
        
//        SfCodeSource sfOrg = new SfCodeSource(
//                workingDir,
//                new DefaultEnterpriseSvc(credentials));
        //sfOrg.fetchAllMetadata();
        //sfOrg.buildTypesPropFile();
        
//        String workingDirTwo = "/home/alex/dev/git/AlexSandboxTestTwo/";
//        SfCodeSource sfOrgTwo = new SfCodeSource(
//                workingDirTwo,
//                new DefaultEnterpriseSvc(credentials));
//        sfOrg.compareLocalSourceTo(sfOrgTwo);
        //sfOrg.pushAllMetadata();
        
    }
}
