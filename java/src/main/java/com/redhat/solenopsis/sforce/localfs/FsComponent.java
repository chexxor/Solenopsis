package com.redhat.solenopsis.sforce.localfs;

import com.redhat.solenopsis.sforce.Component;
import java.io.File;

/**
 *
 * @author alex
 */
public class FsComponent extends File implements Component {

    public FsComponent(String componentFilePath) {
        super(componentFilePath);
    }
    public FsComponent(File file) {
        super(file.getAbsolutePath());
    }

    public String getDir() {
        return super.getParentFile().getName();
    }
    
    @Override
    public String getFullName() {
        return super.getName();
    }

    @Override
    public String getType() {
        return "TODO";
    }

    @Override
    public String getFileName() {
        return super.getParentFile().getName() + "/" + super.getName();
    }
    
}
