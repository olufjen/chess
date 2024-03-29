package no.chess.web.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.IntPredicate;

import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import aima.core.search.csp.examples.NQueensCSP;
import aima.core.util.datastructure.XYLocation;
import no.basic.ontology.model.OntologyModel;
import no.basic.ontology.model.ParentModel;
import no.chess.ontology.BlackBoardPosition;
import no.chess.ontology.BoardPosition;
import no.chess.ontology.Piece;
import no.chess.ontology.Taken;
import no.chess.ontology.WhiteBoardPosition;
import no.chess.ontology.impl.DefaultWhitePiece;

/*import no.basis.felles.semanticweb.chess.BlackBoardPosition;
import no.basis.felles.semanticweb.chess.BlackPiece;
import no.basis.felles.semanticweb.chess.BoardPosition;
import no.basis.felles.semanticweb.chess.Piece;
import no.basis.felles.semanticweb.chess.WhiteBoardPosition;
import no.basis.felles.semanticweb.chess.WhitePiece;
import no.basis.felles.semanticweb.chess.impl.DefaultBlackPiece;
import no.basis.felles.semanticweb.chess.impl.DefaultWhitePiece;*/

/**
 * This class represent a position on the chessboard
 * It also contains the correct ontology whiteboard or blackboard position
 * If it is occupied it contains the correct chesspiece (usedBy) and the equivalent ontology chesspiece (HashSet<Piece> pieces).
 * 
 * @author oluf
 *
 */
public class Position extends ParentModel {
	private String positionName;
	private String column;
	private int intColumn;
	private String row;
	private int intRow;
	private String positionColor;
	private boolean inUse;
	private boolean centerlefthigh = false;
	private boolean centerrighthigh = false;
	private boolean centerrightlow = false;
	private boolean centerleftlow = false;
	private String predicate = "none"; //The predicate used by this object
	private String piecePred = "none";
	private List<String> predicates; 
	private ChessPiece usedBy;
	private ChessPiece removed;
	private Stack<ChessPiece> removedPieces;
	private BlackBoardPosition blackBoardPosition = null;
	private WhiteBoardPosition whiteBoardPosition = null;
	private HashSet<Piece> pieces;
	private XYLocation xyloc = null; // Represents the XYLocation of a aima board
	private Integer sumDif = new Integer(0); // A distance indicator in a set of removed positions (See the PreferredMove processor)
	private int sumNorth = 0; //Is greater than 0 if position is north of current position
	private int sumSouth = 0;//Is greater than 0 if position is south of current position
	private int sumEast = 0; //Is greater than 0 if position is east of current position
	private int sumWest = 0; //Is greater than 0 if position is west of current position
/*
 * These booleans indicates which direction this position belongs to
 * It is used when calculating removed positions for bishops queens and rooks olj 14.06.21	
 */
	private boolean nw = false;
	private boolean ne = false;
	private boolean sw = false;
	private boolean se = false;
	private boolean north = false;
	private boolean south = false;
	private boolean east = false;
	private boolean west = false;
	
	public static enum direction{
		NW,
		NE,
		SW,
		SE,
		NORTH,
		SOUTH,
		EAST,
		WEST,
		NONE;
	}
	private direction mydirection;
	private direction lastDirection;
	private direction neDirection = direction.NE;
	private direction nWDirection = direction.NW;
	private direction seDirection = direction.SE;
	private direction swDirection = direction.SW;
	private direction northDirection = direction.NORTH;
	private direction southDirection = direction.SOUTH;
	private direction westDirection = direction.WEST;
	private direction eastDirection = direction.EAST;
	
