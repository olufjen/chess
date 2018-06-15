/*
* Initialiseringsfil (document ready) for jQuery validation plugin.
* Locale: NO (Norwegian)
* Opprettet: 03.05.2012(ban)
* Kommontar: Velger å ha egen "doc.ready" for validation plugin, for det gjør det enkelt å bytte språk, enten via javascrip, eller via XSLT på portlett-/framework-nivå.
*            PS. Har også lagt "info-scriptene" i denne fila.
*
* Oppdatert: 22.05.2012(ban) Siste oppdatering av selve valideringen.
*            23.05.2012(ban) Lagt til sortSelect() til slutt i "#where-when-who-form"-blokken.
*            14.12.2012(pcr) Fjerner feilmelding for feil �rstall dersom det trykkes p� "Flere pasienter involvert" eller "Ingen pasienter involvert"
*/
$(function() {

    $(window).bind('beforeunload', function(){ 
       $('#summary-form\\:sessionKill').click();
       
    });
    $(window).unload(function() {
       $('#summary-form\\:sessionKill').click();
    });

    /*       
             $('#summary-form\\:sessionExpire').attr('value','true');
             if ($('#summary-form').length !== 0) {

             
             $(body).unload(function() {
             alert('Handler for .unload() called.');

             $('#sessionExpire').attr('value','true');

             });
             }
    */



    /****************************
     *   #where-when-who-form
     ****************************/
    if ($('#where-when-who-form').length !== 0) {

       // jquery.ui.datepicker
        /*$('#where-when-who-form\\:date-whenInputDate').datepicker(
          {maxDate: '+0d'},
          $.datepicker.regional['no']
          );*/
       
       $.validator.addMethod('date-when', function(value, element) {
            var dateWhenRegExp = new RegExp('^(0[1-9]|[12][0-9]|3[01])[.](0[1-9]|1[012])[.](20|21)\\d\\d$', 'i');
            if (value === '') {
             //return false; // hvis required
             return true;
            } else if (dateWhenRegExp.test(value)) {
             var today = new Date();
             var ddVal = value.slice(0, 2);
             var mmVal = value.slice(3, 5);
             var yyyyVal = value.slice(6);
             var inputDate = new Date(yyyyVal, mmVal-1, ddVal)
             if (inputDate.getTime() <= today.getTime() && inputDate.getTime() > (today.getTime() - 31558464000)) {
                    return true;
             } else {
                    return false;
             }
            } else {
             return false;
            }
       }, 'Dato skal v&aelig;re p&aring; formen dd.mm.&aring;&aring;&aring;&aring;<br/> og kan ikke v&aelig;re i fremtiden,<br/> eller mer enn ett &aring;r bakover i tid.');

       $.validator.addMethod('time-when', function(value, element) {  
            return this.optional(element) || /^([0-1][0-9]|2[0-3])[:]([0-5][0-9])?$/i.test(value);  
       }, 'Klokkeslett m&aring; v&aelig;re p&aring; formen "tt:mm".');

       $.validator.addMethod('year-of-birth', function(value, element) { 
            var dateWhenRegExp = new RegExp('^(19|20|21)\\d\\d$', 'i');
            if (value === '') {
             //return false; // hvis required
             return true;
            } else if (dateWhenRegExp.test(value)) {
             var today = new Date();
             var yyyyVal = value;
             var inputDate = new Date(yyyyVal, 0, 1)
              if (inputDate.getTime() <= today.getTime()) {
                    return true;
             } else {
                    return false;
             }
            } else {
             return false;
            }
       }, 'F&oslash;dsels&aring;r m&aring; v&aelig;re p&aring; formen "&aring;&aring;&aring;&aring;", og v&aelig;re mellom 1900 og dagens dato.');

       // Selve validator-initialiseringen:
       $('#where-when-who-form').validate({
            onsubmit: false,
            errorClass: "form-error",
            rules: {
             'where-when-who-form:date-whenInputDate': 'date-when',
             'where-when-who-form:time-when': 'time-when',
             'where-when-who-form:year-of-birth': 'year-of-birth',
             'where-when-who-form:spesialist': ($('where-when-who-form:ikke').checked = 'true') ? 'required' : 'notrequired',
             'where-when-who-form:eksisterende':	 ($('where-when-who-form:ikke').checked = 'false') ? 'required' : 'notrequired'
            //,'where-when-who-form:eksisterende': 'required'  (14.06.2012 sliter med denne... vet ikke hvordan jeg kan forandre regler dynamisk, og stor fare for at det ikke er mulig)
            },
            showErrors: function(errorMap, errorList) {
             this.defaultShowErrors(); // Denne må være med, hvis ikke vil showErrors: fjerne de vanlige feilmeldingene.
             // datoHidden
             if ($('#where-when-who-form\\:date-whenInputDate').attr('class') === 'rf-cal-inp valid') {
                    $('#where-when-who-form\\:datoHidden').attr('value','true');
              } 
             else if ($('#where-when-who-form\\:date-whenInputDate').attr('class') === 'rf-cal-inp form-error') {
                    $('#where-when-who-form\\:datoHidden').attr('value','false');
             }
             // time-when
             if ($('#where-when-who-form\\:time-when').attr('class') === 'valid') {
                    $('#where-when-who-form\\:klokkeHidden').attr('value','true');
             }
             else if ($('#where-when-who-form\\:time-when').attr('class') === 'form-error') {
                    $('#where-when-who-form\\:klokkeHidden').attr('value','false');
             }
             // aarHidden
             if ($('#where-when-who-form\\:year-of-birth').attr('class') === 'valid') {
                    $('#where-when-who-form\\:aarHidden').attr('value','true');
             } 
             else if ($('#where-when-who-form\\:year-of-birth').attr('class') === 'form-error') {
                    $('#where-when-who-form\\:aarHidden').attr('value','false');
             }


             // spesialistHidden (lagt til 14.06.2012)
             if ($('#where-when-who-form\\:spesialist').attr('class') === 'valid') {
                    $('#where-when-who-form\\:spesialistHidden').attr('value','true');
             } 
             else if ($('#where-when-who-form\\:spesialist').attr('class') === 'form-error') {
                    $('#where-when-who-form\\:spesialistHidden').attr('value','false');
             }

             // sykehusHidden (lagt til 14.06.2012)
             if ($('#where-when-who-form\\:eksisterende').attr('class') === 'valid') {
                    $('#where-when-who-form\\:sykehusHidden').attr('value','true');
             } 
              else if ($('#where-when-who-form\\:eksisterende').attr('class') === 'form-error') {
                    $('#where-when-who-form\\:sykehusHidden').attr('value','false');
             }

            }  
       });

       // Tvinger frem validering p� utvalgte felt n�r side lastes.
       if ($('#where-when-who-form\\:ikke').attr('checked') === 'checked') {
      	 $('#where-when-who-form').validate().element('#where-when-who-form\\:spesialist');
    }
       if ($('#where-when-who-form\\:spesialist').attr('value') !== '') {
            $('#where-when-who-form').validate().element('#where-when-who-form\\:spesialist');
       }
       if ($('#where-when-who-form\\:date-whenInputDate').attr('value') !== '') {
            $('#where-when-who-form').validate().element('#where-when-who-form\\:date-whenInputDate');
       }
       
          if ($('#where-when-who-form\\:eksisterende').attr('value') !== '') {
           $('#where-when-who-form').validate().element('#where-when-who-form\\:eksisterende');
          }
       
       /*
          if ($('#where-when-who-form\\:where-when-who-form:year-of-birth').attr('value') !== '') {
           $('#where-when-who-form').validate().element('#where-when-who-form\\:where-when-who-form:year-of-birth');
          }
         if ($('#where-when-who-form\\:where-when-who-form:time-when').attr('value') !== '') {
           $('#where-when-who-form').validate().element('#where-when-who-form\\:where-when-who-form:time-when');
          }
       */
       



       // Tvinger frem validering p� "required" felt "spesialist". (Som ellers bare blir validert p� submit.)
       $('#where-when-who-form\\:spesialist').blur(function() {
            $('#where-when-who-form').validate().element('#where-when-who-form\\:spesialist');
       });
       // Tvinger frem validering p� "required" felt "eksisterende". (Som ellers bare blir validert p� submit.)
       $('#where-when-who-form\\:eksisterende').blur(function() {
            $('#where-when-who-form').validate().element('#where-when-who-form\\:eksisterende');
       });
       // Tvinger frem validering p� datoene i "datepicer". (Som ikke er en del av validatoren, og ikke har noen innebygd event knyttet til seg.)
       $('#where-when-who-form\\:date-whenContent td').click(function() {
            $('#where-when-who-form').validate().element('#where-when-who-form\\:date-whenInputDate');
       });



       // Gr�e ut (og vica versa) sykehus, samt vise og skjule "Hvor skjedde hendelsen?":
       $('#where-when-who-form\\:ikke').click(function() {
            if (this.checked) {
             $('#borte-tittei').prepend('<div id="hide-hospital"></div>');
             $('#where-when-who-form\\:eksisterende').attr('value','');
             $('#where-when-who-form\\:eksisterende').attr('class','valid');
             $('#where-when-who-form\\:spesialistHidden').attr('value','false');
             $('#where-when-who-form\\:sykehusHidden').attr('value','true');
             $('label[for="where-when-who-form:eksisterende"]').attr('style','display:none;');
             //fjerne feilmelding hvis den er aktivert
             $('#where-when-who-form\\:avdelinger').attr('value','');
             $('#where-when-who-form\\:ward').attr('value','');
             $('#where-when-who-form\\:eksisterende').attr('disabled','disabled');
             $('#where-when-who-form\\:avdelinger').attr('disabled','disabled');
             $('#where-when-who-form\\:ward').attr('disabled','disabled');
             // Viser "Hvor skjedde hendelsen?"
             $('.not-hospital').css({display:'block'});
            }
            else {
             $('#hide-hospital').remove();
             $('#where-when-who-form\\:eksisterende').removeAttr('disabled');
             $('#where-when-who-form\\:avdelinger').removeAttr('disabled');
             $('#where-when-who-form\\:ward').removeAttr('disabled');
             $('#where-when-who-form\\:spesialist').attr('value','');
             $('#where-when-who-form\\:spesialist').attr('class','valid');
             $('#where-when-who-form\\:spesialistHidden').attr('value','true');
             $('label[for="where-when-who-form:spesialist"]').attr('style','display:none;');
             //fjerne feilmelding hvis den er aktivert
             // skjuler "Hvor skjedde hendelsen?"
             $('.not-hospital').css({display:'none'});
            }
       });



       // Gr�e ut sykehus mm n�r siden lastes hvis checked=true.
       if ($('#where-when-who-form\\:ikke').attr('checked')) {
            $('#borte-tittei').prepend('<div id="hide-hospital"></div>');

           // (14.06.2012) her fjernes ikke valuer, men det skal det vel heller ikke...
            $('#where-when-who-form\\:sykehusHidden').attr('value','true');
            $('#where-when-who-form\\:eksisterende').attr('disabled','disabled');
            $('#where-when-who-form\\:avdelinger').attr('disabled','disabled');
            $('#where-when-who-form\\:ward').attr('disabled','disabled');
            // Viser "Hvor skjedde hendelsen?"
            $('.not-hospital').css({display:'block'});
       }
       

       // Gr�e ut, t�mme og disable f�dsels�r, samt fjerne feilmelding hvis den vises:
       $('#where-when-who-form\\:flere, #where-when-who-form\\:ingen').click(function() {
    	   if ($('label.form-error[generated=true]').length > 0) {
    		   $('label.form-error[generated=true]').remove();
    		   $('#where-when-who-form\\:year-of-birth').removeClass('form-error');
    		   $('#where-when-who-form\\:aarHidden').prop('value', 'true');
    	   }
    	   
            if($('#hide-year-of-birth').length === 0) {
                $('#birthyear').prepend('<div id="hide-year-of-birth"></div>');
                $('#where-when-who-form\\:year-of-birth').attr('value', '');
                $('#where-when-who-form\\:year-of-birth').attr('disabled', 'disabled');                    
            }
       });

       $('#where-when-who-form\\:mann, #where-when-who-form\\:kvinne').click(function() {
            $('#hide-year-of-birth').remove();
            $('#where-when-who-form\\:year-of-birth').removeAttr('disabled');
       });
       
       //hvis mann eller dame er markert
      /* if ($('#where-when-who-form\\:mann, #where-when-who-form\\:kvinne').attr('checked')==='checked') {
         $('#hide-year-of-birth').remove();
         $('#where-when-who-form\\:year-of-birth').removeAttr('disabled');
    });*/
//hvis flere eller ingen er markert
if ($('#where-when-who-form\\:ingen').attr('checked')==='checked' || $('#where-when-who-form\\:flere').attr('checked')==='checked') {
	if($('#hide-year-of-birth').length === 0) {
    $('#birthyear').prepend('<div id="hide-year-of-birth"></div>');
    $('#where-when-who-form\\:year-of-birth').attr('value', '');
    $('#where-when-who-form\\:year-of-birth').attr('disabled', 'disabled');  
}
};

       // Sjekke om validering skal trigges n�r siden lastes.
       if ( $('#where-when-who-form\\:btnKontroll').length !== 0 ||
             $('#where-when-who-form\\:datoHidden').attr('value') === 'false' ||
             $('#where-when-who-form\\:klokkeHidden').attr('value') === 'false' ||
             $('#where-when-who-form\\:aarHidden').attr('value') === 'false'
           ) {

            $('#where-when-who-form').validate().form();
       }

    }//#where-when-who-form slutt



    /************************
     *    #incident-form
     ************************/
    if ($('#incident-form').length !== 0) {
       
       // Selve validator-initialiseringen:
       $('#incident-form').validate({
            onsubmit: false,
            errorClass: "form-error",
            rules: {
             'incident-form:incident': 'required'
            },
            messages: {
             'incident-form:incident': 'Dette feltet er obligatorisk.'
            },
            showErrors: function(errorMap, errorList) {
             this.defaultShowErrors(); // Denne må være med, hvis ikke vil showErrors: fjerne de vanlige feilmeldingene.
             // hendelseHidden
             if ($('#incident-form\\:incident').attr('class') === 'valid') {
                    $('#incident-form\\:hendelseHidden').attr('value','true');
             } 
             else if ($('#incident-form\\:incident').attr('class') === 'form-error') {
                    $('#incident-form\\:hendelseHidden').attr('value','false');
             }
            }
       });

       // Tvinger frem validering på "required" felt. (Som ellers bare blir validert på submit.)
       $('#incident-form\\:incident').blur(function() {
            $('#incident-form').validate().element('#incident-form\\:incident');
       });
       
       // Sjekke om validering skal trigges når siden lastes.
       if ( $('#incident-form\\:btnKontroll').length !== 0 ||
             $('#incident-form\\:hendelseHidden').attr('value') === 'false'
           ) {
            
            $('#incident-form').validate().form();
       }
    }//#incident-form slutt



    /***********************
     *    #contact-form
     ***********************/
    if ($('#contact-form').length !== 0) {
       
       // Selve validator-initialiseringen:
       $('#contact-form').validate({
            onsubmit: false,
            errorClass: "form-error",
            rules: {
             'contact-form:phone-sender': {
                    digits: true
             },
             'contact-form:phone-secondary-source': {
                    digits: true
             },
             'contact-form:email-sender': {
                    required: true,
                    email: true
             },
             'contact-form:email-sender-rep': {
                    equalTo: '#contact-form\\:email-sender'
             },
             'contact-form:email-secondary-source': {
                    email: true
             },
             'contact-form:email-secondary-source-rep': {
                    equalTo: '#contact-form\\:email-secondary-source'
             }
            },
            messages: {
             'contact-form:email-sender': 'Oppgi en gyldig epostadresse. (Dette feltet er obligatorisk.)',
             'contact-form:email-sender-rep': 'M&aring; v&aelig;re likt som e-post over. (Dette feltet er obligatorisk.)',
             'contact-form:email-secondary-source-rep': 'M&aring; v&aelig;re likt som e-post over.'
            },
            showErrors: function(errorMap, errorList) {
             this.defaultShowErrors(); // Denne må være med, hvis ikke vil showErrors: fjerne de vanlige feilmeldingene.
             // phoneHidden
             if ($('#contact-form\\:phone-sender').attr('class') === 'telephone valid') {
                    $('#contact-form\\:phoneHidden').attr('value','true');
             }
             else if ($('#contact-form\\:phone-sender').attr('class') === 'telephone form-error') {
                    $('#contact-form\\:phoneHidden').attr('value','false');
             }
             // phoneSecondaryHidden
             if ($('#contact-form\\:phone-secondary-source').attr('class') === 'telephone valid') {
                    $('#contact-form\\:lederTelefon').attr('value','true');
             } 
             else if ($('#contact-form\\:phone-secondary-source').attr('class') === 'telephone form-error') {
                    $('#contact-form\\:lederTelefon').attr('value','false');
             }
             // emailHidden
             if ($('#contact-form\\:email-sender').attr('class') === 'valid') {
                    $('#contact-form\\:emailHidden').attr('value','true');
             } 
             else if ($('#contact-form\\:email-sender').attr('class') === 'form-error') {
                    $('#contact-form\\:emailHidden').attr('value','false');
             }
             // gjentaHidden
             if ($('#contact-form\\:email-sender-rep').attr('class') === 'valid') {
                    $('#contact-form\\:gjentaHidden').attr('value','true');
             } 
             else if ($('#contact-form\\:email-sender-rep').attr('class') === 'form-error') {
                    $('#contact-form\\:gjentaHidden').attr('value','false');
             }
             // lederGjentaHidden
             if ($('#contact-form\\:email-secondary-source-rep').attr('class') === 'valid') {
                    $('#contact-form\\:lederGjentaHidden').attr('value','true');
             } 
             else if ($('#contact-form\\:email-secondary-source-rep').attr('class') === 'form-error') {
                    $('#contact-form\\:lederGjentaHidden').attr('value','false');
             }
            }
       });

       // Tvinger frem validering på "required" felt. (Som ellers bare blir validert på submit.)
       $('#contact-form\\:email-sender').blur(function() {
            $('#contact-form').validate().element('#contact-form\\:email-sender');
       });
       // Tvinger frem validering på "required" felt. (Som ellers bare blir validert på submit.)
       $('#contact-form\\:email-sender-rep').blur(function() {
            $('#contact-form').validate().element('#contact-form\\:email-sender-rep');
       });

       // Sjekke om validering skal trigges når siden lastes.
       // På kontaktsiden finne btnKontroll ved første treff, så sjekker på kontrollHidden istede.
       if ( $('#contact-form\\:kontrollHidden').attr('value') === 'true' || // NB! denne skal være 'false'
             $('#contact-form\\:phoneHidden').attr('value') === 'false' ||
             $('#contact-form\\:lederTelefon').attr('value') === 'false' ||
             $('#contact-form\\:emailHidden').attr('value') === 'false' ||
             $('#contact-form\\:gjentaHidden').attr('value') === 'false' ||
             $('#contact-form\\:lederGjentaHidden').attr('value') === 'false'
           ) { 

            $('#contact-form').validate().form();
       }
    }//#contact-form slutt



    /***********************
     *    alle sider
     ***********************/

    // Vise og skjule infoboksene:
    $('.info-icon').click(function() {

       if ($('#info').css('display') !== 'none') {
            //console.log( 'skjuler info' );
            $('.helparea').fadeTo(300, 0, function () {
             $('#info').css({ display: 'none' });
            });
            $('.info-icon').attr('src', '../images/helpicon2.png');
       }
       else {
            //console.log( 'viser info' );
            $('#info').css({ display: 'block' });
            $('.helparea').fadeTo(1000, 1);
            $('.info-icon').attr('src', '../images/helpicon.png');
       }
    });
});
