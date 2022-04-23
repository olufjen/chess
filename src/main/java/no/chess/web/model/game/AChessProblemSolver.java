
package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import aima.core.logic.fol.Connectors;
import aima.core.logic.fol.domain.FOLDomain;
import aima.core.logic.fol.inference.FOLFCAsk;
import aima.core.logic.fol.inference.InferenceProcedure;
import aima.core.logic.fol.inference.InferenceResult;
import aima.core.logic.fol.inference.InferenceResultPrinter;
import aima.core.logic.fol.kb.FOLKnowledgeBase;
import aima.core.logic.fol.kb.data.Literal;
import aima.core.logic.fol.parsing.ast.AtomicSentence;
import aima.core.logic.fol.parsing.ast.ConnectedSentence;
import aima.core.logic.fol.parsing.ast.Constant;
import aima.core.logic.fol.parsing.ast.NotSentence;
import aima.core.logic.fol.parsing.ast.Predicate;
import aima.core.logic.fol.parsing.ast.QuantifiedSentence;
import aima.core.logic.fol.parsing.ast.Sentence;
import aima.core.logic.fol.parsing.ast.Term;
import aima.core.logic.fol.parsing.ast.Variable;
import aima.core.logic.planning.ActionSchema;
import aima.core.logic.planning.GraphPlanAlgorithm;
import aima.core.logic.planning.Problem;
import aima.core.logic.planning.State;
import aima.core.logic.propositional.parsing.ast.ComplexSentence;
import aima.core.logic.propositional.parsing.ast.Connective;
import no.chess.web.model.PlayGame;
import no.chess.web.model.Position;
import no.games.chess.ChessPieceType;
import no.games.chess.AbstractGamePiece.pieceType;
import no.games.chess.fol.BCGamesAskHandler;
import no.games.chess.fol.FOLGamesBCAsk;
import no.games.chess.fol.FOLGamesFCAsk;
import no.games.chess.planning.ChessProblem;
/**
 * AChessProblemSolver
 * This class is used to find best moves in the chess game through planning as described in chapter 10 and 11 of 
 * the aima book.
 * The ProblemSolver is created in the ChessAgent's execute method.
 * In search for the best move, the ChessProblemSolver creates an opponent Agent object and a performance measure object.
 * The problem solver creates a ChessProblem object for the ChessSearchAlgorithm to solve.
 * The ChessProblem object contains one or more ActionSchemas. There is one ActionSchema for every ChessAction available for the player
 * Some definitions:
 * The planning phase: Actions are created and selected with some ordering constraints.
 * The scheduling phase: Temporal information is added to ensure that the plan meets the goal: A winning game.
 * (See chapter 11 p. 401) 
 * @author oluf
 * 
 */
public class AChessProblemSolver {
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
  private String playerName =  "";
  private String OCCUPIES = "";
  private String PAWNATTACK ="";
  private String playSide;
  private String BOARD;
  private String PLAYER;
  private String CASTLE;
  private String OPPONENTTO;
  private String POSSIBLETHREAT;
  /**
   *  The type of piece under consideration
   */
  private String typeofPiece;

  /**
   *  The name of the action schema - pawnmove.bishopmove ...
   */
  private String moveName;

  private String outputFileName =  "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\problem.txt";
  private ChessStateImpl stateImpl =  null;
  private ChessActionImpl localAction =  null;
  private ChessKnowledgeBase kb =  null;
  private int noofMoves =  0;
  private List<Position> positionList = null; // The original HashMap of positions as a list
  /**
   * 
   * A first order knowledge base
   * 
   */
  private ChessFolKnowledgeBase folKb;

  /**
   * 
   * ChessDomain:
   *  All pieces are constants
   *  all positions are constants
   * 
   */
  private FOLGamesFCAsk forwardChain;
  private FOLDomain chessDomain;
  
  private FOLGamesBCAsk backwardChain; // The backward chain inference procedure
  private PrintWriter writer =  null;
  private FileWriter fw =  null;
  private PlayGame game =  null;
  private APlayer myPlayer =  null;
  private APlayer opponent =  null;
  private State initialState =  null;
  private State goalState =  null;
  private State deferredInitial = null;
  private State deferredGoal = null;
  private Map<String,State>deferredGoalstates = null;
  private String deferredKey = null;
  private GraphPlanAlgorithm graphPlan =  null;
  private Map<String,ActionSchema> actionSchemas = null;
  private Map<String,State>initStates = null;
  private Map<String,State>goalStates = null;
  private Map<String,AgamePiece>possiblePieces = null; // Contains opponent pieces that can be taken
  private Map<String,Position>possiblePositions = null; // Contains the positions of these opponent pieces.
  private Map<String,AgamePiece>threatenedPieces = null; // Contains pieces that are threatened by the opponent
  private Map<String,Position>threadenedPositions = null; // Contains the positions of these pieces.
  private Map<String,ArrayList<AgamePiece>>protectors = null; // Contains pieces that protect other pieces. The key is the name of the piece that they protect.
  private Map<String,ArrayList<AgamePiece>>attackers = null; // Contains opponent pieces that can capture a piece. The key is the name of the piece that they can capture.
  private HashMap<String,Position> positions; // The original HashMap of positions
  private AgamePiece opponentCatch = null;
  private Position opponentcatchPosition = null;
  private List <ChessActionImpl> actions = null;
  private ChessActionImpl castleAction = null;

  private OpponentAgent opponentAgent = null;
  private AgamePiece chosenPiece = null;
  private AgamePiece opponentKing = null;
  private String opponentKingPosition = null;
  
  public AChessProblemSolver(ChessStateImpl stateImpl, ChessActionImpl localAction, ChessFolKnowledgeBase folKb, FOLDomain chessDomain, FOLGamesFCAsk forwardChain, FOLGamesBCAsk backwardChain, PlayGame game, APlayer myPlayer, APlayer opponent) {
		super();
		this.stateImpl = stateImpl;
		this.localAction = localAction;
		this.folKb = folKb;
		this.chessDomain = chessDomain;
		this.forwardChain = forwardChain;
		this.backwardChain = backwardChain;
		this.game = game;
		this.myPlayer = myPlayer;
		this.opponent = opponent;
		positions = this.game.getPositions();
		opponentAgent = new OpponentAgent(this.stateImpl,this.game,this.opponent,this.myPlayer,this.folKb,chessDomain);
		opponentAgent.setPositions(positions);
		playerName = this.myPlayer.getNameOfplayer();
		playSide = playerName.substring(0,5);
		noofMoves = game.getMovements().size();
		graphPlan = new GraphPlanAlgorithm();
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
	    setPredicatenames();
	    actionSchemas = new HashMap<String,ActionSchema>();
	    initStates = new HashMap<String,State>(); 
	    goalStates = new HashMap<String,State>();
	    possiblePieces = new HashMap<String,AgamePiece>();
	    possiblePositions = new HashMap<String,Position>();
	    threatenedPieces = new HashMap<String,AgamePiece>();
	    threadenedPositions = new HashMap<String,Position>();
	    protectors = new HashMap<String,ArrayList<AgamePiece>>();
	    attackers = new HashMap<String,ArrayList<AgamePiece>>();
	    deferredGoalstates = new HashMap<String,State>();
	    deferredGoal = game.getDeferredGoal();
	    if (deferredGoal == null) {
	    	  deferredGoalstates = new HashMap<String,State>();
	    }else {
	    	deferredGoalstates = game.getDeferredGoalstates();
	    	for (String key :deferredGoalstates.keySet()) {
	    		deferredKey = key;
	    	}
	    }
	    findOpponentKing();
  }
  /**
 * findOpponentKing
 * This method finds the opponent king
 */
public void findOpponentKing() {
	  String firstKing = "WhiteKing";
	  String lastKing = "BlackKing";
	  List<AgamePiece> opponentpieces = opponent.getMygamePieces();
	  AgamePiece opponentKing = (AgamePiece) opponentpieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(firstKing)).findAny().orElse(null);
	  if (opponentKing == null) {
		  opponentKing = (AgamePiece) opponentpieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(lastKing)).findAny().orElse(null);
	  }
	  if (opponentKing == null) {
		  writer.println("Opponent king not found");
	  }else {
		  String kingName = opponentKing.getMyPiece().getOntlogyName();
		  writer.println("Opponent king is "+kingName);
		  this.opponentKing = opponentKing;
	  }
	  
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
  

 public HashMap<String, Position> getPositions() {
	return positions;
}

