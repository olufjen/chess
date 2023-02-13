package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import aima.core.logic.fol.domain.FOLDomain;
import aima.core.logic.fol.inference.InferenceProcedure;
import aima.core.logic.fol.inference.InferenceResult;
import aima.core.logic.fol.kb.FOLKnowledgeBase;
import aima.core.logic.fol.parsing.ast.Constant;
import aima.core.logic.fol.parsing.ast.Predicate;
import aima.core.logic.fol.parsing.ast.Term;
import aima.core.logic.fol.parsing.ast.Variable;
import no.chess.web.model.Position;
import no.games.chess.fol.BCGamesAskHandler;
import no.games.chess.fol.FOLGamesBCAsk;

/**
 * The  ChessFolKnowledgeBase is a subclass of the FOLKnowledgeBase.
 * It is used to construct a First order knowledge base to search for the best move.
 * Two separate First order knowledge bases are maintained. The parent knowledge base and the
 * strategy knowledge base. The strategy knowledge base contains knowledge about chess state one ply ahead.
 * It is filled with facts about possible new reachable positions and facts about opponent positions.
 * Then we can determine which opponent pieces I can capture based on which move I make.
 * The ChessFolKnowledgebases are created in the execute method of the Chess Agent  and when the opponent agent is created
 * @author oluf
 *
 */
public class ChessFolKnowledgeBase extends FOLKnowledgeBase {
	private PrintWriter writer = null;
	private FileWriter fw = null;
	private String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\";
	private String fileName ="";
	private FOLGamesBCAsk backWardChain;
	private String PAWN;
	private String KNIGHT;
	private String BISHOP;
	private String ROOK;
	private String KING;
	private String QUEEN;
	private List<String>pieceTypes;
	private List<AgamePiece> movePieces; // A list of pieces actively involved in a possible move. This list is set when the checkthreats
	// method is called.
	
	public ChessFolKnowledgeBase(ChessDomain domain, InferenceProcedure inferenceProcedure) {
		super(domain, inferenceProcedure);
		fileName = "tempknowledgebase.txt";
		outputFileName = outputFileName+this.fileName;
		movePieces = new ArrayList<AgamePiece>();
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
	    setPieceTypes();
	 
	}
	public ChessFolKnowledgeBase(ChessDomain domain, InferenceProcedure inferenceProcedure,String fileName) {
		super(domain, inferenceProcedure);
		this.fileName = fileName;
		outputFileName = outputFileName+this.fileName;
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));
		movePieces = new ArrayList<AgamePiece>();
	    setPieceTypes();
	}
	private void setPieceTypes() {
		PAWN = KnowledgeBuilder.getPAWN();
		KNIGHT = KnowledgeBuilder.getKNIGHT();
		BISHOP = KnowledgeBuilder.getBISHOP();
		ROOK = KnowledgeBuilder.getROOK();
		KING = KnowledgeBuilder.getKING();
		QUEEN = KnowledgeBuilder.getQUEEN();
		pieceTypes = new ArrayList<String>();
		pieceTypes.add(PAWN);
		pieceTypes.add(BISHOP);
		pieceTypes.add(KNIGHT);
		pieceTypes.add(ROOK);
		pieceTypes.add(KING);
		pieceTypes.add(QUEEN);
	}
	
	public List<AgamePiece> getMovePieces() {
		return movePieces;
	}
	public void setMovePieces(List<AgamePiece> movePieces) {
		this.movePieces = movePieces;
	}
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public FOLGamesBCAsk getBackWardChain() {
		return backWardChain;
	}
	public void setBackWardChain(FOLGamesBCAsk backWardChain) {
		this.backWardChain = backWardChain;
	}
	/**
	 * checkPieceType
	 * This method checks what type of piece a chesspiece is
	 * @param name The name of the piece
	 * @return the type of the piece
	 */
	public String checkPieceType(String name) {
		String foundType = null;
		Constant pieceVariable= new Constant(name);
		List<Term> typeTerms = new ArrayList<Term>();
		for (String type:pieceTypes) {
			Predicate typePredicate = new Predicate(type,typeTerms);
			InferenceResult backWardresult =  backWardChain.ask(this, typePredicate);
			if (backWardresult.isTrue()) {
				foundType = type;
				return foundType;
			}
		}
		return foundType;
	}
/**
 * checkmyProtection
 * This method checks if a piece is protected by other pieces than by itself
 * @param pieceName
 * @param pos
 * @return true if it is protected
 */
