package org.example.core.traverse;

import org.example.core.graph.GraphNode;
import org.example.core.graph.GraphService;

import java.util.*;

public class TraverseService {
  private final GraphService graphService;
  private Map<String, GraphNode> lookupMap;


  public TraverseService(GraphService graphService) {
    this.graphService = graphService;
  }

  /**
   * Method responsible to initialize the graph.
   * As we let the user control what should be initialized this needs to be called
   * manually before other operations in this class can be used.
   * @param absolutePathAndFileName the path and file name of a file with the graph data to load.
   */
  public void initGraph(String absolutePathAndFileName) {
    if (this.lookupMap == null) {
      this.lookupMap = this.graphService.constructGraphFromFile(absolutePathAndFileName);
    }
  }

  /**
   * Method to retrieve the average latency of a given path.
   * @param path the path where the first element is the starting point and the last element is the end point.
   * @return the average latency
   * @throws NoTraceFoundException in case the requested path can't be found in the graph.
   */
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

  /**
   * Method to find the max number of traces from a start to a end node.
   * @param startNode the start node name
   * @param endNode the end node name
   * @param maxHops the maximal amount of hops allowed to take. A - B - C would be 2 hops.
   * @return the number of permutations possible given the restraints we have.
   */
  public int findNumberOfPossibleTraces(String startNode, String endNode, int maxHops, boolean onlyExactHops) {
    if(startNode == null || endNode == null) {
      throw new InvalidInputException("The start and end node can't be empty or equal.");
    }
    if(maxHops < 1) {
      throw new InvalidInputException("There can't be a trace if there is no hops are allowed.");
    }
    GraphNode gn = this.lookupMap.get(startNode);
    int possiblePaths = 0;

    Queue<GraphNode> nodeQueue = new LinkedList<>();
    gn.setHops(0);
    nodeQueue.add(gn);
    while (!nodeQueue.isEmpty()) {
      GraphNode currentNode = nodeQueue.poll();
      if (currentNode.getHops() <= maxHops) {
        if (currentNode.getName().equals(endNode) && currentNode.getHops() > 0
                && (onlyExactHops ? currentNode.getHops() == maxHops : currentNode.getHops() <= maxHops)) {
          possiblePaths++;
        }
        for (Map.Entry<GraphNode, Integer> childNode : currentNode.getDependentNodes().values()) {
          // The copy constructor is important because if we keep using the same object the hops information
          // will be updated for all objects of the same reference in the queue.
          // In the copy constructor we are NOT making a deep copy of the dependent array as this would
          // be unnecessary. We are referencing the same HashMap and only copying the name and the hops field.
          // So that we can have multiple nodes of the same type in our queue with different hop values.
          GraphNode newChildNode = new GraphNode(childNode.getKey());
          newChildNode.setHops(currentNode.getHops() + 1);
          nodeQueue.add(newChildNode);
        }
      }
    }
    return possiblePaths;
  }
}
