package cmu.csdetector.refactor;
import cmu.csdetector.resources.Resource;

public class ResourcePair {
    private Resource targetClassCopy;
    private Resource sourceClassCopy;

    public ResourcePair(Resource targetClassCopy, Resource sourceClassCopy) {
        this.targetClassCopy = targetClassCopy;
        this.sourceClassCopy = sourceClassCopy;
    }

    public Resource getTargetClassCopy() {
        return targetClassCopy;
    }

    public Resource getSourceClassCopy() {
        return sourceClassCopy;
    }
}