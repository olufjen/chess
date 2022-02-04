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
import no.chess.web.model.ChessPiece;
import no.chess.web.model.Position;
import no.games.chess.ChessPieceType;
import no.games.chess.fol.FOLGamesBCAsk;
import no.games.chess.fol.FOLGamesFCAsk;

/**
 * APerformance
 * This class is responsible for calculating the performance measure.
 * It is used to create temporal information to the planning problem.
 * It represent the scheduling phase. (See chapter 11) :
 * Examples of temporal information:
 * The from position d5 investigated
 *	protectorPieces:
 *	Piece WhiteBishop2 is protector of d5 from c4
 *	Piece WhiteBishop2 is protector of d5 from e4
 *	Piece WhiteQueen is protector of d5 from b3
 *	needprotectionPieces:
 *	Piece WhiteKnight1 with value 3 must have a protection for d5
 *	These pieces threatens the piece (at the from position):
 *	BlackKnight2
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
 * From the parent KB I know which positions these pieces can reach (OPPONENTTO)
 * From the strategy KB I know which of my pieces can reach these positions one ply down 
 * @author oluf
 *
 */
public class APerformance {

	private List<Position> controlPositions; // The original set of positions as a list
	private HashMap<String,Position> positions; // The original HashMap of positions
	private Map<String,Position> occupiedPositions; //Positions occupied by the opponent pieces. The key is the name of the piece
	private Map<String,Position> takenPositions; //Positions occupied by the player's pieces. The key is the name of the piece
	private APlayer myPlayer = null; // The player of the game
	private APlayer opponent = null; // The opponent of the game
	private ChessFolKnowledgeBase folKb = null; // The parent knowledge base
	private ChessFolKnowledgeBase localKb; // The strategy knowledge base
	private FOLDomain chessDomain;
	private FOLGamesFCAsk forwardChain;
	private FOLGamesBCAsk backwardChain;
	private List<String>positionKeys = null;// The key for positions that are reachable. The key is of the form: piecename_frompostopos
	private Map<String,ArrayList<String>> reachablePieces; //to position reachable from strategy KB. Contains piecenames of the form WhiteQueen_c4, with a position name as a key
	private Map<String,ArrayList<String>> tothreatPieces; // to position threats from parent KB
	private Map<String,ArrayList<String>> toreachablePieces; // to position reachable from parent KB
	private Map<String,ArrayList<String>> topawnthreatPieces;// to position pawn threats from parent KB
	private Map<String,ArrayList<String>> fromreachablePieces; //From position reachable from strategy KB
	private Map<String,ArrayList<String>> fromthreatparentPieces; //From position threats from parent KB
	private Map<String,ArrayList<String>> fromreachparentPieces; //From position reachable from parent KB
	private Map<String,ArrayList<String>> frompawnparentPieces; //From position pawnthreats from parent KB
	private Map<String,ArrayList<String>> threatparentPieces; // position threats from parent KB contains positions occupied by player's pieces
	private Map<String,ArrayList<String>> reachparentPieces; // position reachable from parent KB contains positions occupied by player's pieces
	private Map<String,ArrayList<String>> pawnparentPieces; // position pawnthreats from parent KB contains positions occupied by player's pieces
	private Map<String,String> reachableOpponent; // A map of reachable opponent pieces. Contains piece names of the form BlackPawn5, with a position name like d5 as a key

