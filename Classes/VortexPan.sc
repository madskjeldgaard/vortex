VortexPan{
	var <numInchannels, <numOutchannels, <panFunc;

	*new { | numInchannels=2, numOutchannels=2, voice|
		^super.newCopyArgs(numInchannels, numOutchannels).init(numInchannels, numOutchannels, voice)
	}

	init { | numInchannels, numOutchannels, voice|
		var classpath = Main.packages.asDict.at('Vortex');
		var panner = (classpath +/+ "lib/panfunctions.scd").load;
		var index = voice.panIndex;

		voice.nodeproxy[index] = \kfilter -> panner.value(inchans: numInchannels, outchans: numOutchannels);
	}
}
