package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import aima.core.logic.fol.domain.FOLDomain;
import no.chess.web.model.Position;
import no.games.chess.fol.FOLGamesBCAsk;
import no.games.chess.fol.FOLGamesFCAsk;

/**
 * APerformance
 * This class is responsible for calculating the performance measure.
 * It is created by the opponent agent when the opponent agent is created
 * The questions to answer are:
 * How many of my pieces can reach/protect a position? 
 * What rank do these pieces have?
 * How many of opponent pieces threaten this position?
 * What rank do these pieces have?
 * What makes a position valuable? So it becomes important to control?
 * What material gain do I achieve by controlling this position?
 * What strategic advantage do I have from controlling this position?
 * I have a control over a position if I have more pieces reaching it than my opponent.
 * So:
 * I have a map of positions occupied by my opponent
 * From the strategy rule base I know which positions these pieces can reach (OPPONENTTO)
 * From the strategy rule base I know which of my pieces can reach these positions one ply down 
 * @author oluf
 *
 */
public class APerformance {

	private List<Position> controlPositions;
	private HashMap<String,Position> positions; // The original HashMap of positions
	private Map<String,Position> occupiedPositions; //Positions occupied by the opponent pieces. The key is the name of the piece
	private APlayer myPlayer = null; // The player of the game
	private APlayer opponent = null; // The opponent of the game
	private ChessFolKnowledgeBase folKb = null; // The parent knowledge base
	private ChessFolKnowledgeBase localKb; // The strategy knowledge base
	private FOLDomain chessDomain;
	private FOLGamesFCAsk forwardChain;
	private FOLGamesBCAsk backwardChain;
	private List<String>positionKeys = null;// The key for positions that are reachable. The key is of the form: piecename_frompostopos
	private Map<String,ArrayList<String>> reachablePieces; //contains piecenames of the form WhiteQueen_c4, with a position name as a key
	private Map<String,String> reachableOpponent; //contains piece names of the form BackPawn5, with a position name as a key
	private OpponentAgent agent;
	private String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\performance.txt";
	private PrintWriter writer = null;
	private FileWriter fw = null;
	
	public APerformance(HashMap<String, Position> positions, APlayer opponent, APlayer myPlayer,ChessFolKnowledgeBase folKb, ChessFolKnowledgeBase localKb, FOLDomain chessDomain,
			FOLGamesFCAsk forwardChain, FOLGamesBCAsk backwardChain) {
		super();
		this.positions = positions;
		this.myPlayer = myPlayer;
		this.opponent = opponent;
		this.folKb = folKb;
		this.localKb = localKb;
		this.chessDomain = chessDomain;
		this.forwardChain = forwardChain;
		this.backwardChain = backwardChain;
		occupiedPositions = new HashMap();
		reachablePieces = new HashMap();
		reachableOpponent = new HashMap();
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
	}
	
	public OpponentAgent getAgent() {
		return agent;
	}

	public void setAgent(OpponentAgent agent) {
		this.agent = agent;
	}

	public List<String> getPositionKeys() {
		return positionKeys;
	}

	public void setPositionKeys(List<String> positionKeys) {
		this.positionKeys = positionKeys;
	}

	public List<Position> getControlPositions() {
		return controlPositions;
	}
	public void setControlPositions(List<Position> controlPositions) {
		this.controlPositions = controlPositions;
	}
	public HashMap<String, Position> getPositions() {
		return positions;
	}
	public void setPositions(HashMap<String, Position> positions) {
		this.positions = positions;
	}
	public APlayer getMyPlayer() {
		return myPlayer;
	}
	public void setMyPlayer(APlayer myPlayer) {
		this.myPlayer = myPlayer;
	}
	public APlayer getOpponent() {
		return opponent;
	}
	public void setOpponent(APlayer opponent) {
		this.opponent = opponent;
	}
	public ChessFolKnowledgeBase getFolKb() {
		return folKb;
	}
	public void setFolKb(ChessFolKnowledgeBase folKb) {
		this.folKb = folKb;
	}
	public ChessFolKnowledgeBase getLocalKb() {
		return localKb;
	}
	public void setLocalKb(ChessFolKnowledgeBase localKb) {
		this.localKb = localKb;
	}
	public FOLDomain getChessDomain() {
		return chessDomain;
	}
	public void setChessDomain(FOLDomain chessDomain) {
		this.chessDomain = chessDomain;
	}
	public FOLGamesFCAsk getForwardChain() {
		return forwardChain;
	}
	public void setForwardChain(FOLGamesFCAsk forwardChain) {
		this.forwardChain = forwardChain;
	}
	public FOLGamesBCAsk getBackwardChain() {
		return backwardChain;
	}
	public void setBackwardChain(FOLGamesBCAsk backwardChain) {
		this.backwardChain = backwardChain;
	}
	