public boolean checkmyProtection(String pieceName,String pos,String predName,APlayer myPlayer) {
	  List<AgamePiece> myPieces = myPlayer.getMygamePieces();
	  boolean protectedpiece = false;
	  for (AgamePiece piece:myPieces) {
		  String name = piece.getMyPiece().getOntlogyName();
		  if (!name.equals(pieceName)) {
				Constant pieceVariable= new Constant(name);
				Constant posVariable = new Constant(pos);
				List<Term> reachableTerms = new ArrayList<Term>();
				reachableTerms.add(pieceVariable);
				reachableTerms.add(posVariable);
				Predicate reachablePredicate = new Predicate(predName,reachableTerms);
				InferenceResult backWardresult =  backWardChain.ask(this, reachablePredicate);
				protectedpiece = backWardresult.isTrue();
				if (protectedpiece)
					return protectedpiece;
		  }
	  }
	return protectedpiece;
}
	
	  /**
	   * checkThreats
	   * This method checks the FOL knowledge base for certain facts about the opponent's pieces.
	   * These facts can be any of the available predicates in the FOL Domain (see the domain object)
	   * The APlayer parameter must always be the opponent
	 * @param pieceName
	 * @param pos
	 * @param fact
	 * @param opponent. The opponent player
	 * @return true if the fact is true
	 */
	public boolean checkThreats(String pieceName,String pos,String fact,APlayer opponent ) {
		  movePieces.clear();	
		  List<AgamePiece> pieces = opponent.getMygamePieces();
		  AgamePiece piece = pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().equals(pieceName)).findAny().orElse(null);
		  Constant pieceVariable = null;
		  Variable pieceVar = null;
		  List<Term> reachableTerms = new ArrayList<Term>();
		  if (piece != null && piece.isActive()) {
			  pieceVariable = new Constant(pieceName);
			  reachableTerms.add(pieceVariable);
		  }else if(piece == null) {
			  pieceVar = new Variable(pieceName);
			  reachableTerms.add(pieceVar);
		  }
		  Constant posVariable = new Constant(pos);
		  reachableTerms.add(posVariable);
		  Predicate threatPredicate = new Predicate(fact,reachableTerms);
//		  writer.println("Trying to prove\n"+threatPredicate.toString());
		  InferenceResult backWardresult =  backWardChain.ask(this,threatPredicate);
//		  boolean result = backWardresult.isTrue();
		  boolean result = false;
