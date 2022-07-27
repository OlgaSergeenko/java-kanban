package manager;

import HttpManager.HTTPTaskManager;

import java.io.IOException;
import java.net.URL;

public class Managers {

    public static TaskManager getDefault(URL url) {
        return new HTTPTaskManager(url);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
