package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aima.core.logic.fol.domain.FOLDomain;
import aima.core.logic.fol.inference.InferenceResult;
import aima.core.logic.fol.kb.FOLKnowledgeBase;
import aima.core.logic.fol.parsing.ast.Constant;
import aima.core.logic.fol.parsing.ast.Predicate;
import aima.core.logic.fol.parsing.ast.Term;
import aima.core.logic.planning.State;
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
 * Based on the knowledge from the strategy knowledge base, the agent creates and maintains a Performance measure.
 * The opponent agent object must be able to return with a best strategy for moves 
 * @author olj
 *
 */
/**
 * @author bruker
 *
 */
public class OpponentAgent {

	private ChessStateImpl stateImpl = null;
	private ChessActionImpl localAction = null;
	private List <ChessAction> actions = null; // All actions available to the opponent of the game
	private List <ChessActionImpl> playeractions = null; // all actions available to player
	private String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\opponent.txt";
	private PrintWriter writer = null;
	private FileWriter fw = null;
	private PlayGame game = null;
	private APlayer myPlayer = null; // This is the opponent of the game
	private APlayer opponent = null; // This is the chess player of the game
	private ChessFolKnowledgeBase folKb = null; // The parent knowledge base
	private ChessDomain chessDomain;
	private FOLGamesFCAsk forwardChain;
	private FOLGamesBCAsk backwardChain;
	private ChessFolKnowledgeBase localKb; // The local strategy knowledge base
	private APerformance performanceMeasure;
	private Map<String,Position>protectedPositions = null; // Contains the set of positions protected by the player of the game
	// These positions can be occupied by opponent pieces, be vacant or occupied by a friendly piece.
	// Key: A piece x is protected at pos y when it moves from pos z to y. ex: WhiteBishop1_d2e1. This means that the
	// Bishop is protected by another player's piece if it moves from d2 to e1
	// With the key: BlackBishop2_a3c1, then this opponent Bishop is threatened by another player's piece if it moves from 
	// a3 to c1
	// This map is filled in the tellnewfacts method
	private Map<String,Position>possiblePositions = null; // Contains the set of possible reachable positions in the strategy KB
	// The key for these two maps is of the form: WhiteBishop2_c4d5
	private HashMap<String,Position> positions; // The original HashMap of positions
	private List<String>positionKeys = null; // contains position keys of the form: WhiteBishop2_c4d5:
/*
 * From position c4 the white bishop can reach d5	
 */
	private String opponentKingPosition = null;
	private List<String>myPieceNames = null;
    private Map<String,State>initStates = null; // Contains all initial states for current move
	private Map<String,State>goalStates = null; // Contains all goal states for current move

	
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
	private String PIECE = "PIECE"; 
	private String PAWNMOVE;
	private String playerName = "";
    private String OCCUPIES = "";
    private String PAWNATTACK ="";
    private String BOARD;
    private String PLAYER;
    private String CASTLE;
    private String OPPONENTTO;
    private String POSSIBLETHREAT;
    private String POSSIBLEPROTECT; // All available positions for a piece are possibly protected by that piece
    private String POSSIBLEREACH; // All available positions for a piece are possibly reachable by that piece

    
	public OpponentAgent(ChessStateImpl stateImpl, PlayGame game, APlayer myPlayer, APlayer opponent,ChessFolKnowledgeBase folKb,ChessDomain chessDomain) {
		super();
		this.stateImpl = stateImpl;
		this.game = game;
		this.myPlayer = myPlayer; // myPlayer is the opponent
		this.opponent = opponent; // The opponent is the player of the game
		this.folKb = folKb;
		this.chessDomain = chessDomain;
		forwardChain = new FOLGamesFCAsk(); // A Forward Chain inference procedure see p. 332
		backwardChain = new FOLGamesBCAsk(); // A backward Chain inference procedure see p. 337
		localKb = new ChessFolKnowledgeBase(chessDomain, forwardChain);
		localKb.setBackWardChain(backwardChain);
		actions = this.stateImpl.getActions(myPlayer); // These are the opponent's actions
		this.myPlayer.setActions(actions);
		possiblePositions = new HashMap<String,Position>(); // Which positions are reachable
		protectedPositions = new HashMap<String,Position>(); //Which positions are protected by opponent
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
			PIECE = KnowledgeBuilder.getPIECE();
			OCCUPIES = KnowledgeBuilder.getOCCUPIES();
			PAWNMOVE = KnowledgeBuilder.getPAWNMOVE();
			PAWNATTACK = KnowledgeBuilder.getPAWNATTACK();
			BOARD = KnowledgeBuilder.getBOARD();
			PLAYER = KnowledgeBuilder.getPLAYER();
			CASTLE = KnowledgeBuilder.getCASTLE();
			OPPONENTTO = KnowledgeBuilder.getOPPONENTTO();
			POSSIBLETHREAT = KnowledgeBuilder.getPOSSIBLETHREAT();
			POSSIBLEPROTECT = KnowledgeBuilder.getPOSSIBLEPROTECT();
			POSSIBLEREACH = KnowledgeBuilder.getPOSSIBLEREACH();
	  }