	private boolean opponentRemove = false; // True if this position is blocked by opponent. It is set by the chessAction when opponent pieces are checked
	private boolean friendlyPosition = false; // True when this position is occupied by a friendly position, and therefore is protected
	// It is set when this position is put in the removed list
	public Position(String positionName, boolean inUse, ChessPiece usedBy) {
		super();
		this.positionName = positionName;
		this.inUse = inUse;
		if (usedBy == null){
			String[] legalMoves = {};
			usedBy =  new ChessPiece("","x","ee",legalMoves);
		}
		this.column = positionName.substring(0, 1);
		this.row = positionName.substring(1);
		intRow = Integer.parseInt(row);
		this.usedBy = usedBy;
		intColumn = findColumn();
		calculateColor();
		int ycol = getIntRow() - 1;	// This is the correct position definition
		int xrow = getIntColumn() - 1;
		xyloc = new XYLocation(xrow,ycol);
		centerleftlow = ycol == 3 && xrow == 3;
		centerlefthigh = ycol == 3 && xrow == 4;
		centerrightlow = ycol == 4 && xrow == 3;
		centerrighthigh = ycol == 4 && xrow == 4;
		removedPieces = new Stack();
		predicates = new ArrayList<String>();
		mydirection = direction.NONE;
	}
	public Position(XYLocation loc, boolean inUse, ChessPiece usedBy) {
		this.xyloc = loc;
		this.usedBy = usedBy;
		this.inUse = inUse;
		if (usedBy == null){
			String[] legalMoves = {};
			usedBy =  new ChessPiece("","x","ee",legalMoves);
		}
		int x = loc.getXCoOrdinate();
		int y = loc.getYCoOrdinate();
		setIntRow(y+1);
		setIntColumn(x+1);
		String row = String.valueOf(y+1);
		String col = findColumnletter(x);
		this.positionName = col+row;
		calculateColor();
		int ycol = getIntRow() - 1;	// This is the correct position definition
		int xrow = getIntColumn() - 1;
		centerleftlow = ycol == 3 && xrow == 3;
		centerlefthigh = ycol == 3 && xrow == 4;
		centerrightlow = ycol == 4 && xrow == 3;
		centerrighthigh = ycol == 4 && xrow == 4;
		removedPieces = new Stack();
		predicates = new ArrayList<String>();
		mydirection = direction.NONE;
	}
	
	
	public int getSumNorth() {
		return sumNorth;
	}
	public void setSumNorth(int sumNorth) {
		this.sumNorth = sumNorth;
	}
	public int getSumSouth() {
		return sumSouth;
	}
	public void setSumSouth(int sumSouth) {
		this.sumSouth = sumSouth;
	}
	public int getSumEast() {
		return sumEast;
	}
	public void setSumEast(int sumEast) {
		this.sumEast = sumEast;
	}
	public int getSumWest() {
		return sumWest;
	}
	public void setSumWest(int sumWest) {
		this.sumWest = sumWest;
	}
	public boolean isNorth() {
		return north;
	}
	public void setNorth(boolean north) {
		this.north = north;
		mydirection = direction.NORTH;
	}
	public boolean isSouth() {
		return south;
	}
	public void setSouth(boolean south) {
		this.south = south;
		mydirection = direction.SOUTH;
	}
	public boolean isEast() {
		return east;
	}
	public void setEast(boolean east) {
		this.east = east;
		mydirection = direction.EAST;
	}
	public boolean isWest() {
		return west;
	}
	public void setWest(boolean west) {
		this.west = west;
		mydirection = direction.WEST;
	}
	public direction getNorthDirection() {
		return northDirection;
	}
	public void setNorthDirection(direction northDirection) {
		this.northDirection = northDirection;
	}
	public direction getSouthDirection() {
		return southDirection;
	}
	public void setSouthDirection(direction southDirection) {
		this.southDirection = southDirection;
	}
	public direction getWestDirection() {
		return westDirection;
	}
	public void setWestDirection(direction westDirection) {
		this.westDirection = westDirection;
	}
	public direction getEastDirection() {
		return eastDirection;
	}
	public void setEastDirection(direction eastDirection) {
		this.eastDirection = eastDirection;
	}
	public direction getNeDirection() {
		return neDirection;
	}
	public void setNeDirection(direction neDirection) {
		this.neDirection = neDirection;
	}
	public direction getnWDirection() {
		return nWDirection;
	}
	public void setnWDirection(direction nWDirection) {
		this.nWDirection = nWDirection;
	}
	public direction getSeDirection() {
		return seDirection;
	}
	public void setSeDirection(direction seDirection) {
		this.seDirection = seDirection;
	}
	public direction getSwDirection() {
		return swDirection;
	}
	public void setSwDirection(direction swDirection) {
		this.swDirection = swDirection;
	}
	public direction getLastDirection() {
		return lastDirection;
	}
	public void setLastDirection(direction lastDirection) {
		this.lastDirection = lastDirection;
	}
	public direction getMydirection() {
		return mydirection;
	}
	public void setMydirection(direction mydirection) {
		this.mydirection = mydirection;
	}
	public void setDefaultdirection() {
		this.mydirection = direction.NONE;
		sumNorth = 0;
		sumSouth = 0;
		sumEast = 0;
		sumWest = 0;
	}
	public Integer getSumDif() {
		return sumDif;
	}
	public void setSumDif(Integer sumDif) {
		this.sumDif = sumDif;
	}
	public boolean isOpponentRemove() {
		return opponentRemove;
	}
	public void setOpponentRemove(boolean opponentRemove) {
		this.opponentRemove = opponentRemove;
	}
	
