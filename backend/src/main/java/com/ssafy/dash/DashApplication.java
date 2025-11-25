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

		SpringApplication.run(DashApplication.class, args);
	}

}
