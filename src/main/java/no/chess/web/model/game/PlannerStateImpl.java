package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import aima.core.logic.planning.ActionSchema;
import no.chess.web.model.Position;
import no.games.chess.ChessPlayer;
import no.games.chess.planning.ChessPlannerAction;
import no.games.chess.planning.PlannerState;

/**
 * PlannerStateImpl
 * This class represent the Planner State of the game.
 * It contains a number of Planner Actions and Action Schemas, and the Player of the game.
 * There is one Planner Action for every Action schema
 * It is created by the Problem Solver when the planProblem method is called.
 * @author oluf
 *
 */
public class PlannerStateImpl implements PlannerState {
	private String[] notations = {"d4","c4","Nf3","Nc3","e3","Bd3","Rf1","h3"}; // These are keys for the first 6 moves. It represent the start strategy
	private APlayer player;
	private APlayer opponent;
	private String forCastlingKey = "o-o";
	private String playerName = "WHITE"; // PLayer is white and opponent is black
	private String kingPos = "e1";
	private String whiteKing = "WhiteKing";
	private String whiteRook = "WhiteRook2"; // For short castling
	private List<ActionSchema> actionSchemas; // Ground action schemas from Chess Actions Each action schema has an initial and goal state CHECK!!
	// These are action schemas produced from all possible available chess actions.	
	private List<ActionSchema>otherSchemaList = null;// A list of propositionalized action schemas from the lifted action schema. It is used for problem solving
	private List<ChessPlannerAction> plannerActions; // Contains the list of propositionalized action schemas from the lifted action schema
	private ChessPlannerAction plannerAction; // The first entry of the list of plannerActions
	private int moveNr;
	private APerceptor thePerceptor = null;
	private String outputFileName =  "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\planning.txt";
	private PrintWriter writer =  null;
	private FileWriter fw =  null;
	private String[] liftedKey = new String[5]; // A String array used as a parameter set for a lifted action
	private ThePeas peas = null;
	public PlannerStateImpl(APlayer player,APlayer opponent, List<ActionSchema> actionSchemas,List<ActionSchema>otherSchemaList, int moveNr) {
		super();
		this.player = player;
		this.opponent = opponent;
		this.actionSchemas = actionSchemas;
		this.otherSchemaList = otherSchemaList;
		this.moveNr = moveNr;
		plannerActions = new ArrayList<ChessPlannerAction>();
		createplannerActions();
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
	    if (liftedKey == null || Arrays.stream(liftedKey).allMatch(Objects::isNull)) {
	    	writer.println("liftedkey is empty");
	    }
	    
	}
	

	public PlannerStateImpl(APlayer player,APlayer opponent, List<ActionSchema> actionSchemas, int moveNr, APerceptor thePerceptor) {
		super();
		this.player = player;
		this.opponent = opponent;
		this.actionSchemas = actionSchemas;
		this.moveNr = moveNr;
		this.thePerceptor = thePerceptor;
		plannerActions = new ArrayList<ChessPlannerAction>();

		createplannerActions();
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
	    writer.println("Planning");
	    if (liftedKey == null || Arrays.stream(liftedKey).allMatch(Objects::isNull)) {
	    	writer.println("liftedkey is empty");
	    }
	    writer.flush();
	}


	private void createplannerActions() {
		for (ActionSchema schema:actionSchemas) {
			ChessPlannerAction plannerAction = new ChessPlannerActionImpl(schema,player, moveNr, this);
			plannerActions.add(plannerAction);
		}
		if (otherSchemaList != null) {
			for (ActionSchema schema:otherSchemaList) {
				ChessPlannerAction plannerAction = new ChessPlannerActionImpl(schema,player, moveNr, this);
				plannerActions.add(plannerAction);
			}
		}

		plannerAction = plannerActions.get(0);
	}
	public List<ChessPlannerAction> getPlannerActions() {
		return plannerActions;
	}

	public void setPlannerActions(List<ChessPlannerAction> plannerActions) {
		this.plannerActions = plannerActions;
	}

