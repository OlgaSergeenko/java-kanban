package tests;

import controllers.HttpTaskServer;
import controllers.KVServer;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    private KVServer kvServer;
    private HttpTaskServer httpTaskServer;
    private HttpClient client;
    private Task task1;
    private Task task2;
    private Epic epic;
    private Subtask subtask;
    private Gson gson;

    @BeforeEach
    public void BeforeEach() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
        httpTaskServer = new HttpTaskServer();
        httpTaskServer.start();
        client = HttpClient.newHttpClient();
        task1 = new Task("Задача1", "Описание1", Status.NEW,
                LocalDateTime.of(2022, Month.JULY, 9, 22, 15), 60);
        task1.setId(1);
        task2 = new Task("Задача2", "Описание2", Status.IN_PROGRESS,
                LocalDateTime.of(2022, Month.JULY, 20, 12, 0), 90);
        task2.setId(2);
        epic = new Epic("Epic1", "Descr1", null,0);
        epic.setId(3);
        subtask = new Subtask("Subtask", "Descr", Status.DONE,
                LocalDateTime.of(2022,8,20,15,30), 120, 3);
        subtask.setId(4);
        gson = new Gson();
    }

    @AfterEach
    public void AfterEach() {
        kvServer.stop();
        httpTaskServer.stop();
    }

    @Test
    public void shouldHandleTask() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/task");
        String json = gson.toJson(task1);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request1 = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response1.statusCode(), "Неверный статус-код при создании задачи.");

        URI url2 = URI.create("http://localhost:8080/tasks/task?id=1");
        HttpRequest request2 = HttpRequest.newBuilder().uri(url2).GET().build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        String jsonTask = response2.body();
        Task taskReceived = gson.fromJson(jsonTask, Task.class);
        assertEquals(200, response2.statusCode(), "Неверный статус-код при получении задачи.");
        assertEquals(task1, taskReceived, "Задачи не совпадают.");

        Task updated = new Task("Задача", "Описание", Status.IN_PROGRESS,
                LocalDateTime.of(2022, Month.JULY, 9, 22, 15), 90);
        updated.setId(1);
        json = gson.toJson(updated);
        final HttpRequest.BodyPublisher body2 = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request3 = HttpRequest.newBuilder().uri(url2).POST(body2).build();
        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response3.statusCode(), "Неверный статус-код при обновлении задачи.");

        HttpResponse<String> response4 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        jsonTask = response4.body();
        taskReceived = gson.fromJson(jsonTask, Task.class);
        assertEquals(200, response4.statusCode(), "Неверный статус-код при получении задачи.");
        assertEquals(updated, taskReceived, "Задачи не совпадают.");

        HttpRequest request5 = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response5 = client.send(request5, HttpResponse.BodyHandlers.ofString());
        String jsonTasks = response5.body();
        List<Task> tasks = gson.fromJson(jsonTasks, List.class);
        assertEquals(200, response4.statusCode(), "Неверный статус-код при получении всех задач.");
        assertEquals(tasks.size(), List.of(updated).size(), "Неверное количество задач.");

        HttpRequest request6 = HttpRequest.newBuilder().uri(url2).DELETE().build();
        HttpResponse<String> response6 = client.send(request6, HttpResponse.BodyHandlers.ofString());
        assertEquals(202, response6.statusCode(), "Ошибка при удалении задачи");
    }

    @Test
    public void shouldHandleEpic() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/epic");
        String json = gson.toJson(epic);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request1 = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response1.statusCode(), "Неверный статус-код при создании эпика.");

        URI url2 = URI.create("http://localhost:8080/tasks/epic?id=3");
        HttpRequest request2 = HttpRequest.newBuilder().uri(url2).GET().build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        String jsonTask = response2.body();
        Epic epicReceived = gson.fromJson(jsonTask, Epic.class);
        assertEquals(200, response2.statusCode(), "Неверный статус-код при получении эпика'.");
        assertEquals(epic, epicReceived, "Эпики не совпадают.");

        URI url3 = URI.create("http://localhost:8080/tasks/epic?id=3");
        Epic updatedEpic = new Epic("EpicNewName", "Descr1", null,0);
        updatedEpic.setId(3);
        json = gson.toJson(updatedEpic);
        final HttpRequest.BodyPublisher body3 = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request3 = HttpRequest.newBuilder().uri(url3).POST(body3).build();
        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response3.statusCode(), "Неверный статус-код при обновлении эпика.");

        HttpResponse<String> response4 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        jsonTask = response4.body();
        epicReceived = gson.fromJson(jsonTask, Epic.class);
        assertEquals(200, response2.statusCode(), "Неверный статус-код при получении эпика'.");
        assertEquals(updatedEpic, epicReceived, "Эпики не совпадают.");

        HttpRequest request5 = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response5 = client.send(request5, HttpResponse.BodyHandlers.ofString());
        String jsonEpics = response5.body();
        List<Epic> epics = gson.fromJson(jsonEpics, List.class);
        assertEquals(200, response5.statusCode(), "Неверный статус-код при получении всех эпиков.");
        assertEquals(epics.size(), List.of(updatedEpic).size(), "Неверное количество эпиков.");

        HttpRequest request6 = HttpRequest.newBuilder().uri(url2).DELETE().build();
        HttpResponse<String> response6 = client.send(request6, HttpResponse.BodyHandlers.ofString());
        assertEquals(202, response6.statusCode(), "Ошибка при удалении эпика");
    }

    @Test
    public void shouldHandleSubtask() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/epic");
        String json = gson.toJson(epic);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request1 = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        URI url2 = URI.create("http://localhost:8080/tasks/subtask");
        String json2 = gson.toJson(subtask);
        final HttpRequest.BodyPublisher body2 = HttpRequest.BodyPublishers.ofString(json2);
        HttpRequest request2 = HttpRequest.newBuilder().uri(url2).POST(body2).build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response2.statusCode(), "Неверный статус-код при создании подзадачи.");

        URI url3 = URI.create("http://localhost:8080/tasks/subtask?id=4");
        HttpRequest request3 = HttpRequest.newBuilder().uri(url3).GET().build();
        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        String jsonTask = response3.body();
        Subtask subtaskReceived = gson.fromJson(jsonTask, Subtask.class);
        assertEquals(200, response3.statusCode(), "Неверный статус-код при получении подзадачи.");
        assertEquals(subtask, subtaskReceived, "Подзадачи не совпадают.");

        Subtask updatedSubtask = new Subtask("Subtask", "Descr", Status.IN_PROGRESS,
                LocalDateTime.of(2022,8,20,15,30), 120, 3);
        updatedSubtask.setId(4);
        json2 = gson.toJson(updatedSubtask);
        final HttpRequest.BodyPublisher body4 = HttpRequest.BodyPublishers.ofString(json2);
        HttpRequest request4 = HttpRequest.newBuilder().uri(url3).POST(body4).build();
        HttpResponse<String> response4 = client.send(request4, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response4.statusCode(), "Неверный статус-код при обновлении подзадачи.");

        HttpResponse<String> response5 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        jsonTask = response5.body();
        subtaskReceived = gson.fromJson(jsonTask, Subtask.class);
        assertEquals(200, response5.statusCode(), "Неверный статус-код при получении подзадачи.");
        assertEquals(updatedSubtask, subtaskReceived, "Подзадачи не совпадают.");

        HttpRequest request6 = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response6 = client.send(request6, HttpResponse.BodyHandlers.ofString());
        String jsonEpics = response6.body();
        List<Subtask> subtasks = gson.fromJson(jsonEpics, List.class);
        assertEquals(200, response6.statusCode(), "Неверный статус-код при получении всех подзадач.");
        assertEquals(subtasks.size(), List.of(updatedSubtask).size(), "Неверное количество подзадач.");

        HttpRequest request7 = HttpRequest.newBuilder().uri(url3).DELETE().build();
        HttpResponse<String> response7 = client.send(request7, HttpResponse.BodyHandlers.ofString());
        assertEquals(202, response7.statusCode(), "Ошибка при удалении подзадачи");
    }

    @Test
    public void shouldDeleteAllTaskTypes() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/all");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(202, response.statusCode(), "Ошибка при удалении всех задач");
    }

    @Test
    public void shouldGetHistoryAndPriorities() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/task");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(gson.toJson(task1));
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        URI url2 = URI.create("http://localhost:8080/tasks/task?id=1");
        HttpRequest request2 = HttpRequest.newBuilder().uri(url2).GET().build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());

        URI url3 = URI.create("http://localhost:8080/tasks/history");
        HttpRequest request3 = HttpRequest.newBuilder().uri(url3).GET().build();
        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        String json = response3.body();
        List<Task> history = gson.fromJson(json, List.class);
        assertEquals(200, response3.statusCode(), "Неверный статус-код при получении истории.");
        assertEquals(history.size(), List.of(task1).size(), "Неверное количество задач в истории.");

        URI url4 = URI.create("http://localhost:8080/tasks/priorities");
        HttpRequest request4 = HttpRequest.newBuilder().uri(url4).GET().build();
        HttpResponse<String> response4 = client.send(request4, HttpResponse.BodyHandlers.ofString());
        String jsonP = response4.body();
        List<Task> priorities = gson.fromJson(jsonP, List.class);
        assertEquals(200, response4.statusCode(), "Неверный статус-код при получении приоритетов.");
        assertEquals(priorities.size(), List.of(task1).size(), "Неверное количество задач в списке приоритетов.");
    }
}
