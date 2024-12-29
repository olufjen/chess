package no.chess.web.model;

import java.util.HashSet;
import java.util.Iterator;

import no.basic.ontology.control.OntologyContainer;
import no.basic.ontology.model.ParentModel;
import no.chess.ontology.BlackBoardPosition;
/*import no.basis.felles.semanticweb.chess.BlackBoardPosition;
import no.basis.felles.semanticweb.chess.BlackPiece;
import no.basis.felles.semanticweb.chess.Piece;
import no.basis.felles.semanticweb.chess.WhiteBoardPosition;
import no.basis.felles.semanticweb.chess.WhitePiece;
import no.basis.felles.semanticweb.chess.impl.DefaultBlackPiece;
import no.basis.felles.semanticweb.chess.impl.DefaultWhitePiece;*/
import no.chess.ontology.Piece;
import no.chess.ontology.WhiteBoardPosition;
import no.chess.ontology.impl.DefaultWhitePiece;
import no.chess.web.model.game.AgamePiece;
import no.chess.ontology.BFObject;
/**
 * This class represent a a front end chess piece.
 * It contains information about the ontology chesspiece and its position.
 * @author oluf
 *
 */
public class ChessPiece extends ParentModel{

	private String position;
	private String column;
	private String color;
	private int value;
	private String name; // The full name of the piece (wP, bP etc)
	private String[] legalMoves;
	private boolean use = true;
	private String pieceName = ""; // The type of piece: P,R, K etc.
	private Piece blackPiece = null; // The ontology piece for this piece if it is black.
	private Piece whitePiece = null;// The ontology piece for this piece if it is white.
	private BlackBoardPosition blackBoardPosition = null;
	private WhiteBoardPosition whiteBoardPosition = null;
	private String ontlogyName = null;
	private String fullName =null; // This represent the full ontology name of the piece
	private AgamePiece myPiece;
	private String predicate = "none"; //The predicate used by this object
	private String capturedName = null; // The ontology name of the latest captured piece (added 24.12.24)
	public ChessPiece(String position, String color, String name,
			String[] legalMoves) {
		super();
		this.position = position;
//		this.column = position.substring(0, 1);
		this.color = color;
		this.name = name;
		this.legalMoves = legalMoves;
		this.value = 0;
		this.pieceName = name.substring(1);
		calculateValue();
	}
	public void createOntologyPiece(OntologyContainer modelContainer,String newName) {
		String name = "http://www.co-ode.org/ontologies/ont.owl#" + newName;
		no.chess.ontology.ChessPiece newPiece = modelContainer.getChessFactory().createPiece(name);
		if (blackPiece != null)
			blackPiece = null;
		whitePiece = (Piece) newPiece;
		System.out.println(whitePiece.toString());
	}
	private void setcorrectPieceName(HashSet<String> names){
		for (String o : names) {
			name = o.toString();
		}
		pieceName = name.substring(1);

	}
	
	public String getCapturedName() {
		return capturedName;
	}
	public void setCapturedName(String capturedName) {
		this.capturedName = capturedName;
	}
	public String getPredicate() {
		return predicate;
	}
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}
	public AgamePiece getMyPiece() {
		return myPiece;
	}
	public void setMyPiece(AgamePiece myPiece) {
		this.myPiece = myPiece;
	}
	public String getOntlogyName() {
		if (ontlogyName == null)
			ontlogyName = "x";
		return ontlogyName;
	}

	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public void setOntlogyName(String ontlogyName) {
		this.ontlogyName = ontlogyName;
	}

	public String getPieceName() {
		return pieceName;
	}

	public void setPieceName(String pieceName) {
		this.pieceName = pieceName;
	}


	public Piece getBlackPiece() {
		return blackPiece;
	}

	public void setBlackPiece(Piece blackPiece) {
		this.blackPiece = blackPiece;
		HashSet<String> names = (HashSet<String>) this.blackPiece.getHasName();
		setcorrectPieceName(names);
	}

	public Piece getWhitePiece() {
		return whitePiece;

	}

	public void setWhitePiece(Piece whitePiece) {
		this.whitePiece = whitePiece;
		HashSet<String> names = (HashSet<String>) this.whitePiece.getHasName();
		setcorrectPieceName(names);
	}

	public void setWhitePiece(DefaultWhitePiece whitePiece) {
		this.whitePiece = whitePiece;
		HashSet<String> names = (HashSet<String>) this.whitePiece.getHasName();
		setcorrectPieceName(names);
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

	public void setWhiteBoardPosition(WhiteBoardPosition whiteBoardPosition) {
		this.whiteBoardPosition = whiteBoardPosition;
	}
	/**
	 * acceptMove
	 * This method accepts a move according to the rules of the chessgame for a given piece.
	 * The move is only accepted if it is an opposing piece that is removed
	 * @param newPos
	 * @param position
	 */
	public boolean acceptMove(String newPos, Position oldPosition,Position newPosition) {
		ChessPiece opposingPiece = newPosition.getUsedBy();
		capturedName = opposingPiece.getOntlogyName();
		boolean accept = false;
		if (blackPiece == null && opposingPiece.getWhitePiece() == null) {
       	   	oldPosition.setUsedBy(); // The start position is emptied
//           	oldPosition.setInUse(false);
           	HashSet pieces = oldPosition.getPieces();
           	newPosition.setUsedBy(this);
        	newPosition.setInUse(true);
        	newPosition.setPieces(pieces);
        	accept = true;
 /*
  * Setting opponent piece passive: added 21.04.20       	
  */
        	opposingPiece.setUse(false);
        	opposingPiece.getMyPiece().setActive(false);
//        	opposingPiece.setValue(-1); //How to set a piece inactive (vacant)? See Ontology
			// move is legal. Later: Check move according to rules given for this piece
		}
		if (whitePiece == null && opposingPiece.getBlackPiece() == null) {
			// Move is legal
       	   	oldPosition.setUsedBy();
//           	oldPosition.setInUse(false);
           	HashSet pieces = oldPosition.getPieces();
           	newPosition.setUsedBy(this);
        	newPosition.setInUse(true);
        	newPosition.setPieces(pieces);
//        	opposingPiece.setValue(-1); //How to set a piece inactive (vacant)? See Ontology
        	accept = true;
 /*
 * Setting opponent piece passive: added 21.04.20       	
 */        	
        	opposingPiece.setUse(false);
        	opposingPiece.getMyPiece().setActive(false);
		}
		return accept;
	}
	public void calculateValue(){
		char t = name.charAt(1);
		switch(t)
		{
		case 'P':
			value = 1;
			break;
		case 'R':
			value = 5;
			break;
		case 'B':
			value = 3;
			break;
		case 'N':
			value = 3;
			break;	
		case 'Q':
			value = 9;
			break;
		default:
			value = 0;
			break;
		}
	}
	public void calculateMoves(){
		
	}
	public boolean isUse() {
		return use;
	}

	public void setUse(boolean use) {
		this.use = use;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String[] getLegalMoves() {
		return legalMoves;
	}
	public void setLegalMoves(String[] legalMoves) {
		this.legalMoves = legalMoves;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
		this.column = position.substring(0, 1);
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("no.chess.web.model.ChessPiece Ontology name "+ontlogyName+"\nChesspiece position "+position.toString());
		if (getMyPiece() != null)
			result.append("\n Piece active: "+getMyPiece().isActive());
		return result.toString();
	}
}
