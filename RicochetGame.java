// 9/12/14

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.ArrayList;

public class RicochetGame {
	JFrame frame;
	GamePanel panel;

	public static void main (String [] args) {
		RicochetGame r = new RicochetGame();
		r.run();
	}

	public void run() {
		frame = new JFrame("Ricochet Robots");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(840, 750);
		
		panel = new GamePanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		
		frame.setResizable(false);
		frame.setVisible(true);
	}

	class GamePanel extends JPanel implements KeyListener {
		private JButton returnButton, newButton, newTargetButton, undoButton;
		private Robot [] robots;		// red green yellow blue black
		private RicochetBoard board;
		private Robot selectedRobot, lastMovedRobot;
		private Target selectedTarget, rainbowTarget;
		private int moveCount, time;
		private boolean success, showScores;
		private ArrayList<Robot> orderOfMoving;
		private Timer timer;
		private JButton timerButton, showScoresButton;
		private JTextArea scoresText;
		private static final int numBoards = 4;

		public GamePanel() {
			addKeyListener(this);

			robots = new Robot[5];
			robots[0] = new Robot(Color.red);
			robots[1] = new Robot(Color.green);
			robots[2] = new Robot(Color.yellow);
			robots[3] = new Robot(Color.blue);
			robots[4] = new Robot(Color.black);
			timer = new Timer(1000, new TimerAction());
			time = 60;
			scoresText = new JTextArea("  Edit this text as you please.\n"
				+ "  Points??\n"
				+ "  Person 1: 0 | Person 2: 0 | Person 3: 0");
			add(scoresText);
			scoresText.setBounds(300, 650, 350, 69);
			scoresText.setEditable(true);
			scoresText.setVisible(false);

			resetBoard();
			requestFocus();
			setLayout(null);
			setUpButtons();
		}

		public void setUpButtons() {
			undoButton = new JButton("Undo (Ctrl+Z)");
			returnButton = new JButton("Reset robots (Ctrl+R)");
			newButton = new JButton("New board (Ctrl+N)");
			newTargetButton = new JButton("New target (Ctrl+T)");

			add(undoButton);
			add(returnButton);
			add(newButton);
			add(newTargetButton);

			undoButton.setBounds(670, 190, 140, 50);
			returnButton.setBounds(670, 360, 140, 50);
			newButton.setBounds(670, 430, 140, 50);
			newTargetButton.setBounds(670, 500, 140, 50);

			undoButton.addActionListener(new UndoAction());
			returnButton.addActionListener(new ReturnAction());
			newButton.addActionListener(new NewAction());
			newTargetButton.addActionListener(new NewTargetAction());

			Font font = new Font("Arial", Font.PLAIN, 12);
			undoButton.setFont(font);
			returnButton.setFont(font);
			newButton.setFont(font);
			newTargetButton.setFont(font);

			timerButton = new JButton("Start timer");
			add(timerButton);
			timerButton.setBounds(80, 660, 140, 50);
			timerButton.addActionListener(new TimerButtonAction());
			timerButton.setFont(font);

			showScoresButton = new JButton("Show scores");
			add(showScoresButton);
			showScoresButton.setBounds(670, 660, 140, 50);
			showScoresButton.addActionListener(new ShowScoresAction());
			showScoresButton.setFont(font);
		}

		public void resetBoard() {
			board = new RicochetBoard((int)(Math.random()*numBoards));
			board.setUpGrid();
			for (int i = 0; i < 5; i++) {		// add robots
				int r, c;
				Cell newCell;
				do {
					r = (int)(Math.random()*16);
					c = (int)(Math.random()*16);
					newCell = board.getCell(r, c);
				}	// 2 robots can't be in same cell
				while (newCell.getMirror() != null || newCell.isOccupied() || !board.isValid(newCell));
				robots[i].setCell(newCell);
				robots[i].setOrigCell(newCell);
				board.addOccupant(robots[i]);
			}

			rainbowTarget = new Target(null, -1);
			rainbowTarget.setCell(board.getRandTargetCell());

			for (int i = 0; i < 4; i++)			// place all targets in a cell
				for (int j = 0; j < 4; j++)
					robots[i].getTarget(j).setCell(board.getRandTargetCell());

			orderOfMoving = new ArrayList<Robot>();
			chooseNewTarget();
			resetRobotPaths();
			moveCount = 0;
			success = false;
			repaint();
		}

