{|inchans, outchans|
	var postInfo = {
		"Making pan function for % in channels and % out channels".format(inchans, outchans).postln
	};

	var panfunc = case
	{(inchans == 1).and(outchans == 1)} { 
		{|in| in }
	}
	{(inchans == 1).and( outchans == 2 )} { 
		{|in, pan| 
			Pan2.ar(in, pan.linlin(0.0,1.0,-1.0,1.0)) 
		}
	}
	{(inchans == 1).and(outchans > 2)} {
		{|in, pan=0.0, width=2.0, orientation=0.5| 
			PanAz.ar(
				numChans: outchans, 
				in: in, 
				pos: pan.linlin(0.0,1.0,-1.0,1.0),
				width: width, 
				orientation: orientation
			) 
		}
	}
	// Stereo in
	{(inchans >= 2).and(outchans == 1)} {
		{|in| 
			Mix.ar(in) // TODO: SelectXFocus ?
		}
	}
	{(inchans == 2).and(outchans == 2)} {
		{|in, pan=0.5| 
			Balance2.ar(in[0], in[1], pos: pan.linlin(0.0,1.0,-1.0,1.0)) 
		}
	}
	{(inchans > 2).and(outchans == 2)} {
		{|in, pan=0.5, spread=1, width=1, orientation=0.5| 
			Splay.ar(
				in,  
				spread: spread,  
				level: 1,  
				center: pan.linlin(0.0,1.0,-1.0,1.0),  
				levelComp: true
			)			
		}
	}
	// Stereo and multi chanin multi chan out
	{(inchans >= 2).and(outchans > 2)} {
		{|in, pan=0.5, spread=1, width=1, orientation=0.5| 
			SplayAz.ar(
				outchans, 
				in,  
				spread: spread,  
				level: 1,  
				width: width,  
				center: pan.linlin(0.0,1.0,-1.0,1.0),  
				orientation: orientation,  
				levelComp: true
			)
		}
	};

	// Post info about result
	postInfo.value;

	// Return function
	panfunc
}
