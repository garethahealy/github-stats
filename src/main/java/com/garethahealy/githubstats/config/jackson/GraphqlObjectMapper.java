package com.garethahealy.githubstats.config.jackson;

import jakarta.inject.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Qualifies the {@link com.fasterxml.jackson.databind.ObjectMapper} used for GitHub GraphQL response binding.
 * Not {@link jakarta.inject.Named} so this bean does not participate in ambiguous {@code @Inject ObjectMapper} resolution.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
public @interface GraphqlObjectMapper {
}
