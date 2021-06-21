package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import aima.core.logic.fol.domain.FOLDomain;
import aima.core.logic.fol.kb.FOLKnowledgeBase;
import aima.core.logic.fol.parsing.ast.Constant;
import aima.core.logic.fol.parsing.ast.Predicate;
import aima.core.logic.fol.parsing.ast.Term;
import no.chess.web.model.PlayGame;
import no.chess.web.model.Position;
import no.games.chess.ChessAction;
import no.games.chess.fol.FOLGamesBCAsk;
import no.games.chess.fol.FOLGamesFCAsk;

/**
 * The opponent agent object,
 * contains all opponent available actions, and their action schemas.
 * The opponent agent object must be able to answer the  question,
 * Which of my pieces are threatened or lost if a make a certain move.
 * @author olj
 *
 */
public class OpponentAgent {

	private ChessStateImpl stateImpl = null;
	private ChessActionImpl localAction = null;
	private List <ChessAction> actions = null; // All actions available to the opponent
	private String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\opponent.txt";
	private PrintWriter writer = null;
	private FileWriter fw = null;
	private PlayGame game = null;
	private APlayer myPlayer = null;
	private APlayer opponent = null;
	private FOLKnowledgeBase folKb = null;
	private FOLDomain chessDomain;
	private FOLGamesFCAsk forwardChain;
	private FOLGamesBCAsk backwardChain;
	
	private String ACTION;
	private String PROTECTED;
	private String simpleProtected;
	private String ATTACKED;
	private String CAPTURE;
	private String CONQUER;
	private String THREATEN;
	private String OWNER;
	private String MOVE;
	private String REACHABLE;
    private String CANMOVE;
	private String SAFEMOVE;
	private String STRIKE;
	private String PIECETYPE;
	private String PLAY;
	private String PAWN;
	private String KNIGHT;
	private String BISHOP;
	private String ROOK;
	private String KING;
	private String QUEEN;
	private String PAWNMOVE;
	private String playerName = "";
    private String OCCUPIES = "";
    private String PAWNATTACK ="";
    private String BOARD;
    private String PLAYER;
    private String CASTLE;
    private String OPPONENTTO;
    private String POSSIBLETHREAT;
    
	public OpponentAgent(ChessStateImpl stateImpl, PlayGame game, APlayer myPlayer, APlayer opponent,FOLKnowledgeBase folKb) {
		super();
		this.stateImpl = stateImpl;
		this.game = game;
		this.myPlayer = myPlayer;
		this.opponent = opponent;
		this.folKb = folKb;
		actions = this.stateImpl.getActions(myPlayer);
		this.myPlayer.setActions(actions);
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
	    setPredicatenames();
	    defineFacts();
	    writer.flush();
	}
	  public void setPredicatenames() {
			ACTION = KnowledgeBuilder.getACTION();
			ATTACKED = KnowledgeBuilder.getATTACKED();
			CANMOVE = KnowledgeBuilder.getCANMOVE();
			CAPTURE =  KnowledgeBuilder.getCAPTURE();
			CONQUER = KnowledgeBuilder.getCONQUER();
			MOVE =  KnowledgeBuilder.getMOVE();
			OWNER = KnowledgeBuilder.getOWNER();
			PROTECTED =  KnowledgeBuilder.getPROTECTED();
			REACHABLE = KnowledgeBuilder.getREACHABLE();
			SAFEMOVE = KnowledgeBuilder.getSAFEMOVE();
			STRIKE = KnowledgeBuilder.getSTRIKE();
			simpleProtected =  KnowledgeBuilder.getSimpleProtected();
			THREATEN = KnowledgeBuilder.getTHREATEN();
			PIECETYPE = KnowledgeBuilder.getPIECETYPE();
			PLAY = 	KnowledgeBuilder.getPLAY();
			PAWN = KnowledgeBuilder.getPAWN();
			KNIGHT = KnowledgeBuilder.getKNIGHT();
			BISHOP = KnowledgeBuilder.getBISHOP();
			ROOK = KnowledgeBuilder.getROOK();
			KING = KnowledgeBuilder.getKING();
			QUEEN = KnowledgeBuilder.getQUEEN();
			OCCUPIES = KnowledgeBuilder.getOCCUPIES();
			PAWNMOVE = KnowledgeBuilder.getPAWNMOVE();
			PAWNATTACK = KnowledgeBuilder.getPAWNATTACK();
			BOARD = KnowledgeBuilder.getBOARD();
			PLAYER = KnowledgeBuilder.getPLAYER();
			CASTLE = KnowledgeBuilder.getCASTLE();
			OPPONENTTO = KnowledgeBuilder.getOPPONENTTO();
			POSSIBLETHREAT = KnowledgeBuilder.getPOSSIBLETHREAT();

	  }

	public ChessStateImpl getStateImpl() {
		return stateImpl;
	}
	public void setStateImpl(ChessStateImpl stateImpl) {
		this.stateImpl = stateImpl;
	}
	public ChessActionImpl getLocalAction() {
		return localAction;
	}
	public void setLocalAction(ChessActionImpl localAction) {
		this.localAction = localAction;
	}
	public List<ChessAction> getActions() {
		return actions;
	}
	public void setActions(List<ChessAction> actions) {
		this.actions = actions;
	}
	public PlayGame getGame() {
		return game;
	}
	public void setGame(PlayGame game) {
		this.game = game;
	}
	public APlayer getMyPlayer() {
		return myPlayer;
	}
	public void setMyPlayer(APlayer myPlayer) {
		this.myPlayer = myPlayer;
	}
	public APlayer getOpponent() {
		return opponent;
	}
	public void setOpponent(APlayer opponent) {
		this.opponent = opponent;
	}
	/**
	 * defineFacts
	 * 
	 */
	public void defineFacts() {
		for (ChessAction action:actions) {
			ChessActionImpl localAction = (ChessActionImpl) action;
			ApieceMove move = localAction.getPossibleMove();
			AgamePiece piece = localAction.getChessPiece();
			String pieceName = piece.getMyPiece().getOntlogyName();
			if (move != null) {
				List<Position> available = localAction.getAvailablePositions();
				List<Position> removed = localAction.getPositionRemoved();
				for (Position apos: available) {
					String posName = apos.getPositionName();
					Position pos =  (Position) removed.stream().filter(c -> c.getPositionName().contains(posName)).findAny().orElse(null);
//					if (pos != null) {
						if(!piece.checkRemoved(apos)) {
							String posA = apos.getPositionName();
							tellFacts(pieceName,posA,OPPONENTTO);
						}
//					}
				}
			}
		}
	}
	public void tellFacts(String piece,String pos,String predicate) {
		Constant pieceVariable = new Constant(piece);
		Constant posVariable = new Constant(pos);
		List<Term> terms = new ArrayList<Term>();
		terms.add(pieceVariable);
		terms.add(posVariable);
		Predicate folPredicate = new Predicate(predicate,terms);
		folKb.tell(folPredicate);
		writer.println("Opponent piece "+piece+"\ncan move to "+pos);
	}
	/**
	 * probeConsequences
	 * This action examines consequences a player's action may have.                                                                                    
	 * @param playerAction
	 */
	public void probeConsequences(ChessActionImpl playerAction) {
		writer.println("Chosen action\n"+playerAction);
		writer.println("Opponent actions:");
		for (ChessAction action:actions) {
			ChessActionImpl localAction = (ChessActionImpl) action;
			writer.println(localAction);
		}
	}
}
