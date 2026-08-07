package no.chess.web.model.game.strategy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import no.chess.web.model.Position;
import no.chess.web.model.game.ApieceMove;
import no.chess.web.model.game.GroundGameAction;
import no.chess.web.model.game.GroundGameState;
import no.chess.web.model.game.KnowledgeBuilder;
import no.games.chess.search.nondeterministic.ChessPlan;

/**
 * ChessPlanCheck
 * This class checks if there is an established plan for the next move
 * It contains a ChessPlan of the form:
 * if S10BlackPawn6_f5 WhitePawn5_e3  then [Action(WhitePawn5_e3)
	PRECOND:^PROTECTEDBY(WhitePawn6,e3)^PROTECTEDBY(WhiteBishop1,e3)^REACHABLE(WhitePawn5,e3)^occupies(WhitePawn5,e2)
	EFFECT:^~occupies(WhitePawn5,e2)^occupies(WhitePawn5,e3)], 
 * 
 * @author oluf
 */
public class ChessPlanCheck {
    private ChessPlan currentPlan = null;
	private String outputFileName = "chessplan";
	private GroundGameState currectState = null; // The initial state for the chessplan 
	private FileWriter fw =  null;
	private PrintWriter writer =  null;	
	
    public ChessPlanCheck() {
		super();
		String catalog = KnowledgeBuilder.getFileCatalog();
	    String filename = catalog + outputFileName+".txt";
		try {
			fw = new FileWriter(filename, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));
	}

	public ChessPlanCheck(ChessPlan currentPlan) {
		super();
		this.currentPlan = currentPlan;
		String catalog = KnowledgeBuilder.getFileCatalog();
	    String filename = catalog + outputFileName+".txt";
		try {
			fw = new FileWriter(filename, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));
	}

	public GroundGameState getCurrectState() {
		return currectState;
	}

	public void setCurrectState(GroundGameState currectState) {
		this.currectState = currectState;
	}

	public ChessPlan getCurrentPlan() {
		return currentPlan;
	}

	public void setCurrentPlan(ChessPlan currentPlan) {
		this.currentPlan = currentPlan;
	}
	public boolean checkPlan() {
		return currentPlan != null && !currentPlan.isEmpty() && currentPlan.size()> 1;
	}
	/**
	 * selectMove
	 * This method checks if the current plan contains a response to the opponent move
	 * @param move - The opponent move
	 * @param noofMoves the number of moves so far
	 * @return - respons action
	 */
	public GroundGameAction selectMove(ApieceMove move, int noofMoves) {
        
        // 1. HAR VI EN EKSISTERENDE PLAN SOM PASSER FOR DENNE TILSTANDEN?
        if (currentPlan != null && !currentPlan.isEmpty() && currentPlan.size()> 1) {
        	String nameofPiece = move.getPiece().getMyPiece().getOntlogyName();
        	Position pos = move.getToPosition();
        	String posName = pos.getPositionName();
        	int step = 1;
        	int lastMove = noofMoves - 2;
        	String stateId = "S"+ Integer.toString(lastMove) + "_" + nameofPiece + "_"+posName;
            // Hent ut hva motstanderen SIKKERT gjorde i forrige tur
 //           GroundGameAction lastOpponentAction = currentState.getLastOpponentAction();
 //           currentPlan.get
            // Sjekk om planen har et definert svar for dette trekket
            GroundGameAction plannedAction = null; //currentPlan.getStepFor(lastOpponentAction);
            ChessPlan plan = currentPlan.getNextChessPlan(step, stateId);
            if(plan != null)
            	plannedAction = (GroundGameAction) plan.getAction(0);
            if (plannedAction != null) {
                // VI HAR EN PLAN! Hent ut trekket og forkort planen for neste steg.
//                currentPlan = currentPlan.getRemainingPlan(lastOpponentAction);
                System.out.println("Følger eksisterende plan: " + plannedAction.toString());
                return plannedAction;
            }
        }

        // 2. INGEN PLAN ELLER PLANEN ER BRUKT OPP -> KJØR NYTT AND-OR-SØK!
        System.out.println("Ingen gjeldende plan. Starter nytt AndOrSearch...");

        // Returner det første trekket i den nye planen
//        return currentPlan.getInitialAction();
        return null;
    }

	public String toString() {
		return currentPlan.toString();
	}
	
}
