package no.chess.web.model.game;

import java.util.HashMap;
import java.util.List;

import aima.core.logic.fol.parsing.ast.Term;
import no.function.FunctionExecutor;

public class ApawnstrikeFunction implements FunctionExecutor {
	private String pawnKey = "pawn";
	private String pieceKey = "piecename";
	private List<Term> variables = null;
	@Override
	public Object execute() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPawnKey() {
		return pawnKey;
	}

	public void setPawnKey(String pawnKey) {
		this.pawnKey = pawnKey;
	}

	public String getPieceKey() {
		return pieceKey;
	}

	public void setPieceKey(String pieceKey) {
		this.pieceKey = pieceKey;
	}

	public List<Term> getVariables() {
		return variables;
	}

	public void setVariables(List<Term> variables) {
		this.variables = variables;
	}

	public ApawnstrikeFunction(List<Term> variables) {
		super();
		this.variables = variables;
	}

	@Override
	public void buildTerms(HashMap<String, Term> cParam, HashMap<String, Term> vParam) {
		// TODO Auto-generated method stub

	}

}
