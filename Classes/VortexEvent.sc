VortexEvent{
	classvar loaded=false, <events;

	*new {
		^super.new.init()
	}

	init {
		var classpath = Main.packages.asDict.at('Vortex');
		var result;
		if(loaded.not, {
			"Loading Vortex events ".postln;
			loaded = true;
			result = (classpath +/+ "lib/vortexevent.scd").load;
			^result
		}, {
			"Vortex events already loaded".postln;
		});
	}
}

