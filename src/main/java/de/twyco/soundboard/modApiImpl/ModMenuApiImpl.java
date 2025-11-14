package de.twyco.soundboard.modApiImpl;

import com.terraformersmc.modmenu.api.ModMenuApi;
import de.twyco.soundboard.gui.config.ConfigScreenFactory;

public class ModMenuApiImpl implements ModMenuApi {

    @Override
    public com.terraformersmc.modmenu.api.ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreenFactory::create;
    }

}
