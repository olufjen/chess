package no.chess.web.server.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import no.chess.web.model.ChessMoves;

/**
 * Denne klasses er felles for alle resursklasser i Sjakk
 * 
 * @author oluf
 *
 */
public class ChessServerResource extends SessionServerResource {


	protected String moveId = "move";
	protected String pieceId = "piece";
	protected String piece = "pp"; // name of opponent piece when opponent has made a move
	protected String soldPosition = "p";
	protected String snewPosition = "p";
	protected String newPos = "x"; // Contains the opponent new position as a string during a game
	protected String oldPos = "y"; // Contains the opponent old position as a string during a game
	protected String newPosId = "newpos";
	protected String oldPosId = "oldpos";
	protected String pawnId = "whitepawn1";
	protected String fenPosid = "fenpos";
	protected String popupId ="popup";
	protected String chessBoardsession = "chessboard";
	protected String gameboardSession = "gameboard";
	
	protected String[] positionId = {"a1","b1","c1","d1","e1","f1","g1","h1","a2","b2",
			"c2","d2","e2","f2","g2","h2","a3","b3","c3","d3","e3","f3",
			"g3","h3","a4","b4","c4","d4","e4","f4","g4","h4","a5","b5",
			"c5","d5","e5","f5","g5","h5","a6","b6","c6","d6","e6","f6",
			"g6","h6","a7","b7","c7","d7","e7","f7","g7","h7","a8","b8",
			"c8","d8","e8","f8","g8","h8"};
	
	public ChessServerResource() {
		super();
	
	}



	public String getOldPos() {
		return oldPos;
	}



	public void setOldPos(String oldPos) {
		this.oldPos = oldPos;
	}



	public String getFenPosid() {
		return fenPosid;
	}



	public void setFenPosid(String fenPosid) {
		this.fenPosid = fenPosid;
	}



	public String[] getPositionId() {
		return positionId;
	}



	public void setPositionId(String[] positionId) {
		this.positionId = positionId;
	}



	public String getPawnId() {
		return pawnId;
	}

	public void setPawnId(String pawnId) {
		this.pawnId = pawnId;
	}


	public String getMoveId() {
		return moveId;
	}

	public void setMoveId(String moveId) {
		this.moveId = moveId;
	}

	public String getPiece() {
		return piece;
	}

	public void setPiece(String piece) {
		this.piece = piece;
	}

	public String getNewPos() {
		return newPos;
	}

	public void setNewPos(String newPos) {
		this.newPos = newPos;
	}

	public String getPieceId() {
		return pieceId;
	}

	public void setPieceId(String pieceId) {
		this.pieceId = pieceId;
	}

	 public String printGame(ArrayList<ChessMoves> chessMoves) throws Exception {

		 String headerText = "Chess moves";


       	 /** create temporary pdf file **/
 		File temp = File.createTempFile("game", ".pdf"); 
 	    String path = temp.getAbsolutePath();

 		Document document = new Document(PageSize.A4, 50, 50, 50, 50);

 		// Listing 2. Creation of PdfWriter object
 		PdfWriter writer = PdfWriter.getInstance(document,
 				new FileOutputStream(path));
 		
 		document.open();
 		PdfPTable table = createTable("Game","Game");
 		for (ChessMoves chessMove:chessMoves) {
 			String moveNr = Integer.toString(chessMove.getMoveNr());
 			table.addCell(moveNr+": ");
 			table.addCell(chessMove.getWhiteMove()+" "+ chessMove.getBlackMove());
 			table.completeRow();
 		}
 	
 		
    	 


    	 document.add(table);
    	 document.close();
    	 return path;

	 }	
		private PdfPTable createTable(String heading, String nokkel){
			
			if(nokkel == null){
				nokkel ="";
			}
		
	      //specify column widths
	        float[] columnWidths = {2f, 3f};
	     // a table with two columns
	        PdfPTable table = new PdfPTable(columnWidths);
	     // set table width a percentage of the page width
	        table.setWidthPercentage(80f);
	        
	     // creation of paragraph object
			Paragraph heading1 = new Paragraph();
			
				
			heading1 = getHeading1(heading);
			PdfPCell headingCell = new PdfPCell();
			
			headingCell.setColspan(2);
			headingCell.addElement(heading1);
			table.addCell(headingCell);
			PdfPCell cellh1 = new PdfPCell();
			PdfPCell cellh2 = new PdfPCell();
		   if(!nokkel.isEmpty()){
			   	String nokkelHeading ="Game";
		        cellh1.addElement(getHeading2(nokkelHeading));
				table.addCell(cellh1);
				cellh2.addElement(getText(nokkel));
				table.addCell(cellh2);
		   }	     

			int cts = 0;
			char scount = Character.forDigit(cts, 10);
//			String gmlhtemp = "";
			return table;
		}
		private Paragraph getHeading1(String txt){
			Paragraph p = new Paragraph();
			Chunk chunk = new Chunk(txt);
	        chunk.setUnderline(0.2f, -2f);
	        Font font = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
	        chunk.setFont(font);
	        p.setAlignment(Element.ALIGN_CENTER);
	        p.setSpacingAfter(20);
	        p.add(chunk);
	        
	        return p;
		}
		private Paragraph getHeading2(String txt){
			Paragraph p = new Paragraph();
	        Chunk chunk = new Chunk(txt,  
	        		new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD));
	        p.setAlignment(Element.ALIGN_LEFT);
	        p.add(chunk);
	        return p;
		}
		private Paragraph getText(String txt){
			Paragraph p = new Paragraph();
	        Chunk chunk = new Chunk(txt,  
	        		new Font(Font.FontFamily.TIMES_ROMAN, 10));
	        p.setAlignment(Element.ALIGN_LEFT);
	        p.add(chunk);
	        
	        return p;
		}		
	
}
