package no.chess.web.model.game;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import aima.core.util.datastructure.XYLocation;
import no.chess.web.model.Position;
import no.games.chess.ChessFunctions;
import no.games.chess.MoveRule;
import no.games.chess.AbstractGamePiece.pieceColor;
import no.games.chess.AbstractGamePiece.pieceType;

/**
 * APawnMoveRuler
 * This ruler calculates all the legal moves for a pawn
 * @author oluf
 * @since 14.10.20
 * Calculates possible attack positions
 *  * Usage:
 * 		APawnMoveRuler pawnRules = new APawnMoveRuler();
 *		List<XYLocation> locations = ChessFunctions.moveRule(this, pawnRules);
 */
public class APawnMoveRuler implements MoveRule<APawn, List<XYLocation>> {
	private String color;
	private List<XYLocation> attackPositions;
	@Override
	public List<XYLocation> calculateRule(APawn t) {
		PrintWriter writer = null;
		String name = t.getMyPiece().getName();
		Position from = t.getmyPosition();
		XYLocation loc = from.getXyloc();
		int x = loc.getXCoOrdinate();
		int y = loc.getYCoOrdinate();
		String xpos = Integer.toString(x);
		String ypos = Integer.toString(y);
		if (name == null)
			name = "xx";
/*	    String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\positions"+name+xpos+ypos+".txt";
	      //create an output print writer for the results
	      try 
	      {
	         writer = new PrintWriter(outputFileName);
	      } catch (FileNotFoundException e) {
	         System.err.println("'" + outputFileName 
	            + "' is an invalid output file.");
	      }*/
		List<XYLocation> locations = new ArrayList<>();
		attackPositions = new ArrayList<>();
		pieceColor colorType = t.getPieceColor();
//		writer.println("Pawnruler startposition: "+loc.toString()+" color "+colorType);
		

		int addition = 1;
		boolean whitePawn = colorType == pieceColor.WHITE;
		boolean blackPawn = colorType == pieceColor.BLACK;
		boolean pawnstart = (whitePawn && y == 1) || (blackPawn && y == 6); 
//		boolean blackstart = blackPawn && y == 7;
		if (blackPawn)
			addition = -1;
		XYLocation newloc = createPosition(x,y,addition,writer);
		createAttack(x,y,addition);
		if (newloc != null)
			locations.add(newloc);
		if (pawnstart) {
			int addit = addition + addition;
			XYLocation xloc = createPosition(x,y,addit,writer);
			if (xloc != null)
				locations.add(xloc);
		}
//	     writer.close();
		return locations;
	}
	
	public List<XYLocation> getAttackPositions() {
		return attackPositions;
	}

	public void setAttackPositions(List<XYLocation> attackPositions) {
		this.attackPositions = attackPositions;
	}

	private XYLocation createPosition(int x,int y,int addition,PrintWriter writer) {
		if (y+addition < 8 && y+addition >= 0) {
			XYLocation newloc = new XYLocation(x,y+addition);
//			 writer.println("Pawnruler to position: "+newloc.toString());
			 return newloc; 
		}
		return null;
	}
	private void createAttack(int x,int y,int addition) {
		if (addition > 0 && y+addition < 8 && x+1<8) {
			XYLocation newloc = new XYLocation(x+1,y+addition);
			attackPositions.add(newloc);
		}
		if (addition > 0 && y+addition < 8 && x-1>=0) {
			XYLocation newloc = new XYLocation(x-1,y+addition);
			attackPositions.add(newloc);
		}
		if (addition < 0 && y+addition > 0 && x+1<8) {
			XYLocation newloc = new XYLocation(x+1,y+addition);
			attackPositions.add(newloc);
		}
		if (addition < 0 && y+addition > 0 && x-1>=0) {
			XYLocation newloc = new XYLocation(x-1,y+addition);
			attackPositions.add(newloc);
		}		
	
	}

}
