package dev.ghen.thirst.foundation.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

import java.util.*;

public class ConfigHelper
{
    /**
     * This class was taken from <a href="https://github.com/Momo-Studios/Cold-Sweat/blob/1.18.x-FG/src/main/java/dev/momostudios/coldsweat/util/config/ConfigHelper.java">Cold Sweat</a>
     */

    public static Map<Item, Number[]> getItemsWithValues(List<? extends List<?>> source)
    {
        Map<Item, Number[]> map = new HashMap<>();
        for (List<?> entry : source)
        {
            String itemID = (String) entry.get(0);

            if (itemID.startsWith("#"))
            {
                final String tagID = itemID.replace("#", "");
                Optional<ITag<Item>> optionalTag = ForgeRegistries.ITEMS.tags().stream().filter(tag ->
                        tag.getKey().location().toString().equals(tagID)).findFirst();
                optionalTag.ifPresent(itemITag ->
                {
                    for (Item item : optionalTag.get().stream().toList())
                    {
                        map.put(item, new Number[]{(Number) entry.get(1), (Number) entry.get(2)});
                    }
                });
            }
            else
            {
                Item newItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemID));


                if (newItem != null) map.put(newItem, new Number[]{(Number) entry.get(1), (Number) entry.get(2)});
            }
        }
        return map;
    }

    public static List<Item> getItems(List<? extends String> source){
        List<Item> list = new ArrayList<>();
        for(String itemID : source){
            if (itemID.startsWith("#"))
            {
                final String tagID = itemID.replace("#", "");
                Optional<ITag<Item>> optionalTag = ForgeRegistries.ITEMS.tags().stream().filter(tag ->
                        tag.getKey().location().toString().equals(tagID)).findFirst();
                optionalTag.ifPresent(itemITag ->
                        list.addAll(optionalTag.get().stream().toList()));
            }
            else
            {
                Item newItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemID));


                if (newItem != null) list.add(newItem);
            }
        }
        return list;
    }
}
