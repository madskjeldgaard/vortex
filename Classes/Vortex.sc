// TODO
Vortex{

	*new { | server, numVortices=2, numChannels=2 |
		^super.new.init(server, numVortices, numChannels)
	}

	init { | server, numVortices, numChannels |

		numVortices.do{|i|
			VortexVoice.new(server,  voicename: nil,  numChans: numChannels,  time: 8)
		};

		this.mixVoices;
	}

	// Mix voices together
	mixVoices{
		var voiceArray = [];
		var numVoices = VortexVoice.voices.size;

		// Convert to array for easier mixing
		VortexVoice.voices.keysValuesDo{|voicename, voice, voxnum|
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
			}
		}

	}

}

// Networked computers providing extra audio muscle power
NetworkedDevice{}
