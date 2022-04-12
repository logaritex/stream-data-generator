package com.logaritex.stream.data;

public interface MessageSender {

	MessageSenderType type();

	void send(String destination, Object key, Object value);
}
