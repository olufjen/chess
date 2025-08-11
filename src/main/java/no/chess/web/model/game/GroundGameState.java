package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import aima.core.logic.fol.parsing.ast.Sentence;
import aima.core.logic.fol.kb.data.Literal;
import aima.core.logic.planning.ActionSchema;

import no.games.chess.GamePiece;
import no.games.chess.search.nondeterministic.ChessPercept;
import no.games.chess.search.nondeterministic.GameAction;
import no.games.chess.search.nondeterministic.GameState;

/**
 * GroundGameState
 * This is a subclass of the GameState.
 * A ground game state has an id consisting of the name of the piece and it position on the board.
 * A gamestate is created for every chess action and actions schema created in the searcProblem method.
 * It contains a game piece and a set of Game actions one of each of the types:
 * Myaction{CAPTUREPOS,MOVE,ATTACK,CAPTUREPIECE,PROTECTPOS,PROTECTPIECE,CASTLING;}
 * 
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
	private APerceptor thePerceptor = null;
	private String outputFileName =  "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\gamestate";
	private PrintWriter writer =  null;
	private FileWriter fw =  null;
	private ThePeas peas = null;
	private String pieceName;
	private String posName;
	private String stateId;
	private AgamePiece statePiece = null;
	
	/**
	 * Constructor used in the searchProblem method
	 * @param gamePiece
	 * @param actionSchema
	 */
	public GroundGameState(AgamePiece gamePiece, ActionSchema actionSchema) {
		super(gamePiece, actionSchema);
	    statePiece = (AgamePiece) this.gamePiece;
	    pieceName = statePiece.getMyPiece().getOntlogyName();
	    posName = statePiece.getmyPosition().getPositionName();
//	    stateId = pieceName+"_"+posName;
	    stateId = this.actionSchema.getName() + "_" + posName;
	    String filename = outputFileName + pieceName + ".txt";
		try {
			fw = new FileWriter(filename, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));

	}

	public GroundGameState(List<Sentence> pieceSentences, GamePiece<?> gamePiece, ActionSchema actionSchema,
			ChessPercept thePerceptor) {
		super(pieceSentences, gamePiece, actionSchema, thePerceptor);
		// TODO Auto-generated constructor stub
	}

	public GroundGameState(AgamePiece gamePiece, ActionSchema actionSchema,APlayer player,APlayer opponent, int moveNr, APerceptor thePerceptor) {
		super(gamePiece, actionSchema);
		this.player = player;
		this.opponent = opponent;
		this.moveNr = moveNr;
		this.thePerceptor = thePerceptor;
	    statePiece = (AgamePiece) this.gamePiece;
	    pieceName = statePiece.getMyPiece().getOntlogyName();
	    posName = statePiece.getmyPosition().getPositionName();
//	    stateId = pieceName+"_"+posName;
	    stateId = this.actionSchema.getName() + "_" + posName;
	    String filename = outputFileName + pieceName + ".txt";
		try {
			fw = new FileWriter(filename, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
//	    stateStatisics();
	}

	public String getPieceName() {
		return pieceName;
	}

	public void setPieceName(String pieceName) {
		this.pieceName = pieceName;
	}

	public String getPosName() {
		return posName;
	}

	public void setPosName(String posName) {
		this.posName = posName;
	}

	public String getStateId() {
		return stateId;
	}

	public void setStateId(String stateId) {
		this.stateId = stateId;
	}

	public AgamePiece getStatePiece() {
		return statePiece;
	}

	public void setStatePiece(AgamePiece statePiece) {
		this.statePiece = statePiece;
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
		writer.println("Total no of moves player "+noofplayerActions);
		writer.println("Total no of inactive player pieces "+noofplayerinactive);
		writer.println("Total no of moves opponent "+noofopponentActions);
		writer.println("Total no of inactive opponent pieces "+noofopponentinactive);
		List<ApieceMove> moves = statePiece.getMyMoves();
		List<Literal> literals = this.actionSchema.getEffects();
		writer.println("Effects ");
		for (Literal l:literals) {
			writer.println(l.toString());
		}
		writer.println("Moves ");
		if (moves != null && !moves.isEmpty()) {
			for(ApieceMove move:moves) {
				writer.println(move.toString());
			}
		}
		writer.println("********* end statistics **********");
		writer.flush();
	}
	public void setStatestatistics() {
		HashMap<String,ApieceMove> myMoves = player.getMyMoves();
		List<AgamePiece>myinactivePieces = player.getInactivePieces();
		HashMap<String,ApieceMove> oppMoves =opponent.getMyMoves();
		List<AgamePiece>oppinactivePieces = opponent.getInactivePieces();
		noofplayerActions = myMoves.size();
		noofopponentActions = oppMoves.size();
		noofplayerinactive = myinactivePieces.size();
		noofopponentinactive = oppinactivePieces.size();
	    stateStatisics();	
		
	}
	/**
	 * testEnd
	 * This is the method used by the ChessGoalTest functional interface 
	 * the testGoal method
	 * A Game Action may return a set of GameStates, each of these states have a value.
	 * 
	 * @param action Based on this action, is this the goal state?
	 * @return true if this is the goal state. This results in an empty plan in the search tree
	 */
	@Override
	public boolean testEnd(GameAction action) {
		
		return super.testEnd(action);
	}

}
