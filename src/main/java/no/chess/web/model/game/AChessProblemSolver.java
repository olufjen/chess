package no.chess.web.model.game;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.ToDoubleFunction;

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
import aima.core.search.adversarial.AdversarialSearch;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import aima.core.search.framework.Metrics;
import aima.core.search.framework.Node;
import aima.core.search.framework.NodeExpander;
import aima.core.search.framework.problem.ActionsFunction;
import aima.core.search.framework.problem.ResultFunction;
import aima.core.search.framework.problem.StepCostFunction;
import no.function.FunctionContect;
import no.chess.web.model.PlayGame;
import no.chess.web.model.Position;
import no.games.chess.ChessPieceType;
import no.games.chess.ChessPlayer;
import no.games.chess.AbstractGamePiece.pieceType;
import no.games.chess.fol.BCGamesAskHandler;
import no.games.chess.fol.FOLGamesBCAsk;
import no.games.chess.fol.FOLGamesFCAsk;
import no.games.chess.planning.ChessProblem;
import no.games.chess.planning.PlannerGame;
import no.games.chess.planning.ChessGraphPlanAlgorithm;
import no.games.chess.planning.ChessPlannerSearch;
import no.games.chess.planning.PlannerState;
import no.games.chess.search.ChessGoalTest;
import no.games.chess.search.ChessNode;
import no.games.chess.search.ChessSearchProblem;
import no.games.chess.search.PlannerQueueBasedSearch;
import no.games.chess.search.PlannerQueueSearch;
import no.games.chess.search.nondeterministic.AndOrChessSearch;
import no.games.chess.search.nondeterministic.ChessPath;
import no.games.chess.search.nondeterministic.ChessPlan;
import no.games.chess.search.nondeterministic.GameAction;
import no.games.chess.search.nondeterministic.GameState;
import no.games.chess.search.nondeterministic.NonDetermineChessActionFunction;
import no.games.chess.search.nondeterministic.NonDetermineResultFunction;
import no.games.chess.search.nondeterministic.NondeterministicChessProblem;
import no.games.chess.planning.ChessPlannerAction;


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
/*
 * Predicate names	
 */
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
  private String playerName =  "";
  private String OCCUPIES = "";
  private String PAWNATTACK ="";
  private String playSide;
  private String BOARD;
  private String PLAYER;
  private String CASTLE;
  private String OPPONENTTO;
  private String POSSIBLETHREAT;
  private String POSSIBLEPROTECT; // All available positions for a piece are possibly protected by that piece
  private String POSSIBLEREACH; // All available positions for a piece are possibly reachable by that piece
  private String CENTERSQUARE;
  private String HOMESQUARE;
  private String MINORPIECE;
  private String CONTROLCENTER;
  
  /**
   *  The type of piece under consideration
   */
  private String typeofPiece;

  /**
   *  The name of the action schema - pawnmove.bishopmove ...
   */
  private String moveName;
  private String theCastling = "castlingmove";
  private String outputFileName =  "problem.txt";
  private ChessStateImpl stateImpl =  null;
  private ChessActionImpl localAction =  null;
  private ChessKnowledgeBase kb =  null;
  private int noofMoves =  0; //game.getMovements().size(); 
  private List<Position> positionList = null; // The original HashMap of positions as a list
  /**
   * 
   * A first order knowledge base
   * 
   */
  private ChessFolKnowledgeBase folKb; // The chess knowledge base
  private ChessFolKnowledgeBase localKb = null; // The strategy knowledge base
  /**
   * 
   * ChessDomain:
   *  All pieces are constants
   *  all positions are constants
   * 
   */
  private FOLGamesFCAsk forwardChain;
  private ChessDomain chessDomain;
  
  private FOLGamesBCAsk backwardChain; // The backward chain inference procedure
  private PrintWriter writer =  null;
  private FileWriter fw =  null;
  private PlayGame game =  null;
  private APlayer myPlayer =  null;
  private APlayer opponent =  null;
  private State initialState =  null; // Collected from the perceptor and used when creating the Chessproblem used in the graphplan alogithm. (See the planProblem method)
  private State goalState =  null;// Collected from the perceptor and used when creating the Chessproblem used in the graphplan alogithm.
  private State deferredInitial = null;
  private State deferredGoal = null;
  private Map<String,State>deferredGoalstates = null;
  private String deferredKey = null;
  private GraphPlanAlgorithm graphPlan =  null;
  private Map<String,ActionSchema> actionSchemas = null;
  private List<List> initialStates = null; // A list of initial states 
  private Map<String,State>initStates = null; // Contains all initial states for current move
  private Map<String,State>goalStates = null; // Contains all goal states for current move
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
  private List<ActionSchema> actionSchemalist = null; // The list of actionschemas produced by the searchProblem method (Check the Map actionSchemas)
