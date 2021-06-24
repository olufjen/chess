
package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * the aima book
 * 
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
  private FOLKnowledgeBase folKb;

  /**
   * 
   * ChessDomain:
   *  All pieces are constants
   *  all positions are constants
   * 
   */
  private FOLDomain chessDomain;
  private FOLGamesFCAsk forwardChain;
  private FOLGamesBCAsk backwardChain;
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
  private AgamePiece opponentCatch = null;
  private Position opponentcatchPosition = null;
  
  private ChessActionImpl castleAction = null;

  private OpponentAgent opponentAgent = null;
  
  public AChessProblemSolver(ChessStateImpl stateImpl, ChessActionImpl localAction, FOLKnowledgeBase folKb, FOLDomain chessDomain, FOLGamesFCAsk forwardChain, FOLGamesBCAsk backwardChain, PlayGame game, APlayer myPlayer, APlayer opponent) {
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
		opponentAgent = new OpponentAgent(this.stateImpl,this.game,this.opponent,this.myPlayer,this.folKb);
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

  public FOLKnowledgeBase getFolKb() {
		return folKb;
  }

  public void setFolKb(FOLKnowledgeBase folKb) {
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
   * checkpieceFacts
   * This method checks the FOL knowledge base for certain facts about the player's pieces.
   * These facts can be any of the available predicates in the FOL Domain (see the domain object)
   * If an action is found belonging to the given piece, then this action is given the new position to move to
 * @param pieceName The name of the piece
 * @param pos The position to move to
 * @param fact The predicate fact
 * @param actions All the actions available to the player
 */
public boolean checkpieceFacts(String pieceVar,String pieceName,String pos,String fact,ArrayList<ChessActionImpl> actions) {
		Variable pieceVarx = null;
		if (pieceVar.equals("x"))
			pieceVarx = new Variable(pieceVar);
		Constant pieceVariable= new Constant(pieceName);
		Constant posVariable = new Constant(pos);
		List<Term> reachableTerms = new ArrayList<Term>();
		if (pieceVarx != null) {
			reachableTerms.add(pieceVarx);
		}else
			reachableTerms.add(pieceVariable);
		reachableTerms.add(posVariable);
		Predicate reachablePredicate = new Predicate(fact,reachableTerms);
		writer.println("PieceFacts Trying to prove\n"+reachablePredicate.toString());
		InferenceResult backWardresult =  backwardChain.ask(folKb, reachablePredicate);
		BCGamesAskHandler handler = (BCGamesAskHandler)backWardresult;
		HashMap vars = null;
		Term usedTerm = null;
		String termName = null;
		boolean properProtection = false;
		List<HashMap<Variable, Term>> finals = handler.getFinalList();
		if (finals != null && !finals.isEmpty() && pieceVarx != null) {
			vars = finals.get(0);
			usedTerm = (Term) vars.get(pieceVarx);
			termName = usedTerm.getSymbolicName(); // Finds which piece is protecting this position. This is only true if fact is PROTECTEDBY
			properProtection = !termName.equals(pieceName);
			writer.println("PieceFacts: position "+pos+" protected by "+termName+" and reachable by "+pieceName);
			return properProtection;
		}
		return backWardresult.isTrue();
		
		
/*		
		
	    ChessActionImpl naction =  (ChessActionImpl) actions.stream().filter(c -> c.getActionName().contains(pieceName)).findAny().orElse(null);
	    Position position =  (Position) positionList.stream().filter(c -> c.getPositionName().contains(pos)).findAny().orElse(null);
		if (backWardresult.isTrue() && termName != null && termName.equals(pieceName) && naction != null && naction.getPossibleMove() != null) {
			naction.getPossibleMove().setToPosition(position);
			naction.setPreferredPosition(position);
		 	writer.println("Checking piecefacts for \n"+fact + " piece "+pieceName + " and position "+pos);
//			writer.println("True");
			return true;
		}
		if (backWardresult.isTrue() && naction.getPossibleMove() == null) {
			writer.println(" NO MOVE !!! Checking piecefacts for \n"+fact + " piece "+pieceName + " and action "+naction.toString());
			return true;
		}
//		writer.println("False");
		return false;*/
  }
/**
 * checkoppoentThreat
 * This method checks if any of my pieces are threatened by the opponent player
 * If there are no threatened pieces then it returns null.
 * It is called from the prepareAction method
 * OBS!:: Must empty the hashmaps before using them again. It is done at the start of this method
 * @param fact
 * @param actions
 * @return The name of the pieces that is threatened if it has an action
 * If it does not have an action, Find a piece that can protect it.
 */
public String checkoppoentThreat(String fact,ArrayList<ChessActionImpl> actions) {
//	  List<AgamePiece> pieces = opponent.getMygamePieces();
	  threatenedPieces.clear();	
	  threadenedPositions.clear();
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
			  boolean threat = checkThreats("x", myposName, fact);
			  if(threat) {
				  threatenedPieces.put(myPieceName, mypiece);
				  threadenedPositions.put(myPieceName, myposition);
			  }
		  }
	  }
	  if (!threatenedPieces.isEmpty()) {
		  List<AgamePiece> pieces = myPlayer.getMygamePieces();
		  for (AgamePiece piece:myPieces) {// For all my pieces: is this piece under threat from any opponent piece?
			  String name = piece.getMyPiece().getOntlogyName();
			  if (threatenedPieces.containsKey(name)) {
				  ActionSchema movedAction = actionSchemas.get(name);
				  if (movedAction != null) {
					  return name;
				  }else {
						pieceType type = piece.getPieceType();
						if (type != type.PAWN) {
							Position threatPos = threadenedPositions.get(name);
							String posName = threatPos.getPositionName();
							boolean toProtect = checkFacts(name, posName, REACHABLE, actions);
							if (toProtect) {
								return name;
							}
						}
				  }
			  }
		  }
	  }
	return null;
}
  /**
   * checkOpponent
   * This method finds which opponent pieces the active player can safely take.
   * The pieces found are placed in a Map called possiblePieces, and its position is placed in a Map called possiblePositions
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
					  pawn = checkpieceFacts("y",name, posName, PAWNATTACK, actions);
					  if (pawn) {
						  possiblePieces.put(name, piece);
						  possiblePositions.put(name, position);
					  }
				  }
				  if (type  != type.PAWN) {
					  reachable = checkpieceFacts("y",name,posName,REACHABLE,actions);
					  if (reachable) {
						 pieceProtected = checkpieceFacts("x",name,posName,PROTECTED,actions);
						  if (pieceProtected) {
							  possiblePieces.put(name, piece);
							  possiblePositions.put(name, position);
							  writer.println("Piece is protected and safe to take with : "+name+"\n"+piece.getMyPiece().getOntlogyName());
						  }
					  }
				  }
				  boolean threat = checkThreats("x", posName, THREATEN);
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
   * checkThreats
   * This method checks the FOL knowledge base for certain facts about the opponent's pieces.
   * These facts can be any of the available predicates in the FOL Domain (see the domain object)
 * @param pieceName
 * @param pos
 * @param fact
 * @return
 */
