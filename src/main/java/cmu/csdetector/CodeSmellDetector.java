package cmu.csdetector;

import cmu.csdetector.ast.ASTBuilder;
import cmu.csdetector.ast.visitors.CyclomaticComplexityVisitor;
import cmu.csdetector.ast.visitors.StatementCollector;
import cmu.csdetector.console.ConsoleProgressMonitor;
import cmu.csdetector.console.ToolParameters;
import cmu.csdetector.console.output.ObservableExclusionStrategy;
import cmu.csdetector.metrics.MethodMetricValueCollector;
import cmu.csdetector.metrics.TypeMetricValueCollector;
import cmu.csdetector.metrics.calculators.type.LCOM2Calculator;
import cmu.csdetector.refactor.Heuristic1;
import cmu.csdetector.refactor.MethodMover;
import cmu.csdetector.resources.ParenthoodRegistry;
import cmu.csdetector.resources.Resource;
import cmu.csdetector.resources.loader.SourceFileASTRequestor;
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
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CodeSmellDetector {

    private SourceFilesLoader compUnitLoader;

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

//        saveSmellsFile(allTypes);

        refactor(allTypes, sourcePaths);

//        testClustering();

        System.out.println(new Date());

    }

    private void complexClassAlgorithm(List<Type> complexClasses, List<String> sourcePaths){
        for (Type type: complexClasses) {
            for (Method method: type.getMethods()) {
                    // TODO run heuristics
                Heuristic1 heuristic1 = new Heuristic1(method, sourcePaths);
                List<Integer> bestCluster = heuristic1.generateExtractOpportunity();

            }
        }

    }

    public List<Resource> featureEnvyAlgorithm(List<Method> featureEnvies, ArrayList<Resource> sourceClasses, ArrayList<Resource> classes) {
        List<Resource> target_classes = new ArrayList<>();
        for (int i = 0; i < featureEnvies.size(); i++) {
            Method method = featureEnvies.get(i);
            Resource sourceClass = sourceClasses.get(i);
            // TODO extract the best code fragment

            // extract the entire method
            MethodMover methodMover = new MethodMover();
            Resource classToMoveMethodTo = methodMover.moveMethod(method.getNode(), sourceClass, classes);
            target_classes.add(classToMoveMethodTo);
        }
        return target_classes;
    }

    private void refactor(List<Type> allTypes, List<String> sourcePaths) {
        // get complexClass
        List<Type> complexClasses = new ArrayList<>();
        ComplexClass complexClass = new ComplexClass();
        for (Type type : allTypes) {
            List<Smell> smells = complexClass.detect(type);
            if (smells.size() > 0) {
                complexClasses.add(type);
            }
        }
        System.out.println("Analyze complex class, " + complexClasses.size() + " classes are complex classes.");
        this.complexClassAlgorithm(complexClasses, sourcePaths);

        // get featureEnvy
        List<Method> featureEnvies = new ArrayList<>();
        ArrayList<Resource> canidateClasses = new ArrayList<>();
        ArrayList<Resource> featureEnvyClasses = new ArrayList<>();
        FeatureEnvy detector = new FeatureEnvy();
        for (Type type : allTypes) {
            System.out.println("Analyzing feature envy in " + type.toString() + "...");
            canidateClasses.add(type);
            for (Method method : type.getMethods()) {
                List<Smell> smells = detector.detect(method);
                if (smells.size() > 0) {
                    featureEnvies.add(method);
                    featureEnvyClasses.add(type);
                }
            }
        }
        System.out.println("Analyze feature envy, " + featureEnvies.size() + " methods have feature envy.");
        this.featureEnvyAlgorithm(featureEnvies, featureEnvyClasses, canidateClasses);
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
        this.compUnitLoader = new SourceFilesLoader(sourceLoader);
        List<SourceFile> sourceFiles = this.compUnitLoader.getLoadedSourceFiles();

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