public void setPositions(HashMap<String, Position> positions) {
	this.positions = positions;
}

public ChessActionImpl getCastleAction() {
	return castleAction;
}

public void setCastleAction(ChessActionImpl castleAction) {
	this.castleAction = castleAction;
}

public State getDeferredInitial() {
	return deferredInitial;
}

public void setDeferredInitial(State deferredInitial) {
	this.deferredInitial = deferredInitial;
}

public State getDeferredGoal() {
	return deferredGoal;
}

public void setDeferredGoal(State deferredGoal) {
	this.deferredGoal = deferredGoal;
}

public List<Position> getPositionList() {
	return positionList;
}

public void setPositionList(List<Position> positionList) {
	this.positionList = positionList;
}

public Map<String, ActionSchema> getActionSchemas() {
	return actionSchemas;
}

public void setActionSchemas(Map<String, ActionSchema> actionSchemas) {
	this.actionSchemas = actionSchemas;
}

public Map<String, State> getInitStates() {
	return initStates;
}

public void setInitStates(Map<String, State> initStates) {
	this.initStates = initStates;
}

public Map<String, State> getGoalStates() {
	return goalStates;
}

public void setGoalStates(Map<String, State> goalStates) {
	this.goalStates = goalStates;
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

  public ChessFolKnowledgeBase getFolKb() {
		return folKb;
  }

  public void setFolKb(ChessFolKnowledgeBase folKb) {
		this.folKb = folKb;
  }

  public FOLDomain getChessDomain() {
		return chessDomain;
  }

  public void setChessDomain(FOLDomain chessDomain) {
		this.chessDomain = chessDomain;
  }

  public FOLGamesFCAsk getForwardChain() {
		return forwardChain;
  }

  public void setForwardChain(FOLGamesFCAsk forwardChain) {
		this.forwardChain = forwardChain;
  }

  public FOLGamesBCAsk getBackwardChain() {
		return backwardChain;
  }

  public void setBackwardChain(FOLGamesBCAsk backwardChain) {
		this.backwardChain = backwardChain;
  }

  public no.chess.web.model.PlayGame getGame() {
		return game;
  }

  public void setGame(no.chess.web.model.PlayGame game) {
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
   * checkPossiblePieces
   * This method checks the two Maps possiblePieces and possiblePositions
   * to see if there are opponent pieces that can be taken
   * The two Maps are filled by the call to the checkOpponent method
   * It is called from the default part of the checkmovenumber method
 * @return
 * The name of the piece that can take this opponent piece
 * It only returns a name if the opponent piece has a value greater or equal to the value of the active piece
 */
public String checkPossiblePieces() {
	  if (!possiblePieces.isEmpty() && !possiblePositions.isEmpty()) {
		  List<AgamePiece> myPieces = myPlayer.getMygamePieces();
		  for (AgamePiece piece:myPieces) {
			  String name = piece.getMyPiece().getOntlogyName();
			  int value = piece.getMyPiece().getValue();
			  AgamePiece opponentPiece = possiblePieces.get(name);
			  if (opponentPiece != null) {
				  writer.println("Checking possible pieces "+" Found by "+name+"\n"+opponentPiece.toString());
				  int oppValue = opponentPiece.getMyPiece().getValue();
				  Position opponentPos = opponentPiece.getHeldPosition();
				  opponentCatch = opponentPiece;
				  if (opponentPos == null) {
					  opponentPos = opponentPiece.getMyPosition();
				  }
				  opponentcatchPosition = opponentPos;
				  if (oppValue >= value) {
					  writer.println("Returns with piece "+name);
					 return name; 
				  }
			  }
			 
		  }
	  }
	  return null;
  }
  
/**
 * checkoppoentThreat
 * This method checks if any of my pieces are threatened by the opponent player
 * The threatened pieces and their positions are placed in the hashmaps threatenedPieces and threatenedPositions
 * It is called from the prepareAction method
 * OBS!:: Must empty the hashmaps before using them again. It is done at the start of this method
 * The key used in the hashmaps is the name of the piece
 * @param fact
 * @param actions
 * @since 11.03.22 This method is reworked
 * The threatenedPieces threadenedPositions are filled if there are any threats
 */
public void checkoppoentThreat(String fact,ArrayList<ChessActionImpl> actions) {
	  List<AgamePiece> opponentpieces = opponent.getMygamePieces();
	  threatenedPieces.clear();	
	  threadenedPositions.clear();
	  protectors.clear();
	  attackers.clear();
	  AgamePiece opponentCatcher = null;
	  int opponentValue = 0;
	  String thisPiece = "";String thisPos = "";
	  List<AgamePiece> myPieces = myPlayer.getMygamePieces();
	  for (AgamePiece mypiece:myPieces) { // For all my pieces: is this piece under threat from any opponent piece?
		  if (mypiece.isActive()) {
			  Position myposition = mypiece.getHeldPosition();
			  String myposName = "";
			  if (myposition == null) {
				  myposition = mypiece.getmyPosition();
				  myposName = myposition.getPositionName();
			  }else {
				  myposName = myposition.getPositionName();
			  }
			  String myPieceName = mypiece.getMyPiece().getOntlogyName();
			  boolean threat = folKb.checkThreats("x", myposName, fact,opponent);
			  List<String> opponentNames = folKb.searchFacts("x", myposName,fact);
			  if(threat) {
				  ArrayList<AgamePiece> myCaptures = new ArrayList();
				  threatenedPieces.put(myPieceName, mypiece);
				  threadenedPositions.put(myPieceName, myposition);
				  for (String opponentPiece:opponentNames) {
						  AgamePiece capturePiece = (AgamePiece) opponentpieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(opponentPiece)).findAny().orElse(null);
						  if (capturePiece != null) {
							  myCaptures.add(capturePiece);
							  writer.println(myPieceName+ " can be captured by "+opponentPiece);
						  }
				  }
				  attackers.put(myPieceName, myCaptures);

			  }
		  }
	  }
	  if (!threatenedPieces.isEmpty()) {
		  ArrayList<AgamePiece> myProtectors = new ArrayList();
		  String name = null;
		  for (AgamePiece piece:myPieces) {// For all my pieces: is this piece under threat from any opponent piece?
			  name = piece.getMyPiece().getOntlogyName();
			  boolean protectedPiece = false;
			  String posName = null;
			  Position threatPos = null;
			  int piecevalue = piece.getValue();
			  if (threatenedPieces.containsKey(name)) {
				  ActionSchema movedAction = actionSchemas.get(name);
				  threatPos = threadenedPositions.get(name);
				  posName = threatPos.getPositionName();
				  writer.println("Piece under threat "+name+ " at "+posName);
				  boolean maybeProtected = folKb.checkmyProtection(name, posName, PROTECTED, myPlayer);
				  List<String> pieceNames = folKb.searchFacts("x", posName,PROTECTED);
				  if (maybeProtected) {
					  for (String protector:pieceNames) {
 						  AgamePiece protectorPiece = (AgamePiece) myPieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(protector)).findAny().orElse(null);
 						  if (protectorPiece != null) {
 							  writer.println(name+ " is protected by "+protector);
 							  protectedPiece = true;
 							  myProtectors.add(protectorPiece);
 						  }
					  }
					  protectors.put(name, myProtectors);
				  }
				  thisPiece = name;thisPos = posName;
			  }
			  ArrayList<AgamePiece> myCaptures = attackers.get(name);
			  if (myCaptures != null && !myCaptures.isEmpty()) {
  				Optional<AgamePiece> chosenopponent = myCaptures.stream().reduce((p1, p2) -> p1.getValue()<=p2.getValue() ? p1:p2);
  				if (chosenopponent.isPresent()) {
  					opponentCatcher = chosenopponent.get();
  					opponentValue = opponentCatcher.getValue();
  					writer.println("Found an opponent catcher with lowest value "+opponentCatcher.getMyPiece().getOntlogyName()+" value "+opponentValue);
  				}
			  }
			  if (piecevalue<opponentValue && !protectedPiece && posName != null) {
				  List<String>positionKeys = opponentAgent.getPerformanceMeasure().getPositionKeys(); // Positions that are reachable
//				  Map<String,Position> takenPositions = opponentAgent.getPerformanceMeasure().getTakenPositions();
//				  List<String> fromreachStrategy = opponentAgent.getPerformanceMeasure().getFromreachablePieces().get(posName);
//				  List<String> reachStrategy = opponentAgent.getPerformanceMeasure().getReachablePieces().get(posName);
				  writer.println("Trying to find a protector for "+posName);
				  for (String pieceKey:positionKeys) {// The key for positions that are reachable. The key is of the form: piecename_frompostopos
						int l = pieceKey.length();
						int index = l-2;
						String toPosname = pieceKey.substring(index);
						String fromposName = pieceKey.substring(l-4, l-2); // The position from which to reach a position
						Position toPos = positions.get(fromposName); // The map of all positions
						String pieceName = pieceKey.substring(0,l-5);
						if (toPosname.equals(posName)) { // We have found a possible protector for posName at toPosname
						AgamePiece protector =  (AgamePiece) myPieces.stream().filter(c -> c.getMyPiece().getOntlogyName().equals(pieceName)).findAny().orElse(null);
						if (protector != null && protector.isActive()) {
							chosenPiece = protector;
							writer.println("A protector "+pieceName+ " at "+fromposName+ " protects "+toPosname);
							ChessActionImpl naction =  (ChessActionImpl) actions.stream().filter(c -> c.getActionName().contains(pieceName)).findAny().orElse(null);
							if (naction != null && naction.getPossibleMove() != null) {
								naction.getPossibleMove().setToPosition(toPos); 
								naction.setPreferredPosition(toPos);
								return;
							}
						}else { // Must find a safe position for this piece or a protector
							writer.println("No protector for  "+thisPiece+ " at "+thisPos);
							List<Position> availablePositions = piece.getNewlistPositions();
							for (Position pos:availablePositions){
								String newPos = pos.getPositionName();
								if(!piece.checkRemoved(pos) ) {
									boolean threat = folKb.checkThreats("x", newPos, fact,opponent);
									if (!threat) {
										chosenPiece = piece;
										String xName = name;
										ChessActionImpl naction =  (ChessActionImpl) actions.stream().filter(c -> c.getActionName().contains(xName)).findAny().orElse(null);
										if (naction != null && naction.getPossibleMove() != null) {
											naction.getPossibleMove().setToPosition(pos); // OBS !! wrong position !!!
											naction.setPreferredPosition(pos);
											writer.println("Moves  "+thisPiece+ " to "+newPos);
											return;
										}
									}
								}
							}
						}
						}
				  }

			  }
			  if (piecevalue>opponentValue && posName != null) {
					writer.println("Piece value higher   "+thisPiece+ " at "+thisPos);
					List<Position> availablePositions = piece.getNewlistPositions();
					for (Position pos:availablePositions){
						String newPos = pos.getPositionName();
						if(!piece.checkRemoved(pos) ) {
							boolean threat = folKb.checkThreats("x", newPos, fact,opponent);
							if (!threat) {
								chosenPiece = piece;
								String xName = name;
								ChessActionImpl naction =  (ChessActionImpl) actions.stream().filter(c -> c.getActionName().contains(xName)).findAny().orElse(null);
								if (naction != null && naction.getPossibleMove() != null) {
									naction.getPossibleMove().setToPosition(pos); // OBS !! wrong position !!!
									naction.setPreferredPosition(pos);
									writer.println("Moves  "+thisPiece+ " to "+newPos);
									return;
								}
							}
						}
					}
			  }
		  }// END For all my pieces under threat
	  }
	  
}
  /**
   * checkOpponent
   * This method finds which opponent pieces the active player can safely take.
   * The pieces found are placed in a Map called possiblePieces, and its position is placed in a Map called possiblePositions
   * It is called from the checkMovenumber method
   * @since 15.03.21 Only pieces that are active are considered
 * @param fact
 * @param actions
 * @return
 */
