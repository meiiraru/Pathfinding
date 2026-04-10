package pathfinding;

import cinnamon.Cinnamon;
import cinnamon.Client;
import cinnamon.events.EventType;
import cinnamon.settings.Settings;

public class Main {

    public static void main(String... args) {
        Cinnamon.TITLE = "Pathfinding";
        Client.mainScreen = GridManager::new;
        Client client = Client.getInstance();
        client.events.registerEvent(EventType.CLIENT_INIT, objects -> {
            Settings.guiScale.set(client.window.guiScale);
            client.windowResize(client.window.width, client.window.height);
        });
        new Cinnamon().run();
    }
}
