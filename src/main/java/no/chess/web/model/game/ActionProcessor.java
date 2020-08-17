package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import aima.core.util.datastructure.XYLocation;
import no.chess.web.model.PlayGame;
import no.chess.web.model.Position;
import no.games.chess.ChessFunctions;
import no.games.chess.ChessPieceType;
import no.games.chess.ChessProcessor;
import no.games.chess.FilterMove;
import no.games.chess.AbstractGamePiece.pieceColor;
import no.games.chess.AbstractGamePiece.pieceType;

/**
 * ActionProcessor
 * It is created and called whenever the chess game (AchessGame) attempts to analyze (evaluate) a given chess action or
 * when the chess state is created. This is necessary in order to give the initial state an evaluation for each action.
 * This is the given structure:
 * The chess game has an initial chess state. A Chess State contains a set of available chess actions for this given chess state.
 *  
 * This processor acts as an evaluation function and returns an action value (Type Double) for a given action
 * This action value is used as a basis for the minimax search algorithm and to sort the available actions so 
 * that the player makes the best choice of action for the current state.
 * The algorithm for calculating the action value is as follows:
 * Suggestion: Make several types of action processors to choose from depending on ????
 * See notes (3) on Favourable positions.
 * The results of the evaluations are shown in the file ontpositions(p).txt where (p) is the piece name
 * @author oluf
 *
 */
public class ActionProcessor implements ChessProcessor<ChessActionImpl,PlayGame,Double> {

	private List<Position> opponentPositions;
	private Position strikePosition = null;
	private boolean black = false;
	private boolean white = false;
	private String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\";
	private PrintWriter writer = null;
	private FileWriter fw = null;
	private Integer processNumber;
	
	public ActionProcessor(Integer processNumber, String pname) {
		super();
		this.processNumber = processNumber;
		String pNumber = processNumber.toString();
		outputFileName = outputFileName + "ontpositions"+pname +".txt";
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
		
/*	      try 
	      {
	         writer = new PrintWriter(outputFileName);
	      } catch (FileNotFoundException e) {
	         System.err.println("'" + outputFileName 
	            + "' is an invalid output file.");
	      }	*/
	}

