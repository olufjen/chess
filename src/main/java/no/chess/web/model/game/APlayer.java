package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import no.chess.web.model.Position;
import no.games.chess.AbstractGamePiece;
import no.games.chess.AbstractGamePiece.pieceType;
import no.games.chess.AbstractPlayer;
import no.games.chess.ChessAction;
import no.games.chess.ChessFunctions;
import no.games.chess.ChessPlayer;
import no.games.chess.ChessState;

/**
 * APlayer represent the implementation of a Chess player
 * It is the player who possesses the white or black chess pieces
 * depending on the playerName
 * The player is responsible for calculating the preferred position of a given piece.
 * @author oluf
 *
 * @param <P>
 */
public class APlayer extends AbstractPlayer<AgamePiece,ApieceMove> implements ChessPlayer<AgamePiece,ApieceMove>{

	private HashMap<String,AgamePiece> myPieces;
	private HashMap<String,ApieceMove> myMoves;
	private ApieceMove currentMove;
	private ArrayList<AgamePiece> mygamePieces;
	private player playerName; // Tells if player is white or black
	private boolean active = false;
	private List<ChessAction> actions; //Actions available to this player
	private AgamePiece preferredPiece;
	private List<Position> preferredPositions;
	
	public APlayer(ArrayList<AgamePiece> mygamePieces) {
		super();
		this.mygamePieces = mygamePieces;
	}

	public APlayer() {
		super();
		mygamePieces = new ArrayList<AgamePiece>();
		
	}

	public APlayer(player playerName) {
		super();
		this.playerName = playerName;
		mygamePieces = new ArrayList<AgamePiece>();
	}

	public List<ChessAction> getActions() {
		return actions;
	}

	public void setActions(List<ChessAction> actions) {
		this.actions = actions;
	}

	@Override
	public HashMap<String, AgamePiece> getPieces() {
		
		return (HashMap<String, AgamePiece>) myPieces;
	}

	@Override
	public void collectmyPieces() {
		
		
	}

	/**
	 * calculatePreferredPosition
	 * This method is called when the chessAction is requested to return a preferred position.
	 * If the action does not contain a preferred position then this method is called.
	 * This method calculates a preferred position for a given piece.
	 * The questions asked are:
	 * Which rank has this piece?
	 * which pieces are available to be moved after I move this piece?
	 * Which pieces from opponent can I capture when I move this piece?
	 * @param piece
	 * @return
	 */
	public Position calculatePreferredPosition(AgamePiece piece, ChessActionImpl action) {
		String name = piece.getMyPiece().getPieceName();
		int pn = piece.getMyPosition().getIntRow();
		Integer prn = new Integer(pn);
		PreferredMoveProcessor pr = new PreferredMoveProcessor(prn,name);
		ApieceMove move = ChessFunctions.processChessgame(action,piece,pr); // The processor can be replaced by a lambda expression
		Position preferredPosition = null;
		if (move != null) {
			preferredPosition = move.getToPosition();
		}
		return preferredPosition;
	}

	public HashMap<String, AgamePiece> getMyPieces() {
		return myPieces;
	}

	public void setMyPieces(HashMap<String, AgamePiece> myPieces) {
		this.myPieces = myPieces;
	}

	public HashMap<String, ApieceMove> getMyMoves() {
		return myMoves;
	}

	public void setMyMoves(HashMap<String, ApieceMove> myMoves) {
		this.myMoves = myMoves;
	}

	public ApieceMove getCurrentMove() {
		return currentMove;
	}

	public void setCurrentMove(ApieceMove currentMove) {
		this.currentMove = currentMove;
	}

	public ArrayList<AgamePiece> getMygamePieces() {
		return mygamePieces;
	}

	public void setMygamePieces(ArrayList<AgamePiece> mygamePieces) {
		this.mygamePieces = mygamePieces;
	}

	public player getPlayerName() {
		return playerName;
	}

	public void setPlayerName(player playerName) {
		this.playerName = playerName;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	
}
