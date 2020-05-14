// TODO

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

