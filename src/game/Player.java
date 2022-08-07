package game;

import java.awt.Color;
import java.awt.Graphics2D;

import controller.ai.InputAI;
import controller.human.InputKeyboard;
import engine.Clock;
import engine.input.ContInputHandler;
import engine.input.Controller;
import engine.input.Keyboard;
import game.Field.FieldObj;
import game.Field.ObjType;
import game.tiles.Tile;
import math.linAlgebra.Vector2f;

public class Player{
	
	private final float DOOMTIME = 10f;
	private Controller controller;
	private ControllType contType;
	private int tileSize;
	private Vector2f pos;
	private Vector2f des;
	private Field field;
	private Direction direction, perfDirection;
	private PackmanModel packmanModel;
	private byte lastDirection_index;
	private float speed;
	private float doomTimer;
	private boolean doomMode;
	
	public Player(ControllType contType, int xCoord, int yCoord, int tileSize, Field field) {
		this.contType = contType;
		this.direction = new Direction();
		this.perfDirection = new Direction();
		this.lastDirection_index = 0;
		ContInputHandler contHandler = new ContInputHandler() {
			public void otherActionEvents(int arg0) {}
			public void action_right() 	{
				perfDirection.right();
			}
			public void action_left() 	{
				perfDirection.left();
			}
			public void action_up() 	{
				perfDirection.up();
			}
			public void action_down() 	{
				perfDirection.down();
			}
			public void action_space() 	{}
			public void action_shift() 	{}
			public void action_t()		{}
			public void action_e() 		{}
			public void action_q() 		{}
			public void action_r() 		{}
			public void action_f() 		{}
		};
		
		if(contType == ControllType.HUMAN) {
			controller = new InputKeyboard(contHandler);
		}else if(contType == ControllType.AI) {
			controller = new InputAI(contHandler);
		}
		this.tileSize = tileSize;
		
		this.field = field;
		
		this.pos = new Vector2f(xCoord * tileSize, yCoord * tileSize);
		this.des = new Vector2f(pos);
		
		this.speed = 100;
		
		this.packmanModel = new PackmanModel(tileSize/2, this.direction);
		this.doomMode = false;
		this.doomTimer = 0;
	}
	
	public void update() {
		//Keyboard Input
		if(contType == ControllType.HUMAN) {
			((Keyboard) controller).update();
		}
		else if(contType == ControllType.AI) {
			((InputAI) controller).update();
		}
		
		Vector2f steer = new Vector2f(des.getX() - pos.getX(), des.getY() - pos.getY());
		float steer_mag = steer.mag();
		if(steer_mag < 1) { // desReached
			//terminates pos to a perfekt Coord position
			int xCoord = (int) Math.round(pos.getX()/ tileSize);
			int yCoord = (int) Math.round(pos.getY()/ tileSize);
			FieldObj fobj = field.checkForFieldObj(xCoord, yCoord);
			if(fobj != null) {
				if(fobj.getType() == ObjType.POINT) {
					this.field.removePoint(fobj);
				}else if(fobj.getType() == ObjType.PORTAL) {
					xCoord = fobj.getXCoord();
					yCoord = fobj.getYCoord();
				}else if(fobj.getType() == ObjType.POWERUP) {
					enableDoomMode();
					this.field.removePowerup(fobj);
				}
			}
			this.pos = new Vector2f(xCoord*tileSize, yCoord*tileSize);
			
			this.direction.setDirectionIndex(perfDirection.getIndex());
			this.des = newDestination(xCoord, yCoord);
		}else { // desNotReached
			steer.scale((float)(speed*Clock.dT())/steer_mag); //sets the mag of steer to speed
			pos.add(steer);
		}
		
		if(doomTimer > 0) {
			doomTimer -= Clock.dT();
		}else if(doomMode) {
			disableDoomMode();
		}
		
		packmanModel.update();
	}
	private void enableDoomMode() {
		if(doomMode) {
			this.speed/=1.5f;
		}
		this.doomMode = true;
		this.field.enableDoomMode();
		this.doomTimer = DOOMTIME;
		this.speed*=1.5f;
	}
	
	private void disableDoomMode() {
		this.doomMode = false;
		this.field.disableDoomMode();
		this.speed/=1.5f;
	}
	
