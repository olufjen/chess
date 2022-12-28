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
import java.util.Optional;
import java.util.stream.Collectors;

import aima.core.logic.fol.domain.FOLDomain;
import no.chess.web.model.ChessPiece;
import no.chess.web.model.Position;
import no.games.chess.ChessPieceType;
import no.games.chess.AbstractGamePiece.pieceType;
import no.games.chess.fol.FOLGamesBCAsk;
import no.games.chess.fol.FOLGamesFCAsk;
import no.games.chess.planning.PerceptSchema;
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

	private String ACTION;
	private String PROTECTED;
	private String simpleProtected;
	private String ATTACKED;
	private String CAPTURE;
	private String CONQUER;
	private String THREATEN;
	private String OWNER;
	private String MOVE;
	private String REACHABLE;
	private String CANMOVE;
	private String SAFEMOVE;
	private String STRIKE;
	private String PIECETYPE;
	private String PLAY;
	private String PAWN;
	private String KNIGHT;
	private String BISHOP;
	private String ROOK;
	private String KING;
	private String QUEEN;
	private String PAWNMOVE;
	private String playerName =  "";
	private String OCCUPIES = "";
	private String PAWNATTACK ="";
	private String playSide;
	private String BOARD;
	private String PLAYER;
	private String CASTLE;
	private String OPPONENTTO;
	private String POSSIBLETHREAT;
	private List<Position> controlPositions; // The original set of positions as a list
	private HashMap<String,Position> positions; // The original HashMap of positions
	private Map<String,Position> occupiedPositions; //Positions occupied by the opponent pieces. The key is the name of the piece
	private Map<String,Position> takenPositions; //Positions occupied by the player's pieces. The key is the name of the piece
	private APlayer myPlayer = null; // The player of the game
	private APlayer opponent = null; // The opponent of the game
	private ChessFolKnowledgeBase folKb = null; // The parent knowledge base
	private ChessFolKnowledgeBase localKb = null; // The strategy knowledge base
	private FOLDomain chessDomain;
	private FOLGamesFCAsk forwardChain;
	private FOLGamesBCAsk backwardChain;
	private List<String>positionKeys = null;// The key for positions that are reachable. The key is of the form: piecename_frompostopos
	private PerceptSchema thePreceptSchema = null;
	
/*
 * The following two maps are only filled if there are opponent pieces than are reachable:
 * See the method findreachable	
 */
	private Map<String,ArrayList<String>> reachablePieces; //to position reachable from strategy KB. 
