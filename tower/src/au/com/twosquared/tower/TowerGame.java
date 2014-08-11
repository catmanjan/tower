package au.com.twosquared.tower;

import org.flixel.FlxCamera;
import org.flixel.FlxGame;

public class TowerGame extends FlxGame {

	public TowerGame() {
		super(512, 853, PlayState.class, 1, 60, 60, true, FlxCamera.FILL_Y);
	}
}
