/*
 * Localized default methods for the jQuery validation plugin.
 * Locale: NO (Norwegian)
 * Opprettet: 03.05.2012(ban)
 */
jQuery.extend(jQuery.validator.methods, {
    date: function(value, element) {
	// her er det lov med alle mulige datoer på format dd[-/.]mm[-/.]åååå (så år fra 0000-9999 er mao lov)
	return this.optional(element) || /^(0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.]\d\d\d\d$/i.test(value);
    },
    number: function(value, element) {
	return this.optional(element) || /^-?(?:\d+|\d{1,3}(?:\.\d{3})+)(?:,\d+)?$/.test(value);
    }
});