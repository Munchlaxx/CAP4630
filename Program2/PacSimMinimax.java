/*
 * University of Central Florida
 * CAP4630 - Spring 2019
 * Authors: <John Mirschel, Wen Lam>
 */

/*
 * Simulation using minimax to evaulate board states
 *
 * Evaulation function:
 * First evaulation: Calculate movement distance from pacman to each ghosts and subtracting the score by the reciprocal of the distance multiplied by 4.
 *					 Gives a penalty based off the distance between pacmana and the ghosts. Higher penalty the closer they are.
 * 					 Ignore values if ghost is in FEAR mode.
 *
 * Second evaluation: Calculate number of food left on the board and adding the reciprocal to the total score
 *					  Incentivizes pacman to keep clearing food.
 *
 * Third evaluation: Calculate number of power pellets left on the board and adding the reciprocal to the total score
 *					 Incentivizes pacman to keep clearing power pellets.
 *
 * Fourth evaluation: Calculate the distance between pacman and the nearest food and adding the reciprocal of the distance.
 *					  Incentivizes pacman to go toward location with food.
 *
 * Fifth evaluation: Keep track of all location that pacman have been and subtracts the score by how often pacman goes there.
 *					 Gives a penalty for stuttering in a location for too long. Higher penalty the longer it stutters.
 *					 Promotes going a new direction.
 *
 * Sixth evaluation: Looks for the nearest intersection from pacman and subtract the score based off the distance divided by 2.
 *					 Gives a penalty if pacman is in location that are likely to be cornered by ghosts. Higher penalty the farther away from an intersection.
 */

import java.awt.Point;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import pacsim.BFSPath;
import pacsim.PacAction;
import pacsim.PacCell;
import pacsim.GhostCell;
import pacsim.PacFace;
import pacsim.PacSim;
import pacsim.PacUtils;
import pacsim.PacmanCell;

public class PacSimMinimax implements PacAction {
	
	// Depth and visited variables
    private int depth;
	HashMap<Point, Double> visited;
	
	// Movement for pacman and the ghosts (up, right, down, left)
	private int[] xDir = {0, 1,  0, -1};
	private int[] yDir = {1, 0, -1,  0};
	private Point[] points = new Point[8];
	  
	public PacSimMinimax( int depth, String fname, int te, int gran, int max ) {
		
		// Initialize the depth
		this.depth = depth;
		
		PacSim sim = new PacSim( fname, te, gran, max );
		sim.init(this);
	}
   
	public static void main( String[] args ) {  
		String fname = args[ 0 ];
		int depth = Integer.parseInt(args[ 1 ]);
		
		int te = 0;
		int gr = 0;
		int ml = 0;
		
		if( args.length == 5 ) {
			te = Integer.parseInt(args[ 2 ]);
			gr = Integer.parseInt(args[ 3 ]);
			ml = Integer.parseInt(args[ 4 ]);
		}
		
		new PacSimMinimax( depth, fname, te, gr, ml );
		
		System.out.println("\nAdvesarial Search using Minimax by John Mirschel and Wen Lam:");
		System.out.println("\n	Game board	: " + depth + "\n" );
		System.out.println("	Search depth : " + depth + "\n");
		if( te > 0 ) {
			System.out.println(" Preliminary runs : " + te + "\n Granularity : " + gr + "\n Max move limit : " + ml + "\n\nPreliminary run results :\n");
		}
	}

	@Override
	public void init() {
		// Initialize the visited table
		visited = new HashMap<>();
		populatePoints();
	}
	   
	@Override
	public PacFace action( Object state ) {

		PacCell[][] grid = (PacCell[][]) state;
		PacmanCell pc = PacUtils.findPacman(grid);
		
		// Initialize the possible grid states and a copy of visited location
		List<PossibleDir> scores = new ArrayList<>();
		HashMap<Point, Double> visitedCopy = copyHash(visited);
		
		// Call minimax to check grid states
		minimax(grid, new PossibleDir(), scores, visitedCopy, 0, true);
		
		// Sort the value of all states
		scores.sort(Comparator.comparing(PossibleDir::getScore));
		
		// Take the highest score
		Point next = scores.get(scores.size() - 1).getPoint();
		
		// Change the value of the visited table for the official movement
		if(visited.containsKey(next)) {
			visited.put(next, visited.get(next) - 1.0);
		} else {
			visited.put(next, -1.0);
		}

		PacFace face = PacUtils.direction(pc.getLoc(), next);
		return face;
    }
	
