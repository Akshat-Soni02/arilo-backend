package com.project_x.project_x_backend.service;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PubSubService {

    @Value("${spring.gcs.project.id}")
    private String projectId;

    @Value("${spring.pubsub.topic.name}")
    private String topicId;

    public void publishMessage(String message) {
        log.info("Preparing to publish message to topic: {}", topicId);
        TopicName topicName = TopicName.of(projectId, topicId);
        Publisher publisher = null;

        try {
            publisher = Publisher.newBuilder(topicName).build();

            ByteString data = ByteString.copyFromUtf8(message);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

            log.debug("Publishing message payload: {}", message);
            ApiFuture<String> future = publisher.publish(pubsubMessage);

            ApiFutures.addCallback(
                    future,
                    new ApiFutureCallback<String>() {

                        @Override
                        public void onFailure(Throwable t) {
                            log.error("Failed to publish message to topic: {}. Error: {}", topicId, t.getMessage(), t);
                        }

                        @Override
                        public void onSuccess(String messageId) {
                            log.info("Successfully published message. ID: {} to topic: {}", messageId, topicId);
                        }
                    },
                    MoreExecutors.directExecutor());

        } catch (IOException e) {
            log.error("Could not create PubSub publisher for topic: {}. Error: {}", topicId, e.getMessage(), e);
        } finally {
            if (publisher != null) {
                // When finished with the publisher, shutdown to free up resources.
                try {
                    log.debug("Shutting down PubSub publisher for topic: {}", topicId);
                    publisher.shutdown();
                    publisher.awaitTermination(1, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    log.error("Interrupted while shutting down PubSub publisher: {}", e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