	public Vector2f newDestination(int xCoord, int yCoord) {
		Tile[][] tiles = field.getTiles();
		int nx = getNx(xCoord);
		int ny = getNy(yCoord);
		if(tiles[nx][ny].isBlocked()) {
			//check last Direction:
			direction.setDirectionIndex(lastDirection_index);
			if(!tiles[getNx(xCoord)][getNy(yCoord)].isBlocked()) {
				return newDestination(xCoord, yCoord);
			}
			
			//check every other directon
			byte c1 = (byte) (lastDirection_index+1);
			if(c1 > 4)c1-=4;
			direction.setDirectionIndex(c1);
			if(!tiles[getNx(xCoord)][getNy(yCoord)].isBlocked()) {
				return newDestination(xCoord, yCoord);
			}
			
			byte c2 = (byte) (lastDirection_index-1);
			if(c2 < 1)c2+=4;
			direction.setDirectionIndex(c2);
			if(!tiles[getNx(xCoord)][getNy(yCoord)].isBlocked()) {
				return newDestination(xCoord, yCoord);
			}
			
			direction.stop();
			return newDestination(xCoord, yCoord);
		}else {
			lastDirection_index = direction.getIndex();
			return new Vector2f(nx*tileSize, ny *tileSize); 
		}
	}
	/**
	 * calc the new XCoord after moved in direction
	 * @param xCoord the current pos
	 * @return the coord after moving
	 */
	public int getNx(int xCoord) {
		int nx = xCoord;
		if(this.direction.isRight()) {
			nx += 1;
		}else if(this.direction.isLeft()) {
			nx -= 1;
		}
		return nx;
	}
	/**
	 * calc the new YCoord after moved in direction
	 * @param YCoord the current pos
	 * @return the coord after moving
	 */
	public int getNy(int yCoord) {
		int ny = yCoord;
		if(this.direction.isUp()) {
			ny -= 1;
		}else if(this.direction.isDown()) {
			ny += 1;
		}
		return ny;
	}
	
	private class Direction {
		private byte direction;
		
		private Direction() {stop();}
		
		private void up() {this.direction = 1;}
		private void right() {this.direction = 2;}
		private void down() {this.direction = 3;}
		private void left() {this.direction = 4;}
		private void stop() {this.direction = 0;}
		private void setDirectionIndex(byte index) {this.direction =index;}
		private boolean isUp() {return this.direction == 1;}
		private boolean isRight() {return this.direction == 2;}
		private boolean isDown() {return this.direction == 3;}
		private boolean isLeft() {return this.direction == 4;}
		private boolean isStatic() {return this.direction == 0;}
		
		private byte getIndex() {return this.direction;}
		public String toString() {
			switch(direction) {
			case 0:
				return "static";
			case 1:
				return "up";
			case 2:
				return "right";
			case 3:
				return "down";
			case 4:
				return "left";
			default:
				System.err.println("[class]Player.Direction ERROR: Direction is not Defined: " + direction);
				System.exit(-1);
				return null;
			}
		}
	}
	
	
	
	
	
	/*
	 * RENDER SEGMENT:
	 */
	
	
	
	public void render(Graphics2D g2d, int tx, int ty) {
		g2d.setColor(Color.yellow);
		
		int px;
		int py;
		synchronized(pos) {
			px = (int) pos.getX() + tileSize/2;
			py = (int) pos.getY() + tileSize/2;
		}
		g2d.translate(px + tx, py + ty);
		packmanModel.render(g2d);
		g2d.translate(-px -tx, py-ty);
	}
	
	private class PackmanModel{
		private static final int OPENANGLE = 150;
		private static final float OPENINGSPEED = 3;
		
		private Direction direction;
		private byte currDirection;
		private int radius;
		private int rotation;
		private float open;
		private boolean opening;
		
		private PackmanModel(int radius, Direction direction) {
			this.direction = direction;
			this.radius = radius;
			this.opening = true;
			this.open = 0;
		}
		
		public void render(Graphics2D g2d) {
			int closeAngle = (int) ((1-open)*OPENANGLE);
			g2d.fillArc(-radius, -radius, radius*2, radius*2, OPENANGLE/2 - closeAngle + rotation,
					360-OPENANGLE + closeAngle*2);
			
			
		}
		public void update() {
			if(opening) {
				open += OPENINGSPEED *Clock.dT();
				if(open >= 1)opening = false;
			}else {
				open -= OPENINGSPEED *Clock.dT();
				if(open <= 0.5f)opening = true;
			}
			//open = 1;
			ajustDirection();
		}
		
		private void ajustDirection() {
			if(direction.getIndex() != currDirection || direction.isStatic()) {
				this.rotation = 0;
				if(direction.isUp()) {
					rotation = 90;
				}else if(direction.isRight()) {
					rotation = 0;
				}else if(direction.isDown()) {
					rotation = -90;
				}else if(direction.isLeft()) {
					rotation = 180;
				}
				currDirection = direction.getIndex();
			}
		}
	}
	
	
	
	public enum ControllType{
		HUMAN, AI;
	}
	public Controller getController() {
		return controller;
	}
	
}
