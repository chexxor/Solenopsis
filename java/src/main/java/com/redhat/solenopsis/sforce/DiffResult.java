package com.redhat.solenopsis.sforce;

import java.util.List;
import java.util.Map;

/**
 *
 * @author alex
 */
public interface DiffResult {
    
    public Boolean isDifferent();
    
    public List<String> getDifferentMemberNames();
    
    //TODO: What diff info do we want to pass back?
    public Map<String, String> getMemberNameToDiffMap();
}
