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
import cmu.csdetector.refactor.ExtractMethodOpportunity;
import cmu.csdetector.refactor.Heuristic1;
import cmu.csdetector.refactor.MethodMover;
import cmu.csdetector.refactor.SaveRecommendationIntoFile;
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
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CodeSmellDetector {

    private SourceFilesLoader compUnitLoader;

    private SaveRecommendationIntoFile writer = new SaveRecommendationIntoFile();

    public static void main(String[] args) throws IOException{
        CodeSmellDetector instance = new CodeSmellDetector();

        instance.start(args);

    }

    private void start(String[] args) throws IOException {
        ToolParameters parameters = ToolParameters.getInstance();
        writer.clear();

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
            writer.save("\nCOMPLEX CLASS: " + type.getFullyQualifiedName());
            for (Method method: type.getMethods()) {
                writer.save("\nANALYZING METHOD: " + method.getFullyQualifiedName());

                CyclomaticComplexityVisitor ccVisitor = new CyclomaticComplexityVisitor();
                method.getNode().accept(ccVisitor);
                System.out.println("Cyclomatic Complexity: " + ccVisitor.getCyclomaticComplexity());

                List<List<String>> recommendations = new ArrayList<>();
                while (ccVisitor.getCyclomaticComplexity() > 5) {
                    // Run heuristics
                    Heuristic1 heuristic1 = new Heuristic1(method, sourcePaths);
                    ExtractMethodOpportunity bestOpportunity = heuristic1.generateExtractOpportunity();
                    List<Integer> bestCluster = bestOpportunity.getCluster();

                    List<String> modifiedMethods = heuristic1.modifyMethod(bestCluster);
                    List<String> results = new ArrayList<>(modifiedMethods);
                    ccVisitor = new CyclomaticComplexityVisitor();
                    method.getNode().accept(ccVisitor);
                    System.out.println("Cyclomatic Complexity: " + ccVisitor.getCyclomaticComplexity());

                    if (bestCluster != null && bestCluster.size() > 1) {
                        System.out.println("The best cluster is from line " + bestCluster.get(0) + " to " + bestCluster.get(1));
                        results.addAll(bestOpportunity.getParameters());
                        recommendations.add(results);
                    } else {
                        writer.save("No clusters found");
                        System.out.println("No cluster found");
                    }
                }



                writer.save("Recommendations:");
                for (List<String> rec : recommendations) {
                    writer.save("Extract the following method from " + method.getFullyQualifiedName() + " with parameters: " + rec.subList(2, rec.size()));
                    writer.save(rec.get(1));
                    writer.save("... so that " + method.getFullyQualifiedName() + " becomes: ");
                    writer.save(rec.get(0));
                }

            }
        }

    }

    public List<Resource> featureEnvyAlgorithm(List<MethodDeclaration> featureEnvyNodes, List<Method> featureEnvyMethods, ArrayList<Resource> sourceClasses, ArrayList<Resource> classes, List<String> sourcePaths) {
        List<Resource> target_classes = new ArrayList<>();

        for (int i = 0; i < featureEnvyNodes.size(); i++) {
            MethodDeclaration methodNode = featureEnvyNodes.get(i);
            Method method = featureEnvyMethods.get(i);
            Resource sourceClass = sourceClasses.get(i);

            writer.save("\nFEATURE ENVY IN METHOD: " + method.getFullyQualifiedName());

            // extract the best code fragment
            List<Integer> bestCluster;
            Heuristic1 heuristic1 = new Heuristic1(method, sourcePaths);
            ExtractMethodOpportunity bestOpportunity = heuristic1.generateExtractOpportunity();
            if (bestOpportunity == null) {
                bestCluster = new ArrayList<>();
            } else {
                bestCluster = bestOpportunity.getCluster();
            }


            TypeDeclaration classAfterAddingCluster = heuristic1.createNewClassAfterAddingCluster(bestCluster);
            ArrayList<MethodDeclaration> methodsAfterAddingCluster = new ArrayList<>(classAfterAddingCluster.bodyDeclarations());
            MethodDeclaration methodToMove = methodsAfterAddingCluster.get(methodsAfterAddingCluster.size() - 1);

            if (bestCluster.size() == 0) {
                System.out.println(method.getFullyQualifiedName() + " has feature envy but no cluster to remove was found. Remove entire method.");
                methodToMove = methodNode;
            }

            // determine best target class for cluster
            MethodMover methodMover = new MethodMover();
            // UNCOMMENT line below to more entire methods instead of clusters
//             Resource classToMoveMethodTo = methodMover.moveMethod(method.getNode(), sourceClass, classes);
            Resource classToMoveMethodTo = methodMover.moveMethod(methodToMove, sourceClass, classes);
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
        String output_text = "************PART 1: REFACTORING COMPLEX CLASS************";
        writer.save(output_text);
        output_text = complexClasses.size() + " classes are complex classes.";
        writer.save(output_text);

        this.complexClassAlgorithm(complexClasses, sourcePaths);

        // get featureEnvy
        List<MethodDeclaration> featureEnvyNodes = new ArrayList<>();
        List<Method> featureEnvyMethods = new ArrayList<>();
        ArrayList<Resource> canidateClasses = new ArrayList<>();
        ArrayList<Resource> featureEnvyClasses = new ArrayList<>();
        FeatureEnvy detector = new FeatureEnvy();
        for (Type type : allTypes) {
            System.out.println("Analyzing feature envy in " + type.toString() + "...");
            canidateClasses.add(type);
            for (Method method : type.getMethods()) {
                List<Smell> smells = detector.detect(method);
                if (smells.size() > 0) {
                    featureEnvyNodes.add((MethodDeclaration) method.getNode());
                    featureEnvyMethods.add(method);
                    featureEnvyClasses.add(type);
                }
            }
        }

        writer.save("\n************PART 2: REFACTORING FEATURE ENVY************");
        writer.save(featureEnvyNodes.size() + " methods have feature envy.");
        this.featureEnvyAlgorithm(featureEnvyNodes, featureEnvyMethods, featureEnvyClasses, canidateClasses, sourcePaths);
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