//		  writer.println(InferenceResultPrinter.printInferenceResult(backWardresult));
/*
 * Added 28.5.22:
 */
		  List<String> termNames = new ArrayList<String>();
		  BCGamesAskHandler handler = (BCGamesAskHandler)backWardresult;
		  List<HashMap<Variable, Term>> finals = handler.getFinalList();
		  int noofFinals = finals.size();
		  HashMap vars = null;
		  Term usedTerm = null;
		  String termName = null;
		  if (finals != null && !finals.isEmpty() && pieceVar != null) {
			  for (int i = 0;i<noofFinals;i++) {
				  vars = finals.get(i);
				  usedTerm = (Term) vars.get(pieceVar);
				  termName = usedTerm.getSymbolicName(); // Finds which piece(s) is protecting/threatening this position.
				  termNames.add(termName);
			  }
			  for (String name:termNames) {
				  AgamePiece opponentpiece = pieces.stream().filter(c -> c.getMyPiece().getOntlogyName().equals(name)).findAny().orElse(null);
				  if (opponentpiece != null && opponentpiece.isActive()) {
					  movePieces.add(opponentpiece);
					  result = true;
				  }
			  }
		  }
		  return result;
		  
	  }	
	  /**
	   * checkpieceFacts
	   * This method checks the FOL knowledge base for certain facts about the player's pieces.
	   * These facts can be any of the available predicates in the FOL Domain (see the domain object)
	   * It is called from the ProblemSolver's checkOpponent, prepareAction, and checkCastling methods.
	   * @since 22.10.21 The list of actions are not used
	 * @param pieceVar If pieceVar has value x, the pieceName is of type Variable to the predicate
	 * @param pieceName The name of the piece
	 * @param pos The position to move to
	 * @param fact The predicate fact
	 * @param actions All the actions available to the player - removed 22.10.21
	 */
	public boolean checkpieceFacts(String pieceVar,String pieceName,String pos,String fact) {
			Variable pieceVarx = null;
			if (pieceVar.equals("x"))
				pieceVarx = new Variable(pieceVar);
			Constant pieceVariable= new Constant(pieceName);
			Constant posVariable = new Constant(pos);
			List<Term> reachableTerms = new ArrayList<Term>();
			if (pieceVarx != null) {
				reachableTerms.add(pieceVarx);
			}else
				reachableTerms.add(pieceVariable);
			reachableTerms.add(posVariable);
			Predicate reachablePredicate = new Predicate(fact,reachableTerms);
//			writer.println("PieceFacts Trying to prove\n"+reachablePredicate.toString());
			InferenceResult backWardresult =  backWardChain.ask(this, reachablePredicate);
			BCGamesAskHandler handler = (BCGamesAskHandler)backWardresult;
			HashMap vars = null;
			Term usedTerm = null;
			String termName = null;
			boolean properProtection = false;
			List<HashMap<Variable, Term>> finals = handler.getFinalList();
			if (finals != null && !finals.isEmpty() && pieceVarx != null) {
				vars = finals.get(0);
				usedTerm = (Term) vars.get(pieceVarx);
				termName = usedTerm.getSymbolicName(); // Finds which piece is protecting this position. This is only true if fact is PROTECTEDBY
				properProtection = !termName.equals(pieceName);
//				writer.println("PieceFacts: position "+pos+" protected by "+termName+" and reachable by "+pieceName);
				return properProtection;
			}
			return backWardresult.isTrue();

	  }	
	  /**
	   * checkFacts
	   * This method checks the FOL knowledge base for certain facts about a player's pieces.
	   * These facts can be any of the available predicates in the FOL Domain (see the domain object)
	   * The parameter pos is used to give the chosen action a new position to move to.
	 * @param pieceName The name of the piece
	 * @param pos The position to move to
	 * @param fact The predicate fact
	 * @param actions All the actions available to the player
	 */
	public boolean checkFacts(String pieceName,String pos,String fact,List<ChessActionImpl> actions,List<Position>positionList) {
			Constant pieceVariable= new Constant(pieceName);
			Constant posVariable = new Constant(pos);
			List<Term> reachableTerms = new ArrayList<Term>();
			reachableTerms.add(pieceVariable);
			reachableTerms.add(posVariable);
			Predicate reachablePredicate = new Predicate(fact,reachableTerms);
			InferenceResult backWardresult =  backWardChain.ask(this, reachablePredicate);
		    ChessActionImpl naction =  (ChessActionImpl) actions.stream().filter(c -> c.getActionName().contains(pieceName)).findAny().orElse(null);
		    Position position =  (Position) positionList.stream().filter(c -> c.getPositionName().contains(pos)).findAny().orElse(null);
			if (backWardresult.isTrue() && naction != null) {
				if (naction.getPossibleMove() == null) {
					writer.println("Action "+naction.toString()+ " has no move");
					return false;
				}
				naction.getPossibleMove().setToPosition(position);
				naction.setPreferredPosition(position);
				return true;
			}
			return false;
	  }	
	/**
	 * searchFacts
	 * This method returns a list of term names (ontology names of pieces)
	 * It is used to find which piece(s) can reach/protect/threaten/occupy a given position
	 * @param pieceName
	 * @param posName
	 * @param fact
	 * @return a List of term names (or empty list)
	 */
	public List<String> searchFacts(String pieceName,String posName,String fact) {
		Variable pieceVarx = null;
		Constant pieceVariable = null;
		List<String> termNames = new ArrayList<String>();
		List<Term> reachableTerms = new ArrayList<Term>();
		if (pieceName.equals("x")) {
			pieceVarx = new Variable(pieceName);
			reachableTerms.add(pieceVarx);
		}else {
			pieceVariable = new Constant(pieceName);
			reachableTerms.add(pieceVariable);
		}
		Constant posVariable = new Constant(posName);
		reachableTerms.add(posVariable);
		Predicate reachablePredicate = new Predicate(fact,reachableTerms);
//		writer.println("PieceFacts Trying to prove\n"+reachablePredicate.toString());
		InferenceResult backWardresult =  backWardChain.ask(this, reachablePredicate);
		BCGamesAskHandler handler = (BCGamesAskHandler)backWardresult;
		HashMap vars = null;
		Term usedTerm = null;
		String termName = null;
		List<HashMap<Variable, Term>> finals = handler.getFinalList();
		int noofFinals = finals.size();
		if (finals != null && !finals.isEmpty() && pieceVarx != null) {
			for (int i = 0;i<noofFinals;i++) {
				vars = finals.get(i);
				usedTerm = (Term) vars.get(pieceVarx);
				termName = usedTerm.getSymbolicName(); // Finds which piece is protecting this position.
				termNames.add(termName);
			}
		}
		return termNames;
	}
	/**
	 * createfacts
	 * This method creates fact about a piece and its position to the knowledge base
	 * @param fact
	 * @param pos
	 * @param piece
	 */
	public void createfacts(String fact,String pos, String piece) {
		Constant pieceVariable= new Constant(piece);
		Constant posVariable = new Constant(pos);
		List<Term> reachableTerms = new ArrayList<Term>();
		reachableTerms.add(pieceVariable);
		reachableTerms.add(posVariable);
		Predicate factPredicate = new Predicate(fact,reachableTerms);
		tell(factPredicate);
		
	}
	public void writeKnowledgebase() {
		writer.println("The first order knowledge base");
		writer.println(this.toString());
		writer.flush();
		
	}
	/**
	 * findFacts
	 * This method returns a list of names of pieces that can reach a certain position
	 * @param name - The name of the position
	 * @param fact - The fact to query the KB 
	 * @return - A list containing the name of the pieces
	 */
	public List<String> findFacts(String name,String fact) {
		Constant nameConstant = new Constant(name);
		Variable nameVar = new Variable("x");
		List<Term> reachableTerms = new ArrayList<Term>();
		List<String> termNames = new ArrayList<String>();
		reachableTerms.add(nameVar);
		reachableTerms.add(nameConstant);
		Predicate factPredicate = new Predicate(fact,reachableTerms);
		InferenceResult backWardresult =  backWardChain.ask(this, factPredicate);
		BCGamesAskHandler handler = (BCGamesAskHandler)backWardresult;
		HashMap vars = null;
		Term usedTerm = null;
		String termName = null;
		List<HashMap<Variable, Term>> finals = handler.getFinalList();
		int noofFinals = finals.size();
		if (finals != null && !finals.isEmpty() && nameConstant != null) {
			for (int i = 0;i<noofFinals;i++) {
				vars = finals.get(i);
				usedTerm = (Term) vars.get(nameVar);
				termName = usedTerm.getSymbolicName(); // Finds which name of piece
				termNames.add(termName);
			}
		}
		return termNames;
	}
	/**
	 * checkPosition
	 * This method checks the position of a given piece
	 * @param name - the name of the piece
	 * @param fact - the fact is the term occupies
	 * @return The name of the position
	 */
	public String checkPosition(String name,String fact) {
		Constant pieceVariable= new Constant(name);
		Variable posVar = new Variable("x");
		List<Term> reachableTerms = new ArrayList<Term>();
		reachableTerms.add(pieceVariable);
		reachableTerms.add(posVar);
		Predicate factPredicate = new Predicate(fact,reachableTerms);
		InferenceResult backWardresult =  backWardChain.ask(this, factPredicate);
		BCGamesAskHandler handler = (BCGamesAskHandler)backWardresult;
		HashMap vars = null;
		Term usedTerm = null;
		String termName = null;
		List<HashMap<Variable, Term>> finals = handler.getFinalList();
		int noofFinals = finals.size();
		if (finals != null && !finals.isEmpty() && pieceVariable != null) {
			for (int i = 0;i<noofFinals;i++) {
				vars = finals.get(i);
				usedTerm = (Term) vars.get(posVar);
				termName = usedTerm.getSymbolicName(); // Finds which position this is used.
//				termNames.add(termName);
			}
		}
		return termName;
	}
	/**
	 * searchKing
	 * Given the name predicate this method returns a list of positions
	 * from which the opponent king can be taken
	 * @param namePredicate QUEEN,BISHOP,ROOK,KNIGHT,PAWN
	 * @param kingPos
	 * @return A list of positions
	 */
	public List<String> searchKing(String namePredicate,String kingPos){
		Constant kingPosition = new Constant(kingPos);
		Variable posVar = new Variable("x");
		List<String> termNames = new ArrayList<String>();
		List<Term> reachableTerms = new ArrayList<Term>();
		reachableTerms.add(posVar);
		reachableTerms.add(kingPosition);
		Predicate factPredicate = new Predicate(namePredicate,reachableTerms);
		InferenceResult backWardresult =  backWardChain.ask(this, factPredicate);
		BCGamesAskHandler handler = (BCGamesAskHandler)backWardresult;
		HashMap vars = null;
		Term usedTerm = null;
		String termName = null;
		List<HashMap<Variable, Term>> finals = handler.getFinalList();
		int noofFinals = finals.size();
		if (finals != null && !finals.isEmpty() && kingPosition != null) {
			for (int i = 0;i<noofFinals;i++) {
				vars = finals.get(i);
				usedTerm = (Term) vars.get(posVar);
				termName = usedTerm.getSymbolicName(); // Finds which position this is used.
				termNames.add(termName);
			}
		}
		return termNames;
	}
}
