
package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import aima.core.logic.fol.Connectors;
import aima.core.logic.fol.kb.FOLKnowledgeBase;
import aima.core.logic.fol.kb.data.Literal;
import aima.core.logic.fol.parsing.ast.AtomicSentence;
import aima.core.logic.fol.parsing.ast.ConnectedSentence;
import aima.core.logic.fol.parsing.ast.Constant;
import aima.core.logic.fol.parsing.ast.Predicate;
import aima.core.logic.fol.parsing.ast.Term;
import aima.core.logic.fol.parsing.ast.Variable;
import aima.core.logic.planning.ActionSchema;
import aima.core.logic.planning.State;
import aima.core.logic.planning.Utils;
import aima.core.search.framework.Node;
import aima.core.search.framework.problem.ActionsFunction;
import aima.core.search.framework.problem.ResultFunction;
import aima.core.search.framework.problem.StepCostFunction;
import no.function.FunctionContect;
import no.games.chess.AbstractGamePiece.pieceType;
import no.games.chess.planning.ChessPlannerAction;
import no.games.chess.planning.PlannerState;
import no.games.chess.search.ChessGoalTest;
import no.games.chess.search.ChessStepCostImpl;
import no.games.chess.search.nondeterministic.GameAction;
import no.games.chess.search.nondeterministic.GameState;
import no.games.chess.search.nondeterministic.NonDetermineChessActionFunction;
import no.games.chess.search.nondeterministic.NonDetermineResultFunction;

/**
 * KnowledgeBuilder
 * This class contains constants and knowledge base facts to be used in knowledgebases used in a chess game.
 * All these constants act as predicates in first order logic.
 * @since 18.01.22
 * Added two more knowledge base facts: POSSIBLEPROTECT, POSSIBLEREACH
 * All available positions for a piece are possible to protect or possible to reach
 * @since April 2024
 * Added two methods: createOccupyAction - crates an lifted action schema
 * findApplicable - a method that returns possible applicable action schemas given a lifted action schema
 * @author oluf
 * 
 */
public class KnowledgeBuilder {
/*
 * Predicate names	
 */
  private static String ACTION =  "ACTION";
  private static String PLAY =  "PLAY";
  private static String PROTECTED =  "PROTECTEDBY";
  private static String simpleProtected =  "PROTECTED";
  private static String ATTACKED =  "ATTACKEDBY";
  private static String CAPTURE =  "CAPTURE";
  private static String CONQUER =  "CONQUER";
  private static String THREATEN =  "THREATENEDBY";
  private static String OWNER =  "OWNER";
  private static String MOVE =  "MOVE";
  private static String REACHABLE =  "REACHABLE";
  private static String CANMOVE =  "CANMOVE";
  private static String SAFEMOVE =  "SAFEMOVE";
  private static String STRIKE =  "STRIKE";
  private static String PIECETYPE =  "PIECETYPE";
  private static String PIECE = "PIECE";  
  private static String PAWNMOVE =  "PAWNMOVE";
  private static String PAWN =  "PAWN";
  private static String KNIGHT =  "KNIGHT";
  private static String BISHOP =  "BISHOP";
  private static String ROOK =  "ROOK";
  private static String KING =  "KING";
  private static String QUEEN =  "QUEEN";
  private static String OCCUPIES = "occupies";
  private static String PAWNATTACK = "PAWNATTACK";
  private static String BOARD = "BOARD";
  private static String PLAYER = "PLAYER";
  private static String CASTLE = "CASTLE";
  private static String OPPONENTTO = "OPPONENTTO";
  private static String POSSIBLETHREAT = "POSSIBLETHREAT"; // All available positions for a piece are possibly threatened by that piece
  private static String POSSIBLEPROTECT = "POSSIBLEPROTECT"; // All available positions for a piece are possibly protected by that piece
  private static String POSSIBLEREACH = "POSSIBLEREACH"; // All available positions for a piece are possibly reachable by that piece
/*
 * Additional predicate names  
 */
  private static String OCCUPY = "OCCUPY"; // Move to occupy a position
  private static String PROTECT = "PROTECT"; // Move to protect a position
  private static String MAKESTRONG = "MAKESTRONG"; // Make a position strong
  private static String kingCastleKey = "o-ok"; // The key to King's Castling
  private static List<Constant> allconstants= new ArrayList();
  // To make list of possible init states ??
  // 16.12.24 A new key is added: pawn. It is used to signal a pawn strike PAWNATTACK                                                                                                                                                                                                                                                                                       
  private static String[] keys = new String[] {"startpos","piecename","newpos","piecetype","pawn"};
  
  
  public static String getKingCastleKey() {
	return kingCastleKey;
}

public static void setKingCastleKey(String kingCastleKey) {
	KnowledgeBuilder.kingCastleKey = kingCastleKey;
}

public static String getPIECE() {
	return PIECE;
  }

