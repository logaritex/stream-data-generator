package com.logaritex.stream.data.protocol;

import com.logaritex.stream.data.MessageSender;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class RabbitMessageSender implements MessageSender {

	private final RabbitTemplate rabbitTemplate;

	public RabbitMessageSender() {
		this.rabbitTemplate = new RabbitTemplate();

	}

	@Override
	public void send(Object key, Object value) {
		//this.rabbitTemplate.send(value);
	}
}
