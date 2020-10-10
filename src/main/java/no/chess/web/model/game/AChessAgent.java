package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import aima.core.agent.Action;
import aima.core.agent.Percept;
import aima.core.logic.propositional.agent.KBAgent;
import aima.core.logic.propositional.kb.KnowledgeBase;
import aima.core.logic.propositional.kb.data.Clause;
import aima.core.logic.propositional.parsing.ast.PropositionSymbol;
import aima.core.logic.propositional.parsing.ast.Sentence;
import no.chess.web.model.PlayGame;
import no.chess.web.model.Position;
import no.games.chess.ChessPieceType;

/**
 * This is a Knowledgebase agent derived from the generic knowledgebase agent of AIMA chapter 7.
 * The state of the chess game implements the Percept interface.
 * The ChessAction interface extends the AIMA Action interface.
 * KBAgent is an abstract class extending the AbstracAgent class 
 * 
 * The agent main purpose is to choose the best action from the list of available actions.
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
	private List <ChessActionImpl> actions = null;
	private String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\knowledgebase.txt";
	private PrintWriter writer = null;
	private FileWriter fw = null;
	private PlayGame game = null;
	private APlayer myPlayer = null;
	private APlayer opponent = null;
	private List<Position> emptyPositions = null;
	private int noofMoves = 0;
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

	}


	/**
	 * This is constructor used by PlayGame
	 * @param KB
	 * @param localAction
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

	}


	@Override
	public Action execute(Percept state) {
		stateImpl = (ChessStateImpl)state;
		myPlayer = stateImpl.getMyPlayer();
		opponent = stateImpl.getOpponent();
		actions = stateImpl.getActions(); // creates new actions !!!
		kb.setStateImpl(stateImpl);

	
/*
 * To move this into makePerceptSentence ?		
 */
		int oppmoves = stateImpl.getOpponent().getMyMoves().size() -1;
		int mymoves = stateImpl.getMyPlayer().getMyMoves().size() -1;
		String playerName = stateImpl.getMyPlayer().getPlayerId();
		if (oppmoves < 0)
			oppmoves = 0;
		if (mymoves <0)
			mymoves = 0;
//		kb.tell("AFACT");
		emptyPositions = game.getNotusedPositionlist();
		noofMoves = game.getMovements().size();
		
		Sentence playSentence = kb.newSymbol(kb.TOPLAY+playerName, noofMoves);
		kb.tell(playSentence);

		makeOpponentsentences(stateImpl.getOpponent(),noofMoves);
		makeSentences();
/*
 * End move ?
 */
/*
 * Here we must ask the knowledge base what is the best action to perform:		
 */
		Action newAction = null;
		for (ChessActionImpl action:actions) {
//			double evaluation = game.getGame().analyzePieceandPosition(action);
			if (action.getPossibleMove()!= null && !action.isBlocked()) {
				newAction = kb.askPossibleAction(action, noofMoves);
				if (newAction != null)
					break;
			}

		}
	
//		makeRules(noofMoves);
		StringBuilder builder = new StringBuilder();
		builder.append("A CHESS Knowledge base\n");
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
		}
		writer.flush();
//		Sentence sentence = makePerceptSentence(state, 0);
//		KB.tell(sentence);
		if (newAction != null)
			return newAction;
		return localAction;