  public static void setPIECE(String pIECE) {
	  PIECE = pIECE;
  }

  public static String getOPPONENTTO() {
	  return OPPONENTTO;
  }

  public static String getPOSSIBLEPROTECT() {
	  return POSSIBLEPROTECT;
  }

  public static void setPOSSIBLEPROTECT(String pOSSIBLEPROTECT) {
	  POSSIBLEPROTECT = pOSSIBLEPROTECT;
  }

  public static String getPOSSIBLEREACH() {
	  return POSSIBLEREACH;
  }

  public static void setPOSSIBLEREACH(String pOSSIBLEREACH) {
	  POSSIBLEREACH = pOSSIBLEREACH;
  }

  public static void setOPPONENTTO(String oPPONENTTO) {
	  OPPONENTTO = oPPONENTTO;
  }
  public static String getPOSSIBLETHREAT() {
	  return POSSIBLETHREAT;
  }
  public static void setPOSSIBLETHREAT(String pOSSIBLETHREAT) {
	  POSSIBLETHREAT = pOSSIBLETHREAT;
  }
  public static String getCASTLE() {
	  return CASTLE;
  }
  public static void setCASTLE(String cASTLE) {
	  CASTLE = cASTLE;
  }
  public static String getBOARD() {
	  return BOARD;
  }
  public static void setBOARD(String bOARD) {
	  BOARD = bOARD;
  }
  public static String getPLAYER() {
	  return PLAYER;
  }
  public static void setPLAYER(String pLAYER) {
	  PLAYER = pLAYER;
  }
  public static String getPAWNATTACK() {
	  return PAWNATTACK;
  }
  public static void setPAWNATTACK(String pAWNATTACK) {
	  PAWNATTACK = pAWNATTACK;
  }
  public static String getOCCUPIES() {
	  return OCCUPIES;
  }
  public static void setOCCUPIES(String oCCUPIES) {
	  OCCUPIES = oCCUPIES;
  }
  public static String getPLAY()
  {
		return PLAY;
  }
  public static void setPLAY(String pLAY)
  {
		PLAY = pLAY;
  }
  public static String getPAWN()
  {
		return PAWN;
  }
  public static void setPAWN(String pAWN)
  {
		PAWN = pAWN;
  }
  public static String getKNIGHT()
  {
		return KNIGHT;
  }
  public static void setKNIGHT(String kNIGHT)
  {
		KNIGHT = kNIGHT;
  }
  public static String getBISHOP()
  {
		return BISHOP;
  }
  public static void setBISHOP(String bISHOP)
  {
		BISHOP = bISHOP;
  }
  public static String getROOK()
  {
		return ROOK;
  }
  public static void setROOK(String rOOK)
  {
		ROOK = rOOK;
  }
  public static String getKING()
  {
		return KING;
  }
  public static void setKING(String kING)
  {
		KING = kING;
  }
  public static String getQUEEN()
  {
		return QUEEN;
  }
  public static void setQUEEN(String qUEEN)
  {
		QUEEN = qUEEN;
  }
  public static String getPAWNMOVE()
  {
		return PAWNMOVE;
  }
  public static void setPAWNMOVE(String pAWNMOVE)
  {
		PAWNMOVE = pAWNMOVE;
  }
  public static String getPIECETYPE()
  {
		return PIECETYPE;
  }
  public static void setPIECETYPE(String pIECETYPE)
  {
		PIECETYPE = pIECETYPE;
  }
  public static String getACTION()
  {
		return ACTION;
  }
  public static void setACTION(String aCTION)
  {
		ACTION = aCTION;
  }
  public static String getPROTECTED()
  {
		return PROTECTED;
  }
  public static void setPROTECTED(String pROTECTED)
  {
		PROTECTED = pROTECTED;
  }
  public static String getSimpleProtected()
  {
		return simpleProtected;
  }
  public static void setSimpleProtected(String simpleProtected)
  {
		KnowledgeBuilder.simpleProtected = simpleProtected;
  }
  public static String getATTACKED()
  {
	  return ATTACKED;
  }
  public static void setATTACKED(String aTTACKED)
  {
		ATTACKED = aTTACKED;
  }
  public static String getCAPTURE()
  {
		return CAPTURE;
  }
  public static void setCAPTURE(String cAPTURE)
  {
		CAPTURE = cAPTURE;
  }
  public static String getCONQUER()
  {
		return CONQUER;
  }
  public static void setCONQUER(String cONQUER)
  {
		CONQUER = cONQUER;
  }
  public static String getTHREATEN()
  {
		return THREATEN;
  }
  public static void setTHREATEN(String tHREATEN)
  {
		THREATEN = tHREATEN;
  }
  public static String getOWNER()
  {
		return OWNER;
  }
  public static void setOWNER(String oWNER)
  {
		OWNER = oWNER;
  }
  public static String getMOVE()
  {
		return MOVE;
  }
  public static void setMOVE(String mOVE)
  {
		MOVE = mOVE;
  }
  public static String getREACHABLE()
  {
		return REACHABLE;
  }
  public static void setREACHABLE(String rEACHABLE)
  {
		REACHABLE = rEACHABLE;
  }
  public static String getCANMOVE()
  {
		return CANMOVE;
  }
  public static void setCANMOVE(String cANMOVE)
  {
		CANMOVE = cANMOVE;
  }
  public static String getSAFEMOVE()
  {
		return SAFEMOVE;
  }
  public static void setSAFEMOVE(String sAFEMOVE)
  {
	  SAFEMOVE = sAFEMOVE;
  }
  public static String getSTRIKE()
  {
	  return STRIKE;
  }
  public static void setSTRIKE(String sTRIKE)
  {
	  STRIKE = sTRIKE;
  }
  
