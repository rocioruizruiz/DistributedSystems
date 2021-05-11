originalImage = cv2.imread(imgPath)
global n
saturated = cv2.cvtColor(originalImage, cv2.COLOR_BGR2HSV).astype("float32")
path = os.path.split(imgPath)[0]
destPath = os.path.join(path, 'filtered' + str(n) + '.jpg')
cv2.imwrite(destPath, saturated)
n += 1
print(destPath)

