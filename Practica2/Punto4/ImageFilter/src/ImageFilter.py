import cv2
import os

imgPath = '/home/agus/Documents/tierra.jpg'
originalImage = cv2.imread(imgPath)

grayScale = cv2.cvtColor(originalImage, cv2.COLOR_BGR2GRAY)
(thresh, BandW) = cv2.threshold(grayScale, 127, 255, cv2.THRESH_BINARY)
saturated = cv2.cvtColor(originalImage, cv2.COLOR_BGR2HSV).astype("float32")

# Quitamos /tierra.jpg para escribir el resultado en el mismo path
path = os.path.split(imgPath)[0]
cv2.imwrite(os.path.join(path , 'gray.jpg'), grayScale)
cv2.imwrite(os.path.join(path , 'bw.jpg'), BandW)
cv2.imwrite(os.path.join(path , 'saturated.jpg'), saturated)
