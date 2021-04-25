import cv2
import os


n = 0


def filter(filtro, imgPath):
    originalImage = cv2.imread(imgPath)
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
    path = filter("GrayScale", "/Users/Shared/tierra.jpg")
    print(path)
    
    path = filter("Sature", "/Users/Shared/filtered1.jpg")
    print(path)

    path = filter("B&W", "/Users/Shared/filtered0.jpg")
    print(path)

# Quitamos /tierra.jpg para escribir el resultado en el mismo path



