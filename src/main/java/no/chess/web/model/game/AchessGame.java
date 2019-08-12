package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import aima.core.util.datastructure.XYLocation;
import no.chess.web.model.ChessBoard;
import no.chess.web.model.ChessPiece;
import no.chess.web.model.Position;
import no.games.chess.AbstractChessGame;
import no.games.chess.ChessAction;
import no.games.chess.ChessPlayer;
import no.games.chess.ChessState;

/**
 * AChessGame inherits from the abstract class AbstractChessgame
 * It is created when the user wants to play a game of chess.
 * It transfers the current positions of the chess pieces to the AIMA chessboard
 * It also creates a AgamePiece for every chess piece found from the ontology game
 * and places these pieces in an array called piecesonBoard.
 * It also creates a white player and a black player and gives the correct chesspieces to each of the players.
 * It finally creates an initial state of the game with the initial gameboard and the two players
 * This initial state calculates all available actions for this state.
 * @author oluf
 *
 */
public class AchessGame extends AbstractChessGame {
	private AgameBoard gameBoard;
	private HashMap<String, Position> positions;
	private ArrayList<Position> usedPositionlist;
	private ArrayList<AgamePiece> piecesonBoard;

	private ChessBoard myFrontBoard; // The front chessboard to display board and pieces
	
	public AchessGame(int size,HashMap<String, Position> positions,ChessBoard frontBoard) {
		super(size);
		gameBoard = new AgameBoard(positions);
		this.positions = positions;
		piecesonBoard = new ArrayList();
		tranferBoard();

		chessState = new ChessStateImpl(this, gameBoard,(APlayer)whitePlayer,(APlayer)blackPlayer);
	}

	/**
	 * transferBoard
	 * This method transfers piece positions to the aima chessboard
	 * It also creates all available pieces determine their type and calculates all reachable positions
	 * And it creates a white player and a black player with their available pieces.
	 */
	public void tranferBoard() {
		usedPositionlist = (ArrayList<Position>) gameBoard.getUsedPositionlist();
		StringBuilder builder = new StringBuilder();
		whitePlayer = (ChessPlayer) new APlayer();
		whitePlayer.setPlayerName( whitePlayer.getWhitePlayer());
		blackPlayer = (ChessPlayer) new APlayer();
		blackPlayer.setPlayerName(blackPlayer.getBlackPlayer());
		for (Position position:usedPositionlist) {
			XYLocation loc = position.getXyloc();
			String pieceName = position.getUsedBy().getName();
			ChessPiece piece = position.getUsedBy();
			AgamePiece<Position> gamePiece = new AgamePiece(position,piece);
			
			if(gamePiece.getColor().equals("w")) {
				whitePlayer.getMygamePieces().add(gamePiece);
			}
			if(gamePiece.getColor().equals("b")) {
				blackPlayer.getMygamePieces().add(gamePiece);
			}
			piecesonBoard.add(gamePiece);
			addPieceAt(loc);
			addPieceAtPos(loc, pieceName);
			builder.append(gamePiece.toString());
			builder.append("\n");
		}
		System.out.println(getBoardPic());
		System.out.println(builder.toString());
	}
	public void movePiece(XYLocation from, XYLocation to) {
		
		super.movePiece(from, to);
		gameBoard.setusedunused();
	}
	public APlayer getWhitePlayer() {
		return (APlayer) whitePlayer;
	}

	public void setWhitePlayer(APlayer whitePlayer) {
		this.whitePlayer = (ChessPlayer) whitePlayer;
	}

	public APlayer getBlackPlayer() {
		return (APlayer) blackPlayer;
	}

	public void setBlackPlayer(APlayer blackPlayer) {
		this.blackPlayer = blackPlayer;
	}

	public ChessBoard getMyFrontBoard() {
		return myFrontBoard;
	}

	public void setMyFrontBoard(ChessBoard myFrontBoard) {
		this.myFrontBoard = myFrontBoard;
	}

	public AgameBoard getGameBoard() {
		return gameBoard;
	}

	public void setGameBoard(AgameBoard gameBoard) {
		this.gameBoard = gameBoard;
	}

	public HashMap<String, Position> getPositions() {
		return positions;
	}

	public void setPositions(HashMap<String, Position> positions) {
		this.positions = positions;
	}

	public ArrayList<Position> getUsedPositionlist() {
		return usedPositionlist;
	}

	public void setUsedPositionlist(ArrayList<Position> usedPositionlist) {
		this.usedPositionlist = usedPositionlist;
	}

	/**
	 * getActions
	 * This method returns a list of all possible moves all available pieces 
	 * for the active player
	 * can make from the current state
	 */
	@Override
	public List<ChessAction> getActions(ChessState chessState) {
		
		return chessState.getActions();
	}

	@Override
	public ChessState getInitialState() {
		
		return chessState;
	}

	@Override
	public ChessPlayer getPlayer(ChessState chessState) {
		// TODO Auto-generated method stub
		return chessState.getPlayerTomove();
	}

	@Override
	public ChessPlayer[] getPlayers() {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * getResult
	 * This method returns a new state as a result of a chosen action 
	 * 	
	 */
	@Override
	public ChessState getResult(ChessState chessState, ChessAction action) {
		((ChessStateImpl) chessState).mark();
		return chessState;
	}

	@Override
	public double getUtility(ChessState state, ChessPlayer player) {
		
		return state.getUtility();
	}

	@Override
	public boolean isTerminal(ChessState state) {
		
		return state.getUtility() != -1;
	}
	
}
