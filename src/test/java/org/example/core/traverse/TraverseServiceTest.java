package org.example.core.traverse;

import org.example.core.graph.GraphNode;
import org.example.core.graph.GraphService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraverseServiceTest {
    @InjectMocks private TraverseService traverseService;

    @Mock private GraphService graphService;

    @Test
    void test_initGraph_notSetYet_initialized() throws IllegalAccessException {
        // Arrange
        String filePath = "test/test.txt";
        GraphNode graphNode = new GraphNode("A");
        Map<String, GraphNode> lookupMap = new HashMap<>();
        lookupMap.put("A", graphNode);
        doReturn(lookupMap).when(this.graphService).constructGraphFromFile(filePath);

        Field lookupMapField = ReflectionUtils
                .findFields(TraverseService.class, f -> f.getName().equals("lookupMap"),
                        ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
                .get(0);
        lookupMapField.setAccessible(true);
        lookupMapField.set(this.traverseService, null);

        // Act
        this.traverseService.initGraph(filePath);

        // Assert
        verify(this.graphService, times(1)).constructGraphFromFile(anyString());
        assertEquals(lookupMap, lookupMapField.get(this.traverseService));
    }

    @Test
    void test_initGraph_setAlready_doNothing() throws IllegalAccessException {
        // Arrange
        String filePath = "test/test.txt";
        Field lookupMapField = ReflectionUtils
                .findFields(TraverseService.class, f -> f.getName().equals("lookupMap"),
                        ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
                .get(0);
        lookupMapField.setAccessible(true);
        lookupMapField.set(this.traverseService, new HashMap<>());

        // Act
        this.traverseService.initGraph(filePath);

        // Assert
        verify(this.graphService, times(0)).constructGraphFromFile(anyString());
    }

    @Test
    void test_averageLatencyOfPath_validPath_returnAverageLatency() throws IllegalAccessException {
        // Arrange
        GraphNode graphNodeA = new GraphNode("A");
        GraphNode graphNodeB = new GraphNode("B");
        GraphNode graphNodeC = new GraphNode("C");
        Map<String, GraphNode> lookupMap = new HashMap<>();
        lookupMap.put("A", graphNodeA);
        lookupMap.put("B", graphNodeB);
        lookupMap.put("C", graphNodeC);
        graphNodeA.addDependentNode(graphNodeB, 4);
        graphNodeB.addDependentNode(graphNodeC, 6);
        Field lookupMapField = ReflectionUtils
                .findFields(TraverseService.class, f -> f.getName().equals("lookupMap"),
                        ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
                .get(0);
        lookupMapField.setAccessible(true);
        lookupMapField.set(this.traverseService, lookupMap);
        // Act
        int averageLatency = this.traverseService.averageLatencyOfPath(List.of("A","B","C"));
        // Assert
        assertEquals(10, averageLatency);
    }

    @Test
    void test_averageLatencyOfPath_noPathFound_exception() throws IllegalAccessException {
        // Arrange
        GraphNode graphNodeA = new GraphNode("A");
        GraphNode graphNodeB = new GraphNode("B");
        GraphNode graphNodeC = new GraphNode("C");
        Map<String, GraphNode> lookupMap = new HashMap<>();
        lookupMap.put("A", graphNodeA);
        lookupMap.put("B", graphNodeB);
        lookupMap.put("C", graphNodeC);
        graphNodeA.addDependentNode(graphNodeB, 4);
        graphNodeB.addDependentNode(graphNodeC, 6);
        Field lookupMapField = ReflectionUtils
                .findFields(TraverseService.class, f -> f.getName().equals("lookupMap"),
                        ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
                .get(0);
        lookupMapField.setAccessible(true);
        lookupMapField.set(this.traverseService, lookupMap);
        // Act
        Assertions.assertThrows(NoTraceFoundException.class, () -> this.traverseService.averageLatencyOfPath(List.of("A","B","D")));
    }
}
