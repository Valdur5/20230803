package org.example.core.graph;

import org.example.details.MalformedInputFormatException;

import java.util.List;

public interface GraphReader {
    /**
     * Reads the content of a file and returns a list of GraphTuples which contain the information about
     * the graph nodes.
     * @param filePathAndName the path and name all in one argument.
     * @return the list of GraphTuple
     * @throws MalformedInputFormatException in case the file is empty or not in the right format.
     */
    List<GraphTuple> getGraphTuplesForFile(String filePathAndName);
}
