package space.unai.socket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Servidor {

    public static void main(String[] args) {
        Server radar = new Server();
        radar.start();
    }
}

class Server extends Thread {
    private ServerSocket ssk;
    private File file, file_passed;

    public Server() {
        this.file = new File("registry.txt"); // Archivo donde se registra la información de los vehículos
        this.file_passed = new File("passed_cars.txt");
    }

    @Override
    public void run() {
        System.out.println("[!] Servidor RADAR en funcionamiento.");
        try {
            ssk = new ServerSocket(9000); // Socket del servidor en el puerto 9000

            while (true) {
                Socket sk = ssk.accept(); // Espera a que un cliente se conecte
                handleClient(sk); // Procesa los datos del cliente conectado
            }
        } catch (IOException e) {
            System.out.println("[!] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private synchronized void handleClient(Socket sk) {
        try (DataInputStream in = new DataInputStream(sk.getInputStream())) {
            int SIZE = in.readInt(); // Lee el tamaño de los datos recibidos
            HashMap<String, Long> data = new HashMap<>(); // Almacena la matrícula y el tiempo de cada vehículo

            for (int i = 0; i < SIZE; i++) {
                String key = in.readUTF(); // Lee la matrícula del vehículo
                long value = in.readLong(); // Lee el tiempo del vehículo
                data.put(key, value); // Almacena la matrícula y el tiempo en el HashMap
            }

            readDataOfRegistry(data); // Procesa los datos del vehículo
            System.out.println("[!] La infracción ha sido registrada!");
        } catch (IOException e) {
            System.out.println("[!] ERROR: " + e.getMessage());
        }
    }

    public HashMap<String, Long> registryToHashMap() {
        HashMap<String, Long> hashMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                for (int i = 0; i < parts.length; i++) {
                    System.out.println(parts[i]);
                }
                hashMap.put(parts[1], Long.valueOf(parts[2]));
            }
        } catch (IOException ex) {
            System.out.println("[!] ERROR: " + ex.getMessage());
        }
        return hashMap;
    }

    private void readDataOfRegistry(HashMap<String, Long> data) {
        for (Map.Entry<String, Long> entry : data.entrySet()) {
            String licensePlate = entry.getKey(); // Matrícula del vehículo
            long entryTime = entry.getValue(); // Tiempo de entrada del vehículo

            boolean found = false; // Indica si el vehículo ya ha pasado por un punto de control

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(";");
                    if (parts.length >= 2) {
                        if (parts[1].equals(licensePlate)) {
                            found = true; // El vehículo ya ha pasado
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("[!] ERROR: " + e.getMessage());
            }

            if (found) {
                // El vehículo ya ha pasado, se escribe en passed_cars.txt
                try (BufferedWriter passedCarsWriter = new BufferedWriter(new FileWriter(file_passed, true))) {
                    HashMap<String, Long> hm = registryToHashMap();
                    long totalTime = System.currentTimeMillis() - hm.get(licensePlate);
                    passedCarsWriter.write(licensePlate + ";" + totalTime);
                    passedCarsWriter.newLine();
                    passedCarsWriter.flush();
                } catch (IOException e) {
                    System.out.println("[!] ERROR: " + e.getMessage());
                }
            } else {
                // El vehículo no ha pasado aún, se añade una nueva línea a registry.txt
                synchronized (this) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                        int n = 0;
                        n++;  // Incrementa el contador
                        writer.write(n + ";" + licensePlate + ";" + (entryTime));
                        writer.newLine();
                        writer.flush();
                    } catch (IOException e) {
                        System.out.println("[!] ERROR: " + e.getMessage());
                    }
                }
            }
        }
    }
}
