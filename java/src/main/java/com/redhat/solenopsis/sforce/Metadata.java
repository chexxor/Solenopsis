package com.redhat.solenopsis.sforce;

import java.util.List;
import java.util.Map;

/**
 *
 * @author alex
 */
public interface Metadata {
    
    public List<Type> getTypes();

    public List<Component> getComponentsOfType(String typeName);

    public Map<String, TypeDiff> compareTo(Metadata fsMetadata2, List<String> typesToCompare);
    
}