	// Minimax function
	public void minimax(PacCell[][] grid, PossibleDir score, List<PossibleDir> scores, HashMap<Point, Double> visitedCopy, int treeDepth, boolean isMax) {
		// Stop once the depth is reached and save the score for the state
		if(treeDepth == depth){
			scores.add(score);
			return;
		}
		
		// Get pacman and the ghosts positions
		PacmanCell pacman = PacUtils.findPacman(grid);
		List<Point> ghosts = PacUtils.findGhosts(grid);
		
		// Does max if true, does min if false of the minimax function
		if(isMax == true) {
			// Loop through the 4 directions for pacman
			for(int i = 0; i < 4; i++) {
				// Get the location of pacman after the movement
				PacCell location = grid[pacman.getX() + xDir[i]][pacman.getY() + yDir[i]];
				Point movement = new Point(location.getX(), location.getY());
				
				// Create copy of these variables to prevent pass by reference
				HashMap<Point, Double> visitedCopy2 = copyHash(visitedCopy);
				PossibleDir temp = score.copy();
				PacCell[][] newGrid = grid.clone();
				
				// Check if the location is a wall or house cell (skip movement)
				if(location instanceof pacsim.WallCell == true || location instanceof pacsim.HouseCell) {
					continue;
				}
				
				// Check if the location is a ghost cell
				if(location instanceof pacsim.GhostCell == true) {
					// Add a negative if ghost, do not move to prevent overriding
					temp.add(-5000, movement);
				} else {
					// If not a ghost, evaluate the board state and move pacman
					newGrid = PacUtils.movePacman(pacman.getLoc(), movement, newGrid);
					temp.add(evaluation(newGrid, visitedCopy2), movement);
				}

				// Recursive call with new values and going from max to min
				minimax(newGrid, temp, scores, visitedCopy2, treeDepth, false);
			}
			
		} else {
			// Loop through the 4 directions for the first ghost
			for(int i = 0; i < 4; i++) {
				// Get the location of the first ghost after the movement
				PacCell ghost1 = grid[ghosts.get(0).x + xDir[i]][ghosts.get(0).y + yDir[i]];
				
				// Create copy to prevent pass by reference
				PacCell[][] newGrid = grid.clone();
				
				// Check if the location is a wall cell (skip movement)
				if(ghost1 instanceof pacsim.WallCell == true) {
					continue;
				}
				
				// Loop through the 4 directions for the second ghost
				for(int j = 0; j < 4; j++) {
					PacCell ghost2 = grid[ghosts.get(1).x + xDir[j]][ghosts.get(1).y + yDir[j]];
					PossibleDir temp = score.copy();
					
					// Check if the location is a wall cell (skipmovem)
					if(ghost2 instanceof pacsim.WallCell == true) {
						continue;
					}
					
					// Check if the location is a pac cell
					if(ghost1 instanceof pacsim.PacCell == true || ghost2 instanceof pacsim.PacCell == true) {
						// Add a negative if pacman, do not move to prevent overriding
						temp.add(-5000, null);
					} else {
						// If not pacman, evaluate the board state and move the ghosts
						newGrid = PacUtils.moveGhost(ghosts.get(0), ghost1.getLoc(), newGrid);
						newGrid = PacUtils.moveGhost(ghosts.get(1), ghost2.getLoc(), newGrid);
						temp.add(evaluation(newGrid, visitedCopy), null);
					}
					
					// Recursive call with new values and going from min to max, increase the depth by 1
					minimax(newGrid, temp, scores, visitedCopy, treeDepth + 1, true);
				}
			}	
		}
	}
	