public boolean checkThreats(String pieceName,String pos,String fact) {
	  List<AgamePiece> pieces = opponent.getMygamePieces();
	  AgamePiece piece = pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().equals(pieceName)).findAny().orElse(null);
	  Constant pieceVariable = null;
	  Variable pieceVar = null;
	  List<Term> reachableTerms = new ArrayList<Term>();
	  if (piece != null && piece.isActive()) {
		  pieceVariable = new Constant(pieceName);
		  reachableTerms.add(pieceVariable);
	  }else if(piece == null) {
		  pieceVar = new Variable(pieceName);
		  reachableTerms.add(pieceVar);
	  }
	  Constant posVariable = new Constant(pos);
	  reachableTerms.add(posVariable);
	  Predicate threatPredicate = new Predicate(fact,reachableTerms);
	  writer.println("Trying to prove\n"+threatPredicate.toString());
	  InferenceResult backWardresult =  backwardChain.ask(folKb,threatPredicate);
//	  writer.println(InferenceResultPrinter.printInferenceResult(backWardresult));

	  return backWardresult.isTrue();
	  
  }
  /**
   * checkFacts
   * This method checks the FOL knowledge base for certain facts about a player's pieces.
   * These facts can be any of the available predicates in the FOL Domain (see the domain object)
   * The parameter pos is used to give the chosen action a new position to move to.
 * @param pieceName The name of the piece
 * @param pos The position to move to
 * @param fact The predicate fact
 * @param actions All the actions available to the player
 */
