package org.example.core.graph;

import org.example.details.MalformedInputFormatException;

import java.util.Map;

public interface GraphService {
    /**
     * Create a Graph from a given file.
     * @param filePathAndName the path and file name.
     * @return the first GraphNode (based on occurrence in the list) and the
     * lookup map where all nodes are available.
     * @throws MalformedInputFormatException in case the file could not be loaded becase the format is not right.
     */
    Map.Entry<GraphNode, Map<String, GraphNode>> constructGraphFromFile(String filePathAndName);
}
