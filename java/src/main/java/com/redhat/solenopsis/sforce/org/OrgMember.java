package com.redhat.solenopsis.sforce.org;

import com.redhat.sforce.soap.metadata.FileProperties;
import com.redhat.solenopsis.sforce.Member;

/**
 *
 * @author alex
 */
public class OrgMember implements Member {

    private String name;
    public OrgMember(String name) {
        this.name = name;
    }

    OrgMember(FileProperties fileProp) {
        this.name = fileProp.getFileName();
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
}
