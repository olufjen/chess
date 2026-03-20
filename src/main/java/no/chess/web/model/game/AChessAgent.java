package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import aima.core.agent.Action;
import aima.core.agent.Percept;
import aima.core.logic.fol.Connectors;
import aima.core.logic.fol.domain.FOLDomain;
import aima.core.logic.fol.inference.FOLFCAsk;
import aima.core.logic.fol.inference.InferenceProcedure;
import aima.core.logic.fol.inference.InferenceResult;
import aima.core.logic.fol.inference.InferenceResultPrinter;
import aima.core.logic.fol.inference.proof.Proof;
import aima.core.logic.fol.kb.FOLKnowledgeBase;
import aima.core.logic.fol.kb.data.Literal;
import aima.core.logic.fol.parsing.ast.ConnectedSentence;
import aima.core.logic.fol.parsing.ast.Constant;
import aima.core.logic.fol.parsing.ast.Predicate;
import aima.core.logic.fol.parsing.ast.QuantifiedSentence;
import aima.core.logic.fol.parsing.ast.Term;
import aima.core.logic.fol.parsing.ast.Variable;
import aima.core.logic.planning.ActionSchema;
import aima.core.logic.planning.Problem;
import aima.core.logic.propositional.agent.KBAgent;
import aima.core.logic.propositional.kb.KnowledgeBase;
import aima.core.logic.propositional.kb.data.Clause;
import aima.core.logic.propositional.parsing.ast.AtomicSentence;
import aima.core.logic.propositional.parsing.ast.Sentence;
import aima.core.util.datastructure.Pair;
import no.chess.web.model.PlayGame;
import no.chess.web.model.Position;
import no.games.chess.ChessPieceType;
import no.games.chess.AbstractGamePiece.pieceType;
import no.games.chess.ChessAction;
import no.games.chess.fol.BCGamesAskHandler;
import no.games.chess.fol.FOLGamesBCAsk;
import no.games.chess.fol.FOLGamesFCAsk;
import no.games.chess.planning.ChessProblem;
import no.games.chess.planning.ChessSearchAlgorithm;

/**
 * The Chess Agent is both a utility based agent, a goal based agent and a model based agent.
 * (For definition see p. 52 and p. 53.)
 * This is a Knowledgebase agent derived from the generic knowledgebase agent of AIMA chapter 7.
 * It is further adapted for the FOL knowledge base as described in chapter 10 and 11.
 * It is created every time the PlayGame object makes a move.
 * The state of the chess game implements the Percept interface.
 * The ChessAction interface extends the AIMA Action interface.
 * KBAgent is an abstract class extending the AbstractAgent class 
 * 
 * The agent main purpose is to choose the best action from the list of available actions.
 * All the available chess actions are held in the ChessState object (the Percept)
 * So the agent program performs a mapping from the Percept to an action.
 * For this purpose the agent must find:
 * If the chosen action has a movement
 * The number of moves so far
 * The preferred position
 * If preferred position is occupied by opponent
 * If the opponent piece is protected
 * If the preferred position is a center position
 * @since 05.01.26
 * Creating more rules to the knowledge base
 * @author oluf
 *
 */
public class AChessAgent extends KBAgent {

	private ChessStateImpl stateImpl = null;
	private ChessActionImpl localAction = null;
	private ChessKnowledgeBase kb = null;
/**
 * A first order knowledge base
 */
	private ChessFolKnowledgeBase folKb;
	private ChessFolKnowledgeBase strategyKB = null;
/**
 * ChessDomain:
 *  All pieces are constants
 *  all positions are constants
 */
	private ChessDomain chessDomain;
	private FOLGamesFCAsk forwardChain;
	private FOLGamesBCAsk backwardChain;
	private InferenceProcedure infp;
	private List<ChessAction> allActions = null;
	private List <ChessActionImpl> actions = null;
	private List <ChessActionImpl> opponentActions = null;
	private String knowledgeFilename = "knowledgebase.txt";
	private String outputFileName = "chessAgent.txt";
	private PrintWriter writer = null;
	private FileWriter fw = null;
	private PlayGame game = null; // The PlayGame object
	private APlayer myPlayer = null;
	private APlayer opponent = null;
	private List<Position> emptyPositions = null;
	private List<Position> positionList = null; // The original HashMap of positions as a list
	private HashMap<String,Position> positions; // The original HashMap of positions
	private List<String> opponentPieces = null;
	private AChessProblemSolver solver = null;
	private ChessSearchAlgorithm chessSearch = null;
	private int noofMoves = 0; // settes fra game.getMovements().size() fra execute function
	
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
	private String PIECE = "PIECE"; 
	private String PAWNMOVE;
	private String playerName = "";
    private String OCCUPIES = "";
    private String PAWNATTACK ="";
    private String BOARD;
    private String PLAYER;
    private String CASTLE;
    private String OPPONENTTO;
    private String POSSIBLETHREAT;
    private String POSSIBLEPROTECT; // All available positions for a piece are possibly protected by that piece
    private String POSSIBLEREACH; // All available positions for a piece are possibly reachable by that piece
    private String CENTERSQUARE;
    private String HOMESQUARE;
    private String MINORPIECE;
    private String CONTROLCENTER;
    /* 
     * Added 23.02.26 Possible types of piecemoves
     */
    private String KNIGHTMOVE = "KNIGHTMOVE";
    private String BISHOPMOVE = "BISHOPMOVE";
    private String ROOKMOVE = "ROOKMOVE";
    private String KINGMOVE = "KINGMOVE";
    private String QUEENMOVE = "QUEENMOVE";
    private String MINORMOVE = "MINORMOVE";    
    private String DEVELOPED = "DEVELOPED"; // A piece is developed when it has made a move
    private ChessActionImpl castleAction = null;
    
	public AChessAgent(KnowledgeBase kb) {
		super(kb);
		
	}

	
	public AChessAgent(ChessKnowledgeBase KB, ChessActionImpl localAction) {
		super(KB);
		String catalog = KnowledgeBuilder.getFileCatalog();
		outputFileName = catalog+outputFileName;
		this.kb = (ChessKnowledgeBase)KB;
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
		this.localAction = localAction;
		actions = new ArrayList<ChessActionImpl>();
		opponentPieces = new ArrayList<String>();
		positionList = game.getPositionlist();
		for (Position pos:positionList) {
			pos.setFriendlyPosition(false);
		}
		chessDomain = new ChessDomain();
		setPredicatenames();
	}


