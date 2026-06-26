package dev.michaelgoldman.subscriptiontrackerbackend;

import org.springframework.boot.SpringApplication;

public class TestSubscriptionTrackerBackendApplication {

	static void main(String[] args) {
		SpringApplication.from(SubscriptionTrackerBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