public void checkOpponent(String fact,ArrayList<ChessActionImpl> actions) {
	  List<AgamePiece> pieces = opponent.getMygamePieces();
	  List<AgamePiece> myPieces = myPlayer.getMygamePieces();
	  for (AgamePiece piece:pieces) {
		  String posName = "";
		  Position position = piece.getHeldPosition();
		  if (position == null) {
			  writer.println("\nPosition from myposition\n"+piece.toString());
			  position = piece.getmyPosition();
			  posName = position.getPositionName();
		  }else {
			  posName = position.getPositionName();
			  writer.println("\nPosition from heldposition\n"+piece.toString());
		  }
		  if (piece.isActive()) {
			  for (AgamePiece mypiece:myPieces) { // For all my pieces: Can this piece reach the opponent's position?
				  String name = mypiece.getMyPiece().getOntlogyName();
				  pieceType type = mypiece.getPieceType();
				  boolean reachable = false;
				  boolean pawn = false;
				  boolean pieceProtected = false;
				  if (type  == type.PAWN) {
					  pawn = folKb.checkpieceFacts("y",name, posName, PAWNATTACK);
					  if (pawn) {
						  possiblePieces.put(name, piece);
						  possiblePositions.put(name, position);
					  }
				  }
				  if (type  != type.PAWN) {
					  reachable = folKb.checkpieceFacts("y",name,posName,REACHABLE);
					  if (reachable) {
						 pieceProtected = folKb.checkpieceFacts("x",name,posName,PROTECTED);
						  if (pieceProtected) {
							  possiblePieces.put(name, piece);
							  possiblePositions.put(name, position);
							  writer.println("Piece is protected and safe to take with : "+name+"\n"+piece.getMyPiece().getOntlogyName());
						  }
					  }
				  }
				  boolean threat = folKb.checkThreats("x", posName, THREATEN,opponent);
				  if (!threat && reachable && !pieceProtected) {
					  possiblePieces.put(name, piece);
					  possiblePositions.put(name, position);
					  writer.println("Piece is safe to take with : "+name+"\n"+piece.getMyPiece().getOntlogyName());
				  }

			  }
		  }

	  }
	
  }

  /**
   * prepareAction
   * This method attempts to analyze the chess state and prepare for next move.
   * It is called from the checkMovenumber method when the opening moves have been made.
   * From the default case of checkmovenumber
 * @param actions
 * @return
 */
