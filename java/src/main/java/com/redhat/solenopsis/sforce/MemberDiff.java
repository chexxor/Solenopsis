package com.redhat.solenopsis.sforce;

/**
 *
 * @author alex
 */
public interface MemberDiff {

    public String getDiffMessage();
    public String memberName();
    public Boolean isDifferent();
    
}
