package cmu.csdetector.smells;

import cmu.csdetector.resources.Method;
import cmu.csdetector.smells.detectors.FeatureEnvy;
import org.eclipse.core.runtime.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class FeatureEnvyTest extends SmellTest {
    private FeatureEnvy detector = new FeatureEnvy();

    @Test
    void FELocalA() {
        String testClassName = "FeatureEnvyMethod";
        String testMethodName = "localA";
        List<Smell> smells = detector.detect(testMethods.get(testClassName).get(testMethodName));

        Assertions.assertEquals(0, smells.size());
    }

    @Test
    void FELocalB() {
        String testClassName = "FeatureEnvyMethod";
        String testMethodName = "localB";
        List<Smell> smells = detector.detect(testMethods.get(testClassName).get(testMethodName));

        Assertions.assertEquals(0, smells.size());
    }

    @Test
    void FELocalC() {
        String testClassName = "FeatureEnvyMethod";
        String testMethodName = "localC";
        List<Smell> smells = detector.detect(testMethods.get(testClassName).get(testMethodName));

        Assertions.assertEquals(0, smells.size());
    }

    @Test
    void FELocalD() {
        String testClassName = "FeatureEnvyMethod";
        String testMethodName = "localD";
        List<Smell> smells = detector.detect(testMethods.get(testClassName).get(testMethodName));

        Assertions.assertEquals(0, smells.size());
    }

    @Test
    void FESuperLocal() {
        String testClassName = "FeatureEnvyMethod";
        String testMethodName = "superLocal";
        List<Smell> smells = detector.detect(testMethods.get(testClassName).get(testMethodName));

        Assertions.assertEquals(0, smells.size());
    }

    @Test
    void FESuperForeign() {
        String testClassName = "FeatureEnvyMethod";
        String testMethodName = "superForeign";
        List<Smell> smells = detector.detect(testMethods.get(testClassName).get(testMethodName));

        Assertions.assertEquals(2, smells.size());
        for (Smell smell : smells) {
            Assertions.assertEquals(SmellName.FeatureEnvy, smell.getName());
        }
    }

    @Test
    void FEMostLocal() {
        String testClassName = "FeatureEnvyMethod";
        String testMethodName = "mostLocal";
        List<Smell> smells = detector.detect(testMethods.get(testClassName).get(testMethodName));

        Assertions.assertEquals(0, smells.size());
    }

    @Test
    void FEMostForeign() {
        String testClassName = "FeatureEnvyMethod";
        String testMethodName = "mostForeign";
        List<Smell> smells = detector.detect(testMethods.get(testClassName).get(testMethodName));

        Assertions.assertEquals(1, smells.size());
        for (Smell smell : smells) {
            Assertions.assertEquals(SmellName.FeatureEnvy, smell.getName());
        }
    }
}
