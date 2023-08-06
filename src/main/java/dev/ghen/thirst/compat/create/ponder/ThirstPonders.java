package dev.ghen.thirst.compat.create.ponder;

import com.simibubi.create.foundation.ponder.PonderRegistrationHelper;
import com.simibubi.create.foundation.ponder.PonderTag;
import com.simibubi.create.infrastructure.ponder.AllPonderTags;
import dev.ghen.thirst.compat.create.ponder.scene.SandFilterScene;
import dev.ghen.thirst.Thirst;
import dev.ghen.thirst.compat.create.CreateRegistry;


public class ThirstPonders {
    public static final PonderTag PURIFICATION = new PonderTag(Thirst.asResource("purification"))
            .item(CreateRegistry.SAND_FILTER_BLOCK.get().asItem(), true, false)
            .defaultLang("Purification", "Components which purifying water");

    static final PonderRegistrationHelper HELPER = new PonderRegistrationHelper(Thirst.ID);

    public static void register(){
        HELPER.addStoryBoard(
                CreateRegistry.SAND_FILTER_BLOCK,
                "sand_filter",
                SandFilterScene::filtering,
                AllPonderTags.FLUIDS,
                PURIFICATION
        );
    }
}
