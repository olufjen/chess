package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import aima.core.agent.Action;
import aima.core.logic.propositional.inference.DPLL;
import aima.core.logic.propositional.inference.OptimizedDPLL;
import aima.core.logic.propositional.inference.PLFCEntails;
import aima.core.logic.propositional.kb.KnowledgeBase;
import aima.core.logic.propositional.kb.data.ConjunctionOfClauses;
import aima.core.logic.propositional.parsing.PLParser;
import aima.core.logic.propositional.parsing.ast.ComplexSentence;
import aima.core.logic.propositional.parsing.ast.Connective;
import aima.core.logic.propositional.parsing.ast.PropositionSymbol;
import aima.core.logic.propositional.parsing.ast.Sentence;
import no.chess.web.model.Position;
import no.games.chess.AbstractGamePiece.pieceType;
import no.games.chess.ChessPieceType;

/**
 * ChessKnowledgeBase
 * This is a propositional knowledge base for chess.
 * This class contains propositional rules of the chessgame as created by the ChessAgent
 * This knowledgebase extends the aima.core.logic.propositional.kb.KnowledgeBase.
 * This knowledgebase contains facts about the current chess state (the chess percept).
 * It is created and maintained by the chess agent (AChessAgent).
 * Based on the facts known by the chess state rules are created and used to find the next best move for the player.
 * The knowledge base uses the Davis putnam algorithm for backward chaining to answer question like:
 * What shall I do now?. What is the best move now?
 *   
 * @author Oluf
 *
 */
public class ChessKnowledgeBase extends KnowledgeBase {

	public static final String TOPLAY = "TOPLAY";
	public static final String MAKEMOVE = "MAKEMOVE";
	private String ACTION = "ACTION";
	private String PROTECTED = "PROTECTEDBY";
	private String simpleProtected = "PROTECTED";
	private String ATTACKED = "ATTACKEDBY";
	private String CAPTURE = "CAPTURE";
	private String CONQUER = "CONQUER";
	private String THREATEN = "THREATENEDBY";
	private String PREFERPOS = "PREFER";
	private String PREFERPIECE = "PREFERPIECE";
	private String ownKnight = null;
	private String ownBishop = null;
	private String ownRook = null;
	private String ownQueen = null;
	private String ownKing = null;
	private String ownKnight1 = null;
	private String ownBishop1 = null;
	private String ownRook1 = null;
	private List<String> officers = null;
	private List<String> opponentPieces = null;
	private APlayer myPlayer = null;
	private APlayer opponent = null;
	private DPLL dpll; // The Davis putnam algorithm for backward chaining
/*
 * Forward chaining is a data-driven reasoning.
 * The focus of attention starts with the known data.
 * It can be used to derive conclusions from incoming percepts.
 * See p. 258	
 */
	private PLFCEntails forwardInference; // For forward chaining: See p. 258
	
    private long reasoningTime; // in milliseconds
    
    private ChessStateImpl stateImpl = null; // The percept for the knowledge base

    public ChessKnowledgeBase() {
		super();
		dpll = new OptimizedDPLL();
		forwardInference = new PLFCEntails();
		officers = new ArrayList<String>();
		
		
	}
    
    public String getOwnKnight() {
		return ownKnight;
	}

	public void setOwnKnight(String ownKnight) {
		if (this.ownKnight != null) {
			this.ownKnight1 = ownKnight;
			officers.add(ownKnight);
			return;
		}
		this.ownKnight = ownKnight;
		officers.add(ownKnight);
	}

	public String getOwnBishop() {
		return ownBishop;
	}

	public void setOwnBishop(String ownBishop) {
		if (this.ownBishop != null) {
			this.ownBishop1 = ownBishop;
			officers.add(ownBishop);
			return;
		}
		this.ownBishop = ownBishop;
		officers.add(ownBishop);
	}

	public String getOwnRook() {
		return ownRook;
	}

	public void setOwnRook(String ownRook) {
		if (this.ownRook != null) {
			this.ownRook1 = ownRook;
			officers.add(ownRook);
			return;
		}
		this.ownRook = ownRook;
		officers.add(ownRook);
	}

	public String getOwnQueen() {
		return ownQueen;
	}

	public void setOwnQueen(String ownQueen) {
		officers.add(ownQueen);
		this.ownQueen = ownQueen;
	}

	public String getOwnKing() {
		return ownKing;
	}

	public void setOwnKing(String ownKing) {
		this.ownKing = ownKing;
	}
	

	public List<String> getOpponentPieces() {
		return opponentPieces;
	}

	public void setOpponentPieces(List<String> opponentPieces) {
		this.opponentPieces = opponentPieces;
	}