  public static String getOCCUPY() {
	return OCCUPY;
}

public static void setOCCUPY(String oCCUPY) {
	OCCUPY = oCCUPY;
}

public static String getPROTECT() {
	return PROTECT;
}

public static void setPROTECT(String pROTECT) {
	PROTECT = pROTECT;
}

public static String getMAKESTRONG() {
	return MAKESTRONG;
}

public static void setMAKESTRONG(String mAKESTRONG) {
	MAKESTRONG = mAKESTRONG;
}

public static List<Constant> getAllconstants() {
	return allconstants;
}

public static void setAllconstants(List<Constant> allconstants) {
	KnowledgeBuilder.allconstants = allconstants;
}

/**
   * getPieceType
   * This method returns the string type of the piece
   * @param piece
   * @return The piecetype as a string
   */
  public static String getPieceType(AgamePiece piece) {
	  pieceType type = piece.getPieceType();
	  if (type == type.PAWN) {
		  return PAWN;
	  }
	  if (type == type.BISHOP) {
		  return BISHOP;
	  }		
	  if (type == type.ROOK) {
		  return ROOK;
	  }			
	  if (type == type.KNIGHT) {
		  return KNIGHT;
	  }
	  if (type == type.QUEEN) {
		  return QUEEN;
	  }
	  if (type == type.KING) {
		  return KING;
	  }	
	  return null;
  }

