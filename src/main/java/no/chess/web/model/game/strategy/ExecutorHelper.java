package no.chess.web.model.game.strategy;

import java.util.ArrayList;
import java.util.List;

import no.chess.web.model.Position;
import no.chess.web.model.game.APlayer;
import no.chess.web.model.game.AgamePiece;
import no.chess.web.model.game.ChessFolKnowledgeBase;
import no.chess.web.model.game.GroundGameState;
import no.chess.web.model.game.KnowledgeBuilder;

/**
 * ExecutorHelper
 * This class contains methods to help executors decide on the best action
 * 
 */
public class ExecutorHelper {

	private ChessFolKnowledgeBase kb;
	private String occupiedSquare = null;
	private APlayer opponent;

	public ExecutorHelper(ChessFolKnowledgeBase kb) {
		super();
		this.kb = kb;
	}
	
	public String getOccupiedSquare() {
		return occupiedSquare;
	}

	public void setOccupiedSquare(String occupiedSquare) {
		this.occupiedSquare = occupiedSquare;
	}

	public APlayer getOpponent() {
		return opponent;
	}

	public void setOpponent(APlayer opponent) {
		this.opponent = opponent;
	}

	public ChessFolKnowledgeBase getKb() {
		return kb;
	}

	public void setKb(ChessFolKnowledgeBase kb) {
		this.kb = kb;
	}

	
	/**
	 * isOnSameFile
	 * This method checks if two positions are on the same file:
	 * Example: e1 to e8
	 * @param square1
	 * @param square2
	 * @return True if they are on same file
	 */
	public boolean isOnSameFile(String square1, String square2) {
		// Sjekker om første tegn (bokstaven) er identisk, f.eks. 'e' == 'e'
		return square1.charAt(0) == square2.charAt(0);
	}
	/**
	 * isOnSameRank
	 * This method checks if two positions are on the same rank:
	 * Example: a2 .. h2
	 * @param square1
	 * @param square2
	 * @return True if they are on same rank
	 */
	public boolean isOnSameRank(String square1, String square2) {
		// Sjekker om tallet (raden) er identisk, f.eks. '4' == '4'
		return square1.charAt(1) == square2.charAt(1);
	}
	/**
	 * isOnSameDiagonal
	 * This method checks if two positions are on the same diagonal:
	 * Example: a1 to h8
	 * @param square1
	 * @param square2
	 * @return True if they are on same diagonal
	 */
	public boolean isOnSameDiagonal(String square1, String square2) {
		int fileDiff = Math.abs(square1.charAt(0) - square2.charAt(0));
		int rankDiff = Math.abs(square1.charAt(1) - square2.charAt(1));
		return fileDiff == rankDiff;
	}
	/**
	 * isDiagonalLineClear
	 * This method checks if the diagonal between to positions is clear
	 * @param s1
	 * @param s2
	 * @return true if the diagonal is clear
	 */
	public boolean isDiagonalLineClear(String s1, String s2) {
		// 1. Verifiser først at de faktisk deler diagonal
		if (!isOnSameDiagonal(s1, s2)) {
			return false;
		}

		// 2. Bruk BETWEEN-fakta til å sjekke om noen felt imellom er okkupert
		List<String> intermediateSquares = getSquaresBetween(s1, s2);
		String occupy = KnowledgeBuilder.getOCCUPIES();
		for (String midSquare : intermediateSquares) {
			List<String> terms = kb.searchFacts("x", midSquare, occupy);
			if (!terms.isEmpty()) {
				occupiedSquare = midSquare;
				break;
			}
		}
		if (occupiedSquare != null)
			return false;
		return true; // Fri sikt!
	}
	
	/**
	 * isFileLineClear
	 * This method checks if the file line between to positions is clear
	 * @param s1
	 * @param s2
	 * @return
	 */
	public boolean isFileLineClear(String s1, String s2) {
	    List<String> between = getSquaresBetweenOnSameFile(s1, s2);
		String occupy = KnowledgeBuilder.getOCCUPIES();
	    for (String sq : between) {
	    	List<String> terms = kb.searchFacts("x", sq, occupy);
	    	if (!terms.isEmpty()) {
	            return false; // Blokkert av en brikke
	        }
	    }
	    return true; // Fri bane
	}

