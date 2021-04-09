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
import aima.core.logic.planning.State;
import aima.core.search.adversarial.AdversarialSearch;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import aima.core.search.adversarial.MinimaxSearch;
import no.chess.ontology.BlackPiece;
import no.chess.ontology.WhitePiece;
import no.chess.web.model.game.AChessAgent;
import no.chess.web.model.game.APlayer;
import no.chess.web.model.game.AchessGame;
import no.chess.web.model.game.AgamePiece;
import no.chess.web.model.game.ApieceMove;
import no.chess.web.model.game.ChessActionImpl;
import no.chess.web.model.game.ChessKnowledgeBase;
import no.chess.web.model.game.ChessStateImpl;
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
	private HashMap<String,Position> positions; // The original HashMap of positions
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
	private ChessStateImpl activeState; // Is the active state of the game; a node in the game tree
	private AChessAgent chessAgent = null;
	private ChessKnowledgeBase kb = null;
	private State deferredInitial = null;
	private State deferredGoal = null;
	private Map<String,State>deferredGoalstates = null;
	public PlayGame(HashMap<String, Position> positions,ChessBoard frontBoard)   {
		super();
		this.myFrontBoard = frontBoard;
		this.positions = positions;
		positionlist = new ArrayList(positions.values());
		setusedunused();
		availablePositions = new HashMap();
		availablePositionlist = new ArrayList();
		game = new AchessGame(8,positions,myFrontBoard);
		game.setGamePlayer(this); // Creates the initial state. This is the only place where the ChessState object is created
// The ChessState object also implements the Percept interface		
		currentState = game.getInitialState();
		movements = new ArrayList<ApieceMove>();
		game.setMovements(movements);
//		kb = new ChessKnowledgeBase();
	}

	public Map<String, State> getDeferredGoalstates() {
		return deferredGoalstates;
	}

	public void setDeferredGoalstates(Map<String, State> deferredGoalstates) {
		this.deferredGoalstates = deferredGoalstates;
	}

	public State getDeferredInitial() {
		return deferredInitial;
	}

	public void setDeferredInitial(State deferredInitial) {
		this.deferredInitial = deferredInitial;
	}

	public State getDeferredGoal() {
		return deferredGoal;
	}

	public void setDeferredGoal(State deferredGoal) {
		this.deferredGoal = deferredGoal;
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
	
	public ChessStateImpl getActiveState() {
		return activeState;
	}

	public void setActiveState(ChessStateImpl activeState) {
		this.activeState = activeState;
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
		ChessStateImpl stateImpl = (ChessStateImpl) currentState;
		activeState = stateImpl;
		AdversarialSearch<ChessState<GameBoard>, ChessAction<?, ?, ?,  GamePiece<?>, ?>> search; // FILL IN !!!!
//		ChessSearch<ChessState,ChessAction> search;
		search = ChessAlphaBetaSearch.createFor(game, 0.0, 1.0, 1); // Changed timer from 2 to 1.
		search = (ChessAlphaBetaSearch)search;
//		search = ChessAlphaBetaSearch.createFor(game, 0.0, 1.0, 2);
//		search = ChessSearchImpl.createFor(game, 0.0, 1.0, 5);
//		search = MinimaxSearch.createFor(game);
// 		search.logEnabled(true);	

/*
 * The makeDecision method returns either at timeout or when the state is terminal
 * (a lose or a win for the active player).	
 * The makeDecision method makes and creates a number of moves and returns the top action from a set of actions 
 * that has been performed
 * All the moves that are made is a result of the getResult method of the game object.
 * 13.11.2019 : makeDecision must not return an action that has an inactive piece. 
 */
		
/*		ChessAction newAction = search.makeDecision(currentState);
		ChessActionImpl localAction = (ChessActionImpl) newAction;*/// Call to makeDecision removed 04.03.21 
		
		
/*
 * For every move a new knowledge base and agent must be created		
 */
		ChessActionImpl localAction = null;
		kb = null;
		kb = new ChessKnowledgeBase();
		kb.setStateImpl(stateImpl); // The knowledge base receives the percepts of the the game which is the state of the game
		chessAgent = null;
		chessAgent = new AChessAgent(kb,localAction,this);
//		chessAgent.execute(currentState); // Creates new knowledge for the knowledge base
		localAction = (ChessActionImpl) chessAgent.execute(currentState); // Creates new knowledge for the knowledge base and determines the next move.
// The next move is in the returned action.		
		
/*		if (localAction != newAction)
			newAction = localAction;*/
		
		ApieceMove chosenMove = localAction.getPossibleMove();

		Position movPos = chosenMove.getToPosition();
		APlayer playerTomove = stateImpl.getMyPlayer();
	
		writer.println("Player to move "+playerTomove.getPlayerName()+" "+playerTomove.getPlayerId()); 
		if (playerTomove.getPlayerName() == playerTomove.getBlackPlayer()) {
			writer.println("Wrong player "+playerTomove.getPlayerName());
		}
		currentState.setAction(localAction);
/*		currentState.setAction(newAction); // Set state action to action to be performed
*/
//		String a = newAction.toString();
/*		List<ChessAction> actions = currentState.getActions();
		ChessAction action = actions.get(0);*/
		writer.println("Before call to emptymovements \n"+game.getBoardPic());
/*		for (Position pos:positionlist) {
			if (pos.getPositionName().equals("a3")) {
				writer.println("!!Playgame position!! "+pos.toString());
			}
		}*/
		List<ApieceMove> stateMoves = stateImpl.getMovements();
/*		writer.println("State moves\n"); // OBS: 01.05.20 state moves are always empty !!
		for (ApieceMove stateMove : stateMoves) {
			writer.println(stateMove.toString());
			Position pos = stateMove.getFromPosition();
			AgamePiece piece = stateMove.getPiece();
			AgamePiece posPiece = null;
			if (pos.isInUse())
				posPiece = pos.getUsedBy().getMyPiece();
			if (posPiece != null && piece != posPiece) {
				AgamePiece removed = pos.getRemoved().getMyPiece();
				ChessPiece removedfromstack = pos.getRemovedPieces().pop();
				AgamePiece removedGamepiece = removedfromstack.getMyPiece();
				writer.println("Move piece different from position piece "+piece.toString()+" Position piece "+posPiece.toString() );
				if (removed != null)
					writer.println("Removed piece "+ removed.toString());
			}
			
			
		}*/
		stateImpl.setChosenMove(chosenMove);
	
//		AgamePiece piece = (AgamePiece) newAction.getChessPiece();
		AgamePiece piece = (AgamePiece) localAction.getChessPiece();
/*
 * Keeps track of move numbers and the number of moves		
 */
		piece.setNofMoves(0);

		if (!piece.isActive()) {
			writer.println("Chosen action has a passive piece "+ piece.toString() );
		}
		currentState.emptyMovements(); // empty all movements before the chosen action and move.
		if (piece.isActive()) {
			writer.println("Passive piece set active"+ piece.toString() );
		}		
//		Position position = (Position) newAction.getPreferredPosition();
		Position position = (Position) localAction.getPreferredPosition();
		if (position != movPos) {
			writer.println("Preferred position different from move position "+ position.toString()+" Move Position: "+movPos.toString() );
			
		}
/*		List<Position> availablePositions = (List<Position>) newAction.getAvailablePositions();
		if (position == null)
			position = availablePositions.get(0); // Should not happen !!!
*/		
/*		Caused by: java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
	at java.util.ArrayList.rangeCheck(Unknown Source)
	at java.util.ArrayList.get(Unknown Source)
	at no.chess.web.model.PlayGame.proposeMove(PlayGame.java:232)
	
 * List<Position> availablePositions = (List<Position>) newAction.getAvailablePositions(); 
		Position position = availablePositions.get(0);*/
		
		
/*
 * At this call the holds the correct from position !!!!		
 */
		writer.println("Proposemove\n"+piece.toString()+"Action\n"+localAction.toString());
/*
 * The chosen action must be verified. OLJ 28.02.20:
 * This must be done as follows:
 *  1. The current state must be emptied of all movements made during the makeDecision process.
 *  2. New available positions and removed positions must be calculated for the piece belonging to the action.
 *  3. Based on this, the preferable position of the action must be changed, - if necessary. 
 * 		
 */
//		verifyAction(localAction, piece, position, playerTomove); Noty Used !!!
		String newPos = position.getPositionName();
		String pieceName = piece.getMyPiece().getName();
		Position piecePos = piece.getMyPosition();
		writer.println("Proposemove The piece start position:\n"+ piecePos.toString()+"\nPosition contains: "+piecePos.getUsedBy().toString());
		Position oldPosition = null;
/*
 * position is preferred position.
 * Does it contain a piece?
 */
		AgamePiece activeGamePiece = null;
		ChessPiece activePiece = position.getUsedBy();
		if (activePiece != null)
			activeGamePiece = activePiece.getMyPiece();
		
		if (activeGamePiece != null && activeGamePiece == piece) {
			writer.println(" Chosen Piece must be cleared from suggested position "+piece.toString()+"\n");
//			currentState.clearMovements(piece); // This creates problems !!!! The emptymovements method is ok. !!!
//			position.returnPiece();
			piecePos = piece.getMyPosition();
		}else {
			writer.println(" == Gamepieces are not the same "+piece.toString()+"\n"+"In position "+position.toString()+"\n");
			// This indicate an opponent piece that must be removed in case position is occupied !!!
			// 31.01 2020 Removes also black Knight !!!
			if (activeGamePiece != null) {
				writer.println("Taken piece: "+ activeGamePiece.toString()+"\n");
/*				activeGamePiece.setActive(false); This must be done after call to determineMove OJN 3.02.20
				activeGamePiece.setMyPosition(null);
				position.setUsedBy();*/
//				currentState.clearMovements(piece);
//				position.returnPiece();
				piecePos = piece.getMyPosition();
			}
		}
			
		writer.println("After call to emptymovements \n"+game.getBoardPic());
/*
 * These two statements executed before call to emptyMovements	
 * OBS!!! oldpos and newpos are the same. the .emptymovewments clears this !!!	
 */
		String oldPos = piece.getMyPosition().getPositionName();
		oldPosition = piece.getMyPosition();
		if (position == piecePos) {
			Position heldPosition = piece.getHeldPosition();
			if (heldPosition == null) {
				piece.restorePosition();
				heldPosition = piece.getmyPosition();
			}
			oldPosition = heldPosition;
			oldPos = oldPosition.getPositionName();
		}

		// This call must be carried out before check of activegamepiece !!!
//		 writer.println("Positionlist before determinemove removed \n");
/*		 for ( Position pos : positionlist) {
			 writer.println(pos.toString());
		 }*/
		myFrontBoard.determineMove(oldPos, newPos, pieceName); // New fen is created based on this. This is done after the call to proposemove
// The determinemove method sets new position Name in chesspiece !!
	    piece.setMyPosition(position); // position is the preferred position from action This is the new position of the piece
//	    piece.setHeldPosition(null); // Then there are no previous positions to restore from 
	    
//		currentState.emptyMovements(); // empty all movements before the chosen action and move.
	    writer.println("After call to board.determineMove \n"+game.getBoardPic()); // OK 

//		piece.setHeldPosition(position); // New position to the position to restore to removed olj 10.07.20 !!!
		position.setUsedandRemoved(piece.getMyPiece()); // THe preferred position: Sets chesspiece to new position and also sets it in the removed list
//		myFrontBoard.determineMove(oldPos, newPos, pieceName); // New fen is created based on this
//		Position newPosition = myFrontBoard.findPostion(newPos);
	

//		clearMoves();
//	    writer.println(game.getBoardPic());
	    clearChessboard();
		 writer.println("After call to clearchessboard \n"+game.getBoardPic());
/*
 * OBS: Move from old to new position	!!! OJN 3.12.19    
 */
	    game.movePiece(piece, position," Playgame"); // This call creates the move on the aima chessboard

//		game.movePiece(piece.getMyPosition().getXyloc(),position.getXyloc()); // The piece is moved to the new location on the chessboard held by the AbstractChessGame
		
	    writer.println("After call to game.movepiece \n"+game.getBoardPic());
		
		createMove(piece,oldPosition, position);

		HashMap<String,ApieceMove> myMoves = stateImpl.getMyPlayer().getMyMoves();
		int index = movements.size();
		ApieceMove lastMove = movements.get(index-1);
		String moveNot = lastMove.getMoveNotation(); // OBS move notation is not set !!!
		myMoves.put(moveNot, lastMove);
		Integer moveNumber = new Integer(lastMove.getMoveNumber());
		piece.getMoveNumbers().add(moveNumber);
		checkCastling(stateImpl.getMyPlayer());
		stateImpl.switchActivePlayer(); // 16.04.20 After a move, must switch active player
//		localAction.getActions(playerTomove); // Added 24.02.20 When a move has been made then the pieces belonging to the same player must get new
//available positions calculated		
		game.createNewboard(); // A new set of usedunused lists are created.
		 writer.println("After call to game.createnewboard \n"+game.getBoardPic()+"\n");
/*		 for ( Position pos : positionlist) {
			 writer.println(pos.toString());
		 }*/
		 writer.close();
	}
	private void checkCastling(APlayer player) {
		ChessActionImpl localAction = (ChessActionImpl) chessAgent.getCastleAction();
		if (localAction != null) {
			AgamePiece castle = localAction.getChessPiece();
			Position castlePos = localAction.getPreferredPosition();
   	    	if (castle != null) {
   	    		Position castlePosfrom = castle.getHeldPosition();
   	    		if (castlePosfrom == null)
   	    			castlePosfrom = castle.getMyPosition();
   	    		String fromPos = castlePosfrom.getPositionName();
   	    		String toPos = "";
   	    		String castleName = castle.getName();
   	    		if (castlePos != null) {
   	    			toPos = castlePos.getPositionName();
   	    			myFrontBoard.determineMove(fromPos, toPos, castleName); // Determine if move is legal
   	    			castle.setNofMoves(0);
   	    			castle.setMyPosition(castlePos);
   	    			castle.produceLegalmoves(castlePos);
   	    			player.calculatePreferredPosition(castle, localAction);
   	    		}
   	    		
   	    	}
		}
	}
	/**
	 * verifyAction
	 * This method is used to verify the action chosen by the search object
	 * If the action has a preferable position that is occupied by a friendly piece
	 * an alterative position must be chosen.
	 * @deprecated
	 * @param action
	 * @param piece
	 * @param position
	 * @param playerTomove
	 */
	private void verifyAction(ChessActionImpl action,AgamePiece piece,Position position,APlayer playerTomove) {
		action.getActions(playerTomove);
		List<Position> availablePositions = (List<Position>) action.getAvailablePositions();
		List<Position>  removedPos = (List<Position>)action.getPositionRemoved();
		/*
		 * Added 24.02.20		
		 * When a move has been made then the pieces belonging to the same player must get new
		 * available positions calculated				
		 */
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
		if (removed) {
			writer.println("Piece preferable position is occupied by friendly piece:\n"+piece.toString()+"\n Position "+position.toString());
//			Double evaluation = new Double(0);
		    writer.close();
//			return evaluation;
		}		
	}
	/**
	 * createMove
	 * This method creates a move based on a move carried out in the during the search.makedecision call.
	 * The game piece to be moved calculates new available positions from the new position it is moved to.
	 * This method is called from the chessState object during the makeDecision process
	 * @deprecated Not used by chessstate olj 01.05.20
	 * @param piece The game piece moved
	 * @param from The from Position
	 * @param to The to Position
	 * @param moves A temporary list of moves created in the chessState object
	 */
	public void createMove(AgamePiece piece,Position from,Position to,List<ApieceMove> moves) {
		int noofMoves = 0;
		String algebraicmove = "";
/*		ArrayList<ChessMoves> amoves = myFrontBoard.getChessMoves();
		if (amoves != null && !amoves.isEmpty()) {
			int ll = amoves.size();
			ChessMoves move = amoves.get(ll-1);
			algebraicmove = move.getBlackMove();
			if (algebraicmove.equals(""))
				algebraicmove = move.getWhiteMove();
			
		} This procedure is not necessary here*/
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
		 writer.println("Creating a move with the makeDecision call: Piece "+piece.toString()+" is moved to \n"+to.toString()+"\n");
		moves.add(pieceMove);
			
	}
	/**
	 * createMove
	 * This method creates a move based on a move carried out in the proposeMove method or by the opponent
	 * The game piece to be moved calculates new available positions from the new position it is moved to.
	 * This method is called from the proposemove method
	 * The method is also called when the opponent player makes a move (from RapporterChessStartServerResource)
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
		algebraicmove = myFrontBoard.getAlgebraicNotation();
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
