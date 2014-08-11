package au.com.twosquared.tower;

import java.util.ArrayList;

import org.flixel.FlxG;
import org.flixel.FlxGesture;
import org.flixel.FlxObject;
import org.flixel.FlxPath;
import org.flixel.FlxRect;
import org.flixel.FlxSprite;
import org.flixel.event.IFlxGesture;
import org.flixel.plugin.GestureManager;
import org.flixel.plugin.GestureManager.GestureData;
import org.flxbox2d.B2FlxState;
import org.flxbox2d.collision.shapes.B2FlxBox;
import org.flxbox2d.collision.shapes.B2FlxCircle;
import org.flxbox2d.collision.shapes.B2FlxPolygon;
import org.flxbox2d.collision.shapes.B2FlxShape;

import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import flash.ui.Mouse;

public class PlayState extends B2FlxState {

	private final int BACKGROUND = 0xFFD0F4F7;
	private final int MAX_HEIGHT = 999999;

	private final short NONE = 0x000;
	private final short GRASS = 0x001;
	private final short BLOCK = 0x002;
	private final short BLOCK_MASK = (short) (GRASS | BLOCK);

	private final float BLOCK_DENSITY = 2000f;
	private final float BLOCK_FRICTION = 0.5f;
	private final float BLOCK_LINEAR_DAMPING = 0.9f;

	private final float FORCE_MODIFIER = 5000f;
	private final float INACTIVE_MARGIN = 0.00001f;

	private ArrayList<B2FlxShape> blocks;
	private FlxObject cameraman;
	private B2FlxShape activeBlock;

	@Override
	public void create() {
		super.create();

		Mouse.show();
		FlxG.setBgColor(BACKGROUND);
		FlxG.camera.setBounds(0, -MAX_HEIGHT, FlxG.width, MAX_HEIGHT + 70);
		FlxG.addPlugin(GestureManager.class);
		// FlxG.visualDebug = true;
		// B2FlxDebug.drawBodies = true;

		FlxGesture gesture = new FlxGesture();
		gesture.start(new IFlxGesture() {
			@Override
			public void callback(int gesture, GestureData data) {
				if (activeBlock == null) {
					return;
				}

				activeBlock.body.applyForceToCenter(data.velocityX
						* FORCE_MODIFIER, 0, true);
			}
		});

		blocks = new ArrayList<B2FlxShape>();

		cameraman = new FlxObject();
		add(cameraman);

		B2FlxBox sky = new B2FlxBox(0, -512, 512, 512);
		sky.loadGraphic("pack:colored_grass");
		sky.setType(BodyType.StaticBody);
		sky.setCategoryBits((short) 0);
		sky.create();
		add(sky);

		for (int i = -3; i < (FlxG.width / 70) + 4; i++) {
			B2FlxBox grass = new B2FlxBox(i * 70, 0, 70, 70);
			grass.loadGraphic("pack:grass");
			grass.setType(BodyType.StaticBody);
			grass.setCategoryBits(GRASS);
			grass.create();
			add(grass);
		}

		int j = (int) (Math.random() * (FlxG.width / 70));

		B2FlxBox bush = new B2FlxBox(j * 70, -70, 70, 70);
		bush.loadGraphic("pack:bush");
		bush.setType(BodyType.StaticBody);
		bush.setCategoryBits(NONE);
		bush.create();
		add(bush);
	}

	@Override
	public void update() {
		super.update();

		if (activeBlock != null) {
			if (activeBlock.body.getLinearVelocity().y < INACTIVE_MARGIN) {
				activeBlock = null;
			}
		} else {
			spawnBlock();
		}

		B2FlxShape highestBlock = null;
		float maxHeight = 0;

		// Find highest block
		for (B2FlxShape block : blocks) {
			if (block == activeBlock) {
				continue;
			}

			if (block.y < maxHeight) {
				highestBlock = block;
				maxHeight = block.y;
			}
		}

		// Center camera on highest block
		if (highestBlock != null) {
			FlxPath path = new FlxPath();
			path.addPoint(cameraman.getMidpoint());
			path.addPoint(highestBlock.getMidpoint());

			cameraman.stopFollowingPath(true);
			cameraman.followPath(path, 100);

			FlxG.camera.follow(cameraman);
			FlxG.camera.deadzone = new FlxRect(0, FlxG.height * 1 / 2,
					FlxG.width, FlxG.height * 1 / 2);
		}
	}

	private void spawnBlock() {
		String[] names = { "elementGlass012", "elementGlass018",
				"elementGlass021", "elementMetal011", "elementMetal017",
				"elementMetal020", "elementStone011", "elementStone017",
				"elementStone020", "elementWood010", "elementWood016",
				"elementWood019" };

		// Pick a random block type
		int i = (int) (Math.random() * names.length);

		// Initial block details
		String name = names[i];
		Block type = Block.BOX;
		String graphic = "pack:" + name;
		FlxSprite sprite = new FlxSprite().loadGraphic(graphic);

		// Position information based on sprite
		float width = sprite.width;
		float height = sprite.height;
		float x = (FlxG.width - width) / 2;
		float y = FlxG.camera.scroll.y - height;

		B2FlxShape block;

		switch (type) {
		case TRIANGLE:
			x += width / 2;
			y += width / 2;

			float[][][] right = { { { -width / 2, -height / 2 },
					{ -width / 2, height / 2 }, { width / 2, height / 2 } } };

			block = new B2FlxPolygon(x, y, right);
			break;
		case ISOCELES:
			x += width / 2;
			y += width / 4;

			float[][][] isoceles = { { { -width / 2, height / 2 },
					{ 0, -height / 2 }, { width / 2, height / 2 } } };

			block = new B2FlxPolygon(x, y, isoceles);
			break;
		case CIRCLE:
			block = new B2FlxCircle(x, y, width / 2);
			break;
		default:
			block = new B2FlxBox(x, y, width, height);
			break;
		}

		// 50% chance of being rotated initially
		if (Math.random() > 0.5) {
			block.setAngle(90f);
		}

		block.loadGraphic(graphic);
		block.getTexture()
				.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		block.setCategoryBits(BLOCK);
		block.setMaskBits(BLOCK_MASK);
		block.setDensity(BLOCK_DENSITY);
		block.setFriction(BLOCK_FRICTION);
		block.setLinearDamping(BLOCK_LINEAR_DAMPING);
		block.create();
		add(block);

		// Apply downward impulse to prevent initial zero velocity
		Vector2 center = block.body.getLocalCenter();
		block.body.applyLinearImpulse(0, 1, center.x, center.y, true);

		// Add block to height listener (for camera and score)
		blocks.add(block);

		// Set current block to active
		activeBlock = block;
	}
}