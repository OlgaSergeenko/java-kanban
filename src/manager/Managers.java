package manager;

import manager.http.HTTPTaskManager;

import java.net.URL;

public class Managers {

    public static TaskManager getDefault(URL url) {
        return new HTTPTaskManager(url);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
