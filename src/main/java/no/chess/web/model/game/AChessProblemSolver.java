
package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import aima.core.logic.fol.Connectors;
import aima.core.logic.fol.domain.FOLDomain;
import aima.core.logic.fol.inference.FOLFCAsk;
import aima.core.logic.fol.inference.InferenceProcedure;
import aima.core.logic.fol.inference.InferenceResult;
import aima.core.logic.fol.inference.InferenceResultPrinter;
import aima.core.logic.fol.kb.FOLKnowledgeBase;
import aima.core.logic.fol.kb.data.Literal;
import aima.core.logic.fol.parsing.ast.AtomicSentence;
import aima.core.logic.fol.parsing.ast.ConnectedSentence;
import aima.core.logic.fol.parsing.ast.Constant;
import aima.core.logic.fol.parsing.ast.NotSentence;
import aima.core.logic.fol.parsing.ast.Predicate;
import aima.core.logic.fol.parsing.ast.QuantifiedSentence;
import aima.core.logic.fol.parsing.ast.Sentence;
import aima.core.logic.fol.parsing.ast.Term;
import aima.core.logic.fol.parsing.ast.Variable;
import aima.core.logic.planning.ActionSchema;
import aima.core.logic.planning.GraphPlanAlgorithm;
import aima.core.logic.planning.Problem;
import aima.core.logic.planning.State;
import aima.core.logic.propositional.parsing.ast.ComplexSentence;
import aima.core.logic.propositional.parsing.ast.Connective;
import no.chess.web.model.PlayGame;
import no.chess.web.model.Position;
import no.games.chess.ChessPieceType;
import no.games.chess.AbstractGamePiece.pieceType;
import no.games.chess.fol.FOLGamesBCAsk;
import no.games.chess.fol.FOLGamesFCAsk;
/**
 * AChessProblemSolver
 * This class is used to find best moves in the chess game through planning as described in chapter 10 of 
 * the aima book
 * 
 * @author oluf
 * 
 */
public class AChessProblemSolver {
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
  private String playerName =  "";
  private String OCCUPIES = "";
  private String playSide;
  /**
   *  The type of piece under consideration
   */
  private String typeofPiece;

  /**
   *  The name of the action schema - pawnmove.bishopmove ...
   */
  private String moveName;

  private String outputFileName =  "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\problem.txt";
  private ChessStateImpl stateImpl =  null;
  private ChessActionImpl localAction =  null;
  private ChessKnowledgeBase kb =  null;
  private int noofMoves =  0;

  /**
   * 
   * A first order knowledge base
   * 
   */
  private FOLKnowledgeBase folKb;

  /**
   * 
   * ChessDomain:
   *  All pieces are constants
   *  all positions are constants
   * 
   */
  private FOLDomain chessDomain;
  private FOLGamesFCAsk forwardChain;
  private FOLGamesBCAsk backwardChain;
  private PrintWriter writer =  null;
  private FileWriter fw =  null;
  private PlayGame game =  null;
  private APlayer myPlayer =  null;
  private APlayer opponent =  null;
  private State initialState =  null;
  private State goalState =  null;
  private GraphPlanAlgorithm graphPlan =  null;

  public AChessProblemSolver(ChessStateImpl stateImpl, ChessActionImpl localAction, FOLKnowledgeBase folKb, FOLDomain chessDomain, FOLGamesFCAsk forwardChain, FOLGamesBCAsk backwardChain, PlayGame game, APlayer myPlayer, APlayer opponent) {
		super();
		this.stateImpl = stateImpl;
		this.localAction = localAction;
		this.folKb = folKb;
		this.chessDomain = chessDomain;
		this.forwardChain = forwardChain;
		this.backwardChain = backwardChain;
		this.game = game;
		this.myPlayer = myPlayer;
		this.opponent = opponent;
		playerName = this.myPlayer.getNameOfplayer();
		playSide = playerName.substring(0,5);
		noofMoves = game.getMovements().size();
		graphPlan = new GraphPlanAlgorithm();
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
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
  }

  public ChessStateImpl getStateImpl() {
		return stateImpl;
  }

  public void setStateImpl(ChessStateImpl stateImpl) {
		this.stateImpl = stateImpl;
  }

  public ChessActionImpl getLocalAction() {
		return localAction;
  }

  public void setLocalAction(ChessActionImpl localAction) {
		this.localAction = localAction;
  }

