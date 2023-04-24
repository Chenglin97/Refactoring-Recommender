package cmu.csdetector.metrics;

import cmu.csdetector.resources.Type;
import cmu.csdetector.util.GenericCollector;
import cmu.csdetector.util.TypeLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LCOM3RFTest extends MetricTest {

    public static File file = new File("src/test/java/cmu/csdetector/dummy");;
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

    @Override
    public double getMetric(String testClassName) {
        return testTypes.get(testClassName).getMetricValue(MetricName.LCOM3);
    }

    public double computeMetric(double m, double a, double sumMA) {
        double lcom3 = (m - (sumMA / a)) / (m - 1);
        return Double.isNaN(lcom3) ? 0 : lcom3;
    }

    @Test
    void FeatureEnvyMethodRF() {
        String testClassName = "FeatureEnvyMethodRF";
        double m = 9;
        double a = 3;
        double sumMA = 4 + 3 + 3;
        double lcom3 = computeMetric(m, a, sumMA);
        // 0.7083333333333333
        Assertions.assertEquals(lcom3, 0.7083333333333333);
        Assertions.assertEquals(lcom3, getMetric(testClassName));
    }

    @Test
    void FeatureEnvyMethod() {
        String testClassName = "FeatureEnvyMethod";
        double m = 10;
        double a = 3;
        double sumMA = 4 + 4 + 3;
        double lcom3 = computeMetric(m, a, sumMA);
        // 0.7037037037037037
        Assertions.assertEquals(lcom3, 0.7037037037037037);
        Assertions.assertEquals(lcom3, getMetric(testClassName));
    }

    @Test
    void FieldAccessedByMethodRF() {
        String testClassName = "FieldAccessedByMethodRF";
        double m = 8;
        double a = 6;
        double sumMA = 8;
        double lcom3 = computeMetric(m, a, sumMA);
        // 0.9523809523809524
        Assertions.assertEquals(lcom3, 0.9523809523809524);
        Assertions.assertEquals(lcom3, getMetric(testClassName));
    }

    @Test
    void FieldAccessedByMethod() {
        String testClassName = "FieldAccessedByMethod";
        double m = 7;
        double a = 6;
        double sumMA = 8;
        double lcom3 = computeMetric(m, a, sumMA);
        // 0.9444444444444445
        Assertions.assertEquals(lcom3, 0.9444444444444445);
        Assertions.assertEquals(lcom3, getMetric(testClassName));
    }

    @Test
    void RefusedBequestSampleRF() {
        String testClassName = "RefusedBequestSampleRF";
        double m = 4;
        double a = 1;
        double sumMA = 0;
        double lcom3 = computeMetric(m, a, sumMA);
        Assertions.assertEquals(lcom3, 1.3333333333333333);
        Assertions.assertEquals(lcom3, getMetric(testClassName));
    }

    @Test
    void RefusedBequestSample() {
        String testClassName = "RefusedBequestSample";
        double m = 3;
        double a = 1;
        double sumMA = 0;
        double lcom3 = computeMetric(m, a, sumMA);
        Assertions.assertEquals(lcom3, 1.5);
        Assertions.assertEquals(lcom3, getMetric(testClassName));
    }
}