		public void paintComponent(Graphics g) {
			requestFocus();
			super.paintComponent(g);
			drawNotes(g);
			drawGrid(g);
			drawWalls(g);
			drawTarget(g);
			drawMirrors(g);
			drawPaths(g);
			drawRobots(g);
			g.setColor(Color.black);
			g.drawString("Move Count: " + moveCount, 670, 270);
			if (success) {
				g.setColor(Color.red);
				g.drawString("Success! Click a button", 670, 305);
				g.drawString("below.", 670, 325);
			}
			if (time >= 0) drawTime(g);
			//showAllTargets(g);
		}

		public void drawTime(Graphics g) {
			g.setColor(Color.black);
			g.setFont(new Font("Arial", Font.BOLD, 20));
			g.drawString(time/60 + ":" + String.format("%02d", time%60), 20, 690);
		}

		public void drawNotes(Graphics g) {
			g.setColor(Color.black);
			g.drawString("Use arrow keys to move", 670, 30);
			g.drawString("selected robot (cyan).", 670, 45);
			g.drawString("Switch robots:", 670, 85);
			g.drawString("r = red", 690, 100);
			g.drawString("g = green", 690, 115);
			g.drawString("y = yellow", 690, 130);
			g.drawString("b = blue", 690, 145);
			g.drawString("x = black", 690, 160);
		}

		public void drawGrid(Graphics g) {
			g.setColor(Color.lightGray);
			g.fillRect(0, 0, 640, 640);
			g.setColor(Color.black);
			for (int i = 0; i <= 640; i+=40) {	// each box is 40x40
				g.drawLine(0, i, 640, i);	// horizontal
				g.drawLine(i, 0, i, 640);	// vert
			}
		}

		public void drawWalls(Graphics g) {
			g.setColor(Color.black);
			for (int r = 0; r < 16; r++) {
				for (int c = 0; c < 16; c++) {
					boolean [] cellWalls = board.getCell(r, c).getWallsArr();
					if (cellWalls[0])	// N
						g.fillRect(c*40-2, r*40-2, 44, 4);
					if (cellWalls[1])	// E
						g.fillRect(c*40+38, r*40-2, 4, 44);
					if (cellWalls[2])	// S
						g.fillRect(c*40-2, r*40+38, 44, 4);
					if (cellWalls[3])	// W
						g.fillRect(c*40-2, r*40-2, 4, 44);
				}
			}
		}

		public void drawTarget(Graphics g) {
			Cell chosenCell = selectedTarget.getCell();
			if (selectedTarget.getColor() == null) {
				g.drawImage(selectedTarget.getImage(), chosenCell.getC()*40+5, chosenCell.getR()*40+5, 30, 30, this);
				return;
			}
			g.setColor(selectedTarget.getColor());
			g.fillOval(chosenCell.getC()*40+5, chosenCell.getR()*40+5, 30, 30);
			g.setColor(Color.black);
			g.drawString("" + selectedTarget.getIndex(), chosenCell.getC()*40+17, chosenCell.getR()*40+25);
		}

		public void drawMirrors(Graphics g) {
			for (Mirror m: board.getMirrors()) {
				g.setColor(m.getColor());
				int r = m.getCell().getR(), c = m.getCell().getC();
				if (m.getDir()) {
					g.drawLine(c*40+40, r*40, c*40, r*40+40);
				}
				else {
					g.drawLine(c*40, r*40, c*40+40, r*40+40);
				}
			}
		}

