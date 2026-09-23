package no.chess.web.model.game.strategy;

import java.util.List;

import no.chess.web.model.game.APlayer;
import no.chess.web.model.game.ChessFolKnowledgeBase;
import no.chess.web.model.game.KnowledgeBuilder;

/**
 * BishopEvaluationService
 * This service evaluates and determines which position is the best for the Bishop to move to
 */
public class BishopEvaluationService {
    private  ChessFolKnowledgeBase kb;
    private APlayer player; 
    private APlayer opponent;
    
    public BishopEvaluationService(ChessFolKnowledgeBase kb, APlayer player, APlayer opponent) {
		super();
		this.kb = kb;
		this.player = player;
		this.opponent = opponent;
	}

	public ChessFolKnowledgeBase getKb() {
		return kb;
	}

	public void setKb(ChessFolKnowledgeBase kb) {
		this.kb = kb;
	}

	public APlayer getPlayer() {
		return player;
	}

	public void setPlayer(APlayer player) {
		this.player = player;
	}

	public APlayer getOpponent() {
		return opponent;
	}

	public void setOpponent(APlayer opponent) {
		this.opponent = opponent;
	}

	/**
     * Evaluerer og gir en poengsum for et kandidatfelt for en hvit løper.
     * Høyere score = bedre og mer aktiv utvikling.
     */
    public int evaluateBishopDevelopment(String pieceId, String targetSquare) {
        int score = 0;
        String thePlayer = player.getNameOfplayer();
        String theOpponent = opponent.getNameOfplayer();
        List<String> myBishops = player.getNameofBishops();
        List<String> opponentKnights = player.getNameofKnights();
        String occupies = KnowledgeBuilder.getOCCUPIES();
        String centerPawn = "";
        if (player.getPlayerId().equals("WHITE")) {
        	centerPawn = "WhitePawn4";
        }else {
        	centerPawn = "BlackPawn4";
        }
        // 2. AKTIVITET OG DYBDE (Høyere rad gir mer romkontroll)
        int rank = Character.getNumericValue(targetSquare.charAt(1));
        if (rank == 5) {
            score += 45; // Svært aktivt felt (f.eks. Bg5, Bb5)
        } else if (rank == 4) {
            score += 35; // Aktivt felt (f.eks. Bf4, Bc4)
        } else if (rank == 3) {
            score += 25; // Solid og sentralt (f.eks. Be3, Bd3)
        } else if (rank == 2) {
            score += 10; // Passivt (f.eks. Bd2, Be2)
        }

        // 3. SENTRUMSKONTROLL & DIAGONAL-LENGDE
        // Felter som ser inn mot eller okkuperer sentrum
        if (targetSquare.equals("e3") || targetSquare.equals("d3") || 
            targetSquare.equals("c4") || targetSquare.equals("f4")) {
            score += 20;
        }

        // 4. SPESIFIKK EVALUERING FOR WhiteBishop1 (Svartfeltsløper fra c1)
        if (pieceId.equals(myBishops.get(0))|| pieceId.equals(myBishops.get(1))) {
            // g5 er et klassisk kjempefelt (pin mot Nf6 eller Dronning/Konge)
            if (targetSquare.equals("g5")) {
                score += 40;
                // Ekstra bonus hvis svart har en springer på f6 (aktiv binding!)
                if (kb.existsFact(occupies,opponentKnights.get(0),"f6") || kb.existsFact(occupies,opponentKnights.get(1),"f6")) {
                    score += 30; // Sterk taktisk binding (PIN)
                }
            } else if (targetSquare.equals("f4")) {
                score += 30; // Aktiv diagonal, kontrollerer e5
            } else if (targetSquare.equals("e3")) {
                score += 20; // Trygt og støttende for d4-bonden
            } else if (targetSquare.equals("d2")) {
                score -= 15; // Passivt, stenger for dronningens forsvar av d4/sentrum
            }
            
            // c4 retter seg direkte mot f7 (svarts svakeste punkt) - WhiteBishop2
            if (targetSquare.equals("c4")) {
                score += 40;
            } else if (targetSquare.equals("b5")) {
                score += 35; // Spansk/Italiensk binding eller sjakk mot Konge/Sc6
                if (kb.existsFact(occupies,opponentKnights.get(0),"c6") || kb.existsFact(occupies,opponentKnights.get(1),"c6")) {
                    score += 25; // Binder springeren på c6
                }
            } else if (targetSquare.equals("d3")) {
                // d3 er aktivt, MEN sjekk om den blokkerer d2-bonden dersom d-bonden ikke er flyttet
                if (kb.existsFact(occupies,centerPawn,"d2")) {
                    score -= 50; // Alvorlig strategisk feil: sperrer sentrumsbonden!
                } else {
                    score += 25; // Godt felt hvis d4 allerede er spilt
                }
            } else if (targetSquare.equals("e2")) {
                score += 10; // Trygt, gjør klar for rokkade, men passivt
            }
        }

 
        // 6. SJEKK OM FELTET ER DEKKET AV EN MEDSPILLER (MINORMOVE-harmoni)
        if (kb.askRule("MINORMOVE", pieceId, targetSquare)) {
            score += 15; // Feltet er harmonisk dekket av medspiller
        }

        return score;
    }
}
