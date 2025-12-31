package test;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.pathfinder.FileGraphLoader;
import software.ulpgc.pathfinder.GraphContainer;
import software.ulpgc.pathfinder.GraphLoader;
import software.ulpgc.pathfinder.FileGraphLoader.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FullCoverageTest {
    private GraphContainer graphContainer;

    @BeforeEach
    void setUp() {
        Graph<String, DefaultEdge> graph = new SimpleWeightedGraph<>(DefaultEdge.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addEdge("A", "B");
        graph.setEdgeWeight("A", "B", 1.0);
        graph.addEdge("B", "C");
        graph.setEdgeWeight("B", "C", 2.0);
        graphContainer = new GraphContainer(graph);
    }

    // ================= GraphContainer =================
    @Test
    void testShortestPathNormal() {
        List<String> path = graphContainer.shortestPathBetween("A", "C");
        assertEquals(List.of("A", "B", "C"), path);
    }

    @Test
    void testPathWeight() {
        double weight = graphContainer.pathWeightBetween("A", "C");
        assertEquals(3.0, weight);
    }

    @Test
    void testShortestPathNonExistentVertex() {
        assertThrows(IllegalArgumentException.class,
                () -> graphContainer.shortestPathBetween("A", "Z"));
        assertThrows(IllegalArgumentException.class,
                () -> graphContainer.pathWeightBetween("X", "C"));
    }

    // ================= FileGraphLoader =================
    @Test
    void testFileGraphLoaderValidFile() throws IOException {
        File tempFile = File.createTempFile("graph", ".txt");
        tempFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("A,B,1.0\nB,C,2.0\n");
        }

        GraphLoader loader = new FileGraphLoader(tempFile);
        GraphContainer container = loader.load();

        List<String> path = container.shortestPathBetween("A", "C");
        assertEquals(List.of("A", "B", "C"), path);
    }

    @Test
    void testFileGraphLoaderInvalidLine() throws IOException {
        File tempFile = File.createTempFile("graph", ".txt");
        tempFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("A,B,1.0\nINVALID_LINE\nB,C,2.0\n");
        }

        GraphLoader loader = new FileGraphLoader(tempFile);
        assertDoesNotThrow(loader::load); // No debe fallar, solo imprime error
    }

    @Test
    void testFileGraphLoaderInvalidWeight() throws IOException {
        File tempFile = File.createTempFile("graph", ".txt");
        tempFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("A,B,NOT_A_NUMBER\n");
        }

        GraphLoader loader = new FileGraphLoader(tempFile);
        assertDoesNotThrow(loader::load); // Captura ParseException internamente
    }

    @Test
    void testFileGraphLoaderEmptyFile() throws IOException {
        File tempFile = File.createTempFile("graph", ".txt");
        tempFile.deleteOnExit();

        GraphLoader loader = new FileGraphLoader(tempFile);
        GraphContainer container = loader.load();
        assertThrows(IllegalArgumentException.class,
                () -> container.shortestPathBetween("A", "B"));
    }
}
