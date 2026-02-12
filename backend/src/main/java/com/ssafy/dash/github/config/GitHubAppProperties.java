package com.ssafy.dash.github.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "github.app")
public class GitHubAppProperties {

    private final String id;
    private final String privateKeyPath;

    @ConstructorBinding
    public GitHubAppProperties(String id, String privateKeyPath) {
        this.id = id;
        this.privateKeyPath = privateKeyPath;
    }

    public String getId() {
        return id;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }
}
