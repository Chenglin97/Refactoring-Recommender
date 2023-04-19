package cmu.csdetector.smells;

import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import cmu.csdetector.util.GenericCollector;
import cmu.csdetector.util.TypeLoader;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SmellTest {
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
}
