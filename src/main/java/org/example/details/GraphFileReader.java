package org.example.details;

import org.example.core.graph.GraphReader;
import org.example.core.graph.GraphTuple;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Here are all the details about the file format contained.
 * If we want to switch the format in how the graph nodes are stored
 * we just have to touch this file.
 */
public class GraphFileReader implements GraphReader {

    @Override
    public List<GraphTuple> getGraphTuplesForFile(String fileNameAndPath) {
        List<String> graphTuplesAsString = this.readFileContentAsList(fileNameAndPath);
        List<GraphTuple> graphTuples = new ArrayList<>();
        for(String s : graphTuplesAsString) {
            if(s == null || s.length() < 3 || !Character.isDigit(s.charAt(2))) {
                throw new MalformedInputFormatException();
            }
            GraphTuple graphTuple = new GraphTuple(
                    String.valueOf(s.charAt(0)),
                    String.valueOf(s.charAt(1)),
                    Integer.valueOf(s.substring(2))
            );
            graphTuples.add(graphTuple);
        }
        return graphTuples;
    }

    List<String> readFileContentAsList(String fileNameAndPath) {
        try {
            Path filePath = Path.of(fileNameAndPath);
            String content = Files.readString(filePath);
            return new ArrayList<>(List.of(content.split(", ")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
