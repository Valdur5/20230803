package org.example.details;

import org.example.core.graph.GraphTuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import java.util.List;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GraphFileReaderTest {

  @Spy @InjectMocks private GraphFileReader graphFileReader;

  @Test
  void test_getGraphTuplesForFile_emptyList_exception() {
    // Arrange
    doReturn(new ArrayList<>()).when(this.graphFileReader).readFileContentAsList(anyString());
    // Act
    Assertions.assertThrows(MalformedInputFormatException.class, () -> this.graphFileReader.getGraphTuplesForFile("anyString"));
  }

  @Test
  void test_getGraphTuplesForFile_listLikeExample_validList() {
    // Arrange
    doReturn(new ArrayList<>(List.of("AB1")))
        .when(this.graphFileReader)
        .readFileContentAsList(anyString());
    // Act
    List<GraphTuple> graphTuples = this.graphFileReader.getGraphTuplesForFile("anyString");
    // Assert
    assertEquals(1, graphTuples.size());
    assertEquals("A", graphTuples.get(0).parentName());
    assertEquals("B", graphTuples.get(0).childName());
    assertEquals(1, graphTuples.get(0).latency());
  }

  @Test
  void test_getGraphTuplesForFile_latencyBiggerThan9_validList() {
    // Arrange
    doReturn(new ArrayList<>(List.of("AB10")))
        .when(this.graphFileReader)
        .readFileContentAsList(anyString());
    // Act
    List<GraphTuple> graphTuples = this.graphFileReader.getGraphTuplesForFile("anyString");
    // Assert
    assertEquals(1, graphTuples.size());
    assertEquals("A", graphTuples.get(0).parentName());
    assertEquals("B", graphTuples.get(0).childName());
    assertEquals(10, graphTuples.get(0).latency());
  }

  @Test
  void test_getGraphTuplesForFile_toManyCharactersInName_exception() {
    // Arrange
    doReturn(new ArrayList<>(List.of("AAB1")))
        .when(this.graphFileReader)
        .readFileContentAsList(anyString());
    // Act
    Assertions.assertThrows(
        MalformedInputFormatException.class,
        () -> this.graphFileReader.getGraphTuplesForFile("anyString"));
  }

  @Test
  void test_getGraphTuplesForFile_multiple_validList() {
    // Arrange
    doReturn(new ArrayList<>(List.of("AB10", "BC9", "CA1200")))
            .when(this.graphFileReader)
            .readFileContentAsList(anyString());
    // Act
    List<GraphTuple> graphTuples = this.graphFileReader.getGraphTuplesForFile("anyString");
    // Assert
    assertEquals(3, graphTuples.size());
    assertEquals("A", graphTuples.get(0).parentName());
    assertEquals("B", graphTuples.get(0).childName());
    assertEquals(10, graphTuples.get(0).latency());
    assertEquals("B", graphTuples.get(1).parentName());
    assertEquals("C", graphTuples.get(1).childName());
    assertEquals(9, graphTuples.get(1).latency());
    assertEquals("C", graphTuples.get(2).parentName());
    assertEquals("A", graphTuples.get(2).childName());
    assertEquals(1200, graphTuples.get(2).latency());
  }
}
