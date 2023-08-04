package org.example;

import org.example.core.graph.GraphReader;
import org.example.core.graph.GraphService;
import org.example.core.graph.GraphServiceImpl;
import org.example.core.traverse.NoTraceFoundException;
import org.example.core.traverse.TraverseService;
import org.example.details.GraphFileReader;

import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String NO_SUCH_TRACE = "NO SUCH TRACE";
    private static final String DEFAULT_GRAPH_DATA = "src/main/resources/dependencyGraph.txt";

    private final GraphReader graphReader = new GraphFileReader();
    private final GraphService graphService = new GraphServiceImpl(graphReader);
    private final TraverseService traverseService = new TraverseService(graphService);

    public static void main(String[] args) {
        // Please keep in mind that we are not working with any DPI frameworks etc.
        // I consolidated all concrete implementations in the Main which makes the Main class
        // look a bit messy. The rest of the classes are structured properly.
        // Classes in the core package are the central logic they do not depend on the input or output
        // format needed to make this use case running.
        // Classes in the details package are more concrete implementations for example how to read exactly.
        // The given file format. There should be NO dependency to the core package from the details package.
        Main main = new Main();
        main.initGraphDialog();
        main.calculateAndPrintAverageLatency(1, List.of("A", "B","C"));
        main.calculateAndPrintAverageLatency(2, List.of("A", "D"));
        main.calculateAndPrintAverageLatency(3, List.of("A", "D", "C"));
        main.calculateAndPrintAverageLatency(4, List.of("A", "E", "B", "C", "D"));
        main.calculateAndPrintAverageLatency(5, List.of("A", "E", "D"));
        main.calculateAndPrintNumberOfTracesWithHops(6, "C", "C", 3, false);
        main.calculateAndPrintNumberOfTracesWithHops(6, "A", "C", 4, true);

    }

    private void initGraphDialog() {
        System.out.println("In case you want to load another file than the default ["+DEFAULT_GRAPH_DATA+"].");
        System.out.println("Please enter the absolute path and file name of the file with the graph definition.");
        System.out.println("Otherwise just hit enter.");
        Scanner s = new Scanner(System.in);
        String value = s.nextLine();
        this.traverseService.initGraph(value != null && !value.isEmpty() ? value : DEFAULT_GRAPH_DATA);
    }

    private void calculateAndPrintNumberOfTracesWithHops(int index, String startName, String endName, int maxHops, boolean onlyExactHops) {
        String preFix = index + ". ";
        System.out.println(preFix + this.traverseService.findNumberOfPossibleTraces(startName, endName, maxHops, onlyExactHops));
    }

    private void calculateAndPrintAverageLatency(int index, List<String> path) {
        String preFix = index + ". ";
        try {
            System.out.println(preFix + traverseService.averageLatencyOfPath(path));
        } catch (NoTraceFoundException e) {
            System.out.println(preFix + NO_SUCH_TRACE);
        }
    }
}