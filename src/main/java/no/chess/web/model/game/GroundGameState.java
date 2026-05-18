package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aima.core.logic.fol.parsing.ast.Sentence;
import aima.core.logic.fol.kb.data.Literal;
import aima.core.logic.planning.ActionSchema;
import no.chess.web.model.Position;
import no.games.chess.GamePiece;
import no.games.chess.search.nondeterministic.ChessPercept;
import no.games.chess.search.nondeterministic.GameAction;
import no.games.chess.search.nondeterministic.GameState;

/**
 * GroundGameState
 * This is a subclass of the GameState.
 * Revised 26.01.26:
 * A ground game state contains the FOLKnowledgebase representing the state of the game.
 * An action schema is given the Name of action (pieceName+"_"+toPosit)
 * Lifted action schemas are created by the perceptor with parameters in the following order:
 * Startpos, Piecename, Newpos, Piecetype, or null. A fifth parameter "pawn" is added signaling a pawn strike, or "castle" signaling castling with king
 * Lifted action schemas represent goal formulations.
 * 
 * @author oluf
 *
 */
public class GroundGameState extends GameState {
	private APlayer player;
	private APlayer opponent;
	private int moveNr;
	private int noofplayerActions = 0;
	private int noofopponentActions = 0;
	private int noofplayerinactive = 0;
	private int noofopponentinactive = 0;
	private int noofLostpieces;
	private int noofoppremovedPieces;
	private int noofActions;
	private APerceptor thePerceptor = null;
	private String outputFileName =  "gamestate";
	private String stateId;
	private PrintWriter writer =  null;
	private FileWriter fw =  null;
	private ThePeas peas = null;
    private List<Position> positionList = null; // The original HashMap of positions as a list
	private ChessFolKnowledgeBase knowledgeBase;// This represent the state of the game
	private HashMap<String,ApieceMove> myMoves;
	private List<GroundGameAction> myActions;
	private List<AgamePiece>myinactivePieces;
	private HashMap<String,ApieceMove> oppMoves;
	private List<AgamePiece>oppinactivePieces;
	private List<AgamePiece> mylostPieces;
	private List<AgamePiece> opponentremovedPieces;
	private List<ActionSchema> opponentActions; // The list of action schemas available to the opponent
	private List<GroundGameAction> opponentGameActions;
	private List<GroundGameAction> relevantActions; // This list contains actions which are relevant for this state based on query to knowlewdge base and the evaluation function
	private HashMap<String,GroundGameAction> relevantMapActions;
	private ActionSchema opponentAction = null; // This action schema shows a possible chosen opponent action
	private GroundGameAction myAction = null; // designated gameAction for this state
	private GroundGameAction oppAction = null; // Designated opponent action for this state. It is set by the performAction method of the gameAction (the result function).
	
	private double heuristicScore; // Verdien beregnet ut fra fakta i KB
	private boolean newState = false; // A temporary flag
	private boolean contextFound = false; // True when a move context is found
	private boolean openingFound = false; // True when opening is performed
	private boolean chosenState = false; // A flag to determine if this is the end state - the chosen state
	private String chosenAction = null; // The id of the chosen action after call to the evaluation function
	/**
	 * Basic Constructor
	 * @param gamePiece
	 * @param actionSchema
	 */
	public GroundGameState() {
		super();
		String catalog = KnowledgeBuilder.getFileCatalog();
	    String filename = catalog + outputFileName+".txt";
		try {
			fw = new FileWriter(filename, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));

	}

