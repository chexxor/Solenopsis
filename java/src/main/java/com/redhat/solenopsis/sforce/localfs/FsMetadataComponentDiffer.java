package com.redhat.solenopsis.sforce.localfs;

import com.redhat.solenopsis.sforce.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author alex
 */
class FsMetadataComponentDiffer implements TypeDiff {
    
    private final String diffedTypeName;
    private final Map<String, List<MemberDiff>> componentNameToDiffMap;

    public FsMetadataComponentDiffer(
            List<Component> componentList1, List<Component> componentList2) {
        
        if (componentList1 == null || componentList2 == null) {
            throw new IllegalArgumentException("Two parameters are required.");
        }
        
        if (componentList1.isEmpty()) {
            this.diffedTypeName = "";
        }
        else {
            Component someComponent = componentList1.get(0);
            this.diffedTypeName = someComponent.getType();
        }
        
        Map<String, List<MemberDiff>> myComponentNameToDiffMap
                = new TreeMap<String, List<MemberDiff>>();
        for (Component comp1 : componentList1) {
            Boolean foundMatch = false;
            for (Component comp2 : componentList2) {
                Boolean isComponentComparable = false;
                if (comp1.getType().equalsIgnoreCase(comp2.getType())
                        && comp1.getFullName().equalsIgnoreCase(comp2.getFullName())) {
                    isComponentComparable = true;
                }
                
                if (isComponentComparable) {
                    List<MemberDiff> compMemberDiffList = this.diffComponent(comp1, comp2);
                    myComponentNameToDiffMap.put(comp1.getFullName(), compMemberDiffList);
                    foundMatch = true;
                }
                if (foundMatch)
                    continue;
            }
            if (!foundMatch) {
                //Component was in list1, but not list2.
                List<MemberDiff> compMemberDiffList = new ArrayList<MemberDiff>();
                compMemberDiffList.add(new FsMissingComponentDiff(comp1.getFullName()));
                myComponentNameToDiffMap.put(comp1.getFullName(), compMemberDiffList);
            }
        }
        this.componentNameToDiffMap = myComponentNameToDiffMap;
        
    }
    
    private List<MemberDiff> diffComponent(
            Component component1, Component component2) {
        
        if (!component1.getFullName().equalsIgnoreCase(component2.getFullName())) {
            //TODO: If components aren't comparable, return what?
        }
        
        List<MemberDiff> memberDiffList
                = new ArrayList<MemberDiff>();
        
        List<ComponentMember> memberList1 = component1.getMembers();
        List<ComponentMember> memberList2 = component2.getMembers();
        
        for (ComponentMember member1 : memberList1) {
            for (ComponentMember member2 : memberList2) {
                
                Boolean isMemberComparable = false;
                
                if (member1.getName().equalsIgnoreCase(member2.getName())) {
                    isMemberComparable = true;
                }
                
                if (isMemberComparable) {
                    MemberDiff memberDiffResult
                            = new FsComponentMemberDiff(member1, member2);
                    memberDiffList.add(memberDiffResult);
                }
            }
        }
        
        return memberDiffList;
    }

    @Override
    public Boolean isDifferent() {
        return false;
    }

    @Override
    public List<String> getComponentList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, List<MemberDiff>> getComponentNameToDiffMap() {
        return this.componentNameToDiffMap;
    }
    
}
