// 9/12/14
// inheritance!! - extended by Robot, Mirror, Target

import java.awt.Color;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;
import java.awt.Image;

public class Occupant {
	private Color color;
	private Cell cell;

	public Occupant(Color co, Cell ce) {
		color = co;
		cell = ce;
	}

	public Color getColor() {
		return color;
	}

	public Cell getCell() {
		return cell;
	}

	public void setColor(Color c) {
		color = c;
	}

	public void setCell(Cell c) {
		cell = c;
	}

	public String toString() {
		String s = "";
		if (this instanceof Robot)
			s += "Robot (";
		else if (this instanceof Mirror)
			s += "Mirror " + ((Mirror)(this)).getDir() + "(";
		s += getCell().getR() + ", " + getCell().getC() + ")";
		Color c = getColor();
		if (c.equals(Color.red)) return s+=" red";
		if (c.equals(Color.green)) return s+=" green";
		if (c.equals(Color.yellow)) return s+=" yellow";
		if (c.equals(Color.blue)) return s+=" blue";
		if (c.equals(Color.black)) return s+=" black";
		return s;
	}
}

// 9/12/14

class Robot extends Occupant {
	private Target [] targets;
	private Cell origCell;
	private ArrayList<Cell> path;
	private char keyStroke;
	private ArrayList<Boolean> actuals;	// saves whether each cell in
							// path is an actual move

	public Robot(Color c) {
		super(c, null);
		targets = new Target[4];
		path = new ArrayList<Cell>();
		actuals = new ArrayList<Boolean>();
		actuals.add(Boolean.TRUE);
		for (int i = 0; i < 4; i++)
			targets[i] = new Target(getColor(), i);
		if (c.equals(Color.red)) keyStroke = 'r';
		if (c.equals(Color.green)) keyStroke = 'g';
		if (c.equals(Color.yellow)) keyStroke = 'y';
		if (c.equals(Color.blue)) keyStroke = 'b';
		if (c.equals(Color.black)) keyStroke = 'x';
	}

	public Target getTarget(int index) {
		return targets[index];
	}

	public Cell getOrigCell() {
		return origCell;
	}

	public char getKeyStroke() {
		return keyStroke;
	}

	public void setOrigCell(Cell c) {
		origCell = c;
	}

	public ArrayList<Boolean> getAllActuals() {
		return actuals;
	}

	public void setActual(int index, boolean b) {
		actuals.set(index, new Boolean(b));
	}

	public void setPath(ArrayList<Cell> arr) {
		path = arr;
	}

	public void resetPath() {
		path = new ArrayList<Cell>();
		actuals = new ArrayList<Boolean>();
		path.add(getCell());
		actuals.add(Boolean.TRUE);
	}

	public int getPathSize() {
		return path.size();
	}

	public Cell getPathIndex(int index) {
		return path.get(index);
	}

	public Cell getLastInPath() {
		return path.get(path.size()-1);
	}

	public void removeLastFromPath() {
		path.remove(path.size()-1);
		actuals.remove(path.size());
		//System.out.println(this + " removed " + path + "\n" + actuals);
	}

	public void removeFromPath(int index) {
		path.remove(index);
		actuals.remove(index);
	}

	public void addToPath(Cell c, boolean actualMove) {
		path.add(c);
		c.setActualMove(actualMove);
		actuals.add(new Boolean(actualMove));
		//System.out.println(this + " added " + path + "\n" + actuals);
	}

	public void moveTo(Cell c) {
		setCell(c);
	}
}

// 12/21/14

class Mirror extends Occupant {
	public boolean direction;	// true = upper right to lower left (+)
								// false = upper left to lower right (-)

	public Mirror (boolean d, Color c) {
		super(c, null);
		direction = d;
	}

	public boolean getDir() {
		return direction;
	}

	// 0 north, 1 east, 2 south, 3 west
	// k = robot is trying to go in that direction
	public int getNewKey(int k) {
		if (k < 0 || k > 3) return -1;
		int [] key1 = {1, 0, 3, 2};
		int [] key2 = {3, 2, 1, 0};
		if (direction)
			return key1[k];
		else
			return key2[k];
	}

	public static Color matchColor(int c) {
		if (c<0 || c>3) return null;
		Color [] colors = {Color.red, Color.green, Color.yellow, Color.blue};
		return colors[c];
	}

	public String toString() {
		return super.toString() + " direction: " + direction;
	}
}

// 9/21/14

class Target extends Occupant {
	private int index;		// if -1, rainbow
	private Image image;	// null unless rainbow

	public Target (Color c, int i) {
		super(c, null);
		index = i;
		if (c == null) {
			try {
				image = ImageIO.read(new File("spiral.gif"));
			} catch (IOException e) {
				System.err.println("ERROR: File not found - spiral.gif");
				System.exit(1);
			}
		}
	}

	public int getIndex() {
		return index;
	}

	public Image getImage() {
		return image;
	}

	public boolean equals(Object o) {
		if (o == null) return false;
		Target t = (Target)o;
		if (getColor() == null)
			return t.getColor() == null;
		return t.getColor().equals(getColor()) && t.getIndex() == getIndex();
	}

	public String toString() {
		String s = "Target #" + index + " (" + getCell().getR() + ", " + getCell().getC() + ")";
 		Color c = getColor();
		if (c.equals(Color.red)) return s+=" red";
		if (c.equals(Color.green)) return s+=" green";
		if (c.equals(Color.yellow)) return s+=" yellow";
		if (c.equals(Color.blue)) return s+=" blue";
		if (c.equals(Color.black)) return s+=" black";
		return s+=" rainbow";
	}
}