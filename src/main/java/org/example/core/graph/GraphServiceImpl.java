package org.example.core.graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphServiceImpl implements GraphService {

  private final GraphReader graphReader;

  public GraphServiceImpl(GraphReader graphReader) {
    this.graphReader = graphReader;
  }

  @Override
  public Map<String, GraphNode> constructGraphFromFile(String filePathAndName) {
    List<GraphTuple> tuples = this.graphReader.getGraphTuplesForFile(filePathAndName);
    Map<String, GraphNode> lookupMap = new HashMap<>();
    for (GraphTuple t : tuples) {
      if (!lookupMap.containsKey(t.parentName()))
        lookupMap.put(t.parentName(), new GraphNode(t.parentName()));
      if (!lookupMap.containsKey(t.childName()))
        lookupMap.put(t.childName(), new GraphNode(t.childName()));
    }
    for (GraphTuple t : tuples) {
      lookupMap.get(t.parentName()).addDependentNode(lookupMap.get(t.childName()), t.latency());
    }
    return lookupMap;
  }
}
