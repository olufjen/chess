package no.chess.web.model.game;

import java.util.HashMap;
import java.util.List;

import aima.core.logic.fol.parsing.ast.Constant;
import aima.core.logic.fol.parsing.ast.Term;
import aima.core.logic.fol.parsing.ast.Variable;
import no.function.FunctionExecutor;

/**
 * This FunctionExecutor fills the list of Terms correctly with Constants and Variables
 * It is used to create correct lifted action schemas.
 * @author oluf
 *
 */
public class APiecetypefunction implements FunctionExecutor {
	private String typeKey = "piecetype";
	private String pieceKey = "piecename";
	private List<Term> variables = null;

	public String getTypeKey() {
		return typeKey;
	}

	public void setTypeKey(String typeKey) {
		this.typeKey = typeKey;
	}

	public String getPieceKey() {
		return pieceKey;
	}

	public void setPieceKey(String pieceKey) {
		this.pieceKey = pieceKey;
	}

	public APiecetypefunction(List<Term> variables) {
		super();
		this.variables = variables;
	}
	

	public List<Term> getVariables() {
		return variables;
	}

	public void setVariables(List<Term> variables) {
		this.variables = variables;
	}

	@Override
	public Object execute() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void buildTerms(HashMap<String, Term> cParam, HashMap<String, Term> vParam) {
		Constant cpiece = (Constant) cParam.get(pieceKey);
		Variable vpiece = (Variable) vParam.get(pieceKey);
		Constant ctype = (Constant) cParam.get(typeKey);
		Variable vtype = (Variable) vParam.get(typeKey);
		if (cpiece != null) {
			variables.add(cpiece);
		}
		if (vpiece != null) {
			variables.add(vpiece);
		}
		if (ctype != null) {
			variables.add(ctype);
		}
		if (vtype != null) {
			variables.add(vtype);
		}
	}

}