public String prepareAction( ArrayList<ChessActionImpl> actions) {
	String pname = null;
	opponentAgent.probepossibilities(actions, myPlayer);
	opponentAgent.chooseStrategy(actions);
	checkoppoentThreat(THREATEN,actions); // fills the threatenedPieces and threatenedPositions if any 
	String pieceName = "WhiteBishop2";
	String fpos = "f1";
	String toPos = "d3";
	boolean bishop = folKb.checkpieceFacts("y",pieceName,fpos,OCCUPIES); // Rook occupies f1 !!??
	State goal = null;
	State initstate = null;
	if (bishop) {
		String name = pieceName; // Check to see if piecename has an action
		ChessActionImpl naction =  (ChessActionImpl) actions.stream().filter(c -> c.getActionName().contains(name)).findAny().orElse(null);
		if (naction != null) {
			AgamePiece piece = naction.getChessPiece();
			pieceType type = piece.getPieceType();
			determineType(type);
			goal = buildGoalstate(pieceName,toPos);
			List<Position> removed = piece.getRemovedPositions();
			Position pos =  (Position) removed.stream().filter(c -> c.getPositionName().contains(toPos)).findAny().orElse(null);
			if (pos != null) { // The bishop cannot be moved
				String pawnName = "WhitePawn5";
				String pawnPos = "e2";
				boolean pawn = folKb.checkpieceFacts("y",pawnName,pawnPos,OCCUPIES);
				if (pawn) {
					typeofPiece = PAWN;
					moveName = "pawnmove";
					initstate = buildInitialstate(pawnName, pawnPos);
					deferredInitial = initstate;
					deferredGoal = goal;
					deferredGoalstates.put(pieceName, goal);
					game.setDeferredGoal(deferredGoal);
					game.setDeferredInitial(deferredInitial);
					game.setDeferredGoalstates(deferredGoalstates);
					return pawnName;
				}
			}else { // The bishop can be moved
				checkCastling(actions);
				boolean threat = folKb.checkThreats("x", "c4", THREATEN,opponent);
//				boolean threat = true;
				if (threat) {
					  folKb.checkFacts(pieceName, "d3", REACHABLE, actions,positionList);
				}
				return pieceName;
			}
		}
	}
	if (chosenPiece != null) {
		return chosenPiece.getMyPiece().getOntlogyName();
	}
/*	if (pname != null) { // Has found a piece that is threatened. Then determine the best move
		String name = pname;
		ChessActionImpl naction =  (ChessActionImpl) actions.stream().filter(c -> c.getActionName().contains(name)).findAny().orElse(null);
		if (naction != null) { // If the threatened piece has an action. Then move it to a safe position
			ApieceMove move = naction.getPossibleMove();
			AgamePiece piece = naction.getChessPiece();
			List<Position> prefPos = piece.getPreferredPositions();
			Position tonewPos = move.getToPosition();
			String toPosname = tonewPos.getPositionName();
			boolean protectedpiece = false;
			protectedpiece = folKb.checkmyProtection(name,toPosname,PROTECTED,myPlayer); // Is the new position protected then move the piece
			if (protectedpiece) {
//				opponentAgent.probeConsequences(naction);
				opponentAgent.probepossibilities(actions, myPlayer);
				opponentAgent.chooseStrategy(actions);
//				opponentAgent.writeFacts();
				return pname;
			}
			else { // Never reaches this !!!
				List<Position> reachable = piece.getNewlistPositions();
				for (Position pos:reachable) {
					if (!piece.checkRemoved(pos)) {
						String posName = pos.getPositionName();
						protectedpiece = folKb.checkmyProtection(name,posName,PROTECTED,myPlayer); // must also set the new to position !!
						boolean threat = folKb.checkThreats(pname, posName, THREATEN,opponent);
						if (protectedpiece && !threat) {
							naction.getPossibleMove().setToPosition(pos);
							naction.setPreferredPosition(pos);
//							opponentAgent.probeConsequences(naction);
							opponentAgent.probepossibilities(actions, myPlayer);
							opponentAgent.chooseStrategy(actions);
							return pname;
						}
					}
				}
			}
		} // Never reaches this !!!
//		opponentAgent.probeConsequences(naction);
		opponentAgent.probepossibilities(actions, myPlayer);
		opponentAgent.chooseStrategy(actions);
		return pname; // The name of the piece that is threatened. Or the name of the piece that can protect it
	}
*/	// Find the best move and a protected position to move to.
	// For a possible strategy, see notes in compendium and notes on zenhub
	opponentAgent.probepossibilities(actions, myPlayer);
	opponentAgent.chooseStrategy(actions);
	writer.println("Prepareaction returns with no piece ");
	return null;
}
/**
 * determineType
 * This method determines and sets the ChessProblem piecetype
 * It is used when the initial and goal states are created
 * @param type
 */
public void determineType(pieceType type) {	 
	if (type == type.PAWN) {
		typeofPiece = PAWN;
		moveName = "pawnmove";
	}
	if (type == type.BISHOP) {
		typeofPiece = BISHOP;
		moveName = "bishopmove";
	}		
	if (type == type.ROOK) {
		typeofPiece = ROOK;
		moveName = "rookmove";
	}			
	if (type == type.KNIGHT) {
		typeofPiece = KNIGHT;
		moveName = "knoghtmove";
	}
	if (type == type.QUEEN) {
		typeofPiece = QUEEN;
		moveName = "queenmove";
	}
	if (type == type.KING) {
		typeofPiece = KING;
		moveName = "kingmove";
	}	
	
  }
/**
 * checkCastling
 * This method check if castling with the white king is possible
 * @param actions
 */
public void checkCastling(ArrayList<ChessActionImpl> actions) {
	String pieceName = "WhiteKing";
	String kingPos = "e1";
	State goal = null;
	State initstate = null;
	ChessActionImpl naction =  (ChessActionImpl) actions.stream().filter(c -> c.getActionName().contains(pieceName)).findAny().orElse(null);
	if (naction != null) {
		AgamePiece king = naction.getChessPiece();
		List<Position> removed = king.getRemovedPositions();
		String posName = "f1";
		Position pos =  (Position) removed.stream().filter(c -> c.getPositionName().contains(posName)).findAny().orElse(null);
		if (pos != null) {
			String bishopName = "WhiteBishop2";
			String fpos = "f1";
			String toPos = "d3";
			boolean bishop = folKb.checkpieceFacts("y",bishopName,fpos,OCCUPIES);
			if (bishop) {
				String castlePos = "g1";
				typeofPiece = KING;
				moveName = "kingmove";
				goal = buildGoalstate(pieceName,castlePos);
				typeofPiece = BISHOP;
				moveName = "bishopmove";
				initstate = buildInitialstate(bishopName, toPos);
				deferredInitial = initstate;
				deferredGoal = goal;
				deferredGoalstates.put(pieceName, goal);
				game.setDeferredGoalstates(deferredGoalstates);
				game.setDeferredGoal(deferredGoal);
				game.setDeferredInitial(deferredInitial);		
			}
		}
		
	}
}
  /**
 * buildGoalstate
 * This method builds a goal state based on a chosen piece name and a chosen position name
 * @param pieceName
 * @param toPos
 * @return
 */
public State buildGoalstate(String pieceName,String toPos) {
		List<Term> terms = new ArrayList<Term>();
		List<Term> typeTerms = new ArrayList<Term>();
		List<Term> boardTerms = new ArrayList<Term>();
		List<Term> playerTerms = new ArrayList<Term>();
		
		Constant pieceVar = new Constant(pieceName);
		Constant posVar = new Constant(toPos);
		Constant type = new Constant(typeofPiece);
		Constant ownerVar = new Constant(playerName);
		playerTerms.add(ownerVar);
		boardTerms.add(posVar);
		terms.add(pieceVar);
		terms.add(posVar);
		typeTerms.add(pieceVar);
		typeTerms.add(type);
		Predicate playerPredicate = new Predicate(PLAYER,playerTerms);
		Predicate boardPredicate = new Predicate(BOARD,boardTerms);
		Predicate typePredicate = new Predicate(PIECETYPE,typeTerms);
		Predicate posSentence = new Predicate(OCCUPIES,terms);
		List<Term> ownerterms = new ArrayList<Term>();
	
		ownerterms.add(ownerVar);
		ownerterms.add(pieceVar);
		Predicate ownerSentence = new Predicate(OWNER,ownerterms);
		List<Literal> literals = new ArrayList();
		Literal pos = new Literal((AtomicSentence) posSentence);
		Literal own = new Literal((AtomicSentence) ownerSentence);
		Literal types = new Literal((AtomicSentence)typePredicate);
		Literal boards = new Literal((AtomicSentence)boardPredicate);
		Literal player = new Literal((AtomicSentence)playerPredicate);
		
		literals.add(pos);
//		literals.add(own);
		literals.add(types);
//		literals.add(player);
		literals.add(boards);
		State gState = new State(literals);
		return gState;
	  
  }
  /**
   * checkMovenumber
   * This method determine the first moves based on the queen gambit process
   * After the first 4 moves, the default part of the case statement then determines the next move 
 * @param actions
 * @return
 */
