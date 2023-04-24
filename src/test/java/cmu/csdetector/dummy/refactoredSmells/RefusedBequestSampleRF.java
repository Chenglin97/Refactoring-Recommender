package cmu.csdetector.dummy.refactoredSmells;

public class RefusedBequestSampleRF extends SuperDummyRF {

	@Override
	protected void a() {
		super.a();
	}

	@Override
	protected void b() {
		super.b();
	}

	@Override
	protected void c() {
		super.c();
	}

	public void superForeign(int a) {
		FieldAccessedByMethodRF fieldAccessedByMethodRF = new FieldAccessedByMethodRF();
		fieldAccessedByMethodRF.a();
		fieldAccessedByMethodRF.b();
		fieldAccessedByMethodRF.c();
		new String().toCharArray();
		new String().toLowerCase();
		Integer.bitCount(12);
	}
}
