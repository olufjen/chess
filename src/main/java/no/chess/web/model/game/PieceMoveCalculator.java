package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import aima.core.util.datastructure.XYLocation;
import no.chess.web.model.Position;
import no.games.chess.AbstractGamePiece;
import no.games.chess.AbstractGamePiece.pieceColor;
import no.games.chess.AbstractGamePiece.pieceType;
import no.games.chess.ChessFunctions;
import no.games.chess.ChessPieceType;
import no.games.chess.GamePiece;
import no.games.chess.MoveCalculator;
import no.games.chess.MoveRule;

/**
 * PieceMoveCalculator
 * This is an implementation of the functional interface MoveCalculator
 * The PieceMoveCalculator returns all possible positions avaiable to the piece.
 * Restrictions are given by positions occupied by other friendly pieces.
 * It is created and called from the PreferredMove calculator.
 * Based on piece type this class calculates available positions for the piece
 * @author oluf
 *
 */
public class PieceMoveCalculator implements MoveCalculator<AbstractGamePiece, List<Position>> {

	private List<Position> availablePositions;
	private List<Position> removedPositions;
	private AgamePiece piece;
	private int remy = 0; int remx = 0;
	private Predicate<Integer> lessThanx = (i) -> i < remx; 
	private Predicate<Integer> greaterThanx = (i) -> i > remx; 
	private Predicate<Integer> greaterThany = (i) -> i > remy; 
	private Predicate<Integer> lessThany = (i) -> i < remy;  

	public PieceMoveCalculator(List<Position> availablePositions, AgamePiece piece) {
		super();
		this.availablePositions = availablePositions;
		this.piece = piece;
	}

	public PieceMoveCalculator(List<Position> availablePositions, List<Position> removedPositions, AgamePiece piece) {
		super();
		this.availablePositions = availablePositions;
		this.removedPositions = removedPositions;
		this.piece = piece;
	}

	public PieceMoveCalculator() {
		super();
	}

	public List<Position> getRemovedPositions() {
		return removedPositions;
	}

	public void setRemovedPositions(List<Position> removedPositions) {
		this.removedPositions = removedPositions;
	}

	public List<Position> getAvailablePositions() {
		return availablePositions;
	}

	public void setAvailablePositions(List<Position> availablePositions) {
		this.availablePositions = availablePositions;
	}

	public AgamePiece getPiece() {
		return piece;
	}

	public void setPiece(AgamePiece piece) {
		this.piece = piece;
	}

	@Override
	public List<Position> calculatePositions(AbstractGamePiece t, Predicate<AbstractGamePiece> p) {
/*
 * The structure and testfunction of the receiving predicate p 
 * is defined by the the calling object.	
 * The Function descriptor of the predicate is: T -> boolean and the Predicate interface single function is defined as:
 * boolean test(T)
 * The lines below:
 * Define a predicate and give it a testfunction	
 * or Predicate<T> nameofPredicate = (anobjectofType T) -> an expression that return a boolean
 * 
 * The MoveCalculator interface single function is defined as:
 * P calculatePositions(T t,Predicate<T>p);
 * Then define a MoveCalculator and give it a calculatePosition function:
 * MoveCalculator<APawn t> pawncalc (AnobjectofType Pawn) -> an expression that return P
 * This is then an instantiation of the MoveCalculator interface.
 */
	


//        boolean result = greaterThanTen.and(lowerThanTwenty).test(15); 
//        System.out.println(result); 
        
        Predicate<ChessPieceType> checkPawn = (pieceT) -> pieceT instanceof APawn;
        piece = (AgamePiece) t;
		APawn pn = null;
		ABishop b = null;
		ARook r = null;
		AQueen qt = null;
		AKnight kn = null;
		Aking king = null;
		ChessPieceType pieceType = piece.getChessType();
		if (checkPawn.test(pieceType)) {
			pn = (APawn) pieceType;
			return pawnCalculator(pn);
		}
		if (pieceType instanceof ABishop) {
			b = (ABishop) pieceType;
			return bishopCalculator(piece);
		}
		if (pieceType instanceof ARook) {
			r = (ARook) pieceType;
			return rookCalculator(piece);
		}
		if (pieceType instanceof AQueen) {
			qt = (AQueen) pieceType;
			List<Position> tempList = bishopCalculator(piece);
			tempList.addAll(rookCalculator(piece));
			return tempList;
		}
		if (pieceType instanceof AKnight) {
			kn = (AKnight) pieceType;
		}

/*		
		Position from = piece.getmyPosition();

		pieceType type = piece.getMyType();
		pieceColor colorType = piece.getLocalColor();
		XYLocation loc = from.getXyloc();
		int x = loc.getXCoOrdinate();
		int y = loc.getYCoOrdinate();
		String posName = from.getPositionName();
		ChessPieceType pt = piece.getChessType();
		GamePiece gamePiece = (GamePiece)pt;
		gamePiece.getLegalmoves(from);
		boolean black = piece.checkBlack();
		boolean white = piece.checkWhite();*/
//		boolean whitePawn = type == pieceType. && colorType == pieceColor.WHITE;
//		boolean pawnstart = whitePawn && y > 1; 

		return null;
	}