	public boolean isFriendlyPosition() {
		return friendlyPosition;
	}
	public void setFriendlyPosition(boolean friendlyPosition) {
		this.friendlyPosition = friendlyPosition;
	}
	public boolean isNw() {
		return nw;
	}
	public void setNw(boolean nw) {
		this.nw = nw;
		mydirection = direction.NW;
	}
	public boolean isNe() {
		return ne;
	}
	public void setNe(boolean ne) {
		this.ne = ne;
		mydirection = direction.NE;
	}
	public boolean isSw() {
		return sw;
	}
	public void setSw(boolean sw) {
		this.sw = sw;
		mydirection = direction.SW;
	}
	public boolean isSe() {
		return se;
	}
	public void setSe(boolean se) {
		this.se = se;
		mydirection = direction.SE;
	}
	public String getPiecePred() {
		return piecePred;
	}
	public void setPiecePred(String piecePred) {
		this.piecePred = piecePred;
	}
	public String getPredicate() {
		return predicate;
	}
	public void setPredicate(String predicate) {
		this.predicate = predicate;
		String str[] = this.predicate.split(";");
		predicates = Arrays.asList(str);
	}
	
	public List<String> getPredicates() {
		return predicates;
	}
	public void setPredicates(List<String> predicates) {
		this.predicates = predicates;
	}
	public boolean isCenterlefthigh() {
		return centerlefthigh;
	}
	public void setCenterlefthigh(boolean centerlefthigh) {
		this.centerlefthigh = centerlefthigh;
	}
	public boolean isCenterrighthigh() {
		return centerrighthigh;
	}
	public void setCenterrighthigh(boolean centerrighthigh) {
		this.centerrighthigh = centerrighthigh;
	}
	public boolean isCenterrightlow() {
		return centerrightlow;
	}
	public void setCenterrightlow(boolean centerrightlow) {
		this.centerrightlow = centerrightlow;
	}
	public boolean isCenterleftlow() {
		return centerleftlow;
	}
	public void setCenterleftlow(boolean centerleftlow) {
		this.centerleftlow = centerleftlow;
	}
	public XYLocation getXyloc() {
		return xyloc;
	}

	public void setXyloc(XYLocation xyloc) {
		this.xyloc = xyloc;
	}

	public HashSet<Piece> getPieces() {
		return pieces;
	}
	
	public Stack<ChessPiece> getRemovedPieces() {
		return removedPieces;
	}
	public void setRemovedPieces(Stack<ChessPiece> removedPieces) {
		this.removedPieces = removedPieces;
	}

