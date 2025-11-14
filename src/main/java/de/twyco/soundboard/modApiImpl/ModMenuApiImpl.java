package de.twyco.soundboard.modApiImpl;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.twyco.soundboard.gui.config.SoundboardConfigScreenFactory;

public class ModMenuApiImpl implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return SoundboardConfigScreenFactory::create;
    }

}
