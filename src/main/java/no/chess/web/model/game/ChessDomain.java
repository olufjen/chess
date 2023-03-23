package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import aima.core.logic.fol.domain.FOLDomain;

/**
 * This is the subclass of the First order logic FOLDomain class for the chess game domain.
 * The models of the First Order Logic are the formal structures that constitutes the 
 * possible world under consideration. Each model links the vocabulary of the logical sentences 
 * to elements of the possible world. (The AIMA book p. 290).
 * The domain of a model is the set of domain elements it contains.
 * The elements are either objects, relations or functions.
 * Here we specify a Chess Model by defining the elements of this Chess Model. 
 * (for a definition see also p. 168 Logiske Metoder).
 * The elements of the chess domain are as follows:
 * The name of all the pieces as constants (objects)
 * The board as a constant (an object)
 * All positions on the board as constants (objects)
 * All predicates as relations
 * All elements of the model are represented by HashSets of Strings.
 * 
 * @author oluf
 *
 */
public class ChessDomain extends FOLDomain {
	private String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\domainmodel.txt";
	private PrintWriter writer = null;
	private FileWriter fw = null;
	
	public ChessDomain() {
		super();
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
		
	}

	public ChessDomain(FOLDomain toCopy) {
		super(toCopy);
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
	}

	public ChessDomain(Set<String> constants, Set<String> functions, Set<String> predicates) {
		super(constants, functions, predicates);
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
	}

	@Override
	public String toString() {
		return "ChessDomain [getConstants()=" + getConstants() + "\n getFunctions()=" + getFunctions()
				+ "\n getPredicates()=" + getPredicates() + "\n addSkolemConstant()=" + addSkolemConstant()
				+ "\n addSkolemFunction()=" + addSkolemFunction() + "\n addAnswerLiteral()=" + addAnswerLiteral()
				+ "]";
	}

	public void printDomain() {
		writer.println("The model\n"+toString());
		writer.flush();
	}
	
}
