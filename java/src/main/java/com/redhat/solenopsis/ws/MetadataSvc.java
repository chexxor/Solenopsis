package com.redhat.solenopsis.ws;

import com.redhat.sforce.soap.metadata.MetadataPortType;

/**
 *
 * Interface surrounding the metadata web service.
 *
 * @author sfloess
 *
 */
public interface MetadataSvc extends Svc {
    /**
     * Return the port.
     */
    public MetadataPortType getPort() throws Exception;
}