	private IntPredicate evenNumbers = (int i) -> i%2 == 0;
	private void calculateColor(){
		if (evenNumbers.test(intRow+intColumn)){
			positionColor = "B";
		}else
			positionColor = "W";
	}
	public <T,R> Integer transformValue(List<String> letters,Function<List<String>, Integer> f) {
			return f.apply(letters);
	}
	private String findColumnletter(int col) {
		List <String> letters  = Arrays.asList("a","b","c","d","e","f","g","h");
		for (int i = 0;i<8;i++) {
			if (i == col)
				return letters.get(i);
		}
		return null;
		
	}
	private int findColumn(){
		List <String> letters  = Arrays.asList("a","b","c","d","e","f","g","h");
		Function<List<String>,Integer> f = (List<String> l) -> {
			int ct = 0;
			for (String s:letters){
				ct++;
				if (s.equals(column)){
					return ct;
				}
	
			}
			return ct;
		};
		return transformValue(letters,f);
	}
	/**
	 * checkUsed
	 * This method checks if there is disagreement between the inUse flag and 
	 * the Piece in this position
	 * @since 6.12.19 must incorporate the case when the piece is inactive (removed by opponent)
	 */
	public void checkUsed() {
		if (inUse &&getUsedBy() == null){
			setInUse(false);
		}
	}
	/**
	 * setPieces
	 * This method puts the correct ontology chess piece to the correct ontology position
	 * It is called whenever a player makes a move.
	 * @param pieces
	 */
	public void setPieces(HashSet<Piece> pieces) {
		this.pieces = pieces;
		if (pieces != null){
			Iterator<Piece> pieceIterator = pieces.iterator();
			while (pieceIterator.hasNext()){
				Piece piece = pieceIterator.next();
				IRI ir = piece.getOwlIndividual().getIRI();
				String irs = ir.toString();
		    	char sep = '#';
		    	String name = extractString(irs, sep,-1); 
		    	if (name.startsWith("White")){
		    		usedBy.setWhitePiece(piece);
		    		if (piece != null)
		    			checkPieceOccupation(piece);
		    		else
		    			System.out.println("Piece is null!! Name of piece: "+name+" Name of chess piece: "+usedBy.getPieceName()+" "+usedBy.getName());
		    	}
		    	if (name.startsWith("Black")){
		    		usedBy.setBlackPiece(piece);
		    		if (piece != null)
		    			checkPieceOccupation(piece);
		    		else
		    			System.out.println("Piece is null!! Name of piece: "+name+" Name of chess piece: "+usedBy.getPieceName()+" "+usedBy.getName());
		    	}
		    	usedBy.setOntlogyName(name);
		    	usedBy.setFullName(irs);
//		    	usedBy.setPredicate(piecePred);
		    	System.out.println("setPieces: Name of piece: "+name+" Name of chess piece: "+usedBy.getPieceName()+" "+usedBy.getName());

			}
		} 
	}
	/**
	 * checkPieceOccupation
	 * This method checks if an individual piece occupies correct position.
	 * It ensures that the piece individual always occupies correct position after a move
	 * @param piece
	 */
	public void checkPieceOccupation(Piece piece){
		if (piece.getOccupies() != null){
			HashSet<BoardPosition> whitePosset = (HashSet<BoardPosition>) piece.getOccupies();
			Iterator<BoardPosition> whitePosIterator =  whitePosset.iterator(); // Empty after move???!!
			while(whitePosIterator.hasNext()){
				BoardPosition whitePos = whitePosIterator.next();
				IRI ir = whitePos.getOwlIndividual().getIRI();
				String irs = ir.toString();
				char sep = '#';
				String name = extractString(irs, sep,-1);
				if (name.equals(positionName)){
//					System.out.println("Occupies correct position: " + irs+ " " + name + " " +positionName);
				}else {
					System.out.println("Occupies wrong position: "+ irs+ " " + name + " " +positionName);
					piece.removeOccupies((Taken) whitePos);
					if (whiteBoardPosition != null){
						piece.addOccupies((Taken) whiteBoardPosition);
						System.out.println("New white position: "+ whiteBoardPosition.getOwlIndividual().getIRI().toString() + " " +positionName);
					}
					else if (blackBoardPosition != null){
						piece.addOccupies((Taken) blackBoardPosition);
						System.out.println("New black position: "+ blackBoardPosition.getOwlIndividual().getIRI().toString() + " " +positionName);
					}
				
				}
			}			
		}else{
			System.out.println("Piece does not occupy position: " + piece.toString()+positionName);
		}
			
	}
	public void checkWhiteOccupation(HashSet<WhiteBoardPosition> whitePositions){
		Iterator<WhiteBoardPosition> whitePosIterator =  whitePositions.iterator();
	      while(whitePosIterator.hasNext()){
	    	  WhiteBoardPosition whitePos = whitePosIterator.next();
	    	  IRI ir = whitePos.getOwlIndividual().getIRI();
	    	  HashSet<Piece> pieces =  (HashSet<Piece>)((Taken) whitePos).getIsOccupiedBy();
	    	  String irs = ir.toString();
	    	  OWLNamedIndividual wp = whitePos.getOwlIndividual();
	    	  char sep = '#';
	    	  String name = extractString(irs, sep,-1);
	    	  if (name.equals(positionName) && usedBy != null){
//	    		  CodeGenerationInference inference = usedBy.getWhitePiece().
	    		  if (pieces.isEmpty() || pieces == null){
	    			Piece newIsOccupiedBy = new DefaultWhitePiece(null, ir);
					((Taken) whitePos).addIsOccupiedBy(newIsOccupiedBy );
	    		  }
	    		  if (pieces != null){
	    			  Iterator<Piece> pieceIterator = pieces.iterator();
	    			  while (pieceIterator.hasNext()){
	    				  Piece newPiece = pieceIterator.next();
	    				  IRI irp = newPiece.getOwlIndividual().getIRI();
	    				  String irsp = ir.toString();
	    				  char sepp = '#';
	    				  String pname = extractString(irs, sepp,-1); 
	    			  }
	    		  }
	    	  }
	      }
		
	}

	public int getIntColumn() {
		return intColumn;
	}

	public void setIntColumn(int intColumn) {
		this.intColumn = intColumn;
	}

	public String getRow() {
		return row;
	}

	public void setRow(String row) {
		this.row = row;
	}

	public int getIntRow() {
		return intRow;
	}

