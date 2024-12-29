package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
	private String[] notations = {"d4","c4","Nf3","Nc3","e3","Bd3","Rf1","h3"}; // These are keys for the first three moves. It represent the start strategy
	private APlayer player;
	private APlayer opponent;
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
	 * The method needs to check if several opponent pieces can be taken and the
	 * choose to take the piece with the highest value.
	 * For the first 5 opening moves skip this check unless the opponent has taken a
	 * piece from the player.
	 * @return true if the move number is less than 6.
	 */
	public boolean selectStategy() {
		
		boolean flag = moveNr/2 < 6; // Signals the first 5 opening moves
		String name = null;
		String posName = null;
		String pieceType = null;
		int opval = 0;
		int myval = 0;
		String alName = null; // The algebraic name of a piece
		String algebraicKey = null;
/*		if (flag)
			return flag;*/
		Map<String,AgamePiece> pieces = thePerceptor.getPossiblePieces(); // Opponent pieces that can be taken
		Map<String,Position> positions = thePerceptor.getPossiblePositions(); // At these positions
		
		List<AgamePiece> opponentPieces = opponent.getMygamePieces();
		List<AgamePiece> myPieces = player.getMygamePieces();
		List<AgamePiece> listPieces = new ArrayList<AgamePiece>(pieces.values());
		if (pieces != null && !pieces.isEmpty()) {
			for (AgamePiece piece:myPieces) {
				name = piece.getMyPiece().getOntlogyName();
				int value = piece.getMyPiece().getValue();
				AgamePiece opponentPiece = pieces.get(name);
				alName = piece.getMyPiece().getName().substring(1);
				if (opponentPiece != null) {
					writer.println("Opponent piece found from map key: "+name);
					writer.println(opponentPiece.toString());
					writer.flush();
					break;
				}
			}
			for (AgamePiece piece:opponentPieces) {
				String oppname = piece.getMyPiece().getOntlogyName();
				AgamePiece taken = (AgamePiece)listPieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(oppname)).findAny().orElse(null);
				if (taken != null) {
					opval = taken.getMyValue().intValue();
					writer.println("Opponent piece found "+oppname);
//					writer.println(taken.toString());
					writer.flush();
					break;
				}

			}
		}
		if (positions != null && !positions.isEmpty()) {
			for (AgamePiece piece:myPieces) {
				name = piece.getMyPiece().getOntlogyName();
				pieceType = piece.getNameType();
				Position pos = positions.get(name);
				if (pos != null) {
					posName = pos.getPositionName();
					myval = piece.getMyValue().intValue();
					writer.println("Position found from map key: "+name + " Type of piece " + pieceType);
					writer.println(pos.toString());
					writer.flush();
					algebraicKey = alName+"x"+posName;
					break;
				}
			}
		}
		flag = opval == myval && flag;
		if (!flag && name != null && posName != null) {
			liftedKey[0]= null;
			liftedKey[1] = name;
			liftedKey[2] = posName;
			liftedKey[3] = pieceType;
			if (pieceType.equals("PAWN"))
				liftedKey[4] = "pawn";
			writer.println("Lifted keys: "+liftedKey[0]+" "+liftedKey[1]+" "+liftedKey[2]+" "+liftedKey[3]+" "+liftedKey[4]+" Algebraic key "+algebraicKey);
			writer.flush();
		}
		return flag;
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
 * @return true if this is the goal state
 */	
	@Override
	public boolean testEnd(ChessPlannerAction a) {
		List<ActionSchema> schemas = a.getActionSchemas();
		ThePeas peas = new ThePeas(player,opponent,moveNr);
		int move = moveNr/2;
		boolean flag = false;
		String newPos = null;
		selectStategy();
		boolean keyflag = (liftedKey == null || Arrays.stream(liftedKey).allMatch(Objects::isNull));
		if (!keyflag) {
			newPos = liftedKey[2];
			String fact = KnowledgeBuilder.getTHREATEN();
			List<AgamePiece> pieces = thePerceptor.checkOpponentthreat("x", newPos, fact);
			if (pieces != null) {
				for (AgamePiece piece:pieces) {
					writer.println("The position "+ newPos + " is threatened by "+piece.getMyPiece().getOntlogyName());
				}
			}
			writer.println("Making a lifted action with: "+liftedKey[0]+" "+liftedKey[1]+" "+liftedKey[2]+" "+liftedKey[3]+" "+liftedKey[4]+" ");
			writer.flush();
			flag = thePerceptor.createLiftedActions(liftedKey);
		}else {
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
		this.otherSchemaList = thePerceptor.getOtherActions();
		return true;
	}

}