	/**
	 * Sjekker om det er fri sikt mellom to ruter langs en horisontal rad.
	 */
	
	
	/**
	 * isRankLineClear
	 * This method checks if the rank line between to positions is clear
	 * @param s1
	 * @param s2
	 * @return true if the rank line is clear
	 */
	public boolean isRankLineClear(String s1, String s2) {
	    List<String> between = getSquaresBetweenOnSameRank(s1, s2);
		String occupy = KnowledgeBuilder.getOCCUPIES();
	    for (String sq : between) {
	    	List<String> terms = kb.searchFacts("x", sq, occupy);
	    	if (!terms.isEmpty()) {
	            return false; // Blokkert av en brikke
	        }
	    }
	    return true; // Fri bane
	}
	
	
	/**
	 * getSquaresBetween
	 * This method returns all positions between two given positions s1 and s2
	 * The knowledgebase contains facts of the form BETWEEN(x, s1, s2)
	 * @param s1
	 * @param s2
	 * @return The list of positions
	 */
	public List<String> getSquaresBetween(String s1, String s2) {
		// Spør KB etter alle felt x der BETWEEN(x, s1, s2) er sant
		return kb.getSquaresBetween(s1, s2);
	}

	/**
	 * isDiagonalPin
	 * This method checks for a diagonal pin. 
	 * @param attackerSquare
	 * @param pinnedSquare
	 * @param targetSquare
	 * @return true if there is a diagonal pin between attackerSquare, pinnedSquare and targetSquare
	 */
	public boolean isDiagonalPin(String attackerSquare, String pinnedSquare, String targetSquare) {
		String between = KnowledgeBuilder.getBETWEEN();
	    // 1. Alle tre feltene må ligge på samme diagonal
	    if (!isOnSameDiagonal(attackerSquare, pinnedSquare) || !isOnSameDiagonal(pinnedSquare, targetSquare)) {
	        return false;
	    }

	    // 2. Den bundne brikken MÅ ligge imellom angriperen og målet bak
	    if (!kb.existsFact(between+"(" + pinnedSquare + ", " + attackerSquare + ", " + targetSquare + ")")) {
	        return false;
	    }

	    // 3. Det må ikke stå NOEN andre brikker mellom angriperen og den bundne brikken
	    if (!isDiagonalLineClear(attackerSquare, pinnedSquare)) {
	        return false;
	    }

	    // 4. Det må ikke stå NOEN andre brikker mellom den bundne brikken og kongen/målet bak
	    if (!isDiagonalLineClear(pinnedSquare, targetSquare)) {
	        return false;
	    }

	    return true; // Dette er en vanntett diagonal binding!
	}
	/**
	 * getSquaresBetweenOnSameRank
	 * REturns all squares that are between s an s2 on same rank
	 * Returnerer alle felt som ligger mellom s1 og s2 langs samme rad.
	 * Eksempel: getSquaresBetweenOnSameRank("b4", "f4") -> ["c4", "d4", "e4"]
	 * @param s1
	 * @param s2
	 * @return A list of positions
	 */
	public List<String> getSquaresBetweenOnSameRank(String s1, String s2) {
	    List<String> intermediateSquares = new ArrayList<>();

	    if (!isOnSameRank(s1, s2)) {
	        return intermediateSquares; // Ikke på samme rad
	    }

	    char rank = s1.charAt(1);
	    char startFile = (char) Math.min(s1.charAt(0), s2.charAt(0));
	    char endFile = (char) Math.max(s1.charAt(0), s2.charAt(0));

	    // Iterer gjennom bokstavene mellom startFile og endFile
	    for (char f = (char) (startFile + 1); f < endFile; f++) {
	        intermediateSquares.add("" + f + rank);
	    }

	    return intermediateSquares;
	}

