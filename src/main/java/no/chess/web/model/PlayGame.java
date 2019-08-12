package no.chess.web.model;

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
import no.games.chess.ChessAction;
import no.games.chess.ChessState;

/**
 * This class is implemented to run a game of chess
 * It is created when the user has selected to play a game of chess.
 * It creates a AchessGame.
 * This AchessGame contains boardpositions from a given ontology game.
 * The AchessGame  transfers the current positions of the chess pieces to the AIMA chessboard
 * It also creates a AgamePiece for every chess piece found from the ontology game
 * and places these pieces in an array called piecesonBoard.
 * It also creates a white player and a black player and gives the correct chesspieces to each of the players.
 * It finally creates an initial state of the game with the initial gameboard and the two players.
 * 
 * When a piece is moved it is placed in the correct HashSet of moved pieces.
 * The piece remains there until it is free to move again.
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
	
	public PlayGame(HashMap<String, Position> positions,ChessBoard frontBoard) {
		super();
		this.myFrontBoard = frontBoard;
		this.positions = positions;
		positionlist = new ArrayList(positions.values());
		setusedunused();
		availablePositions = new HashMap();
		availablePositionlist = new ArrayList();
		game = new AchessGame(8,positions,myFrontBoard);
		currentState = game.getInitialState();
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
	 * Find the name of the piece to be moved (web.model.ChessPiece)
	 * Find the old position positionName
	 * Find the new position positionName
	 * use myfrontBoard.determineMove to make the move
	 * 
	 */
	public void proposeMove() {
		AdversarialSearch<ChessState,ChessAction> search;
		search = IterativeDeepeningAlphaBetaSearch.createFor(game, 0.0, 1.0, 2);
//		search = MinimaxSearch.createFor(game);
		search.setLogEnabled(true);
/*
 * The makeDecision method returns either at timeout or when the state is terminal
 * (a lose or a win for the active player).		
 */
		ChessAction newAction = search.makeDecision(currentState);
//		String a = newAction.toString();
/*		List<ChessAction> actions = currentState.getActions();
		ChessAction action = actions.get(0);*/
		AgamePiece piece = (AgamePiece) newAction.getChessPiece();
		Position position = (Position) newAction.getPreferredPosition();
		List<Position> availablePositions = (List<Position>) newAction.getAvailablePositions();
		if (position == null)
			position = availablePositions.get(0);
/*		List<Position> availablePositions = (List<Position>) newAction.getAvailablePositions();
		Position position = availablePositions.get(0);*/
		System.out.println(piece.toString());
		String newPos = position.getPositionName();
		String pieceName = piece.getMyPiece().getName();
		String oldPos = piece.getMyPosition().getPositionName();
		myFrontBoard.determineMove(oldPos, newPos, pieceName);
		game.movePiece(piece.getMyPosition().getXyloc(),position.getXyloc());
		
	}

	
}
