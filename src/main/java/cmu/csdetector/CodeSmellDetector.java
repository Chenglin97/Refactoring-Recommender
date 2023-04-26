package cmu.csdetector;

import cmu.csdetector.ast.visitors.StatementCollector;
import cmu.csdetector.console.ToolParameters;
import cmu.csdetector.console.output.ObservableExclusionStrategy;
import cmu.csdetector.metrics.MethodMetricValueCollector;
import cmu.csdetector.metrics.TypeMetricValueCollector;
import cmu.csdetector.metrics.calculators.type.LCOM2Calculator;
import cmu.csdetector.refactor.Heuristic1;
import cmu.csdetector.resources.ParenthoodRegistry;
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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
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

    private void complexClassAlgorithm(List<Type> complexClasses) {
        for (Type type: complexClasses) {
            LCOM2Calculator lcom2Calculator = new LCOM2Calculator();
            Double oldLcom2 = lcom2Calculator.getValue(type.getNode());

            for (Method method: type.getMethods()) {
                // TODO run heuristics
                Heuristic1 heuristic1 = new Heuristic1(method);
                heuristic1.generateExtractOpportunity();
            }
        }
    }

    private void featureEnvyAlgorithm(List<Method> featureEnvies) {
        for (Method method: featureEnvies) {
            // TODO run heuristics
        }
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
        this.complexClassAlgorithm(complexClasses);

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
        this.featureEnvyAlgorithm(featureEnvies);
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
