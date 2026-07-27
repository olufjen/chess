package no.chess.web.model.game.strategy;

import java.util.HashMap;

import aima.core.logic.fol.parsing.ast.Term;
import no.function.FunctionExecutor;
import no.chess.web.model.game.APlayer;
import no.chess.web.model.game.ChessFolKnowledgeBase;
/**
 * QueensGambitAcceptedExecutor
 * This class is an implemnentation of the FunctionExecutor thart represent
 * Acceptance of queen gambit. Opponent takes white pawn at c4
 * "If we played 2. c4, and black played 2... dxc4, play 3. e4."
 * 
 */
public class QueensGambitAcceptedExecutor implements FunctionExecutor {
	private ChessFolKnowledgeBase kb;
	private APlayer player;
	private APlayer opponent;
	
	public QueensGambitAcceptedExecutor(ChessFolKnowledgeBase kb, APlayer player, APlayer opponent) {
		super();
		this.kb = kb;
		this.player = player;
		this.opponent = opponent;
	}

	@Override
	public Object execute() {
		
		return null;
	}

	@Override
	public void buildTerms(HashMap<String, Term> cParam, HashMap<String, Term> vParam) {
		// TODO Auto-generated method stub

	}

}
