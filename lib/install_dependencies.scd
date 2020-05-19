(
	var urls = [
		"https://github.com/madskjeldgaard/Sleet",
		"https://github.com/madskjeldgaard/kfilter",
		"https://github.com/madskjeldgaard/influx",
		"https://github.com/madskjeldgaard/allbuf",
		"https://github.com/ModalityTeam/Modality-toolkit"
	];

	urls.do{|u| 
		"Installing quark from url %".format(u).postln;
		Quarks.install(u)
	}
)