	/**
	 * This constructor is used when a state is created from the performAction method of GameAction
	 * which again is called from the orSearch method when the problem testGoal function for a chosen state returns false.
	 * As defined:
	 * For å bruke søkemotoren til å lage en plan, må du definere en Results-funksjon som simulerer nondeterminismen:
	 * Modellering av "Results(s, a)":
	 * Når agenten (Hvit) utfører handlingen $a$:
	1. Transition: Utfør trekket $a$ på brettet.
	2. Opponent's Turn: Identifiser alle lovlige trekk for Sort ($m_1, m_2, ..., m_n$).
	3. State Set: Generer en ny KB for hvert av Sorts mulige svar.
	4. Output: Funksjonen returnerer en liste over disse KB-ene.
	
	 * @param player - the player of the game
	 * @param opponent - the opponent
	 * @param moveNr - the movenumber before opponent move
	 * @param kb the knowledge base
	 * @param thePerceptor
	 * @param actionSchemas - the list of available action schemas for player  
	 * @param actionSchema - The chosen actionschema from opponent
	 */
	public GroundGameState(APlayer player,APlayer opponent, int moveNr, ChessFolKnowledgeBase kb,ChessPercept thePerceptor, List<ActionSchema> actionSchemas,List<ActionSchema> opponentactionSchemas,List<Position> positions,ActionSchema actionSchema) {
		super(thePerceptor, actionSchemas);
		String catalog = KnowledgeBuilder.getFileCatalog();
	    String filename = catalog + outputFileName+".txt";
		try {
			fw = new FileWriter(filename, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
	    this.opponentAction = actionSchema; // A legal move available to the opponent - This state is the result of this opponent action
	    myActions = new ArrayList<GroundGameAction>(); // Moved from GameState
	    actions = new ArrayList<GameAction>();
	    opponentGameActions = new ArrayList<GroundGameAction>();    
	    relevantActions = new ArrayList<GroundGameAction>();
	    relevantMapActions = new HashMap<String,GroundGameAction>();
		this.player = player;
		this.opponent = opponent;
		this.moveNr = moveNr;
		this.knowledgeBase = kb;
		this.actionSchemas = actionSchemas;
		this.positionList = positions;
		this.opponentActions =  opponentactionSchemas;
		noofActions = actionSchemas.size();
		stateId = "S"+Integer.toString(moveNr);	 
		writer.println("-- New state after opponent action "+actionSchema.getName()+ " --");
		gamestateId = stateId + actionSchema.getName(); //myAction.getActionSchema().getName(); The state id after an opponent action
		newState = true; // New state must be set when correct action is found based on what opponent has available and the evaluation score
	    produceActions();

	    setStatestatistics(); // Produces statistics for this state and evaluate the score.
	    actions.addAll(relevantActions);
	    // All states produced get the same preferred game action from the evaluation function
		writer.flush();
/*
 * To do:
 * Find which opponent piece is active in the given action schema
 * How do this action influence the heuristic score?	    
 */
	}

	/**
	 * The main Constructor - used when the GameState is created for the first time
	 * @param player - The player of the game
	 * @param opponent - THe opponent of the game
	 * @param moveNr - The current move number
	 * @param thePerceptor
	 * @param kb - Fol knowlwedge base representing the state of the game
	 * @param actionSchemas These are all available action schemas for this state
	 */
	public GroundGameState(APlayer player,APlayer opponent, int moveNr, APerceptor thePerceptor, ChessFolKnowledgeBase kb,List<ActionSchema> actionSchemas,List<ActionSchema> opponentactionSchemas,List<Position> positions) {
		super();
		this.player = player;
		this.opponent = opponent;
		this.moveNr = moveNr;
		this.thePerceptor = thePerceptor;
		this.knowledgeBase = kb;
		this.actionSchemas = actionSchemas;
		this.positionList = positions;
		this.opponentActions =  opponentactionSchemas;
		noofActions = actionSchemas.size();
		stateId = "S"+Integer.toString(moveNr);
		String catalog = KnowledgeBuilder.getFileCatalog();
//	    stateId = pieceName+"_"+posName;
//	    stateId = this.actionSchema.getName();
	    String filename = catalog + outputFileName + ".txt";
		try {
			fw = new FileWriter(filename, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
	    myActions = new ArrayList<GroundGameAction>();
	    actions = new ArrayList<GameAction>();
	    opponentGameActions = new ArrayList<GroundGameAction>();
	    relevantActions = new ArrayList<GroundGameAction>();
	    relevantMapActions = new HashMap<String,GroundGameAction>();
	    produceActions();
	    setStatestatistics();
	    stateStatisics();	
	    actions.addAll(relevantActions);
		gamestateId = stateId + chosenAction; //myAction.getActionSchema().getName(); The state id after an opponent action
	//    checkKb("CONTROLCENTER(a,b)");
		writer.flush();
	}
	
	public boolean isOpeningFound() {
		return openingFound;
	}

	public void setOpeningFound(boolean openingFound) {
		this.openingFound = openingFound;
	}

	public boolean isChosenState() {
		return chosenState;
	}

	public void setChosenState(boolean chosenState) {
		this.chosenState = chosenState;
	}

	public List<GroundGameAction> getRelevantActions() {
		return relevantActions;
	}
	public List<GameAction> gettheRelavantActions(){
		List<GameAction> superList = relevantActions.stream()
			    .map(GameAction.class::cast)
			    .toList();
		return superList;
	
	}
	/**
	 *  getActions()
	 *  Called by the getAction method of the nondeterminstic problem
	 *	@return A list of game actions relevant for the player to this state
	 */
	public List<GameAction> getActions(){
		return  gettheRelavantActions();
	}
	public void setRelevantActions(List<GroundGameAction> relevantActions) {
		this.relevantActions = relevantActions;
	}

	public HashMap<String, GroundGameAction> getRelevantMapActions() {
		return relevantMapActions;
	}

	public void setRelevantMapActions(HashMap<String, GroundGameAction> relevantMapActions) {
		this.relevantMapActions = relevantMapActions;
	}

	public GroundGameAction getMyAction() {
		return myAction;
	}

	public void setMyAction(GroundGameAction myAction) {
		this.myAction = myAction;
	}

	public GroundGameAction getOppAction() {
		return oppAction;
	}

	public void setOppAction(GroundGameAction oppAction) {
		this.oppAction = oppAction;
//		gamestateId = null;
//		gamestateId = stateId + oppAction.getActionSchema().getName();
		
	}

	public List<Position> getPositionList() {
		return positionList;
	}

	public void setPositionList(List<Position> positionList) {
		this.positionList = positionList;
	}

	public List<ActionSchema> getOpponentActions() {
		return opponentActions;
	}

	public void setOpponentActions(List<ActionSchema> opponentActions) {
		this.opponentActions = opponentActions;
	}

	public ChessFolKnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}

	public void setKnowledgeBase(ChessFolKnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}

	public void determineSchema() {
		for (ActionSchema actionSchema:actionSchemas) {
			myAction.getActionSchema();
		}
	}
	/**
	 * produceActions
	 * This method produces a ground game action for every available action schema
	 * both for the player and the opponent
	 * TO-DO:
	 * Create a method to choose the ? best actions for the player and the opponent
	 */
	public void produceActions() {
		List<AgamePiece> pieces = player.getMygamePieces();
		for (ActionSchema actionSchema:actionSchemas) {
			String name = actionSchema.getName();
			String p = KnowledgeBuilder.extractString(name,'_',0);
			AgamePiece gpiece =  (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(p)).findAny().orElse(null);
			GroundGameAction gameAction = new GroundGameAction(gpiece,actionSchema,this);
			actions.add(gameAction);
		}
		List<AgamePiece> opponentpieces = opponent.getMygamePieces();
		for (ActionSchema actionSchema:opponentActions) {
			String name = actionSchema.getName();
			String p = KnowledgeBuilder.extractString(name,'_',0);
			AgamePiece gpiece =  (AgamePiece) opponentpieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(p)).findAny().orElse(null);
			GroundGameAction gameAction = new GroundGameAction(gpiece,actionSchema,this);
			opponentGameActions.add(gameAction);
		}
	}
	/**
	 * checkKb
	 * This method queries the FOL knowledge base
	 * It is called from the evaluateSore method
	 * @param query The query to ask the knowledge base
	 * If the query has an answer then, the answer contains a number of objects - a piece and its position
	 * @return The single string object from the KB if it is a single answer
	 */
	public String checkKb(String query) {
		String ret = "x";
		List<AgamePiece> pieces = player.getMygamePieces();
		writer.println("The query is "+query);
		List<String> answer = knowledgeBase.checkQuery(query);
		if (answer != null && answer.size() == 1) {
			ret = answer.get(0);
		}
//		List <String> forwardanswer = folKb.forwardcheckQuery(query);
		AgamePiece gpiece = null;
		Position pos = null;
		boolean mypiece = false; // OBS - no ground action produced !!!
		GroundGameAction groundAction = null;
		for (String p: answer) {
			writer.println("The backward chain object is "+p);
			if (!mypiece)
				gpiece =  (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(p)).findAny().orElse(null);
			pos = (Position) positionList.stream().filter(ps -> ps.getPositionName().contains(p)).findAny().orElse(null);

			if (gpiece != null) {
				writer.println("And the piece is "+gpiece.getMyPiece().getOntlogyName());
				mypiece = true;
			}
			if (pos != null && mypiece) {
				String posName = pos.getPositionName();
				for (GameAction action:actions) {
					AgamePiece piece = (AgamePiece) action.getGamePiece();
					if(gpiece == piece) {
						groundAction = (GroundGameAction)action;
						if(groundAction.getEndPos().equals(posName)) {
							relevantActions.add(groundAction);
							AgamePiece thePiece = (AgamePiece) groundAction.getGamePiece();
							String actionId =  thePiece.getMyPiece().getOntlogyName() + "_" + pos.getPositionName();
							relevantMapActions.put(actionId, groundAction);
							writer.println("And the position is "+pos.getPositionName()+" and the action id is "+actionId);
						}
	
					}
				}
				mypiece = false; // OBS - no ground action produced !!!
			}
		}
		return ret;
		/*  
		 * for (String p: forwardanswer) {
		 * writer.println("The forward chain object is "+p); }
		 */
	}

	/**
	 * createAction
	 * This method creates an initial action based on the current state.
	 * 
	 * @return a Game action based on the current state
	 */
	public GameAction createAction() {
		return myAction;
	}
	
	public boolean isContextFound() {
		return contextFound;
	}

	public void setContextFound(boolean contextFound) {
		this.contextFound = contextFound;
	}

	public PrintWriter getWriter() {
		return writer;
	}

	public void setWriter(PrintWriter writer) {
		this.writer = writer;
	}

	public List<GroundGameAction> getOpponentGameActions() {
		return opponentGameActions;
	}

	public void setOpponentGameActions(List<GroundGameAction> opponentGameActions) {
		this.opponentGameActions = opponentGameActions;
	}


	public List<GroundGameAction> getMyActions() {
		return myActions;
	}

	public void setMyActions(List<GroundGameAction> myActions) {
		this.myActions = myActions;
	}

	public String getStateId() {
		return stateId;
	}

	public void setStateId(String stateId) {
		this.stateId = stateId;
	}


	public APlayer getPlayer() {
		return player;
	}

	public void setPlayer(APlayer player) {
		this.player = player;
	}

	public APlayer getOpponent() {
		return opponent;
	}

	public void setOpponent(APlayer opponent) {
		this.opponent = opponent;
	}

	public APerceptor getThePerceptor() {
		return thePerceptor;
	}

	public void setThePerceptor(APerceptor thePerceptor) {
		this.thePerceptor = thePerceptor;
	}

	public ThePeas getPeas() {
		return peas;
	}

	public void setPeas(ThePeas peas) {
		this.peas = peas;
	}
	
	public int getMoveNr() {
		return moveNr;
	}

	public void setMoveNr(int moveNr) {
		this.moveNr = moveNr;
	}
	public void stateStatisics() {
		writer.println("Statistics for "+ stateId);
		writer.println("Heuristic score "+heuristicScore);
		writer.println("Total no of moves player "+noofplayerActions);
		writer.println("Total no of inactive player pieces "+noofplayerinactive);
		writer.println("Total no of moves opponent "+noofopponentActions);
		writer.println("Total no of inactive opponent pieces "+noofopponentinactive);
		writer.println("Total no of lost player pieces "+noofLostpieces);
		writer.println("Total no of taken opponent pieces "+noofoppremovedPieces);
		writer.println("Total no of available actions "+noofActions);
		if (this.actionSchema != null) {
			List<Literal> literals = this.actionSchema.getEffects();
			writer.println("Effects ");
			for (Literal l:literals) {
				writer.println(l.toString());
			}
		}
		writer.println("Relevant Actions available to player");
//		List<GameAction> groundActions = actions;
		for (GameAction action:relevantActions) {
			GroundGameAction localAction = (GroundGameAction)action;
			writer.println(localAction.toString());
		}
		writer.println("Actions available to opponent");	
		for (GroundGameAction action:opponentGameActions) {
			writer.println(action.toString());
		}
		writer.println("********* end statistics **********");

	}
	public void setStatestatistics() {
		myMoves = player.getMyMoves();
		myinactivePieces = player.getInactivePieces();
		oppMoves = opponent.getMyMoves();
		oppinactivePieces = opponent.getInactivePieces();
		mylostPieces = player.getRemovedPieces();
		opponentremovedPieces = opponent.getRemovedPieces();
		noofLostpieces = mylostPieces.size();
		noofoppremovedPieces = opponentremovedPieces.size();
		noofplayerActions = myMoves.size();
		noofopponentActions = oppMoves.size();
		noofplayerinactive = myinactivePieces.size();
		noofopponentinactive = oppinactivePieces.size();
		evaluateScore();
//	    stateStatisics();	
		
	}
	/**
	 * evaluateScore
	 * This method evaluate the score of this state, which then is used to determine if this is the end state
	 * Based on parameters it determines the preferred action of this state
	 * TODO: Use functional interface or enum and own methods
	 */
	public void evaluateScore() {
		int mypieces = player.getMygamePieces().size();
		int opppieces = opponent.getMygamePieces().size();
		int mydiff = mypieces - noofplayerinactive;
		int oppdiff = opppieces - noofopponentinactive;
	
		heuristicScore = mydiff - oppdiff + noofoppremovedPieces - noofLostpieces;
/*
 * TODO develop further:	This structure determines the query to the knowledge base, and the choice of action	
 */
		if (!knowledgeBase.checkKB("OPENING(x)")) { //OJN 18.05 Test of move number 0 replaced - No opening performed
			String strategy = checkKb("WANTSTOPLAY(x)");
			writer.println("The strategy is "+strategy);
		    checkKb("CONTROLCENTER(a,b)"); // To be enhanced with other queries
		    String actionId = "WhitePawn4" + "_" + "d4";
		    myAction = relevantMapActions.get(actionId);
			chosenAction = actionId;
			writer.println("Evaluation chosen action id: "+actionId);
			if (myAction != null)
				writer.println("Evaluation chosen action: "+myAction.toString());
			this.action = myAction; 
			openingFound = true;
//			chosenState = true;
		}
		String context = "no Context";
		if (knowledgeBase.checkKB("CONTEXT(x)")) {
			context = checkKb("CONTEXT(x)");
			checkKb("CONTEXTMOVE(a,b)");
		    String actionId = "WhitePawn3" + "_" + "c4";
		    myAction = relevantMapActions.get(actionId);
			chosenAction = actionId;
			writer.println("The context is " + context + " and evaluation chosen action id: "+actionId);
			if (myAction != null)
				writer.println("Evaluation chosen action: "+myAction.toString());
			this.action = myAction; 
			contextFound = true;
		}
		writer.println("The context is "+context);		
	}
	/**
	 * determineAction
	 * This method determines if a game action is relevant for this state.
	 * @return
	 */
	public GroundGameAction determineAction(String pieceName,GroundGameAction action) {
		
		return null;
	}
	/**
	 * testEnd
	 * This is the method used by the ChessGoalTest functional interface 
	 * the testGoal method
	 * A Game Action may return a set of GameStates, each of these states have a value.
	 * If this method returns true then a chosen actionSchema is available together with its initial and goal states.
	 * OBS 5.2.26 At present the action Parameter is the initial action
	 * 
	 * @param action Based on this action, is this the goal state?
	 * @return true if this is the goal state. This results in an empty plan in the search tree
	 */
	@Override
	public boolean testEnd(GameAction action) {
		GroundGameAction localAction = (GroundGameAction)action;

		if (newState && action != null) {
			writer.println("State testEnd chosen action "+ localAction.toString());
			writer.println("State testEnd chosen state "+ gamestateId);
			this.action = action;
			writer.flush();
			goalState = true;
			return true;
		}
		return false;
	}

}
