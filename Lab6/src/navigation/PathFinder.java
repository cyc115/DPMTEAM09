package navigation;

import odometry.Odometer;
import robotcore.Configuration;
import robotcore.Coordinate;
import lejos.robotics.pathfinding.AstarSearchAlgorithm;
import lejos.robotics.pathfinding.Node;
import lejos.robotics.pathfinding.Path;

/**
 * This finds a path based on the Nodes determined in the map
 * 
 * TODO: Internal implementation of A* uses Euclidean distance, 
 * might be better to override and use Manhattan distance to
 * get the least amount of turns possible
 *
 *	@author Peter Henderson
 */
public class PathFinder {
	
	private AstarSearchAlgorithm search;
	private Map map;
	private Configuration config;
	private static PathFinder instance;
	
	/**
	 * Basic Constructor, instantiates variables
	 */
	private PathFinder(){
		this.search = new AstarSearchAlgorithm();
		this.map = Map.getInstance();
	}
	
	/**
	 * Get singleton instance
	 * @return
	 */
	public static PathFinder getInstance(){
		if(instance == null) instance = new PathFinder();
		
		return instance;
	}
	
	/**
	 * Uses lejos AstarSearch to find optimal path to the flagZone
	 * @return
	 */
	public Path getOptimalPathToFlagZone(){
		config = Configuration.getInstance();

		Coordinate[] coord = config.getFlagZone();
		
		Node flagNode = map.getClosestNode(coord[0].getX()+15, coord[0].getY()-15);
		
		return getPathBetweenNodes(getCurrentNode(), flagNode);
	}
	
	/**
	 * This gets teh current node the robot is at
	 * @return
	 */
	public Node getCurrentNode(){
		return map.getClosestNode(Odometer.getInstance().getX(), Odometer.getInstance().getY());
	}
	
	/**
	 * Uses lejos AstarSearch to find the optimal path to the flag drop zone
	 * @return
	 */
	public Path getOptimalPathToDropZone(){
		config = Configuration.getInstance();

		Coordinate coord = config.getDropZone();
		
		//Assumes bottom left of the tile, so we want to go the center of the tile
		Node dropNode = map.getClosestNode(coord.getX()+15, coord.getY()+15);
		
		return getPathBetweenNodes(getCurrentNode(), dropNode);
		
	}
	
	/**
	 * Gets a path between two given nodes
	 * @param start
	 * @param end
	 * @return
	 */
	public Path getPathBetweenNodes(Node start, Node end){
		Path path = search.findPath(start, end);
		if(path == null || path.isEmpty()){
			map.populateMap(); //clear the map and try again
			path = search.findPath(map.getClosestNode(start.x, start.y), map.getClosestNode(end.x, end.y));
		}
		
		return path;
	}
}