	private Map<String,ArrayList<AgamePiece>>movablePieces; //Pieces that can be moved with no need for protection
	private Map<String,AgamePiece>needprotectionPieces; // Pieces that can be moved that need protection
	private Map<String,AgamePiece>protectorPieces; //Pieces that protect other pieces from a new position
	private List<Position> resourcePositions; // ResourcePositions are reachable positions
	private AgamePiece chosenPiece = null;
	private Position chosenPosition = null;
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
		takenPositions = new HashMap();
		reachablePieces = new HashMap(); // to position reachable from strategy KB
		reachableOpponent = new HashMap(); // What opponent pieces are at what positions.
		tothreatPieces = new HashMap(); // to position threats from parent KB
		toreachablePieces  = new HashMap(); // to position reachable from parent KB
		topawnthreatPieces = new HashMap();// to position pawn threats from parent KB
		fromreachablePieces  = new HashMap(); //From position reachable from strategy KB
		fromthreatparentPieces  = new HashMap(); //From position threats from parent KB
		fromreachparentPieces  = new HashMap(); //From position reachable from parent KB
		frompawnparentPieces  = new HashMap(); //From position pawnthreats from parent KB
		threatparentPieces = new HashMap();
		reachparentPieces = new HashMap();
		pawnparentPieces = new HashMap();
		movablePieces = new HashMap();
		needprotectionPieces = new HashMap();
		protectorPieces = new HashMap();
		resourcePositions = new ArrayList();
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
	}
	
	public AgamePiece getChosenPiece() {
		return chosenPiece;
	}

	public void setChosenPiece(AgamePiece chosenPiece) {
		this.chosenPiece = chosenPiece;
	}

	public Position getChosenPosition() {
		return chosenPosition;
	}

	public void setChosenPosition(Position chosenPosition) {
		this.chosenPosition = chosenPosition;
	}



	public Map<String, ArrayList<AgamePiece>> getMovablePieces() {
		return movablePieces;
	}

	public void setMovablePieces(Map<String, ArrayList<AgamePiece>> movablePieces) {
		this.movablePieces = movablePieces;
	}

	public Map<String, AgamePiece> getNeedprotectionPieces() {
		return needprotectionPieces;
	}

	public void setNeedprotectionPieces(Map<String, AgamePiece> needprotectionPieces) {
		this.needprotectionPieces = needprotectionPieces;
	}

	public Map<String, AgamePiece> getProtectorPieces() {
		return protectorPieces;
	}

	public void setProtectorPieces(Map<String, AgamePiece> protectorPieces) {
		this.protectorPieces = protectorPieces;
	}

	public Map<String, Position> getTakenPositions() {
		return takenPositions;
	}

	public void setTakenPositions(Map<String, Position> takenPositions) {
		this.takenPositions = takenPositions;
	}

	public Map<String, ArrayList<String>> getThreatparentPieces() {
		return threatparentPieces;
	}

	public void setThreatparentPieces(Map<String, ArrayList<String>> threatparentPieces) {
		this.threatparentPieces = threatparentPieces;
	}

	public Map<String, ArrayList<String>> getReachparentPieces() {
		return reachparentPieces;
	}

	public void setReachparentPieces(Map<String, ArrayList<String>> reachparentPieces) {
		this.reachparentPieces = reachparentPieces;
	}

	public Map<String, ArrayList<String>> getPawnparentPieces() {
		return pawnparentPieces;
	}

	public void setPawnparentPieces(Map<String, ArrayList<String>> pawnparentPieces) {
		this.pawnparentPieces = pawnparentPieces;
	}

	public Map<String, ArrayList<String>> getReachablePieces() {
		return reachablePieces;
	}

	public void setReachablePieces(Map<String, ArrayList<String>> reachablePieces) {
		this.reachablePieces = reachablePieces;
	}

	public Map<String, ArrayList<String>> getTothreatPieces() {
		return tothreatPieces;
	}

	public void setTothreatPieces(Map<String, ArrayList<String>> tothreatPieces) {
		this.tothreatPieces = tothreatPieces;
	}

	public Map<String, ArrayList<String>> getToreachablePieces() {
		return toreachablePieces;
	}

	public void setToreachablePieces(Map<String, ArrayList<String>> toreachablePieces) {
		this.toreachablePieces = toreachablePieces;
	}

	public Map<String, ArrayList<String>> getTopawnthreatPieces() {
		return topawnthreatPieces;
	}

	public void setTopawnthreatPieces(Map<String, ArrayList<String>> topawnthreatPieces) {
		this.topawnthreatPieces = topawnthreatPieces;
	}

	public Map<String, ArrayList<String>> getFromreachablePieces() {
		return fromreachablePieces;
	}

	public void setFromreachablePieces(Map<String, ArrayList<String>> fromreachablePieces) {
		this.fromreachablePieces = fromreachablePieces;
	}

	public Map<String, ArrayList<String>> getFromthreatparentPieces() {
		return fromthreatparentPieces;
	}

	public void setFromthreatparentPieces(Map<String, ArrayList<String>> fromthreatparentPieces) {
		this.fromthreatparentPieces = fromthreatparentPieces;
	}

	public Map<String, ArrayList<String>> getFromreachparentPieces() {
		return fromreachparentPieces;
	}

	public void setFromreachparentPieces(Map<String, ArrayList<String>> fromreachparentPieces) {
		this.fromreachparentPieces = fromreachparentPieces;
	}

	public Map<String, ArrayList<String>> getFrompawnparentPieces() {
		return frompawnparentPieces;
	}

	public void setFrompawnparentPieces(Map<String, ArrayList<String>> frompawnparentPieces) {
		this.frompawnparentPieces = frompawnparentPieces;
	}

	public Map<String, String> getReachableOpponent() {
		return reachableOpponent;
	}

	public void setReachableOpponent(Map<String, String> reachableOpponent) {
		this.reachableOpponent = reachableOpponent;
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
		controlPositions = new ArrayList(positions.values());
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
	 * and a map of positions occupied by the player's pieces
	 * The key is the name of the piece
	 */
	public void occupiedPositions() {
//		List<Position> allPositions = (List<Position>) positions.values();
		List<AgamePiece> pieces = opponent.getMygamePieces();
		for (AgamePiece piece:pieces) {
			if (piece.isActive()) {
				String name = piece.getMyPiece().getOntlogyName();
//				int rank = piece.getValue();
				Position pos = piece.getmyPosition();
				if (pos == null)
					pos = piece.getHeldPosition();
				occupiedPositions.put(name, pos);
			}
		}
		List<AgamePiece> mypieces = myPlayer.getMygamePieces();
		for (AgamePiece piece:mypieces) {
			if (piece.isActive()) {
				String name = piece.getMyPiece().getOntlogyName();
//				int rank = piece.getValue();
				Position pos = piece.getmyPosition();
				if (pos == null)
					pos = piece.getHeldPosition();
				takenPositions.put(name, pos);
			}
		}		
	}
	/**
	 * findReachable()
	 * This method runs through all positions occupied by opponent pieces to see
	 * if any of the player's pieces can reach these positions and safely take the opponent piece.
	 * Important elements:
	 * positionKeys: Reachable positions: piecename_frompostopos
	 * positions: A Map of all positions. 
	 * occupiedPositions : A Map of positions occupied by opponent pieces
	 */
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
						tothreatPieces.put(posName, threatNames); 
						ArrayList<String>reachparentNames = (ArrayList<String>) folKb.searchFacts("x", posName, reachable);
						toreachablePieces.put(posName, reachparentNames);
						ArrayList<String>pawnThreats = (ArrayList<String>) folKb.searchFacts("x", posName, pawnAttack);
						topawnthreatPieces.put(posName, pawnThreats);
						ArrayList<String>fromthreatNames = (ArrayList<String>) folKb.searchFacts("x", fromposName, threaten);
						fromthreatparentPieces.put(fromposName, fromthreatNames);
						ArrayList<String>fromreachparentNames = (ArrayList<String>) folKb.searchFacts("x", fromposName, reachable);
						fromreachablePieces.put(fromposName,fromtermNames);
						fromreachparentPieces.put(fromposName, fromreachparentNames);
						ArrayList<String>frompawnThreats = (ArrayList<String>) folKb.searchFacts("x", fromposName, pawnAttack);
						frompawnparentPieces.put(fromposName, frompawnThreats);
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
				} // The end of a confirmed occupied position
			}
		}
		List<AgamePiece> pieces = myPlayer.getMygamePieces();
		List<AgamePiece> opponentPieces = opponent.getMygamePieces();
