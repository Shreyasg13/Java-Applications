package chat;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.io.DataOutputStream;



public class ChatClient extends JFrame {
	private static final long serialVersionUID = 1L;
	DataOutputStream toServer = null;
	DataInputStream fromServer = null;
	JTextField Window = new JTextField();;
	JTextArea textArea = new JTextArea();
	Socket socket = null;
	boolean exit=true;
	listenMessage message;
	int count=0;

	public ChatClient() {
		add(new JScrollPane(Window));
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		// p.add(new JLabel("Type your Message"), BorderLayout.WEST);
		Window.setPreferredSize(new Dimension(20,20));
		Window.setHorizontalAlignment(JTextField.LEFT);
		Window.addActionListener(new TextFieldListener());
		p.add(Window, BorderLayout.CENTER);
		setLayout(new BorderLayout());
		add(p, BorderLayout.SOUTH);
		add(new JScrollPane(textArea), BorderLayout.CENTER);
		setTitle("ChatClient");
		setSize(500, 300);

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				close();
			}
		});
		setVisible(true);
		createMenu();

	}
	private void createMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener((e) ->{close();});
		JMenuItem openItem = new JMenuItem("Connect");
		openItem.addActionListener(new OpenConnectionListener());
		menu.add(openItem);
		menu.add(exitItem);
		menuBar.add(menu);
		this.setJMenuBar(menuBar);
	}

	class OpenConnectionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {

				if(count==0) {
					socket = new Socket("localhost", 9898);
					textArea.append("Connected"+"\n");
					Thread t= new Thread();
					message= new listenMessage();
					System.out.println("New Server connection started");
					count=1;
				}
				else {
					textArea.append("Already connected!!!"+"\n");
				} 
			}catch (IOException e1) {
				e1.printStackTrace();
				textArea.append("connection Failure"+"\n");
			}
		}

	}

	class TextFieldListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {

				toServer = new DataOutputStream(socket.getOutputStream());
			}
			catch (IOException ex) {
				textArea.append(ex.toString() + '\n');
			}

			try {
				String str = Window.getText().trim();
				textArea.append("Me:"+str+"\n");
				toServer.writeUTF(str);
				Window.setText("");
				toServer.flush();
			}
			catch (IOException ex) {
				System.err.println(ex);
			}	    
		}
	}


	public void close() {
		count=0;
		if(socket==null)
			System.exit(0);
		try {

			toServer = new DataOutputStream(socket.getOutputStream());
			toServer.writeUTF("exited");
			exit=false;

			socket.close();
			System.exit(0);
		}catch(Exception e1) {
			System.err.println("error");
		}		 
	}


	class listenMessage extends Thread{

		listenMessage()
		{
			this.start();
		}
		public void run() {
			synchronized (socket){
				while(exit) {
					try {
						fromServer = new DataInputStream(socket.getInputStream());
					}
					catch (IOException e){
						System.err.println(e);
					}
					try {
						String s= fromServer.readUTF();
						textArea.append(s);
						textArea.append("\n");
						Window.setText("");
					}

					catch(IOException ex) {
						textArea.append(ex.toString()+"\n");
					}
				}
			}
		}
	}
	public static void main(String[] args) {
		ChatClient c = new ChatClient();
		c.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		c.setVisible(true);
	}

}