package cmu.csdetector.metrics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LCOM2Test extends MetricTest {

    @Override
    public double getMetric(String testClassName) {
        return testTypes.get(testClassName).getMetricValue(MetricName.LCOM2);
    }

    public static double computeLCOM2(double m, double a, double sumMA) {
        double lcom2 = 1 - (sumMA / (m * a));
        return Double.isNaN(lcom2) ? 0 : lcom2;
    }

    @Test
    void LCOM2DummyDad() {
        String testClassName = "DummyDad";
        double m = 1;
        double a = 2;
        double sumMA = 1;
        double lcom2 = computeLCOM2(m, a, sumMA);
        Assertions.assertEquals(lcom2, getMetric(testClassName));
    }

    @Test
    void LCOM2DummySon() {
        String testClassName = "DummySon";
        double m = 2;
        double a = 1;
        double sumMA = 1;
        double lcom2 = computeLCOM2(m, a, sumMA);
        Assertions.assertEquals(lcom2, getMetric(testClassName));
    }

    @Test
    void LCOM2DummyGrandson() {
        String testClassName = "DummyGrandSon";
        double m = 1;
        double a = 1;
        double sumMA = 1;
        double lcom2 = computeLCOM2(m, a, sumMA);
        Assertions.assertEquals(lcom2, getMetric(testClassName));
    }

    @Test
    void LCOM2DummyLCOM() {
        String testClassName = "DummyLCOM";
        double m = 3;
        double a = 7;
        double sumMA = 6;
        double lcom2 = computeLCOM2(m, a, sumMA);
        Assertions.assertEquals(lcom2, getMetric(testClassName));
    }

    @Test
    void LCOM2EmptyClass() {
        String testClassName = "EmptyClass";
        double m = 0;
        double a = 0;
        double sumMA = 0;
        double lcom2 = computeLCOM2(m, a, sumMA);
        Assertions.assertEquals(lcom2, getMetric(testClassName));
    }

}