  /**
   * parseSentence
   * This method takes two first order logic sentences and creates a rule 
   * to be stored in the knowledge base.
   * @param sentence
   * @param goalSentence
   * @param kb
   */
  public static void parseSentence(String sentence,String goalSentence,FOLKnowledgeBase kb){
	  List<Literal> rules = Utils.parse(sentence);
	  List<Literal> goals = Utils.parse(goalSentence);
	  ConnectedSentence premise = null;
	  int s = rules.size();
	  for (int i= 0; i < s;i=i+2) {
		  Predicate p = (Predicate) rules.get(i).getAtomicSentence();
		  Predicate y = (Predicate) rules.get(i+1).getAtomicSentence();	
		  premise = new ConnectedSentence(Connectors.AND,p,y);

	  }
	  Predicate g = (Predicate)goals.get(0).getAtomicSentence();
	  ConnectedSentence goal = new ConnectedSentence(Connectors.IMPLIES,premise,g);
	  kb.tell(goal);

  }

/**
 * createOccupyaction
 * This method creates an action schema of the form 
 * Action ("occupy_pos",posx,byPiece)
 * PRECONDITION: (REACHABLE(byPiece,posx)
 * EFFECT: (occupies(byPiece,posx)
 * The action schema may contain any number of variables, and so is a lifted action schema
 * @since 16.12.24
 * The precondition can also be PRECONDITION: (PAWNATTACK(byPiece,posx) or CASTLE(king,posx)
 * This is selected by an additional names parameter called "pawn" or "castle".
 * @param names - if parameters are given they are used as Constants in preconditions or effects in the returned action schema
 * The parameters must be given in the following order: Startpos, Piecename, Newpos, Piecetype, or null (and an additional string parameter: pawn, or castle)
 * If a parameter is null a Variable is created for this parameter
 * @return A lifted action Schema
 */
public static ActionSchema createOccupyaction(String... names) {
	int nargs = names.length;
	int noofKeys = keys.length;
	HashMap cparams = new HashMap<String,Constant>();
	HashMap vparams = new HashMap<String,Variable>();
	FunctionContect contex = new FunctionContect(); // Used to register and run functions
/*	String apos = null;
	Variable posname = new Variable("posx"); // The end position as a variable
	Constant newPos = null; // The end position as a constant
	Constant givenPos = null; // The start position as a Constant
	Variable startPos = null; // The start position as a Variable
	Variable pieceName = null; // The piece name as a variable
	Constant namedPiece = null; //The piece name as a constant 
	Variable typeofPiece = null; // The type of piece as a variable
	Constant givenType = null; // The type of piece as a constant
	*/
	List<Term> othervariables = new ArrayList<Term>(); // The list of Terms for the occupy predicate
	List<Term> totalvariables = new ArrayList<Term>(); // The list of all terms
	List<Term> boardTerms = new ArrayList<Term>(); // The list of terms for the board predicate
	List<Term> variables = new ArrayList<Term>(); // The list of Terms for the reachable predicate and the occupy predicate in the Effect.
	List<Term> typevariables = new ArrayList<Term>();// The list of Terms for the piecetype predicate
	ANewposfunction newPosfunction = new ANewposfunction(variables);
	AStartposfunction startPosfunction = new AStartposfunction(othervariables);
	APiecetypefunction pieceTypefunction = new APiecetypefunction(typevariables);
	ABoardtermFunction boardTermfunction = new ABoardtermFunction(boardTerms);
	ApawnstrikeFunction pawnStrikefunction = new ApawnstrikeFunction(variables);
	contex.register(keys[2], newPosfunction);
	contex.register(keys[1], boardTermfunction);
	contex.register(keys[0], startPosfunction);
	contex.register(keys[3], pieceTypefunction);
	contex.register(keys[4],pawnStrikefunction);
/*	pieceName = new Variable("byPiece");
	typeofPiece = new Variable("type");*/
	String reach = REACHABLE;
	if (nargs > 0) {
		for (int i = 0;i<nargs;i++) {
			AChessExecutor exec = null;
			if (names[i] == null) {
				exec = new AChessExecutor(i); // Creates a Variable Term
				Variable var = (Variable)exec.execute();
				vparams.put(keys[i], var);
			}else {
				exec = new AChessExecutor(names[i]); //Creates a Constant Term
				Object tvar = exec.execute();
				if (tvar instanceof Constant) {
					Constant var = (Constant)tvar;
					cparams.put(keys[i], var);
				}
				if (tvar instanceof String) {
					String tpar = (String)tvar;
					if (tpar.equals("pawn"))
						reach = PAWNATTACK;
					if (tpar.equals("castle"))
						reach = CASTLE;
				}
			}

		}
		for (int i = 0;i<noofKeys;i++) {
			contex.get(keys[i]).buildTerms(cparams, vparams);
		}
//		contex.get(keys[0]).buildTerms(cparams, vparams);
		List<Term> thevariables = newPosfunction.getVariables();
		totalvariables.add(othervariables.get(0));
		totalvariables.add(othervariables.get(1));
		totalvariables.add(typevariables.get(1));
		totalvariables.add(variables.get(1));

	}
	if (nargs == 0) {
		for (int i = 0;i<noofKeys;i++) {
			AChessExecutor exec = new AChessExecutor(i); // Creates a Variable Term
			Variable var = (Variable)exec.execute();
			vparams.put(keys[i], var);
		}
		for (int i = 0;i<noofKeys;i++) {
			contex.get(keys[i]).buildTerms(cparams, vparams);
		}
/*		startPos = new Variable("posy");*/
		totalvariables.add(othervariables.get(0));
		totalvariables.add(othervariables.get(1));
		totalvariables.add(typevariables.get(1));
		totalvariables.add(variables.get(1));

	}
	List<Literal> precondition = new ArrayList();
	List<Literal> effects = new ArrayList();
	String actionName = "occupypos";
	Term type = typevariables.get(1);
	Constant piecetype = null;
/*	boolean pawn = false;
	if (type instanceof Constant) {
		piecetype = (Constant)type;
		String typeName = piecetype.getSymbolicName();
		pawn = typeName == PAWN;
	}*/
	Predicate firstposPredicate = new Predicate(OCCUPIES,othervariables); 
	Predicate reachPredicate = new Predicate(reach,variables);
	Predicate typePredicate = new Predicate(PIECETYPE,typevariables);
	Predicate boardPredicate = new Predicate(BOARD,boardTerms);
	precondition.add(new Literal((AtomicSentence)firstposPredicate));
	precondition.add(new Literal((AtomicSentence) boardPredicate));
	precondition.add(new Literal((AtomicSentence) typePredicate));
	precondition.add(new Literal((AtomicSentence) reachPredicate));
	Predicate occupyPredicate = new Predicate(OCCUPIES,variables); 
	effects.add(new Literal((AtomicSentence) occupyPredicate));
	ActionSchema occupyAction = new ActionSchema(actionName,totalvariables,precondition,effects);
	return occupyAction;
  }
  /**
   * findApplicable
   * This method returns a list of applicable action schemas
   * given an action schema containing variables
   * The method is called from the perceptor
   * This method makes use of theprivate method makeprop
 * @param initStates a set of ground initial states
 * @param action The lifted action schema (with variables)
 * @return a list of propositionalized action schemas that are applicable
 */
public static List<ActionSchema> findApplicable(Map<String,State>initStates,ActionSchema action) {
	  List<State> allStates = new ArrayList<State>(initStates.values());
	  List<Constant> stateconstants = new ArrayList<Constant>();
	  List<Constant> tempconstants = new ArrayList<Constant>();
	  allconstants.clear();
	  List<ActionSchema> actions = new ArrayList<ActionSchema>();
	  for (State state:allStates) {
		  List<Literal> literals = state.getFluents(); // The initial states have only ground atoms
		  for (Literal lit:literals) {
			  Predicate p = (Predicate) lit.getAtomicSentence();
			  List<Term> terms = p.getTerms(); 
			  for (Term t:terms) {
				  Constant c = (Constant)t;
				  if (!stateconstants.contains(t)) {
					  stateconstants.add(c);
					  allconstants.add(c);
				  }
				  
			  }
		  }
		  ActionSchema propAction = null;
		  int nofVar = action.getVariables().size();
		  int noC = stateconstants.size(); // This value changes when elements are removed
//		  List<Term> vars = action.getVariables();
/*		  tempconstants.add(stateconstants.get(0));
		  for (int i = 1;i<nofVar;i++) {
			  tempconstants.add(stateconstants.get(i));
		  }*/
		  int diff = noC-nofVar;
		  for (int i = 0;i<diff;i++) {
			  makeProp(action, stateconstants, state, actions);
			  stateconstants.remove(nofVar-1);
		  }
		  stateconstants.clear();
	  }
	  return actions;

	  
  }
	/**
	 * makeProp
	 * This method creates propositionalized action schemas given a lifted action schema
	 * @param action - a lifted action schema
	 * @param stateconstants - state constants to be used in the propositionalization
	 * @param state - The state used to check if the propositionalized action schema is applicable
	 * @param actions - The list of propositionalized action schemas that are applicable in this state
	 */
	private static void makeProp(ActionSchema action,List<Constant> stateconstants,State state, List<ActionSchema> actions) {
		ActionSchema propAction = null;
		propAction = action.getActionBySubstitution(stateconstants); // a propositionalized action schema
		boolean found = state.getFluents().containsAll(propAction.getPrecondition());//is applicable in state s if the precondition of the action is satisfied by s.
		boolean finnes = false;
		if (!actions.isEmpty()) {
			for (ActionSchema schema:actions) {
				finnes = schema.getPrecondition().containsAll(propAction.getPrecondition());
				if (finnes)
					break;
			}
		}
		if (found && !finnes) {
		
			actions.add(propAction);
		}
	}
  public  static String extract(String s,Function <String,String> f){
	  return f.apply(s);
  }

