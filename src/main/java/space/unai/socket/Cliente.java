package space.unai.socket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Cliente extends Thread {

    private static final Object lock = new Object(); // Objeto de bloqueo para sincronización
    private static Socket sk; // Socket para la conexión
    private static Scanner sc; // Scanner para entrada de usuario

    public static void main(String[] args) {
        synchronized (lock) {
            try {
                sk = new Socket("127.0.0.1", 9000); // Se establece la conexión con el servidor en localhost (127.0.0.1) y puerto 9000
                sc = new Scanner(System.in); // Scanner para leer entrada del usuario

                DataOutputStream out = new DataOutputStream(sk.getOutputStream());

                System.out.println("Matricula del coche:");
                String matricula = sc.next(); // Se pide al usuario que ingrese la matrícula del coche
                long timeStamp = System.currentTimeMillis(); // Se obtiene el tiempo actual en milisegundos

                HashMap<String, Long> hm = new HashMap<>(); // Se crea un HashMap para almacenar la matrícula y el tiempo
                hm.put(matricula, timeStamp); // Se agrega la matrícula y el tiempo al HashMap

                serializeHashMap(hm, out); // Se serializa el HashMap y se envía al servidor

                System.out.println("[!] HASHMAP ENVIADO"); // Se imprime un mensaje indicando que el HashMap ha sido enviado
            } catch (IOException e) {
                throw new RuntimeException(e); // Se lanza una excepción en caso de error de E/S
            }
        }
    }

    public static void serializeHashMap(HashMap<String, Long> hashMap, DataOutputStream dataOutputStream) {
        synchronized (lock) {
            try {
                dataOutputStream.writeInt(hashMap.size());  // Se escribe el tamaño del HashMap en el flujo de salida
                for (Map.Entry<String, Long> entry : hashMap.entrySet()) {
                    dataOutputStream.writeUTF(entry.getKey()); // Se escribe la clave (matrícula) en el flujo de salida
                    dataOutputStream.writeLong(entry.getValue()); // Se escribe el valor (tiempo) en el flujo de salida
                }
            } catch (IOException e) {
                throw new RuntimeException(e); // Se lanza una excepción en caso de error de E/S
            }
        }
    }
}
