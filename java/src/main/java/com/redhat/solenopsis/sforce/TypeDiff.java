package com.redhat.solenopsis.sforce;

import java.util.List;
import java.util.Map;

/**
 *
 * @author alex
 */
public interface TypeDiff {
    
    public Boolean isDifferent();
    public List<String> getComponentList();
    public Map<String, List<MemberDiff>> getComponentNameToDiffMap();
}
