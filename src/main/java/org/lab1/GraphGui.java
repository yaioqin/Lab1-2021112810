package org.lab1;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;



/**
 * GraphGui 类是一个用于显示和与图交互的图形用户界面.
 * 它提供了各种功能，如打开文件、保存图、查询桥接词、根据桥接词生成新文本、找到最短路径、执行随机游走.
 */

public class GraphGui extends JFrame {
    private mxGraphComponent graphComponent; // 获取用于显示图形的图组件
    private JButton openButton; //1.打开txt文件 2.显示有向图
    private JButton saveButton; //保存图
    private JButton queryButton; //3.查询桥接词
    private JButton generateButton; //4.根据相邻单词的桥接词生成新的文本
    private JButton shortestPathButton; //5.最短路径
    private JButton randomWalkButton; //6.随机游走
    private JButton pauseButton; // 6.暂停按钮
    private JButton endButton; // 6.结束按钮
    private Graph<String, DefaultWeightedEdge> graph; // 图
    private Timer colorTimer; // 颜色修改器
    private boolean isPaused = false; // 计时器状态
    private final SecureRandom randomBridgeWord = new SecureRandom(); //用于随机选取出桥接词,增强不可预测性
    //private final Random randomBridgeWord = new Random(); //用于随机选取出桥接词 改bad practice时用

    /**
     * 构造函数.
     * 可在这里面绑定每个按钮的触发事件.
     */
    public GraphGui() {
        openButton = new JButton("Open File");
        openButton.addActionListener(new ActionListener() {
            /**
             功能1和2.
             * 读入文本并生成有向图并展示有向图
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePauseTimer();
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    createDirectedGraph(selectedFile);
                }
            }
        });

        saveButton = new JButton("Save As Image");
        saveButton.addActionListener(new ActionListener() {
            /**
             * 可选功能：将生成的有向图以图形文件形式保存到磁盘.

             * @param e the event to be processed.
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                //防止不导图乱按
                if (graph == null || graph.vertexSet().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "还未导入图，请先导入再保存。", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                togglePauseTimer();
                saveAsImage();
            }
        });

        queryButton = new JButton("Query Bridge Words");
        JTextField word1Field = new JTextField(10);
        JTextField word2Field = new JTextField(10);
        queryButton.addActionListener(new ActionListener() {
            /**
             * 功能需求3：查询桥接词（bridge words）.

             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                //防止不导图乱按
                if (graph == null || graph.vertexSet().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "图为空，无法查询桥接词。", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                togglePauseTimer();
                String word1 = word1Field.getText().trim().toLowerCase();
                String word2 = word2Field.getText().trim().toLowerCase();
                String result = queryBridgeWords(graph,word1, word2);
                JOptionPane.showMessageDialog(GraphGui.this, result,
                        "Bridge Words Query Result", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        generateButton = new JButton("Generate New Text");
        JTextArea inputTextArea = new JTextArea(5, 20);
        JTextArea outputTextArea = new JTextArea(5, 20);

        // 启用自动换行并以单词为单位换行
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        outputTextArea.setLineWrap(true);
        outputTextArea.setWrapStyleWord(true);

        generateButton.addActionListener(new ActionListener() {
            /**
             *功能需求4：根据bridge word生成新文本.

             * @param e the event to be processed.
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                //防止不导图乱按
                if (graph == null || graph.vertexSet().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "图为空，无法生成新文本。", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                togglePauseTimer();
                String inputText = inputTextArea.getText();
                String s = generateNewText(graph,inputText);
                outputTextArea.setText(s);
            }
        });

        shortestPathButton = new JButton("Shortest Path");
        shortestPathButton.addActionListener(new ActionListener() {
            /**
             * 5.最短路径.

             * @param e the event to be processed
             * */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graph == null || graph.vertexSet().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "图为空，无法计算最短路径。", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                togglePauseTimer();
                String input = JOptionPane.showInputDialog(null,
                        "Enter one or two words  separated by space:");
                if (input != null && !input.trim().isEmpty()) {
                    String[] words = input.trim().split("\\s+");
                    if (words.length == 2) {
                        String paths = calcShortestPath(graph,words[0], words[1]);
                        // 找到最后一个@符号的索引
                        int lastIndex = paths.lastIndexOf('@');
                        // 分割字符串
                        String message = paths.substring(lastIndex + 1);
                        message = message.replace(",", "->");
                        paths = paths.substring(0, lastIndex);
                        if (!paths.equals("Infinity;")) {
                            showShortestPaths(paths);
                            javax.swing.JOptionPane.showMessageDialog(null, message);
                        } else {
                            message = "没有从" + words[0] + "到" + words[1] + "的路径！";
                            javax.swing.JOptionPane.showMessageDialog(null, message);
                        }
                    } else if (words.length == 1) {
                        // 遍历图中的节点，将words[0]作为起点，调用calcShortestPath
                        String startNode = words[0];
                        // StringBuilder allPaths = new StringBuilder();
                        for (String vertex : graph.vertexSet()) {
                            if (!vertex.equals(startNode)) {
                                String paths = calcShortestPath(graph,startNode, vertex);
                                // 找到最后一个@符号的索引
                                int lastIndex = paths.lastIndexOf('@');
                                // 分割字符串
                                String message = paths.substring(lastIndex + 1);
                                message = message.replace(",", "->");
                                paths = paths.substring(0, lastIndex);
                                if (!paths.equals("Infinity;")) {
                                    showShortestPaths(paths);
                                    javax.swing.JOptionPane.showMessageDialog(null, message);
                                } else {
                                    message = "没有从" + startNode + "到" + vertex + "的路径！";
                                    javax.swing.JOptionPane.showMessageDialog(null, message);
                                }
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "请输入一个或两个单词，以空格隔开");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "请输入一个或两个单词，以空格隔开");
                }
            }
        });
        // 构造函数中初始化按钮
        randomWalkButton = new JButton("Random Walk");
        randomWalkButton.addActionListener(new ActionListener() {
            /**
             * 6.随机游走.

             * @param e the event to be processed
             * */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graph == null || graph.vertexSet().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "图为空，无法执行随机游走。", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                togglePauseTimer();
                String randompath = randomWalk();
                showRandomPath(randompath);
            }
        });

