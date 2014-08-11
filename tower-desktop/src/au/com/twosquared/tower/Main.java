package au.com.twosquared.tower;

import org.flixel.FlxDesktopApplication;

public class Main {
	public static void main(String[] args) {
		new FlxDesktopApplication(new TowerGame(), "Tipsy Towers", 768 / 2,
				1280 / 2, true);
	}
}
