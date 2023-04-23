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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import cmu.csdetector.resources.loader.JavaFilesFinder;
import cmu.csdetector.resources.loader.SourceFile;
import cmu.csdetector.resources.loader.SourceFilesLoader;
import org.apache.commons.cli.ParseException;
import org.eclipse.jdt.core.dom.ASTNode;

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

        System.out.println(new Date());

    }

    private void refactor(List<Type> allTypes) {
        for (Type type : allTypes) {
            MethodLevelSmellDetector methodLevelSmellDetector = new MethodLevelSmellDetector();

            System.out.println("Type name: " + type.getBinding().getName());
            if ("testFile".equals(type.getBinding().getName())) {

                for (Method method : type.getMethods()) {
                    // Refactor here
                    ASTNode node = method.getNode();

                    StatementCollector statementCollector = new StatementCollector();
                    node.accept(statementCollector);
                    List<ASTNode> nodes = statementCollector.getNodesCollected();
                    TreeMap<Integer, Set<String>> matrix = statementCollector.getMatrix();

                    // transform the treeMap to hashMap which has the variable name as the key instead of line
                    HashMap<String, List<Integer>> transformedMatrix = transformMatrix(matrix);

                    for(String key : transformedMatrix.keySet()) {
                        List<Integer> s = transformedMatrix.get(key);
                        System.out.println("");
                        System.out.println(key +" "+ s);
                    }
                }
            }

        }
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
