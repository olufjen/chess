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
import aima.core.logic.propositional.parsing.ast.Sentence;
import aima.core.util.datastructure.Pair;
import no.chess.web.model.PlayGame;
import no.chess.web.model.Position;
import no.games.chess.ChessPieceType;
import no.games.chess.AbstractGamePiece.pieceType;
import no.games.chess.fol.BCGamesAskHandler;
import no.games.chess.fol.FOLGamesBCAsk;
import no.games.chess.fol.FOLGamesFCAsk;
import no.games.chess.planning.ChessProblem;
import no.games.chess.planning.ChessSearchAlgorithm;

/**
 * The Chess Agent is both a utility based agent and a goal based agent.
 * (For definition see p. 52 and p. 53.)
 * This is a Knowledgebase agent derived from the generic knowledgebase agent of AIMA chapter 7.
 * It is further adapted for the FOL knowledge base as described in chapter 10 and 11.
 * It is created every time the PlayGame object makes a move.
 * The state of the chess game implements the Percept interface.
 * The ChessAction interface extends the AIMA Action interface.
 * KBAgent is an abstract class extending the AbstractAgent class 
 * 
 * The agent main purpose is to choose the best action from the list of available actions.
 * The available actions are held in the ChessState object (the Percept)
 * So the agent program performs a mapping from the Percept to an action.
 * For this purpose the agent must find:
 * If the chosen action has a movement
 * The number of moves so far
 * The preferred position
 * If preferred position is occupied by opponent
 * If the opponent piece is protected
 * If the preferred position is a center position
 * 
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
	
/**
 * ChessDomain:
 *  All pieces are constants
 *  all positions are constants
 */
	private FOLDomain chessDomain;
	private FOLGamesFCAsk forwardChain;
	private FOLGamesBCAsk backwardChain;
	private InferenceProcedure infp;
	private List <ChessActionImpl> actions = null;
	private List <ChessActionImpl> opponentActions = null;
	private String knowledgeFilename = "knowledgebase.txt";
	private String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\knowledgebase.txt";
	private PrintWriter writer = null;
	private FileWriter fw = null;
	private PlayGame game = null;
	private APlayer myPlayer = null;
	private APlayer opponent = null;
	private List<Position> emptyPositions = null;
	private List<Position> positionList = null; // The original HashMap of positions as a list
	private HashMap<String,Position> positions; // The original HashMap of positions
	private List<String> opponentPieces = null;
	private AChessProblemSolver solver = null;
	private ChessSearchAlgorithm chessSearch = null;
	private int noofMoves = 0;
	
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
	private String playerName = "";
    private String OCCUPIES = "";
    private String PAWNATTACK ="";
    private String BOARD;
    private String PLAYER;
    private String CASTLE;
    private String OPPONENTTO;
    private String POSSIBLETHREAT;
    
    private ChessActionImpl castleAction = null;
    
	public AChessAgent(KnowledgeBase kb) {
		super(kb);
		
	}

	
	public AChessAgent(ChessKnowledgeBase KB, ChessActionImpl localAction) {
		super(KB);
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
		chessDomain = new FOLDomain();
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
		opponentPieces = new ArrayList<String>();
		positionList = game.getPositionlist();
		for (Position pos:positionList) {
			pos.setFriendlyPosition(false);
		}
		positions = game.getPositions();
		chessDomain = new FOLDomain();
		setPredicatenames();
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
			chessDomain.addPredicate(OPPONENTTO);
			chessDomain.addPredicate(POSSIBLETHREAT);
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
			chessDomain.addPredicate(ROOK);
			chessDomain.addPredicate(PAWN);
			chessDomain.addPredicate(BISHOP);
			chessDomain.addPredicate(KNIGHT);
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
	 * (See the chess problem solver).  
	 */
	/* (non-Javadoc)
	 * @see aima.core.logic.propositional.agent.KBAgent#execute(aima.core.agent.Percept)
	 */
	@Override
	public Action execute(Percept state) {
		stateImpl = (ChessStateImpl)state;
//		Variable kingJohn = new Variable("John");
/*		List<Term> terms = new ArrayList<Term>();
		terms.add(kingJohn);
		Predicate mKing = new Predicate("king",terms);*/
		myPlayer = stateImpl.getMyPlayer();
		opponent = stateImpl.getOpponent();
		actions = stateImpl.getActions(); // creates new actions !!!
		kb.setStateImpl(stateImpl);
		
		forwardChain = new FOLGamesFCAsk(); // A Forward Chain inference procedure see p. 332
		backwardChain = new FOLGamesBCAsk(); // A backward Chain inference procedure see p. 337
//		folKb = new FOLKnowledgeBase(chessDomain);
		folKb = new ChessFolKnowledgeBase(chessDomain, forwardChain,knowledgeFilename);
		folKb.setBackWardChain(backwardChain);
//		folKb.tell(mKing);
		// Moved to behind makerules !! 20.11.21
//		setOpponentpieces(opponent);//creates knowledge about the opponent and its pieces to the first order knowledge base and its domain
		kb.setOpponentPieces(opponentPieces);
		noofMoves = game.getMovements().size();
		String playerName = stateImpl.getMyPlayer().getNameOfplayer();
		Sentence playSentence = kb.newSymbol(kb.TOPLAY+playerName, noofMoves);
		kb.tell(playSentence);
	
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
		
		Predicate folPredicate = new Predicate(chessPr,terms);  // The folPredicate is OWNER
		QuantifiedSentence qSentence = new QuantifiedSentence("FORALL",variables,folPredicate);
//		folKb.tell(qSentence);
		createConnected("player", "y", "x");
		String s1 = "occupies(o,px)^occupies(pi,py)";
		String s2 = "MOVE(pi,pz)";
	//	KnowledgeBuilder.parseSentence(s1,s2,folKb);
		
/*		List<Term> ownerTerms = new ArrayList<Term>();
		Variable ownerVariable = new Variable(playerName);
		ownerTerms.add(ownerVariable);
		ownerTerms.add(pieceVariable);
		Predicate ownerPredicate = new Predicate(OWNER,ownerTerms);
		List<Term> reachableTerms = new ArrayList<Term>();
		reachableTerms.add(pieceVariable);
		reachableTerms.add(posVariable);
		Predicate reachablePredicate = new Predicate(REACHABLE,reachableTerms);
		List<Term> moveTerms = new ArrayList<Term>();
		moveTerms.add(pieceVariable);
		moveTerms.add(posVariable);
		Predicate movePredicate = new Predicate(MOVE,moveTerms);
		*/
		
//		ConnectedSentence reachableSentence = new ConnectedSentence(Connectors.AND,ownerPredicate,reachablePredicate);
//		ConnectedSentence goal = new ConnectedSentence(Connectors.IMPLIES,reachableSentence,movePredicate);
//		folKb.tell(reachableSentence);


//		writer.println(chessDomain.toString());
//		result = folKb.ask(folPredicate);
//		result = folKb.ask(qSentence); 

//		fcResult = forwardChain.ask(folKb, folPredicate);

		Literal chessLiteral = new Literal(folPredicate);
		List<Literal> allLiterals = new ArrayList<Literal>();
		allLiterals.add(chessLiteral);
		LinkedHashSet<Map<Variable, Term>> mapResult = (LinkedHashSet<Map<Variable, Term>>) folKb.fetch(allLiterals);
		List<Map<Variable, Term>> listResults = mapResult.stream().collect(Collectors.toList());
		results.add(result);
//		results.add(fcResult);
/*		for (String c:chessConstants) {
//			String folSentence = chessPr+"("+c+"e1)";

	
			if (c.contains("White"))
			  pieceVariable= new Variable(c);
			if (!c.contains("White")&& !c.contains("Black"))
				posVariable = new Variable(c);
			if (posVariable != null && pieceVariable != null) {
				terms.add(pieceVariable);
				terms.add(posVariable);
				Predicate folPredicate = new Predicate(chessPr,terms);
				result = folKb.ask(folPredicate);
			
				results.add(result);
			}

		}*/
		
/*		for(InferenceResult res:results) {
			writer.println(InferenceResultPrinter.printInferenceResult(res));
			writer.println(res.toString());
		}*/
	
		
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
/*		for (ChessActionImpl action:actions) {
//			double evaluation = game.getGame().analyzePieceandPosition(action);
			if (action.getPossibleMove()!= null && !action.isBlocked()) {

				String aName = action.getActionName()+"_"+movnr;
				action.setActionName(aName);
//				kb.askPossibleAction(action, noofMoves);
				askMove(myPlayer,action);
			}

		} This has been turned off OLJ 25.5.21*/
		
		ChessActionImpl naction = null;
		ChessProblem problem = solver.planProblem((ArrayList<ChessActionImpl>) actions);
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

			String nactionName = actionSchema.getName();
			int nIndex = nactionName.indexOf("_");
			String chessName = nactionName.substring(0, nIndex);
			naction =  (ChessActionImpl) actions.stream().filter(c -> c.getActionName().equals(chessName)).findAny().orElse(null);
		}

		for (ChessActionImpl action:actions) {
			if (action.getActionValue() == null) {
				action.setActionValue(new Integer(0));
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
/*		builder.append("\nA CHESS Knowledge base\n");
		writer.println(builder.toString());
		List<Sentence> mySentences = kb.getSentences();
		Set<Clause> myClauses = kb.asCNF();
		for (Sentence sentence:mySentences) {
			writer.println(sentence.toString());
		}
		for (Clause clause:myClauses) {
			String def = "Not definite";
			if (clause.isDefiniteClause())
				def = "Definite";
			writer.println(clause.toString()+" "+def );
		}*/
	
//		Sentence sentence = makePerceptSentence(state, 0);
//		KB.tell(sentence);
		castleAction = solver.getCastleAction();
		writer.println("The first order knowledge base");
		writer.println(folKb.toString());
		writer.flush();
		if (naction != null)
			localAction = naction;
		return localAction;
//		return super.execute(state);
	}
	
	/**
	 * createConnected
	 * This method creates rules to the fol knowledge base 
	 * @since 18.05.21 The Owner predicate is removed from the rules.
	 * @param name
	 * @param pos
	 * @param piece
	 * @return a Connected sentence
	 */
	public void createConnected(String name, String pos, String piece) {
		List<Term> ownerTerms = new ArrayList<Term>();
		Variable ownerVariable = new Variable(name);
		Variable pieceVariable= new Variable(piece);
		Variable otherPiece = new Variable("p");
		Variable posVariable = new Variable(pos);
		Variable newPosition = new Variable("z");
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
/*		ConnectedSentence protectedGoal = new ConnectedSentence(Connectors.IMPLIES,goal,safemovePredicate);
		ConnectedSentence canAndSafe = new ConnectedSentence(Connectors.AND,movePredicate,safemovePredicate);
		ConnectedSentence safeTomove = new ConnectedSentence(Connectors.IMPLIES,canAndSafe,move);*/
		folKb.tell(goal);
//		folKb.tell(protectedGoal);
//		folKb.tell(safeTomove);
		List<Term> typeTerms = new ArrayList<Term>();
		Constant pieceType = new Constant(PAWN);
		typeTerms.add(pieceVariable);
		typeTerms.add(pieceType);
		Predicate typePredicate = new Predicate(PIECETYPE,typeTerms);
		Predicate pawnMove = new Predicate(PAWNMOVE,reachableTerms);
//		ConnectedSentence pawnSentence = new ConnectedSentence(Connectors.AND,ownerPredicate,typePredicate);
		ConnectedSentence pawnReachable = new ConnectedSentence(Connectors.AND,typePredicate,reachablePredicate);
		ConnectedSentence pawnmove = new ConnectedSentence(Connectors.IMPLIES,pawnReachable,pawnMove);
		folKb.tell(pawnmove);

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
	 * It also tells the first order knowledge base and its domain fact about own pieces
	 * @since 29.01.21 Only active pieces are considered
	 * @since 07.04.21 Castling rules are added
	 * @since 14.08.21 The chesstypes KING,QUEEN,ROOK, etc are unary relations
	 * param t
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
				
				ownerTerms.add(ownerVariable);
				String name = piece.getMyPiece().getOntlogyName(); 
/*				if (name.equals("WhiteQueen")) {
					writer.println("The white queen\n"+piece.toString());
				}
				if (name.contains("WhiteRook")) {
					writer.println("The white rook "+name);
					writer.println("Piece "+piece.toString());
				}*/
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
				List<Term> terms = new ArrayList<Term>();
				terms.add(pieceVariable);
				terms.add(posVariable);
				boardTerms.add(posVariable);
				playerTerms.add(ownerVariable);
				ownerTerms.add(pieceVariable);
				Predicate folPredicate = new Predicate(predicate,terms);
				Predicate ownerPredicate = new Predicate(OWNER,ownerTerms);
				Predicate playerPredicate = new Predicate(PLAYER,playerTerms);
				Predicate boardPredicate = new Predicate(BOARD,boardTerms);
				folKb.tell(folPredicate);
				folKb.tell(ownerPredicate);
				folKb.tell(boardPredicate);
				folKb.tell(playerPredicate);
			
				List<Term> typeTerms = new ArrayList<Term>();
				typeTerms.add(pieceVariable);
				String pieceType = KnowledgeBuilder.getPieceType(piece);
				pieceType type = piece.getPieceType();
				Constant typeConstant = new Constant(pieceType);
				typeTerms.add(typeConstant);
//				chessDomain.addConstant(pieceType);
				Predicate typePredicate = new Predicate(PIECETYPE,typeTerms);
				folKb.tell(typePredicate);
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
					pawnattack = true;
				if (availablePositions != null && !availablePositions.isEmpty()) {
//					chessDomain.addPredicate(REACHABLE);
					for (Position pos:availablePositions){
						//if(!piece.checkRemoved(pos)) 
						//if(!piece.checkFriend(pos))
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
							folKb.tell(reachablePredicate);
/*							if (player == opponent) {
								Predicate threatenPredicate = new Predicate(THREATEN,protectedTerms);
								folKb.tell(threatenPredicate);
							}*/
							chessDomain.addConstant(position);
							if (type == type.KING && castlePositions != null) {
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
									}
								}
							}
						}
					}
				}
				if (pawnattack) {
					for (Position pos:attackPositions){
						if(!piece.checkRemoved(pos)) {
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
						}
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
	 * makeOpponentsentences
	 * This method creates simple facts about the current state of of the opponent:
	 * The opponent pieces and their positions to the propositional knowledge base.
	 * @param opponent
	 */
	public void makeOpponentsentences(APlayer opponent,int t) {
		List<AgamePiece> pieces = opponent.getMygamePieces();
		for (AgamePiece piece:pieces) {
			String name = piece.getMyPiece().getOntlogyName();
			String position = piece.getmyPosition().getPositionName();
			piece.setPredicate(piece.getMyPiece().getPredicate());
			Sentence sentence = kb.newSymbol(name+"_"+"AT"+position, t);
			kb.tell(sentence);
			HashMap<String,Position> reachablePositions = piece.getReacablePositions();
			HashMap<String,Position> attackPositions = piece.getAttackPositions();
			List<Position> piecePositions = null;
			piecePositions = piece.getNewlistPositions();
			if (attackPositions != null && !attackPositions.isEmpty()) {
				piecePositions = new ArrayList(attackPositions.values());
			}

//			List<Position> piecePositions = new ArrayList(reachablePositions.values());
			for (Position opponentposition:piecePositions) {
				if (!piece.checkRemoved(opponentposition)) {
					String pos = opponentposition.getPositionName();
					Sentence attacksentence = kb.newSymbol(THREATEN+name+"_"+pos, t);
					kb.tell(attacksentence);
					Sentence protectSentence = kb.newSymbol(PROTECTED+name+"_"+pos, t);
					kb.tell(protectSentence);
				}
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

	@Override
	public Sentence makeActionSentence(Action action, int t) {
		ChessActionImpl thisAction = (ChessActionImpl) action;
/*		double evaluation = game.getGame().analyzePieceandPosition(thisAction);
		Double value = new Double(evaluation);
		thisAction.setEvaluationValue(value);*/
		String active = "NOMOV";
		List<AgamePiece> attackedPieces = thisAction.getAttacked();
		List<Position> protectedPositions = thisAction.getProtectedPositions();
		List<Position> attackedPositions = thisAction.getAttackedPositions();
		List<Position> otherprotectedPositions = thisAction.getOtherprotectedPositions();
		if (attackedPieces != null) {
			for (AgamePiece attackedPiece:attackedPieces) {
				String name = attackedPiece.getMyPiece().getOntlogyName();
				String pos = attackedPiece.getMyPosition().getPositionName();
				String attack = "ATTACK";
				Sentence sentence = kb.newSymbol(attack+name+"_"+pos, t);
				kb.tell(sentence);
			}
		}
		if (protectedPositions != null) {
			for (Position protectedPos:protectedPositions) {
				String name = thisAction.getChessPiece().getMyPiece().getOntlogyName();
				String pos = protectedPos.getPositionName();
				Sentence sentence = kb.newSymbol(PROTECTED+name+"_"+pos, t);
				Sentence simple = kb.newSymbol(simpleProtected +"_"+pos, t);
				kb.tell(sentence);
				kb.tell(simple);
			}
			
		}
		if (otherprotectedPositions != null) {
			for (Position protectedPos:otherprotectedPositions) {
				String name = thisAction.getChessPiece().getMyPiece().getOntlogyName();
				String pos = protectedPos.getPositionName();
				Sentence sentence = kb.newSymbol(PROTECTED+name+"_"+pos, t);
				kb.tell(sentence);
				Sentence attacksentence = kb.newSymbol(ATTACKED+name+"_"+pos, t);
				kb.tell(attacksentence);
			}
			
		}		
		if (attackedPositions != null) {
			for (Position attackedPos:attackedPositions) {
				String pos = attackedPos.getPositionName();
				String name = thisAction.getChessPiece().getMyPiece().getOntlogyName();
				Sentence sentence = kb.newSymbol(ATTACKED+name+"_"+pos, t);
				kb.tell(sentence);
			}
		}
		if (thisAction.getChessPiece().isActive() ) {
			String name = thisAction.getChessPiece().getMyPiece().getOntlogyName();
			String position = thisAction.getChessPiece().getmyPosition().getPositionName();
			ApieceMove move = thisAction.getPossibleMove();
			if (move == null || thisAction.isBlocked()) {
				Sentence sentence = kb.newSymbol(active+name+"_AT"+position, t);
//				kb.tell(sentence);
				return sentence;
			}
			if (move != null && !thisAction.isBlocked()) {
//				String moveNotation = move.getMoveNotation();
				String toPos = move.getToPosition().getPositionName();
/*				if (moveNotation == null || moveNotation.equals(""))
					moveNotation ="MOV";
				String toPos = move.getToPosition().getPositionName();
				Sentence sentence = kb.newSymbol(moveNotation+name+"_"+toPos, t);*/
//				kb.tell(sentence);
				Sentence sentence = kb.newSymbol(ACTION+name+"_AT"+position, t);
				thisAction.setSentence(sentence);
//				kb.tell(sentence);
				kb.tellmoveRule(kb.newSymbol(ACTION+name+"_TO"+toPos, t), "AT"+position, t);
				return sentence;
			}
		}
		if (!thisAction.getChessPiece().isActive()) {
			active = "VAC";
//			Position newPos = thisAction.getPreferredPosition(); // Makes a new attempt creating a new possible move for this action.
//			ApieceMove newMove = thisAction.getPossibleMove();
			String pieceName = thisAction.getChessPiece().getMyPiece().getOntlogyName();
			String position = thisAction.getChessPiece().getmyPosition().getPositionName();
			return kb.newSymbol(active+pieceName+"_"+position, t);
		}
			

		return null;
	}

	@Override
	public Sentence makePerceptSentence(Percept state, int t) {
		ChessActionImpl action = stateImpl.getChessAction();
		if (localAction == action) {
			String name = localAction.getChessPiece().getMyPiece().getOntlogyName();
			String position = localAction.getChessPiece().getmyPosition().getPositionName();
			return kb.newSymbol(name+"_"+position, 0);
			
		}
		return null;
	}

}
