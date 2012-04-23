package com.redhat.solenopsis.sforce.localfs;

import com.redhat.solenopsis.sforce.Component;
import com.redhat.solenopsis.sforce.Type;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author alex
 */
public class FsType extends File implements Type {

    public FsType(String typeDirPath) {
        super(typeDirPath);
    }
    public FsType(File typeDir) {
        super(typeDir.getAbsolutePath());
    }
    
    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public List<Component> getComponents() {
        List<Component> componentList = new ArrayList<Component>();
        for (File componentFile : Arrays.asList(super.listFiles())) {
            componentList.add(new FsComponent(componentFile));
        }
        return componentList;
    }
    
}
