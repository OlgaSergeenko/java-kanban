package controllers;

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
    private final static String HTTP_GET = "GET";
    private final static String HTTP_POST = "POST";
    private final static String HTTP_DELETE = "DELETE";
    private final static String HTTP_TYPE_TASK = "task";
    private final static String HTTP_TYPE_SUBTASK = "subtask";
    private final static String HTTP_TYPE_EPIC = "epic";
    private final static String HTTP_TYPE_HISTORY = "history";
    private final static String HTTP_TYPE_PRIORITIES = "priorities";
    private final static String HTTP_TYPE_ALL_TASKS = "all";
    private final TaskManager taskManager;
    private final HttpServer httpServer;
    private final Gson gson;

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
        httpServer.createContext("/tasks/all", new TasksHandler());
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
          String requestTaskType = getRequestTaskType(exchange);
          String rawQuery = exchange.getRequestURI().getRawQuery();
          switch (method) {
              case HTTP_GET:
                  if (rawQuery == null) {
                      handleGetAllTasksRequest(requestTaskType, exchange, outputStream);
                  } else {
                      int requestTaskId = getRequestTaskId(rawQuery);
                      handleGetByIdRequest(requestTaskId, requestTaskType, exchange, outputStream);
                  }
                  break;
              case HTTP_POST:
                  if (rawQuery == null) {
                      handlePostNewTaskRequest(requestTaskType, exchange, inputStream);
                  } else {
                      int requestTaskId = getRequestTaskId(rawQuery);
                      handlePostUpdatedTaskRequest(requestTaskId, requestTaskType, exchange, inputStream);
                  }
                  break;
              case HTTP_DELETE:
                  if (rawQuery == null) {
                      handleDeleteAllTasksRequest(requestTaskType, exchange);
                  } else {
                      int requestTaskId = getRequestTaskId(rawQuery);
                      handleDeleteTaskByIdRequest(requestTaskId, requestTaskType, exchange);
                  }
                  break;
              default:
                  System.out.println("Запрос не обработан");
          }
          outputStream.close();
          inputStream.close();
      }

      private String getRequestTaskType(HttpExchange exchange){
          String[] pathParts = exchange.getRequestURI().getPath().split("/");
          return pathParts[pathParts.length - 1];
      }
      private int getRequestTaskId(String rawQuery){
          String[] queryParts = rawQuery.split("=");
          return Integer.parseInt(queryParts[1]);
      }

      private void handleGetAllTasksRequest(
              String requestTaskType,
              HttpExchange exchange,
              OutputStream outputStream) throws IOException {
          switch (requestTaskType) {
              case HTTP_TYPE_TASK:
                  httpGetTasks(exchange, outputStream);
                  break;
              case HTTP_TYPE_SUBTASK:
                  httpGetSubtasks(exchange, outputStream);
                  break;
              case HTTP_TYPE_EPIC:
                  httpGetEpics(exchange, outputStream);
                  break;
              case HTTP_TYPE_HISTORY:
                  httpGetHistory(exchange, outputStream);
                  break;
              case HTTP_TYPE_PRIORITIES:
                  httpGetPriorities(exchange, outputStream);
                  break;
              default:
                  String response = "Неизвестный запрос. Проверьте URL.";
                  exchange.sendResponseHeaders(404, 0);
                  try (OutputStream os = exchange.getResponseBody()) {
                      os.write(response.getBytes());
                  }
          }
      }

      private void handleGetByIdRequest(
              int requestTaskId,
              String requestTaskType,
              HttpExchange exchange,
              OutputStream outputStream) throws IOException { //("http://localhost:8080/tasks/task/id=1")
          switch (requestTaskType) {
              case HTTP_TYPE_TASK:
                  httpGetTaskById(requestTaskId, exchange, outputStream);
                  break;
              case HTTP_TYPE_SUBTASK:
                  httpGetSubtaskById(requestTaskId, exchange, outputStream);
                  break;
              case HTTP_TYPE_EPIC:
                  httpGetEpicById(requestTaskId, exchange, outputStream);
                  break;
              default:
                  String response = "Неизвестный запрос. Проверьте URL.";
                  exchange.sendResponseHeaders(404, 0);
                  try (OutputStream os = exchange.getResponseBody()) {
                      os.write(response.getBytes());
                  }
          }
      }

      private void handlePostNewTaskRequest
              (String requestTaskType,
               HttpExchange exchange,
               InputStream inputStream) throws IOException {
          switch (requestTaskType) {
              case HTTP_TYPE_TASK:
                  httpPostNewTask(exchange, inputStream);
                  break;
              case HTTP_TYPE_SUBTASK:
                  httpPostNewSubtask(exchange, inputStream);
                  break;
              case HTTP_TYPE_EPIC:
                  httpPostNewEpic(exchange, inputStream);
                  break;
              default:
                  String response = "Неизвестный запрос. Проверьте URL.";
                  exchange.sendResponseHeaders(404, 0);
                  try (OutputStream os = exchange.getResponseBody()) {
                      os.write(response.getBytes());
                  }
          }
      }


      private void handlePostUpdatedTaskRequest(
              int requestTaskId,
              String requestTaskType,
              HttpExchange exchange,
              InputStream inputStream) throws IOException {
        switch (requestTaskType) {
            case HTTP_TYPE_TASK:
                httpUpdateTask(requestTaskId, exchange, inputStream);
                break;
            case HTTP_TYPE_SUBTASK:
                httpUpdateSubtask(requestTaskId, exchange, inputStream);
                break;
            case HTTP_TYPE_EPIC:
                httpUpdateEpic(requestTaskId, exchange, inputStream);
                break;
            default:
                String response = "Неизвестный запрос. Проверьте URL.";
                exchange.sendResponseHeaders(404, 0);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
        }
      }

      private void handleDeleteAllTasksRequest(
              String requestTaskType,
              HttpExchange exchange) throws IOException {
          switch (requestTaskType) {
              case HTTP_TYPE_TASK:
                  httpDeleteTasks(exchange);
                  break;
              case HTTP_TYPE_SUBTASK:
                  httpDeleteSubtasks(exchange);
                  break;
              case HTTP_TYPE_EPIC:
                  httpDeleteEpics(exchange);
                  break;
              case HTTP_TYPE_ALL_TASKS:
                  httpDeleteAllTaskTypes(exchange);
                  break;
              default:
                  String response = "Неизвестный запрос. Проверьте URL.";
                  exchange.sendResponseHeaders(404, 0);
                  try (OutputStream os = exchange.getResponseBody()) {
                      os.write(response.getBytes());
                  }
          }
      }

      private void handleDeleteTaskByIdRequest(
              int id,
              String requestTaskType,
              HttpExchange exchange) throws IOException {
          switch (requestTaskType) {
              case HTTP_TYPE_TASK:
                  httpDeleteTaskById(id, exchange);
                  break;
              case HTTP_TYPE_SUBTASK:
                  httpDeleteSubtaskById(id, exchange);
                  break;
              case HTTP_TYPE_EPIC:
                  httpDeleteEpicById(id, exchange);
                  break;
              default:
                  String response = "Неизвестный запрос. Проверьте URL.";
                  exchange.sendResponseHeaders(404, 0);
                  try (OutputStream os = exchange.getResponseBody()) {
                      os.write(response.getBytes());
                  }
          }
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

      private void httpDeleteEpicById(int id, HttpExchange exchange) throws IOException {
          try {
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

      private void httpDeleteSubtaskById(int id, HttpExchange exchange) throws IOException {
          try {
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

      private void httpDeleteTaskById(int id, HttpExchange exchange) throws IOException {
          try {
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

      private void httpUpdateEpic(int id, HttpExchange exchange, InputStream inputStream) throws IOException {
          try {
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

      private void httpUpdateSubtask(int id, HttpExchange exchange, InputStream inputStream) throws IOException {
          try {
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

      private void httpUpdateTask(int id, HttpExchange exchange, InputStream inputStream) throws IOException {
          try {
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

      private void httpGetTaskById(int id, HttpExchange exchange, OutputStream outputStream) throws IOException {
          try {
              String jsonTask = gson.toJson(taskManager.findTaskById(id));
              exchange.sendResponseHeaders(200, 0);
              outputStream = exchange.getResponseBody();
              outputStream.write(jsonTask.getBytes(DEFAULT_CHARSET));
          } catch (TaskManagerException e) {
              System.out.println(e.getMessage());
          }
      }

      private void httpGetSubtaskById(int id, HttpExchange exchange, OutputStream outputStream) throws IOException {
          try {
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

      private void httpGetEpicById(int id, HttpExchange exchange, OutputStream outputStream) throws IOException {
          try{
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
