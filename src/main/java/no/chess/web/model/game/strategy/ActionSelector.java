package no.chess.web.model.game.strategy;

import java.util.ArrayList;
import java.util.List;

import no.chess.web.model.game.ChessFolKnowledgeBase;
import no.chess.web.model.game.GroundGameAction;
import no.chess.web.model.game.GroundGameState;
import no.chess.web.model.game.KnowledgeBuilder;
import no.function.FunctionContect;
import no.function.FunctionExecutor;
import no.games.chess.search.nondeterministic.GameAction;

/**
 * ActionSelector
 * This object selects the best available action by testing a number of various rules in the kb
 * The action selector is used to find the best move based on a move chosen by the opponent.
 * The opponent move creates a new kb'. 
 * @author olufj
 */
public class ActionSelector {
    private final FunctionContect functionContext;
    private List<GroundGameAction> relevantActions;
    private List<GameAction> actions;
    private ChessFolKnowledgeBase testKB; // The cloned knowledge base after a move
    private GroundGameState gameState;
    
    public ActionSelector(FunctionContect functionContext, List<GroundGameAction> relevantActions,GroundGameState gameState) {
		super();
		this.functionContext = functionContext;
		this.relevantActions = relevantActions;
		this.gameState = gameState;
		actions = new ArrayList<GameAction>();
		for (GroundGameAction action:relevantActions) {
			actions.add(action);
		}
		registerFunctions();
	}

	public ChessFolKnowledgeBase getTestKB() {
		return testKB;
	}

	public void setTestKB(ChessFolKnowledgeBase testKB) {
		this.testKB = testKB;
	}

	public void fillSelectors() {
		for (int i=0;i<KnowledgeBuilder.getTactics().size();i++ ) {
			String key = KnowledgeBuilder.getTactics().get(i);
		}
    }
    public void registerFunctions() {
		MidGamePositional exec = new MidGamePositional(gameState.getKnowledgeBase(),gameState,actions);
		String key = exec.getKey();
		functionContext.register(key, exec);
    }
    /**
     * Velger det mest relevante trekket for Hvit basert på den nye tilstanden s'
     */
    public GroundGameAction selectBestAction() {
        String midGameFork = KnowledgeBuilder.getMidgameTacticFork();
        String midGamePin = KnowledgeBuilder.getMidgameTacticPin();
        String midGamePos = KnowledgeBuilder.getMidgamePositional();
        GroundGameAction selectedAction = null;
        // 1. PRIORITET 1: Taktisk Gaffel (MIDGAME_TACTIC_FORK)
        FunctionExecutor forkExecutor = functionContext.get(midGameFork);
        if (forkExecutor != null) {
            GroundGameAction forkMove = (GroundGameAction) forkExecutor.execute();
            if (forkMove != null) {
                selectedAction =  forkMove; // Hvis en gaffel er mulig, GJØR DEN!
            }
        }

        // 2. PRIORITET 2: Taktisk Binding (MIDGAME_TACTIC_PIN)
        FunctionExecutor pinExecutor = functionContext.get(midGamePin);
        if (pinExecutor != null && selectedAction == null) {
            GroundGameAction pinMove = (GroundGameAction) pinExecutor.execute();
            if (pinMove != null) {
                selectedAction = pinMove; // Hvis en binding er mulig, GJØR DEN!
            }
        }

        // 3. PRIORITET 3: Posisjonell vurdering (MIDGAME_POSITIONAL)
        FunctionExecutor positionalExecutor = functionContext.get(midGamePos);
        if (positionalExecutor != null && selectedAction == null) {
            GroundGameAction posMove = (GroundGameAction) positionalExecutor.execute();
            if (posMove != null) {
                selectedAction = posMove; // Velg det posisjonelt beste trekket (sentrum, mobilitet)
            }
        }
        if(selectedAction == null)
        	selectedAction =  relevantActions.get(0);
        // Fallback: Hvis ingen executor ga utslag, returner det første lovlige trekket
        return selectedAction;
    }
}