//		return super.execute(state);
	}
	/**
	 * makeRules
	 * At present not used
	 * @param t
	 */
	public void makeRules(int t) {
		APlayer player= stateImpl.getMyPlayer();
		List<AgamePiece> pieces = player.getMygamePieces();
		for (AgamePiece piece:pieces) {
			String name = piece.getMyPiece().getOntlogyName();
			String position = piece.getmyPosition().getPositionName();
			String searchName = "ACTION"+name+"_"+position+"_"+new Integer(t).toString();
			PropositionSymbol foundSymbol = kb.findSymbol(searchName);
			if (foundSymbol != null) {
				kb.tellmoveRule(foundSymbol, position, t);
			}
		}
	}
	/**
	 * makeSentences
	 * This method creates simple facts about the current state of the game:
	 * Which pieces are available for the active player and their position and possible moves
	 * Which actions are available for the active player
	 * Which positions are empty on the board
	 * 
	 */
	public void makeSentences() {
		APlayer player= stateImpl.getMyPlayer();
		List<AgamePiece> pieces = player.getMygamePieces();
		ABishop b = null;
		ARook r = null;
		AQueen qt = null;
		AKnight kn = null;
		Aking king = null;
		for (AgamePiece piece:pieces) {
			String name = piece.getMyPiece().getOntlogyName();
			ChessPieceType pieceType = piece.getChessType();
			if (pieceType instanceof ABishop) {
				b = (ABishop) pieceType;
				kb.setOwnBishop(name);
			}
			if (pieceType instanceof ARook) {
				r = (ARook) pieceType;
				kb.setOwnRook(name);
			}
			if (pieceType instanceof AQueen) {
				qt = (AQueen) pieceType;
				kb.setOwnQueen(name);
			}
			if (pieceType instanceof AKnight) {
				kn = (AKnight) pieceType;
				kb.setOwnKnight(name);
			}
			if (pieceType instanceof Aking) {
				king = (Aking) pieceType;
				kb.setOwnKing(name);
			}			

			String position = piece.getmyPosition().getPositionName();
			Sentence sentence = kb.newSymbol(name+"_"+"AT"+position, noofMoves);
			kb.tell(sentence);
		}		
		for (ChessActionImpl action:actions) {
			Sentence sentence = makeActionSentence(action,noofMoves);
			if (sentence != null)
				kb.tell(sentence);
		}
		for (Position position:emptyPositions) {
			String name = position.getPositionName();
			Sentence sentence =  kb.newSymbol(name+"_",noofMoves);
			kb.tell(sentence);
		}
	}
	/**
	 * makeOpponentsentences
	 * This method creates simple facts about the current state of of the opponent:
	 * The opponent pieces and their positions
	 * @param opponent
	 */
	public void makeOpponentsentences(APlayer opponent,int t) {
		List<AgamePiece> pieces = opponent.getMygamePieces();
		for (AgamePiece piece:pieces) {
			String name = piece.getMyPiece().getOntlogyName();
			String position = piece.getmyPosition().getPositionName();
			Sentence sentence = kb.newSymbol(name+"_"+"AT"+position, t);
			kb.tell(sentence);
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
		double evaluation = game.getGame().analyzePieceandPosition(thisAction);
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
				String prot = "PROTECTEDBY";
				String simpleProt ="PROTECTED";
				String name = thisAction.getChessPiece().getMyPiece().getOntlogyName();
				String pos = protectedPos.getPositionName();
				Sentence sentence = kb.newSymbol(prot+name+"_"+pos, t);
				Sentence simple = kb.newSymbol(simpleProt +"_"+pos, t);
				kb.tell(sentence);
				kb.tell(simple);
			}
			
		}
		if (otherprotectedPositions != null) {
			for (Position protectedPos:otherprotectedPositions) {
				String prot = "PROTECTEDBY";
				String name = thisAction.getChessPiece().getMyPiece().getOntlogyName();
				String pos = protectedPos.getPositionName();
				Sentence sentence = kb.newSymbol(prot+name+"_"+pos, t);
				kb.tell(sentence);
				String atta = "ATTACKEDBY";
				Sentence attacksentence = kb.newSymbol(atta+name+"_"+pos, t);
				kb.tell(attacksentence);
			}
			
		}		
		if (attackedPositions != null) {
			for (Position attackedPos:attackedPositions) {
				String atta = "ATTACKEDBY";
				String pos = attackedPos.getPositionName();
				String name = thisAction.getChessPiece().getMyPiece().getOntlogyName();
				Sentence sentence = kb.newSymbol(atta+name+"_"+pos, t);
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
				Sentence sentence = kb.newSymbol("ACTION"+name+"_AT"+position, t);
				thisAction.setSentence(sentence);
//				kb.tell(sentence);
				kb.tellmoveRule(kb.newSymbol("ACTION"+name+"_TO"+toPos, t), "AT"+position, t);
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
