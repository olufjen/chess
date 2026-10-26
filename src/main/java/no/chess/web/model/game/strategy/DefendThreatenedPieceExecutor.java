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
 * If no pieces are threatened then it investigates other posssible moves
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
    private ExecutorHelper helper;
    private boolean pawnAttack = false; // true if player's piece is attacked by an opponent pawn
    private boolean opponentAttack = false; // true if player's piece is attacked by an opponent piece
    private String attackerPiece = null; // The name of the opponent piece that attacks player's piece
    private String attackerPos = null;  // The position of the opponent piece 
	private PieceEvaluationService evaluationService;
	private boolean threats = false; // This flag is true when a player's officer is under attack. This move is top priority 
	private GroundGameAction defenderAction = null;// Suggested move if any of player's pieces are threatened 
	private GroundGameAction otherAction = null;// Suggested move if none of player's pieces are threatened
	
    public DefendThreatenedPieceExecutor(GroundGameState state) {
        this.state = state;
        this.kb = state.getKnowledgeBase();
        this.availableActions = state.getActions();
        this.player = state.getPlayer();
        this.opponent = state.getOpponent();
        helper = new ExecutorHelper(kb); 
		evaluationService = new PieceEvaluationService(kb,player,opponent);
    }	
    
    
	public GroundGameAction getDefenderAction() {
		return defenderAction;
	}


	public void setDefenderAction(GroundGameAction defenderAction) {
		this.defenderAction = defenderAction;
	}


	public GroundGameAction getOtherAction() {
		return otherAction;
	}


	public void setOtherAction(GroundGameAction otherAction) {
		this.otherAction = otherAction;
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

	public boolean isThreats() {
		return threats;
	}


	public void setThreats(boolean threats) {
		this.threats = threats;
	}


	@Override
	public Object execute() {
        // 1. FINN DEN MEST VERDIFULLE TRUEDE BRIKKEN VÅR
        String mostValuableThreatenedPiece = findMostValuableThreatenedPiece();

        if (mostValuableThreatenedPiece == null) {
        	otherAction =  pieceMove();
            return otherAction; // Ingen akutt trussel, check if possible minor move or to take opponent piece
        }
        String pos = "";
    
        pos = KnowledgeBuilder.extractString(mostValuableThreatenedPiece,'_', -1); // 
        String thratenedPiece = KnowledgeBuilder.extractString(mostValuableThreatenedPiece,'_', 0); //
        GroundGameAction bestAttack = counterAttack(attackerPiece,attackerPos);
        defenderAction = bestAttack;
        if (bestAttack != null)
        	return bestAttack;
        // 2. PARER TRUSSELEN (Flytt, slå eller dekk)
        GroundGameAction bestDefense = findBestDefenseAction(thratenedPiece,pos);
        defenderAction = bestDefense;
        if (bestDefense != null)
            threats = true;
        return bestDefense;
	}
	/**
	 * pieceMove
	 * This method is called if there are no threats to player's pieces
	 * @return a GroundGameAction 
	 */
	private GroundGameAction pieceMove() {
		GroundGameAction localAction = null;
		GroundGameAction chosenAction = null;
		String minormove = KnowledgeBuilder.getMINORMOVE();
		String pawnmove = KnowledgeBuilder.getPAWNATTACK();
		int bestScore = 0;
		for (GameAction action : availableActions) {
			localAction = (GroundGameAction) action;
			ActionSchema schema = localAction.getActionSchema();
			String schemaName = schema.getName();
			String pos = "";
			pos = KnowledgeBuilder.extractString(schemaName,'_', -1); // The new position....
			String usedPiece = KnowledgeBuilder.extractString(schemaName,'_', 0); // For the used piece
			AgamePiece thePiece = (AgamePiece)action.getGamePiece();
			if (!thePiece.isActive())
				continue;
			String pieceId =  thePiece.getMyPiece().getOntlogyName();
			Position currentPos = thePiece.getmyPosition();
			String thisPos = currentPos.getPositionName();
			int score = 0;
			boolean taken = evaluationService.takeOpponent(pieceId, pos);
			if (taken) {
				if (kb.askRule(minormove, pieceId, pos)) {
					chosenAction = localAction;
					bestScore = 100;
					break;
				}
				if (kb.existsFact(pawnmove, pos)) { // Can it be taken by a pawn?
					chosenAction = localAction;
					bestScore = 200;
					break;
				}
			}
			if (pieceId.contains("Bishop")) {
				score = evaluationService.evaluateBishopDevelopment(pieceId, pos,thisPos);
				if (score > bestScore) {
					bestScore = score;
					chosenAction = localAction;
				}
			}
			if (pieceId.contains("Knight")) {
				score = evaluationService.evaluateKnightsDevelopment(pieceId, pos,thisPos);
				if (score > bestScore) {
					bestScore = score;
					chosenAction = localAction;
				}
			}
		}
		chosenAction.setValuePoints(bestScore);
		return chosenAction;
	}
	/**
	 * counterAttack
	 * This method checks if there is a counterattack possible
	 * @param attackerPiece Name of piece that threatens our pieces
	 * @param attackpos Name of position of attackerPiece
	 * @return a GroundGameAction that creates a pin against opponent king or queen
	 */
	private GroundGameAction counterAttack(String attackerPiece,String attackpos) {
		List<AgamePiece> opponentpieces = opponent.getMygamePieces();
		 GroundGameAction bestAttack = null;
		 int score = 0;
		for (AgamePiece piece:opponentpieces) {
			String oppieceName = piece.getMyPiece().getOntlogyName();
			String opPosition = piece.getmyPosition().getPositionName();
			if (oppieceName.contains("King") ||oppieceName.contains("Queen") ) {
			       for (GameAction action : availableActions) {
			        	GroundGameAction localAction = (GroundGameAction) action;
			        	ActionSchema schema = localAction.getActionSchema();
			        	String schemaName = schema.getName();
			        	String pos = "";
			            pos = KnowledgeBuilder.extractString(schemaName,'_', -1); // The new position....
			            String name = KnowledgeBuilder.extractString(schemaName,'_', 0);
			            boolean longRange = helper.isLongRangePiece(name);
			            boolean pinonFile = helper.canPinOnFile(name); //A queen or rook
			            boolean pinonRank = helper.canPinOnRank(name); // AQueen or Rook
			            boolean pinonDiagonal = helper.canPinOnDiagonal(name); // A Queen or Bishop
			            if (!longRange)
			            	continue;
			            String occupy = KnowledgeBuilder.getOCCUPIES();
			            String kqpos = kb.checkPosition(oppieceName, occupy);
			            boolean pinPossible = helper.createsPinAgainstpiece(name, pos, attackpos,kqpos);
			            if (pinPossible) {
			            	bestAttack = localAction;
			            	score = 200;
			            	bestAttack.setValuePoints(score);
			            	break;
			            }
			            
			       }
			}
		}
		
		return bestAttack;
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
			if (!temppiece.isActive())
				continue;
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
				boolean attackedByBlack = false;
				String oppieceName = opponentPiece.getMyPiece().getOntlogyName();
				String opPosition = opponentPiece.getmyPosition().getPositionName();
				attackedByBlack = kb.existsFact(threatens,oppieceName,square);
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
					pawnAttack = attackedByPawn;
					opponentAttack = attackedByBlack;
					attackerPiece = oppieceName;
					attackerPos = opPosition;
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
        	int score = 0;
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
       			if (bestAction != null) { // Need a method to determine if the new position is a "good and stratetic" position
       				break;
       			}
       			if ((!attackedByBlack && !attackedByPawn)  || (properprotect && !attackedByPawn) && bestAction == null) {
       				bestAction = localAction; // Reddet brikken!
       				score = 150;
       				bestAction.setValuePoints(score);
       			}

            }
        }

        // Strategi B: Slå brikken som angriper (Capture)
        for (GameAction action : availableActions) {
        	int score = 0;
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
        			score = 160;
        			bestAction.setValuePoints(score);
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