public String checkMovenumber(ArrayList<ChessActionImpl> actions) {
	  String pieceName = "";
	  switch(noofMoves) {
	  case 0:
		  pieceName = "WhitePawn4";
		  break;
	  case 2:
		  pieceName = "WhitePawn3";
		  String pos = "c4";
		  folKb.checkFacts(pieceName, pos, REACHABLE, actions,positionList);
		  break;
	  case 4:
		  checkOpponent("", actions);
		  pieceName = "WhiteKnight1";
		  break;
	  case 6:
		  checkOpponent("", actions);
		  pieceName = "WhiteKnight2";
		  String posx = "f3";
		  folKb.checkFacts(pieceName, posx, REACHABLE, actions,positionList);
		  break;
	  default:
		  checkOpponent("", actions); // Result: A list of opponent pieces that can be taken
		  String blackpieceName = "BlackBishop1";
		  String blackpos = "g4";
		  if (folKb.checkThreats(blackpieceName, blackpos, OCCUPIES,opponent)) {
			  pieceName = "WhitePawn8";
			  break;
		  }
		  String pname = "x";
		  String bpos = "d5";
		  //			  pieceName = "WhitePawn3";
		  if (folKb.checkThreats(pname, bpos, OCCUPIES,opponent)) {  // OBS What happens if a friendly piece occupies this position !!!???
			  pieceName = "WhitePawn3";
			  String wpos = "d5";
			  if (folKb.checkFacts(pieceName, wpos, PAWNATTACK, actions,positionList))
				  break;
		  }
		  String kingName = opponentKing.getMyPiece().getOntlogyName();
		  String kingPos = folKb.checkPosition(kingName, OCCUPIES);
		  opponentKingPosition = kingPos;
		  writer.println("The opponent king is in "+kingPos);
		  pieceName = checkPossiblePieces(); // Checks which opponent pieces that be safely taken from the list of opponent pieces
		  if (pieceName != null) {
			  Position opponentPos = possiblePositions.get(pieceName);
			  if (opponentPos != null) {
				  String name = pieceName;
				  ChessActionImpl naction =  (ChessActionImpl) actions.stream().filter(c -> c.getActionName().contains(name)).findAny().orElse(null);
				  if (naction != null && naction.getPossibleMove() != null) {
					  naction.getPossibleMove().setToPosition(opponentPos);
					  naction.setPreferredPosition(opponentPos);
					  break;
				  }else {
					  writer.println("No action for "+pieceName);
				  }
			  }
		  }
		  writer.println("No pieces and positions ");
/*
 * Here we must find a safe move		  
 */
		  pieceName = prepareAction(actions);
		  if (pieceName == null) {
			  AgamePiece chosen = opponentAgent.getPerformanceMeasure().getChosenPiece();
			  Position chosenpos = opponentAgent.getPerformanceMeasure().getChosenPosition();
			  if (chosen != null && chosenpos != null) {
				  pieceName = chosen.getMyPiece().getOntlogyName();
				  String pchosenPosname = chosenpos.getPositionName();
				  folKb.checkFacts(pieceName, pchosenPosname, REACHABLE, actions,positionList);
				  writer.println("Chosen piece from Opponent agent "+pieceName);
			  }
			  if (pieceName == null) {
				  pieceName = "WhitePawn1";
				  writer.println("No chosen piece "+pieceName);
			  }
		  }
		  break;
	  }
	  return pieceName;
  }

/**
 * deferredMove
 * This method checks if any deferred move has been set.
 * If so, it returns the deferred key
 * This is a special case for castling
 * @param actions
 * @return
 */
public String deferredMove(ArrayList<ChessActionImpl> actions) {
	if (deferredKey == null) {
		return null;
	}
	String key = deferredKey;
	if (key.equals("WhiteKing")) {
		String posx = "g1";
		folKb.checkFacts(key, posx, CASTLE, actions,positionList);
		List<AgamePiece> pieces = myPlayer.getMygamePieces();
		AgamePiece movedPiece = (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(key)).findAny().orElse(null);
		HashMap<String,Position> castlePos = movedPiece.getCastlePositions();
		Position toCastle = castlePos.get(posx);
		if (movedPiece != null && toCastle != null) {	
			AgamePiece castle = myPlayer.checkCastling(movedPiece, toCastle); // Returns the castle piece to do castling with
			String castleName = castle.getMyPiece().getOntlogyName();
			ChessActionImpl naction =  (ChessActionImpl) actions.stream().filter(c -> c.getActionName().contains(castleName)).findAny().orElse(null);
			Position toCastlePos = castle.getCastlePositions().get("f1");
			if (toCastlePos != null && naction != null && naction.getPossibleMove() != null) {
				  naction.getPossibleMove().setToPosition(toCastlePos);
				  naction.setPreferredPosition(toCastlePos);
				  castleAction = naction;
			}
 	    	if (castle != null) {
   	    		castle.setCastlingMove(true);
   	    		movedPiece.setCastlingMove(true);
   	    		Position castlePosfrom = castle.getHeldPosition();
   	    		if (castlePosfrom == null)
   	    			castlePosfrom = castle.getMyPosition();
 	    	}
		}
		return deferredKey; // This create castling		
	}

	return null;
//	return deferredKey; This does not perform castling
}
  /**
 * planProblem
 * This is the main method of the ProblemSolver
 * This method creates and returns a High Level Action Problem given the available actions
 * Hierarchical Task networks and High Level Actions are described in  chapter 11.
 * At present, the High Level Problem contains one primitive action schema (chess action).
 * @param actions
 * @return a ChessProblem
 */
