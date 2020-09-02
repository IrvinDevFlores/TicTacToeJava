
package servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.LinkedList;


public class ServerThread implements Runnable{
    //Declaramos las variables que utiliza el hilo para estar recibiendo y mandando mensajes
    private Socket socket;

    private DataOutputStream out;
    private DataInputStream in;
    //Varible para guardar que le toco al jugador X o O
    private int XO;
    //Matriz del juego
    private int G[][];
    //Turno
    private boolean turno;
    //Lista de los usuarios conectados al servidor
    private LinkedList<Socket> usuarios = new LinkedList<Socket>();
    
    //Constructor que recibe el socket que atendera el hilo y la lista de los jugadores el turno y la matriz del juego
    public ServerThread(Socket soc, LinkedList users, int xo, int[][] Gato){
        socket = soc;
        usuarios = users;
        XO = xo;
        G = Gato;
    }
    
    
    @Override
    public void run() {
        try {
            //Inicializamos los canales de comunicacion y mandamos el turno a cada jugador
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            turno = XO == 1;
            String msg = "";
            msg += "JUEGAS: " + (turno ? "X;":"O;");
            msg += turno;
            out.writeUTF(msg);
            
            boolean isRunning = true;
            while(isRunning){
                //Leer los datos que se mandan cuando se selecciona un boton
                String recibidos = in.readUTF();
                String recibido [] = recibidos.split(";");

                int filaTablero = recibido[0];
                int columnaTablero = recibido[1];

                int f = Integer.parseInt(filaTablero);
                int c = Integer.parseInt(columnaTablero);

                G[f][c] = XO;

                String mensajeParaJugadores = "";
                mensajeParaJugadores += XO+";";
                mensajeParaJugadores += f+";";
                mensajeParaJugadores += c+";";

                boolean ganador = HaGanado(XO);
                boolean completo = TableroEstaLleno();
                
                if(!ganador && !completo){
                    mensajeParaJugadores += "NADIE";
                }
                else if(!ganador && completo){
                    mensajeParaJugadores += "EMPATE";
                }
                else if(ganador){
                    vaciarMatriz();
                    mensajeParaJugadores += XO == 1 ? "X":"O";
                }
                
                
                
                for (Socket usuario : usuarios) {
                    out = new DataOutputStream(usuario.getOutputStream());
                    out.writeUTF(mensajeParaJugadores);
                }
            }
        } catch (Exception e) {
            
            //Si ocurre un excepcion lo mas seguro es que sea por que algun jugador se desconecto asi que lo quitamos de la lista de conectados
            for (int i = 0; i < usuarios.size(); i++) {
                if(usuarios.get(i) == socket){
                    usuarios.remove(i);
                    break;
                } 
            }
            vaciarMatriz();
        }
    }
    
    //Funcion comprueba si algun jugador ha ganado el juego
    public boolean HaGanado(int n){
        for (int i = 0; i < 3; i++) {
            boolean isWinner = true;
            for (int j = 0; j < 3; j++) {
                 isWinner = isWinner && (G[i][j] == n);
            }
            if(isWinner){
                return true;
            }
        }
        
        for (int i = 0; i < 3; i++) {
            boolean gano = true;
            for (int j = 0; j < 3; j++) {
                 gano = gano && (G[j][i] == n); 
            }
            if(gano){
                return true;
            }
        }
        
        if(G[0][0] == n && G[1][1] == n && G[2][2] == n)return true;
        
        return false;
    }
    
    //Funcion comprueba si el tablero ya esta lleno
    public boolean TableroEstaLleno(){
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if(G[i][j] == -1)return false;
            }
        }
        
        vaciarMatriz();
        return true;
    }
    
    //Funcion para reiniciar la matriz del juego
    public void vaciarMatriz(){
        for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    G[i][j] = -1;
                }
        }
    }
}
