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
 * A ChessAction is created by the call to the ChesState getActions() method
 * It contains an AgamePiece and its available (reachable) positions
 * Revised: From this a preferred position for this piece is calculated by the player
 * From this a possible move is created and a preferred position is calculated
 * It also calculates which reachable positions are occupied by other pieces belonging to the same player.
 * They are held in the List positionRemoved
 * The PreferredMove processor uses this information to determine which positions are available for a given piece
 * @author oluf 
 *
 */
public class ChessActionImpl implements ChessAction<HashMap<String, Position>,List<Position>,List<Position>,AgamePiece,Position> {

	private HashMap<String, Position> positions; // All the reachable positions
	private AgamePiece chessPiece;
	private List<Position> availablePositions;
	private List<Position> positionRemoved;
	private Position preferredPosition = null; // Each action has a preferred position that the piece should move to
	private Position strikePosition = null; // This position is set if it is occupied by an opponent piece
	private boolean strike = false; // This flag is set by the actionprocessor
	private APlayer player;
	private ApieceMove possibleMove;
	private int pn = 0;
	private int pny = 0;
	private PreferredMoveProcessor myProcessor;

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
		myProcessor = pr;
		possibleMove = ChessFunctions.processChessgame(this,chessPiece, pr); // The processor can be replaced by a lambda expression?
		if (possibleMove != null)
			preferredPosition = possibleMove.getToPosition();
//		preferredPosition = player.calculatePreferredPosition(chessPiece,this);      
		player.getHeldPositions().add(pr.getHeldPosition()); // This is the position held by the piece under consideration
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
	 * This method is called when the ChessAction is created.
	 * A ChessAction is created by the call to the ChesState getActions() method
	 * @return
	 */
	public List<Position> getActions(){
		if (availablePositions != null) {
			availablePositions.clear();
			availablePositions = null;
		}
		if (positionRemoved != null) {
			positionRemoved.clear();
			positionRemoved = null;
		}
		availablePositions = new ArrayList(positions.values());
		positionRemoved = new ArrayList();
		List<AgamePiece> pieces = player.getMygamePieces(); 
		for (Position position:availablePositions) {
			for (AgamePiece otherPiece:pieces) {
				boolean inuse = otherPiece.getMyPiece().isUse();// inuse is false if a piece is removed permanently olj 1.08.20
				if (inuse && otherPiece.isActive() && otherPiece != chessPiece) { // Added 31.07.20 Check if piece is active
					Position pos = otherPiece.getMyPosition();
					if (pos != null) {
						if (pos.isInUse()) { // OBS: Added 14.05.20 Are never active !! ??
							if (otherPiece.getMyPosition().getPositionName().equals(position.getPositionName())) {
								positionRemoved.add(position);
							}
						}else {
							 System.out.println("??????? piece has position that is not in use ?????????????? "+otherPiece.toString()+"\n Posisjon: "+pos.toString()+"\n"+this.toString());
						}
					}
				}
			}

		
		}
		return availablePositions;
		
	}
	/**
	 * getActions
	 * This method returns all possible position reachable by the piece belonging to this action
	 * If a position is occupied by another piece for this action's player, this position is placed in the removed list
	 * This method is called from the ChessState mark method, when a move has been made during the search process.
	 * 
	 * This is only done if the chosen player is the same as the action's player
	 * @return
	 */
	public List<Position> getActions(APlayer theplayer){

		List<AgamePiece> pieces = theplayer.getMygamePieces(); 
		if (theplayer == player) {
			if (availablePositions != null) {
				availablePositions.clear();
				availablePositions = null;
			}
			if (positionRemoved != null) {
				positionRemoved.clear();
				positionRemoved = null;
			}
			availablePositions = new ArrayList(positions.values());
			positionRemoved = new ArrayList();
			for (Position position:availablePositions) {
				for (AgamePiece otherPiece:pieces) {
					if (otherPiece != chessPiece) {
						Position pos = otherPiece.getMyPosition();
						if (pos != null) {
							if (otherPiece.getMyPosition().getPositionName().equals(position.getPositionName())) {
								positionRemoved.add(position);
							}

						}
					}
				}

			
			}
		}

		return availablePositions;
		
	}
	public String toString() {
		String posName = "Unknown";
		String pMove = " === No move ===";
		if (possibleMove != null)
			pMove = possibleMove.toString();
		if (preferredPosition != null)
			posName = preferredPosition.getPositionName();
		StringBuffer logText = new StringBuffer("ChessAction: Preferred Position " + posName+ " Piece " + chessPiece.toString()+" Possible move "+pMove);
		return logText.toString();
	}
}
