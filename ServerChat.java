package servidor.chat;

import javax.swing.*; //clases y métodos para crear interfaces gráficas de usuario 
import java.awt.*;   //Proporciona clases y métodos para dibujar gráficos y administrar componentes de la interfaz.
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;  // Esta clase proporciona métodos estáticos para obtener y manipular direcciones IP y nombres de host. 
import java.net.ServerSocket;//Estas clases se utilizan para implementar la comunicación de red en Java.
import java.net.Socket; //representa un socket de cliente que puede conectarse a un servidor.
import java.util.ArrayList; //Crea una lista
import java.util.HashMap; //Esta clase implementa una estructura de datos de mapa en Java que almacena pares clave-valor.
import java.util.List; //Esta interfaz representa una lista ordenada de elementos en Java
import java.util.Map; //: Esta interfaz representa una colección de pares clave-valor en Java

public class ServerChat extends JFrame { //JFrame crea la ventana de interfaz

    private static final int PORT = 1234;
    private static final String USER_FILE_PATH = System.getProperty("user.dir");; //system.getProperty  para obtener la ruta del directorio del usuario. 
    private List<String> listaClientes = new ArrayList<>();
    private List<String> listaMensajes = new ArrayList<>();

    private Map<String, String> users; 
    
    private JTextArea logTextArea;
    private JButton startButton;
    private JButton stopButton;


