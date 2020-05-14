# VORTEX
![vortex](/documentation/vortex.jpg)
*Edward C. Moore Collection, Bequest of Edward C. Moore, 1891*

Vortex is a multi modal computer music system.

It is inspired by reel to reel tape recorders, cybernetic and feedback music as well as more contemporary generative systems.

At it's core it has a complex web of sound processes divided in voices, all of them interconnected in feedback paths.

The interface for Vortex is a simple to use but esoteric API which obscures the complexity and makes it easy to influence, but hard or impossible to control. In other words: 's a black box.

One of the main ideas is to have a performers movements cause effects on three different time scales:
1. The immediate
2. The near future 
3. The beyond.

The name is meant to illustrate the fact that there is a continuous, turbulent flow and that the sounds and movements of the performer disappear in this fluid motion.

You can hear a very early prototype of this system [by clicking this link](http://mads-kjeldgaard.bandcamp.com/track/discussions-with-geographical-entities)

## Inspirations
- Cyberneticism
- Reel to reel tape recorder instruments (Long varispeed loops + Sound reinjection)

Inspirations:
[Eliane Radigue](https://www.youtube.com/watch?v=C_3Fu8YfSdI)
[Jeroeme Noetinger](https://www.youtube.com/watch?v=pnZ55jQe8jA)
[Giovanni Lami](https://vimeo.com/238351530)

## Design goals
![vortex](/documentation/vortex_blackbox.jpg)

### Interface
- Black box style: Complexity is hidden behind very simple interface. 
- Lose control, gain influence (Alberto de Campo)

### As an instrument
- Player actions have unpredictable consequences 
- Consequences on multiple time scales: The immediate, the near future and the beyond.
- The system's configuration is generative
- Extremely dynamic: The entire system can easily be reconfigured while playing it

## Control
![vortex](/documentation/vortex_influx.jpg)

Uses a fork of Alberto de Campo's brilliant Influx package. The basic idea is to take an input, multiply it by weights and spread it to multiple outputs. This is an extremely powerful idea that can transform a simple input (usually one or two inputs) to many differing outputs.

[This article explains the idea nicely](https://www.3dmin.org/research/open-development-and-design/influx/)

### Trajectories
![trajectory](/documentation/trajectory.png)

In stead of letting input data be mapped directly (but weighted) to outputs, the weighted data is used as pointers in a trajectory alloted to each voice. This means that turning a control from 0.0 to 1.0 will pass through valleys and peaks in a path, sort of like a wavetable index.

The same trajectory may be used on all three time scales discussed elsewhere.

### Multimodal
![controllers](/documentation/vortex_controllers.JPG)

The system is written mostly as an API which makes it easy to map it to many different control platforms, thanks to [Modality](https://github.com/ModalityTeam/Modality-toolkit).

## Time
![vortex](/documentation/voirtex_time.jpg)

### The Immediate
Performer is influential
Action -> reaction.

### The near future
Performer is somewhat influential
Action -> delayed and hard but not impossible to recognize reaction.

### The beyond
Performer's influence is obscure
Action -> 

## A voice
![vortex](/documentation/vortex_voice.jpg)

A vortex voice contains:
- A sound process:
	- Input from the soundcard
	- An fxchain (organized via Sleet)
	- A time machine (a long, looping buffer recorder playing at variable rate)
	- Outputs going to the soundcard
	- Output going to other voices
	- A mixer for mixing in other voices
- A data process:
	- Simple input controller input
- A trajectory which has multiple usecases:
	- Warps the data output as a sort of wavetable where the data becomes an index in the trajectory
	- An envelope for sound processes
	- A trajectory for musical structures on a stretchable time scale 

