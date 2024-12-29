package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Consumer;

import no.function.FunctionContect;

/**
 * ThePeas
 * This class represent the Performance measure, Environment, Actuators, Sensors. 
 * It is created by the PlannerState testEnd function.
 * @author oluf
 *
 */
public class ThePeas {
	private final static String[] notations = {"d4","e4","c4","Nf3","Nc3","e3","Bd3","Rf1","h3"}; // These are the first six moves. (to Bd3)
	// It represent the start strategy. They represent the chess algebraic notation.
	private String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\peas.txt";
	private APlayer player;
	private APlayer opponent;
	private int movNr; // The move numbers are 0, 2, 4, etc
	private String moveKey;
	private HashMap<String,String[]> performance;// The String array is of the form: Startpos, Piecename, Newpos, Piecetype
	private FunctionContect contex; // Used to register and run functions
	private Consumer<HashMap<String,String[]>> c; // This function adds a new entry to the Map performance. 
													//See functional interface definitions page 53 Java 8
	private Function<String,String[]> f; // This function picks a chosen entry from the Map performance
	public ThePeas(APlayer player, APlayer opponent, int moveNr) {
		super();
		this.player = player;
		this.opponent = opponent;
		this.movNr = moveNr;
		moveKey = String.valueOf(moveNr);
		performance = new HashMap<String,String[]>();
		contex = new FunctionContect();
		c = (HashMap<String,String[]> m) -> getPerformance().put(moveKey, new String[] {null,null,"d4",null});
		f = (String s) -> getPerformance().get(notations[movNr]);
//		if (movNr < 5) {
			performance.put(notations[0],new String[] {null,null,"d4",null});
			performance.put(notations[1],new String[] {null,null,"e4",null});
			performance.put(notations[2],new String[] {null,null,"c4",null});
			performance.put(notations[3],new String[] {null,"WhiteKnight2","f3",null});
			performance.put(notations[4],new String[] {null,"WhiteKnight1","c3",null});
			performance.put(notations[5],new String[] {null,null,"e3",null});
			performance.put(notations[6],new String[] {null,"WhiteBishop2","d3",null});
			performance.put(notations[8],new String[] {null,null,"h3",null});
			performance.put(notations[7],new String[] {null,"WhiteRook2","f1",null});
//			performance.put(notations[8],new String[] {null,"WhiteBishop2","d3",null});
//		}

	}
	public void checkMoves(APlayer theplayer) {
		ArrayList<AgamePiece> pieces = theplayer.getMygamePieces();
		if (pieces != null && !pieces.isEmpty()) {
			for (AgamePiece piece : pieces ) {
				List<ApieceMove> moves = piece.getMyMoves();
			}
		}

	}
	public String[] selectPerformance(String key) {

		return getPerformance().get(key);

	}
	
	public Consumer<HashMap<String, String[]>> getC() {
		return c;
	}
	public void setC(Consumer<HashMap<String, String[]>> c) {
		this.c = c;
	}
	public Function<String, String[]> getF() {
		return f;
	}
	public void setF(Function<String, String[]> f) {
		this.f = f;
	}
	public APlayer getPlayer() {
		return player;
	}

	public void setPlayer(APlayer player) {
		this.player = player;
	}

	public APlayer getOpponent() {
		return opponent;
	}

	public void setOpponent(APlayer opponent) {
		this.opponent = opponent;
	}

	public int getMovNr() {
		return movNr;
	}

	public void setMovNr(int movNr) {
		this.movNr = movNr;
	}


	public HashMap<String, String[]> getPerformance() {
		return performance;
	}


	public void setPerformance(HashMap<String, String[]> performance) {
		this.performance = performance;
	}
	
}