  /**
   * extractString
   * This routine extracts a substring from a string  using the Function interface
   * It finds the last index of a string using separator
   * @param line The original string
   * @param separator The separator
   * @param startindex The startindex
   * @return the substring
   */
  public static String extractString(String line,char separator,int startindex){
	  int index = line.lastIndexOf(separator);
	  if (index == -1)
		  return null;
	  Function<String,String> f = (String s) -> line.substring(startindex,index); // The implementation of the apply method of Function
	  Function<String,String> ef = (String s) -> line.substring(index+1);
//	  Function<String,String> xef = String::substring(index+1);
	  if (startindex == -1)
		  return extract(line,ef);
	  else
		  return extract(line,f);

  }
  public static ToDoubleFunction<Node<PlannerState, ChessPlannerAction>> toDouble(){
	  ToDoubleFunction<Node<PlannerState, ChessPlannerAction>> h = node -> node.getPathCost();
	  return h;
  }
  /**
 * ResultFunction<PlannerState, ChessPlannerAction>
 * The resultfunction extends the BIFunction<S,A,S> interface with the signature (S,A) -> S (p. 53 Java 8)
 * A description of what each action does; the formal name for this is the
 * transition model, specified by a function RESULT(s, a) that returns the state
 * that results from doing action a in state s. We also use the term successor
 * to refer to any state reachable from a given state by a single action.
 *
 * @return
 */
  public static ResultFunction<PlannerState, ChessPlannerAction> aResultFunction(){
	  ResultFunction<PlannerState, ChessPlannerAction> r = (s,a) -> a.findPlannerState(s);
	  return r;
  }
  /**
   * ActionsFunction<PlannerState, ChessPlannerAction>
   * The ActionFunction extends the Function<S,A> interface with signature S -> A (p. 53 Java 8)
   * Given a particular state s, ACTIONS(s) returns the set of actions that can be
   * executed in s. We say that each of these actions is <b>applicable</b> in s.
   *
   * @param <S> The type used to represent states
   * @param <A> The type of the actions to be used to navigate through the state space
   * @return
   */
  public static ActionsFunction<PlannerState, ChessPlannerAction> anActionFunction(){
	  ActionsFunction<PlannerState, ChessPlannerAction> a = s -> s.getActions();
	  return a;
  }
  /**
   * StepCostFunction
   * The stepcost object for search problem
 * @return
 */
  public static StepCostFunction<PlannerState, ChessPlannerAction> stepCost(){
	  StepCostFunction<PlannerState, ChessPlannerAction> step = new ChessStepCostImpl();
	  return step;
  }
  /**
   * aGoaltest for the PlannerState
   * The goal test, which determines whether a given state is a goal state
 * @return a goaltest function
 */
  public static ChessGoalTest<PlannerState> aGoaltest(ChessPlannerAction a){
	  ChessGoalTest<PlannerState>  g = s -> s.testEnd(a);
	  return g;
  }
  
