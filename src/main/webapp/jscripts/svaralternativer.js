function getTextBoxValue(){
	 
	 var textsValue="";
	  var size = document.getElementById("input[0]");
	  var val = size.value;
	  var iSize = parseInt(val); 
	  for(var i=0; i<iSize; i++){
		  var temp ="txt["+ i +"]";
      		var element = document.getElementById(temp).value;
			 if(element !="")
		   		textsValue = textsValue + element + ";";
		}
	  document.getElementById("setValue").value=textsValue;
}
