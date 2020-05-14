/*
*/

// TODO
// Base class
Vortex{
	var <sleet, <modality, <buffers, <voices, <manglers;

	// Mix voices together
	entangleVoices{

		/*

		self.numVoices.do{|voxnum|

			// Mix in all other voices (rejecting this one)
			(0..self.numVoices-1).reject({|i| i == voxnum}).do{|otherVoxNum, index|
				var thisVoice = "voice%".format(voxnum).asSymbol;
				var mixinVoice = "voice%".format(otherVoxNum).asSymbol;

				index = index + 1; // Start from 1 to not overwrite source

				Ndef(thisVoice)[index] = \mix -> { Ndef(mixinVoice).ar };

				// Set mix in parameter to 0 by default 
				Ndef(thisVoice).set("mix%".format(index).asSymbol, 0)
			};
		}

		*/

	}
}

// Networked computers providing extra audio muscle power
NetworkedDevice{}
