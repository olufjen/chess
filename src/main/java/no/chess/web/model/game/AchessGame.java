package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.protege.owl.codegeneration.WrappedIndividual;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

import aima.core.util.datastructure.XYLocation;
import no.chess.ontology.ChessGame;
import no.chess.web.model.ChessBoard;
import no.chess.web.model.ChessPiece;
import no.chess.web.model.PlayGame;
import no.chess.web.model.Position;
import no.games.chess.AbstractChessGame;
import no.games.chess.ChessAction;
import no.games.chess.ChessFunctions;
import no.games.chess.ChessPlayer;
import no.games.chess.ChessState;
import no.games.chess.GameBoard;
import no.games.chess.GamePiece;
import no.games.chess.PieceMove;

/**
 * AChessGame inherits from the abstract class AbstractChessgame
 * It is created when the user wants to play a game of chess from the PlayGame object
 * It collects all the pieces and their board positions from the chosen ontology game file. 
 * It transfers the current positions of the chess pieces to the AIMA chessboard
 * It also creates a AgamePiece for every chess piece found from the ontology game file
 * and places these pieces in an array called piecesonBoard.
 * It also creates a white player and a black player and gives the correct chesspieces to each of the players.
 * It finally creates an initial state of the game with the initial gameboard and the two players
 * This initial state calculates all available actions for this state.
 * NOTE: The removePiece method sets a piece passive.
 * @author oluf
 *
 */
public class AchessGame extends AbstractChessGame{
	private AgameBoard gameBoard;
	private List<Position> opponentPositions;
	private HashMap<String, Position> positions; // The HashMap of Positions from the ontology
	private List<Position> allPositions; // The list of Positions from the ontology
	private List<Position> orgPositions; // The list of Positions as originally
	private ArrayList<Position> usedPositionlist;
	private ArrayList<AgamePiece> piecesonBoard;
	private PlayGame gamePlayer;
	private APlayer localwhitePlayer;
	private APlayer localblackPlayer;
	private APlayer playerTomove;
	private int pn = 1;
	private List<ApieceMove> movements; // Movements made during the game
	private ChessBoard myFrontBoard; // The front chessboard to display board and pieces
	private String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\analysis.txt";
	private PrintWriter writer = null;
	private FileWriter fw = null;
	
	public AchessGame(int size,HashMap<String, Position> positions,ChessBoard frontBoard) {
		super(size);
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));		
		gameBoard = new AgameBoard(positions); // Represents the chess gameboard with aima board positions
		this.positions = positions;
		allPositions = new ArrayList(positions.values());
		
		piecesonBoard = new ArrayList();
