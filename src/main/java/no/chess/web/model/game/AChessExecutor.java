package no.chess.web.model.game;
import java.util.HashMap;

import aima.core.logic.fol.parsing.ast.Constant;
import aima.core.logic.fol.parsing.ast.Term;
import aima.core.logic.fol.parsing.ast.Variable;
import no.function.FunctionExecutor;

/**
 *  AChessExecutor
 *  This class determines if an element in an actionschema should be a Constant or a Variable
 * @author oluf
 *
 */
public class AChessExecutor implements FunctionExecutor {
	private Constant c = null;
	private Variable v = null;
	private String name;
	private String[] keys = new String[] {"startpos","piecename","newpos","piecetype"};
	private String[] values = new String[] {"posy","byPiece","posx","type"};
	private int val;
	
	public AChessExecutor() {
		super();
		name = null;
	}
	
	public AChessExecutor(String name) {
		super();
		this.name = name;
		c = new Constant(this.name);
	}
	

	public AChessExecutor(int val) {
		super();
		this.val = val;
		this.name = values[val];
		v = new Variable(name);
	}

	/* (non-Javadoc)
	 * @see no.function.FunctionExecutor#execute()
	 * This method returns a Constant or a Variable
	 */
	@Override
	public Object execute() {
		if (c != null)
			return c;
		if (v != null)
			return v;
		return null;
	}

	@Override
	public void buildTerms(HashMap<String, Term> cParam, HashMap<String, Term> vParam) {
		// TODO Auto-generated method stub
		
	}

}
