package no.chess.web.model.game.strategy;

import java.util.HashMap;
import java.util.List;

import aima.core.logic.fol.parsing.ast.Term;
import aima.core.logic.planning.ActionSchema;
import no.chess.web.model.Position;
import no.chess.web.model.game.AgamePiece;
import no.chess.web.model.game.ChessFolKnowledgeBase;
import no.chess.web.model.game.GroundGameAction;
import no.chess.web.model.game.KnowledgeBuilder;
import no.function.FunctionExecutor;
import no.games.chess.search.nondeterministic.GameAction;

/**
 * PawnStructureExecutor
 * This executor checks to see if a pawn needs to be moved in order to enable development of a bishop
 * 
 */
public class PawnStructureExecutor implements FunctionExecutor {
    private ChessFolKnowledgeBase kb;
    private List<GameAction> availableActions;
    private String key = KnowledgeBuilder.getPawnEnabler();

    
	public PawnStructureExecutor(ChessFolKnowledgeBase kb, List<GameAction> actions) {
		super();
		this.kb = kb;
		this.availableActions = actions;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public ChessFolKnowledgeBase getKb() {
		return kb;
	}

	public void setKb(ChessFolKnowledgeBase kb) {
		this.kb = kb;
	}



	public List<GameAction> getAvailableActions() {
		return availableActions;
	}

	public void setAvailableActions(List<GameAction> availableActions) {
		this.availableActions = availableActions;
	}

	@Override
	public Object execute() {
		GroundGameAction bestPawnMove = null;
		int bestScore = Integer.MIN_VALUE;
		String occupies = KnowledgeBuilder.getOCCUPIES();
    	String enabler = KnowledgeBuilder.getENABLES_BISHOP();
 		for (GameAction action : availableActions) {
			GroundGameAction localAction = (GroundGameAction) action;
        	AgamePiece thePiece = (AgamePiece)action.getGamePiece();
        	ActionSchema schema = localAction.getActionSchema();
        	String actionName = schema.getName();
        	String targetpos = KnowledgeBuilder.extractString(actionName,'_',-1);
        	String pieceId =  thePiece.getMyPiece().getOntlogyName();
        	String pawn = KnowledgeBuilder.getPAWN();
			Position pos = thePiece.getHomePosition();
			String posName = pos.getPositionName();
        	if (!kb.existsFact(pawn,pieceId)) {
        		continue; // Hopp over om det ikke er bonde
        	}

        	if(!kb.existsFact(occupies,pieceId,posName)) {
        		continue; // Hopp over om bonden er flyttet
        	}
        	boolean enabled = false;
          	enabled = kb.askRule(enabler,pieceId,"x");
          	if (!enabled) {
          		continue;
          	}else {
          		bestPawnMove =localAction;
          		break;
          	}
          	
		}
		return bestPawnMove;
	}

	@Override
	public void buildTerms(HashMap<String, Term> cParam, HashMap<String, Term> vParam) {
		// TODO Auto-generated method stub

	}

}
