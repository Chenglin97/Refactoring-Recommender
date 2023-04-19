package cmu.csdetector.smells;

import cmu.csdetector.resources.Type;
import cmu.csdetector.smells.detectors.GodClass;
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

public class GodClassTest extends SmellTest {
    private GodClass detector = new GodClass();

    @Test
    void GCBlobClassSample() {
        String testClassName = "BlobClassSample";
        List<Smell> smells = detector.detect(testTypes.get(testClassName));

        Assertions.assertEquals(1, smells.size());
        for (Smell smell : smells) {
            Assertions.assertEquals(SmellName.GodClass, smell.getName());
        }
    }

    @Test
    void GCBrainClassWithOneBrainMethod() {
        String testClassName = "SuperDummy";
        List<Smell> smells = detector.detect(testTypes.get(testClassName));

        Assertions.assertEquals(0, smells.size());
    }
}
