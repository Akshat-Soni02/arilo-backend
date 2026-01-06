package com.project_x.project_x_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProjectXBackendApplicationTests {

	@org.springframework.boot.test.mock.mockito.MockBean
	private com.project_x.project_x_backend.service.GcsStorageService gcsStorageService;

	@Test
	void contextLoads() {
	}

}
