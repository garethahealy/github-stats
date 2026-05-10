package com.garethahealy.githubstats.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "github")
public interface GitHubConfigProperties {

    String login();

    String oauth();
}
