package controller.human;

import java.awt.event.KeyEvent;

import engine.input.ContInputHandler;
import engine.input.Keyboard;

public class InputKeyboard extends Keyboard{

	public InputKeyboard(ContInputHandler CIH) {
		super(CIH, OutputType.OnPress);
	}
	@Override
	public void pressedOtherKeys(KeyEvent e) {
		
	}
	@Override
	public void releasedOtherKeys(KeyEvent e) {
		
	}
}
