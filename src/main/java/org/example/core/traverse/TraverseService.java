package org.example.core.traverse;

import org.example.core.graph.GraphNode;
import org.example.core.graph.GraphService;

import java.util.List;
import java.util.Map;

public class TraverseService {
  private final GraphService graphService;
  private Map<String, GraphNode> lookupMap;


  public TraverseService(GraphService graphService) {
    this.graphService = graphService;
  }

  public void initGraph(String absolutePathAndFileName) {
    if (this.lookupMap == null) {
      this.lookupMap = this.graphService.constructGraphFromFile(absolutePathAndFileName);
    }
  }

  public int averageLatencyOfPath(List<String> path) {
    GraphNode startingNode = null;
    GraphNode currentNode = null;
    int averageLatency = 0;
    for(String p : path) {
      if(startingNode == null) {
        startingNode = this.lookupMap.get(p);
        currentNode = startingNode;
      } else {
        Map.Entry<GraphNode, Integer> childNode = currentNode.getDependentTreeNodeByName(p);
        if(childNode == null) {
          throw new NoTraceFoundException();
        } else {
          averageLatency += childNode.getValue();
          currentNode = childNode.getKey();
        }
      }
    }
    return averageLatency;
  }
}
