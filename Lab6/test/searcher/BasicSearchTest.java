package searcher;

import java.util.ArrayList;

import movement.Driver;
import navigation.Map;
import navigation.PathTraveller;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.Sound;
import lejos.robotics.navigation.Waypoint;
import objectdetection.ObjectDetector;
import objectdetection.ObstacleDetector;
import objectdetection.Trajectory;
import odometry.Odometer;
import odometry.OdometerCorrection;
import robotcore.Configuration;
import robotcore.Coordinate;
import robotcore.LCDWriter;
import search.ObjRec;
import sensors.LineReader;
import sensors.UltrasonicPoller;

/**
 * Basic search test for 1 tile and a 20 cm radius
 *  * 
 * @author Peter Henderson
 *
 */
public class BasicSearchTest {

	private static Trajectory searchTile(){
		UltrasonicPoller up = UltrasonicPoller.getInstance();
		NXTRegulatedMotor sensorMotor = Configuration.SENSOR_MOTOR;
		sensorMotor.setSpeed(45);
		
		sensorMotor.rotateTo(-55, true);
//		LCDWriter.getInstance().writeToScreen("D: " + up.getDistance(), 0);

		//TODO: better algorithm
		while(sensorMotor.getPosition() > -55){
			LCDWriter.getInstance().writeToScreen("D: " + up.getDistance(), 0);

			if(up.getDistance() < 25){
				sensorMotor.rotateTo(0);
				return new Trajectory(-sensorMotor.getPosition()+15, up.getDistance());
			}
		}
	
		sensorMotor.rotateTo(55, true);
		
		while(sensorMotor.getPosition() < 55){
			LCDWriter.getInstance().writeToScreen("D: " + up.getDistance(), 0);

			if(up.getDistance() < 25){
//				sensorMotor.stop();
				sensorMotor.rotateTo(0);
				return new Trajectory(-sensorMotor.getPosition()-15, up.getDistance());
			}
		}
		
		return null;
	}
	
	public static void main(String[] args){
		LCDWriter lcd = LCDWriter.getInstance();
		lcd.start();
		
		UltrasonicPoller up = UltrasonicPoller.getInstance();

		Odometer odo = Odometer.getInstance();
		Driver.setSpeed(30);
		Driver dr = Driver.getInstance();
		ObjRec or = new ObjRec();
		
		up.start();
		odo.start();

		Trajectory block = searchTile();

//		
		lcd.writeToScreen("here", 1);
		if(block != null){
			dr.turnTo(odo.getTheta() + block.theta);
			
			dr.forward(Math.abs(block.distance-5));
			try{
//				if(or == null) lcd.writeToScreen("NO!", 1);
				
				ArrayList<ObjRec.blockColor> color = or.detect();
			
//		
			if(color == null || color.size() == 0)
				lcd.writeToScreen("EMPTY", 1);
			else{
				int count = 1;
				for(ObjRec.blockColor c : color)
					if(c != null)
						lcd.writeToScreen( c.name(), count++);
			}
			}
			catch(Exception e){
				lcd.writeToScreen("EXCEPTION", 1);
			}

		}
		
//		lcd.writeToScreen("fin", 1);
		//indicate finish
		Sound.beepSequenceUp();		
		
	}
	
}
