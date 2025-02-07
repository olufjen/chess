package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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
	// It represent a start strategy. They represent the chess algebraic notation.
	private String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\peas.txt";
	private PrintWriter writer =  null;
	private FileWriter fw =  null;
	private APlayer player;
	private APlayer opponent;
	private int movNr; // The move numbers are 0, 2, 4, etc
	private String moveKey;
	private HashMap<String,String[]> performance;// The String array is of the form: Startpos, Piecename, Newpos, Piecetype
	private HashMap<String,Integer> rankValues; // The value or rank of the performance/executable entries The one with the lowest rank is executed first
	private HashMap<String,String[]> executable; // A set of parameters for lifted action schemas
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
		executable = new HashMap<String,String[]>();
		rankValues = new HashMap<String,Integer>();
		contex = new FunctionContect();
		boolean flag = moveNr/2 < 8; // Signals the first 5 opening moves
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));
//		c = (HashMap<String,String[]> m) -> getPerformance().put(String xkey,String[] {);
//		f = (String s) -> getPerformance().get(notations[movNr]);
		if (flag) {
/*			for (int i = 0;i<9;i++) {
				rankValues.put(notations[i],new Integer(i));
			}*/
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
		
		}

	}
	/**
	 * addEntries
	 * This method adds parameter entries to the parameter map and gives it a rank
	 * @param key The key to the map
	 * @param rank The rank of the map entry
	 * @param names parameters for the entry
	 */
	public void addEntries(String key,int rank,String... names) {
		performance.put(key, names);
//		rankValues.put(key,new Integer(rank));
	}
	/**
	 * addExecutable
	 * This method adds parameter entries to the executable map and gives it a rank
	 * @param key The key to the map
	 * @param rank The rank of the map entry
	 * @param names parameters for the entry
	 */
	public void addExecutable(String key,int rank,String... names) {
		executable.put(key, names);
		rankValues.put(key,new Integer(rank));
	}
	public void checkMoves(APlayer theplayer) {
		ArrayList<AgamePiece> pieces = theplayer.getMygamePieces();
		if (pieces != null && !pieces.isEmpty()) {
			for (AgamePiece piece : pieces ) {
				List<ApieceMove> moves = piece.getMyMoves();
			}
		}

	}
	/**
	 * selectExecutable
	 * This method returns an executable set with the lowest rank (highest priority)
	 * @return a String array (executable set)
	 */
	public String[] selectExecutable() {
		boolean keyflag = (executable == null || executable.isEmpty());
		if (keyflag) {
			writer.println("No Sorted map ");
			writer.flush();
			return null;
		}
		Map<String,Integer> sortedRanks = rankValues
		        .entrySet()
		        .stream()
		        .sorted(Map.Entry.comparingByValue())
		        .collect(
		        		Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2,
		                LinkedHashMap::new));
//		List<Integer> listranks = new ArrayList<Integer>(rankValues.values());
//		List<Integer> result = (List<Integer>) listranks.stream().sorted((o1, o2)->o1.compareTo(o2));
	    Set< String> names = executable.keySet();
//	    List execValues = new ArrayList(executable.values());
	    writer.println("Executable keys ");
	    for (String key:names) {
	    	writer.println(key);
	    }
//		writer.println("Executable map values"+execValues);
		writer.println("Unsorted map "+rankValues);
		writer.println("Sorted map "+sortedRanks);
		Set<String> keys = sortedRanks.keySet();
		String firstKey = null;
	    for (String key:keys) {
	    	writer.println("The sorted key"+key);
	    	firstKey = key;
	    	break;
	    }
	    String[] returnValues = executable.get(firstKey);
		writer.flush();
		return returnValues;
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
