package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import no.chess.ontology.Piece;
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
 * The player contains a list of its own pieces and their rank.
 * @author oluf
 *
 * @param <P>
 */
public class APlayer extends AbstractPlayer<AgamePiece,ApieceMove> implements ChessPlayer<AgamePiece,ApieceMove>{

	private HashMap<String,AgamePiece> myPieces;
	private HashMap<String,ApieceMove> myMoves;
	private ApieceMove currentMove;
	private ArrayList<AgamePiece> mygamePieces;
	private ArrayList<Piece> myontologyPieces;
	private List<String> pieceNames; // The names of my ontology pieces
	private List<Integer> pieceValues; // The rank of my ontology pieces
	private HashMap<String,Integer>namesAndrank;
	private player playerName; // Tells if player is white or black
	private String nameOfplayer = null;
	private boolean active = false;
	private List<ChessAction> actions; //Actions available to this player
	private AgamePiece preferredPiece;
	private List<Position> preferredPositions;
	private List<Position> heldPositions; // The list of positions held by the pieces belonging to the player
	
	
	public APlayer(ArrayList<AgamePiece> mygamePieces) {
		super();
		this.mygamePieces = mygamePieces;
		heldPositions = new ArrayList<Position>();
		myMoves = new HashMap<String,ApieceMove>();
		pieceNames = new ArrayList<String>();
		pieceValues = new ArrayList<Integer>();
		myontologyPieces = new ArrayList<Piece>();
		namesAndrank = new HashMap();
	}

	public APlayer() {
		super();
		mygamePieces = new ArrayList<AgamePiece>();
		heldPositions = new ArrayList<Position>();
		myMoves = new HashMap<String,ApieceMove>();
		pieceNames = new ArrayList<String>();
		pieceValues = new ArrayList<Integer>();
		myontologyPieces = new ArrayList<Piece>();
		namesAndrank = new HashMap();
	}
	public APlayer(player playerName) {
		super();
		this.playerName = playerName;
		mygamePieces = new ArrayList<AgamePiece>();
		heldPositions = new ArrayList<Position>();
		myMoves = new HashMap<String,ApieceMove>();
		pieceNames = new ArrayList<String>();
		pieceValues = new ArrayList<Integer>();
		myontologyPieces = new ArrayList<Piece>();
		namesAndrank = new HashMap();
	}
	
	public String getNameOfplayer() {
		return nameOfplayer;
	}

	public void setNameOfplayer(String nameOfplayer) {
		this.nameOfplayer = nameOfplayer;
	}

	public ArrayList<Piece> getMyontologyPieces() {
		return myontologyPieces;
	}

	public void setMyontologyPieces(ArrayList<Piece> myontologyPieces) {
		this.myontologyPieces = myontologyPieces;
	}

	public List<String> getPieceNames() {
		return pieceNames;
	}

	public void setPieceNames(List<String> pieceNames) {
		this.pieceNames = pieceNames;
	}

	public List<Integer> getPieceValues() {
		return pieceValues;
	}

	public void setPieceValues(List<Integer> pieceValues) {
		this.pieceValues = pieceValues;
	}


