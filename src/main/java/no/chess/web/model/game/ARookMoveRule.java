package no.chess.web.model.game;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import aima.core.util.datastructure.XYLocation;
import no.chess.web.model.Position;
import no.games.chess.AbstractGamePiece;
import no.games.chess.MoveRule;

/**
 * ARookMoveRule
 * This ruler calculates all the legal moves for a rook 
 * @author oluf
 *
 */
public class ARookMoveRule implements MoveRule<AbstractGamePiece, List<XYLocation>> {

	@Override
	public List<XYLocation> calculateRule(AbstractGamePiece t) {

		
		String name = null;
		Position from = null;
		if (t instanceof ARook) {
			ARook r = (ARook) t;
			name = r.getMyPiece().getName();
			from = r.getmyPosition();
		}
		if (t instanceof AQueen) {	
			AQueen q = (AQueen)t;
			name = q.getMyPiece().getName();
			from = q.getmyPosition();
		}
		

		XYLocation loc = from.getXyloc();
		int x = loc.getXCoOrdinate();
		int y = loc.getYCoOrdinate();
		String xpos = Integer.toString(x);
		String ypos = Integer.toString(y);
		List<XYLocation> locations = new ArrayList<>();
		if (x+1 < 8) {
			for (int i = x+1;i<8;i++) {
				XYLocation xloc = new XYLocation(i,y);
				locations.add(xloc);
			}
		}
		if (y + 1 < 8) {
			for (int i = y+1;i<8;i++) {
				XYLocation xloc = new XYLocation(x,i);
				locations.add(xloc);
			}
		}
		if (x-1>=0 ) {
			for (int i = x-1;i>=0;i--) {
				XYLocation xloc = new XYLocation(i,y);
				locations.add(xloc);
			}
		}
		if (y-1>=0) {
			for (int i = y-1;i>=0;i--) {
				XYLocation xloc = new XYLocation(x,i);
				locations.add(xloc);
			}
		}

		return locations;
	}

}
