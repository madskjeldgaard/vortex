(
var lfos = ();
var iphase = 4pi.rand2;

lfos.put(
	\par, 
	{|feedback=0, freq=1, amp=1.0| LFPar.kr(freq+feedback, iphase, mul: amp) };
);


lfos.put(
	\saw, 
	{|feedback=0, freq=1, amp=1.0| LFSaw.kr(freq+feedback, iphase, mul: amp) };
);

lfos.put(
	\sin, 
	{|feedback=0, freq=1, amp=1.0| SinOsc.kr(freq+feedback, iphase, mul: amp) };
);

lfos.put(
	\tri, 
	{|feedback=0, freq=1, amp=1.0| LFTri.kr(freq+feedback, iphase, mul: amp) };
);

lfos.put(
	\lfnoise1, 
	{|feedback=0, freq=1, amp=1.0| LFNoise1.kr(freq+feedback, mul: amp) };
);

lfos.put(
	\lfnoise2, 
	{|feedback=0, freq=1, amp=1.0| LFNoise2.kr(freq+feedback, mul: amp) };
);

lfos.put(
	\varsaw, 
	{|feedback=0, freq=1, width=0.5, amp=1.0| VarSaw.kr(freq+feedback, iphase, width, mul:amp) };
);

lfos.put(
	\noisesaw, 
	{|feedback=0, freq=1, amp=1.0| VarSaw.kr(freq+feedback, iphase, width: LFNoise2.kr(freq/10.0).range(0.0,1.0), mul:amp) };
);

lfos
)
