package integration;

import java.util.ArrayList;
import java.util.Stack;

import communication.RemoteConnection;
import capture.CaptureMechanism;
import movement.Driver;
import navigation.Map;
import navigation.PathTraveller;
import lejos.nxt.Sound;
import lejos.robotics.navigation.Waypoint;
import localization.LocalizationF;
import objectdetection.ObjectDetector;
import objectdetection.ObstacleDetector;
import odometry.Odometer;
import odometry.OdometerCorrection;
import robotcore.Configuration;
import robotcore.Coordinate;
import robotcore.LCDWriter;
import search.ObjRec;
import search.Searcher;
import search.ObjRec.blockColor;
import sensors.LineReader;
import sensors.UltrasonicPoller;

/**
 * Basic test to scan a tile in front of the robot
 * Place obstacle at different points in the tile and see if the scanner picks up
 * the obstacle correctly
 * 
 * NOTE: This is without odometry correction and pretty slow.
 * 
 * @author Peter Henderson
 *
 */
public class NavTestVII {
	
	private static boolean followPath(){
		PathTraveller t = PathTraveller.getInstance();
		Driver driver = Driver.getInstance();
		ObstacleDetector detector = ObstacleDetector.getInstance();
		Map map = Map.getInstance();
		Configuration conf = Configuration.getInstance();
		
		while(!t.pathIsEmpty()){
			Waypoint next = t.popNextWaypoint();
			//Turn to the next tile
			driver.turnTo(Coordinate.calculateRotationAngle(
												Odometer.getInstance().getCurrentCoordinate(), 
												new Coordinate(next)));
			
			
			//scan the next area
			if(detector.scanTileWithSideSensors()){
				//block next tile
				map.blockNodeAt(next.x, next.y);
				return false;
			}
			
			Coordinate n = new Coordinate(next);
			switch(Odometer.getInstance().getDirection()){
			case NORTH:
				n.setY(n.getY()+3);
				break;
			case WEST:
				n.setX(n.getX()-3);
				break;
			case EAST:
				n.setX(n.getX()+3);
				break;	
			case SOUTH:
				n.setY(n.getY()-3);
				break;
			}
			driver.travelTo(n);
		}
		
		return true;
			
	}
	
	public static void main(String[] args){
		LCDWriter lcd = LCDWriter.getInstance();
		lcd.start();
		Configuration conf = Configuration.getInstance();
		
		//TODO: replace this with bluetooth-----
		conf.setFlagZone(new Coordinate(120, 60,0), new Coordinate(180,120,0));
		conf.setStartCorner(1);
		conf.setDropZone(new Coordinate(0, 0, 0));
		conf.setOpponentDropZone(new Coordinate(250, 250, 0));
		conf.setFlagColor(3);
		//-----------------
		Driver driver = Driver.getInstance();
		UltrasonicPoller up = UltrasonicPoller.getInstance();
		PathTraveller traveller = PathTraveller.getInstance();
		LineReader llr = LineReader.getLeftSensor();	//left + right line reader
		LineReader rlr = LineReader.getRightSensor();
		Odometer odo = Odometer.getInstance();
		OdometerCorrection oc = OdometerCorrection.getInstance();

//		lcd.writeToScreen("destin: " + destination.toString(), 2);
		up.start();
		odo.start();
		llr.start();
		rlr.start();
		
		LocalizationF localizer = new LocalizationF();
		localizer.callback();
		
		LineReader.subscribeToAll(oc);
		
		Coordinate destination = traveller.getDestination();

		
		try {Thread.sleep(1000);}catch(Exception e){};
		
		
		traveller.recalculatePathToCoords((int)destination.getX(), (int)destination.getY() );
		RemoteConnection.getInstance().setupConnection();

		boolean done  = false;
		while(!done){
			try{
			done = followPath();
			if(!done){ 
				if(traveller.pathIsEmpty())
					destination = traveller.getDestination();
				
				traveller.recalculatePathToCoords((int)destination.getX(), (int)destination.getY() );
			}
			else break;
			}
			catch(Exception e){
				lcd.writeToScreen("E: "+ e.toString(), 1);
			}
		}
		
		//indicate finish
		Sound.beepSequenceUp();	
		
		CaptureMechanism cm = CaptureMechanism.getInstance();
		
		CaptureMechanism.setDropOff(destination);
		
		Stack<Coordinate> path = Searcher.generateSearchPath();
		Coordinate first = path.peek();

		int BLOCK_COLOR = conf.getBlockColor().getCode();
		boolean blockFound  = false;

		while(!blockFound && !path.isEmpty()){
			Coordinate p = path.pop();
			lcd.writeToScreen("Des:" +p.toString(), 4);
			driver.turnTo(Coordinate.calculateRotationAngle(conf.getCurrentLocation(), p));
			int result = ObjectDetector.lookForItemII(BLOCK_COLOR);
			if(result ==1 ){
				Sound.beep();
				blockFound = true;
				cm.open();
				driver.forward(15);
				cm.align();
				driver.forward(15);
				cm.close();
				Sound.beep();
				Sound.beepSequenceUp();		
				break;
			}
			else if(result == 0){ 
				cm.removeBlockII();
				
				ObjRec oRec = new ObjRec();
				//test again
				ArrayList <blockColor> bc = oRec.detect();
				String ans = "";
				for (blockColor b : bc){
					ans += b.toString() ;
				}
				lcd.writeToScreen(ans, 6);	
				if(bc.contains(blockColor.getInstance(BLOCK_COLOR))){
					//face the robot
					blockFound = true;
					cm.open();
					driver.forward(15);
					cm.align();
					driver.forward(15);
					cm.close();
					Sound.beep();
					Sound.beepSequenceUp();		
					break;
				}
			}
			
			
			driver.travelTo(p);
			
			result = ObjectDetector.lookForItemII(BLOCK_COLOR);
			if(result ==1 ){
				Sound.beep();
				blockFound = true;
				cm.open();
				driver.forward(15);
				cm.align();
				driver.forward(15);
				cm.close();
				Sound.beep();
				Sound.beepSequenceUp();		
				break;
			}
			else if(result == 0){
				cm.removeBlockII();
				ObjRec oRec = new ObjRec();
				//test again
				ArrayList <blockColor> bc = oRec.detect();
				String ans = "";
				for (blockColor b : bc){
					ans += b.toString() ;
				}
				lcd.writeToScreen(ans, 6);	
				if(bc.contains(blockColor.getInstance(BLOCK_COLOR))){
					//face the robot
					blockFound = true;
					cm.open();
					driver.forward(15);
					cm.align();
					driver.forward(15);
					cm.close();
					Sound.beep();
					Sound.beepSequenceUp();		
					break;
				}
		}
			
			if(path.isEmpty() && !blockFound){
				path = Searcher.generateSearchPath();
			}
			
		}
		
		driver.travelTo(first);
		driver.travelTo(destination);
		
		destination = conf.getDropZone();
		
		
		traveller.recalculatePathToCoords((int)destination.getX(), (int)destination.getY() );
		done  = false;
		while(!done){
			try{
			done = followPath();
			if(!done){ 
				if(traveller.pathIsEmpty())
					destination = traveller.getDestination();
				
				traveller.recalculatePathToCoords((int)destination.getX(), (int)destination.getY() );
			}
			else break;
			}
			catch(Exception e){
				lcd.writeToScreen("E: "+ e.toString(), 1);
			}
		}
		
		Sound.beepSequenceUp();		
		
		cm.open();
		driver.backward(20);
		cm.close();
		
		//TODO: play awesome victory music here
		
//		driver.rotateToRelatively(360);

		
	}
	
}