	// Evaulation function for check the grid state
	public double evaluation(PacCell[][] grid, HashMap<Point,Double> visitedCopy) {
		// Get pacman and the ghosts locations
		PacmanCell pacman = PacUtils.findPacman(grid);
		List<Point> ghosts = PacUtils.findGhosts(grid);
		
		// Get the number of food, power, and the nearest food
		double food = PacUtils.numFood(grid);
		double power = PacUtils.numPower(grid);
		Point near = PacUtils.nearestGoody(pacman.getLoc(), grid);
		
		// Initialize the score
		double score = 0;
		
		// Loop through the ghosts
		for(int i = 0; i < ghosts.size(); i++) {
			// Get the ghost location
			PacCell ghost = grid[ghosts.get(i).x][ghosts.get(i).y];
			
			// Get the mode of the ghost (scatter, chase, fear)
			GhostCell mode = (GhostCell) ghost;

			// Check if fear
			if(mode.getMode().toString() != "FEAR") {
				// Get the distance from pacman and the ghost
				double distance = BFSPath.getPath(grid, pacman.getLoc(), ghost.getLoc()).size();
				score -= (1.0 / distance) * 4;
			}
		}
		
		// Check if there is any food leftover
		if(food != 0) {
			score += (1.0 / food);
		}
		
		// Check if there is any power leftover
		if(power != 0) {
			score += (1.0 / power);
		}
		
		// Check if the nearest food is null
		if(near != null) {
			score += (1.0 / BFSPath.getPath(grid, pacman.getLoc(), near).size());
		}

		// Update the visited table
		if(visitedCopy.containsKey(pacman.getLoc())) {
			score += visitedCopy.get(pacman.getLoc());
			visitedCopy.put(pacman.getLoc(), visitedCopy.get(pacman.getLoc()) - 1.0);
		} else {
			visitedCopy.put(pacman.getLoc(), -1.0);
		}

		// Check distance from intersections
		score -= (BFSPath.getPath(grid, pacman.getLoc(), findPoint(grid, pacman.getLoc())).size()) / 2;
		
		// Return the score
		return score;
	}

	// Initialize intersection
	public void populatePoints(){
		points[0] = new Point(4,3);
		points[1] = new Point(3,5);
		points[2] = new Point(4,7);
		points[3] = new Point(6,7);
		points[4] = new Point(6,3);
		points[5] = new Point(13,7);
		points[6] = new Point(13,3); 
		points[7] = new Point(16,5);
	}

	// Find the nearest intersection
	public Point findPoint(PacCell[][] grid, Point pc){
		double min = 10000;
		int index = 0;

		// Loop through intersections
		for(int i = 0; i < points.length; i++){
			if (BFSPath.getPath(grid, pc, points[i]).size() < min){
				min = BFSPath.getPath(grid, pc, points[i]).size();
				index = i;
			}
		}

		// return the nearest intersection
		return points[index];
	}

	// Copy the hash table to prevent pass by reference
	public HashMap<Point, Double> copyHash(HashMap<Point, Double> table){
		HashMap<Point, Double> temp = new HashMap<Point, Double>(table);
		temp.putAll(table);
		return temp;
	}
	
	// Possible direction class
	class PossibleDir {
		private Point point;
		private double score;
		
		// Constructor
		PossibleDir() {
			this.point = null;
			this.score = 0.0;
		}
		
		// Add to score
		public void add(double s, Point p) {
			// Check if point is null
			if(this.point == null) {
				this.point = p;
			}
			this.score += s;
		}
		
		// Copy the class to prevent pass by reference
		public PossibleDir copy() {
			PossibleDir copy = new PossibleDir();
			copy.score = this.score;
			
			// Check if point is null
			if(this.point == null) {
				copy.point = null;
			} else {
				copy.point = new Point(this.point.x, this.point.y);
			}
			
			// Return the clone
			return copy;
		}
		
		// Return the point
		public Point getPoint() {
			return point;
		}
		
		// Return the score
		public double getScore() {
			return score;
		}
	}
}
