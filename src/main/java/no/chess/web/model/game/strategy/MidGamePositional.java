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
	private GroundGameAction determinedAction = null; // This action is set when a planning procedure takes place from result(s.a) and GroundGameAction.performAction. (The testEnd procedure)

    
    private String key = KnowledgeBuilder.getMidgamePositional();
    
	public MidGamePositional(ChessFolKnowledgeBase kb, GroundGameState state, List<GameAction> availableActions) {
		super();
		this.kb = kb;
		this.state = state;
		this.availableActions = availableActions;
	}

	public GroundGameAction getDeterminedAction() {
		return determinedAction;
	}

	public void setDeterminedAction(GroundGameAction determinedAction) {
		this.determinedAction = determinedAction;
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
		String activeSchema = "";
		String activePiece = "";
		String target = "";
		if (determinedAction != null) {
			activeSchema = determinedAction.getActionSchema().getName();
			activePiece = KnowledgeBuilder.extractString(activeSchema,'_',0);
		}
        for (GameAction action : availableActions) {
        	GroundGameAction localAction = (GroundGameAction) action;
        	AgamePiece thePiece = (AgamePiece)action.getGamePiece();
        	ActionSchema schema = localAction.getActionSchema();
        	String actionName = schema.getName();
        	String pos = KnowledgeBuilder.extractString(actionName,'_',-1);
        	if (!target.equals("b5"))
        		target = pos;
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
        	
			String attack = KnowledgeBuilder.getPAWNATTACK();
			List<String> pawns = kb.searchFacts("x", pos, attack);
			if(!pawns.isEmpty()) {
				continue;
			}
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
        	
        	String protectedBy = KnowledgeBuilder.getPROTECTED();
        	List<String> otherpieces = kb.searchFacts("x", pos,protectedBy);
        	boolean ownProtection = false;
        	boolean isprotected = false;
        	if (!otherpieces.isEmpty()) {
        		for (String pieceName:otherpieces) {
        			ownProtection = pieceName.equals(pieceId);
        			isprotected = !pieceName.contains(opponentType) && !ownProtection;
        			if (isprotected)
        				break;
        		}
        	}
        	if(!isprotected && isThreatened)
        		continue;
            if(localAction.getActionSchema().getName().contains(activePiece))
            	continue;
            // 5. EVALUERING: Beregn hvor verdifull posisjonen er
            int currentScore = calculateSquareValue(pieceId, pos, isThreatened,isprotected);
            // Oppdater dersom dette trekket ga høyere poengsum enn tidligere kandidater
            if (currentScore > maxActionScore) {
                maxActionScore = currentScore;
                bestAction = localAction;
            }

        }
		return bestAction;
	}
    private int calculateSquareValue(String pieceId, String targetSquare, boolean isThreatened,boolean protection) {
        int score = 0;

        String square = KnowledgeBuilder.getCENTERSQUARE();

        // B. Sentrumskontroll (d4, e4, d5, e5)
        if (kb.existsFact(square,targetSquare)) {
            score += 50;
        }
        boolean extended = targetSquare.equals("c4") || targetSquare.equals("f4") ||targetSquare.equals("c5") ||targetSquare.equals("f5");
        // C. Utvidet sentrum/aktive felt (c4, f4, c5, f5 osv.)
        if (extended && protection) {
            score += 20;
        }

        // D. Trekk til trygt uforstyrret felt (ikke truet i det hele tatt)
        if (!isThreatened) {
            score += 10;
        }
        if (protection) {
            score += 21;
        }

        return score;
    }

	@Override
	public void buildTerms(HashMap<String, Term> cParam, HashMap<String, Term> vParam) {
		// TODO Auto-generated method stub

	}

}
