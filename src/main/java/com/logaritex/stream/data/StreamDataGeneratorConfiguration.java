package com.logaritex.stream.data;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.springframework.cloud.function.context.converter.avro.AvroSchemaMessageConverter;
import org.springframework.cloud.function.context.converter.avro.AvroSchemaServiceManager;
import org.springframework.cloud.function.context.converter.avro.AvroSchemaServiceManagerImpl;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.MimeType;

@Configuration
@Import(AvroSchemaServiceManagerImpl.class)
public class StreamDataGeneratorConfiguration {

    @Bean
	public StreamMessageSender messageSender(StreamBridge streamBridge) {
		return new StreamMessageSender(streamBridge);
	}

    @Bean
    public AvroSchemaMessageConverter avroMessageConverter(AvroSchemaServiceManager avroSchemaServiceManager)
            throws IOException {
        return new AvroSchemaMessageConverter(avroSchemaServiceManager) {
            @Override
            protected Schema resolveSchemaForWriting(Object payload, MessageHeaders headers, MimeType hintedContentType) {                                            
                Schema schema =  super.resolveSchemaForWriting(payload, headers, hintedContentType);
                if (schema == null && (payload instanceof GenericData.Record)) {
                    schema = ((GenericData.Record) payload).getSchema();
                }
                return schema;
            }
        };
    }
}