//  private List<GroundGameState> gameStateList = null; // The list of GameState states. Represent the population of GameState OJN June 25 Removed 26.01.26
//  private List<GameState> thegameStateList = null;
  private OpponentAgent opponentAgent = null;
  private AgamePiece chosenPiece = null; // Is only set in the checkoppoentThreat method
  private Position chosenPosition = null;// Is only set in the checkoppoentThreat method
  private AgamePiece opponentKing = null;
  private String opponentKingPosition = null;
  private State chosenInitstate = null;
  private ActionSchema theSolution = null; // An action Schema chosen from the graphplan algorithm
  private Set<ActionSchema> otherSchemas = null;// A Set of propositionalized action schemas from the lifted action schema. It is used for problem solving
  private List<ActionSchema>otherSchemaList = null;// A list of propositionalized action schemas from the lifted action schema. It is used for problem solving
 /*
 * theState represents the total initial state containing all the stateLiterals   
 */
  private List<Literal> stateLiterals = null;
  private State theState = null;
  
  private ChessPlannerSearch search;
  private AplannerGame plannerGame;
  private APerceptor thePerceptor = null;
  
  public AChessProblemSolver(ChessStateImpl stateImpl, ChessActionImpl localAction, ChessFolKnowledgeBase folKb, ChessDomain chessDomain, FOLGamesFCAsk forwardChain, FOLGamesBCAsk backwardChain, PlayGame game, APlayer myPlayer, APlayer opponent) {
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
		localKb = opponentAgent.getLocalKb();
		playerName = this.myPlayer.getNameOfplayer();
		playSide = playerName.substring(0,5);
		noofMoves = game.getMovements().size();
		graphPlan = new GraphPlanAlgorithm();
		String catalog = KnowledgeBuilder.getFileCatalog();
		outputFileName = catalog+outputFileName;
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
	    setPredicatenames();
	    actionSchemas = new HashMap<String,ActionSchema>();
	    actionSchemalist = new ArrayList<ActionSchema>();
//	    gameStateList = new LinkedList<GroundGameState>(); // Removed 26.01.26
//	    thegameStateList = new LinkedList<GameState>();
	    initialStates = new ArrayList<List>();
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
	    stateLiterals = new ArrayList<Literal>();
	    theState = new State(stateLiterals);

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
		
		HOMESQUARE = KnowledgeBuilder.getHOMESQUARE();
		MINORPIECE = KnowledgeBuilder.getMINORPIECE();
		CENTERSQUARE = KnowledgeBuilder.getCENTERSQUARE();
		CONTROLCENTER = KnowledgeBuilder.getCONTROLCENTER();
  }
  

  public List<List> getInitialStates() {
	  return initialStates;
  }
  public void setInitialStates(List<List> initialStates) {
	  this.initialStates = initialStates;
  }
  public ActionSchema getTheSolution() {
	  return theSolution;
  }
  public void setTheSolution(ActionSchema theSolution) {
	  this.theSolution = theSolution;
  }
  public OpponentAgent getOpponentAgent() {
	  return opponentAgent;
  }
  public void setOpponentAgent(OpponentAgent opponentAgent) {
	  this.opponentAgent = opponentAgent;
  }
  public AgamePiece getOpponentKing() {
	  return opponentKing;
  }
  public void setOpponentKing(AgamePiece opponentKing) {
	  this.opponentKing = opponentKing;
  }
  public String getOpponentKingPosition() {
	  return opponentKingPosition;
  }
  public void setOpponentKingPosition(String opponentKingPosition) {
	  this.opponentKingPosition = opponentKingPosition;
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

  public ChessDomain getChessDomain() {
		return chessDomain;
  }

  public void setChessDomain(ChessDomain chessDomain) {
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
   * @since Jan 25 
   * This method is called from the planProblem method
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
							  chosenPosition = toPos;
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
										  chosenPosition = pos;
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
							  boolean protect = folKb.checkThreats("x", newPos, PROTECTED,opponent);
							  if (!protect) {
								  chosenPiece = piece;
								  chosenPosition = pos;
								  String xName = name;
								  ChessActionImpl naction =  (ChessActionImpl) actions.stream().filter(c -> c.getActionName().contains(xName)).findAny().orElse(null);
								  if (naction != null && naction.getPossibleMove() != null) {
									  naction.getPossibleMove().setToPosition(pos); // OBS !! wrong position !!!
									  naction.setPreferredPosition(pos);
									  writer.println("Suggested Move  "+thisPiece+ " to "+newPos);
									  return;
								  }
							  }
						  }
					  }
				  }
			  }
		  }// END For all my pieces under threat
	  } //END there are threatened pieces

  }
  /**
   * checkOpponent
   * This method finds which opponent pieces the active player can safely take.
   * The pieces found are placed in a Map called possiblePieces, and its position is placed in a Map called possiblePositions
   * It is called from the planProblem method
   * The key for the maps is the ontology name of the piece of the player
   * @since 15.03.21 Only pieces that are active are considered
   * @since 14.01.25 The key to the map entries changed to name of piece of player + position of the piece that can be taken
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
			  //			  writer.println("\nPosition from myposition\n"+piece.toString());
			  position = piece.getmyPosition();
			  posName = position.getPositionName();
		  }else {
			  posName = position.getPositionName();
			  //			  writer.println("\nPosition from heldposition\n"+piece.toString());
		  }
		  if (piece.isActive()) {
			  for (AgamePiece mypiece:myPieces) { // For all my pieces: Can this piece reach the opponent's position?
				  String name = mypiece.getMyPiece().getOntlogyName();
				  String keyName = name + posName; // New key for map entries 
				  pieceType type = mypiece.getPieceType();
				  boolean reachable = false;
				  boolean pawn = false;
				  boolean pieceProtected = false;
				  if (type  == type.PAWN) {
					  pawn = folKb.checkpieceFacts("y",name, posName, PAWNATTACK);
					  if (pawn) {
						  possiblePieces.put(keyName, piece);
						  possiblePositions.put(keyName, position);
						  writer.println("The pawn can take a piece with key: "+keyName+"\n"+piece.getMyPiece().getOntlogyName());
					  }
				  }
				  if (type  != type.PAWN) {
					  reachable = folKb.checkpieceFacts("y",name,posName,REACHABLE);
					  if (reachable) {
						  pieceProtected = folKb.checkpieceFacts("x",name,posName,PROTECTED);
						  if (pieceProtected) {
							  possiblePieces.put(keyName, piece);
							  possiblePositions.put(keyName, position);
							  writer.println("Piece is protected and safe to take with : "+keyName+"\n"+piece.getMyPiece().getOntlogyName());
						  }
					  }
				  }
				  boolean threat = folKb.checkThreats("x", posName, THREATEN,opponent);
				  if (!threat && reachable && !pieceProtected) {
					  possiblePieces.put(keyName, piece);
					  possiblePositions.put(keyName, position);
					  writer.println("Piece is safe to take with : "+keyName+"\n"+piece.getMyPiece().getOntlogyName());
				  }

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
   * fillthePerceptor
   * This method fills the perceptor with necessary information.
   * 
 * @param thePerceptor
 */
