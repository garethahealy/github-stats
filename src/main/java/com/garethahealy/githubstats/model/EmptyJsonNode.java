package com.garethahealy.githubstats.model;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.io.IOException;
import java.util.List;

public class EmptyJsonNode extends JsonNode {

    @Override
    public <T extends JsonNode> T deepCopy() {
        return null;
    }

    @Override
    public JsonToken asToken() {
        return null;
    }

    @Override
    public JsonParser.NumberType numberType() {
        return null;
    }

    @Override
    public JsonNode get(int index) {
        return null;
    }

    @Override
    public JsonNode path(String fieldName) {
        return null;
    }

    @Override
    public JsonNode path(int index) {
        return null;
    }

    @Override
    public JsonParser traverse() {
        return null;
    }

    @Override
    public JsonParser traverse(ObjectCodec codec) {
        return null;
    }

    @Override
    protected JsonNode _at(JsonPointer ptr) {
        return null;
    }

    @Override
    public JsonNodeType getNodeType() {
        return null;
    }

    @Override
    public String asText() {
        return null;
    }

    @Override
    public JsonNode findValue(String fieldName) {
        return null;
    }

    @Override
    public JsonNode findPath(String fieldName) {
        return null;
    }

    @Override
    public JsonNode findParent(String fieldName) {
        return null;
    }

    @Override
    public List<JsonNode> findValues(String fieldName, List<JsonNode> foundSoFar) {
        return null;
    }

    @Override
    public List<String> findValuesAsText(String fieldName, List<String> foundSoFar) {
        return null;
    }

    @Override
    public List<JsonNode> findParents(String fieldName, List<JsonNode> foundSoFar) {
        return null;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {

    }

    @Override
    public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {

    }
}
