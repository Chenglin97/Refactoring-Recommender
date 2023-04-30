package cmu.csdetector.refactor;

import cmu.csdetector.CodeSmellDetector;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Resource;
import cmu.csdetector.resources.Type;
import cmu.csdetector.util.GenericCollector;
import cmu.csdetector.util.TypeLoader;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class Heuristic1Test {
    public static File file = new File("../RefactoringTest");
    public static Map<String, Type> testTypes = new HashMap<>();
    public static Map<String, Map<String, Method>> testMethods = new HashMap<>();

    public CodeSmellDetector detector = new CodeSmellDetector();

    @BeforeAll
    public static void beforeAll() throws IOException {


        List<Type> types = TypeLoader.loadAllFromDir(file);

        for (Type type : types) {
            String typeName = type.getFullyQualifiedName();
            int i = typeName.lastIndexOf('.');
            if (i != -1) {
                typeName = typeName.substring(i + 1);
            }
            GenericCollector.collectTypeMetricValues(type);
            testTypes.put(typeName, type);

            Map<String, Method> methods = new HashMap<>();
            for (Method method : type.getMethods()) {
                String methodName = method.getFullyQualifiedName();
                int j = methodName.lastIndexOf('.');
                if (j != -1) {
                    methodName = methodName.substring(j + 1);
                }
                methods.put(methodName, method);
            }
            testMethods.put(typeName, methods);
        }
    }

    @Test
    public void initializeHeuristic1Test() {
        /* should get a new method called "iLoveRefactoringSoMuchFunMethod" from the class */
        ArrayList<String> sourcePaths = new ArrayList<>();
        sourcePaths.add("../RefactoringTest");

        Method method = testMethods.get("testFile").get("grabManifests");
        String newMethodName = "iLoveRefactoringSoMuchFunMethod";
        Heuristic1 heuristic1 = new Heuristic1(method, sourcePaths);
        heuristic1.generateExtractOpportunity();
        List<Integer> cluster = new ArrayList<>();
        cluster.add(4);
        cluster.add(7);

        TypeDeclaration classAfterAddingCluster = heuristic1.createNewClassAfterAddingCluster(cluster);
        ArrayList<MethodDeclaration> methodDeclarations = new ArrayList<>(classAfterAddingCluster.bodyDeclarations());
        MethodDeclaration methodToMove = methodDeclarations.get(methodDeclarations.size() - 1);

        Assertions.assertEquals(methodToMove.getName().toString(), newMethodName);
    }

    @Test
    public void getTargetClassesFromCluster() {
        /* should be able to get target classes to move the cluster to */
        ArrayList<String> sourcePaths = new ArrayList<>();
        sourcePaths.add("../RefactoringTest");
        Method method = testMethods.get("testFile").get("grabManifests");
        Resource sourceClass = (Resource) testTypes.get("testFile");
        Heuristic1 heuristic1 = new Heuristic1(method, sourcePaths);
        heuristic1.generateExtractOpportunity();
        List<Integer> cluster = new ArrayList<>();
        cluster.add(4);
        cluster.add(7);
        TypeDeclaration classAfterAddingCluster = heuristic1.createNewClassAfterAddingCluster(cluster);
        ArrayList<MethodDeclaration> methodDeclarations = new ArrayList<>(classAfterAddingCluster.bodyDeclarations());

        MethodDeclaration methodToMove = methodDeclarations.get(methodDeclarations.size() - 1);
        MethodMover methodMover = new MethodMover();
        /* Call feature envy algorithm to get target classes */
        ArrayList<Resource> target_classes = new ArrayList<>();
        target_classes.add((Resource) (testTypes.get("FileSet")));
        target_classes.add((Resource) (testTypes.get("Resource")));
        target_classes.add((Resource) (testTypes.get("ArchiveFileSet")));

        Resource result_class = methodMover.moveMethod((ASTNode) methodToMove, sourceClass, target_classes);

        Assertions.assertEquals(result_class.getFullyQualifiedName(), "cmu.csdetector.dummy.heu1.ArchiveFileSet");
    }
}
