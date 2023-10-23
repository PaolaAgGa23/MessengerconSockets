package servidor.chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientChat extends JFrame implements ActionListener {//JFrame la ventana de interfaz y ActionListenercrea implementa la interfaz

    private JFrame loginFrame;
    private JTextField ipField;
    private JTextField portField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    private JFrame chatFrame;
    private JTextArea messageArea;
    private JList userList;
    private JTextField messageField;
    private JButton sendButton;
    private JButton fileButton;
    private JButton viewFilesButton;
    private JButton disconnectButton;
    private JButton privateChatButton;

    private JFrame viewFilesFrame;
    private JList fileList;
    private JButton downloadButton;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ClientChat();
            }
        });
    }

    public ClientChat() {
        loginFrame = new JFrame();
        loginFrame.setTitle("Client");
        loginFrame.setSize(300, 250);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 2));

        JLabel ipLabel = new JLabel("IP:");
        ipField = new JTextField(10);
        ipField.setText("");

        JLabel portLabel = new JLabel("Puerto:");
        portField = new JTextField(10);
        portField.setText("1234");

        JLabel usernameLabel = new JLabel("Usuario:");
        usernameField = new JTextField(10);
        usernameField.setText("");

        JLabel passwordLabel = new JLabel("Contraseña:");
        passwordField = new JPasswordField(10);
        passwordField.setText("");

        loginButton = new JButton("Conectar");
        loginButton.addActionListener(this);

        registerButton = new JButton("Registrar");
        registerButton.addActionListener(this);

        panel.add(ipLabel);
        panel.add(ipField);
        panel.add(portLabel);
        panel.add(portField);
        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);
        registerButton.setVisible(false);

        loginFrame.add(panel);
        loginFrame.setVisible(true);
    }

    public void showChatWindow() {
        chatFrame = new JFrame();
        chatFrame.setTitle("Chat Window");
        chatFrame.setSize(600, 400);
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel filePanel = new JPanel();
        fileButton = new JButton("Enviar Archivo");
        fileButton.addActionListener(this);

        viewFilesButton = new JButton("Ver Archivos Recibidos");
        viewFilesButton.addActionListener(this);

        filePanel.add(fileButton);
        filePanel.add(viewFilesButton);

        // Segunda sección: Área de mensajes enviados y lista de usuarios conectados
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new GridLayout(1, 2));

        messageArea = new JTextArea();
        messageArea.setEditable(true);
        JScrollPane messageScrollPane = new JScrollPane(messageArea);

        userList = new JList();
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane userListScrollPane = new JScrollPane(userList);

        chatPanel.add(messageScrollPane);
        chatPanel.add(userListScrollPane);
        
        //crea el area en donde podemos posicionar los botones
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        messageField = new JTextField();
        sendButton = new JButton("Enviar");
        sendButton.addActionListener(this);

        disconnectButton = new JButton("Desconectarse");
        disconnectButton.addActionListener(this);
        
        privateChatButton = new JButton("Chat Privado");
        privateChatButton.addActionListener(this);
        filePanel.add(privateChatButton);

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.add(disconnectButton, BorderLayout.SOUTH);

        panel.add(filePanel, BorderLayout.NORTH);
        panel.add(chatPanel, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        chatFrame.add(panel);
        chatFrame.setVisible(true);

        try {
            String ip = ipField.getText();
            String sPort = portField.getText();
            int port = Integer.parseInt(sPort); //Integer.parseInt() se utiliza para analizar esa cadena y convertirla en un entero.
            socket = new Socket(ip, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            writer.println("listaClientes");
            String response = reader.readLine();
            if (response != null) {
                if (response.equals("listadoClientes")) {
                    System.out.println("Hay un nuevo listado de clientes");
                    String user = "";
                    DefaultListModel modelo = new DefaultListModel();
                    while ((user = reader.readLine()) != null) {
                        modelo.addElement(user);
                    }
                    userList.setModel(modelo);
                }
            }
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            String ip = ipField.getText();
            String sPort = portField.getText();
            int port = Integer.parseInt(sPort);
            socket = new Socket(ip, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            writer.println("listaMensajes");
            System.out.println("En la lista de cliente");
            String response = reader.readLine();
            if (response != null) {
                if (response.equals("listadoMensajes")) {
                    String m = "";
                    while ((m = reader.readLine()) != null) {
                        messageArea.append(m + "\n");
                    }
                }
            }
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void showViewFilaWindow() {
        viewFilesFrame = new JFrame();
        viewFilesFrame.setTitle("Files View");
        viewFilesFrame.setSize(300, 400);
        viewFilesFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        viewFilesFrame.setLocationRelativeTo(null);

        viewFilesFrame.addWindowListener(new WindowAdapter() {
            @Override //sobreescribiendo en un super método
            public void windowClosing(WindowEvent e) {
                int option = JOptionPane.showConfirmDialog(viewFilesFrame,
                        "¿Estás seguro de que quieres cerrar la aplicación?",
                        "Confirmar cierre",
                        JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.YES_OPTION) {
                    viewFilesFrame.setVisible(false);
                    chatFrame.setEnabled(true);
                    chatFrame.setFocusable(true);
                }
            }
        });
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));

        JLabel fileViewLabel = new JLabel("Lista de Archivo:");

        fileList = new JList();
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane userListScrollPane = new JScrollPane(fileList);

        downloadButton = new JButton("Descargar");
        downloadButton.addActionListener(this);

        panel.add(fileViewLabel);
        panel.add(userListScrollPane);
        panel.add(downloadButton);

        viewFilesFrame.add(panel);
        viewFilesFrame.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        String ip = ipField.getText();
        String sPort = portField.getText();
        int port = Integer.parseInt(sPort); //Integer.parseInt() se encarga de analizar la cadena y extraer su valor entero correspondiente.
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (ip.equals("") || sPort.equals("") || username.equals("") || password.equals("")) {
            JOptionPane.showMessageDialog(this, "No deje los campos en blanco");
        } else {
            if (e.getSource() == loginButton) {
                try {
                    socket = new Socket(ip, port);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new PrintWriter(socket.getOutputStream(), true);

                    writer.println("iniciarSesion");
                    writer.println(username);
                    writer.println(password);

                    // Recibe la respuesta del servidor
                    String response = reader.readLine();
                    switch (response) {
                        case "usuarioAutenticado" -> {
                            loginFrame.setVisible(false);
                            showChatWindow();
                        }
                        case "usuarioContraseniaIncorrecta" -> {
                            JOptionPane.showMessageDialog(this, "Verifique su información el usuario Y/o contraseña incorrecta.");
                        }
                        case "usuarioNoExiste" -> {
                            JOptionPane.showMessageDialog(this, "No existe el usuario, registrelo.");
                            registerButton.setVisible(true);
                        }
                        case "usuarioEnChat" -> {
                            JOptionPane.showMessageDialog(this, "Ya esta el usuario conectado.");
                            loginFrame.setVisible(false);
                            showChatWindow();
                        }
                    }
                    socket.close();
                }catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al conectarse al servidor");
                }
            } else if (e.getSource() == registerButton) {
                try {
                    socket = new Socket(ip, port);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new PrintWriter(socket.getOutputStream(), true);

                    writer.println("usuarioRegistrar");
                    writer.println(username);
                    writer.println(password);

                    String response = reader.readLine();

                    if (response.equals("usuarioRegistrado")) {
                        JOptionPane.showMessageDialog(this, "Registro exitoso");
                        registerButton.setVisible(false);
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al registrar");
                    }

                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al conectarse al servidor");
                }
            } else if (e.getSource() == sendButton) {
                try {
                    socket = new Socket(ip, port);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new PrintWriter(socket.getOutputStream(), true);

                    // Envía la información de inicio de sesión al servidor
                    String message = messageField.getText();
                    writer.println("enviarMensaje");
                    writer.println(username + ": " + message);

                    String response = reader.readLine();
                    if (response.equals("mensajeRecibido")) {
                        messageField.setText("");
                        socket.close();

                        socket = new Socket(ip, port);
                        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        writer = new PrintWriter(socket.getOutputStream(), true);

                        writer.println("listaMensajes");
                        response = reader.readLine();
                        if (response != null) {
                            if (response.equals("listadoMensajes")) {
                                messageArea.setText("");
                                String m = "";
                                while ((m = reader.readLine()) != null) {
                                    System.out.println("::" + m);
                                    messageArea.append(m + "\n");
                                }
                            }
                        }
                        socket.close();
                    } else {
                        JOptionPane.showMessageDialog(this, "No se pudo enviar el mensaje.");
                        registerButton.setVisible(true);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al conectarse al servidor");
                    System.out.println("Error al conectarse al servidor");
                }
            } else if (e.getSource() == privateChatButton) {
            String[] args = new String[0]; // Crear un arreglo vacío de String
            Peer llamada = new Peer();
            try {
                llamada.chat(args);
            } catch (Exception ex) {
                ex.printStackTrace();
                }
            }else if (e.getSource() == disconnectButton) {
                try {
                    socket = new Socket(ip, port);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new PrintWriter(socket.getOutputStream(), true);

                    writer.println("desconectarUsuario");
                    writer.println(username);

                    String response = reader.readLine();
                    if (response.equals("desconectado")) {
                        usernameField.setText("");
                        socket.close();
                        System.exit(0);
                    } else {
                        JOptionPane.showMessageDialog(this, "No se pudo enviar el mensaje.");
                        registerButton.setVisible(true);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al conectarse al servidor");
                    System.out.println("Error al conectarse al servidor");
                }
            } else if (e.getSource() == fileButton) {
                if (userList.isSelectionEmpty()) {
                    JOptionPane.showMessageDialog(this, "Selecciona al usuario que deseas enviar el archivo");
                } else {
                    JFileChooser fileChooser = new JFileChooser();
                    int result = fileChooser.showOpenDialog(chatFrame);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        String nombreArchivo = selectedFile.getName();

                        try {
                            socket = new Socket(ip, port);
                            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            writer = new PrintWriter(socket.getOutputStream(), true);

                            writer.println("enviarArchivo");

                            writer.println(userList.getSelectedValue());
                            writer.println(nombreArchivo);

                            // Obtener el flujo de salida para enviar datos al servidor
                            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                            // Enviar nombre de archivo al servidor
                            dos.writeUTF(nombreArchivo);

                            // Leer y enviar el archivo al servidor
                            FileInputStream fis = new FileInputStream(selectedFile);
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = fis.read(buffer)) != -1) {
                                dos.write(buffer, 0, bytesRead);
                            }

                            fis.close();
                            dos.close();
                            socket.close();

//                            socket = new Socket(ip, port);
//                            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                            writer = new PrintWriter(socket.getOutputStream(), true);
//                            
//                            socket.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(this, "Error al conectarse al servidor");
                            System.out.println("Error al conectarse al servidor");
                        }
                    }
                }
            } else if (e.getSource() == viewFilesButton) {
                chatFrame.setEnabled(false);
                showViewFilaWindow();
                try {
                    socket = new Socket(ip, port);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new PrintWriter(socket.getOutputStream(), true);

                    writer.println("listaArchivos");
                    writer.println(username);

                    String response = reader.readLine();
                    if (response != null) {
                        if (response.equals("listadoArchivos")) {
                            String file = "";
                            DefaultListModel modelo = new DefaultListModel();
                            while ((file = reader.readLine()) != null) {
                                modelo.addElement(file);
                            }
                            fileList.setModel(modelo);
                        }
                    }

                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al conectarse al servidor");
                }

            } else if (e.getSource() == downloadButton) {
                if (fileList.isSelectionEmpty()) {
                    JOptionPane.showMessageDialog(this, "Selecciona el archivo que deseas descargar");
                } else {
                    try {
                        socket = new Socket(ip, port);
                        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        writer = new PrintWriter(socket.getOutputStream(), true);

                        writer.println("descargarArchivo");
                        writer.println(username);
                        writer.println(fileList.getSelectedValue());

                        InputStream inputStream = socket.getInputStream();

                        DataInputStream dataInputStream = new DataInputStream(inputStream);
                        String fileName = dataInputStream.readUTF();

                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setSelectedFile(new File(fileName)); // Establecer el nombre de archivo predeterminado
                        fileChooser.setDialogTitle("Guardar archivo");

                        int userSelection = fileChooser.showSaveDialog(null);

                        if (userSelection == JFileChooser.APPROVE_OPTION) {

                            File selectedFile = fileChooser.getSelectedFile();
                            String filePath = selectedFile.getAbsolutePath();
                            System.out.println("Guardando archivo en: " + filePath);

                            File file = new File(filePath);
                            FileOutputStream fileOutputStream = new FileOutputStream(file);

                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                fileOutputStream.write(buffer, 0, bytesRead);
                            }

                            System.out.println("Archivo guardado correctamente.");

                            fileOutputStream.close();
                            dataInputStream.close();
                            inputStream.close();
                        } else {
                            System.out.println("Descarga cancelada por el usuario.");

                            // Cerrar conexiones
                            dataInputStream.close();
                            inputStream.close();
                        }
                        socket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error al conectarse al servidor");
                        System.out.println("Error al conectarse al servidor");
                    }
                }
            }
        }
    }
    private void privateChatButtonActionPerformed(ActionEvent e) {
        String[] args = new String[0];
        Peer peer = new Peer();
        peer.setClientChat(this); // Establece la instancia de ChatWindow en el Peer
        try {
            peer.chat(args);
        } catch (Exception ex) {
            ex.printStackTrace();
            // Maneja la excepción de alguna manera apropiada, como mostrar un mensaje de error
        }
    }
}