	private List<Position> pawnCalculator(APawn pawn){
		if (pawn.isBlocked()) {
			return null;
		}
		List<Position> tempList = new ArrayList();
/*		IS this necessary?
 * Position from = pawn.getMyPosition();
		XYLocation loc = from.getXyloc();
		remx = loc.getXCoOrdinate();
		remy = loc.getYCoOrdinate();
		boolean black = piece.checkBlack();
		boolean white = piece.checkWhite();
		for (Position remPos:removedPositions) {
			XYLocation remloc = remPos.getXyloc();
			int x = remloc.getXCoOrdinate();
			int y = remloc.getYCoOrdinate();
			if (white && greaterThanx.test(x)) {
				tempList.add(remPos);
			}
			if (black && lessThanx.test(x)) {
				tempList.add(remPos);
			}
		}*/
		return tempList;
	}
	private List<Position> bishopCalculator(AgamePiece bishop){
		List<Position> tempList = new ArrayList();
		Position from = bishop.getMyPosition();
/*		XYLocation loc = from.getXyloc();
		remx = loc.getXCoOrdinate();
		remy = loc.getYCoOrdinate();*/
		for (Position remPos:removedPositions) {
			XYLocation remloc = remPos.getXyloc();
			remx = remloc.getXCoOrdinate();
			remy = remloc.getYCoOrdinate();
			for (Position availPos:availablePositions) {
				XYLocation availoc = availPos.getXyloc();
				int x = availoc.getXCoOrdinate();
				int y = availoc.getYCoOrdinate();
				boolean leftup = lessThanx.and(greaterThany).test(x);
				boolean rightup = greaterThanx.and(greaterThany).test(x);
				boolean leftdown = lessThanx.and(lessThany).test(x);
				boolean rightdown = greaterThanx.and(lessThany).test(x);
				if (leftup)
					tempList.add(remPos);
				if (rightup)
					tempList.add(remPos);
				if (leftdown)
					tempList.add(remPos);
				if (rightdown)
					tempList.add(remPos);			
			}

		}
		
		
		return tempList;
	}
	private List<Position> rookCalculator(AgamePiece rook){
		List<Position> tempList = new ArrayList();
		Position from = rook.getMyPosition();
/*		XYLocation loc = from.getXyloc();
		remx = loc.getXCoOrdinate();
		remy = loc.getYCoOrdinate();*/
		for (Position remPos:removedPositions) {
			XYLocation remloc = remPos.getXyloc();
			remx = remloc.getXCoOrdinate();
			remy = remloc.getYCoOrdinate();
			for (Position availPos:availablePositions) {
				XYLocation availoc = availPos.getXyloc();
				int x = availoc.getXCoOrdinate();
				int y = availoc.getYCoOrdinate();
				boolean leftup = lessThanx.and(greaterThany).test(x);
				boolean rightup = greaterThanx.and(greaterThany).test(x);
				boolean leftdown = lessThanx.and(lessThany).test(x);
				boolean rightdown = greaterThanx.and(lessThany).test(x);
				if (leftup)
					tempList.add(remPos);
				if (rightup)
					tempList.add(remPos);
				if (leftdown)
					tempList.add(remPos);
				if (rightdown)
					tempList.add(remPos);			
			}

		}
		return tempList;
	}

}
