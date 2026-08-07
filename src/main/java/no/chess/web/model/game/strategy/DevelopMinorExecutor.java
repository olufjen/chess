package no.chess.web.model.game.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import aima.core.logic.fol.parsing.ast.Term;
import no.chess.web.model.Position;
import no.chess.web.model.game.APlayer;
import no.chess.web.model.game.AgamePiece;
import no.chess.web.model.game.ChessFolKnowledgeBase;
import no.chess.web.model.game.KnowledgeBuilder;
import no.function.FunctionExecutor;

/**
 * DevelopMinorExecutor
 * This Executor checks if any Minor pieces are not developed (they are still in HOMESQUARE)
 */
public class DevelopMinorExecutor implements FunctionExecutor {
	private APlayer myPlayer;
    private ChessFolKnowledgeBase kb;
    private List<AgamePiece> homePieces;
    private String myKey = KnowledgeBuilder.getDevelopPiece();
	
	public DevelopMinorExecutor(APlayer myPlayer) {
		super();
		this.myPlayer = myPlayer;
		homePieces = new ArrayList<AgamePiece>();
	}

	public DevelopMinorExecutor(APlayer myPlayer, ChessFolKnowledgeBase kb) {
		super();
		this.myPlayer = myPlayer;
		this.kb = kb;
		homePieces = new ArrayList<AgamePiece>();
	}

	public String getMyKey() {
		return myKey;
	}

	public void setMyKey(String myKey) {
		this.myKey = myKey;
	}

	public APlayer getmyPlayer() {
		return myPlayer;
	}

	public void setmyPlayer(APlayer myPlayer) {
		this.myPlayer = myPlayer;
	}
	

	public List<AgamePiece> getHomePieces() {
		return homePieces;
	}

	public void setHomePieces(List<AgamePiece> homePieces) {
		this.homePieces = homePieces;
	}

	@Override
	public Object execute() {
		List<AgamePiece> pieces = myPlayer.getMygamePieces();
		homePieces.clear();
		String occupies = KnowledgeBuilder.getOCCUPIES();
		AgamePiece piece = null;
		boolean occupyHome = false;
		for (AgamePiece temppiece:pieces) {
			String pieceName = temppiece.getMyPiece().getOntlogyName();
			Position pos = temppiece.getHomePosition();
			String posName = pos.getPositionName();
			occupyHome = kb.existsFact(occupies,pieceName,posName);
			if (occupyHome) {
				piece = temppiece;
				homePieces.add(piece);
			}	
		}
		return null;
	}

	@Override
	public void buildTerms(HashMap<String, Term> cParam, HashMap<String, Term> vParam) {
		// TODO Auto-generated method stub

	}

}
