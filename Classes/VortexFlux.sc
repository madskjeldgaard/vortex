// Organize Influx stuff here
VortexFlux : Influx{
	var <env;

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
		this.addProc(\base, {|val|
			env.at(val.biuni)
		});
	}

	regenerateEnv{|numSteps = 8, rand = 1.0, maxCurve = 3.0|
		env = this.warpingEnv(numSteps: numSteps, rand: rand, maxCurve: maxCurve);
		^env
	}

	pseg{|durStretch=1, minVal=(-1.0), maxVal=1.0, reverse=false, repeats=inf|
		var levels = env.levels.linlin(-1.0, 1.0, minVal, maxVal);
		var times = env.times;
		var curves = env.curves;
		var c = if(
			curves.isSequenceableCollection.not, { 
				curves 
			}, { 
				Pseq(curves, inf) 
			}
		);

		levels = if(reverse, { levels.reverse }, { levels });

		^Pseg.new(Pseq(levels, inf), durStretch * Pseq(times, inf), c, repeats)
	}
}