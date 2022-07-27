package HttpManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import exceptions.TaskManagerException;
import exceptions.TaskTimeValidationException;
import manager.Managers;
import manager.TaskManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;

import static jdk.internal.util.xml.XMLStreamWriter.DEFAULT_CHARSET;

public class HttpTaskServer {
    private static final int PORT = 8080;
    TaskManager taskManager;
    HttpServer httpServer;
    Gson gson;

    public HttpTaskServer() throws IOException {
        this.taskManager = Managers.getDefault(new URL("http://localhost:8078"));
        this.httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        httpServer.createContext("/tasks/task", new TasksHandler());
        httpServer.createContext("/tasks/subtask", new TasksHandler());
        httpServer.createContext("/tasks/epic", new TasksHandler());
        httpServer.createContext("/tasks/history", new TasksHandler());
        httpServer.createContext("/tasks/priorities", new TasksHandler());
    }

    public void start() {
        httpServer.start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }

    public void stop() {
        System.out.println("Остановка сервера на порту " + PORT);
        httpServer.stop(0);
    }

  class TasksHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
          OutputStream outputStream = exchange.getResponseBody();
          InputStream inputStream = exchange.getRequestBody();
          String method = exchange.getRequestMethod();
          String[] pathParts = exchange.getRequestURI().getPath().split("/");

