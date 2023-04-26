package cmu.csdetector.dummy.refactorCustom;


public class FeatureEnvyMethodCustom {

    private String fPrivate;

    private FieldAccessedByMethodCustom foreign1;

    private RefusedBequestSampleCustom foreign2;

    public FeatureEnvyMethodCustom() {
        foreign1 = new FieldAccessedByMethodCustom();
        foreign2 = new RefusedBequestSampleCustom();
    }

    protected String getfPrivate() {
        return fPrivate;
    }

    public void localA() {
        this.fPrivate = "";
    }

    public void localB() {
        System.out.println("");
        this.fPrivate = "";
    }

    public void localC() {
        new String().length();
        this.fPrivate = "";
    }

    public void localD() {
        Object.class.getName().toLowerCase().toUpperCase().toLowerCase().trim().length();
    }

    public void superLocal() {
        this.localA();
        this.localB();
        this.localC();
        this.localD();
        new String().toCharArray();
        new String().toLowerCase();
        Integer.bitCount(12);
        new String().toCharArray();
        new String().toLowerCase();
        Integer.bitCount(12);
        new String().toCharArray();
        new String().toLowerCase();
        Integer.bitCount(12);
    }

    public void superForeign() {
        String foreign1Attribute1 = foreign1.fieldAccessedByMethodCustomAttribute;
        System.out.println(foreign1Attribute1);
        String foreign1Attribute2 = foreign1.fieldAccessedByMethodCustomAttribute;
        System.out.println(foreign1Attribute2);
        String foreign1Attribute3 = foreign1.fieldAccessedByMethodCustomAttribute;
        System.out.println(foreign1Attribute3);
        String foreign1Attribute4 = foreign1.fieldAccessedByMethodCustomAttribute;
        System.out.println(foreign1Attribute4);
    }

    public void mostLocal(int a, int b) {
        this.localA();
        this.localB();
        this.localC();
        this.localD();
        foreign1.a();
        foreign1.b();
        foreign2.b();
        new String().toCharArray();
        new String().toLowerCase();
        Integer.bitCount(12);
    }

    public void mostForeign(int a, int b, int c) {
        this.mostLocal(a, b);
        this.localD();
        new String().toCharArray();
        new String().toLowerCase();
        Integer.bitCount(12);
        foreign1.a();
        foreign1.b();
        foreign1.b();
        foreign1.b();
        foreign1.b();
        foreign2.b();
    }
}
