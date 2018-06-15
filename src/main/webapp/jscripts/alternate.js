$(document).ready(function() {
													 
	// Initialise the first table
	$(".tablelist").tableDnD();
	
	// Make a nice striped effect on the table
	$('.tablelist tbody tr:even').addClass('alt');
	
	$('.schemaList tbody tr:even').addClass('alt');
	
});

