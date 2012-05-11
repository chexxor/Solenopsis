package com.redhat.solenopsis.sforce.localfs;

import com.redhat.solenopsis.sforce.ComponentMember;

/**
 *
 * @author alex
 */
public class FsComponentMember implements ComponentMember {

    private String memberName;
    private String memberValue;
    
    FsComponentMember(String memberName, String memberValue) {
        this.memberName = memberName;
        this.memberValue = memberValue;
    }
    
    @Override
    public String getName() {
        return this.memberName;
    }
    
    @Override
    public String getValue() {
        return this.memberValue;
    }
    
}
