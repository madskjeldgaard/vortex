/*

(
o = VortexOcean.new(~influx);

Ndef(\yoyo, {|p1, p2, p3| [p1, p2, p3].poll });

o.attachLfosMapped(Ndef(\yoyo));
)

*/

VortexOcean{
	var <lfos, <numConnections, <influx;

	*new { | influxInstance|
		^super.new.init(influxInstance)
	}

	init { | influxInstance|
		influx = influxInstance;

		numConnections = influx.outNames.size;

		lfos = numConnections.collect{|conNum| 
			this.makeLfo(conNum)
		};

		this.addToInflux(influx);

		^this
	}

	makeLfo{|lfoNum=0|
		var initPhase = 4pi.rand2;
		var lfo = NodeProxy.new(rate: 'control',  numChannels: 1);

		lfo.source = {|freq=0.1, amp=1| 
			// SinOsc.kr(freq, initPhase, amp)
			LFSaw.kr(freq, initPhase, amp)
			// LFNoise2.kr(freq, amp)
		};

		^lfo.set(\freq, 0.015.rand2);
	}

	addToInflux{|influx, freqScale = 0.1|
		influx.action.add('setOcean', {|i|
			var outVals = i.outValDict;

			outVals.keysValuesDo{|outName, value, index|
				var modBy = if(lfos.size > outVals.size, { outVals.size }, { lfos.size });
				var lfo = lfos[index % modBy];

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
}
