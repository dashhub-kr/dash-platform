package com.ssafy.dash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class DashApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		
		String githubClientId = dotenv.get("GITHUB_CLIENT_ID");
		String githubClientSecret = dotenv.get("GITHUB_CLIENT_SECRET");
		if (githubClientId != null && !githubClientId.isEmpty()) {
			System.setProperty("GITHUB_CLIENT_ID", githubClientId);
		}
		if (githubClientSecret != null && !githubClientSecret.isEmpty()) {
			System.setProperty("GITHUB_CLIENT_SECRET", githubClientSecret);
		}

		String githubWebhookSecret = dotenv.get("GITHUB_WEBHOOK_SECRET");
		if (githubWebhookSecret != null && !githubWebhookSecret.isEmpty()) {
			System.setProperty("GITHUB_WEBHOOK_SECRET", githubWebhookSecret);
		}

		String githubWebhookCallback = dotenv.get("GITHUB_WEBHOOK_CALLBACK");
		if (githubWebhookCallback != null && !githubWebhookCallback.isEmpty()) {
			System.setProperty("GITHUB_WEBHOOK_CALLBACK", githubWebhookCallback);
		}

		SpringApplication.run(DashApplication.class, args);
	}

}
