
import java.awt.Point;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
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
	public void init() {}
	   
	@Override
	public PacFace action( Object state ) {

		PacCell[][] grid = (PacCell[][]) state;
		PacmanCell pc = PacUtils.findPacman(grid);
		
		List<PossibleDir> scores = new ArrayList<>();
		minimax(grid, scores, 0, true, 0);

		scores.sort(Comparator.comparing(PossibleDir::getScore));
		
		for(int i = 0; i < scores.size(); i++) {
			System.out.println(scores.get(i).getPoint() + ":" + scores.get(i).getScore());
		}
		System.out.println();
		
		Point next = scores.get(scores.size() - 1).getPoint();
		PacFace face = PacUtils.direction(pc.getLoc(), next);
		return face;
    }
	
	public void minimax(PacCell[][] grid, List<PossibleDir> scores, int treeDepth, boolean isMax, int current) {
		if(treeDepth == depth){
			return;
		}

		PacmanCell pacman = PacUtils.findPacman(grid);
		List<Point> ghosts = PacUtils.findGhosts(grid);
		PacCell[][] newGrid;// = grid.clone();
		
		if(isMax == true) {
			for(int i = 0; i < 4; i++) {
				PacCell location = grid[pacman.getX() + xDir[i]][pacman.getY() + yDir[i]];
				Point movement = new Point(location.getX(), location.getY());

				// Skip if pacman cannot move that direction (temporary)
				if(location instanceof pacsim.WallCell == true || location instanceof pacsim.GhostCell == true) {
					continue;
				}

				if(treeDepth == 0) {
					scores.add(new PossibleDir());
					current++;
				}
				
				// Add score based off value (temporary)
				if(location instanceof pacsim.FoodCell == true) {
					scores.get(current - 1).add(5, movement);
				} else if(location instanceof pacsim.PowerCell == true) {
					scores.get(current - 1).add(10, movement);
				} else {
					scores.get(current - 1).add(0, movement);
				}

				newGrid = PacUtils.movePacman(pacman.getLoc(), movement, grid);
				
				minimax(newGrid, scores, treeDepth, false, current);
			}
		} else {
			for(int i = 0; i < 4; i++) {
				PacCell ghost1 = grid[ghosts.get(0).x + xDir[i]][ghosts.get(0).y + yDir[i]];
				
				// Skip if ghost cannot move that direction (temporary)
				if(ghost1 instanceof pacsim.WallCell == true) {
					continue;
				}
				
				newGrid = PacUtils.moveGhost(ghosts.get(0), ghost1.getLoc(), grid);
				
				int score1 = BFSPath.getPath(newGrid, pacman.getLoc(), ghost1.getLoc()).size();
				scores.get(current - 1).add(-(1000 - score1), null);
		
				for(int j = 0; j < 4; j++) {
					PacCell ghost2 = grid[ghosts.get(1).x + xDir[j]][ghosts.get(1).y + yDir[j]];
					
					// Skip if ghost cannot move that direction (temporary)
					if(ghost2 instanceof pacsim.WallCell == true) {
						continue;
					}
					
					int score2 = BFSPath.getPath(newGrid, pacman.getLoc(), ghost2.getLoc()).size();
					scores.get(current - 1).add(-(1000 - score2), null);
			
					newGrid = PacUtils.moveGhost(ghosts.get(1), ghost2.getLoc(), newGrid);
					
					minimax(newGrid, scores, treeDepth + 1, true, current);
				}
			}
		}
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
		
		public Point getPoint() {
			return point;
		}
		
		public int getScore() {
			return score;
		}
	}
}
