package com.project_x.project_x_backend.service;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class PubSubService {

    private static final Logger logger = LoggerFactory.getLogger(PubSubService.class);

    @Value("${spring.gcs.project.id}")
    private String projectId;

    @Value("${spring.pubsub.topic.name}")
    private String topicId;

    public void publishMessage(String message) {
        TopicName topicName = TopicName.of(projectId, topicId);
        Publisher publisher = null;

        try {
            publisher = Publisher.newBuilder(topicName).build();

            ByteString data = ByteString.copyFromUtf8(message);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

            ApiFuture<String> future = publisher.publish(pubsubMessage);

            ApiFutures.addCallback(
                    future,
                    new ApiFutureCallback<String>() {

                        @Override
                        public void onFailure(Throwable t) {
                            logger.error("Error publishing message to topic: {}", topicId, t);
                        }

                        @Override
                        public void onSuccess(String messageId) {
                            logger.info("Published message ID: {} to topic: {}", messageId, topicId);
                        }
                    },
                    MoreExecutors.directExecutor());

        } catch (IOException e) {
            logger.error("Error creating publisher for topic: {}", topicId, e);
        } finally {
            if (publisher != null) {
                // When finished with the publisher, shutdown to free up resources.
                try {
                    publisher.shutdown();
                    publisher.awaitTermination(1, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    logger.error("Error shutting down publisher", e);
                }
            }
        }
    }
}