	public boolean ask(Sentence query) {
        long tStart = System.currentTimeMillis();
        boolean result = dpll.isEntailed(this, query);
        reasoningTime += System.currentTimeMillis() - tStart;
        return result;
    }
	public PropositionSymbol newSymbol(String prefix, int timeStep) {
        return new PropositionSymbol(prefix + "_" + timeStep);
    }

	public ConjunctionOfClauses getAsCNF() {
		return getAsCNF();
	}


	public ChessStateImpl getStateImpl() {
		return stateImpl;
	}
	public void setStateImpl(ChessStateImpl stateImpl) {
		this.stateImpl = stateImpl;
		opponent = this.stateImpl.getOpponent();
		myPlayer = this.stateImpl.getMyPlayer();
	}
	public PropositionSymbol findSymbol(String name) {
		PropositionSymbol sample = new PropositionSymbol(name);
		Set<PropositionSymbol> mySymbols = getSymbols();
		return mySymbols.stream().filter(sample::equals).findAny().orElse(null);
	}

	public void tellPreferPosition(String pieceName,String pos,int t) {
		
	}
	/**
	 * tellCaptureRules
	 * This method creates general rules of how to capture an opponent piece
	 * @param t
	 * @param pos
	 * @param myName
	 */
	public void tellCaptureRules(int t,String pos,String myName) {
		for (String name:opponentPieces) {
			tell(new ComplexSentence(newSymbol(CAPTURE+name+"_AT"+pos,t),Connective.BICONDITIONAL,new ComplexSentence(newSymbol(ATTACKED+myName+"_"+pos,t),
					Connective.AND,newSymbol(name+"_AT"+pos,t))));
		}
	}
	/**
	 * tellmoveRule
	 * This method creates ACTION rules for a given piece
	 * @param first
	 * @param posName
	 * @param t
	 */
	public void tellmoveRule(PropositionSymbol first,String posName ,int t) {
		Sentence sentence =  newSymbol(posName+"_", t);
		
		ComplexSentence move = new ComplexSentence(first, Connective.BICONDITIONAL, sentence);
		tell(move);
	}

