(
Ndef(\sampleplayer2, {|buffer=0, rate=1.0, trigger=1.0, start=0.0, loop=1.0|
	PlayBuf.ar(2, buffer, rate * BufRateScale.kr(buffer), trigger, start, loop,  doneAction: 0);
});

~settings = ~settings ? ();

~getCurrent = {|ndef, except|
	ndef.getKeysValues(except: except)
};

~getSetting = {|name|
	if(name.isNil, {"No setting name".warn}, {
		~settings.at(name);
	})
};

~saveCurrent = {|ndef, settingName|
	var thissetting = ~getCurrent.value(ndef);
	var key = settingName ? "%".format(Date.getDate.stamp); 

	key = key.asSymbol;

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

// TODO
~morphTask = {|envelope|
	Task({
		var env, envval;
		var timegrain = 0.01;

		env = envelope ? Env.perc; 
		env = env.asPseg.asStream;
		envval = env.next;

		while({envval.notNil}, {
			envval = env.next.postln; 
			timegrain.wait; 
		})
	})
};

~morph= {|ndef| 

};

~blendParams = {|ndef, blend=0.5, name1, name2|
	var thisSetting = ~getSetting.(name1);
	var thatSetting = ~getSetting.(name2);

	var blendedParams = thisSetting.collect{|pair|
		var thisParam, value, thatParam, thatValue, blended;
		# thisParam, value = pair;
		# thatValue = thatSetting.select{|pair| pair[0] == thisParam };
		thatValue = thatValue[1]; // Only get value from key-value pair
		blended = value.blend(thatValue, blend);

		"Blending param % from % to %: %".format(thisParam, value, thatValue, blended).postln;

		value.postln; thatValue.postln; thatParam;
		[thisParam, blended]
	};

	blendedParams
};

~xfade={|ndef|};
// Ndef(\sampleplayer2).gui;

~getCurrent.(Ndef(\sampleplayer2));
~randAll.(Ndef(\sampleplayer2));
~saveCurrent.(Ndef(\sampleplayer2));
)

ProxyPreset

a = [0.25, 0.5, 0.21]
b = [0.25, 0.35, 0.21]*2

a.difference(b)

~settings.keys
~randAll.(Ndef(\sampleplayer2))

~blendParams.(Ndef(\sampleplayer2), 0.5, '200526_200352', '200526_200621')
(
)
