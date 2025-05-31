package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import aima.core.logic.propositional.parsing.ast.Sentence;
import no.chess.web.model.Position;
import no.games.chess.ChessAction;
import no.games.chess.ChessFunctions;
import no.games.chess.ChessPieceType;
import no.games.chess.AbstractGamePiece.pieceType;


/**
 * ChessActionImpl
 * This class implements the ChessAction interface.
 * A ChessAction is created by the call to the ChessState getActions() method
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
	private AgamePiece chessPiece; //  The chesspiece involved in this action
	private ChessPieceType pieceType;
	private pieceType type;
	/*
	 * These four lists are recreated when the action is created and when the 
	 * action's getAction method is called
	 */
	private List<Position> availablePositions;
	private List<Position> positionRemoved; // When the ChessAction is created the getAction method and the preferredMove processor
	// fill this table with all removed positions.
	private List<Position> opponentRemoved; // These are the positions blocked by opponent pieces.
	private List<Position> bishopRemoved; // This list contains removed positions for the queen in bishop movements
	private Position preferredPosition = null; // Each action has a preferred position that the piece should move to
	private Position strikePosition = null; // This position is set if it is occupied by an opponent piece
	private boolean strike = false; // This flag is set by the actionprocessor
	private APlayer player; // The player for this action
	private APlayer opponent; // The opponent player
	
	private ApieceMove possibleMove;
	private int pn = 0;
	private int pny = 0;
	private PreferredMoveProcessor myProcessor;
