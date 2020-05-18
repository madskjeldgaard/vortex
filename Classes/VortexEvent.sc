VortexEvent{
	classvar loaded=false, <events;

	*new {
		^super.new.init()
	}

	init {
		var classpath = Main.packages.asDict.at('Vortex');
		if(loaded.not, {
			"Loading Vortex events ".postln;
			loaded = true;
			(classpath +/+ "events/vortexevent.scd").load;
		}, {
			"Vortex events already loaded".postln;
		})
	}
}

