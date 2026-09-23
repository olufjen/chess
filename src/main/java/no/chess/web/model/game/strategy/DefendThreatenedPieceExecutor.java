package no.chess.web.model.game.strategy;

import java.util.HashMap;
import java.util.List;

import aima.core.logic.fol.parsing.ast.Term;
import aima.core.logic.planning.ActionSchema;
import no.chess.web.model.Position;
import no.chess.web.model.game.APlayer;
import no.chess.web.model.game.AgamePiece;
import no.chess.web.model.game.ChessFolKnowledgeBase;
import no.chess.web.model.game.GroundGameAction;
import no.chess.web.model.game.GroundGameState;
import no.chess.web.model.game.KnowledgeBuilder;
import no.function.FunctionExecutor;
import no.games.chess.search.nondeterministic.GameAction;

/**
 * DefendThreatenedPieceExecutor
 * This executor checks if any of the player's pieces (officers) are under attack, and if so
 * determines the best action.
 * 
 * @author olufj
 */
public class DefendThreatenedPieceExecutor implements FunctionExecutor {

    private String myKey = KnowledgeBuilder.getDefendthreatenedpiece();
    private ChessFolKnowledgeBase kb;
    private GroundGameState state;
    private List<GameAction> availableActions;
    private APlayer player;
    private APlayer opponent;

    public DefendThreatenedPieceExecutor(GroundGameState state) {
        this.state = state;
        this.kb = state.getKnowledgeBase();
        this.availableActions = state.getActions();
        this.player = state.getPlayer();
        this.opponent = state.getOpponent();
    }	
    
    
	public String getMyKey() {
		return myKey;
	}


	public void setMyKey(String myKey) {
		this.myKey = myKey;
	}


	public APlayer getPlayer() {
		return player;
	}

	public void setPlayer(APlayer player) {
		this.player = player;
	}

	public APlayer getOpponent() {
		return opponent;
	}

	public void setOpponent(APlayer opponent) {
		this.opponent = opponent;
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
        // 1. FINN DEN MEST VERDIFULLE TRUEDE BRIKKEN VÅR
        String mostValuableThreatenedPiece = findMostValuableThreatenedPiece();

        if (mostValuableThreatenedPiece == null) {
            return null; // Ingen akutt trussel, la neste executor overta!
        }
        String pos = "";
        pos = KnowledgeBuilder.extractString(mostValuableThreatenedPiece,'_', -1); // 
        String thratenedPiece = KnowledgeBuilder.extractString(mostValuableThreatenedPiece,'_', 0); //
        // 2. PARER TRUSSELEN (Flytt, slå eller dekk)
        GroundGameAction bestDefense = findBestDefenseAction(thratenedPiece,pos);

        return bestDefense;
	}
	
    /**
     * Sjekker om noen av Hvits brikker er i reell fare.
     * En brikke er truet dersom:
     * a) Den er under angrep og ubeskyttet (HANGING), ELLER
     * b) Den er angrepet av en brikke av LAVERE verdi (f.eks. Dronning angrepet av Bonde/Springer).
     */
    private String findMostValuableThreatenedPiece() {
        String highestThreatPiece = null;
        int maxPieceValue = 0;
		List<AgamePiece> pieces = player.getMygamePieces();
		List<AgamePiece> opponentpieces = opponent.getMygamePieces();
		for (AgamePiece temppiece:pieces) {
			String pieceName = temppiece.getMyPiece().getOntlogyName();
			Position pos = temppiece.getmyPosition();
			if (pos == null)
				pos = temppiece.getHeldPosition();
			String square = pos.getPositionName();
			if (square == null)
				continue;
			String threatens = KnowledgeBuilder.getTHREATEN();
			String pred = KnowledgeBuilder.getPROTECTED();
			for (AgamePiece opponentPiece:opponentpieces) {
				boolean attackedByPawn = false;
				String oppieceName = opponentPiece.getMyPiece().getOntlogyName();
				boolean attackedByBlack = kb.existsFact(threatens,oppieceName,square);
				if (attackedByBlack)
					attackedByPawn = oppieceName.contains("Pawn");
				boolean properprotect = false;
				if (!attackedByBlack && !attackedByPawn)
					continue;
				if (attackedByBlack || attackedByPawn) {
					for (AgamePiece protectpiece:pieces) {
						String otherPieceName = protectpiece.getMyPiece().getOntlogyName();
						if (!otherPieceName.equals(pieceName)) {
							properprotect = kb.existsFact(pred, otherPieceName,square);
							if(properprotect) {
								break;
							}
						}
					}
				}
				int pieceVal = temppiece.getValue();

				// Reell trussel dersom:
				// 1. Angrepet og helt ubeskyttet, ELLER
				// 2. Angrepet av bonde og er en offiser (selv om den er dekket!)
				if ((attackedByBlack && !properprotect && pieceVal > 1) || (attackedByPawn && pieceVal > 1)) {
					if (pieceVal > maxPieceValue) {
						maxPieceValue = pieceVal;
						highestThreatPiece = pieceName+"_"+square; //square : posisjonen brikken er på og som er truet.
					}
				}

			}
		}
        return highestThreatPiece;
    }

