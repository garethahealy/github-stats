package com.garethahealy.githubstats;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.directory.api.ldap.codec.standalone.StandaloneLdapApiService;
import org.apache.mina.transport.socket.nio.NioProcessor;

@RegisterForReflection(targets = {StandaloneLdapApiService.class, NioProcessor.class})
public class ReflectionConfiguration {
}
