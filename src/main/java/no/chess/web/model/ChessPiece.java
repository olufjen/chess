package no.chess.web.model;

import java.util.HashSet;
import java.util.Iterator;

import no.basis.felles.model.ParentModel;
import no.basis.felles.semanticweb.chess.BlackBoardPosition;
import no.basis.felles.semanticweb.chess.BlackPiece;
import no.basis.felles.semanticweb.chess.Piece;
import no.basis.felles.semanticweb.chess.WhiteBoardPosition;
import no.basis.felles.semanticweb.chess.WhitePiece;
import no.basis.felles.semanticweb.chess.impl.DefaultBlackPiece;
import no.basis.felles.semanticweb.chess.impl.DefaultWhitePiece;

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
	private String name;
	private String[] legalMoves;
	private boolean use = true;
	private String pieceName = "";
	private Piece blackPiece = null;
	private Piece whitePiece = null;
	private BlackBoardPosition blackBoardPosition = null;
	private WhiteBoardPosition whiteBoardPosition = null;
	private String ontlogyName = null;
	
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
	private void setcorrectPieceName(HashSet<String> names){
		Iterator<String> namesIterator = names.iterator();
		while (namesIterator.hasNext()){
			String ontpieceName = namesIterator.next();
			if (pieceName != null){
				name = ontpieceName;
				break;
			}
		}
	}
	public String getOntlogyName() {
		return ontlogyName;
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
	
}
