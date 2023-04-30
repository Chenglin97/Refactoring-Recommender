package cmu.csdetector.refactor;

import cmu.csdetector.CodeSmellDetector;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import cmu.csdetector.util.GenericCollector;
import cmu.csdetector.util.TypeLoader;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Assertions.assertEquals(methodDeclarations.get(methodDeclarations.size() - 1).getName().toString(), newMethodName);
    }
}
