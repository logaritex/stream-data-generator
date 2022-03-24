package com.logaritex.stream.data;

public interface MessageSender {
	void send(String streamName, Object key, Object value);
}
