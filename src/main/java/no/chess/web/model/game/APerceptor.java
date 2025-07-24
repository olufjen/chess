package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import aima.core.logic.fol.kb.data.Literal;
import aima.core.logic.fol.parsing.ast.AtomicSentence;
import aima.core.logic.fol.parsing.ast.Constant;
import aima.core.logic.fol.parsing.ast.Predicate;
import aima.core.logic.fol.parsing.ast.Term;
import aima.core.logic.fol.parsing.ast.Variable;
import aima.core.logic.planning.ActionSchema;
import aima.core.logic.planning.State;
import no.chess.web.model.ChessPiece;
import no.chess.web.model.Position;
import no.games.chess.planning.PerceptSchema;
import no.games.chess.search.nondeterministic.ChessPercept;
import no.games.chess.ChessPieceType;
import no.games.chess.ChessVariables;
import no.games.chess.GamePiece;

/**
 * This class creates percept schemas 
 * From page 416 in the AIMA book:
 * - To solve a partially observable problem, the agent will have to 
 * reason about the percepts it will obtain when it is executing a plan.. -
 * When it is planning we create a percept schema with a precondition of the form:
 * REACHABLE(x,POS)^PIECETYPE(x,TYPE)
 * which says Is there a piece that can reach position POS and is of Type TYPE)?
 * If that is the case then we choose this piece for the next move.
 * @since 04.03.23
 * An enhancement of the perceptor:
 * Given a position:
 * Find all type of pieces that can reach this position.
 * This is recorded in the knowledge base in the form:
 * BISHOP(h7,g8) - which says that the position h7 is reachable from g8 by a BISHOP
 * See the method findReachable
 * @since 24.07.24
 * Creates a set of propositionalized action schemas from the lifted action schema
 * These action schemas are used as a basis for problem solving see the method createLiftedActions
 * 
 */
public class APerceptor implements ChessPercept {
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
	  private String PAWNMOVE;
	  private String PIECE = "PIECE"; 
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
	  
	  private APlayer myPlayer = null; // The player of the game
	  private APlayer opponent = null; // The opponent of the game
	  private ChessFolKnowledgeBase folKb = null; // The parent knowledge base
	  private ChessFolKnowledgeBase localKb = null; // The strategy knowledge base
	  private OpponentAgent agent;
	  private PerceptSchema percept;
	  private String reaches; // The REACHABLE predicate - could be any defined predicate
	  private String types;	// CHESSTYPE predicate - could be any defined predicate
	  private Variable pieceName;
	  private Variable pieceType;
	  private Position pos; // A position under investigation, a position of interest to reach/occupy 
	  private Constant topos;
	  private Constant typePiece;
	  private List<Literal> precondition = null;
	  private List<Term> typeTerms = null;
	  private List<Term> reachTerms = null;
	  private List<Term> variables = null;
	  private Predicate reachPredicate = null; // could be any defined predicate 
	  private Predicate typePredicate = null;	// could be any defined predicate
	  private List<String> mypieceNames = null; // The name of the pieces available to player
	  private AgamePiece playerPiece = null; // A possible piece to use: which chess piece is applicable to the chosen state
	  
	  private PrintWriter writer =  null;
	  private String outputFileName =  "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\perceptor.txt";
	  private FileWriter fw =  null;
	  private List<List> initialStates = null; // A list of initial states 
	  private Map<String,State>initStates = null; // Contains all initial states for current move
	  private Map<String,State>goalStates = null; // Contains all goal states for current move
	  
	  private State initState = null; // initState found from lifted action schemas
	  private State goalState = null; // goalState found from lifted action schemas
	  private  List<ActionSchema> otherActions = null;// A list of propositionalized action schemas from the lifted action schema
	  private Set<ActionSchema> otherSchemas = null; // A Set of propositionalized action schemas from the lifted action schema
	  private List<State>propinitStates = null;// Init states for the set of propositionalized action schemas from the lifted action schema
	  private List<State>propgoalStates = null;// Goal states for the set of propositionalized action schemas from the lifted action schema
	  