          switch (method) {
              case "GET":
                  if (pathParts[pathParts.length - 1].equals("task")) {
                      httpGetTasks(exchange, outputStream);
                  } if (exchange.getRequestURI().getPath().contains("/task/id=")) {
                      httpGetTaskById(exchange, outputStream);
                  } if (pathParts[pathParts.length - 1].equals("subtask")) {
                      httpGetSubtasks(exchange, outputStream);
                  } if (exchange.getRequestURI().getPath().contains("subtask/id=")) {
                      httpGetSubtaskById(exchange, outputStream);
                  } if (pathParts[pathParts.length - 1].equals("epic")) {
                      httpGetEpics(exchange, outputStream);
                  } if (exchange.getRequestURI().getPath().contains("epic/id=")) {
                      httpGetEpicById(exchange, outputStream);
                  } if (pathParts[pathParts.length - 1].equals("history")) {
                      httpGetHistory(exchange, outputStream);
                  } if (pathParts[pathParts.length - 1].equals("priorities")) {
                      httpGetPriorities(exchange, outputStream);
                  }
                  break;
              case "POST":
                  if (pathParts[pathParts.length - 1].equals("task")) {
                      httpPostNewTask(exchange, inputStream);
                  } if (exchange.getRequestURI().getPath().contains("/task/id=")) {
                      httpUpdateTask(exchange, inputStream);
                  } if (pathParts[pathParts.length - 1].equals("subtask")) {
                      httpPostNewSubtask(exchange, inputStream);
                  } if (exchange.getRequestURI().getPath().contains("subtask/id=")) {
                      httpUpdateSubtask(exchange, inputStream);
                  } if (pathParts[pathParts.length - 1].equals("epic")) {
                      httpPostNewEpic(exchange, inputStream);
                  } if (exchange.getRequestURI().getPath().contains("epic/id=")) {
                      httpUpdateEpic(exchange, inputStream);
                  }
                  break;
              case "DELETE":
                  if (pathParts[pathParts.length - 1].equals("task")) {
                      httpDeleteTasks(exchange);
                  } if (exchange.getRequestURI().getPath().contains("/task/id=")) {
                      httpDeleteTaskById(exchange);
                  } if (pathParts[pathParts.length - 1].equals("subtask")) {
                      httpDeleteSubtasks(exchange);
                  } if (exchange.getRequestURI().getPath().contains("subtask/id=")) {
                      httpDeleteSubtaskById(exchange);
                  } if (pathParts[pathParts.length - 1].equals("epic")) {
                      httpDeleteEpics(exchange);
                  } if (exchange.getRequestURI().getPath().contains("epic/id=")) {
                      httpDeleteEpicById(exchange);
                  } if (pathParts[pathParts.length - 1].equals("all")) {
                      httpDeleteAllTaskTypes(exchange);
                  }
                  break;
              default:
                  System.out.println("Запрос не обработан");
          }
          outputStream.close();
          inputStream.close();
      }

      private void httpGetPriorities(HttpExchange exchange, OutputStream outputStream) throws IOException {
          String history = gson.toJson(taskManager.getPrioritizedTasks());
          exchange.sendResponseHeaders(200, 0);
          outputStream = exchange.getResponseBody();
          outputStream.write(history.getBytes(DEFAULT_CHARSET));
      }

      private void httpGetHistory(HttpExchange exchange, OutputStream outputStream) throws IOException {
          try {
              String history = gson.toJson(taskManager.getHistory());
              exchange.sendResponseHeaders(200, 0);
              outputStream = exchange.getResponseBody();
              outputStream.write(history.getBytes(DEFAULT_CHARSET));
          } catch (TaskManagerException e) {
              System.out.println(e.getMessage());
          }
      }

      private void httpDeleteAllTaskTypes(HttpExchange exchange) throws IOException {
          taskManager.deleteAllTaskTypes();
          String response = "Все задачи, эпики и подзадачи удалены.";
          exchange.sendResponseHeaders(202, 0);
          try (OutputStream os = exchange.getResponseBody()) {
              os.write(response.getBytes());
          }
      }

      private void httpDeleteEpicById(HttpExchange exchange) throws IOException {
          try {
              String[] uriParts = exchange.getRequestURI().getPath().split("=");
              int id = Integer.parseInt(uriParts[1]);
              taskManager.deleteEpicById(id);
              String response = "Эпик номер " + id + " и входящие в него подзадачи удалены.";
              exchange.sendResponseHeaders(202, 0);
              try (OutputStream os = exchange.getResponseBody()) {
                  os.write(response.getBytes());
              }
          } catch (TaskManagerException e) {
              System.out.println(e.getMessage());
          }
      }

      private void httpDeleteEpics(HttpExchange exchange) throws IOException {
          taskManager.deleteAllEpics();
          String response = "Эпики и подзадачи удалены.";
          exchange.sendResponseHeaders(202, 0);
          try (OutputStream os = exchange.getResponseBody()) {
              os.write(response.getBytes());
          }
      }

      private void httpDeleteSubtaskById(HttpExchange exchange) throws IOException {
          try {
              String[] uriParts = exchange.getRequestURI().getPath().split("=");
              int id = Integer.parseInt(uriParts[1]);
              taskManager.deleteSubtaskById(id);
              String response = "Подзадача номер " + id + " удалена.";
              exchange.sendResponseHeaders(202, 0);
              try (OutputStream os = exchange.getResponseBody()) {
                  os.write(response.getBytes());
              }
          } catch (TaskManagerException e) {
              System.out.println(e.getMessage());
          }
      }

      private void httpDeleteSubtasks(HttpExchange exchange) throws IOException {
          try {
              taskManager.deleteAllSubtasks();
              String response = "Подзадачи удалены.";
              exchange.sendResponseHeaders(202, 0);
              try (OutputStream os = exchange.getResponseBody()) {
                  os.write(response.getBytes());
              }
          } catch (TaskManagerException e) {
              System.out.println(e.getMessage());
          }
      }

      private void httpDeleteTaskById(HttpExchange exchange) throws IOException {
          try {
              String[] uriParts = exchange.getRequestURI().getPath().split("=");
              int id = Integer.parseInt(uriParts[1]);
              taskManager.deleteTaskById(id);
              String response = "Задача номер " + id + " удалена.";
              exchange.sendResponseHeaders(202, 0);
              try (OutputStream os = exchange.getResponseBody()) {
                  os.write(response.getBytes());
              }
          } catch (TaskManagerException e) {
          System.out.println(e.getMessage());
          }
      }

      private void httpDeleteTasks(HttpExchange exchange) throws IOException {
          taskManager.deleteAllTasks();
          String response = "Задачи удалены.";
          exchange.sendResponseHeaders(202, 0);
          try (OutputStream os = exchange.getResponseBody()) {
              os.write(response.getBytes());
          }
      }

      private void httpPostNewEpic(HttpExchange exchange, InputStream inputStream) throws IOException {
          String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
          Epic epic = gson.fromJson(body, Epic.class);
          taskManager.createEpic(epic);
          String response = "Эпик успешно создан";
          exchange.sendResponseHeaders(201, 0);
          try (OutputStream os = exchange.getResponseBody()) {
              os.write(response.getBytes());
          }
      }

      private void httpUpdateEpic(HttpExchange exchange, InputStream inputStream) throws IOException {
          try {
              String[] uriParts = exchange.getRequestURI().getPath().split("=");
              int id = Integer.parseInt(uriParts[1]);
              String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
              Epic epic = gson.fromJson(body, Epic.class);
              epic.setId(id);
              taskManager.updateEpic(epic);
              String response = "Эпик успешно обновлен";
              exchange.sendResponseHeaders(201, 0);
              try (OutputStream os = exchange.getResponseBody()) {
                  os.write(response.getBytes());
              }
          } catch (TaskManagerException e) {
              System.out.println(e.getMessage());
          }
      }

      private void httpUpdateSubtask(HttpExchange exchange, InputStream inputStream) throws IOException {
          try {
              String[] uriParts = exchange.getRequestURI().getPath().split("=");
              int id = Integer.parseInt(uriParts[1]);
              String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
              Subtask subtask = gson.fromJson(body, Subtask.class);
              subtask.setId(id);
              taskManager.updateSubtask(subtask);
              String response = "Подзадача успешно обновлена";
              exchange.sendResponseHeaders(201, 0);
              try (OutputStream os = exchange.getResponseBody()) {
                  os.write(response.getBytes());
              }
          } catch (TaskManagerException | TaskTimeValidationException e) {
              System.out.println(e.getMessage());
          }
      }

      private void httpPostNewSubtask(HttpExchange exchange, InputStream inputStream) throws IOException {
          try {
              String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
              Subtask subtask = gson.fromJson(body, Subtask.class);
              taskManager.createSubtask(subtask);
              String response = "Подзадача успешно создана";
              exchange.sendResponseHeaders(201, 0);
              try (OutputStream os = exchange.getResponseBody()) {
                  os.write(response.getBytes());
              }
          } catch (TaskManagerException | TaskTimeValidationException e) {
              System.out.println(e.getMessage());
          }
      }

      private void httpPostNewTask(HttpExchange exchange, InputStream inputStream) throws IOException {
          try {
              String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
              Task task = gson.fromJson(body, Task.class);
              taskManager.createTask(task);
              String response = "Задача успешно создана";
              exchange.sendResponseHeaders(201, 0);
              try (OutputStream os = exchange.getResponseBody()) {
                  os.write(response.getBytes());
              }
          } catch (TaskTimeValidationException e) {
              System.out.println(e.getMessage());
          }
      }

      private void httpUpdateTask(HttpExchange exchange, InputStream inputStream) throws IOException {
          try {
              String[] uriParts = exchange.getRequestURI().getPath().split("=");
              int id = Integer.parseInt(uriParts[1]);
              String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
              Task task = gson.fromJson(body, Task.class);
              task.setId(id);
              taskManager.updateTask(task);
              String response = "Задача успешно обновлена";
              exchange.sendResponseHeaders(201, 0);
              try (OutputStream os = exchange.getResponseBody()) {
                  os.write(response.getBytes());
              }
          } catch (TaskManagerException e) {
              System.out.println(e.getMessage());
          }
      }

      private void httpGetTasks(HttpExchange exchange, OutputStream outputStream) throws IOException {
          String jsonTask = gson.toJson(taskManager.getTasks());
          exchange.sendResponseHeaders(200, 0);
          outputStream = exchange.getResponseBody();
          outputStream.write(jsonTask.getBytes(DEFAULT_CHARSET));
      }

      private void httpGetSubtasks(HttpExchange exchange, OutputStream outputStream) throws IOException {
          String jsonTask = gson.toJson(taskManager.getSubtasks());
          exchange.sendResponseHeaders(200, 0);
          outputStream.write(jsonTask.getBytes(DEFAULT_CHARSET));
      }

      private void httpGetTaskById(HttpExchange exchange, OutputStream outputStream) throws IOException {
          try {
              String[] uriParts = exchange.getRequestURI().getPath().split("=");
              int id = Integer.parseInt(uriParts[1]);
              String jsonTask = gson.toJson(taskManager.findTaskById(id));
              exchange.sendResponseHeaders(200, 0);
              outputStream = exchange.getResponseBody();
              outputStream.write(jsonTask.getBytes(DEFAULT_CHARSET));
          } catch (TaskManagerException e) {
              System.out.println(e.getMessage());
          }
      }

      private void httpGetSubtaskById(HttpExchange exchange, OutputStream outputStream) throws IOException {
          try {
              String[] uriParts = exchange.getRequestURI().getPath().split("=");
              int id = Integer.parseInt(uriParts[1]);
              String jsonTask = gson.toJson(taskManager.findSubtaskById(id));
              exchange.sendResponseHeaders(200, 0);
              outputStream = exchange.getResponseBody();
              outputStream.write(jsonTask.getBytes(DEFAULT_CHARSET));
          } catch (TaskManagerException e) {
              System.out.println(e.getMessage());
          }
      }

      private void httpGetEpics(HttpExchange exchange, OutputStream outputStream) throws IOException {
          String jsonTask = gson.toJson(taskManager.getEpics());
          exchange.sendResponseHeaders(200, 0);
          outputStream = exchange.getResponseBody();
          outputStream.write(jsonTask.getBytes(DEFAULT_CHARSET));
      }

      private void httpGetEpicById(HttpExchange exchange, OutputStream outputStream) throws IOException {
          try{
              String[] uriParts = exchange.getRequestURI().getPath().split("=");
              int id = Integer.parseInt(uriParts[1]);
              String jsonTask = gson.toJson(taskManager.findEpicById(id));
              exchange.sendResponseHeaders(200, 0);
              outputStream = exchange.getResponseBody();
              outputStream.write(jsonTask.getBytes(DEFAULT_CHARSET));
          } catch (TaskManagerException e) {
              System.out.println(e.getMessage());
          }
      }
  }
}
