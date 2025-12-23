package no.chess.web.view;

import java.util.Map;
/**
 * En wrapper for å holde Freemarker malnavn og datamodellen.
 * Tilsvarer elementene i en TemplateRepresentation.
 */
public class FreemarkerView {

	   private final String templateName; // .html/.ftl filnavn (f.eks. "index.ftl")
	    private final Map<String, Object> dataModel; // Map<String, Object>

	    public FreemarkerView(String templateName, Map<String, Object> dataModel) {
	        this.templateName = templateName;
	        this.dataModel = dataModel;
	    }

	    public String getTemplateName() {
	        return templateName;
	    }

	    public Map<String, Object> getDataModel() {
	        return dataModel;
	    }
}
