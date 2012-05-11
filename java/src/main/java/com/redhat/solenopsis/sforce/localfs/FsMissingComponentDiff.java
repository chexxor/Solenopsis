/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.solenopsis.sforce.localfs;

import com.redhat.solenopsis.sforce.MemberDiff;

/**
 *
 * @author alex
 */
public class FsMissingComponentDiff implements MemberDiff {

    private String componentName;
    
    public FsMissingComponentDiff(String componentName) {
        this.componentName = componentName;
    }

    @Override
    public String getDiffMessage() {
        return "Missing Component: " + this.componentName;
    }

    @Override
    public String memberName() {
        return "Missing Component: " + this.componentName;
    }

    @Override
    public Boolean isDifferent() {
        return true;
    }
    
}
