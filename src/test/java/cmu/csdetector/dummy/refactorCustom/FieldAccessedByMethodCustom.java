package cmu.csdetector.dummy.refactorCustom;

import org.apache.commons.io.FileUtils;

public class FieldAccessedByMethodCustom extends SuperDummyCustom {
	protected String fieldAccessedByMethodCustomAttribute = "FieldAccessedByMethodCustomAttribute";
	private String b;
	
	private Object c;
	
	private Object d;

	private String e;
	
	private FileUtils noBinding;
	
	@Override
	protected void a() {
		super.a();
	}
	
	private void f() {
		
	}
	
	void g() {
		this.f();
	}
	
	protected void h() {
		System.out.println(d);
	}

	public void i() {
		int kkk = 0;
		System.out.println(kkk);
		FileUtils a = noBinding;
		System.out.println(this.b + c + super.a + a);
	}
	
	public void j() {
		System.out.println(e);
	}
	
	public void k() {
		System.out.println(c + e);
	}
}
