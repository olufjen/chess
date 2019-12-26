package no.chess.web.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import aima.core.environment.nqueens.NQueensBoard;
import aima.core.search.adversarial.AdversarialSearch;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import aima.core.search.adversarial.MinimaxSearch;
import no.chess.ontology.BlackPiece;
import no.chess.ontology.WhitePiece;
import no.chess.web.model.game.AchessGame;
import no.chess.web.model.game.AgamePiece;
import no.chess.web.model.game.ApieceMove;
import no.games.chess.ChessAction;
import no.games.chess.ChessAlphaBetaSearch;
import no.games.chess.ChessSearch;
import no.games.chess.ChessSearchImpl;
import no.games.chess.ChessState;
import no.games.chess.GameBoard;
import no.games.chess.GamePiece;
/**
 * This class is implemented to run a game of chess
 * It is created when the user has selected to play a game of chess.
 * It receives a HashMap of Positions with their chesspieces from the ontology
 * From this it creates
 * A list of used positions
 * A list of not used positions.
 * It creates a AchessGame. This object is given the HashMap of positions.
 * This AchessGame contains boardpositions from a given ontology game.
 * The AchessGame  transfers the current positions of the chess pieces to the AIMA chessboard
 * It also creates a AgamePiece for every chess piece found from the ontology game
 * and places these pieces in an array called piecesonBoard.
 * It also creates a white player and a black player and gives the correct chesspieces to each of the players.
 * It finally creates an initial state of the game with the initial gameboard and the two players.
 * The initial state creates a list of actions that are available for the chosen search algorithm.
 * 
 * When a piece is moved a new ApieceMove object is created and placed in the list over moves.
 * 
 * @author oluf
 *
 */
public class PlayGame {

	
	private HashSet<BlackPiece> movedblackPieces;
	private HashSet<WhitePiece> movedwhitePieces;
	private HashMap<String,Position> positions;
	private HashMap<String,Position> usedPositions;
	private HashMap<String,Position> notusedPositions;
	private HashMap<String,Position> availablePositions;
	private List<Position> usedPositionlist;
	private List<Position> notusedPositionlist;
	private List<Position> availablePositionlist;
	private List<Position> positionlist; // The original HashMap of positions as a list
	private AchessGame game;
	private ChessBoard myFrontBoard;
	private ChessState currentState;
	private List<ApieceMove> movements;
	private String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\positions.txt";
	private PrintWriter writer = null;
	private FileWriter fw = null;
	public PlayGame(HashMap<String, Position> positions,ChessBoard frontBoard)   {
		super();
		this.myFrontBoard = frontBoard;
		this.positions = positions;
		positionlist = new ArrayList(positions.values());
		setusedunused();
		availablePositions = new HashMap();
		availablePositionlist = new ArrayList();
		game = new AchessGame(8,positions,myFrontBoard);
		game.setGamePlayer(this);
		currentState = game.getInitialState();
		movements = new ArrayList<ApieceMove>();

	}

	public List<ApieceMove> getMovements() {
		return movements;
	}

	public void setMovements(List<ApieceMove> movements) {
		this.movements = movements;
	}

	public ChessBoard getMyFrontBoard() {
		return myFrontBoard;
	}

	public void setMyFrontBoard(ChessBoard myFrontBoard) {
		this.myFrontBoard = myFrontBoard;
	}

	public AchessGame getGame() {
		return game;
	}

	public void setGame(AchessGame game) {
		this.game = game;
	}

	public HashMap<String, Position> getUsedPositions() {
		return usedPositions;
	}

	public void setUsedPositions(HashMap<String, Position> usedPositions) {
		this.usedPositions = usedPositions;
	}

	public HashMap<String, Position> getNotusedPositions() {
		return notusedPositions;
	}

	public void setNotusedPositions(HashMap<String, Position> notusedPositions) {
		this.notusedPositions = notusedPositions;
	}

	public HashMap<String, Position> getAvailablePositions() {
		return availablePositions;
	}

	public void setAvailablePositions(HashMap<String, Position> availablePositions) {
		this.availablePositions = availablePositions;
	}

	public List<Position> getUsedPositionlist() {
		return usedPositionlist;
	}

	public void setUsedPositionlist(List<Position> usedPositionlist) {
		this.usedPositionlist = usedPositionlist;
	}

