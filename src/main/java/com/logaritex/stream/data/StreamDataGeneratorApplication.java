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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.logaritex.data.generator.DataGenerator;
import com.logaritex.data.generator.DataUtil;
import com.logaritex.data.generator.context.SharedFieldValuesContext;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.source.MutuallyExclusiveConfigurationPropertiesException;
import org.springframework.util.StringUtils;

@SpringBootApplication
@EnableConfigurationProperties(StreamDataGeneratorApplicationProperties.class)
public class StreamDataGeneratorApplication implements CommandLineRunner {

	protected static final Logger logger = LoggerFactory.getLogger(StreamDataGeneratorApplication.class);

	private final StreamDataGeneratorApplicationProperties properties;

	private MessageSender messageSender;

	private ScheduledExecutorService schedulerExecutorService;

	public StreamDataGeneratorApplication(@Autowired BinderMessageSender messageSender,
			@Autowired StreamDataGeneratorApplicationProperties appProperties) {
		this.messageSender = messageSender;
		this.properties = appProperties;
		this.schedulerExecutorService = Executors.newScheduledThreadPool(appProperties.getScheduledThreadPoolSize());
	}

	public static void main(String[] args) {
		SpringApplication.run(StreamDataGeneratorApplication.class, args);
	}

	@Override
	public void run(String... args) {

		long randomSeed = System.currentTimeMillis();

		try (SharedFieldValuesContext sharedFieldsContext = new SharedFieldValuesContext(new Random(randomSeed))) {

			AtomicBoolean exitFlag = new AtomicBoolean(false);

			List<ScheduledFuture> scheduledFutures = new ArrayList<>();

			for (StreamDataGeneratorApplicationProperties.RecordStream topicProperties : this.properties.getStreams()) {

				// Properties mutually exclusion constrain check.
				MutuallyExclusiveConfigurationPropertiesException.throwIfMultipleNonNullValuesIn((entries) -> {
					entries.put("avro-schema", topicProperties.getAvroSchema());
					entries.put("avro-schema-uri", topicProperties.getAvroSchemaUri());
				});

				Schema avroSchema = StringUtils.hasText(topicProperties.getAvroSchema())
						? DataUtil.contentToSchema(topicProperties.getAvroSchema())
						: DataUtil.resourceToSchema(topicProperties.getAvroSchemaUri());

				DataGenerator dataGenerator = new DataGenerator(avroSchema, topicProperties.getBatch().getSize(),
						!DataGenerator.UTF_8_FOR_STRING, sharedFieldsContext, randomSeed);

				// TODO parametrize the key serializer.
				RecordSenderThread recordSenderThread = new RecordSenderThread(
						topicProperties.getStreamName(), this.messageSender, dataGenerator, topicProperties, exitFlag);

				ScheduledFuture<?> future = this.schedulerExecutorService.scheduleWithFixedDelay(
						recordSenderThread,
						topicProperties.getBatch().getInitialDelay().toMillis(),
						topicProperties.getBatch().getDelay().toMillis(),
						TimeUnit.MILLISECONDS);

				scheduledFutures.add(future);
			}

			try {
				Thread.sleep(this.properties.getTerminateAfter().toMillis());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			exitFlag.set(true);
			scheduledFutures.forEach(future -> future.cancel(true));
			awaitTerminationAfterShutdown();
		}
	}

	private void awaitTerminationAfterShutdown() {
		this.schedulerExecutorService.shutdown();
		try {
			if (!this.schedulerExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
				this.schedulerExecutorService.shutdownNow();
			}
		} catch (InterruptedException ex) {
			this.schedulerExecutorService.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
}
