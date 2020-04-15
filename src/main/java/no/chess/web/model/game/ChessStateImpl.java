
package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import no.chess.web.model.ChessPiece;
import no.chess.web.model.Position;
import no.games.chess.ChessAction;
import no.games.chess.ChessPieceType;
import no.games.chess.ChessPlayer;
import no.games.chess.ChessState;
import no.games.chess.GamePiece;


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
	private List<Position> allPositions;
	private int[][] squares;
	private String[][] piecePosition;
	private APlayer whitePlayer;
	private APlayer blackPlayer;
	private APlayer playerTomove = null;
	private APlayer myPlayer = null; // The player that PlayGame represent At present: can only represent the white player
	private ChessActionImpl chessAction; // This is the preferred action for this state
	private List<ChessAction> actions;
	private AchessGame game;
	private ApieceMove chosenMove;
	private AgamePiece playedPiece = null; // This piece is the active piece
	private double utility = -1; // 1 = win for White player, 0 = win for black player, 0.5 is a draw.
	private double orginalUtility = -1;
	private Stack<Double> utilityStack;
//  							The utility is only changed through the call to the analyzeutility function	
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
		allPositions =  new ArrayList(positions.values());
		
	}

	public ChessStateImpl(HashMap<String, Position> positions,int[][] squares,String[][] piecePosition) {
		super();
		this.positions = positions;
		allPositions =  new ArrayList(positions.values());
		this.squares = squares;
		this.piecePosition = piecePosition;
		gameBoard = new AgameBoard(this.positions);
		utilityStack = new Stack();
		Double utilValue = new Double(utility);
		utilityStack.push(utilValue);
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
		allPositions =  new ArrayList(positions.values());
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
		utilityStack = new Stack();
		Double utilValue = new Double(utility);
		utilityStack.push(utilValue);
	}
	
	
	public ApieceMove getChosenMove() {
		return chosenMove;
	}
	public void setChosenMove(ApieceMove chosenMove) {
		this.chosenMove = chosenMove;
	}
	public List<ApieceMove> getMovements() {
		return movements;
	}
	public void setMovements(List<ApieceMove> movements) {
		this.movements = movements;
	}
	/* 
	 *emptyMovements
	 * This method clears all movements made during a makeDecision process
	 * and restores available positions in accordance with the piece's position.
	 * It then produces new legal moves for all involved pieces.
	 * It is called from the PlayGame object before producing the next move.
	 */
	public void emptyMovements() {
		checkPlayers();
		for (Position pos:allPositions) {
			if (pos.getPositionName().equals("a3")||pos.getPositionName().equals("f3")) {
				System.out.println("!!ChessState emptymovement 1  position!! "+pos.toString());
			}
		}	
		boolean moveChange = false;
		for (ApieceMove move:movements) {
			AgamePiece piece = move.getPiece();
			Position toPos = move.getToPosition();
			Position fromPos = move.getFromPosition();
		
//			Position pos = move.getPiece().getmyPosition();
			Position pos = move.getPiece().getHeldPosition();
			if (pos == null)
				pos = move.getPiece().getMyPosition();
	
			ChessPiece chp = piece.getMyPiece();
			fromPos.returnPiece(chp);
//			piece.restorePosition(); // An alternative to .setMyposition
			piece.setMyPosition(fromPos);
		
			if (toPos.getUsedBy() == fromPos.getUsedBy()){
				System.out.println("!!ChessState emptymovement return piece of movement "+move.toString());
				toPos.returnPiece();
				moveChange = true;
			}
			if (!moveChange){
				System.out.println("!!ChessState emptymovement NO return piece of movement "+move.toString());
				toPos.returnPiece();
			}
			moveChange = false;
//			piece.setActive(true); // Always set piece active again !!!!
//			if (piece.isActive())
			piece.produceLegalmoves(pos);
			if (!utilityStack.isEmpty())
				utility = utilityStack.pop();
		}
		movements.clear();
		utility = orginalUtility;
		for (Position pos:allPositions) {
			if (pos.getPositionName().equals("a3")||pos.getPositionName().equals("f3")) {
				System.out.println("!!ChessState emptymovements 2 position!! "+pos.toString());
			}
		}
//		checkPlayers();
	}

	/**
	 * checkPlayers
	 * This method restores any removed pieces in a search for the best move
	 * It is called from the emptyMovements method
	 */
	public void checkPlayers() {
		playedPiece = chessAction.getChessPiece();
//		chessAction.getPreferredPosition();
		List<AgamePiece> pieces = whitePlayer.getMygamePieces();
		
		for (AgamePiece piece:pieces) {
			Position toPosition = piece.getmyPosition();
			if (!piece.isActive()) {
				piece.getMyPosition().returnPiece();
//				piece.setActive(true);
//				piece.restorePosition();
//				piece.restoreValue();
			}
/*
 * This call restores the piece to the last held position			
 */
			if (playedPiece != piece)
				piece.restorePosition(); // Restore positions for all pieces OBS !!!! 
/*
 * If we parameterize this then we can show on board all movements
 * Then if the piece is restored to a previous position the position held by the piece
 * must be restored as well
 */
			if (piece.getMyPosition() != toPosition && playedPiece != piece ) {
				toPosition.returnPiece();
			}
		
//			piece.produceLegalmoves(pos); // Should it also produce new legal moves ??
		}
		List<AgamePiece> blackpieces = blackPlayer.getMygamePieces();
		for (AgamePiece piece:blackpieces) {
			Position toPosition = piece.getmyPosition();
			if (!piece.isActive()) {
				piece.getMyPosition().returnPiece();
//				piece.setActive(true);
//				piece.restorePosition();
//				piece.restoreValue();
			}
			if (playedPiece != piece)
				piece.restorePosition(); // Restore positions for all pieces
			if (piece.getMyPosition() != toPosition && playedPiece != piece ) {
				toPosition.returnPiece();
			}
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
	 * This method is only called at startup of game
	 */
	public void returnMyplayer() {
		if(playerTomove.getPlayerName() == playerTomove.getBlackPlayer()) {
			whitePlayer.setActive(true);
			blackPlayer.setActive(false);
		}
		playerTomove = whitePlayer;
		actions = getActions();
		playerTomove.setActions(actions);
		analyzeutility();
	}
	private boolean checkToposition(ApieceMove move) {
		boolean result = false;
		Position  to = move.getToPosition();
		AgamePiece movePiece = move.getPiece();
		AgamePiece occupant = null;
		if (to.getUsedBy() != null) {
			occupant = to.getUsedBy().getMyPiece();
			if (movePiece != occupant) {
				ArrayList<AgamePiece> pieces = playerTomove.getMygamePieces();
				for (AgamePiece piece : pieces) {
					if (occupant == piece) {
						result = true;
						break;
					}
				}
			}

		}
		Position piecePos = movePiece.getMyPosition();
		if (occupant == null && piecePos != to) {
			result = true;
		}
		return result;
	}
	/**
	 * mark
	 * @since 8.04.20
	 * This method is reworked. the old method is stored in the file statemarkmethod.java
	 * The action under consideration has produced a possible move for this action.
	 * This move is used and placed in the list of moves that is being considered during the 
	 * search object makeDecision process.
	 * 
	 * This method changes the state by switching active player
	 * and creating new set of actions available for the active player
	 * Before switching active player the method performs the move suggested by the received action.
	 * This move is held in the actionMove object.
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
		List<ApieceMove> playedMovements = game.getMovements();
		ChessActionImpl localAction = (ChessActionImpl) action;
		ApieceMove actionMove = localAction.getPossibleMove(); // The action Move is the move suggested by the action
		// under consideration
		AgamePiece piece = (AgamePiece) action.getChessPiece();
		ChessPieceType pieceType = piece.getChessType();
		int noofmoves = 0;
/*
 * Does the move to position contain a piece?
 * and is this piece a friendly piece?
 * Then this move must not be performed?
 */


		Position prefPos = localAction.getPreferredPosition();
		if (prefPos != null) {
			if (prefPos.getPositionName().equals("a3")) {
				System.out.println("!!ChessState mark!!"+prefPos.toString()+"\n"+localAction.getChessPiece().toString());
			}
		}
		
		if (pieceType instanceof APawn) {
			APawn pawn = (APawn) pieceType;
			blocked = pawn.isBlocked();
			if (localAction.isStrike())
				blocked = false;
		}
		Position position = (Position) action.getPreferredPosition();
		List<Position> availablePositions = (List<Position>) action.getAvailablePositions();
		List<Position>  removedPos = (List<Position>)action.getPositionRemoved();
/*
 * Added 24.02.20		
 * When a move has been made then the pieces belonging to the same player must get new
 * available positions calculated				
 */
		boolean occupied = false;
		if (actionMove != null)
				occupied = checkToposition(actionMove);
		boolean available = false;
		boolean removed = false;
		for (Position pos:availablePositions) {
			if (position == pos) {
				available = true;
				break;
			}
		}
		for (Position pos:removedPos) {
			if (position == pos) {
				removed = true;
				break;
			}
		}
/*
 * end added		
 */
		if (available && !removed && !blocked && !occupied && actionMove != null) {
//			if (piece.isActive()) { // Should this be reopened again??
/*
 * The piece is moved and a new set of used an not used positions are calculated:			
 */
				game.movePiece(piece, position); // Performes the move !!
				piece.produceLegalmoves(position); // Added 8.04.20 produces new available positions			
//				The piece is moved to the new location on the chessboard held by the AbstractChessGame
//				Position heldPosition = piece.getHeldPosition(); // Changed from getMyposition OLJ 2.03.20
//				Position heldPosition  
/*				if (heldPosition == null) {
					heldPosition = piece.getMyPosition();
					
				}*/
				
//				game.getGamePlayer().createMove(piece, heldPosition, position,movements); // Creates a local list of movements
				noofmoves = movements.size() - 1;
				actionMove.setMoveNumber(noofmoves+1);
				movements.add(actionMove);

				localAction.getActions(playerTomove); // Added 24.02.20 When a move has been made then the pieces belonging to the same player must get new
// available positions calculated				

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

//			} if piece is active to reopen again?
		}


//		if (piece.isActive()) { The getActions method checks if piece is active
			actions = getActions(); // A new set of actions are produced. Then these actions are analyzed
	
			playerTomove.setActions(actions);
			analyzeutility();
//		}
	
	}

	/**
	 * analyzeutility
	 * The state's utility function is an internalization of the performance measure.
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
				int temputil = calculateUtility(piece, noofmoves,(Position) action.getPreferredPosition());
/*				ChessPieceType pieceType = piece.getChessType();
				if (pieceType instanceof APawn && noofmoves < 3)  {
		 
					factor = 10;
					int col = piece.getMyPosition().getXyloc().getXCoOrdinate();
					if (col > 3 && col < 5)
						factor = 2 +factor*col; 
				}
				int temputil = piece.getMyPiece().getValue() + factor;
*/
				if (temputil > utility) {
					utilityStack.push(new Double(utility));
					utility = temputil;
					chessAction = (ChessActionImpl) action;
				}
			}
			if (chessAction == null) {
				chessAction = (ChessActionImpl) actions.get(0);
			}
		}

	}
	/**
	 * calculateUtility
	 * This method calculates the utility value for the current state given
	 * @param piece a gamepiece
	 * @param noofMoves the number of moves made so far
	 * @param preferredPosition and the preferred position of the action.
	 * The utility value represent the performance measure
	 * @return
	 */
	private int calculateUtility(AgamePiece piece, int noofMoves,Position preferredPosition) {
		int pieceValue = piece.getMyPiece().getValue();
		int posValue = 1;
		int gameFactor = 1;
		int temputil = 0;
		Position position = piece.getMyPosition();
		boolean leftHigh = position.isCenterlefthigh();
		boolean rightHigh = position.isCenterrighthigh();
		boolean leftLow = position.isCenterleftlow();
		boolean rightLow = position.isCenterrightlow();
		if (leftHigh || leftLow || rightHigh || rightLow) {
			posValue = 4;
		}
		ChessPieceType pieceType = piece.getChessType();
		if (pieceType instanceof APawn && noofMoves < 3)  {
			gameFactor = 10;
		}
		if (pieceType instanceof AQueen )  {
			gameFactor = 15;
		}
		if (pieceType instanceof ABishop )  {
			gameFactor = 12;
		}
		if (pieceType instanceof AKnight )  {
			gameFactor = 12;
		}
		if (pieceType instanceof ARook )  {
			gameFactor = 13;
		}
		if (pieceType instanceof Aking )  {
			gameFactor = 11;
		}
		temputil = posValue + pieceValue + gameFactor;
		
		return temputil;
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
	 * These actions contain an AgamePiece and its available (reachable) positions and a possible move.
	 * Only active pieces is involved in producing new actions.
	 * Each action also calculates which reachable positions are occupied by other pieces belonging to the same player.
	 * @since 13.11.2019 Only active pieces are considered
	 * @return
	 */
	@Override
	public List<ChessAction> getActions(){
		if (actions != null)
			actions.clear();
		actions = null;
		List<ChessAction> actions = new ArrayList<ChessAction>();
		playerTomove.emptyPositions(); //empties the positions held by the player's pieces
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
	/* clearMovements
	 * This method clears all movements for a given piece
	 * @see no.games.chess.ChessState#clearMovements(no.games.chess.GamePiece)
	 */
	@Override
	public void clearMovements(GamePiece piece) {
		AgamePiece localPiece = (AgamePiece) piece;
		Position toPosition = localPiece.getmyPosition();
		localPiece.restorePosition();
		if (localPiece.getMyPosition() != toPosition) {
			toPosition.returnPiece();
		}
		playedPiece = localPiece;

	}
	@Override
	public void setAction(ChessAction action) {
		this.chessAction = (ChessActionImpl) action;
		
	}

}