public void fillthePerceptor(APerceptor thePerceptor) {
	  thePerceptor.setMyPlayer(myPlayer);
	  thePerceptor.setOpponent(opponent);
	  thePerceptor.setAgent(opponentAgent);
	  thePerceptor.setFolKb(folKb);
	  thePerceptor.setLocalKb(localKb);
	  thePerceptor.setInitStates(initStates);
	  thePerceptor.setGoalStates(goalStates);
	  thePerceptor.setPossiblePieces(possiblePieces);
	  thePerceptor.setPossiblePositions(possiblePositions);
	  thePerceptor.setAttackers(attackers);
	  thePerceptor.setProtectors(protectors);
	  thePerceptor.setThreadenedPositions(threadenedPositions);
	  thePerceptor.setThreatenedPieces(threatenedPieces);
  }
  /**
 * createPerceptor
 * Creates a simple perceptor
 */
public void createPerceptor() {
	  State thechosenState = null;
	  Position posin = null;
//	  posin = positions.get("d4");
	  thePerceptor = new APerceptor(playerName);
//	  thePerceptor = new APerceptor(posin,REACHABLE,PIECETYPE,null,playerName);
	  fillthePerceptor(thePerceptor);
//	  thechosenState = thePerceptor.checkPercept(initStates); // A hash table of available init states.
	  thePerceptor.findReachable(posin);
//	  pieceName = thePerceptor.getPlayerPiece().getMyPiece().getOntlogyName();
	  //		  thePerceptor.createLiftedActions(null,null,"d4",null);
//	  checkOpponent("", (ArrayList<ChessActionImpl>) actions);
	  // The statements below must be as part of the search strategy !!!

  }
 


  /**
 * planProblem
 * This is the main method of the ProblemSolver
 * This method creates and returns a High Level Action Problem given the available actions
 * Hierarchical Task networks and High Level Actions are described in  chapter 11.
 * At present, the High Level Problem contains one primitive action schema (chess action).
 * @param actions - A list of chess actions available
 * @return a ChessProblem to be solved
 */
/**
 * @param actions
 * @return
 */