	/**
	 * occupiedPositions
	 * This method creates a map of positions occupied by the opponent's pieces
	 * The key is the name of the piece
	 */
	public void occupiedPositions() {
//		List<Position> allPositions = (List<Position>) positions.values();
		List<AgamePiece> pieces = opponent.getMygamePieces();
		for (AgamePiece piece:pieces) {
			if (piece.isActive()) {
				String name = piece.getMyPiece().getOntlogyName();
				int rank = piece.getValue();
				Position pos = piece.getmyPosition();
				if (pos == null)
					pos = piece.getHeldPosition();
				occupiedPositions.put(name, pos);
			}
		}
	}
	public void findReachable() {
//		List<Position> occupied = (List<Position>) occupiedPositions.values();
		String foundKey = null;
		List<String> termTotals = new ArrayList<String>();
		for (String key:positionKeys) { //Reachable positions: piecename_frompostopos
			int l = key.length();
			int index = l-2;
			String toPosname = key.substring(index);
			Position toPos = positions.get(toPosname); // The map of all positions
			String fromposName = key.substring(l-4, l-2);
			String pieceName = key.substring(0,l-5);
			writer.println("Checking position key: "+key+" Piece "+pieceName+" From position "+fromposName);
			if (toPos != null) {
				if (occupiedPositions.containsValue(toPos)) {
					Position entryPos = null;
					writer.println("An occupied position: "+toPos.toString());
					for (Map.Entry<String,Position> entry:occupiedPositions.entrySet()) {
//						writer.println("Key of entry set: "+entry.getKey()+ " value of entry set: "+entry.getValue().toString());
						entryPos = entry.getValue();
						if (entryPos == toPos) {
							foundKey = entry.getKey();
							writer.println("A found key "+foundKey); // The key is the name of the piece which occupies this position and it is an opponent piece
							break;
						}
					}
					if (foundKey != null) {
						writer.println("Must find which piece can reach this position "+entryPos.toString()+"\n");
						String posName = entryPos.getPositionName();
/*						if (posName.equals("d5")) {
							writer.println("Checking for this position "+posName);
						}*/
						String reachable = agent.getREACHABLE();
						ArrayList<String>termNames = (ArrayList<String>) localKb.searchFacts("x", posName, reachable);
						ArrayList<String>fromtermNames = (ArrayList<String>) localKb.searchFacts("x", fromposName, reachable);
						reachablePieces.put(posName, termNames); // posName is the name of the position of the opponent piece
						reachableOpponent.put(posName, foundKey); // foundKey is the name of the opponent piece
						String threaten = agent.getTHREATEN();
						String pawnAttack = agent.getPAWNATTACK();
						ArrayList<String>threatNames = (ArrayList<String>) folKb.searchFacts("x", posName, threaten);
						ArrayList<String>reachparentNames = (ArrayList<String>) folKb.searchFacts("x", posName, reachable);
						ArrayList<String>pawnThreats = (ArrayList<String>) folKb.searchFacts("x", posName, pawnAttack);
						ArrayList<String>fromthreatNames = (ArrayList<String>) folKb.searchFacts("x", fromposName, threaten);
						ArrayList<String>fromreachparentNames = (ArrayList<String>) folKb.searchFacts("x", fromposName, reachable);
						ArrayList<String>frompawnThreats = (ArrayList<String>) folKb.searchFacts("x", fromposName, pawnAttack);
						termTotals.addAll(termNames);
						int no = termNames.size();
						int tn = threatNames.size();
						int tr = reachparentNames.size();
						int pn = pawnThreats.size();
						int fno = fromtermNames.size();
						int ftn = fromthreatNames.size();
						int ftr = fromreachparentNames.size();
						int fpn = frompawnThreats.size();
						writer.println("Reachable from strategy knowledge base");
						for (int i = 0;i<no;i++) {
							writer.println(termNames.get(i));
						}
						writer.println("Threats from parent knowledge base");
						for (int i = 0;i<tn;i++) {
							writer.println(threatNames.get(i));
						}
						writer.println("Reachable from parent knowledge base");
						for (int i = 0;i<tr;i++) {
							writer.println(reachparentNames.get(i));
						}
						writer.println("Pawn attack from parent knowledge base");
						for (int i = 0;i<pn;i++) {
							writer.println(pawnThreats.get(i));
						}
						writer.println("The from position "+fromposName+" Reachable from strategy knowledge base");
						for (int i = 0;i<fno;i++) {
							writer.println(fromtermNames.get(i));
						}
						writer.println("The from position Threats from parent knowledge base");
						for (int i = 0;i<ftn;i++) {
							writer.println(fromthreatNames.get(i));
						}
						writer.println("The from position Reachable from parent knowledge base");
						for (int i = 0;i<ftr;i++) {
							writer.println(fromreachparentNames.get(i));
						}
						writer.println("The from position Pawn attack from parent knowledge base");
						for (int i = 0;i<fpn;i++) {
							writer.println(frompawnThreats.get(i));
						}
					}
				}
			}
		}
		List<AgamePiece> pieces = myPlayer.getMygamePieces();
		for (AgamePiece piece:pieces) {
			if (piece.isActive()) {
				
			}
		}
	    writer.flush();
	}
	
}
