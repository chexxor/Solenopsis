package com.redhat.solenopsis.sforce;

import java.util.List;

/**
 *
 * @author alex
 */
public interface Component {
    
    public String getFullName();
    public String getType();
    public String getFileName();
    public List<ComponentMember> getMembers();
    
}