	/**
	 * This is constructor used by PlayGame
	 * @param KB
	 * @param localAction At present null
	 * @param game
	 */
	public AChessAgent(ChessKnowledgeBase KB,ChessActionImpl localAction,PlayGame game) {
		super(KB);
		String catalog = KnowledgeBuilder.getFileCatalog();
		outputFileName = catalog+outputFileName;
		this.game = game;
		this.kb = (ChessKnowledgeBase)KB;
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
		this.localAction = localAction;
		actions = new ArrayList<ChessActionImpl>();
		allActions = new ArrayList<ChessAction>();
		opponentPieces = new ArrayList<String>();
		positionList = game.getPositionlist();

		for (Position pos:positionList) {
			pos.setFriendlyPosition(false);
		}
		positions = game.getPositions();
		chessDomain = new ChessDomain();
		setPredicatenames();
		
	}
	
	public String getDEVELOPED() {
		return DEVELOPED;
	}


	public void setDEVELOPED(String dEVELOPED) {
		DEVELOPED = dEVELOPED;
	}


	public void setPredicatenames() {
		  KnowledgeBuilder.generatePieceTypePreds();
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
			PIECE = KnowledgeBuilder.getPIECE();
			OCCUPIES = KnowledgeBuilder.getOCCUPIES();
			PAWNMOVE = KnowledgeBuilder.getPAWNMOVE();
			PAWNATTACK = KnowledgeBuilder.getPAWNATTACK();
			BOARD = KnowledgeBuilder.getBOARD();
			PLAYER = KnowledgeBuilder.getPLAYER();
			CASTLE = KnowledgeBuilder.getCASTLE();
			OPPONENTTO = KnowledgeBuilder.getOPPONENTTO();
			POSSIBLETHREAT = KnowledgeBuilder.getPOSSIBLETHREAT();
			POSSIBLEPROTECT = KnowledgeBuilder.getPOSSIBLEPROTECT();
			POSSIBLEREACH = KnowledgeBuilder.getPOSSIBLEREACH();
			
			HOMESQUARE = KnowledgeBuilder.getHOMESQUARE();
			MINORPIECE = KnowledgeBuilder.getMINORPIECE();
			CENTERSQUARE = KnowledgeBuilder.getCENTERSQUARE();
			CONTROLCENTER = KnowledgeBuilder.getCONTROLCENTER();
			
			KNIGHTMOVE = KnowledgeBuilder.getKNIGHTMOVE();
			BISHOPMOVE = KnowledgeBuilder.getBISHOPMOVE();
			ROOKMOVE = KnowledgeBuilder.getROOKMOVE();
			KINGMOVE = KnowledgeBuilder.getKINGMOVE();
			QUEENMOVE = KnowledgeBuilder.getQUEENMOVE();
			MINORMOVE = KnowledgeBuilder.getMINORMOVE();
			DEVELOPED = KnowledgeBuilder.getDEVELOPED();
			
			chessDomain.addPredicate(DEVELOPED);
			chessDomain.addPredicate(KNIGHTMOVE);
			chessDomain.addPredicate(BISHOPMOVE);
			chessDomain.addPredicate(ROOKMOVE);
			chessDomain.addPredicate(KINGMOVE);
			chessDomain.addPredicate(QUEENMOVE);
			chessDomain.addPredicate(MINORMOVE);
			chessDomain.addPredicate(PAWNMOVE);
			chessDomain.addPredicate(CONTROLCENTER);
			chessDomain.addPredicate(CENTERSQUARE);
			chessDomain.addPredicate(MINORPIECE);
			chessDomain.addPredicate(HOMESQUARE);
			chessDomain.addPredicate(OPPONENTTO);
			chessDomain.addPredicate(POSSIBLETHREAT);
			chessDomain.addPredicate(POSSIBLEPROTECT);
			chessDomain.addPredicate(POSSIBLEREACH);
			chessDomain.addPredicate(PROTECTED);
			chessDomain.addPredicate(MOVE);
			chessDomain.addPredicate(ACTION);
			chessDomain.addPredicate(ATTACKED);
			chessDomain.addPredicate(CANMOVE);
			chessDomain.addPredicate(CAPTURE);
			chessDomain.addPredicate(CONQUER);
			chessDomain.addPredicate(OWNER);
			chessDomain.addPredicate(REACHABLE);
			chessDomain.addPredicate(SAFEMOVE);
			chessDomain.addPredicate(PROTECTED);
			chessDomain.addPredicate(STRIKE);
			chessDomain.addPredicate(THREATEN);
			chessDomain.addPredicate(simpleProtected);
			chessDomain.addPredicate(PIECETYPE);
			chessDomain.addPredicate(PLAY);
			chessDomain.addPredicate(PAWNATTACK);
			chessDomain.addPredicate(PLAYER);
			chessDomain.addPredicate(BOARD);
			chessDomain.addPredicate(CASTLE);
			chessDomain.addPredicate(KING);
			chessDomain.addPredicate(QUEEN);
			chessDomain.addPredicate(PIECE);
			chessDomain.addPredicate(ROOK);
			chessDomain.addPredicate(PAWN);
			chessDomain.addPredicate(BISHOP);
			chessDomain.addPredicate(KNIGHT);

	  }
	  

	public ChessFolKnowledgeBase getStrategyKB() {
		return strategyKB;
	}


	public void setStrategyKB(ChessFolKnowledgeBase strategyKB) {
		this.strategyKB = strategyKB;
	}


	public ChessFolKnowledgeBase getFolKb() {
		return folKb;
	}


	public void setFolKb(ChessFolKnowledgeBase folKb) {
		this.folKb = folKb;
	}


	public ChessActionImpl getCastleAction() {
		return castleAction;
	}


	public String getPAWNATTACK() {
		return PAWNATTACK;
	}


	public void setPAWNATTACK(String pAWNATTACK) {
		PAWNATTACK = pAWNATTACK;
	}


