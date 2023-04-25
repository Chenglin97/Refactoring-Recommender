package cmu.csdetector.refactor;


import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Resource;
import cmu.csdetector.resources.Type;
import cmu.csdetector.smells.Smell;
import cmu.csdetector.smells.detectors.FeatureEnvy;
import cmu.csdetector.refactor.MethodMover;
import cmu.csdetector.util.GenericCollector;
import cmu.csdetector.util.TypeLoader;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodMoverTest {

    public static File file = new File("src/test/java/cmu/csdetector/dummy/smells");
    public static Map<String, Type> testTypes = new HashMap<>();
    public static Map<String, Map<String, Method>> testMethods = new HashMap<>();

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

    protected Type findTypeByName(String name) {
        for (Type type : this.testTypes.values()) {
            TypeDeclaration td = (TypeDeclaration)type.getNode();
            String typeName = td.getName().toString();

            if (typeName.equals(name)) {
                return type;
            }
        }
        return null;
    }

    private MethodMover methodMover = new MethodMover();

    @Test
    void MoveMethod() {
        String testClassName = "FeatureEnvyMethod";
        ArrayList<Resource> target_classes = new ArrayList<>();
        target_classes.add((Resource) (testTypes.get("FieldAccessedByMethod")));
        target_classes.add((Resource) testTypes.get("RefusedBequestSample"));
        String testMethodName = "superForeign";
        Method method = testMethods.get(testClassName).get(testMethodName);
        Resource target_class = methodMover.moveMethodBasedOnLCOM3(method, testTypes.get(testClassName), target_classes);

        Assertions.assertEquals(testTypes.get("FeatureEnvyMethod"), target_class);
    }

}
