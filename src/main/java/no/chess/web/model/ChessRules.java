package no.chess.web.model;

import java.util.ArrayList;

/**
 * ChessRules
 * Contains arrayLists of defined rules in the chessontology (rule labels and rules)
 * It is used to present the rules on the page.
 * @author oluf
 *
 */
public class ChessRules {
	private String exeRules;
	private String exeLabels;
	
	public ChessRules() {
		super();
    	exeRules = "No Rules available";
    	exeLabels = "Label";

	}

	public String getExeRules() {
		return exeRules;
	}

	public void setExeRules(String exeRules) {
		this.exeRules = exeRules;
	}

	public String getExeLabels() {
		return exeLabels;
	}

	public void setExeLabels(String exeLabels) {
		this.exeLabels = exeLabels;
	}

	

}
