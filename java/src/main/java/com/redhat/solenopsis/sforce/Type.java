package com.redhat.solenopsis.sforce;

import java.util.List;

/**
 *
 * @author alex
 */
public interface Type {
    
    public String getName();
    public List<Component> getComponents();
}
