package game.tiles;

public class Tile {
	
	public enum Type{
		PATH(false),
		BLOCK(true);
		
		private boolean blocked;
		
		Type(boolean blocked){
			this.blocked = blocked;
		}
		public boolean isBlocked() {
			return this.blocked;
		}
	}
	
	private Type type;
	
	public Tile(Type type) {
		this.type = type;
	}
	
	public Type getType() {
		return this.type;
	}
	public boolean isBlocked() {
		return this.type.blocked;
	}
	
}
