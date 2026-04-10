package pathfinding;

import cinnamon.Cinnamon;
import cinnamon.Client;

public class Main {

    public static void main(String... args) {
        Cinnamon.TITLE = "Pathfinding";
        Client.mainScreen = GridManager::new;
        new Cinnamon().run();
    }
}
