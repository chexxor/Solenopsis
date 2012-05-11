package com.redhat.solenopsis.sforce.localfs;

import com.redhat.solenopsis.sforce.ComponentMember;
import com.redhat.solenopsis.sforce.MemberDiff;

/**
 *
 * @author alex
 */
public class FsComponentMemberDiff implements MemberDiff {

    private Boolean isDifferent;
    private String memberName;
    private String diffMessage;
    
    public FsComponentMemberDiff(ComponentMember member1, ComponentMember member2) {
        if (member1 == null || member2 == null)
            throw new IllegalArgumentException("Can't compare null members.");
        
        this.memberName = member1.getName();
        if (!member1.getValue().equalsIgnoreCase(member2.getValue())) {
            this.diffMessage = member1.getValue() + "/" + member2.getValue();
        }
    }
    
    @Override
    public Boolean isDifferent() {
        return this.isDifferent;
    }
    
    @Override
    public String getDiffMessage() {
        return this.diffMessage;
    }

    @Override
    public String memberName() {
        return this.memberName;
    }
    
}
