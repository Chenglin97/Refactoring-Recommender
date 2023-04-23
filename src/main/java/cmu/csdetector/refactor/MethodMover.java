package cmu.csdetector.refactor;

import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.resources.Resource;

import java.util.ArrayList;

public class MethodMover {
    public Resource moveMethod(Resource method, Resource source_class, ArrayList<Resource> classes) {
        // Resource source_class = method.getBelongingClass();
        Double source_lcom3 = source_class.getMetricValue(MetricName.LCOM3);
        for (Resource target_class: classes) {
            Resource old_class = target_class;
            Double old_class_lcom3 = old_class.getMetricValue(MetricName.LCOM3);
            Resource new_class = simulateMove(method, target_class);
            Double new_class_lcom3 = new_class.getMetricValue(MetricName.LCOM3);
            Double new_source_lcom_3 = source_class.getMetricValue(MetricName.LCOM3);
            if (new_class_lcom3 < old_class_lcom3 && new_source_lcom_3 < source_lcom3) {
                return new_class;
            }
        }
        return null;
    }

    public Resource simulateMove(Resource method, Resource target_class) {
        // TODO: implement this method
        return target_class;
    }
    public Resource getBelongingClass(Resource method) {
        return null;
    }
    // 0. Imagine we have a method to move
    // 1. get the class with the highest number of calls
    // 2. move the method to that class
    // 3. run feature envy detector again
    // not for now but maybe for the future: if the method is still feature envy, move it to the next class
    // not for now but maybe for the future: repeat until the method is not feature envy anymore
    // return the name of the class to move the method
}
