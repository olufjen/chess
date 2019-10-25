package no.chess.web.model.game;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import aima.core.util.datastructure.XYLocation;
import no.chess.web.model.PlayGame;
import no.chess.web.model.Position;
import no.games.chess.ChessPieceType;
import no.games.chess.ChessProcessor;
import no.games.chess.AbstractGamePiece.pieceColor;
import no.games.chess.AbstractGamePiece.pieceType;

/**
 * ActionProcessor
 * It is created and called when the chess game (AchessGame) attempts to analyze (evaluate) a given chess action.
 * This is the given structure:
 * The chess game has an initial chess state. A Chess State contains a set of available chess actions for this given chess state.
 *  
 * This processor acts as an evaluation function and returns an action value (Type Double) for a given action
 * This action value is used as a basis for the minimax search algorithm and to sort the available actions so 
 * that the player makes the best choice of action for the current state.
 * The algorithm for calculating the action value is as follows:
 * Suggestion: Make several types of action processors to choose from depending on ????
 * See notes (3) on Favourable positions.
 * @author oluf
 *
 */
public class ActionProcessor implements ChessProcessor<ChessActionImpl,PlayGame,Double> {

	private List<Position> opponentPositions;
	private boolean black = false;
	private boolean white = false;
	private String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\";
	private PrintWriter writer = null;
	private Integer processNumber;
	
	public ActionProcessor(Integer processNumber) {
		super();
		this.processNumber = processNumber;
		String pNumber = processNumber.toString();
		outputFileName = outputFileName + "ontpositions" + pNumber+".txt";
	      try 
	      {
	         writer = new PrintWriter(outputFileName);
	      } catch (FileNotFoundException e) {
	         System.err.println("'" + outputFileName 
	            + "' is an invalid output file.");
	      }	
	}

	@Override
	public Double processChessObject(ChessActionImpl p, PlayGame q) {
		Position prefPosx = null;
//		ApieceMove move = null;
		AgamePiece lastPiece = null;
		opponentPositions = new ArrayList<Position>();
		List<ApieceMove> movements = null;
		int noofMoves = 0;
		int movefactor = 1;
		int pieceFactor = 0;
		
		if (q != null) {
			movements = q.getMovements();
		}
		if (movements != null && !movements.isEmpty()) {
			noofMoves = movements.size();
			movefactor = noofMoves -1;
			for (ApieceMove move:movements) {
				lastPiece = move.getPiece();
				writer.println("Previous moves "+move.toString());
			}
//			move = movements.get(movefactor);
		}
			
		Position position = (Position) p.getPreferredPosition();
		 
		String prefPos = "None ";
		if (position != null)
			prefPos = position.toString();
		Position tempPos = null; // Used to give the action a new preferred position ??
		AgamePiece piece =  (AgamePiece) p.getChessPiece();
		List<Position> prefPositions = piece.getPreferredPositions();
		
		AchessGame game = q.getGame();
		APlayer opponent = null;
		APlayer blackPlayer = game.getLocalblackPlayer();
		APlayer whitePlayer = game.getLocalwhitePlayer();
		boolean whiteTurn = whitePlayer.isActive();
		boolean blackTurn = blackPlayer.isActive();
		if (whiteTurn)
			opponent = blackPlayer;
		else
			opponent = whitePlayer;
		findOpponentPieces(opponent); // All opponent's positions are known in opponentPositions
		ChessPieceType pieceType = piece.getChessType();
		pieceType type =  piece.getMyType();
		HashMap<String,Position>np = piece.getNewPositions(); // New positions available for piece	
		
		if (pieceType instanceof APawn) {
			APawn pawn = (APawn) pieceType;
// Temporary			
			Position from = pawn.getmyPosition();
			XYLocation loc = from.getXyloc();
			int x = loc.getXCoOrdinate();
			int y = loc.getYCoOrdinate();
			List<Position> tempPositions = new ArrayList(np.values());
			if (x == 1) {
				for (Position pos:tempPositions) {
					XYLocation ploc = pos.getXyloc();
					int px = ploc.getXCoOrdinate();
					int py = ploc.getYCoOrdinate();
					if (px >= 4) {
						writer.println("Available position is wrong "+pos.toString());
					}
				}
			}
// End temporary
			if (movefactor < 4)
				pieceFactor = 5;
			if (checkPawnopponents(np)) {
				pieceFactor = 0;
			}
		}
		pieceColor colorType = piece.getPieceColor();

		black = piece.checkBlack();
		white = piece.checkWhite();
	
		List<Position>pp = piece.getPreferredPositions();
		HashMap<String,Position>op = piece.getOntologyPositions(); // Ontpositions contains all positions !!!
		int posFactor = 0;
		posFactor = calculatePositionFactor(np);
		if (lastPiece != null && lastPiece == piece)
			posFactor = 0;
		if (posFactor == 4) {
			List<Position> tempPositions = new ArrayList(np.values());
			for (Position pos:tempPositions) {
				if (pos.isCenterlefthigh()) {
					p.setPreferredPosition(pos);
					break;
				}
				if (pos.isCenterrighthigh()) {
					p.setPreferredPosition(pos);
					break;
				}
				writer.println("Preferred position set to "+pos.toString());
			}
			

		}
		if (prefPositions == null || prefPositions.isEmpty()) {
			if (position == null) {
				writer.println("No preferred positions !!!");
				posFactor = 0;pieceFactor = 0;movefactor =  0;
			}
		}
		Double evaluation = new Double(posFactor+pieceFactor+movefactor);
		writer.println("Piece:\n");
		writer.println(piece.toString());
		writer.println("Evaluation value:\n");
		writer.println(evaluation.toString());
        writer.close();
		return evaluation;
	}

