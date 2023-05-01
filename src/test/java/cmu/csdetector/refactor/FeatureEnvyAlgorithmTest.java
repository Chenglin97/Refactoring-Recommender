package cmu.csdetector.refactor;

import cmu.csdetector.CodeSmellDetector;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Resource;
import cmu.csdetector.resources.Type;
import cmu.csdetector.util.GenericCollector;
import cmu.csdetector.util.TypeLoader;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureEnvyAlgorithmTest {
    public static File file = new File("src/test/java/cmu/csdetector/dummy");
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
    public void testFeatureEnvyAlgorithm() {
        ArrayList<MethodDeclaration> featureEnvies = new ArrayList<>();

        ArrayList<Resource> sourceClasses = new ArrayList<>();
        String testClassName = "FeatureEnvyMethod";
        Type sourceClass = testTypes.get(testClassName);
        sourceClasses.add((Resource) sourceClass);
        String testMethodName = "superForeign";
        ASTNode methodNode = testMethods.get(testClassName).get(testMethodName).getNode();
        featureEnvies.add((MethodDeclaration) methodNode);

        testClassName = "BlobClassSample";
        sourceClass = testTypes.get(testClassName);
        sourceClasses.add((Resource) sourceClass);
        testMethodName = "a";
        methodNode = testMethods.get(testClassName).get(testMethodName).getNode();
        featureEnvies.add((MethodDeclaration) methodNode);

        ArrayList<Resource> target_classes = new ArrayList<>();

        target_classes.add((Resource) (testTypes.get("FieldAccessedByMethod")));
        target_classes.add((Resource) testTypes.get("RefusedBequestSample"));

        // System.out.println("featureEnvies: " + featureEnvies);

        System.out.println("testMethods: " + testMethods);
        System.out.println("sourceClasses: " + sourceClasses);
        System.out.println("target_classes: " + target_classes);

        List<Resource> result_classes = detector.featureEnvyAlgorithm(featureEnvies, sourceClasses, target_classes);
        Assertions.assertEquals(2, result_classes.size());
        Assertions.assertEquals("cmu.csdetector.dummy.smells.RefusedBequestSample", result_classes.get(0).getFullyQualifiedName());
        Assertions.assertEquals("cmu.csdetector.dummy.smells.BlobClassSample", result_classes.get(1).getFullyQualifiedName());
    }
}
