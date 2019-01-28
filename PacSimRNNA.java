import java.awt.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

import pacsim.BFSPath;
import pacsim.PacAction;
import pacsim.PacCell;
import pacsim.PacFace;
import pacsim.PacSim;
import pacsim.PacUtils;
import pacsim.PacmanCell;

public class PacSimRNNA implements PacAction {
	private List<Point> path;
	private int simTime;
	private List<Point> locArray;
	private List<Point> foodArray;
	private int[][] costTable;
	
	public PacSimRNNA(String fname) {
		PacSim sim = new PacSim(fname);
		sim.init(this);
	}
	
	public static void main(String[] args) {
		System.out.println("\nTSP using RNNA agent by :");
		System.out.println("\nMaze: " + args[0] + "\n");
		new PacSimRNNA(args[0]);
	}
	
	@Override
	public void init() {
		simTime = 0;
		path = new ArrayList();
		locArray = null;
	}
	
	@Override
	public PacFace action(Object state) {
		PacCell[][] grid = (PacCell[][]) state;
		PacmanCell pc = PacUtils.findPacman(grid);
		List<Possible> finalPaths = new ArrayList<>();
		
		if(pc == null) {
			return null;
		}
		
		// Check if the food array is null, if null, occur once start RNNA
		if(foodArray == null) {
			foodArray = PacUtils.findFood(grid);
			locArray = PacUtils.findFood(grid);
			locArray.add(0, pc.getLoc());
			costTable = new int[locArray.size()][locArray.size()];
			
			// Fill the cost table with values of distance between all the points, including pacman
			for(int i = 0; i < locArray.size(); i++) {
				for(int j = 0; j < locArray.size(); j++) {
					costTable[i][j] = BFSPath.getPath(grid, locArray.get(i), locArray.get(j)).size();
				}
			}
			
			// Loop through all starting food pellet from pacman
			for(int i = 1; i <= foodArray.size(); i++) {
				Possible path = new Possible();
				List<Possible> finalize = new ArrayList<>();
				List<Integer> indexes = new ArrayList<>();
				
				// Create the initial list of food pellets
				path.add(costTable[i][0], foodArray.get(i - 1));
				for(int j = 1; j <= foodArray.size(); j++){
					indexes.add(j);
				}
				
				// Call to start searching for paths and branches
				rec(path, finalize, indexes, i);
				
				// Keep track of the minimum distance and the path itself
				int min = Integer.MAX_VALUE;
				Possible savedPath = null;
				
				// Loop for the minimum distance and path
				for(int j = 0; j < finalize.size(); j++){
					if(finalize.get(j).getDistance() < min){
						savedPath = finalize.get(j);
						min = finalize.get(j).getDistance();
					}
				}
				
				// Add the path with the shortest distance for that initial start
				finalPaths.add(savedPath);
			}
			
			// Keep track of the minimum distance and path of all starting food pellet
			int minPath = Integer.MAX_VALUE;
			Possible fastestPath = null;
			
			// Loop for the path with the minimun distance
			for(int i = 0; i < finalPaths.size(); i++){
				if(finalPaths.get(i).getDistance() < minPath){
					fastestPath = finalPaths.get(i);
					minPath = finalPaths.get(i).getDistance();
				}
			}
			
			System.out.println(fastestPath.getDistance());
		}
		
		if( path.isEmpty() ) {
         Point tgt = PacUtils.nearestFood( pc.getLoc(), grid);
         path = BFSPath.getPath(grid, pc.getLoc(), tgt);

         System.out.println("Pac-Man currently at: [ " + pc.getLoc().x
               + ", " + pc.getLoc().y + " ]");
         System.out.println("Setting new target  : [ " + tgt.x
               + ", " + tgt.y + " ]");
		}
      
      // take the next step on the current path
      
      Point next = path.remove( 0 );
      PacFace face = PacUtils.direction( pc.getLoc(), next );
      System.out.printf( "%5d : From [ %2d, %2d ] go %s%n", 
            ++simTime, pc.getLoc().x, pc.getLoc().y, face );
      return face;
	}
	
	// Recursion to determine the paths and branches
	public void rec(Possible path, List<Possible> finalize, List<Integer> indexes, int current) {
		// Value to keep track of minimum distance and index of it
		int min = Integer.MAX_VALUE;
		int index = 0;
		
		// Remove the current food pellet
		for(Integer value: indexes) {
			if(value == current) {
				indexes.remove(value);
				break;
			}
		}
		
		// End the recursion if all food pellets are gone
		if(indexes.size() == 0) {
			finalize.add(path);
			return;
		}
		
		// Find the minimum distance
		for(int i = 0; i < indexes.size(); i++) {
			if(costTable[current][indexes.get(i)] < min) {
				min = costTable[current][indexes.get(i)];
				index = indexes.get(i);
			}
		}
		
		// Save the paths and create possible branches if same distance
		for(int i = 0; i < indexes.size(); i++){
			if(costTable[current][indexes.get(i)] == min) {
				
				// Copy the path because of passed by reference
				Possible pathCopy = path.copy();
				pathCopy.add(min, foodArray.get(index - 1));
				
				// Copy the indexes because of passed by reference
				List<Integer> indexesCopy = new ArrayList<>();
				indexesCopy.addAll(indexes);
				
				// Continue the path or create a new branch
				rec(pathCopy, finalize, indexesCopy, index);
			}
		}
	}

	// Class to save the path points and the distance
	class Possible {
		private int distance;
		private List<Point> path;
		
		// Constructor
		Possible() {
			this.distance = 0;
			path = new ArrayList<>();
		}
		
		// Add the point and path distance
		public void add(int dist, Point next) {
			this.distance += dist;
			this.path.add(next);
		}
		
		// Used to copy a path if there is a branching
		public Possible copy() {
			Possible copy = new Possible();
			copy.distance = this.distance;
			copy.path = this.path;
			return copy;
		}
		
		// Return distance
		public int getDistance() {
			return this.distance;
		}
		
		// Return size of the path
		public int getSize() {
			return this.path.size();
		}
		
		// Return the path
		public List<Point> getPath() {
			return this.path;
		}
	}
}