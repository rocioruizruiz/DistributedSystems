import cv2
import os
import time
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler


n = 0

class MyHandler(FileSystemEventHandler):
    def on_created(self, event):
        print("on_created", event.src_path)
        


    def on_modified(self, event):
        
        # leo archivo request
        if event.src_path[-4:] == '.txt': #no es un directorio
            print("on_modified: ", event.src_path)
            f = open (event.src_path,'r')
            file_lines = f.readlines()
            f.close()
            ultima_linea = file_lines[len(file_lines) -1]
            filtro, path = ultima_linea.split(';')
            print(filtro, path)
            # llamo filter
            dstPath = filter(filtro, path)
            print(dstPath)
            # write en response
            f = open ('/Users/Shared/Response/response.txt','w')
            f.write(dstPath)
            f.close()

def filter(filtro, imgPath):
    originalImage = cv2.imread(imgPath, -1)
    global n
    
    if(filtro == "GrayScale"):
        grayScale = cv2.cvtColor(originalImage, cv2.COLOR_BGR2GRAY)
        path = os.path.split(imgPath)[0]
        destPath = os.path.join(path , 'filtered' + str(n) + '.jpg')
        cv2.imwrite(destPath, grayScale)
        n += 1 
        return destPath
    elif(filtro == "B&W"):
        grayScale = cv2.cvtColor(originalImage, cv2.COLOR_BGR2GRAY)
        (thresh, BandW) = cv2.threshold(grayScale, 127, 255, cv2.THRESH_BINARY)
        path = os.path.split(imgPath)[0]
        destPath = os.path.join(path , 'filtered' + str(n) + '.jpg')
        cv2.imwrite(destPath, BandW)
        n += 1 
        return destPath
    elif(filtro == "Sature"):
        saturated = cv2.cvtColor(originalImage, cv2.COLOR_BGR2HSV).astype("float32")
        path = os.path.split(imgPath)[0]
        destPath = os.path.join(path , 'filtered' + str(n) + '.jpg')
        cv2.imwrite(destPath, saturated)
        n += 1 
        return destPath
    else:
        return "ERROR"

if __name__ == '__main__':
    event_handler = MyHandler()
    observer = Observer()
    observer.schedule(event_handler, path='/Users/Shared/Request/', recursive=True)
    observer.start()

    try:
        while True:
            time.sleep(1)

    except KeyboardInterrupt:
        observer.stop()

    # path = filter("GrayScale", "/Users/Shared/tierra.jpg")
    # print(path)
    
    # path = filter("Sature", "/Users/Shared/filtered1.jpg")
    # print(path)

    # path = filter("B&W", "/Users/Shared/filtered0.jpg")
    # print(path)

# Quitamos /tierra.jpg para escribir el resultado en el mismo path



