package game;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import engine.Engine;
import engine.GameContainer;
import game.Player.ControllType;

public class PackMan extends GameContainer{
	
	public static final int WIDTH = 645, HEIGHT = 669;
	
	private Player player;
	private Field field;
	
	protected PackMan() {super(WIDTH, HEIGHT);}
	
	@Override
	public void init(Engine e) {
		BufferedImage fbg= null;
		BufferedImage fmap = null;
		try {
			fbg = ImageIO.read(new File("./res/Fieldbackground.png"));
			fmap = ImageIO.read(new File("./res/Map.png"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		this.field = new Field(fbg, fmap, 644, 644);
		this.player = new Player(ControllType.HUMAN,6,5,field.getTileSize(), field);
		e.addController(player.getController());
	}

	@Override
	public void update() {
		this.player.update();
		this.field.update();
	}
	
	@Override
	public void render(Graphics2D g2d) {
		field.render(g2d, 2, 26); // windowborder offset
		player.render(g2d, 2, 26);
	}
	
	
	public static void main(String[] args) {
		new Engine(new PackMan(), 60).init();
	}
}
