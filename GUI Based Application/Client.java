import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;


class FileUploadEvent
{
private String uploaderId;
private File file;
private long numberOfBytesUploaded;
public FileUploadEvent()
{
this.uploaderId=null;
this.file=null;
this.numberOfBytesUploaded=0;
}

public void setUploaderId(String uploaderId)
{
this.uploaderId=uploaderId;
}
public String getUploaderId()
{
return this.uploaderId;
}
public void setFile(File file)
{
this.file=file;
}
public File getFile()
{
return this.file;
}
public void setNumberOfBytesUploaded(long numberOfBytesUploaded)
{
this.numberOfBytesUploaded=numberOfBytesUploaded;
}
public long getNumberOfBytesUploaded()
{
return numberOfBytesUploaded;
}
}//fileUploadEvent


interface FileUploadListener
{
public void fileUploadStatusChanged(FileUploadEvent fileUploadEvent);
}


class FileModel extends AbstractTableModel
{
private ArrayList<File> files;
FileModel()
{
this.files=new ArrayList<>();
}
public int getRowCount()
{
return this.files.size();
}
public int getColumnCount()
{
return 2;
}
public String getColumnName(int c)
{
if(c==0)return "Sr.No.";
return "Files";
}
public Class getColumnClass(int c)
{
if(c==0) return Integer.class;
return String.class;
}
public boolean isCellEditable(int r,int c)
{
return false;
}
public Object getValueAt(int r,int c)
{
if(c==0) return r+1;
return this.files.get(r).getAbsolutePath();
}
public void addFile(File file)
{
this.files.add(file);
fireTableDataChanged();
}
public ArrayList<File> getFiles()
{
return this.files; 
}
}//


