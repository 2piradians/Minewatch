package twopiradians.minewatch.client.gui;

public interface IGuiScreen {

	public enum Screen {
		MAIN, QUESTION_MARK;
	};
	
	public Screen getCurrentScreen();
}