public ChessProblem planProblem(ArrayList<ChessActionImpl> actions) {
	  searchProblem(actions); // Builds an ActionSchema for every Chess Action. This is the planning phase
	  String pieceName = null;
	  ChessProblem problem = null;
	  String actionName = deferredMove(actions); // For castling
      pieceName = checkMovenumber(actions); // Returns a possible piecename - a piece to be moved - calls the prepareAction method
//      searchProblem(actions); // Builds an ActionSchema for every Chess Action. This is the planning phase
	  if (actionName != null && !pieceName.equals(actionName)) {
		  pieceName = actionName;
		  deferredKey = null;
		  deferredInitial = null;
		  deferredGoal = null;
		  deferredGoalstates.clear();
		  game.setDeferredGoalstates(deferredGoalstates);
		  game.setDeferredGoal(deferredGoal);
		  game.setDeferredInitial(deferredInitial);		
/*	  }else {
		 pieceName = checkMovenumber(actions);*/
	  }
	  ActionSchema movedAction = actionSchemas.get(pieceName);
	  if (movedAction != null) {
		  String nactionName = movedAction.getName();
		  if (deferredInitial != null && deferredGoal != null) {
			  writer.println("Deferred initial and goal states\n");
		      for (Literal literal :
		    	  deferredInitial.getFluents()) {
		    	 writer.println(literal.toString());
		      }
		      writer.println("Deferred goal state\n");
		      for (Literal literal :
		    	  deferredGoal.getFluents()) {
		    	 writer.println(literal.toString());
		      }		      
		  }
 /*
  * The initial and goal states are determined here
  * given the name of the piece.
 */

		  writer.println("Chosen action Schema\n"+movedAction.toString());
		  ChessActionImpl naction =  (ChessActionImpl) actions.stream().filter(c -> c.getActionName().equals(nactionName)).findAny().orElse(null);
		  String newPos = naction.getPossibleMove().getToPosition().getPositionName(); // Get newPos from Preferred position ??!!
		  String newprefPos = naction.getPreferredPosition().getPositionName(); 
		  writer.println("The new position and the preferred position\n"+newPos+"\n"+newprefPos);
		  State initState = initStates.get(pieceName);
		  State goal = goalStates.get(pieceName);
		  Set<ActionSchema> aSchemas =  new HashSet<ActionSchema>(actionSchemas.values());
//		  problem = new ChessProblem(initState,goal,movedAction);
		  problem = new ChessProblem(initState,goal,aSchemas);		  
		  writer.println("The fluents of the init state");
	      for (Literal literal :
	    	  initState.getFluents()) {
	    	 writer.println(literal.toString());
	      }
	      writer.println("The fluents of the goal state");
	      for (Literal literal :
	    	  goal.getFluents()) {
	    	 writer.println(literal.toString());
	      }      
		  List<Constant> problemConstants = problem.getProblemConstants();
		  writer.println("Problem constants - no of constants "+problemConstants.size());
		  for (Constant c:problemConstants) {
			  writer.println(c.toString());
		  }
		   List<ActionSchema> schemas =  problem.getGroundActions();
		   int s = schemas.size();
		   writer.println("No of permuted primitive actions from problem "+s);
		   for (ActionSchema primitiveAction :
			   schemas) {
			   writer.println(primitiveAction.toString());
		   }
		  writer.println("Chosen action\n"+naction.toString());
	  }

	  writer.flush();
	  return problem;
  }
  /**
 * searchProblem
 * For every available chessaction that contains a possible move and that is not blocked
 * create an actionschema
 * @Since 17.12.21
 * Preconditions and Effects are populated with Constants, the given piecename, posname and owner
 * @param actions
 * @return a List of ActionSchemas
 */
  public List<ActionSchema> searchProblem(ArrayList<ChessActionImpl> actions) {
	  List<ActionSchema> schemas = new ArrayList<ActionSchema>();
	  List<AgamePiece> pieces = myPlayer.getMygamePieces();
	  for (ChessActionImpl action:actions) {
			if (action.getPossibleMove()!= null && !action.isBlocked()) {
				determineParameters(action);
				String newPos = action.getPossibleMove().getToPosition().getPositionName(); // Get newPos from Preferred position ??!!
//				String newPos = action.getPreferredPosition().getPositionName(); !!!
				String pieceName = action.getChessPiece().getMyPiece().getOntlogyName();
				Position position = action.getChessPiece().getHeldPosition();
				if (position == null) {
					position = action.getChessPiece().getMyPosition();
				}
				String posName = position.getPositionName();
				String actionName = action.getActionName();
				State localinitialState = buildInitialstate(pieceName,posName);
				State localgoalState = buildGoalstate(action);
				initStates.put(pieceName, localinitialState);
				goalStates.put(pieceName, localgoalState);
				ArrayList<String>reachparentNames = (ArrayList<String>) folKb.searchFacts("x", newPos, PROTECTED);
//				Variable piece = new Variable("piece");
//				Variable pos = new Variable("pos");
//				Variable toPos = new Variable("topos");
				Constant piece = new Constant(pieceName);
				Constant pos = new Constant(posName);
				Constant toPos = new Constant(newPos);
				Constant type = new Constant(typeofPiece);
//				Variable ownerVar = new Variable("owner");
				Constant ownerVar = new Constant(playerName);
//				List variables = new ArrayList<Variable>(Arrays.asList(piece,pos,toPos));
				List variables = new ArrayList<Constant>(Arrays.asList(piece,pos,toPos));
				List<Term> terms = new ArrayList<Term>();
				List<Term> ownerterms = new ArrayList<Term>();
				List<Term> newterms = new ArrayList<Term>();
				List<Term> typeTerms = new ArrayList<Term>();
				
				List<Term> protectorTerms = new ArrayList();
				Predicate protectedBy = null;
				Constant protector = null;
				String protectorName = null;
				List<Literal> precondition = new ArrayList();
				List<Literal> effects = new ArrayList();
				if (reachparentNames != null && !reachparentNames.isEmpty() ) {
					int psize = reachparentNames.size();
					for (int i = 0;i<psize;i++) {
						protectorName = reachparentNames.get(i);
						String p = protectorName;
						AgamePiece gpiece =  (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(p)).findAny().orElse(null);
						if (gpiece != null &&!pieceName.equals(protectorName)) {
							protector = new Constant(protectorName);
//							Variable protector = new Variable("x"); //Cannot use Variable in preconditions and effects?
							protectorTerms.add(protector);
							protectorTerms.add(toPos);
							protectedBy = new Predicate(PROTECTED,protectorTerms);
							if (!typeofPiece.equals(PAWN)) {
								precondition.add(new Literal((AtomicSentence) protectedBy));
								variables.add(protector);
							}
							protectorTerms.clear();
						}
					}

				}
				ownerterms.add(ownerVar);
				ownerterms.add(piece);
				terms.add(piece);
				terms.add(pos);
				newterms.add(piece);
				newterms.add(toPos);
				typeTerms.add(piece);
				typeTerms.add(type);
				Predicate typePred = new Predicate(PIECETYPE,typeTerms);
				Predicate reachablePredicate = new Predicate(REACHABLE,newterms);
				Predicate pospred = new Predicate(OCCUPIES,terms);
				Predicate ownerPred = new Predicate(OWNER,ownerterms);
				Predicate newPospred = new Predicate(OCCUPIES,newterms);
				precondition.add(new Literal((AtomicSentence) pospred));
//				precondition.add(new Literal((AtomicSentence) ownerPred));
				precondition.add(new Literal((AtomicSentence) typePred));
//				Literal notAt = new Literal(pospred, true);
//				effects.add(notAt);
				effects.add(new Literal( (AtomicSentence)newPospred));
//				effects.add(new Literal( (AtomicSentence)ownerPred));
				effects.add(new Literal( (AtomicSentence)typePred));
				ActionSchema movedAction = new ActionSchema(actionName,variables,precondition,effects);
				actionSchemas.put(pieceName, movedAction);
				schemas.add(movedAction);
				
			}
	  }
	  return schemas;
  }

  /**
   * OBS: NOT USED
   * solveProblem
   * This method solves a Problem for a given ChessAction
   * using the Graphplan algorithm. (see p. 383 chapter 10.3)
 * @param action
 * @return
 */
public List<List<ActionSchema>> solveProblem(ChessActionImpl action) {
		determineParameters(action);
		Problem myProblem = buildProblem(action);
		if (myProblem != null) {
			List<List<ActionSchema>> solution = graphPlan.graphPlan(myProblem);
			for (List<ActionSchema> la:solution){
				for (ActionSchema as:la) {
					writer.println(as.toString());
				}
			}
			writer.flush();
			return solution;
		}

		writer.flush();
		return null;
  }

  /**
   * OBS: NOT USED
 * @param action
 * @return
 */
