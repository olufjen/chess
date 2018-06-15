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


// SWITCH STYLE
function switchStyle(element,classID) {
	if (browser == "Microsoft") {
		element.className = classID;
	}
}


function MM_jumpMenu(targ,selObj,restore){ //v3.0
  eval(targ+".location='"+selObj.options[selObj.selectedIndex].value+"'");
  if (restore) selObj.selectedIndex=0;
}

// Metoden setter en id i et hidden-felt i form.
// Benyttes for å sende ulike hjelpetekst-id'er i post-forespørsler i
// webapplikasjonen for skoleordningen
// Forfatter: Leif Torger Grøndahl
function setHjelpeId(element, id) {
	element.form.hjelpeId.value=id;
}


// Metoden viser hjelpetekster i Elmer-skjemaer. Det forutsettes at HTML-siden som benytter
// funksjonen inneholder et element (gjerne span) med id lik "infoTekst"
// Forfatter: Leif Torger Grøndahl
// Lagt til: 23.04.2003
function visInfotekst(tekst) {
	document.getElementById("infoTekst").innerHTML = tekst;
}

function noenter() {
  return !(window.event && window.event.keyCode == 13);
}