package no.chess.web.model.game.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import aima.core.logic.fol.parsing.ast.Term;
import aima.core.logic.planning.ActionSchema;
import no.chess.web.model.Position;
import no.chess.web.model.game.APlayer;
import no.chess.web.model.game.AgamePiece;
import no.chess.web.model.game.ChessFolKnowledgeBase;
import no.chess.web.model.game.GroundGameAction;
import no.chess.web.model.game.KnowledgeBuilder;
import no.function.FunctionExecutor;
import no.games.chess.search.nondeterministic.GameAction;

/**
 * DevelopMinorExecutor
 * This Executor checks if any Minor pieces are not developed (they are still in HOMESQUARE)
 * The execute routine returns the first available groundaction for undeveloped pieces
 */
public class DevelopMinorExecutor implements FunctionExecutor {
	private APlayer myPlayer;
    private ChessFolKnowledgeBase kb;
    private List<AgamePiece> homePieces;
    private  List<GameAction> availableActions;
    private String myKey = KnowledgeBuilder.getDevelopPiece();
	private GroundGameAction determinedAction = null; // This action is set when a planning procedure takes place from result(s.a) and GroundGameAction.performAction. (The testEnd procedure)

	public DevelopMinorExecutor(APlayer myPlayer) {
		super();
		this.myPlayer = myPlayer;
		homePieces = new ArrayList<AgamePiece>();
	}

	public DevelopMinorExecutor(APlayer myPlayer, ChessFolKnowledgeBase kb,List<GameAction> availableActions) {
		super();
		this.myPlayer = myPlayer;
		this.kb = kb;
		homePieces = new ArrayList<AgamePiece>();
		this.availableActions = availableActions;
	}

	public GroundGameAction getDeterminedAction() {
		return determinedAction;
	}

	public void setDeterminedAction(GroundGameAction determinedAction) {
		this.determinedAction = determinedAction;
	}

	public List<GameAction> getAvailableActions() {
		return availableActions;
	}

	public void setAvailableActions(List<GameAction> availableActions) {
		this.availableActions = availableActions;
	}

	public String getMyKey() {
		return myKey;
	}

	public void setMyKey(String myKey) {
		this.myKey = myKey;
	}

	public APlayer getmyPlayer() {
		return myPlayer;
	}

	public void setmyPlayer(APlayer myPlayer) {
		this.myPlayer = myPlayer;
	}
	

	public List<AgamePiece> getHomePieces() {
		return homePieces;
	}

	public void setHomePieces(List<AgamePiece> homePieces) {
		this.homePieces = homePieces;
	}

	@Override
	public Object execute() {
		GroundGameAction bestAction = null;
		List<AgamePiece> pieces = myPlayer.getMygamePieces();
		homePieces.clear();
		String activeSchema = "";
		String activePiece = "";
		if (determinedAction != null) {
			activeSchema = determinedAction.getActionSchema().getName();
			activePiece = KnowledgeBuilder.extractString(activeSchema,'_',0);
		}

		String occupies = KnowledgeBuilder.getOCCUPIES();
		AgamePiece piece = null;
		boolean occupyHome = false;
		for (AgamePiece temppiece:pieces) {
			String pieceName = temppiece.getMyPiece().getOntlogyName();
			String minor = KnowledgeBuilder.getMINORPIECE();
			boolean minorPiece = kb.existsFact(minor, pieceName);
			Position pos = temppiece.getHomePosition();
			String posName = pos.getPositionName();
			occupyHome = kb.existsFact(occupies,pieceName,posName);
			if (occupyHome && minorPiece) {
				piece = temppiece;
				homePieces.add(piece);
			}	
		}
       	boolean foundPiece = false;
       	GroundGameAction localAction = null;
       	if(availableActions != null && !availableActions.isEmpty()) {
    		for (GameAction action : availableActions) {
            	localAction = (GroundGameAction) action;
            	AgamePiece thePiece = (AgamePiece)action.getGamePiece();
            	ActionSchema schema = localAction.getActionSchema();
               	String pieceId =  thePiece.getMyPiece().getOntlogyName();

               	if (!homePieces.isEmpty()) {
                  	for (AgamePiece homePiece:homePieces) {
                   		String homePieceName = homePiece.getMyPiece().getOntlogyName();
                   		if(pieceId.equals(homePieceName) && !pieceId.equals(activePiece)){
                   			foundPiece = true;
                   			break;
                   		}         		
                  	}
               	}
               	if (foundPiece) {
               		break;
               	}
    		}
       	}

		if(foundPiece)
			bestAction = localAction;
		return bestAction;
	}

	@Override
	public void buildTerms(HashMap<String, Term> cParam, HashMap<String, Term> vParam) {
		// TODO Auto-generated method stub

	}

}