	public ChessFolKnowledgeBase getLocalKb() {
		return localKb;
	}
	public void setLocalKb(ChessFolKnowledgeBase localKb) {
		this.localKb = localKb;
	}
	public Map<String, State> getInitStates() {
		return initStates;
	}
	public void setInitStates(Map<String, State> initStates) {
		this.initStates = initStates;
		performanceMeasure.setInitStates(initStates);
	}
	public Map<String, State> getGoalStates() {
		return goalStates;
	}
	public void setGoalStates(Map<String, State> goalStates) {
		this.goalStates = goalStates;
		performanceMeasure.setGoalStates(goalStates);
	}
	public List<ChessActionImpl> getPlayeractions() {
		return playeractions;
	}
	public void setPlayeractions(List<ChessActionImpl> playeractions) {
		this.playeractions = playeractions;
	    performanceMeasure.setPlayeractions(playeractions);
	}
	public String getPAWN() {
		return PAWN;
	}
	public void setPAWN(String pAWN) {
		PAWN = pAWN;
	}
	public String getKNIGHT() {
		return KNIGHT;
	}
	public void setKNIGHT(String kNIGHT) {
		KNIGHT = kNIGHT;
	}
	public String getBISHOP() {
		return BISHOP;
	}
	public void setBISHOP(String bISHOP) {
		BISHOP = bISHOP;
	}
	public String getROOK() {
		return ROOK;
	}
	public void setROOK(String rOOK) {
		ROOK = rOOK;
	}
	public String getKING() {
		return KING;
	}
	public void setKING(String kING) {
		KING = kING;
	}
	public String getQUEEN() {
		return QUEEN;
	}
	public void setQUEEN(String qUEEN) {
		QUEEN = qUEEN;
	}
	public String getOpponentKingPosition() {
		return opponentKingPosition;
	}
	/**
	 * setOpponentKingPosition
	 * This method sets the position of the opponent king and 
	 * creates fact of the type PAWN(x,kingPosition) to the strategy KB
	 * - A pawn can reach the king position from position x
	 * This is done by the call to the findKingsReachable method of the performanceMeasure object
	 * It is called from the ChessProblemSolver
	 * @param opponentKingPosition
	 */
	public void setOpponentKingPosition(String opponentKingPosition) {
		this.opponentKingPosition = opponentKingPosition;
		performanceMeasure.setOpponentKingPosition(opponentKingPosition);
		performanceMeasure.findKingsReachable();
	}
	public APerformance getPerformanceMeasure() {
		return performanceMeasure;
	}
	public void setPerformanceMeasure(APerformance performanceMeasure) {
		this.performanceMeasure = performanceMeasure;
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
		performanceMeasure.setPositions(positions);
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
	
	public String getPAWNATTACK() {
		return PAWNATTACK;
	}
	public void setPAWNATTACK(String pAWNATTACK) {
		PAWNATTACK = pAWNATTACK;
	}
	/**
	 * defineFacts
	 * This method creates facts about the opponent pieces to the local strategy knowledge base
	 * This is done by calling the probePossibilities method
	 * These facts are: which positions they occupy and where they can move to.
	 * It is called when the opponent agent is created.                           
	 */
	public void defineFacts() {
		Position heldPosition = null;
		for (AgamePiece piece:myPlayer.getMygamePieces()) { // myPlayer is here the opponent of the game
			if (piece.isActive()){
				heldPosition = piece.getMyPosition();
				if (heldPosition == null) {
					heldPosition = piece.getHeldPosition();
				}
				String occupies = piece.returnPredicate();
				String pieceName = piece.getMyPiece().getOntlogyName();
				String posName = heldPosition.getPositionName();
				localKb.createfacts(occupies, posName, pieceName); // localKB is the strategy KB
				myPieceNames.add(pieceName);
			}
		}
		List<ChessActionImpl> myActions = new ArrayList<ChessActionImpl>();
		for (ChessAction action:actions) {
			ChessActionImpl localAction = (ChessActionImpl) action;
			myActions.add(localAction);
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

//		myActions.addAll((Collection<? extends ChessActionImpl>) actions);
		probepossibilities(myActions, myPlayer);
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
		folKb.tell(folPredicate); // parent KB
		localKb.tell(folPredicate); // strategy KB
//		writer.println("Opponent piece "+piece+"\ncan move to "+pos);
	}
	/**
	 * findPiece
	 * This method returns a list of piece names.
	 * These pieces can reach a certain position from the strategy KB
	 * It is called from the AChessProblem object to find pieces that can reach the opponent king position
	 * @param pos The position to reach
	 * @param fact The fact REACHABLE
	 * @return
	 */
	public List<String> findPiece(String pos,String fact) {
		List<String> pieces = localKb.findFacts(pos, fact);
		writer.println("The following pieces can reach "+pos);
		if (pieces != null && !pieces.isEmpty()) {
			for (String name:pieces) {
				writer.println("Piece "+name);
			}
		}
		 writer.flush();
		return pieces;
	}
	/**
	 * probeConsequences
	 * This action examines consequences a player's action may have.  
	 * AT PRESENT NOT USED                                                                                  
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
	 * It is called from the ChessProblemSolver
	 * @param actions The actions available to player
	 */
	public void chooseStrategy(List<ChessActionImpl>actions) {
		writer.println("Choose strategy");
		performanceMeasure.setPositions(positions);
		performanceMeasure.setPositionKeys(positionKeys); // contains position keys of the form: WhiteBishop2_c4d5:
		performanceMeasure.occupiedPositions(); // Finds positions occupied by the opponent's pieces and player's pieces
		performanceMeasure.findReachable(); //runs through all positions occupied by opponent pieces to see
//		 * if any of the player's pieces can reach these positions and safely take the opponent piece.
		performanceMeasure.simpleSearch(); // Builds a map of movable pieces, protector pieces and need protection pieces.
		List<AgamePiece> opponentPieces = myPlayer.getMygamePieces();
		Map<String,Position> opponentPositions = performanceMeasure.getOccupiedPositions();
		for (ChessAction action:actions) { // For all actions available to player
			ChessActionImpl localAction = (ChessActionImpl) action;
			ApieceMove move = localAction.getPossibleMove();
			AgamePiece piece = localAction.getChessPiece();
			ChessPieceType pieceType = piece.getChessType();
//			Map<String,Position> attackPositions = null;
			boolean pawn = pieceType instanceof APawn;
			String pawnPos1 = null;
			String pawnPos2 = null;
			List<Position> attackpos = null;
/*
 * OBS attack position must be recalculated from their possible positions !!!			
 */

			String pieceName = piece.getMyPiece().getOntlogyName();
			String sep = "_";
			if (move != null) {
				List<Position> available = localAction.getAvailablePositions();
				List<Position> removed = localAction.getPositionRemoved();
				for (Position apos: available) {
					String posName = apos.getPositionName();
					Position pos =  (Position) removed.stream().filter(c -> c.getPositionName().contains(posName)).findAny().orElse(null);
					if (pawn && pos != null) {
						attackpos =	piece.getMyPawn().produceAttack(pos);
						pawnPos1 = attackpos.get(0).getPositionName();
						pawnPos2 = attackpos.get(1).getPositionName();
					}
					if(!piece.checkRemoved(apos)|| piece.checkFriendlyPosition(pos)) {
//						String posA = apos.getPositionName();
//						boolean posOccupies = checkFacts(pieceName,posName,OCCUPIES);
// Here we must find which opponent pieces are at these reachable positions OLJ 24.10.22 !!
// We must also check if the piece is a pawn
						for (String key: positionKeys) {
							if (key.contains(pieceName+sep+posName)) {
								Position possiblepos = possiblePositions.get(key);
								if (possiblepos != null) {
									String posA = possiblepos.getPositionName();
//									boolean reachpos = checkReachable(pieceName, posA, key);
									writer.println("Checking reachable for key "+key+" from occupied position "+posName+" can then reach position "+posA);
									for (AgamePiece opponentPiece:opponentPieces) {
										String opponentName = opponentPiece.getMyPiece().getOntlogyName();
										boolean oppactive = opponentPiece.isActive();
										Position opponentPos = opponentPositions.get(opponentName);
										int posIndex = key.indexOf(sep);
										if (opponentPos != null && oppactive && opponentPos.getPositionName().equals(posA) && !pawn ) {
											if (posIndex > 0) {
												String catcherPieceName = key.substring(0, posIndex);
												writer.println("Piece "+catcherPieceName+" can take "+opponentName+" at "+posA+ " from "+posName);
											}
										} // Must be checked!!!! attackPosition must correspond to the position of the opponent piece
										if (pawn && attackpos != null && oppactive && (opponentPos.getPositionName().equals(pawnPos1)||opponentPos.getPositionName().equals(pawnPos2))) {
											if (posIndex > 0) {
												String catcherPieceName = key.substring(0, posIndex);
												writer.println("The pawn piece "+catcherPieceName+" can take "+opponentName+" at "+pawnPos1+ " or "+pawnPos2+" from "+posName);
											}
										}
										
									}
								}
							}
						}
  					}
				}
			}
		}
/*		for (String key: positionKeys) {
			Position pos = possiblePositions.get(key);
			if (pos != null) {
				writer.println(key);
				writer.println(pos.toString());
			}
			
		}*/
//	    writer.flush();
	}
	/**
	 * probepossibilities
	 * This method probes possible reachable positions given a
	 * possible occupied position from the available actions for the player of the game.
	 * Facts of the form
	 * occupies(WhiteBishop2,c4)
	 * REACHABLE(WhiteBishop2_c4,d5)
	 * are created in the strategy KB. - From position c4, the bishop can reach d5.
	 * The strategy KB represents possible moves one ply down.
	 * It is called from the ChessProblemSolver prepareAction method
	 * It is also called from the defineFact method when the opponent agent is created,
	 * to create facts about the opponent pieces to the strategy knowledge base.
	 * @param actions - The actions available to player
	 * @param player - The player of the game when called from the chessproblemsolver. The opponent when called from the definefact method
	 */
	public void probepossibilities(List<ChessActionImpl>actions,APlayer player) {
		Position heldPosition = null;
//		possiblePositions.clear(); These tables and maps are created for every new move
//		protectedPositions.clear();
//		positionKeys.clear();
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
			localKb.createfacts(occupiesNow, posnameNow, piecenameNow); //Must also set current occupied position. This is the strategy knowledge base
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
/*					if (pieceType instanceof AQueen && piece.checkWhite() ) {
						writer.println("Checking reachable for white queen\n"+piece.toString());
					}*/
					ChessActionImpl tempaction = new ChessActionImpl(reachablePositions,piece,player,myPlayer); // Creates new removed positions
//					player.calculatePreferredPosition(piece, tempaction); // Must use a new action

					String occupies = piece.returnPredicate();
					String piecename = piece.getMyPiece().getOntlogyName();
					String posname = pos.getPositionName();
					localKb.createfacts(occupies, posname, piecename);
					piecename = piecename+"_"+posname; //OBS: Separate piece and position !!!
					tellnewFacts(piece,piecename,posnameNow);
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
	 * This method creates new REACHABLE facts to the strategy knowledge base.
	 * This is done by calling the strategy knowledge base createfacts method
	 * It also checks to see if the new reachable position is protected.
	 * It is called from the probepossiblities method
	 * @since 18.01.23 Added possible reach facts
	 * @param piece
	 * @param name name is of type piecename + _ + posname
	 * @param posnameNow The name of the position the piece is occupying now
	 */
	public void tellnewFacts(AgamePiece piece,String name,String posnameNow) {
		List<Position> availablePositions = piece.getNewlistPositions();
		String piecename = piece.getMyPiece().getOntlogyName();
		ChessPieceType pieceType = piece.getChessType();
		for (Position pos:availablePositions) {
			String posname = pos.getPositionName();
			localKb.createfacts(POSSIBLEREACH, posname, name);
			writer.println("Checking available position for  "+piecename+ " and "+posname + " with the given name "+name+" From occupied position "+posnameNow);
			if (piece.checkFriendlyPosition(pos)) {
				writer.println("Position "+posname+" is occupied by friendly piece");
			}
			if (!piece.checkRemoved(pos)|| piece.checkFriendlyPosition(pos)) {
//				String piecename = piece.getMyPiece().getOntlogyName();
				possiblePositions.put(name+posname, pos);
//				writer.println("Possible position\n"+name+posname);
				positionKeys.add(name+posname);
				localKb.createfacts(REACHABLE, posname, name);
				boolean protectedpiece = false;
				boolean pawnprotect = false;
				String protectPiece = PROTECTED;
				String protectText = "Protected position\n";
				if ((pieceType instanceof APawn)) {
					List<Position> attackpos = new ArrayList<Position>(piece.getAttackPositions().values());
					protectPiece = PAWNATTACK;
					protectText = "Protected position by pawn\n";
					for (Position apos:attackpos) {
						String aposname = apos.getPositionName();
						writer.println("Checking pawn attack position for  "+piecename+ " and "+aposname + " with the given name "+name+" From occupied position "+posnameNow);
						pawnprotect = folKb.checkmyProtection(piecename,aposname,protectPiece,opponent);
						if (pawnprotect) {
							protectedPositions.put(name+aposname, apos); // Key: A piece x is protected at pos y when it moves from pos z to y. !!
							writer.println(protectText+name+aposname);
						}
					}
				}
				if (!(pieceType instanceof APawn)) {
					protectedpiece = folKb.checkmyProtection(piecename,posname,protectPiece,opponent); // Is the new position protected by player with a different piece then add it to protected positions
					if (protectedpiece) {
						protectedPositions.put(name+posname, pos); // Key: A piece x is protected at pos y when it moves from pos z to y. !!
						writer.println(protectText+name+posname);
					}
					if (!protectedpiece) { // Added 3.02.23 Position is also protected with possiblereach
						protectedpiece = folKb.checkmyProtection(piecename,posname,POSSIBLEREACH,opponent);
						if (protectedpiece) {
							protectText = protectText + "by possible reach ";
							protectedPositions.put(name+posname, pos); // Key: A piece x is protected at pos y when it moves from pos z to y. !!
							writer.println(protectText+name+posname);
						}
					}
				}
			}
		}
	}
	public void writeFacts() {
		localKb.writeKnowledgebase();
	}
}
