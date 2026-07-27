package no.chess.web.model.game.strategy;

import java.util.HashMap;
import java.util.List;

import aima.core.logic.fol.parsing.ast.Term;
import no.chess.web.model.game.GroundGameAction;
import no.function.FunctionExecutor;

public class TargetMoveExecutor implements FunctionExecutor {
    private String actionId = "";

    private List<GroundGameAction> availableActions;
    
	public TargetMoveExecutor(String actionid, 
			List<GroundGameAction> availableActions) {
		super();
		this.actionId = actionid;
		this.availableActions = availableActions;
	}
	

	public TargetMoveExecutor(List<GroundGameAction> availableActions) {
		super();
		this.availableActions = availableActions;
	}


	@Override
	public Object execute() {
        // INGEN if-setninger, ingen KB-spørringer!
        // Siden vi ble kalt, VET vi at situasjonen er riktig. 
        // Vi finner bare trekket som matcher ordren vår.
        for (GroundGameAction action : availableActions) {
        	String name = action.getActionSchema().getName();
            if (name.equals(actionId)) {
                return action; 
            }
        }
        return null; // Sikkerhetsnett hvis trekket av en eller annen grunn er ulovlig
	}

	@Override
	public void buildTerms(HashMap<String, Term> cParam, HashMap<String, Term> vParam) {
		// TODO Auto-generated method stub

	}


}