  /**
   * gameActionFunction
   * This action function is related to the action function described in chapter 4.
   * ActionsFunction<GameState, GameAction>
   * The ActionFunction extends the Function<S,A> interface with signature S -> A (p. 53 Java 8)
   * Given a particular state s, ACTIONS(s) returns the set of actions that can be
   * executed in s. We say that each of these actions is <b>applicable</b> in s.
   *
   * @param <S> The type used to represent states
   * @param <A> The type of the actions to be used to navigate through the state space
   * @return
   */
  public static ActionsFunction<GameState, GameAction> gameActionFunction(){
	 ActionsFunction<GameState, GameAction> a = s -> s.getActions();
	  return a;
  }
  /**
   * nondeterminGoaltest
   *This is a nondeterministic goal test function Chapter 4 AIMA book
   * The goal test, which determines whether a given state is a goal state
 * @return a nondeterminGoaltest function
 */
  public static ChessGoalTest<GameState> nondeterminGoaltest(GameAction a){
	  ChessGoalTest<GameState>  g = s -> s.testEnd(a);
	  return g;
  }
  // The resultfunction is not a functional interface
/*  public static NonDetermineResultFunction<GameState,GameAction> nonDetermineRsltFunction(GameState state,GameAction action){
	  return NonDetermineResultFunction<GameState,GameAction> r = action -> action.getStates();
  }*/
}
