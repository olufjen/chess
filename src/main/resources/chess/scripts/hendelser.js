//The magic code to add show/hide custom event triggers
(function ($) {
      $.each(['show', 'hide'], function (i, ev) {
        var el = $.fn[ev];
        $.fn[ev] = function () {
          this.trigger(ev);
          return el.apply(this, arguments);
        };
      });
    })(jQuery);
 
 


//on Show button click; show the #foo div
$('#btnShow').click(function(){
   $('#foo').show();
});
 
//on Hide button click; hide the #foo div
$('#btnHide').click(function(){
   $('#foo').hide();
});
 
//Add custom handler on show event and print message
$('#foo').on('show', function(){
    $('#console').html( $('#console').html() + '#foo is now visible'+ '<br>'  )
});
 
//Add custom handler on hide event and print message
$('#foo').on('hide', function(){
    $('#console').html( $('#console').html() + '#foo is hidden'+ '<br>'  )
});
$('#p-blod-erytrocytt').checked(function(){
	 $('#foo').show();
});