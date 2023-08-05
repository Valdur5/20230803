package org.example.core.traverse;

import org.example.core.graph.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
        prepareSimpleGraph();
        // Act
        int averageLatency = this.traverseService.averageLatencyOfPath(List.of("A","B","C"));
        // Assert
        assertEquals(10, averageLatency);
    }

    @Test
    void test_averageLatencyOfPath_noPathFound_exception() throws IllegalAccessException {
        // Arrange
        prepareSimpleGraph();
        // Act
        Assertions.assertThrows(NoTraceFoundException.class, () -> this.traverseService.averageLatencyOfPath(List.of("A","B","D")));
    }

    @Mock private GraphReader graphReader;
    @Test
    void test_findLatencyForPaths_obviousChoiceIsBad_findNotObviousChoice() throws IllegalAccessException {
        List<GraphTuple> graphTuples = new ArrayList<>();
        fillWithLongSideGraphUseCase(graphTuples);
        doReturn(graphTuples).when(this.graphReader).getGraphTuplesForFile(anyString());
        GraphService gs = new GraphServiceImpl(this.graphReader);

        Field lookupMapField = ReflectionUtils
                .findFields(TraverseService.class, f -> f.getName().equals("lookupMap"),
                        ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
                .get(0);
        lookupMapField.setAccessible(true);
        lookupMapField.set(this.traverseService, gs.constructGraphFromFile("anyString"));

        Map<String, GraphNode> paths = this.traverseService.findLatencyForPaths("A","C",5, null);
        // Assert
        assertEquals(11, paths.size());
        assertEquals(20, paths.get("A-D-E-F-G-H-I-J-K-L-M-N-O-P-Q-R-S-T-U-V-C-").getLatencySum());
    }

    @Test
    void test_findShortestLatencyForPath_obviousChoiceIsBad_findNotObviousChoice() throws IllegalAccessException {
        List<GraphTuple> graphTuples = new ArrayList<>();
        fillWithLongSideGraphUseCase(graphTuples);
        doReturn(graphTuples).when(this.graphReader).getGraphTuplesForFile(anyString());
        GraphService gs = new GraphServiceImpl(this.graphReader);

        Field lookupMapField = ReflectionUtils
                .findFields(TraverseService.class, f -> f.getName().equals("lookupMap"),
                        ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
                .get(0);
        lookupMapField.setAccessible(true);
        lookupMapField.set(this.traverseService, gs.constructGraphFromFile("anyString"));

        int shortestPath = this.traverseService.findShortestLatencyForPath("A","C");
        // Assert
        assertEquals(20, shortestPath);
    }

    private static void fillWithLongSideGraphUseCase(List<GraphTuple> graphTuples) {
        graphTuples.add(new GraphTuple("A","B",15));
        graphTuples.add(new GraphTuple("B","C",15));
        graphTuples.add(new GraphTuple("B","A",15));
        graphTuples.add(new GraphTuple("A","D",1));
        graphTuples.add(new GraphTuple("D","E",1));
        graphTuples.add(new GraphTuple("E","F",1));
        graphTuples.add(new GraphTuple("F","G",1));
        graphTuples.add(new GraphTuple("G","H",1));
        graphTuples.add(new GraphTuple("H","I",1));
        graphTuples.add(new GraphTuple("I","J",1));
        graphTuples.add(new GraphTuple("J","K",1));
        graphTuples.add(new GraphTuple("K","L",1));
        graphTuples.add(new GraphTuple("L","M",1));
        graphTuples.add(new GraphTuple("M","N",1));
        graphTuples.add(new GraphTuple("N","O",1));
        graphTuples.add(new GraphTuple("O","P",1));
        graphTuples.add(new GraphTuple("P","Q",1));
        graphTuples.add(new GraphTuple("Q","R",1));
        graphTuples.add(new GraphTuple("R","S",1));
        graphTuples.add(new GraphTuple("S","T",1));
        graphTuples.add(new GraphTuple("T","U",1));
        graphTuples.add(new GraphTuple("U","V",1));
        graphTuples.add(new GraphTuple("V","C",1));
    }

    @Test
    void test_findNumberOfPossibleTraces_simpleGraphNoCycles_justOneWay() throws IllegalAccessException {
        // Arrange
        prepareSimpleGraph();
        // Act
        int result = this.traverseService.findNumberOfPossibleTraces("A","C", 3, false);
        // Assert
        assertEquals(1, result);
    }

    @Test
    void test_findNumberOfPossibleTraces_simpleGraphNoCyclesOnlyExactHopMatches_nothingFound() throws IllegalAccessException {
        // Arrange
        prepareSimpleGraph();
        // Act
        int result = this.traverseService.findNumberOfPossibleTraces("A","C", 3, true);
        // Assert
        assertEquals(0, result);
    }

    private void prepareSimpleGraph() throws IllegalAccessException {
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
    }
}
