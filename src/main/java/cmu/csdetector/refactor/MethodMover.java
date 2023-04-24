package cmu.csdetector.refactor;

import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.metrics.calculators.type.LCOM3Calculator;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Resource;
import cmu.csdetector.resources.Type;

import java.util.ArrayList;

public class MethodMover {
    public Resource moveMethod(Method method, Resource source_class, ArrayList<Type> classes) {
        // returns the class the method should be moved to
        // Resource source_class = method.getBelongingClass();
        LCOM3Calculator lcom3Calculator = new LCOM3Calculator();
        Double old_source_lcom_3 = source_class.getMetricValue(MetricName.LCOM3);
        System.out.println("old source lcom3: " + old_source_lcom_3);
        for (Resource target_class: classes) {
            Double old_target_lcom_3 = target_class.getMetricValue(MetricName.LCOM3);

            Double new_source_lcom_3 = lcom3Calculator.calculateWithoutMethod(source_class, method);
            Double new_target_lcom_3 = lcom3Calculator.calculateWithAdditionalMethod(target_class, method);
            System.out.println("target class: " + target_class.getFullyQualifiedName());
            System.out.println("new source lcom3: " + new_source_lcom_3);
            System.out.println("old target lcom3: " + old_target_lcom_3);
            System.out.println("new target lcom3: " + new_target_lcom_3);

            if (new_target_lcom_3 + new_source_lcom_3 < old_source_lcom_3 + old_target_lcom_3) {
                return target_class;
            }
        }
        return null;
    }



}
