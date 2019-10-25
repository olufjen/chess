package no.chess.web.model.game;

import java.io.FileNotFoundException;
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
 * It is created when the user wants to play a game of chess from th PlayGame object
 * It transfers the current positions of the chess pieces to the AIMA chessboard
 * It also creates a AgamePiece for every chess piece found from the ontology game
 * and places these pieces in an array called piecesonBoard.
 * It also creates a white player and a black player and gives the correct chesspieces to each of the players.
 * It finally creates an initial state of the game with the initial gameboard and the two players
 * This initial state calculates all available actions for this state.
 * @author oluf
 *
 */
public class AchessGame extends AbstractChessGame{
	private AgameBoard gameBoard;
	private HashMap<String, Position> positions; // THe HashMap of Positions from the ontology
	private ArrayList<Position> usedPositionlist;
	private ArrayList<AgamePiece> piecesonBoard;
	private PlayGame gamePlayer;
	private APlayer localwhitePlayer;
	private APlayer localblackPlayer;
	private int pn = 1;

	private ChessBoard myFrontBoard; // The front chessboard to display board and pieces
	private String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\analysis.txt";
	private PrintWriter writer = null;
	
	public AchessGame(int size,HashMap<String, Position> positions,ChessBoard frontBoard) {
		super(size);
		gameBoard = new AgameBoard(positions); // Represents the chess gameboard with aima board positions
		this.positions = positions;
		piecesonBoard = new ArrayList();
		tranferBoard(); // transfers piece positions to the aima chessboard
		chessState = new ChessStateImpl(this, gameBoard,localwhitePlayer,localblackPlayer);
	      try 
	      {
	         writer = new PrintWriter(outputFileName);
	      } catch (FileNotFoundException e) {
	         System.err.println("'" + outputFileName 
	            + "' is an invalid output file.");
	      }
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
	 * And it creates a white player and a black player with their available pieces ad their available positions.
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
//		System.out.println(getBoardPic());
//		System.out.println(builder.toString());
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
				ChessPiece piece = position.getUsedBy();
				AgamePiece gamePiece = new AgamePiece(position,piece);
				addPieceAt(loc);
				addPieceAtPos(loc, pieceName);
			}else {
				System.out.println("AchessGame: Used position not in use !!! \n"+position.toString());
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

	/* movePiece
	 * This method moves a piece from its present location to a new location
	 * It is called from the PlayGame object when a piece is moved.
	 * 
	 */
	public void movePiece(XYLocation from, XYLocation to) {
		
		super.movePiece(from, to);
//		gameBoard.setusedunused();
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
 * high ranking 
 * Multiply the utility value with a factor depending on:
 * - Early/late in the game
 * - Other??
 */

	public double analyzePieceandPosition(ChessAction action) {
		List<ApieceMove> movements = null;
		StringBuilder builder = new StringBuilder();
		builder.append("Analyzepieceandposition\n");
		builder.append("Analyzing action: "+action.toString()+"\n");
		Integer pNumber = new Integer(pn);
		pn++;
		ActionProcessor actionProcessor = new ActionProcessor(pNumber);
//		AgamePiece p = (AgamePiece) action.getChessPiece();
		Double d  = ChessFunctions.processChessgame(action, gamePlayer,actionProcessor);
		return d.doubleValue();
		
/*		int noofMoves = 0;
		int factor = 1;
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

	@Override
	public ChessPlayer<GamePiece, PieceMove> getPlayer(ChessState<GameBoard> state) {
		// TODO Auto-generated method stub
		return null;
	}



/*
 * 
 */









	
}
