
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
	
    private int depth;
	private int fear;
	HashMap<Point, Double> visited;
	
	// Up, Right, Down, Left
	private int[] xDir = {0, 1,  0, -1};
	private int[] yDir = {1, 0, -1,  0};
	  
	public PacSimMinimax( int depth, String fname, int te, int gran, int max ) {
		
		this.depth = depth;
		fear = 0;
		
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
		
		System.out.println("\nAdvesarial Search using Minimax by :");
		System.out.println("\n	Game board	: " + depth + "\n" );
		if( te > 0 ) {
			System.out.println(" Preliminary runs : " + te + "\n Granularity : " + gr + "\n Max move limit : " + ml + "\n\nPreliminary run results :\n");
		}
	}

	@Override
	public void init() {
		visited = new HashMap<>();
	}
	   
	@Override
	public PacFace action( Object state ) {

		PacCell[][] grid = (PacCell[][]) state;
		PacmanCell pc = PacUtils.findPacman(grid);
		
		List<PossibleDir> scores = new ArrayList<>();
		HashMap<Point, Double> visitedCopy = copyHash(visited);
		
		minimax(grid, new PossibleDir(), scores, visitedCopy, 0, true);
		
		scores.sort(Comparator.comparing(PossibleDir::getScore));
		
		Point next = scores.get(scores.size() - 1).getPoint();
		/*
		for(int i = 0; i < scores.size(); i++) {
			System.out.println(scores.get(i).getPoint() + " " + scores.get(i).getScore());
		}
		*/
		
		if(visited.containsKey(next)) {
			visited.put(next, visited.get(next) - 1.0);
		} else {
			visited.put(next, -1.0);
		}

		//System.out.println();
		
		PacFace face = PacUtils.direction(pc.getLoc(), next);
		return face;
    }
	
	public void minimax(PacCell[][] grid, PossibleDir score, List<PossibleDir> scores, HashMap<Point, Double> visitedCopy, int treeDepth, boolean isMax) {
		if(treeDepth == depth){
			scores.add(score);
			return;
		}
		
		PacmanCell pacman = PacUtils.findPacman(grid);
		List<Point> ghosts = PacUtils.findGhosts(grid);
		
		if(isMax == true) {
			for(int i = 0; i < 4; i++) {
				PacCell location = grid[pacman.getX() + xDir[i]][pacman.getY() + yDir[i]];
				Point movement = new Point(location.getX(), location.getY());
				HashMap<Point, Double> visitedCopy2 = copyHash(visitedCopy);
				PossibleDir temp = score.copy();
				PacCell[][] newGrid = grid.clone();
				
				// Skip if pacman cannot move that direction (temporary)
				if(location instanceof pacsim.WallCell == true || location instanceof pacsim.HouseCell) {
					continue;
				}
				
				if(location instanceof pacsim.GhostCell == true) {
					temp.add(-5000, movement);
				} else {
					newGrid = PacUtils.movePacman(pacman.getLoc(), movement, newGrid);
					temp.add(evaluation(newGrid, visitedCopy2), movement);
				}

				minimax(newGrid, temp, scores, visitedCopy2, treeDepth, false);
			}
			
		} else {
			for(int i = 0; i < 4; i++) {
				PacCell ghost1 = grid[ghosts.get(0).x + xDir[i]][ghosts.get(0).y + yDir[i]];
				PacCell[][] newGrid = grid.clone();
				
				// Skip if ghost cannot move that direction (temporary)
				if(ghost1 instanceof pacsim.WallCell == true) {
					continue;
				}
				
				for(int j = 0; j < 4; j++) {
					PacCell ghost2 = grid[ghosts.get(1).x + xDir[j]][ghosts.get(1).y + yDir[j]];
					PossibleDir temp = score.copy();
					
					// Skip if ghost cannot move that direction (temporary)
					if(ghost2 instanceof pacsim.WallCell == true) {
						continue;
					}
					
					if(ghost1 instanceof pacsim.PacCell == true || ghost2 instanceof pacsim.PacCell == true) {
						temp.add(-5000, null);
					} else {
						newGrid = PacUtils.moveGhost(ghosts.get(0), ghost1.getLoc(), newGrid);
						newGrid = PacUtils.moveGhost(ghosts.get(1), ghost2.getLoc(), newGrid);
						temp.add(evaluation(newGrid, visitedCopy), null);
					}
					
					minimax(newGrid, temp, scores, visitedCopy, treeDepth + 1, true);
				}
			}	
		}
	}
	
	public double evaluation(PacCell[][] grid, HashMap<Point,Double> visitedCopy) {
		PacmanCell pacman = PacUtils.findPacman(grid);
		List<Point> ghosts = PacUtils.findGhosts(grid);
		double food = PacUtils.numFood(grid);
		double power = PacUtils.numPower(grid);
		Point near = PacUtils.nearestGoody(pacman.getLoc(), grid);
		
		double score = 0;
		
		for(int i = 0; i < ghosts.size(); i++) {
			PacCell ghost = grid[ghosts.get(i).x][ghosts.get(i).y];
			
			GhostCell mode = (GhostCell) ghost;
			
			if(mode.getMode().toString() != "FEAR") {
				double distance = BFSPath.getPath(grid, pacman.getLoc(), ghost.getLoc()).size();
				score -= (1.0 / distance) * 2;
			}
		}
		/*
		for(int i = 0; i < 4; i++) {
			PacCell location = grid[pacman.getX() + xDir[i]][pacman.getY() + yDir[i]];
			
			if(location instanceof pacsim.WallCell == true || location instanceof pacsim.HouseCell) {
				score -= 1;
			}
		}*/
		
		//score += PacUtils.manhattanDistance(ghosts.get(0), ghosts.get(1));
		
		if(food != 0) {
			score += (1.0 / food);
		}
		
		if(power != 0) {
			score += (1.0 / power);
		}
		
		if(near != null) {
			score += (1.0 / BFSPath.getPath(grid, pacman.getLoc(), near).size());
		}
		//score += (1.0 / PacUtils.manhattanDistance(pacman.getLoc(), near)) * 5;

		if(visitedCopy.containsKey(pacman.getLoc())) {
			score += visitedCopy.get(pacman.getLoc());
			visitedCopy.put(pacman.getLoc(), visitedCopy.get(pacman.getLoc()) - 1.0);
		} else {
			visitedCopy.put(pacman.getLoc(), -1.0);
		}
		
		return score;
	}

	public HashMap<Point, Double> copyHash(HashMap<Point, Double> table){
		HashMap<Point, Double> temp = new HashMap<Point, Double>(table);
		temp.putAll(table);
		return temp;
	}
	
	class PossibleDir {
		private Point point;
		private double score;
		
		PossibleDir() {
			this.point = null;
			this.score = 0.0;
		}
		
		public void add(double s, Point p) {
			if(this.point == null) {
				this.point = p;
			}
			this.score += s;
		}
		
		public PossibleDir copy() {
			PossibleDir copy = new PossibleDir();
			copy.score = this.score;
			if(this.point == null) {
				copy.point = null;
			} else {
				copy.point = new Point(this.point.x, this.point.y);
			}
			return copy;
		}
		
		public Point getPoint() {
			return point;
		}
		
		public double getScore() {
			return score;
		}
	}
}
