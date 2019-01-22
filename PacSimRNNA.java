
import java.awt.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;

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
		
		if(pc == null) {
			return null;
		}
		
		if(foodArray == null) {
			foodArray = PacUtils.findFood(grid);
			locArray = PacUtils.findFood(grid);
			locArray.add(0, pc.getLoc());
			costTable = new int[locArray.size()][locArray.size()];
			
			for(int i = 0; i < locArray.size(); i++) {
				for(int j = 0; j < locArray.size(); j++) {
					costTable[i][j] = BFSPath.getPath(grid, locArray.get(i), locArray.get(j)).size();
				}
			}
			
			Queue<Possible> branch = new LinkedList<>();
			List<Possible> finish = new ArrayList<>();
			
			//Create function later
			for(int i = 0; i < foodArray.size(); i++) {
				// add one from here on out
				List<Integer> temp = new ArrayList<>();
				for(int j = 0; j < foodArray.size(); i++){
					temp.add(j);
				}
				
				int min = Math.MAX_VALUE;
				
				while(!temp.isEmpty()) {
					for(int j = 0; j < temp.size(); j++){
						
					}
				}
			}
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
}

class Possible {
	private int distance;
	private List<Point> path;
	
	Possible() {
		this.distance = 0;
		path = new ArrayList<>();
	}
	
	public void add(int dist, Point next) {
		this.distance += dist;
		this.path.add(next);
	}
	
	public Possible copy() {
		Possible copy = new possible();
		copy.distance = this.distance;
		copy.path = this.path;
		return copy;
	}
	
	public int getDistance() {
		return this.distance;
	}
	
	public List<Point> getPath() {
		return this.path;
	}
}

























