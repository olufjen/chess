package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import aima.core.logic.fol.domain.FOLDomain;
import aima.core.logic.fol.inference.InferenceProcedure;
import aima.core.logic.fol.inference.InferenceResult;
import aima.core.logic.fol.inference.proof.Proof;
import aima.core.logic.fol.kb.FOLKnowledgeBase;
import aima.core.logic.fol.parsing.FOLParser;
import aima.core.logic.fol.parsing.ast.AtomicSentence;
import aima.core.logic.fol.parsing.ast.Constant;
import aima.core.logic.fol.parsing.ast.Predicate;
import aima.core.logic.fol.parsing.ast.Sentence;
import aima.core.logic.fol.parsing.ast.Term;
import aima.core.logic.fol.parsing.ast.Variable;
import no.chess.web.model.Position;
import no.games.chess.fol.BCGamesAskHandler;
import no.games.chess.fol.FCGamesAskAnswerHandler;
import no.games.chess.fol.FOLGamesBCAsk;
import no.games.chess.fol.FOLGamesFCAsk;

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
	private String outputFileName = "";
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
	private ChessDomain localDomain;
	// method is called.
	
	public ChessFolKnowledgeBase(ChessDomain domain, InferenceProcedure inferenceProcedure) {
		super(domain, inferenceProcedure);
		String catalog = KnowledgeBuilder.getFileCatalog();
		fileName = "tempknowledgebase.txt";
		outputFileName = catalog+outputFileName+this.fileName;
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
		localDomain = domain;
		String catalog = KnowledgeBuilder.getFileCatalog();
		this.fileName = fileName;
		outputFileName = catalog+outputFileName+this.fileName;
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
	
	/**
	 * checkNumberofparams
	 * This method return the number of parameters for a given predicate
	 * @param name
	 * @return
	 */
	public int checkNumberofparams(String name) {
		List<Sentence> sentences = this.getOriginalSentences();
		int antPar = 0;
		for (Sentence sentence:sentences) {
			if( sentence instanceof Predicate) {
				Predicate pred = (Predicate)sentence;
				if (name.equals(pred.getPredicateName())){
					antPar = pred.getTerms().size();
					/*
					 * if (name.equals("PAWN")) { System.out.println("The pawn found"); }
					 */
					break;
				}
			}
		}
		return antPar;
	}
	
	public String getOutputFileName() {
		return outputFileName;
	}
	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}
	public ChessDomain getLocalDomain() {
		return localDomain;
	}
	public void setLocalDomain(ChessDomain localDomain) {
		this.localDomain = localDomain;
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
	 * checkQuery
	 * Asks a query to the knowledge base using backward chaining
	 * @param stringQuery
	 * @return a List of Strings containg term names
	 */
	public List<String> checkQuery(String stringQuery){
		FOLParser parser = getParser();
		Sentence query = parser.parse(stringQuery);
		InferenceResult backWardresult =  backWardChain.ask(this,query);
		BCGamesAskHandler handler = (BCGamesAskHandler)backWardresult;
		List<HashMap<Variable, Term>> finals = handler.getFinalList();
		int noofFinals = finals.size();
		HashMap<Variable, Term> vars = null;
		String termName = null;
		List<String> termNames = new ArrayList<String>();
		if (finals != null && !finals.isEmpty()) {
			for (int i = 0;i<noofFinals;i++) {
				vars = finals.get(i);
				for (Variable v:vars.keySet()) {
					if(!v.getSymbolicName().startsWith("v")) {
						Term term = vars.get(v);
						termName = term.getSymbolicName(); // What is the name of this object?
						termNames.add(termName);
					}
				}
			}
		}
		return termNames;
	}
	/**
	 * checkKB
	 * This method test if a certain query returns true
	 * @param thequery
	 * @return true if the query returns a result.
	 * Otherwise it returns false
	 */
	public boolean checkKB(String thequery) {
		FOLParser parser = getParser();
		Sentence query = parser.parse(thequery);
		InferenceResult backWardresult =  backWardChain.ask(this,query);
		BCGamesAskHandler handler = (BCGamesAskHandler)backWardresult;
		List<HashMap<Variable, Term>> finals = handler.getFinalList();
		HashMap<Variable, Term> vars = null;
		boolean backresult = backWardresult.isTrue();
		int noofFinals = finals.size();
		if (finals != null && !finals.isEmpty()) {
			for (int i = 0;i<noofFinals;i++) {
				vars = finals.get(i);
				for (Variable v:vars.keySet()) {
					if(!v.getSymbolicName().startsWith("v")) {
						return true;
					}
				}
			}
		}
		return false;
	}
	/**
	 * forwardcheckQuery
	 * Asks a query to the knowledge base using forward chaining
	 * @param stringQuery
	 * @return a List of stringa of given term names
	 */
	public List<String>forwardcheckQuery(String stringQuery){
		FOLParser parser = getParser();
		Sentence query = parser.parse(stringQuery);
	
		FOLGamesFCAsk forwardChain = (FOLGamesFCAsk) getInferenceProcedure();
		InferenceResult forWardresult =  forwardChain.ask(this,query);
		FCGamesAskAnswerHandler handler = (FCGamesAskAnswerHandler)forWardresult;
		List<Proof> proofs = handler.getProofs();
		String termName = null;
		List<String> termNames = new ArrayList<String>();
		for (Proof proof:proofs) {
		    Map<Variable, Term> bindings = proof.getAnswerBindings();
		    for (Variable var : bindings.keySet()) {
		        // Ignorer interne variabler som v1, v2, v14 osv.
		        if (!var.getSymbolicName().startsWith("v")) {
					Term term =  bindings .get(var);
					termName = term.getSymbolicName(); // What is the name of this object?
					termNames.add(termName);
		        }
		    }

		}
		return termNames;
	}
	  /**
	   * checkThreats
	   * This method checks the FOL knowledge base for certain facts about the opponent's pieces.
	   * These facts can be any of the available predicates in the FOL Domain (see the domain object)
	   * The APlayer parameter must always be the opponent
	   * @since 29.07.24
	   * Using the occupies fact OBS!
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
		  boolean result = false;
		  boolean tempResult = false;
		  List<Term> reachableTerms = new ArrayList<Term>();
		  if (piece != null && piece.isActive()) {
			  pieceVariable = new Constant(pieceName);
			  reachableTerms.add(pieceVariable);
			  tempResult = pos.equals(piece.getMyPosition().getPositionName()) && fact.equals("occupies"); // OBS OJN 29.07.24
		  }else if(piece == null) {
			  pieceVar = new Variable(pieceName);
			  reachableTerms.add(pieceVar);
		  }
		  Constant posVariable = new Constant(pos);
		  reachableTerms.add(posVariable);
		  Predicate threatPredicate = new Predicate(fact,reachableTerms);
//		  writer.println("Trying to prove\n"+threatPredicate.toString());
		  InferenceResult backWardresult =  backWardChain.ask(this,threatPredicate);
		  boolean backresult = backWardresult.isTrue();
		
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
		  if (!result && backresult) {
			  result = backresult;
			  writer.println("This predicate has no answer "+ backresult);
			  writer.println(threatPredicate.toString());
/*			  if (tempResult) {
				  writeKnowledgebase();
			  }*/

		  }
		  return result;
		  
	  }
	/**
	 * isStartPhase
	 * This method checks if the game is in the start phase:
	 * No pieces have been moved
	 * @return true if the game is in the start phase
	 */
	public boolean isStartPhase() {
	    // 1. Hent alle brikker som har en definert HOMESQUARE
	    // Vi ser etter alle fakta på formen HOMESQUARE(brikke, felt)
		String home = KnowledgeBuilder.getHOMESQUARE();
		String occupies = KnowledgeBuilder.getOCCUPIES();
	    for (Sentence s : getOriginalSentences()) {
	        if (s instanceof Predicate && ((Predicate) s).getPredicateName().equals(home)) {
	            Predicate p = (Predicate) s;
	            String piece = p.getArgs().get(0).getSymbolicName();
	            String homeSquare = p.getArgs().get(1).getSymbolicName();

	            // 2. Sjekk om denne spesifikke brikken står på sitt hjemmefelt
	            if (!existsFact(occupies, piece, homeSquare)) {
	                // Hvis vi finner én brikke som ikke er hjemme, er startfasen over
	                return false;
	            }
	        }
	    }
	    
	    // Hvis vi har gått gjennom alle og ikke funnet noen som har flyttet, er vi i startfasen
	    return true;
	}
	/**
	 * existsFact
	 *  This method checks if certain facts in the KB are true.
	 * @param fact - the fact to be checked
	 * @param preds - any number of parameters for the given fact 
	 * @return true if the fact is true
	 */
	public boolean existsFact(String predicateName, String... args) {
	    for (Sentence s : getOriginalSentences()) {
	        if (s instanceof Predicate) {
	            Predicate p = (Predicate) s;
	            
	            // 1. Sjekk predikatnavn og antall argumenter (arity)
	            if (p.getPredicateName().equals(predicateName) && p.getArgs().size() == args.length) {
	                
	                boolean match = true;
	                // 2. Sammenlign hvert argument
	                for (int i = 0; i < args.length; i++) {
	                    String actualArg = p.getArgs().get(i).getSymbolicName();
	                    if (!actualArg.equals(args[i])) {
	                        match = false;
	                        break;
	                    }
	                }
	                
	                // 3. Hvis alle argumentene matchet, har vi funnet faktumet
	                if (match) {
	                    return true;
	                }
	            }
	        }
	    }
	    return false;
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
	private boolean checkAnswer(List<HashMap<Variable, Term>> finals,Variable pieceVarx) {
		int noofFinals = finals.size();
		HashMap vars = null;
		Term usedTerm = null;
		if (finals != null && !finals.isEmpty()) {
			for (int i = 0;i<noofFinals;i++) {
				vars = finals.get(i);
				usedTerm = (Term) vars.get(pieceVarx);
				if (usedTerm != null) {  //  Check if usedTerm is null !!
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * searchFacts
	 * This method returns a list of term names (ontology names of pieces)
	 * It is used to find which piece(s) can reach/protect/threaten/occupy a given position
	 * @since 21.05.26 Check if usedTerm is null !!
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
				if (usedTerm != null) {  //  Check if usedTerm is null !!
					termName = usedTerm.getSymbolicName(); // Finds which piece is protecting this position.
					termNames.add(termName);
				}
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
	public void createsinglefacts(String fact, String piece) {
		Constant pieceVariable= new Constant(piece);
		List<Term> reachableTerms = new ArrayList<Term>();
		reachableTerms.add(pieceVariable);
		Predicate factPredicate = new Predicate(fact,reachableTerms);
		tell(factPredicate);
		
	}
	/**
	 * createsinglefacts
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
	
	/**
	 * getFacts()
	 * This method returns all facts from the kb
	 * @return - A list of facts - Predicates from the kb
	 */
	public List<Predicate> getFacts(){
	    List<Predicate> facts = new ArrayList<>();
	    
	    // getOriginalSentences() returnerer alle setninger som er lagt til 
	    // eller utledet via tell() i kunnskapsbasen.
	    for (Sentence s : getOriginalSentences()) {
	        // Vi sjekker om setningen er et Predicate (f.eks. CONTROLCENTER(WhitePawn4, d4))
	        // og ikke en regel (som f.eks. ConnectedSentence / Implication)
	        if (s instanceof Predicate) {
	            facts.add((Predicate) s);
	        }
	    }
		return facts;
		
	}
	/**
	 * cloneOrCopy
	 * This method makes a clone of the knowledge base
	 * @return The cloned Knowledge base
	 */
	public ChessFolKnowledgeBase cloneOrCopy() {
	    // 1. Opprett en helt ny instans av kunnskapsbasen med samme parser og inferensmotor
	    // (Bruk de samme parameterne som du opprinnelig instansierte KB-en din med)
	    ChessFolKnowledgeBase newKB = new ChessFolKnowledgeBase(this.getLocalDomain(), this.getInferenceProcedure(),"clonecopy.txt");
	    newKB.setBackWardChain(backWardChain);
	    // 2. Gjør en dyp kopiering av alle setninger (fakta og regler)
	    // AIMA lagrer originale setninger i en liste vi kan hente ut
		
		  for (Sentence sentence : this.getOriginalSentences()) { // Vi bruker AIMA sin
//		  innebygde parser/substitusjon til å lage en uavhengig kopi // av hver
//		  setning, slik at de ikke deler minnereferanser på muterbare objekter
			  newKB.tell(sentence.copy());
		  }
		 

	    return newKB;
	}
    /**
     * retract
     * THis method removes negative sentences from the knowledge base
     * @param sentenceToRemove
     */
    public void retract(AtomicSentence sentenceToRemove) {
        // 1. Hent ut en kopi av alle nåværende setninger i KB-en
        List<Sentence> currentSentences = new ArrayList<>(this.getOriginalSentences());
        boolean removed = false;

        // 2. Finn setningen som matcher strukturelt og fjern den fra listen
        // Vi bruker toString() eller en strukturell sjekk for å sikre treff uavhengig av objektreferanse
        String targetStr = sentenceToRemove.toString();
        
        for (int i = 0; i < currentSentences.size(); i++) {
            if (currentSentences.get(i).toString().equals(targetStr)) {
                currentSentences.remove(i);
                removed = true;
                break; // Vi antar at fakta er unike, så vi stopper ved første treff
            }
        }

        // 3. Hvis vi faktisk fant og fjernet setningen, må vi tvinge KB-en til å oppdatere seg
        if (removed) {
            this.clear(); // Tømmer KB-ens interne indekser og cacher fullstendig
            
            // 4. Gjenskap kunnskapsbasen med de gjenværende setningene
            for (Sentence remainingSentence : currentSentences) {
                this.tell(remainingSentence);
            }
        }
    }
}
