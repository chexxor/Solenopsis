package com.redhat.solenopsis.sforce.org;

import com.redhat.solenopsis.sforce.Type;
import com.redhat.sforce.soap.metadata.FileProperties;
import com.redhat.solenopsis.sforce.Member;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alex
 */
public class OrgType implements Type {

    private String name;
    private List<Member> members;
    
    public OrgType(String typeName, List<FileProperties> fileProps) {
        this.name = typeName;
        this.members = createMembers(fileProps);
    }
    
    public OrgType(FileProperties fileProp) {
        this.name = fileProp.getFileName();
        String type = fileProp.getType();
        String prefix = fileProp.getNamespacePrefix();
        String id = fileProp.getId();
        String fullName = fileProp.getFullName();
        //this.members =
    }
    
    public List<Member> createMembers(List<FileProperties> fileProps) {
        List<Member> members = new ArrayList<Member>();
        for (FileProperties fileProp : fileProps) {
            members.add(new OrgMember(fileProp));
        }
        return members;
    }
    
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<Member> getMembers() {
        return this.members;
    }
    
}