//		createStart();
		tranferBoard(); // transfers piece positions to the aima chessboard
		chessState = new ChessStateImpl(this, gameBoard,localwhitePlayer,localblackPlayer);
		playerTomove = (APlayer) chessState.getPlayerTomove();

	}
	/**
	 * restorepositions
	 * This method returns original values to all positions.
	 * @deprecated - Not necessary
	 */
	public void restorePositions() {
		for (Position position:allPositions) {
			restoreposition(position);
		}
		createStart();
	}
	/**
	 * createStart
	 * This method creates a list of positions that contains the original values of the positions.
	 * It is called when the AChessGame object is created.
	 * @deprecated - Not necessary 
	 */
	public void createStart() {
		orgPositions = null;
		orgPositions = new ArrayList();
		for (Position position:allPositions) {
			Position orgPos = new Position(position.getPositionName(),position.isInUse(),position.getUsedBy());
			orgPositions.add(orgPos);
		}
	}
	/**
	 * restoreposition
	 * This method returns original values to a given position.
	 * @deprecated - Not necessary 
	 * @param pos
	 */
	public void restoreposition(Position pos) {
		for (Position position:orgPositions) {
			String posName = position.getPositionName();
			boolean inUse = position.returnInuse();
			ChessPiece piece = position.getUsedBy();
			if (pos.getPositionName().equals(posName)) {
				pos.setInUse(inUse);
				pos.returnPiece(piece);
				break;
			}
			
		}
	}
	/**
	 * @param location
	 * @deprecated !!?? as of december 2019
	 * @return
	 */
	private AgamePiece findPiece (XYLocation location) {
		APlayer activePlayer = null;
		AgamePiece activePiece = null;
		APlayer blackPlayer = getLocalblackPlayer();
		APlayer whitePlayer = getLocalwhitePlayer();
		boolean whiteTurn = whitePlayer.isActive();
		boolean blackTurn = blackPlayer.isActive();
		if (whiteTurn)
			activePlayer = whitePlayer;
		else
			activePlayer = blackPlayer;
		List<AgamePiece> pieces = activePlayer.getMygamePieces();
		for (AgamePiece piece:pieces) {
			Position position = piece.getMyPosition();
			if (position != null && piece.isActive()) {
				opponentPositions.add(position);
				XYLocation xyloc = position.getXyloc();
				int x = xyloc.getXCoOrdinate();
				int y = xyloc.getYCoOrdinate();
				int tx = location.getXCoOrdinate();
				int ty = location.getYCoOrdinate();
				if (x == tx && y == ty && piece.isActive()) {
					writer.println("*** Active piece found **** "+piece.toString()+" "+piece.getMyPiece().toString()+"\n");
					activePiece = piece;
				}
			}
		}
		return activePiece;
	}	
	public List<ApieceMove> getMovements() {
		return movements;
	}
	public void setMovements(List<ApieceMove> movements) {
		this.movements = movements;
	}
	public List<Position> getAllPositions() {
		return allPositions;
	}

	public void setAllPositions(List<Position> allPositions) {
		this.allPositions = allPositions;
	}

	public List<Position> getOpponentPositions() {
		return opponentPositions;
	}

	public void setOpponentPositions(List<Position> opponentPositions) {
		this.opponentPositions = opponentPositions;
	}

	public APlayer getLocalwhitePlayer() {
		return localwhitePlayer;
	}

	public void setLocalwhitePlayer(APlayer localwhitePlayer) {
		this.localwhitePlayer = localwhitePlayer;
	}

	public APlayer getLocalblackPlayer() {
		return localblackPlayer;
	}

	public void setLocalblackPlayer(APlayer localblackPlayer) {
		this.localblackPlayer = localblackPlayer;
	}

	public void setChosenPlayer() {
		chessState.returnMyplayer();
	}
	/**
	 * transferBoard
	 * This method transfers piece positions to the aima chessboard
	 * It also creates all available pieces determine their type and calculates all reachable positions
	 * And it creates a white player and a black player with their available pieces and their available positions.
	 */
	public void tranferBoard() {
		usedPositionlist = (ArrayList<Position>) gameBoard.getUsedPositionlist();
		StringBuilder builder = new StringBuilder();
		builder.append("Chessgame From transferBoard\n");
		localwhitePlayer = new APlayer();
		localwhitePlayer.setPlayerName( localwhitePlayer.getWhitePlayer());
		localblackPlayer = new APlayer();
		localblackPlayer.setPlayerName(localblackPlayer.getBlackPlayer());
		setBlackPlayer(blackPlayer);
		setWhitePlayer(whitePlayer);
//		whitePlayer = (ChessPlayer<GamePiece<?>, PieceMove<?, ?>>) localwhitePlayer;
//		blackPlayer = (ChessPlayer<?, ?>) localblackPlayer;
		for (Position position:usedPositionlist) {
			XYLocation loc = position.getXyloc();
			String pieceName = position.getUsedBy().getName();
			ChessPiece piece = position.getUsedBy();
			AgamePiece gamePiece = new AgamePiece(position,piece);
			piece.setMyPiece(gamePiece);
			gamePiece.setOntologyPositions(positions);
			
			if(gamePiece.getColor().equals("w")) {
				localwhitePlayer.getMygamePieces().add(gamePiece);
//				ArrayList<GamePiece<Position>> x = (ArrayList<GamePiece<Position>>) whitePlayer.getMygamePieces();
//				x.add(gamePiece);
			}
			if(gamePiece.getColor().equals("b")) {
				localblackPlayer.getMygamePieces().add(gamePiece);
//				ArrayList<GamePiece> y = (ArrayList<GamePiece>) whitePlayer.getMygamePieces();
//				y.add(gamePiece);
				
			}
			piecesonBoard.add(gamePiece);
			addPieceAt(loc);
			addPieceAtPos(loc, pieceName);
			builder.append(gamePiece.toString());
			builder.append("\n");
		}
		builder.append("End transferBoard\n");
		writer.println(getBoardPic());
		writer.println(builder.toString());
		writer.flush();
	}
	/**
	 * createNewboard
	 * This method creates a new aima board, based on the new usedpositionlist
	 * The gameBoard.setusedunused() is called to create a new usedpositionlist after a movement
	 * It is called from the proposemove method of the playgame object and
	 * When the opponent has made a move (RapporterChessStartServerResourceHTML)
	 * 
	 */
	public void createNewboard() {
		gameBoard.getUsedPositionlist().clear();
		gameBoard.getNotusedPositionlist().clear();
		gameBoard.setusedunused();
//		gameBoard.setUsedPositionlist(usedPositionlist);
		usedPositionlist = (ArrayList<Position>) gameBoard.getUsedPositionlist();
		for (Position position:usedPositionlist) {
			XYLocation loc = position.getXyloc();
			if (position.isInUse()) {
				String pieceName = position.getUsedBy().getName();
//				ChessPiece piece = position.getUsedBy();
//				AgamePiece gamePiece = new AgamePiece(position,piece);
				addPieceAt(loc);
				addPieceAtPos(loc, pieceName);
			}else {
				writer.println("AchessGame: Used position not in use !!! \n"+position.toString());
			}

	//		builder.append(gamePiece.toString());
	//		builder.append("\n");
		}
	}
	public PlayGame getGamePlayer() {
		return gamePlayer;
	}

	public void setGamePlayer(PlayGame gamePlayer) {
		this.gamePlayer = gamePlayer;
	}
	/**
	 * movePiece
	 * This method is called from the chessstate object to perform a move
	 * It uses the position .setUsedBy method to perform the move.
	 * When a piece is moved using the position.setUsedBy method, then 
	 * the piece position must be set accordingly.
	 * @param piece
	 * @param to
	 */
	public void movePiece(AgamePiece piece,Position to) {
		boolean removed = false;
		writer.println("To move Piece ===================="+" Chessstate"+"\n"+piece.toString());
		XYLocation from = piece.getMyPosition().getXyloc();
		if (piece.getHeldPosition() != null) {
			writer.println("From position collected from heldPosition\n"+piece.getHeldPosition().toString());
		    from = piece.getHeldPosition().getXyloc();
		}
		XYLocation xyto = to.getXyloc();
		if (piece != null)
			removed = removePiece(piece,xyto);
		if (!removed)
			pieceMove(piece,xyto);
		movePiece(from,to.getXyloc());
	}
	/**
	 * movePiece
	 * This method is called from the PlayGame object to perform a move.
	 * It is called after a call to the chessboard .determineMove method
	 * The chessboard .determineMove method uses the position .setUsedBy method
	 * @param piece
	 * @param to
	 * @param source
	 */
	public void movePiece(AgamePiece piece,Position to, String source) {
		writer.println("To move Piece ===================="+source+"\n"+piece.toString());
		boolean removed = false;
		XYLocation xyto = to.getXyloc();
		Position heldPosition = piece.getHeldPosition();
		if (heldPosition == null)
			heldPosition = piece.getmyPosition();
		if (piece != null)
			removed = removePiece(piece,xyto);
		if (!removed)
			pieceMove(piece,xyto);
		movePiece(heldPosition.getXyloc(),to.getXyloc());
//		ChessStateImpl localState = (ChessStateImpl) chessState;
		// Return to original utility value
	}
	/* movePiece
	 * This method moves a piece from its present location to a new location
	 * It is called from the PlayGame object when a piece is moved.
	 * It is also called from the chessstate object during the makeDecision - getResult and  - mark procedure
	 * This method must include a procedure to remove an opponent piece as a result of the move
	 * 
	 */
	public void movePiece(XYLocation from, XYLocation to) {
//		boolean removed = false;
		writer.println("Move from to ====================\n");
		writer.println("From "+from.toString()+ " To "+to.toString());
//		AgamePiece activePiece = findPiece(from);
//		position.setUsedBy(activePiece.getMyPiece());
/*		if (activePiece != null)
			removed = removePiece(activePiece,to);
		if (!removed)
			pieceMove(activePiece,to);*/
		super.movePiece(from, to);
		writer.flush();
		gameBoard.setusedunused();
	}

	/**
	 * pieceMove
	 * This method performs the actual move to the new location for the piece
	 * and places the piece in that location, and the piece is given that new location.
	 * @param piece
	 * @param to
	 */
	private void pieceMove(AgamePiece piece,XYLocation to) {
		if (piece != null) {
			Position mypos = piece.getMyPosition();
			for (Position pos: allPositions) {
				XYLocation loc = pos.getXyloc();
				int x = loc.getXCoOrdinate();
				int y = loc.getYCoOrdinate();
				int tx = to.getXCoOrdinate();
				int ty = to.getYCoOrdinate();
				if (x == tx && y == ty ) {
					pos.setUsedBy(piece.getMyPiece());
					piece.setMyPosition(pos); // When a piece is moved using the position.setUsedBy method, then 
					// the piece position must be set accordingly. The piece.setMyposition pushes the former position to stack of held positions.
					writer.println("*** Active piece moved **** "+piece.toString()+"\n");
				}
			}
		}
		
	}


	private boolean removePiece(AgamePiece activePiece,XYLocation to) {
		APlayer opponent = null;
		Position moveTo = null;
		boolean removed = false;
		APlayer blackPlayer = getLocalblackPlayer();
		APlayer whitePlayer = getLocalwhitePlayer();
		boolean whiteTurn = whitePlayer.isActive();
		boolean blackTurn = blackPlayer.isActive();
		opponentPositions.clear();
		if (whiteTurn)
			opponent = blackPlayer;
		else
			opponent = whitePlayer;
		List<AgamePiece> pieces = opponent.getMygamePieces();
		for (AgamePiece piece:pieces) {
			Position position = piece.getMyPosition();
			if (position != null && piece.isActive()) {
				opponentPositions.add(position);
				XYLocation xyloc = position.getXyloc();
				int x = xyloc.getXCoOrdinate();
				int y = xyloc.getYCoOrdinate();
				int tx = to.getXCoOrdinate();
				int ty = to.getYCoOrdinate();
				if (x == tx && y == ty && piece.isActive()) {
					writer.println("*** Opponent piece Taken **** "+piece.toString()+" "+piece.getMyPiece().toString()+"\n");
					removed = true;
					position.setUsedBy(activePiece.getMyPiece());
					activePiece.setMyPosition(position); // When a piece is moved using the position.setUsedBy method, then 
					// the piece position must be set accordingly.
//					piece.setActive(false);
//					piece.setValue(-1);
//					piece.setMypositionEmpty(null);
				}
			}
		}
		return removed;
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
	 * getResult
	 * This method returns a new state as a result of a chosen action 
	 * This method is called during a minimax search
	 * 	
	 */
	@Override
	public ChessState getResult(ChessState chessState, ChessAction action) {
		((ChessStateImpl) chessState).mark(action);
		return chessState;
	}

	@Override
	public double getUtility(ChessState state, ChessPlayer player) {
		
		return state.getUtility();
	}

	@Override
	public boolean isTerminal(ChessState state) {
		
		return state.getUtility() == 1 || state.getUtility() == 0;
	}
/*
 * analyzePieceandPosition
 * This method returns a utility value that is high if the preferred position is a central position and the piece has a
 * It makes use of an action processor that returns utility value for the given action.
 * This value is used by the search object to order the actions.
 */

	public double analyzePieceandPosition(ChessAction action) {
		List<ApieceMove> movements = null;
/*		fw = null;
		writer = null;*/
/*		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	      writer = new PrintWriter(new BufferedWriter(fw));		*/
		playerTomove = (APlayer) chessState.getPlayerTomove();
//		playerTomove.checkPreferredPosition(action);
		StringBuilder builder = new StringBuilder();
		builder.append("Analyzepieceandposition\n");
		builder.append("Analyzing action: "+action.toString()+"\n");
		
		Integer pNumber = new Integer(pn);
		pn++;
		AgamePiece piece = (AgamePiece) action.getChessPiece();
		String name = piece.getMyPiece().getPieceName();
		ActionProcessor actionProcessor = new ActionProcessor(pNumber,name);
		Double d  = ChessFunctions.processChessgame(action, gamePlayer,actionProcessor);
		opponentPositions = actionProcessor.getOpponentPositions();
		builder.append("Evaluation value "+ d.toString()+"\n");
		writer.println(builder.toString());
		builder = null;
		writer.flush();
		return d.doubleValue();
		
/*		int noofMoves = 0;
		int factor = 1;bhfgdchgcfdg
		if (gamePlayer != null) {
			movements = gamePlayer.getMovements();
		}
		if (movements != null && !movements.isEmpty()) {
			noofMoves = movements.size();
			factor = noofMoves -1;
		}
			
		Position position = (Position) action.getPreferredPosition();
		String prefPos = "None ";
		if (position != null)
			prefPos = position.toString();
		Position tempPos = null;
		int row = -1;
		AgamePiece piece =  (AgamePiece) action.getChessPiece();

		String pieceName = piece.getMyPiece().getPieceName();
		List<Position> preferredPositions = piece.getPreferredPositions();
		String color = piece.getColor();
		String preferred = ((preferredPositions != null && !preferredPositions.isEmpty() ? " not empty" : " empty"));
		builder.append("Piece "+piece.toString()+" Position "+piece.getMyPosition().toString()+" Xcoordinate "+
				piece.getMyPosition().getXyloc().getXCoOrdinate()+" Ycoordinate "+piece.getMyPosition().getXyloc().getYCoOrdinate()+"\n"+"Preferred position: "
				+prefPos+preferred);	
		
		if (pieceName.equals("P") && factor <= 3) {
	
			factor = 10;
			int col = piece.getMyPosition().getXyloc().getXCoOrdinate();
			int lrow = piece.getMyPosition().getXyloc().getYCoOrdinate();
			if (col > 3 && col < 5)
				factor = factor*col; // 22.08 19: Alle samme verdi???
//			System.out.println(builder.toString());
			
		}
		if (position != null && preferredPositions != null && !preferredPositions.isEmpty()) {
			row = position.getIntRow();
			for (Position pos : preferredPositions) {
				int nRow = pos.getIntRow();
				if (color.equals("w") && nRow > row) {
					row = nRow;
					tempPos = pos; //Must break loop???
//					System.out.println("Analyzepieceandposition - loop: "+tempPos.toString());
				}
				if (color.equals("b") && nRow < row) {
					row = nRow;
					tempPos = pos;
				}
			}
		}
		if (tempPos != null) {
//			System.out.println("Analyzepieceandposition: "+position.toString()+"\n"+tempPos.toString());
			position = tempPos;
			builder.append("New Preferred position: "+position.toString());
		}
		int val = piece.getMyPiece().getValue();
		action.setPreferredPosition(position);
//		
		int col = 0; 
		if (position != null)
			col = position.getIntColumn();
		if (col > 4)
			col = 7 - col;
		writer.println(builder.toString());
		builder = null;
		writer.close();
		return factor*val;*/
	}

	/**
	 * getActions
	 * This method returns a list of all possible moves all available pieces 
	 * for the active player can make from the current state
	 */
	public List<ChessAction> getActions(ChessState<GameBoard> state) {
		List<ChessAction> actions = state.getActions();
		return actions;
	}



	@Override
	public ChessState<GameBoard> getInitialState() {
	
		return (ChessState<GameBoard>) chessState;
	}

	@Override
	public ChessPlayer<GamePiece, PieceMove>[] getPlayers() {
		return null;
	}

	/* 
	 * getPlayer
	 * returns the active player
	 */
	@Override
	public ChessPlayer<GamePiece, PieceMove> getPlayer(ChessState<GameBoard> state) {
		if (localwhitePlayer.isActive())
			return (ChessPlayer) localwhitePlayer;
		if (localblackPlayer.isActive())
			return (ChessPlayer)localblackPlayer;
		
		return null;
	}
	/* 
	 * analyzeState
	 * This method is part of the evaluation function for the agent object
	 * and is called from the search object's evaluation function.
	 * The method attempts to analyze the features of the state.
	 * @see no.games.chess.ChessGame#analyzeState(java.lang.Object)
	 */
	@Override
	public double analyzeState(ChessState<GameBoard> state) {
		StringBuilder builder = new StringBuilder();
		builder.append(" ** Analyzing state ***\n");
		boolean opponentToplay = false;
		double evaluation = 0;
		ChessStateImpl localState = (ChessStateImpl) state;
		APlayer playerTomove = localState.getPlayerTomove();
		APlayer opponent = localState.getBlackPlayer();
		List<AgamePiece> myPieces =  localState.getPlayerTomove().getMygamePieces();
		List<AgamePiece> opponentPieces = opponent.getMygamePieces();
		if(playerTomove.getPlayerName() == playerTomove.getBlackPlayer()) {
			opponent = localState.getMyPlayer();
			opponentToplay = true;
			builder.append("Opponent to play\n");
// 			
		}
		int myFeatures = analyzeFeatures(myPieces);
		int opponentFeatures = analyzeFeatures(opponentPieces);
		int myCount= countPieces(myPieces);
		int opponentCount= countPieces(opponentPieces);		
		builder.append("My features "+myFeatures+" Oppenent features "+opponentFeatures+ " state utility "+localState.getUtility()+" No of pieces white "+myCount+" No of pieces black "+opponentCount );
		writer.println(builder.toString());
//		writer.flush();
		evaluation = myFeatures - opponentFeatures; 
		int pieceCount = myCount - opponentCount;
		if (evaluation < 0)
			evaluation = 0;
		evaluation = evaluation + localState.getUtility() + pieceCount;
		return evaluation;
	}
	private int countPieces(List<AgamePiece> myPieces) {
		int nofPieces = 0;
		for (AgamePiece piece:myPieces) {
			boolean active = piece.isActive();
			if (active) {
				nofPieces++;
			}
		}
		return nofPieces;
		
	}
	/**
	 * analyzeFeatures
	 * This method makes an analysis of all the available features of a list of pieces
	 * and their positions.
	 * @param myPieces
	 * @return
	 */
	private int analyzeFeatures(List<AgamePiece> myPieces) {
		int nofOfactivePieces = 0;
		int featureValue = 0;
		for (AgamePiece piece:myPieces) {
			boolean active = piece.isActive();
			int pieceValue = 0;
			int posValue = 0;
			if (active) {
				posValue = 1;
				nofOfactivePieces++;
				pieceValue = piece.getMyPiece().getValue();
				Position position = piece.getMyPosition();
				boolean leftHigh = position.isCenterlefthigh();
				boolean rightHigh = position.isCenterrighthigh();
				boolean leftLow = position.isCenterleftlow();
				boolean rightLow = position.isCenterrightlow();
				if (leftHigh || leftLow || rightHigh || rightLow) {
					posValue = 4;
					featureValue++;
				}
			}
			featureValue = featureValue + pieceValue + posValue;
			
		}
		return featureValue;
	}

	
}
