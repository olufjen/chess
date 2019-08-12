package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import no.chess.web.model.Position;
import no.games.chess.ChessAction;

/**
 * ChessActionImpl
 * This class implements the ChessAction interface.
 * It contains an AgamePiece and its available (reachable) positions
 * From this a preferred position for this piece is calculated by the player
 * It also calculates which reachable positions are occupied by other pieces belonging to the same player.
 * @author oluf 
 *
 */
public class ChessActionImpl implements ChessAction<HashMap<String, Position>,List<Position>,List<Position>,AgamePiece<Position>,Position> {

	private HashMap<String, Position> positions;
	private AgamePiece chessPiece;
	private List<Position> availablePositions;
	private List<Position> positionRemoved;
	private Position preferredPosition; // Each action has a preferred position that the piece should move to
	private APlayer player;

	public ChessActionImpl(HashMap<String, Position> positions, AgamePiece chessPiece,APlayer player) {
		super();
		this.positions = positions;
		this.chessPiece = chessPiece;
		this.player = player;
		this.availablePositions = getActions();
		preferredPosition = player.calculatePreferredPosition(chessPiece,this);

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
		List<Position> availablePositions = new ArrayList(positions.values());
		positionRemoved = new ArrayList();
		//List<AgamePiece> pieces = player.getMygamePieces(); Available positions only for the chessPiee belonging to the action !!
		for (Position position:availablePositions) {
			
				if (chessPiece.getMyPosition().getPositionName().equals(position.getPositionName())) {
					positionRemoved.add(position);
				}
		
		}
		return availablePositions;
		
	}
	public String toString() {
		String posName = "Unkown";
		if (preferredPosition != null)
			posName = preferredPosition.getPositionName();
		StringBuffer logText = new StringBuffer("Preferred Position " + posName+ " Piece " + chessPiece.toString());
		return logText.toString();
	}
}