	/**
	 * getSquaresBetweenOnSameFile
	 * Returns all positions between s1 and s2 on same file
	 * Returnerer alle felt som ligger mellom s1 og s2 langs samme vertikale linje.
	 * Eksempel: getSquaresBetweenOnSameFile("e2", "e8") -> ["e3", "e4", "e5", "e6", "e7"]
	 * @param s1
	 * @param s2
	 * @return A list of positions
	 */
	public List<String> getSquaresBetweenOnSameFile(String s1, String s2) {
	    List<String> intermediateSquares = new ArrayList<>();

	    if (!isOnSameFile(s1, s2)) {
	        return intermediateSquares; // Ikke på samme linje
	    }

	    char file = s1.charAt(0);
	    int startRank = Math.min(Character.getNumericValue(s1.charAt(1)), Character.getNumericValue(s2.charAt(1)));
	    int endRank = Math.max(Character.getNumericValue(s1.charAt(1)), Character.getNumericValue(s2.charAt(1)));

	    // Iterer gjennom tallene mellom startRank og endRank
	    for (int r = startRank + 1; r < endRank; r++) {
	        intermediateSquares.add("" + file + r);
	    }

	    return intermediateSquares;
	}
	
    public boolean isLongRangePiece(String pieceId) {
        return pieceId.contains("Queen") || pieceId.contains("Rook") || pieceId.contains("Bishop");
    }

    public boolean canPinOnFile(String pieceId) {
        return pieceId.contains("Queen") || pieceId.contains("Rook");
    }

    public boolean canPinOnRank(String pieceId) {
        return pieceId.contains("Queen") || pieceId.contains("Rook");
    }

    public boolean canPinOnDiagonal(String pieceId) {
        return pieceId.contains("Queen") || pieceId.contains("Bishop");
    }
    
    /**
     * createsPinAgainstpiece
     * This methods checks if it is possible to create a pin on an opponent piece
     * @param piece
     * @param targetSquare The target position of the player's piece on this move
     * @param attackerSquare The position of the opponent attacker
     * @param kingSquare The position of the valuable opponent piece
     * @return true if pin is possible
     */
    public boolean createsPinAgainstpiece(String piece, String targetSquare, String attackerSquare, String kingSquare) {
        
        // 1. TILFELLE: Binding langs samme linje (vertikalt, f.eks. Qe2 med e4 og e8)
        if (canPinOnFile(piece) && isOnSameFile(targetSquare, attackerSquare) && isOnSameFile(attackerSquare, kingSquare)) {
            // attackerSquare må ligge mellom targetSquare og kingSquare
            List<String> fullLine = getSquaresBetweenOnSameFile(targetSquare, kingSquare);
            if (fullLine.contains(attackerSquare)) {
                // Sjekk at det er fri sikt: target -> attacker og attacker -> king
                if (isFileLineClear(targetSquare, attackerSquare) && isFileLineClear(attackerSquare, kingSquare)) {
                    return true;
                }
            }
        }

        // 2. TILFELLE: Binding langs samme rad (horisontalt)
        if (canPinOnRank(piece) && isOnSameRank(targetSquare, attackerSquare) && isOnSameRank(attackerSquare, kingSquare)) {
            List<String> fullRank = getSquaresBetweenOnSameRank(targetSquare, kingSquare);
            if (fullRank.contains(attackerSquare)) {
                if (isRankLineClear(targetSquare, attackerSquare) && isRankLineClear(attackerSquare, kingSquare)) {
                    return true;
                }
            }
        }

        // 3. TILFELLE: Binding langs diagonal
        if (canPinOnDiagonal(piece) && isOnSameDiagonal(targetSquare, attackerSquare) && isOnSameDiagonal(attackerSquare, kingSquare)) {
            if (isDiagonalPin(targetSquare, attackerSquare, kingSquare)) {
                return true;
            }
        }

        return false;
    }
    /**
     * checkAttack
     * This method checks if a PAWNATTACK is legal
     * The target position must be occupied by an opponent piece
     * @param predicate The predicate name
     * @param posName The target position
     * @return true if PAWNATTack is legal
     */
    public boolean checkAttack(String posName) {
    	boolean legal = false;
    	List<AgamePiece> pieces = opponent.getMygamePieces();
    	for(AgamePiece piece:pieces) {
    		Position pos = piece.getmyPosition();
    		if (pos == null)
    			pos = piece.getHeldPosition();
    		String myPosname = pos.getPositionName();
    		legal = myPosname.equals(posName);
    		if(legal)
    			break;
    	}
    	return legal;
    }

}