  public FOLKnowledgeBase getFolKb() {
		return folKb;
  }

  public void setFolKb(FOLKnowledgeBase folKb) {
		this.folKb = folKb;
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

  public no.chess.web.model.PlayGame getGame() {
		return game;
  }

  public void setGame(no.chess.web.model.PlayGame game) {
		this.game = game;
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

  public List<ActionSchema> searchProblem(ArrayList<ChessActionImpl> actions) {
	  for (ChessActionImpl action:actions) {
			if (action.getPossibleMove()!= null && !action.isBlocked()) {
				
			}
	  }
		return null;
  }

  public List<List<ActionSchema>> solveProblem(ChessActionImpl action) {
		determineParameters(action);
		Problem myProblem = buildProblem(action);
		if (myProblem != null) {
			List<List<ActionSchema>> solution = graphPlan.graphPlan(myProblem);
			for (List<ActionSchema> la:solution){
				for (ActionSchema as:la) {
					writer.println(as.toString());
				}
			}
			writer.flush();
			return solution;
		}

		writer.flush();
		return null;
  }

  public Problem buildProblem(ChessActionImpl action) {
		String pieceName = action.getChessPiece().getMyPiece().getOntlogyName();
		AgamePiece apiece = action.getChessPiece();
		ChessPieceType thepieceType = apiece.getChessType();
//		if (thepieceType instanceof APawn) {
			String actionName = action.getActionName();
			initialState = buildInitialstate(pieceName);
			goalState = buildGoalstate(action);
			Variable piece = new Variable("piece");
			Variable pos = new Variable("pos");
			Variable toPos = new Variable("topos");
			Constant type = new Constant(typeofPiece);
			Variable ownerVar = new Variable("owner");
			ArrayList variables = new ArrayList<Variable>(Arrays.asList(piece,pos,toPos,ownerVar));
			List<Term> terms = new ArrayList<Term>();
			List<Term> ownerterms = new ArrayList<Term>();
			List<Term> newterms = new ArrayList<Term>();
			List<Term> typeTerms = new ArrayList<Term>();
			ownerterms.add(ownerVar);
			ownerterms.add(piece);
			terms.add(piece);
			terms.add(pos);
			newterms.add(piece);
			newterms.add(toPos);
			typeTerms.add(piece);
			typeTerms.add(type);
			Predicate reachablePredicate = new Predicate(REACHABLE,newterms);
			Predicate typePred = new Predicate(PIECETYPE,typeTerms);
			List<Literal> typeprecondition = new ArrayList();
			List<Literal> typeeffects = new ArrayList();
			Predicate pospred = new Predicate(OCCUPIES,terms);
			Predicate ownerPred = new Predicate(OWNER,ownerterms);
			Predicate newPospred = new Predicate(OCCUPIES,newterms);
			typeprecondition.add(new Literal((AtomicSentence) typePred));
			typeprecondition.add(new Literal((AtomicSentence) pospred));
			typeprecondition.add(new Literal((AtomicSentence) reachablePredicate));
			typeeffects.add(new Literal((AtomicSentence) typePred));
			typeeffects.add(new Literal( (AtomicSentence)newPospred));
			ActionSchema typeAction = new ActionSchema("type",variables,typeprecondition,typeeffects);
			Literal notAt = new Literal(pospred, true);
			writer.println("The type preconditions");
			for (Literal f:typeprecondition) {
				writer.println(f.toString());
			}
			writer.println("The type effects");
			for (Literal f:typeeffects) {
				writer.println(f.toString());
			}
			List<Literal> precondition = new ArrayList();
			List<Literal> effects = new ArrayList();
			precondition.add(new Literal((AtomicSentence) pospred));
			precondition.add(new Literal((AtomicSentence) ownerPred));
			effects.add(notAt);
			effects.add(new Literal( (AtomicSentence)newPospred));
			effects.add(new Literal( (AtomicSentence)ownerPred));
			writer.println("The moved preconditions");
			for (Literal f:precondition) {
				writer.println(f.toString());
			}
			writer.println("The moved effects");
			for (Literal f:effects) {
				writer.println(f.toString());
			}
			ActionSchema movedAction = new ActionSchema(moveName,variables,precondition,effects);
			List<Literal> initFluents = initialState.getFluents();
			List<Literal> goalFluents = goalState.getFluents();
			writer.println("The fluents of Initial state. They are nodes in S0");
			for (Literal f:initFluents) {
				writer.println(f.toString());
			}
			writer.println("The fluents the goal state");
			for (Literal f:goalFluents) {
				writer.println(f.toString());
			}
			return new Problem(initialState,goalState,typeAction,movedAction);
//		}
//	return null;
  }

  public State buildGoalstate(ChessActionImpl action) {
		String pieceName = action.getChessPiece().getMyPiece().getOntlogyName();
		String toPos = action.getPossibleMove().getToPosition().getPositionName();
		List<Term> terms = new ArrayList<Term>();
		List<Term> typeTerms = new ArrayList<Term>();
				
		Constant pieceVar = new Constant(pieceName);
		Constant posVar = new Constant(toPos);
		Constant type = new Constant(typeofPiece);
		terms.add(pieceVar);
		terms.add(posVar);
		typeTerms.add(pieceVar);
		typeTerms.add(type);
		Predicate typePredicate = new Predicate(PIECETYPE,typeTerms);
		Predicate posSentence = new Predicate(OCCUPIES,terms);
		List<Term> ownerterms = new ArrayList<Term>();
		Constant ownerVar = new Constant(playerName);
		ownerterms.add(ownerVar);
		ownerterms.add(pieceVar);
		Predicate ownerSentence = new Predicate(OWNER,ownerterms);
		List<Literal> literals = new ArrayList();
		Literal pos = new Literal((AtomicSentence) posSentence);
		Literal own = new Literal((AtomicSentence) ownerSentence);
		Literal types = new Literal((AtomicSentence)typePredicate);
		literals.add(pos);
		literals.add(own);
		literals.add(types);
		State gState = new State(literals);
		return gState;
  }

  /**
   * determineParameters
   * Parameters that determine the structure of the Problem, and the states:
   * The number of moves so far.
   * If the piece of the action is an officer or a pawn.
   * If the piece of the action has been moved recently
   * If the piece with its new position protects/reaches a centre position.
   * If the piece with its new position can capture an opponent piece.
   * @param localAction
   */
  public void determineParameters(ChessActionImpl localAction) {
		String name = localAction.getChessPiece().getMyPiece().getOntlogyName();
		localAction.processPositions();//This method recalculates removed positions for this action. Why is this necessary?
		AgamePiece piece = localAction.getChessPiece();
		pieceType type = piece.getPieceType();
//		int totalmoves = localAction.getMoveNumber().intValue();
		List<Integer> moveNumbers = piece.getMoveNumbers(); // Which moves has this piece been part of?
		int nofMoves = piece.getNofMoves();
		HashMap<String,ApieceMove> myMoves = myPlayer.getMyMoves(); // Get the moves so far and compare
		List<ApieceMove> myListmoves = new ArrayList(myMoves.values());
		boolean bNr = false; // bNr is true if the piece of the action has been moved recently
		for (ApieceMove move:myListmoves) {
			String moveName = move.getPiece().getMyPiece().getOntlogyName();
			int mNr = move.getMoveNumber();
			for (Integer pNr:moveNumbers) {
				bNr = mNr == pNr.intValue() && moveName.equals(name) && mNr - pNr.intValue() < 3;
				if (bNr)
					break;
			}
			if (bNr)
				break;
		}
		
		String position = piece.getmyPosition().getPositionName();
		List<Position> removedList = localAction.getPositionRemoved();
		List<Position> availableList = localAction.getAvailablePositions();
		ApieceMove move = localAction.getPossibleMove();
		List<Position> preferredPositions = move.getPreferredPositions();
		String toPos = move.getToPosition().getPositionName();
		Position toPosition = move.getToPosition();
		if (type == type.PAWN) {
			typeofPiece = PAWN;
			moveName = "pawnmove";
		}
		if (type == type.BISHOP) {
			typeofPiece = BISHOP;
			moveName = "bishopmove";
		}		
		if (type == type.ROOK) {
			typeofPiece = ROOK;
			moveName = "rookmove";
		}			
		if (type == type.KNIGHT) {
			typeofPiece = KNIGHT;
			moveName = "knoghtmove";
		}
		if (type == type.QUEEN) {
			typeofPiece = QUEEN;
			moveName = "queenmove";
		}
		if (type == type.KING) {
			typeofPiece = KING;
			moveName = "kingmove";
		}	
		/*		
		if (type == type.PAWN && !bNr ){
			boolean center = toPosition.isCenterlefthigh()||toPosition.isCenterleftlow()||toPosition.isCenterrighthigh()||toPosition.isCenterrightlow();
		}
		if (type != type.PAWN && !bNr) {
			
		}*/
  }

  /**
   * buildInitialstate
   * THis method creates an initial state for a problem 
   * with a given piece name
   * @param piece
   * @return
   */
  public State buildInitialstate(String piece) {
		List<Sentence> folSentences = folKb.getOriginalSentences();
		State initState = null;
		String pieceName = null;
		String owner = null;
		List<String> reachablePos = new ArrayList<String>();
		List<Literal> literals = new ArrayList();

		for (Sentence s : folSentences) {
			String symName = s.getSymbolicName();
			if (symName.equals(OCCUPIES)) {
				 ArrayList<Term> literalTerms = new ArrayList<>();
				List<Term> terms = (List<Term>) s.getArgs();
				Term f = terms.get(0);
				Term p = terms.get(1);
				pieceName = f.getSymbolicName();
				if (pieceName.equals(piece)) {
/*					Term term = new Constant(pieceName);
					Term ps = new Constant(p.getSymbolicName());
					literalTerms.add(term);
					literalTerms.add(ps);
					Literal l = new Literal(new Predicate(symName, literalTerms));*/
					Literal l = new Literal((AtomicSentence) s);
					literals.add(l);
				}
	
			}
			if (symName.equals(OWNER)) {
				List<Term> terms = (List<Term>) s.getArgs();
				Term f = terms.get(0);
				ArrayList<Term> literalTerms = new ArrayList<>();
				owner = f.getSymbolicName();
				Term last = terms.get(1);
				String p = last.getSymbolicName();
				if (owner.equals(playerName)&& p.equals(piece)) {
	/*				Term term = new Constant(owner);
					Term ps = new Constant(p);
					literalTerms.add(term);
					literalTerms.add(ps);
					Literal l = new Literal(new Predicate(symName, literalTerms));*/
					Literal l = new Literal((AtomicSentence) s);
					literals.add(l);
				}
			}
			if (symName.equals(REACHABLE)) {
				List<Term> terms = (List<Term>) s.getArgs();
				ArrayList<Term> literalTerms = new ArrayList<>();
				Term f = terms.get(0);
				Term last = terms.get(1);
				String p = f.getSymbolicName();
				String pos = last.getSymbolicName();
				if (p.equals(piece)) {
/*					Term term = new Constant(owner);
					Term ps = new Constant(p);
					literalTerms.add(term);
					literalTerms.add(ps);
					Literal l = new Literal(new Predicate(symName, literalTerms));*/
					Literal l = new Literal((AtomicSentence) s);
					literals.add(l);
					reachablePos.add(pos);
				}
			}
			if (symName.equals(PIECETYPE)) {
				List<Term> terms = (List<Term>) s.getArgs();
				ArrayList<Term> literalTerms = new ArrayList<>();
				Term f = terms.get(0);
				Term last = terms.get(1);
				String p = f.getSymbolicName();
				String type = last.getSymbolicName();
				if (p.equals(piece) && type.equals(typeofPiece)) {
					Literal l = new Literal((AtomicSentence) s);
					literals.add(l);
				}
			}
		}
		List<Literal>temp = addProtected(folSentences,reachablePos,piece);
		literals.addAll(temp);
		return initState = new State(literals);
		
  }

  /**
   * addProtected
   * This method adds protected literals to a state
   * @param folSentences
   * @param reachablePos
   * @param piece
   * @return
   */
  public List<Literal> addProtected(List<Sentence> folSentences, List<String> reachablePos, String piece) {
		List<Literal> literals = new ArrayList();
		for (Sentence s : folSentences) {
			String symName = s.getSymbolicName();
			if (symName.equals(PROTECTED)) {
				List<Term> terms = (List<Term>) s.getArgs();
				Term f = terms.get(0);
				Term last = terms.get(1);
				String p = f.getSymbolicName();
				String pos = last.getSymbolicName();
				String posto = reachablePos.stream().filter(pos::equals).findAny().orElse(null);
				if (!p.equals(piece) &&posto != null) {
					Literal l = new Literal((AtomicSentence) s);
					literals.add(l);
				}
			}
		}
		
		return literals;
  }

}