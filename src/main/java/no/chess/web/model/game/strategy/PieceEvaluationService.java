package no.chess.web.model.game.strategy;

import java.util.List;

import no.chess.web.model.game.APlayer;
import no.chess.web.model.game.AgamePiece;
import no.chess.web.model.game.ChessFolKnowledgeBase;
import no.chess.web.model.game.KnowledgeBuilder;

/**
 * BishopEvaluationService
 * This service evaluates and determines which position is the best for the Bishop to move to
 */
public class PieceEvaluationService {
    private  ChessFolKnowledgeBase kb;
    private APlayer player; 
    private APlayer opponent;
    private String opponentColor = "";
    
    public PieceEvaluationService(ChessFolKnowledgeBase kb, APlayer player, APlayer opponent) {
		super();
		this.kb = kb;
		this.player = player;
		this.opponent = opponent;
		opponentColor = opponent.getNameOfplayer();
		opponentColor = KnowledgeBuilder.extractString(opponentColor,'P', 0);
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
	 * evaluateKnightsDevelopment
	 * This method evaluates the knights development
	 * @param pieceId
	 * @param targetSquare
	 * @return The score
	 */
	public int evaluateKnightsDevelopment(String pieceId, String targetSquare,String currentPos) {
		 int score = 0;
		 String homeSquare  = KnowledgeBuilder.getHOMESQUARE();
		 List<String> myKnights = player.getNameofKnights();
		 String centerPawn = "";
		 if (player.getPlayerId().equals("WHITE")) {
			 centerPawn = "WhitePawn4";
		 }else {
			 centerPawn = "BlackPawn4";
		 }		 
	      if (pieceId.equals(myKnights.get(0))|| pieceId.equals(myKnights.get(1))) {
	    	  if(kb.existsFact(homeSquare,pieceId,currentPos))
	    		  score +=90;
	    	  else
	    		  score -=5;
	          String pawnAttack = KnowledgeBuilder.getPAWNATTACK();
	          List<String> pawnNames = kb.findFacts(targetSquare,pawnAttack);
	          boolean pawnThreat = false;
	          if (!pawnNames.isEmpty()) {
	          	for (String pawn:pawnNames) {
	          		if (pawn.contains(opponentColor)) {
	          			score = -100;
	          			pawnThreat = true;
	          			break;
	          		}
	          	}
	          }
	          if (pawnThreat)
	        	  return score;
        	  boolean pieceFork = false;
	          String fork = KnowledgeBuilder.getKNIGHT_FORK();
	          List<AgamePiece> pieces = opponent.getMygamePieces();
	          for(AgamePiece piecea:pieces) {
	        	  boolean pawn = piecea.getPieceType() == piecea.getMyType().PAWN;
	        	  if (pawn)
	        		  continue;
	        	  for (AgamePiece pieceb:pieces) {
		        	  boolean pawnb = pieceb.getPieceType() == piecea.getMyType().PAWN;
		        	  if (pawnb)
		        		  continue;	
		        	  String posa = piecea.getmyPosition().getPositionName();
		        	  String posb = pieceb.getmyPosition().getPositionName();
		        	  String namea = piecea.getMyPiece().getOntlogyName();
		        	  String nameb = pieceb.getMyPiece().getOntlogyName();
		        	  if (namea.equals(nameb))
		        		  continue;
		        	  pieceFork = kb.askRule(fork, pieceId,namea,nameb);
		        	  if (pieceFork) {
		        		  score += 50;
		        		  break;
		        	  }
		          }
	        	  if(pieceFork)
	        		  break;
	        	  
	          }
	          boolean taken = false;
	          if (!pieceFork) {
	        	  taken = takeOpponent(pieceId,targetSquare);
	        	  if (taken)
	        		  score +=40;
	          }
	          score +=10;
	      }
		 return score;
	}
	/**
     * Evaluerer og gir en poengsum for et kandidatfelt for en hvit løper.
     * Høyere score = bedre og mer aktiv utvikling.
     */
    public int evaluateBishopDevelopment(String pieceId, String targetSquare,String currentPos) {
        int score = 0;
        String thePlayer = player.getNameOfplayer();
        String theOpponent = opponent.getNameOfplayer();
        List<String> myBishops = player.getNameofBishops();
		String homeSquare  = KnowledgeBuilder.getHOMESQUARE();
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
	    	  if(kb.existsFact(homeSquare,pieceId,currentPos))
	    		  score +=90;
	    	  else
	    		  score -=5;
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
        String minor = KnowledgeBuilder.getMINORMOVE();
        String pawnAttack = KnowledgeBuilder.getPAWNATTACK();
        List<String> pawnNames = kb.findFacts(targetSquare,pawnAttack);
        boolean pawnThreat = false;
        if (!pawnNames.isEmpty()) {
        	for (String pawn:pawnNames) {
        		if (pawn.contains(opponentColor)) {
        			score = -100;
        			pawnThreat = true;
        			break;
        		}
        	}
        }
        // 6. SJEKK OM FELTET ER DEKKET AV EN MEDSPILLER (MINORMOVE-harmoni) - Og ikke truet av opponent bonde
        boolean pieceProtect = kb.askRule(minor, pieceId, targetSquare);
        if (!pawnThreat && pieceProtect) {
            score += 15; // Feltet er harmonisk dekket av medspiller
        }else if(!pieceProtect) {
        	score -= 50;
        }

        return score;
    }
    /**
     * takeOpponent
     * This method checks if an opponent piece can be taken
     * @param pieceId
     * @param pos
     * @return
     */
    public boolean takeOpponent(String pieceId, String pos) {
		List<AgamePiece> opponentpieces = opponent.getMygamePieces();
		String takePred = KnowledgeBuilder.getTAKE_PIECE();
		boolean taken = false;
		for (AgamePiece oppPiece:opponentpieces) {
			String piecePos = oppPiece.getmyPosition().getPositionName();
			String pieceName = oppPiece.getMyPiece().getOntlogyName();
			taken = kb.askRule(takePred,pieceId,pieceName,pos);
			if(taken) {
				break;
			}
		}
    	return taken;
    }
}
