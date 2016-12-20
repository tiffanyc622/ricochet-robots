// 9/12/14

import java.util.Scanner;
import java.awt.Color;
import java.util.ArrayList;

public class RicochetBoard {
	private int boardNum;		// corresponds to name of text file
	private boolean [][] targetCells;	// places where targets go
	private Cell [][] grid;
	private ArrayList<Mirror> mirrors;

	// text file layout:
	// <number of cells with walls as first line>
	// <cell's row> <cell's column> <N> <E> <S> <W>
		// eg. 0 0 1 0 0 1 (1 = true = wall in that dir ie. walls at N and W)
		// each line is one cell
	// then cells to put targets, one per line: <r> <c>
	// then mirrors: <row> <col> <direction> <color>
		// direction: 1 = true, 0 = false
		// color: 0 red, 1 green, 2 yellow, 3 blue
		// eg. 0 0 1 3 (at (0, 0), + dir, blue)

	public RicochetBoard(int num) {
		grid = new Cell[16][16];
		targetCells = new boolean[16][16];
		mirrors = new ArrayList<Mirror>();
		boardNum = num;
	}

	public Cell [][] getGrid() {
		return grid;
	}

	public Cell getCell(int r, int c) {
		return grid[r][c];
	}

	public int getNum() {
		return boardNum;
	}

	public Cell getRandTargetCell() {
		ArrayList<Cell> tempList = new ArrayList<Cell>();
		for (int r = 0; r < targetCells.length; r++)
			for (int c = 0; c < targetCells[r].length; c++)
				if (targetCells[r][c])
					tempList.add(grid[r][c]);

		int rand = (int)(Math.random()*tempList.size());
		Cell chosen = tempList.get(rand);
		targetCells[chosen.getR()][chosen.getC()] = false;	// prevent other targets from being in same cell
		return chosen;
	}

	public void setNum(int i) {
		boardNum = i;
	}

	public void setUpGrid() {
		Scanner input = OpenFile.openToRead("board" + boardNum + ".txt");
		int numLines = input.nextInt();
		int lineNum = 0;
		while (lineNum < numLines) {			// walls
			Cell cell = new Cell(input.nextInt(), input.nextInt());
			for (int i = 0; i < 4; i++)
				cell.setWall(i, input.nextInt() == 1);
			grid[cell.getR()][cell.getC()] = cell;
			lineNum++;
		}
		for (int r = 0; r < grid.length; r++)	// cells with no walls - not in text file
			for (int c = 0; c < grid[r].length; c++)
				if (grid[r][c] == null)
					grid[r][c] = new Cell(r, c);

		for (int i = 0; i < 17; i++)					// targetCells
			targetCells[input.nextInt()][input.nextInt()] = true;

		int numMirrors = input.nextInt();	// sometimes 0
		for (int i = 0; i < numMirrors; i++) {			// mirrors
			Cell newCell = new Cell(input.nextInt(), input.nextInt());
			Mirror newMirror = new Mirror(input.nextInt()==1, Mirror.matchColor(input.nextInt()));
			mirrors.add(newMirror);
			newMirror.setCell(newCell);
			grid[newCell.getR()][newCell.getC()].setMirror(newMirror);
		}	
		input.close();
	}

	public boolean isValid(Cell c) {
		int x = c.getR(), y = c.getC();
		// can't be in the 4 blocks at the center of the board, or off the board
		return !((x == 7 || x == 8) && (y == 7 || y == 8)) && x >= 0
			&& x <= 15 && y >= 0 && y <= 15;
	}

	public void addOccupant(Robot o) {
		grid[o.getCell().getR()][o.getCell().getC()].setOccupant(o);
	}

	public void removeOccupant(Robot o) {
		grid[o.getCell().getR()][o.getCell().getC()].setOccupant(null);
	}

	public void removeOccupant(Cell c) {
		grid[c.getR()][c.getC()].setOccupant(null);
	}

	// dir: 0 = N, 1 = E, 2 = S, 3 = W
	// returns null if that cell isn't valid
	public Cell getAdjCell(Cell c, int dir) {
		switch (dir) {
			case 0:		// N
				if (c.getR() > 0) return grid[c.getR()-1][c.getC()];
				return null;
			case 1:		// E
				if (c.getC() < 15) return grid[c.getR()][c.getC()+1];
				return null;
			case 2:		// S
				if (c.getR() < 15) return grid[c.getR()+1][c.getC()];
				return null;
			case 3:		// W
				if (c.getC() > 0) return grid[c.getR()][c.getC()-1];
				return null;
		}
		return null;
	}

	public ArrayList<Mirror> getMirrors() {
		return mirrors;
	}
}