//	private Sentence sentence = null; // Contains a possible action sentence contained in the chess knowledge base
	// This is for propositional logic and not used olj 13.2.23
	private boolean blocked = false;
	private Integer actionValue = null; // The action value for this action created from the chess agent and its knowledge bases
	private List<Position> attackedPositions = null; // These positions are set from the action processor
	private List<Position> notAttackedPos = null;
	private List<Position> notProtected = null;
	private List<Position> protectedPositions = null;
	private List<AgamePiece> attacked = null;
	private List<Position> otherattackedPositions = null;
	private List<Position> otherprotectedPositions  = null;
	private String actionName;
	private Double evaluationValue = null; // An evaluation value for the action. It is produced by the ActionProcessor
	private boolean moveFlag = false; // True when a move is possible
	public ChessActionImpl(HashMap<String, Position> positions, AgamePiece chessPiece,APlayer player, APlayer opponent) {
		super();
		this.positions = positions;
		this.chessPiece = chessPiece;
		this.player = player;
		this.opponent = opponent;
		pieceType = chessPiece.getChessType();
		type = chessPiece.getPieceType();
		this.availablePositions = getActions(); // The positionRemoved are also created and filled. They are positions occupied by other pieces owned by the player
		String name = this.chessPiece.getMyPiece().getPieceName();
		name = name + this.chessPiece.getColor();
		pn = this.chessPiece.getMyPosition().getIntRow()*10;
		pny = this.chessPiece.getMyPosition().getIntColumn();
		Integer prn = new Integer(pn+pny);
		PreferredMoveProcessor pr = new PreferredMoveProcessor(prn,name);
		myProcessor = pr;
		possibleMove = ChessFunctions.processChessgame(this,chessPiece, pr); // The processor can be replaced by a lambda expression?
		if (possibleMove != null) {
			preferredPosition = possibleMove.getToPosition();
			moveFlag = true;
		}
//		preferredPosition = player.calculatePreferredPosition(chessPiece,this);      
		player.getHeldPositions().add(pr.getHeldPosition()); // This is the position held by the piece under consideration
		actionName = this.chessPiece.getMyPiece().getOntlogyName();
	}


	public boolean isMoveFlag() {
		return moveFlag;
	}


	public void setMoveFlag(boolean moveFlag) {
		this.moveFlag = moveFlag;
	}


	public APlayer getOpponent() {
		return opponent;
	}


	public void setOpponent(APlayer opponent) {
		this.opponent = opponent;
	}


	public Double getEvaluationValue() {
		return evaluationValue;
	}


	public void setEvaluationValue(Double evaluationValue) {
		this.evaluationValue = evaluationValue;
	}


	public String getActionName() {
		return actionName;
	}


	public void setActionName(String actionName) {
		this.actionName = actionName;
	}


	public Integer getActionValue() {
		return actionValue;
	}


	public void setActionValue(Integer actionValue) {
		this.actionValue = actionValue;
	}


	/**
	 * processPositions
	 * This method recalculates removed positions for this action.
	 * @since 6.01.22
	 * When recalculating removed position, then the possible move must keep the original to position ???
	 * @deprecated
	 * 12.01.22 This method is no longer necessary
	 */
	public void processPositions() {
		Position movePos = null;
		if (possibleMove != null) {
			movePos = possibleMove.getToPosition();
		}
		ApieceMove newMove = ChessFunctions.processChessgame(this,chessPiece, myProcessor);
		if (possibleMove != null && newMove != null) {
			possibleMove = newMove;
			possibleMove.setToPosition(movePos);
		}
		if (newMove == null) {
			System.out.println("Action.processPosition new move is null "+possibleMove.toString());
		}
			
	}
	public List<Position> getAttackedPositions() {
		return attackedPositions;
	}


	public void setAttackedPositions(List<Position> attackedPositions) {
		this.attackedPositions = attackedPositions;
	}


	public List<Position> getNotAttackedPos() {
		return notAttackedPos;
	}


	public void setNotAttackedPos(List<Position> notAttackedPos) {
		this.notAttackedPos = notAttackedPos;
	}


	public List<Position> getNotProtected() {
		return notProtected;
	}


	public void setNotProtected(List<Position> notProtected) {
		this.notProtected = notProtected;
	}


	public List<Position> getProtectedPositions() {
		return protectedPositions;
	}


	public void setProtectedPositions(List<Position> protectedPositions) {
		this.protectedPositions = protectedPositions;
	}


	public List<Position> getBishopRemoved() {
		return bishopRemoved;
	}


	public void setBishopRemoved(List<Position> bishopRemoved) {
		this.bishopRemoved = bishopRemoved;
	}


	public List<AgamePiece> getAttacked() {
		return attacked;
	}


	public void setAttacked(List<AgamePiece> attacked) {
		this.attacked = attacked;
	}


	public List<Position> getOtherattackedPositions() {
		return otherattackedPositions;
	}


	public void setOtherattackedPositions(List<Position> otherattackedPositions) {
		this.otherattackedPositions = otherattackedPositions;
	}


	public List<Position> getOtherprotectedPositions() {
		return otherprotectedPositions;
	}


	public void setOtherprotectedPositions(List<Position> otherprotectedPositions) {
		this.otherprotectedPositions = otherprotectedPositions;
	}


	public boolean isBlocked() {
		return blocked;
	}


	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
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


	public List<Position> getOpponentRemoved() {
		return opponentRemoved;
	}


	public void setOpponentRemoved(List<Position> opponentRemoved) {
		this.opponentRemoved = opponentRemoved;
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
	 * @since 25.02.21 Adapted for castling
	 * @since 24.12.22 Added friendspositions for knights
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
		if (bishopRemoved != null) {
			bishopRemoved.clear();
			bishopRemoved = null;
		}
		if (opponentRemoved != null) {
			opponentRemoved.clear();
			opponentRemoved = null;
		}
		ChessPieceType pieceType = chessPiece.getChessType();
		availablePositions = new ArrayList(positions.values());
		if (pieceType instanceof AQueen) {
			List<Position> bishopPositions = new ArrayList(chessPiece.getBishopPositions().values());
			availablePositions.addAll(bishopPositions);
		}
		
		positionRemoved = new ArrayList();
		opponentRemoved = new ArrayList();
		bishopRemoved = new ArrayList();
		List<Position> castlePositions = null;
		Position castlePosition = null;
		
		if (pieceType instanceof Aking) {
			castlePositions = new ArrayList(chessPiece.getCastlePositions().values());
			castlePosition = castlePositions.get(0);
		}
		if (pieceType instanceof ARook) {
			castlePositions = new ArrayList(chessPiece.getCastlePositions().values());
			castlePosition = castlePositions.get(0);
		}
		boolean bKnight = false;
		if (pieceType instanceof AKnight) {
			bKnight = true;
		}
		List<AgamePiece> pieces = player.getMygamePieces(); 
		for (Position position:availablePositions) {
			for (AgamePiece otherPiece:pieces) {
				boolean inuse = otherPiece.getMyPiece().isUse();// inuse is false if a piece is removed permanently olj 1.08.20
				if (inuse && otherPiece.isActive() && otherPiece != chessPiece) { // Added 31.07.20 Check if piece is active
					Position pos = otherPiece.getMyPosition();
//					pos.setFriendlyPosition(false);
					if (pos != null) {
						if (pos.isInUse()) { // OBS: Added 14.05.20 Are never active !! ??
							if (otherPiece.getMyPosition().getPositionName().equals(position.getPositionName())) {
								String name = otherPiece.getMyPosition().getPositionName();
								String pName = otherPiece.getMyPiece().getOntlogyName();
/*								if (pieceType instanceof AQueen)
									System.out.println("!!!!!! piece and position "+pName+" "+name);*/
								Position posinTable =  (Position) positionRemoved.stream().filter(c -> c.getPositionName().contains(name)).findAny().orElse(null); // Do not put position in removed table if it is there already
								if (posinTable == null && !checkQueen(pos) && ! bKnight) { //The bKnight added nov. 21. All positions are available to the knight
									positionRemoved.add(position);
									chessPiece.determinFriendPosition(pos);
									//position.setFriendlyPosition(true);
								}
								if (posinTable == null && bKnight) { // If piece is a Knight
//									positionRemoved.add(position);
//									String posName = position.getPositionName();
									chessPiece.determinFriendPosition(pos);
								}
							}
							if (castlePosition != null) 
								checkCastling(pos, castlePositions);
						}else {
							 System.out.println("??????? piece has position that is not in use ?????????????? "+otherPiece.toString()+"\n Posisjon: "+pos.toString()+"\n"+this.toString());
						}

					}
				}
			}

		
		}

		checkOpponent();
		return availablePositions;
		
	}
	/**
	 * checkOpponent
	 * This method check for opponent pieces and their positions.
	 * If opponent pieces blocks movements for the active player,
	 * then these positions must be placed in the removed list
	 * @since 20.05.21 Opponent pieces that block movements for the active player
	 * must be put in a separate removed list.
	 * This is so because the opponent piece can be taken.
	 * 
	 */
	private void checkOpponent() {
		List<AgamePiece> pieces = opponent.getMygamePieces(); 
		for (Position position:availablePositions) {
			position.setOpponentRemove(false);
			for (AgamePiece otherPiece:pieces) {
				boolean inuse = otherPiece.getMyPiece().isUse();// inuse is false if a piece is removed permanently olj 1.08.20
				if (inuse) {
					Position pos = otherPiece.getMyPosition();
					if (otherPiece.getMyPosition().getPositionName().equals(position.getPositionName())) {
						if (type != type.KNIGHT) { // !checkQueen(pos) &&  the check for queen is removed !!! olj 06.10.21
							opponentRemoved.add(position);
							position.setOpponentRemove(true);
							String playerId = opponent.getPlayerId();
							if (type == type.BISHOP && playerId.equals("WHITE")) {
								System.out.println("=== Opponent removed === "+position.toString());
							}
						}
						if (type == type.PAWN) { //  If position is in the list of attackPositions then do not remove it !!!
							ArrayList<Position> attackPos = new ArrayList<Position>(chessPiece.getAttackPositions().values());
							String name = pos.getPositionName();
							Position posinTable =  (Position) attackPos.stream().filter(c -> c.getPositionName().contains(name)).findAny().orElse(null); // Do not put position in removed table if it is there already
							if (posinTable == null)
								positionRemoved.add(position);
						}
					}
				}
			}
		}
	}
	/**
	 * checkQueen
	 * This method checks for removed positions for the queen and its bishop movements
	 * @param pos
	 * @return
	 */
	private boolean checkQueen(Position pos) {
		boolean queen = false;
		if (pieceType instanceof AQueen) {
			List<Position>bishopPositions = new ArrayList(chessPiece.getBishopPositions().values());
			String name = pos.getPositionName();
/*			if (name.equals("c2") || name.equals("e2")) {
				System.out.println("Pos !!! "+name);
			}*/
			Position posinTable =  (Position)bishopPositions.stream().filter(c -> c.getPositionName().contains(name)).findAny().orElse(null);
			if (posinTable != null) { // If position is a bishopPosition remove it
				bishopRemoved.add(pos);
				chessPiece.determinFriendPosition(pos); // Only friendly positions
				queen = true;
			}
	
		}
		chessPiece.setBishopRemoved(bishopRemoved);
		return queen;
	}
	/**
	 * checkCastling
	 * This method moves the castling positions to the removed list if necessary
	 * @param otherPos
	 * @param castlePositions
	 */
	private void checkCastling(Position otherPos,List<Position> castlePositions) {
	
		for (Position castlePos:castlePositions) {
			if (otherPos.getPositionName().equals(castlePos.getPositionName())) {
				String name = otherPos.getPositionName();
				Position posinTable =  (Position) positionRemoved.stream().filter(c -> c.getPositionName().contains(name)).findAny().orElse(null); // Do not put position in removed table if it is there already
				if (posinTable == null) {
					positionRemoved.add(castlePos);
					chessPiece.determinFriendPosition(castlePos);
	//				System.out.println("Castle position: "+castlePos.toString());
				}
			}
		}
/*	
		for (Position removed:positionRemoved) {
			System.out.println(removed.toString());
		}*/

	}
	/**
	 * getActions
	 * This method returns all possible position reachable by the piece belonging to this action
	 * If a position is occupied by another piece for this action's player, this position is placed in the removed list
	 * This method is called from the ChessState mark method, when a move has been made during the search process. - At present: turned off OLJ 25.02.21
	 * It is also called from the ActionProcessor which is used to give the action an evaluation value
	 * @since 25.02.21 Adapted for castling
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
			
			List<Position> castlePositions = null;
			Position castlePosition = null;
			ChessPieceType pieceType = chessPiece.getChessType();
			if (pieceType instanceof Aking) {
				castlePositions = new ArrayList(chessPiece.getCastlePositions().values());
				castlePosition = castlePositions.get(0);
			}
			if (pieceType instanceof ARook) {
				castlePositions = new ArrayList(chessPiece.getCastlePositions().values());
				castlePosition = castlePositions.get(0);
			}
			
			for (Position position:availablePositions) {
				for (AgamePiece otherPiece:pieces) {
					if (otherPiece != chessPiece && otherPiece.isActive()) {
						Position pos = otherPiece.getMyPosition();
						if (pos != null) {
							if (otherPiece.getMyPosition().getPositionName().equals(position.getPositionName())) {
								positionRemoved.add(position);
							}
							if (castlePosition != null)
								checkCastling(pos, castlePositions);
						}
					}
				}

			
			}
		}

		return availablePositions;
		
	}
/*	public  List<Position> getOpponentPositions(){
		
		return null;
	}*/
	public String toString() {
		String posName = "Unknown";
		String pMove = " === No move ===";
		if (possibleMove != null)
			pMove = possibleMove.toString();
		if (preferredPosition != null)
			posName = preferredPosition.getPositionName();
		StringBuffer logText = new StringBuffer(actionName + " Preferred Position " + posName+ " Piece " + chessPiece.toString()+" Possible move "+pMove);
		return logText.toString();
	}
	/**
	 * Indicates whether or not this Action is a 'No Operation'.<br>
	 * Note: AIMA3e - NoOp, or no operation, is the name of an assembly language
	 * instruction that does nothing.
	 * 
	 * @return true if this is a NoOp Action.
	 */

	@Override
	public boolean isNoOp() {
		// TODO Auto-generated method stub
		return false;
	}
}
