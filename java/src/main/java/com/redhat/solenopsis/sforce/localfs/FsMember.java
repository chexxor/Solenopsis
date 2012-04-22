package com.redhat.solenopsis.sforce.localfs;

import com.redhat.solenopsis.sforce.Member;
import java.io.File;

/**
 *
 * @author alex
 */
public class FsMember extends File implements Member {

    public FsMember(String memberFilePath) {
        super(memberFilePath);
    }
    public FsMember(File file) {
        super(file.getAbsolutePath());
    }
    
    @Override
    public String getName() {
        return super.getName();
    }
    
}
