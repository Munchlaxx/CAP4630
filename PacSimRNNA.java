
/*
* University of Central Florida
* CAP4630 - Spring 2019
* Authors: <John Mirschel, Wen Lam>
*/


import java.awt.Point;
import java.util.Comparator;
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
	private Possible fastestPath;
	
	public PacSimRNNA(String fname) {
		PacSim sim = new PacSim(fname);
		sim.init(this);
	}
	
	public static void main(String[] args) {
		System.out.println("\nTSP using Repetitive Nearset Neighbor Algorithm by John Mirschel and Wen Lam:");
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
		List<Possible> populationOne = new ArrayList<>();
		
		if(pc == null) {
			return null;
		}
		
		// Check if the food array is null, if null, occur once start RNNA
		if(foodArray == null) {
			long start = System.currentTimeMillis();
			foodArray = PacUtils.findFood(grid);
			locArray = PacUtils.findFood(grid);
			locArray.add(0, pc.getLoc());
			costTable = new int[locArray.size()][locArray.size()];
			
			// Fill the cost table with values of distance between all the points, including pacman
			System.out.println("Cost table: \n");
			for(int i = 0; i < locArray.size(); i++) {
				for(int j = 0; j < locArray.size(); j++) {
					costTable[i][j] = BFSPath.getPath(grid, locArray.get(i), locArray.get(j)).size();
					System.out.printf(" %3d", costTable[i][j]);
				}

				System.out.println();
			}

			// Printing and sorting the food array.
			System.out.println("\nFood Array: \n");
			locArray.remove(0);
			locArray.sort(Comparator.comparing(Point::getX).thenComparing(Point::getY));
			for(int i = 0; i < locArray.size(); i++){
				System.out.println(i + ": (" + (int)locArray.get(i).getX() + ", " + (int)locArray.get(i).getY() + ")");
				populationOne.add(new Possible(costTable[i + 1][0], foodArray.get(i), costTable[i + 1][0]));
			}

			populationOne.sort(Comparator.comparing(Possible::getDistance));
			printPopulation(populationOne);


			
			// Loop through all starting food pellet from pacman
			for(int i = 1; i <= foodArray.size(); i++) {
				Possible path = new Possible();
				List<Integer> indexes = new ArrayList<>();
				
				// Create the initial list of food pellets
				path.add(costTable[i][0], foodArray.get(i - 1), costTable[i][0]);
				for(int j = 1; j <= foodArray.size(); j++){
					indexes.add(j);
				}
				
				// Call to start searching for paths and branches
				rec(path, finalPaths, indexes, i);
			}
			
			// Keep track of the minimum distance and path of all starting food pellet
			int minPath = Integer.MAX_VALUE;
			fastestPath = null;
			
			// Loop for the path with the minimun distance
			for(int i = 0; i < finalPaths.size(); i++){
				if(finalPaths.get(i).getDistance() < minPath){
					fastestPath = finalPaths.get(i);
					minPath = finalPaths.get(i).getDistance();
				}
			}

			finalPaths.sort(Comparator.comparing(Possible::getDistance));			
			printPopulation(finalPaths);

			fastestPath = finalPaths.get(0);
			
			long end = System.currentTimeMillis();
			System.out.println("\nTime to generate plan: " + (end - start) + " msec");
			System.out.println("\nSolution Moves: \n");
		}
			if( path.isEmpty() ) {
	         Point tgt = fastestPath.getPath().remove(0);
	         path = BFSPath.getPath(grid, pc.getLoc(), tgt);

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

			}
		}
		
		// Save the paths and create possible branches if same distance
		for(int i = 0; i < indexes.size(); i++){
			if(costTable[current][indexes.get(i)] == min) {
				
				// Copy the path because of passed by reference
				Possible pathCopy = null;
				pathCopy = path.copy();
				pathCopy.add(min, foodArray.get(indexes.get(i) - 1), costTable[current][indexes.get(i)]);
				
				// Copy the indexes because of passed by reference
				List<Integer> indexesCopy = new ArrayList<>();
				indexesCopy.addAll(indexes);
				
				// Continue the path or create a new branch
				rec(pathCopy, finalize, indexesCopy, indexes.get(i));
			}
		}
	}

	public void printPopulation(List<Possible> path){
		System.out.println("\nPopulation at step " + path.get(0).getSize() + " :");
		for(int i = 0; i < path.size(); i++) {
            System.out.print("   " + i + " : cost=" + path.get(i).getDistance() + " : ");

            for(int j = 0; j < path.get(i).getSize(); j++) {
                System.out.print("[(" + (int) path.get(i).getPath().get(j).getX() + "," + (int) path.get(i).getPath().get(j).getY()  + ")," + path.get(i).getPellet().get(j) + "]");
            }

            System.out.println();
        }
	}

	// Class to save the path points and the distance
	class Possible{
		private int distance;
		private List<Integer> pelletDistance;
		private List<Point> path;
		
		// Constructor
		Possible() {
			this.distance = 0;
			this.path = new ArrayList<>();
			this.pelletDistance = new ArrayList<>();
		}

		// Constructor
		Possible(int dist, Point init, int pellet) {
			this.distance = dist;
			this.path = new ArrayList<>();
			this.path.add(init);
			this.pelletDistance = new ArrayList<>();
			this.pelletDistance.add(pellet);
		}
		
		// Add the point and path distance
		public void add(int dist, Point next, int pellet) {
			this.distance += dist;
			this.path.add(next);
			this.pelletDistance.add(pellet);
		}
		
		// Used to copy a path if there is a branching
		public Possible copy() {
			Possible copy = new Possible();
			copy.distance = this.distance;
			
			for(int i = 0; i < this.path.size(); i++){
				copy.path.add(this.path.get(i));
				copy.pelletDistance.add(this.pelletDistance.get(i));
			}
			return copy;
		}
		
		// Return distance
		public int getDistance() {
			return this.distance;
		}

		// Return pellet
		public List<Integer> getPellet() {
			return this.pelletDistance;
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