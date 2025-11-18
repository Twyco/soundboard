package de.twyco.soundboard.modImplementations.modMenu;

import de.twyco.soundboard.gui.config.ConfigScreenFactory;

public class ModMenuApi implements com.terraformersmc.modmenu.api.ModMenuApi {

    @Override
    public com.terraformersmc.modmenu.api.ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreenFactory::create;
    }

}
