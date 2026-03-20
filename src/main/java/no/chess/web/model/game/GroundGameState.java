package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	private List<AgamePiece>myinactivePieces;
	private HashMap<String,ApieceMove> oppMoves;
	private List<AgamePiece>oppinactivePieces;
	private List<AgamePiece> mylostPieces;
	private List<AgamePiece> opponentremovedPieces;
	private List<ActionSchema> opponentActions; // The list of action schemas available to the opponent
	private List<GroundGameAction> opponentGameActions;
	private double heuristicScore; // Verdien beregnet ut fra fakta i KB
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
	 * The main Constructor
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
	    actions = new ArrayList<GameAction>();
	    opponentGameActions = new ArrayList<GroundGameAction>();
	    produceActions();
	    setStatestatistics();
	    checkKb("CONTROLCENTER(a,b)");
		writer.flush();
	}
	/**
	 * produceActions
	 * This method produces a ground game action for every available action schema
	 * both for the player and the opponent
	 * 
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
	 * @param query The query to ask the knowledge base
	 * 
	 */
	public void checkKb(String query) {
		List<AgamePiece> pieces = player.getMygamePieces();
		writer.println("The query is "+query);
		List<String> answer = knowledgeBase.checkQuery(query);
//		List <String> forwardanswer = folKb.forwardcheckQuery(query);
		AgamePiece gpiece = null;
		Position pos = null;
		for (String p: answer) {
			writer.println("The backward chain object is "+p);
			gpiece =  (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(p)).findAny().orElse(null);
			pos = (Position) positionList.stream().filter(ps -> ps.getPositionName().contains(p)).findAny().orElse(null);
			if (gpiece != null) {
				writer.println(gpiece.toString());
			} 
		}
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
		return actions.get(0);
	}
	public List<GameAction> getActions(){
		return actions;
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
		writer.println("Actions available to player");
		List<GameAction> groundActions = actions;
		for (GameAction action:groundActions) {
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
	    stateStatisics();	
		
	}
	public void evaluateScore() {
		int mypieces = player.getMygamePieces().size();
		int opppieces = opponent.getMygamePieces().size();
		int mydiff = mypieces - noofplayerinactive;
		int oppdiff = opppieces - noofopponentinactive;
	
		heuristicScore = mydiff - oppdiff + noofoppremovedPieces - noofLostpieces;
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
//		return super.testEnd(action);
		return true;
	}

}
