package no.chess.web.model.game.strategy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import aima.core.logic.planning.ActionSchema;
import no.chess.web.model.game.APlayer;
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
 * The Action Selecter works with the following priorities:
 * PRIORITET 0 DEFEND_THREATENED Redde ubeskyttede brikker i fare (Unngå bukk)
 * PRIORITET 1 MIDGAME_TACTIC_FORK Skape direkte dobbelttangrep (vinne materiell)
 * PRIORITET 2 MIDGAME_TACTIC_PIN Etablere bindinger mot tunge offiserer
 * PRIORITET 3 DEVELOP_MINOR & PAWN_ENABLER Få uutviklede brikker ut / åpne diagonaler
 * PRIORITET 4 MIDGAME_POSITIONAL Sentrumskontroll, aktive felt, manøvrering
 * MIDGAME_TACTIC_FORK and MIDGAME_TACTIC_PIN are now part of DEFEND_THREATENED (15.10.26)
 * @author olufj
 */
public class ActionSelector {
    private final FunctionContect functionContext;
    private List<GroundGameAction> relevantActions;
    private List<GameAction> actions;
    private ChessFolKnowledgeBase testKB; // The cloned knowledge base after a move
    private GroundGameState gameState;
	private GroundGameAction determinedAction = null; // This action is set when a planning procedure takes place from result(s.a) and GroundGameAction.performAction. (The testEnd procedure)
	private GroundGameAction chosenAction = null;	// This action is current selected action
	private String outputFileName =  "actionselection";
	private PrintWriter writer =  null;
	private FileWriter fw =  null;
	private GroundGameAction defenderAction = null;// Satt dersom en brikke må flyttes fordi den er under angrep 
	
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
		String catalog = KnowledgeBuilder.getFileCatalog();
	    String filename = catalog + outputFileName+".txt";
		try {
			fw = new FileWriter(filename, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));
		writer.println("\nThe Action selector");
	}

	public GroundGameAction getChosenAction() {
		return chosenAction;
	}

	public void setChosenAction(GroundGameAction chosenAction) {
		this.chosenAction = chosenAction;
	}

	public GroundGameAction getDeterminedAction() {
		return determinedAction;
	}

	public void setDeterminedAction(GroundGameAction determinedAction) {
		if(this.determinedAction == null)
			this.determinedAction = determinedAction;
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
    /**
     * registerFunctions
     * This method register executor functions
     */
    public void registerFunctions() {
		MidGamePositional mexec = new MidGamePositional(gameState.getKnowledgeBase(),gameState,actions); //Midgamepositional executor
		String key = mexec.getKey();
		functionContext.register(key, mexec); // Register mid game positional
		APlayer player = gameState.getPlayer();
		APlayer opponent = gameState.getOpponent();
		DevelopMinorExecutor devmexec = new DevelopMinorExecutor(player,opponent,gameState.getKnowledgeBase(),actions); // Develop minor executor
		functionContext.register(devmexec.getMyKey(), devmexec); // Register develop minor
		PawnStructureExecutor pawnStructurexec = new PawnStructureExecutor(gameState.getKnowledgeBase(),actions); //The Pawn structure executor
		functionContext.register(pawnStructurexec.getKey(), pawnStructurexec);// Register pawn structure
		DirectMinorMoveExecutor directExecutor = new DirectMinorMoveExecutor(gameState.getKnowledgeBase(),actions,player.getMygamePieces()); // DirectMinor Move executor IKKE I BRUK????
		functionContext.register(directExecutor.getKey(), directExecutor);
		DefendThreatenedPieceExecutor defenderExecutor = new DefendThreatenedPieceExecutor(gameState); // Defend threatened piece executor
		functionContext.register(defenderExecutor.getMyKey(), defenderExecutor); // Register defender executor
    }
    /**
     * registerFunctions
     * This method register executor functions with a cloned KB
     */
    public void registernewFunctions() {
		MidGamePositional mexec = new MidGamePositional(testKB,gameState,actions); //Midgamepositional executor
		String key = mexec.getKey();
		functionContext.register(key, mexec); // Register mid game positional
		APlayer player = gameState.getPlayer();
		APlayer opponent = gameState.getOpponent();
		DevelopMinorExecutor devmexec = new DevelopMinorExecutor(player,opponent,testKB,actions);// Develop minor executor
		functionContext.register(devmexec.getMyKey(), devmexec); // Register develop minor
		PawnStructureExecutor pawnStructurexec = new PawnStructureExecutor(testKB,actions); //The Pawn structure executor
		functionContext.register(pawnStructurexec.getKey(), pawnStructurexec);// Register pawn structure
		DirectMinorMoveExecutor directExecutor = new DirectMinorMoveExecutor(testKB,actions,player.getMygamePieces());// DirectMinor Move executor IKKE I BRUK????
		functionContext.register(directExecutor.getKey(), directExecutor);
		DefendThreatenedPieceExecutor defenderExecutor = new DefendThreatenedPieceExecutor(gameState);// Defend threatened piece executor
		defenderExecutor.setKb(null);
		defenderExecutor.setKb(testKB); // Med ny KB
		functionContext.register(defenderExecutor.getMyKey(), defenderExecutor); // Register defender executor
    }
 
    /**
     * selectBestAction
     * This method selct the best action to take based on information from the kb
     * The following executors are at present (15.10.26) available:
     * The defend threatened piece executor
     * The develop minor executor
     * The pawn structure executor
     * The midgame positional executor
     * @return - The best available GroundGameAction
     */
    public GroundGameAction selectBestAction() {
        String midGameFork = KnowledgeBuilder.getMidgameTacticFork();
        String midGamePin = KnowledgeBuilder.getMidgameTacticPin();
        String midGamePos = KnowledgeBuilder.getMidgamePositional(); // Finnes
        String devPiece = KnowledgeBuilder.getDevelopPiece(); // Finnes
        String pawnenabler = KnowledgeBuilder.getPawnEnabler(); // Finnes
        String defender = KnowledgeBuilder.getDefendthreatenedpiece(); // Finnes
        GroundGameAction selectedAction = null;
        int maxScore = 0;
        FunctionExecutor defenderExec = functionContext.get(defender);
        DefendThreatenedPieceExecutor defend = (DefendThreatenedPieceExecutor)defenderExec;
        if (defenderExec != null ) {
        	GroundGameAction defenderMove = (GroundGameAction) defenderExec.execute(); 
        	if (defenderMove != null ) {
        		selectedAction = defenderMove;
        		chosenAction = defenderMove;
        		if (chosenAction.getValuePoints()> maxScore)
        			maxScore = chosenAction.getValuePoints();
            	writer.println("DefenderExecutor "+chosenAction.toString()+ "Score "+maxScore);
        	}

        	if(defend.isThreats() && selectedAction != null) {
        		return selectedAction;
        	}
        }
        
        // 1. PRIORITET 1: Taktisk Gaffel (MIDGAME_TACTIC_FORK) OBS: Lagt inn i defender executor (15.10.26)
        FunctionExecutor forkExecutor = functionContext.get(midGameFork);
        if (forkExecutor != null) {
            GroundGameAction forkMove = (GroundGameAction) forkExecutor.execute();
            if (forkMove != null) {
                selectedAction =  forkMove; // Hvis en gaffel er mulig, GJØR DEN!
            }
        }

        // 2. PRIORITET 2: Taktisk Binding (MIDGAME_TACTIC_PIN) OBS: Lagt inn i defender executor (15.10.26)
        FunctionExecutor pinExecutor = functionContext.get(midGamePin);
        if (pinExecutor != null) {
            GroundGameAction pinMove = (GroundGameAction) pinExecutor.execute();
            if (pinMove != null) {
                selectedAction = pinMove; // Hvis en binding er mulig, GJØR DEN!
            }
        }
        FunctionExecutor devPieceExec = functionContext.get(devPiece);
        if (devPieceExec != null) {
        	DevelopMinorExecutor developMinor = (DevelopMinorExecutor)devPieceExec;
        	developMinor.setDeterminedAction(determinedAction);
            GroundGameAction devMove = (GroundGameAction) developMinor.execute();
            if (devMove != null) {
                selectedAction = devMove; // Hvis en brikke ikke er utviklet, GJØR DEN!
         		if (devMove.getValuePoints()> maxScore) {
        			maxScore = devMove.getValuePoints();
                    chosenAction = devMove;
        	       	writer.println("DevelopExecutor "+chosenAction.toString()+ "Score "+maxScore);
 
        		}
            }
        }
        FunctionExecutor pawnEnabler = functionContext.get(pawnenabler);
        if(pawnEnabler != null) {
        	PawnStructureExecutor pawnStructurexec = (PawnStructureExecutor)pawnEnabler;
        	GroundGameAction pawnMove = (GroundGameAction) pawnStructurexec.execute();
        	if(pawnMove != null) {
        		selectedAction = pawnMove;
        		if (pawnMove.getValuePoints()> maxScore) {
        			maxScore = pawnMove.getValuePoints();
                    chosenAction = pawnMove;
        	       	writer.println("PawnExecutor "+chosenAction.toString()+ "Score "+maxScore);
        		}
         	}
        }
        // 3. PRIORITET 3: Posisjonell vurdering (MIDGAME_POSITIONAL)
        FunctionExecutor positionalExecutor = functionContext.get(midGamePos);
        if (positionalExecutor != null && selectedAction == null) {
        	MidGamePositional positionalExec = (MidGamePositional)positionalExecutor;
        	positionalExec.setDeterminedAction(determinedAction);
        	GroundGameAction posMove = (GroundGameAction) positionalExecutor.execute();
            if (posMove != null) {
                selectedAction = posMove; // Velg det posisjonelt beste trekket (sentrum, mobilitet)
        		if (posMove.getValuePoints()> maxScore) {
        			maxScore = posMove.getValuePoints();
                    chosenAction = posMove;
        	       	writer.println("Pos Executor "+chosenAction.toString()+ "Score "+maxScore);
  
        		}
             }
        }
        if(selectedAction == null) {
        	selectedAction =  relevantActions.get(0);
        	chosenAction = selectedAction;
        	writer.println("From relevant actions "+chosenAction.toString());
        }
        
        // Fallback: Hvis ingen executor ga utslag, returner det første lovlige trekket
        writer.flush();
        return chosenAction;
    }
}