public ChessProblem planProblem(ArrayList<ChessActionImpl> actions) {
	  this.actions = actions; // All chess actions available to player
	  opponentAgent.setPlayeractions(actions);
	  actionSchemalist = searchProblem(actions); // Builds an ActionSchema and GameStates for every Chess Action. This is the planning phase
// The maps initStates and goalStates are also filled.	
	  opponentAgent.setInitStates(initStates);
	  opponentAgent.setGoalStates(goalStates);
	  String pieceName = null;
	  ChessProblem problem = null;
	
	  State theInitState = null;
      State theGoal = null;
	  opponentAgent.probepossibilities(actions, myPlayer); // Creates entries to the strategy knowledge base. This call is moved from the prepareAction method
      checkOpponent("", actions); //This method finds which opponent pieces the active player can safely take
      checkoppoentThreat(THREATEN, actions); // This method find opponent pieces that threaten player's pieces, which pieces protect player's own pieces
      // and opponents attacker pieces.
      createPerceptor();
//      List<Sentence> sentences = folKb.getOriginalSentences();
//      writer.println("The population of gamestates");
		/*
		 * for (GroundGameState gameState:gameStateList) { // A population of GameStates
		 * gameState.setThePerceptor(thePerceptor); gameState.setOpponent(opponent);
		 * gameState.setPlayer(myPlayer); gameState.setMoveNr(noofMoves);
		 * gameState.setStatestatistics(); writer.println(gameState.toString()); }
		 * thegameStateList.addAll(gameStateList);
		 */
/*
 * How to define the initial state?   At present not in use OJN 05.08.25   
 */
/*
 * GameState initialGameState = gameStateList.get(0); GameAction gameAction =
 * initialGameState.getAction();
 */
      PlannerState plannerState = new PlannerStateImpl(myPlayer,opponent,actionSchemalist,noofMoves,thePerceptor); // An alternative Plannerstate creator
      ChessPlannerAction plannerAction = plannerState.getAction();

/*
 * The structure of queue search:    
 *   
 */
      PlannerStateImpl localPlanner = (PlannerStateImpl)plannerState;
      NodeExpander exp = new NodeExpander();
      PlannerQueueSearch queueSearch = new PlannerQueueSearch(exp);
      ToDoubleFunction<Node<PlannerState, ChessPlannerAction>> h = KnowledgeBuilder.toDouble(); // The evaluation function
/*
 * PlannerQueueBasedSearch - an implementation of an informed search strategy described in chapter 3.5 p. 92
 * The general approach is called best first search. 
 * This type of search explores available paths from an initial state to a goal state
 * Is it possible to use this search mechanism to find
 * 1. which position to occupy with what piece?
 * 2. which piece to move to a certain position?
 * 3. which opponent pieces to take?     
 */
      PlannerQueueBasedSearch queuebasedSearch = new PlannerQueueBasedSearch(queueSearch,plannerState,plannerAction,h);
      ActionsFunction<PlannerState, ChessPlannerAction> af = KnowledgeBuilder.anActionFunction();// The Actions Function
      ResultFunction<PlannerState, ChessPlannerAction> rf = KnowledgeBuilder.aResultFunction(); // The Result function
      ChessGoalTest<PlannerState> gt = KnowledgeBuilder.aGoaltest(plannerAction); // The goal test function
      StepCostFunction<PlannerState, ChessPlannerAction> stepcost = KnowledgeBuilder.stepCost();
      ChessSearchProblem searchProblem = new ChessSearchProblem(plannerState,af, rf, gt,stepcost,plannerState,plannerAction,plannerState.getActions());
/*
 * The following call generates the following set of calls:
 *    1. The findState call
 *    2. The queuesearch.findnode
 *    3. The findnode method removes a node from frontier (The queue)
 *    	calls the problem testSolution method with this node
 *    	If the testSolution return true then findnode returns an optional node.
 *    	If not the current node is expanded using the nodeexpander expand method.
 *    	The expand method calls the problem getResult method, that must return a new state for the next node.
 *    	This call is repeated for all available actions in the problem.
 *    	The expand method returns a list of successor nodes.  
 */

      /*
       * The findState call returns with an initial and goal state in the perceptor to be used in the planning procedure below.
       * The set of all available Action Schemas is held by the perceptor.
       * These parameters are used to create the ChessProblem object to be used in the Graphplan procedures.
       */
      Optional<PlannerState> astate = queuebasedSearch.findState(searchProblem);
      Metrics metric = queueSearch.getMetrics();
      Queue<Node<PlannerState, ChessPlannerAction>> front = queueSearch.getFrontier();
/*
 * For nondeterministic search as described in chapter 4.3.2 and figure 4.11 page 135     
 */
      GroundGameState nondeterminState = new GroundGameState(myPlayer,opponent,noofMoves,thePerceptor,folKb,actionSchemalist,positionList); // When created also create the initial action
      GroundGameAction gameAction  = (GroundGameAction) nondeterminState.createAction(); // creates an initial action
      NonDetermineResultFunction ndeterRFN = new NonDetermineResultFunction (nondeterminState,gameAction); //
      ChessGoalTest<GameState> gameTest = KnowledgeBuilder.nondeterminGoaltest(gameAction); //OBS no initial action?
      NonDetermineChessActionFunction ndeterActionfn =new NonDetermineChessActionFunction();
      NondeterministicChessProblem nondeterProblem = new NondeterministicChessProblem(nondeterminState,ndeterActionfn,ndeterRFN,gameTest); // No step cost function !!
      /*
       * The structure for And or search chapter 4.    
       * A nondeterministic environment. 
       */
      AndOrChessSearch andorSearch = new AndOrChessSearch();
      ChessPath path = new ChessPath();
      Optional<ChessPlan> aPlan = andorSearch.search(nondeterProblem) ; // A nondeterministic environment. 
      ChessPlan thisPlan = aPlan.isPresent() ? aPlan.get() : null;
/*
 * The population of gamestates ends up in the ifstatements of the plan !!
 * At present all game states are the goal state, so there is no plan
 * And the plan contains the GroundState with WhitePawn 6 - The first move
 */
      if (thisPlan != null) {
   	    writer.println("The plan - ");
        writer.println(thisPlan.toString());
      }else {
    	  writer.println("No plan - ");
      }
  
// The optional astate object refers to the same object as plannerState and localPlanner       
//	  thePerceptor.createLiftedActions(null,"WhiteKnight2","f3",null); // Creates the initial and goal states based on this action schema
	  initialState = thePerceptor.getInitState();
	  goalState = thePerceptor.getGoalState();
	  otherSchemas = thePerceptor.getOtherSchemas();
/*
 * The above states and schemas are necessary for creating the ChessProblem for the planning graph 	  
 */
	  otherSchemaList = thePerceptor.getOtherActions();
 /* 
  * This astate object must contain the initial and goal states used when creating the ChessProblem used in the graphplan algorithm.     
  */
      String pieceKey = null;
      List<AgamePiece>  pieces = myPlayer.getMygamePieces();
      if(localPlanner.isCastling()) {
 //   	  localPlanner.PerformKingCastling();
    	  String kingpieceName = "WhiteKing";
    	  String kingPos = "e1";
    	  State goal = null;
    	  State initstate = null;
		  String castlePos = "g1";
		  String piecepos = kingpieceName+"_"+castlePos;
		  typeofPiece = KING;
		  moveName = "kingmove";
		  ActionSchema kingAction = makeActionSchemas(kingpieceName, piecepos, kingPos,castlePos);
		  writer.println("The King castle action check - ");
		  writer.println(kingAction.toString());
		  ChessActionImpl naction =  (ChessActionImpl) actions.stream().filter(c -> c.getActionName().contains(kingpieceName)).findAny().orElse(null);
		  castleAction = naction;
		  Position toCastlePos = positions.get(castlePos);
		  castleAction.setPreferredPosition(toCastlePos);
		  goal = buildGoalstate(kingpieceName,castlePos);
		  String bishopName = "WhiteBishop2";// Rewrite: must find player's bishop
		  String fpos = "f1";
		  String toPos = "d3";
		  typeofPiece = BISHOP;
		  moveName = "bishopmove";
		  initstate = buildInitialstate(bishopName,fpos, toPos);
		  deferredInitial = initstate;
		  deferredGoal = goal;
		  deferredGoalstates.put(kingpieceName, goal);
		  game.setDeferredGoalstates(deferredGoalstates);
		  game.setDeferredGoal(deferredGoal);
		  game.setDeferredInitial(deferredInitial);	
		  if (deferredInitial != null && deferredGoal != null) {
			  writer.println("Castling - Deferred initial and goal states\n");
		      for (Literal literal :
		    	  deferredInitial.getFluents()) {
		    	 writer.println(literal.toString());
		      }
		      writer.println("Castling - Deferred goal state\n");
		      for (Literal literal :
		    	  deferredGoal.getFluents()) {
		    	 writer.println(literal.toString());
		      }		      
		  }
      }
/*
 * Added 10.12.22 
 * The APerceptor object checks for a percept action to see if it
 * is applicable in an initial state s. That is if the precondition of the percept action is satisfied by s.
 * Then s is the returned chosenInitstate      
 */
      if (chosenInitstate != null) { // This structure is never executed !!
          for (Map.Entry<String,State> entry:initStates.entrySet()) {
        	  State thisState = entry.getValue();
        	  String theKey = entry.getKey();
        	  writer.println("The key in list "+theKey+" the piecename key "+pieceName);
        	  if (thisState.equals(chosenInitstate)) {
        		  pieceKey = entry.getKey();
        		  writer.println("Key of chosen init state "+pieceKey);
        		  if (pieceKey.equals(pieceName)) {
        			  break;
        		  }
        	  }
          }
      }// The above structure is never executed !!

	
/*
 * Planning graphs p. 379.
 * S0 - A0 - S1
 * A0 contains ground actions that might be applicable in S0
 * S0 consists of nodes representing fluents that holds in S0	  
 */
	  ChessProblem differentProblem = new ChessProblem(initialState,goalState,otherSchemas);
	  ChessGraphPlanAlgorithm graphplan = new ChessGraphPlanAlgorithm(); // Added graphplan 8.3.23
//	  List<List<ActionSchema>> solutions = graphplan.graphPlan(problem);
	  List<List<ActionSchema>> solutions = graphplan.graphPlan(differentProblem);
	  if (solutions != null && !solutions.isEmpty()) {
		  for (List<ActionSchema> solutionschemas:solutions) {
			  writer.println("Solutions from graphplan - levelled off at "+graphplan.getTheLevel());
			  writer.println("The levels \n"+ graphplan.getTheChesslevel().printLevelObject());
			  for (ActionSchema asolution:solutionschemas) {
				  String solutionName = asolution.getName();
				  if (!solutionName.equals("No-op")) {
					  theSolution = asolution;
					  writer.println("The solution name "+solutionName+"\n"+asolution.toString());
					  List<Constant>solConstants = theSolution.getConstants();
					  for (Constant constant:solConstants) {
						  writer.println(constant.getSymbolicName());
						  String symName = constant.getSymbolicName();
						  AgamePiece gpiece =  (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(symName)).findAny().orElse(null);
						  if (gpiece != null) {
							 
							  break;
						  }
					  }
				  }
			  }
		  }
	  }else {
		  writer.println("No solutions from graphplan\n");
	  }

	  writer.flush();
//	  return problem;
	  return differentProblem;
  }
  /**
 * searchProblem
 * For every available chessaction that contains a possible move and that is not blocked
 * create an actionschema.
 * These action schemas are held in the Map actionSchemas with the name of the piece as the key.
 * With every action schema there is an init state and a goal state. These are held in the maps initStates and goalStates
 * @Since 17.12.21
 * Preconditions and Effects are populated with Constants, the given piecename, posname
 * @since June 25 - OBS: Removed since 26.01.26 
 * Creates a GameState list from actionschemas
 * @param actions
 * @return a List of ActionSchemas
 */
  public List<ActionSchema> searchProblem(ArrayList<ChessActionImpl> actions) {
	  List<ActionSchema> schemas = new ArrayList<ActionSchema>();
	//  List<AgamePiece> pieces = myPlayer.getMygamePieces();
	  for (ChessActionImpl action:actions) {
			if (action.getPossibleMove()!= null && !action.isBlocked()) {
				determineParameters(action);
				List<Position> availablePos = action.getAvailablePositions();
				List<Position> removedPos = action.getPositionRemoved();
				String newPos = action.getPossibleMove().getToPosition().getPositionName(); // Get newPos from Preferred position ??!!
//				String newPos = action.getPreferredPosition().getPositionName(); !!!
				String pieceName = action.getChessPiece().getMyPiece().getOntlogyName(); // Ontology name of piece
				AgamePiece actionPiece = action.getChessPiece();
				Position position = action.getChessPiece().getHeldPosition();
				if (position == null) {
					position = action.getChessPiece().getMyPosition();
				}
				String posName = position.getPositionName();
				String actionName = action.getActionName()+"_"+newPos;
// OJN 28.05.24: To return a list of init states for this piece:				
				State localinitialState = buildInitialstate(pieceName,posName,newPos);// Ontology name of piece, name of occupied position
				State localgoalState = buildGoalstate(action);
				if (pieceName.equals("WhiteKing")) {
					writer.println("=== Creating action schema for ==== "+pieceName);
					writer.println("Goal state for "+pieceName);
					for (Literal literal :
						localgoalState.getFluents()) {
						writer.println(literal.toString());
					}
					writer.println("Init state for "+pieceName);
					for (Literal literal :
						localinitialState.getFluents()) {
						writer.println(literal.toString());
					}
				}
				String apiecepos = pieceName+"_"+newPos;
				initStates.put(apiecepos, localinitialState);
				goalStates.put(apiecepos, localgoalState);
				ActionSchema movedAction = makeActionSchemas(pieceName, actionName, posName, newPos);
				if (movedAction!= null) {
					schemas.add(movedAction);
//					GroundGameState gameState = new GroundGameState(actionPiece,movedAction); // Added june 25 OJNRemoved as of 26.01.26
//					gameStateList.add(gameState);
				}else
					writer.println("No action schema for "+pieceName);
	//			AgamePiece gpiece =  action.getChessPiece();
				for (Position apos:availablePos) {
					String aposName = apos.getPositionName();
					Position pos =  (Position) removedPos.stream().filter(c -> c.getPositionName().contains(aposName)).findAny().orElse(null);
					if (pos == null) {
						State anotherinitialState = buildInitialstate(pieceName,posName,aposName); //This is the same initial state
						State anothergoalstate = buildGoalstate(pieceName, aposName);
						String piecepos = pieceName+"_"+aposName;
						initStates.put(piecepos, anotherinitialState);//The same initial state with a different key
						goalStates.put(piecepos, anothergoalstate);
						ActionSchema anotherActionschema = makeActionSchemas(pieceName, piecepos, posName, aposName);
//Parameters: Ontology name of piece, Ontology name of piece+ name of available position,name of occupied position,name of available position
						if (anotherActionschema!= null) {
							schemas.add(anotherActionschema);
							/*
							 * GroundGameState gameState = new
							 * GroundGameState(actionPiece,anotherActionschema); // Added june 25 OJN
							 * Removed 26.01.26 gameStateList.add(gameState);
							 */
						}
					}
				}
				if (typeofPiece.equals(PAWN)) {
					 HashMap<String,Position>attackpos=  action.getChessPiece().getAttackPositions();
					 Collection<Position> attackCollection = attackpos.values();
					 ArrayList<Position> attackList = new ArrayList<>(attackCollection);
					 for (Position apos:attackList) {
							String aposName = apos.getPositionName();
							Position pos =  (Position) removedPos.stream().filter(c -> c.getPositionName().contains(aposName)).findAny().orElse(null);
							if (pos == null) {
								State anotherinitialState = buildInitialstate(pieceName,posName,aposName);
								State anothergoalstate = buildGoalstate(pieceName, aposName);
								String piecepos = pieceName+"_"+aposName;
								initStates.put(piecepos, anotherinitialState);
								goalStates.put(piecepos, anothergoalstate);
								ActionSchema anotherActionschema = makeActionSchemas(pieceName, piecepos, posName, aposName);
								if (anotherActionschema!= null) {
									schemas.add(anotherActionschema);
//									GroundGameState gameState = new GroundGameState(actionPiece,anotherActionschema); // Added june 25 OJN Removed 26.01.26
//									gameStateList.add(gameState);
								}
							}
					 }
				}
		
			}
	  }
	  return schemas;
  }
  /**
 * makeActionSchemas
 * This method creates an action schema based on a chess action.
 * @param pieceName Name of piece
 * @param actionName Name of action (pieceName+"_"+toPosit)
 * @param posName Starting position of piece
 * @param toPosit Available position to move to
 * @return
 */
