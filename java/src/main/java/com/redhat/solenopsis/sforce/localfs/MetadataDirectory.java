package com.redhat.solenopsis.sforce.localfs;

import com.redhat.solenopsis.sforce.Component;
import com.redhat.solenopsis.sforce.Metadata;
import com.redhat.solenopsis.sforce.TypeDiff;
import com.redhat.solenopsis.sforce.Type;
import com.redhat.solenopsis.sforce.localfs.FsType;
import java.io.File;
import java.util.*;

/**
 *
 * The SfMetadataDirectory class parses a file system directory that is created
 * by unzipping the metadata zip file that is returned by the Salesforce
 * metadata API. This directory's structure is presumed as follows:
 *  - (root)
 *    - applications
 *    - classes
 *       - ClassOne.cls
 *       - ClassOne.cls-meta.xml
 *       - ClassTwo.cls
 *       - ClassTwo.cls-meta.xml
 *       .
 *       .
 *       .
 *    .
 *    .
 *    .
 *    - components
 *    - layouts
 *    - pages
 *    .
 *    .
 *    .
 * 
 * Once parsed, the metadata will be in a format that can be compared to
 * other objects that implement the SfMetadata interface, such as
 * SfMetadataDirectory and SfMetadataOrg objects.
 * 
 * @author alex berg
 */
public class MetadataDirectory implements Metadata {
    
    private File metadataDir;
    private List<Type> types;
    
    public MetadataDirectory(String metadataRootDir) {
        this.metadataDir = new File(metadataRootDir);
        this.types = this.parseTypes(this.metadataDir);
    }
    
    //The parseTypes method parses Salesforce metadata types
    //  that exist in the specified directory.
    private List<Type> parseTypes(File metadataDir) {
        List<Type> foundTypes = new ArrayList<Type>();
        
        List<File> typeDirs = Arrays.asList(metadataDir.listFiles());
        for (File typeDir : typeDirs) {
            if (typeDir.isDirectory() == false)
                continue;
            Type metadataType = new FsType(typeDir);
            foundTypes.add(metadataType);
        }
        
        return foundTypes;
    }

    @Override
    public List<Type> getTypes() {
        return this.types;
    }

    @Override
    public List<Component> getComponentsOfType(String desiredTypeName) {
        List<Component> desiredComponentList = new ArrayList<Component>();
        for (Type type : this.types) {
            if (type.getName().equalsIgnoreCase(desiredTypeName)) {
                desiredComponentList = type.getComponents();
            }
        }
        
        return desiredComponentList;
    }

    @Override
    public Map<String, TypeDiff> compareTo(
            Metadata fsMetadata2, List<String> typesToCompareList) {
        
        Map<String, TypeDiff> typeToDiffMap = new TreeMap<String, TypeDiff>();
        
        for (String typeToCompare : typesToCompareList) {
            
            List<Component> componentList1 = this.getComponentsOfType(typeToCompare);
            List<Component> componentList2 = fsMetadata2.getComponentsOfType(typeToCompare);
            
            TypeDiff typeDiff = new FsMetadataComponentDiffer(
                    componentList1, componentList2);
            
            typeToDiffMap.put(typeToCompare, typeDiff);
        }
        
        return typeToDiffMap;
    }
    
}
