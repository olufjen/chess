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
public class ABoardtermFunction implements FunctionExecutor {
	private String boardKey = "startpos";
	private List<Term> variables = null;

	public ABoardtermFunction(List<Term> variables) {
		super();
		this.variables = variables;
	}
	

	public String getBoardKey() {
		return boardKey;
	}


	public void setBoardKey(String boardKey) {
		this.boardKey = boardKey;
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
		Constant cpiece = (Constant) cParam.get(boardKey);
		Variable vpiece = (Variable) vParam.get(boardKey);

		if (cpiece != null) {
			variables.add(cpiece);
		}
		if (vpiece != null) {
			variables.add(vpiece);
		}

	}

}