        //暂停随机游走按钮
        pauseButton = new JButton("Pause Random Walk");
        pauseButton.addActionListener(new ActionListener() {
            /**
             * 6.随机游走的暂停/继续按钮.

             * @param e the event to be processed
             * */
            @Override
            public void actionPerformed(ActionEvent e) {

                if (colorTimer != null) {
                    if (isPaused) {
                        colorTimer.start();
                        pauseButton.setText("Pause Random Walk");
                    } else {
                        colorTimer.stop();
                        pauseButton.setText("Resume Random Walk");
                    }
                    isPaused = !isPaused;
                }
            }
        });

        //结束随机游走按钮
        endButton = new JButton("End Random Walk");
        endButton.addActionListener(new ActionListener() {
            /**
             * 6.随机游走的结束按钮.

             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean nullFlag = false;
                if (colorTimer != null) {
                    nullFlag = true;
                }
                togglePauseTimer();
                if (nullFlag) {
                    int confirmResult = JOptionPane.showConfirmDialog(null,
                            "结束游走成功，需要保存随机游走路径吗?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (confirmResult == JOptionPane.NO_OPTION) {
                        return; // 如果用户选择不保存，直接返回
                    } else if (confirmResult == JOptionPane.YES_OPTION) {
                        // 用户选择保存，弹出文件选择对话框
                        JFileChooser fileChooser = new JFileChooser();
                        int result = fileChooser.showSaveDialog(null);
                        if (result != JFileChooser.APPROVE_OPTION) {
                            return; // 如果用户未选择文件，直接返回
                        }
                        // 获取用户选择的文件
                        File selectedFile = fileChooser.getSelectedFile();
                        // 清空文件内容  添加, StandardCharsets.UTF_8指定字符集解决spotsbug问题
                        //try (BufferedWriter clearWriter = new BufferedWriter(new FileWriter(selectedFile))) {
                        try (BufferedWriter clearWriter = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(selectedFile), StandardCharsets.UTF_8))) {
                            clearWriter.write(""); // 清空文件内容
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null,
                                    "Failed to clear file content.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        // 复制文件内容
                        // 也是指定字符集的问题
                        //try (BufferedReader reader = new BufferedReader(new FileReader("tmpshort.txt"));
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                                new FileInputStream("tmpshort.txt"), StandardCharsets.UTF_8));
                             //也是指定UTF-8
                             //BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                                     new FileOutputStream(selectedFile), StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                writer.write(line);
                                writer.newLine();
                            }
                            JOptionPane.showMessageDialog(null,
                                    "保存成功", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null,
                                    "Failed to copy file content.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }

                togglePauseTimer();
            }
        });
        JPanel panel = new JPanel();
        panel.add(openButton);
        panel.add(saveButton);
        panel.add(shortestPathButton);
        panel.add(randomWalkButton);
        panel.add(pauseButton);
        panel.add(endButton);
        final JPanel RightPanel = new JPanel();
        RightPanel.add(new JLabel("Word 1: "));
        RightPanel.add(word1Field);
        RightPanel.add(new JLabel("Word 2: "));
        RightPanel.add(word2Field);
        RightPanel.add(queryButton);
        RightPanel.add(new JLabel("input: "));
        RightPanel.add(inputTextArea);
        RightPanel.add(generateButton);
        RightPanel.add(new JLabel("output: "));
        RightPanel.add(outputTextArea);


        this.add(panel, BorderLayout.NORTH);
        this.add(RightPanel, BorderLayout.SOUTH);

        setTitle("Graph GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 600);
        setVisible(true);
    }


    /**
     * 打开文件，忽略文件中的所有非英文字母的字符，并将所有单词保存在 words 数组中.
     *
     * @param file 要处理的文件
     */
    private void createDirectedGraph(File file) {
        StringBuilder content = new StringBuilder();
        //也是指定utf-8编码的问题
        //try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append(" ");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String text = content.toString().replaceAll("[^a-zA-Z ]", "").toLowerCase();
        String[] words = text.split("\\s+");
        buildGraph(words);
    }
    /**
     * 打开文件，忽略文件中的所有非英文字母的字符，并将所有单词保存在 words 数组中.
     *
     * @param fileName 要处理的文件的路径
     */
    public Graph<String, DefaultWeightedEdge> createDirectedGraph(String fileName) {
        Graph<String, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fileName), StandardCharsets.UTF_8))) {
            String line;
            String previousWord = null;

            while ((line = br.readLine()) != null) {
                String[] words = line.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");

                for (String word : words) {
                    if (!word.isEmpty()) {
                        graph.addVertex(word);

                        if (previousWord != null) {
                            DefaultWeightedEdge edge = graph.getEdge(previousWord, word);
                            if (edge != null) {
                                double currentWeight = graph.getEdgeWeight(edge);
                                graph.setEdgeWeight(edge, currentWeight + 1);
                            } else {
                                Graphs.addEdgeWithVertices(graph, previousWord, word, 1);
                            }
                        }
                        previousWord = word;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return graph;
    }

    /**
     * 创建图.
     *
     * @param words 图的顶点
     *
     */
    private void buildGraph(String[] words) {
        graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];

            graph.addVertex(word1);
            graph.addVertex(word2);

            DefaultWeightedEdge edge = graph.getEdge(word1, word2);
            if (edge != null) { //如果找到边，则权重+1
                double currentWeight = graph.getEdgeWeight(edge);
                graph.setEdgeWeight(edge, currentWeight + 1);
            } else { //找不到边，则增加边
                Graphs.addEdgeWithVertices(graph, word1, word2, 1);
            }
        }
        showDirectedGrap();
    }

    /**
     * 展示有向图.
     */
    private void showDirectedGrap() {
        JGraphXAdapter<String, DefaultWeightedEdge> graphAdapter = new JGraphXAdapter<>(graph);

        // 为图应用布局
        mxCircleLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        // 获取用于显示图形的图组件
        graphComponent = new mxGraphComponent(graphAdapter);
        //  this.graphComponent=graphComponent;

        // 迭代边以设置显示权重的标签和改变边的颜色
        mxGraph jgxGraph = graphComponent.getGraph();
        Object[] edges = jgxGraph.getChildEdges(jgxGraph.getDefaultParent());

        for (Object edge : edges) {
            mxCell cell = (mxCell) edge;
            String source = cell.getSource().getValue().toString();
            String target = cell.getTarget().getValue().toString();
            DefaultWeightedEdge jgtEdge = graph.getEdge(source, target);
            if (jgtEdge != null) {
                double weight = graph.getEdgeWeight(jgtEdge);
                jgxGraph.getModel().setValue(cell, String.valueOf((int) weight));
                jgxGraph.setCellStyle("strokeColor=black;edgeStyle=curved;", new Object[]{cell}); // 边为黑色
            }
        }


        add(graphComponent, BorderLayout.CENTER); //画图

        revalidate();
    }


    /**
     * 将图保存为图像.
     */
    private void saveAsImage() {
        // 确保正确的类型转换
        if (graphComponent instanceof mxGraphComponent) {
            // 获取 mxGraph 组件
            mxGraph graph = graphComponent.getGraph();
            // 创建图像
            BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE,
                    graphComponent.isAntiAlias(), null, graphComponent.getCanvas());

            // 创建文件选择器
            JFileChooser fileChooser = new JFileChooser();
            // 显示保存文件对话框
            int result = fileChooser.showSaveDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                // 获取用户选择的文件
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    // 将图像保存为 PNG 格式
                    ImageIO.write(image, "PNG", selectedFile);
                    JOptionPane.showMessageDialog(null,
                            "Graph saved as image successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Failed to save graph as image.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            // 如果组件不是 mxGraphComponent，则显示错误消息
            JOptionPane.showMessageDialog(null,
                    "Component is not a mxGraphComponent.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * 查询桥接词.
     *
     * @param word1 第一个单词
     * @param word2 第二个单词
     * @return 包含桥接词信息的字符串
     */
    private String queryBridgeWords(Graph<String, DefaultWeightedEdge> G,String word1, String word2) {
        // 检查图中是否包含 word1 和 word2
        if (!G.containsVertex(word1) && !G.containsVertex(word2)) {
            return "No \"" + word1 + "\" and \"" + word2 + "\" in the graph!";
        } else if (!G.containsVertex(word1) || !G.containsVertex(word2)) {
            return "No \"" + (G.containsVertex(word1) ? word2 : word1) + "\" in the graph!";
        }

        // 用于存储桥接词的结果字符串
        StringBuilder result = new StringBuilder();
        List<String> list = new ArrayList<>();
        // 是否找到桥接词的标志
        boolean foundBridge = false;

        // 获取以 word1 为源的所有边
        Set<DefaultWeightedEdge> edgesFromWord1 = G.outgoingEdgesOf(word1);

        // 遍历这些边，找到所有的目标顶点
        for (DefaultWeightedEdge edge : edgesFromWord1) {
            String intermediateWord = G.getEdgeTarget(edge);
            // 获取 intermediateWord 到 word2 的所有边
            Set<DefaultWeightedEdge> edgesFromIntermediate = G.outgoingEdgesOf(intermediateWord);
            for (DefaultWeightedEdge secondEdge : edgesFromIntermediate) {
                if (G.getEdgeTarget(secondEdge).equals(word2)) {
                    // 如果已找到桥接词，则在结果字符串中添加逗号
                    if (foundBridge) {
                        result.append(", ");

                    }
                    // 将 intermediateWord（桥接词）添加到结果字符串中
                    result.append(intermediateWord);
                    list.add(intermediateWord);
                    foundBridge = true;
                }
            }
        }

        // 如果未找到桥接词，则返回相应消息
        if (!foundBridge) {
            return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
        }
        List<String> word1List = new ArrayList<>();
        word1List.add(word1);
        List<String> word2List = new ArrayList<>();
        word2List.add(word2);
        customizeGraph(word1List, word2List, list);
        // 返回包含桥接词信息的字符串
        return "The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: " + result;
    }

    /**
     * 在图中显示出桥接词.
     *
     * @param word1List         单词1的列表
     * @param word2List         单词2的列表
     * @param intermediateWords 中间词的列表
     */
    private void customizeGraph(List<String> word1List, List<String> word2List,
                                List<String> intermediateWords) {
        JGraphXAdapter<String, DefaultWeightedEdge> graphAdapter = new JGraphXAdapter<>(graph);
        // 为图应用布局
        mxCircleLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        // 获取用于显示图形的图组件
        mxGraphComponent graphComponent = new mxGraphComponent(graphAdapter);

        // 迭代边以设置显示权重的标签和改变边的颜色
        mxGraph jgxGraph = graphComponent.getGraph();
        Object[] edges = jgxGraph.getChildEdges(jgxGraph.getDefaultParent());

        for (Object edge : edges) {
            mxCell cell = (mxCell) edge;
            String source = cell.getSource().getValue().toString();
            String target = cell.getTarget().getValue().toString();
            DefaultWeightedEdge jgtEdge = graph.getEdge(source, target);
            if (jgtEdge != null) {
                double weight = graph.getEdgeWeight(jgtEdge);
                jgxGraph.getModel().setValue(cell, String.valueOf((int) weight));
                jgxGraph.setCellStyle("strokeColor=black", new Object[]{cell}); // 边设置为黑色

            }
        }

        // 修改节点颜色
        for (String word1 : word1List) {
            Object nodeCell1 = graphAdapter.getVertexToCellMap().get(word1);
            if (nodeCell1 != null) {
                graphComponent.getGraph().setCellStyle("fillColor=red;strokeColor=black",
                        new Object[]{nodeCell1});
            }
        }

        for (String intermediateWord : intermediateWords) {
            Object nodeCell = graphAdapter.getVertexToCellMap().get(intermediateWord);
            if (nodeCell != null) {
                graphComponent.getGraph().setCellStyle("fillColor=lightgreen;strokeColor=black",
                        new Object[]{nodeCell});
            }
        }

        for (String word2 : word2List) {
            Object nodeCell2 = graphAdapter.getVertexToCellMap().get(word2);
            if (nodeCell2 != null) {
                graphComponent.getGraph().setCellStyle("fillColor=red;strokeColor=black",
                        new Object[]{nodeCell2});
            }
        }

        add(graphComponent, BorderLayout.CENTER); // 画图
        revalidate();
    }


    /**
     * 生成一个新的文本字符串，通过在输入文本的相邻单词之间插入“桥梁”词.
     *
     * @param inputText 原始输入文本
     * @param graph 有向带权图
     * @return 新的文本字符串，如果输入为空则返回空字符串。
     */
    public String generateNewText(Graph<String, DefaultWeightedEdge> graph, String inputText) {
        // 如果输入文本为空，则返回空字符串
        if (inputText.isEmpty()) {
            return "";
        }
        // 处理文本中的标点符号和非字母字符
        inputText = inputText.replaceAll("[^A-Za-z ]", " ").replaceAll("\\s+", " ").trim();
        // 如果处理后文本为空，则返回空字符串
        if (inputText.isEmpty()) {
            return "";
        }
        if (graph==null){
            return inputText;
        }
        // 将输入文本按空格拆分成单词数组
        String[] inputWords = inputText.split("\\s+");
        // 创建一个用于存储输出单词的列表
        List<String> outputWords = new ArrayList<>();
        List<String> word1List = new ArrayList<>();
        List<String> word2List = new ArrayList<>();
        List<String> intermediateWords = new ArrayList<>();

        // 遍历输入单词数组中的每个单词（除了最后一个）
        for (int i = 0; i < inputWords.length - 1; i++) {
            // 当前单词
            String word1 = inputWords[i].toLowerCase();
            // 下一个单词
            String word2 = inputWords[i + 1].toLowerCase();
            // 将当前单词添加到输出单词列表中
            outputWords.add(inputWords[i]);

            // 如果图中包含当前单词和下一个单词作为顶点
            if (graph.containsVertex(word1) && graph.containsVertex(word2)) {
                // 获取当前单词和下一个单词之间的随机桥梁词
                String bridge = getRandomBridgeWord(graph,word1, word2);
                // 如果桥梁词不为空，则将其添加到输出单词列表中
                if (bridge != null) {
                    outputWords.add(bridge);
                    word1List.add(word1);
                    word2List.add(word2);
                    intermediateWords.add(bridge);
                }
            }
        }

        // 将最后一个单词添加到输出单词列表中
        outputWords.add(inputWords[inputWords.length - 1]);
        //customizeGraph(word1List, word2List, intermediateWords);

        // 将输出单词列表转换为字符串，并以空格分隔
        return String.join(" ", outputWords);
    }

    /**
     * 获取两个单词之间的随机桥接词.
     *
     * @param word1 第一个单词
     * @param word2 第二个单词
     * @param graph 有向无权图
     * @return 桥接词，如果不存在桥接词则返回null
     */
    private String getRandomBridgeWord(Graph<String, DefaultWeightedEdge> graph,String word1, String word2) {
        // 用于存储桥接词的集合
        Set<String> bridgeWords = new HashSet<>();

        // 获取以 word1 为源的所有边
        Set<DefaultWeightedEdge> edgesFromWord1 = graph.outgoingEdgesOf(word1);

        // 遍历这些边，找到所有的目标顶点
        for (DefaultWeightedEdge edge : edgesFromWord1) {
            String intermediateWord = graph.getEdgeTarget(edge);
            // 获取 intermediateWord 到 word2 的所有边
            Set<DefaultWeightedEdge> edgesFromIntermediate = graph.outgoingEdgesOf(intermediateWord);
            for (DefaultWeightedEdge secondEdge : edgesFromIntermediate) {
                if (graph.getEdgeTarget(secondEdge).equals(word2)) {
                    // 添加 intermediateWord 到桥接词集合中
                    bridgeWords.add(intermediateWord);
                }
            }
        }
        // 如果集合为空，返回null
        if (bridgeWords.isEmpty()) {
            return null;
        }
        // 随机选择一个桥接词
        int size = bridgeWords.size();
        // int item = new Random().nextInt(size); //有bad practice问题每次使用都创建一个Radom对象
        int item = randomBridgeWord.nextInt(size);
        int i = 0;
        for (String bridgeWord : bridgeWords) {
            if (i == item) {
                return bridgeWord;
            }
            i++;
        }
        return null; // 这个返回实际上不会被执行
    }

    /**
     * 计算最短路径的方法.
     *
     * @param word1 起始单词
     * @param word2 目标单词
     * @return 最短路径的字符串表示形式，如果不存在最短路径和距离，则返回相应的提示信息
     */
    private String calcShortestPath(Graph<String, DefaultWeightedEdge> graph,String word1, String word2) {
        if (!graph.containsVertex(word1) || !graph.containsVertex(word2)) {
            javax.swing.JOptionPane.showMessageDialog(null, "图中未找到一个或两个单词。");
            return "0";
        }
        // 用于记录每个顶点到起始点的最短距离
        java.util.Map<String, Double> distance = new java.util.HashMap<>();
        // 用于记录每个顶点的前驱节点，用于还原最短路径
        java.util.Map<String, java.util.List<String>> predecessors = new java.util.HashMap<>();
        // 初始化距离和前驱节点
        for (String vertex : graph.vertexSet()) {
            distance.put(vertex, java.lang.Double.POSITIVE_INFINITY);
            predecessors.put(vertex, new java.util.ArrayList<>());
        }
        distance.put(word1, 0.0); // 起始点到自身的距离为0
        // 未访问的顶点集合
        java.util.Set<String> visited = new java.util.HashSet<>();
        // 使用 BFS 找到所有最短路径
        java.util.Queue<String> queue = new java.util.ArrayDeque<>();
        queue.offer(word1);
        visited.add(word1);

        while (!queue.isEmpty()) {
            String currentVertex = queue.poll();
            for (DefaultWeightedEdge edge : graph.outgoingEdgesOf(currentVertex)) {
                String neighbor = org.jgrapht.Graphs.getOppositeVertex(graph, edge, currentVertex);
                double edgeWeight = graph.getEdgeWeight(edge);
                double alt = distance.get(currentVertex) + edgeWeight;

                if (alt < distance.get(neighbor)) {
                    distance.put(neighbor, alt);
                    predecessors.get(neighbor).clear();
                    predecessors.get(neighbor).add(currentVertex);

                    if (!visited.contains(neighbor)) {
                        queue.offer(neighbor);
                        visited.add(neighbor);
                    }
                } else if (alt == distance.get(neighbor)) {
                    predecessors.get(neighbor).add(currentVertex);
                }
            }
        }
        // 获取所有最短路径
        java.util.List<java.util.List<String>> allShortestPaths = new java.util.ArrayList<>();
        java.util.List<String> path = new java.util.ArrayList<>();
        path.add(word2);
        getAllShortestPaths(word1, word2, predecessors, path, allShortestPaths);

        // 显示所有最短路径和距离
        java.lang.StringBuilder message = new java.lang.StringBuilder();
        message.append("从 ").append(word1).append(" 到 ").append(word2).append(" 的所有最短路径为：\n");
        for (java.util.List<String> shortestPath : allShortestPaths) {
            java.util.Collections.reverse(shortestPath); // 翻转路径顺序
            message.append(shortestPath).append("\n");
        }
        message.append("最短距离为：").append(distance.get(word2));
        //message信息用弹窗表示出来
        //javax.swing.JOptionPane.showMessageDialog(null, message.toString());

        // 组织返回的字符串
        StringBuilder result = new StringBuilder();
        result.append(distance.get(word2)).append(";");
        // 将每条路径拼接到结果中
        for (List<String> shortestPath : allShortestPaths) {
            Collections.reverse(shortestPath); // 翻转路径顺序
            result.append(String.join(" ", shortestPath)).append(";");
        }

        return result.toString() + '@' + message.toString();

    }

    /**
     * 获取所有最短路径的方法.
     *
     * @param word1              起始单词
     * @param word2              目标单词
     * @param predecessors       前任节点映射表
     * @param currentPath        当前路径列表
     * @param allShortestPaths   所有最短路径列表
     */
    private void getAllShortestPaths(String word1, String word2, java.util.Map<String,
            java.util.List<String>> predecessors,
                                     java.util.List<String> currentPath,
                                     java.util.List<java.util.List<String>> allShortestPaths) {
        if (word1.equals(word2)) {
            allShortestPaths.add(new java.util.ArrayList<>(currentPath));
        } else {
            for (String predecessor : predecessors.get(word2)) {
                currentPath.add(predecessor);
                getAllShortestPaths(word1, predecessor, predecessors, currentPath, allShortestPaths);
                currentPath.remove(currentPath.size() - 1);
            }
        }
    }

    /**
     * 显示最短路径的方法.
     *
     * @param paths 最短路径的字符串表示形式
     */
    private void showShortestPaths(String paths) {
        JGraphXAdapter<String, DefaultWeightedEdge> graphAdapter = new JGraphXAdapter<>(graph);
        // 为图应用布局
        mxCircleLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        // 获取用于显示图形的图组件
        mxGraphComponent graphComponent = new mxGraphComponent(graphAdapter);

        // 迭代边以设置显示权重的标签和改变边的颜色
        mxGraph jgxGraph = graphComponent.getGraph();
        Object[] edges = jgxGraph.getChildEdges(jgxGraph.getDefaultParent());

        for (Object edge : edges) {
            mxCell cell = (mxCell) edge;
            String source = cell.getSource().getValue().toString();
            String target = cell.getTarget().getValue().toString();
            DefaultWeightedEdge jgtEdge = graph.getEdge(source, target);
            if (jgtEdge != null) {
                double weight = graph.getEdgeWeight(jgtEdge);
                jgxGraph.getModel().setValue(cell, String.valueOf((int) weight));
                jgxGraph.setCellStyle("strokeColor=black", new Object[]{cell}); // 边为黑色

            }
        }

        // 根据路径信息标记最短路径
        String[] pathInfo = paths.split(";");
        if (pathInfo.length >= 2) {
            String[] firstPath = pathInfo[1].split("\\s+");
            Object nodeCell1 = graphAdapter.getVertexToCellMap().get(firstPath[0]);
            if (nodeCell1 != null) {
                // 在图组件中找到节点修改颜色
                graphComponent.getGraph().setCellStyle("fillColor=red;strokeColor=red",
                        new Object[]{nodeCell1});
            }
            Object nodeCell2 = graphAdapter.getVertexToCellMap().get(firstPath[firstPath.length - 1]);
            if (nodeCell2 != null) {
                // 在图组件中找到节点修改颜色
                graphComponent.getGraph().setCellStyle("fillColor=red;strokeColor=red",
                        new Object[]{nodeCell2});
            }
        }
        // Red, Orange,Green, Blue, Purple
        String[] colorSchemes = new String[]{
                "#FF0000", "#FFA500", "#008000", "#0000FF", "#800080"};
        for (int i = 1; i < pathInfo.length; i++) {
            String[] vertices = pathInfo[i].split("\\s+");

            for (int j = 0; j < vertices.length - 1; j++) {
                String source = vertices[j];
                String target = vertices[j + 1];
                // 获取边对应的cell
                Object edge = graphAdapter.getEdgeToCellMap().get(graph.getEdge(target, source));
                // 根据最短路径的顺序设置虚线样式和颜色
                jgxGraph.setCellStyles(mxConstants.STYLE_DASHED, "1", new Object[]{edge});
                jgxGraph.setCellStyles(mxConstants.STYLE_STROKECOLOR,
                        colorSchemes[(i - 1) % pathInfo.length], new Object[]{edge}); // 橙色
            }
        }
        add(graphComponent, BorderLayout.CENTER); //画图
        revalidate();
    }

    /**
     * 执行随机游走的方法.
     *
     * @return walkPath.toString().trim()随机游走的结果，即生成的随机路径
     */
    private String randomWalk() {
        StringBuilder walkPath = new StringBuilder();
        Set<DefaultWeightedEdge> visitedEdges = new HashSet<>();
        SecureRandom random = new SecureRandom(); //增强不可预测性
        //Random random = new Random();

        List<String> vertices = new ArrayList<>(graph.vertexSet());
        String currentVertex = vertices.get(random.nextInt(vertices.size())); // 直接随机选择起始节点
        walkPath.append(currentVertex).append(" ");

        while (true) {
            //找当前点的出边
            Set<DefaultWeightedEdge> outgoingEdges = graph.outgoingEdgesOf(currentVertex);

            if (outgoingEdges.isEmpty()) {
                break; // 如果当前节点没有出边，则结束游走
            }
            //随机取一个出边
            DefaultWeightedEdge randomEdge =
                    (DefaultWeightedEdge) outgoingEdges.toArray()[random.nextInt(outgoingEdges.size())];
            //出边相连的点
            String nextVertex = Graphs.getOppositeVertex(graph, randomEdge, currentVertex);
            walkPath.append(nextVertex).append(" ");
            //标记走过的出边
            if (!visitedEdges.add(randomEdge)) {
                break;
            }
            //判断是不是第二次走这条出边
            currentVertex = nextVertex;
        }

        return walkPath.toString().trim(); // 返回游走路径字符串，去除末尾空格
    }

    /**
     * 显示随机路径的方法.
     *
     * @param randompath 随机路径的字符串表示形式
     */
    private void showRandomPath(String randompath) {

        File tmpFile = new File("tmpshort.txt");
        // 清空文件内容 同样指定utf-8
        //try (BufferedWriter clearWriter = new BufferedWriter(new FileWriter(tmpFile))) {
        try (BufferedWriter clearWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tmpFile), StandardCharsets.UTF_8))) {
            clearWriter.write(""); // 清空文件内容
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Failed to clear tmpfile content.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JGraphXAdapter<String, DefaultWeightedEdge> graphAdapter = new JGraphXAdapter<>(graph);
        // 为图应用布局
        mxCircleLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        // 获取用于显示图形的图组件
        mxGraphComponent graphComponent = new mxGraphComponent(graphAdapter);

        // 迭代边以设置显示权重的标签和改变边的颜色
        mxGraph jgxGraph = graphComponent.getGraph();
        Object[] edges = jgxGraph.getChildEdges(jgxGraph.getDefaultParent());

        for (Object edge : edges) {
            mxCell cell = (mxCell) edge;
            String source = cell.getSource().getValue().toString();
            String target = cell.getTarget().getValue().toString();
            DefaultWeightedEdge jgtEdge = graph.getEdge(source, target);
            if (jgtEdge != null) {
                double weight = graph.getEdgeWeight(jgtEdge);
                jgxGraph.getModel().setValue(cell, String.valueOf((int) weight));
                jgxGraph.setCellStyle("strokeColor=black", new Object[]{cell}); // 边为黑色

            }
        }

        add(graphComponent, BorderLayout.CENTER); //画图
        revalidate();

        //在这里分隔出randompath里的所有节点
        String[] nodes = randompath.split("\\s+"); // 使用正则表达式以支持一个或多个空格分隔
        // 用于定时器的索引
        final int[] index = {0};
        // 创建Timer，每3秒执行一次
        if (colorTimer != null) {
            colorTimer.stop();
            colorTimer = null; // 释放之前的计时器
        }
        colorTimer = new Timer(1000, e -> {
            if (index[0] < nodes.length) {
                String nodeCurrent = nodes[index[0]]; // 当前节点，作为边的源节点
                Object nodeCell = graphAdapter.getVertexToCellMap().get(nodeCurrent);
                if (nodeCell != null) {
                    // 在图组件中找到节点修改颜色
                    graphComponent.getGraph().setCellStyle("strokeColor=red", new Object[]{nodeCell});
                }
                if (index[0] != 0) {
                    String nodeLast = nodes[index[0] - 1]; // 上一个节点
                    // 获取图中的边对象
                    DefaultWeightedEdge edge = graph.getEdge(nodeLast, nodeCurrent);
                    if (edge != null) {
                        // 使用graphAdapter来获取边对应的mxCell对象
                        Object edgeCell = graphAdapter.getEdgeToCellMap().get(edge);
                        if (edgeCell != null) {
                            // 设置边的样式为红色和虚线
                            graphComponent.getGraph().setCellStyle("strokeColor=red;dashed=1",
                                    new Object[]{edgeCell});
                            if (index[0] == nodes.length - 1) {
                                graphComponent.getGraph().setCellStyle("strokeColor=yellow;dashed=1",
                                        new Object[]{edgeCell});
                            }
                        }
                        // 高亮显示源节点和目标节点（如果需要的话）
                    }
                }
                graphComponent.revalidate();
                graphComponent.repaint();
                index[0]++;
                // 将节点值写入文件 指定utf-8
                //try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, true))) {
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(tmpFile, true), StandardCharsets.UTF_8))) {
                    writer.write(nodeCurrent + " "); // 写入节点值并用空格分隔
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Failed to write node value to file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                ((Timer) e.getSource()).stop(); // 停止计时器
                //遍历完成
                int confirmResult = JOptionPane.showConfirmDialog(null,
                        "遍历完成，需要保存随机游走路径吗?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirmResult == JOptionPane.NO_OPTION) {
                    return; // 如果用户选择不保存，直接返回
                } else if (confirmResult == JOptionPane.YES_OPTION) {
                    // 用户选择保存，弹出文件选择对话框
                    JFileChooser fileChooser = new JFileChooser();
                    int result = fileChooser.showSaveDialog(null);
                    if (result != JFileChooser.APPROVE_OPTION) {
                        return; // 如果用户未选择文件，直接返回
                    }
                    // 获取用户选择的文件
                    File selectedFile = fileChooser.getSelectedFile();
                    // 复制文件内容
                    // 清空文件内容 指定utf-8
                    //try (BufferedWriter clearWriter = new BufferedWriter(new FileWriter(selectedFile))) {
                    try (BufferedWriter clearWriter = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(selectedFile), StandardCharsets.UTF_8))) {
                        clearWriter.write(""); // 清空文件内容
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null,
                                "Failed to clear file content.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    //指定utf-8
                    //try (BufferedReader reader = new BufferedReader(new FileReader("tmpshort.txt"));
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                            new FileInputStream("tmpshort.txt"), StandardCharsets.UTF_8));
                         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                                 new FileOutputStream(selectedFile), StandardCharsets.UTF_8))) {
                        //BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            writer.write(line);
                            writer.newLine();
                        }
                        JOptionPane.showMessageDialog(null, "保存成功", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null,
                                "Failed to copy file content.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

            }
        });
        colorTimer.start();
        return;
    }

    /**
     * 销毁计时器的方法.
     */
    private void togglePauseTimer() {
        if (colorTimer != null) {
            colorTimer.stop();
            colorTimer = null;
            pauseButton.setText("Pause Random Walk");
            isPaused = false;
        }
    }

    /**
     * 获取
     * @return graphComponent
     */
    public mxGraphComponent getGraphComponent() {
        return graphComponent;
    }

    /**
     * 设置
     * @param graphComponent
     */
    public void setGraphComponent(mxGraphComponent graphComponent) {
        this.graphComponent = graphComponent;
    }

    /**
     * 获取
     * @return openButton
     */
    public JButton getOpenButton() {
        return openButton;
    }

    /**
     * 设置
     * @param openButton
     */
    public void setOpenButton(JButton openButton) {
        this.openButton = openButton;
    }

    /**
     * 获取
     * @return saveButton
     */
    public JButton getSaveButton() {
        return saveButton;
    }

    /**
     * 设置
     * @param saveButton
     */
    public void setSaveButton(JButton saveButton) {
        this.saveButton = saveButton;
    }

    /**
     * 获取
     * @return queryButton
     */
    public JButton getQueryButton() {
        return queryButton;
    }

    /**
     * 设置
     * @param queryButton
     */
    public void setQueryButton(JButton queryButton) {
        this.queryButton = queryButton;
    }

    /**
     * 获取
     * @return generateButton
     */
    public JButton getGenerateButton() {
        return generateButton;
    }

    /**
     * 设置
     * @param generateButton
     */
    public void setGenerateButton(JButton generateButton) {
        this.generateButton = generateButton;
    }

    /**
     * 获取
     * @return shortestPathButton
     */
    public JButton getShortestPathButton() {
        return shortestPathButton;
    }

    /**
     * 设置
     * @param shortestPathButton
     */
    public void setShortestPathButton(JButton shortestPathButton) {
        this.shortestPathButton = shortestPathButton;
    }

    /**
     * 获取
     * @return randomWalkButton
     */
    public JButton getRandomWalkButton() {
        return randomWalkButton;
    }

    /**
     * 设置
     * @param randomWalkButton
     */
    public void setRandomWalkButton(JButton randomWalkButton) {
        this.randomWalkButton = randomWalkButton;
    }

    /**
     * 获取
     * @return pauseButton
     */
    public JButton getPauseButton() {
        return pauseButton;
    }

    /**
     * 设置
     * @param pauseButton
     */
    public void setPauseButton(JButton pauseButton) {
        this.pauseButton = pauseButton;
    }

    /**
     * 获取
     * @return endButton
     */
    public JButton getEndButton() {
        return endButton;
    }

    /**
     * 设置
     * @param endButton
     */
    public void setEndButton(JButton endButton) {
        this.endButton = endButton;
    }

    /**
     * 获取
     * @return graph
     */
    public Graph<String, DefaultWeightedEdge> getGraph() {
        return graph;
    }

    /**
     * 设置
     * @param graph
     */
    public void setGraph(Graph<String, DefaultWeightedEdge> graph) {
        this.graph = graph;
    }

    /**
     * 获取
     * @return colorTimer
     */
    public Timer getColorTimer() {
        return colorTimer;
    }

    /**
     * 设置
     * @param colorTimer
     */
    public void setColorTimer(Timer colorTimer) {
        this.colorTimer = colorTimer;
    }

    /**
     * 获取
     * @return isPaused
     */
    public boolean isIsPaused() {
        return isPaused;
    }

    /**
     * 设置
     * @param isPaused
     */
    public void setIsPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public String toString() {
        return "GraphGui{graphComponent = " + graphComponent + ", openButton = " + openButton + ", saveButton = " + saveButton + ", queryButton = " + queryButton + ", generateButton = " + generateButton + ", shortestPathButton = " + shortestPathButton + ", randomWalkButton = " + randomWalkButton + ", pauseButton = " + pauseButton + ", endButton = " + endButton + ", graph = " + graph + ", colorTimer = " + colorTimer + ", isPaused = " + isPaused + ", randomBridgeWord = " + randomBridgeWord + "}";
    }
}