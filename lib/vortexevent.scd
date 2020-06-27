// Event type for VortexVoice control
(
var name = \vortex;
var ev = {
	// ~type = \note;

	~voice = ~voice ? Vortex.voices[0];
	~x = ~x ? 0.5;
	~y = ~y ? 0.5;
	~z = ~z ? 0.5;
	~play = ~play ? 1;
	~record = ~record ? 1;

	// Monitor voice
	if(~play > 0, {
		~voice.play
	}, {
		~voice.stop
	});

	// Record to internal buffer
	if(~record > 0, {
		~voice.startRecording
	}, {
		~voice.stopRecording
	});

	// Randomize parameters, or use values from keys
	if(~rand.notNil, {
		~voice.rand(~rand);
	}, {
		~voice.set(\x, ~x, \y, ~y, \z, ~z);
	});

	// currentEnvironment.play;
};

"Making event type %".format(name).postln;

Event.addEventType(name, ev);
)
