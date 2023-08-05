package org.example.core.graph;

import org.example.details.MalformedInputFormatException;

import java.util.Map;

public interface GraphService {
  /**
   * Create a Graph from a given file.
   *
   * @param filePathAndName the path and file name.
   * @return lookup map where all nodes are available.
   * @throws MalformedInputFormatException in case the file could not be loaded because the format is
   *     not right.
   */
  Map<String, GraphNode> constructGraphFromFile(String filePathAndName);
}
