/*
- Event types for changing the influx via patterns
*/
(

// Play Vortex' timebuffers in events
Event.addEventType(\vortexbuf, {|server|
	~type = \note;
	~buffer = voice.timebuffer;

	currentEnvironment.play;
});

/*

Test \vortexbuf

Pbind(\type, \vortexbuf).play;

*/

// Set Vortex voice
Event.addEventType(\vortexset, {|server|
	~type = \note;
	currentEnvironment.play;
});

/*

Test \vortexset

Pbind(\type, \vortexset).play;

*/

)