		public void drawPaths(Graphics g) {
			for (int i = 0; i < robots.length; i++) {
				g.setColor(robots[i].getColor());
				if (robots[i].getPathSize() > 0)
					for (int j = 0; j < robots[i].getPathSize()-1; j++) {	// paths of diff colors differ in location by one pixel
						Cell cell1 = robots[i].getPathIndex(j), cell2 = robots[i].getPathIndex(j+1);
						g.drawLine(cell1.getC()*40+23-i, cell1.getR()*40+23-i,
							cell2.getC()*40+23-i, cell2.getR()*40+23-i);
					}
			}
		}

		public void drawRobots(Graphics g) {
			g.setColor(Color.cyan);
			g.fillRect(selectedRobot.getCell().getC()*40+1, selectedRobot.getCell().getR()*40+1, 39, 39);
			for (int i = 0; i < robots.length; i++) {
				Robot r = robots[i];
				g.setColor(r.getColor());
				g.fillRect(r.getCell().getC()*40+10, r.getCell().getR()*40+10, 20, 20);
			}
		}

		public void showAllTargets(Graphics g) {
			Cell rainbowCell = rainbowTarget.getCell();
			g.drawImage(rainbowTarget.getImage(), rainbowCell.getC()*40+5, rainbowCell.getR()*40+5, 30, 30, this);
			for (int i = 0; i < 4; i++)
				for (int j = 0; j < 4; j++) {
					Target currentT = robots[i].getTarget(j);
					Cell currentC = currentT.getCell();
					g.setColor(currentT.getColor());
					g.fillOval(currentC.getC()*40+5, currentC.getR()*40+5, 30, 30);
					g.setColor(Color.black);
					g.drawString("" + currentT.getIndex(), currentC.getC()*40+17, currentC.getR()*40+25);
				}
		}

		public void undo() {
			requestFocus();
			if (orderOfMoving.size() == 0) return;
			Robot lastMovedRobot = orderOfMoving.get(orderOfMoving.size()-1);
			if (lastMovedRobot.getPathSize() < 2) return;
			board.removeOccupant(lastMovedRobot);
			//System.out.println(lastMovedRobot + " 1 removing " + lastMovedRobot.getLastInPath());
			lastMovedRobot.removeLastFromPath();
			lastMovedRobot.moveTo(lastMovedRobot.getLastInPath());
			board.addOccupant(lastMovedRobot);
			orderOfMoving.remove(orderOfMoving.size()-1);
			ArrayList<Boolean> allActuals = lastMovedRobot.getAllActuals();
			while (!allActuals.get(allActuals.size()-1).booleanValue()) {
				board.removeOccupant(lastMovedRobot);
				//System.out.println(lastMovedRobot + " 2 removing " + lastMovedRobot.getLastInPath());
				lastMovedRobot.removeLastFromPath();
				lastMovedRobot.moveTo(lastMovedRobot.getLastInPath());
				board.addOccupant(lastMovedRobot);
			}
			if (moveCount > 0)
				moveCount--;
			success = false;
			repaint();
		}

		public void returnRobots() {
			success = false;
			requestFocus();
			for (int i = 0; i < robots.length; i++) {
				board.removeOccupant(robots[i]);
				robots[i].setCell(robots[i].getOrigCell());
				board.addOccupant(robots[i]);
			}
			resetRobotPaths();
			orderOfMoving = new ArrayList<Robot>();
			moveCount = 0;
			repaint();
		}

		public void chooseNewTarget() {
			requestFocus();
			if (!success)
				returnRobots();
			for (int i = 0; i < 5; i++)
				robots[i].setOrigCell(robots[i].getCell());
			boolean valid = true;
			do {
				valid = true;
				int randRobot = (int)(Math.random()*17);
				if (randRobot == 16) {		// rainbow target chosen
					selectedTarget = rainbowTarget;
					for (int i = 0; i < 5; i++)
						if (robots[i].getCell().equals(selectedTarget.getCell()))
							valid = false;
				}
				else {						// some other color target chosen
					selectedRobot = robots[(int)(Math.random()*4)];
					selectedTarget = selectedRobot.getTarget((int)(Math.random()*4));
					valid = !selectedRobot.getCell().equals(selectedTarget.getCell());
				}
			} while (!valid);
			resetRobotPaths();
			orderOfMoving = new ArrayList<Robot>();
			moveCount = 0;
			success = false;
			repaint();
		}