	public void setCastleAction(ChessActionImpl castleAction) {
		this.castleAction = castleAction;
	}
	public void clearFriends(APlayer player) {
		List<AgamePiece>pieces = player.getMygamePieces();
		for (AgamePiece piece:pieces) {
			piece.clearfriendPositions();
		}
	}
	/**
	 * execute
	 * The execute function creates the necessary inference procedures and the parent knowledge base based on the current percept: The ChessState object.
	 * Once the knowledge base is in place, a Problemsolver is created.
	 * It is called from the PlayGame object proposeMove method
	 * (See the chess problem solver).  
	 */
	/* (non-Javadoc)
	 * @see aima.core.logic.propositional.agent.KBAgent#execute(aima.core.agent.Percept)
	 */
	@Override
	public Action execute(Percept state) {
		stateImpl = (ChessStateImpl)state; // The aima.core.Percept interface is an empty interface
		myPlayer = stateImpl.getMyPlayer();
		opponent = stateImpl.getOpponent();
		allActions = stateImpl.getActions(); // creates new actions !!!
		
		for (ChessAction action:allActions) { //*** Added 23.05.25 olj
			ChessActionImpl localAction =(ChessActionImpl) action;
			actions.add(localAction);
		}
//		actions.add((ChessActionImpl) allActions); //*** Added 23.05.25 olj
		myPlayer.setActions(allActions);
//		kb.setStateImpl(stateImpl);
		
		forwardChain = new FOLGamesFCAsk(); // A Forward Chain inference procedure see p. 332
		backwardChain = new FOLGamesBCAsk(); // A backward Chain inference procedure see p. 337
//		folKb = new FOLKnowledgeBase(chessDomain);
		folKb = new ChessFolKnowledgeBase(chessDomain, forwardChain,knowledgeFilename);
		folKb.setBackWardChain(backwardChain);
		String[] cpos = KnowledgeBuilder.getCentre();
		List<Term> centerTerms = new ArrayList<Term>();
		List<Constant> centerConstants = new ArrayList<Constant>();
		for (Position pos:positionList) {
			for(String p:cpos) {
				if (p.equals(pos.getPositionName())) {
					Constant ct = new Constant(p);
					centerConstants.add(ct);
					centerTerms.addAll(centerConstants);
					Predicate centerPred = new Predicate(CENTERSQUARE,centerTerms);
					folKb.tell(centerPred);
				}
				centerConstants.clear();
				centerTerms.clear();
			}
		}
		noofMoves = game.getMovements().size();
		String playerName = stateImpl.getMyPlayer().getNameOfplayer();

		makeRules(myPlayer,"g1","c1"); // tells the FOL knowledgebase rules about how to capture opponent pieces
		for (Position pos:positionList) {
			pos.setFriendlyPosition(false);
		}

		// 04.11.21 This is done to create opponent friendly positions
		opponentActions = this.stateImpl.getActions(opponent); // These are the opponent's actions
		
		// OBS uses white friendly positions: !!!!
		makeRules(opponent,"g8","c8"); // tells the FOL knowledgebase rules about how to capture opponent pieces
		setOpponentpieces(opponent);//creates knowledge about the opponent and its pieces to the first order knowledge base and its domain
		clearFriends(myPlayer);
		clearFriends(opponent);
		HashSet<String> chessConstants = (HashSet<String>) chessDomain.getConstants();
		HashSet<String> chessPredicates = (HashSet<String>) chessDomain.getPredicates();
		String sample = "WhitePawn1";
		List<String> chessConstant = chessConstants.stream().collect(Collectors.toList());
		List<String> chesspredicateList = chessPredicates.stream().collect(Collectors.toList());
/*		writer.println("The chessdomain constants");
		for (String c:chessConstant) {
			writer.println(c);
		}
		writer.println("The chessdomain predicates");
		for (String c:chesspredicateList) {
			writer.println(c);
		}*/
		String pieceConstant = chessConstants.stream().filter(sample::equals).findAny().orElse(null);
		String chessPr = chesspredicateList.get(0);
		
		InferenceResult result = null;
		InferenceResult fcResult = null;
		Variable posVariable = null;
		Variable pieceVariable = null;
		Variable newPosition = null;
		List<Term> terms = new ArrayList<Term>();
		List<Variable> variables = new ArrayList<Variable>();
		List<InferenceResult>results = new ArrayList<InferenceResult>();
		pieceVariable= new Variable("x");
		posVariable = new Variable("y");
		newPosition = new Variable("z");
		
		variables.add(pieceVariable);
		variables.add(posVariable);
		terms.add(pieceVariable);
		terms.add(posVariable);
		
		Predicate folPredicate = new Predicate(chessPr,terms);  // The folPredicate is POSSIBLEPROTECT
//		QuantifiedSentence qSentence = new QuantifiedSentence("FORALL",variables,folPredicate); // A single sentence including FORALL
//		folKb.tell(qSentence); // Returned to code 28.02.23 REplaced, see createConnected
//		String asentence = "FORALL a b ((REACHABLE(a,b) AND CENTERSQUARE(b)) => CONTROLCENTER(a,b))";
//		createConnected("player", "y", "x"); Not to be used. The defineRules method is used instead
/*
 * Create all types of move to the knowledge base		
 */
		createFact(MINORMOVE,2);
		createFact(KNIGHTMOVE,2);
		createFact(BISHOPMOVE,2);
		createFact(ROOKMOVE,2);
		createFact(KINGMOVE,2);
		createFact(QUEENMOVE,2);
		createFact(PAWNMOVE,2);
		createFact(MOVE, 2);
		createFact(CONTROLCENTER,2);
//		These calls create the unary relation of type BISHOP(piecename) etc. for all the player's pieces
		makeSentences(stateImpl.getMyPlayer()); // 
		makeSentences(stateImpl.getOpponent()); // 
//		defineRules("FORALL", 1,PIECETYPE,MINORPIECE);
		defineRules("FORALL", 3,REACHABLE,PROTECTED,MINORMOVE);	
		defineRules("FORALL",2,REACHABLE,CENTERSQUARE,CONTROLCENTER);
		defineRules("FORALL",2,REACHABLE,PAWN,CENTERSQUARE,PAWNMOVE);
		defineRules("FORALL",2,REACHABLE,PAWN,PAWNMOVE); // Pawn move
//		AnswerHandler res = folKb.ask(asentence);
//		String s1 = "occupies(o,px)^occupies(pi,py)";
//		String s2 = "MOVE(pi,pz)";
	//	KnowledgeBuilder.parseSentence(s1,s2,folKb);
		checkKb("CONTROLCENTER(a,b)");
		checkKb("MINORMOVE(a,b)");
		checkKb("PAWNMOVE(a,b)");
/*
 * Create statistics before the move. It creates information about developed pieces.		
 */
		game.moveStatistics(stateImpl.getMyPlayer());
		game.moveStatistics(stateImpl.getOpponent());		
		Literal chessLiteral = new Literal(folPredicate);
		List<Literal> allLiterals = new ArrayList<Literal>();
		allLiterals.add(chessLiteral);
		LinkedHashSet<Map<Variable, Term>> mapResult = (LinkedHashSet<Map<Variable, Term>>) folKb.fetch(allLiterals);
		List<Map<Variable, Term>> listResults = mapResult.stream().collect(Collectors.toList());
		results.add(result);
	
/*
 * To move this into makePerceptSentence ?		
 */
		int oppmoves = stateImpl.getOpponent().getMyMoves().size() -1;
		int mymoves = stateImpl.getMyPlayer().getMyMoves().size() -1;
	
		if (oppmoves < 0)
			oppmoves = 0;
		if (mymoves <0)
			mymoves = 0;
//		kb.tell("AFACT");
		emptyPositions = game.getNotusedPositionlist();

//		makeOpponentsentences(stateImpl.getOpponent(),noofMoves); //knowledge about the opponent and its pieces to the proportional knowledge base knowledge base
		makeSentences(stateImpl.getMyPlayer()); // 
		makeSentences(stateImpl.getOpponent()); // 
		solver = new AChessProblemSolver(stateImpl, localAction, folKb, chessDomain, forwardChain, backwardChain, game, myPlayer, opponent);
		solver.setPositionList(positionList);
/*
 * End move ?
 */
/*
 * Here we must ask the knowledge base what is the best action to perform:		
 */
		String movnr = Integer.toString(noofMoves);
		ChessActionImpl naction = null;
		/*
		 * Returns a problem containing an initial and goal state, and a set of Action Schemas.
		 */
		ChessProblem problem = solver.planProblem((ArrayList<ChessActionImpl>) actions);
		List<AgamePiece>  pieces = myPlayer.getMygamePieces();
		if (problem != null) {
			chessSearch = new ChessSearchAlgorithm(fw,writer);
//			List<List<ActionSchema>> solution = solver.solveProblem(localAction);
			List<ActionSchema> actionSchemas = chessSearch.heirarchicalSearch(problem);
			writer.println("No of action schemas: "+actionSchemas.size()+"\n");
/*
 * The hiearchical search returns three actionschemas.
 * The two first are identical, the third one is the original HLA
 * If the hiearchical search returns a list of seperate actions then perform these actions in steps?
 * Must wait for the opponent move, first !! See section 11.2.2 p. 408	 		
 */
			ActionSchema actionSchema = actionSchemas.get(0);
			List<Constant>solConstants = actionSchema.getConstants();

			AgamePiece gpiece = null;
			writer.println("The constants of the action schema");
			String aName = null; // Name to be used to find the chessAction that the actionSchema corresponds to
			for (Constant constant:solConstants) {
				writer.println(constant.getSymbolicName());
				String symName = constant.getSymbolicName();
				gpiece =  (AgamePiece) pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().contains(symName)).findAny().orElse(null);
				if (gpiece != null) {
					aName = symName;
					break;
				}
			}
//			folKb.createsinglefacts(DEVELOPED, aName);  Removed 8.03.26 - see movestatistics
			String nactionName = actionSchema.getName();
			String chessName = null;
			int nIndex = nactionName.indexOf("_");
			if (nIndex != -1)
				chessName = nactionName.substring(0, nIndex);
			else
				chessName = aName;
			String newName = chessName; // Name to be used to find the chessAction that the actionSchema corresponds to
			naction =  (ChessActionImpl) actions.stream().filter(c -> c.getActionName().equals(newName)).findAny().orElse(null);
			Position altPos = null;
			List<Literal>effects = actionSchema.getEffects(); // Find alt. new position
			for (Literal literal:effects) {
				aima.core.logic.fol.parsing.ast.AtomicSentence sentence = literal.getAtomicSentence();
				List<Term> effectterms = sentence.getArgs();
				String posname = "";
				Position  toPos = null;
				for (Term effectTerm:effectterms) {
					posname = effectTerm.getSymbolicName();
					toPos = positions.get(posname);
					if (toPos != null) {
						altPos = toPos;
						break;
					}
				}

//				Position  toPos = (Position) positionList.stream().filter(c -> c.getPositionName().contains(posname)).findAny().orElse(null);
			}
			if (altPos != null) {
				naction.setPreferredPosition(altPos);
			}
		}

