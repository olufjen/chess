package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aima.core.logic.fol.domain.FOLDomain;
import aima.core.logic.fol.inference.InferenceResult;
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
 * It is created whenever the chessproblemsolver is created
 * The opponent agent object must be able to return with a best strategy for moves 
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
	private APlayer myPlayer = null; // This is the opponent of the game
	private APlayer opponent = null; // This is the chess player of the game
	private FOLKnowledgeBase folKb = null; // The full knowledge base
	private FOLDomain chessDomain;
	private FOLGamesFCAsk forwardChain;
	private FOLGamesBCAsk backwardChain;
	private ChessFolKnowledgeBase localKb; // The local temporary knowledge base
	private Map<String,Position>possiblePositions = null; // Contains the positions of possible positions
	private List<String>positionKeys = null;
	private List<String>myPieceNames = null;
	
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
    
	public OpponentAgent(ChessStateImpl stateImpl, PlayGame game, APlayer myPlayer, APlayer opponent,FOLKnowledgeBase folKb,FOLDomain chessDomain) {
		super();
		this.stateImpl = stateImpl;
		this.game = game;
		this.myPlayer = myPlayer;
		this.opponent = opponent;
		this.folKb = folKb;
		this.chessDomain = chessDomain;
		forwardChain = new FOLGamesFCAsk(); // A Forward Chain inference procedure see p. 332
		backwardChain = new FOLGamesBCAsk(); // A backward Chain inference procedure see p. 337
		localKb = new ChessFolKnowledgeBase(chessDomain, forwardChain);
		actions = this.stateImpl.getActions(myPlayer);
		this.myPlayer.setActions(actions);
		possiblePositions = new HashMap<String,Position>(); // Which positions are reachable
		positionKeys = new ArrayList<String>();// The key for positions that are reachable
		myPieceNames = new ArrayList<String>(); // A list of opponent pieces that are active
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
	 * This method creates facts about the opponent pieces to the local knowledge base
	 * These facts are: which positions they occupy and where they can move to.
	 * It is called when the opponent agent is created.                           
	 */
	public void defineFacts() {
		Position heldPosition = null;
		for (AgamePiece piece:myPlayer.getMygamePieces()) {
			if (piece.isActive()){
				heldPosition = piece.getMyPosition();
				if (heldPosition == null) {
					heldPosition = piece.getHeldPosition();
				}
				String occupies = piece.returnPredicate();
				String pieceName = piece.getMyPiece().getOntlogyName();
				String posName = heldPosition.getPositionName();
				localKb.createfacts(occupies, posName, pieceName);
				myPieceNames.add(pieceName);
			}
		}
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
	/**
	 * tellFacts
	 * This method creates the OPPONENTTO fact to the knowledge base
	 * @param piece
	 * @param pos
	 * @param predicate
	 */
	public void tellFacts(String piece,String pos,String predicate) {
		Constant pieceVariable = new Constant(piece);
		Constant posVariable = new Constant(pos);
		List<Term> terms = new ArrayList<Term>();
		terms.add(pieceVariable);
		terms.add(posVariable);
		Predicate folPredicate = new Predicate(predicate,terms);
		folKb.tell(folPredicate);
		localKb.tell(folPredicate);
//		writer.println("Opponent piece "+piece+"\ncan move to "+pos);
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
	/**
	 * checkFacts
	 * This method checks if a given predicate is true in the local first order knowledge base.
	 * @param pieceName
	 * @param posName
	 * @param fact
	 * @return true if the predicate is true
	 */
	public boolean checkFacts(String pieceName,String posName,String fact) {
		boolean result = false;
		Constant pieceVariable= new Constant(pieceName);
		Constant posVariable = new Constant(posName);
		List<Term> reachableTerms = new ArrayList<Term>();
		reachableTerms.add(pieceVariable);
		reachableTerms.add(posVariable);
		Predicate reachablePredicate = new Predicate(fact,reachableTerms);
		InferenceResult backWardresult =  backwardChain.ask(localKb, reachablePredicate);
		result = backWardresult.isTrue();
		return result;
	}
	private boolean checkReachable(String pieceName,String posName,String key) {
		boolean result = false;
		String lastPos = null;
		String sep = "_";
		int posIndex = key.indexOf(posName);
		if ( posIndex > 0) {
			lastPos = key.substring(posIndex-2,posIndex);
			if (lastPos != null && !lastPos.isEmpty()) {
				result = checkFacts(pieceName+sep+lastPos,posName,REACHABLE);
				return result;
			}
		}
		return result;
	}
	/**
	 * chooseStrategy
	 * Based on the facts found in the probepossibilities method
	 * This method chooses a strategy for the next move.
	 * @param actions The actions available to player
	 */
	public void chooseStrategy(List<ChessActionImpl>actions) {
		writer.println("Choose strategy");

		for (ChessAction action:actions) {
			ChessActionImpl localAction = (ChessActionImpl) action;
			ApieceMove move = localAction.getPossibleMove();
			AgamePiece piece = localAction.getChessPiece();
			String pieceName = piece.getMyPiece().getOntlogyName();
			String sep = "_";
			if (move != null) {
				List<Position> available = localAction.getAvailablePositions();
				List<Position> removed = localAction.getPositionRemoved();
				for (Position apos: available) {
					String posName = apos.getPositionName();
					Position pos =  (Position) removed.stream().filter(c -> c.getPositionName().contains(posName)).findAny().orElse(null);
					if(!piece.checkRemoved(apos)) {
//						String posA = apos.getPositionName();
						boolean posOccupies = checkFacts(pieceName,posName,OCCUPIES);
						for (String key: positionKeys) {
							if (key.contains(pieceName+sep+posName)) {
								Position possiblepos = possiblePositions.get(key);
								if (possiblepos != null) {
									String posA = possiblepos.getPositionName();
									boolean reachpos = checkReachable(pieceName, posA, key);
									writer.println("Checking reachable for key "+key+" occupies is "+posOccupies+" for position "+posName+" is reachable "+reachpos+" for pos "+posA);
								}
							}
						}
  					}
				}
			}
		}
		for (String key: positionKeys) {
			Position pos = possiblePositions.get(key);
			if (pos != null) {
				writer.println(key);
				writer.println(pos.toString());
			}
			
		}
	}
	/**
	 * probepossibilities
	 * This method probes possible reachable positions given a
	 * possible occupied position from the available actions
	 * @param actions - The actions available to player
	 * @param player - The player of the game
	 */
	public void probepossibilities(List<ChessActionImpl>actions,APlayer player) {
		Position heldPosition = null;
		possiblePositions.clear();
		positionKeys.clear();
		for (ChessAction action:actions) {
			ChessActionImpl localAction = (ChessActionImpl) action;
			AgamePiece piece = localAction.getChessPiece();
			heldPosition = piece.getMyPosition();
			if (heldPosition == null) {
				heldPosition = piece.getHeldPosition();
			}
			String occupiesNow = piece.returnPredicate();
			String piecenameNow = piece.getMyPiece().getOntlogyName();
			String posnameNow = heldPosition.getPositionName();
			localKb.createfacts(occupiesNow, posnameNow, piecenameNow); //Must also set current occupied position
			List<Position> availablePositions = piece.getNewlistPositions();
			for (Position pos:availablePositions) {
				if (!piece.checkRemoved(pos)) {
					piece.produceLegalmoves(pos); // Produces new reachable positions
					HashMap<String,Position> reachablePositions = piece.getReacablePositions();
					ChessActionImpl tempaction = new ChessActionImpl(reachablePositions,piece,player,myPlayer);
//					player.calculatePreferredPosition(piece, tempaction); // MUst use a new action
					String occupies = piece.returnPredicate();
					String piecename = piece.getMyPiece().getOntlogyName();
					String posname = pos.getPositionName();
					localKb.createfacts(occupies, posname, piecename);
					piecename = piecename+"_"+posname; //OBS: Separate piece and position !!!
					tellnewFacts(piece,piecename);
				}
			}
			piece.produceLegalmoves(heldPosition);
			player.calculatePreferredPosition(piece, localAction);
		}
		localKb.writeKnowledgebase();
	}
	/**
	 * tellnewFacts
	 * This method creates new REACHABLE facts to the temporary knowledge base
	 * It is called from the probepossiblities method
	 * @param piece
	 * @param name
	 */
	public void tellnewFacts(AgamePiece piece,String name) {
		List<Position> availablePositions = piece.getNewlistPositions();
		String piecename = piece.getMyPiece().getOntlogyName();
		if (piecename.equals("WhiteRook2")) {
			System.out.println(piece.toString());
		}
		for (Position pos:availablePositions) {
			if (!piece.checkRemoved(pos)) {
//				String piecename = piece.getMyPiece().getOntlogyName();
				String posname = pos.getPositionName();
				possiblePositions.put(name+posname, pos);
				positionKeys.add(name+posname);
				localKb.createfacts(REACHABLE, posname, name);
			}
		}
	}
	public void writeFacts() {
		localKb.writeKnowledgebase();
	}
}
