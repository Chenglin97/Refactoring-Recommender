package cmu.csdetector.metrics.calculators.method;


import cmu.csdetector.graph.CallGraph;
import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.metrics.calculators.MetricValueCalculator;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.HashSet;
import java.util.Set;

/**
 * Computed the changing class metric value for a method. It is
 * defined as the number of classes in which the methods that call
 * the measured method are defined in
 *  
 * @author Diego Cedrim
 */
public class ChangingClassesCalculator extends MetricValueCalculator {

	@Override
	protected Double computeValue(ASTNode target) {
		MethodDeclaration method = (MethodDeclaration)target;
		IMethodBinding binding = method.resolveBinding();
		if (binding == null) {
			//TODO log!
			return null;
		}
		
		CallGraph callGraph = CallGraph.getInstance();
		Set<IMethodBinding> callers = callGraph.getCallers(binding);
		Set<ITypeBinding> typesOfCallers = new HashSet<>();
		for (IMethodBinding caller : callers) {
			ITypeBinding typeBinding = caller.getDeclaringClass();
			if (typeBinding == null) {
				//TODO LOG!
				continue;
			}
			typesOfCallers.add(typeBinding);
		}
		return new Double(typesOfCallers.size());
	}

	@Override
	public MetricName getMetricName() {
		return MetricName.ChangingClasses;
	}

}
