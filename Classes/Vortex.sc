/*

Dependencies:
- kfilter
- Sleet
- Influx

TODO:
- dynamic fx patching

IDEAS:
- An event type for changing the influx via patterns

*/

VortexVoice{
	var <>dict, <numChannels, sleet;

	*new { |numChans=2, time=8|
		^super.new.init(numChans, time)
	}

	init{|numChans, time|
		numChannels = numChans;
		sleet = Sleet.new(numChannels: numChannels);

		// Everything is stored here
		dict = (
			influx: nil, // Influx
			env: nil,	
			timebuffer: Buffer.alloc(Server.local, 48000 * time, numChannels),
			nodeproxy: nil, // Sound process and mixer
			lfos: [], // ?
			analysis: [],
		).postln;

		// Data setup
		this.initInflux;
		this.initDataWarping;

		// Nodeproxy setup
		this.initNodeproxy(fadeTime:1);
		this.initDefaultFxPatch;
		this.initTimemachine;
	}

	initNodeproxy{|fadeTime=1|
		// Initialise nodeproxy
		dict.nodeproxy = NodeProxy.new(
			server: Server.default,  
			rate: 'audio',  
			numChannels: numChannels
		);

		dict.nodeproxy.fadeTime_(fadeTime);

		// // Add source sound function
		dict.nodeproxy.source_(this.defaultSource);
	}

	defaultSource {
		^{|in=#[ 0, 0 ], invol=0.4|
			SoundIn.ar(in, invol)
		};
	}


	// TODO
	initDefaultFxPatch{

	}

	initTimemachine{
		var timeSlot = 1001;
		var initPlayrate = rrand(0.1,1.0);
		var recordOnInit = 0.0;

		// Add timemachine function to nodeproxy 
		dict.nodeproxy[timeSlot] = \filter -> sleet.get('timemachine_ext');

		// Initial settings
		dict.nodeproxy.set(
			\timerate, initPlayrate, 
			\record, recordOnInit, 
			\buffer, dict.timebuffer
		);
	}

	initInflux{|ins=2, outs=8|
		dict.influx = Influx.new(ins, outs);
	}

	// Reappropriated from Alberto De Campo's example included in the Influx package
	warpingEnv{ |numSteps = 8, rand = 1.0, maxCurve = 3.0|
		var numTimes = numSteps.max(2).round(2).asInteger;
		var levels = (numTimes + 1).collect { |i| 1.0.rand2.blend( (i / numTimes).unibi, 1-rand) };
		// bit boring to have them regular
		var times = (1/numTimes).dup(numTimes);
		var curves = numTimes.collect { maxCurve.rand2 };
		// put in fixed values
		levels[0] = -1;
		levels[numTimes div: 2] = 0;
		levels[numTimes] = 1;

		^Env(levels, times, curves);
	}

	initDataWarping{
		this.regenerateEnv;
		dict.influx.addProc(\base, {|val|
			dict.env.at(val.biuni)
		});
	}

	regenerateEnv{|numSteps = 8, rand = 1.0, maxCurve = 3.0|
		dict.env = this.warpingEnv(numSteps: numSteps, rand: rand, maxCurve: maxCurve);
		^dict.env
	}

	// Getter functions
	timebuffer{
		^dict.timebuffer
	}
	 
	nodeproxy{
		^dict.nodeproxy
	}
	 
	env{
		^dict.env
	}

	pseg{|durStretch=1, minVal=(-1.0), maxVal=1.0, repeats=inf|
		var levels = this.env.levels.linlin(-1.0, 1.0, minVal, maxVal);
		var times = this.env.times;
		var curves = this.env.curves;
		var c = if(curves.isSequenceableCollection.not) { curves } { Pseq(curves, inf) };

		^Pseg.new(Pseq(levels, inf), durStretch * Pseq(times, inf), c, repeats)
	}
}

// Main interface
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


// Dynamically switch between fx chains
// Uses Sleet to organize the fx themselves
// Might be moved into Sleet itself
FxPatcher{}

// Networked computers providing extra audio muscle power
NetworkedDevice{}

// Make a Modality device button blink in random pattern (to show that it's alive)
VortexButtonBlinker{
	*new{|button|
		^Task({ 
			var newVal, button, pat; 
			pat = Pseq(Array.rand(3, 0.125, 0.5), inf).asStream;
			loop{
				pat.next.wait;  

				if(
					button.value == 1.0, { 
						newVal = 0 
					}, { 
						newVal = 1 
					}
				);

				button.value_(newVal)
			}
		});
	}
}

InfluxTestGui{

	*new { |influx|
		^super.new.init(influx)
	}

	init{|influx|

		var w = Window.new("InfluxTestGui");
		var insliders = influx.inNames.collect{|inname| 
			Slider.new.action_({|obj|
				var val = obj.value;
				influx.set(inname, val.unibi)
			})
		};

		var sliders = influx.outNames.collect{|outname| 
			// VLayout(
			// 	StaticText.new.string_("out %".format(outname)), Slider.new
			// ) 
			Slider.new
		};
		var layout = VLayout(
			StaticText.new.string_("Influx inputs"),
			VLayout.new(*insliders),

			StaticText.new.string_("Influx outputs"),
			HLayout.new(*sliders)
		);

		w.layout = layout;

		w.front;

		// Add actions to influx

		influx.action.add(\setSliders, {|i|
			var vals = i.outValDict;

			vals.keysValuesDo{|key, value, valuenum|
				sliders[valuenum].value = value.biuni
			}
		})
	}

}
