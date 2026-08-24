package no.chess.web.model.game.strategy;

import java.util.HashMap;
import java.util.List;

import aima.core.logic.fol.parsing.ast.Term;
import no.chess.web.model.game.ChessFolKnowledgeBase;
import no.chess.web.model.game.GroundGameAction;
import no.function.FunctionExecutor;

public class PawnStructureExecutor implements FunctionExecutor {
    private ChessFolKnowledgeBase kb;
    private List<GroundGameAction> availableActions;
    
    
    
    
	public ChessFolKnowledgeBase getKb() {
		return kb;
	}

	public void setKb(ChessFolKnowledgeBase kb) {
		this.kb = kb;
	}

	public List<GroundGameAction> getAvailableActions() {
		return availableActions;
	}

	public void setAvailableActions(List<GroundGameAction> availableActions) {
		this.availableActions = availableActions;
	}

	@Override
	public Object execute() {
		GroundGameAction bestPawnMove = null;
		int bestScore = Integer.MIN_VALUE;
		for (GroundGameAction action : availableActions) {


		}
		return null;
	}

	@Override
	public void buildTerms(HashMap<String, Term> cParam, HashMap<String, Term> vParam) {
		// TODO Auto-generated method stub

	}

}
