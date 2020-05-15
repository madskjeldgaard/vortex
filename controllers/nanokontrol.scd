(
s.waitForBoot{
	// TODO: HACKY
	InfluxBase.baseSpec = [0.0,1.0].asSpec;

	v = VortexVoice.new(s);
	w = VortexVoice.new(s);

	v.set(\x, 1.0.rand2, \y, 1.0.rand2);
	w.set(\x, 1.0.rand2, \y, 1.0.rand2);

	// v.play; w.play;

	k = MKtl('nano', "korg-nanokontrol2");
};

)
(

	VortexVoice.voices.keysValuesDo{|voicename, voice, voicenum|
		voice.play;

		// Status
		k.elAt('bt', 'S', voicenum).action_({|el|
			var val = el.value;

			if(val == 1, {
				voice.status;
			})

		});

		// mute
		k.elAt('bt', 'M', voicenum).action_({|el|
			var val = el.value;

			if(val == 1, {
				el.value = 1;
				voice.stop;
			}, {
				el.value = 0;
				voice.play;
			})
		});


		// Toggle recording
		k.elAt('bt', 'R', voicenum).action_({|el|
			var val = el.value;

			if(val == 1, {
				el.value = 1;
				voice.startRecording;
			}, {
				el.value = 0;
				voice.stopRecording;
			})
		});

		// Slider action
		k.elAt('sl', voicenum).action_({|el|
			var val = el.value;
			voice.set(\x, val);
		});

		// Knob action
		k.elAt('kn', voicenum).action_({|el|
			var val = el.value;
			voice.set(\x, val);
		});

	};

)
