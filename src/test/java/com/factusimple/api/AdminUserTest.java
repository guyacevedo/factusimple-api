package com.factusimple.api;

import com.factusimple.api.user.entity.Role;
import com.factusimple.api.user.entity.User;
import com.factusimple.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AdminUserTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void testAdminUserCreatedByMigration() {
		User admin = userRepository.findByEmail("admin@factusimple.com").orElse(null);
		assertNotNull(admin, "Admin user should be created by migration");
		assertEquals("admin@factusimple.com", admin.getEmail());
		assertEquals(Role.ADMIN, admin.getRole());
		assertEquals("Admin", admin.getFirstName());
		assertEquals("FactuSimple", admin.getLastName());
		assertTrue(admin.getIsActive(), "Admin user should be active");
	}

	@Test
	void testAdminPasswordVerification() {
		User admin = userRepository.findByEmail("admin@factusimple.com").orElse(null);
		assertNotNull(admin);
		assertTrue(
			passwordEncoder.matches("Admin1234!", admin.getPassword()),
			"Password should match 'Admin1234!'"
		);
	}
}
