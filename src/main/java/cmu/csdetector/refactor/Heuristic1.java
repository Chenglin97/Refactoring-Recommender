package cmu.csdetector.refactor;

import cmu.csdetector.ast.visitors.StatementCollector;
import cmu.csdetector.resources.Method;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.*;

public class Heuristic1 {

    private Method method;
    private List<ASTNode> statementNodes;
    private TreeMap<Integer, List<List<Integer>>> clusters;

    public Heuristic1(Method method) {
        this.method = method;
    }

    public List<List<Integer>> getBestCluster() {
        this.generateExtractOpportunity(this.method);
        List<List<Integer>> bestCluster = null;

        // TODO ranking by using statementNodes, clusters

        return bestCluster;
    }

    private void generateExtractOpportunity(Method method) {
        ASTNode node = method.getNode();
        StatementCollector statementCollector = new StatementCollector();
        node.accept(statementCollector);
        this.statementNodes = statementCollector.getNodesCollected();
        TreeMap<Integer, Set<String>> matrix = statementCollector.getMatrix();
        HashMap<String, List<Integer>> transformedMatrix = transformMatrix(matrix);
        this.clusters = this.generateClusters(transformedMatrix, matrix.size());
    }

    private void testClustering() {
        Map<String, List<Integer>> matrix = new HashMap<>();
        matrix.put("manifests", List.of(2,25,29,30,33));
        matrix.put("rcs", List.of(2,3,5,6,7,8,12,13,14,16,18));
        matrix.put("length", List.of(2,3,10));
        matrix.put("rcs.length", List.of(2,3,10));
        matrix.put("i", List.of(3,5,6,7,8,12,13,14,16,18,29,30));
        matrix.put("rec", List.of(4,6,8,10,11,25));
        matrix.put("grabRes", List.of(6));
        matrix.put("grabNonFileSetRes", List.of(8));
        matrix.put("j", List.of(10,11,25));
        matrix.put("name", List.of(11,15,21,24));
        matrix.put("rec.getName.replace", List.of(11));
        matrix.put("getName.replace", List.of(11));
        matrix.put("getName", List.of(11));
        matrix.put("replace", List.of(11));
        matrix.put("afs", List.of(13,14,15,16,17,18));
        matrix.put("equals", List.of(14,16,18));
        matrix.put("afs.getFullpath", List.of(14,15,16,18));
        matrix.put("getProj;", List.of(14,15,16,17));
        matrix.put("name.afs.getFullpath", List.of(15));
        matrix.put("afs.getPref;", List.of(16,17,18));
        matrix.put("getPref", List.of(16,17,18));
        matrix.put("pr", List.of(17,18,19,21));
        matrix.put("pr.endsWith", List.of(18));
        matrix.put("endsWith", List.of(18));
        matrix.put("name.equalsIgnoreCase", List.of(24));
        matrix.put("equalsIgnoreCase", List.of(24));
        matrix.put("rec.length", List.of(10));
        generateClusters(matrix, 34);
    }

    private TreeMap<Integer, List<List<Integer>>> generateClusters(Map<String, List<Integer>> matrix, int loc) {
        TreeMap<Integer, List<List<Integer>>> clustersByStep = new TreeMap<>();
        for (int step = 1; step <= loc; step++) {
            System.out.println("\nStep: " + step);
            List<List<Integer>> stepClusters = new ArrayList<>();
            for (String node : matrix.keySet()) {
                List<Integer> sortedLines = matrix.get(node);
                List<List<Integer>> individualClusters = generateStepClusters(sortedLines, step);
                stepClusters.addAll(individualClusters);
            }
            stepClusters = new ArrayList<>(new HashSet<>(stepClusters));
            stepClusters = mergeAndSortClusters(stepClusters);
            clustersByStep.put(step, stepClusters);
            System.out.println(stepClusters);
        }
        return clustersByStep;
    }

    private List<List<Integer>> mergeAndSortClusters(List<List<Integer>> baseClusters) {
        if (baseClusters.isEmpty()) return baseClusters;
        baseClusters.sort(Comparator.comparingInt((List<Integer> a) -> a.get(0)));
        List<List<Integer>> mergedClusters = new ArrayList<>();

        // Old Algorithm
        int low = baseClusters.get(0).get(0);
        int high = baseClusters.get(0).get(1);
        for (int i = 1; i < baseClusters.size(); i++) {
            int i_low = baseClusters.get(i).get(0);
            int i_high = baseClusters.get(i).get(1);
            if (i_low <= high) {
                if (high < i_high) high = i_high;
            } else {
                mergedClusters.add(List.of(low, high));
                low = i_low;
                high = i_high;
            }
        }
        mergedClusters.add(List.of(low, high));

        // New Algorithm
//        for (int i = 0; i < baseClusters.size(); i++) {
//            int low = baseClusters.get(i).get(0);
//            int i_high = baseClusters.get(i).get(1);
//            int high = i_high;
//            for (int j = i+1; j < baseClusters.size(); j++) {
//                int j_low = baseClusters.get(j).get(0);
//                int j_high = baseClusters.get(j).get(1);
//                if (j_low <= high) {
//                    if (j_low <= i_high) {
//                        if (i_high < j_high) {
//                            mergedClusters.add(List.of(low, j_high));
//                        }
//                    }
//                    if (high < j_high) {
//                        high = j_high;
//                        mergedClusters.add(List.of(low, high));
//                    }
//                } else {
//                    mergedClusters.add(List.of(low, high));
//                    break;
//                }
//            }
//        }

        mergedClusters.addAll(baseClusters);
        mergedClusters = new ArrayList<>(new HashSet<>(mergedClusters));
        mergedClusters.sort(Comparator.comparingInt((List<Integer> a) -> a.get(0)));
        return mergedClusters;
    }

    private List<List<Integer>> generateStepClusters(List<Integer> sortedLines, int step) {
        List<List<Integer>> clusters = new ArrayList<>();
        List<Integer> cluster = new ArrayList<>();
        for (int i = 0; i < sortedLines.size(); i++) {
            if (i == 0 || sortedLines.get(i) - sortedLines.get(i-1) > step) {
                if (cluster.size() > 1) {
                    clusters.add(List.of(cluster.get(0), cluster.get(cluster.size()-1)));
                }
                cluster = new ArrayList<>();
                cluster.add(sortedLines.get(i));
            } else {
                cluster.add(sortedLines.get(i));
            }
        }
        if (cluster.size() > 1) {
            clusters.add(List.of(cluster.get(0), cluster.get(cluster.size()-1)));
        }
        return clusters;
    }

    private HashMap<String, List<Integer>> transformMatrix(TreeMap<Integer, Set<String>> matrix){
        HashMap<String, List<Integer>> transformedMatrix =new HashMap<>();

        for(Integer key : matrix.keySet()) {
            Set<String> s = matrix.get(key);
            List<Integer> newList = new ArrayList<Integer>();
            for(String name: s) {
                if(transformedMatrix.keySet().contains(name)){
                    newList = transformedMatrix.get(name);
                    newList.add(key);
                } else{
                    newList.add(key);
                }
                HashSet<Integer> finalSet = new HashSet<>(newList);
                List<Integer> finalList = new ArrayList<>(finalSet);
                Collections.sort(finalList);
                transformedMatrix.put(name, finalList);
            }
        }
        return transformedMatrix;
    }

    public List<ASTNode> getStatementNodes() {
        return statementNodes;
    }

}
