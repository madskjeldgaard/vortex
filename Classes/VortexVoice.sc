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

	<ocean,

	// Exclude from influx influence
	excludeParams,

	// Important indexes in the NodeProxy
	<mixIndex=10, // Mixers start here
	<fxIndex=100, // Fx chain starts here
	<timeIndex=1000, // Timemachine effect is here
	<protectionIndex=1001,
	<panIndex=1005; // DC filter and limiter here;

	*initClass{
		Class.initClassTree(KFilter);
		Class.initClassTree(Sleet);

		
	} 

	*new { |server, voicename, numChans=2, time=8, fluid=true|
		^super.new.init(server, voicename, numChans, time, fluid)
	}

	init{|server, voicename, numChans, time, fluid|
		// Load vortex event types
		VortexEvent.new;

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

			VortexPan.new(numInchannels: numChannels, numOutchannels: numChannels, voice: this);
			thisServer.sync;
			"Vortex pan adder step done".postln;

			// Data setup
			this.initInflux(ins:2, outs: 32, fluid: fluid);
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
		var protection = this.p("wet", protectionIndex);
		var finalVol = \finalVol;//this.p("finalVol", protectionIndex);
		var invol = \invol;
		var in = \in;

		var record = this.p("record", timeIndex);
		var buffer = this.p("buffer", timeIndex);

		excludeParams = [
			in, 
			protection, \limiterlevel, \limiterdur,
			finalVol,
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
		var defaultChain = [\delay, \pitchshift, \chorus, \freqshift, \phaser, \filter];
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
		dict.nodeproxy[protectionIndex] = \filter -> {|in, limiterlevel=0.95, limiterdur=0.01, finalVol=0.1|
			LeakDC.ar(Limiter.ar(finalVol * in, limiterlevel, limiterdur))

		}
	}

	initInflux{|ins=2, outs=32, fluid=false|
		var params = this.okParams;

		// Create influx
		dict.influx = VortexFlux.new(
			ins, 
			outs
		).initDataWarping;

		// Pointer to influx envelope
		dict.env = dict.influx.env;
		
		// Attach to NodeProxy
		if(fluid, {
			ocean = VortexOcean.new(dict.influx);
			ocean.attachLfosMapped(dict.nodeproxy, paramNames: params)
		}, {
			dict.influx.attachMapped(
				dict.nodeproxy, 
				paramNames: params
			);


		})
		
		^dict.influx
	}

	rand{|randMax=1.0|
		dict.influx.randomizeIns(randMax)
	}	

	reattach{
		var params = this.okParams;

		// Attach to NodeProxy
		dict.influx.attachMapped(
			dict.nodeproxy, 
			paramNames: params
		);

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

	startRecording{
		var recordparam = this.p("record", timeIndex);
		dict.nodeproxy.set(recordparam, 1.0);
		"Started recording in %".format(name).postln;
	}

	stopRecording{
		var recordparam = this.p("record", timeIndex);
		dict.nodeproxy.set(recordparam, 0.0);
		"Stopped recording in %".format(name).postln;
	}

	// Save buffer contents to file
	vortexRecordingsDir{
		var p = Platform.recordingsDir +/+ "vortex";

		PathName(p).isFolder.if({
			"touch %/yo.txt".format(p).unixCmdGetStdOut.postln 
		},{
			"mkdir %".format(p).unixCmdGetStdOut.postln 
		});

		^p
	}

	write{|to|
		var filename = "%_timebuffer_%.wav".format(name, Date.getDate.stamp);

		// Resolve path
		to = to ?? this.vortexRecordingsDir;
		to = to.asAbsolutePath;

		// Make filename 
		to = to +/+ filename;

		"Saving timebuffer: %".format(to).postln;

		dict.timebuffer.write(to, headerFormat: "wav")
	}

	*writeAll{|to|
		this.voices.do{|v|
			v.write(to)
		}
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

	clearBuffer{
		var buffer = dict.timebuffer;
		var numFrames = buffer.numFrames;
		buffer.sendCollection(Array.fill(numFrames, { 0 }));

		^buffer
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