private ActionSchema makeActionSchemas(String pieceName,String actionName,String posName,String toPosit){
	  ArrayList<String>reachparentNames = (ArrayList<String>) folKb.searchFacts("x", toPosit, PROTECTED);
	  List<AgamePiece> pieces = myPlayer.getMygamePieces();
	  List<Term> protectorTerms = new ArrayList();
	  String piecepos = pieceName+"_"+toPosit;
	  Constant piece = new Constant(pieceName);
	  Constant pos = new Constant(posName);
	  Constant toPos = new Constant(toPosit);
	  Constant type = new Constant(typeofPiece);
	  //		Variable ownerVar = new Variable("owner");
//	  Constant ownerVar = new Constant(playerName);
	  //		List variables = new ArrayList<Variable>(Arrays.asList(piece,pos,toPos));
	  List variables = new ArrayList<Constant>(Arrays.asList(piece,pos,toPos));
	  List<Term> terms = new ArrayList<Term>();
	  List<Term> attackTerms = new ArrayList<Term>();
	  List<Term> ownerterms = new ArrayList<Term>();
	  List<Term> newterms = new ArrayList<Term>();
	  List<Term> typeTerms = new ArrayList<Term>();
	  List<Term> castlingTerms = null;
	  List<Term> boardTerms = new ArrayList<Term>();
	  Predicate protectedBy = null;
	  Constant protector = null;
	  String protectorName = null;
	  //		Constant toPos = new Constant(toPosit);
	  List<Literal> precondition = new ArrayList();
	  List<Literal> effects = new ArrayList();
	  boolean protectorFlag = false;
	  boolean attackFlag = false;
	  if (reachparentNames != null && !reachparentNames.isEmpty() ) { // A list of piece names that protect a given position
		  int psize = reachparentNames.size();
		  for (int i = 0;i<psize;i++) {
			  protectorName = reachparentNames.get(i);
			  String p = protectorName;
			  AgamePiece gpiece =  (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(p)).findAny().orElse(null);
			  if (gpiece != null &&!pieceName.equals(protectorName)) {
				  protector = new Constant(protectorName);
				  //					Variable protector = new Variable("x"); //Cannot use Variable in preconditions and effects?
				  protectorTerms.add(protector);
				  protectorTerms.add(toPos);
				  protectedBy = new Predicate(PROTECTED,protectorTerms);
				  if (!typeofPiece.equals(PAWN)) {
					  precondition.add(new Literal((AtomicSentence) protectedBy));
					  variables.add(protector);
					  protectorFlag = true;
				  }
				  protectorTerms.clear();
				  if (typeofPiece.equals(PAWN)) {
					  AgamePiece pawnpiece =  (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(pieceName)).findAny().orElse(null);
					  if (pawnpiece != null) {
						  HashMap<String,Position>attackpos=  pawnpiece.getAttackPositions();
						  Collection<Position> attackCollection = attackpos.values();
						  ArrayList<Position> attackList = new ArrayList<>(attackCollection);
						  for (Position apos:attackList) { // Must also check if an opponent piece can be taken !!
								String aposName = apos.getPositionName();
								String occupant= null;
								AgamePiece ownpiece = null;
								 attackFlag = true;
								ArrayList<String>occupier = (ArrayList<String>)folKb.searchFacts("x", aposName, OCCUPIES);
								if (!occupier.isEmpty()) {
									occupant = occupier.get(0);
									String lp = occupant;
									ownpiece =  (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(lp)).findAny().orElse(null);
								}
								List<Position> removedPos = pawnpiece.getRemovedPositions();
								Position pawnpos =  (Position) removedPos.stream().filter(c -> c.getPositionName().contains(aposName)).findAny().orElse(null);
								if (pawnpos == null && ownpiece == null && occupant != null) {
									 Constant pawnConstant = new Constant(aposName);
									 attackTerms.add(piece);
									 attackTerms.add(pawnConstant);
								}
						  }
					  }

				  }
				  if (typeofPiece.equals(KING)&& moveName.equals(theCastling)) {
					  castlingTerms = new ArrayList<Term>();
					  castlingTerms.add(piece);
					  castlingTerms.add(toPos);
					  
				  }
			  }
		  } // End for all piece names that protect a given position

	  } // end if protector names
//	  ownerterms.add(ownerVar);
//	  ownerterms.add(piece);
	  terms.add(piece);
	  terms.add(pos);
	  boardTerms.add(pos);
	  newterms.add(piece);
	  newterms.add(toPos);
	  typeTerms.add(piece);
	  typeTerms.add(type);
	  Predicate typePred = new Predicate(PIECETYPE,typeTerms);
	  Predicate reachablePredicate = new Predicate(REACHABLE,newterms);
	  Predicate boardPredicate = new Predicate(BOARD,boardTerms);
	  Predicate attackPredicate = null;
	  if (!attackTerms.isEmpty()) {
		  attackPredicate = new Predicate(PAWNATTACK,attackTerms);
	  }
	  Predicate pospred = new Predicate(OCCUPIES,terms);
	  //		Predicate ownerPred = new Predicate(OWNER,ownerterms);
	  Predicate newPospred = new Predicate(OCCUPIES,newterms);
/*	  precondition.add(new Literal((AtomicSentence) pospred));
	  precondition.add(new Literal((AtomicSentence) boardPredicate)); // The board predicate is added to the precondition
	  precondition.add(new Literal((AtomicSentence) typePred)); // The type predicate is moved in position before the reachable predicate
*/	  if (attackPredicate != null) {
		  precondition.add(new Literal((AtomicSentence) attackPredicate));
		  precondition.add(new Literal((AtomicSentence) pospred));
		  precondition.add(new Literal((AtomicSentence) boardPredicate)); // The board predicate is added to the precondition
		  precondition.add(new Literal((AtomicSentence) typePred)); // The type predicate is moved in position before the reachable predicate
		  effects.add(new Literal( (AtomicSentence)newPospred));
		  //		effects.add(new Literal( (AtomicSentence)ownerPred));
		  effects.add(new Literal( (AtomicSentence)typePred));
	  }
	  if (!attackFlag ) { // To prevent pawn attack position to become a reachable action schema OJN 31.05.24
		  precondition.add(new Literal((AtomicSentence) reachablePredicate));
		  precondition.add(new Literal((AtomicSentence) pospred));
		  precondition.add(new Literal((AtomicSentence) boardPredicate)); // The board predicate is added to the precondition
		  precondition.add(new Literal((AtomicSentence) typePred)); // The type predicate is moved in position before the reachable predicate
		  effects.add(new Literal( (AtomicSentence)newPospred));
		  //		effects.add(new Literal( (AtomicSentence)ownerPred));
		  effects.add(new Literal( (AtomicSentence)typePred));
	  }
	  Predicate castlingPredicate = null;
	  if (castlingTerms != null) {
		  castlingPredicate = new Predicate(CASTLE,castlingTerms);
		  precondition.add(new Literal((AtomicSentence) castlingPredicate));
	  }
	
	  //		Literal notAt = new Literal(pospred, true);
	  //		effects.add(notAt);
	  ActionSchema movedAction = null;
	  if (!precondition.isEmpty()) {
		  movedAction = new ActionSchema(actionName,variables,precondition,effects);
		  actionSchemas.put(piecepos, movedAction);
	  }
	
	  

	  //		schemas.add(movedAction);
	  return movedAction;
  }
