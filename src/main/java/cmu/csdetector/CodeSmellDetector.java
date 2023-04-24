package cmu.csdetector;

import cmu.csdetector.ast.visitors.FieldDeclarationCollector;
import cmu.csdetector.ast.visitors.StatementCollector;
import cmu.csdetector.console.ToolParameters;
import cmu.csdetector.console.output.ObservableExclusionStrategy;
import cmu.csdetector.metrics.MethodMetricValueCollector;
import cmu.csdetector.metrics.TypeMetricValueCollector;
import cmu.csdetector.metrics.calculators.method.MethodLOCCalculator;
import cmu.csdetector.smells.ClassLevelSmellDetector;
import cmu.csdetector.smells.MethodLevelSmellDetector;
import cmu.csdetector.smells.Smell;
import cmu.csdetector.smells.detectors.ComplexClass;
import cmu.csdetector.smells.detectors.FeatureEnvy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import cmu.csdetector.resources.loader.JavaFilesFinder;
import cmu.csdetector.resources.loader.SourceFile;
import cmu.csdetector.resources.loader.SourceFilesLoader;
import org.apache.commons.cli.ParseException;
import org.eclipse.jdt.core.dom.*;

import javax.swing.plaf.nimbus.State;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CodeSmellDetector {

    public static void main(String[] args) throws IOException{
        CodeSmellDetector instance = new CodeSmellDetector();

        instance.start(args);

    }

    private void start(String[] args) throws IOException {
        ToolParameters parameters = ToolParameters.getInstance();

        try {
            parameters.parse(args);
        } catch (ParseException exception) {
            System.out.println(exception.getMessage());
            parameters.printHelp();

            System.exit(-1);
        }

        System.out.println(new Date());
        List<String> sourcePaths = List.of(parameters.getValue(ToolParameters.SOURCE_FOLDER));
        List<Type> allTypes = this.loadAllTypes(sourcePaths);

        collectTypeMetrics(allTypes);

        detectSmells(allTypes);

        saveSmellsFile(allTypes);

        refactor(allTypes);

//        testClustering();

        System.out.println(new Date());

    }

    private void complexClassHeuristic(List<Type> complexClasses) {
        for (Type type: complexClasses) {
            for (Method method: type.getMethods()) {
                // TODO run heuristics
                TreeMap<Integer, List<List<Integer>>> clustersByStep = this.findExtractOpportunity(method);
                printClusters(clustersByStep);
            }
        }
    }

    private void featureEnvyHeuristic(List<Method> featureEnvies) {
        for (Method method: featureEnvies) {
            // TODO run heuristics
            TreeMap<Integer, List<List<Integer>>> clustersByStep = this.findExtractOpportunity(method);
            printClusters(clustersByStep);
        }
    }

    private TreeMap<Integer, List<List<Integer>>> findExtractOpportunity(Method method) {
        ASTNode node = method.getNode();
        StatementCollector statementCollector = new StatementCollector();
        node.accept(statementCollector);
        List<ASTNode> statementNodes = statementCollector.getNodesCollected();
        TreeMap<Integer, Set<String>> matrix = statementCollector.getMatrix();
        HashMap<String, List<Integer>> transformedMatrix = transformMatrix(matrix);
        return this.generateClusters(transformedMatrix, matrix.size());
    }

    private void printClusters(TreeMap<Integer, List<List<Integer>>> clustersByStep) {
        System.out.println("");
        for (Integer key: clustersByStep.keySet()) {
            System.out.println("Step: " + key + ", clusters: "+ clustersByStep.get(key));
        }
    }

    private void print(Object object) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonStr = gson.toJson(object);
        System.out.println(jsonStr);
    }

    private void refactor(List<Type> allTypes) {
        // get complexClass
        List<Type> complexClasses = new ArrayList<>();
        ComplexClass complexClass = new ComplexClass();
        for (Type type : allTypes) {
            List<Smell> smells = complexClass.detect(type);
            if (smells.size() > 0) {
                complexClasses.add(type);
            }
        }
        System.out.println("Analyze complex class, " + complexClasses.size() + " classes are complex class.");
        this.complexClassHeuristic(complexClasses);

        // get featureEnvy
        List<Method> featureEnvies = new ArrayList<>();
        FeatureEnvy detector = new FeatureEnvy();
        for (Type type : allTypes) {
            for (Method method : type.getMethods()) {
                List<Smell> smells = detector.detect(method);
                if (smells.size() > 0) {
                    featureEnvies.add(method);
                }
            }
        }
        System.out.println("Analyze feature envy, " + featureEnvies.size() + " methods are feature envy.");
        this.featureEnvyHeuristic(featureEnvies);
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
//            System.out.println("\nStep: " + step);
            List<List<Integer>> stepClusters = new ArrayList<>();
            for (String node : matrix.keySet()) {
                List<Integer> sortedLines = matrix.get(node);
                List<List<Integer>> individualClusters = generateStepClusters(sortedLines, step);
                stepClusters.addAll(individualClusters);
            }
            stepClusters = new ArrayList<>(new HashSet<>(stepClusters));
            stepClusters.sort(Comparator.comparingInt((List<Integer> a) -> a.get(0)));
            clustersByStep.put(step, stepClusters);
//            System.out.println(stepClusters);
        }
        return clustersByStep;
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

    private void detectSmells(List<Type> allTypes) {
        // homework
        for (Type type : allTypes) {
            // It is important to detect certain smells at method-level first, such as Brain Method
            MethodLevelSmellDetector methodLevelSmellDetector = new MethodLevelSmellDetector();

            for (Method method : type.getMethods()) {
                List<Smell> smells = methodLevelSmellDetector.detect(method);
                method.addAllSmells(smells);
            }

            // Some class-level smell detectors rely on method-level smells as part of their detection
            ClassLevelSmellDetector classLevelSmellDetector = new ClassLevelSmellDetector();
            List<Smell> smells = classLevelSmellDetector.detect(type);
            type.addAllSmells(smells);
        }
    }

    private List<Type> loadAllTypes(List<String> sourcePaths) throws IOException {
        List<Type> allTypes = new ArrayList<>();

        JavaFilesFinder sourceLoader = new JavaFilesFinder(sourcePaths);
        SourceFilesLoader compUnitLoader = new SourceFilesLoader(sourceLoader);
        List<SourceFile> sourceFiles = compUnitLoader.getLoadedSourceFiles();

        for (SourceFile sourceFile : sourceFiles) {
            allTypes.addAll(sourceFile.getTypes());
        }
        return allTypes;
    }

    private void collectTypeMetrics(List<Type> types) {
        for (Type type : types) {
            TypeMetricValueCollector typeCollector = new TypeMetricValueCollector();
            typeCollector.collect(type);

            this.collectMethodMetrics(type);
        }
    }

    private void collectMethodMetrics(Type type) {
        for (Method method: type.getMethods()) {
            MethodMetricValueCollector methodCollector = new MethodMetricValueCollector();
            methodCollector.collect(method);
        }
    }

    private void saveSmellsFile(List<Type> smellyTypes) throws IOException {
        ToolParameters parameters = ToolParameters.getInstance();
        File smellsFile = new File(parameters.getValue(ToolParameters.SMELLS_FILE));
        BufferedWriter writer = new BufferedWriter(new FileWriter(smellsFile));
        System.out.println("Saving smells file...");

        GsonBuilder builder = new GsonBuilder();
        builder.addSerializationExclusionStrategy(new ObservableExclusionStrategy());
        builder.disableHtmlEscaping();
        builder.setPrettyPrinting();
        builder.serializeNulls();

        Gson gson = builder.create();
        gson.toJson(smellyTypes, writer);
        writer.close();
    }

}
