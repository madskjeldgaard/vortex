(
Ndef(\sine1, {|amp=1, f=0.25, pan=0|
	Pan2.ar(SinOsc.ar(f.linexp(0.0,1.0,40.0,12000.0)) * amp, pan.linlin(0.0,1.0,-1.0,1.0))
});

~settings = ~settings ? ();

~getCurrent = {|ndef, except|
	var keysVals = ndef.getKeysValues(except: except);
	~settings.put(\current, keysVals);
	keysVals
};

~getSetting = {|name|
	if(name.isNil, {"No setting name".warn}, {
		~settings.at(name);
	})
};

~saveCurrent = {|ndef, settingName, overwrite=true|
	var thissetting = ~getCurrent.value(ndef);
	var key = settingName ? "%".format(Date.getDate.stamp); 

	

	key = if(~settings.at(key).notNil and: overwrite, { "%%".format(key,rand(1000,10000)) }, { key });
	key = key.asSymbol;

	"Saving preset %".format(key).postln;

	~settings.put(key, thissetting);
};

~loadSettings = {|ndef, settingName|
	var settings = ~settings.at(settingName);
	"Settings contain:".postln;
	settings.do{|pair| 
		var param, value;
		# param, value = pair;
		"%: %".format(param, value).postln;

		ndef.set(param, value)	
	};

	settings
};

// TODO
~writeSettings = {};

~randAll = {|ndef, maxRand=0.5|
	var args = ~getCurrent.(ndef);
	var argPairs;

	args.do{|pair|
		var param, value, newpair;

		#param, value = pair;

		ndef.set(param, rrand(0.0,maxRand));
	};
};

~morphEnv = {|env, morphStart=0.0, morphEnd=0.5, time=4|
	var maximum = env.levels.maxValue({|i| i });
	var minimum = env.levels.minValue({|i| i });

	// Rescale old values to new values
	env.levels = env.levels.linlin(
		minimum,
		maximum,
		morphStart,
		morphEnd
	);

	// Normalize times to match length requested in time arg
	env.times = env.times.normalizeSum * time;

	// Make sure it starts at morph start
	// env.levels[0] = morphStart;

	// And ends at morphEnd
	// env.levels[env.levels.lastIndex] = morphEnd;

	env
};

~morphTask = {|ndef, envelope, blendTo, time=4|
	Task({
		var env, envval;
		var timegrain = 0.01;

		// Update current
		~getCurrent.value(ndef);

		env = if(envelope.isNil, {  
			Env(levels:[0,1], times: [time], curve: \lin); 
		}, {  
			~morphEnv.value(env: envelope, morphStart:0.0, morphEnd: 1.0, time: time)
		});

		env = env.asPseg.asStream;
		envval = 0;

		while({envval.notNil}, {
			envval = env.next.postln; 
			// This extra control structure is to avoid returning nil
			if(envval.notNil, {
				~blendParams.value(ndef, blend: envval, name1: \current, name2: blendTo);
			});
			timegrain.wait; 
		})
	})
};

~morph = {|ndef, envelope, blendTo, time=4| 
	var task = ~morphTask.value(ndef: ndef, envelope: envelope, blendTo: blendTo, time: time);

	task.play;
};

~blendParams = {|ndef, blend=0.5, name1, name2|
	var thisSetting = if(name1.isNil, { 
		~getCurrent.value(ndef, except: nil);
	}, { 
		~getSetting.(name1) 
	});

	var thatSetting = ~getSetting.(name2);

	var blendedParams = thisSetting.collect{|pair|
		var thisParam, value, thatParam, thatValue, blended;
		# thisParam, value = pair;
		# thatValue = thatSetting.select{|pair| pair[0] == thisParam };
		thatValue = thatValue[1]; // Only get value from key-value pair
		blended = value.blend(thatValue, blend);

		"Blending param % from % to %: %".format(thisParam, value, thatValue, blended).postln;

		[thisParam, blended]
	};

	blendedParams.do{|pair|
		ndef.set(*pair)
	};

	blendedParams
};

~xfade={|ndef|};

// Generate some random presets
5.do{|i|
	~getCurrent.(Ndef(\sine1));
	~randAll.(Ndef(\sine1));
	~saveCurrent.(Ndef(\sine1), settingName: "preset%".format(i));
};

Ndef(\sine1).gui;
)

(
~morph.value(
	ndef: Ndef(\sine1), 
	envelope: Env.perc,
	blendTo: ~settings.keys.choose
);
)
