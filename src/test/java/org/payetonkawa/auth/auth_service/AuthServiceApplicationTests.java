package org.payetonkawa.auth.auth_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest (
		classes = AuthServiceApplication.class
)
@ActiveProfiles("test")
public class AuthServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
