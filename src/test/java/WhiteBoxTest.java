import static org.junit.jupiter.api.Assertions.assertEquals;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lab1.GraphGui;

public class WhiteBoxTest {
    private Graph<String, DefaultWeightedEdge> graph;
    private GraphGui graphGui;
    @BeforeEach
    public void setUp(){
        graphGui=new GraphGui();
        Graph<String, DefaultWeightedEdge> graph = graphGui.createDirectedGraph("C:\\Users\\49552\\Documents\\temp\\txt\\1.txt");
        graphGui.setGraph(graph);
        this.graph=graph;
    }
    @Test
    public void testCase1() {
        Graph<String, DefaultWeightedEdge> nullGraph = null;
        String result = graphGui.queryBridgeWords(nullGraph, "www", "iii");
        assertEquals("No \"www\" and \"iii\" in the graph!", result);
    }

    @Test
    public void testCase2() {
        String result = graphGui.queryBridgeWords(graph, "worlds", "iii");
        assertEquals("No \"iii\" in the graph!", result);
    }

    @Test
    public void testCase3() {
        String result = graphGui.queryBridgeWords(graph, "iii", "worlds");
        assertEquals("No \"iii\" in the graph!", result);
    }

    @Test
    public void testCase4() {
        String result = graphGui.queryBridgeWords(graph, "end", "seek");
        assertEquals("No bridge words from \"end\" to \"seek\"!", result);
    }

    @Test
    public void testCase5() {
        String result = graphGui.queryBridgeWords(graph, "places", "strange");
        assertEquals("No bridge words from \"places\" to \"strange\"!", result);
    }

    @Test
    public void testCase6() {
        String result = graphGui.queryBridgeWords(graph, "seek", "to");
        assertEquals("No bridge words from \"seek\" to \"to\"!", result);
    }

    @Test
    public void testCase7() {
        String result = graphGui.queryBridgeWords(graph, "and", "end");
        assertEquals("The bridge words from \"and\" to \"end\" is:seek", result);
    }

    @Test
    public void testCase8() {
        String result = graphGui.queryBridgeWords(graph, "new", "explore");
        assertEquals("The bridge words from \"new\" to \"explore\" is:and", result);
    }

    @Test
    public void testCase9() {
        String result = graphGui.queryBridgeWords(graph, "new", "to");
        assertEquals("The bridge words from \"new\" to \"to\" are:life, worlds, civilizations, adventures, and, places", result);
    }

    @Test
    public void testCase10() {
        String result = graphGui.queryBridgeWords(graph, "and", "new");
        assertEquals("The bridge words from \"and\" to \"new\" are:seek, explore", result);
    }



}