/*
 * Different test strategies/statistics:
 */
		for (Position position:controlPositions) { // For all positions on the board
			AgamePiece opponentPiece = null;
			AgamePiece opponentrPiece = null;
			AgamePiece piece = null;
			AgamePiece strategyPiece = null;
			AgamePiece opponentstrategyPiece = null;
			String allposName = position.getPositionName();
			String opponentPieceName = reachableOpponent.get(allposName);
			if (opponentPieceName != null) {
				opponentPiece =  (AgamePiece) opponentPieces.stream().filter(c -> c.getMyPiece().getOntlogyName().equals(opponentPieceName)).findAny().orElse(null);
			}
			List<String> fromreachParent = fromreachparentPieces.get(allposName); 
			List<String> fromreachStrategy = fromreachablePieces.get(allposName);
			if (fromreachParent != null && !fromreachParent.isEmpty()) {
				writer.println("Statistics for position "+allposName);
				for (String name:fromreachParent) { // found a "from position" position reachable from parent KB
					piece =  (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().equals(name)).findAny().orElse(null);
					opponentrPiece = (AgamePiece) opponentPieces.stream().filter(c -> c.getMyPiece().getOntlogyName().equals(name)).findAny().orElse(null);
					if (piece != null && piece.isActive()) {
						writer.println("Reachable from parent KB "+piece.getMyPiece().getOntlogyName());
					}
					if (opponentrPiece != null && opponentrPiece.isActive()) {
						writer.println("Opponent reachable from parent KB "+opponentrPiece.getMyPiece().getOntlogyName());
					}
				}

			}
			if (fromreachStrategy != null && !fromreachStrategy.isEmpty()) {
				for (String name:fromreachStrategy) { // found a "from position" position reachable from strategy KB
					int l = name.length();
					int index = l-2;
					String pieceName = name.substring(0,l-5);
					strategyPiece =  (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().equals(pieceName)).findAny().orElse(null);
					opponentstrategyPiece = (AgamePiece) opponentPieces.stream().filter(c -> c.getMyPiece().getOntlogyName().equals(pieceName)).findAny().orElse(null);
					if (strategyPiece != null && strategyPiece.isActive()) {
						writer.println("Reachable from strategy KB "+strategyPiece.getMyPiece().getOntlogyName());
					}
					if (opponentstrategyPiece != null && opponentstrategyPiece.isActive()) {
						writer.println("Opponent reachable from strategy KB "+opponentstrategyPiece.getMyPiece().getOntlogyName());
					}
				}
			}
		}

	    writer.flush();
	}
	/**
	 * simpleSearch
	 * This method builds a map of movable pieces, protector pieces and need protection pieces.
	 * 
	 */
	public void simpleSearch() {
		List<AgamePiece> pieces = myPlayer.getMygamePieces();
		List<AgamePiece> opponentpieces = opponent.getMygamePieces();
		List<AgamePiece> opponentPawns = new ArrayList();
		List<AgamePiece> myPawns = new ArrayList();
		List<AgamePiece> opponentPieceThreats = new ArrayList();
		ArrayList<AgamePiece> mymovablePieces = new ArrayList();
		for (Position pos:controlPositions) { // for all positions
			String posName = pos.getPositionName();
			opponentPawns.clear();
			opponentPieceThreats.clear();
			ArrayList<String> reachablePieces = fromreachparentPieces.get(posName);
			ArrayList<String>protectorPieces = fromreachablePieces.get(posName);// Reachable from strategy KB
			if (reachablePieces != null && !reachablePieces.isEmpty()) { // There is a "from" position (like d5,b5) reachable from parent KB
				writer.println("The from position "+posName + " investigated");
				ArrayList<String> threatPieces = fromthreatparentPieces.get(posName);
				ArrayList<String> threatPawns = frompawnparentPieces.get(posName);
				boolean pawnthreats = threatPawns != null && !threatPawns.isEmpty();
				boolean threats = threatPieces != null && !threatPieces.isEmpty();
				if (pawnthreats) {
					int psize = threatPawns.size();
					for (int i = 0;i<psize;i++) {
						String pawnName = threatPawns.get(i);
						AgamePiece opponentPawn = (AgamePiece) opponentpieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(pawnName)).findAny().orElse(null);
						AgamePiece myPawn = (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(pawnName)).findAny().orElse(null);
						if (opponentPawn != null)
							opponentPawns.add(opponentPawn);
						if (myPawn != null) {
							myPawns.add(myPawn);
							pawnthreats = false;
						}
					}
				}
				if (threats) {
					int psize = threatPieces.size();
					for (int i = 0;i<psize;i++) {
						String pieceName = threatPieces.get(i);
						AgamePiece opponentPiece = (AgamePiece) opponentpieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(pieceName)).findAny().orElse(null);
						opponentPieceThreats.add(opponentPiece);
					}
				}
				int prsize = protectorPieces.size();
				for (int i = 0;i<prsize;i++) {
					String fp = protectorPieces.get(i);
					int l = fp.length();
					int index = l-2;
					String toPosname = fp.substring(index);
					String pieceName = fp.substring(0, index-1);
					AgamePiece protectorPiece = (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(pieceName)).findAny().orElse(null);
					if (protectorPiece != null) { // found a protector
						ChessPieceType pieceType = protectorPiece.getChessType();
						boolean pawn = pieceType instanceof APawn;
						if (!pawn) {
							String protectorKey = posName+"_"+toPosname;
							this.protectorPieces.put(protectorKey, protectorPiece);
							writer.println("Piece "+protectorPiece.getMyPiece().getOntlogyName()+" is protector of "+posName+" from "+toPosname);
						}
					}
				}
				int rsize = reachablePieces.size();
				for (int i = 0;i<rsize;i++) {
					String pieceName = reachablePieces.get(i);
					AgamePiece movePiece = (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(pieceName)).findAny().orElse(null);
					if (movePiece != null) { // found a piece to reach position pos
						mymovablePieces.add(movePiece);
						resourcePositions.add(pos); // reachable positions
						int v = movePiece.getValue();
						if (!pawnthreats && !threats) {
							writer.println("Piece "+movePiece.getMyPiece().getOntlogyName()+" with value "+v+ " can safely move to "+posName);
							movablePieces.put(posName, mymovablePieces);
						}
						if (pawnthreats && !opponentPawns.isEmpty() || threats) {
							writer.println("Piece "+movePiece.getMyPiece().getOntlogyName()+" with value "+v+ " must have a protection for "+posName);
							needprotectionPieces.put(posName, movePiece);
							if (pawnthreats && !opponentPawns.isEmpty()) {
								writer.println("These pawns threatens the piece ");
								int psize = opponentPawns.size();
								for (int ip = 0;ip<psize;ip++) {
									AgamePiece opponentPawn = opponentPawns.get(ip);
									writer.println(opponentPawn.getMyPiece().getOntlogyName());
								}
							}
							if (threats) {
								writer.println("These pieces threatens the piece ");
								int psize = opponentPieceThreats.size();
								for (int ip = 0;ip<psize;ip++) {
									AgamePiece opponentPiece = opponentPieceThreats.get(ip);
									writer.println(opponentPiece.getMyPiece().getOntlogyName());
									
								}
							}
						}
					}
				}

			}
		}
		evaluate();
	}
	public String evaluate() {
		List<AgamePiece> opponentpieces = opponent.getMygamePieces();
		String posName = null;
		Position foundPos = null;
		for (Position pos:resourcePositions) { // Resource positions are reachable positions.
			ChessPiece piece = pos.getUsedBy();
			if (piece != null) {
				AgamePiece myPiece = piece.getMyPiece();
				String name = piece.getOntlogyName();
				AgamePiece opponentPiece = (AgamePiece) opponentpieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(name)).findAny().orElse(null);
				if (opponentPiece != null && opponentPiece == myPiece ) {
					posName = pos.getPositionName();
					foundPos = pos;
					break;
				}
			}
		}
		if (foundPos != null) {	// Found a position with an opponent piece
			boolean needProtection = false;
			ArrayList<AgamePiece> mymovablePieces = movablePieces.get(posName);
			AgamePiece piece = null;
			boolean pieceProtection = false;
			if (mymovablePieces != null && !mymovablePieces.isEmpty()) {
				piece = mymovablePieces.get(0);
				pieceProtection = mymovablePieces.size()>1;
			}
			
			if (piece == null) {
				piece = needprotectionPieces.get(posName);
				needProtection = true;
			}
			if (!needProtection ||pieceProtection ) {
				chosenPiece = piece;
				chosenPosition = foundPos;
			}
			if (needProtection) {
				for (Map.Entry<String,AgamePiece> entry:protectorPieces.entrySet()) {
					String protectorKey = entry.getKey();
					String protectorPos = protectorKey.substring(0, 2);
					String fromPos = protectorKey.substring(3,5);
					Position pos =  (Position) controlPositions.stream().filter(c -> c.getPositionName().contains(fromPos)).findAny().orElse(null);
					if (posName.equals(protectorPos)) {
						AgamePiece protector = entry.getValue();
						ArrayList<AgamePiece> posPieces = movablePieces.get(fromPos);
						String protectorName = protector.getMyPiece().getOntlogyName();
						AgamePiece protPiece = null;
						if (posPieces != null && !posPieces.isEmpty())
							protPiece = (AgamePiece) posPieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(protectorName)).findAny().orElse(null);
						if (protPiece != null) {
							chosenPiece = protector;
							chosenPosition = pos;
						}
//						ArrayList<AgamePiece> myprotPieces = movablePieces.get(fromPos);
//						AgamePiece protpiece = myprotPieces.get(0);
/*						if (piece != null && piece != protector) {
							chosenPiece = protector;
							if (pos != null)
								chosenPosition = pos;
						}*/
					}
				}

			}
		}
		return posName;
	}
}