	/**
	 * findOpponentPieces
	 * This method finds all opponent's piece's positions
	 * and places them in the list opponentPositions
	 */
	private void findOpponentPieces(APlayer opponent) {
		
		List<AgamePiece> pieces = opponent.getMygamePieces();
		for (AgamePiece piece:pieces) {
			Position position = piece.getMyPosition();
			opponentPositions.add(position);
		}
	}

	/**
	 * calculatePositionFactor
	 * This method calculates if a rechable position is a center position on the chessboard
	 * @param op
	 * @return an integer value indicating if a position is a center position
	 */
	private int calculatePositionFactor(HashMap<String,Position>op) {
		List<Position> tempPositions = new ArrayList(op.values());
		int factor = 1;
		for (Position position:tempPositions) {
	
			if (white && position.isCenterlefthigh()) {
			     writer.println("From Position Factor high left\n");
//			     writer.println(position.toString());
				factor = 4;	
				
				break;
			}
		
			if(white && position.isCenterrighthigh()) {
			     writer.println("From Position Factor high right\n");
//			     writer.println(position.toString());
				factor = 4;
			
				break;
			}
			if (black && position.isCenterleftlow()) {
				 writer.println("From Position Factor low  left\n");
//				 writer.println(position.toString());
				factor = 4;	
				break;
			}
		
			if(black && position.isCenterrightlow()) {
				 writer.println("From Position Factor low right\n");
//				 writer.println(position.toString());
				factor = 4;
				break;
			}			
		}
		return factor;
	}
	
	/**
	 * checkPawnopponents
	 * This method checks if a pawn's movement is blocked by an opponent's piece
	 * @param np
	 * @return true if the pawn is blocked
	 */
	private boolean checkPawnopponents(HashMap<String,Position>np) {
		boolean blocked = false;
		List<Position> tempPositions = new ArrayList(np.values());
		for (Position pos:tempPositions) {
			for (Position oponentPos : opponentPositions) {
				blocked = oponentPos.getPositionName().equals(pos.getPositionName());
				if (blocked) {
					writer.println("Pawn blocked  "+pos.getPositionName());
					break;
				}
		
			}
		}
		return blocked;
	}




}
