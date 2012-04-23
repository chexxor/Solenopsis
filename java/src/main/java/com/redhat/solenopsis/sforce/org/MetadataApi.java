package com.redhat.solenopsis.sforce.org;

import com.redhat.sforce.soap.metadata.DescribeMetadataObject;
import com.redhat.sforce.soap.metadata.DescribeMetadataResult;
import com.redhat.sforce.soap.metadata.FileProperties;
import com.redhat.sforce.soap.metadata.ListMetadataQuery;
import com.redhat.solenopsis.sforce.Metadata;
import com.redhat.solenopsis.sforce.Type;
import com.redhat.solenopsis.ws.LoginSvc;
import com.redhat.solenopsis.ws.MetadataSvc;
import com.redhat.solenopsis.ws.impl.DefaultMetadataSvc;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author alex
 */
public class MetadataApi implements Metadata {
    //Login properties.
    private final File workingDir;
    private final LoginSvc login;
    private final double apiVersion;
    
    //Metadata types, which a SfMetadataOrg should provide.
    private List<Type> types;

    //Constructors
    public MetadataApi(String workingDir, LoginSvc loginSvc) throws Exception {
        this(workingDir, loginSvc, Double.valueOf(loginSvc.getCredentials().getApiVersion()));
    }
    public MetadataApi(String workingDir, LoginSvc loginSvc, double apiVersion) throws Exception {
        this.workingDir = new File(workingDir);
        this.login = loginSvc;
        this.apiVersion = apiVersion;
        
        this.types = queryTypes(this.login, this.apiVersion);
    }
    
    //Helper functions
    private static List<Type> queryTypes(LoginSvc login, double apiVersion) throws Exception {
        
        //Get the org's metadata from its web service.
        MetadataSvc metadataSvc = new DefaultMetadataSvc(login);
        final DescribeMetadataResult describeMetadata = metadataSvc.getPort().describeMetadata(apiVersion);
        final List<DescribeMetadataObject> metaDescribe = describeMetadata.getMetadataObjects();                
        
        //Save the metadata description to state. We can serialize to disk later.
        //this.metadataObjects = metaDescribe;
        
        //Then use this list to get list of all components.
        List<Type> types = buildTypes(metadataSvc, metaDescribe, apiVersion);
        
        return types;
    }
    
    private static List<Type> buildTypes(
            MetadataSvc metadataSvc, List<DescribeMetadataObject> metaDescribe,
            double apiVersion) throws Exception {
        
        List<Type> types = new ArrayList<Type>();
                
        List<ListMetadataQuery> typeQueries = buildTypeQuery(metaDescribe);
        
        //Seems the API only allows 3 typeQueries per request,
        //  so we must slice the typeQueries into chunks of 3 to send.
        final List<FileProperties> fileProperties = new ArrayList<FileProperties>();
        List<ListMetadataQuery> currentQuery = new ArrayList<ListMetadataQuery>();
        int numberRequests = (int) Math.ceil(typeQueries.size() % 3);
        for (int i = 1; i < typeQueries.size() + 1; i++) {
            //Build up a few into one query,
            currentQuery.add(typeQueries.get(i-1));
            if (i % 3 == 0 || i == typeQueries.size()) {//break point
                //then request them.
                List<FileProperties> tempFileProps = metadataSvc.getPort().listMetadata(currentQuery, apiVersion);
                fileProperties.addAll(tempFileProps);
                currentQuery.clear();
            }
        }
        
        //We've got all the components. They need to be organized by type.
        TreeMap<String, List<FileProperties>> typeToComponentProps = 
                new TreeMap<String, List<FileProperties>>();
        for (FileProperties fileProp : fileProperties) {
            String fileName = fileProp.getFileName();
            String folderName = fileName.split("/")[0];
            //List<FileProperties> fileProps = typeToComponentProps.get(fileProp.getType());
            List<FileProperties> fileProps = typeToComponentProps.get(folderName);
            if (fileProps == null)
                fileProps = new ArrayList<FileProperties>();
            fileProps.add(fileProp);
            //typeToComponentProps.put(fileProp.getType(), fileProps);
            typeToComponentProps.put(folderName, fileProps);
        }
        
        //The received properties should be sorted by type,
        //  now construct OrgTypes for them.
        for (String typeName : typeToComponentProps.keySet()) {
            Type type = new OrgType(typeName, typeToComponentProps.get(typeName));
            types.add(type);
        }
        
        return types;
    }
    
    private static List<ListMetadataQuery> buildTypeQuery(
            List<DescribeMetadataObject> metaDescribe) {
        
        List<ListMetadataQuery> typeQueries = new ArrayList<ListMetadataQuery>();
        
        //Build query list to retrieve all the components.
        final ListMetadataQuery componentQuery = new ListMetadataQuery();
        for (DescribeMetadataObject dmo : metaDescribe) {
            ListMetadataQuery typeQuery = new ListMetadataQuery();
            typeQuery.setType(dmo.getXmlName());
            if (dmo.getDirectoryName() != null || !dmo.getDirectoryName().equals("")) {
                typeQuery.setFolder(dmo.getDirectoryName());
            }
            typeQueries.add(typeQuery);
        }

        return typeQueries;
    }
    
    
    @Override
    public List<Type> getTypes() {
        return this.types;
    }
    
}
