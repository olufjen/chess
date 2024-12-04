package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import no.function.FunctionContect;

/**
 * ThePeas
 * This class represent the Performance measure, Environment, Actuators, Sensors. 
 * 
 * @author oluf
 *
 */
public class ThePeas {
	private String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\peas.txt";
	private APlayer player;
	private APlayer opponent;
	private int movNr;
	private String moveKey;
	private HashMap<String,String[]> performance;
	private FunctionContect contex; // Used to register and run functions
	
	public ThePeas(APlayer player, APlayer opponent, int moveNr) {
		super();
		this.player = player;
		this.opponent = opponent;
		this.movNr = moveNr;
		moveKey = String.valueOf(moveNr);
		performance = new HashMap<String,String[]>();
		contex = new FunctionContect();
//		if (movNr == 0) {
			performance.put("d4",new String[] {null,null,"d4",null});
			performance.put("e4",new String[] {null,null,"e4",null});
			performance.put("c4",new String[] {null,null,"c4",null});
			performance.put("Nf3",new String[] {null,"WhiteKnight2","f3",null});
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
	public String[] selectPerformance() {
		if (movNr == 0) {
			return getPerformance().get("d4");
		}
		else
			return getPerformance().get("c4");

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