	  private Map<String,AgamePiece>possiblePieces = null; // Contains opponent pieces that can be taken. New key: name of piece of player + position of the piece that can be taken
	  private Map<String,Position>possiblePositions = null; // Contains the positions of these opponent pieces. New key: name of piece of player + position of the piece that can be taken	  
	  private Map<String,AgamePiece>threatenedPieces = null; // Contains pieces that are threatened by the opponent
	  private Map<String,Position>threadenedPositions = null; // Contains the positions of these pieces.
	  private Map<String,ArrayList<AgamePiece>>protectors = null; // Contains pieces that protect other pieces. The key is the name of the piece that they protect.
	  private Map<String,ArrayList<AgamePiece>>attackers = null; // Contains opponent pieces that can capture a piece. The key is the name of the piece that they can capture.

	  /**
	   * This is the simple constructor of the perceptor
	 * @param playerName
	 */
	public APerceptor(String playerName) {
		  super();
		  setPredicatenames();
		  this.playerName = playerName;
		  ChessVariables.setPlayerName(playerName);
		  mypieceNames = new ArrayList<String>();
		  propinitStates = new ArrayList<State>();
		  propgoalStates = new ArrayList<State>();
		  try {
			  fw = new FileWriter(outputFileName, true);
		  } catch (IOException e1) {

			  e1.printStackTrace();
		  }
		  writer = new PrintWriter(new BufferedWriter(fw));	
	  }
	/*
	   * In order to use any defined predicate, the predicates must have two terms
	   */
	  /**
	   * This constructor creates a percept schema as described in AIMA chapter 11 p. 416.
	   * 
	 * @param posin A position under investigation - of interest to reach/occupy
	 * @param reach A predicate name
	 * @param type The type predicate
	 * @param typeofPiece
	 * @param playerName
	 */
	public APerceptor(Position posin,String reach,String type,String typeofPiece,String playerName) {
		  super();
		  setPredicatenames();
		  this.playerName = playerName;
		  ChessVariables.setPlayerName(playerName);
		  mypieceNames = new ArrayList<String>();
		  pos = posin;
		  String posname = pos.getPositionName();
		  reaches = reach;
		  types = type;
		  pieceName = new Variable("piecename");
		  typeTerms = new ArrayList<Term>();
		  typeTerms.add(pieceName); // Two terms for the piecetype predicate
		  if (typeofPiece == null) {
			  pieceType = new Variable("type");
			  typeTerms.add(pieceType);
		  }else {
			  typePiece = new Constant(typeofPiece);
			  typeTerms.add(typePiece);
		  }
		  topos = new Constant(posname);
		  precondition = new ArrayList();
		  reachTerms = new ArrayList<Term>();
		  variables = new ArrayList<Term>();
		  reachTerms.add(pieceName); // Two terms for the reachable predicate
		  reachTerms.add(topos);
		  variables.addAll(reachTerms);
		  variables.addAll(typeTerms);
		  reachPredicate = new Predicate(reaches,reachTerms);
		  typePredicate = new Predicate(types,typeTerms);
		  precondition.add(new Literal((AtomicSentence) reachPredicate));
		  precondition.add(new Literal((AtomicSentence)typePredicate));
		  percept = new PerceptSchema("MOVE",variables,precondition);
		  propinitStates = new ArrayList<State>();
		  propgoalStates = new ArrayList<State>();
		  try {
			  fw = new FileWriter(outputFileName, true);
		  } catch (IOException e1) {

			  e1.printStackTrace();
		  }
		  writer = new PrintWriter(new BufferedWriter(fw));	
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

	  public Map<String, AgamePiece> getThreatenedPieces() {
		  return threatenedPieces;
	  }
	  public void setThreatenedPieces(Map<String, AgamePiece> threatenedPieces) {
		  this.threatenedPieces = threatenedPieces;
	  }
	  public Map<String, Position> getThreadenedPositions() {
		  return threadenedPositions;
	  }
	  public void setThreadenedPositions(Map<String, Position> threadenedPositions) {
		  this.threadenedPositions = threadenedPositions;
	  }
	  public Map<String, ArrayList<AgamePiece>> getProtectors() {
		  return protectors;
	  }
	  public void setProtectors(Map<String, ArrayList<AgamePiece>> protectors) {
		  this.protectors = protectors;
	  }
	  public Map<String, ArrayList<AgamePiece>> getAttackers() {
		  return attackers;
	  }
	  public void setAttackers(Map<String, ArrayList<AgamePiece>> attackers) {
		  this.attackers = attackers;
	  }
	  public List<ActionSchema> getOtherActions() {
		  return otherActions;
	  }
	  public void setOtherActions(List<ActionSchema> otherActions) {
		  this.otherActions = otherActions;
	  }
	  public Set<ActionSchema> getOtherSchemas() {
		  return otherSchemas;
	  }
	  public void setOtherSchemas(Set<ActionSchema> otherSchemas) {
		  this.otherSchemas = otherSchemas;
	  }
	  public State getInitState() {
		  return initState;
	  }
	  public void setInitState(State initState) {
		  this.initState = initState;
	  }
	  public State getGoalState() {
		  return goalState;
	  }
	  public void setGoalState(State goalState) {
		  this.goalState = goalState;
	  }
	  public List<List> getInitialStates() {
		  return initialStates;
	  }
	  public void setInitialStates(List<List> initialStates) {
		  this.initialStates = initialStates;
	  }
	  public Map<String, State> getGoalStates() {
		  return goalStates;
	  }
	  public void setGoalStates(Map<String, State> goalStates) {
		  this.goalStates = goalStates;
	  }
	  public OpponentAgent getAgent() {
		  return agent;
	  }
	  
	  public Map<String, State> getInitStates() {
		return initStates;
	}
	public void setInitStates(Map<String, State> initStates) {
		this.initStates = initStates;
	}
	public void setAgent(OpponentAgent agent) {
		  this.agent = agent;
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
	  

	  public ChessFolKnowledgeBase getFolKb() {
		  return folKb;
	  }
	  public void setFolKb(ChessFolKnowledgeBase folKb) {
		  this.folKb = folKb;
	  }
	  public ChessFolKnowledgeBase getLocalKb() {
		  return localKb;
	  }
	  public void setLocalKb(ChessFolKnowledgeBase localKb) {
		  this.localKb = localKb;
	  }
	  public PerceptSchema getPercept() {
		  return percept;
	  }
	  public void setPercept(PerceptSchema percept) {
		  this.percept = percept;
	  }
	  public Variable getPieceName() {
		  return pieceName;
	  }
	  public void setPieceName(Variable pieceName) {
		  this.pieceName = pieceName;
	  }
	  public Variable getPieceType() {
		  return pieceType;
	  }
	  public void setPieceType(Variable pieceType) {
		  this.pieceType = pieceType;
	  }
	  public Position getPos() {
		  return pos;
	  }
	  public void setPos(Position pos) {
		  this.pos = pos;
	  }
	  
	  public AgamePiece getPlayerPiece() {
		return playerPiece;
	}
	public void setPlayerPiece(AgamePiece playerPiece) {
		this.playerPiece = playerPiece;
	}
	
	public Map<String, AgamePiece> getPossiblePieces() {
		return possiblePieces;
	}
	public void setPossiblePieces(Map<String, AgamePiece> possiblePieces) {
		this.possiblePieces = possiblePieces;
	}
	public Map<String, Position> getPossiblePositions() {
		return possiblePositions;
	}
	public void setPossiblePositions(Map<String, Position> possiblePositions) {
		this.possiblePositions = possiblePositions;
	}
	/**
	 * checkOpponentthreat
	 * This method returns a list of opponent pieces that threaten a given position
	 * @param pieceName
	 * @param pos
	 * @param fact
	 * @return a list of opponent pieces or null
	 */
	public List<GamePiece> checkOpponentthreat(String pieceName,String pos,String fact){
		boolean threats = folKb.checkThreats(pieceName, pos, fact, opponent);
		if (!threats)
			return null;
		List<AgamePiece> pieces = folKb.getMovePieces();
		List<GamePiece> gamePieces = new ArrayList<GamePiece>(pieces);
		return gamePieces;
	}
	/**
	   * checkPercept
	   * This method checks the generated percept action against all available initial states.
	   * The initial states have only ground atoms.
	   * The percept action is applicable in state s if the precondition of the percept action is satisfied by s.
	   * @since 15.03.24
	   * This method also determines which chess piece is applicable to the chosen state
	   * @param initStates all initial states
	   * @return the chosen state or null
	   */
	  public State checkPercept( Map<String,State>initStates) {
		  List<AgamePiece> pieces = myPlayer.getMygamePieces();
		  List<State> allStates = new ArrayList<State>(initStates.values());
		  List<Constant> stateconstants = new ArrayList<Constant>();
		  List<Literal> preconditions = percept.getPrecondition();
		  for (State state:allStates) {
			  List<Literal> literals = state.getFluents(); // The initial states have only ground atoms
			  for (Literal lit:literals) {
				  Predicate p = (Predicate) lit.getAtomicSentence();
				  List<Term> terms = p.getTerms(); // Must find the correct type of constants, piece piecetype etc!!
				  for (Term t:terms) {
					  Constant c = (Constant)t;
					  if (!stateconstants.contains(t))
						  stateconstants.add(c);
				  }
			  }
			  PerceptSchema conPercept = percept.getActionBySubstitution(stateconstants); // Create a percept with only ground atoms
			  boolean found = state.getFluents().containsAll(conPercept.getPrecondition());//is applicable in state s if the precondition of the percept action is satisfied by s.
			  if (found) {
				  for (Constant c:stateconstants) {
					  String cn = c.getValue();
					  AgamePiece thepiece = (AgamePiece) pieces.stream().filter(p -> p.getMyPiece().getOntlogyName().contains(cn)).findAny().orElse(null);
					  if (thepiece != null) {
						  playerPiece = thepiece;
						  break;
					  }
				  }
			  }
			  stateconstants.clear();
			  if (found) {
				  return state;
			  }
		  }
		  return null;
	  }
	  /**
	   * findReachable
	   * This method uses the method createKnowledgefacts
	   * to create facts of the following form to the parent and strategy knowledge base:
	   * piecetype(posa,posb) as: The parent knowledge base: PAWN(d4,d2). The PAWN can reach d4 from d2 and d2 is the current position 
	   * of the PAWN.
	   * In the strategy knowledge base these facts are of the form: PAWN(e5,d4) and BISHOP(a7,d4)
	   * The PAWN can strike e5 from d4 and the BISHOP can reach a7 from d4
	 * @param position is the position under investigation (d4) This position is only used for the strategy knowledge base
	 */
	public void findReachable(Position position) {
		  List<AgamePiece> pieces = myPlayer.getMygamePieces();
		  List<String>thepieceNames = myPlayer.getPieceNames();
		  List<String> pieceNames = new ArrayList<String>();
		  for (String fullName:thepieceNames) {
			  String apieceName = KnowledgeBuilder.extractString(fullName, '#',-1);
			  pieceNames.add(apieceName);
			  mypieceNames.add(apieceName);
		  }
		  for (String apieceName:pieceNames) {
			  AgamePiece thepiece = (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(apieceName)).findAny().orElse(null);
			  createKnowledgefacts(thepiece, position);
		  }

	  }
	  /**
	   * createKnowledgefacts
	   * This method creates facts to the strategy and parent knowledge bases 
	   * as described in the findReachable method.
	 * @param piece The piece under investigation
	 * @param position The position under investigation. This position is only used for the strategy knowledge base. 
	 * @since 25.11.24
	 * The position may be null.
	 */
	private void createKnowledgefacts(AgamePiece piece,Position position) {
		  ChessPiece chessPiece = piece.getMyPiece();
		  String newPosname = null;
		  if (position != null)
			  newPosname = position.getPositionName();
		  ABishop bishop = null;
		  ARook rook = null;
		  AKnight knight = null;
		  AQueen queen = null;
		  Aking king = null;
		  APawn pawn = null; 
		  String predicate = null;
		  HashMap<String,Position> reachables = null;

		  HashMap<String,Position>piecereachables = piece.getReacablePositions();
		  List<Position>removed = piece.getRemovedPositions();
		  Position piecePosition = piece.getmyPosition();
		  String piecePosname = piecePosition.getPositionName();
		  if (piece.getMybishop() != null) {
			  if (position != null) {
				  bishop = new ABishop(position,chessPiece);
				  reachables = bishop.getLegalmoves();
			  }
			  predicate = agent.getBISHOP();
		  }
		  if (piece.getMyrook() != null) {
			  if (position != null) {
				  rook = new ARook(position,chessPiece);
				  reachables = rook.getLegalmoves();
			  }
			  predicate = agent.getROOK();
		  }
		  if (piece.getMyKnight() != null) {
			  if (position != null) {
				  knight = new AKnight(position,chessPiece);
				  reachables = knight.getLegalmoves(); 
			  }
			  predicate = agent.getKNIGHT();
		  }
		  if (piece.getMyqueen() != null) {
			  if (position != null) {
				  queen = new AQueen(position,chessPiece);
				  reachables = queen.getLegalmoves();
			  }
			  predicate = agent.getQUEEN();
		  }
		  if (piece.getMyKing() != null) {
			  if (position != null) {
				  king = new Aking(position,chessPiece);
				  reachables = king.getLegalmoves();
			  }
			  predicate = agent.getKING();
		  }	 
		  if (piece.getMyPawn() != null) {
			  if (position != null) {
				  pawn = new APawn(position,chessPiece);
				  reachables = pawn.getAttackPositions();
			  }
			  predicate = agent.getPAWN();
		  }
		  if(newPosname != null && reachables != null && !reachables.isEmpty()) {
			  List<Position> reachablelist = new ArrayList<Position>(reachables.values());
			  for (Position pos:reachablelist) {
				  String posName = pos.getPositionName();
				  //				writer.println("Bishop Reachable to Kings position from "+posName + " King at "+opponentKingPosition);
				  localKb.createfacts(predicate,newPosname, posName); // To strategy knowledge base
			  }
		  }
		  if(piecereachables != null && !piecereachables.isEmpty()) {
			  List<Position> reachablelist = new ArrayList<Position>(piecereachables.values());
			  for (Position pos:reachablelist) {
				  String posName = pos.getPositionName();
				  if(removed != null && !removed.isEmpty()) {
					  Position removedPos = (Position) removed.stream().filter(c -> c.getPositionName().contains(posName)).findAny().orElse(null);
					  if(removedPos == null) {
						  folKb.createfacts(predicate,piecePosname, posName); // To parent knowledge base
					  }
				  }
				  if(removed != null && removed.isEmpty()) {
					  folKb.createfacts(predicate,piecePosname, posName); // To parent knowledge base
				  }
				  if(removed == null) {
					  folKb.createfacts(predicate,piecePosname, posName); // To parent knowledge base
				  }
			  }
		  }
	  }

	/**
	 * createLiftedActions
	 * This method creates lifted action schemas based on the names parameters
	 * These lifted action schemas are propositionalized and then a possible initial state and goal state for a Problem is searched for.
	 * @param names. These are given in the following order:
	 * Startpos, Piecename, Newpos, Piecetype, or null. A fifth parameter "pawn" is added signaling a pawn strike, or "castle" to signal a castling
	 */
	public boolean createLiftedActions(String... names) {
		  writer.println("Actions with ");
		  ActionSchema occupy = KnowledgeBuilder.createOccupyaction(names); // Creates a lifted action schema
		  writer.println(occupy.toString());
		  otherActions = KnowledgeBuilder.findApplicable(initStates,occupy); // Returns propositionalized action schemas from the lifted action schemas
		  otherSchemas =  new HashSet<ActionSchema>(otherActions);
		
		  State theInitState = null; // A determined init state for the Problem
	      State theGoal = null;		// A determined goalState for the Problem
		  for (ActionSchema primitiveAction :
			   otherActions) {	 
			  writer.println(primitiveAction.toString()); // This is the propositionalized action schema from the lifted action schema
		  }
		  List<State> allStates = new ArrayList<State>(initStates.values()); // All Init states from all available action schemas
		  List<State> allGoals = new ArrayList<State>(goalStates.values()); // All goal states from all available action schemas
	/*
	 * A procedure to determine a possible initial state
	 */
		  boolean rfound = true;
		  writer.println("New goal states - testing the result function");
		  for (State state:allStates) { // For all the initial states:
			  State agoalState = state.result(otherActions); // Given a list of propostionalized action schema the result function returns a goal state
			  boolean found = state.getFluents().containsAll(agoalState.getFluents()); // Could any of the initial states entail the new goal state?
			  if (!found) { // If not check if the new goal state entails the given goal state
				  writer.println("A possible goal state not entailed by an init state");
			      boolean agree = false;
			      for (State goalstate:allGoals) { // All goal states from all available action schemas
			    	  agree = agoalState.getFluents().containsAll(goalstate.getFluents());// A possible goal state entails the given goal state
			    	  if (agree) {
			    		  theGoal = goalstate; // The goal state has been found
			    		  propgoalStates.add(theGoal);
			    		  break;
			    	  }
			      }
			      if (agree) {
			    	  writer.println("Found the goal state");
				      for (Literal literal :
				    	  agoalState.getFluents()) {
				    	 writer.println(literal.toString());
				      } 
			      }
		      
			      if (agree) { // When this is true we have found the current init state
			    	  writer.println("A possible goal state entails the given goal state");
				      for (Literal literal :
				    	  theGoal.getFluents()) {
				    	 writer.println(literal.toString());
				      }
				      theInitState = state; // The initial state has been found
				      propinitStates.add(theInitState);
				      writer.println("--");
				      writer.println("And the init state is"); // This is then the current initial state
				      for (Literal literal :
				    	  state.getFluents()) {
				    	 writer.println(literal.toString());
				      }
//				      break; //  OBS: This causes error !!!!!! Added 4.02.25 No need to search further when initial and goal states are found?
			      }
			      if (!agree) {
			    	  writer.println("==No goal state or init state found== returning "+found);
			      }
			      rfound = found;
		//		  writer.println(agoalState.toString());  
			  }else {
				  writer.println("No Goal state found "+found);
 /*			      for (Literal literal :
			    	  state.getFluents()) {
			    	 writer.println(literal.toString());
			      }
			      writer.println("For this goal state");*/
				  writer.println("And the next initial state is");
			      for (Literal literal :
			    	  state.getFluents()) {
			    	 writer.println(literal.toString());
			      }
			      boolean agree = false;
/*			      for (State goalstate:allGoals) {
			    	  agree = agoalState.getFluents().containsAll(goalstate.getFluents());
			    	  if (agree) {
			    		  theGoal = goalstate;
			    		  break;
			    	  }
			      } OJN 8.04.25 Do not test possible goal states here !!!*/
			     
			      if (agree) { // Never found !!! ????
			    	  writer.println("A possible goal state entails the given goal state in agreement with init state");
				      for (Literal literal :
				    	  theGoal.getFluents()) {
				    	 writer.println(literal.toString());
				      }
			      }
//			      rfound = found;
//			      break;
			  }

		  } // End for all initial states
		  initState = theInitState;
		  goalState = theGoal;
		  writer.flush();
		  return rfound;
	}

}
