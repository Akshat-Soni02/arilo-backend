package com.project_x.project_x_backend.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class PubSubServiceMock extends PubSubService {
    @Override
    public void publishMessage(String message) {
        // Do nothing in tests
    }
}
