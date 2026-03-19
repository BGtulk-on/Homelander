package com.uktc.schoolInventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SchoolInventoryApplication {
	public static void main(String[] args) {
		SpringApplication.run(SchoolInventoryApplication.class, args);
	}
}
