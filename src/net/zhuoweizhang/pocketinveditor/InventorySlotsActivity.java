package net.zhuoweizhang.pocketinveditor;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import net.zhuoweizhang.pocketinveditor.material.icon.MaterialIcon;
import net.zhuoweizhang.pocketinveditor.material.icon.MaterialKey;

public final class InventorySlotsActivity extends ListActivity implements OnItemClickListener {

	private List<InventorySlot> inventory;

	private List<InventorySlot> tempInventory;

	private ArrayAdapter<InventorySlot> inventoryListAdapter;

	public static final int EDIT_SLOT_REQUEST = 534626;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		tempInventory = EditorActivity.level.getPlayer().getInventory();
		int slotsSize = tempInventory.size() - 8;
		inventory = new ArrayList<InventorySlot>(slotsSize >= 0 ? slotsSize : 0);
		for (InventorySlot slot: tempInventory) {
			if (slot.getSlot() > 8) {
				inventory.add(slot);
			}
		}

		inventoryListAdapter = new ArrayAdapter<InventorySlot>(this, R.layout.slot_list_item, R.id.slot_list_main_text, inventory) {
			public View getView(int position, View convertView, ViewGroup parent) {
				View retval = super.getView(position, convertView, parent);
				ImageView iconView = (ImageView) retval.findViewById(R.id.slot_list_icon);
				InventorySlot slot = this.getItem(position);
				ItemStack stack = slot.getContents();
				MaterialIcon icon = MaterialIcon.icons.get(new MaterialKey(stack.getTypeId(), stack.getDurability()));
				if (icon == null) {
					icon = MaterialIcon.icons.get(new MaterialKey(stack.getTypeId(), (short) 0));
				}
				if (icon != null) {
					BitmapDrawable myDrawable = new BitmapDrawable(icon.bitmap);
					myDrawable.setDither(false);
					myDrawable.setAntiAlias(false);
					myDrawable.setFilterBitmap(false);
					iconView.setImageDrawable(myDrawable);
					iconView.setVisibility(View.VISIBLE);
				} else {
					iconView.setVisibility(View.INVISIBLE);
				}
				return retval;
			}
		};

		setListAdapter(inventoryListAdapter);
		getListView().setOnItemClickListener(this);
	}

	public void onStart() {
		super.onStart();
		inventoryListAdapter.notifyDataSetChanged();
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		openInventoryEditScreen(position, inventory.get(position));
	}

	private void openInventoryEditScreen(int position, InventorySlot slot) {
		Intent intent = new Intent(this, EditInventorySlotActivity.class);
		ItemStack stack = slot.getContents();
		intent.putExtra("TypeId", stack.getTypeId());
		intent.putExtra("Damage", stack.getDurability());
		intent.putExtra("Count", stack.getAmount());
		intent.putExtra("Slot", slot.getSlot());
		intent.putExtra("Index", position);
		startActivityForResult(intent, EDIT_SLOT_REQUEST);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == EDIT_SLOT_REQUEST) {
			if (resultCode == Activity.RESULT_OK) {
				int slotIndex = intent.getIntExtra("Index", -1);
				if (slotIndex < 0) {
					System.err.println("wrong slot index");
					return;
				}
				InventorySlot slot = inventory.get(slotIndex);
				ItemStack stack = slot.getContents();
				stack.setAmount(intent.getIntExtra("Count", 0));
				stack.setDurability(intent.getShortExtra("Damage", (byte) 0));
				stack.setTypeId(intent.getShortExtra("TypeId", (byte) 0));
				EditorActivity.save(this);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(getResources().getString(R.string.add_empty_slot));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(item.getTitle().equals(getResources().getString(R.string.add_empty_slot))){
			InventorySlot newSlot = addEmptySlot();
			if (newSlot != null) {
				openInventoryEditScreen(inventoryListAdapter.getPosition(newSlot), newSlot);
			}
		}
		return false;

	}

	private InventorySlot addEmptySlot(){
		//Check for 36 item cap, if we have a full inventory, just return
		if(inventory.size() > 35){
			return null;
		}
		List<InventorySlot> outInventory = new ArrayList<InventorySlot>();
		for(int i = 0; i < tempInventory.size(); i++){
			if(tempInventory.get(i).getSlot() < 9){
				outInventory.add(tempInventory.get(i));
			}
		}


		InventorySlot slot = new InventorySlot((byte) (inventory.size() + 9), new ItemStack((short)0,(short)0,(short)1));
		alignSlots();
		inventory.add(slot);
		inventoryListAdapter.notifyDataSetChanged();
		outInventory.addAll(inventory);
		EditorActivity.level.getPlayer().setInventory(outInventory);
		EditorActivity.save(this);
		return slot;
	}

	private void alignSlots(){
		for(int i = 0; i < inventory.size(); i++){
			inventory.get(i).setSlot((byte) (i + 9));
		}
	}
}
