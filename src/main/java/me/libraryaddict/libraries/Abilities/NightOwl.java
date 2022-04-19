package me.libraryaddict.libraries.Abilities;

import org.bukkit.event.EventHandler;

import me.libraryaddict.Hungergames.Abilities.Ninja;
import me.libraryaddict.Hungergames.Events.GameStartEvent;

public class NightOwl extends Ninja {
    public String[] potionEffectsDuringDay = new String[] { "NIGHT_VISION 0" };
    public String[] potionEffectsDuringNight = new String[] { "NIGHT_VISION 0"};

    @EventHandler
    public void gameStartEvent(GameStartEvent event) {
        super.potionEffectsDuringDay = potionEffectsDuringDay;
        super.potionEffectsDuringNight = potionEffectsDuringNight;
        super.gameStartEvent(event);
    }
}