class FTClientFrame extends JFrame
{
private String host;
private int portNumber;
private FileSelectionPanel fileSelectionPanel;
private FileUploadViewPanel fileUploadViewPanel;
private Container container;
FTClientFrame(String host,int portNumber)
{
super("Tanishq's - File Transfer Application");
this.host=host;
this.portNumber=portNumber;
fileSelectionPanel=new FileSelectionPanel();
fileUploadViewPanel=new FileUploadViewPanel();
container=getContentPane();
container.setLayout(new GridLayout(1,2));
container.add(fileSelectionPanel);
container.add(fileUploadViewPanel);
setSize(400,500);
setLocation(100,200);
setVisible(true);
setDefaultCloseOperation(EXIT_ON_CLOSE);
}



class FileSelectionPanel extends JPanel implements ActionListener
{
private JLabel titleLabel;
private JTable table;
private JButton button;
private JScrollPane jsp;
private FileModel model;

FileSelectionPanel()
{
setLayout(new BorderLayout());
titleLabel=new JLabel("Files");
model=new FileModel();
table=new JTable(model);
jsp=new JScrollPane(table,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
button=new JButton("ADD FILES");
button.addActionListener(this);
add(titleLabel,BorderLayout.NORTH);
add(jsp,BorderLayout.CENTER);
add(button,BorderLayout.SOUTH);
}
public void actionPerformed(ActionEvent av)
{
JFileChooser jfc=new JFileChooser();
jfc.setCurrentDirectory(new File("."));
int selectedOption=jfc.showOpenDialog(this);
if(selectedOption==jfc.APPROVE_OPTION)
{
File selectedFile=jfc.getSelectedFile();
model.addFile(selectedFile);
}
}

public ArrayList<File> getFiles()
{
return model.getFiles();
}
}//class FileSelectionPanel



class FileUploadViewPanel extends JPanel implements ActionListener,FileUploadListener
{
ArrayList<FileUploadThread> fileUploaders;
private JButton uploadFileButton;
private JPanel progressPanelsContainer;
private JScrollPane jsp;
private ArrayList<ProgressPanel> progressPanels;
ArrayList<File> files;
public FileUploadViewPanel()
{
uploadFileButton=new JButton("Upload File");
setLayout(new BorderLayout());
add(uploadFileButton,BorderLayout.NORTH);
uploadFileButton.addActionListener(this);
}

public void actionPerformed(ActionEvent ev)
{
files=fileSelectionPanel.getFiles();
if(files.size()==0)
{
JOptionPane.showMessageDialog(FTClientFrame.this,"No files selected to upload..");
return;
}
fileUploaders=new ArrayList<>();
FileUploadThread fut;
String uploaderId;

progressPanelsContainer=new JPanel();
progressPanelsContainer.setLayout(new GridLayout(files.size(),1));
ProgressPanel progressPanel;
progressPanels=new ArrayList<>();
for(File file:files)
{
uploaderId=UUID.randomUUID().toString();
progressPanel=new ProgressPanel(uploaderId,file);
progressPanels.add(progressPanel);
progressPanelsContainer.add(progressPanel);
fut=new FileUploadThread(this,uploaderId,file,host,portNumber);
fileUploaders.add(fut);
}
jsp=new JScrollPane(progressPanelsContainer,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
add(jsp,BorderLayout.CENTER);
this.revalidate();
this.repaint();

for(FileUploadThread fileUploadThread:fileUploaders)
{
fileUploadThread.start();
}
}

public void fileUploadStatusChanged(FileUploadEvent fue)
{
String uploaderId=fue.getUploaderId();
long numberOfBytesUploaded=fue.getNumberOfBytesUploaded();
File file=fue.getFile();

for(ProgressPanel progressPanel:progressPanels)
{
if(progressPanel.getId().equals(uploaderId))
{
progressPanel.updateProgressBar(numberOfBytesUploaded);
break;
}
}
}


class ProgressPanel extends JPanel
{
private File file;
private JLabel fileNameLabel;
private JProgressBar progressBar;
private long fileLength;
private String id;
public ProgressPanel(String id,File file)
{
this.id=id;
this.file=file;
this.fileLength=file.length();
fileNameLabel=new JLabel("Uploading..."+file.getAbsolutePath());
progressBar=new JProgressBar(1,100);
setLayout(new GridLayout(2,1));
add(fileNameLabel);
add(progressBar);
}
public String getId()
{
return this.id;
}
long bytes=0;
public void updateProgressBar(long bytesUploaded)
{
int percentage;
if(bytes==fileLength)
{
percentage=100;
this.fileNameLabel.setText("Uploaded");
}else
{
bytes+=bytesUploaded;
percentage=(int)((bytes*100)/fileLength);
progressBar.setValue(percentage);
if(percentage==100)
{
fileNameLabel.setText("Uploaded..."+file.getAbsolutePath());
}//if
}//else
}//updateProgressBar
}//class ProgressPanel

}//class uploadViewPanel

public static void main(String gg[])
{
FTClientFrame fcf=new FTClientFrame("localhost",5500);
}
}//FTClientFrame


class FileUploadThread extends Thread
{
private FTClientFrame fcf;
private String id;
private File file;
private String host;
private int portNumber;
private FileUploadListener fileUploadListener;

FileUploadThread(FileUploadListener fileUploadListener,String id,File file,String host,int portNumber)
{
this.fcf=fcf;
this.host=host;
this.portNumber=portNumber;
this.file=file;
this.id=id;
this.fileUploadListener=fileUploadListener;
}
public void run()
{
try
{
byte header[]=new byte[1024];
String fileName=file.getName();
long lengthOfFile=file.length();
int x,i;
long y,z;
y=lengthOfFile;
i=0;
while(y>0)
{
header[i]=(byte)(y%10);
y=y/10;
i++;
}

System.out.println("Lenght of file inserted into header");
header[i]=(byte)',';
i++;
x=0;
while(x<fileName.length())
{
header[i]=(byte)fileName.charAt(x);
x++;
i++;
}
while(i<1024)
{
header[i]=(byte)32;
i++;

}

System.out.println("Name of file inserted into header");

Socket socket=new Socket(host,portNumber);
InputStream is=socket.getInputStream();
OutputStream os=socket.getOutputStream();
os.write(header,0,1024);
os.flush();

byte ack[]=new byte[1];
int bytesToRead=0;
while(true)
{
bytesToRead=is.read(ack);
if(bytesToRead==-1) continue;
break;
}
System.out.println("Acknowledgement recieved");

int chunkSize=4096;
byte bytes[]=new byte[chunkSize];
//FileOutputStream fos=new FileOutputStream(file);
FileInputStream fis=new FileInputStream(file);
y=0;
z=0;
long j=0;

while(j<lengthOfFile)
{
bytesToRead=fis.read(bytes);
if(bytesToRead==-1) continue;
os.write(bytes,0,bytesToRead);
os.flush();
j+=bytesToRead;
long brc=bytesToRead;
SwingUtilities.invokeLater(()->{
FileUploadEvent fue=new FileUploadEvent();
fue.setUploaderId(id);
fue.setNumberOfBytesUploaded(brc);
fue.setFile(file);
fileUploadListener.fileUploadStatusChanged(fue);
});
}
System.out.println("file transfered");
fis.close();

while(true)
{
bytesToRead=is.read(ack);
if(bytesToRead==-1) continue;
break;
}
System.out.println("Acknowledgement Recieved");
socket.close();

}catch(Exception e)
{
System.out.println(e);
}
}
}
