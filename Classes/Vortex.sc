Vortex{
	var <voices;

	*new { | server, numVortices=2, numChannels=2 |
		^super.new.init(server, numVortices, numChannels)
	}

	init { | server, numVortices, numChannels |

		numVortices.do{|i|
			VortexVoice.new(server,  voicename: nil,  numChans: numChannels,  time: 8)
		};

		voices = VortexVoice.voices;

		this.mixVoices;
	}

	play{
		voices.do{|v|
			v.play
		}
	}

	stop{
		voices.do{|v|
			v.stop
		}
	}

	buffers{
		var buffers = [];

		voices.do{|v|
			buffers = buffers.add(v.timebuffer)
		};

		^buffers
	}

	randAll{|randMax=1.0|
		voices.do{|v|
			v.rand(randMax)
		}
	}

	// Mix voices together
	mixVoices{
		var voiceArray = [];
		var numVoices = voices.size;

		// Convert to array for easier mixing
		voices.keysValuesDo{|voicename, voice, voxnum|
			voiceArray = voiceArray.add(voice);
		};

		// Then iterate over array of voices and mix them together
		voiceArray.do{|voice, voxnum|

			// Filter out the voice index of this one
			var filteredindices = (0..numVoices-1).reject({|i| i == voxnum});

			// And mix the rest of the voices in
			filteredindices.do{|otherVoxNum, index|
				var thisVoice = voice;
				var mixinVoice = voiceArray[otherVoxNum % numVoices];

				"Mixing in voice % in %".format(thisVoice.nodeproxy.key, mixinVoice.nodeproxy.key).postln;

				index = index + 1; // Start from 1 to not overwrite source
				thisVoice.nodeproxy[index] = \mix -> { mixinVoice.nodeproxy.ar };
			};

			// Reattach influx
			voice.reattach
		}

	}

}

// Networked computers providing extra audio muscle power
NetworkedDevice{}
