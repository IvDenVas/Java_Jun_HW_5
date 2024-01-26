package ru.geekbrains.junior.chat.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientManager implements Runnable {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String name;
    public static ArrayList<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket) {
        try {
            this.socket = socket;
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            name = bufferedReader.readLine();
            clients.add(this);
            System.out.println(name + " подключился к чату.");
            broadcastMessage("Server: " + name + " подключился к чату.");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Удаление клиента из коллекции
     */
    private void removeClient() {
        clients.remove(this);
        System.out.println(name + " покинул чат.");
        broadcastMessage("Server: " + name + " покинул чат.");

    }

    @Override
    public void run() {
        String massageFromClient;

        while (socket.isConnected()) {
            try {
                // Чтение данных
                massageFromClient = bufferedReader.readLine();
                if (massageFromClient == null) {
                    // для  macOS
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }
                // Отправка данных всем слушателям
//                broadcastMessage(massageFromClient);
                // Выбор метода по составу сообщения
                if(massageFromClient.contains("-->")) privateMessage(massageFromClient);
                else if (massageFromClient.contains("@all@")) allClientOnline(massageFromClient);
                else broadcastMessage(name + ": " + massageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }
    /**
     * Отправка сообщения всем слушателям
     *
     * @param message сообщение
     */
    private void broadcastMessage(String message) {
        for (ClientManager client : clients) {
            try {
                if (!client.name.equals(name) && message != null) {
                    client.bufferedWriter.write(message);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                }
            }
            catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    //Отправка приватного сообщения конкретному клиенту
    private void privateMessage(String message) {
//        String[] tmp = message.split(": ");
//        String privateMsg = message.split(": ")[1];
        if (message.split("-->").length == 2) {
            String[] tmpResult = message.split("-->");
            String nameString = tmpResult[0];
            String messageString = tmpResult[1];
            for (ClientManager client : clients) {
                try {
                    if (!client.name.equals(name) && client.name.equals(nameString)) {
                        client.bufferedWriter.write(name + " !private message!: " + messageString);
                        client.bufferedWriter.newLine();
                        client.bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        } else{//чтобы не лег сервер по IndexOutOfBoundsException во время split("-->")
            for (ClientManager client : clients) {
                try {
                    if (client.name.equals(name) && client.name.equals(message.split("-->")[0])) {
                        client.bufferedWriter.write("Server: Отсутствует сообщение после -->! Попробуй еще!");
                        client.bufferedWriter.newLine();
                        client.bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }

    }

    //Отправка списка онлайн клиентов запрашивающему
    private void allClientOnline(String message) {
        StringBuilder res = new StringBuilder();
        for (ClientManager client : clients) {
            res.append(client.name);
            res.append(", ");
        }
        res.deleteCharAt(res.length() - 2);

        for (ClientManager client : clients) {
            try {
                if (client.name.equals(name) && message.equals("@all@")) {
                    client.bufferedWriter.write("В сети: " + String.valueOf(res));
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        // Удаление клиента из коллекции
        removeClient();
        try {
            // Завершаем работу буфера на чтение данных
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            // Завершаем работу буфера для записи данных
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            // Закрытие соединения с клиентским сокетом
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
