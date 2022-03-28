package com.logaritex.stream.data;

import org.springframework.cloud.stream.function.StreamBridge;

public class BinderMessageSender implements MessageSender {

    private StreamBridge streamBridge;

    public BinderMessageSender(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public void send(String streamName, Object key, Object value) {
        this.streamBridge.send(streamName, value);
    }
}
