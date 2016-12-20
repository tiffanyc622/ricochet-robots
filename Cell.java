// 9/12/14

import java.awt.Color;

public class Cell {
	private boolean [] walls;		// length 4 = N E W S
									// true = wall
	private Robot occupant;		// null if empty
	private int r, c;				// coordinates in grid
	private Mirror mirror;
	private boolean isActualMove;	// almost always true except when:
						// is being added to path at the point of a mirror
						// and therefore needs to be drawn as a pt but
						// doesn't count as a move
						// when undoing, undo past it

	public Cell(int i1, int i2) {
		walls = new boolean[4];
		r = i1;
		c = i2;
		isActualMove = true;
	}

	public int getR() {
		return r;
	}

	public int getC() {
		return c;
	}

	public Robot getOccupant() {
		return occupant;
	}

	public void setOccupant(Robot o) {
		occupant = o;
	}

	public boolean isOccupied() {
		return occupant != null;
	}

	public void setWallsArr(boolean [] arr) {
		walls = arr;
	}

	public void setWall(int i, boolean b) {
		walls[i] = b;
	}

	public boolean [] getWallsArr () {
		return walls;
	}

	// index: 0 = N, 1 = E, 2 = S, 3 = W
	public boolean getWall(int index) {
		return walls[index];
	}

	public Mirror getMirror() {
		return mirror;
	}

	public void setMirror(Mirror m) {
		mirror = m;
	}

	public boolean equals (Object o) {
		Cell other = (Cell)o;
		return other != null && other.getR() == r && other.getC() == c;
	}

	public String toString() {
		String s = "Cell (" + r + ", " + c + ")";
		if (walls[0]) s += " N";
		if (walls[1]) s += " E";
		if (walls[2]) s += " S";
		if (walls[3]) s += " W";
		return s;
	}

	public static int getOppDir(int i) {
		switch (i) {
			case 0: return 2;
			case 1: return 3;
			case 2: return 0;
			case 3: return 1;
		}
		return -1;
	}

	public void setActualMove(boolean b) {
		isActualMove = b;
	}

	public boolean isActualMove() {
		return isActualMove;
	}
}