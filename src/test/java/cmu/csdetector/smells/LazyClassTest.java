package cmu.csdetector.smells;

import cmu.csdetector.smells.detectors.LazyClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class LazyClassTest extends SmellTest {
    private LazyClass detector = new LazyClass();

    @Test
    void LCSuperDummy() {
        String testClassName = "SuperDummy";
        List<Smell> smells = detector.detect(testTypes.get(testClassName));

        Assertions.assertEquals(1, smells.size());
        for (Smell smell : smells) {
            Assertions.assertEquals(SmellName.LazyClass, smell.getName());
        }
    }

    @Test
    void LCBlobClassSample() {
        String testClassName = "BlobClassSample";
        List<Smell> smells = detector.detect(testTypes.get(testClassName));

        Assertions.assertEquals(0, smells.size());
    }
}