	public List<Position> getNotusedPositionlist() {
		return notusedPositionlist;
	}

	public void setNotusedPositionlist(List<Position> notusedPositionlist) {
		this.notusedPositionlist = notusedPositionlist;
	}

	public List<Position> getAvailablePositionlist() {
		return availablePositionlist;
	}

	public void setAvailablePositionlist(List<Position> availablePositionlist) {
		this.availablePositionlist = availablePositionlist;
	}

	public List<Position> getPositionlist() {
		return positionlist;
	}

	public void setPositionlist(List<Position> positionlist) {
		this.positionlist = positionlist;
	}

	public HashMap<String, Position> getPositions() {
		return positions;
	}
	public void setPositions(HashMap<String, Position> positions) {
		this.positions = positions;
	}

	public HashSet<BlackPiece> getMovedblackPieces() {
		return movedblackPieces;
	}
	public void setMovedblackPieces(HashSet<BlackPiece> movedblackPieces) {
		this.movedblackPieces = movedblackPieces;
	}
	public HashSet<WhitePiece> getMovedwhitePieces() {
		return movedwhitePieces;
	}
	public void setMovedwhitePieces(HashSet<WhitePiece> movedwhitePieces) {
		this.movedwhitePieces = movedwhitePieces;
	}
	
	public void setusedunused() {
		usedPositionlist = (List<Position>) ((List<Position>) positionlist).stream().filter(Position::isInUse).collect(Collectors.toList());
		notusedPositionlist = (List<Position>) ((List<Position>) positionlist).stream().filter(Position::notisInUse).collect(Collectors.toList());

//		notusedPositions = (List<Position>) ((List<Position>) positions).stream().filter(board.queenExistsAt(position.getXyloc())).collect(Collectors.toList());
	}
	/**
	 * proposeMove
	 * This method uses a chosen (aima) search algorithm to find the best next move
	 * The search object is created every time this method is called
	 * Find the name of the piece to be moved (web.model.ChessPiece)
	 * Find the old position positionName
	 * Find the new position positionName
	 * use myfrontBoard.determineMove to make the move
	 * The determineMove method checks if the move is legal, then accepts it and carries out the move
	 * The chess piece that is moved receives the new position, and the move is recorded as move in algebraic notation 
	 * After each move a new search object is created to make a new search on the current state.
	 */
	public void proposeMove() {
		fw = null;
		writer = null;
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	      writer = new PrintWriter(new BufferedWriter(fw));

		currentState = game.getInitialState();
		AdversarialSearch<ChessState<GameBoard>, ChessAction<?, ?, ?,  GamePiece<?>, ?>> search; // FILL IN !!!!
//		ChessSearch<ChessState,ChessAction> search;
		search = ChessAlphaBetaSearch.createFor(game, 0.0, 1.0, 2);
		
//		search = ChessAlphaBetaSearch.createFor(game, 0.0, 1.0, 2);
//		search = ChessSearchImpl.createFor(game, 0.0, 1.0, 5);
//		search = MinimaxSearch.createFor(game);
		search.setLogEnabled(true);
/*
 * The makeDecision method returns either at timeout or when the state is terminal
 * (a lose or a win for the active player).	
 * The makeDecision method makes and creates a number of moves and returns the top action from a set of actions 
 * that has been performed
 * All the moves that are made is a result of the getResult method of the game object.
 * 13.11.2019 : makeDecision must not return an action that has an inactive piece.
 */
		ChessAction newAction = search.makeDecision(currentState);
//		String a = newAction.toString();
/*		List<ChessAction> actions = currentState.getActions();
		ChessAction action = actions.get(0);*/
		AgamePiece piece = (AgamePiece) newAction.getChessPiece();
		Position position = (Position) newAction.getPreferredPosition();
		List<Position> availablePositions = (List<Position>) newAction.getAvailablePositions();
		if (position == null)
			position = availablePositions.get(0); // Should not happen !!!
		
/*		Caused by: java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
	at java.util.ArrayList.rangeCheck(Unknown Source)
	at java.util.ArrayList.get(Unknown Source)
	at no.chess.web.model.PlayGame.proposeMove(PlayGame.java:232)
	
 * List<Position> availablePositions = (List<Position>) newAction.getAvailablePositions();
		Position position = availablePositions.get(0);*/
		writer.println("Proposemove\n"+piece.toString()+"Action\n"+newAction.toString());
		String newPos = position.getPositionName();
		String pieceName = piece.getMyPiece().getName();
		String oldPos = piece.getMyPosition().getPositionName();
		Position oldPosition = piece.getMyPosition();
		currentState.emptyMovements(); // empty all movements before the chosen action and move.
		myFrontBoard.determineMove(oldPos, newPos, pieceName); // New fen is created based on this
//		Position newPosition = myFrontBoard.findPostion(newPos);

//		clearMoves();
	    writer.println(game.getBoardPic());
	    clearChessboard();
/*
 * OBS: Move from old to new position	!!! OJN 3.12.19    
 */
	    game.movePiece(piece, position," Playgame"); // This call creates the move on the aima chessboard
	    piece.setMyPosition(position); // position is the preferred position from action This is the new position of the piece

//		game.movePiece(piece.getMyPosition().getXyloc(),position.getXyloc()); // The piece is moved to the new location on the chessboard held by the AbstractChessGame
		
		game.createNewboard(); // A new set of usedunused lists are created.
		
		createMove(piece,oldPosition, position);
		 writer.println(game.getBoardPic());
		 writer.close();
	}
	/**
	 * createMove
	 * This method creates a move based on a move carried out in the during the search.makedecision call.
	 * The game piece to be moved calculates new available positions from the new position it is moved to.
	 * This method is called from the chessState object during the makeDecision process
	 * @param piece The game piece moved
	 * @param from The from Position
	 * @param to The to Position
	 * @param moves A temporary list of moves created in the chessState object
	 */
	public void createMove(AgamePiece piece,Position from,Position to,List<ApieceMove> moves) {
		int noofMoves = 0;
		String algebraicmove = "";
		ArrayList<ChessMoves> amoves = myFrontBoard.getChessMoves();
		if (amoves != null && !amoves.isEmpty()) {
			int ll = amoves.size();
			ChessMoves move = amoves.get(ll-1);
			algebraicmove = move.getBlackMove();
			if (algebraicmove.equals(""))
				algebraicmove = move.getWhiteMove();
			
		}
		if (moves.isEmpty()) {
			noofMoves = 0;
		}
		if (!moves.isEmpty()) {
			noofMoves = moves.size();
		}
		noofMoves++;
		piece.produceLegalmoves(to);
//		piece.getLegalmoves(to); // Create a new list of available position after move
		ApieceMove pieceMove = new ApieceMove(piece,from, to, noofMoves, algebraicmove);

		moves.add(pieceMove);
			
	}
	/**
	 * createMove
	 * This method creates a move based on a move carried out in the proposeMove method
	 * THe game piece to be moved calculates new available positions from the new position it is moved to.
	 * This method is called from the proposemove method
	 * THe method is also called when the opponent player makes a move (from RapporterChessStartServerResource)
	 * @param piece The game piece moved
	 * @param from The from Position
	 * @param to The to Position
	 */
	public void createMove(AgamePiece piece,Position from,Position to) {
		int noofMoves = 0;
		String algebraicmove = "";
		ArrayList<ChessMoves> moves = myFrontBoard.getChessMoves();
		if (moves != null && !moves.isEmpty()) {
			int ll = moves.size();
			ChessMoves move = moves.get(ll-1);
			algebraicmove = move.getBlackMove();
			if (algebraicmove.equals(""))
				algebraicmove = move.getWhiteMove();
			
		}
		if (movements.isEmpty()) {
			noofMoves = 0;
		}
		if (!movements.isEmpty()) {
			noofMoves = movements.size();
		}
		noofMoves++;
		piece.produceLegalmoves(to);
//		piece.getLegalmoves(to); // Create a new list of available position after move
		ApieceMove pieceMove = new ApieceMove(piece,from, to, noofMoves, algebraicmove);
		pieceMove.setMoveNumber(noofMoves);
		 writer.println("Creating piecemove "+pieceMove.toString());
		movements.add(pieceMove);
			
	}
	/**
	 * clearMoves
	 * This method clears the list containing ApieceMove
	 * 
	 */
	public void clearMoves() {
		if (!movements.isEmpty()) {
			movements.clear();
		}
	
	}
	/**
	 * clearChessboard
	 * This method clears the aima chessboard
	 */
	public void clearChessboard() {
		game.clear();
	}
	
}
