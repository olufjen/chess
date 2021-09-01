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
 * The questions to answer are:
 * How many of my pieces can reach/protect a position? 
 * What rank do these pieces have?
 * How many of opponent pieces threaten this position?
 * What rank do these pieces have?
 * What makes a position valuable? So the it becomes important to control?
 * What material gain do I achieve by controlling this position?
 * What strategic advantage do I have from controlling this position?
 * I have a control over a position if I have more pieces reaching it that my opponent.
 * @author oluf
 *
 */
public class APerformance {

	private List<Position> controlPositions;
	private HashMap<String,Position> positions; // The original HashMap of positions
	private Map<String,Position> occupiedPositions;
	private APlayer myPlayer = null;
	private APlayer opponent = null;
	private ChessFolKnowledgeBase folKb = null; // The parent knowledge base
	private ChessFolKnowledgeBase localKb; // The strategy knowledge base
	private FOLDomain chessDomain;
	private FOLGamesFCAsk forwardChain;
	private FOLGamesBCAsk backwardChain;
	private List<String>positionKeys = null;
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
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
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
		for (String key:positionKeys) {
			int l = key.length();
			int index = l-2;
			String toPosname = key.substring(index);
			Position toPos = positions.get(toPosname); // The map of all positions
			if (toPos != null) {
				if (occupiedPositions.containsValue(toPos)) {
					writer.println("An occupied position: "+toPos.toString());
					for (Map.Entry<String,Position> entry:occupiedPositions.entrySet()) {
						writer.println("Key of entry set: "+entry.getKey()+ " value of entry set: "+entry.getValue().toString());
						Position entryPos = entry.getValue();
						if (entryPos == toPos) {
							foundKey = entry.getKey();
							break;
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
	}
	
}
