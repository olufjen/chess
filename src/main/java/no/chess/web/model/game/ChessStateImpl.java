
package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import no.chess.web.model.Position;
import no.games.chess.ChessAction;
import no.games.chess.ChessPieceType;
import no.games.chess.ChessPlayer;
import no.games.chess.ChessState;

/**
 * This class represent the state defined in the games java project and the no.games.chess structure.
 * It implements ChessState from games structure
 * An initial state is created by the AChessGame object when this object is created.
 * It creates an initial state based on the boardpositions from a given ontology game file.
 * The state must be able to return an answer to the question whether this current state is a final state.
 * A final state is either a loss, a win or a draw.
 * A ChessState is characterized by a AgameBoard and positions held in a HashMap.
 * The positions tell which positions are vacant or occupied by a chessPiece. 
 * THere exists only one ChessState object, and the values of the state changes only when the .mark method is called.
 * 
 * @author oluf
 * @param <GameBoard> Represent the AIMA Gameboard
 *
 */
public class ChessStateImpl<GameBoard> implements ChessState<GameBoard> {

	private AgameBoard gameBoard;
	private HashMap<String, Position> positions;
	private int[][] squares;
	private String[][] piecePosition;
	private APlayer whitePlayer;
	private APlayer blackPlayer;
	private APlayer playerTomove = null;
	private APlayer myPlayer = null; // The player that PlayGame represent At present: can only represent the white player
	private ChessActionImpl chessAction;
	private List<ChessAction> actions;
	private AchessGame game;
	private double utility = -1; // 1 = win for White player, 0 = win for black player, 0.5 is a draw.
	private List<ApieceMove> movements;
	
	@Override
	public GameBoard getBoard() {
		
		return (GameBoard) gameBoard;
	}
	public AgameBoard getGameboard(){
		return gameBoard;
	}
	public ChessStateImpl() {
		super();
		
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
		this.myPlayer = whitePlayer; // Set to represent the white player
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
		movements = new ArrayList();
	}
	/* 
	 *emptyMovements
	 * This method clears all movements made during a makeDecision process
	 * and restores available positions in accordance with the piece's position.
	 * It then produces new legal moves for all involved pieces.
	 */
	public void emptyMovements() {
		checkPlayers();
		for (ApieceMove move:movements) {
			AgamePiece piece = move.getPiece();
			Position pos = move.getPiece().getmyPosition();
//			piece.setActive(true); // Always set piece active again !!!!
//			if (piece.isActive())
			piece.produceLegalmoves(pos);
		}
		movements.clear();
//		checkPlayers();
	}

	/**
	 * checkPlayers
	 * This method restores any removed pieces in a search for the best move
	 * It is called from the emptyMovements method
	 */
	public void checkPlayers() {
		List<AgamePiece> pieces = whitePlayer.getMygamePieces();
		
		for (AgamePiece piece:pieces) {
			if (!piece.isActive()) {
				piece.getMyPosition().returnPiece();
//				piece.setActive(true);
//				piece.restorePosition();
//				piece.restoreValue();
			}
			piece.restorePosition(); // Restore positions for all pieces
//			piece.produceLegalmoves(pos); // Should it also produce new legal moves ??
		}
		List<AgamePiece> blackpieces = blackPlayer.getMygamePieces();
		for (AgamePiece piece:blackpieces) {
			if (!piece.isActive()) {
				piece.getMyPosition().returnPiece();
//				piece.setActive(true);
//				piece.restorePosition();
//				piece.restoreValue();
			}
			piece.restorePosition(); // Restore positions for all pieces
//			piece.produceLegalmoves(pos);
		}		
	}
/*	public ChessStateImpl(AchessGame achessGame, AgameBoard gameBoard, ChessPlayer<?, ?> whitePlayer,
			ChessPlayer<?, ?> blackPlayer) {
		// TODO Auto-generated constructor stub
	}*/
	public APlayer getPlayerTomove() {
		return playerTomove;
	}

	public void setPlayerTomove(APlayer playerTomove) {
		this.playerTomove = playerTomove;
	}

