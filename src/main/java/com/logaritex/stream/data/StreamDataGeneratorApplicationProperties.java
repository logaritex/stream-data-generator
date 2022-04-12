/*
 * Copyright 2022-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.logaritex.stream.data;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties("stream.data.generator")
public class StreamDataGeneratorApplicationProperties {

	public enum ValueFormat {AVRO, JSON, YAML}

	private int scheduledThreadPoolSize = 10;

	/**
	 * Terminate the generator after given duration of time. Defaults to 1 billion years, e.g. run forever.
	 */
	private Duration terminateAfter = Duration.ofDays(1000000 * 356); // 1B years

	/**
	 * Configuration for each topic.
	 */
	private List<RecordStream> streams = new ArrayList<>();

	public int getScheduledThreadPoolSize() {
		return scheduledThreadPoolSize;
	}

	public void setScheduledThreadPoolSize(int scheduledThreadPoolSize) {
		this.scheduledThreadPoolSize = scheduledThreadPoolSize;
	}

	public Duration getTerminateAfter() {
		return terminateAfter;
	}

	public void setTerminateAfter(Duration terminateAfter) {
		this.terminateAfter = terminateAfter;
	}

	public List<RecordStream> getStreams() {
		return streams;
	}
	
	public static class Destination {
		private MessageSenderType type = MessageSenderType.STREAM;
		private String name;

		public MessageSenderType getType() {
			return type;
		}
		public void setType(MessageSenderType type) {
			this.type = type;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}		
	}

	public static class RecordStream {
		/**
		 * Kafka topic name.
		 */
		private String streamName;

		private Destination destination = new Destination();

		/**
		 * Annotated Avro schema used by the DataFaker for data generation.
		 * Only one of avroSchema or avroSchemaUri is allowed.
		 */
		private Resource avroSchemaUri;

		/**
		 * Annotated Avro Schema text content.
		 * Only one of avroSchema or avroSchemaUri is allowed.
		 */
		private String avroSchema;

		/**
		 * Value serialization format sent to Kafka topic. Defaults to JSON.
		 */
		private ValueFormat valueFormat = ValueFormat.JSON;

		/**
		 * The data generator produces new data in batch of records.
		 */
		private final Batch batch = new Batch();

		/**
		 * If set to true the random data is generated but not send to the topic.
		 */
		private boolean skipSending = false;

		public String getStreamName() {
			return streamName;
		}

		public void setStreamName(String streamName) {
			this.streamName = streamName;
		}

		public Resource getAvroSchemaUri() {
			return avroSchemaUri;
		}

		public void setAvroSchemaUri(Resource avroSchemaUri) {
			this.avroSchemaUri = avroSchemaUri;
		}

		public String getAvroSchema() {
			return avroSchema;
		}

		public void setAvroSchema(String avroSchema) {
			this.avroSchema = avroSchema;
		}

		public ValueFormat getValueFormat() {
			return valueFormat;
		}

		public void setValueFormat(ValueFormat valueFormat) {
			this.valueFormat = valueFormat;
		}

		public Batch getBatch() {
			return batch;
		}

		public boolean isSkipSending() {
			return skipSending;
		}

		public void setSkipSending(boolean skipSending) {
			this.skipSending = skipSending;
		}

		public Destination getDestination() {
			return destination;
		}
	
		@Override
		public String toString() {
			return "Stream{" +
					"streamName='" + streamName + '\'' +
					", avroSchemaUri=" + avroSchemaUri +
					", avroSchema='" + avroSchema + '\'' +
					", valueFormat=" + valueFormat +
					", batch=" + batch +
					'}';
		}
	}

	public static class Batch {
		/**
		 * Number of the records produced in one batch generation iteration.
		 */
		private int size;

		/**
		 * The time to delay before the first batch records generation.
		 */
		private Duration initialDelay = Duration.ofMillis(10);

		/**
		 * The delay between the termination of one records batch generation and the commencement of the next.
		 * Defaults to 1 billion years, e.g. no consecutive batches are generated.
		 */
		private Duration delay = Duration.ofDays(1000000 * 356); // 1B years

		/**
		 * The delay between sending each message in the batch to the Kafka topic.
		 */
		private Duration messageDelay = Duration.ofSeconds(1);

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public Duration getInitialDelay() {
			return initialDelay;
		}

		public void setInitialDelay(Duration initialDelay) {
			this.initialDelay = initialDelay;
		}

		public Duration getDelay() {
			return delay;
		}

		public void setDelay(Duration delay) {
			this.delay = delay;
		}

		public Duration getMessageDelay() {
			return messageDelay;
		}

		public void setMessageDelay(Duration messageDelay) {
			this.messageDelay = messageDelay;
		}

		@Override
		public String toString() {
			return "Batch{" +
					"size=" + size +
					", initialDelay=" + initialDelay +
					", delay=" + delay +
					", messageDelay=" + messageDelay +
					'}';
		}
	}
}
