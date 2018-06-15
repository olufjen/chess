package no.chess.web.server.resource;

import no.basis.felles.server.resource.SessionServerResource;

/**
 * Denne klasses er felles for alle resursklasser i Sjakk
 * 
 * @author oluf
 *
 */
public class ChessServerResource extends SessionServerResource {


	protected String moveId = "move";
	protected String pieceId = "piece";
	protected String piece = "p";
	protected String newPos = "pos";
	protected String oldPos = "xy";
	protected String pawnId = "whitepawn1";
	protected String fenPosid = "fenpos";
	protected String chessBoardsession = "chessboard";
	
	protected String[] positionId = {"a1","b1","c1","d1","e1","f1","g1","h1","a2","b2",
			"c2","d2","e2","f2","g2","h2","a3","b3","c3","d3","e3","f3",
			"g3","h3","a4","b4","c4","d4","e4","f4","g4","h4","a5","b5",
			"c5","d5","e5","f5","g5","h5","a6","b6","c6","d6","e6","f6",
			"g6","h6","a7","b7","c7","d7","e7","f7","g7","h7","a8","b8",
			"c8","d8","e8","f8","g8","h8"};
	
	public ChessServerResource() {
		super();
	
	}



	public String getOldPos() {
		return oldPos;
	}



	public void setOldPos(String oldPos) {
		this.oldPos = oldPos;
	}



	public String getFenPosid() {
		return fenPosid;
	}



	public void setFenPosid(String fenPosid) {
		this.fenPosid = fenPosid;
	}



	public String[] getPositionId() {
		return positionId;
	}



	public void setPositionId(String[] positionId) {
		this.positionId = positionId;
	}



	public String getPawnId() {
		return pawnId;
	}

	public void setPawnId(String pawnId) {
		this.pawnId = pawnId;
	}


	public String getMoveId() {
		return moveId;
	}

	public void setMoveId(String moveId) {
		this.moveId = moveId;
	}

	public String getPiece() {
		return piece;
	}

	public void setPiece(String piece) {
		this.piece = piece;
	}

	public String getNewPos() {
		return newPos;
	}

	public void setNewPos(String newPos) {
		this.newPos = newPos;
	}

	public String getPieceId() {
		return pieceId;
	}

	public void setPieceId(String pieceId) {
		this.pieceId = pieceId;
	}

	
	
	
}