	public APlayer getMyPlayer() {
		return myPlayer;
	}
	public void setMyPlayer(APlayer myPlayer) {
		this.myPlayer = myPlayer;
	}
	/**
	 * returnMyplayer
	 * This method sets the active player to myPlayer
	 * which is the player that the game represent
	 */
	public void returnMyplayer() {
		if(playerTomove.getPlayerName() == playerTomove.getBlackPlayer()) {
			whitePlayer.setActive(true);
			blackPlayer.setActive(false);
		}
		playerTomove = whitePlayer;
		actions = getActions();
		analyzeutility();
		playerTomove.setActions(actions);
	}
	/**
	 * mark
	 * This method changes the state by switching active player
	 * and creating new set of actions available for the active player
	 * Before switching active player the method performs the move suggested by the received action
	 * This method is called from the game object's getresult method
	 * The method performs the action and then a new set of actions are produced, and the analyzeutility function changes
	 * the value of the state's utility based on the new set of actions.
	 * Once this has been performed, the active player is switched.
	 * Question: How to perform a given number of moves and then store these such that
	 * the preferred player executes them and waits for the opponents move???
	 * @since 13.11 2019: Only active pieces are used and analyzed - removed !!!
	 * Also if piece is passive (taken) then no movement takes place and active player does not change
	 * @since 18.12 2019:
	 * If the action's piece is a pawn and it is blocked then this action is not considered
	 */
	public void mark(ChessAction action) {
		boolean blocked = false;
		ChessActionImpl localAction = (ChessActionImpl) action;
		AgamePiece piece = (AgamePiece) action.getChessPiece();
		ChessPieceType pieceType = piece.getChessType();
		if (pieceType instanceof APawn) {
			APawn pawn = (APawn) pieceType;
			blocked = pawn.isBlocked();
			if (localAction.isStrike())
				blocked = false;
		}
		Position position = (Position) action.getPreferredPosition();
		List<Position> availablePositions = (List<Position>) action.getAvailablePositions();
		if (!blocked && position != null && piece.getmyPosition() != null) {
//			if (piece.isActive()) { // Should this be reopened again??
				game.movePiece(piece, position);
//				game.movePiece(piece.getMyPosition().getXyloc(),position.getXyloc()); // Performes the move: 
//				The piece is moved to the new location on the chessboard held by the AbstractChessGame
				
				game.getGamePlayer().createMove(piece, piece.getMyPosition(), position,movements); // Creates a local list of movements
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

//			}
		}


//		if (piece.isActive()) { The getActions method checks if piece is active
			actions = getActions();
			analyzeutility();
			playerTomove.setActions(actions);
//		}
	
	}
	/**
	 * analyzeutility
	 * This method finds an action with a preferred position and the highest ranking piece
	 * Then the utility is the value of the rank of this piece.
	 * if it is early in the game and the piece is a pawn, then the rank is multiplied by a factor
	 * How to determine if the state is a final state? 
	 * @since 13.11 2019 Only active pieces are used and analyzed
	 * 
	 */
	private void analyzeutility() {
		int noofmoves = 0;
		int factor = 1;
		for (ChessAction action:actions) {
			if (action.getPreferredPosition()!= null) {
				AgamePiece piece = (AgamePiece) action.getChessPiece();
				String pieceName = piece.getMyPiece().getPieceName();
				List<ApieceMove> movements = game.getGamePlayer().getMovements();
				if (movements != null && !movements.isEmpty()) {
					noofmoves = movements.size() - 1;
				}
				ChessPieceType pieceType = piece.getChessType();
				if (pieceType instanceof APawn && noofmoves < 3)  {
		 
					factor = 10;
					int col = piece.getMyPosition().getXyloc().getXCoOrdinate();
					if (col > 3 && col < 5)
						factor = 2 +factor*col; 
				}
				int temputil = piece.getMyPiece().getValue() + factor;
				if (temputil > utility) {
					utility = temputil;
					chessAction = (ChessActionImpl) action;
				}
			}
			if (chessAction == null) {
				chessAction = (ChessActionImpl) actions.get(0);
			}
		}

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
	 * Only active pieces is involved in producing new actions.
	 * Each action also calculates which reachable positions are occupied by other pieces belonging to the same player.
	 * @since 13.11.2019 Only active pieces are considered
	 * @return
	 */
	@Override
	public List<ChessAction> getActions(){
		actions = null;
		List<ChessAction> actions = new ArrayList<ChessAction>();
		ArrayList<AgamePiece> pieces = playerTomove.getMygamePieces();
		for (AgamePiece piece : pieces) {
			if (piece.isActive()) {
				HashMap<String,Position> reachablePositions = piece.getReacablePositions();
				ChessAction action = new ChessActionImpl(reachablePositions,piece,playerTomove);
				actions.add(action);
			}
		}
		return actions;
		
	}


	@Override
	public ChessAction getAction() {
		
		return chessAction;
	}

}
