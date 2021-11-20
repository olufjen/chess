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
import no.games.chess.ChessPieceType;
import no.games.chess.fol.FOLGamesBCAsk;
import no.games.chess.fol.FOLGamesFCAsk;

/**
 * The Opponent Agent is both a utility based agent and a goal based agent.
 * (For definition see p. 52 and p. 53.)
 * The opponent agent object, is a Knowledge based agent and it
 * contains all opponent available actions, and their action schemas.
 * It is created whenever the chessproblemsolver is created.
 * It also contains a FOL strategy Knowledge base, that contains
 * knowledge of possible moves one ply ahead.
 * Based on the knowledge from the strategy knowledge base, the agent maintains a Performance measure.
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
	private ChessFolKnowledgeBase folKb = null; // The parent knowledge base
	private FOLDomain chessDomain;
	private FOLGamesFCAsk forwardChain;
	private FOLGamesBCAsk backwardChain;
	private ChessFolKnowledgeBase localKb; // The local strategy knowledge base
	private APerformance performanceMeasure;
	private Map<String,Position>protectedPositions = null; // Contains the set of protected positions
	private Map<String,Position>possiblePositions = null; // Contains the set of possible positions
	// The key for this map is of the form: WhiteBishop2_c4d5
	private HashMap<String,Position> positions; // The original HashMap of positions
	private List<String>positionKeys = null; // contains position keys of the form: WhiteBishop2_c4d5:
/*
 * From position c4 the white bishop can reach d5	
 */
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
    
	public OpponentAgent(ChessStateImpl stateImpl, PlayGame game, APlayer myPlayer, APlayer opponent,ChessFolKnowledgeBase folKb,FOLDomain chessDomain) {
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
		localKb.setBackWardChain(backwardChain);
		actions = this.stateImpl.getActions(myPlayer); // These are the opponent's actions
		this.myPlayer.setActions(actions);
		possiblePositions = new HashMap<String,Position>(); // Which positions are reachable
		protectedPositions = new HashMap<String,Position>(); //Which positions are protected
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
	    performanceMeasure = new APerformance(positions,myPlayer,opponent,folKb,localKb,chessDomain,forwardChain,backwardChain);
	    performanceMeasure.setAgent(this);
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

	public String getPROTECTED() {
		return PROTECTED;
	}
	public void setPROTECTED(String pROTECTED) {
		PROTECTED = pROTECTED;
	}
	public String getREACHABLE() {
		return REACHABLE;
	}
	public void setREACHABLE(String rEACHABLE) {
		REACHABLE = rEACHABLE;
	}
	
	public String getTHREATEN() {
		return THREATEN;
	}
	public void setTHREATEN(String tHREATEN) {
		THREATEN = tHREATEN;
	}
	public String getPIECETYPE() {
		return PIECETYPE;
	}
	public void setPIECETYPE(String pIECETYPE) {
		PIECETYPE = pIECETYPE;
	}
	public HashMap<String, Position> getPositions() {
		return positions;
	}
	public void setPositions(HashMap<String, Position> positions) {
		this.positions = positions;
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
	/**
	 * checkReachable
	 * This method checks if a certain piece can reach a certain position
	 * @param pieceName
	 * @param posName
	 * @param key
	 * @return
	 */
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
		performanceMeasure.setPositions(positions);
		performanceMeasure.setPositionKeys(positionKeys); // contains position keys of the form: WhiteBishop2_c4d5:
		performanceMeasure.occupiedPositions(); // Finds positions occupied by the opponent's pieces
		performanceMeasure.findReachable();
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
					if(!piece.checkRemoved(apos)|| piece.checkFriendlyPosition(pos)) {
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
	    writer.flush();
	}
	/**
	 * probepossibilities
	 * This method probes possible reachable positions given a
	 * possible occupied position from the available actions for the player of the game.
	 * Facts of the form
	 * occupies(WhiteBishop2,c4)
	 * REACHABLE(WhiteBishop2_c4,d5)
	 * are created. - From position c4, the bishop can reach d5
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
			ChessPieceType pieceType = piece.getChessType();
			heldPosition = piece.getMyPosition();
			if (heldPosition == null) {
				heldPosition = piece.getHeldPosition();
			}
			String occupiesNow = piece.returnPredicate();
			String piecenameNow = piece.getMyPiece().getOntlogyName();
			String posnameNow = heldPosition.getPositionName();
			localKb.createfacts(occupiesNow, posnameNow, piecenameNow); //Must also set current occupied position
			List<Position> availablePositions = piece.getNewlistPositions();
			List<Position> actionAvailablePositions = new ArrayList();
			actionAvailablePositions.addAll(availablePositions);
			List<Position> actionRemoved = new ArrayList();
			actionRemoved.addAll(piece.getRemovedPositions());
			for (Position pos:actionAvailablePositions) {
				String posName = pos.getPositionName();
				Position rpos =  (Position) actionRemoved.stream().filter(c -> c.getPositionName().contains(posName)).findAny().orElse(null);
				if (rpos == null) {
	
//					piece.setMyPosition(pos);// This causes the moved piece to appear in two places !!!
					piece.settempMyposition(pos);
					piece.produceLegalmoves(pos); // Produces new reachable positions
					piece.giveNewdirections(); // In case of bishop or queen or rook
					HashMap<String,Position> reachablePositions = piece.getReacablePositions();
					if (pieceType instanceof AQueen && piece.checkWhite() ) {
						writer.println("Checking reachable for white queen\n"+piece.toString());
					}
					ChessActionImpl tempaction = new ChessActionImpl(reachablePositions,piece,player,myPlayer); // Creates new removed positions
//					player.calculatePreferredPosition(piece, tempaction); // MUst use a new action

					String occupies = piece.returnPredicate();
					String piecename = piece.getMyPiece().getOntlogyName();
					String posname = pos.getPositionName();
					localKb.createfacts(occupies, posname, piecename);
					piecename = piecename+"_"+posname; //OBS: Separate piece and position !!!
					tellnewFacts(piece,piecename);
					if (pieceType instanceof APawn) {
						HashMap<String,Position> attackPositions = piece.getAttackPositions();
						List<Position> attackedPositions = new ArrayList(attackPositions.values());
						for (Position attackpos:attackedPositions) {
							String aposName = attackpos.getPositionName();
							localKb.createfacts(PAWNATTACK, aposName, piecename);
						}
					}
				}
			}
//			piece.setMyPosition(heldPosition);
			piece.settempMyposition(heldPosition);
			piece.produceLegalmoves(heldPosition);
			piece.giveNewdirections();
			player.calculatePreferredPosition(piece, localAction);
	
		}
		localKb.writeKnowledgebase();
	}
	/**
	 * tellnewFacts
	 * This method creates new REACHABLE facts to the strategy knowledge base
	 * It also checks to see if the new reachable position is protected.
	 * It is called from the probepossiblities method
	 * @param piece
	 * @param name name is of type piecename + _ + posname
	 */
	public void tellnewFacts(AgamePiece piece,String name) {
		List<Position> availablePositions = piece.getNewlistPositions();
		String piecename = piece.getMyPiece().getOntlogyName();
/*		if (piecename.equals("WhiteRook2")) {
			writer.println("TellnewFacts from position "+name);
			writer.println(piece.toString());
		}*/
		for (Position pos:availablePositions) {
			if (!piece.checkRemoved(pos)|| piece.checkFriendlyPosition(pos)) {
//				String piecename = piece.getMyPiece().getOntlogyName();
				String posname = pos.getPositionName();
				possiblePositions.put(name+posname, pos);
				positionKeys.add(name+posname);
				localKb.createfacts(REACHABLE, posname, name);
				boolean protectedpiece = false;
				protectedpiece = folKb.checkmyProtection(piecename,posname,PROTECTED,opponent); // Is the new position protected then add it to protected positions
				if (protectedpiece) {
					protectedPositions.put(name+posname, pos);
					writer.println("Protected position\n"+name+posname+"\n"+pos.toString());
				}
			}
		}
	}
	public void writeFacts() {
		localKb.writeKnowledgebase();
	}
}
