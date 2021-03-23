import socket
serverSocket=socket.socket(socket.AF_INET,socket.SOCK_STREAM)
serverSocket.bind(("localhost",5500))
print("server is listening on port 5500")
serverSocket.listen()
clientSocket,socketName=serverSocket.accept()
dataBytes=b''
toReceive=100
while len(dataBytes)<toReceive:
    by=clientSocket.recv(toReceive-len(dataBytes))
    dataBytes+=by
requestDataLength=int(dataBytes.decode("utf-8").strip())

dataBytes=b''
toReceive=requestDataLength
fileName=input("Save as : ")

fileName="C:\\Work\\Python\\pyapps\\networking\\uploads\\"+fileName.strip()
file=open(fileName,"wb")
while len(dataBytes)<toReceive:
    by=clientSocket.recv(toReceive-len(dataBytes))
    file.write(by)
    dataBytes+=by
file.close()
clientSocket.close()