	public void emptyPositions() {
		heldPositions.clear();
	}
	public List<ChessAction> getActions() {
		return actions;
	}
	/**
	 * collectOntlogyPieces()
	 * This method collects all ontology pieces belonging to the player
	 */
	public void collectOntlogyPieces() {
		if (playerName == getWhitePlayer()) {
			for (AgamePiece piece:mygamePieces ) {
				Piece ontPiece = piece.getMyPiece().getWhitePiece();
				myontologyPieces.add(ontPiece);
			}
		}
		if (playerName == getBlackPlayer()) {
			for (AgamePiece piece:mygamePieces ) {
				Piece ontPiece = piece.getMyPiece().getBlackPiece();
				myontologyPieces.add(ontPiece);
			}
		}
		producePrioritylist();
	}
	/**
	 * producePrioritylist
	 * This method produces a list containing piece names and their priority for movement
	 * This method is called at the start of the game to collect all ontology pieces and their rank.
	 * 
	 */
	public void producePrioritylist() {
		List<OWLNamedIndividual> ontNames =	myontologyPieces.stream().map(Piece::getOwlIndividual).collect(Collectors.toList());
		List<IRI>iris = ontNames.stream().map(OWLNamedIndividual::getIRI).collect(Collectors.toList());
		pieceNames = iris.stream().map(IRI::toString).collect(Collectors.toList());
//		List<List<Integer>>valueList = new ArrayList<List<Integer>>();
		List<Integer>valueList = new ArrayList<Integer>();
		for (Piece ontPiece:myontologyPieces) {
			HashSet<Integer> values =  (HashSet) ontPiece.getHasValue();
			pieceValues.addAll(values);
//			List<List<Integer>>tempList = new ArrayList<List<Integer>>(((Map<String, AgamePiece>) values).values());
//			tempList.forEach(valueList::addAll);
//			tempList.forEach(pieceValues::addAll);
		}
/*
 * This stream operation collects the ontology piecenames and their rank to a hashmap.		
 */
		namesAndrank = (HashMap<String,Integer>) IntStream.range(0,pieceNames.size()).boxed().collect(Collectors.toMap(i -> pieceNames.get(i), i -> pieceValues.get(i)));
/*		Map<Double, String> map = IntStream.range(0, list1.size())
	            .boxed()
	            .collect(Collectors.toMap(i -> list1.get(i), i -> list2.get(i)));*/
//		List<List<Integer>>valueList = (List<List<Integer>>)(List<?>) myontologyPieces.stream().map(Piece::getHasValue).collect(Collectors.toList());
//		pieceValues = valueList.stream().flatMap(List::stream).collect(Collectors.toList());
//		List<Integer>valuesimple
//		List<Integer>valueList = (List<Integer>)(List<?>) myontologyPieces.stream().map(Piece::getHasValue).collect(Collectors.toList());
//		valuesimple.forEach(pieceValues::addAll);
//		Stream<List<Integer>> streamArray = Stream.of(valueList);

//		pieceValues = streamArray.stream().flatMap(List::stream).collect(Collectors.toList());
//		streamArray.forEach(pieceValues::addAll);
/*		for (Piece ontPiece:myontologyPieces) {
			//ontPiece.
		}*/
	}
 /**
  * checkPreferredPosition
  * This method sets the action's preferred position to null, 
  * if it is the same as one of the the heldpositions of the player's pieces.
  * @since 17.08.20 This method is not in use.
 * @param action
 */
public void checkPreferredPosition(ChessAction action) {
	 for (Position heldPos:heldPositions) {
			Position prefPos = (Position) action.getPreferredPosition();
			if(prefPos == null)
				break;
			if (prefPos == heldPos) {
				action.setPreferredPosition(null);
				break;
			}
			

	}
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

	public List<Position> getHeldPositions() {
		return heldPositions;
	}

	public void setHeldPositions(List<Position> heldPositions) {
		this.heldPositions = heldPositions;
	}

	public AgamePiece getChosenPiece(String name) {
		AgamePiece cPiece =  (AgamePiece) mygamePieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(name)).findAny().orElse(null);
		return cPiece;
	}
	/**
	 * checkCastling
	 * This method checks if a castling has taken place
	 * If that is the case it returns the castle piece that is involved
	 * @param piece
	 * @param newPosition
	 * @return
	 */
	public AgamePiece checkCastling(AgamePiece piece,Position newPosition) {
		
		String posName = newPosition.getPositionName();
		String name = "";
		AgamePiece cPiece = null;
		HashMap<String,Position> castlePositions = piece.getCastlePositions();
		pieceType myType = piece.getMyType();
		int noofMoves = piece.getNofMoves();
		if (playerName == player.BLACK) {
			if(posName.equals("c8") && castlePositions != null && !castlePositions.isEmpty() && myType == myType.KING && noofMoves == 0) {
				name = "BlackRook1";


			}
			if(posName.equals("g8") && castlePositions != null && !castlePositions.isEmpty() && myType == myType.KING && noofMoves == 0) {
				name = "BlackRook2";
			}
			String cName = name;
			if (!cName.equals(""))
				cPiece =  (AgamePiece) mygamePieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(cName)).findAny().orElse(null);
			if (cPiece != null)
				return cPiece;
			else
				return null;
		}
		if (playerName == player.WHITE) {
			if(posName.equals("c1") && castlePositions != null && !castlePositions.isEmpty() && myType == myType.KING && noofMoves == 0) {
				name = "WhiteRook1";
			}
			if(posName.equals("g1") && castlePositions != null && !castlePositions.isEmpty() && myType == myType.KING && noofMoves == 0) {
				name = "WhiteRook2";
			}
			String cName = name;
			if (!cName.equals(""))
				cPiece =  (AgamePiece) mygamePieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(cName)).findAny().orElse(null);
			if (cPiece != null)
				return cPiece;
			else
				return null;			
		}

		return null;
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

	/**
	 * calculateOpponentActions
	 * This method calculates the players actions
	 * It is used for the opponent player.
	 * This method replaces the calculateOpponentPositions method
	 * @param state
	 */
	public void calculateOpponentActions(ChessStateImpl state) {
		actions = state.getActions(this);
	}
	/**
	 * calculateOpponentPositions
	 * This method calculates all removed positions for opponent
	 * @deprecated 31.05.21
	 */
	public void calculateOpponentPositions() {
		for (AgamePiece piece:mygamePieces ) {
			String name = piece.getMyPiece().getPieceName();
			int pn = piece.getMyPosition().getIntRow();
			Integer prn = new Integer(pn);
			OpponentMoveProcessor op = new OpponentMoveProcessor(prn,name);
			ApieceMove move = ChessFunctions.processChessgame(this, piece,op);
			currentMove = move;
		}
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
		if (playerName == whitePlayer) {
			setWhitePlayer(playerName);
			nameOfplayer = "WhitePlayer";
		}
		if (playerName == blackPlayer) {
			setBlackPlayer(playerName);
			nameOfplayer = "BlackPlayer";
		}
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	@Override
	public boolean equals(Object arg0) {
		// TODO Auto-generated method stub
		return super.equals(arg0);
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public String toString() {
		
		return playerName+" "+playerId;
	}

	
}
