package com.dezzy.skrop2_server.net.tcp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.dezzy.skrop2_server.net.NetUtils;
import com.dezzy.skrop2_server.server.GameServer;

/**
 * Facilitates a TCP connection to another device. The Server will send a random 1000-character key to the client and hold all outgoing messages (except "quit" and "timeout") until the client accepts the key
 * with a <code>"key-accepted"</code> message. Before the client accepts the key, traffic is encrypted with {@link #NEGOTIATION_KEY}. 
 * After the client accepts the key, all traffic will be encrypted and decrypted with this new key.
 * 
 * @author Dezzmeister
 *
 */
public class Server implements Runnable {
	
	/**
	 * When a new client connects, this key is used to encrypt/decrypt messages until a new random key is negotiated.
	 */
	private static final String NEGOTIATION_KEY = "GaRZ}:zZO}o%L9<7&LWhNkSA@oPlMJ!&6QpC/+2Hgd_@{wp;0);R.)puQMJ~|:^fBYTs|SibxgR,5*TbPps1RAV)P'oG3XzaMO44`19\\8Rbhp>>M;p}5^qh>se#(TfV5rW7MOaP(;w?/v-DmR`N^rSl(-U)`:.~L%O0a\"DzJLUS`i&HscQ|vHwaZae/,#KG|\"i(z5@9ry=\"G`*l]Fy%^.9H=+.P&D=:j5BTW\"o~_XC(chSgUKh%8-ioyc\"A/~ns\\1*O6gC=irzguy<Ki#!Gq,f<<|V/Wf\\N2'dM0db/$2Kv>blGWf@-/I[kNu5GlD?$e'@EC=UZh{:.|JVt%v-[:9A>S4oqD{[xoI.a?tnHLy|XcVQJF[642SpOQoOKL;T2^YzT/H\\6N'XI]tq\"DgQGUaj0_m|wBFA1E&GCTG:i{9\"siXF\"]X99XJ3sV|xz^[yu>ALS|%,Ky!+_vlBcc[n[nlVDP+<4+9d,s\"Z:2jMvj0PZF&%pq3k)BCX9HU_bn-UWi1Fh0=T{lPz6TQ\"*/m:l=-w8Pt,g,Y#'r#ER;\"q*LJ<OWW$L6ti$]V*1z|q8YZpXBrZRK@MOX-#YvKC\\RiAA]D_[KLv;t${q'JDhk:C%G.1I>NQup>L#[&wwgq*j8M0S=S09,T{tWdyh%Wz{Vt}59bg5`4%ZvSJW]it@7'G8!2'I<O.c{D.I#R~eY%TvLHhU?Z.#O}A<PD;^SK[$`>\"vdgfSw9^60{J%`<~~=2i'+<8\"iV*/2#QqgbR6OWr~b?84i\\O(qs2KZZpJg%#!-JF+T|5W<`qu!6*j&Cs(+F9$<~E)0`:AuwK|M=zjn}Aw*6()dY$!5,:ddw9v+gP1yJ0EnDo%0tt>S?~Upw/`Nu5GRzIW%xwI6m\\3k::Df\"N4h2lF#F7I/C1c\"Dy,0#$apq.Okn7S0GajqZRu=!N2E%+@J|Wu/i4(?lSYYAbHR*',K-;rOecYli95%MMI>0=}(!~Sxmp1-#!X%O[#/O.ol7d@G%Whn%s0MZ#MyQv0jT8fRj\\h<eyQc\\7|5qHg+eTQ[&)MWqkaS81\"0w9@NqUZGnQse/[9cY_c2]4P($_5PKHN\\`$~\\0hl:M'\"PH<$xJ7zF!t&!p'ZI^x%gf.jh[Ri{FS{a2<Ba]%G=!=er4+!U*Z4na'n[y+']h!0O\"mFMzwvp-H%)!QnRl+e4RH_^/@(4RV{ZB|Lh0@%1?aB1[2{6+H,Aj-]ni_y(*5]'zL=/N1Nl>I~)ZaC8qK>o\"2aN6P>q'\"rq9.7$TQHPw5%5{TT:*Hs--EgAR}J;8&-O:Pc4v=^G46#~?oMy?(#Cx$^F(FH4Q[&Tus^0\\U!m2giSkAlwVri^D#-Z2YIuZ`|?2Q(}M@^!J6!e_qX73v'!L<1m[/jHM\"4h%(QT#S%Z2xYRaXkrV\"ZP@W5S1VFNtc:dlT56_l8AgcW<fOwTB/5h'FL}2oVbLJ<yYl*ErAjaI4FBr]5\"XxrB7-sg\"xU\\4mD.<mkgxtG*,|uA\\],_uEt&z2(@h8OwLU6LM<+|DAdq\\oQ\\2;dpD/uov9-EZxw<@xO%.X[oDjR,U@WYBZf:8B\"W+@la}.}){?{4h;K}/bFJ(\"f-Kq=8,8C8O1!&@B,ui5~&C<f:?-<RLWnybYvX,TDL4[<:GBDS}%_Oha0q-!<<F+c#R;4~`mGhHOg6}9_Atx3R2#\"KKot2:yN{AjEyY_l2RZa=xm\"$bQ]lr+~@+L0A%S$&CiO>/lrZh[~{x,8;/l6JBk0`.I^:l`*V*k<?6IjbLmiQ)-!9ct\"Qs^v_$W}N@yjNQ\\@7g5o8O7Bwf5-DqesW)4CU,deM17eVa3l_,}*p*E0u5Q[X+}twY\\GkG4!9-8uv1o^'UI[C^bJ).~aKhGAXO+l9s0iBGddzJ;:&sgzFa=Mzm8R`\"8rwhif&j_cHtP~c660MmhTQ17'-#JKdexm%zO'mk%lg'>!JNVGu6NbbJ<&J@XW$\"S;P/5\"#L;iMrQi:z~)=Zf~f`,m(L+IvV_qY^E|3z:/mV0nzGV9^zz{z?to*9%&'T$P0me&f=u(XbeU>#z$w~[>W~ozE";
	
