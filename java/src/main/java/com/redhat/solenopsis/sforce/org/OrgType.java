package com.redhat.solenopsis.sforce.org;

import com.redhat.solenopsis.sforce.Type;
import com.redhat.sforce.soap.metadata.FileProperties;
import com.redhat.solenopsis.sforce.Component;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alex
 */
public class OrgType implements Type {

    private String name;
    private List<Component> components;
    
    public OrgType(String typeName, List<FileProperties> fileProps) {
        this.name = typeName;
        this.components = createComponents(fileProps);
    }
    
    public OrgType(FileProperties fileProp) {
        this.name = fileProp.getFileName();
        String type = fileProp.getType();
        String prefix = fileProp.getNamespacePrefix();
        String id = fileProp.getId();
        String fullName = fileProp.getFullName();
        //this.members =
    }
    
    public List<Component> createComponents(List<FileProperties> fileProps) {
        List<Component> components = new ArrayList<Component>();
        for (FileProperties fileProp : fileProps) {
            components.add(new OrgComponent(fileProp));
        }
        return components;
    }
    
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<Component> getComponents() {
        return this.components;
    }
    
}
