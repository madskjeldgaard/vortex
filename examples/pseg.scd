v = VortexVoice.new()

v.env.plot

v.regenerateEnv

p = Particular.new(2);

Pdef.all.clear


// Envelope test
(
	Pdef(\p, Pbind(
		\dur, v.pseg(durStretch: Pwhite(4,12), minVal:0.01, maxVal:0.5), 
		\sustain, Pkey(\dur), 
		\instrument, p.def(), 
		\freq, 0.7,
		\amp, 1.0
	)
).play;

Pdef(\p2, 
	Pbindf(
		Pdef(\p), 
		\freq, Pkey(\freq) + 0.02, 
		\pan, 1.0
	)
).play;
)