	public ChessPlannerAction getPlannerAction() {
		return plannerAction;
	}

	public void setPlannerAction(ChessPlannerAction plannerAction) {
		this.plannerAction = plannerAction;
	}

	public List<ActionSchema> getActionSchemas() {
		return actionSchemas;
	}

	public void setActionSchemas(List<ActionSchema> actionSchemas) {
		this.actionSchemas = actionSchemas;
	}

	public APlayer getPlayer() {
		return player;
	}

	public void setPlayer(APlayer player) {
		this.player = player;
	}
	public List<ActionSchema> getOtherSchemaList() {
		return otherSchemaList;
	}
	public void setOtherSchemaList(List<ActionSchema> otherSchemaList) {
		this.otherSchemaList = otherSchemaList;
	}
	@Override
	public double getUtility() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setUtility(double utility) {
		// TODO Auto-generated method stub

	}

	@Override
	public ChessPlayer getPlayerTomove() {
		// TODO Auto-generated method stub
		return player;
	}

	@Override
	public ChessPlannerAction getAction() {
	
		return this.plannerAction;
	}

	@Override
	public List<ChessPlannerAction> getActions() {
			return getPlannerActions();
	}

	@Override
	public ActionSchema getActionSchema() {
		
		return null;
	}

	@Override
	public void setAction(ChessPlannerAction action) {
		this.plannerAction = action;
		
	}
	/**
	 * selectStategy
	 * This method check if any opponent pieces are possible to catch.
	 * The method needs to check if several opponent pieces can be taken and then
	 * choose to take the piece with the highest value.
	 * For the first 5 opening moves skip this check unless the opponent has taken a
	 * piece from the player.
	 * @return true if the move number is less than 6.
	 */
	public boolean selectStrategy() {
		String[] paramKey = new String[5];
		boolean flag = moveNr/2 < 6; // Signals the first 5 opening moves
		String name = null;
		String posName = null;
		String pieceType = null;
		int totopval = 0;
		int myval = 0;
		boolean take = false;
		String alName = null; // The algebraic name of a piece
		String algebraicKey = null;

		Map<String,AgamePiece> pieces = thePerceptor.getPossiblePieces(); // Opponent pieces that can be taken. Key name of piece of player + position of the piece that can be taken
		Map<String,Position> positions = thePerceptor.getPossiblePositions(); // At these positions
		Map<String, ArrayList<AgamePiece>> attackers = thePerceptor.getAttackers();// Contains opponent pieces that can capture a piece. The key is the name of the piece that they can capture.
		Map<String,AgamePiece> victims = thePerceptor.getThreatenedPieces();// Contains pieces that are threatened by the opponent
		Map<String,Position> threatPositions = thePerceptor.getThreadenedPositions();
		
		List<AgamePiece> opponentPieces = opponent.getMygamePieces();
		List<AgamePiece> myPieces = player.getMygamePieces();
		List<AgamePiece> listPieces = new ArrayList<AgamePiece>(pieces.values());
		if (pieces != null && !pieces.isEmpty()) {// Opponent pieces that can be taken
			for (AgamePiece piece:listPieces) {
				String opposName = piece.getmyPosition().getPositionName();
				for (AgamePiece mypiece:myPieces) {
					String pName = mypiece.getMyPiece().getOntlogyName();
//					myval = mypiece.getMyValue().intValue(); 
					alName = mypiece.getmyPosition().getPositionName();
					String nameKey = pName + opposName;
					AgamePiece oppPiece = pieces.get(nameKey);
					Position pos = positions.get(nameKey);
					if (oppPiece != null) {
						name = mypiece.getMyPiece().getOntlogyName();
//						opval = oppPiece.getMyValue().intValue();
						pieceType = mypiece.getNameType();
						writer.println("Opponent piece found from map key: "+nameKey);
						writer.println("Opponent piece "+oppPiece.toString());
					}
					if (pos != null) {
						posName = pos.getPositionName();
						algebraicKey = alName+"x"+posName;
						writer.println("Opponent piece is at "+pos.getPositionName()+" and map key: "+nameKey);
						String fact = KnowledgeBuilder.getTHREATEN();
						List<AgamePiece> threatpieces = thePerceptor.checkOpponentthreat("x", posName, fact);
						int antThreat = 0;
						if (threatpieces != null) { // If this is the case, find an alternative move !!!
							for (AgamePiece threatpiece:threatpieces) {
								writer.println("This position "+ posName + " is protected by opponent "+threatpiece.getMyPiece().getOntlogyName());
								int oppvalue = threatpiece.getMyValue().intValue();
								antThreat++;
							}

						}
						String pawnfact = KnowledgeBuilder.getPAWNATTACK();
						List<AgamePiece> pawnpieces = thePerceptor.checkOpponentthreat("x", posName, pawnfact);
						if (pawnpieces != null) {
							for (AgamePiece pawnpiece:pawnpieces) {
								writer.println("This position "+ posName + " is under pawn attack from opponent "+pawnpiece.getMyPiece().getOntlogyName());
								int oppvalue = pawnpiece.getMyValue().intValue();
								antThreat++;
							}
						}
						int rank = antThreat;
						paramKey[0]= null;
						paramKey[1] = name;
						paramKey[2] = posName;
						paramKey[3] = pieceType;
						if (pieceType.equals("PAWN")) {
							paramKey[4] = "pawn";
							rank = 0;
						}
						writer.println("Param keys to executable: "+paramKey[0]+" "+paramKey[1]+" "+paramKey[2]+" "+paramKey[3]+" "+paramKey[4]+" Algebraic key "+algebraicKey+ " Rank "+rank);
						writer.flush();
						peas.addExecutable(algebraicKey, rank, paramKey);
	//					flag = true;
					}

				}
			} // End for all my pieces
		} // End opponent pieces that can be taken
		flag = totopval >= myval && flag; // flag is true if still in opening phase
/*		if (!flag && name != null && posName != null) {
			int rank = 0;
			paramKey[0]= null;
			paramKey[1] = name;
			paramKey[2] = posName;
			paramKey[3] = pieceType;
			if (pieceType.equals("PAWN"))
				paramKey[4] = "pawn";
			writer.println("Param keys: "+paramKey[0]+" "+paramKey[1]+" "+paramKey[2]+" "+paramKey[3]+" "+paramKey[4]+" Algebraic key "+algebraicKey);
			writer.flush();
			peas.addExecutable(algebraicKey, rank, paramKey);
		}*/
		return flag;
	}
	/**
	 * checkCastling
	 * This method checks if castling is possible
	 * @return boolean true if castling is possible
	 */
	public boolean checkCastling() {
		String theplayerName = player.getPlayerId();
		String[] castlingKey = new String[5];
		String pieceName = null;
		AgamePiece piece = null;
		boolean canCastle = false;
		String pieceType = null;
		String fact = KnowledgeBuilder.getCASTLE();
/*		writer.println("Checking castling with "+theplayerName+ " and "+playerName);
		writer.flush();*/
		if (theplayerName.equals(playerName)) {
			pieceName = whiteKing;
/*			writer.println("Checking castling player is "+theplayerName+ " and piece "+pieceName);
			writer.flush();*/
			piece = player.getChosenPiece(pieceName);
			pieceType = piece.getNameType();
			HashMap<String,Position> castlePos = piece.getCastlePositions();
			Set<String> posCastle = castlePos.keySet();
			String keyName = null;
			for (String key:posCastle ) {
//				writer.println("Castle position key "+key);
				keyName = key;
				break;
			}
			int thisactivity = piece.getMyMoves().size();
			Position pos = piece.getMyPosition();
			String posName = pos.getPositionName();
			writer.println("Test for castling with "+pieceName+ " Position "+keyName+" Predicate "+fact);
			canCastle = thePerceptor.getFolKb().checkpieceFacts("y", pieceName, keyName, fact);
			if (canCastle && thisactivity <= 0 && posName.equals(kingPos)) {
				writer.println("Castling can take place for "+pieceName+ " to castle position "+castlePos.get(keyName)+" Predicate "+fact+" Flag "+canCastle);
				writer.flush();
				int rank = 0;
//				String algebraicKey = "o-o";
				castlingKey[0]= posName;
				castlingKey[1] = pieceName;
				castlingKey[2] = "e2"; // Should be keyName
				castlingKey[3] = pieceType;
				peas.addExecutable(forCastlingKey, rank, castlingKey);
			}
		}
		return canCastle;
	}
/**
 * testEnd
 * This is the active goal test function
 * It determines whether a given state is a goal state
 * A proposal:
 * Use this function to 
 * fill the otherSchemaList based on parameters as shown 
 * in the PEAS tables 
 * Lifted action schemas are created with parameters in the following order:
 * Startpos, Piecename, Newpos, Piecetype, or null. A fifth parameter "pawn" is added signaling a pawn strike.
 * Lifted action schemas represent goal formulations.
 * Create other objects that implements PlannerState and ChessPlannerAction interfaces.
 * Objects that can be returned with the node
 * Questions:
 * What moves has the player made?
 * What moves has the opponent made?
 * Check the Maps possiblePieces and possiblePositions (thePerceptor)
 * @return true if this is the goal state in the search
 */	
	@Override
	public boolean testEnd(ChessPlannerAction a) {
		List<ActionSchema> schemas = a.getActionSchemas();
		peas = new ThePeas(player,opponent,moveNr);
		int move = moveNr/2;
		boolean flag = false;
		boolean selectFlag = false;
		String newPos = null;
		selectFlag = selectStrategy(); // true if still in opening phase
		boolean castling = checkCastling();
		if (castling) {
			liftedKey = peas.selectExecutable();
			writer.println("Castling: Making a lifted action with: "+liftedKey[0]+" "+liftedKey[1]+" "+liftedKey[2]+" "+liftedKey[3]+" "+liftedKey[4]+" ");
			writer.flush();
			flag = thePerceptor.createLiftedActions(liftedKey);
			selectFlag = true;
		}
//		boolean keyflag = (liftedKey == null || Arrays.stream(liftedKey).allMatch(Objects::isNull));
		if (!selectFlag) { //: SelectStrategy finished opening moves
			liftedKey = peas.selectExecutable();
			newPos = liftedKey[2];
			String fact = KnowledgeBuilder.getTHREATEN();
/*			List<AgamePiece> pieces = thePerceptor.checkOpponentthreat("x", newPos, fact);
			if (pieces != null) {
				for (AgamePiece piece:pieces) {
					writer.println("The position "+ newPos + " is threatened by "+piece.getMyPiece().getOntlogyName());
				}
			}*/
			writer.println("selectStrategy: Making a lifted action with: "+liftedKey[0]+" "+liftedKey[1]+" "+liftedKey[2]+" "+liftedKey[3]+" "+liftedKey[4]+" ");
			writer.flush();
			flag = thePerceptor.createLiftedActions(liftedKey);
		}else if(!castling){
			writer.println("Making a lifted action with: "+notations[move]+" and move number "+move);
			String[] param = peas.selectPerformance(notations[move]);
			newPos = param[2];
			String fact = KnowledgeBuilder.getTHREATEN();
			List<AgamePiece> pieces = thePerceptor.checkOpponentthreat("x", newPos, fact);
			if (pieces != null) { // If this is the case, find an alternative move !!!
				for (AgamePiece piece:pieces) {
					writer.println("The position "+ newPos + " is threatened by "+piece.getMyPiece().getOntlogyName());
				}
			}
			flag = thePerceptor.createLiftedActions(param); // Creates the initial and goal states based on this action schema
			writer.flush();
		}
	
		if (flag) { // If flag is true the lifted action had no solution !!!
			writer.println("Failure: Making a lifted action with: "+notations[7]);
			writer.flush();
			String[]param = peas.selectPerformance(notations[7]);
			flag = thePerceptor.createLiftedActions(param);
		}
		this.otherSchemaList = thePerceptor.getOtherActions(); // The propositionalized action schemas
		return true;
	}

}
