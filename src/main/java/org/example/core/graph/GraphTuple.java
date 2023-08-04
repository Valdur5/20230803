package org.example.core.graph;

/**
 * Here we are utilizing a java 14 feature to avoid having this useless data transfer classes with
 * a lot of boilerplate code.
 */
public record GraphTuple(String parentName, String childName, Integer latency) {
}
