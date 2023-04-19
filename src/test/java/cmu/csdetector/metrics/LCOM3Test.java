package cmu.csdetector.metrics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LCOM3Test extends MetricTest {

    @Override
    public double getMetric(String testClassName) {
        return testTypes.get(testClassName).getMetricValue(MetricName.LCOM3);
    }

    public double computeMetric(double m, double a, double sumMA) {
        double lcom3 = (m - (sumMA / a)) / (m - 1);
        return Double.isNaN(lcom3) ? 0 : lcom3;
    }

    @Test
    void LCOM3DummyDad() {
        String testClassName = "DummyDad";
        double m = 1;
        double a = 2;
        double sumMA = 1;
        double lcom3 = computeMetric(m, a, sumMA);
        Assertions.assertEquals(lcom3, getMetric(testClassName));
    }

    @Test
    void LCOM3DummySon() {
        String testClassName = "DummySon";
        double m = 2;
        double a = 1;
        double sumMA = 1;
        double lcom3 = computeMetric(m, a, sumMA);
        Assertions.assertEquals(lcom3, getMetric(testClassName));
    }

    @Test
    void LCOM3DummyGrandson() {
        String testClassName = "DummyGrandSon";
        double m = 1;
        double a = 1;
        double sumMA = 1;
        double lcom3 = computeMetric(m, a, sumMA);
        Assertions.assertEquals(lcom3, getMetric(testClassName));
    }

    @Test
    void LCOM3DummyLCOM() {
        String testClassName = "DummyLCOM";
        double m = 3;
        double a = 7;
        double sumMA = 6;
        double lcom3 = computeMetric(m, a, sumMA);
        Assertions.assertEquals(lcom3, getMetric(testClassName));
    }

    @Test
    void LCOM3EmptyClass() {
        String testClassName = "EmptyClass";
        double m = 0;
        double a = 0;
        double sumMA = 0;
        double lcom3 = computeMetric(m, a, sumMA);
        Assertions.assertEquals(lcom3, getMetric(testClassName));
    }

}
