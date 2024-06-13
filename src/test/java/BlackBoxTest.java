import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.jupiter.api.Test;
import org.lab1.GraphGui;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlackBoxTest {
    GraphGui graphGui = new GraphGui();

    /**
     * graph!=null，inputText=“new to”
     */
    @Test
    public void testGenerateNewTextCase1() {
        Graph<String, DefaultWeightedEdge> graph = graphGui.createDirectedGraph("C:\\Users\\49552\\Documents\\temp\\txt\\1.txt");
        graphGui.setGraph(graph);
        String inputText = "new to";
        Set<String> expectedOutputs = new HashSet<>();
        expectedOutputs.add("new life to");
        expectedOutputs.add("new civilizations to");
        expectedOutputs.add("new worlds to");
        expectedOutputs.add("new adventures to");
        expectedOutputs.add("new and to");
        expectedOutputs.add("new places to");
        String actualOutput = graphGui.generateNewText(graph, inputText);
        assertTrue(expectedOutputs.contains(actualOutput), "Actual output: " + actualOutput);
    }
    @Test
    public void testGenerateNewTextCase2() {
        Graph<String, DefaultWeightedEdge> graph =
                graphGui.createDirectedGraph("C:\\Users\\49552\\Documents\\temp\\txt\\1.txt");
        graphGui.setGraph(graph);
        String inputText = "hello world!";
        String expectedOutputs ="hello world";
        String actualOutput = graphGui.generateNewText(null, inputText);
        assertEquals(expectedOutputs, actualOutput);
    }

    @Test
    public void testGenerateNewTextCase3() {
        Graph<String, DefaultWeightedEdge> graph = graphGui.createDirectedGraph("C:\\Users\\49552\\Documents\\temp\\txt\\1.txt");
        graphGui.setGraph(graph);
        String inputText = "It is new8 to these strange worlds!";
        Set<String> expectedOutputs = new HashSet<>();
        expectedOutputs.add("It is new life to these strange new worlds");
        expectedOutputs.add("It is new civilizations to these strange new worlds");
        expectedOutputs.add("It is new worlds to these strange new worlds");
        expectedOutputs.add("It is new adventures to these strange new worlds");
        expectedOutputs.add("It is new and to these strange new worlds");
        expectedOutputs.add("It is new places to these strange new worlds");
        String actualOutput = graphGui.generateNewText(graph, inputText);
        assertTrue(expectedOutputs.contains(actualOutput), "Actual output: " + actualOutput);
    }

    @Test
    public void testGenerateNewTextCase4() {
        Graph<String, DefaultWeightedEdge> graph =
                graphGui.createDirectedGraph(
                        "C:\\Users\\49552\\Documents\\temp\\txt\\1.txt");
        graphGui.setGraph(graph);
        String inputText = "new";
        String expectedOutput = "new";
        assertEquals(expectedOutput, graphGui.generateNewText(graph, inputText));
    }

    @Test
    public void testGenerateNewTextCase5() {
        Graph<String, DefaultWeightedEdge> graph = null;
        String inputText = "It is new";
        String expectedOutput = "It is new";
        assertEquals(expectedOutput, graphGui.generateNewText(graph, inputText));
    }

}
