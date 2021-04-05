package com.marginallyclever.makelangelo.nodes.maze;

import com.marginallyclever.core.Translator;
import com.marginallyclever.core.node.NodeConnectorInteger;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.makelangelo.nodes.TurtleGenerator;

/**
 * Makes a "well formed" maze.
 * @see <a href='https://en.wikipedia.org/wiki/Maze_generation_algorithm#Recursive_backtracker'>Wikipedia</a>
 * @author Dan Royer
 */
public class Generator_Maze extends TurtleGenerator {
	// controls complexity of maze
	private NodeConnectorInteger inputRows = new NodeConnectorInteger(Translator.get("Generator_Maze.inputRows"),10);
	// controls complexity of maze
	private NodeConnectorInteger inputCols = new NodeConnectorInteger(Translator.get("Generator_Maze.inputCols"),10);

	protected double xMax, xMin, yMax, yMin;
	protected MazeCell[] cells;
	protected MazeWall[] walls;
	
	public Generator_Maze() {
		super();
		inputs.add(inputRows);
		inputs.add(inputCols);
		inputRows.setDescription(Translator.get("Generator_Maze.inputRows.tooltip"));
		inputCols.setDescription(Translator.get("Generator_Maze.inputCols.tooltip"));
	}

	@Override
	public String getName() {
		return Translator.get("Generator_Maze.name");
	}

	/**
	 * build a list of walls in the maze, cells in the maze, and how they connect to each other.
	 */
	@Override
	public boolean iterate() {
		int rowCount = inputRows.getValue();
		int colCount = inputCols.getValue();
		
		// build the cells
		cells = new MazeCell[rowCount * colCount];

		int x, y, i = 0;
		for (y = 0; y < rowCount; ++y) {
			for (x = 0; x < colCount; ++x) {
				cells[i] = new MazeCell();
				cells[i].visited = false;
				cells[i].onStack = false;
				cells[i].x = x;
				cells[i].y = y;
				++i;
			}
		}

		// build the graph
		walls = new MazeWall[((rowCount - 1) * colCount) + ((colCount - 1) * rowCount)];
		i = 0;
		for (y = 0; y < rowCount; ++y) {
			for (x = 0; x < colCount; ++x) {
				if (x < colCount - 1) {
					// vertical wall between horizontal cells
					walls[i] = new MazeWall();
					walls[i].removed = false;
					walls[i].cellA = y * colCount + x;
					walls[i].cellB = y * colCount + x + 1;
					++i;
				}
				if (y < rowCount - 1) {
					// horizontal wall between vertical cells
					walls[i] = new MazeWall();
					walls[i].removed = false;
					walls[i].cellA = y * colCount + x;
					walls[i].cellB = y * colCount + x + colCount;
					++i;
				}
			}
		}

		int unvisitedCells = cells.length; // -1 for initial cell.
		int cellsOnStack = 0;

		// Make the initial cell the current cell and mark it as visited
		int currentCell = 0;
		cells[currentCell].visited = true;
		--unvisitedCells;

		// While there are unvisited cells
		while (unvisitedCells > 0) {
			// If the current cell has any neighbours which have not been visited
			// Choose randomly one of the unvisited neighbours
			int nextCell = chooseUnvisitedNeighbor(currentCell);
			if (nextCell != -1) {
				// Push the current cell to the stack
				cells[currentCell].onStack = true;
				++cellsOnStack;
				// Remove the wall between the current cell and the chosen cell
				int wallIndex = findWallBetween(currentCell, nextCell);
				assert (wallIndex != -1);
				walls[wallIndex].removed = true;
				// Make the chosen cell the current cell and mark it as visited
				currentCell = nextCell;
				cells[currentCell].visited = true;
				--unvisitedCells;
			} else if (cellsOnStack > 0) {
				// else if stack is not empty pop a cell from the stack
				for (i = 0; i < cells.length; ++i) {
					if (cells[i].onStack) {
						// Make it the current cell
						currentCell = i;
						cells[i].onStack = false;
						--cellsOnStack;
						break;
					}
				}
			}
		}

		// draw the maze
		Turtle turtle = new Turtle();
		drawMaze(turtle);

		outputTurtle.setValue(turtle);
		
	    return false;
	}

	private void drawMaze(Turtle turtle) {
		int rowCount = inputRows.getValue();
		int colCount = inputCols.getValue();

		double pw = inputWidth.getValue();
		double ph = inputHeight.getValue();
		yMin = -ph/2;
		yMax = ph/2;
		xMin = -pw/2;
		xMax = pw/2;
		
		double w = (xMax - xMin) / colCount;
		double h = (yMax - yMin) / rowCount;

		turtle.reset();
		
		// Draw outside edge
		turtle.jumpTo(xMin, yMax);
		turtle.moveTo(xMax, yMax);
		turtle.moveTo(xMax, yMin + h);
		// bottom right gap for exit is here
		turtle.jumpTo(xMax, yMin);
		turtle.moveTo(xMin, yMin);
		// top-left gap for entrance is left here
		turtle.moveTo(xMin, yMax - h);

		int i;
		for (i = 0; i < walls.length; ++i) {
			if (walls[i].removed)
				continue;
			int a = walls[i].cellA;
			int b = walls[i].cellB;
			int ax = cells[a].x;
			int ay = cells[a].y;
			int bx = cells[b].x;
			int by = cells[b].y;
			if (ay == by) {
				// vertical wall
				double x = xMin + (ax + 1) * w;
				double y0 = yMin + (ay + 0) * h;
				double y1 = yMin + (ay + 1) * h;
				turtle.jumpTo(x, y0);
				turtle.moveTo(x, y1);
			} else if (ax == bx) {
				// horizontal wall
				double x0 = xMin + (ax + 0) * w;
				double x1 = xMin + (ax + 1) * w;
				double y = yMin + (ay + 1) * h;
				turtle.jumpTo(x0, y);
				turtle.moveTo(x1, y);
			}
		}
	}

	private int chooseUnvisitedNeighbor(int currentCell) {
		int rowCount = inputRows.getValue();
		int colCount = inputCols.getValue();
		
		int x = cells[currentCell].x;
		int y = cells[currentCell].y;

		int[] candidates = new int[4];
		int found = 0;

		// left
		if (x > 0 && cells[currentCell - 1].visited == false) {
			candidates[found++] = currentCell - 1;
		}
		// right
		if (x < colCount - 1 && !cells[currentCell + 1].visited) {
			candidates[found++] = currentCell + 1;
		}
		// up
		if (y > 0 && !cells[currentCell - colCount].visited) {
			candidates[found++] = currentCell - colCount;
		}
		// down
		if (y < rowCount - 1 && !cells[currentCell + colCount].visited) {
			candidates[found++] = currentCell + colCount;
		}

		if (found == 0)
			return -1;

		// choose a random candidate
		int choice = (int) (Math.random() * found);
		assert (choice >= 0 && choice < found);

		return candidates[choice];
	}

	private int findWallBetween(int currentCell, int nextCell) {
		int i;
		for (i = 0; i < walls.length; ++i) {
			if (walls[i].cellA == currentCell || walls[i].cellA == nextCell) {
				if (walls[i].cellB == currentCell || walls[i].cellB == nextCell)
					return i;
			}
		}
		return -1;
	}
}