		for (ChessActionImpl action:actions) {
			if (action.getActionValue() == null) {
//				action.setActionValue(new Integer(0));
				action.setActionValue(Integer.valueOf(0));
			}
		}
/*		List<Integer> sortedActions = actions.stream().sorted(Comparator.comparing(ChessActionImpl::getActionValue)).map(ChessActionImpl::getActionValue)
        .collect(Collectors.toList());*/
		
//		int topValue = sortedActions.get(sortedActions.size()-1).intValue();
/*		for (ChessActionImpl action:actions) {
			if(action.getPossibleMove() != null) {
				localAction = action;
				break;
			}
		}*/
		StringBuilder builder = new StringBuilder();

		castleAction = solver.getCastleAction();
//		writer.println("The first order knowledge base");
//		writer.println(folKb.toString());

//		folKb.writeKnowledgebase(); Moved to end of statistics
		chessDomain.printDomain();
		strategyKB =  solver.getOpponentAgent().getLocalKb();
		strategyKB.writeKnowledgebase();
		writer.flush();
		if (naction != null)
			localAction = naction;
		return localAction;

	}
	public void checkKb(String query) {
		writer.println("The query is "+query);
		List<String> answer = folKb.checkQuery(query);
//		List <String> forwardanswer = folKb.forwardcheckQuery(query);
		for (String p: answer) {
			writer.println("The backward chain object is "+p);
		}
		/*
		 * for (String p: forwardanswer) {
		 * writer.println("The forward chain object is "+p); }
		 */
	}
	/**
	 * createFact
	 * THis method creates a single fact to the fol knowledge base with type variables
	 * @param fact - name of fact
	 * @param antvar - no of variables
	 */
	public void createFact(String fact, int antvar) {
		String[] pvar = {"a","b","c","d","e"}; // Max 5 variables
		List<Term> myTerms = new ArrayList<Term>();
		List<Variable> variables = new ArrayList<Variable>();
		for (int i=0;i<antvar;i++) {
			Variable var = new Variable(pvar[i]);
			variables.add(var);
		}
		myTerms.addAll(variables);
		Predicate pred = new Predicate(fact,myTerms);
		folKb.tell(pred);
	}
	
	/**
	 * updateKnowledge
	 * This method updates the knowledge base after the latest move.
	 * But first we must empty the knowledge base!
	 */
	public void updateKnowledge() {
		folKb.clear(); // Empty the knowledge base
		actions = stateImpl.getActions(); // creates new actions !!!
		opponentActions = this.stateImpl.getActions(opponent); // These are the opponent's actions
		noofMoves = game.getMovements().size();
		String playerName = stateImpl.getMyPlayer().getNameOfplayer();
		makeRules(myPlayer,"g1","c1"); // tells the FOL knowledgebase rules about how to capture opponent pieces
		for (Position pos:positionList) {
			pos.setFriendlyPosition(false);
		}
		makeRules(opponent,"g8","c8"); // tells the FOL knowledgebase rules about how to capture opponent pieces
		setOpponentpieces(opponent);//creates knowledge about the opponent and its pieces to the first order knowledge base and its domain
		clearFriends(myPlayer);
		clearFriends(opponent);
//		createConnected("player", "y", "x");
		emptyPositions = game.getNotusedPositionlist();
		makeSentences(stateImpl.getMyPlayer()); //
		makeSentences(stateImpl.getOpponent()); //
	}
	/**
	 * defineRules
	 * This method defines rules to the parent knowledge base
	 * The piecetype predicate defines which variables are used in each defined predicate!!
	 * @param qualifier  - The qualifier for the rule 
	 * @param antVars - The number of variables for the rule - max 5
	 * @param preds - The list of predicates for the rule. The last predicate is the implies predicate
	 */
	public void defineRules(String qualifier,int antVars,String...preds) {
		String[] pvar = {"a","b","c","d","e"}; // Max 5 variables
		List<Term> predTerms = new ArrayList<Term>();
		List<Variable> variables = new ArrayList<Variable>(); // Contains the number of Variables given by antVars
		List<Predicate> predicates = new ArrayList<Predicate>();
		int antPreds = preds.length;
		List<Integer> antParam = new ArrayList<Integer>();
		
		for(String pr:preds) {
			int ant = folKb.checkNumberofparams(pr);
			Integer antP = Integer.valueOf(ant);
			antParam.add(antP);
		}
		boolean single = antPreds<=2;
		for (int i=0;i<antVars;i++) {
			Variable varx = new Variable(pvar[i]);
			variables.add(varx);
		}
//		predTerms.addAll(variables);
		ConnectedSentence firstSentence = null;
		ConnectedSentence finalSentence = null;
		ConnectedSentence goal = null;
		for(int i=0;i<antPreds;i++) {
			String pr = preds[i];
			int t = antParam.get(i).intValue();
			boolean tpred = KnowledgeBuilder.checkpieceTypepred(pr); // What type of predicate?
//			int diff = antVars-t;
			if (i==0 || i==antPreds-1) {
				for (int n=0;n<t;n++) {
					if (tpred)
						predTerms.add(variables.get(0));
					else
						predTerms.add(variables.get(n));
				}
			}else {
				for (int n=t;n>0;n--) {
					if (tpred)
						predTerms.add(variables.get(0));
					else
						predTerms.add(variables.get(n));
				}
			}
			Predicate pred = new Predicate(preds[i],predTerms);
			predicates.add(pred);
			predTerms.clear();
		}
		if(!single && antPreds == 3)
			firstSentence = new ConnectedSentence(Connectors.AND,predicates.getFirst(),predicates.get(1));
		if(!single && antPreds > 3) {
			Predicate lastPred = predicates.getLast();
			ConnectedSentence next = null;
			List<ConnectedSentence> sentences = new ArrayList<ConnectedSentence>();
//			firstSentence = new ConnectedSentence(Connectors.AND,predicates.getFirst(),predicates.get(1));
			for (int i = 0;i<antPreds;i=i+2) {
				if (predicates.get(i+1) == lastPred)
					break;
				next = new ConnectedSentence(Connectors.AND,predicates.get(i),predicates.get(i+1));
				sentences.add(next);
			}
			
			int cn = sentences.size();
			if (cn == 1) {
				finalSentence = new ConnectedSentence(Connectors.AND,sentences.get(0),predicates.get(2));
			}
			else {
				for(int i = 0;i<cn;i=i+2) {  // OBS ????
					finalSentence = new ConnectedSentence(Connectors.AND,sentences.get(i),sentences.get(i+1));
				}
			}

		}
		if(single)
			goal = new ConnectedSentence(Connectors.IMPLIES,predicates.getFirst(),predicates.getLast());
		else if (antPreds == 3)
			goal = new ConnectedSentence(Connectors.IMPLIES,firstSentence,predicates.getLast());
		else if  (antPreds > 3) {
			goal = new ConnectedSentence(Connectors.IMPLIES,finalSentence,predicates.getLast());	
		}
		QuantifiedSentence qSentence = new QuantifiedSentence(qualifier,variables,goal); // A single qualified sentence including FORALL
		folKb.tell(qSentence);
	}
	/**
	 * createConnected
	 * This method creates rules to the fol knowledge base 
	 * These rules are of the form:
	 * ((REACHABLE(x,y) AND occupies(enemy_piece, y)) => ATTACK_MOVE(x,y))
	 * @since 18.05.21
	 * The Owner predicate is removed from the rules.
	 * @since 07.01.26
	 * This function is enhanced to be able to create any type of quantified rule.
	 * 31.01.26 must be reworked or abandoned
	 * @param name
	 * @param pos
	 * @param piece

	 */
	public void createConnected(String name, String pos, String piece) {
		List<Term> ownerTerms = new ArrayList<Term>();
		Variable ownerVariable = new Variable(name);
		Variable pieceVariable= new Variable(piece);
		Variable otherPiece = new Variable("p");
		Variable posVariable = new Variable(pos);
		Variable newPosition = new Variable("z");
		List<Variable> variables = new ArrayList<Variable>();
		variables.add(pieceVariable);
		variables.add(posVariable);
		variables.add(newPosition);
		variables.add(otherPiece);
		ownerTerms.add(ownerVariable);
		ownerTerms.add(pieceVariable);
		Predicate ownerPredicate = new Predicate(OWNER,ownerTerms);
		List<Term> reachableTerms = new ArrayList<Term>();
		reachableTerms.add(pieceVariable);
		reachableTerms.add(posVariable);
		List<Term> protectedTerms = new ArrayList<Term>();
		protectedTerms.add(otherPiece);
		protectedTerms.add(posVariable);
		Predicate reachablePredicate = new Predicate(REACHABLE,reachableTerms);
		Predicate protectedPredicate = new Predicate(PROTECTED,protectedTerms);
		List<Term> moveTerms = new ArrayList<Term>();
		moveTerms.add(pieceVariable);
		moveTerms.add(posVariable);
		Predicate movePredicate = new Predicate(CANMOVE,moveTerms);
		Predicate move = new Predicate(MOVE,moveTerms);
//		ConnectedSentence reachableSentence = new ConnectedSentence(Connectors.AND,ownerPredicate,reachablePredicate);
		ConnectedSentence reachableandprotectedSentence = new ConnectedSentence(Connectors.AND,reachablePredicate,protectedPredicate);
		Predicate safemovePredicate = new Predicate(SAFEMOVE,moveTerms);
		ConnectedSentence goal = new ConnectedSentence(Connectors.IMPLIES,reachableandprotectedSentence,move);
		QuantifiedSentence qSentence = new QuantifiedSentence("FORALL",variables,goal); // A single qualified sentence including FORALL
/*		ConnectedSentence protectedGoal = new ConnectedSentence(Connectors.IMPLIES,goal,safemovePredicate);
		ConnectedSentence canAndSafe = new ConnectedSentence(Connectors.AND,movePredicate,safemovePredicate);
		ConnectedSentence safeTomove = new ConnectedSentence(Connectors.IMPLIES,canAndSafe,move);*/
		folKb.tell(qSentence);
//		folKb.tell(protectedGoal);
//		folKb.tell(safeTomove);
		List<Term> typeTerms = new ArrayList<Term>();
		Constant pieceType = new Constant(PAWN);
		typeTerms.add(pieceVariable);
		typeTerms.add(pieceType);
		Predicate typePredicate = new Predicate(PIECETYPE,typeTerms);
		Predicate pawnMove = new Predicate(PAWNMOVE,reachableTerms);
//		ConnectedSentence pawnSentence = new ConnectedSentence(Connectors.AND,ownerPredicate,typePredicate);
		ConnectedSentence pawnReachable = new ConnectedSentence(Connectors.AND,typePredicate,reachablePredicate); // *** Add FORALL ****
		ConnectedSentence pawnmove = new ConnectedSentence(Connectors.IMPLIES,pawnReachable,pawnMove);
		QuantifiedSentence qpawn = new QuantifiedSentence("FORALL",variables,pawnmove); // A single qualified sentence including FORALL
		folKb.tell(qpawn);

		}
	/**
	 * askMove
	 * This method asks the inference procedure which moves are available
	 * Given a ChessAction it tries to prove that the action has a move available in the FOL knowledge base
	 * @since 01.12.20:
	 * Reworked this method takes the piece from the current action
	 * @param player
	 */
	public void askMove(APlayer player,ChessActionImpl action) {
		List<AgamePiece> pieces = player.getMygamePieces();
		
		String playername = player.getNameOfplayer();
		AgamePiece piece = action.getChessPiece();
		String name = action.getChessPiece().getMyPiece().getOntlogyName(); 
		Constant ownerVariable = new Constant(playername);
		List<Position> availablePositions = piece.getNewlistPositions();
		if (availablePositions != null && !availablePositions.isEmpty()) {
			for (Position pos:availablePositions){
				if(!piece.checkRemoved(pos)) {
					List<Term> ownerTerms = new ArrayList<Term>();
					String position = pos.getPositionName();
					Constant pieceVariable = new Constant(name);
					ownerTerms.add(ownerVariable);
					ownerTerms.add(pieceVariable);
					Constant posVariable = new Constant(position);
					List<Term> moveTerms = new ArrayList<Term>();
					moveTerms.add(pieceVariable);
					moveTerms.add(posVariable);
					Predicate movePredicate = new Predicate(MOVE,moveTerms);
					writer.println("Trying to prove\n"+movePredicate.toString());
					InferenceResult result = forwardChain.ask(folKb,movePredicate);
	//				writer.println(InferenceResultPrinter.printInferenceResult(result));
					writer.println("Trying to prove backward chaining\n"+movePredicate.toString());
					InferenceResult backWardresult =  backwardChain.ask(folKb, movePredicate);
					backWardresult.isTrue();
					
					BCGamesAskHandler bcHandler = (BCGamesAskHandler) backWardresult;
//					writer.println(bcHandler.toString());
//					writer.println(InferenceResultPrinter.printInferenceResult(backWardresult));
					
				}
			}
		}
		

	}
	/**
	 * makeRules
	 * This method tells the FOL knowledgebase rules about how to capture opponent pieces
	 * It also tells the FOL knowledge base basic facts about the player's pieces, their positions and reachable positions 
	 * It also tells the first order knowledge base and its domain fact about own pieces
	 * @since 29.01.21 Only active pieces are considered
	 * @since 07.04.21 Castling rules are added
	 * @since 14.08.21 The chesstypes KING,QUEEN,ROOK, etc are unary relations
	 * @since 18.01.22 Added possible reach and possible protect facts
	 * @since 23.01.25 Even if position is removed this position is protected by pawn and under pawn attack
	 * @since 08.01.26 Added predicate HOMESQUARE
	 * @since 08.01.26 Added predicate MINORPIECE for Bishop and Knight
	 * @since 02.03.26 If a position is occupied by a friend then it is not reachable.And if it is a pawn attack position, then it is not reachable 
	 *
	 */
	public void makeRules(APlayer player,String castleone,String castle2) {
//		APlayer player= stateImpl.getMyPlayer();
		chessDomain.addConstant(player.getNameOfplayer());
//		chessDomain.addPredicate(OWNER);
		Constant ownerVariable = new Constant(player.getNameOfplayer());
		playerName = player.getNameOfplayer();
		String predicate = "";
		List<AgamePiece> pieces = player.getMygamePieces();
		for (AgamePiece piece:pieces) {
			if (piece.isActive()) {
				List<Term> ownerTerms = new ArrayList<Term>();
				List<Term> boardTerms = new ArrayList<Term>();
				List<Term> playerTerms = new ArrayList<Term>();
				List<Term> homeTerms = new ArrayList<Term>();
				ownerTerms.add(ownerVariable);
				String name = piece.getMyPiece().getOntlogyName(); 

				String posName = piece.getmyPosition().getPositionName();
				String homePos = piece.getHomePosition().getPositionName();
				chessDomain.addConstant(homePos);
				chessDomain.addConstant(posName);
				chessDomain.addConstant(name);
				chessDomain.addPredicate(piece.returnPredicate());
				if (!predicate.equals(piece.returnPredicate())) {
					predicate = piece.returnPredicate();
				}
//				predicate = predicate+"("+name+","+posName+")";
				chessDomain.addPredicate(predicate);
//				chessDomain.addConstant(predicate);
				Constant pieceVariable = new Constant(name);
				Constant posVariable = new Constant(posName);
				Constant homeVariable = new Constant(homePos);
				List<Term> terms = new ArrayList<Term>();
				terms.add(pieceVariable);
				terms.add(posVariable);
				boardTerms.add(posVariable);
				playerTerms.add(ownerVariable);
				ownerTerms.add(pieceVariable);
				homeTerms.add(pieceVariable);
				homeTerms.add(homeVariable);
				Predicate folPredicate = new Predicate(predicate,terms);
				Predicate ownerPredicate = new Predicate(OWNER,ownerTerms);
				Predicate playerPredicate = new Predicate(PLAYER,playerTerms);
				Predicate boardPredicate = new Predicate(BOARD,boardTerms);
				Predicate homePredicate = new Predicate(HOMESQUARE,homeTerms);
				folKb.tell(folPredicate);
				folKb.tell(ownerPredicate);
				folKb.tell(boardPredicate);
				folKb.tell(playerPredicate);
				folKb.tell(homePredicate);	
				List<Term> typeTerms = new ArrayList<Term>();
				typeTerms.add(pieceVariable);
				String pieceType = KnowledgeBuilder.getPieceType(piece);
				pieceType type = piece.getPieceType();
				Constant typeConstant = new Constant(pieceType);
				typeTerms.add(typeConstant);
//				chessDomain.addConstant(pieceType);
				Predicate typePredicate = new Predicate(PIECETYPE,typeTerms);
				folKb.tell(typePredicate);
				if (type == type.KNIGHT || type == type.BISHOP) {
					List minorTerms = new ArrayList<Term>();
					minorTerms.add(pieceVariable);
					Predicate minorpred = new Predicate(MINORPIECE,minorTerms);
					folKb.tell(minorpred);	
				}
				HashMap<String,Position> attackMap = piece.getAttackPositions();
				List<Position> attackPositions = null;
				if (attackMap != null)
					attackPositions = new ArrayList(attackMap.values());
				List<Position> availablePositions = piece.getNewlistPositions();
				List<Position> castlePositions = null;
				if (type == type.KING) {
					HashMap<String,Position> castler = piece.getCastlePositions();
					castlePositions = new ArrayList<Position>(castler.values());
				}
				boolean pawnattack = false;
				if (attackPositions != null && !attackPositions.isEmpty())
					pawnattack = true; // This is a pawn
				if (availablePositions != null && !availablePositions.isEmpty()) {
//					chessDomain.addPredicate(REACHABLE);
					for (Position pos:availablePositions){
						//if(!piece.checkRemoved(pos)) 
						//if(!piece.checkFriend(pos))
						String possibleposition = pos.getPositionName();
						Constant possibleprotectorVariable = new Constant(name);
						Constant possibleprotectedVariable = new Constant(possibleposition);
						List<Term> possibleprotectedTerms = new ArrayList<Term>();
						possibleprotectedTerms.add(possibleprotectorVariable);
						possibleprotectedTerms.add(possibleprotectedVariable);
						Predicate possibleprotectorPredicate = new Predicate(POSSIBLEPROTECT,possibleprotectedTerms);
						Predicate possiblereachablePredicate = new Predicate(POSSIBLEREACH,possibleprotectedTerms);
						if (!pawnattack) {
							folKb.tell(possibleprotectorPredicate);
//							kb.tellCaptureRules(t, position, name);
						}
						folKb.tell(possiblereachablePredicate);
						if(!piece.checkRemoved(pos) || piece.checkFriendlyPosition(pos)) { // isFriendlyPosition() added 01.11.21
/*							if (pos.isFriendlyPosition()) {
								writer.println("=========== position is friendly !!"+pos.toString());
							}*/
							String position = pos.getPositionName();
							Constant protectorVariable = new Constant(name);
							Constant protectedVariable = new Constant(position);
							List<Term> protectedTerms = new ArrayList<Term>();
							protectedTerms.add(protectorVariable);
							protectedTerms.add(protectedVariable);
							Predicate protectorPredicate = new Predicate(PROTECTED,protectedTerms);
							Predicate reachablePredicate = new Predicate(REACHABLE,protectedTerms);
							if (!pawnattack) {
								folKb.tell(protectorPredicate);
//								kb.tellCaptureRules(t, position, name);
							}
							boolean pawnapos = false;
							if (pawnattack) {
								Position pawnpos =  (Position) attackPositions.stream().filter(c -> c.getPositionName().contains(position)).findAny().orElse(null);
								pawnapos = pawnpos != null; // TRue if this is a pawn attack position
							}
							if (!piece.checkFriendlyPosition(pos) && !pawnapos) // Added 2.03.26 A position occupied by a friend is not reachable. And if it is a pawn attack position, then it is not reachable
								folKb.tell(reachablePredicate);

/*							if (player == opponent) {
								Predicate threatenPredicate = new Predicate(THREATEN,protectedTerms);
								folKb.tell(threatenPredicate);
							}*/
							chessDomain.addConstant(position);
							if (type == type.KING && castlePositions != null) { // Must be simplified
								List<Position> removedKing = piece.getRemovedPositions();
								for (Position cpos: castlePositions) {
									String cPosname = cpos.getPositionName();
									String xposname = "";
									if (cPosname.equals("g1")) {
										xposname = "f1";
									}
									if (cPosname.equals("c1")) {
										xposname = "d1";
									}
									if (cPosname.equals("g8")) {
										xposname = "f8";
									}
									if (cPosname.equals("c8")) {
										xposname = "d8";
									}
									String xxpos = xposname;
									Position xposn =  (Position) removedKing.stream().filter(c -> c.getPositionName().contains(xxpos)).findAny().orElse(null);
									if (!cpos.isInUse() && xposn == null) {
										List<Term> castleTerms = new ArrayList<Term>();
										Constant castleConstant = new Constant(cPosname);
										Constant castleKing = new Constant(name);
										castleTerms.add(castleKing);
										castleTerms.add(castleConstant);
										Predicate castlePredicate = new Predicate(CASTLE,castleTerms);
										folKb.tell(castlePredicate);
										writer.println("Made a CASTLE predicate for "+name+" and position "+cPosname);
										writer.flush();
/*										writer.println(folKb.toString());
										folKb.writeKnowledgebase();*/
									}
								}
							}
						}
					}
				}
				if (pawnattack) {
					for (Position pos:attackPositions){
//						if(!piece.checkRemoved(pos)) { OJN 23.01.25 Even if position is removed this position is protected and under pawn attack
							String position = pos.getPositionName();
							Constant protectorVariable = new Constant(name);
							Constant protectedVariable = new Constant(position);
							List<Term> protectedTerms = new ArrayList<Term>();
							protectedTerms.add(protectorVariable);
							protectedTerms.add(protectedVariable);
							Predicate protectorPredicate = new Predicate(PROTECTED,protectedTerms);
							Predicate pawnAttack = new Predicate(PAWNATTACK,protectedTerms);
							folKb.tell(protectorPredicate);
							folKb.tell(pawnAttack);
							chessDomain.addConstant(position);
//							kb.tellCaptureRules(t, position, name);
//						}
				 	}
				}
			}

		}
	}
	/**
	 * makeSentences
	 * This method creates simple facts about the player's pieces to the FOL knowledge base:
	 * It creates the unary relation of type BISHOP(piecename) etc. for all the player's pieces
	 */
	public void makeSentences(APlayer player) {
		List<AgamePiece> pieces = player.getMygamePieces();
		ABishop b = null;
		ARook r = null;
		AQueen qt = null;
		AKnight kn = null;
		Aking king = null;
		List<Term> typeTerms = new ArrayList<Term>();
	
		for (AgamePiece piece:pieces) {
			String name = piece.getMyPiece().getOntlogyName();
			Constant nameVariable = new Constant(name);
			List<Term> nameTerms = new ArrayList<Term>();
			nameTerms.add(nameVariable);
			Predicate piecePredicate = new Predicate(PIECE,nameTerms);
			folKb.tell(piecePredicate);
			typeTerms.clear();
			typeTerms.add(nameVariable);
			piece.setPredicate(piece.getMyPiece().getPredicate());
			ChessPieceType pieceType = piece.getChessType();
			if (pieceType instanceof APawn) {
				Predicate typePredicate = new Predicate(PAWN,typeTerms);
				folKb.tell(typePredicate);
			}
			if (pieceType instanceof ABishop) {
				b = (ABishop) pieceType;
				Predicate typePredicate = new Predicate(BISHOP,typeTerms);
				folKb.tell(typePredicate);
//				kb.setOwnBishop(name);
			}
			if (pieceType instanceof ARook) {
				r = (ARook) pieceType;
				Predicate typePredicate = new Predicate(ROOK,typeTerms);
				folKb.tell(typePredicate);
//				kb.setOwnRook(name);
			}
			if (pieceType instanceof AQueen) {
				qt = (AQueen) pieceType;
				Predicate typePredicate = new Predicate(QUEEN,typeTerms);
				folKb.tell(typePredicate);
//				kb.setOwnQueen(name);
			}
			if (pieceType instanceof AKnight) {
				kn = (AKnight) pieceType;
				Predicate typePredicate = new Predicate(KNIGHT,typeTerms);
				folKb.tell(typePredicate);
//				kb.setOwnKnight(name);
			}
			if (pieceType instanceof Aking) {
				king = (Aking) pieceType;
				Predicate typePredicate = new Predicate(KING,typeTerms);
				folKb.tell(typePredicate);
//				kb.setOwnKing(name);
			}			

		}		

	}

	/**
	 * setOpponentpieces
	 * This method creates knowledge about the opponent and its pieces to the first order knowledge base and its domain
	 * @since 29.01.21 Only active pieces are considered
	 * @param opponent
	 */
	public void setOpponentpieces(APlayer opponent) {
		List<AgamePiece> pieces = opponent.getMygamePieces();
		chessDomain.addConstant(opponent.getNameOfplayer());
		Constant ownerVariable = new Constant(opponent.getNameOfplayer());
		String predicate = "";
		chessDomain.addPredicate(THREATEN);
		for (AgamePiece piece:pieces) {
			if (piece.isActive()) {
				List<Term> ownerTerms = new ArrayList<Term>();
				ownerTerms.add(ownerVariable);
				String name = piece.getMyPiece().getOntlogyName();
				String posName = piece.getmyPosition().getPositionName();
				chessDomain.addConstant(posName);
				chessDomain.addConstant(name);
				chessDomain.addPredicate(piece.returnPredicate());
				if (!predicate.equals(piece.returnPredicate())) {
					predicate = piece.returnPredicate();
				}
//				predicate = predicate+"("+name+","+posName+")";
				chessDomain.addPredicate(predicate);
//				chessDomain.addConstant(predicate);
				Constant pieceVariable = new Constant(name);
				Constant posVariable = new Constant(posName);
				ownerTerms.add(pieceVariable);
				Predicate ownerPredicate = new Predicate(OWNER,ownerTerms);

				List<Term> terms = new ArrayList<Term>();
				terms.add(pieceVariable);
				terms.add(posVariable);
				Predicate folPredicate = new Predicate(predicate,terms);
				folKb.tell(folPredicate);
				folKb.tell(ownerPredicate);
				
				List<Term> typeTerms = new ArrayList<Term>();
				typeTerms.add(pieceVariable);
				String pieceType = KnowledgeBuilder.getPieceType(piece);
				Constant typeConstant = new Constant(pieceType);
				typeTerms.add(typeConstant);
//				chessDomain.addConstant(pieceType);
				Predicate typePredicate = new Predicate(PIECETYPE,typeTerms);
				folKb.tell(typePredicate);
				opponentPieces.add(name);
				HashMap<String,Position> attackMap = piece.getAttackPositions();
				List<Position> attackPositions = null;
				if (attackMap != null)
					attackPositions = new ArrayList(attackMap.values());
				boolean pawnattack = false;
				if (attackPositions != null && !attackPositions.isEmpty())
					pawnattack = true;		
				List<Position> availablePositions = piece.getNewlistPositions();
				if (attackPositions != null && !attackPositions.isEmpty())
					availablePositions = attackPositions;
				if (availablePositions != null && !availablePositions.isEmpty()) {
					for (Position pos:availablePositions ){
						// if(!piece.checkRemoved(pos)) 
						if(!piece.checkRemoved(pos)) {
							String position = pos.getPositionName();
							Constant protectorVariable = new Constant(name);
							Constant protectedVariable = new Constant(position);
							List<Term> protectedTerms = new ArrayList<Term>();
							protectedTerms.add(protectorVariable);
							protectedTerms.add(protectedVariable);
							Predicate protectorPredicate = new Predicate(THREATEN,protectedTerms);
							if (!pawnattack)
								folKb.tell(protectorPredicate);
							chessDomain.addConstant(position);

						}
					}
				}
				if (pawnattack) {
					for (Position pos:attackPositions){
						if(!piece.checkRemoved(pos) && piece.isActive()) {
							String position = pos.getPositionName();
							Constant protectorVariable = new Constant(name);
							Constant protectedVariable = new Constant(position);
							List<Term> protectedTerms = new ArrayList<Term>();
							protectedTerms.add(protectorVariable);
							protectedTerms.add(protectedVariable);
							Predicate protectorPredicate = new Predicate(THREATEN,protectedTerms);
							folKb.tell(protectorPredicate);
							chessDomain.addConstant(position);
						}
					}
				}
			}
		}

	}

	@Override
	public Action ask(KnowledgeBase kb, Sentence sentence) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Sentence makeActionQuery(int t) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * This method is used to make sentences for propositional logic
	 * @see aima.core.logic.propositional.agent.KBAgent#makeActionSentence(aima.core.agent.Action, int)
	 * @since ?? Not in use
	 */
	@Override
	public Sentence makeActionSentence(Action action, int t) {


		return null;
	}

	@Override
	public Sentence makePerceptSentence(Percept state, int t) {

		return null;
	}

}
