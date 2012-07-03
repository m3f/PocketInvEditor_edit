package net.zhuoweizhang.pocketinveditor;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.zhuoweizhang.pocketinveditor.entity.*;
import net.zhuoweizhang.pocketinveditor.io.EntityDataConverter;
import net.zhuoweizhang.pocketinveditor.util.Vector;

public final class EntitiesInfoActivity extends Activity implements View.OnClickListener {

	private TextView entityCountText;

	private List<Entity> entitiesList;

	private Button apozombielypseButton;

	public void onCreate(Bundle savedInstanceState)	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entities_info);
		entityCountText = (TextView) findViewById(R.id.entities_main_count);
		apozombielypseButton = (Button) findViewById(R.id.entities_apozombielypse);
		if (apozombielypseButton != null) {
			apozombielypseButton.setOnClickListener(this);
		}
		loadEntities();
	}

	protected void loadEntities() {
		new Thread(new LoadEntitiesTask()).start();
	}

	protected void onEntitiesLoaded(List<Entity> entitiesList) {
		EditorActivity.level.setEntities(entitiesList);
		this.entitiesList = entitiesList;
		countEntities();
	}

	public void onClick(View v) {
		if (v == apozombielypseButton) {
			apozombielypse();
		}
	}

	protected void countEntities() {
		Map<EntityType, Integer> countMap = new EnumMap<EntityType, Integer>(EntityType.class);
		for (Entity e: entitiesList) {
			EntityType entityType = e.getEntityType();
			int newCount = 1;
			Integer oldCount = countMap.get(entityType);
			if (oldCount != null) {
				newCount += oldCount;
			}
			countMap.put(entityType, newCount);
		}

		String entityCountString = buildEntityCountString(countMap);
		entityCountText.setText(entityCountString);
	}

	private String buildEntityCountString(Map<EntityType, Integer> countMap) {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<EntityType, Integer> entry: countMap.entrySet()) {
			builder.append(this.getResources().getText(EntityTypeLocalization.namesMap.get(entry.getKey())));
			builder.append(':');
			builder.append(entry.getValue());
			builder.append('\n');
		}
		return builder.toString();
	}

	public void apozombielypse() {
		List<Entity> list = EditorActivity.level.getEntities();
		Vector playerLoc = EditorActivity.level.getPlayer().getLocation();
		int beginX = (int) playerLoc.getX() - 16;
		int endX = (int) playerLoc.getX() + 16;
		int beginZ = (int) playerLoc.getZ() - 16;
		int endZ = (int) playerLoc.getZ() + 16;
		for (int x = beginX; x < endX; x += 2) {
			for (int z = beginZ; z < endZ; z += 2) {
				zombie zombie = new Zombie();
				zombie.setLocation(new Vector(x, 128, z));
				zombie.setEntityTypeId(EntityType.ZOMBIE.getId());
				zombie.setHealth((short) 25);
				list.add(zombie);
			}
		}
		save(this);
		countEntities();
	}

	private class LoadEntitiesTask implements Runnable {
		public void run() {
			File entitiesFile = new File(EditorActivity.worldFolder, "entities.dat");
			try {
				final List<Entity> entitiesList = EntityDataConverter.read(entitiesFile);
				EntitiesInfoActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						EntitiesInfoActivity.this.onEntitiesLoaded(entitiesList);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				EntitiesInfoActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						EntitiesInfoActivity.this.onEntitiesLoaded(new ArrayList<Entity>());
					}
				});
			}
		}
	}

	public static void save(final Activity context) {
		new Thread(new Runnable() {
			public void run() {
				try {
					EntityDataConverter.write(EditorActivity.level.getEntities(), new File(EditorActivity.worldFolder, "entities.dat"));
					if (context != null) {
						context.runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show();
							}
						});
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (context != null) {
						context.runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(context, R.string.savefailed, Toast.LENGTH_SHORT).show();
							}
						});
					}
				}
			}
		}).start();
	}
}
