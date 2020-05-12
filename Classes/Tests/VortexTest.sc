VortexTest1 : UnitTest {
	test_check_classname {
		var result = Vortex.new;
		this.assert(result.class == Vortex);
	}
}


VortexTester {
	*new {
		^super.new.init();
	}

	init {
		VortexTest1.run;
	}
}
