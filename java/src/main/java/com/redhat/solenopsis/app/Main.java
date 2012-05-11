package com.redhat.solenopsis.app;

import com.redhat.sforce.soap.metadata.DescribeMetadataObject;
import com.redhat.sforce.soap.metadata.DescribeMetadataResult;
import com.redhat.sforce.soap.metadata.FileProperties;
import com.redhat.sforce.soap.metadata.ListMetadataQuery;
import com.redhat.solenopsis.credentials.Credentials;
import com.redhat.solenopsis.credentials.impl.PropertiesCredentials;
import com.redhat.solenopsis.properties.impl.FileMonitorPropertiesMgr;
import com.redhat.solenopsis.sforce.*;
import com.redhat.solenopsis.sforce.localfs.MetadataDirectory;
import com.redhat.solenopsis.sforce.org.MetadataApi;
import com.redhat.solenopsis.ws.LoginSvc;
import com.redhat.solenopsis.ws.MetadataSvc;
import com.redhat.solenopsis.ws.impl.DefaultEnterpriseSvc;
import com.redhat.solenopsis.ws.impl.DefaultMetadataSvc;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        String workingDir2 = "/home/alex/dev/git/AlexSandboxTest2/unpackaged/";

        System.out.println("============ FS metadata ============");
        Metadata fsMetadata = new MetadataDirectory(workingDir);
        Metadata fsMetadata2 = new MetadataDirectory(workingDir2);
//        List<Type> fsTypes = fsMetadata.getTypes();
//        for (Type fsType : fsTypes) {
//            System.out.println("Found type: " + fsType.getName());
//            for (Component fsComponent : fsType.getComponents()) {
//                System.out.println(" - " + fsComponent.getFullName()
//                        + " (" + fsComponent.getType() + ")"
//                        + " [" + fsComponent.getFileName() + "]");
//                for (ComponentMember member : fsComponent.getMembers()) {
//                    System.out.println("  -- " + member);
//                }
//            }
//        }
        
//        System.out.println("============ FS Classes ============");
//        List<Component> classList = fsMetadata.getComponentsOfType("classes");
//        for (Component classComponent : classList) {
//            System.out.println("Class name: " + classComponent.getFullName());
//        }
//        System.out.println("============ FS Objects ============");
//        List<Component> objectList = fsMetadata.getComponentsOfType("objects");
//        for (Component objectComponent : objectList) {
//            System.out.println("Object name: " + objectComponent.getFullName());
//        }
        List<String> typesToCompare = new ArrayList<String>();
        typesToCompare.add("classes");
        Map<String, TypeDiff> typeNameToDiffMap
                = fsMetadata.compareTo(fsMetadata2, typesToCompare);
        
        for (String typeName : typeNameToDiffMap.keySet()) {
            System.out.println("Type: " + typeName);
            TypeDiff typeDiff = typeNameToDiffMap.get(typeName);
//            List<String> componentNameList = typeDiff.getComponentList();
            Map<String, List<MemberDiff>> componentNameToDiffMap
                    = typeDiff.getComponentNameToDiffMap();
            for (String componentName : componentNameToDiffMap.keySet()) {
                System.out.println("    Component: " + componentName);
                List<MemberDiff> memberDiffList
                        = componentNameToDiffMap.get(componentName);
                for (MemberDiff diff : memberDiffList) {
                    System.out.println("        Member different? " + diff.isDifferent()
                            + ": " + diff.getDiffMessage());
                }
            }
        }
        
        //        System.out.println("============ Org metadata ============");
        //        Metadata orgMetadata = new MetadataApi(workingDir, new DefaultEnterpriseSvc(credentials));
        //        List<Type> orgTypes = orgMetadata.getTypes();
        //        for (Type orgType : orgTypes) {
        //            System.out.println("Found type: " + orgType.getName());
        //            for (Component orgComponent : orgType.getComponents()) {
        //                System.out.println(" - " + orgComponent.getFullName()
        //                        + " (" + orgComponent.getType() + ")"
        //                        + " [" + orgComponent.getFileName() + "]");
        //            }
        //        }
        //        System.out.println("============ Org Classes ============");
        //        List<Component> orgClassList = orgMetadata.getComponentsOfType("classes");
        //        for (Component classComponent : orgClassList) {
        //            System.out.println("Class name: " + classComponent.getFullName());
        //        }
        
        
    }
}
