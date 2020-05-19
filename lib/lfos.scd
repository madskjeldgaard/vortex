(
var lfos = ();
var iphase = 4pi.rand2;

lfos.put(
	\par, 
	{|freq=1, amp=1.0| LFPar.kr(freq, iphase, mul: amp) };
);


lfos.put(
	\saw, 
	{|freq=1, amp=1.0| LFSaw.kr(freq, iphase, mul: amp) };
);

lfos.put(
	\sin, 
	{|freq=1, amp=1.0| SinOsc.kr(freq, iphase, mul: amp) };
);

lfos.put(
	\tri, 
	{|freq=1, amp=1.0| LFTri.kr(freq, iphase, mul: amp) };
);

lfos.put(
	\lfnoise1, 
	{|freq=1, amp=1.0| LFNoise1.kr(freq, mul: amp) };
);

lfos.put(
	\lfnoise2, 
	{|freq=1, amp=1.0| LFNoise2.kr(freq, mul: amp) };
);

lfos.put(
	\varsaw, 
	{|freq=1, width=0.5, amp=1.0| VarSaw.kr(freq, iphase, width, mul:amp) };
);

lfos.put(
	\noisesaw, 
	{|freq=1, amp=1.0| VarSaw.kr(freq, iphase, width: LFNoise2.kr(freq/10.0).range(0.0,1.0), mul:amp) };
);

lfos
)
