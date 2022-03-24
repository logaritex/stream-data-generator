package com.logaritex.stream.data;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.logaritex.data.generator.DataGenerator;
import com.logaritex.data.generator.DataUtil;

import org.apache.avro.generic.GenericData;

public class RecordSenderThread implements Runnable {

	private final MessageSender messageSender;
	private final DataGenerator dataGenerator;
	private final StreamDataGeneratorApplicationProperties.RecordStream topicProperties;
	private final AtomicBoolean exitFlag;
	private String streamName;

	public RecordSenderThread(
			String streamName,
			MessageSender messageSender,
			DataGenerator dataGenerator,
			StreamDataGeneratorApplicationProperties.RecordStream topicProperties,
			AtomicBoolean exitFlag) {

		this.streamName = streamName;
		this.messageSender = messageSender;
		this.dataGenerator = dataGenerator;
		this.topicProperties = topicProperties;
		this.exitFlag = exitFlag;
	}

	@Override
	public void run() {

		final AtomicLong messageKey = new AtomicLong(System.currentTimeMillis());

		Iterator<GenericData.Record> iterator = dataGenerator.iterator();
		if (!this.topicProperties.isSkipSending()) {
			while (!this.exitFlag.get() && iterator.hasNext()) {
				GenericData.Record record = iterator.next();
				if (record != null) {
					Object messageValue = toValueFormat(record);
					this.messageSender.send(this.streamName, messageKey.incrementAndGet(), messageValue);
				}
				try {
					Thread.sleep(this.topicProperties.getBatch().getMessageDelay().toMillis());
				} catch (InterruptedException e) {
					// e.printStackTrace();
				}
			}
		}
	}

	private Object toValueFormat(GenericData.Record record) {
		switch (this.topicProperties.getValueFormat()) {
			case JSON:
				return DataUtil.toJsonObjectNode(record);
			case YAML:
				return DataUtil.toYaml(record);
			default:
				return record;
		}
	}
}
