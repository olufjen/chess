package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.List;

import aima.core.util.datastructure.XYLocation;
import no.chess.web.model.Position;
import no.games.chess.AbstractGamePiece;
import no.games.chess.MoveRule;

/**
 * ABishopMoveRule
 * This ruler calculates all legal moves for a bishop
 * and returns a list of XYLocations reachable by the bishop.
 * @since 16.08.20
 * Changed  <7 to <=7 olj
 * @author oluf
 *
 */
public class ABishopMoveRule implements MoveRule<AbstractGamePiece, List<XYLocation>> {

	@Override
	public List<XYLocation> calculateRule(AbstractGamePiece t) {
		
		String name = null;
		Position from = null;
		if (t instanceof AQueen) {	
			AQueen q = (AQueen)t;
			name = q.getMyPiece().getName();
			from = q.getmyPosition();
		}		
		if (t instanceof ABishop) {	
			ABishop b = (ABishop)t;
			name = b.getMyPiece().getName();
			from = b.getmyPosition();
		}		
		
		XYLocation loc = from.getXyloc();
		int x = loc.getXCoOrdinate();
		int y = loc.getYCoOrdinate();
		String xpos = Integer.toString(x);
		String ypos = Integer.toString(y);
		List<XYLocation> locations = new ArrayList<>();
		int j = x + 1;
		if (j<=7) {
			for (int i = y+1;i<=7;i++) {
				XYLocation xloc = new XYLocation(j,i);
				locations.add(xloc);
				if (j<7) // not <=7
					j++;
				else
					break;
			}
		}
		j = x -1;
		if (j>0) {
			for (int i = y+1;i<=7;i++) {
				XYLocation xloc = new XYLocation(j,i);
				locations.add(xloc);
				if (j>0)
					j--;
				else
					break;
			}
		}
		j = x + 1;
		if (j <=7 && y>0) {
			for (int i = y-1;i>=0;i--) { //Changed from i>0
				XYLocation xloc = new XYLocation(j,i);
				locations.add(xloc);
				if (j<7) // not <=7
					j++;
				else
					break;				
			}
		}
		j = x -1;
		if (j > 0 && y>0) {
			for (int i = y-1;i>=0;i--) { //Changed from i>0
				XYLocation xloc = new XYLocation(j,i);
				locations.add(xloc);
				if (j>0)
					j--;
				else
					break;				
			}
		}
		
		return locations;
	}

}
