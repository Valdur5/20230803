package org.example.core.graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphService {

  private final GraphReader graphReader;

  public GraphService(GraphReader graphReader) {
    this.graphReader = graphReader;
  }

  public GraphNode constructTreeNodeFromFile(String filePathAndName) {
    List<GraphTuple> tuples = this.graphReader.getGraphTuplesForFile(filePathAndName);
    Map<String, GraphNode> lookupMap = new HashMap<>();
    GraphNode graphNode = null;
    for (GraphTuple t : tuples) {
      if (graphNode == null) {
        graphNode = new GraphNode(t.parentName());
        lookupMap.put(t.parentName(), graphNode);
        lookupMap.put(t.childName(), new GraphNode(t.childName()));
      } else {
        if (!lookupMap.containsKey(t.parentName()))
          lookupMap.put(t.parentName(), new GraphNode(t.parentName()));
        if (!lookupMap.containsKey(t.childName()))
          lookupMap.put(t.childName(), new GraphNode(t.childName()));
      }
    }
    for (GraphTuple t : tuples) {
      lookupMap.get(t.parentName()).addDependentNode(lookupMap.get(t.childName()), t.latency());
    }
    return graphNode;
  }
}
