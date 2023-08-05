package org.example.core.traverse;

import org.example.core.graph.GraphNode;
import org.example.core.graph.GraphService;

import java.util.*;

public class TraverseService {
  private final GraphService graphService;
  private Map<String, GraphNode> lookupMap;

  // We want to make sure that we have at least a collection of paths calculated to make sure we are not choosing
  // a "short" one because of the lack of options.
  private static final int MIN_NUMBER_OF_PATHS_TO_INVESTIGATE = 10;


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
      throw new InvalidInputException("The start and end node can't be empty.");
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

  public Integer findShortestLatencyForPath(String startName, String endName) {
    Map<String, GraphNode> existingPaths = this.findLatencyForPaths(startName, endName, MIN_NUMBER_OF_PATHS_TO_INVESTIGATE, null);
    int minLatency = Integer.MAX_VALUE;
    for(Map.Entry<String, GraphNode> e : existingPaths.entrySet()) {
      if(e.getValue().getLatencySum() < minLatency) {
        minLatency = e.getValue().getLatencySum();
      }
    }
    return minLatency;
  }

  public Integer findAllPathsWithLessThanLatency(String startName, String endName, int maxLatencyIncluding) {
    Map<String, GraphNode> existingPaths = this.findLatencyForPaths(startName, endName, MIN_NUMBER_OF_PATHS_TO_INVESTIGATE, maxLatencyIncluding);
    return existingPaths.size();
  }

  /**
   * Method to calculate a set of paths for a given start and end.
   * @param startName the start node
   * @param endName the end node
   * @param minPaths the minimum amount of paths to calculate (can be more because it does not end before all nodes were visited).
   * @return the different paths.
   */
  public Map<String, GraphNode> findLatencyForPaths(String startName, String endName, int minPaths, Integer maxLatency) {
    if(startName == null || endName == null) {
      throw new InvalidInputException("The start and end node can't be empty.");
    }
    GraphNode gn = this.lookupMap.get(startName);
    gn.setLatencySum(0);
    Queue<GraphNode> nodeQueue = new LinkedList<>();
    nodeQueue.add(gn);
    Map<String, GraphNode> existingPaths = new HashMap<>();
    Set<String> visited = new HashSet<>();
    int iteration = 0;
    int lastIterationAddedNewVisitNode = 0;
    while (!nodeQueue.isEmpty()) {
      GraphNode currentNode = nodeQueue.poll();
      if(currentNode.getPreviousPath().isEmpty()) {
        currentNode.addToPreviousPath(null, currentNode.getName());
      }
      iteration++;
      if (!visited.contains(currentNode.getName())) {
        visited.add(currentNode.getName());
        lastIterationAddedNewVisitNode = iteration;
      }
      if(maxLatency == null || maxLatency > currentNode.getLatencySum()) {
        if (currentNode.getName().equals(endName) && currentNode.getLatencySum() > 0) {
          existingPaths.put(currentNode.previousPathAsString(), currentNode);
        }
        for (Map.Entry<GraphNode, Integer> childNode : currentNode.getDependentNodes().values()) {
          GraphNode newChildNode = new GraphNode(childNode.getKey());
          newChildNode.setLatencySum(currentNode.getLatencySum() + childNode.getValue());
          newChildNode.addToPreviousPath(currentNode.getPreviousPath(), newChildNode.getName());
          nodeQueue.add(newChildNode);
        }
      }
      if(existingPaths.size() >= minPaths && iteration > lastIterationAddedNewVisitNode + 20) {
        break; // Finding out if we really went all possible paths is not trivial.
        // As a visited all does not mean we tried all permutations of the nodes.
        // Neither does the first found path necessarily represent the fastest path.
        // As a compromise I decided to wait at least to have X paths and check if any new node was added in the last 20 iterations.
        // So if we have a graph like this A15->B15->C & B15->A & A1->D1->E1->F1->G1->H1->I1->J1->K1->L1->M1->N1->O1->P1->Q1->R1->S1->T1->U1->V1->C
        // In this example we would generate plenty of paths like A->B->C (30 cost) Or A->B->A->B->C (60 cost) etc.
        // But the cheapest way would not easily be found as the BFS algorithm slowly follows the long path.
        // As we would keep adding new nodes to the visited set we would also increase the lastIterationAddedNewVisitNode
        // which would prevent us from exiting too early. Please refer to the unit tests to validate this scenario.
      }
    }
    return existingPaths;
  }
}