	public void setIntRow(int intRow) {
		this.intRow = intRow;
	}

	public String getPositionColor() {
		return positionColor;
	}

	public void setPositionColor(String positionColor) {
		this.positionColor = positionColor;
	}

	public BlackBoardPosition getBlackBoardPosition() {
		return blackBoardPosition;
	}

	public void setBlackBoardPosition(BlackBoardPosition blackBoardPosition) {
		this.blackBoardPosition = blackBoardPosition;
	}

	public WhiteBoardPosition getWhiteBoardPosition() {
		return whiteBoardPosition;
	}

	public void setWhiteBoardPosition(WhiteBoardPosition whitePos) {
		this.whiteBoardPosition = whitePos;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getPositionName() {
		return positionName;
	}
	public void setPositionName(String positionName) {
		this.positionName = positionName;
	}
	public boolean returnInuse() {
		return inUse;
	}
	/**
	 * isInUse()
	 * Checks if position is in use
	 * @return true if position is in use and piece is active
	 * @since 15.11.19 check if piece in position is occupied by a piece that has been removed
	 */
	public boolean isInUse() {
		boolean active = false;
		if (usedBy != null && usedBy.getMyPiece() != null)
			active = usedBy.getMyPiece().isActive();
		if (active && inUse)
			return inUse;
		if (!active && usedBy != null && usedBy.getMyPiece() != null)
			return active;
		return inUse;
	}
	public boolean notisInUse() {
		return !inUse;
	}
	public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}
	public ChessPiece getUsedBy() {
		return usedBy;
	}
	
	public ChessPiece getRemoved() {
		return removed;
	}
	public void setRemoved(ChessPiece removed) {
		this.removed = removed;
	}

	/**
	 * returnPiece()
	 * This method sets a removed piece back in position and sets it active again.
	 * It is called from the chessstateimpl object emptyMovements() and checkPlayers() methods
	 */
	public void returnPiece() {
/*		if (removed != null) {
			usedBy = removed;
		}*/
		if (removed == null && removedPieces.empty()) {
			setUsedBy();
		}
		if (removedPieces != null && !removedPieces.empty()) {
			ChessPiece removed = removedPieces.pop();
			if (removed.isUse()) { // Added 21.04.20  The use flag is only set false in chesspiece acceptmove method
				usedBy = removed;
				usedBy.getMyPiece().setActive(true);
				inUse = true; // Added 4.08.20 olj
			}
		}

	}
	public void returnPiece(ChessPiece piece) {
		this.usedBy = piece;
	}
	/**
	 * This method is used when a position is set vacant.
	 * IT Is called from the Chessboard object and from the ChessPiece object .acceptMove method
	 * 
	 */
	public void setUsedBy() {
		this.usedBy = null;
		inUse = false;
	}
	/**
	 * setUsedandRemoved
	 * This method sets the chosen ChessPiece to this position
	 * and also puts it in the removed list.
	 * This make sure that the piece is correctly restored after a search process
	 * and all moves generated in the search are removed.
	 * @param usedBy - the chesspiece
	 */
	public void setUsedandRemoved(ChessPiece usedBy) {
		this.usedBy = usedBy;
		inUse = true;
		setRemoved(usedBy);
		removedPieces.push(removed);
		
	}
	/**
	 * This method is used when a piece is moved from this position
	 * and so the position becomes vacant or the piece occupying this position is removed.
	 * The method is called from Chessboard when:
	 * 1 Creating an empty chessboard
	 * 2 Creating ontology positions
	 * 3 Determine if a move is legal . This is checked when a move is made
	 * The determinemove method calls the chesspiece's acceptmove method
	 * which calls this method.
	 * The method is also called from the AchessGame object movePiece method when this object is used by the
	 * ChessState object in the search process
	 * @param usedBy
	 */
	public void setUsedBy(ChessPiece usedBy) {
		if (this.usedBy != null && this.usedBy.getMyPiece() != null && this.usedBy != usedBy) {
			removed = this.usedBy;
			removed.getMyPiece().setActive(false);
			removedPieces.push(removed);
		}
		this.usedBy = usedBy;
		inUse = true;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		String p = "None";
		String gp = "Gamenone";
		if (usedBy != null) {
			p = usedBy.toString();
			gp = usedBy.getMyPiece().getName();
		}
		
		builder.append(positionName+ " Color "+positionColor+" Direction "+mydirection+" sumdif "+ sumDif+" Piece  "+p+" "+inUse+" Friendly "+friendlyPosition+" gamepiece "+gp+"\n");
		return builder.toString();
	}
}
