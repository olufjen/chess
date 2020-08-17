package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.List;

import aima.core.util.datastructure.XYLocation;
import no.chess.web.model.Position;
import no.games.chess.MoveRule;

/**
 *  AKnightMoveRule
 *  This rule calculates all legal moves for the Knight
 * @author oluf
 * Usage:
 * 		AKnightMoveRule knightRules = new AKnightMoveRule;
 *		List<XYLocation> locations = ChessFunctions.moveRule(this, knightRules);
 */
public class AKnightMoveRule implements MoveRule<AKnight, List<XYLocation>> {

	@Override
	public List<XYLocation> calculateRule(AKnight t) {
		Position from = t.getmyPosition();
		XYLocation loc = from.getXyloc();
		int x = loc.getXCoOrdinate();
		int y = loc.getYCoOrdinate();
		List<XYLocation> locations = new ArrayList<>();
		if (y<5 && x>0) {
			XYLocation xloc = new XYLocation(x-1,y+2);
			locations.add(xloc);
		}
		if (y>1 && x>0) {
			XYLocation xloc = new XYLocation(x-1,y-2);
			locations.add(xloc);
		}
		if (x<7 && y<6) {
			XYLocation xloc = new XYLocation(x+1,y+2);
			locations.add(xloc);
		}
		if (x<7 && y>1) {
			XYLocation xloc = new XYLocation(x+1,y-2);
			locations.add(xloc);
		}
		if (x<6 && y<7) { // from b1
			XYLocation xloc = new XYLocation(x+2,y+1);
			locations.add(xloc);
		}
		if (x<6 && y>0) { // from b8
			XYLocation xloc = new XYLocation(x+2,y-1);
			locations.add(xloc);
		}
		if (x>1 &&(y>0&&y<7)) { // Replaces the below lines olj 29.07.20
			XYLocation xloc = new XYLocation(x-2,y+1);
			locations.add(xloc);
			XYLocation xloc1 = new XYLocation(x-2,y-1);
			locations.add(xloc1);
			
		}
/*		if (x == 6 && y == 0) {  // From g1
			XYLocation xloc = new XYLocation(x-2,y+1);
			locations.add(xloc);
		}
		if (x == 6 && y == 7) {  // From g8
			XYLocation xloc = new XYLocation(x-2,y-1);
			locations.add(xloc);
		}*/
		return locations;
	}

}
