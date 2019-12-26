package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import no.chess.web.model.Position;
import no.games.chess.ChessAction;
import no.games.chess.ChessFunctions;

/**
 * ChessActionImpl
 * This class implements the ChessAction interface.
 * It contains an AgamePiece and its available (reachable) positions
 * Revised: From this a preferred position for this piece is calculated by the player
 * From this a possible move and a preferred position is calculated
 * It also calculates which reachable positions are occupied by other pieces belonging to the same player.
 * They are held in the List positionRemoved
 * The PreferredMove processor uses this information to determine which positions are available for a given piece
 * @author oluf 
 *
 */
public class ChessActionImpl implements ChessAction<HashMap<String, Position>,List<Position>,List<Position>,AgamePiece,Position> {

	private HashMap<String, Position> positions;
	private AgamePiece chessPiece;
	private List<Position> availablePositions;
	private List<Position> positionRemoved;
	private Position preferredPosition = null; // Each action has a preferred position that the piece should move to
	private Position strikePosition = null; // This position is set if it is occupied by an opponent piece
	private boolean strike = false;
	private APlayer player;
	private ApieceMove possibleMove;
	private int pn = 0;
	private int pny = 0;

	public ChessActionImpl(HashMap<String, Position> positions, AgamePiece chessPiece,APlayer player) {
		super();
		this.positions = positions;
		this.chessPiece = chessPiece;
		this.player = player;
		this.availablePositions = getActions(); // The positionRemoved are also created and filled. They are positions occupied by other pieces owned by the player
		String name = this.chessPiece.getMyPiece().getPieceName();
		pn = this.chessPiece.getMyPosition().getIntRow()*10;
		pny = this.chessPiece.getMyPosition().getIntColumn();
		Integer prn = new Integer(pn+pny);
		PreferredMoveProcessor pr = new PreferredMoveProcessor(prn,name);
		possibleMove = ChessFunctions.processChessgame(this,chessPiece, pr); // The processor can be replaced by a lambda expression?
		if (possibleMove != null)
			preferredPosition = possibleMove.getToPosition();
//		preferredPosition = player.calculatePreferredPosition(chessPiece,this);      

	}


	public Position getStrikePosition() {
		return strikePosition;
	}


	public void setStrikePosition(Position strikePosition) {
		this.strikePosition = strikePosition;
	}


	public boolean isStrike() {
		return strike;
	}


	public void setStrike(boolean strike) {
		this.strike = strike;
	}


	public ApieceMove getPossibleMove() {
		return possibleMove;
	}


	public void setPossibleMove(ApieceMove possibleMove) {
		this.possibleMove = possibleMove;
	}


	public List<Position> getPositionRemoved() {
		return positionRemoved;
	}


	public void setPositionRemoved(List<Position> positionRemoved) {
		this.positionRemoved = positionRemoved;
	}


	public APlayer getPlayer() {
		return player;
	}


	public void setPlayer(APlayer player) {
		this.player = player;
	}


	public AgamePiece getChessPiece() {
		return chessPiece;
	}


	public void setChessPiece(AgamePiece chessPiece) {
		this.chessPiece = chessPiece;
	}


	public void setPositions(HashMap<String, Position> positions) {
		this.positions = positions;
	}


	public HashMap<String,Position> getPositions() {
		return positions;
	}

	public List<Position> getAvailablePositions() {
		return availablePositions;
	}


	public void setAvailablePositions(List<Position> availablePositions) {
		this.availablePositions = availablePositions;
	}


	public Position getPreferredPosition() {
		if (preferredPosition == null) {
			preferredPosition = player.calculatePreferredPosition(chessPiece,this);
		}
		return preferredPosition;
	}


	public void setPreferredPosition(Position preferredPosition) {
		this.preferredPosition = preferredPosition;
	}


	/**
	 * getActions
	 * This method returns all possible position reachable by the piece belonging to this action
	 * If a position is occupied by another piece for this action's player, this position is placed in the removed list
	 * @return
	 */
	public List<Position> getActions(){
		availablePositions = new ArrayList(positions.values());
		positionRemoved = new ArrayList();
		List<AgamePiece> pieces = player.getMygamePieces(); 
		for (Position position:availablePositions) {
			for (AgamePiece otherPiece:pieces) {
				Position pos = otherPiece.getMyPosition();
				if (pos != null) {
					if (otherPiece.getMyPosition().getPositionName().equals(position.getPositionName())) {
						positionRemoved.add(position);
					}

				}
			}

		
		}
		return availablePositions;
		
	}
	public String toString() {
		String posName = "Unknown";
		if (preferredPosition != null)
			posName = preferredPosition.getPositionName();
		StringBuffer logText = new StringBuffer("ChessAction: Preferred Position " + posName+ " Piece " + chessPiece.toString());
		return logText.toString();
	}
}