	private volatile boolean newKeyNegotiated = false;
	private volatile String newKey;
	
	/**
	 * The {@link GameServer} in control of this Server
	 */
	private final GameServer gameServer;
	
	/**
	 * Server identifier
	 */
	private final int clientID;
	
	/**
	 * True if the server should close its current connection
	 */
	private volatile boolean quit = false;
	
	/**
	 * True if the server should continue running
	 */
	private volatile boolean isRunning = true;
	
	private final ServerSocket serverSocket;
	
	/**
	 * Server TCP port
	 */
	public final int port;
	
	/**
	 * Time (in milliseconds) to wait between messages before considering a client to have timed out 
	 */
	private final int timeoutMillis;
	
	/**
	 * Time in milliseconds at which the last message was received from the client, or if no messages have been received yet,
	 * the time at which the client connected
	 */
	private long lastMessageReceived;
	
	/**
	 * A FIFO that contains any messages that need to be sent to the client;
	 * this is better than the old <code>sendMessage</code> flag because multiple messages can wait in a queue instead of destroying any unsent message
	 */
	private final ConcurrentLinkedQueue<String> messageQueue;
	
	/**
	 * Create a TCP server with the specified {@link GameServer}.
	 * 
	 * @param _game GameServer object controlling this Server
	 * @param _port TCP port the server will open on
	 * @param _clientID number to identify the client connected to this server
	 * @param _timeoutMillis number of milliseconds to wait between messages before notifying the game server of a timeout
	 * @throws IOException if the {@link java.net.ServerSocket ServerSocket} cannot be created
	 */
	public Server(final GameServer _game, int _port, int _clientID, int _timeoutMillis) throws IOException {
		gameServer = _game;
		clientID = _clientID;
		port = _port;
		timeoutMillis = _timeoutMillis;
		
		messageQueue = new ConcurrentLinkedQueue<String>();
		
		serverSocket = new ServerSocket(port);
	}
	
