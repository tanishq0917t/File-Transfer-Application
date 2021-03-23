import socket,os
clientSocket=socket.socket(socket.AF_INET,socket.SOCK_STREAM)
clientSocket.connect(("localhost",5500))
fileName=input("File Name :")
fileName.strip()
fileStats=os.stat(fileName)
fileSize=fileStats.st_size
clientSocket.sendall(bytes(str(fileSize).ljust(100),"utf-8"))
file=open(fileName,'rb')
toSend=fileSize
while toSend>0:
    if toSend<1024: l=toSend
    l=file.read(1024)    
    clientSocket.sendall(l)
    toSend-=1024
file.close()
clientSocket.close()


