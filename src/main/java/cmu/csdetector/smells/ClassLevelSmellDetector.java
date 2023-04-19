package cmu.csdetector.smells;

import cmu.csdetector.smells.detectors.ComplexClass;
import cmu.csdetector.smells.detectors.GodClass;
import cmu.csdetector.smells.detectors.LazyClass;

public class ClassLevelSmellDetector extends CompositeSmellDetector {
	
	public ClassLevelSmellDetector() {
		addDetector(new ComplexClass());
		addDetector(new GodClass());
		addDetector(new LazyClass());
	}

	@Override
	protected SmellName getSmellName() {
		return null;
	}

}
