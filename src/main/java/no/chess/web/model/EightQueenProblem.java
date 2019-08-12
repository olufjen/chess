package no.chess.web.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import aima.core.agent.Agent;
import aima.core.environment.nqueens.NQueensBoard;
import aima.core.environment.nqueens.NQueensFunctions;
import aima.core.environment.nqueens.QueenAction;
import aima.core.search.agent.SearchAgent;
import aima.core.search.csp.examples.NQueensCSP;
import aima.core.search.framework.SearchForActions;
import aima.core.search.framework.problem.ActionsFunction;
import aima.core.search.framework.problem.GeneralProblem;
import aima.core.search.framework.problem.Problem;
import aima.core.search.framework.qsearch.GraphSearch;
import aima.core.search.uninformed.DepthFirstSearch;
import aima.core.util.datastructure.XYLocation;
import aima.gui.swing.applications.search.games.NQueensApp.NQueensEnvironment;



/**
 * This is a model object to solve the eight queen problem
 * Some definitions: (p. 35)
 * The Agent function maps any given percept sequence into an action.
 * The Agent function is implementd by an agent program.
 * The Agent's behavior is described by the agent function 
 * @author oluf
 *
 */
public class EightQueenProblem {
	private NQueensCSP nQueens;
	private int noofQueens;
	private QueensEnvironment env = null;
	private SearchAgent agent = null;
	private	NQueensBoard board = null;
	private boolean boardDirty;
	private  List<SearchForActions<NQueensBoard, QueenAction>> SEARCH_ALGOS = new ArrayList<>();
	/** List of supported search algorithm names. */
	private  List<String> SEARCH_NAMES = new ArrayList<>();
	public NQueensCSP getnQueens() {
		return nQueens;
	}

	public int getNoofQueens() {
		return noofQueens;
	}

	public void setNoofQueens(int noofQueens) {
		this.noofQueens = noofQueens;
	}

	public QueensEnvironment getEnv() {
		return env;
	}

	public void setEnv(QueensEnvironment env) {
		this.env = env;
	}

	public SearchAgent getAgent() {
		return agent;
	}

	public void setAgent(SearchAgent agent) {
		this.agent = agent;
	}

	public NQueensBoard getBoard() {
		return board;
	}

	public void setBoard(NQueensBoard board) {
		this.board = board;
	}

	public void setnQueens(NQueensCSP nQueens) {
		this.nQueens = nQueens;
	}

	public EightQueenProblem() {
		super();
		nQueens = new NQueensCSP(8);
		noofQueens = 8;
		addSearchAlgorithm("Depth First Search (Graph Search)",
				new DepthFirstSearch<>(new GraphSearch<>()));
	}

	public EightQueenProblem(int noofQueens) {
		super();
		this.noofQueens = noofQueens;
		nQueens = new NQueensCSP(noofQueens);
		addSearchAlgorithm("Depth First Search (Graph Search)",
				new DepthFirstSearch<>(new GraphSearch<>())); // All search algorithms implement Depthfirstsearch
	}
	public void prepare() {
		board = new NQueensBoard(noofQueens);
		env = new QueensEnvironment(board);
	/*	for (int i = 0; i < board.getSize(); i++)
			board.addQueenAt(new XYLocation(i, 0)); */
		boardDirty = false;
		agent = null;
	}
	/** Adds a new item to the list of supported search algorithms. */
	public  void addSearchAlgorithm(String name, SearchForActions<NQueensBoard, QueenAction> algo) {
		SEARCH_NAMES.add(name);
		SEARCH_ALGOS.add(algo);
	}

	/**
	 * Creates a new search agent and adds it to the current environment if
	 * necessary.
	 */
	public void addAgent() throws Exception {
		if (agent != null && agent.isDone()) {
			env.removeAgent(agent);
			agent = null;
		}
		if (agent == null) {
//			int pSel = frame.getSelection().getIndex(NQueensFrame.PROBLEM_SEL);
//			int sSel = frame.getSelection().getIndex(NQueensFrame.SEARCH_SEL);
			int pSel = 0;
			int sSel = 0;
			ActionsFunction<NQueensBoard, QueenAction> actionsFn;
			if (pSel == 0)
				actionsFn = NQueensFunctions::getIFActions; //A shorthand for lambdas calling a specific method (p. 60 3.6.1 Java 8)
			else
				actionsFn = NQueensFunctions::getCSFActions;

			Problem<NQueensBoard, QueenAction> problem = new GeneralProblem<>(env.getBoard(),
					actionsFn, NQueensFunctions::getResult, NQueensFunctions::testGoal);
			SearchForActions<NQueensBoard, QueenAction> search = SEARCH_ALGOS.get(sSel);
			agent = new SearchAgent<>(problem, search);
			env.addAgent(agent);
		}
	}
	public void solveProblem() {
 		try {
			addAgent();
			while (!agent.isDone()) {
				Thread.sleep(200);
				env.step();
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}

	}
	public boolean solvepartProblem() {
 		try {
			addAgent();
			Thread.sleep(200);
			env.step();
		} catch (Exception e) {
			e.printStackTrace();
			return agent.isDone();
		}
 		return agent.isDone();
	}	
	/** Provides a text with statistical information about the last run. */
	public String getStatistics() {
		StringBuilder result = new StringBuilder();
		String board =env.getBoard().getBoardPic();
		Properties properties = agent.getInstrumentation();
		for (Object o : properties.keySet()) {
			String key = (String) o;
			String property = properties.getProperty(key);
			result.append("\n").append(key).append(" : ").append(property).append("\n").append(" Board\n "+board);
		}
		return result.toString();	
	}
}
