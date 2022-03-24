package com.logaritex.stream.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class BinderMessageSender implements MessageSender {

    private StreamBridge streamBridge;

    public BinderMessageSender(@Autowired StreamBridge streamBridge){
        this.streamBridge = streamBridge;

    }

    @Override
    public void send(String streamName, Object key, Object value) {        
        this.streamBridge.send(streamName, value);        
    }    
}
