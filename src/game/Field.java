package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import engine.Clock;
import engine.graphics.Animation;
import engine.graphics.Image;
import game.tiles.Tile;
import game.tiles.Tile.Type;

public class Field {
	
	private BufferedImage field_bg, fmap;
	private Tile[][] tiles;
	private FieldObj[] portals;
	private ArrayList<FieldObj> points;
	private ArrayList<FieldObj> powerups;
	private int tileSize;
	private Image img_point, img_powerup;
	private Animation doomAni;
	private boolean doomMode;
	private int width,height;
	private ArrayList<Blend> blendings;
	
	public Field(BufferedImage field_bg, BufferedImage fmap, int width, int height) {
		this.field_bg = field_bg;
		this.fmap = fmap;
		int hor = fmap.getWidth();
		int ver = fmap.getHeight();
		if(width%hor == 0 && height%ver == 0) {
			tileSize = width/hor;
		}else {
			System.err.println("The width and/or height are shit");
			System.exit(0);
		}
		this.width = hor * tileSize;
		this.height = ver * tileSize;
		
		this.img_point = new Image("./res/Point.png");
		this.img_powerup = new Image("./res/Crystal.png");
		
		this.points = new ArrayList<>();
		this.powerups = new ArrayList<>();
		
		this.portals = new FieldObj[2];
		int portal_index = 0;
		
		this.tiles = new Tile[hor][ver];
		for(int x = 0; x < hor; x++) {
			for(int y = 0; y < ver; y++) {
				int color= fmap.getRGB(x, y);
				if(color == Color.BLUE.getRGB()) {//blue
					this.tiles[x][y] = new Tile(Type.PATH);
					//adding Points
					this.points.add(new FieldObj(x, y, ObjType.POINT));
				}else if(color == -1) {//white
					this.tiles[x][y] = new Tile(Type.PATH);
				}else if(color == -16777216) { //black
					this.tiles[x][y] = new Tile(Type.BLOCK);
				}else if(color == Color.YELLOW.getRGB()) { //yellow
					this.tiles[x][y] = new Tile(Type.PATH);
					//adding Powerups
					this.powerups.add(new FieldObj(x, y, ObjType.POWERUP));
				}else if(color ==  -65281) { //pink
					this.tiles[x][y] = new Tile(Type.BLOCK);
				}else if(color == Color.RED.getRGB()) { // red
					//adding portals
					this.tiles[x][y] = new Tile(Type.PATH);
					portals[portal_index] = new FieldObj(x, y, ObjType.PORTAL);
					portal_index++;
					
				}else {
					System.err.println("The Color[" +new Color(color) +"] in the sourcemap is not defined at pixel:" + x + "|" + y);
					System.exit(0);
				}
			}
		}
		this.doomMode = false;
		this.doomAni = new Animation("./res/Field/", 0.1f);
		
		this.blendings = new ArrayList<>();
	}
	
	public void genPointsAndPowerups() {
		this.powerups.clear();
		this.points.clear();
		for(int x = 0; x < fmap.getWidth(); x++) {
			for(int y = 0; y < fmap.getHeight(); y++) {
				int color= fmap.getRGB(x, y);
				if(color == Color.BLUE.getRGB()) {//blue
					this.points.add(new FieldObj(x, y, ObjType.POINT));
				}else if(color == Color.YELLOW.getRGB()) { //yellow
					this.powerups.add(new FieldObj(x, y, ObjType.POWERUP));
				}
			}
		}
	}
	
	public void update() {
		if(points.size() == 0)genPointsAndPowerups();
		for(int i = 0; i < blendings.size(); i++) {
			Blend b = blendings.get(i);
			b.update();
			if(!b.isAlive()) {
				blendings.remove(b);
				i--;
			}
			
		}
	}
	
	public class FieldObj {
		private int xCoord, yCoord;
		private ObjType type;
		public FieldObj(int xCoord, int yCoord, ObjType type) {
			this.xCoord = xCoord;
			this.yCoord = yCoord;
			this.type = type;
		}
		public boolean posEquals(int ex, int ey) {
			return ex==xCoord && ey==yCoord;
		}
		public int getXCoord() {
			return this.xCoord;
		}
		public int getYCoord() {
			return this.yCoord;
		}
		public ObjType getType() {
			return this.type;
		}
	}
	public enum ObjType{
		POINT, POWERUP, PORTAL;
	}
	
	
	/**
	 * Checks if the x/y Coords are on a Portal and gives back the Portal to teleport to
	 * @param xCoord
	 * @param yCoord
	 * @return
	 */
	public FieldObj checkForFieldObj(int xCoord, int yCoord) {
		if(portals[0].posEquals(xCoord, yCoord)) {
			return portals[1];
		}else if(portals[1].posEquals(xCoord, yCoord)) {
			return portals[0];
		}
		for(FieldObj obj : points) {
			if(obj.posEquals(xCoord, yCoord))return obj;
		}
		for(FieldObj obj : powerups) {
			if(obj.posEquals(xCoord, yCoord))return obj;
		}
		return null;
	}
	
	public void removePoint(FieldObj point) {
		this.points.remove(point);
		if(doomMode) {
			this.doomAni.step();
		}
	}
	public void removePowerup(FieldObj powerup) {
		this.powerups.remove(powerup);
	}
	
	public void render(Graphics2D g2d, int tx, int ty) {
		if(doomMode) {
			doomAni.render(g2d, tx, ty, width, height);
		}else {
			g2d.drawImage(field_bg, tx, ty, width, height, null);
		}
		for(int i = 0; i < points.size(); i++) {
			FieldObj p = points.get(i);
			img_point.render(g2d, tx + p.getXCoord()*tileSize, ty + p.getYCoord()*tileSize, tileSize, tileSize);
		}
		for(int i = 0; i < powerups.size(); i++) {
			FieldObj p = powerups.get(i);
			img_powerup.render(g2d, tx + p.getXCoord()*tileSize, ty + p.getYCoord()*tileSize, tileSize, tileSize);
		}
		for(int i = 0; i < blendings.size();i++) {
			blendings.get(i).render(g2d, tx, ty, width, height);
		}
		
	}
	
	public class Blend {
		private float blend;
		private float blendtime;
		
		private int r,g,b;
		
		public Blend(int red, int green, int blue, float blendtime) {
			this.blend = blendtime;
			this.blendtime = blendtime;
			this.r = red;
			this.g = green;
			this.b = blue;
		}
		public void update() {
			blend-=Clock.dT();
		}
		public void render(Graphics2D g2d, int tx, int ty, int width, int height) {
			g2d.setColor(new Color(r, g, b, (int)(blend/blendtime*255)));
			g2d.fillRect(tx, ty, width, height);
		}
		public boolean isAlive() {
			return blend > 0;
		}
	}
	
	public void enableDoomMode() {
		this.blendings.add(new Blend(255, 255, 255, 1));
		this.doomMode = true;
	}
	
	public void disableDoomMode() {
		this.doomMode = false;
	}
	
	public int getTileSize() {
		return this.tileSize;
	}
	public Tile[][] getTiles(){
		return tiles;
	}
}
