/*

(
o = VortexOcean.new(~influx);

Ndef(\yoyo, {|p1, p2, p3| [p1, p2, p3].poll });

o.attachLfosMapped(Ndef(\yoyo));
)

*/

VortexOcean{
	var <lfos, <lfoFuncs, <numConnections, <influx;

	*new { | influxInstance, feedback=true|
		^super.new.init(influxInstance, feedback)
	}

	init { | influxInstance, feedback|
		var classpath = Main.packages.asDict.at('Vortex');
		lfoFuncs = (classpath +/+ "lib/lfos.scd").load;

		influx = influxInstance;

		numConnections = influx.outNames.size;

		lfos = numConnections.collect{|conNum| 
			this.makeLfo(conNum, \varsaw)
		};

		this.addToInflux(influx, valScale: 2.0, parameter:\amp);

		if(feedback, {
			// this.feedbackPatch(lag: 1, param: \feedback);
			this.feedbackPatch(lag: 1, param: \width);
		});

		^this
	}

	makeLfo{|lfoNum=0, kind=\noisesaw|
		var initPhase = 4pi.rand2;
		var lfoFunc;
		var lfo = NodeProxy.new(rate: 'control',  numChannels: 1);

		lfoFunc = lfoFuncs[kind];

		lfo.source = lfoFunc; 

		^lfo.set(\freq, rrand(0.01,1.0));
	}

	addToInflux{|influx, valScale = 0.1, parameter=\freq|
		influx.action.add('setOcean', {|i|
			var outVals = i.outValDict;

			// Iterate over influx values
			outVals.keysValuesDo{|outName, value, index|
				// Used for wrapping
				var modBy = if(lfos.size > outVals.size, { outVals.size }, { lfos.size });

				// Get lfo for index (wrapped)
				var lfo = lfos[index % modBy];

				// Set using influx value
				lfo.set(parameter, value * valScale)
			}
		})
	}

	attachLfosMapped{|ndef, paramNames, specs|
		var args, argArray;
		// specs = if(specs.isNil, {
		// 	var dict = ();

		// 	paramNames.do{|param|
		// 		dict.put(param, Influx.baseSpec)
		// 	};

		// 	dict

		// }, { specs });

		args = paramNames ? ndef.controlKeys;
		argArray = args.collect{|a, argNum|
			var lfoNum = argNum % lfos.size;
			var lfo = lfos[lfoNum];

			"Mapping lfo % to arg %".format(lfoNum, a).postln;

			[a, lfo]
		}; 

		ndef.map(*argArray.flatten)

		// lfos.do{|lfo, lfoNum| }

	}

	// TODO
	feedbackPatch{|lag=0.14, param=\feedback|
		"Applying feedback patch".postln;
		fork{
			lfos.do{|lfo, lfoNum|
				// Filter out this lfo
				var filteredlfos = lfos.reject({|thislfo, i| i == lfoNum});

				// Randomly choose lfo from filtered
				var chosenlfo = filteredlfos.choose;

				Server.default.sync;

				lfo.lag(param, lag);
				lfo.map(param, chosenlfo)
			}
		}

	}
}
