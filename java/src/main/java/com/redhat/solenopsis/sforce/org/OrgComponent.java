package com.redhat.solenopsis.sforce.org;

import com.redhat.sforce.soap.metadata.FileProperties;
import com.redhat.solenopsis.sforce.Component;
import com.redhat.solenopsis.sforce.ComponentMember;
import java.util.List;

/**
 *
 * @author alex
 */
public class OrgComponent implements Component {

    private String fullName;
    private String type;
    private String fileName;
    public OrgComponent(String fullName) {
        this.fullName = fullName;
    }

    OrgComponent(FileProperties fileProp) {
        //this.fullName = fileProp.getFileName();
        this.fullName = fileProp.getFileName().substring(
                fileProp.getFileName().lastIndexOf('/') + 1);
        this.type = fileProp.getType();
        this.fileName = fileProp.getFileName();
    }
    
    @Override
    public String getFullName() {
        return this.fullName;
    }
    
    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public List<ComponentMember> getMembers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
