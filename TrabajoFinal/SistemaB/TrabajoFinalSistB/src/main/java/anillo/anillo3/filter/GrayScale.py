originalImage = cv2.imread(imgpath)
global n
grayScale = cv2.cvtColor(originalImage, cv2.COLOR_BGR2GRAY)
path = os.path.split(imgpath)[0]
destPath = os.path.join(path, 'filtered' + str(n) + '.jpg')
cv2.imwrite(destPath, grayScale)
n += 1
print destPath