    public ServerChat() {
        String filePath = USER_FILE_PATH + "/usuarios.txt";
        File file = new File(filePath);

        if (file.exists()) {
            log("El archivo usuarios.txt ya existe.");
        } else {
            try {
                boolean created = file.createNewFile();
                if (created) {
                    log("El archivo usuarios.txt ha sido creado."); //el log verifica que se haya creado fuera del código e informa al usuario 
                } else {
                    log("No se pudo crear el archivo usuarios.txt.");
                }
            }catch (IOException e) {
                log("Ocurrió un error al crear el archivo usuarios.txt: " + e.getMessage());
            }
        }
        
        File directory = new File(USER_FILE_PATH + "/archivos"); 
        //En resumen, este fragmento de código verifica la existencia de un directorio llamado "archivos" 
        //en la ruta del directorio de trabajo actual y, si no existe, lo crea.
        if (directory.exists()) {
            log("El directorio archivos ya existe.");
        } else {
            boolean created = directory.mkdirs();
            if (created) {
                log("El directorio archivos ha sido creado.");
            } else {
                log("No se pudo crear el directorio archivos.");
            }
        }
        
        users = readUserFile();
        initializeGUI(); //inicializa la interfaz 
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() { //se muestra la interfaz 
            public void run() {
                new ServerChat(); //se convoca a la clase principal 
            }
        });
    }
    
    private void initializeGUI() {
        setTitle("Servidor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Encender servidor");
        stopButton = new JButton("Apagar servidor");
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        add(buttonPanel, BorderLayout.SOUTH);

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startServer();
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                log("Servidor encendido. Esperando conexiones por el puerto: " + PORT);
            }
        });

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopServer();
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                log("Servidor apagado.");
            }
        });
        stopButton.setEnabled(false);

        setSize(400, 300);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void startServer() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    InetAddress ipAddress = InetAddress.getLocalHost();
                    String serverIP = ipAddress.getHostAddress();
                    log("Dirección IP del servidor: " + serverIP);

                    ServerSocket serverSocket = new ServerSocket(PORT, 0, InetAddress.getByName("0.0.0.0"));
                    while (true) {
                        Socket socket = serverSocket.accept();
                        log("Cliente conectado: " + socket.getInetAddress().getHostAddress());

                        Thread clientThread = new Thread(new ClientHandler(socket));
                        clientThread.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    private void stopServer() {
        System.exit(0);
    }

    private class ClientHandler implements Runnable { //mensajes grupales, lista de usuarios, archivos 

        private Socket clientSocket;
        private BufferedReader reader;
        private PrintWriter writer;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;

            try {
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                String action = reader.readLine();
                if (action.equals("iniciarSesion")) {
                    String username = reader.readLine();
                    String password = reader.readLine();
                    log("Intento de inicio de sesión: " + username);

                    String r = checkCredentials(username, password);
                    writer.println(r);
                    if (r.equals("usuarioAutenticado")) {
                        listaClientes.add(username);
                        log("inicio de sesión: " + username + " exitoso.");
                    }
                } else if (action.equals("usuarioRegistrar")) {
                    String username = reader.readLine();
                    String password = reader.readLine();
                    if (registerUser(username, password)) {
                        writer.println("usuarioRegistrado");
                        log("Usuario registrado");
                    } else {
                        log("Usuario no registrado");
                    }
                }else if (action.equals("listaClientes")) {
                    if (listaClientes.size() > 0) {
                        writer.println("listadoClientes");
                        log("Listado de usuarios");
                        for (String cliente : listaClientes) {
                            writer.println(cliente);
                        }
                    }else{
                        writer.println("listadoClientesVacio");
                    }
                } else if (action.equals("enviarMensaje")) {
                    String mensaje = reader.readLine();
                    listaMensajes.add(mensaje);
                    writer.println("mensajeRecibido");
                    log("Mensaje recibido " + mensaje);
                } else if (action.equals("listaMensajes")) {
                    if (listaMensajes.size() > 0) {
                        writer.println("listadoMensajes");
                        log("Listado de Mensajes");
                        for ( String m : listaMensajes) {
                            System.out.println(m);
                            writer.println(m);
                        }
                    } else {
                        writer.println("listadoMensajesVacio");
                    }
                } else if (action.equals("desconectarUsuario")) {
                    String username = reader.readLine();
                    listaClientes.remove(username);
                    writer.println("desconectado");
                    log("Desconectar usuario: " + username);
                    if (listaClientes.size() == 0) {
                        listaMensajes.clear();
                    }
                } else if (action.equals("enviarArchivo")) {
                    
                    String username = reader.readLine();
                    String nombreArchivo = reader.readLine();
                    
                    DataInputStream dis = null;
                    FileOutputStream fos = null;
                    
                    dis = new DataInputStream(this.clientSocket.getInputStream());
                    
                    // String fileName = dis.readUTF();
                    File directory = new File(USER_FILE_PATH + "/archivos/" + username);
                    if (directory.exists()) {
                        log("El directorio archivos ya existe.");
                    } else {
                        boolean created = directory.mkdirs(); //mkdirs() método para crear directorios
                        if (created) {
                            log("El directorio archivos ha sido creado.");
                        } else {
                            log("No se pudo crear el directorio archivos.");
                        }
                    }
        
                    File file = new File(USER_FILE_PATH + "/archivos/" + username + "/" + nombreArchivo);

                    fos = new FileOutputStream(file);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = dis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    dis.close();
                    fos.close();
                    log("Enviar archivo: " + nombreArchivo + " al usuario " + username); 
                    //log es para verofocar lo que sucede fuera del código
                } else if (action.equals("listaArchivos")) {
                    String username = reader.readLine();
                    
                    System.out.println("Entre en el listado");
                    File directory = new File(USER_FILE_PATH + "/archivos/" + username);
                    if (directory.exists() && directory.isDirectory()) {
                        File[] files = directory.listFiles();
                        if (files != null && files.length > 0) {
                            writer.println("listadoArchivos");
                            for (File file : files) {
                                writer.println(file.getName());
                                System.out.println(file.getName());
                            }
                        } else {
                            writer.println("listadoArchivosVacio");
                            //writer se utiliza para escribir una cadena de texto seguida de un salto de línea en un flujo de salida.
                            //Permite escribir datos en un archivo o en otro tipo de salida, como una conexión de red o un flujo de bytes.
                        }
                    } else {
                        writer.println("errorDirectorio");
                    }
                } else if (action.equals("descargarArchivo")) {
                    
                    String username = reader.readLine();
                    String nombreArchivo = reader.readLine();
                    System.out.println(username + " " + nombreArchivo);
                    
                    File file = new File(USER_FILE_PATH + "/archivos/" + username + "/" + nombreArchivo);
                    FileInputStream fis = new FileInputStream(file);
                    
                    OutputStream ops = clientSocket.getOutputStream();

                    DataOutputStream dos = new DataOutputStream(ops);
                    dos.writeUTF(file.getName());

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        ops.write(buffer, 0, bytesRead);
                    }
                    
                    fis.close();
                    ops.close();
                    dos.close();
                    log("Descargando  archivo: " + nombreArchivo + " el usuario " + username);
                }
                reader.close();
                writer.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace(); 
                // e.printStackTrace() se utiliza comúnmente en los bloques catch para imprimir la traza de la pila de una excepción capturada
            }
        }

        private String checkCredentials(String username, String password) {
            String respuesta = "";
            if (users.containsKey(username)) {
                String storedPassword = users.get(username);
                if (password.equals(storedPassword)) {
                    respuesta = "usuarioAutenticado";
                    boolean contieneUsuario = listaClientes.contains(username);
                    if(contieneUsuario){
                        respuesta = "usuarioEnChat";
                    }
                } else {
                    respuesta = "usuarioContraseniaIncorrecta";
                }
            } else {
                respuesta = "usuarioNoExiste";
            }
            return respuesta;
        }
    }

    private boolean registerUser(String username, String password) {
        if (!users.containsKey(username)) {
            users.put(username, password);
            saveUserFile(); //convoca al método
            return true;
        }
        return false;
    }

    private Map<String, String> readUserFile() {
        Map<String, String> userMap = new HashMap<>();
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(USER_FILE_PATH + "\\usuarios.txt"));
            String line;
            while ((line = fileReader.readLine()) != null) {
                String[] parts = line.split(",");
                String storedUsername = parts[0].trim();
                String storedPassword = parts[1].trim();
                userMap.put(storedUsername, storedPassword);
            }
            fileReader.close();
        } catch (IOException e) {
        //IOException es una clase en Java que representa una excepción relacionada con entradas y salidas
        //de archivos, flujo de datos, un socket de red, etc.
            e.printStackTrace();
        }
        return userMap;
    }

    private void saveUserFile() { //se crea el método
        try {
            PrintWriter fileWriter = new PrintWriter(new FileWriter(USER_FILE_PATH + "\\usuarios.txt"));
            // PrintWriter es una clase en Java que proporciona métodos para imprimir 
            //representaciones formateadas de objetos en un flujo
            for (Map.Entry<String, String> entry : users.entrySet()) {
                fileWriter.println(entry.getKey() + "," + entry.getValue());
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                logTextArea.append(message + "\n");
            }
        });
    }
}