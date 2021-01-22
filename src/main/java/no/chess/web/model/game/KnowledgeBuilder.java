
package no.chess.web.model.game;

import no.games.chess.AbstractGamePiece.pieceType;

/**
 * KnowledgeBuilder
 * This class contains constants and buildingblocks to be used in knowledgebases.
 * 
 * @author oluf
 * 
 */
public class KnowledgeBuilder {
  private static String ACTION =  "ACTION";
  private static String PLAY =  "PLAY";
  private static String PROTECTED =  "PROTECTEDBY";
  private static String simpleProtected =  "PROTECTED";
  private static String ATTACKED =  "ATTACKEDBY";
  private static String CAPTURE =  "CAPTURE";
  private static String CONQUER =  "CONQUER";
  private static String THREATEN =  "THREATENEDBY";
  private static String OWNER =  "OWNER";
  private static String MOVE =  "MOVE";
  private static String REACHABLE =  "REACHABLE";
  private static String CANMOVE =  "CANMOVE";
  private static String SAFEMOVE =  "SAFEMOVE";
  private static String STRIKE =  "STRIKE";
  private static String PIECETYPE =  "PIECETYPE";
  private static String PAWNMOVE =  "PAWNMOVE";
  private static String PAWN =  "PAWN";
  private static String KNIGHT =  "KNIGHT";
  private static String BISHOP =  "BISHOP";
  private static String ROOK =  "ROOK";
  private static String KING =  "KING";
  private static String QUEEN =  "QUEEN";
  private static String OCCUPIES = "occupies";
  
  
  public static String getOCCUPIES() {
	return OCCUPIES;
}
public static void setOCCUPIES(String oCCUPIES) {
	OCCUPIES = oCCUPIES;
}
public static String getPLAY()
  {
		return PLAY;
  }
  public static void setPLAY(String pLAY)
  {
		PLAY = pLAY;
  }

  public static String getPAWN()
  {
		return PAWN;
  }

  public static void setPAWN(String pAWN)
  {
		PAWN = pAWN;
  }

  public static String getKNIGHT()
  {
		return KNIGHT;
  }

  public static void setKNIGHT(String kNIGHT)
  {
		KNIGHT = kNIGHT;
  }

  public static String getBISHOP()
  {
		return BISHOP;
  }

  public static void setBISHOP(String bISHOP)
  {
		BISHOP = bISHOP;
  }

  public static String getROOK()
  {
		return ROOK;
  }

  public static void setROOK(String rOOK)
  {
		ROOK = rOOK;
  }

  public static String getKING()
  {
		return KING;
  }

  public static void setKING(String kING)
  {
		KING = kING;
  }

  public static String getQUEEN()
  {
		return QUEEN;
  }

  public static void setQUEEN(String qUEEN)
  {
		QUEEN = qUEEN;
  }

  public static String getPAWNMOVE()
  {
		return PAWNMOVE;
  }

  public static void setPAWNMOVE(String pAWNMOVE)
  {
		PAWNMOVE = pAWNMOVE;
  }

  public static String getPIECETYPE()
  {
		return PIECETYPE;
  }

  public static void setPIECETYPE(String pIECETYPE)
  {
		PIECETYPE = pIECETYPE;
  }

  public static String getACTION()
  {
		return ACTION;
  }

  public static void setACTION(String aCTION)
  {
		ACTION = aCTION;
  }

  public static String getPROTECTED()
  {
		return PROTECTED;
  }

  public static void setPROTECTED(String pROTECTED)
  {
		PROTECTED = pROTECTED;
  }

  public static String getSimpleProtected()
  {
		return simpleProtected;
  }

  public static void setSimpleProtected(String simpleProtected)
  {
		KnowledgeBuilder.simpleProtected = simpleProtected;
  }

  public static String getATTACKED()
  {
		return ATTACKED;
  }

  public static void setATTACKED(String aTTACKED)
  {
		ATTACKED = aTTACKED;
  }

  public static String getCAPTURE()
  {
		return CAPTURE;
  }

  public static void setCAPTURE(String cAPTURE)
  {
		CAPTURE = cAPTURE;
  }

  public static String getCONQUER()
  {
		return CONQUER;
  }

  public static void setCONQUER(String cONQUER)
  {
		CONQUER = cONQUER;
  }

  public static String getTHREATEN()
  {
		return THREATEN;
  }

  public static void setTHREATEN(String tHREATEN)
  {
		THREATEN = tHREATEN;
  }

  public static String getOWNER()
  {
		return OWNER;
  }

  public static void setOWNER(String oWNER)
  {
		OWNER = oWNER;
  }

  public static String getMOVE()
  {
		return MOVE;
  }

  public static void setMOVE(String mOVE)
  {
		MOVE = mOVE;
  }

  public static String getREACHABLE()
  {
		return REACHABLE;
  }

  public static void setREACHABLE(String rEACHABLE)
  {
		REACHABLE = rEACHABLE;
  }

  public static String getCANMOVE()
  {
		return CANMOVE;
  }

  public static void setCANMOVE(String cANMOVE)
  {
		CANMOVE = cANMOVE;
  }

  public static String getSAFEMOVE()
  {
		return SAFEMOVE;
  }

  public static void setSAFEMOVE(String sAFEMOVE)
  {
		SAFEMOVE = sAFEMOVE;
  }

  public static String getSTRIKE()
  {
		return STRIKE;
  }

  public static void setSTRIKE(String sTRIKE)
  {
		STRIKE = sTRIKE;
  }

  public static String getPieceType(AgamePiece piece) {
	  pieceType type = piece.getPieceType();
		if (type == type.PAWN) {
			return PAWN;
		}
		if (type == type.BISHOP) {
			return BISHOP;
		}		
		if (type == type.ROOK) {
			return ROOK;
		}			
		if (type == type.KNIGHT) {
			return KNIGHT;
		}
		if (type == type.QUEEN) {
			return QUEEN;
		}
		if (type == type.KING) {
			return KING;
		}	
	  return null;
  }
}
