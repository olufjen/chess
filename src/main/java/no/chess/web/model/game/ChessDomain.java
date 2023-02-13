package no.chess.web.model.game;

import java.util.Set;

import aima.core.logic.fol.domain.FOLDomain;

/**
 * This is the subclass of the First order logic FOLDomain for the chess game domain
 * @author oluf
 *
 */
public class ChessDomain extends FOLDomain {

	public ChessDomain() {
		super();
		
	}

	public ChessDomain(FOLDomain toCopy) {
		super(toCopy);
		
	}

	public ChessDomain(Set<String> constants, Set<String> functions, Set<String> predicates) {
		super(constants, functions, predicates);
		
	}

	@Override
	public String toString() {
		return "ChessDomain [getConstants()=" + getConstants() + ", getFunctions()=" + getFunctions()
				+ ", getPredicates()=" + getPredicates() + ", addSkolemConstant()=" + addSkolemConstant()
				+ ", addSkolemFunction()=" + addSkolemFunction() + ", addAnswerLiteral()=" + addAnswerLiteral()
				+ ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
				+ "]";
	}

	
}