	/**
	 * findPreferableAction
	 * This method finds the best action to perform given the rules and precepts of the knowledge base.
	 * 
	 * @param actions
	 * @param t
	 * @return
	 */
	public Action findPreferableAction(List <ChessActionImpl> actions,int t) {
		for (ChessActionImpl action:actions) {
			if (action.getPossibleMove()!= null && !action.isBlocked()) {
				String name = action.getChessPiece().getMyPiece().getOntlogyName();
				action.processPositions();//This method recalculates removed positions for this action. Why is this necessary?
				AgamePiece piece = action.getChessPiece();
				String position = piece.getmyPosition().getPositionName();
				List<Position> removedList = action.getPositionRemoved();
				List<Position> availableList = action.getAvailablePositions();
				ApieceMove move = action.getPossibleMove();
				List<Position> preferredPositions = move.getPreferredPositions();
				String toPos = move.getToPosition().getPositionName();
				Position toPosition = move.getToPosition();

				for (Position availablePos:availableList) {
					if (!checkRemoved(availablePos,removedList)) {
						boolean officerMove = checkOfficers(name,position,toPos,t);
						toPos = availablePos.getPositionName();
						boolean capture = captureOpponent(toPos, name, t);
						move.setToPosition(availablePos);
						action.setPreferredPosition(availablePos);
					}
				}	
			}
		}
		return null;
	}
	/**
	 * captureOpponent
	 * This method asks if it is possible to capture an opponent piece
	 * @param pos - the position of the opponent
	 * @param myName - the name of the piece that can capture it
	 * @param t - the move number minus one?
	 * @return
	 */
	public boolean captureOpponent(String pos,String myName,int t) {
		boolean answer = false;
		for (String name:opponentPieces) {
			Sentence capture = (new ComplexSentence(newSymbol(CAPTURE+name+"_AT"+pos,t),Connective.BICONDITIONAL,new ComplexSentence(newSymbol(ATTACKED+myName+"_"+pos,t),
					Connective.AND,newSymbol(name+"_AT"+pos,t))));
			answer = ask(capture);
			if (answer)
				break;
		}
		return answer;
	}
	/**
	 * askPossibleAction
	 * This method determines which action to perform based on the available 
	 * facts and rules of the chess knowledge base
	 * A possible pseudocode:
	 * If action has a possible move and preferred position is occupied by an opponent piece 
	 * and opponent piece is not protected then perform action.
	 * If action 
	 * @param action - The action under consideration
	 * @param t - represents the move number
	 * @return
	 */
	public void askPossibleAction(Action action,int t) {
		ChessActionImpl localAction = (ChessActionImpl) action;
		String name = localAction.getChessPiece().getMyPiece().getOntlogyName();
		localAction.processPositions();//This method recalculates removed positions for this action. Why is this necessary?
		AgamePiece piece = localAction.getChessPiece();
		pieceType type = piece.getPieceType();
		
		String position = piece.getmyPosition().getPositionName();
		List<Position> removedList = localAction.getPositionRemoved();
		List<Position> availableList = localAction.getAvailablePositions();
		ApieceMove move = localAction.getPossibleMove();
		List<Position> preferredPositions = move.getPreferredPositions();
		String toPos = move.getToPosition().getPositionName();
		Position toPosition = move.getToPosition();
		if (type == type.PAWN ){
			boolean center = toPosition.isCenterlefthigh()||toPosition.isCenterleftlow()||toPosition.isCenterrighthigh()||toPosition.isCenterrightlow();
			String moveNotation = move.getMoveNotation();
			if (moveNotation == null || moveNotation.equals(""))
				moveNotation ="MOV";
			
			Sentence sentence = newSymbol(ACTION+name+"_"+"AT"+position, t);
			boolean answer = ask(sentence);
			if (center && answer&& t <= 2)
				localAction.setActionValue(new Integer(20));
			if (!center && answer && t > 2)
				localAction.setActionValue(new Integer(15));
	
		}
		if (type != type.PAWN) {
			boolean officerMove = checkOfficers(name,position,toPos,t);
			if(!officerMove) {	// Is true if piece can move and is protected
				for (Position availablePos:availableList) {
					if (!checkRemoved(availablePos,removedList)) {
						toPos = availablePos.getPositionName();
						officerMove = checkOfficers(name,position,toPos,t);
						if(officerMove) {
							move.setToPosition(availablePos);
							localAction.setPreferredPosition(availablePos);
							int v = piece.getMyPiece().getValue();
							int nv = 20;
							boolean center = availablePos.isCenterlefthigh()||toPosition.isCenterleftlow()||toPosition.isCenterrighthigh()||toPosition.isCenterrightlow();
							if (center)
								nv = 25;
							localAction.setActionValue(new Integer(nv+v));
							if (center)
								break;
						}

					}

				}
			}
		}			

	}
	/**
	 * checkOfficers
	 * This method checks if an officer can be moved to a safe position.
	 * The safe position cannot be protected by itself
	 * Must find if the safe position is only protected by itself.
	 * The safe position cannot be attacked by an opponent officer
	 * @param name
	 * @param fromPos
	 * @param toPos
	 * @param t
	 * @return
	 */
	public boolean checkOfficers(String name,String fromPos,String toPos,int t) {
		boolean pieceprotected = false;
		List<AgamePiece> pieces = myPlayer.getMygamePieces();
		for (AgamePiece piece:pieces) {
			String pieceName = piece.getMyPiece().getOntlogyName();
			Position position = piece.getmyPosition();
			if (!pieceName.equals(name)) {
				Sentence pieceProtected =  newSymbol(PROTECTED+pieceName+"_"+toPos,t);
				pieceprotected = ask(pieceProtected);
			}

		}	
		Sentence sentence = newSymbol(ACTION+name+"_"+"AT"+fromPos, t);
		boolean possible = false;
		boolean action = ask(sentence);
		possible = action && pieceprotected;
		return possible;
	}
	public boolean checkRemoved(Position pos,List<Position> removedList) {
		boolean removed = false;
		for (Position rpos:removedList) {
			if (rpos.equals(pos))
				return true;
			
		}
		return removed;
	}
	public Position newtoPosition(List<Position> availableList,List<Position> removedList) {
		
		return null;
	}
	/**
	 * checkOpponentavailable
	 * This method checks to see if an opponent piece can be taken
	 * @param availableList
	 * @param removedList
	 * @param t
	 * @return
	 */
	public boolean checkOpponentavailable(List<Position> availableList,List<Position> removedList, int t) {
		List<AgamePiece> pieces = opponent.getMygamePieces();
		boolean avail = false;
		boolean removed = false;
		for (Position available:availableList) {
			for (AgamePiece piece:pieces) {
				String name = piece.getMyPiece().getOntlogyName();
				Position position = piece.getmyPosition();
				if (position == available) {
					String posname = piece.getmyPosition().getPositionName();
					Sentence sentence = newSymbol(name+"_"+"AT"+posname, t);
					boolean answer = ask(sentence);
					avail = answer;
					break;
				}

			}			
		}
		if (avail) {
			for (Position notavailable:removedList) {
				for (AgamePiece piece:pieces) {
					String name = piece.getMyPiece().getOntlogyName();
					Position position = piece.getmyPosition();
					if (position == notavailable) {
						String posname = piece.getmyPosition().getPositionName();
						Sentence sentence = newSymbol(name+"_"+"AT"+posname, t);
						boolean answer = ask(sentence);
						removed = answer;
						break;
					}

				}	
		}

	  }
		return avail && !removed;
	}
}
