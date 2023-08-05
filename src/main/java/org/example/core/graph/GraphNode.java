package org.example.core.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphNode {
  // The current input format would suggest that a Character as data type would here be sufficient.
  // But as we do not want to couple our Graph generation logic to the input format we choose to use
  // a more universal way of storing the name (string).
  private final String name;

  // The key is the name so we can access the dependent nodes fast and easy.
  // The value is the TreeNode itself as well as the edge weight to the dependent
  // node.
  private final Map<String, Map.Entry<GraphNode, Integer>> dependentNodes;

  // This attribute is used for the calculation itself. To keep track how many hops we already did
  // to reach this node.
  private int hops;
  private final List<String> previousPath;
  private int latencySum;


  public List<String> getPreviousPath() {
    return previousPath;
  }

  public String previousPathAsString() {
    StringBuilder sb = new StringBuilder();
    for (String p : previousPath) {
      sb.append(p);
      sb.append("-");
    }
    return sb.toString();
  }

  public void addToPreviousPath(List<String> previousPath, String name) {
      if (previousPath != null) {
          this.previousPath.addAll(previousPath);
      }
      this.previousPath.add(name);
  }

  @Override
  public String toString() {
    return "[" + getName() + " # dependents: " + dependentNodes.size() + " hops: " + hops + " latency: "+latencySum+ " path: " + previousPathAsString() + "]";
  }

  public GraphNode(GraphNode graphNode) {
    this.name = graphNode.name;
    this.hops = graphNode.hops;
    this.latencySum = graphNode.latencySum;
    this.dependentNodes = graphNode.dependentNodes;
    this.previousPath = new ArrayList<>();
  }

  public GraphNode(String name) {
    this.name = name;
    this.dependentNodes = new HashMap<>();
    this.previousPath = new ArrayList<>();
  }

  public void addDependentNode(GraphNode dependentNode, Integer averageTime) {
    this.dependentNodes.put(dependentNode.getName(), Map.entry(dependentNode, averageTime));
  }

  public Map.Entry<GraphNode, Integer> getDependentTreeNodeByName(String name) {
    return dependentNodes.get(name);
  }

  public Map<String, Map.Entry<GraphNode, Integer>> getDependentNodes() {
    return dependentNodes;
  }

  public String getName() {
    return name;
  }

  public int getHops() {
    return hops;
  }

  public void setHops(int hops) {
    this.hops = hops;
  }

  public int getLatencySum() {
    return latencySum;
  }

  public void setLatencySum(int latencySum) {
    this.latencySum = latencySum;
  }
}
