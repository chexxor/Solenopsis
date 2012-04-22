/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.solenopsis.sforce.util;

import com.redhat.sforce.soap.metadata.AsyncResult;
import com.redhat.sforce.soap.metadata.MetadataPortType;
import com.redhat.solenopsis.app.Main;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alex
 */
public class AsyncUtils {
    
    public static AsyncResult waitForResponse(AsyncResult asyncRetrieveResponse, MetadataPortType port) throws Exception {
        
        final long ONE_SECOND = 1000;
        final int MAX_NUM_POLL_REQUESTS = 50;
        
        // Wait until the AsyncResult has returned with a value.
        int poll = 0;
        long waitTimeMilliSecs = ONE_SECOND;
        
        //Get response
        while (!asyncRetrieveResponse.isDone()) {
            try {
                // Exponential backoff
                Thread.sleep(waitTimeMilliSecs);
                waitTimeMilliSecs *= 2;
                if (poll++ > MAX_NUM_POLL_REQUESTS) {
                    throw new Exception("Request timed out.  If this is a large set "
                            + "of metadata components, check that the time allowed "
                            + "by MAX_NUM_POLL_REQUESTS is sufficient.");
                }

                // Log the Id of the last response
                List<String> responseIds = new ArrayList<String>();
                responseIds.add(asyncRetrieveResponse.getId());
                
                // We've waited, let's check the web service again for completion.
                asyncRetrieveResponse = (AsyncResult) port.checkStatus(responseIds).get(0);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return asyncRetrieveResponse;
        
    }
    
}
