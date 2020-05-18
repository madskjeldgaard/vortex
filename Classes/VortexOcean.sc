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
		var name = "vortex_fluid_lfo%".format(lfoNum).asSymbol;

		Ndef(name, {|freq=0.1, amp=1| 
			SinOsc.kr(freq, initPhase, amp)
		});

		^Ndef(name).set(\freq, 2.0.rand2);
	}

	addToInflux{|influx|
		influx.action.add('setOcean', {|i|
			var outVals = i.outValDict;

			outVals.keysValuesDo{|outName, value, index|
				var modBy = if(lfos.size > outVals.size, { outVals.size }, { lfos.size });
				var lfo = lfos[index % modBy];

				lfo.set(\freq, value)
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
			var lfo = lfos[argNum % lfos.size];

			"Mapping lfo % to arg %".format(lfo.key, a).postln;

			[a, lfo]
		}; 

		ndef.map(*argArray.flatten)

		// lfos.do{|lfo, lfoNum| }

	}
}
