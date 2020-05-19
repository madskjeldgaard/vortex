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
			this.makeLfo(conNum)
		};

		this.addToInflux(influx);

		if(feedback, {
			this.feedbackPatch;
		});

		^this
	}

	makeLfo{|lfoNum=0, kind=\saw|
		var initPhase = 4pi.rand2;
		var lfoFunc;
		var lfo = NodeProxy.new(rate: 'control',  numChannels: 1);

		lfoFunc = lfoFuncs.choose;

		lfo.source = lfoFunc; 

		^lfo.set(\freq, 0.15.rand2);
	}

	addToInflux{|influx, freqScale = 0.1|
		influx.action.add('setOcean', {|i|
			var outVals = i.outValDict;

			// Iterate over influx values
			outVals.keysValuesDo{|outName, value, index|
				// Used for wrapping
				var modBy = if(lfos.size > outVals.size, { outVals.size }, { lfos.size });

				// Get lfo for index (wrapped)
				var lfo = lfos[index % modBy];

				// Set using influx value
				lfo.set(\freq, value * freqScale)
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

	feedbackPatch{|lag=4|
		"Applying feedback patch".postln;

		lfos.do{|lfo, lfoNum|
			// Filter out the lfo receiving the data from other lfo
			var filteredlfos = lfos.reject({|thislfo, i| i == lfoNum});
			var chosenlfo = filteredlfos.choose;

			lfo.lag(\feedback, lag);
			lfo.map(\feedback, chosenlfo)
		}
	}
}
