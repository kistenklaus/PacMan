package controller.ai;

import engine.input.ContInputHandler;
import engine.input.Controller;

public class InputAI extends Controller{

	public InputAI(ContInputHandler CIH) {
		super(CIH);
	}
	
	public void update() {
		performAction(Controller.RIGHT);
	}

}