public boolean checkFacts(String pieceName,String pos,String fact,ArrayList<ChessActionImpl> actions) {
		Constant pieceVariable= new Constant(pieceName);
		Constant posVariable = new Constant(pos);
		List<Term> reachableTerms = new ArrayList<Term>();
		reachableTerms.add(pieceVariable);
		reachableTerms.add(posVariable);
		Predicate reachablePredicate = new Predicate(fact,reachableTerms);
		InferenceResult backWardresult =  backwardChain.ask(folKb, reachablePredicate);
	    ChessActionImpl naction =  (ChessActionImpl) actions.stream().filter(c -> c.getActionName().contains(pieceName)).findAny().orElse(null);
	    Position position =  (Position) positionList.stream().filter(c -> c.getPositionName().contains(pos)).findAny().orElse(null);
		if (backWardresult.isTrue() && naction != null) {
			naction.getPossibleMove().setToPosition(position);
			naction.setPreferredPosition(position);
			return true;
		}
		return false;
  }
/**
 * checkmyProtection
 * This method checks if a piece is protected by other pieces than by itself
 * @param pieceName
 * @param pos
 * @return true if it is protected
 */
public boolean checkmyProtection(String pieceName,String pos) {
	  List<AgamePiece> myPieces = myPlayer.getMygamePieces();
	  boolean protectedpiece = false;
	  for (AgamePiece piece:myPieces) {
		  String name = piece.getMyPiece().getOntlogyName();
		  if (!name.equals(pieceName)) {
				Constant pieceVariable= new Constant(name);
				Constant posVariable = new Constant(pos);
				List<Term> reachableTerms = new ArrayList<Term>();
				reachableTerms.add(pieceVariable);
				reachableTerms.add(posVariable);
				Predicate reachablePredicate = new Predicate(PROTECTED,reachableTerms);
				InferenceResult backWardresult =  backwardChain.ask(folKb, reachablePredicate);
				protectedpiece = backWardresult.isTrue();
				if (protectedpiece)
					return protectedpiece;
		  }
	  }
	return protectedpiece;
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
	String pname = checkoppoentThreat(THREATEN,actions); // returns null if no pieces are threatened
	String pieceName = "WhiteBishop2";
	String fpos = "f1";
	String toPos = "d3";
	boolean bishop = checkpieceFacts("y",pieceName,fpos,OCCUPIES,actions); // Rook occupies f1 !!??
	State goal = null;
	State initstate = null;
	if (bishop) {
		String name = pieceName;
		ChessActionImpl naction =  (ChessActionImpl) actions.stream().filter(c -> c.getActionName().contains(name)).findAny().orElse(null);
		if (naction != null) {
			AgamePiece piece = naction.getChessPiece();
			pieceType type = piece.getPieceType();
			determineType(type);
			goal = buildGoalstate(pieceName,toPos);
			List<Position> removed = piece.getRemovedPositions();
			Position pos =  (Position) removed.stream().filter(c -> c.getPositionName().contains(toPos)).findAny().orElse(null);
			if (pos != null) {
				String pawnName = "WhitePawn5";
				String pawnPos = "e2";
				boolean pawn = checkpieceFacts("y",pawnName,pawnPos,OCCUPIES,actions);
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
			}else {
				checkCastling(actions);
				boolean threat = checkThreats("x", "c4", THREATEN);
//				boolean threat = true;
				if (threat) {
					  checkFacts(pieceName, "d3", REACHABLE, actions);
				}
				return pieceName;
			}
		}
	}
	if (pname != null) { // Has found a piece that is threatened. Then determine the best move
		String name = pname;
		ChessActionImpl naction =  (ChessActionImpl) actions.stream().filter(c -> c.getActionName().contains(name)).findAny().orElse(null);
		if (naction != null) { // If the threatened piece has an action. Then move it to a safe position
			ApieceMove move = naction.getPossibleMove();
			AgamePiece piece = naction.getChessPiece();
			List<Position> prefPos = piece.getPreferredPositions();
			Position tonewPos = move.getToPosition();
			String toPosname = tonewPos.getPositionName();
			boolean protectedpiece = false;
			protectedpiece = checkmyProtection(name,toPosname); // Is the new position protected then move the piece
			if (protectedpiece) {
				opponentAgent.probeConsequences(naction);
				return pname;
			}
			else {
				List<Position> reachable = piece.getNewlistPositions();
				for (Position pos:reachable) {
					if (!piece.checkRemoved(pos)) {
						String posName = pos.getPositionName();
						protectedpiece = checkmyProtection(name,posName); // must also set the new to position !!
						boolean threat = checkThreats(pname, posName, THREATEN);
						if (protectedpiece && !threat) {
							naction.getPossibleMove().setToPosition(pos);
							naction.setPreferredPosition(pos);
							opponentAgent.probeConsequences(naction);
							return pname;
						}
					}
				}
			}
		}
		opponentAgent.probeConsequences(naction);
		return pname; // The name of the piece that is threatened. Or the name of the piece that can protect it
	}
	// Find the best move and a protected position to move to.
	// For a possible strategy, see notes in compendium
	for (ChessActionImpl action:actions){
		AgamePiece piece = action.getChessPiece();
		String playername = myPlayer.getNameOfplayer();
		Constant ownerVariable = new Constant(playername);
		String name = action.getChessPiece().getMyPiece().getOntlogyName(); 
		ApieceMove move = action.getPossibleMove();
/*		String toPosition = "";
		if (action.getPreferredPosition() != null)
			toPosition = action.getPreferredPosition().getPositionName();*/
		List<Position> availablePositions = piece.getNewlistPositions();
		if (move != null && availablePositions != null && !availablePositions.isEmpty()) {
			for (Position pos:availablePositions){
				if(!piece.checkRemoved(pos)) {
					List<Term> ownerTerms = new ArrayList<Term>();
					String position = pos.getPositionName();
					Constant pieceVariable = new Constant(name);
					
					Variable otherPiece = new Variable("x");
					ownerTerms.add(ownerVariable);
					ownerTerms.add(pieceVariable);
					Predicate ownerPredicate = new Predicate(OWNER,ownerTerms);
					Constant posVariable = new Constant(position);
			        List<Term> reachableTerms = new ArrayList<Term>();
					reachableTerms.add(pieceVariable);
					reachableTerms.add(posVariable);
					List<Term> protectedTerms = new ArrayList<Term>();
					protectedTerms.add(otherPiece);
					protectedTerms.add(posVariable);
					Predicate reachablePredicate = new Predicate(REACHABLE,reachableTerms);
					Predicate protectedPredicate = new Predicate(PROTECTED,protectedTerms);
					ConnectedSentence reachableSentence = new ConnectedSentence(Connectors.AND,ownerPredicate,reachablePredicate);
					ConnectedSentence protectedSentence = new ConnectedSentence(Connectors.AND,ownerPredicate,protectedPredicate);
//					ConnectedSentence reachablegoal = new ConnectedSentence(Connectors.IMPLIES,reachableSentence,movePredicate);
//					ConnectedSentence protectedGoal = new ConnectedSentence(Connectors.IMPLIES,protectedSentence,safemovePredicate);
					List<Term> moveTerms = new ArrayList<Term>();
					moveTerms.add(pieceVariable);
					moveTerms.add(posVariable);
					Predicate movePredicate = new Predicate(MOVE,moveTerms);
					Predicate safemovePredicate = new Predicate(SAFEMOVE,moveTerms);
					writer.println("THE BEST MOVE Trying to prove backward chaining safemove\n"+movePredicate.toString());
					InferenceResult backWardresult =  backwardChain.ask(folKb, movePredicate);
					boolean movePossible = backWardresult.isTrue();
					boolean protectedpiece = false;
					protectedpiece = checkmyProtection(name,position);
					if (movePossible && protectedpiece) {
						BCGamesAskHandler bcHandler = (BCGamesAskHandler) backWardresult;
						writer.println(InferenceResultPrinter.printInferenceResult(backWardresult));
					}
					writer.println("THE BEST MOVE Trying to prove backward chaining owner\n"+ownerPredicate.toString());
					InferenceResult ownerresult =  backwardChain.ask(folKb, ownerPredicate);	// OBS can only prove atomic sentences			
//					bcHandler.clearLists();
//					writer.println(bcHandler.toString());
//					writer.println(InferenceResultPrinter.printInferenceResult(backWardresult));
					if(movePossible && protectedpiece && ownerresult.isTrue()) {
//						writer.println("\n"+InferenceResultPrinter.printInferenceResult(ownerresult));
						action.getPossibleMove().setToPosition(pos);
						action.setPreferredPosition(pos);
						opponentAgent.probeConsequences(action);
						return name;
					}
				}
			}
		}
	}
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
			boolean bishop = checkpieceFacts("y",bishopName,fpos,OCCUPIES,actions);
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
		literals.add(own);
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
		  checkFacts(pieceName, pos, REACHABLE, actions);
		  break;
	  case 4:
		  checkOpponent("", actions);
		  pieceName = "WhiteKnight1";
		  break;
	  case 6:
		  checkOpponent("", actions);
		  pieceName = "WhiteKnight2";
		  String posx = "f3";
		  checkFacts(pieceName, posx, REACHABLE, actions);
		  break;
	  default:
		  checkOpponent("", actions); // Result: A list of opponent pieces that can be taken
		  String blackpieceName = "BlackBishop1";
		  String blackpos = "g4";
		  if (checkThreats(blackpieceName, blackpos, OCCUPIES)) {
			  pieceName = "WhitePawn8";
			  break;
		  }
		  String pname = "x";
		  String bpos = "d5";
		  //			  pieceName = "WhitePawn3";
		  if (checkThreats(pname, bpos, OCCUPIES)) {  // OBS What happens if a friendly piece occupies this position !!!???
			  pieceName = "WhitePawn3";
			  String wpos = "d5";
			  if (checkFacts(pieceName, wpos, PAWNATTACK, actions))
				  break;
		  }
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
		  if (pieceName == null)
			  pieceName = "WhitePawn1";
		  break;
	  }
	  return pieceName;
  }

