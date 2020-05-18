(
var events = ();

events.vortexbuf = {
	~type = \note;
	// ~buffer = voice.timebuffer;

	currentEnvironment.play;
};

events.vortexset = {
	~type = \note;
	currentEnvironment.play;
};

events.keysValuesDo{|name, ev|
	"adding % event type".format(name).postln;
	Event.addEventType(name.asSymbol, ev);
};

events
)