    /**
     * Finner det beste trekket for å redde den truede brikken.
     */
    private GroundGameAction findBestDefenseAction(String threatenedPiece, String currentSquare) {
       
		List<AgamePiece> pieces = player.getMygamePieces();
		List<AgamePiece> opponentpieces = opponent.getMygamePieces();
		String threatens = KnowledgeBuilder.getTHREATEN();
		String pred = KnowledgeBuilder.getPROTECTED();
		String attackerPiece = "";
		boolean attackedByPawn = false;
		GroundGameAction bestAction = null;
        // Strategi A: Flytt den truede brikken til et trygt felt (Run)
        for (GameAction action : availableActions) {
        	GroundGameAction localAction = (GroundGameAction) action;
        	ActionSchema schema = localAction.getActionSchema();
        	String schemaName = schema.getName();
        	String pos = "";
            pos = KnowledgeBuilder.extractString(schemaName,'_', -1); // The new position....
            String threatenPiece = KnowledgeBuilder.extractString(schemaName,'_', 0); // For the threatened piece
 			AgamePiece thePiece = (AgamePiece)action.getGamePiece();
   			String pieceId =  thePiece.getMyPiece().getOntlogyName();
   			
   			boolean attackedByBlack = false;
            if (pieceId.equals(threatenedPiece)) {
       			for (AgamePiece opponentPiece:opponentpieces) { //OBS: Må løpe gjennom alle opponent's pieces for å fastslå om feltet er truet.
    				String oppieceName = opponentPiece.getMyPiece().getOntlogyName();
    				attackerPiece = oppieceName;
    				attackedByBlack = kb.existsFact(threatens,oppieceName,pos);
    				if (attackedByBlack)
    					attackedByPawn = oppieceName.contains("Pawn");
    				if (attackedByBlack)
    					break;		// If this new position is threatened by this opponent piece then break
      			}    				
       			boolean properprotect = false;
       			for (AgamePiece protectpiece:pieces) {
       				String otherPieceName = protectpiece.getMyPiece().getOntlogyName();
       				if (!otherPieceName.equals(threatenedPiece)) {
       					properprotect = kb.existsFact(pred, otherPieceName,pos);
       					if(properprotect) {
       						break;
       					}
       				}
       			}
       			if (bestAction != null) // Need a method to determine if the new position is a "good and stratetic" position
       				break;
       			if ((!attackedByBlack && !attackedByPawn)  || (properprotect && !attackedByPawn) && bestAction == null) {
       				bestAction = localAction; // Reddet brikken!
       			}

            }
        }

        // Strategi B: Slå brikken som angriper (Capture)
        for (GameAction action : availableActions) {
        	GroundGameAction localAction = (GroundGameAction) action;
        	ActionSchema schema = localAction.getActionSchema();
        	AgamePiece thePiece = (AgamePiece)action.getGamePiece();
        	String actionName = schema.getName();
        	String pieceId =  thePiece.getMyPiece().getOntlogyName();
        	String targetSquare = KnowledgeBuilder.extractString(actionName,'_', -1); // "f3"  
        	
            // Hvis et trekk slår en brikke som angriper vårt felt
        	for (AgamePiece opponentPiece:opponentpieces) {
        		String oppieceName = opponentPiece.getMyPiece().getOntlogyName();
    			Position pos = opponentPiece.getMyPosition();
    			if (pos == null)
    				pos = opponentPiece.getHeldPosition();
    			String square = pos.getPositionName();
        		if (attackerPiece.equals(oppieceName) && targetSquare.equals(square) && !attackedByPawn) { // Dette må revideres !!!!
        			bestAction = localAction;
        			break;
        		}
        	}
          }
        return bestAction;
    }
	@Override
	public void buildTerms(HashMap<String, Term> cParam, HashMap<String, Term> vParam) {
		// TODO Auto-generated method stub

	}

}
