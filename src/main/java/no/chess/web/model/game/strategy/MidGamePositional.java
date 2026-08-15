package no.chess.web.model.game.strategy;

import java.util.HashMap;
import java.util.List;

import aima.core.logic.fol.parsing.ast.Term;
import aima.core.logic.planning.ActionSchema;
import no.chess.web.model.game.AgamePiece;
import no.chess.web.model.game.ChessFolKnowledgeBase;
import no.chess.web.model.game.GroundGameState;
import no.chess.web.model.game.KnowledgeBuilder;
import no.chess.web.model.game.GroundGameAction;
import no.function.FunctionExecutor;
import no.games.chess.search.nondeterministic.GameAction;

/**
 * MidGamePositional
 * This Executor checks for the best mid game development move
 * @author olufj
 * 
 */
public class MidGamePositional implements FunctionExecutor {

    private  ChessFolKnowledgeBase kb;
    private  GroundGameState state;
    private  List<GameAction> availableActions;
    private String opponentPiece = "BlackPawn";
    private String opponentType = "Black";
    private String ownType = "White";
    
    private String key = KnowledgeBuilder.getMidgamePositional();
    
	public MidGamePositional(ChessFolKnowledgeBase kb, GroundGameState state, List<GameAction> availableActions) {
		super();
		this.kb = kb;
		this.state = state;
		this.availableActions = availableActions;
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

	public GroundGameState getState() {
		return state;
	}

	public void setState(GroundGameState state) {
		this.state = state;
	}



	public List<GameAction> getAvailableActions() {
		return availableActions;
	}

	public void setAvailableActions(List<GameAction> availableActions) {
		this.availableActions = availableActions;
	}

	@Override
	public Object execute() {
		GroundGameAction bestAction = null;
		int maxActionScore = Integer.MIN_VALUE;
        for (GameAction action : availableActions) {
        	GroundGameAction localAction = (GroundGameAction) action;
        	AgamePiece thePiece = (AgamePiece)action.getGamePiece();
        	ActionSchema schema = localAction.getActionSchema();
        	String actionName = schema.getName();
        	String pos = KnowledgeBuilder.extractString(actionName,'_',-1);
        	String pieceId =  thePiece.getMyPiece().getOntlogyName();
        	String minor = KnowledgeBuilder.getMINORPIECE();
        	if (!kb.existsFact(minor,pieceId)) {
        		continue; // Hopp over om det er bonde, tårn, dronning eller konge
        	}
        	String occupies = KnowledgeBuilder.getOCCUPIES();
        	List<String> occupypieces = kb.searchFacts("x", pos,occupies);
        	boolean ownOccupy = false;
        	if (!occupypieces.isEmpty()) {
        		for (String pieceName:occupypieces) {
        			ownOccupy = pieceName.contains(ownType);
        			if (ownOccupy)
        				break;
        		}
        	}
        	if (ownOccupy)
        		continue;
        	String threaten = KnowledgeBuilder.getTHREATEN();
        	List<String> pieces = kb.searchFacts("x", pos, threaten);
        	boolean noGood = false;
        	boolean isThreatened = false; 
        	if (!pieces.isEmpty()) {
        		for (String pieceName:pieces) {
        			noGood = pieceName.contains(opponentPiece);
        			if (noGood)
        				break;
          		}
        	}
        	if (noGood)
        		continue;
        	if (!noGood) {
           		for (String pieceName:pieces) {
        			isThreatened = pieceName.contains(opponentType);
        			if (isThreatened)
        				break;
          		}
        	}
        	String protector = KnowledgeBuilder.getPROTECTOR();
        	boolean pieceProtector = false;
        	pieceProtector = kb.askRule(protector,pieceId,"x");
        	if(pieceProtector)
        		continue;
        	
            // 5. EVALUERING: Beregn hvor verdifull posisjonen er
            int currentScore = calculateSquareValue(pieceId, pos, isThreatened);
            // Oppdater dersom dette trekket ga høyere poengsum enn tidligere kandidater
            if (currentScore > maxActionScore) {
                maxActionScore = currentScore;
                bestAction = localAction;
            }
        }
		return bestAction;
	}
    private int calculateSquareValue(String pieceId, String targetSquare, boolean isThreatened) {
        int score = 0;

        String square = KnowledgeBuilder.getCENTERSQUARE();

        // B. Sentrumskontroll (d4, e4, d5, e5)
        if (kb.existsFact(square,targetSquare)) {
            score += 50;
        }
        boolean extended = targetSquare.equals("c4") || targetSquare.equals("f4") ||targetSquare.equals("c5") ||targetSquare.equals("f5");
        // C. Utvidet sentrum/aktive felt (c4, f4, c5, f5 osv.)
        if (extended) {
            score += 20;
        }

        // D. Trekk til trygt uforstyrret felt (ikke truet i det hele tatt)
        if (!isThreatened) {
            score += 10;
        }

        return score;
    }

	@Override
	public void buildTerms(HashMap<String, Term> cParam, HashMap<String, Term> vParam) {
		// TODO Auto-generated method stub

	}

}
