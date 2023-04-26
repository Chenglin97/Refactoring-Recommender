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

    private void move(List<ASTNode> statementNodes){
        // complationUnit is in sourceFile
        ASTNode nodeToMove = statementNodes.get(20).getParent();

        CompilationUnit root = (CompilationUnit) nodeToMove.getRoot();

        AST ast = root.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        ASTNode oldParentNode = nodeToMove.getParent();
        ListRewrite oldListRewrite = rewriter.getListRewrite(oldParentNode, Block.STATEMENTS_PROPERTY);

        oldListRewrite.remove(nodeToMove, null);

        // Obtain the modified source code
        Document document = new Document(root.toString());
        TextEdit edit = rewriter.rewriteAST(document, null);
        try {
            edit.apply(document);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        String modifiedSourceCode = document.get();



        // Step 7: add the node to the new parent list at the desired index
//        newListRewrite.insertAt(nodeToMove, 0, null);


        // Step 8: obtain the modified AST root node
//        CompilationUnit modifiedAST = (CompilationUnit) rewriter.rewriteAST();
//
//        List<Statement> statements = (List<Statement>) listRewrite.getRewrittenList();
//
//        int indexToDelete = statements.indexOf(nodeToDelete);
//        listRewrite.remove(nodeToDelete, null);
//
//        ASTNode modifiedNode = listRewrite.createCopyTarget(parentNode);
//
//        // Print the modified AST
//        System.out.println(root);


//
//
//        ASTNode root = node.getRoot();
//        CompilationUnit compilationUnit = (CompilationUnit) root;
//        TypeDeclaration typeDeclaration = (TypeDeclaration) compilationUnit.types().get(0);
//
//        MethodDeclaration[] methodDeclarations = typeDeclaration.getMethods();
//        MethodDeclaration newMethodDeclaration = root.getAST().newMethodDeclaration();
//
//        // Set the name of the method
//        String suggestedName = "suggectedMethodName";
//        newMethodDeclaration.setName(root.getAST().newSimpleName(suggestedName));
//
//        // Set the return type of the method
//        newMethodDeclaration.setReturnType2(root.getAST().newPrimitiveType(PrimitiveType.VOID));
//
//        // Add the method to the TypeDeclaration node
//        typeDeclaration.bodyDeclarations().add(newMethodDeclaration);





//
//        MethodDeclaration methodDecl = methodDeclarations[0];
//
//        AST targetAST = AST.newAST(AST.JLS11);
//
//        // Copy the method declaration subtree to the target AST
//        MethodDeclaration copiedMethodDecl = (MethodDeclaration) ASTNode.copySubtree(targetAST, methodDecl);
//
//        // Add the copied method declaration node to the target AST
//        TypeDeclaration targetClassDecl = n.getAST().newTypeDeclaration();
//        targetClassDecl.setName(targetAST.newSimpleName("MyTargetClass"));
//        targetClassDecl.modifiers().add(targetAST.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
//        targetClassDecl.bodyDeclarations().add(copiedMethodDecl);
//
//        System.out.println(targetClassDecl);
////
//
//        AST ast = n.getAST();


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
