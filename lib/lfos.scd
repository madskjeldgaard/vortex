(
var lfos = ();
var iphase = 4pi.rand2;
var minOut=0.0, maxOut=1.0;

lfos.put(
	\par, 
	{|feedback=0, freq=1, amp=1.0| 
		LFPar.kr(freq+feedback, iphase, mul: amp).linlin(-1.0, 1.0, minOut, maxOut) 
	};
);


lfos.put(
	\saw, 
	{|feedback=0, freq=1, amp=1.0| 
		LFSaw.kr(freq+feedback, iphase, mul: amp).linlin(-1.0, 1.0, minOut, maxOut) 
	};
);

lfos.put(
	\sin, 
	{|feedback=0, freq=1, amp=1.0| 
		SinOsc.kr(freq+feedback, iphase, mul: amp).linlin(-1.0, 1.0, minOut, maxOut) 
	};
);

lfos.put(
	\tri, 
	{|feedback=0, freq=1, amp=1.0| 
		LFTri.kr(freq+feedback, iphase, mul: amp).linlin(-1.0, 1.0, minOut, maxOut) 
	};
);

lfos.put(
	\lfnoise1, 
	{|feedback=0, freq=1, amp=1.0| 
		LFNoise1.kr(freq+feedback, mul: amp).linlin(-1.0, 1.0, minOut, maxOut) 
	};
);

lfos.put(
	\lfnoise2, 
	{|feedback=0, freq=1, amp=1.0| 
		LFNoise2.kr(freq+feedback, mul: amp).linlin(-1.0, 1.0, minOut, maxOut) 
	};
);

lfos.put(
	\varsaw, 
	{|feedback=0, freq=1, width=0.5, amp=1.0| 
		VarSaw.kr(freq+feedback, iphase, width, mul:amp).linlin(-1.0, 1.0, minOut, maxOut) 
	};
);

lfos.put(
	\noisesaw, 
	{|feedback=0, freq=1, amp=1.0| 
		VarSaw.kr(freq+feedback, iphase, width: LFNoise2.kr(freq/10.0).range(0.0,1.0), mul:amp).linlin(-1.0, 1.0, minOut, maxOut) 
	};
);

lfos
)