	@Override
	public Double processChessObject(ChessActionImpl p, PlayGame q) {
		Position prefPosx = null;

		boolean pawnblocked = false;
		boolean pawnStrike = false;
//		ApieceMove move = null;
		AgamePiece lastPiece = null;
		opponentPositions = new ArrayList<Position>();
		List<ApieceMove> movements = null;
		int noofMoves = 0;
		int movefactor = 1;
		int pieceFactor = 0;

		ChessStateImpl state = q.getActiveState();
		
		if (q != null) {
			movements = q.getMovements();
		}
		if (movements != null && !movements.isEmpty()) {
			noofMoves = movements.size();
			movefactor = noofMoves -1;
			for (ApieceMove move:movements) {
				lastPiece = move.getPiece();
				writer.println("===== Previous moves ====== \n"+move.toString());
			}
//			move = movements.get(movefactor);
		}
		AchessGame game = q.getGame();
		ApieceMove move = p.getPossibleMove();
		Position toPosition = null;
		if (move != null)
			toPosition = move.getToPosition();
		APlayer opponent = null;
		APlayer playerTomove = null;
		APlayer blackPlayer = game.getLocalblackPlayer();
		APlayer whitePlayer = game.getLocalwhitePlayer();
		boolean whiteTurn = whitePlayer.isActive();
		boolean blackTurn = blackPlayer.isActive();

		Position position = (Position) p.getPreferredPosition();
		 
		String prefPos = "None ";
		if (position != null)
			prefPos = position.toString();
		Position tempPos = null; // Used to give the action a new preferred position ??
		AgamePiece piece =  (AgamePiece) p.getChessPiece();
		List<Position> availablePositions = new ArrayList(piece.getReacablePositions().values());
		;
		if (!piece.isActive()) { // If piece is inactive it cannot be used
			writer.println("Piece is inactive ======================:\n"+piece.toString());
			Double evaluation = new Double(0);
		    writer.close();
			return evaluation;
		}
		ApieceMove actionMove = p.getPossibleMove();
		if (actionMove == null) {
			writer.println("Action has no move  ======================:\n"+p.toString());
			Double evaluation = new Double(0);
		    writer.close();
			return evaluation;
		}
		if (position == null) {
			writer.println("No preferred position  ======================:\n"+p.toString());
			Double evaluation = new Double(0);
		    writer.close();
			return evaluation;
		}
		boolean checkMoves = false;
		if (position != null) {
			checkMoves = checkPlayedMovements(p, movements,whiteTurn,blackTurn);
			if (checkMoves) {
				writer.println("Moveconflict !!!! ======================:\n"+p.toString());
				Double evaluation = new Double(0);
			    writer.close();
				return evaluation;
			}
		}

		List<Position> prefPositions = piece.getPreferredPositions();
		boolean newPos = piece.checkPositions(); // Creates new available positions if empty !!
		if (newPos)
			writer.println("New available positions are created for piece:\n"+piece.toString());


		if (whiteTurn) {
			opponent = blackPlayer;
			playerTomove = whitePlayer;
		}else {
			opponent = whitePlayer;
			playerTomove = blackPlayer;
		}
		List<Position> opponentPos = p.getActions(opponent);
		List<Position> opponentRemoved = p.getPositionRemoved();
		List<Position> playerTomovePositions = p.getActions(playerTomove);
		List<Position> playerRemoved = p.getPositionRemoved();
		List<AgamePiece> pieces = playerTomove.getMygamePieces();
		List<AgamePiece> opponentpieces = opponent.getMygamePieces();
		
/*
 * The filterpiece is part of the list of pieces.		
 * The pos is part of the list of OpponentPos
 */
//		List<AgamePiece> protectors = ChessFunctions.filterPiece(pieces, (AgamePiece filterpiece) -> filterpiece.getmyPosition() == position);
		List<Position> attackedPositions = ChessFunctions.filterPiece(opponentPos, (Position pos) -> pos == position);
		List<Position> notAttackedPos = ChessFunctions.filterPiece(opponentRemoved, (Position pos) -> pos == position);
		List<Position> notProtected = ChessFunctions.filterPiece(playerRemoved, (Position pos) -> pos == position);
		List<Position> protectedPositions = ChessFunctions.filterPiece(playerTomovePositions, (Position pos) -> pos == position);
		List<AgamePiece> attacked = ChessFunctions.filterPiece(opponentpieces, (AgamePiece filterpiece) -> filterpiece.getmyPosition() == position);
		List<Position> otherattackedPositions = new ArrayList();
		for (Position availpos:availablePositions) {
			List<Position> tempattackedPositions = ChessFunctions.filterPiece(opponentPos, (Position pos) -> pos == availpos);
			otherattackedPositions.addAll(tempattackedPositions);
		}
		List<Position> otherprotectedPositions = new ArrayList();
		for (Position availpos:availablePositions) {
			List<Position> tempPositions = ChessFunctions.filterPiece(playerTomovePositions, (Position pos) -> pos == availpos);
			otherprotectedPositions.addAll(tempPositions);
		}
		for (Position pos:otherattackedPositions) {
			writer.println("Other attacked positions "+pos.toString()+"\n");
		}
		for (Position pos:otherprotectedPositions) {
			writer.println("Other protected positions "+pos.toString()+"\n");
		}	
		for (Position pos:notProtected) {
			writer.println("Not protected positions "+pos.toString()+"\n");
		}
		for (Position pos:protectedPositions) {
			writer.println("Protected positions "+pos.toString()+"\n");
		}
		
		for (Position pos:attackedPositions) {
			writer.println("Attacked positions "+pos.toString()+"\n");
		}
		for (Position pos:notAttackedPos) {
			writer.println("Not attacked positions "+pos.toString()+"\n");
		}
		// Removed temporary
/*		p.getActions(playerTomove);
		List<Position> availablePositions = (List<Position>) p.getAvailablePositions();
		List<Position>  removedPos = (List<Position>)p.getPositionRemoved();
		*/
		 /* Added 24.02.20		
		 * When a move has been made then the pieces belonging to the same player must get new
		 * available positions calculated
		 */	
				
// Removed	temporary	? 
/*				boolean available = false;
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
				}*/
		
		 /* end added	
		  *
		*/	
// REmoved temporary?		 
/*		if (removed) {
			writer.println("Piece preferable position is occupied by friendly piece:\n"+piece.toString()+"\n Position "+position.toString());
			Double evaluation = new Double(0);
		    writer.close();
			return evaluation;
		}*/
		
		findOpponentPieces(opponent); // All opponent's positions are held in opponentPositions
		ChessPieceType pieceType = piece.getChessType();
		pieceType type =  piece.getMyType();
		HashMap<String,Position>np = piece.getNewPositions(); // New positions available for piece	
		APawn pawn = null;
		if (pieceType instanceof APawn) {
			pawn = (APawn) pieceType;
			pawn.setBlocked(false);
			Position from = pawn.getmyPosition();
			XYLocation loc = from.getXyloc();
			int x = loc.getXCoOrdinate();
			int y = loc.getYCoOrdinate();
// Temporary	 This temporary test does not occur!!!		
			List<Position> tempPositions = new ArrayList(np.values()); // New positions as an array.
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
			if (movefactor < 2)
				pieceFactor = 5;
			if (checkPawnopponents(np)) {
				pieceFactor = 0;
				pawnblocked = true;
				pawn.setBlocked(pawnblocked);
			}
			if (strikeOpponent(from, whiteTurn)) {
				pawnStrike = true;
				pieceFactor = 5;
				p.setPreferredPosition(strikePosition);
				p.setStrikePosition(strikePosition);
				p.setStrike(true);
				move.setToPosition(strikePosition);
				removeOpponentPiece(piece,opponent, strikePosition);
				writer.println("Pawn strike at "+strikePosition.toString());
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
/*
 * This code is removed olj 31.07.20: It sets the preferred position different from the move position
 * 
 * ????????????????????
 */
		if (posFactor == 4 && !pawnStrike && pawn != null) {
			List<Position> tempPositions = new ArrayList(np.values());
			for (Position pos:tempPositions) {
				if (pos.isCenterlefthigh()) {
					p.setPreferredPosition(pos);
					move.setToPosition(pos);
					break;
				}
				if (pos.isCenterrighthigh()) {
					move.setToPosition(pos);
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
		if (pawnblocked && !pawnStrike) {
			posFactor = 0;pieceFactor = 0;movefactor =  0;
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
	 * checkPlayedMovements
	 * This method checks if a chosen action has a preferred position that is in conflict with 
	 * a played move
	 * @param localAction
	 * @param playedMovements
	 */
	private boolean checkPlayedMovements(ChessActionImpl localAction,List<ApieceMove> playedMovements,boolean whiteTurn,boolean blackTurn) {
		boolean result = false;
		if (playedMovements != null && !playedMovements.isEmpty()) {
			for (ApieceMove move: playedMovements) {
				boolean whiteMove = move.isWhiteMove();
				boolean blackMove = move.isBlackMove();
				Position toPosition = move.getToPosition();
				Position preferredPosition = localAction.getPreferredPosition();
				if (preferredPosition == null) {
					writer.println("No preferred position:\n"+localAction.toString());
					break;
				}
				if (toPosition == preferredPosition && whiteMove && whiteTurn) {
					result = true;
					writer.println("White turn:\n"+localAction.toString());
					break;
				}
				if (toPosition == preferredPosition && blackMove && blackTurn) {
					result = true;
					writer.println("Black turn:\n"+localAction.toString());
					break;
				}
			}
		}
		return result;
	}
	public List<Position> getOpponentPositions() {
		return opponentPositions;
	}

	public void setOpponentPositions(List<Position> opponentPositions) {
		this.opponentPositions = opponentPositions;
	}

	/**
	 * findOpponentPieces
	 * This method finds all opponent's active piece's positions
	 * and places them in the list opponentPositions
	 */
	private void findOpponentPieces(APlayer opponent) {
		
		List<AgamePiece> pieces = opponent.getMygamePieces();
		for (AgamePiece piece:pieces) {
			Position position = piece.getMyPosition();
			if (position != null && piece.isActive())
				opponentPositions.add(position);
		}
	}
	/**
	 * removeOpponentPiece
	 * This method removes a player's piece from the set of active and available pieces.
	 * @param opponent
	 * @param position
	 */
	private void removeOpponentPiece(AgamePiece activePiece,APlayer opponent,Position position) {
		List<AgamePiece> pieces = opponent.getMygamePieces();
		writer.println("Active piece checking removes \n"+activePiece.toString()+" "+activePiece.getMyPiece().toString());
		for (AgamePiece piece:pieces) {
			Position pos = piece.getMyPosition();
			if (pos == position) {
				writer.println("Opponent piece taken:\n"+piece.toString()+" "+piece.getMyPiece().toString() );
/*
 * The pos.setUsedBy call is not necessary
 * This call is carried out when the Playgame object uses the determineMove method of the chessboard			
 */
//				pos.setUsedBy(activePiece.getMyPiece());
//				piece.setActive(false);
//				piece.setValue(-1);
//				piece.setMypositionEmpty(null);
//				position.setUsedBy(null);
//				position.setInUse(false);
				
			}
		}
	}
	/**
	 * calculatePositionFactor
	 * This method calculates if a reachable position is a center position on the chessboard
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
		writer.println("Checking opponent\n");
		List<Position> tempPositions = new ArrayList(np.values()); // np contains available positions
		for (Position pos:tempPositions) {
			for (Position oponentPos : opponentPositions) {
//				writer.println("Opponent "+oponentPos.toString()+ " Available positions "+pos.toString());
				blocked = oponentPos.getPositionName().equals(pos.getPositionName());
				if (blocked) {
					writer.println("Pawn blocked  "+pos.getPositionName());
					break;
				}
		
			}
		}
		return blocked;
	}

	/**
	 * strikeOpponent
	 * This method return true if a pawn can strike an opponent piece
	 * @param from
	 * @return
	 */
	private boolean strikeOpponent(Position from,boolean whiteTurn) {
		boolean strike = false;
		int ic = 1;
		if (!whiteTurn)
			ic = -1;
		for (Position oponentPos:opponentPositions) {
			XYLocation loc = from.getXyloc();
			int x = loc.getXCoOrdinate();
			int y = loc.getYCoOrdinate();
			XYLocation oploc = oponentPos.getXyloc();
			int ox = oploc.getXCoOrdinate();
			int oy = oploc.getYCoOrdinate();
			strike = (oy==(y+ic)&&ox==(x-1))||(oy==(y+ic)&&ox==(x+1));
			if (strike) {
				strikePosition = oponentPos;
				break;
			}
		
		}
		return strike;
		
	}


}
