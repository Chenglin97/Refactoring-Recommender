package cmu.csdetector.dummy.refactoredSmells;

public class FeatureEnvyMethodRF {
	// refactored so that super foreign is not in class

	private String fPrivate;

	private FieldAccessedByMethodRF foreign1;

	private RefusedBequestSampleRF foreign2;

	public FeatureEnvyMethodRF() {
		foreign1 = new FieldAccessedByMethodRF();
		foreign2 = new RefusedBequestSampleRF();
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