		public void resetRobotPaths() {
			requestFocus();
			for (int i = 0; i < robots.length; i++)
				robots[i].resetPath();
		}

		private class TimerAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				time--;
				repaint();
			}
		}

		private class TimerButtonAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				timer.stop();
				if (timerButton.getText().equals("Start timer")) {	
					timer.start();
					timerButton.setText("Stop timer");
				}
				else {
					time = 60;
					timerButton.setText("Start timer");
				}
				repaint();
			}
		}

		private class ShowScoresAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				if (scoresText.isVisible()) {
					showScoresButton.setText("Show scores");
					scoresText.setVisible(false);
				}
				else {
					showScoresButton.setText("Hide scores");
					scoresText.setVisible(true);
				}
			}
		}

		private class UndoAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				undo();
			}
		}

		private class ReturnAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				returnRobots();
			}
		}

		private class NewAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				resetBoard();
			}
		}

		private class NewTargetAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				chooseNewTarget();
			}
		}

		public void moveRobot(int key) {
			Cell cell1 = selectedRobot.getCell();
			Cell oldCell = cell1;
			Cell newCell = board.getAdjCell(oldCell, key);
			while (newCell != null && !oldCell.getWall(key) &&
				!(newCell.getWall(Cell.getOppDir(key))) && !newCell.isOccupied()) {
				Mirror mirror = newCell.getMirror();
				if (mirror != null && !mirror.getColor().equals(selectedRobot.getColor())) {
					selectedRobot.addToPath(newCell, false);
					//System.out.println(newCell + " false");
					key = mirror.getNewKey(key);
				}
				board.removeOccupant(oldCell);
				oldCell = newCell;
				selectedRobot.moveTo(newCell);
				newCell.setOccupant(selectedRobot);
				newCell = board.getAdjCell(oldCell, key);
			}
			if (!selectedRobot.getCell().equals(cell1)) {
				moveCount++;
				if (selectedRobot.getCell().getMirror() != null) {
					//System.out.println(selectedRobot.getLastInPath() + " jk true");
					selectedRobot.setActual(selectedRobot.getPathSize()-1, true);
				}
				else {
					//System.out.println(selectedRobot.getCell() + " true");
					selectedRobot.addToPath(selectedRobot.getCell(), true);
				}
				orderOfMoving.add(selectedRobot);
			}
			if (selectedTarget.getColor() == null && selectedRobot.getCell().equals(selectedTarget.getCell()))
				success = true;
			else if (selectedRobot.getColor().equals(selectedTarget.getColor()) &&
				selectedRobot.getCell().equals(selectedTarget.getCell()))
				success = true;
		}

		public void keyPressed(KeyEvent e) {
			int key = e.getKeyCode();
			// meta = command key on mac
			if (e.isControlDown() || e.isMetaDown()) {
				switch (key) {
					case KeyEvent.VK_Z: undo(); break;
					case KeyEvent.VK_N: resetBoard(); break;
					case KeyEvent.VK_T: chooseNewTarget(); break;
					case KeyEvent.VK_R: returnRobots(); break;
				}
				return;
			}
			if (success) return;
			switch (key) {
				case KeyEvent.VK_UP: key = 0; break;
				case KeyEvent.VK_RIGHT: key = 1; break;
				case KeyEvent.VK_DOWN: key = 2; break;
				case KeyEvent.VK_LEFT: key = 3; break;
			}
			moveRobot(key);
			repaint();
		}

		public void keyTyped(KeyEvent e) {
			if (success) return;
			char c = e.getKeyChar();
			for (int i = 0; i < 5; i++)
				if (robots[i].getKeyStroke() == c)
					selectedRobot = robots[i];
			//System.out.println(c + " " + selectedRobot);
			repaint();
		}

		public void keyReleased(KeyEvent e) {}
	}
}