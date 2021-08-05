package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import aima.core.logic.fol.domain.FOLDomain;
import aima.core.logic.fol.inference.InferenceProcedure;
import aima.core.logic.fol.kb.FOLKnowledgeBase;
import aima.core.logic.fol.parsing.ast.Constant;
import aima.core.logic.fol.parsing.ast.Predicate;
import aima.core.logic.fol.parsing.ast.Term;
import aima.core.logic.fol.parsing.ast.Variable;

/**
 * The  ChessFolKnowledgeBase is a subclass of the FOLKnowledgeBase.
 * It is used to construct a local knowledge base to search for the best move.
 * It is filled with facts about possible new reachable positions and facts about opponent positions.
 * Then we can determine which opponent pieces I can capture based on which move I make.
 * 
 * @author oluf
 *
 */
public class ChessFolKnowledgeBase extends FOLKnowledgeBase {
	private PrintWriter writer = null;
	private FileWriter fw = null;
	private String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\tempknowledgebase.txt";
	public ChessFolKnowledgeBase(FOLDomain domain, InferenceProcedure inferenceProcedure) {
		super(domain, inferenceProcedure);
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
	}

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
}