	/**
	 * This is set to true when the game tries to send a quit or timeout message while the server is still negotiating a key.
	 */
	private volatile boolean clearQueue = false;
	
	@Override
	public void run() {
		while (isRunning) {
			messageQueue.clear();
			
			try (Socket socket = serverSocket.accept();
					BufferedReader din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					PrintWriter dout = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()))) {
				System.out.println("Client connected to TCP port " + port);
				
				socket.setTcpNoDelay(true);
				
				newKey = NetUtils.getRandomKey(1000);
				sendString("key " + newKey);
				
				String in = "";
				
				lastMessageReceived = System.currentTimeMillis();
				while (!quit || !messageQueue.isEmpty()) {
					
					if (din.ready()) {
						in = din.readLine();
						
						lastMessageReceived = System.currentTimeMillis();
						
						String decrypted = NetUtils.decrypt(in, newKeyNegotiated ? newKey : NEGOTIATION_KEY);
						
						if (decrypted.equals("quit")) {
							quit = true;
							gameServer.processClientEvent(clientID, "quit");
							break;
						}
						
						if (newKeyNegotiated) {
							gameServer.processClientEvent(clientID, decrypted);
						}
						
						if (decrypted.equals("key-accepted")) {
							newKeyNegotiated = true;
						}
					} else {
						
						String message = null;
						
						if (!newKeyNegotiated) {
							if (!messageQueue.isEmpty() && messageQueue.peek().startsWith("key ")) {
								dout.println(NetUtils.encrypt(messageQueue.poll(), NEGOTIATION_KEY));
								dout.flush();
							}
							
							messageQueue.stream().forEach(s -> {
								if (s.equals("quit")) {
									dout.println(NetUtils.encrypt("quit", NEGOTIATION_KEY));
									dout.flush();
									clearQueue = true;
								} else if (s.equals("timeout")) {
									dout.println(NetUtils.encrypt("timeout", NEGOTIATION_KEY));
									dout.flush();
									clearQueue = true;									
								}
							});
							
							if (clearQueue) {
								messageQueue.clear();
								clearQueue = false;
							}
						} else {
							String out = "";
							while ((message = messageQueue.poll()) != null) {
								out += message + "\r\n";
							}
							
							if (!out.isEmpty()) {
								dout.println(NetUtils.encrypt(out.trim(), newKey));
								dout.flush(); //Flush the buffer once instead of for every waiting message, for performance
							}
						}
					}
					
					if (System.currentTimeMillis() - lastMessageReceived > timeoutMillis) {
						if (!quit) {
							gameServer.processClientEvent(clientID, "timeout");
						}
					}
				}
				quit = false;
				newKeyNegotiated = false;
				newKey = null;
				messageQueue.clear();
			} catch (Exception e) {
				System.err.println("Error with TCP server on port " + port + ", processing client quit message");
				e.printStackTrace();
				gameServer.processClientEvent(clientID, "quit");
				messageQueue.clear();
			}
		}
		
		try {
			serverSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the IP of the last client to connect to this server, or the current client if one is connected.
	 * 
	 * @return the last client to connect to this server
	 */
	public InetAddress lastClientIP() {
		return serverSocket.getInetAddress();
	}
	
	/**
	 * Forces the server to close its current connection.
	 */
	public void closeConnection() {
		quit = true;
	}
	
	/**
	 * Stops the server and closes the {@link java.net.ServerSocket ServerSocket}.
	 */
	public void stopServer() {
		quit = true;
		isRunning = false;
	}
	
	/**
	 * Tries to send a String to the client.
	 * 
	 * @param _message String to send, with the newline omitted
	 */
	public void sendString(final String _message) {
		/*
		message = _message;
		sendMessage = true;
		*/
		messageQueue.add(_message);
	}
	
	/**
	 * True if the server is waiting to send a message.
	 * 
	 * @return true if the server needs to send a message
	 */
	public boolean sendingMessage() {
		return !messageQueue.isEmpty();
	}
}