//															Contains piecenames of the form WhiteQueen_c4, with a position name as a key
//															The position name is a position occupied by an opponent piece.
	private Map<String,ArrayList<String>> fromreachablePieces; //From positions reachable from strategy KB
	private Map<String,ArrayList<String>> tothreatPieces; // to position threats from parent KB
	private Map<String,ArrayList<String>> toreachablePieces; // to position reachable from parent KB
	private Map<String,ArrayList<String>> topawnthreatPieces;// to position pawn threats from parent KB
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
	private Map<String,AgamePiece>takePieces; //Opponent pieces that that can be taken
	private Map<String,AgamePiece>canlosePieces; //Player's pieces that that can be lost to the opponent
	
	private Map<String,List<AgamePiece>>knownprotectorPieces;// Pieces that protect a given position
	private Map<String,List<AgamePiece>>knownopponentPieces;// Pieces that threaten a given position
	private List<AgamePiece> opponentThreats; // A list of opponent pieces that are threatening a given position
	// It is filled with a call to the folKb.checkThreats method
	private List<AgamePiece> opponentProtectors;// A list of opponent pieces that are protecting a given position
	// It is filled with a call to the folKb.checkThreats method
	private List<Position> resourcePositions; // ResourcePositions are reachable positions
	private AgamePiece chosenPiece = null;
	private Position chosenPosition = null; // This position is null or set in the evaluate function
	private String opponentColor = "Black"; //OBS !!
	private String playerColor = "White";
	private OpponentAgent agent;
	private String opponentKingPosition = null;
	private List <ChessActionImpl> playeractions = null; // all actions available to player
	private String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\performance.txt";
	private PrintWriter writer = null;
	private FileWriter fw = null;
	
	private boolean canTakeKing = false; // Set true when when opponent king can be taken
	
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
		knownprotectorPieces = new HashMap();
		knownopponentPieces = new HashMap();
		resourcePositions = new ArrayList();
		takePieces = new HashMap(); //Opponent pieces that can be taken.
		canlosePieces = new HashMap(); // pieces that can be lost to opponent
		setPredicatenames();
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
	}
	  public void setPredicatenames() {
			ACTION = KnowledgeBuilder.getACTION();
			ATTACKED = KnowledgeBuilder.getATTACKED();
			CANMOVE = KnowledgeBuilder.getCANMOVE();
			CAPTURE =  KnowledgeBuilder.getCAPTURE();
			CONQUER = KnowledgeBuilder.getCONQUER();
			MOVE =  KnowledgeBuilder.getMOVE();
			OWNER = KnowledgeBuilder.getOWNER();
			PROTECTED =  KnowledgeBuilder.getPROTECTED();
			REACHABLE = KnowledgeBuilder.getREACHABLE();
			SAFEMOVE = KnowledgeBuilder.getSAFEMOVE();
			STRIKE = KnowledgeBuilder.getSTRIKE();
			simpleProtected =  KnowledgeBuilder.getSimpleProtected();
			THREATEN = KnowledgeBuilder.getTHREATEN();
			PIECETYPE = KnowledgeBuilder.getPIECETYPE();
			PLAY = 	KnowledgeBuilder.getPLAY();
			PAWN = KnowledgeBuilder.getPAWN();
			KNIGHT = KnowledgeBuilder.getKNIGHT();
			BISHOP = KnowledgeBuilder.getBISHOP();
			ROOK = KnowledgeBuilder.getROOK();
			KING = KnowledgeBuilder.getKING();
			QUEEN = KnowledgeBuilder.getQUEEN();
			OCCUPIES = KnowledgeBuilder.getOCCUPIES();
			PAWNMOVE = KnowledgeBuilder.getPAWNMOVE();
			PAWNATTACK = KnowledgeBuilder.getPAWNATTACK();
			BOARD = KnowledgeBuilder.getBOARD();
			PLAYER = KnowledgeBuilder.getPLAYER();
			CASTLE = KnowledgeBuilder.getCASTLE();
			OPPONENTTO = KnowledgeBuilder.getOPPONENTTO();
			POSSIBLETHREAT = KnowledgeBuilder.getPOSSIBLETHREAT();
	  }	
	  
	  public List<ChessActionImpl> getPlayeractions() {
		return playeractions;
	  }
	  public void setPlayeractions(List<ChessActionImpl> playeractions) {
		  this.playeractions = playeractions;
	  }
	public Map<String, Position> getOccupiedPositions() {
		  return occupiedPositions;
	  }

	  public void setOccupiedPositions(Map<String, Position> occupiedPositions) {
		  this.occupiedPositions = occupiedPositions;
	  }

	  public boolean isCanTakeKing() {
		  return canTakeKing;
	  }

	  public void setCanTakeKing(boolean canTakeKing) {
		  this.canTakeKing = canTakeKing;
	  }

	  public String getOpponentKingPosition() {
		  return opponentKingPosition;
	  }

	  public void setOpponentKingPosition(String opponentKingPosition) {
		  this.opponentKingPosition = opponentKingPosition;
	  }

	  public String getOpponentColor() {
		  return opponentColor;
	  }

	  public void setOpponentColor(String opponentColor) {
		  this.opponentColor = opponentColor;
	  }

	  public String getPlayerColor() {
		  return playerColor;
	  }

	  public void setPlayerColor(String playerColor) {
		  this.playerColor = playerColor;
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
	   * The maps are called occupiedPositions and takenPositions
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
			  String fromposName = key.substring(l-4, l-2); // The position from which to reach a position
			  String pieceName = key.substring(0,l-5);
			  writer.println("Checking position key: "+key+" Piece "+pieceName+" From position "+fromposName);
			  if (toPos != null) {
				  if (occupiedPositions.containsValue(toPos)) {
					  Position entryPos = null;
					  writer.println("An occupied position: "+toPos.getPositionName());
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
						  // fromposName The position from which to reach a position:
						  ArrayList<String>fromtermNames = (ArrayList<String>) localKb.searchFacts("x", fromposName, reachable);
						  reachablePieces.put(posName, termNames); // posName is the name of the position of the opponent piece
						  fromreachablePieces.put(fromposName,fromtermNames);
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
		  } // End reachable positions+
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
			  List<String> reachStrategy = reachablePieces.get(allposName);
			  if (fromreachParent != null && !fromreachParent.isEmpty()) {
				  writer.println("Statistics for position "+allposName); // Makes statistics for all positions on the board
				  for (String name:fromreachParent) { // found a "from position" position reachable from parent KB
					  piece =  (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().equals(name)).findAny().orElse(null);
					  opponentrPiece = (AgamePiece) opponentPieces.stream().filter(c -> c.getMyPiece().getOntlogyName().equals(name)).findAny().orElse(null);
					  if (piece != null && piece.isActive()) {
						  String pPos = piece.getMyPosition().getPositionName();
						  writer.println("Reachable from parent KB "+piece.getMyPiece().getOntlogyName()+ " At "+ pPos);
					  }
					  if (opponentrPiece != null && opponentrPiece.isActive()) {
						  writer.println("Opponent reachable from parent KB "+opponentrPiece.getMyPiece().getOntlogyName());
					  }
				  }

			  }
			  if (fromreachStrategy != null && !fromreachStrategy.isEmpty()) { // These maps are only filled if there is an opponent piece at this particular position
				  for (String name:fromreachStrategy) { // found a "from position" position reachable from strategy KB Always empty??!! 30.03.22
					  int l = name.length();
					  int index = l-2;
					  String pieceName = name.substring(0,l-3);
					  //					writer.println("Strategy: "+pieceName);
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
			  if (reachStrategy != null && !reachStrategy.isEmpty()) { // These maps are only filled if there is an opponent piece at this particular position
				  for (String name:reachStrategy) { // found a "from position" position reachable from strategy KB
					  int l = name.length();
					  int index = l-2;
					  String pieceName = name.substring(0,l-3);
					  //					writer.println("Strategy 2: "+pieceName);
					  strategyPiece =  (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().equals(pieceName)).findAny().orElse(null);
					  opponentstrategyPiece = (AgamePiece) opponentPieces.stream().filter(c -> c.getMyPiece().getOntlogyName().equals(pieceName)).findAny().orElse(null);
					  if (strategyPiece != null && strategyPiece.isActive()) {
						  writer.println(allposName+ " Reachable from strategy KB "+strategyPiece.getMyPiece().getOntlogyName());
					  }
					  if (opponentstrategyPiece != null && opponentstrategyPiece.isActive()) {
						  writer.println(allposName+" Opponent reachable from strategy KB "+opponentstrategyPiece.getMyPiece().getOntlogyName());
					  }
				  }
			  }
		  }


	  }
	  /**
	   * simpleSearch
	   * This method runs through all positions on the board and
	   * investigates positions that are reachable from the parent KB
	   * and which pieces that possibly can protect these reachable positions.
	   * this results in three different Maps:
	   * protectorPieces - pieces that protect a position from a new position
	   * needprotectionPieces - pieces that need a protection in a new position.
	   * movablePieces - pieces that can be moved to a new position without a threat.
	   */
	  public void simpleSearch() {
		  writer.println("A simple search");
		  List<AgamePiece> pieces = myPlayer.getMygamePieces();
		  List<AgamePiece> opponentpieces = opponent.getMygamePieces();
		  List<AgamePiece> opponentPawns = new ArrayList();
		  List<AgamePiece> myPawns = new ArrayList();
		  List<AgamePiece> opponentPieceThreats = new ArrayList();
		  for (Position pos:controlPositions) { // for all positions
			  String posName = pos.getPositionName();
			  opponentPawns.clear();
			  opponentPieceThreats.clear();
			  ArrayList<String> reachablePieces = fromreachparentPieces.get(posName); // More than one to this position!!??
			  AgamePiece opponent = null;
			  boolean opponentreach = false;
			  if (reachablePieces != null && !reachablePieces.isEmpty()) {
				  for (String name:reachablePieces) { 
					  opponent = (AgamePiece) opponentpieces.stream().filter(c -> c.getMyPiece().getOntlogyName().equals(name)).findAny().orElse(null);
					  if (opponent != null) {
						  ChessPieceType pieceType = opponent.getChessType();
						  boolean pawn = pieceType instanceof APawn;
						  if (!pawn) {
							  opponentreach = true;
						  }
					  }
				  }
			  }
			  ArrayList<String>protectorPieces = fromreachablePieces.get(posName);// Reachable from strategy KB
			  if (reachablePieces != null && !reachablePieces.isEmpty()) { // There is a "from" position (like d5,b5) reachable from parent KB
				  writer.println("The from position "+posName + " investigated");
				  ArrayList<AgamePiece> mymovablePieces = new ArrayList();
				  mymovablePieces.clear();
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
				  int rsize = reachablePieces.size(); // reachablePieces also contains opponent pieces!!
				  checkOpponent(posName,reachablePieces); // opponent pieces placed in knownopponentPieces
				  List<AgamePiece> opponentProtectors = knownopponentPieces.get(posName);
				  pawnthreats = opponentProtectors != null && !opponentProtectors.isEmpty(); // Added 2.05.22
				  //				String opponentP = (String) reachablePieces.stream().filter(c -> c.contains("Black")).findAny().orElse(null);
				  boolean protectorpieces = checkMypieces(posName, reachablePieces);// player pieces placed in knownprotectorPieces
				  for (int i = 0;i<rsize;i++) {
					  String pieceName = reachablePieces.get(i);
					  /*
					if (posName.equals("b5")) { // TESTS !!
						writer.println("checking "+posName + " "+rsize);
					}*/
					  AgamePiece movePiece = (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(pieceName)).findAny().orElse(null);
					  if (movePiece != null) { // found a piece for player to reach position posName
						  resourcePositions.add(pos); // reachable positions - pos is one of all positions on the board
						  int v = movePiece.getValue();
						  boolean safemove = false;
						  if ((!pawnthreats && !threats && !opponentreach) || protectorpieces) {
							  mymovablePieces.add(movePiece);
							  writer.println("Piece "+movePiece.getMyPiece().getOntlogyName()+" with value "+v+ " can safely move to "+posName);
							  safemove = true;
						  }
						  if ((pawnthreats && !opponentPawns.isEmpty() || threats || opponentreach) && !safemove) {
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
				  movablePieces.put(posName,mymovablePieces);
			  } //End There is a "from" position
		  } //End for all positions
		  evaluate();
		  writer.flush();
	  }
	  /**
	   * evaluate
	   * This method attempts to find the next move.
	   * It runs through all reachable positions to find if there are any opponent pieces to take, and if there are any player's pieces under threat.
	   * They are held in Maps called takePieces and canlosepieces. The key is the position they occupy.
	   * If these maps are empty the method attempts to find the next move among the movable pieces held in the map movablePieces.
	   * The key in this map is the position they occupy.
	   * @return
	   */
	  public String evaluate() {
		  writer.println("Evaluation");
		  List<AgamePiece> opponentpieces = opponent.getMygamePieces();
		  List<AgamePiece> pieces = myPlayer.getMygamePieces();
		  String posName = null;
		  Position foundPos = null;
		  takePieces.clear();
		  canlosePieces.clear();
		  chosenPiece = null;
		  chosenPosition = null;
		  boolean chosen = false;
		  boolean noexit = false;
		  for (Position pos:resourcePositions) { // Resource positions are reachable positions.
			  ChessPiece piece = pos.getUsedBy();
			  if (piece != null) {
				  AgamePiece myPiece = piece.getMyPiece();
				  String name = piece.getOntlogyName();
				  AgamePiece opponentPiece = (AgamePiece) opponentpieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(name)).findAny().orElse(null);
				  AgamePiece threatenedPiece = (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(name)).findAny().orElse(null);
				  if (opponentPiece != null && opponentPiece == myPiece ) {
					  posName = pos.getPositionName();
					  //					foundPos = pos;
					  takePieces.put(posName, myPiece); // opponent pieces that can be taken
					  //					break;
				  }
				  if (threatenedPiece != null && threatenedPiece == myPiece ) {
					  posName = pos.getPositionName();
					  //					foundPos = pos;
					  canlosePieces.put(posName, myPiece); // own pieces that can be lost
				  }
			  }
		  }
		  if (!canlosePieces.isEmpty()) { //Find a safe place for these pieces
			  for (Map.Entry<String,AgamePiece> entry:canlosePieces.entrySet()) {
				  posName = entry.getKey();
				  AgamePiece myownPiece = entry.getValue();
				  String myPieceName = myownPiece.getMyPiece().getOntlogyName();
				  writer.println("This piece can be lost "+myPieceName + " at "+posName);
			  }

		  }
		  if (!takePieces.isEmpty()) {	// Found opponent pieces that can be taken at reachable positions
			  int takesize = takePieces.size();
			  writer.println("There are "+takesize+ " opponent pieces to take ");
			  for (Map.Entry<String,AgamePiece> entry:takePieces.entrySet()) { // For loop 1 Opponent pieces to take
				  posName = entry.getKey();
				  AgamePiece opponentPiece = entry.getValue();
				  foundPos = positions.get(posName);
				  writer.println("A piece to take "+opponentPiece.getMyPiece().getOntlogyName()+" at "+posName);
				  //				ChessPieceType thepieceType = opponentPiece.getChessType();
				  pieceType type = opponentPiece.getPieceType();
				  boolean opponentKing = false;
				  if (type == type.KING) {
					  opponentKing = true;
				  }
				  List<AgamePiece>  opponentprotectors = knownopponentPieces.get(posName);// Pieces that threaten a given position
				  List<AgamePiece>  protectors = knownprotectorPieces.get(posName);// Pieces that protect a given position
				  int protsize = protectors.size();
				  Optional<AgamePiece> chosenpiece = protectors.stream().reduce((p1, p2) -> p1.getValue()<=p2.getValue() ? p1:p2);
				  if (opponentKing || opponentprotectors == null || opponentprotectors.isEmpty()) {
					  if (chosenpiece.isPresent()) {
						  chosenPiece = chosenpiece.get();
						  chosenPosition = foundPos;
						  writer.println("Exit with no protector or threat  "+chosenPiece.getMyPiece().getOntlogyName()+" to position "+foundPos.getPositionName());
						  break; // break leaves the for loop 1
					  }
				  }
				  if (opponentprotectors != null && !opponentprotectors.isEmpty()) {
					  opponentprotectors.sort((p1,p2)->p1.getMyValue().compareTo(p2.getMyValue()));
					  AgamePiece opponentProtector = opponentprotectors.get(0);
					  int size = opponentprotectors.size();
					  AgamePiece lastprotector = opponentprotectors.get(size-1);
					  String firstname = opponentProtector.getMyPiece().getOntlogyName();
					  String lastname = lastprotector.getMyPiece().getOntlogyName();
					  writer.println("Opponent protectors "+firstname+" "+lastname+ " No of protectors "+size);
					  if (protsize > 1 && chosenpiece.isPresent()) {
						  chosenPiece = chosenpiece.get();
						  chosenPosition = foundPos;
						  writer.println("Exit with  protection "+chosenPiece.getMyPiece().getOntlogyName()+" to position "+foundPos.getPositionName());
						  break;// break leaves the for loop 1
					  }
					  if (protsize <= 1) { // Must find a piece to protect the piece I want to move
						  String chosenpieceName = "";
						  if (chosenpiece.isPresent()) {
							  chosenpieceName = chosenpiece.get().getMyPiece().getOntlogyName();
						  }
						  writer.println("Must find protectors for  "+chosenpieceName+ " at "+posName);
						  List<String> fromreachStrategy = fromreachablePieces.get(posName);
						  AgamePiece strategyPiece = null;
						  AgamePiece opponentstrategyPiece = null;
						  if (fromreachStrategy != null && !fromreachStrategy.isEmpty()) {
							  for (String name:fromreachStrategy) { // for loop 2 found a "from position" reachable from strategy KB
								  int l = name.length();
								  int index = l-2;
								  String toPosname = name.substring(index);
								  String pieceName = name.substring(0,l-3);
								  writer.println("Evaluation investigating "+pieceName + " and position "+toPosname);
								  /*								if (posName.equals("d5")) {
									writer.println("A protector for "+posName+ " is "+pieceName+" from "+toPosname);
								}*/
								  strategyPiece =  (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().equals(pieceName)).findAny().orElse(null);
								  opponentstrategyPiece = (AgamePiece) opponentpieces.stream().filter(c -> c.getMyPiece().getOntlogyName().equals(pieceName)).findAny().orElse(null);
								  if (strategyPiece != null && strategyPiece.isActive()) {
									  writer.println("Evaluation player reachable from strategy KB "+strategyPiece.getMyPiece().getOntlogyName()+" from "+toPosname);
									  //									writer.println("Reachable from strategy KB "+strategyPiece.getMyPiece().getOntlogyName());
									  String threaten = agent.getTHREATEN();
									  String protect = agent.getPROTECTED();
									  boolean threat = folKb.checkThreats("x", toPosname, threaten,opponent);
									  opponentThreats = folKb.getMovePieces();
									  boolean opponentProtect = folKb.checkThreats("x", toPosname, protect,opponent); // Added 29.05.22
									  opponentProtectors = folKb.getMovePieces();
									  if (!threat && !opponentProtect) { // Changed 29.05.22
										  chosenPiece = strategyPiece; 
										  chosenPosition = positions.get(toPosname);
										  posName = toPosname;
										  writer.println("Exit with  protector "+chosenPiece.getMyPiece().getOntlogyName()+" to position "+toPosname);
										  break;// break leaves the for loop 2
									  }
								  }
								  if (opponentstrategyPiece != null && opponentstrategyPiece.isActive()) {
									  writer.println("Evaluation Opponent reachable from strategy KB "+opponentstrategyPiece.getMyPiece().getOntlogyName()+" from "+toPosname);
									  noexit = true;
								  }
							  } // End for loop 2
						  }

					  }
					  //					List<AgamePiece> attackers = opponentprotectors.stream().sorted((p1,p2) -> p1.getMyValue().compareTo(p2.getMyValue()).;
				  } // End there exist opponent protectors

			  } // End for loop 1
		  } // End pieces can be taken
		  if (takePieces.isEmpty()|| chosenPiece == null || noexit) { // No opponent pieces can be taken. What movable Piece to use?
			  writer.println("No opponent pieces to take");
			  if (!movablePieces.isEmpty()) {
				  writer.println("What movable piece to use?");
				  for (Map.Entry<String,ArrayList<AgamePiece>> entry:movablePieces.entrySet()) { // Checking for all my movable pieces
					  String possibleposName = entry.getKey(); // A position the piece can move to
					  ArrayList<AgamePiece> movablePieces = entry.getValue();
					  for (AgamePiece piece:movablePieces) { // A set of movable pieces to use
						  String pieceName = piece.getMyPiece().getOntlogyName();
						  String predicate = piece.getNameType();
						  Position inPos = null;
						  inPos = piece.getMyPosition();
						  if (inPos == null) {
							  inPos = piece.getHeldPosition();
						  }
						  writer.println("Investigating for piece "+pieceName + " at " + inPos.getPositionName() + " with type "+predicate+" and position to move to "+possibleposName);
						  List<String> localpositions = localKb.searchKing(predicate, opponentKingPosition);
						  boolean possibleMove = folKb.checkFacts(pieceName, opponentKingPosition, REACHABLE, playeractions,controlPositions);
						  for (String pos:localpositions) { // Positions that can reach the opponent king's position given a certain type of piece
							  writer.println("The piece can take the king from "+pos+" and move is possible? "+possibleMove);
							  boolean takeKing = inPos.getPositionName().equals(pos); // inPos is the position of the piece investigated
							  if (takeKing && possibleMove) { // Must create a warning. The opponent king will be taken
								  posName = opponentKingPosition;
								  chosenPosition = positions.get(opponentKingPosition);
								  writer.println(pieceName+" Takeking set !!");
								  // This creates problems !!!
								  // chosenPiece = piece;
								  canTakeKing = takeKing;
							  }
							  List<String> pieceNames = fromreachparentPieces.get(pos); // Pieces that can reach a given position
							  if (pieceNames != null && !pieceNames.isEmpty()) {
								  for (String pName:pieceNames) {
									  AgamePiece possiblePiece = (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(pName)).findAny().orElse(null);
									  if(possiblePiece != null) { // Must check pawn !!!
										  ChessPieceType pieceType = possiblePiece.getChessType();
										  boolean pawn = pieceType instanceof APawn;
										  Position pawnPos = null;
										  if (pawn) {
											  pawnPos = possiblePiece.getAttackPositions().get(pos);
										  }
										  if (!pawn || pawnPos != null) {
											  Position thePos = null;
											  thePos = possiblePiece.getMyPosition();
											  if (thePos == null) {
												  thePos = possiblePiece.getHeldPosition();
											  }
											  writer.println("The following piece is available as protector for position "+pos+": "+pName+ " and is at "+thePos.getPositionName() );
											  if (pName.equals(pieceName) || takeKing) {
												  writer.println(pieceName+" Can threaten the king from  "+pos);
												  if (takeKing) {// Must create a warning. The opponent king will be taken
													  writer.println(pieceName+"Takes the King WARNING!!");
													  chosenPiece = possiblePiece;
													  canTakeKing = takeKing;
												  }
											  }
										  }

									  }

								  }
							  }
						  } // Loop positions that can reach opponent king's position
						  if(canTakeKing) {
							  writer.println(pieceName+" Takes the king ");
							  break; // break loop 1 checking my movable pieces
						  }else { // No pieces can take the king

						  }
					  } // Loop set of movable pieces
				  } // End checking my movable pieces
			  }
		  }
		  String chosename = "No name";
		  if (chosenPiece != null) {
			  chosename = chosenPiece.getMyPiece().getOntlogyName();
		  }
		  writer.println("Return from evaluation "+chosename+ " "+posName);
		  return posName; //posName is the name of the position I want to move to or protect
	  }
	  /**
	   * checkOpponent
	   * This method checks if there are opponent pieces among the reachable pieces.
	   * If the opponent piece is a pawn, it then searches for opponent pawns that may have an attack position
	   * These pieces are put in a map called knownopponentPieces with posName as key
	   * that is posName
	   * @param posName,reachablePieces
	   */
	  public void checkOpponent(String posName,ArrayList<String> reachablePieces) {
		  List<AgamePiece> opponentpieces = opponent.getMygamePieces();
		  List<AgamePiece>  protectors = new ArrayList();
		  //		String opponentP = (String) reachablePieces.stream().filter(c -> c.contains("Black")).findAny().orElse(null);
		  for (String name:reachablePieces) {
			  if (name.contains(opponentColor)) {
				  AgamePiece opponentPawn = (AgamePiece) opponentpieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(name)).findAny().orElse(null);
				  String pawname = opponentPawn.getMyPiece().getOntlogyName();
				  ChessPieceType pieceType = opponentPawn.getChessType();
				  boolean pawn = pieceType instanceof APawn;
				  if (!pawn && opponentPawn.isActive()) {
					  protectors.add(opponentPawn);
					  writer.println("Opponent  "+pawname+ " can reach "+posName);
				  }
			  }
		  }
		  for (AgamePiece piece:opponentpieces) {
			  ChessPieceType pieceType = piece.getChessType();
			  boolean pawn = pieceType instanceof APawn;
			  String name = piece.getMyPiece().getOntlogyName();
			  if (pawn && piece.isActive()) {
				  HashMap<String,Position> attackPos = piece.getAttackPositions();
				  if (attackPos != null && !attackPos.isEmpty()) {
					  Position pos = attackPos.get(posName);
					  if (pos != null) {
						  protectors.add(piece);
						  writer.println("Opponent pawn "+name+ " can take at "+posName);
					  }
				  }
			  }
		  }
		  knownopponentPieces.put(posName,protectors);

	  }
	  /**
	   * checkMypieces
	   * This method checks which of the player's pieces protect a given position 
	   * These pieces are put in a map called knownprotectorPieces with posName as key
	   * @param posName The given position
	   * @param reachablePieces
	   * @return true if the position has a protection
	   */
	  public boolean checkMypieces(String posName,ArrayList<String> reachablePieces) {
		  List<AgamePiece> mypieces = myPlayer.getMygamePieces();
		  List<AgamePiece>  protectors = new ArrayList();
		  boolean protectorflag = false;
		  int counter = 0;
		  for (String name:reachablePieces) {
			  if (name.contains(playerColor)) {
				  AgamePiece myPiece = (AgamePiece) mypieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(name)).findAny().orElse(null);
				  if (myPiece != null && myPiece.isActive()) {
					  ChessPieceType pieceType = myPiece.getChessType();
					  String pawname = myPiece.getMyPiece().getOntlogyName();
					  boolean pawn = pieceType instanceof APawn;
					  if (!pawn) {
						  protectors.add(myPiece);
						  writer.println("Player's  "+pawname+ " is protector at "+posName);
					  }
					  counter++;
				  }
			  }
		  }
		  for (AgamePiece piece:mypieces) {
			  ChessPieceType pieceType = piece.getChessType();
			  boolean pawn = pieceType instanceof APawn;
			  String name = piece.getMyPiece().getOntlogyName();
			  if (pawn && piece.isActive()) {
				  HashMap<String,Position> attackPos = piece.getAttackPositions();
				  if (attackPos != null && !attackPos.isEmpty()) {
					  Position pos = attackPos.get(posName);
					  if (pos != null) {
						  protectors.add(piece);
						  writer.println("Player's  "+name+ " is protector at "+posName);
					  }
				  }
			  }
		  }
		  knownprotectorPieces.put(posName,protectors);
		  protectorflag = counter>1;
		  return protectorflag;
	  }
	  /**
	   * findKingsReachable
	   * This method produces facts to the strategy KB of the form:
	   * BISHOP(h7,g8) - which says that the position g8 is reachable from h7 by a BISHOP
	   * The position g8 is the current position of the opponent king
	   * It is called from the opponent agent when the opponent king's position is set.
	   */
	  public void findKingsReachable() {
		  List<AgamePiece> pieces = myPlayer.getMygamePieces();
		  AgamePiece piece = pieces.get(0);
		  ChessPiece chesspiece = piece.getMyPiece();
		  if (opponentKingPosition != null) {
			  Position position = positions.get(opponentKingPosition);
			  ABishop bishop = new ABishop(position,chesspiece);
			  HashMap<String,Position> reachables = bishop.getLegalmoves();
			  String predicate = agent.getBISHOP();
			  List<Position> reachablelist = new ArrayList<Position>(reachables.values());
			  for (Position pos:reachablelist) {
				  String posName = pos.getPositionName();
				  //				writer.println("Bishop Reachable to Kings position from "+posName + " King at "+opponentKingPosition);
				  localKb.createfacts(predicate,opponentKingPosition, posName);
			  }
			  ARook rook = new ARook(position,chesspiece);
			  HashMap<String,Position> rookreachables = rook.getLegalmoves();
			  String rookpredicate = agent.getROOK();
			  List<Position> rookreachablelist = new ArrayList<Position>(rookreachables.values());
			  for (Position pos:rookreachablelist) {
				  String posName = pos.getPositionName();
				  //				writer.println("Rook Reachable to Kings position from "+posName + " King at "+opponentKingPosition);
				  localKb.createfacts(rookpredicate,opponentKingPosition, posName);
			  }
			  AQueen queen = new AQueen(position,chesspiece);
			  HashMap<String,Position> queenreachables = queen.getLegalmoves();
			  HashMap<String,Position> queenbishop = queen.getBishopPositions();
			  String queenpredicate = agent.getQUEEN();
			  List<Position> queenreachablelist = new ArrayList<Position>(queenreachables.values());
			  List<Position> queenbishoplist = new ArrayList<Position>(queenbishop.values());
			  for (Position pos:queenreachablelist) {
				  String posName = pos.getPositionName();
				  //				writer.println("Queen rook Reachable to Kings position from "+posName + " King at "+opponentKingPosition);
				  localKb.createfacts(queenpredicate,opponentKingPosition, posName);
			  }
			  for (Position pos:queenbishoplist) {
				  String posName = pos.getPositionName();
				  //				writer.println("Queen bishop Reachable to Kings position from "+posName + " King at "+opponentKingPosition);
				  localKb.createfacts(queenpredicate,opponentKingPosition, posName);
			  }
			  AKnight knight = new AKnight(position,chesspiece);
			  HashMap<String,Position> knightreachables = knight.getLegalmoves();
			  List<Position> knightreachablelist = new ArrayList<Position>(knightreachables.values());
			  String knightpredicate = agent.getKNIGHT();
			  for (Position pos:knightreachablelist) {
				  String posName = pos.getPositionName();
				  //				writer.println("Knight Reachable to Kings position from "+posName + " King at "+opponentKingPosition);
				  localKb.createfacts(knightpredicate,opponentKingPosition, posName);
			  }
			  APawn pawn = new APawn(position,chesspiece);
			  /*			HashMap<String,Position> pawnreachables = pawn.getAttackPositions();
			List<Position> pawnreachablelist = new ArrayList<Position>(pawnreachables.values());*/
			  List<Position> pawnreachablelist = pawn.produceAttack(position);
			  String pawnpredicate = agent.getPAWN();
			  for (Position pos:pawnreachablelist) {
				  String posName = pos.getPositionName();
				  writer.println("Pawn Reachable to Kings position from "+posName + " King at "+opponentKingPosition);
				  localKb.createfacts(pawnpredicate,opponentKingPosition, posName);
			  }
		  }

	  }
}
