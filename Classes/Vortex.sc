/*

Dependencies:
- kfilter
- Sleet
- Influx

TODO:

IDEAS:
- An event type for changing the influx via patterns

*/


/*

TODO:
- LFOs
- Analysis

*/

VortexVoice{
	classvar <instances=0, <voices;
	var <>dict, 
	<numChannels, 
	sleet, 
	<thisServer,

	<name,

	// Exclude from influx influence
	excludeParams,

	// Important indexes in the NodeProxy
	mixIndex=10, // Mixers start here
	fxIndex=100, // Fx chain starts here
	timeIndex=1000, // Timemachine effect is here
	protectionIndex=1001; // DC filter and limiter here;

	*initClass{
		Class.initClassTree(KFilter);
		Class.initClassTree(Sleet);
	} 

	*new { |server, voicename, numChans=2, time=8|
		^super.new.init(server, voicename, numChans, time)
	}

	// TODO
	*writeAll{}

	init{|server, voicename, numChans, time|

		// Global dictionary for voice management
		voices = voices ?? ();

		thisServer = server ?? Server.default;
		numChannels = numChans;
		name = voicename ?? "vortvoice%".format(instances).asSymbol;
		instances = instances + 1;

		// Local dictionary 
		dict = (
			name: name,
			influx: nil, // Influx
			env: nil,	
			timebuffer: nil,
			nodeproxy: nil, // Sound process and mixer
			lfos: [], // ?
			analysis: [],
			fxpatcher: nil
		);


		// Make bundle to make sure everything happens in order
		fork{
			thisServer.sync;

			sleet = Sleet.new(numChannels: numChannels);
			thisServer.sync;

			// Allocate time buffer. This can be finicky: If not done before
			// initializing Ndef, problems may arise
			this.allocBuf(time: time, sampleRate: 48000);
			thisServer.sync;
			"Buffer allocation step done".postln;

			// Nodeproxy setup
			this.initNodeproxy(fadeTime:1);
			thisServer.sync;
			"Nodeproxy init step done".postln;

			this.initFxpatcher;
			this.initTimemachine;
			this.initProtection;
			thisServer.sync;
			"Vortex patching step done".postln;

			// Data setup
			this.initInflux;
			thisServer.sync;
			"Influx init step done".postln;

			// Add to global directory
			voices.put(name.asSymbol, this);

		};

		^this
	}

	allocBuf{|time=16, sampleRate=48000|
		var buf = Buffer.alloc(thisServer, sampleRate * time, numChannels ?? 2);

		dict.timebuffer = buf;

		^buf	
	}

	exclusionParams{
		var protection = "wet%".format(protectionIndex).asSymbol;
		var invol = \invol;
		var in = \in;

		var record = this.p("record", timeIndex);
		var buffer = this.p("buffer", timeIndex);

		excludeParams = [
			in, 
			protection, \limiterlevel, \limiterdur,
			record,
			buffer,
			invol
		];

		^excludeParams
	}

	okParams{
		^dict.nodeproxy.controlKeys(
			except: this.exclusionParams
		);
	}

	// Param formatting
	p {|name, index|
		^"%%".format(name, index).asSymbol
	}

	initFxpatcher{
		var defaultChain = [\delay, \pitchshift];
		dict.fxpatcher = SleetPatcher.new(dict.nodeproxy, defaultChain, fxIndex);
		^dict.fxpatcher
	}

	initNodeproxy{|fadeTime=1|
		dict.nodeproxy = Ndef(name);
		dict.nodeproxy.mold(numChannels, 'audio');
		dict.nodeproxy.fadeTime_(fadeTime);

		// // Add source sound function
		dict.nodeproxy.source_(this.defaultSource);
	}

	defaultSource {
		^{|in=#[ 0, 0 ], invol=0.75|
			SoundIn.ar(in, invol)
		}
	}

	initTimemachine{
		var initPlayrate = rrand(0.1,1.0);
		var recordOnInit = 1.0;
		var timerate, record, buffer;

		// Add timemachine function to nodeproxy 
		dict.nodeproxy[timeIndex] = \kfilter -> sleet.get('timemachine_ext');

		// Initial settings
		timerate = this.p("timerate", timeIndex);
		record = this.p("record", timeIndex);
		buffer = this.p("buffer", timeIndex);

		/*
		TODO:
		MAKE SURE SERVER IS BOOTED + BUFFER ALLOCATED
		*/


		dict.nodeproxy.set(
			timerate, initPlayrate, 
			record, recordOnInit, 
			buffer, dict.timebuffer
		);
	}

	initProtection{
		dict.nodeproxy[protectionIndex] = \filter -> {|in, limiterlevel=0.95, limiterdur=0.01|
			LeakDC.ar(Limiter.ar(in, limiterlevel, limiterdur))

		}
	}

	initInflux{|ins=2, outs=32|
		var params = this.okParams;
		dict.influx = VortexFlux.new(
			ins, 
			outs
		).initDataWarping;
		dict.env = dict.influx.env;
		
		// Set default range
		// dict.influx;
		// Attach to NodeProxy
		dict.influx.attachMapped(
			dict.nodeproxy, 
			paramNames: params
		);

		^dict.influx
	}
	// Convenience functions
	play{
		^dict.nodeproxy.play
	}

	stop{
		^dict.nodeproxy.stop
	}

	set{|...args|
		^dict.influx.set(*args)
	}

	// Save buffer contents to file
	write{|to="~"|
		var filename = "%_timebuffer_%.wav".format(name, Date.getDate.stamp);

		// Resolve path
		to = to.asAbsolutePath;

		// Make filename 
		to = to +/+ filename;

		"Saving timebuffer: %".format(to).postln;

		dict.timebuffer.write(to, headerFormat: "wav")
	}

	plotTime{
		dict.timebuffer.plot
	}

	plotEnv{
		dict.env.plot
	}

	testEnv{
		dict.env.test
	}

	influx{
		^dict.influx
	}

	timebuffer{
		^dict.timebuffer
	}
	 
	nodeproxy{
		^dict.nodeproxy
	}
	 
	env{
		^dict.env
	}

	status{
		"Status of %".format(name).postln;
		dict.nodeproxy.getKeysValues.do{|argument| 
			var k = argument[0];
			var v = argument[1];
			"\t%: %".format(k, v).postln
		}
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