/**
 * buildGoalstate
 * This method builds a goal state based on a ChessAction
 * It is called from the method searchProblem
 * @param action
 * @return
 */
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
   * This method creates an initial state for a problem 
   * with a given piece name and posname
   * @since 09.08.22
   * The object variable theState contains all Literals of the initial state
   * @since 28.05.24
   * OJN 28.05.24: To return a list of init states for the piece:	
   * @param piece - ontology name of piece
   * @param posName - held position
   * @param newPos - reachable position - added 25.05.24 OJN
   * @return
   */
  public State  buildInitialstate(String piece,String posName,String newPos) {
		List<Sentence> folSentences = folKb.getOriginalSentences();
		State initState = null;
		String pieceName = null;
		String owner = null;
		List<String> reachablePos = new ArrayList<String>();
		List<Literal> literals = new ArrayList();
		List<String> attackablePos = new ArrayList<String>();
		List<String> castlePos = new ArrayList<String>();
		List<State> states = new ArrayList<State>();
		boolean reachable = false; // added 25.05.24 - only one reachable fluent in initial state
		for (Sentence s : folSentences) {
			String symName = s.getSymbolicName();
			List<Literal> localLiterals = new ArrayList();
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
					localLiterals.add(l);
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
/*					Literal l = new Literal((AtomicSentence) s);
					literals.add(l);
					localLiterals.add(l);*/
					reachablePos.add(pos);
					Constant posVar = new Constant(pos);
					Constant pieceC = new Constant(p);
					List<Term> boardTerms = new ArrayList<Term>();
					List<Term> reachTerms = new ArrayList<Term>();
					boardTerms.add(posVar);
					reachTerms.add(pieceC);
					reachTerms.add(posVar);
					Predicate boardPredicate = new Predicate(BOARD,boardTerms);
					Predicate reachPredicate = new Predicate(REACHABLE,reachTerms);
					Literal boards = new Literal((AtomicSentence)boardPredicate);
					Literal reaches = new Literal((AtomicSentence)reachPredicate);
					literals.add(boards); 
					localLiterals.add(boards);
					if (!reachable)
						localLiterals.add(reaches);
					reachable = true;
					if (pos.equals(newPos))
						literals.add(reaches);
					
				}
			}
			if (symName.equals(PIECE)) { // Added new initial state fluent : The piece name as PIECE OJN 18.05.24
				List<Term> terms = (List<Term>) s.getArgs();
				ArrayList<Term> literalTerms = new ArrayList<>();
				Term f = terms.get(0);
				String p = f.getSymbolicName();
				if (p.equals(piece)) {
					Literal l = new Literal((AtomicSentence) s);
					literals.add(l);
					localLiterals.add(l);
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
					localLiterals.add(l);
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
					localLiterals.add(l);
				}
			} //The board fluent is removed OJN 7.05.24
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
					localLiterals.add(boards);
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
					Constant pieceC = new Constant(p);
					List<Term> reachTerms = new ArrayList<Term>();
					reachTerms.add(pieceC);
					reachTerms.add(posVar);
					Predicate reachPredicate = new Predicate(REACHABLE,reachTerms);
					List<Term> boardTerms = new ArrayList<Term>();
					Literal reaches = new Literal((AtomicSentence)reachPredicate);
					boardTerms.add(posVar);
					Predicate boardPredicate = new Predicate(BOARD,boardTerms);
					Literal boards = new Literal((AtomicSentence)boardPredicate);
					literals.add(boards);
					literals.add(reaches);
					localLiterals.add(boards);
					localLiterals.add(reaches);
				}
			}
			State localState = null;
			if (!localLiterals.isEmpty()) {
				localState = new State(localLiterals);
				states.add(localState);
			}
			initialStates.add(states);
			states.clear();
			reachable = false;
		} // end for all sentences in knowledge base
		stateLiterals.addAll(literals);
		initState = new State(literals);
		
		return initState;
		
  }
}