public Problem buildProblem(ChessActionImpl action) {
		String pieceName = action.getChessPiece().getMyPiece().getOntlogyName();
		AgamePiece apiece = action.getChessPiece();
		ChessPieceType thepieceType = apiece.getChessType();
//		if (thepieceType instanceof APawn) {
		String actionName = action.getActionName();
		Position position = action.getChessPiece().getHeldPosition();
		if (position == null) {
			position = action.getChessPiece().getMyPosition();
		}
		String posName = position.getPositionName();
		initialState = buildInitialstate(pieceName,posName);
		goalState = buildGoalstate(action);
		Variable piece = new Variable("piece");
		Variable pos = new Variable("pos");
		Variable toPos = new Variable("topos");
		Constant type = new Constant(typeofPiece);
//		Variable ownerVar = new Variable("owner");
		Constant ownerVar = new Constant(playerName);
		ArrayList variables = new ArrayList<Variable>(Arrays.asList(piece,pos,toPos));
		List<Term> terms = new ArrayList<Term>();
		List<Term> ownerterms = new ArrayList<Term>();
		List<Term> newterms = new ArrayList<Term>();
		List<Term> typeTerms = new ArrayList<Term>();
		ownerterms.add(ownerVar);
		ownerterms.add(piece);
		terms.add(piece);
		terms.add(pos);
		newterms.add(piece);
		newterms.add(toPos);
		typeTerms.add(piece);
		typeTerms.add(type);
		Predicate reachablePredicate = new Predicate(REACHABLE,newterms);
		Predicate typePred = new Predicate(PIECETYPE,typeTerms);
		List<Literal> typeprecondition = new ArrayList();
		List<Literal> typeeffects = new ArrayList();
		Predicate pospred = new Predicate(OCCUPIES,terms);
		Predicate ownerPred = new Predicate(OWNER,ownerterms);
		Predicate newPospred = new Predicate(OCCUPIES,newterms);
		typeprecondition.add(new Literal((AtomicSentence) typePred));
		typeprecondition.add(new Literal((AtomicSentence) pospred));
		typeprecondition.add(new Literal((AtomicSentence) reachablePredicate));
		typeeffects.add(new Literal((AtomicSentence) typePred));
		typeeffects.add(new Literal( (AtomicSentence)newPospred));
		ActionSchema typeAction = new ActionSchema("type",variables,typeprecondition,typeeffects);
		Literal notAt = new Literal(pospred, true);
		writer.println("The type preconditions");
		for (Literal f:typeprecondition) {
			writer.println(f.toString());
		}
		writer.println("The type effects");
		for (Literal f:typeeffects) {
			writer.println(f.toString());
		}
		List<Literal> precondition = new ArrayList();
		List<Literal> effects = new ArrayList();
		precondition.add(new Literal((AtomicSentence) pospred));
		precondition.add(new Literal((AtomicSentence) ownerPred));
		effects.add(notAt);
		effects.add(new Literal( (AtomicSentence)newPospred));
		effects.add(new Literal( (AtomicSentence)ownerPred));
		writer.println("The moved preconditions");
		for (Literal f:precondition) {
			writer.println(f.toString());
		}
		writer.println("The moved effects");
		for (Literal f:effects) {
			writer.println(f.toString());
		}
		ActionSchema movedAction = new ActionSchema(moveName,variables,precondition,effects);
		List<Literal> initFluents = initialState.getFluents();
		List<Literal> goalFluents = goalState.getFluents();
		writer.println("The fluents of Initial state. They are nodes in S0");
		for (Literal f:initFluents) {
			writer.println(f.toString());
		}
		writer.println("The fluents the goal state");
		for (Literal f:goalFluents) {
			writer.println(f.toString());
		}
		return new Problem(initialState,goalState,typeAction,movedAction);
//		}
//	return null;
  }

  public State buildGoalstate(ChessActionImpl action) {
		String pieceName = action.getChessPiece().getMyPiece().getOntlogyName();
		String toPos = "";
		if (action.getPossibleMove() == null) {
			writer.println("No to position in move "+action.toString());
			List<Position> available  = action.getChessPiece().getNewlistPositions();
			toPos = available.get(0).getPositionName();
			writer.println("Using position "+available.get(0).toString());
		}else {
			toPos = action.getPossibleMove().getToPosition().getPositionName();
		}
		
		List<Term> terms = new ArrayList<Term>();
		List<Term> typeTerms = new ArrayList<Term>();
		List<Term> boardTerms = new ArrayList<Term>();
		List<Term> playerTerms = new ArrayList<Term>();
		
		Constant pieceVar = new Constant(pieceName);
		Constant posVar = new Constant(toPos);
		Constant type = new Constant(typeofPiece);
		Constant ownerVar = new Constant(playerName);
		playerTerms.add(ownerVar);
		boardTerms.add(posVar);
		terms.add(pieceVar);
		terms.add(posVar);
		typeTerms.add(pieceVar);
		typeTerms.add(type);
		Predicate playerPredicate = new Predicate(PLAYER,playerTerms);
		Predicate boardPredicate = new Predicate(BOARD,boardTerms);
		Predicate typePredicate = new Predicate(PIECETYPE,typeTerms);
		Predicate posSentence = new Predicate(OCCUPIES,terms);
		List<Term> ownerterms = new ArrayList<Term>();
	
		ownerterms.add(ownerVar);
		ownerterms.add(pieceVar);
		Predicate ownerSentence = new Predicate(OWNER,ownerterms);
		List<Literal> literals = new ArrayList();
		Literal pos = new Literal((AtomicSentence) posSentence);
		Literal own = new Literal((AtomicSentence) ownerSentence);
		Literal types = new Literal((AtomicSentence)typePredicate);
		Literal boards = new Literal((AtomicSentence)boardPredicate);
		Literal player = new Literal((AtomicSentence)playerPredicate);
		
		literals.add(pos);
//		literals.add(own);
		literals.add(types);
//		literals.add(player);
		literals.add(boards);
		State gState = new State(literals);
		return gState;
  }

  /**
   * determineParameters
   * Parameters that determine the structure of the Problem, and the states:
   * OBS: At present only the piece type is determined OLJ 06.02.21
   * The number of moves so far.
   * If the piece of the action is an officer or a pawn.
   * If the piece of the action has been moved recently
   * If the piece with its new position protects/reaches a centre position.
   * If the piece with its new position can capture an opponent piece.
   * @since 12.01.22
   * Recalculation of removed position no longer necessary
   * @param localAction
   */
  public void determineParameters(ChessActionImpl localAction) {
		String name = localAction.getChessPiece().getMyPiece().getOntlogyName();
//		localAction.processPositions();//This method recalculates removed positions for this action. Why is this necessary?
		AgamePiece piece = localAction.getChessPiece();
		pieceType type = piece.getPieceType();
//		int totalmoves = localAction.getMoveNumber().intValue();
		List<Integer> moveNumbers = piece.getMoveNumbers(); // Which moves has this piece been part of?
		int nofMoves = piece.getNofMoves();
		HashMap<String,ApieceMove> myMoves = myPlayer.getMyMoves(); // Get the moves so far and compare
		List<ApieceMove> myListmoves = new ArrayList(myMoves.values());
		boolean bNr = false; // bNr is true if the piece of the action has been moved recently
		for (ApieceMove move:myListmoves) {
			String moveName = move.getPiece().getMyPiece().getOntlogyName();
			int mNr = move.getMoveNumber();
			for (Integer pNr:moveNumbers) {
				bNr = mNr == pNr.intValue() && moveName.equals(name) && mNr - pNr.intValue() < 3;
				if (bNr)
					break;
			}
			if (bNr)
				break;
		}
		
/*		String position = piece.getmyPosition().getPositionName();
		List<Position> removedList = localAction.getPositionRemoved();
		List<Position> availableList = localAction.getAvailablePositions();
		ApieceMove move = localAction.getPossibleMove();
		List<Position> preferredPositions = move.getPreferredPositions();
		String toPos = move.getToPosition().getPositionName();
		Position toPosition = move.getToPosition();*/
		if (type == type.PAWN) {
			typeofPiece = PAWN;
			moveName = "pawnmove";
		}
		if (type == type.BISHOP) {
			typeofPiece = BISHOP;
			moveName = "bishopmove";
		}		
		if (type == type.ROOK) {
			typeofPiece = ROOK;
			moveName = "rookmove";
		}			
		if (type == type.KNIGHT) {
			typeofPiece = KNIGHT;
			moveName = "knoghtmove";
		}
		if (type == type.QUEEN) {
			typeofPiece = QUEEN;
			moveName = "queenmove";
		}
		if (type == type.KING) {
			typeofPiece = KING;
			moveName = "kingmove";
		}	
		/*		
		if (type == type.PAWN && !bNr ){
			boolean center = toPosition.isCenterlefthigh()||toPosition.isCenterleftlow()||toPosition.isCenterrighthigh()||toPosition.isCenterrightlow();
		}
		if (type != type.PAWN && !bNr) {
			
		}*/
  }

  /**
   * buildInitialstate
   * THis method creates an initial state for a problem 
   * with a given piece name and posname
   * @param piece
   * @return
   */
  public State buildInitialstate(String piece,String posName) {
		List<Sentence> folSentences = folKb.getOriginalSentences();
		State initState = null;
		String pieceName = null;
		String owner = null;
		List<String> reachablePos = new ArrayList<String>();
		List<Literal> literals = new ArrayList();
		List<String> attackablePos = new ArrayList<String>();
		List<String> castlePos = new ArrayList<String>();
		for (Sentence s : folSentences) {
			String symName = s.getSymbolicName();
			if (symName.equals(OCCUPIES)) {
				 ArrayList<Term> literalTerms = new ArrayList<>();
				List<Term> terms = (List<Term>) s.getArgs();
				Term f = terms.get(0);
				Term p = terms.get(1);
				pieceName = f.getSymbolicName();
				if (pieceName.equals(piece)) {
/*					Term term = new Constant(pieceName);
					Term ps = new Constant(p.getSymbolicName());
					literalTerms.add(term);
					literalTerms.add(ps);
					Literal l = new Literal(new Predicate(symName, literalTerms));*/
					Literal l = new Literal((AtomicSentence) s);
					literals.add(l);
				}
	
			}
/*			if (symName.equals(OWNER)) {
				List<Term> terms = (List<Term>) s.getArgs();
				Term f = terms.get(0);
				ArrayList<Term> literalTerms = new ArrayList<>();
				owner = f.getSymbolicName();
				Term last = terms.get(1);
				String p = last.getSymbolicName();
				if (owner.equals(playerName)&& p.equals(piece)) {
					Term term = new Constant(owner);
					Term ps = new Constant(p);
					literalTerms.add(term);
					literalTerms.add(ps);
					Literal l = new Literal(new Predicate(symName, literalTerms));
					Literal l = new Literal((AtomicSentence) s);
					literals.add(l);
				}
			}*/
			if (symName.equals(REACHABLE)) {
				List<Term> terms = (List<Term>) s.getArgs();
				ArrayList<Term> literalTerms = new ArrayList<>();
				Term f = terms.get(0);
				Term last = terms.get(1);
				String p = f.getSymbolicName();
				String pos = last.getSymbolicName();
				if (p.equals(piece)) {
/*					Term term = new Constant(owner);
					Term ps = new Constant(p);
					literalTerms.add(term);
					literalTerms.add(ps);
					Literal l = new Literal(new Predicate(symName, literalTerms));*/
					Literal l = new Literal((AtomicSentence) s);
					literals.add(l);
					reachablePos.add(pos);
					Constant posVar = new Constant(pos);
					List<Term> boardTerms = new ArrayList<Term>();
					boardTerms.add(posVar);
					Predicate boardPredicate = new Predicate(BOARD,boardTerms);
					Literal boards = new Literal((AtomicSentence)boardPredicate);
					literals.add(boards);
				}
			}
			if (symName.equals(PIECETYPE)) {
				List<Term> terms = (List<Term>) s.getArgs();
				ArrayList<Term> literalTerms = new ArrayList<>();
				Term f = terms.get(0);
				Term last = terms.get(1);
				String p = f.getSymbolicName();
				String type = last.getSymbolicName();
				if (p.equals(piece) && type.equals(typeofPiece)) {
					Literal l = new Literal((AtomicSentence) s);
					literals.add(l);
				}
			}
			if (symName.equals(BOARD)) {
				List<Term> terms = (List<Term>) s.getArgs();
				ArrayList<Term> literalTerms = new ArrayList<>();
				Term f = terms.get(0);
				String p = f.getSymbolicName();
				if (p.equals(posName)) {
					Literal l = new Literal((AtomicSentence) s);
					literals.add(l);
				}
			}
			if (symName.equals(PAWNATTACK)) {
				List<Term> terms = (List<Term>) s.getArgs();
				ArrayList<Term> literalTerms = new ArrayList<>();
				Term f = terms.get(0);
				Term last = terms.get(1);
				String pos = last.getSymbolicName();
				String p = f.getSymbolicName();
				if (p.equals(piece)) {
					attackablePos.add(pos);
					Literal l = new Literal((AtomicSentence) s);
					literals.add(l);
					Constant posVar = new Constant(pos);
					List<Term> boardTerms = new ArrayList<Term>();
					boardTerms.add(posVar);
					Predicate boardPredicate = new Predicate(BOARD,boardTerms);
					Literal boards = new Literal((AtomicSentence)boardPredicate);
					literals.add(boards);
				}
			}
			if (symName.equals(CASTLE)) {
				List<Term> terms = (List<Term>) s.getArgs();
				ArrayList<Term> literalTerms = new ArrayList<>();
				Term f = terms.get(0);
				Term last = terms.get(1);
				String pos = last.getSymbolicName();
				String p = f.getSymbolicName();
				if (p.equals(piece)) {
					Literal l = new Literal((AtomicSentence) s);
					literals.add(l);
					castlePos.add(pos);
					Constant posVar = new Constant(pos);
					List<Term> boardTerms = new ArrayList<Term>();
					boardTerms.add(posVar);
					Predicate boardPredicate = new Predicate(BOARD,boardTerms);
					Literal boards = new Literal((AtomicSentence)boardPredicate);
					literals.add(boards);
				}
			}
		}
		List<Literal>temp = addProtected(folSentences,reachablePos,piece);
		literals.addAll(temp);
		List<Literal>attacktemp = addProtected(folSentences,attackablePos,piece);
		List<Literal>castletemp = addProtected(folSentences,castlePos,piece);
		literals.addAll(attacktemp);
		literals.addAll(castletemp);
		return initState = new State(literals);
		
  }

  /**
   * addProtected
   * This method adds protected literals to a state
   * @param folSentences
   * @param reachablePos
   * @param piece
   * @return
   */
  public List<Literal> addProtected(List<Sentence> folSentences, List<String> reachablePos, String piece) {
		List<Literal> literals = new ArrayList();
		List<AgamePiece> pieces = myPlayer.getMygamePieces();
		for (Sentence s : folSentences) {
			String symName = s.getSymbolicName();
			if (symName.equals(PROTECTED)) {
				List<Term> terms = (List<Term>) s.getArgs();
				Term f = terms.get(0);
				Term last = terms.get(1);
				String p = f.getSymbolicName();
				String pos = last.getSymbolicName();
				AgamePiece gpiece =  (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(p)).findAny().orElse(null);
				String posto = reachablePos.stream().filter(pos::equals).findAny().orElse(null);
				if (!p.equals(piece) && posto != null && gpiece != null) {
					Literal l = new Literal((AtomicSentence) s);
					literals.add(l);
				}
			}
		}
		
		return literals;
  }

}
