//Set up some variables and get ua (user agent) info.
   var browser = "unknown"
   var version = 0
   var detected = false
   var ua = window.navigator.userAgent
   var version = navigator.appVersion

//Are we in Microsoft Internet Explorer or Netscape Navigator?
if (ua.substring(0,7) == "Mozilla") {
	if (ua.indexOf("MSIE") > 0) {
		browser = "Microsoft"
	}
	else {
		browser = "Netscape"
	}
}

//Which plattform are we on?
if (version.indexOf("Macintosh") == -1) {
	//it's not a MAC
	MAC = false;
	} else {
	MAC = true;
}

//Now check the browser version number.
if (ua.indexOf("4.") > 0) {
	version = 4
}
if (ua.indexOf("5.") > 0) {
	version = 5
}
if (ua.indexOf("6.") > 0) {
	version = 6;
}


//If Microsoft Internet Explorer 4 or greater is in use and not on a Mac, use the ie-stylesheet.
	if ((browser == "Microsoft" && version >= 4) && !MAC) {
		document.write("<link rel=stylesheet href=../css/cssie.css type=text/css>");
	}

//If Microsoft Internet Explorer 5 is in use and on a Mac, use the ie-stylesheet.
	if ((browser == "Microsoft" && version == 5) && MAC) {
		document.write("<link rel=stylesheet href=../css/cssie.css type=text/css>");
	}

//If Microsoft Internet Explorer 4 is in use and on a Mac, use the mac-stylesheet.
	if ((browser == "Microsoft" && version == 4) && MAC) {
		document.write("<link rel=stylesheet href=../css/cssie.css type=text/css>");
	}
   
//If Netscape Navigator 4 is in use and not on a Mac, use the nn-stylesheet.
	if ((browser == "Netscape" && version >= 4 && version <= 6) && !MAC) {
		document.write("<link rel=stylesheet href=../css/cssnn.css type=text/css>");
	}

//If Netscape Navigator 6 is in use and not on a Mac,.
	if ((browser == "Netscape" && version >= 6) && !MAC) {
	document.write("<link rel=stylesheet href=../css/cssie.css type=text/css>");
	}

//If Netscape Navigator 4 is in use and on a Mac, use the mac-stylesheet.
    if ((browser == "Netscape" && version >= 4 && version <= 6) && MAC) {
	document.write("<link rel=stylesheet href=../css/cssnn.css type=text/css>");
	}
	
//If Netscape Navigator 6 is in use and on a Mac, use the mac-stylesheet.
	if ((browser == "Netscape" && version >= 6) && MAC) {
	document.write("<link rel=stylesheet href=../css/cssie.css type=text/css>");
	}

//If browser is unknown, use the ie-stylesheet.
	if (browser == "unknown") {
	document.write("<link rel=stylesheet href=../css/cssie.css type=text/css>");
	}