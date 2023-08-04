package org.example.core.graph;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphServiceImplTest {

  @InjectMocks private GraphServiceImpl graphServiceImpl;

  @Mock private GraphReader graphReader;

  @Test
  void test_constructTreeNodeFromFile_verySimpleExample_valid() {
    // Arrange
    List<GraphTuple> gt = new ArrayList<>();
    gt.add(new GraphTuple("A", "B", 4));
    doReturn(gt).when(this.graphReader).getGraphTuplesForFile(anyString());

    // Act
    Map.Entry<GraphNode, Map<String, GraphNode>> gA = this.graphServiceImpl.constructGraphFromFile("anyFilePath/AndName.txt");
    // Assert
    test(gA.getKey(), "A", List.of("B"), List.of(4));
  }

  @Test
  void test_constructTreeNodeFromFile_circularDependency_valid() {
    // Arrange
    List<GraphTuple> gt = new ArrayList<>();
    gt.add(new GraphTuple("A", "B", 4));
    gt.add(new GraphTuple("B", "A", 5));
    doReturn(gt).when(this.graphReader).getGraphTuplesForFile(anyString());
    // Act
    Map.Entry<GraphNode, Map<String, GraphNode>> gA = this.graphServiceImpl.constructGraphFromFile("anyFilePath/AndName.txt");
    // Assert
    test(gA.getKey(), "A", List.of("B"), List.of(4));
    test(gA.getKey().getDependentTreeNodeByName("B").getKey(), "B", List.of("A"), List.of(5));
  }

  @Test
  void test_constructTreeNodeFromFile_taskExample_valid() {
    // Arrange
    List<GraphTuple> gt = new ArrayList<>();
    gt.add(new GraphTuple("A", "B", 5));
    gt.add(new GraphTuple("B", "C", 4));
    gt.add(new GraphTuple("C", "D", 8));
    gt.add(new GraphTuple("D", "C", 8));
    gt.add(new GraphTuple("D", "E", 6));
    gt.add(new GraphTuple("A", "D", 5));
    gt.add(new GraphTuple("C", "E", 2));
    gt.add(new GraphTuple("E", "B", 3));
    gt.add(new GraphTuple("A", "E", 7));
    doReturn(gt).when(this.graphReader).getGraphTuplesForFile(anyString());
    // Act
    Map.Entry<GraphNode, Map<String, GraphNode>> gA = this.graphServiceImpl.constructGraphFromFile("anyFilePath/AndName.txt");
    // Assert
    test(gA.getKey(), "A", List.of("B", "D", "E"), List.of(5, 5, 7));
    GraphNode gB = gA.getKey().getDependentTreeNodeByName("B").getKey();
    test(gB, "B", List.of("C"), List.of(4));
    GraphNode gC = gB.getDependentTreeNodeByName("C").getKey();
    test(gC, "C", List.of("D", "E"), List.of(8, 2));
    GraphNode gD = gC.getDependentTreeNodeByName("D").getKey();
    test(gD, "D", List.of("C", "E"), List.of(8, 6));
    GraphNode gE = gD.getDependentTreeNodeByName("E").getKey();
    test(gE, "E", List.of("B"), List.of(3));
  }

  void test(GraphNode gn, String parentName, List<String> childNames, List<Integer> latencies) {
    assertEquals(childNames.size(), latencies.size(), "The two lists need to be equally long.");
    assertEquals(parentName, gn.getName());
    assertEquals(gn.dependentNodeCount(), childNames.size());
    for (int i = 0; i < childNames.size(); i++) {
      Map.Entry<GraphNode, Integer> childNode = gn.getDependentTreeNodeByName(childNames.get(i));
      assertEquals(childNode.getKey().getName(), childNames.get(i));
      assertEquals(childNode.getValue(), latencies.get(i));
    }
  }
}
