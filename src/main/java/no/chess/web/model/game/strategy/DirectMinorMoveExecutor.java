package no.chess.web.model.game.strategy;

import java.util.HashMap;
import java.util.List;

import aima.core.logic.fol.parsing.ast.Term;
import aima.core.logic.planning.ActionSchema;
import no.chess.web.model.game.ChessFolKnowledgeBase;
import no.chess.web.model.game.GroundGameAction;
import no.chess.web.model.game.KnowledgeBuilder;
import no.chess.web.model.game.AgamePiece;
import no.function.FunctionExecutor;
import no.games.chess.search.nondeterministic.GameAction;

/**
 * DirectMinorMoveExecutor
 * This class checks to see if there are minor officers (bishops or knights that can be
 * safely moved accoring the the MINORMOVE rule in the kb 
 * OBS must be tested for threatenedby facts!
 * 
 */
public class DirectMinorMoveExecutor implements FunctionExecutor {
    private ChessFolKnowledgeBase kb;
    private GroundGameAction availableAction;
    private  List<GameAction> availableActions;
    private String key;
    private List<AgamePiece> pieces;
    
	public DirectMinorMoveExecutor(ChessFolKnowledgeBase kb, GroundGameAction availableAction) {
		super();
		this.kb = kb;
		this.availableAction = availableAction;
		key = KnowledgeBuilder.getMINORMOVE();
	}

	/**
	 * Contructor for DirectMinorMove executor 
	 * @param kb
	 * @param availableActions
	 * @param pieces
	 */
	public DirectMinorMoveExecutor(ChessFolKnowledgeBase kb, List<GameAction> availableActions,List<AgamePiece> pieces) {
		super();
		this.kb = kb;
		this.availableActions = availableActions;
		this.pieces = pieces;
		this.key = KnowledgeBuilder.getMINORMOVE();
	}

	public List<GameAction> getAvailableActions() {
		return availableActions;
	}

	public void setAvailableActions(List<GameAction> availableActions) {
		this.availableActions = availableActions;
	}

	public List<AgamePiece> getPieces() {
		return pieces;
	}

	public void setPieces(List<AgamePiece> pieces) {
		this.pieces = pieces;
	}

	public GroundGameAction getAvailableAction() {
		return availableAction;
	}

	public void setAvailableAction(GroundGameAction availableAction) {
		this.availableAction = availableAction;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	/**
	 *
	 */
	/**
	 *
	 */
	@Override
	public Object execute() {
        // Vi går gjennom alle de LOVLIGE handlingene motoren vurderer akkurat nå
     	if(availableActions != null && !availableActions.isEmpty()) {
       		for (GameAction action : availableActions) {
       			GroundGameAction localAction = (GroundGameAction) action;
       			ActionSchema schema = localAction.getActionSchema();
       			String actionName = schema.getName();
       			String targetSquare = KnowledgeBuilder.extractString(actionName,'_', -1); // "f3"
       			AgamePiece piece = (AgamePiece) localAction.getGamePiece();
       			// 1. Ekstraher brikke (a) og destination-felt (b) fra lovlig handling
       			// F.eks. fra Move(WhiteKnight1, g1, f3) får vi a = WhiteKnight1, b = f3
       			String pieceName = piece.getMyPiece().getOntlogyName();   // "WhiteKnight1"
       			// 2. Vi spør KB REGELEN DIREKTE!
       			// Har KB utledet at dette spesifikke paret utgjør en MINORMOVE?
       			String queryFact = key;
       			boolean properprotect = false;
       			String pred = KnowledgeBuilder.getPROTECTED();
       			for (AgamePiece otherpiece:pieces) {
       				String otherPieceName = otherpiece.getMyPiece().getOntlogyName();
       				if (!otherPieceName.equals(pieceName)) {
       					properprotect = kb.existsFact(pred, otherPieceName, targetSquare);
       					if (properprotect)
       						break;
       				}
       			}
       			if (kb.askRule(queryFact,pieceName,targetSquare) && properprotect) {
       				// REGELEN SLÅR TIL! KB har gjort hele jobben og bekreftet at trekket er smart.
       				availableAction = localAction;
       				return availableAction; 
       			}
       		}
     	}
		return null; // Ingen av de tilgjengelige trekkene tilfredsstilte MINORMOVE-regelen
	}

	@Override
	public void buildTerms(HashMap<String, Term> cParam, HashMap<String, Term> vParam) {


	}

}
