import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
class RequestProcessor extends Thread
{
private Socket socket;
private String id;
FTServerFrame fsf;
RequestProcessor(Socket socket,String id,FTServerFrame fsf)
{
this.socket=socket;
this.id=id;
this.fsf=fsf;
start();
}
public void run()
{
try
{
SwingUtilities.invokeLater(new Runnable(){
public void run()
{
fsf.updateLog("Client connected and id alloted is: "+id);
}
});
InputStream is=socket.getInputStream();
OutputStream os=socket.getOutputStream();
int bytesToReceive=1024;
byte temp[]=new byte[1024];
byte header[]=new byte[1024];
int bytesReadCount=0;
int i,j,k;
i=0;
j=0;
while(j<bytesToReceive)
{
bytesReadCount=is.read(temp);
if(bytesReadCount==-1)continue;
for(k=0;k<bytesReadCount;k++)
{
header[i]=temp[k];
i++;
}
j=j+bytesReadCount;
}
long fileLength=0;
i=0;
j=1;
while(header[i]!=',')
{
fileLength=fileLength+(header[i]*j);
j=j*10;
i++;
}
i++;
StringBuffer buffer=new StringBuffer();
while(i<=1023)
{
buffer.append((char)header[i]);
i++;
}
String fileName=buffer.toString().trim();
long lof=fileLength;
SwingUtilities.invokeLater(()->{
fsf.updateLog("Receiving file: "+fileName+" of length "+lof);
});
System.out.println("Receiving File: "+fileName+" of length: "+fileLength);
File file=new File("uploads"+File.separator+fileName);
if(file.exists())file.delete();
FileOutputStream fos=new FileOutputStream(file);
byte ack[]=new byte[1];
ack[0]=1;
os.write(ack,0,1);
os.flush();
int chunksize=4096;
byte bytes[]=new byte[chunksize];
i=0;
long m=0;
while(m<fileLength)
{
bytesReadCount=is.read(bytes);
if(bytesReadCount==-1)continue;
fos.write(bytes,0,bytesReadCount);
fos.flush();
m+=bytesReadCount;
}
fos.close();
ack[0]=1;
os.write(ack,0,1);
os.flush();
System.out.println("File Saved\nFile Path: "+file.getAbsolutePath());
socket.close();
SwingUtilities.invokeLater(()->{
fsf.updateLog("File saved to "+file.getAbsolutePath());
fsf.updateLog("Connection with client whose id is: "+id+" closed");
});
}catch(Exception e)
{
System.out.println(e);
}
}//run ends
}//class ends


class FTServer extends Thread
{
private ServerSocket serverSocket;
private FTServerFrame fsf;
FTServer(FTServerFrame fsf)
{
this.fsf=fsf;
}
public void run()
{
try
{
serverSocket=new ServerSocket(5500);
startListening();
}catch(Exception e)
{
System.out.println(e);
}
}
public void shutDown()
{
try
{
serverSocket.close();
}catch(Exception e)
{
System.out.println(e);
}
}
private void startListening()
{
try
{
Socket socket;
RequestProcessor rp;
while(true)
{
System.out.println("Server is ready to accept request");
SwingUtilities.invokeLater(new Thread(){
public void run()
{
fsf.updateLog("Server started and is listening on port 5500");
}
});
socket=serverSocket.accept();
rp=new RequestProcessor(socket,UUID.randomUUID().toString(),fsf);
}
}catch(Exception e)
{
System.out.println("Server stopped listening");
System.out.println(e);
}
}//start listening ends
}//class ends
class FTServerFrame extends JFrame implements ActionListener
{
private FTServer server;
private JButton button;
private Container container;
private JTextArea jta;
private JScrollPane jsp;
private boolean serverState=false;
FTServerFrame()
{
super("Tanishq's Server");
container=getContentPane();
container.setLayout(new BorderLayout());
button=new JButton("Start");
jta=new JTextArea();
jsp=new JScrollPane(jta,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
container.add(jsp,BorderLayout.CENTER);
container.add(button,BorderLayout.SOUTH);
setLocation(100,100);
setSize(500,500);
setVisible(true);
setDefaultCloseOperation(EXIT_ON_CLOSE);
button.addActionListener(this);
}
public void updateLog(String message)
{
jta.append(message+"\n");
}
public void actionPerformed(ActionEvent ev)
{
if(serverState==false)
{
server=new FTServer(this);
server.start();
serverState=true;
button.setText("Stop");
}
else
{
server.shutDown();
serverState=false;
button.setText("Start");
jta.append("Server Stopped\n");
}
}

public static void main(String gg[])
{
FTServerFrame fssf=new FTServerFrame();
}//main ends
}
