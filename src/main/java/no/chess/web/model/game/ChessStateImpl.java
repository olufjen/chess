
package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import no.chess.web.model.Position;
import no.games.chess.ChessAction;
import no.games.chess.ChessState;

/**
 * This class represent the state defined in the games java project and the no.games.chess structure.
 * It implements ChessState from games structure
 * It creates an initial state based on the boardpositions from a given ontology game.
 * The state must be able to return an answer to the question whether this current state is a final state.
 * A final state is either a loss, a win or a draw.
 * A ChessState is characterized by a AgameBoard and positions held in a HashMap.
 * The positions tell which positions are vacant or occupied by a chessPiece.
 * 
 * @author oluf
 *
 */
public class ChessStateImpl implements ChessState<AgameBoard> {

	private AgameBoard gameBoard;
	private HashMap<String, Position> positions;
	private int[][] squares;
	private String[][] piecePosition;
	private APlayer whitePlayer;
	private APlayer blackPlayer;
	private APlayer playerTomove = null;
	private ChessActionImpl chessAction;
	private List<ChessAction> actions;
	private AchessGame game;
	private double utility = -1; // 1 = win for White player, 0 = win for black player, 0.5 is a draw.
	
	@Override
	public AgameBoard getBoard() {
		// TODO Auto-generated method stub
		return gameBoard;
	}

	public ChessStateImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ChessStateImpl(AgameBoard gameBoard) {
		super();
		this.gameBoard = gameBoard;
		this.positions = gameBoard.getPositions();
		
	}

	public ChessStateImpl(HashMap<String, Position> positions,int[][] squares,String[][] piecePosition) {
		super();
		this.positions = positions;
		this.squares = squares;
		this.piecePosition = piecePosition;
		gameBoard = new AgameBoard(this.positions);
	}

	public ChessStateImpl(AchessGame game,AgameBoard gameBoard, APlayer whitePlayer, APlayer blackPlayer) {
		super();
		this.game = game;
		this.gameBoard = gameBoard;
		this.whitePlayer = whitePlayer;
		this.blackPlayer = blackPlayer;
		this.whitePlayer.setActive(true);
		positions = this.game.getPositions();
		piecePosition = this.game.getPiecePosition();
		squares = this.game.getSquares();
		playerTomove = null;
		if (whitePlayer.isActive())
			playerTomove  = whitePlayer;
		else if (blackPlayer.isActive())
			playerTomove = blackPlayer;
		actions = getActions();
		playerTomove.setActions(actions);
	}

	public APlayer getPlayerTomove() {
		return playerTomove;
	}

	public void setPlayerTomove(APlayer playerTomove) {
		this.playerTomove = playerTomove;
	}

	/**
	 * mark
	 * This method changes the state by switching active player
	 * and creating new set of actions available for the active player
	 */
	public void mark() {
		if(playerTomove.getPlayerName() == playerTomove.getWhitePlayer()) {
			whitePlayer.setActive(false);
			blackPlayer.setActive(true);
	
		}
		if(playerTomove.getPlayerName() == playerTomove.getBlackPlayer()) {
			whitePlayer.setActive(true);
			blackPlayer.setActive(false);

		}
		if (whitePlayer.isActive())
			playerTomove  = whitePlayer;
		else if (blackPlayer.isActive())
			playerTomove = blackPlayer;
		actions = getActions();
		analyzeutility();
		playerTomove.setActions(actions);
	}
	private void analyzeutility() {
		if (whitePlayer.isActive())
			utility = 1;
		else
			utility = -1;
	}
	public double getUtility() {
		return utility;
	}

	public void setUtility(double utility) {
		this.utility = utility;
	}

	public AgameBoard getGameBoard() {
		return gameBoard;
	}

	public void setGameBoard(AgameBoard gameBoard) {
		this.gameBoard = gameBoard;
	}

	public APlayer getWhitePlayer() {
		return whitePlayer;
	}

	public void setWhitePlayer(APlayer whitePlayer) {
		this.whitePlayer = whitePlayer;
	}

	public APlayer getBlackPlayer() {
		return blackPlayer;
	}

	public void setBlackPlayer(APlayer blackPlayer) {
		this.blackPlayer = blackPlayer;
	}

	public ChessActionImpl getChessAction() {
		return chessAction;
	}

	public void setChessAction(ChessActionImpl chessAction) {
		this.chessAction = chessAction;
	}
	
	/**
	 * getActions
	 * This method returns a list of actions that is possible to perform by the active player
	 * These actions contain an AgamePiece and its available (reachable) positions.
	 * Each action also calculates which reachable positions are occupied by other pieces belonging to the same player.
	 * @return
	 */
	public List<ChessAction> getActions(){
		List<ChessAction> actions = new ArrayList<ChessAction>();
		ArrayList<AgamePiece> pieces = playerTomove.getMygamePieces();
		for (AgamePiece piece : pieces) {
			HashMap<String,Position> reachablePositions = piece.getReacablePositions();
			ChessAction action = new ChessActionImpl(reachablePositions,piece,playerTomove);
			actions.add(action);

		}
		return actions;
		
	}

}
