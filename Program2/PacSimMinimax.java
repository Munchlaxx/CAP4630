
import java.awt.Point;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import pacsim.BFSPath;
import pacsim.PacAction;
import pacsim.PacCell;
import pacsim.PacFace;
import pacsim.PacSim;
import pacsim.PacUtils;
import pacsim.PacmanCell;

public class PacSimMinimax implements PacAction {
	
	//Optional: class and instance variables
    private int depth;
	private int[] xDir = {1, -1, 0,  0};
	private int[] yDir = {0,  0, 1, -1};
	HashMap<Point,Integer> visited;
	  
	public PacSimMinimax( int depth, String fname, int te, int gran, int max ) {
		
		//Optional: initialize some variables
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
		
		PossibleDir score = new PossibleDir();
		//HashMap<Point,Integer> visitedCopy = copyHash(visited);
		minimax(grid, score, 0, true);
		
		Point next = score.getPoint();
		/*
		if(visited.containsKey(next)) {
			visited.put(next, visited.get(next) - 10);
		} else {
			visited.put(next, -10);
		}
		*/
		
		PacFace face = PacUtils.direction(pc.getLoc(), next);
		return face;
    }
	
	public void minimax(PacCell[][] grid, PossibleDir score, int treeDepth, boolean isMax) {
		if(treeDepth == depth){
			return;
		}
		
		PacmanCell pacman = PacUtils.findPacman(grid);
		List<Point> ghosts = PacUtils.findGhosts(grid);
		PacCell[][] newGrid = grid.clone();
		
		List<PossibleDir> scores = new ArrayList<>();
		
		if(isMax == true) {
			for(int i = 0; i < 4; i++) {
				PacCell location = grid[pacman.getX() + xDir[i]][pacman.getY() + yDir[i]];
				Point movement = new Point(location.getX(), location.getY());
				PossibleDir temp = new PossibleDir();
				
				// Skip if pacman cannot move that direction (temporary)
				if(location instanceof pacsim.WallCell == true || location instanceof pacsim.HouseCell) {
					continue;
				}
				
				newGrid = PacUtils.movePacman(pacman.getLoc(), movement, grid);
				
				if(location instanceof pacsim.GhostCell == true) {
					temp.add(-1000, movement);
				} else {
					double val = evaluation(newGrid);
					temp.add(val, movement);
				}

				scores.add(temp);
			}

			scores.sort(Comparator.comparing(PossibleDir::getScore));

			PossibleDir optimalScore = scores.get(scores.size() - 1);
			score.add(optimalScore.getScore(), optimalScore.getPoint());
			
			PacCell[][] optimalGrid = grid.clone();
			
			if(optimalScore.getScore() != -1000){
				optimalGrid = PacUtils.movePacman(pacman.getLoc(), optimalScore.getPoint(), optimalGrid);
			}
			
			minimax(optimalGrid, score, treeDepth, false);	
			
		} else {
			for(int i = 0; i < 4; i++) {
				PacCell ghost1 = grid[ghosts.get(0).x + xDir[i]][ghosts.get(0).y + yDir[i]];
				
				// Skip if ghost cannot move that direction (temporary)
				if(ghost1 instanceof pacsim.WallCell == true) {
					continue;
				}
				
				for(int j = 0; j < 4; j++) {
					PacCell ghost2 = grid[ghosts.get(1).x + xDir[j]][ghosts.get(1).y + yDir[j]];
					PossibleDir temp = new PossibleDir();
					
					// Skip if ghost cannot move that direction (temporary)
					if(ghost2 instanceof pacsim.WallCell == true) {
						continue;
					}
					
					if(ghost1 instanceof pacsim.PacCell == true || ghost2 instanceof pacsim.PacCell == true) {
						temp.add(-1000, null);
					} else {
						double val = evaluation(newGrid);
						temp.add(val, null);
					}
					
					scores.add(temp);
				}
			}
			
			scores.sort(Comparator.comparing(PossibleDir::getScore));

			PossibleDir optimalScore = scores.get(0);
			score.add(optimalScore.getScore(), optimalScore.getPoint());
			
			PacCell[][] optimalGrid = grid.clone();
			
			if(optimalScore.getScore() != -1000){
				optimalGrid = PacUtils.movePacman(pacman.getLoc(), optimalScore.getPoint(), optimalGrid);
			}
			System.out.println();
			minimax(optimalGrid, score, treeDepth + 1, true);	
		}
	}
	
	public double evaluation(PacCell[][] grid) {
		PacmanCell pacman = PacUtils.findPacman(grid);
		List<Point> ghosts = PacUtils.findGhosts(grid);
		double food = PacUtils.numFood(grid);
		double power = PacUtils.numPower(grid);
		Point near = PacUtils.nearestGoody(pacman.getLoc(), grid);
		
		double score = 0;
		
		for(int i = 0; i < ghosts.size(); i++) {
			PacCell ghost = grid[ghosts.get(i).x][ghosts.get(i).y];
			double distance = BFSPath.getPath(grid, pacman.getLoc(), ghost.getLoc()).size();
			score -= 1.0 / distance;
		}
		
		//score += PacUtils.manhattanDistance(ghosts.get(0), ghosts.get(1));
		
		score += 1.0 / food;
		score += 1.0 / power;
		
		score += 1.0 / PacUtils.manhattanDistance(pacman.getLoc(), near);
		
		System.out.println(score);
		return score;
	}

	public HashMap<Point,Integer> copyHash(HashMap<Point,Integer> table){
			HashMap<Point,Integer> temp = new HashMap<Point,Integer>(table);
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
