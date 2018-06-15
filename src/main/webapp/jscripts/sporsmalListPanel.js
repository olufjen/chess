jQuery(function($){$(document).ready(function(){
	jQuery.noConflict();
	var obj = document.getElementById("innhold:splst:mineSporsmal:questiongrid");
	$(obj).tableDnD({
	  //  onDragClass: "myDragClass",
	    onDrop: function(table, row) {
		if(obj != null){
	      	jQuery.tableDnD.currentTable = document.getElementById("innhold:splst:mineSporsmal:questiongrid");
			      var a = obj.getElementsByTagName("tr");
				  	var va="";
			
				  for(k=0; k<a.length; k++){
						var  strId= a[k].childNodes[3].childNodes[0].id.split("_")  ;
						var temp = strId[1];
						va += temp + ";";
					}
				
				document.getElementById("hideValue").value = va;
				$("#clk2").click();
		}
			callScreenSaver ();
			},
	
		onDragStart: function(table, row) {
			
		}
	});
	

	//0 means disabled; 1 means enabled;
	var popupStatus = 0;

	//loading popup with jQuery magic!
	function loadPopup(){
		//loads popup only if it is disabled
		if(popupStatus==0){
			$("#backgroundPopup").css({
				"opacity": "0.7"
			});
			$("#backgroundPopup").fadeIn("slow");
			$("#popupContact").fadeIn("slow");
			popupStatus = 1;
		}
	}

	//disabling popup with jQuery magic!
	function disablePopup(){
		//disables popup only if it is enabled
		if(popupStatus==1){
			$("#backgroundPopup").fadeOut("slow");
			$("#popupContact").fadeOut("slow");
			popupStatus = 0;
		}
	}

	//centering popup
	function centerPopup(){
		//request data for centering
		var windowWidth = document.documentElement.clientWidth;
		var windowHeight = document.documentElement.clientHeight;
		var popupHeight = $("#popupContact").height();
		var popupWidth = $("#popupContact").width();
		//centering
		$("#popupContact").css({
		//	"position": "absolute",
			"top": 200,
			"left": 200
		});
		//only need force for IE6
		
		$("#backgroundPopup").css({
			"height": windowHeight
		});
		
	}
	  
	 function callScreenSaver (){
	        jQuery.noConflict();
			//centering with css
			centerPopup();
			//load popup
			loadPopup();
	 }
	  
});});