/**
 * deferredMove
 * This method checks if any deferred move has been set.
 * If so, it returns the deferred key
 * A special case for castling
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
		checkFacts(key, posx, CASTLE, actions);
		List<AgamePiece> pieces = myPlayer.getMygamePieces();
		AgamePiece movedPiece = (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(key)).findAny().orElse(null);
		HashMap<String,Position> castlePos = movedPiece.getCastlePositions();
		Position toCastle = castlePos.get(posx);
		if (movedPiece != null && toCastle != null) {	
			AgamePiece castle = myPlayer.checkCastling(movedPiece, toCastle);
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
 * This method creates and returns a High Level Action Problem given the available actions
 * Hierarchical Task networks and High Level Actions are described in  chapter 11.
 * At present, the High Level Problem contains one primitive action schema (chess action).
 * @param actions
 * @return
 */
/**
 * @param actions
 * @return
 */
public ChessProblem planProblem(ArrayList<ChessActionImpl> actions) {
	  searchProblem(actions); // Builds an ActionSchema for every Chess Action.
	  String pieceName = null;
	  ChessProblem problem = null;
	  String actionName = deferredMove(actions);
      pieceName = checkMovenumber(actions);
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
		  writer.println("Chosen action Schema\n"+movedAction.toString());
		  ChessActionImpl naction =  (ChessActionImpl) actions.stream().filter(c -> c.getActionName().equals(nactionName)).findAny().orElse(null);
		  State initState = initStates.get(pieceName);
		  State goal = goalStates.get(pieceName);
		  problem = new ChessProblem(initState,goal,movedAction);
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
 * @param actions
 * @return
 */
  public List<ActionSchema> searchProblem(ArrayList<ChessActionImpl> actions) {
	  List<ActionSchema> schemas = new ArrayList<ActionSchema>();
	  for (ChessActionImpl action:actions) {
			if (action.getPossibleMove()!= null && !action.isBlocked()) {
				determineParameters(action);
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
				Variable piece = new Variable("piece");
				Variable pos = new Variable("pos");
				Variable toPos = new Variable("topos");
				Constant type = new Constant(typeofPiece);
//				Variable ownerVar = new Variable("owner");
				Constant ownerVar = new Constant(playerName);
				List variables = new ArrayList<Variable>(Arrays.asList(piece,pos,toPos));
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
				Predicate typePred = new Predicate(PIECETYPE,typeTerms);
				Predicate reachablePredicate = new Predicate(REACHABLE,newterms);
				Predicate pospred = new Predicate(OCCUPIES,terms);
				Predicate ownerPred = new Predicate(OWNER,ownerterms);
				Predicate newPospred = new Predicate(OCCUPIES,newterms);
				List<Literal> precondition = new ArrayList();
				List<Literal> effects = new ArrayList();
				precondition.add(new Literal((AtomicSentence) pospred));
				precondition.add(new Literal((AtomicSentence) ownerPred));
				precondition.add(new Literal((AtomicSentence) typePred));
//				Literal notAt = new Literal(pospred, true);
//				effects.add(notAt);
				effects.add(new Literal( (AtomicSentence)newPospred));
				effects.add(new Literal( (AtomicSentence)ownerPred));
				effects.add(new Literal( (AtomicSentence)typePred));
				ActionSchema movedAction = new ActionSchema(actionName,variables,precondition,effects);
				actionSchemas.put(pieceName, movedAction);
				schemas.add(movedAction);
				
			}
	  }
	  return schemas;
  }

  /**
   * solveProblem
   * This method solves a Problem for a given ChessAction
   * using the Graphplan algorithm. (see p. 383 chapter 10.3)
 * @param action
 * @return
 */
/**
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
		literals.add(own);
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
   * @param localAction
   */
  public void determineParameters(ChessActionImpl localAction) {
		String name = localAction.getChessPiece().getMyPiece().getOntlogyName();
		localAction.processPositions();//This method recalculates removed positions for this action. Why is this necessary?
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
   * with a given piece name
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
			if (symName.equals(OWNER)) {
				List<Term> terms = (List<Term>) s.getArgs();
				Term f = terms.get(0);
				ArrayList<Term> literalTerms = new ArrayList<>();
				owner = f.getSymbolicName();
				Term last = terms.get(1);
				String p = last.getSymbolicName();
				if (owner.equals(playerName)&& p.equals(piece)) {
	/*				Term term = new Constant(owner);
					Term ps = new Constant(p);
					literalTerms.add(term);
					literalTerms.add(ps);
					Literal l = new Literal(new Predicate(symName, literalTerms));*/
					Literal l = new Literal((AtomicSentence) s);
					literals.add(l);
				}
			}
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
/*			if (symName.equals(PLAYER)) {
				List<Term> terms = (List<Term>) s.getArgs();
				ArrayList<Term> literalTerms = new ArrayList<>();
				Term f = terms.get(0);
				String p = f.getSymbolicName();
				if (p.equals(playerName)) {
					Literal l = new Literal((AtomicSentence) s);
					literals.add(l);
				}
			}*/
		}
		List<Literal>temp = addProtected(folSentences,reachablePos,piece);
		literals.addAll(temp);
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
		for (Sentence s : folSentences) {
			String symName = s.getSymbolicName();
			if (symName.equals(PROTECTED)) {
				List<Term> terms = (List<Term>) s.getArgs();
				Term f = terms.get(0);
				Term last = terms.get(1);
				String p = f.getSymbolicName();
				String pos = last.getSymbolicName();
				String posto = reachablePos.stream().filter(pos::equals).findAny().orElse(null);
				if (!p.equals(piece) &&posto != null) {
					Literal l = new Literal((AtomicSentence) s);
					literals.add(l);
				}
			}
		}
		
		return literals;
  }

}
