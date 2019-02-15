
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
		
		List<PossibleDir> scores = new ArrayList<>();
		PossibleDir score = new PossibleDir();
		HashMap<Point,Integer> visitedCopy = copyHash(visited);
		minimax(grid, score, scores, visitedCopy, 0, true);
		scores.sort(Comparator.comparing(PossibleDir::getScore));
		
		Point next = scores.get(scores.size() - 1).getPoint();
		if(visited.containsKey(next)) {
			visited.put(next, visited.get(next) - 10);
		} else {
			visited.put(next, -10);
		}
		
		PacFace face = PacUtils.direction(pc.getLoc(), next);
		return face;
    }
	
	public void minimax(PacCell[][] grid, PossibleDir score, List<PossibleDir> scores, HashMap<Point,Integer> visitedCopy, int treeDepth, boolean isMax) {
		if(treeDepth == depth){
			scores.add(score);
			return;
		}
		
		PacmanCell pacman = PacUtils.findPacman(grid);
		List<Point> ghosts = PacUtils.findGhosts(grid);
		PacCell[][] newGrid = grid.clone();
		
		if(isMax == true) {
			for(int i = 0; i < 4; i++) {
				PacCell location = grid[pacman.getX() + xDir[i]][pacman.getY() + yDir[i]];
				Point movement = new Point(location.getX(), location.getY());
				
				// Skip if pacman cannot move that direction (temporary)
				if(location instanceof pacsim.WallCell == true || location instanceof pacsim.HouseCell) {
					continue;
				}
				
				PossibleDir scoreCopy = null;
				scoreCopy = score.copy();
				
				if(location instanceof pacsim.GhostCell == true) {
					scoreCopy.add(-100000, movement);
					minimax(grid, scoreCopy, scores, visitedCopy, treeDepth, false);
					continue;
				}
				
				// Add score based off value (temporary)
				if(location instanceof pacsim.FoodCell == true) {
					scoreCopy.add(55, movement);
				} else if(location instanceof pacsim.PowerCell == true) {
					scoreCopy.add(77, movement);
				} else {
					scoreCopy.add(22, movement);
				}
				
				if(visitedCopy.containsKey(movement)) {
					scoreCopy.add(visitedCopy.get(movement), null);
					visitedCopy.put(movement, visitedCopy.get(movement) - 10);
				} else {
					visitedCopy.put(movement, -10);
				}
				
				newGrid = PacUtils.movePacman(pacman.getLoc(), movement, grid);
				HashMap<Point,Integer> visitedCopy2 = copyHash(visitedCopy);
				
				minimax(newGrid, scoreCopy, scores, visitedCopy2, treeDepth, false);
			}
		} else {
			for(int i = 0; i < 4; i++) {
				PacCell ghost1 = grid[ghosts.get(0).x + xDir[i]][ghosts.get(0).y + yDir[i]];
				
				// Skip if ghost cannot move that direction (temporary)
				if(ghost1 instanceof pacsim.WallCell == true) {
					continue;
				}
				
				PossibleDir scoreCopy1 = null;
				scoreCopy1 = score.copy();
				
				if(ghost1 instanceof pacsim.PacmanCell == true) {
					scoreCopy1.add(-100000, null);
				} else {
					newGrid = PacUtils.moveGhost(ghosts.get(0), ghost1.getLoc(), grid);

					int score1 = BFSPath.getPath(newGrid, pacman.getLoc(), ghost1.getLoc()).size();
					scoreCopy1.add(-(1000 - score1), null);
				}
				
				for(int j = 0; j < 4; j++) {
					PacCell ghost2 = grid[ghosts.get(1).x + xDir[j]][ghosts.get(1).y + yDir[j]];
					
					// Skip if ghost cannot move that direction (temporary)
					if(ghost2 instanceof pacsim.WallCell == true) {
						continue;
					}
					
					PossibleDir scoreCopy2 = null;
					scoreCopy2 = scoreCopy1.copy();
					
					if(ghost2 instanceof pacsim.PacmanCell == true) {
						scoreCopy2.add(-1000, null);
					} else {
						newGrid = PacUtils.moveGhost(ghosts.get(1), ghost2.getLoc(), newGrid);

						int score2 = BFSPath.getPath(newGrid, pacman.getLoc(), ghost2.getLoc()).size();
						scoreCopy2.add(-(1000 - score2), null);
					}
				
					minimax(newGrid, scoreCopy2, scores, visitedCopy, treeDepth + 1, true);
				}
			}
		}
	}
	
	public HashMap<Point,Integer> copyHash(HashMap<Point,Integer> table){
			HashMap<Point,Integer> temp = new HashMap<Point,Integer>(table);
			temp.putAll(table);
			return temp;
		}
	
	class PossibleDir {
		private Point point;
		private int score;
		
		PossibleDir() {
			this.point = null;
			this.score = 0;
		}
		
		public void add(int s, Point p) {
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
		
		public int getScore() {
			return score;
		}
	}
}
