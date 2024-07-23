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
public class ANewposfunction implements FunctionExecutor {
	private String posKey = "newpos";
	private String pieceKey = "piecename";
	private List<Term> variables = null;
	
	
	public String getPosKey() {
		return posKey;
	}

	public void setPosKey(String posKey) {
		this.posKey = posKey;
	}

	public String getPieceKey() {
		return pieceKey;
	}

	public void setPieceKey(String pieceKey) {
		this.pieceKey = pieceKey;
	}

	public ANewposfunction(List<Term> variables) {
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
		Constant cpos = (Constant) cParam.get(posKey);
		Variable vpos = (Variable) vParam.get(posKey);
		if (cpiece != null) {
			variables.add(cpiece);
		}
		if (vpiece != null) {
			variables.add(vpiece);
		}
		if (cpos != null) {
			variables.add(cpos);
		}
		if (vpos != null) {
			variables.add(vpos);
		}
	}

}
