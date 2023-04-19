package cmu.csdetector.metrics;

import cmu.csdetector.resources.Type;
import cmu.csdetector.util.GenericCollector;
import cmu.csdetector.util.TypeLoader;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MetricTest {
    public static File file = new File("src/test/java/cmu/csdetector/dummy/lcom");;
    public static Map<String, Type> testTypes = new HashMap<>();

    @BeforeAll
    public static void beforeAll() throws IOException {
        List<Type> types = TypeLoader.loadAllFromDir(file);

        // Populate classes into hashmap for individual testing
        for (Type type : types) {
            String name = type.getFullyQualifiedName();
            int index = name.lastIndexOf('.');
            if (index != -1) {
                name = name.substring(index + 1);
            }
            GenericCollector.collectTypeMetricValues(type);
            testTypes.put(name, type);
        }
    }

    abstract double getMetric(String testClassName);

}
